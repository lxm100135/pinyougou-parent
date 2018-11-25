var app=angular.module('pinyougou',[]);//定义模块（不需要分页的模块）

//过滤器
/*
 * $sce服务 angularjs信任策略
 * angularjs过滤器（看成全局方法），一个过滤器只做一件事
 */
app.filter('trustHtml', ['$sce', function($sce) {//加载$sce组件，并传到方法中
	return function(data) {//data要过滤的内容
		return $sce.trustAsHtml(data);//返回过滤好的内容（信任html的转换）
	}
}]);