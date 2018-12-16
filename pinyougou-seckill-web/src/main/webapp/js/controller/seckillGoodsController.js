app.controller('seckillGoodsController', function($scope,$location,$interval,seckillGoodsService) {
	//获取秒杀商品
	$scope.findList=function(){
		seckillGoodsService.findList().success(
				function(response) {
					$scope.seckillGoodsList=response;
				}
		);
	}
	//秒杀商品详情
	$scope.findOne=function(){
		//接收参数id
		var id = $location.search()['id'];
		seckillGoodsService.findOne(id).success(
				function(response) {
					$scope.entity=response;
					allSecond=Math.floor((new Date(response.endTime).getTime()-new Date().getTime())/1000);
					
					var time = $interval(function() {
						allSecond-=1;
						$scope.timeString = convertTimeString(allSecond);
						if (allSecond<=0) {
							$interval.cancel(time);
						}
					},1000);
				}
		);
	}
	//转换秒为天时分秒	days hours:minutes:seconds
	convertTimeString=function(allSecond){
		var days = Math.floor(allSecond/(60*60*24));
		var hours = Math.floor((allSecond-days*(60*60*24))/(60*60));
		var minutes = Math.floor((allSecond-days*(60*60*24)-hours*60*60)/60);
		var seconds = Math.floor(allSecond-days*(60*60*24)-hours*60*60-minutes*60);
		var timeString = "";
		if (days>=0) {
			timeString+=days+"天"
		}
		return timeString+hours+":"+minutes+":"+seconds;
	}
	//提交秒杀订单
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
				function(response) {
					if (response.success) {
						alert('抢购成功，请在5分钟内完成支付');
						location.href="pay.html";
					}else {
						alert(response.message);
					}
				}
		);
	}
});