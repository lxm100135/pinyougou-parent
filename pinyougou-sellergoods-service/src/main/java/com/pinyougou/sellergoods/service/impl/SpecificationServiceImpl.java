package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
//import com.pinyougou.pojo.TbSpecificationOptionExample.Criteria;
import com.pinyougou.pojogroup.Specification;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		TbSpecification tbSpecification =  specification.getSpecification();
		specificationMapper.insert(tbSpecification);		//插入规格并返回id
		List<TbSpecificationOption> specificationOptions = specification.getSpecificationOptionList();
		for (TbSpecificationOption option : specificationOptions) {
			option.setSpecId(tbSpecification.getId());	//设置得到的id
			specificationOptionMapper.insert(option);//新增规格选项
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//更新规格实体
		TbSpecification tbSpecification = specification.getSpecification();
		specificationMapper.updateByPrimaryKey(tbSpecification);
		
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria =example.createCriteria();
		criteria.andSpecIdEqualTo(tbSpecification.getId());
		//删除原来规格对应的规格选项在插入
		specificationOptionMapper.deleteByExample(example);
		List<TbSpecificationOption> specificationOptions = specification.getSpecificationOptionList();
		for (TbSpecificationOption option : specificationOptions) {
			option.setSpecId(tbSpecification.getId());	//设置得到的id
			specificationOptionMapper.insert(option);//新增规格选项
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//获取规格实体
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		
		//获取规格选项列表
		TbSpecificationOptionExample example =new TbSpecificationOptionExample();
		com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(id);//精确查询
		List<TbSpecificationOption> specificationOptions = specificationOptionMapper.selectByExample(example );
		
		return new Specification(tbSpecification, specificationOptions);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//删除规格表数据
			specificationMapper.deleteByPrimaryKey(id);
			
			//删除规格选项表数据
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria =example.createCriteria();
			criteria.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(example);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public List<Map> selectOptionList() {
			// TODO Auto-generated method stub
			return specificationMapper.selectOptionList();
		}
	
}
