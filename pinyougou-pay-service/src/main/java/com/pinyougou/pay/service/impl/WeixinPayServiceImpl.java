package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pingou.pay.service.WeixinPayService;

import util.HttpClient;
@Service
public class WeixinPayServiceImpl implements WeixinPayService{
	
	@Value("${appid}")
	private String appid;
	@Value("${partner}")
	private String partner;
	@Value("${partnerkey}")
	private String partnerkey;
	@Value("${notifyurl}")
	private String notifyurl;
	
	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		//1.参数封装
		Map param = new HashMap();
		param.put("appid", appid);//公众账号ID
		param.put("mch_id", partner);//商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
//		param.put("sign", "");//签名sdk自动生成
		param.put("body", "旅行喵壹号");//商品描述
		param.put("out_trade_no", out_trade_no);//商户订单号
		param.put("total_fee", total_fee);//标价金额（分）
		param.put("spbill_create_ip", "127.0.0.1");//终端IP
		param.put("notify_url", notifyurl);//回调地址、可以随便写、必须给但用不到
		param.put("trade_type", "NATIVE");//交易类型、本地交易
		
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);//MAP转换为XML字符串（自动添加签名）
			System.out.println("请求的参数："+xmlParam);
			//2.发送请求
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();
			//3.获取结果
			String xmlContent = httpClient.getContent();
			Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlContent);
			System.out.println("微信收到信息："+mapResult);
			Map map = new HashMap<>();
			map.put("code_url", mapResult.get("code_url"));//生成支付二维码链接
			map.put("out_trade_no", out_trade_no);//商户订单号
			map.put("total_fee", total_fee);//金额
			
			return map;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Map queryPayStatus(String out_trade_no) {
		//1.封装参数
		Map param = new HashMap<>();
		param.put("appid", appid);//公众账号ID
		param.put("mch_id", partner);//商户号
		param.put("out_trade_no", out_trade_no);//商户订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			//2.发送请求
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();
			//3.获得结果
			String xmlContent = httpClient.getContent();
			System.out.println("得到结果："+xmlContent);
			Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlContent);
			return mapResult;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

	@Override
	public Map closePay(String out_trade_no) {
		//1封装参数
		Map param = new HashMap();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		
		try {
			//2提交参数
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println("请求的参数："+xmlParam);
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();
			//3获取参数
			String xmlContent = httpClient.getContent();
			System.out.println("从微信获取的数据："+xmlContent);
			Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlContent);
			return mapResult;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
}
