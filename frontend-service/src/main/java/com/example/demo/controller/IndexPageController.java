package com.example.demo.controller;


import com.example.demo.model.LogisticsRequest;

import com.example.demo.model.MaintenanceRecord;
import com.example.demo.model.ResourceAllocation;
import com.example.demo.model.User;
import com.example.demo.model.Vehicle;
import java.util.Arrays;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.demo.model.*;
@Controller
public class IndexPageController {

    private final String BACKEND_URL = "http://localhost:8081/api/personnel";
    private final String SUPPLIES_BACKEND_URL = "http://localhost:8082/api/supplies";
    private final String EQUIPMENT_BACKEND_URL = "http://localhost:8084/api/equipment";
    @Value("${services.personnel.logistics-url:http://localhost:8081/api}")
    private String logisticsBackendUrl;
    private final RestTemplate restTemplate;

    public IndexPageController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping({"/", "/index"})
    public String showIndexPage(Model model) {
        try {
            LogisticsRequest[] requests = restTemplate.getForObject(logisticsBackendUrl + "/requests", LogisticsRequest[].class);
            MaintenanceRecord[] maintenance = restTemplate.getForObject(logisticsBackendUrl + "/maintenance", MaintenanceRecord[].class);
            ResourceAllocation[] allocations = restTemplate.getForObject(logisticsBackendUrl + "/allocations", ResourceAllocation[].class);
            SupplyItem[] supplies = restTemplate.getForObject(SUPPLIES_BACKEND_URL, SupplyItem[].class);
            EquipmentDTO[] equipment = restTemplate.getForObject(EQUIPMENT_BACKEND_URL, EquipmentDTO[].class);
            Vehicle[] vehicles = restTemplate.getForObject("http://localhost:8083/api/vehicles", Vehicle[].class);
            model.addAttribute("pendingRequests", requests == null ? 0 : Arrays.stream(requests)
                    .filter(request -> "PENDING".equals(request.getStatus())).count());
            model.addAttribute("activeMaintenance", maintenance == null ? 0 : Arrays.stream(maintenance)
                    .filter(record -> !"COMPLETED".equals(record.getStatus())).count());
            model.addAttribute("activeAllocations", allocations == null ? 0 : allocations.length);
            model.addAttribute("supplyCount", supplies == null ? 0 : supplies.length);
            model.addAttribute("equipmentCount", equipment == null ? 0 : equipment.length);
            model.addAttribute("vehicleCount", vehicles == null ? 0 : vehicles.length);
            long lowStockItems = supplies == null ? 0 : Arrays.stream(supplies)
                    .filter(item -> item.getQuantityOnHand() != null && item.getReorderLevel() != null
                            && item.getQuantityOnHand().compareTo(item.getReorderLevel()) <= 0)
                    .count();
            long availableEquipment = equipment == null ? 0 : Arrays.stream(equipment)
                    .filter(item -> "AVAILABLE".equalsIgnoreCase(item.getStatus())).count();
            long availableVehicles = vehicles == null ? 0 : Arrays.stream(vehicles)
                    .filter(vehicle -> "AVAILABLE".equalsIgnoreCase(vehicle.getStatus())).count();
            model.addAttribute("lowStockItems", lowStockItems);
            model.addAttribute("availableResources", availableEquipment + availableVehicles);
        } catch (Exception exception) {
            model.addAttribute("error", "Could not load dashboard data. Check that the logistics and resource services are running.");
        }
        return "index";
    }

    @GetMapping("/requests")
    public String showRequests(Model model) {
        model.addAttribute("request", new LogisticsRequest());
        try {
            LogisticsRequest[] requests = restTemplate.getForObject(logisticsBackendUrl + "/requests", LogisticsRequest[].class);
            model.addAttribute("requests", requests == null ? List.of() : Arrays.asList(requests));
        } catch (Exception exception) {
            model.addAttribute("requests", List.of());
            model.addAttribute("error", "Could not load requests. Check that the personnel service is running.");
        }
        return "requests";
    }

