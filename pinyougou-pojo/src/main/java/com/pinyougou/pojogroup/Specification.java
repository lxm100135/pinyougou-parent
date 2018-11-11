package com.pinyougou.pojogroup;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
/**
 * 规格组合实体类 
 * @author Lxm_pc
 *
 */
public class Specification implements Serializable{
	private TbSpecification specification;
	private List<TbSpecificationOption> specificationOptionList;
	public Specification(){
		super();
	}
	public Specification(TbSpecification tbSpecification, List<TbSpecificationOption> specificationOptions) {
		this.specification = tbSpecification;
		this.specificationOptionList = specificationOptions;
	}
	public TbSpecification getSpecification() {
		return specification;
	}
	public void setSpecification(TbSpecification specification) {
		this.specification = specification;
	}
	public List<TbSpecificationOption> getSpecificationOptionList() {
		return specificationOptionList;
	}
	public void setSpecificationOptionList(List<TbSpecificationOption> specificationOptionList) {
		this.specificationOptionList = specificationOptionList;
	}
	
}
