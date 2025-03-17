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

    @Operation(summary = "회원가입",
            description = """
                    - 소셜로그인 처음 이용시 연결\s
                    - request : SignupReq\s""")
    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody SignupReq signupReq) {
        log.info("MemberController.signUp() - 진입");
        Map<String, String> signupRes = new HashMap<>();
        signupRes.put("token", memberService.signUp(signupReq));
        log.info("MemberController.signUp() - 회원가입 성공");
        return ResponseEntity.ok(signupRes);
    }

    @Operation(summary = "회원 조회")
    @GetMapping
    public ResponseEntity<MemberRes> getMember(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("MemberController.getMember() - 진입");
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
        memberService.deleteMember(MemberOAuthVO.from(userDetails.getMember()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "redirect용 (사용X)")
    @GetMapping("/sign-up")
    public ResponseEntity<?> resolveRedirectSign(@RequestParam String email, @RequestParam String token) {
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "redirect용 (사용X)")
    @GetMapping("/success")
    public ResponseEntity<?> resolveRedirectSuccess(@RequestParam String token) {
        return ResponseEntity.noContent().build();
    }

    //TODO test용이기에 이후에 지우기
    @Operation(summary = "[test] 소셜로그인")
    @PostMapping("/test/social-login")
    public ResponseEntity<MemberRes> socialLogin(@Valid @RequestBody LoginReq loginReq) {
        return ResponseEntity.ok(memberService.socialLogin(loginReq));
    }

    @Operation(summary = "[test] 로그인")
    @PostMapping("/test/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginReq loginReq) {
        Map<String, String> signupRes = new HashMap<>();
        signupRes.put("token", memberService.login(loginReq));
        return ResponseEntity.ok(signupRes);
    }

    @Operation(summary = "[test] 회원 포인트 추가")
    @PatchMapping("/test")
    public ResponseEntity<PointRes> updatePoint(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam int point) {
        return ResponseEntity.ok(memberService.updatePoint(userDetails.getMember().getId(), point));
    }
}
