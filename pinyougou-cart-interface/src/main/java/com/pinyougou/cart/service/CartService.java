package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojogroup.Cart;

public interface CartService {
	/**
	 * 添加商品到购物车
	 * @return
	 */
	public List<Cart> addGoodsToCartList(List<Cart> list, Long itemId, Integer num);
	/**
	 * 从redis中获取购物车列表
	 * @param userName
	 * @return
	 */
	public List<Cart> findCartListFromRedis(String userName);
	/**
	 * 购物车列表存入redis中
	 * @param userName
	 * @return
	 */
	public void saveCartListToRedis(String userName,List<Cart> cartList);
	/**
	 * 合并购物车列表
	 * @param cartList1
	 * @param cartList2
	 * @return
	 */
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);
}
