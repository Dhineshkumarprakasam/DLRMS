package com.example.demo.controller;

import com.example.demo.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class LoginPageController {
    private final String BACKEND_URL = "http://localhost:8081/api/personnel";
    private final RestTemplate restTemplate;

    // Constructor injection using the bean from FrontendServiceApplication
    public LoginPageController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 1. Return the index/login page
    @GetMapping({"/", "/index"})
    public String showIndexPage(Model model) {
        model.addAttribute("user", new User());
        return "index";
    }

    // 2. Call backend to save user data (Matched to form action: /submit-personnel)
    @PostMapping("/submit-personnel")
    public String saveUser(@ModelAttribute User user) {
        try {
            restTemplate.postForObject(BACKEND_URL + "/save", user, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    // 3. Call backend to get user data by ID
    @GetMapping("/get-user")
    public String getUserById(@RequestParam Long id, Model model) {
        try {
            User user = restTemplate.getForObject(BACKEND_URL + "/" + id, User.class);
            model.addAttribute("fetchedUser", user);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "User not found or backend offline.");
        }
        
        model.addAttribute("user", new User());
        return "index";
    }
}