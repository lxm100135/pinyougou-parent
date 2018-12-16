app.controller('cartController',function($scope, cartService){
	
	//得到购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
				function(response) {
					$scope.cartList=response;
					$scope.totalValue=cartService.sum(response);////购物车金额合计,商品数量合计
				}
		);
	}
	//添加商品到购物车
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
				function(response) {
					if (!response.success) {
						alert(response.message);
					}
					$scope.findCartList();//重新加载购物车
				}
				
		);
	}
	//当前用户收货地址
	$scope.findAddressList=function(){
		cartService.findListByUserId().success(
				function(response) {
					$scope.addressList=response;
					//根据isSelectAd让默认地址选中
					for (var i = 0; i < response.length; i++) {
						if (response[i].isDefault=='1') {
							$scope.address=response[i];
							break;
						}
					}
				}
		);
	}
	//选中的地址
	$scope.selectAddress=function(address){
		$scope.address=address;
	}
	//是否是选中的地址
	$scope.isSelectAd=function(address){
		if ($scope.address==address) {
			return true;
		}
		return false;
	}
	$scope.order={paymentType:'1'};
	//支付方式选择
	$scope.selectPayType=function(type){
		$scope.order.paymentType=type;
	}
	$scope.submitOrder=function(){
		$scope.order.receiverAreaName=$scope.address.provinceId+$scope.address.cityId+$scope.address.address;
		$scope.order.receiverMobile=$scope.address.mobile;
		$scope.order.receiver=$scope.address.contact;
//		$scope.order.sellerId=$scope.cart.sellerId;
//		$scope.order.paymentType=
		cartService.add($scope.order).success(
				function(response) {
					if (!response.success) {
						alert(response.message);//保存失败
					}else {
						if ($scope.order.paymentType=='1') {//微信支付
							location.href="pay.html";
						}else {//
							location.href="paysuccess.html";
						}
					}
				}
		);
	}
});