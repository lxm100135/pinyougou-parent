package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
@Service
public class CartServiceImpl implements CartService{
	@Autowired
	TbItemMapper itemMapper;
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> list, Long itemId, Integer num) {
		//1.根据SKU ID查询商品明细SKU对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item==null) {
			throw new RuntimeException("商品不存在！");
		}
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态不合法！");
		}
		if (item.getNum()<=0 || item.getNum()<num) {
			throw new RuntimeException("商品库存不足！！");
		}
		//2.根据SKU对象得到商家ID
		String sellerId = item.getSellerId();
		
		//3.根据商家ID判断购物车列表是否存在该商家购物车对象
		Cart cart = getCart(list, sellerId);
		//4.存在对应商家购物车
		if (cart!=null) {
			
			TbOrderItem orderItem = getOrderItem(cart.getOrderItemList(), item.getId());
			//4.1存在该商品订单明细
			if (orderItem!=null) {
				orderItem.setNum(orderItem.getNum()+num);//更新数量
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));//更新价格总额
				if (orderItem.getNum()<=0) {//当数量少于等于0时，移除该商品订单明细
					cart.getOrderItemList().remove(orderItem);
				}
				if (cart.getOrderItemList().size()==0) {//当购物车商品明细条数为0，移除当前购物车
					list.remove(cart);
				}
			}
			//4.2不存在该商品订单明细
			else {
				//4.3创建订单明细并且添加到明细列表
				createAndAddOrderItem(num, item, cart);
			}
		}
		else {
			//5.不存在该购物车
			//5.1创建购物车对象
			cart = new Cart(); 
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			//5.2创建明细列表
			List<TbOrderItem> orderItems = new ArrayList<TbOrderItem>();
			cart.setOrderItemList(orderItems);//设置明细列表
			//5.3创建订单明细并且添加到明细列表
			createAndAddOrderItem(num, item, cart);
			//5.4添加购物车到购物车列表
			list.add(cart);
		}
		return list;
	}
	/**
	 * 返回对应的订单明细
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private TbOrderItem getOrderItem(List<TbOrderItem> orderItems, Long itemId) {
		for (TbOrderItem orderItem : orderItems) {
			if (orderItem.getItemId().longValue()==itemId.longValue()) {
				return orderItem;
			}
		}
		return null;
	}
	/**
	 * 返回对应的购物车对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart getCart(List<Cart> cartList,String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {//4.存在商家购物车对象
				return cart;
			}
		}
		return null;
	}
	/**
	 * 创建订单明细并且添加到明细列表
	 * @param sellerId
	 * @param num
	 * @param item
	 * @param cart
	 */
	private void createAndAddOrderItem(Integer num, TbItem item, Cart cart) {
		//创建订单明细
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId()); 
		orderItem.setSellerId(item.getSellerId());
		orderItem.setNum(num);
		orderItem.setPrice(item.getPrice());
		orderItem.setPicPath(item.getImage());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		//4.3订单明细添加到明细列表
		
		cart.getOrderItemList().add(orderItem);//明细添加到明细列表
	}
	@Autowired
	private RedisTemplate redisTemplate;
	@Override
	public List<Cart> findCartListFromRedis(String userName) {
		System.out.println("从redis中提取购物车："+userName);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(userName);
		if (cartList==null) {
			cartList=new ArrayList<>();
		}
		return cartList;
	}
	@Override
	public void saveCartListToRedis(String userName, List<Cart> cartList) {
		redisTemplate.boundHashOps("cartList").put(userName, cartList);
		System.out.println("购物车存入redis:"+userName);
	}
	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		for (Cart cart2 : cartList2) {
			for (TbOrderItem orderItem : cart2.getOrderItemList()) {//购物车2的每一个商品明细加入购物车1
					Long itemId = orderItem.getItemId();
					cartList1=addGoodsToCartList(cartList1, itemId, orderItem.getNum());
			}
		}
		return cartList1;//返回合并后的cartList1
	}
	

}
