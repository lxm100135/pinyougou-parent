app.controller('itemController', function($scope,$http) {
	//变量初始化
	$scope.num=1;
	//存储用户选择的规格
	$scope.specification={};
	
	//商品选择数量
	$scope.addNum=function(x){
		$scope.num+=x;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	//选中规格选项
	$scope.selectSpecification=function(attributeName,option){
			$scope.specification[attributeName]=option;//没有就给这个attributeName赋值	
			for(var i=0;i<skuList.length;i++){
				if($scope.matchObject($scope.specification,skuList[i].spec)){
				$scope.sku=skuList[i];
				break;
				}else{
					$scope.sku={};
				}
			}
			
	}
	//当前规格选项是否是选中状态
	$scope.isChose=function(key,value){
		if($scope.specification[key]==value){
			return true;
		}
		return false;
	}
	$scope.sku={};
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		//$scope.specification=skuList[0].spec;浅克隆（对象的引用）
		$scope.specification=JSON.parse(JSON.stringify(skuList[0].spec));//深克隆（新的对象），不使用浅克隆
	}
	//匹配两个对象是否相等
	$scope.matchObject=function(map1,map2){
		for(var key in map1){
			if(map1[key]!=map2[key]){
				return false;
			}
		}
		for(var key in map2){
			if(map1[key]!=map2[key]){
				return false;
			}
		}
		return true;
	}
	$scope.addToCart=function(){
//		alert("SKU_id:"+$scope.sku.id);
		//跨域请求
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
				+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
						function(response) {
							if (!response.success) {
								alert(response.message);
							}else {
								location.href="http://localhost:9107/cart.html";
							}
						}
				);
	}
});