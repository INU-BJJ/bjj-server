package com.appcenter.BJJ.controller;

import com.appcenter.BJJ.dto.MemberRes;
import com.appcenter.BJJ.dto.SignupReq;
import com.appcenter.BJJ.dto.SignupRes;
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
    public ResponseEntity<SignupRes> signUp(@RequestBody SignupReq signupReq) {
        log.info("MemberController-signUp: 진입");
        SignupRes signupRes = new SignupRes(memberService.signUp(signupReq));
        log.info("MemberController-signUp: 회원가입 성공");
        return ResponseEntity.ok(signupRes);
    }

    @GetMapping("")
    public ResponseEntity<MemberRes> getMember(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("MemberController-getMember: 진입");
        return ResponseEntity.ok(memberService.getMember(userDetails.getMember().getId()));
    }
}
