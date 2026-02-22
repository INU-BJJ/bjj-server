package com.appcenter.BJJ.domain.member;

import com.appcenter.BJJ.domain.member.dto.*;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 API")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "소셜로그인",
            description = """
                    - 소셜로그인 인증 처리\s
                    - 가입된 회원의 경우, AccessToken 발급\s
                    - 가입되지 않은 회원의 경우, 404 Not Found 반환\s
                    - request : SocialLoginReq\s""")
    @PostMapping("/socialLogin")
    public ResponseEntity<Map<String, String>> socialLogin(@RequestBody SocialLoginReq socialLoginReq) {
        Map<String, String> socialLoginRes = new HashMap<>();
        socialLoginRes.put("token", memberService.socialLogin(socialLoginReq));
        return ResponseEntity.ok(socialLoginRes);
    }

    @Operation(summary = "회원가입",
            description = "- request : SignupReq")
    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody SignupReq signupReq) {
        Map<String, String> signupRes = new HashMap<>();
        signupRes.put("token", memberService.signUp(signupReq));
        return ResponseEntity.ok(signupRes);
    }

    @Operation(summary = "회원 조회")
    @GetMapping
    public ResponseEntity<MemberRes> getMember(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(memberService.getMember(userDetails.getMember().getId()));
    }

    @Operation(summary = "닉네임 중복 확인")
    @PostMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@Valid @RequestParam String nickname) {
        return ResponseEntity.ok(memberService.isNicknameAvailable(nickname));
    }

    @Operation(summary = "닉네임 수정")
    @PatchMapping("/nickname")
    public ResponseEntity<Map<String, String>> putNickname(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @NotBlank(message = "닉네임은 필수항목입니다.") @RequestParam String nickname) {
        Map<String, String> nicknameRes = new HashMap<>();
        nicknameRes.put("nickname", memberService.changeNickname(userDetails.getNickname(), nickname));
        return ResponseEntity.ok(nicknameRes);
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping
    public ResponseEntity<?> deleteMember(@Valid @AuthenticationPrincipal UserDetailsImpl userDetails) {
        memberService.deleteMember(userDetails.getMember().getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "알림 설정 토글", description = "알림을 켠 경우 true, 알림을 끈 경우 false 반환")
    @PatchMapping("/notification")
    public ResponseEntity<Boolean> toggleNotification(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] PATCH /api/members/notification, memberNickname: {}", userDetails.getNickname());

        boolean isNotificationActive = memberService.toggleNotification(userDetails.getMember().getId());
        return ResponseEntity.ok(isNotificationActive);
    }

    //TODO test용이기에 이후에 지우기
    @Operation(summary = "[test] 회원가입 및 로그인")
    @PostMapping("/test/login")
    public ResponseEntity<String> socialLogin(@Valid @RequestBody LoginReq loginReq) {
        return ResponseEntity.ok(memberService.socialLogin(loginReq));
    }

    @Operation(summary = "[test] 회원 포인트 추가")
    @PatchMapping("/test")
    public ResponseEntity<PointRes> updatePoint(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam int point) {
        return ResponseEntity.ok(memberService.updatePoint(userDetails.getMember().getId(), point));
    }
}
