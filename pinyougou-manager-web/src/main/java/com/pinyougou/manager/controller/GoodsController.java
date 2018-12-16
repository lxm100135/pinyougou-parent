package com.pinyougou.manager.controller;
import java.util.Arrays;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
//import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
//import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@Autowired
	private Destination queuesolrDeleteDestination;
	@Autowired
	private Destination topicPageDeleteDestination;
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
		//数据库中删除
			goodsService.delete(ids);
		//索引库中删除
//			itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
//			String jsonString = JSON.toJSONString(ids);
			jmsTemplate.send(queuesolrDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
//					return session.createTextMessage(jsonString);//也可以
					return session.createObjectMessage(ids);//ids是一个序列化对象
				}
			});
		//删除每个服务器上的商品详情页
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);//ids是一个序列化对象;
				}
			});
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	/**
	 * 更改审核状态
	 * @param ids
	 * @param status
	 * @return
	 */
//	@Reference
//	ItemSearchService itemSearchService;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination queuesolrDestination;
	@Autowired
	private Destination topicPageDestination;
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status) {
		try {
			goodsService.updateStatus(ids, status);//往下就审核成功
			if ("1".equals(status)) {//商家已上架商品
				//*****导入到索引库
				//得到需要导入的SKU列表
				List<TbItem> itemList = goodsService.findItemListByGoodsIdListAndStatus(ids, status);
				//存储到solr
//				itemSearchService.importList(itemList);
				String jsonList = JSON.toJSONString(itemList);//转换成json
				jmsTemplate.send(queuesolrDestination, new MessageCreator() {
					
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(jsonList);
					}
				});
				//生成商品详情页
				for (long goodsId : ids) {
//					itemPageService.genItemHtml(id);
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createObjectMessage(goodsId);
						}
					});
				}
			}
			return new Result(true, "更改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "更改失败");
		}
		
	}
//	@Reference(timeout=5000)
//	private ItemPageService itemPageService;
	//商品详情测试
//	@RequestMapping("/genHtml")
//	public boolean genHtml(Long goodsId) {
//		return itemPageService.genItemHtml(goodsId);
//	}
}
