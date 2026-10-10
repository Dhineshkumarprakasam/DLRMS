package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.SupplyItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IndexPageController {

    private final String BACKEND_URL = "http://localhost:8081/api/personnel";
    private final String SUPPLIES_BACKEND_URL = "http://localhost:8082/api/supplies";
    private final RestTemplate restTemplate;

    public IndexPageController(RestTemplate restTemplate) {
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

    @GetMapping("/supplies")
    public String showSuppliesPage(Model model) {
        model.addAttribute("supplyItem", new SupplyItem());
        loadSupplies(model);
        return "supply-management";
    }

    @GetMapping("/edit-supply")
    public String editSupply(@RequestParam Long id, Model model) {
        try {
            model.addAttribute("supplyItem",
                    restTemplate.getForObject(SUPPLIES_BACKEND_URL + "/" + id, SupplyItem.class));
        } catch (Exception e) {
            model.addAttribute("supplyItem", new SupplyItem());
            model.addAttribute("error", "Could not load the supply item. Check that the supplies service is running.");
        }
        loadSupplies(model);
        return "supply-management";
    }

    @PostMapping("/submit-supply")
    public String saveSupply(@ModelAttribute SupplyItem supplyItem, RedirectAttributes redirectAttributes) {
        try {
            if (supplyItem.getId() == null) {
                restTemplate.postForObject(SUPPLIES_BACKEND_URL + "/save", supplyItem, SupplyItem.class);
            } else {
                restTemplate.put(SUPPLIES_BACKEND_URL + "/" + supplyItem.getId(), supplyItem);
            }
            redirectAttributes.addFlashAttribute("message", "Supply item saved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not save the supply item. Check the service and item details.");
        }
        return "redirect:/supplies";
    }

    @PostMapping("/delete-supply")
    public String deleteSupply(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            restTemplate.delete(SUPPLIES_BACKEND_URL + "/" + id);
            redirectAttributes.addFlashAttribute("message", "Supply item deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Could not delete the supply item. Check that the supplies service is running.");
        }
        return "redirect:/supplies";
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

    private void loadSupplies(Model model) {
        try {
            SupplyItem[] items = restTemplate.getForObject(SUPPLIES_BACKEND_URL, SupplyItem[].class);
            model.addAttribute("supplies", items == null ? new SupplyItem[0] : items);
        } catch (Exception e) {
            model.addAttribute("supplies", new SupplyItem[0]);
            model.addAttribute("error", "Could not load supplies. Check that the supplies service and database are running.");
        }

    }
}
