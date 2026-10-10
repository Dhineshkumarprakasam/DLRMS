package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import com.example.demo.model.User;
import com.example.demo.model.SupplyItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// This legacy controller is intentionally kept as a no-op placeholder.
// The active web routes for the frontend are owned by IndexPageController,
// which keeps the merged feature set aligned with the main branch and the equipment module.
@Controller
public class LoginPageController {

    @Value("${services.personnel.base-url:http://localhost:8081/api/personnel}")
    private String backendUrl;

    @Value("${services.personnel.auth-url:http://localhost:8081/api/authenticate}")
    private String authBackendUrl;

    @Value("${services.supplies.base-url:http://localhost:8082/api/supplies}")
    private String suppliesBackendUrl;
    private final RestTemplate restTemplate;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    public LoginPageController(RestTemplate restTemplate,
                               SecurityContextRepository securityContextRepository,
                               SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        this.restTemplate = restTemplate;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
    }

    @GetMapping({"/", "/index"})
    public String showIndexPage(Model model) {
        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return redirectForRole(authentication);
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(required = false) String forceNumber,
                        @RequestParam(required = false) String password,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        if (forceNumber == null || forceNumber.isBlank() || password == null || password.isBlank()) {
            model.addAttribute("error", "Enter your force number and password.");
            model.addAttribute("forceNumber", forceNumber);
            return "login";
        }

        try {
            AuthenticationResult result = restTemplate.postForObject(authBackendUrl,
                    new LoginRequest(forceNumber, password), AuthenticationResult.class);
            if (result == null || !("ADMIN".equals(result.role()) || "USER".equals(result.role()))) {
                throw new IllegalStateException("Authentication service returned an invalid role.");
            }

                UserDetails principal = org.springframework.security.core.userdetails.User
                    .withUsername(result.forceNumber())
                    .password("")
                    .roles(result.role())
                    .build();
            Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal, null, principal.getAuthorities());
            sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            return redirectForRole(authentication);
        } catch (RestClientException | IllegalStateException exception) {
            SecurityContextHolder.clearContext();
            model.addAttribute("error", "Invalid force number or password.");
            model.addAttribute("forceNumber", forceNumber);
            return "login";
        }
    }

    @GetMapping("/welcome")
    public String showWelcomePage() {
        return "welcome";
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
            model.addAttribute("user", restTemplate.getForObject(backendUrl + "/" + id, User.class));
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
                    restTemplate.getForObject(suppliesBackendUrl + "/" + id, SupplyItem.class));
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
                restTemplate.postForObject(suppliesBackendUrl + "/save", supplyItem, SupplyItem.class);
            } else {
                restTemplate.put(suppliesBackendUrl + "/" + supplyItem.getId(), supplyItem);
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
            restTemplate.delete(suppliesBackendUrl + "/" + id);
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
                restTemplate.postForObject(backendUrl + "/save", user, User.class);
            } else {
                restTemplate.put(backendUrl + "/" + user.getId(), user);
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
            restTemplate.delete(backendUrl + "/" + id);
            redirectAttributes.addFlashAttribute("message", "Personnel deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not delete personnel. Check the service and database.");
        }
        return "redirect:/personnel";
    }

    private void loadPersonnel(Model model) {
        try {
            User[] personnel = restTemplate.getForObject(backendUrl, User[].class);
            model.addAttribute("personnel", personnel == null ? new User[0] : personnel);
        } catch (Exception e) {
            model.addAttribute("personnel", new User[0]);
            model.addAttribute("error", "Could not load personnel. Check that the personnel service and database are running.");
        }
    }

    private void loadSupplies(Model model) {
        try {
            SupplyItem[] items = restTemplate.getForObject(suppliesBackendUrl, SupplyItem[].class);
            model.addAttribute("supplies", items == null ? new SupplyItem[0] : items);
        } catch (Exception e) {
            model.addAttribute("supplies", new SupplyItem[0]);
            model.addAttribute("error", "Could not load supplies. Check that the supplies service and database are running.");
        }

    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String redirectForRole(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return "redirect:/";
        }
        if (authentication.getAuthorities().stream().anyMatch(authority -> "ROLE_USER".equals(authority.getAuthority()))) {
            return "redirect:/welcome";
        }
        return "redirect:/login";
    }

    public record LoginRequest(String forceNumber, String password) {}

    public record AuthenticationResult(String forceNumber, String role) {}
}
