package com.pinyougou.search.service.impl;

import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.search.service.ItemSearchService;
@Component
public class ItemDeleteListener implements MessageListener {
	@Autowired
	private ItemSearchService itemSearchService;
	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			Long[] ids = (Long[]) objectMessage.getObject();//取出ids
			System.out.println("监听获取到消息："+ids);
			itemSearchService.deleteByGoodsIds(Arrays.asList(ids));//删除solr索引库
			System.out.println("删除对应solr索引库");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
