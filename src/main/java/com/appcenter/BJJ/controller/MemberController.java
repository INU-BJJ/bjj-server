package com.appcenter.BJJ.controller;

import com.appcenter.BJJ.dto.MemberResponseDTO;
import com.appcenter.BJJ.dto.SignupDTO;
import com.appcenter.BJJ.jwt.UserDetailsImpl;
import com.appcenter.BJJ.service.MemberService;
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
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 API")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "소셜로그인 처음 이용시 연결")
    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> signUp(@RequestBody SignupDTO signupDTO) {
        log.info("MemberController-signUp: 진입");
        Map<String, String> response = new HashMap<>();
        response.put("token", memberService.signUp(signupDTO));
        log.info("MemberController-signUp: 회원가입 성공");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "모든 회원 조회")
    @GetMapping("")
    public ResponseEntity<MemberResponseDTO> getMember(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("MemberController-getMember: 진입");
        return ResponseEntity.ok(memberService.getMember(userDetails.getMember().getId()));
    }
}
