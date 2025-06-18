package com.appcenter.BJJ.domain.item.repository;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import com.appcenter.BJJ.domain.item.dto.MyItemRes;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("""
        SELECT inven FROM Inventory inven
        INNER JOIN Item item ON item.id = inven.itemId
        WHERE inven.memberId = :memberId
        AND inven.itemId = :itemId
        AND item.itemType = :itemType
        """)
    Optional<Inventory> findByMemberIdAndItemTypeAndItemId(Long memberId,ItemType itemType, Long itemId);

    @Query("""
            SELECT new com.appcenter.BJJ.domain.item.dto.MyItemRes(
            member.nickname,
            MAX(CASE WHEN item.itemType = 'CHARACTER' THEN item.id END),
            MAX(CASE WHEN item.itemType = 'CHARACTER' THEN item.itemName END),
            MAX(CASE WHEN item.itemType = 'BACKGROUND' THEN item.id END),
            MAX(CASE WHEN item.itemType = 'BACKGROUND' THEN item.itemName END),
            member.point
            )
            FROM Inventory inven
            INNER JOIN Member member ON inven.memberId = member.id
            INNER JOIN Item item ON inven.itemId = item.id
            WHERE inven.memberId = :memberId
            AND inven.isWearing = true
            GROUP BY member.id
            """)
    Optional<MyItemRes> findMyItemResByMemberId(Long memberId);

    @Query("""
            SELECT inven FROM Inventory inven
            LEFT JOIN Item item ON item.id = inven.itemId
            WHERE inven.memberId = :memberId
            AND inven.isWearing = true
            AND item.itemType = :itemType
            """)
    Optional<Inventory> findWearingItemByMemberIdAndItemType(Long memberId, ItemType itemType);

    @Query("""
            SELECT COUNT(*) = 1
            FROM Inventory inven
            LEFT JOIN Item item ON item.id = inven.itemId
            WHERE inven.memberId = :memberId
            AND inven.isWearing = true
            AND item.itemType = :itemType
            """)
    boolean existsWearingItemByMemberIdAndItemType(Long memberId, ItemType itemType);
}
