//控制层 
app.controller('goodsController', function($scope, $controller, $location, goodsService,
		uploadService, itemCatService, typeTemplateService) {

	$controller('baseController', {
		$scope : $scope
	});// 继承

	// 读取列表数据绑定到表单中
	$scope.findAll = function() {
		goodsService.findAll().success(function(response) {
			$scope.list = response;
		});
	}

	// 分页
	$scope.findPage = function(page, rows) {
		goodsService.findPage(page, rows).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	// 查询实体
	$scope.findOne = function() {
		
		var id = $location.search()[ 'id' ];//获取页面传输参数id
		if (id==null) {
			return;
		}
		goodsService.findOne(id).success(function(response) {
			$scope.entity = response;
			//数据库中的html文本转文本信息
			editor.html($scope.entity.goodsDesc.introduction);
			$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
			$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
			$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
			
			for (var i = 0; i < $scope.entity.itemList.length; i++) {
				$scope.entity.itemList[ i ].spec=JSON.parse($scope.entity.itemList[ i ].spec);
			}
		});
	}
	$scope.save=function(){
		$scope.entity.goodsDesc.introduction = editor.html();
		var serviceObject;
		if ($scope.entity.goods.id==null) {
			serviceObject=goodsService.add( $scope.entity );
		}else {
			serviceObject=goodsService.update( $scope.entity );
		}serviceObject.success(
				function(response) {
					if (response.success) {
						alert('保存成功');
//						$scope.entity = {};
//						editor.html('');// 富文本器清空
						location.href="goods.html";
					} else {
						alert(response.message);
					}
				}
		);
	}
	// 保存
	$scope.add = function() {
		$scope.entity.goodsDesc.introduction = editor.html();
		goodsService.add($scope.entity).success(function(response) {
			if (response.success) {
				alert('新增成功');
				$scope.entity = {};
				editor.html('');// 富文本器清空
			} else {
				alert(response.message);
			}
		});
	}

	// 批量删除
	$scope.dele = function() {
		// 获取选中的复选框
		goodsService.dele($scope.selectIds).success(function(response) {
			if (response.success) {
				$scope.reloadList();// 刷新列表
				$scope.selectIds = [];
			}
		});
	}

	$scope.searchEntity = {};// 定义搜索对象

	// 搜索
	$scope.search = function(page, rows) {
		goodsService.search(page, rows, $scope.searchEntity).success(
				function(response) {
					$scope.list = response.rows;
					$scope.paginationConf.totalItems = response.total;// 更新总记录数
				});
	}
	// 上传图片
	$scope.uploadFile = function() {
		uploadService.uploadFile().success(function(response) {
			if (response.success) {
				$scope.image_entity.url = response.message;
			} else {
				alert(response.message);
			}
		});
	}
	// 定义实体结构、初始化
	$scope.entity = {
		goods : {},
		goodsDesc : {
			itemImages : [],
			specificationItems: []
		}
	};
	// 添加上传的图片实体存入图片列表
	$scope.addImage_entity = function() {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	// 移除
	$scope.removeImage_entity = function(index) {
		$scope.entity.goodsDesc.itemImages.splice(index, 1);
	}
	//一级分类下拉列表
	$scope.itemCatList = function() {
		itemCatService.findByParentId(0).success(function(response) {
			$scope.itemCatList=response;
		});
	}
	
	/*
	 * 监控id变化获取列表
	 */
	//二级分类下拉列表
	$scope.$watch('entity.goods.category1Id', function(newValue, oldValue) {
		itemCatService.findByParentId(newValue).success(function(response) {
			$scope.itemCatList2=response;
		});
	});
	//三级分类下拉列表
	$scope.$watch('entity.goods.category2Id', function(newValue, oldValue) {
		itemCatService.findByParentId(newValue).success(function(response) {
			$scope.itemCatList3=response;
		});
	});
	//模板id
	$scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {
		itemCatService.findOne(newValue).success(function(response) {
			$scope.entity.goods.typeTemplateId=response.typeId;
		});
	});
	//根据模板id取得品牌下拉列表
	$scope.$watch('entity.goods.typeTemplateId', function(newValue, oldValue) {
		typeTemplateService.findOne(newValue).success(function(response) {
			$scope.brandList=JSON.parse(response.brandIds);
			if ($location.search()[ 'id' ]==null) {//如果没传id就是增加商品
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse(response.customAttributeItems);
			}
		});
		//得到规格列表及规格选项
		typeTemplateService.findSpecList(newValue).success(
				function(response) {
//					$scope.specList = JSON.parse(response);
					$scope.specList = response;
				});
	});
	//初始化规格选项
	var specs = $scope.entity.goodsDesc.specificationItems;
	//更新选中的规格选项
	$scope.updateChoseValue=function($event, attributeName, value){
		//查询是否存在规格
		var object = $scope.searchList(specs, 'attributeName', attributeName);
		if (object!=null) {
			if($event.target.checked ) {
				object.attributeValue.push(value);
			}else {//取消勾选
				
				object.attributeValue.splice(object.attributeValue.indexOf(value), 1);//移除选项
				//全部规格选项移除后，移除选项集合
				if (object.attributeValue.length==0) {
					specs.splice(specs.indexOf(object), 1);
				}
			}
		}else {
			specs.push({ 'attributeName':attributeName, 'attributeValue': [value] });
		}
	}
	
//	var specs = $scope.entity.goodsDesc.specificationItems;
	
	$scope.createItemList=function(){
		//初始化SKU
		$scope.entity.itemList= [ {spec:{ }, price:0, num:99999, status:'0', isDefault:'0' } ];
//		$scope.entity.itemList= [ {spec:{ }, 'price': "23423", 'num' : "2332", 'status' :"0", 'is_default' : "0" } ];
		var items = $scope.entity.goodsDesc.specificationItems;
		for (var i = 0; i < items.length; i++) {
			$scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
		}
	}
	//添加列
	addColumn=function(list, columnName, columnValues){
		var newList=[ ];
		for (var i = 0; i < list.length; i++) {
			var oldRow = list[i];
			
			for (var j = 0; j < columnValues.length; j++) {
				var newRow = JSON.parse( JSON.stringify(oldRow) );//oldRow深克隆
				newRow.spec[ columnName ]=columnValues[ j ];
				newList.push( newRow );
			}
		}
		return newList;
	}
	$scope.status=[ '未审核', '已通过', '未通过' ];
	//初始化分类列表
	$scope.itemCatLista=[ ];
	$scope.findItemCatList=function(){
		itemCatService.findAll().success(
				function(response) {
					for (var i = 0; i < response.length; i++) {
						$scope.itemCatLista[ response[ i ].id ]=response[ i ].name;
					}
				}
		);
	}
	//判断规格与规格选项是否应该勾选
	$scope.checkAttributeValue=function(specName, optionName){
		var items = $scope.entity.goodsDesc.specificationItems;
		var object = $scope.searchList(items, "attributeName", specName);
		if (object!=null) {
			if (object.attributeValue.indexOf(optionName)>=0) {//根据下标得出此规格中是否有此规格选项
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}

	}
});
