package com.example.demo.controller;

import com.example.demo.model.Vehicle;
import java.util.Arrays;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class VehiclePageController {

    private static final String BACKEND_URL = "http://localhost:8083/api/vehicles";

    private final RestTemplate restTemplate;

    public VehiclePageController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/vehicles")
    public String showVehicles(Model model) {
        Vehicle[] vehicles =
                restTemplate.getForObject(BACKEND_URL, Vehicle[].class);

        if (vehicles == null) {
            vehicles = new Vehicle[0];
        }

        model.addAttribute("vehicles", Arrays.asList(vehicles));
        model.addAttribute("vehicle", new Vehicle());

        return "vehicles";
    }

    @PostMapping("/submit-vehicle")
    public String saveVehicle(@ModelAttribute Vehicle vehicle) {
        restTemplate.postForObject(
                BACKEND_URL + "/save",
                vehicle,
                Vehicle.class
        );

        return "redirect:/vehicles";
    }
}