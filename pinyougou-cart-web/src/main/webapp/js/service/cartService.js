app.service('cartService',function($http){
	
	//得到购物车列表
	this.findCartList=function(){
		return $http.get('/cart/findCartList.do');
	}
	this.addGoodsToCartList=function(itemId,num){
		return $http.get('/cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
	}
	//购物车金额合计,商品数量合计
	this.sum=function(cartList){
		//购物车金额合计
		var totalValue={totalNum:0,toltalprice:0};
		for (var i = 0; i < cartList.length; i++) {//循环每一个购物车对象
			var orderItemList = cartList[i].orderItemList
			for (var j = 0; j < orderItemList.length; j++) {//循环每一个订单详情
				totalValue.toltalprice+=orderItemList[j].totalFee;
				totalValue.totalNum+=orderItemList[j].num;
			}
		}
		return totalValue;
	}
	//当前用户收货地址
	this.findListByUserId=function(){
		return $http.get('/address/findListByUserId.do');
	}
	//提交订单
	this.add=function(order){
		return $http.post('order/add.do',order);
	}
});