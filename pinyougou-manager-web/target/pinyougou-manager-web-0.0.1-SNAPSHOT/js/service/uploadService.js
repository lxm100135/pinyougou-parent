//服务层
app.service('uploadService',function($http){
	    	this.uploadFile=function(){
	    		var formData = new FormData();
	    		formData.append('file', file.files[0]);//file文件上传框name
	    		
	    		return $http({
	    			url: '../upload.do',
	    			method: 'post',
	    			data: formData,
	    			headers: {'Content-Type': undefined},//默认是json，上传文件要设成unddfined
	    			transformRequest: angular.identity//对整个表单进行二进制序列化
	    		});
	    	}
});
