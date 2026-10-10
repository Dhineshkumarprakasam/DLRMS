package com.example.demo.repository;

import com.example.demo.model.SupplyItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyItemRepository extends JpaRepository<SupplyItem, Long> {

    boolean existsByItemCodeIgnoreCase(String itemCode);

    boolean existsByItemCodeIgnoreCaseAndIdNot(String itemCode, Long id);
}
