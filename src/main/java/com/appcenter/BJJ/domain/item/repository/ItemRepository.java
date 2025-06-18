package com.appcenter.BJJ.domain.item.repository;

import com.appcenter.BJJ.domain.item.domain.Item;
import com.appcenter.BJJ.domain.item.dto.DetailItemRes;
import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> getItemsByItemLevelAndItemType(ItemLevel itemLevel, ItemType itemType);

    @Query("""
            SELECT new com.appcenter.BJJ.domain.item.dto.DetailItemRes(
            item.id,
            item.itemName,
            item.itemType,
            item.itemLevel,
            inven.validPeriod,
            coalesce(inven.isWearing, false),
            coalesce(inven.isOwned, false)
            )
            From Item item
            LEFT JOIN Inventory inven ON item.id = inven.itemId
            AND inven.memberId = :memberId
            WHERE item.itemType = :itemType
            """)
    List<DetailItemRes> getAllDetailItemsByMemberIdAndItemType(Long memberId, ItemType itemType);

    @Query("""
            SELECT new com.appcenter.BJJ.domain.item.dto.DetailItemRes(
            item.id,
            item.itemName,
            item.itemType,
            item.itemLevel,
            inven.validPeriod,
            coalesce(inven.isWearing, false),
            coalesce(inven.isOwned, false))
            FROM Item item
            LEFT JOIN Inventory inven ON inven.itemId = :itemId
            AND inven.memberId = :memberId
            WHERE item.id = :itemId
            AND item.itemType = :itemType
            """)
    Optional<DetailItemRes> findDetailItemByIdAndMemberIdAndItemType(Long memberId, Long itemId, ItemType itemType);

    @Query("SELECT COUNT(i) > 0 FROM Item i")
    boolean existsAny();

    @Modifying
    @Transactional
    @Query("DELETE FROM Item")
    void deleteAllRows();
}
