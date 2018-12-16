 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
//	$controller('baseController',{$scope:$scope});//继承
	$scope.reg=function(){
		//比较两次输入密码
		if ($scope.entity.password!=$scope.password) {
			alert('两次密码不一致');
			$scope.entity.password='';
			$scope.password='';
			return;
		}
		userService.add($scope.entity,$scope.code).success(function(response) {
			alert(response.message);
		});
	}
	//发送短信验证码
	$scope.sendCode=function(){
		if ($scope.entity.phone==null || $scope.entity.phone=='') {
			alert('请填写手机号');
			return;
		}
		userService.sendCode($scope.entity.phone).success(function(response) {
			alert(response.message);
		});
	}
});	
