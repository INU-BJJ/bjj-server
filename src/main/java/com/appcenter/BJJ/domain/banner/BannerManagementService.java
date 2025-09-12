package com.appcenter.BJJ.domain.banner;

import com.appcenter.BJJ.domain.banner.BannerReq.BannerPost;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BannerManagementService {

    private final BannerRepository bannerRepository;

    @Value("${storage.images.banner}")
    private String BANNER_IMG_DIR;

    @Transactional
    public Banner uploadBanner(BannerPost bannerPost, MultipartFile imageFile) {
        Integer sortOrder = calculateSortOrder(bannerPost.getSortOrder(), null);

        Banner build = Banner.builder()
                .imageName(uploadImage(imageFile))
                .pageUri(bannerPost.getPageUri())
                .sortOrder(sortOrder)
                .build();

        return bannerRepository.save(build);
    }

    public List<Banner> getBanners() {
        return bannerRepository.findAllOrderBySortOrderAscNullsLast();
    }

    @Transactional
    public Banner updateBanner(Long bannerId, BannerReq.BannerPut bannerPut) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new CustomException(ErrorCode.BANNER_NOT_FOUND));

        if (bannerPut.getPageUri() != null) {
            banner.updatePageUri(bannerPut.getPageUri());
        }

        Integer oldOrder = banner.getSortOrder();
        Integer newOrder = calculateSortOrder(bannerPut.getSortOrder(), oldOrder);

        if (!Objects.equals(newOrder, oldOrder)) {
            banner.updateSortOrder(newOrder);
        }

        return banner;
    }

    /*
    * 요청한 sortOrder를 계산하고, 기존 배너들의 순서를 조정
    * @param requestedOrder - 사용자가 요청한 sortOrder (0부터 시작, -1=마지막, null=비노출)
    * @param oldOrder      - 기존 배너의 sortOrder (신규 배너는 null)
    * */
    private Integer calculateSortOrder(Integer requestedOrder, Integer oldOrder) {

        // 1. 비노출 배너
        if (requestedOrder == null) {
            if (oldOrder != null) {
                bannerRepository.findBySortOrderGreaterThan(oldOrder)
                        .forEach(b -> b.updateSortOrder(b.getSortOrder() - 1));
            }
            return null;
        }

        int maxOrder = Objects.requireNonNullElse(bannerRepository.findMaxSortOrder(), -1);

        // 2. 신규 배너 삽입
        if (oldOrder == null) {
            // requestedOrder가 -1 또는 최대값 초과인 경우 마지막 위치로
            if (requestedOrder == -1 || requestedOrder > maxOrder) {
                return maxOrder + 1;
            }

            // gap 없애며 삽입
            bannerRepository.findBySortOrderGreaterThanEqual(requestedOrder)
                    .forEach(b -> b.updateSortOrder(b.getSortOrder() + 1));

            return requestedOrder;
        }

        // 3. 기존 노출 배너 이동
        // requestedOrder 조정 (-1 또는 최대값 초과인 경우 마지막 위치로)
        if (requestedOrder == -1 || requestedOrder > maxOrder) {
            requestedOrder = maxOrder;
        }

        if (oldOrder < requestedOrder) {
            bannerRepository.findBySortOrderBetween(oldOrder + 1, requestedOrder)
                    .forEach(b -> b.updateSortOrder(b.getSortOrder() - 1));
        } else if (oldOrder > requestedOrder) {
            bannerRepository.findBySortOrderBetween(requestedOrder, oldOrder - 1)
                    .forEach(b -> b.updateSortOrder(b.getSortOrder() + 1));
        }

        return requestedOrder;
    }

    /*
    * 배너 이미지 저장
    * */
    private String uploadImage(MultipartFile imageFile) {
        // 업로드할 디렉토리가 존재하는지 확인
        File directory = new File(BANNER_IMG_DIR);
        if (!directory.exists()) {
            log.info("[로그] 업로드할 디렉토리가 존재하지 않음. {} 디렉토리 생성", BANNER_IMG_DIR);
            // 디렉토리가 존재하지 않을 경우 생성
            boolean created = directory.mkdirs(); // 부모 디렉토리도 포함하여 생성
            if (!created) {
                throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다: " + BANNER_IMG_DIR);
            }
        }

        // 파일 확장자 추출
        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_FILE_NAME);
        }
        String fileExtension = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf("."));

        // 고유한 파일 이름 생성 (UUID 사용)
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        // 파일 업로드 로직 (파일 저장 등)
        try {
            File destinationFile = new File(directory, uniqueFileName);
            imageFile.transferTo(destinationFile);
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 중 오류 발생", e);
        }

        return uniqueFileName;
    }
}
