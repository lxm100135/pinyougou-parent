package com.pinyougou.manager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {
	@Reference
	private BrandService brandService;
	
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();
	}
	
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows) {
		return brandService.findPage(page, rows);
	}
	
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand tbBrand) {//参数传递的是对象要用@RequestBody注解
		try {
			brandService.add(tbBrand);
			return new Result(true, "新增成功!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "新增失败!");
		}
	}
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) {
		return brandService.findOne(id);
	}
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {//参数传递的是对象要用@RequestBody注解
		try {
			brandService.update(brand);
			return new Result(true, "更新成功!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "更新失败!");
		}
	}
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {//参数传递的是对象要用@RequestBody注解
		try {
			brandService.delete(ids);
			return new Result(true, "删除成功!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "删除失败!");
		}
	}
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand brand, int page, int rows) {//参数传递的是对象要用@RequestBody注解
			return brandService.findPage(brand, page, rows);
	}
	
}
