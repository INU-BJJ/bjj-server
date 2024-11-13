package com.appcenter.BJJ.domain.review.service;

import com.appcenter.BJJ.domain.image.Image;
import com.appcenter.BJJ.domain.image.ImageRepository;
import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.menu.repository.MenuPairRepository;
import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.MyReviewDetailRes;
import com.appcenter.BJJ.domain.review.dto.MyReviewRes;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;
import com.appcenter.BJJ.domain.review.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.domain.review.dto.ReviewRes;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final MenuPairRepository menuPairRepository;

    @Value("${dir.img.review}")
    private String REVIEW_IMG_DIR;

    @Transactional
    public long create(ReviewPost reviewPost, List<MultipartFile> files, Long memberId) {
        log.info("[로그] create(), REVIEW_IMG_DIR : {}", REVIEW_IMG_DIR);

        if (memberId == null) {
            throw new IllegalArgumentException("해당하는 멤버가 존재하지 않습니다.");
        }

        MenuPair menuPair = menuPairRepository.findById(reviewPost.getMenuPairId())
                .orElseThrow(() -> new IllegalArgumentException("해당하는 메뉴쌍이 존재하지 않습니다."));
        Review review = reviewPost.toEntity(memberId, menuPair);

        // .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.multipart.MaxUploadSizeExceededException: Maximum upload size exceeded]
        // 파일 최대 용량 초과 에러에 대한 예외 처리 필요
        // 이미지 변환
        if (files != null) {
            files.forEach(file -> {
                try {
                    log.info("[로그] 이미지 변환 전, file.getOriginalFilename() : {}, file 크기 : {} KB", file.getOriginalFilename(), String.format("%.2f", file.getSize() / 1024.0));
                    Image image = Image.of(file, review, REVIEW_IMG_DIR);
                    review.getImages().add(image);
                    log.info("[로그] 이미지 변환 후, image.getName() : {}, image.getPath() : {}", image.getName(), image.getPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return reviewRepository.save(review).getId();
    }

    public ReviewRes findByMenuPair(Long memberId, Long menuPairId, int pageNumber, int pageSize, Sort sort, Boolean isWithImages) {
        log.info("[로그] findByMenuPair(), memberId : {}", memberId);

        MenuPair menuPair = menuPairRepository.findById(menuPairId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 메뉴쌍이 존재하지 않습니다."));

        List<ReviewDetailRes> reviewDetailResList = reviewRepository.findReviewsWithImagesAndMemberDetails(memberId, menuPair.getMainMenuId(), menuPair.getSubMenuId(), pageNumber, pageSize, sort, isWithImages);

        List<Long> reviewIdList = reviewDetailResList.stream().map(ReviewDetailRes::getReviewId).toList();

        List<Image> images = imageRepository.findByReviewIdList(reviewIdList);
        Map<Long, List<Image>> imageListMap = images
                .stream().collect(Collectors.groupingBy(image -> image.getReview().getId()));

        reviewDetailResList.forEach(reviewDetailRes -> {
            List<String> imageNameList = imageListMap
                    .getOrDefault(reviewDetailRes.getReviewId(), List.of())
                    .stream().map(Image::getName).toList();
            reviewDetailRes.setImageNames(imageNameList);
        });

        // 마지막 페이지 여부 확인
        Long totalCount = reviewRepository.countReviewsWithImagesAndMemberDetails(memberId, menuPair.getMainMenuId(), menuPair.getSubMenuId(), pageNumber, pageSize, sort, isWithImages);
        boolean isLastPage = (pageNumber + pageSize >= totalCount);

        return ReviewRes.builder()
                .reviewDetailList(reviewDetailResList)
                .isLastPage(isLastPage)
                .build();
    }

    public MyReviewRes findMyReviews(Long memberId) {
        log.info("[로그] findByMenuPair(), memberId : {}", memberId);

        Map<String, List<MyReviewDetailRes>> myReviewDetailList = reviewRepository.findMyReviewsWithImagesAndMemberDetailsAndCafeteria(memberId);

        return MyReviewRes.builder()
                .myReviewDetailList(myReviewDetailList)
                .build();
    }

    /*public ReviewRes findMyReviewsByCafeteria(Long memberId, int pageNumber, int pageSize) {
        log.info("[로그] findByMenuPair(), memberId : {}", memberId);

        // Page<Review> reviewPage = reviewRepository.findByMemberIdOrderByCreatedDateDesc(memberId, PageRequest.of(pageNumber, pageSize));

        Map<String, List<MyReviewDetailRes>> myReviewDetailList = reviewRepository.findMyReviewsWithImagesAndMemberDetailsAndCafeteria(memberId);

        MyReviewRes.builder()
                .myReviewDetailList(myReviewDetailList)
                .build();

        List<Review> reviewList = reviewPage.getContent();

        List<ReviewDetailRes> reviewDetailList = reviewList.stream().map(review -> {
            List<String> imageNameList = review.getImages().stream().map(Image::getName).toList();

            return ReviewDetailRes.builder()
                    .reviewId(review.getId())
                    .comment(review.getComment())
                    .rating(review.getRating())
                    .imageNames(imageNameList)
                    .likeCount(review.getLikeCount())
                    .createdDate(review.getCreatedDate())
                    .memberId(review.getMemberId())
                    .build();
        }).toList();

        return ReviewRes.builder()
                .reviewDetailList(reviewDetailList)
                .isLastPage(reviewPage.isLast())
                .build();
    }*/

    @Transactional
    public Long delete(Long reviewId) {

        Long menuPairId = null;

        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();

            review.getImages().forEach(image -> {
                boolean result = image.removeImageFromPath(REVIEW_IMG_DIR);

                if (!result) {
                    log.info("이미지 {} 삭제에 실패했습니다.", image.getName());
                }
            });

            menuPairId = review.getMenuPair().getId();

            reviewRepository.delete(review);
        }

        return menuPairId;
    }
}
