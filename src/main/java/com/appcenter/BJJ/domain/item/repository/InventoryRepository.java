package com.appcenter.BJJ.domain.item.repository;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import com.appcenter.BJJ.domain.item.dto.MyItemRes;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("""
            SELECT inven FROM Inventory inven
            WHERE inven.memberId = :memberId
            AND inven.itemType = :itemType
            AND inven.itemIdx = :itemIdx
            """)
    Optional<Inventory> findByMemberIdAndItemTypeAndItemIdx(Long memberId, ItemType itemType, Integer itemIdx);

    @Query("""
            SELECT new com.appcenter.BJJ.domain.item.dto.MyItemRes(
            member.nickname,
            MAX(CASE WHEN item.itemType = 'CHARACTER' THEN item.itemIdx END),
            MAX(CASE WHEN item.itemType = 'CHARACTER' THEN item.imageName END),
            MAX(CASE WHEN item.itemType = 'BACKGROUND' THEN item.itemIdx END),
            MAX(CASE WHEN item.itemType = 'BACKGROUND' THEN item.imageName END),
            member.point
            )
            FROM Inventory inven
            INNER JOIN Member member ON inven.memberId = member.id
            INNER JOIN Item item ON inven.itemIdx = item.itemIdx AND item.itemType = inven.itemType
            WHERE inven.memberId = :memberId
            AND inven.isWearing = true
            GROUP BY member.id
            """)
    Optional<MyItemRes> findMyItemResByMemberId(Long memberId);

    @Query("""
            SELECT inven FROM Inventory inven
            WHERE inven.memberId = :memberId
            AND inven.isWearing = true
            AND inven.isOwned = true
            AND inven.itemType = :itemType
            """)
    Optional<Inventory> findWearingItemByMemberIdAndItemType(Long memberId, ItemType itemType);

    @Query("""
            SELECT COUNT(*) = 1
            FROM Inventory inven
            WHERE inven.memberId = :memberId
            AND inven.isWearing = true
            AND inven.itemType = :itemType
            """)
    boolean existsWearingItemByMemberIdAndItemType(Long memberId, ItemType itemType);

    @Modifying
    @Transactional
    void deleteByMemberIdAndItemIdxAndItemType(Long memberId, Integer itemIdx, ItemType itemType);
}
