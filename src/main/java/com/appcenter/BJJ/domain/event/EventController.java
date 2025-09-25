package com.appcenter.BJJ.domain.event;

import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
                    - 아직 이벤트를 참여하지 않는 경우 : 포인트 지급 후 true 반환
                    - 이미 이벤트에 1회 참여한 경우 : 포인트 지급 없이 false 반환
                    - 이벤트 참여 기록이 0과 1 이외의 횟수일 경우 : 서버 내부 오류로 간주해 예외 발생 (500 Internal Server Error)
                    """
    )
    @ResponseBody
    @PostMapping("/welcome-point")
    public Map<String, Boolean> welcomePoint(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Map.of("isParticipated", eventService.welcomePoint(userDetails.getMember().getId()));
    }

    @Operation(summary = "웰컴포인트 이벤트 뷰 조회")
    @GetMapping("/welcome-point/view")
    public String welcomePointView(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            Model model) {

        model.addAttribute("accessToken", authorization);
        return "banners/welcome-point";
    }
}
