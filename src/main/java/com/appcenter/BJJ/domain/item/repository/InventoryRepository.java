package com.appcenter.BJJ.domain.item.repository;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByMemberIdAndItemId(Long memberId, Integer itemId);
}
