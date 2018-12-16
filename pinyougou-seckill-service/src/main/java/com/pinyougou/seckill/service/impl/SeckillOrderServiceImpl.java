package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.util.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import util.HttpClient;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {
	

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
		@Autowired
		private RedisTemplate redisTemplate;
		@Autowired
		private TbSeckillGoodsMapper seckillGoodsMapper;
		@Autowired
		private IdWorker idWorker;
		@Override
		public void submitOrder(Long seckillId, String userId) {
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
			if (seckillGoods==null) {
				throw new RuntimeException("商品不存在或已抢空！");
			}
			if (seckillGoods.getStockCount()<=0) {
				throw new RuntimeException("商品已经抢光！");
			}
			//1减库存并设置库存
			seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
			//2更新缓存库存
			redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
			//3当库存为0时，更新数据库,并删除缓存
			if (seckillGoods.getStockCount()==0) {
				//更新数据库
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
				//删除缓存
				redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
				System.out.println("商品同步到数据库");
			}
			//4存储订单（存入缓存，不存到数据库，支付完成才存入数据库）
			TbSeckillOrder order = new TbSeckillOrder();
			order.setId(idWorker.nextId());
			order.setCreateTime(new Date());
			order.setUserId(userId);
			order.setMoney(seckillGoods.getCostPrice());
			order.setSeckillId(seckillId);
			order.setSellerId(seckillGoods.getSellerId());
			
			redisTemplate.boundHashOps("seckillOrder").put(userId, order);
			System.out.println("保存订单到缓存");
		}

		@Override
		public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
			
			return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		}

		@Override
		public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
			TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
			if (seckillOrder==null) {
				throw new RuntimeException("订单不存在");
			}
			if (seckillOrder.getId().longValue()!=orderId.longValue()) {
				throw new RuntimeException("订单号不符");
			}else {
				seckillOrder.setPayTime(new Date());
				seckillOrder.setStatus("1");//已支付
				seckillOrder.setTransactionId(transactionId);
				//订单存入数据库并清理缓存
				seckillOrderMapper.insert(seckillOrder);
				redisTemplate.delete(userId);
			}
		}
		
		@Override
		public void deleteOrderFromRedisToDb(String userId, Long orderId) {
			//1查询缓存中订单
			TbSeckillOrder seckillOrder = (TbSeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);
			if (seckillOrder!=null) {
				//2删除订单
				redisTemplate.boundHashOps("seckillOrder").delete("userId");
				//3商品库存回退
				TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
				if (seckillGoods!=null) {
					
					seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
					redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getSellerId(),seckillGoods);
				}else {
					seckillGoods = new TbSeckillGoods();
					seckillGoods.setId(seckillOrder.getSeckillId());
					seckillGoods.setCostPrice(seckillOrder.getMoney());
					seckillGoods.setIntroduction(seckillGoods.getIntroduction());
					seckillGoods.setNum(1);
//					*********还有其他属性
					redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getSellerId(),seckillGoods);
				}
				System.out.println("订单取消："+orderId);
			}
		}
		
	
}
