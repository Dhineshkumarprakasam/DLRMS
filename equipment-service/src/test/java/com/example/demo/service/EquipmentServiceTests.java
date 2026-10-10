package com.example.demo.service;

import com.example.demo.model.Equipment;
import com.example.demo.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTests {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    @Test
    void createEquipmentTrimsFieldsAndPersistsEquipment() {
        Equipment equipment = validEquipment();
        equipment.setSerialNumber(" EQ-001 ");
        equipment.setName(" Radio set ");
        equipment.setLocation(" Depot A ");
        when(equipmentRepository.save(same(equipment))).thenReturn(equipment);

        Equipment saved = equipmentService.createEquipment(equipment);

        assertEquals("EQ-001", saved.getSerialNumber());
        assertEquals("Radio set", saved.getName());
        assertEquals("Depot A", saved.getLocation());
        verify(equipmentRepository).save(equipment);
    }

    @Test
    void createEquipmentRejectsDuplicateSerialNumber() {
        Equipment equipment = validEquipment();
        when(equipmentRepository.existsBySerialNumberIgnoreCase("EQ-001")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> equipmentService.createEquipment(equipment));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void createEquipmentRejectsMissingRequiredValues() {
        Equipment equipment = new Equipment();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> equipmentService.createEquipment(equipment));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private Equipment validEquipment() {
        Equipment equipment = new Equipment();
        equipment.setSerialNumber("EQ-001");
        equipment.setName("Radio set");
        equipment.setCategory("Communications");
        equipment.setStatus("AVAILABLE");
        equipment.setOperationalCondition("SERVICEABLE");
        equipment.setUnitId(1L);
        equipment.setOwnership("Government");
        return equipment;
    }
}
