package com.appcenter.BJJ.domain.item.repository;

import com.appcenter.BJJ.domain.item.domain.Item;
import com.appcenter.BJJ.domain.item.dto.DetailItemRes;
import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByItemLevelAndItemType(ItemLevel itemLevel, ItemType itemType);

    Optional<Item> findByItemId(Integer ItemId);

    @Query("""
            SELECT new com.appcenter.BJJ.domain.item.dto.DetailItemRes(
            item.itemId,
            item.itemName,
            item.itemType,
            item.itemLevel,
            item.imageName,
            inven.validPeriod,
            coalesce(inven.isWearing, false),
            coalesce(inven.isOwned, false)
            )
            From Item item
            LEFT JOIN Inventory inven
            ON item.itemId = inven.itemId
            AND inven.memberId = :memberId
            """)
    List<DetailItemRes> getAllDetailItemsByMemberId(Long memberId);

    @Query("""
            SELECT new com.appcenter.BJJ.domain.item.dto.DetailItemRes(
            item.itemId,
            item.itemName,
            item.itemType,
            item.itemLevel,
            item.imageName,
            inven.validPeriod,
            coalesce(inven.isWearing, false),
            coalesce(inven.isOwned, false))
            FROM Item item
            LEFT JOIN Inventory inven
            ON item.itemId = inven.itemId
            AND inven.memberId = :memberId
            WHERE item.itemId = :itemId
            """)
    Optional<DetailItemRes> getDetailItemByMemberIdAndItemId(Long memberId, Integer itemId);
}
