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
    private final String EQUIPMENT_BACKEND_URL = "http://localhost:8082/api/equipment";
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

    // 4. Return the equipment registration page
    @GetMapping("/equipment")
    public String showEquipmentPage(Model model) {
        model.addAttribute("equipment", new EquipmentDTO());
        return "equipment";
    }

    // 5. Call backend to save equipment data (Matched to form action: /submit-equipment)
    @PostMapping("/submit-equipment")
    public String saveEquipment(@ModelAttribute("equipment") EquipmentDTO equipment) {
        try {
            restTemplate.postForObject(EQUIPMENT_BACKEND_URL + "/save", equipment, EquipmentDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/equipment";
    }

    // DTO matching equipment entity fields for form binding
    public static class EquipmentDTO {
        private String serialNumber;
        private String name;
        private String category;
        private String status;
        private String operationalCondition;
        private String location;
        private Long unitId;
        private String ownership;

        public EquipmentDTO() {}

        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getOperationalCondition() { return operationalCondition; }
        public void setOperationalCondition(String operationalCondition) { this.operationalCondition = operationalCondition; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Long getUnitId() { return unitId; }
        public void setUnitId(Long unitId) { this.unitId = unitId; }

        public String getOwnership() { return ownership; }
        public void setOwnership(String ownership) { this.ownership = ownership; }
    }
}