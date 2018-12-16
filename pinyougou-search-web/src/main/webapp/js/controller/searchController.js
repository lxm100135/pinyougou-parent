app.controller("searchController", function($scope, searchService) {
	//定义搜索对象结构
	$scope.searchMap={'keywords':'','category':'','brand':'',
			'spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);//转成int类型
		searchService.search($scope.searchMap).success(
			function(response) {
				$scope.resultMap=response;
				//构建分页兰
				buildPageLabel();
			}
		);
	}
	//构建分页兰
	buildPageLabel=function(){
		$scope.pageLabel=[];
		var beginPage=1;//开始页
		var lastPage=$scope.resultMap.totalPages;//截止页
		
		if ($scope.resultMap.totalPages>5) {//总页数大于5（不经过这里就是小于等于5）
			if ($scope.searchMap.pageNo > $scope.resultMap.totalPages-2) {//显示后5页
  				beginPage=lastPage-4;
			}else if ($scope.searchMap.pageNo <= 3) {//显示前5页
				lastPage=5; 
			}else {//当前页为中心的5页
				beginPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
				
		}
		
		for (var i = beginPage; i <= lastPage; i++) {
			$scope.pageLabel.push(i);
		}
		
	}
	$scope.queryPage=function(pageNo){
		if (pageNo<1 || pageNo>$scope.resultMap.totalPages) {
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();//搜索
	}
	//添加搜索项，改变searchMap的值
	$scope.addSearchItem=function(key, value){
		if (key=='category' || key=='brand' || key=='price') {//点击的是分类或品牌、价格区间
			$scope.searchMap[key]=value;
		}else {//点击的是规格
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//搜索
	}
	//移除搜索项，改变searchMap的值
	$scope.removeSearchItem=function(key){
		if (key=='category' || key=='brand' || key=='price') {//点击的是分类或品牌、价格区间
			$scope.searchMap[key]='';//初始化过，设置为空字符串就行
		}else {//点击的是规格
			delete $scope.searchMap.spec[key];//本来是不存在的，需要删除
		}
		$scope.search();//搜索
	}
	//排序
	$scope.sort=function(sort,type){
		$scope.searchMap.sort=sort;
		$scope.searchMap.sortField=type;
		$scope.search();//搜索
	}
});