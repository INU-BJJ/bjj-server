package com.appcenter.BJJ.controller;

import com.appcenter.BJJ.dto.MemberResponseDTO;
import com.appcenter.BJJ.dto.SignupDTO;
import com.appcenter.BJJ.jwt.UserDetailsImpl;
import com.appcenter.BJJ.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> signUp(@RequestBody SignupDTO signupDTO) {
        log.info("MemberController-signUp: 진입");
        Map<String, String> response = new HashMap<>();
        response.put("token", memberService.signUp(signupDTO));
        log.info("MemberController-signUp: 회원가입 성공");
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<MemberResponseDTO> getMember(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("MemberController-getMember: 진입");
        return ResponseEntity.ok(memberService.getMember(userDetails.getMember().getId()));
    }
}
