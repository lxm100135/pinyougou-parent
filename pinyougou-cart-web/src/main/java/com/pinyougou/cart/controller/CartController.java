package com.pinyougou.cart.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {
	@Reference
	private CartService cartService;
	@Autowired
	HttpServletRequest request;
	@Autowired
	HttpServletResponse response;

	/**
	 * 获取购物车
	 * 
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		// 获取当前用户名
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("userName:" + userName);

		List<Cart> cartList = new ArrayList<Cart>();
		// 从cookie提取购物车
		String jsonString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if (jsonString == null || jsonString.equals("")) {
			jsonString = "[]";// 防止JSON.parseArray报错
		}
		List<Cart> cartList_cookie = JSON.parseArray(jsonString, Cart.class);
		try {

			if (userName.equals("anonymousUser")) {// 未登录、从cookie提取购物车
				cartList = cartList_cookie;
				System.out.println("从cookie提取购物车");
			} else { // 已登录、合并购物车

				// 从redis中提取购物车、
				List<Cart> cartList_redis = cartService.findCartListFromRedis(userName);
				System.out.println("从redis提取购物车");
				if (cartList_cookie.size() > 0) {
					// 合并购物车
					cartList = cartService.mergeCartList(cartList_redis, cartList_cookie);
					System.out.println("合并购物车:" + userName);
					// 存入购物车
					cartService.saveCartListToRedis(userName, cartList);
					// 清空cookie
					util.CookieUtil.deleteCookie(request, response, "cartList");
					System.out.println("清空购物车:" + userName);
				} else {
					cartList = cartList_redis;
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cartList;
	}
//	@CrossOrigin(origins="http://localhost:9105")//可以替代response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
	@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")//替代下面两句
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId, int num) {
		//设置允许源头信息
		//response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//(当此方法不需要操作cookie,只用只一句就够了)
//		response.setHeader("Access-Control-Allow-Origin", "*");//允许所有请求
		//response.setHeader("Access-Control-Allow-Credentials", "true");//允许使用cookie
		
		// 获取当前用户名
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("userName:" + userName);
		try {
			// 1.读取购物车
			List<Cart> cartList = findCartList();
			// 2.调用服务方法操作购物车
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			if (userName.equals("anonymousUser")) {
				// 将购物车存入cookie
				String cartListString = JSON.toJSONString(cartList);
				util.CookieUtil.setCookie(request, response, "cartList", cartListString, 24 * 60 * 60 * 60, "UTF-8");
			} else {
				// 将购物车存入redis
				cartService.saveCartListToRedis(userName, cartList);
			}

			return new Result(true, "操作成功！");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "操作失败！");
		}
	}
}
