//控制层 
app.controller('itemCatController', function($scope, $controller,
		itemCatService, typeTemplateService) {

	$controller('baseController', {
		$scope : $scope
	});// 继承

	// 读取列表数据绑定到表单中
	$scope.findAll = function() {
		itemCatService.findAll().success(function(response) {
			$scope.list = response;
		});
	}

	// 分页
	$scope.findPage = function(page, rows) {
		itemCatService.findPage(page, rows).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	// 查询实体
	$scope.findOne = function(id) {
		itemCatService.findOne(id).success(function(response) {
			$scope.entity = response;
		});
	}

	// 批量删除
	$scope.dele = function() {
		// 获取选中的复选框
		itemCatService.dele($scope.selectIds).success(function(response) {
			if (response.success) {
				// 重新查询
				$scope.nowParentId();
				$scope.findByParentId($scope.parentId);// 重新加载
				$scope.selectIds = [];
			}else {
				alert(response.message);
			}
		});
	}

	$scope.searchEntity = {};// 定义搜索对象

	// 搜索
	$scope.search = function(page, rows) {
		itemCatService.search(page, rows, $scope.searchEntity).success(
				function(response) {
					$scope.list = response.rows;
					$scope.paginationConf.totalItems = response.total;// 更新总记录数
				});
	}
	// 根据parentId查询列表
	$scope.findByParentId = function(parentId) {
		itemCatService.findByParentId(parentId).success(function(response) {
			$scope.list = response;
		});
	}
	// 当前级别
	$scope.grade = 1;
	// 设置级别
	$scope.setGrade = function(value) {
		$scope.grade = value;
	}
	$scope.selectList = function(entity) {
		if ($scope.grade == 1) {
			$scope.entity_1 = null;
			$scope.entity_2 = null;
		} else if ($scope.grade == 2) {
			$scope.entity_1 = entity;
			$scope.entity_2 = null;
		} else if ($scope.grade == 3) {
			$scope.entity_2 = entity;// entity_1不变
		}
		$scope.findByParentId(entity.id)
	}

	$scope.parentId = 0;
	// 当前上级id
	$scope.nowParentId = function() {
		if ($scope.grade == 1) {
			$scope.parentId = 0;
		} else if ($scope.grade == 2) {
			$scope.parentId = $scope.entity_1.id;
		} else if ($scope.grade == 3) {
			$scope.parentId = $scope.entity_2.id
		}
	}
	// 保存
	$scope.save = function() {
		$scope.nowParentId();
		$scope.entity.parentId = $scope.parentId;
		
		$scope.entity.typeId=parseInt($scope.entity.typeId.text);
		var serviceObject;// 服务层对象
		if ($scope.entity.id != null) {// 如果有ID
			serviceObject = itemCatService.update($scope.entity); // 修改
		} else {
			serviceObject = itemCatService.add($scope.entity);// 增加
		}
		serviceObject.success(function(response) {
			if (response.success) {
				// 重新查询
				$scope.findByParentId($scope.parentId);// 重新加载
			} else {
				alert(response.message);
			}
		});
	}

	$scope.typList = { data: [ ] };
	// 模板选项下拉列表
	$scope.selectOptionList = function() {
		typeTemplateService.selectOptionList().success(function(response) {
//			response右边id转String
			for(var i =0;i<response.length;){
				response[i].text=response[i].text+"";
				i++;
			}
			$scope.typList={ data: response};
		});
	}
});
