package com.example.demo.controller;

import com.example.demo.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginPageController {

    private final String BACKEND_URL = "http://localhost:8081/api/personnel";
    private final RestTemplate restTemplate;

    public LoginPageController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping({"/", "/index"})
    public String showIndexPage(Model model) {
        return "index";
    }

    @GetMapping("/personnel")
    public String showPersonnelPage(Model model) {
        model.addAttribute("user", new User());
        loadPersonnel(model);
        return "personnel-management";
    }

    @GetMapping("/edit-personnel")
    public String editPersonnel(@RequestParam Long id, Model model) {
        try {
            model.addAttribute("user", restTemplate.getForObject(BACKEND_URL + "/" + id, User.class));
        } catch (Exception e) {
            model.addAttribute("user", new User());
            model.addAttribute("error", "Could not load personnel. Check that the personnel service is running.");
        }
        loadPersonnel(model);
        return "personnel-management";
    }

    @PostMapping("/submit-personnel")
    public String savePersonnel(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            if (user.getId() == null) {
                restTemplate.postForObject(BACKEND_URL + "/save", user, User.class);
            } else {
                restTemplate.put(BACKEND_URL + "/" + user.getId(), user);
            }
            redirectAttributes.addFlashAttribute("message", "Personnel saved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not save personnel. Check the service and database.");
        }
        return "redirect:/personnel";
    }

    @PostMapping("/delete-personnel")
    public String deletePersonnel(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            restTemplate.delete(BACKEND_URL + "/" + id);
            redirectAttributes.addFlashAttribute("message", "Personnel deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not delete personnel. Check the service and database.");
        }
        return "redirect:/personnel";
    }

    private void loadPersonnel(Model model) {
        try {
            User[] personnel = restTemplate.getForObject(BACKEND_URL, User[].class);
            model.addAttribute("personnel", personnel == null ? new User[0] : personnel);
        } catch (Exception e) {
            model.addAttribute("personnel", new User[0]);
            model.addAttribute("error", "Could not load personnel. Check that the personnel service and database are running.");
        }
    }
}
