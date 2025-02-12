package com.appcenter.BJJ.domain.item.repository;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
