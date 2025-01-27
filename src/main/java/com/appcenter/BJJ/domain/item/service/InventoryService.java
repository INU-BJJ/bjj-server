package com.appcenter.BJJ.domain.item.service;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import com.appcenter.BJJ.domain.item.domain.Item;
import com.appcenter.BJJ.domain.item.dto.GotchaRes;
import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.repository.InventoryRepository;
import com.appcenter.BJJ.domain.item.repository.ItemRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;

    public GotchaRes gotcha(Long memberId, ItemType itemType) {

        //[notice] 이후에 100으로 바꾸기
        int gotchaId = getRandomInt(10);
        ItemLevel itemLevel = getItemLevel(gotchaId);
        List<Item> itemList = itemRepository.findByItemLevelAndItemType(itemLevel, itemType);
        int itemId = getRandomInt(itemList.size());
        log.info("gotcha(): itemId= {}", itemId);

        Item item = itemRepository.findByItemId(itemId).orElseThrow(
                () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
        );

        Inventory inventory = Inventory.builder()
                .memberId(memberId)
                .itemId(item.getItemId())
                .isWearing(false)
                .validPeriod(LocalDate.now().plusDays(7))
                .build();
        inventoryRepository.save(inventory);

        log.info("gotcha(): itemId= {}", itemId);

        return GotchaRes.builder()
                .itemId(item.getItemId())
                .itemName(item.getImageName())
                .itemType(item.getItemType())
                .itemLevel(item.getItemLevel())
                .validPeriod(inventory.getValidPeriod())
                .build();

    }

    public int getRandomInt(int bound) {
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
