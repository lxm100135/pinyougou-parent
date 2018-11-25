package com.pinyougou.search.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;

@RestController
@RequestMapping("/itemSearch")
public class ItemSearchController {
	
//	@Reference(timeout=5000)//执行时间可以为5s默认1s
	@Reference
	private ItemSearchService itemSearchService;
	@RequestMapping("/search")
	public Map search(@RequestBody Map searchMap) {//这是一个对象
			return itemSearchService.search(searchMap);
	}
}
