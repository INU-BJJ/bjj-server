package com.appcenter.BJJ.domain.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/policy")
@Tag(name = "Policy", description = "정책 및 약관 API (토큰 X)")
public class PolicyController {
    @Operation(summary = "서비스 이용약관 조회")
    @GetMapping("/terms")
    public String terms() {
        return "banners/terms";
    }

    @Operation(summary = "개인정보 처리방침 조회")
    @GetMapping("/privacy")
    public String privacy() {
        return "banners/privacy";
    }
}
