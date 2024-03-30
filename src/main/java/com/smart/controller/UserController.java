package com.smart.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smart.dao.UserRepositiry;
import com.smart.entities.User;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepositiry userRepositiry;
	
	@RequestMapping("/index")
//	@RequestMapping(path = "/index", method = RequestMethod.POST) // POST
	public String dashBoard(Model model, Principal principal) 
	{
		//getting the user sing user-name(email)
		String userName = principal.getName();
		System.out.println("User Name: " + userName);
		
		User user = userRepositiry.getUserByUserName(userName);
		System.out.println("User : " + user);
		
		model.addAttribute("user", user);
		
		return "normal/user_dashboard";

	}
}