package com.pinyougou.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 认证类
 * @author Lxm_pc
 *
 */
public class UserDetailsServiceImpl implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		/*
		 * 构建角色列表
		 */
		//GrantedAuthority每一个角色
		List<GrantedAuthority> aut = new ArrayList();
//		GrantedAuthority e;
		aut.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		return new User(username, "123456", aut);
	}

}
