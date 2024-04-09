package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepositiry;
import com.smart.entities.User;
import com.smart.helper.HelperMessageClass;
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
	public String registerUser(@Valid @ModelAttribute("user") User user,  @RequestParam("profileImage") MultipartFile file,BindingResult result1,
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

			
			// processing and uploading image file
			if (file.isEmpty()) {
				// if file is empty
				System.out.println("Your image File is Empty!");
				user.setImageUrl("user.png");

			} else {
				// upload file to folder & update the name to contact
				user.setImageUrl(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image Uploaded Successfully.!");
			}

			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement: " + agreement);
			System.out.println("User: " + user);
			User result = this.userRepositiry.save(user);

			m.addAttribute("user", new User());

			session.setAttribute("message", new HelperMessageClass("Sucessfully Registered !!", "alert-success"));

			return "signup";

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message",
					new HelperMessageClass("Something went wrong !!" + e.getMessage(), "alert-danger"));

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
