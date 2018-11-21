app.controller('contentController', function($scope,contentService) {
	$scope.contentList=[ ];
	//根据广告分类id查询广告
	$scope.findByCategoryId=function(categoryId){//不同的广告分类id有不同的list集合
		contentService.findByCategoryId(categoryId).success(
				function(response) {
//					$scope.contentList=response;
					$scope.contentList[categoryId]=response;//为了方便区分变量、用分类id区分不同的list集合
				}
		);
	}
});