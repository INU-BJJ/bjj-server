package com.appcenter.BJJ.domain.item.service;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import com.appcenter.BJJ.domain.item.dto.MyItemRes;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.repository.InventoryRepository;
import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final MemberRepository memberRepository;

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

        //설정한 아이템 착용 활성화
        inventory.toggleIsWearing();
    }
}
