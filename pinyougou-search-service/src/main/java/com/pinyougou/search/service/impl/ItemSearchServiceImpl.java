package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.search.service.ItemSearchService;
@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService{
	@Autowired
	private SolrTemplate solrTemplate;
	
	@Override
	public Map search(Map searchMap) {
		Map map = new HashMap();
		String keywordsStr = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywordsStr.replace(" ", ""));//关键字去掉空格
//		1.查询列表
		map.putAll(searchList(searchMap));//将一个已有Map中的数据压入另一个Map中，且去重。
//		2.分组查询商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
//		3.品牌列表和规格列表
		if (searchMap.get("category")!="") {
			map.putAll(searchBrandAndSpecList((String) searchMap.get("category")));
		}else {
			if (categoryList!=null) {
//				Map brandAndSpecList = searchBrandAndSpecList(categoryList.get(0));
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}

		return map;
	}
	
	//查询列表
	//返回map比list更加灵活，满足复杂需求（传入参数的多元化、传出参数的多元化）
	
	private Map searchList(Map searchMap) {
		Map map = new HashMap();
//		Query query = new SimpleQuery("*:*");
		/*
		 *is：匹配
		 * 根据复制域查询
		 * 因为经过分词，可以使用匹配，不用包含
		 */
//		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//		query.addCriteria(criteria );
//		ScoredPage<TbItem> page = solrTemplate.queryForPage(query , TbItem.class);
//		
//		map.put("rows", page.getContent());
		
		
//		********查询条件**********
		
		//高亮选项初始化
		HighlightQuery query = new SimpleHighlightQuery();
		//addField("item_title")在哪一列添加高亮
		//构建高亮选项对象
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
		highlightOptions.setSimplePostfix("</em>");//后缀
		//为查询对象设置高亮选项
		query.setHighlightOptions(highlightOptions);
		
		//1.1关键字查询
		//根据复制域查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//1.2按照商品分类过滤
		if (!"".equals(searchMap.get("category"))) {//不是空字符串的情况下
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria );
			query.addFilterQuery(filterQuery);
		}
		//1.3按照品牌过滤
		if (!"".equals(searchMap.get("brand"))) {//不是空字符串的情况下
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria );
			query.addFilterQuery(filterQuery);
		}
		//1.4按照规格过滤
		if (searchMap.get("spec")!=null) {//不为空的情况下
			Map<String, String> specMap = (Map) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria );
				query.addFilterQuery(filterQuery);
			}
		}
//		1.5按照价格过滤
		if (!searchMap.get("price").equals("")) {
			String priceStr = (String) searchMap.get("price");
			String[] prices = priceStr.split("-");
			if (!prices[0].equals("0")) {//如果最低价格不等于0
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
				filterQuery.addCriteria(filterCriteria );
				query.addFilterQuery(filterQuery);
			}
			if (!prices[1].equals("*")) {//如果最高价格不等于*
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
				filterQuery.addCriteria(filterCriteria );
				query.addFilterQuery(filterQuery);
			}
		}
//		1.6分页
		Integer pageNo = (Integer) searchMap.get("pageNo");//页码
		if (pageNo==null) {
			pageNo=1;
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
		if (pageSize==null) {
			pageSize=20;
		}
		query.setOffset((pageNo-1)*pageSize);//开始索引
		query.setRows(pageSize);//每页记录数
		
//		1.7排序
		String sortvalue = (String) searchMap.get("sort");//升序还是降序升序ASC降序DESC
		String sortField = (String) searchMap.get("sortField");//根据哪个字段（价格还是销售量）
		if (sortvalue!=null && !sortvalue.equals("")) {
			if (sortvalue.equals("ASC")) {
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort );
			}if(sortvalue.equals("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort );
			}
		}
		
//		*********获取高亮结果集**********
		//返回一个高亮页对象
		HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
		
		//高亮入口集合(每条记录的高亮入口)
		List<HighlightEntry<TbItem>> entryList = highlightPage.getHighlighted();
		
		for (HighlightEntry<TbItem> entry : entryList) {
			//获取高亮列表addField("item_title")添加多个字段时这里就有多个
			//每有一个高亮列就有一个高亮对象Highlight
			List<Highlight> highlighList = entry.getHighlights();
			/*
			for (Highlight highlight : highlighList) {
				//每个域可能存储多值（<field name="item_keywords" type="text_ik" indexed="true" stored="false" multiValued="true"/>）
				List<String> snipplets = highlight.getSnipplets();
				System.out.println(snipplets);
			}*/
			//我们只有一个字段且每个字段只有一个值，可以这样
			if (highlighList.size()>0 && highlighList.get(0).getSnipplets().size()>0) {
				TbItem item = entry.getEntity();
				item.setTitle(highlighList.get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", highlightPage.getContent());
		map.put("totalPages", highlightPage.getTotalPages());//总页数
		map.put("totalElements", highlightPage.getTotalElements());//总条数
		return map;
	}
	//分组查询
	private List<String> searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList();
		Query query = new SimpleQuery("*:*");
		//根据关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria );//相当于where（条件）
		//设置分组选项（可以多个addGroupByField）
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions );//相当于group by（通过哪个字段）
		//分组页（多个addGroupByField的结果）
		GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query , TbItem.class);
		/*
		 * groupPage.getContent();返回的是一个空集合用不到，这是这个接口中一个不得已的方法，因为父接口中存在这个方法
		 */
		//分组结果对象(参数必须是分组选项出现过的域，单独的域)
		GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
		//获取分组入口页（是一个对象）
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//获取分组入口集合（是一个集合）
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : entryList) {
			list.add(entry.getGroupValue());//分组结果添加到返回值中
		}
		return list;
	}
	@Autowired
	RedisTemplate redisTemplate;
	/**
	 *  返回品牌列表和规格列表
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap ();
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (typeId!=null) {
			//品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);
			//规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List goodsIds) {
		Query query=new SimpleQuery("*:*");		
		Criteria criteria=new Criteria("item_goodsId").in(goodsIds);
		query.addCriteria(criteria);		
		solrTemplate.delete(query);
		solrTemplate.commit();
		
	}

}
