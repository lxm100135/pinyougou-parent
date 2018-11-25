package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
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
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService{
	@Autowired
	private SolrTemplate solrTemplate;
	
	@Override
	public Map search(Map searchMap) {
		Map map = new HashMap();
//		1.查询列表
		map.putAll(searchList(searchMap));//将一个已有Map中的数据压入另一个Map中，且去重。
//		2.分组查询商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
//		3.品牌列表和规格列表
		if (categoryList!=null) {
//			Map brandAndSpecList = searchBrandAndSpecList(categoryList.get(0));
			map.putAll(searchBrandAndSpecList(categoryList.get(0)));
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
		
		//高亮显示
		HighlightQuery query = new SimpleHighlightQuery();
		//addField("item_title")在哪一列添加高亮
		//构建高亮选项对象
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
		highlightOptions.setSimplePostfix("</em>");//后缀
		//为查询对象设置高亮选项
		query.setHighlightOptions(highlightOptions);
		
		//关键字查询
		//根据复制域查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
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

}
