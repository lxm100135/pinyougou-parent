app.controller("searchController", function($scope, searchService) {
	//定义搜索对象结构
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{}};
	//搜索
	$scope.search=function(){
		searchService.search($scope.searchMap).success(
			function(response) {
				$scope.resultMap=response;
			}
		);
	}
	//添加搜索项，改变searchMap的值
	$scope.addSearchItem=function(key, value){
		if (key=='category' || key=='brand') {//点击的是分类或品牌
			$scope.searchMap[key]=value;
		}else {//点击的是规格
			$scope.searchMap.spec[key]=value;
		}
	}
});