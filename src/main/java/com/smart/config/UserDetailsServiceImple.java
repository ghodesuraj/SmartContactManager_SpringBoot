package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepositiry;
import com.smart.entities.User;

public class UserDetailsServiceImple implements UserDetailsService {
	@Autowired
	private UserRepositiry userRepositiry;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// TODO: Fetching User from Database
		User user = userRepositiry.getUserByUserName(username);
		
		if (user == null) {
			throw new UsernameNotFoundException("Could not found user !!");
		}
		
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		return customUserDetails;
	}

}
