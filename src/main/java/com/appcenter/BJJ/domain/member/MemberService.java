package com.appcenter.BJJ.domain.member;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.repository.InventoryRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.dto.*;
import com.appcenter.BJJ.domain.member.enums.MemberRole;
import com.appcenter.BJJ.domain.member.enums.MemberStatus;
import com.appcenter.BJJ.domain.member.enums.MemberTaskType;
import com.appcenter.BJJ.domain.member.enums.SocialProvider;
import com.appcenter.BJJ.domain.member.schedule.MemberTaskHandler;
import com.appcenter.BJJ.domain.member.schedule.MemberTaskService;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import com.appcenter.BJJ.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final InventoryRepository inventoryRepository;
    private final MemberTaskService memberTaskService;
    private final MemberTaskHandler memberTaskHandler;

    @Transactional
    public String socialLogin(SocialLoginReq socialLoginReq) {
        Member member = memberRepository.findByProviderAndProviderId(socialLoginReq.getProvider(), socialLoginReq.getProviderId()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND) // 새로운 회원 => 프론트에게 404를 보내고 회원가입 유도
        );

        LocalDateTime now = LocalDateTime.now();
        if (member.getMemberStatus() == MemberStatus.DELETE && memberTaskHandler.isWithinDeletePeriod(member.getId(), now)) {
            // 탈퇴 유예 기간 내 재가입 시 기존 탈퇴를 무효화하고 재가입을 허용
            member.updateMemberStatus(MemberStatus.ACTIVE);
            memberTaskHandler.deleteTask(member.getId(), MemberTaskType.DELETE);
        }

        return this.getToken(member.getProviderId());
    }

    @Transactional
    public String signUp(SignupReq signupReq) {
        this.isNicknameAvailable(signupReq.getNickname());

        //회원 생성
        Member member = Member.create(signupReq.getNickname(), signupReq.getEmail(), signupReq.getProvider(), signupReq.getProviderId());
        memberRepository.save(member);

        //기본 아이템 새성
        inventoryRepository.save(Inventory.createDefault(member.getId(), ItemType.CHARACTER));
        inventoryRepository.save(Inventory.createDefault(member.getId(), ItemType.BACKGROUND));

        return this.getToken(member.getProviderId());
    }

    public MemberRes getMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        return MemberRes.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .provider(member.getProvider())
                .build();
    }

    //Soft Delete
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        //회원 상태를 탈퇴 상태로 변경
        LocalDateTime now = LocalDateTime.now();
        member.updateMemberStatus(MemberStatus.DELETE);
        memberTaskService.addOrUpdateTask(member.getId(), now, now.plusMonths(1), MemberTaskType.DELETE);
    }

    private String getToken(String providerId) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(providerId, "");
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        return jwtProvider.generateToken(authentication, JwtProvider.validAccessTime);
    }

    public boolean isNicknameAvailable(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_REGISTERED);
        }
        return true;
    }

    @Transactional
    public String changeNickname(String currentNickname, String newNickname) {
        isNicknameAvailable(newNickname);

        Member member = memberRepository.findByNickname(currentNickname).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        member.updateNickname(newNickname);
        return newNickname;
    }

    //TODO test용이기에 이후에 없애기
    @Transactional
    public String socialLogin(LoginReq loginReq) {
        log.info("MemberService.login() - 진입");

        Member member = memberRepository.findByEmailAndProvider(loginReq.getEmail(), SocialProvider.GOOGLE).orElseGet(
                () -> {
                    isNicknameAvailable(loginReq.getNickname());
                    log.info("test: login nickname {}", loginReq.getNickname());
                    return Member.builder()
                            .email(loginReq.getEmail())
                            .nickname(loginReq.getNickname())
                            .provider(SocialProvider.GOOGLE)
                            .providerId("0")
                            .build();
                }
        );
        memberRepository.save(member);
        member.updateTestProviderId(String.valueOf(member.getId()));
        member.updateMemberInfo(loginReq.getNickname(), MemberRole.USER);


        //기본 아이템 새성
        inventoryRepository.findByMemberIdAndItemTypeAndItemIdx(member.getId(), ItemType.CHARACTER, 0).orElseGet(
                () -> {
                    inventoryRepository.save(Inventory.createDefault(member.getId(), ItemType.BACKGROUND));
                    return inventoryRepository.save(Inventory.createDefault(member.getId(), ItemType.CHARACTER));
                });


        return getToken(member.getProviderId());
    }

    @Transactional
    public PointRes updatePoint(Long memberId, int point) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        member.increasePoint(point);
        return new PointRes(member.getNickname(), member.getPoint());
    }

    @Transactional
    public boolean toggleNotification(long memberId) {
        log.info("[로그] toggleNotification(), memberId : {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        member.toggleNotification();
        return member.getIsNotificationEnabled();
    }
}
