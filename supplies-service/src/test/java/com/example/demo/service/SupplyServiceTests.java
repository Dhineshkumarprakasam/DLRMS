package com.example.demo.service;

import com.example.demo.model.SupplyItem;
import com.example.demo.repository.SupplyItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyServiceTests {

    @Mock
    private SupplyItemRepository supplyItemRepository;

    @InjectMocks
    private SupplyService supplyService;

    @Test
    void saveItemTrimsFieldsAndPersistsInventoryItem() {
        SupplyItem item = new SupplyItem();
        item.setItemCode(" MED-01 ");
        item.setName(" First aid kit ");
        item.setCategory(" MEDICAL ");
        item.setUnit(" kits ");
        item.setQuantityOnHand(new BigDecimal("12.500"));
        item.setReorderLevel(new BigDecimal("2.000"));

        when(supplyItemRepository.save(same(item))).thenReturn(item);

        SupplyItem saved = supplyService.saveItem(item);

        assertEquals("MED-01", saved.getItemCode());
        assertEquals("First aid kit", saved.getName());
        assertEquals("MEDICAL", saved.getCategory());
        assertEquals("kits", saved.getUnit());
        verify(supplyItemRepository).save(item);
    }

    @Test
    void saveItemRejectsDuplicateItemCode() {
        SupplyItem item = new SupplyItem();
        item.setItemCode("MED-01");
        item.setName("First aid kit");
        item.setCategory("MEDICAL");
        item.setUnit("kits");
        item.setQuantityOnHand(BigDecimal.ONE);
        item.setReorderLevel(BigDecimal.ZERO);
        when(supplyItemRepository.existsByItemCodeIgnoreCase("MED-01")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> supplyService.saveItem(item));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void saveItemRejectsNegativeStock() {
        SupplyItem item = new SupplyItem();
        item.setItemCode("MED-01");
        item.setName("First aid kit");
        item.setCategory("MEDICAL");
        item.setUnit("kits");
        item.setQuantityOnHand(new BigDecimal("-1"));
        item.setReorderLevel(BigDecimal.ZERO);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> supplyService.saveItem(item));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
