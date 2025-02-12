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

    Optional<Item> findByItemId(int ItemId);

    @Query("select new com.appcenter.BJJ.domain.item.dto.DetailItemRes(" +
            "item.itemId, item.itemName, item.itemType, item.itemLevel, item.imageName, " +
            "coalesce(inven.validPeriod, CURRENT TIMESTAMP ), coalesce(inven.isWearing, false),coalesce(inven.isOwned, false)) " +
            "from Item item left join Inventory inven on item.itemId = inven.itemId and inven.memberId = :memberId")
    List<DetailItemRes> getAllDetailItemsByMemberId(Long memberId);

    @Query("select new com.appcenter.BJJ.domain.item.dto.DetailItemRes(" +
            "item.itemId, item.itemName, item.itemType, item.itemLevel, item.imageName, " +
            "coalesce(inven.validPeriod, CURRENT_TIMESTAMP), coalesce(inven.isWearing, false),coalesce(inven.isOwned, false)) " +
            "from Item item left join Inventory inven on item.itemId = inven.itemId and inven.memberId = :memberId where item.itemId = :itemId")
    Optional<DetailItemRes> getDetailItemByMemberIdAndItemId(Long memberId, Integer itemId);
}
