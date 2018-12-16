app.controller('payController',function($scope, $location,payService){
	$scope.createNative=function(){
		payService.createNative().success(
				function(response) {
					//订单金额（元），订单号
					$scope.money=(response.total_fee/100).toFixed(2);//两位小数
					$scope.out_trade_no=response.out_trade_no;
					//二维码链接
					$scope.code_url=response.code_url;
					//生成二维码
					var qr = new QRious({
						      element: document.getElementById('qrious'),
						      size: 250,
						      value: 'http://www.baidu.com',//测试
//						      value: $scope.code_url,
						      level: 'H'//级别
						    });
					//查询订单状态
					queryPayStatus();
				}
		);
	}
	//查询订单状态
	queryPayStatus=function(){
		payService.queryPayStatus($scope.out_trade_no).success(
				function(response) {
					if (response.success) {
						location.href="paysuccess.html#?money"+$scope.money;
					}else {
						if (response.message=="二维码超时") {
							$scope.createNative();//重新创建二维码
						}else {
							location.href="payfail.html";
						}
					}
				}
		);
	}
	//获取金额
	$scope.getMoney=function(){
		return $location.search()['money'];
	}
});