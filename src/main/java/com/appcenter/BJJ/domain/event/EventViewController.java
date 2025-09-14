package com.appcenter.BJJ.domain.event;

import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/view/events")
@RequiredArgsConstructor
@Tag(name = "Event View", description = "이벤트 뷰 API")
public class EventViewController {
    private final EventService eventService;

    @Operation(summary = "웰컴포인트 이벤트 참여 여부 체크",
            description = "- 웰컴포인트 이벤트 뷰에서 사용하는 API\n- true : 이벤트 참여 완료, false : 이벤트 참여 불가 (이미 참여한 상태)")
    @ResponseBody
    @GetMapping("/welcome-point/check")
    public Map<String, Boolean> welcomePoint(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Map.of("isParticipated", eventService.welcomePoint(userDetails.getMember().getId()));
    }

    @Operation(summary = "웰컴포인트 이벤트 뷰 조회")
    @GetMapping("/welcome-point")
    public String welcomePointView() {
        return "banners/welcome-point";
    }
}
