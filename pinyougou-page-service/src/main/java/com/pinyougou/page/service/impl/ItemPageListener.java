package com.pinyougou.page.service.impl;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;
/**
 * 监听类用于生成商品详情页
 * @author Lxm_pc
 *
 */
@Component
public class ItemPageListener implements MessageListener {
	@Autowired
	private ItemPageService itemPageService;
	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			Long goodsId = (Long) objectMessage.getObject();
			System.out.println("接收到消息："+goodsId);
			boolean genItemHtml = itemPageService.genItemHtml(goodsId);
			System.out.println("商品详情页生成："+genItemHtml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
