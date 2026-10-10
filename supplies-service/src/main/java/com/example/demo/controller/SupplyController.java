package com.example.demo.controller;

import com.example.demo.model.SupplyItem;
import com.example.demo.service.SupplyService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/supplies")
public class SupplyController {

    private final SupplyService supplyService;

    public SupplyController(SupplyService supplyService) {
        this.supplyService = supplyService;
    }

    @GetMapping
    public List<SupplyItem> getAllItems() {
        return supplyService.getAllItems();
    }

    @GetMapping("/{id}")
    public SupplyItem getItem(@PathVariable Long id) {
        return supplyService.getItem(id);
    }

    @PostMapping("/save")
    public SupplyItem saveItem(@RequestBody SupplyItem item) {
        return supplyService.saveItem(item);
    }

    @PutMapping("/{id}")
    public SupplyItem updateItem(@PathVariable Long id, @RequestBody SupplyItem item) {
        return supplyService.updateItem(id, item);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        supplyService.deleteItem(id);
    }

}
