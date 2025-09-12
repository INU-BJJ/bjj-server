package com.appcenter.BJJ.domain.banner;

import com.appcenter.BJJ.domain.banner.BannerReq.BannerPost;
import com.appcenter.BJJ.domain.banner.BannerReq.BannerPut;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
@Tag(name = "Banner (Admin)", description = "배너 API (관리자만 사용)")
public class BannerManagementController {

    private final BannerManagementService bannerManagementService;

    @Operation(summary = "배너 저장",
            description = """
                    - pageUri에 배너 클릭 시 이동할 뷰 페이지의 URI를 작성
                    - sortOrder에 희망 하는 배너 노출 순서를 작성
                    - 배너 노출 순서는 0부터 차례대로 배정됨
                    - 기존 배너의 sortOrder를 희망할 시 해당 배너에 희망하는 sortOrder를 배정 및 해당 순서 이후의 배너들의 sortOrder를 하나 씩 증가
                    - sortOrder에 -1 입력 시 가장 마지막 순서에 배정
                    - sortOrder를 입력 하지 않을 시(null), 배너를 노출하지 않음
                    """)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Banner> uploadBanner(
            @Valid @RequestPart BannerPost bannerPost,
            @RequestPart MultipartFile imageFile) {

        return ResponseEntity.status(HttpStatus.CREATED).body(bannerManagementService.uploadBanner(bannerPost, imageFile));
    }

    @Operation(summary = "모든 배너 목록 조회",
            description = """       
                    - sortOrder 오름차순으로 정렬되어 조회
                    - sortOrder가 null인 배너는 가장 뒤에 배치
                    """)
    @GetMapping
    public ResponseEntity<List<Banner>> getBanners() {
        return ResponseEntity.ok(bannerManagementService.getBanners());
    }

    @PutMapping("/{bannerId}")
    public ResponseEntity<Banner> updateBanner(
            @PathVariable Long bannerId,
            @RequestBody BannerPut bannerPut) {
        return ResponseEntity.ok(bannerManagementService.updateBanner(bannerId, bannerPut));
    }
}
