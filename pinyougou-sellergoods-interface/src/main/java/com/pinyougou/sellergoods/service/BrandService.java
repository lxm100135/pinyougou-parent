package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

//import com.pinyougou.pojo.TbBrand;



/**
 * 品牌接口
 * @author Lxm_pc
 *
 */
public interface BrandService {
	
	public List<TbBrand> findAll();
	
	/**
	 * 品牌分页
	 * @param pageNum 当前页码
	 * @param pageSize每 页记录数
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	/**
	 * 增加
	 * @param tbBrand
	 */
	public void add(TbBrand tbBrand);
	/**
	 * 根据id查找实体
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	/**
	 * 更新品牌
	 * @param brand
	 */
	public void update(TbBrand brand);
	/**
	 * 删除品牌
	 * @param ids
	 */
	public void delete(Long[] ids);
	/**
	 * 条件查询
	 * @param brand
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize);
	/**
	 * 返回下拉列表
	 * @return
	 */
	public List<Map>selectOptionList();
}
