 //控制层 
app.controller('indexController' ,function($scope,loginService){	
	
//	$controller('baseController',{$scope:$scope});//继承
	$scope.showName=function(){
		loginService.loginName().success(
				function(response) {
					$scope.loginName=response.loginName;
				}
		);
	}
});	
