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
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        return inventoryRepository.findMyItemResByMemberId(memberId).orElseGet(
                //아이템이 하나도 없으면 null값 return (기본 아이템 장착)
                () -> MyItemRes.builder()
                        .nickname(member.getNickname())
                        .point(member.getPoint())
                        .build()
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

        List<Item> itemList = itemRepository.getItemsByItemLevelAndItemType(itemLevel, itemType);
        Integer itemId = getRandomInt(itemList.size());
        Item item = itemList.get(itemId - 1);

        //뽑은 아이템 저장
        Inventory inventory = inventoryRepository.findByMemberIdAndItemTypeAndItemIdx(memberId, itemType, item.getItemIdx()).orElse(
                //새로 뽑은 것
                Inventory.builder()
                        .memberId(memberId)
                        .itemIdx(item.getItemIdx())
                        .isWearing(false)
                        .isOwned(true)
                        .validPeriod(LocalDateTime.now())
                        .build()
        );

        inventory.updateValidPeriodAndIsOwned(inventory.getValidPeriod());
        inventoryRepository.save(inventory);

        return DetailItemRes.builder()
                .itemIdx(item.getItemIdx())
                .itemName(item.getItemName())
                .itemType(item.getItemType())
                .itemLevel(item.getItemLevel())
                .validPeriod(inventory.getValidPeriod())
                .isWearing(inventory.getIsWearing())
                .isOwned(inventory.getIsOwned())
                .build();

    }

    public List<DetailItemRes> getItems(Long memberId, ItemType itemType) {
        return itemRepository.getAllDetailItemsByMemberIdAndItemType(memberId, itemType);
    }

    public DetailItemRes getItem(Long memberId, Integer itemIdx, ItemType itemType) {
        return itemRepository.findDetailItemByIdAndMemberIdAndItemType(memberId, itemIdx, itemType).orElseThrow(
                () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
        );
    }

    @Transactional
    public void toggleIsWearing(Long memberId, ItemType itemType, Integer itemIdx) {
        if (inventoryRepository.existsWearingItemByMemberIdAndItemType(memberId, itemType)) {
            Inventory currentInven = inventoryRepository.findWearingItemByMemberIdAndItemType(memberId, itemType).orElseThrow(
                    () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
            );

            //현재 아이템 착용 비활성화
            currentInven.toggleIsWearing();
        }

        Inventory inventory = inventoryRepository.findByMemberIdAndItemTypeAndItemIdx(memberId, itemType, itemIdx).orElseThrow(
                () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
        );
        System.out.println("inventory: " + inventory.getItemIdx());

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

    private ItemLevel getItemLevel(int randomNum) {
        // COMMON : 70%, RARE : 20%, LEGENDARY : 10% 확률
        if (randomNum <= 7) {
            return ItemLevel.COMMON;
        } else if (randomNum <= 9) {
            return ItemLevel.NORMAL;
        } else {
            return ItemLevel.RARE;
        }
    }
}
