package com.appcenter.BJJ.domain.member;

import com.appcenter.BJJ.domain.item.domain.Inventory;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.repository.InventoryRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.dto.*;
import com.appcenter.BJJ.domain.member.enums.MemberRole;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import com.appcenter.BJJ.global.jwt.JwtProvider;
import com.appcenter.BJJ.global.oauth.OAuth2Unlink;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final OAuth2Unlink oAuth2Unlink;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public String signUp(SignupReq signupReq) {
        isNicknameAvailable(signupReq.getNickname());

        Member member = memberRepository.findByEmailAndProvider(signupReq.getEmail(), signupReq.getProvider()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        member.updateMemberInfo(signupReq.getNickname(), MemberRole.USER);
        log.info("MemberService.signup() - ROLE_USER로 변경 완료 및 회원가입 성공");

        //기본 아이템 새성
        inventoryRepository.save(Inventory.createDefault(member.getId(), ItemType.CHARACTER));
        inventoryRepository.save(Inventory.createDefault(member.getId(), ItemType.BACKGROUND));

        String accessToken = getToken(member.getProviderId(), JwtProvider.validAccessTime);
        log.info("MemberService.signup() - 토큰 발급 성공");
        return accessToken;
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

    @Transactional
    public void deleteMember(MemberOAuthVO memberOAuthVO) {
        // [notice] 이후 member 관련된 내용도 다같이 지우기 //
        Long memberId = oAuth2Unlink.unlinkHandler(memberOAuthVO);
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        memberRepository.deleteById(memberId);
        log.info("MemberService.deleteMember() - 회원 탈퇴 성공");
    }

    private String getToken(String providerId, Long time) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(providerId, "");
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        log.info("MemberService-getToken: 회원이 존재합니다.");

        String token = jwtProvider.generateToken(authentication, time);
        log.info("MemberService-getToken: 토큰 발급이 됐습니다.");
        return token;
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

        Member member = memberRepository.findByEmailAndProvider(loginReq.getEmail(), "bjj").orElseGet(
                () -> {
                    isNicknameAvailable(loginReq.getNickname());
                    log.info("test: login nickname {}", loginReq.getNickname());
                    return Member.builder()
                            .email(loginReq.getEmail())
                            .nickname(loginReq.getNickname())
                            .provider("bjj")
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


        return getToken(member.getProviderId(), JwtProvider.validAccessTime);
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
