package com.pinyougou.content.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import com.pinyougou.content.service.ContentService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);		
		//清除缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//获取原来的分组id
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		//根据原来分组id删除缓存
		redisTemplate.boundHashOps("content").delete(categoryId);
		//更新数据库
		contentMapper.updateByPrimaryKey(content);
		if (content.getCategoryId()!=categoryId) {//一样的话就不用访问redis
			//删除现在分组id的缓存
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
		
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//清除缓存
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
			redisTemplate.boundHashOps("content").delete(categoryId);
			
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}	
		@Autowired
		private RedisTemplate redisTemplate;
		@Override
		public List<TbContent> findByCategoryId(Long id) {
			/*
			 * 广告缓存(大key:content、 小key:id)
			 * 先查找是否有缓存
			 */
			List<TbContent> list = (List<TbContent>) redisTemplate.boundHashOps("content").get(id);
			
			if (list==null) {//缓存为空
				System.out.println("从数据库中获取广告数据并放入缓存");
				TbContentExample example = new TbContentExample();
				com.pinyougou.pojo.TbContentExample.Criteria criteria = example.createCriteria();
				criteria.andCategoryIdEqualTo(id);
				criteria.andStatusEqualTo("1");//并且状态为1，条件有效
				example.setOrderByClause("sort_order");//按照此字段排序
				list = contentMapper.selectByExample(example);
				//存储到redis(大key、小key)
				redisTemplate.boundHashOps("content").put(id, list);
			}else {
				System.out.println("从缓存中获取数据");
			}
			return list;
		}
	
}
