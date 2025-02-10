package com.appcenter.BJJ.domain.item.repository;

import com.appcenter.BJJ.domain.item.domain.Item;
import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByItemLevelAndItemType(ItemLevel itemLevel, ItemType itemType);

    Optional<Item> findByItemId(int ItemId);
}
