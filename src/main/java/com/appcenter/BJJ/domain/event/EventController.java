package com.appcenter.BJJ.domain.event;

import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "이벤트 API")
public class EventController {
    private final EventService eventService;

    @Operation(summary = "웰컴포인트 이벤트 참여",
            description = """
                    - 웰컴포인트 이벤트 뷰에서 사용하는 API
                    - 성공 시: 이벤트 참여 완료 여부(true) 반환
                    - 실패 시: 이미 참여한 경우 409 CONFLICT 예외 발생
                    """
    )
    @ResponseBody
    @PostMapping("/welcome-point")
    public Map<String, Boolean> welcomePoint(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Map.of("isParticipated", eventService.welcomePoint(userDetails.getMember().getId()));
    }

    @Operation(summary = "웰컴포인트 이벤트 뷰 조회")
    @GetMapping("/welcome-point/view")
    public String welcomePointView() {
        return "banners/welcome-point";
    }
}
