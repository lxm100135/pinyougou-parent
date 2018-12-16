//服务层
app.service('loginService',function($http){
	    	
	//获取登录名
	this.loginName=function(){
		return $http.get('/login/name.do');
	}
	
});
