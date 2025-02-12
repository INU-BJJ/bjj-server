package com.appcenter.BJJ.domain.item.service;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import com.appcenter.BJJ.domain.item.domain.Item;
import com.appcenter.BJJ.domain.item.dto.DetailItemRes;
import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.repository.InventoryRepository;
import com.appcenter.BJJ.domain.item.repository.ItemRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;

    public DetailItemRes gacha(Long memberId, ItemType itemType) {

        //아이템 랜덤으로 뽑기
        int itemSize = (int) itemRepository.count();
        int gachaId = getRandomInt(itemSize);
        ItemLevel itemLevel = getItemLevel(gachaId);

        List<Item> itemList = itemRepository.findByItemLevelAndItemType(itemLevel, itemType);
        Integer itemId = getRandomInt(itemList.size());

        Item item = itemRepository.findByItemId(itemId).orElseThrow(
                () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
        );

        //뽑은 아이템 저장
        Inventory inventory = inventoryRepository.findByMemberIdAndItemId(memberId, itemId).orElse(
                //새로 뽑은 것
                Inventory.builder()
                        .memberId(memberId)
                        .itemId(item.getItemId())
                        .isWearing(false)
                        .isOwned(true)
                        .validPeriod(LocalDateTime.now())
                        .build()
        );

        inventory.updateValidPeriodAndIsOwned(inventory.getValidPeriod());
        inventoryRepository.save(inventory);

        log.info("gacha(): itemId= {}", itemId);

        return DetailItemRes.builder()
                .itemId(item.getItemId())
                .itemName(item.getItemName())
                .itemType(item.getItemType())
                .itemLevel(item.getItemLevel())
                .imageName(item.getImageName())
                .validPeriod(inventory.getValidPeriod())
                .isWearing(inventory.getIsWearing())
                .isOwned(inventory.getIsOwned())
                .build();

    }

    public List<DetailItemRes> getItems(Long memberId) {
        log.info("getItems(): 들어옴");
        List<DetailItemRes> itemList = itemRepository.getAllDetailItemsByMemberId(memberId);
        log.info("getItems(): 아이템 리스트 이름 {}", itemList.get(0).getItemName());
        return itemList;
    }

    public DetailItemRes getItem(Long memberId, Integer itemId) {
        return itemRepository.getDetailItemByMemberIdAndItemId(memberId, itemId).orElseThrow(
                () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
        );
    }

    public Integer getRandomInt(int bound) {
        Random random = new Random();
        return random.nextInt(bound) + 1;
    }

    public ItemLevel getItemLevel(int itemId) {
        // COMMON : 70%, RARE : 20%, LEGENDARY : 10% 확률
        if (itemId <= 70) {
            return ItemLevel.COMMON;
        } else if (itemId <= 90) {
            return ItemLevel.RARE;
        } else {
            return ItemLevel.LEGENDARY;
        }
    }
}
