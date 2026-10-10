package com.example.demo.service;

import com.example.demo.model.Equipment;
import com.example.demo.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public Equipment getEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found."));
    }

    public Equipment createEquipment(Equipment equipment) {
        validate(equipment);
        equipment.setSerialNumber(equipment.getSerialNumber().trim());
        if (equipmentRepository.existsBySerialNumberIgnoreCase(equipment.getSerialNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serial number is already in use.");
        }
        normalize(equipment);
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment updateEquipment(Long id, Equipment equipment) {
        Equipment existing = getEquipment(id);
        validate(equipment);
        String serialNumber = equipment.getSerialNumber().trim();
        if (equipmentRepository.existsBySerialNumberIgnoreCaseAndIdNot(serialNumber, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serial number is already in use.");
        }

        existing.setSerialNumber(serialNumber);
        existing.setName(equipment.getName().trim());
        existing.setCategory(trimToNull(equipment.getCategory()));
        existing.setStatus(trimToNull(equipment.getStatus()));
        existing.setOperationalCondition(trimToNull(equipment.getOperationalCondition()));
        existing.setLocation(trimToNull(equipment.getLocation()));
        existing.setUnitId(equipment.getUnitId());
        existing.setOwnership(trimToNull(equipment.getOwnership()));
        return equipmentRepository.save(existing);
    }

    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found.");
        }
        equipmentRepository.deleteById(id);
    }

    private void validate(Equipment equipment) {
        if (equipment.getSerialNumber() == null || equipment.getSerialNumber().isBlank()
                || equipment.getName() == null || equipment.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serial number and equipment name are required.");
        }
        if (equipment.getSerialNumber().trim().length() > 50 || equipment.getName().trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serial number or equipment name is too long.");
        }
        if (equipment.getUnitId() != null && equipment.getUnitId() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit ID must be zero or greater.");
        }
    }

    private void normalize(Equipment equipment) {
        equipment.setName(equipment.getName().trim());
        equipment.setCategory(trimToNull(equipment.getCategory()));
        equipment.setStatus(trimToNull(equipment.getStatus()));
        equipment.setOperationalCondition(trimToNull(equipment.getOperationalCondition()));
        equipment.setLocation(trimToNull(equipment.getLocation()));
        equipment.setOwnership(trimToNull(equipment.getOwnership()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
