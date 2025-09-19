package com.appcenter.BJJ.domain.banner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
@Tag(name = "Banner", description = "배너 API")
public class BannerController {

    private final BannerService bannerService;

    @Operation(summary = "배너 목록 조회",
            description = """  
                    - 배너 이미지와 페이지 URI를 반환
                    - imageName: /images/banner/{imageName} 형식의 URI로 배너 이미지 접근 가능
                    - pageUri: 배너 클릭 시 이동할 페이지 URI
                    """)
    @GetMapping
    public ResponseEntity<List<BannerRes>> getVisibleBanners() {
        return ResponseEntity.ok(bannerService.findVisibleBanners());
    }
}
