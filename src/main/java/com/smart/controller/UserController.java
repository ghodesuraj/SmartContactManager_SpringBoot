package com.smart.controller;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepositiry;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.HelperMessageClass;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepositiry userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		// getting the user string user-name(email)
		String userName = principal.getName();
		System.out.println("User Name: " + userName);

		User user = userRepository.getUserByUserName(userName);

		model.addAttribute("user", user);
	}

	// dash-board home
	@RequestMapping("/index")
	public String dashBoard(Model model, Principal principal) {

		return "normal/user_dashboard";
	}

	// open contact add form controller
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Conatct");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session, Model model) {
		try {
			// getting the user string user-name(email)
			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);

			// processing and uploading image file
			if (file.isEmpty()) {
				// if file is empty
				System.out.println("Your image File is Empty!");
				contact.setImage("contact.png");

			} else {
				// upload file to folder & update the name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is Uploaded.!");
			}
			contact.setUser(user);
			user.getContacts().add(contact);

			this.userRepository.save(user);

			System.out.println("DATA :" + contact);
			System.out.println("Added to Data Base");

			// Dynamic success message after uploading the contact on front-end
			session.setAttribute("message",
					new HelperMessageClass("Your contact has been added sucessfully !!", "success"));

			model.addAttribute("msg", new HelperMessageClass("Contact Added Successfully!!", "alert-success"));

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			// Dynamic error message after uploading the contact on front-end
			session.setAttribute("message", new HelperMessageClass("Something went wrong. Try Again.!!", "danger"));

			model.addAttribute("msg", new HelperMessageClass("Something Went Wrong!!", "alert-danger"));

		}

		if (session != null) {
			session.removeAttribute("message");
		}

		return "normal/add_contact_form";
	}

	// show contact handler
	// per page = 5[n]
	// current page= 0 [page]
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Displaying User Contacts");
		// send contacts list from database to frontend

		// getting the user string user-name(email)
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);

		Pageable pageable = PageRequest.of(page, 3);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// showing contact details of a selected person
	@RequestMapping("/{cId}/contact")
	public String showContactDeatils(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("Contact Id: " + cId);

		// getting the user string user-name(email)
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		// User Authentication
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_details";
	}

	// delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {

		try {

			// getting the user string user-name(email)
			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);

			Optional<Contact> contactOptional = this.contactRepository.findById(cId);
			Contact contact = contactOptional.get();

			System.out.println("Contact: " + contact);

			// User Authentication, deleting contact & displaying updated contact list
			if (contact == null) {
				session.setAttribute("message", new HelperMessageClass("No such contact found!", "alert-danger"));
				System.out.println("No such contact found!");
			} else if (user.getId() == contact.getUser().getId()) {
				// Deleting Image of the contact from
				// SmartContactManager/src/main/resources/static/img folder
				// Use following code to delete image of a contact
				// make sure every contact should have unique image and unique image name as
				// well
//				if (!contact.getImage().equals("contact.png")) {
//					File file = new ClassPathResource("static/img").getFile();
//					Path path = Paths.get(file.getAbsolutePath() + File.separator + contact.getImage());
//					Files.delete(path);
//					System.out.println("Image deleted sucessfully.!!");
//				}

				contact.setUser(null);
				this.contactRepository.delete(contact);
				// this.contactRepository.deleteContactById(cId);

				// Method 2 to delete contact
				// user.getContacts().remove(contact);
				// this.userRepository.save(user);

//				 Method 3 to delete contact
//				contactRepository.deleteById(contact.getcId());    

				System.out.println("Contact Deleted Sucessfully.!");

				session.setAttribute("message", new HelperMessageClass("Contact Deleted Successfully..", "success"));
				model.addAttribute("msg", new HelperMessageClass("Contact Deleted Successfully..!!", "alert-success"));
			} else if (user.getId() != contact.getUser().getId()) {
				session.setAttribute("message5",
						new HelperMessageClass("Insufficient rights to delete this contact", "alert-danger"));
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			// Dynamic error message after uploading the contact on front-end
			session.setAttribute("message", new HelperMessageClass("Something went wrong. Try Again.!!", "danger"));

			model.addAttribute("msg", new HelperMessageClass("Something Went Wrong!!", "alert-danger"));

		}
		if (session != null) {
			session.removeAttribute("message");
			System.out.println("Session Deleted Sucessfully.!!");
		}

		return "redirect:/user/show_contacts/0";

	}

	// open update form handler
	@PostMapping("/update_contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model m) {

		m.addAttribute("title", "Update Contact");
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		m.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session, Model model) {
		try {

			// getting the user string user-name(email)
			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			// old contact details
			Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();

			// processing and uploading image file
			if (file.isEmpty()) {
				// if file is empty
				contact.setImage(oldContactDetails.getImage());

			} else if (!file.isEmpty()) {

				// Deleting Image of the contact from
				// SmartContactManager/src/main/resources/static/img folder
				// Use following code to delete image of a contact, make sure every contact
				// should have unique image and unique image name as well
				// Method 1 to delete image
				if (!oldContactDetails.getImage().equals("contact.png")) {
					File file1 = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(file1.getAbsolutePath() + File.separator + oldContactDetails.getImage());
					Files.delete(path);
					System.out.println("Previous image deleted sucessfully.!!");
				}

				// Method 2 to delete image
				// File deleFile = new ClassPathResource("static/img").getFile();
				// File file2 = new File(deleFile, oldContactDetails.getImage());
				// file2.delete() ;

				// upload file to folder & update the name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is Updated.!");
			}

			contact.setUser(user);

			this.contactRepository.save(contact);

			// Dynamic success message after uploading the contact on front-end
			session.setAttribute("message",
					new HelperMessageClass("Your contact has been updated sucessfully !!", "success"));

			model.addAttribute("msg", new HelperMessageClass("Contact Updated Successfully!!", "alert-success"));

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();

			// Dynamic error message after uploading the contact on front-end
			session.setAttribute("message", new HelperMessageClass("Something went wrong. Try Again.!!", "danger"));

			model.addAttribute("msg", new HelperMessageClass("Something Went Wrong!!", "alert-danger"));

		}
		if (session != null) {
			session.removeAttribute("message");
			System.out.println("Session Deleted Sucessfully.!!");
		}
		System.out.println("Contact Name" + contact.getName());
		System.out.println("Image Name: " + contact.getImage());

		// return "redirect:/user/show_contacts/0";
		return "normal/add_contact_form";

	}

	// your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model, Principal principal) {
		// getting the user string user-name(email)
//		String userName = principal.getName();
//		User user = userRepository.getUserByUserName(userName);
//		model.addAttribute("user", user);
		model.addAttribute("title", "Profile Page");

		return "normal/profile";
	}
}
