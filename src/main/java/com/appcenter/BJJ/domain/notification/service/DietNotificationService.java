package com.appcenter.BJJ.domain.notification.service;

import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.menu.dto.MenuInfoDto;
import com.appcenter.BJJ.domain.menu.repository.MenuRepository;
import com.appcenter.BJJ.domain.notification.dto.NotificationInfoDto;
import com.appcenter.BJJ.domain.notification.dto.NotifiableMemberDto;
import com.appcenter.BJJ.domain.notification.dto.MemberDeviceTokenDto;
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

    public Map<Long, List<NotificationInfoDto>> collectNotificationTargets(LocalDate date) {
        log.info("[로그] {} 날짜의 메뉴 알림 시작", date);

        // 해당 날짜 식단의 메인 및 서브 메뉴 ID 조회
        Set<Long> menuIdSet = new HashSet<>();
        menuIdSet.addAll(todayDietRepository.findMainMenuIdsByDate(date));
        menuIdSet.addAll(todayDietRepository.findSubMenuIdsByDate(date));  // 조회 시 서브 메뉴 ID가 Null인 경우는 제외
        List<Long> menuIdList = menuIdSet.stream().toList();
        log.debug("[로그] 오늘 식단의 메인 메뉴 및 서브 메뉴 ID 조회, menuIdList.size = {}, menuIdList = {}", menuIdList.size(), menuIdList);

        // 메뉴 ID 목록의 상세 정보 조회 (메뉴 이름, 식당 이름, 식당 코너 이름)
        List<MenuInfoDto> menuInfoDtos = menuRepository.findMenusWithCafeteriaInMenuIds(menuIdList);
        Map<Long, MenuInfoDto> menuInfoMap = menuInfoDtos.stream()
                .collect(Collectors.toMap(MenuInfoDto::getMenuId, Function.identity()));
        log.debug("[로그] 메뉴 ID 목록의 상세 정보 조회, menuInfoMap.size = {}, menuInfoMap = {}", menuInfoMap.size(), menuInfoMap);

        // 각 메뉴에 좋아요를 누른 회원 중 알림 설정을 켜 놓은 회원 정보 조회
        List<NotifiableMemberDto> notifiableMemberDtos = memberRepository.findNotifiableMembersByLikedMenus(menuIdList);
        List<Long> memberIds = notifiableMemberDtos.stream().map(NotifiableMemberDto::getMemberId).distinct().toList();
        log.debug("[로그] 각 메뉴에 좋아요를 누른 회원 중 알림 설정을 켜 놓은 회원 정보 조회, notifiableMemberDtos.size = {}, notifiableMemberDtos = {}", notifiableMemberDtos.size(), notifiableMemberDtos);
        log.debug("[로그] 각 메뉴에 좋아요를 누른 회원 중 알림 설정을 켜 놓은 회원 ID 조회 (중복 X), memberIds.size = {}, memberIds = {}", memberIds.size(), memberIds);

        // 각 회원의 기기 토큰 조회
        List<MemberDeviceTokenDto> memberDeviceTokenDtos = deviceTokenRepository.findTokensInMemberIds(memberIds);
        Map<Long, List<String>> memberTokenMap = memberDeviceTokenDtos.stream()
                .collect(Collectors.groupingBy(MemberDeviceTokenDto::getMemberId,
                        Collectors.mapping(MemberDeviceTokenDto::getToken, Collectors.toList())));
        log.debug("[로그] 각 회원의 기기 토큰 중 알림 설정을 켜 놓은 기기 토큰 조회, memberTokenMap.size = {}, memberTokenMap = {}", memberTokenMap.size(), memberTokenMap);

        // 각 회원 별 메뉴 정보 및 기기 토큰 목록으로 취합
        Map<Long, List<NotificationInfoDto>> notificationInfoMap = new HashMap<>();
        for (NotifiableMemberDto notifiableMemberDto : notifiableMemberDtos) {
            MenuInfoDto menuInfo = menuInfoMap.get(notifiableMemberDto.getMenuId());
            if (menuInfo == null) continue;

            List<String> fcmTokens = memberTokenMap.getOrDefault(notifiableMemberDto.getMemberId(), List.of());

            NotificationInfoDto notification = NotificationInfoDto.builder()
                    .memberNickname(notifiableMemberDto.getMemberNickname())
                    .menuName(menuInfo.getMenuName())
                    .cafeteriaName(menuInfo.getCafeteriaName())
                    .cafeteriaCorner(menuInfo.getCafeteriaCorner())
                    .fcmTokens(fcmTokens)
                    .build();

            notificationInfoMap
                    .computeIfAbsent(notifiableMemberDto.getMemberId(), k -> new ArrayList<>())
                    .add(notification);
        }

        return notificationInfoMap;
    }
}
