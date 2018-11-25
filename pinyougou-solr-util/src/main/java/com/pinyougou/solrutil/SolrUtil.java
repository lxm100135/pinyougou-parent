package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;


@Component
public class SolrUtil {
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private SolrTemplate solrTemplate;
	//导入item数据
	public void importItemData() {
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//审核通过的才导入
		List<TbItem> listItem = itemMapper.selectByExample(example);
		
		System.out.println("---商品列表---");
		for (TbItem item : listItem) {
			System.out.println(item.getId()+" "+item.getTitle()+" "+item.getPrice());
			Map map = JSON.parseObject(item.getSpec(), Map.class);//数据库中取出规格的json字符串，转为map
			item.setSpecMap(map);
		}
		System.out.println("---结束---");
		solrTemplate.saveBeans(listItem);
		solrTemplate.commit();
	}
	/*
	 * classpath:spring/applicationContext.xml
	 * 		只能找到solrutil下的applicationContext.xml
	 * classpath*:spring/applicationContext*.xml
	 * 		才能找到dao下的applicationContext-dao.xml
	 */
	public static void main(String[] args) {//加载到spring中
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");//默认bean的name首字母小写
		solrUtil.importItemData();
	}
}
