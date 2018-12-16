//服务层
app.service('userService',function($http){
	    	
	//增加 
	this.add=function(entity,code){
		return  $http.post('../user/add.do?code='+code,entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../user/update.do',entity );
	}
	//发送验证码
	this.sendCode=function(phone){
		return $http.post('/user/sendCode.do?phone='+phone);
	}
	
});
