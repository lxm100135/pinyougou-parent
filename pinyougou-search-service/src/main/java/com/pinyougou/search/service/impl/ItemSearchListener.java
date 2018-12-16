package com.pinyougou.search.service.impl;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
//有包扫描：<dubbo:annotation package="com.pinyougou.search.service.impl" /> 
@Component//一定要写，因为（配置文件中没有配置bean,ref="itemSearchListener"）
public class ItemSearchListener implements MessageListener {
	@Autowired
	private ItemSearchService itemSearchService;
	@Override
	public void onMessage(Message message) {
		
		TextMessage textMessage = (TextMessage) message;//收到消息
		try {
			String text = textMessage.getText();//manage-web发来的json字符串
			System.out.println("监听到消息："+text);
			List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
			itemSearchService.importList(itemList);//导入列表到solr索引库
			System.out.println("导入列表到solr索引库");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
