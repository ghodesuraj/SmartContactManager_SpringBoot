package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepositiry;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepositiry userRepositiry;

	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home - Smart Contact Manager");

		return "home";
	}

	@RequestMapping("/about/")
	public String about(Model m) {
		m.addAttribute("title", "About - Smart Contact Manager");

		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register - Smart Contact Manager");
		m.addAttribute("user", new User());

		return "signup";
	}

	// this handler is for registering user from sign up page
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model m,
			HttpSession session) {
		try {
			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions.!");
				throw new Exception("You have not agreed the terms and conditions.!");

			}
			System.out.println(agreement);
			if (result1.hasErrors()) {
				System.out.println("ERROR: " + result1.toString());
				m.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnable(true);

			String imgName = user.getName() + ".png";
			user.setImageUrl(imgName);

			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement: " + agreement);
			System.out.println("User: " + user);
			User result = this.userRepositiry.save(user);

			m.addAttribute("user", new User());

			session.setAttribute("message", new Message("Sucessfully Registered !!", "alert-success"));

			return "signup";

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !!" + e.getMessage(), "alert-danger"));

			return "signup";
		}
	}

	// handler for custom login
	@GetMapping("/loggin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login Page - Smart Contact Manager");

		return "login";
	}

	
	
	
	
}
