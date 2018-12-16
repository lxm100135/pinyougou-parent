package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

@Component
public class SeckillTask {
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Scheduled(cron="0 * * * * ?")//每分钟执行一次
	public void refreshSeckillGoods() {
		System.out.println("执行了秒杀商品增量更新 任务调度："+new Date());
		
		//得到缓存中商品key集合
		List keys = new ArrayList<>(redisTemplate.boundHashOps("seckillGoods").keys());
		System.out.println(keys);
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//审核通过商品
		criteria.andStartTimeLessThan(new Date());//开始时间小于当前时间
		criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
		if (keys.size()>0) {
			criteria.andIdNotIn(keys);//缓存中没有的ids
		}
		List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example );
		//商品数据放入缓存
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			System.out.println("增量更新秒杀商品ID："+seckillGoods.getId());
		}
		System.out.println("执行了秒杀商品增量更新 任务调度：END*************");
		
	}
	@Scheduled(cron="* * * * * ?")//每秒执行一次
	public void removeSeckillGoods() {
		System.out.println("执行了移除过期商品任务");
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			//超时
			if (seckillGoods.getEndTime().before(new Date())) {
				//同步到数据库
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
				//从缓存中移除
				redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
				System.out.println("移除秒杀过期商品："+seckillGoods.getId());
			}
		}
		System.out.println("执行了移除过期商品任务 END**********");
	}
}
