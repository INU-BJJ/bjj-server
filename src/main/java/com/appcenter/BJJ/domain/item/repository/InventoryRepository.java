package com.appcenter.BJJ.domain.item.repository;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import com.appcenter.BJJ.domain.item.dto.MyItemRes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByMemberIdAndItemId(Long memberId, Integer itemId);

    @Query("""
            SELECT new com.appcenter.BJJ.domain.item.dto.MyItemRes(
            member.nickname,
            item.itemId,
            item.imageName,
            member.point
            )
            FROM Inventory inven
            INNER JOIN Item item
            ON inven.itemId = item.itemId
            INNER JOIN Member member
            ON inven.memberId = member.id
            WHERE inven.memberId = :memberId
            AND inven.isWearing = true
            """)
    Optional<MyItemRes> findMyItemResByMemberId(Long memberId);

    @Query("""
            SELECT inven FROM Inventory inven
            WHERE inven.memberId = :memberId
            AND inven.isWearing = true
            """)
    Optional<Inventory> findWearingItemByMemberId(Long memberId);


    @Query("""
            SELECT COUNT(*) = 1
            FROM Inventory inven
            WHERE inven.memberId = :memberId
            AND inven.isWearing = true
            """)
    boolean existsIsWearingByMemberId(Long memberId);
}
