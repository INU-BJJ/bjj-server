package com.appcenter.BJJ.domain.item.service;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import com.appcenter.BJJ.domain.item.domain.Item;
import com.appcenter.BJJ.domain.item.dto.DetailItemRes;
import com.appcenter.BJJ.domain.item.dto.MyItemRes;
import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.repository.InventoryRepository;
import com.appcenter.BJJ.domain.item.repository.ItemRepository;
import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private static final int GACHA_RANGE = 10;

    public MyItemRes getMyItem(Long memberId) {
        return inventoryRepository.findMyItemResByMemberId(memberId).orElseGet(
                //아이템이 하나도 없으면 null값 return (기본 아이템 장착)
                () -> MyItemRes.builder().build()
        );
    }

    @Transactional
    public DetailItemRes gacha(Long memberId, ItemType itemType) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        member.decreasePoint(getRequiredPointForGacha(member.getPoint(), itemType));

        //아이템 랜덤으로 뽑기
        int gachaNum = getRandomInt(GACHA_RANGE);
        ItemLevel itemLevel = getItemLevel(gachaNum);

        List<Item> itemList = itemRepository.findByItemLevelAndItemType(itemLevel, itemType);
        Integer itemId = getRandomInt(itemList.size());
        Item item = itemList.get(itemId - 1);

        //뽑은 아이템 저장
        Inventory inventory = inventoryRepository.findByMemberIdAndItemId(memberId, item.getItemId()).orElse(
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
        return itemRepository.getAllDetailItemsByMemberId(memberId);
    }

    public DetailItemRes getItem(Long memberId, Integer itemId) {
        return itemRepository.getDetailItemByMemberIdAndItemId(memberId, itemId).orElseThrow(
                () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
        );
    }

    @Transactional
    public void toggleIsWearing(Long memberId, Integer itemId) {
        if (inventoryRepository.existsWearingItemByMemberId(memberId)) {
            Inventory currentInven = inventoryRepository.findWearingItemByMemberId(memberId).orElseThrow(
                    () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
            );
            //현재 아이템 착용 비활성화
            currentInven.toggleIsWearing();
        }

        Inventory inventory = inventoryRepository.findByMemberIdAndItemId(memberId, itemId).orElseThrow(
                () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
        );
        //설정한 아이템 착용 활성화
        inventory.toggleIsWearing();
    }

    private int getRequiredPointForGacha(int point, ItemType itemType) {
        if (itemType == ItemType.CHARACTER && point - 50 >= 0) {
            return 50;
        } else if (itemType == ItemType.BACKGROUND && point - 100 >= 0) {
            return 100;
        } else {
            throw new CustomException(ErrorCode.NOT_ENOUGH_POINTS);
        }
    }


    //////////
    private Integer getRandomInt(int bound) {
        Random random = new Random();
        return random.nextInt(bound) + 1;
    }

    private ItemLevel getItemLevel(int itemId) {
        // COMMON : 70%, RARE : 20%, LEGENDARY : 10% 확률
        if (itemId <= 7) {
            return ItemLevel.COMMON;
        } else if (itemId <= 9) {
            return ItemLevel.RARE;
        } else {
            return ItemLevel.LEGENDARY;
        }
    }
}
