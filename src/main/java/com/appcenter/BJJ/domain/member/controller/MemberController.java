package com.appcenter.BJJ.domain.member.controller;

import com.appcenter.BJJ.domain.member.dto.LoginReq;
import com.appcenter.BJJ.domain.member.dto.MemberRes;
import com.appcenter.BJJ.domain.member.dto.SignupReq;
import com.appcenter.BJJ.domain.member.service.MemberService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
                    - requestDTO : SignupDTO\s""")
    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> signUp(@RequestBody SignupReq signupReq) {
        log.info("MemberController.signUp() - 진입");
        Map<String, String> signupRes = new HashMap<>();
        signupRes.put("token", memberService.signUp(signupReq));
        log.info("MemberController.signUp() - 회원가입 성공");
        return ResponseEntity.ok(signupRes);
    }

    @Operation(summary = "회원 조회")
    @GetMapping("")
    public ResponseEntity<MemberRes> getMember(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("MemberController.getMember() - 진입");
        return ResponseEntity.ok(memberService.getMember(userDetails.getMember().getId()));
    }

    @Operation(summary = "닉네임 중복 확인")
    @PostMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(memberService.isNicknameAvailable(nickname));
    }

    @Operation(summary = "닉네임 수정")
    @PatchMapping("/nickname")
    public ResponseEntity<Map<String, String>> putNickname(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam String nickname) {
        Map<String, String> nicknameRes = new HashMap<>();
        nicknameRes.put("nickname", memberService.changeNickname(userDetails.getNickname(), nickname));
        return ResponseEntity.ok(nicknameRes);
    }

    // test 회원가입 및 로그인
    @Operation(summary = "[test] 회원가입")
    @PostMapping("/test/social-login")
    public ResponseEntity<MemberRes> socialLogin(@RequestBody LoginReq loginReq) {
        return ResponseEntity.ok(memberService.socialLogin(loginReq));
    }

    @Operation(summary = "[test] 로그인")
    @PostMapping("/test/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginReq loginReq) {
        Map<String, String> signupRes = new HashMap<>();
        signupRes.put("token", memberService.login(loginReq));
        return ResponseEntity.ok(signupRes);
    }
}
