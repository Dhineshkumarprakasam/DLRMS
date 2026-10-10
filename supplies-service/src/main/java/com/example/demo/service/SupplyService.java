package com.example.demo.service;

import com.example.demo.model.SupplyItem;
import com.example.demo.repository.SupplyItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SupplyService {

    private final SupplyItemRepository supplyItemRepository;

    public SupplyService(SupplyItemRepository supplyItemRepository) {
        this.supplyItemRepository = supplyItemRepository;
    }

    public List<SupplyItem> getAllItems() {
        return supplyItemRepository.findAll();
    }

    public SupplyItem getItem(Long id) {
        return supplyItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supply item not found."));
    }

    public SupplyItem saveItem(SupplyItem item) {
        validateItem(item);
        item.setItemCode(item.getItemCode().trim());
        if (supplyItemRepository.existsByItemCodeIgnoreCase(item.getItemCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item code is already in use.");
        }
        item.setName(item.getName().trim());
        item.setCategory(item.getCategory().trim());
        item.setUnit(item.getUnit().trim());
        if (item.getStorageLocation() != null) {
            item.setStorageLocation(item.getStorageLocation().trim());
        }
        return supplyItemRepository.save(item);
    }

    @Transactional
    public SupplyItem updateItem(Long id, SupplyItem item) {
        SupplyItem existing = getItem(id);
        validateItem(item);
        if (supplyItemRepository.existsByItemCodeIgnoreCaseAndIdNot(item.getItemCode().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item code is already in use.");
        }
        existing.setItemCode(item.getItemCode().trim());
        existing.setName(item.getName().trim());
        existing.setCategory(item.getCategory().trim());
        existing.setUnit(item.getUnit().trim());
        existing.setQuantityOnHand(item.getQuantityOnHand());
        existing.setReorderLevel(item.getReorderLevel());
        existing.setStorageLocation(item.getStorageLocation() == null ? null : item.getStorageLocation().trim());
        existing.setExpirationDate(item.getExpirationDate());
        return supplyItemRepository.save(existing);
    }

    @Transactional
    public void deleteItem(Long id) {
        if (!supplyItemRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Supply item not found.");
        }
        supplyItemRepository.deleteById(id);
    }

    private void validateItem(SupplyItem item) {
        if (item.getItemCode() == null || item.getItemCode().isBlank()
                || item.getName() == null || item.getName().isBlank()
                || item.getCategory() == null || item.getCategory().isBlank()
                || item.getUnit() == null || item.getUnit().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Item code, name, category, and unit are required.");
        }
        if (item.getQuantityOnHand() == null || item.getQuantityOnHand().compareTo(BigDecimal.ZERO) < 0
                || item.getReorderLevel() == null || item.getReorderLevel().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock quantity and reorder level must be zero or greater.");
        }
        if (item.getQuantityOnHand().scale() > 3 || item.getReorderLevel().scale() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantities can have at most three decimal places.");
        }
    }
}
