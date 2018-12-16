package com.pinyougou.user.controller;
/**
 * controller
 * @author Administrator
 *
 */

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 获取登录名
 * @author Lxm_pc
 *
 */
@RestController
@RequestMapping("/login")
public class LoginController {
	@RequestMapping("/name")
	public Map getLoginName() {
		Map map = new HashMap();
		String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
		map.put("loginName", loginName);
		return map;
	}
}
