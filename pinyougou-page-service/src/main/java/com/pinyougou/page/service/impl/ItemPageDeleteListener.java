package com.pinyougou.page.service.impl;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;
/**
 * 监听删除商品详情页
 * @author Lxm_pc
 *
 */
@Component
public class ItemPageDeleteListener implements MessageListener {
	@Autowired
	private ItemPageService itemPageService;
	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			Long[] goodsIds = (Long[]) objectMessage.getObject();
			System.out.println("接收到信息："+goodsIds);
			boolean deleteItemHtml = itemPageService.deleteItemHtml(goodsIds);
			System.out.println("删除成功："+deleteItemHtml);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
