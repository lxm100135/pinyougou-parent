package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pingou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;


@RestController
@RequestMapping("/pay")
public class PayController {
	@Reference
	private WeixinPayService weixinPayService;
	@Reference
	private SeckillOrderService seckillOrderService;
	/**
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map createNative() {
//		IdWorker idWorker=new IdWorker();
//		long out_trade_no = idWorker.nextId();
		//1获取当前登录名
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		//2从缓存中提取秒杀订单
		TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
		if (seckillOrder!=null) {
			//3调用微信支付接口
			return weixinPayService.createNative(seckillOrder.getId()+"", (long)(seckillOrder.getMoney().doubleValue()*100)+"");
		}else {
			return new HashMap();
		}
	}
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		//获取用户
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		Result result = null;
		int x = 0;
		while (true) {
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);//调用查询
			if (map==null) {
				result = new Result(false, "支付发生错误");
				break;
			}
			if (map.get("trade_state").equals("SUCCESS")) {
				result = new Result(true, "支付成功");
//				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));//修改订单状态
				seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
				break;
			}
			try {
				Thread.sleep(3000);//休息3s再继续
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			x++;
			if (x>=100) {//大概5分钟
				//关闭微信支付(可能会有时差)
				Map<String,String> map2 = weixinPayService.closePay(out_trade_no);
				if (map2.get("result_code").equals("FAIL") ) {
					if (map2.get("err_code").equals("ORDERPAID")) {//订单已支付
						result = new Result(true, "支付成功");
						seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
						break;
					}
				}
				result = new Result(false, "二维码超时");
				//订单取消库存回退
				seckillOrderService.deleteOrderFromRedisToDb(userId, Long.valueOf(out_trade_no));
				
				break;
			}
		}
		return result;
	}
}
