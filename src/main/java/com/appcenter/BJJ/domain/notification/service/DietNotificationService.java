package com.appcenter.BJJ.domain.notification.service;

import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.menu.dto.MenuInfoDto;
import com.appcenter.BJJ.domain.menu.repository.MenuRepository;
import com.appcenter.BJJ.domain.notification.dto.MemberDeviceTokenDto;
import com.appcenter.BJJ.domain.notification.dto.NotifiableMemberDto;
import com.appcenter.BJJ.domain.notification.dto.NotificationInfoDto;
import com.appcenter.BJJ.domain.notification.repository.DeviceTokenRepository;
import com.appcenter.BJJ.domain.todaydiet.repository.TodayDietRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DietNotificationService {

    private final MemberRepository memberRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final TodayDietRepository todayDietRepository;
    private final MenuRepository menuRepository;

    public List<NotificationInfoDto> collectNotificationTargets(LocalDate date) {
        log.info("[로그] {} 날짜의 메뉴 알림 시작", date);

        // 해당 날짜의 모든 메뉴 ID 수집
        List<Long> menuIds = collectMenuIdsByDate(date);

        // 메뉴 ID → 메뉴 상세 정보 매핑
        Map<Long, MenuInfoDto> menuIdToMenuInfoMap = mapMenuIdToInfo(menuIds);

        // 알림 수신 동의 + 메뉴 좋아요한 회원 목록 조회
        List<NotifiableMemberDto> notifiableMemberDtos = memberRepository.findNotifiableMembersByLikedMenus(menuIds);
        Set<Long> memberIds = notifiableMemberDtos.stream()
                .map(NotifiableMemberDto::getMemberId)
                .collect(Collectors.toSet());

        log.debug("[로그] 알림 대상 회원 수: {}, 일부 메뉴 및 회원 정보: {}", memberIds.size(), notifiableMemberDtos.stream().limit(5).toList());

        // 회원 ID → FCM 토큰 리스트 매핑
        Map<Long, List<String>> memberIdToTokensMap = mapMemberIdToTokens(memberIds);

        // 메뉴 ID → FCM 토큰 리스트 매핑
        Map<Long, List<String>> menuIdToTokens = mapMenuIdToTokens(notifiableMemberDtos, memberIdToTokensMap, menuIdToMenuInfoMap);

        // 메뉴별 FCM 알림 정보 구성
        return menuIdToTokens.entrySet().stream()
                .map(entry -> {
                    Long menuId = entry.getKey();
                    List<String> tokens = entry.getValue();
                    MenuInfoDto menuInfoDto = menuIdToMenuInfoMap.get(menuId);

                    return NotificationInfoDto.builder()
                            .menuName(menuInfoDto.getMenuName())
                            .cafeteriaName(menuInfoDto.getCafeteriaName())
                            .cafeteriaCorner(menuInfoDto.getCafeteriaCorner())
                            .fcmTokens(tokens)
                            .build();
                }).toList();
    }

    private List<Long> collectMenuIdsByDate(LocalDate date) {
        Set<Long> menuIdSet = new HashSet<>();
        menuIdSet.addAll(todayDietRepository.findMainMenuIdsByDate(date));
        menuIdSet.addAll(todayDietRepository.findSubMenuIdsByDate(date));  // 조회 시 서브 메뉴 ID가 Null인 경우는 제외
        List<Long> result = menuIdSet.stream().toList();
        log.debug("[로그] 오늘 식단의 메인 메뉴 및 서브 메뉴 ID 조회, 총 메뉴 개수 = {}, 메뉴 일부 = {}", result.size(), result.stream().limit(5).toList());

        return result;
    }

    private Map<Long, MenuInfoDto> mapMenuIdToInfo(List<Long> menuIdList) {
        List<MenuInfoDto> menuInfoDtos = menuRepository.findMenusWithCafeteriaInMenuIds(menuIdList);
        Map<Long, MenuInfoDto> result = menuInfoDtos.stream()
                .collect(Collectors.toMap(MenuInfoDto::getMenuId, Function.identity()));
        log.debug("[로그] 메뉴 상세 정보 조회, 총 메뉴 상세 정보 개수  = {}, 메뉴 상세 일부 = {}", result.size(), result.entrySet().stream().limit(5).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return result;
    }

    private Map<Long, List<String>> mapMemberIdToTokens(Set<Long> memberIds) {
        List<MemberDeviceTokenDto> memberDeviceTokenDtos = deviceTokenRepository.findActiveTokensInMemberIds(memberIds.stream().toList());
        Map<Long, List<String>> result = memberDeviceTokenDtos.stream()
                .collect(Collectors.groupingBy(MemberDeviceTokenDto::getMemberId,
                        Collectors.mapping(MemberDeviceTokenDto::getToken, Collectors.toList())));
        log.debug("[로그] 회원별 활성화 된 기기 토큰 조회, 총 토큰 개수 = {}, 회원별 토큰 일부 = {}", memberDeviceTokenDtos.size(), result.entrySet().stream().limit(5).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return result;
    }

    private static Map<Long, List<String>> mapMenuIdToTokens(
            List<NotifiableMemberDto> notifiableMemberDtos,
            Map<Long, List<String>> memberIdToTokensMap,
            Map<Long, MenuInfoDto> menuIdToMenuInfoMap
    ) {
        Map<Long, Set<String>> menuIdToTokensSet = new HashMap<>();

        for (NotifiableMemberDto notifiableMemberDto : notifiableMemberDtos) {
            List<String> tokens = memberIdToTokensMap.getOrDefault(notifiableMemberDto.getMemberId(), List.of());
            if (tokens.isEmpty()) continue;

            if (!menuIdToMenuInfoMap.containsKey(notifiableMemberDto.getMenuId())) continue;

            menuIdToTokensSet
                    .computeIfAbsent(notifiableMemberDto.getMenuId(), k -> new HashSet<>())
                    .addAll(tokens);
        }

        return menuIdToTokensSet.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new ArrayList<>(e.getValue())
                ));
    }
}