    @PostMapping("/submit-request")
    public String submitRequest(@ModelAttribute LogisticsRequest request, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            String requester = session != null && session.getAttribute("user") != null
                    ? (String) session.getAttribute("user") : "ADMIN";
            request.setRequester(requester);
            restTemplate.postForObject(logisticsBackendUrl + "/requests", request, LogisticsRequest.class);
            redirectAttributes.addFlashAttribute("message", "Request submitted for approval.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Could not submit request. Check the request details and service.");
        }
        return "redirect:/requests";
    }

    @PostMapping("/update-request")
    public String updateRequest(@ModelAttribute LogisticsRequest request, RedirectAttributes redirectAttributes) {
        try {
            restTemplate.put(logisticsBackendUrl + "/requests/" + request.getId(), request);
            redirectAttributes.addFlashAttribute("message", "Request updated.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Could not update request. Check the service and request status.");
        }
        return "redirect:/requests";
    }

    @PostMapping("/delete-request")
    public String deleteRequest(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            restTemplate.delete(logisticsBackendUrl + "/requests/" + id);
            redirectAttributes.addFlashAttribute("message", "Request deleted.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Could not delete request. Check that the service is running.");
        }
        return "redirect:/requests";
    }

    @GetMapping("/maintenance")
    public String showMaintenance(Model model) {
        model.addAttribute("maintenanceRecord", new MaintenanceRecord());
        loadMaintenance(model);
        return "maintenance";
    }

    @PostMapping("/submit-maintenance")
    public String saveMaintenance(@ModelAttribute MaintenanceRecord record, RedirectAttributes redirectAttributes) {
        try {
            if (record.getId() == null) {
                restTemplate.postForObject(logisticsBackendUrl + "/maintenance", record, MaintenanceRecord.class);
            } else {
                restTemplate.put(logisticsBackendUrl + "/maintenance/" + record.getId(), record);
            }
            redirectAttributes.addFlashAttribute("message", "Maintenance record saved.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Could not save maintenance. Check the service and record details.");
        }
        return "redirect:/maintenance";
    }

    @PostMapping("/delete-maintenance")
    public String deleteMaintenance(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            restTemplate.delete(logisticsBackendUrl + "/maintenance/" + id);
            redirectAttributes.addFlashAttribute("message", "Maintenance record deleted.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Could not delete maintenance record.");
        }
        return "redirect:/maintenance";
    }

    @GetMapping("/allocations")
    public String showAllocations(Model model) {
        model.addAttribute("allocation", new ResourceAllocation());
        loadAllocations(model);
        return "allocations";
    }

    @PostMapping("/submit-allocation")
    public String saveAllocation(@ModelAttribute ResourceAllocation allocation, RedirectAttributes redirectAttributes) {
        try {
            if (allocation.getId() == null) {
                restTemplate.postForObject(logisticsBackendUrl + "/allocations", allocation, ResourceAllocation.class);
            } else {
                restTemplate.put(logisticsBackendUrl + "/allocations/" + allocation.getId(), allocation);
            }
            redirectAttributes.addFlashAttribute("message", "Resource allocation saved.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Could not save allocation. Check the service and allocation details.");
        }
        return "redirect:/allocations";
    }

    @PostMapping("/delete-allocation")
    public String deleteAllocation(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            restTemplate.delete(logisticsBackendUrl + "/allocations/" + id);
            redirectAttributes.addFlashAttribute("message", "Resource allocation deleted.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Could not delete allocation.");
        }
        return "redirect:/allocations";
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

    @GetMapping("/equipment")
    public String showEquipmentPage(Model model) {
        model.addAttribute("equipment", new EquipmentDTO());
        loadEquipment(model);
        return "equipment";
    }

    @GetMapping("/edit-equipment")
    public String editEquipment(@RequestParam Long id, Model model) {
        try {
            model.addAttribute("equipment",
                    restTemplate.getForObject(EQUIPMENT_BACKEND_URL + "/" + id, EquipmentDTO.class));
        } catch (Exception e) {
            model.addAttribute("equipment", new EquipmentDTO());
            model.addAttribute("error", "Could not load equipment. Check that the equipment service is running.");
        }
        loadEquipment(model);
        return "equipment";
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

    @PostMapping("/submit-equipment")
    public String saveEquipment(@ModelAttribute("equipment") EquipmentDTO equipment,
                               RedirectAttributes redirectAttributes) {
        try {
            if (equipment.getId() == null) {
                restTemplate.postForObject(EQUIPMENT_BACKEND_URL + "/save", equipment, EquipmentDTO.class);
                redirectAttributes.addFlashAttribute("message", "Equipment registered.");
            } else {
                restTemplate.put(EQUIPMENT_BACKEND_URL + "/" + equipment.getId(), equipment);
                redirectAttributes.addFlashAttribute("message", "Equipment updated.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Could not save equipment. Check that the serial number is unique and the service is running.");
        }
        return "redirect:/equipment";
    }

    @PostMapping("/delete-equipment")
    public String deleteEquipment(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            restTemplate.delete(EQUIPMENT_BACKEND_URL + "/" + id);
            redirectAttributes.addFlashAttribute("message", "Equipment deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not delete equipment. Check that the service is running.");
        }
        return "redirect:/equipment";
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

    private void loadEquipment(Model model) {
        try {
            EquipmentDTO[] equipment = restTemplate.getForObject(EQUIPMENT_BACKEND_URL, EquipmentDTO[].class);
            model.addAttribute("equipmentList", equipment == null ? new EquipmentDTO[0] : equipment);
        } catch (Exception e) {
            model.addAttribute("equipmentList", new EquipmentDTO[0]);
            model.addAttribute("error", "Could not load equipment. Check that the equipment service and database are running.");
        }
    }

    private void loadMaintenance(Model model) {
        try {
            MaintenanceRecord[] records = restTemplate.getForObject(logisticsBackendUrl + "/maintenance", MaintenanceRecord[].class);
            model.addAttribute("maintenanceRecords", records == null ? List.of() : Arrays.asList(records));
        } catch (Exception exception) {
            model.addAttribute("maintenanceRecords", List.of());
            model.addAttribute("error", "Could not load maintenance records.");
        }
    }

    private void loadAllocations(Model model) {
        try {
            ResourceAllocation[] allocations = restTemplate.getForObject(logisticsBackendUrl + "/allocations", ResourceAllocation[].class);
            model.addAttribute("allocations", allocations == null ? List.of() : Arrays.asList(allocations));
        } catch (Exception exception) {
            model.addAttribute("allocations", List.of());
            model.addAttribute("error", "Could not load resource allocations.");
        }
    }

    public static class EquipmentDTO {
        private Long id;
        private String serialNumber;
        private String name;
        private String category;
        private String status;
        private String operationalCondition;
        private String location;
        private Long unitId;
        private String ownership;

        public EquipmentDTO() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getOperationalCondition() {
            return operationalCondition;
        }

        public void setOperationalCondition(String operationalCondition) {
            this.operationalCondition = operationalCondition;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Long getUnitId() {
            return unitId;
        }

        public void setUnitId(Long unitId) {
            this.unitId = unitId;
        }

        public String getOwnership() {
            return ownership;
        }

        public void setOwnership(String ownership) {
            this.ownership = ownership;
        }
    }
}
