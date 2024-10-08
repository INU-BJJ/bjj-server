package com.appcenter.BJJ.domain.review.service;

import com.appcenter.BJJ.domain.image.Image;
import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.review.repository.ReviewSpecifications;
import com.appcenter.BJJ.domain.review.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.domain.menu.repository.MenuPairRepository;
import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;
import com.appcenter.BJJ.domain.review.dto.ReviewRes;
import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MenuPairRepository menuPairRepository;

    private final String REVIEW_FOLDER_PATH = "C:\\BJJ\\ReviewImages\\";

    @Transactional
    public long create(ReviewPost reviewPost, List<MultipartFile> files, Long memberId) {

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
                    Image image = Image.of(file, review, REVIEW_FOLDER_PATH);
                    review.getImages().add(image);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return reviewRepository.save(review).getId();
    }

    public ReviewRes findByMenuPair(Long menuPairId, int pageNumber, int pageSize, Sort sort, Boolean isWithImages) {

        MenuPair menuPair = menuPairRepository.findById(menuPairId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 메뉴쌍이 존재하지 않습니다."));

        Specification<Review> spec = Specification.where(ReviewSpecifications.withMainMenuId(menuPair.getMainMenuId()))
                .or(ReviewSpecifications.withSubMenuId(menuPair.getSubMenuId()))
                .and(ReviewSpecifications.withImages(isWithImages))
                .and(ReviewSpecifications.sortedBy(sort, menuPair.getMainMenuId(), menuPair.getSubMenuId()));

        Page<Review> reviewPage = reviewRepository.findAll(spec, PageRequest.of(pageNumber, pageSize));

        List<ReviewDetailRes> reviewDetailList = reviewPage.getContent().stream().map(review -> {
            List<String> imagePathList = review.getImages().stream().map(Image::getPath).toList();

            return ReviewDetailRes.builder()
                    .reviewId(review.getId())
                    .comment(review.getComment())
                    .rating(review.getRating())
                    .imagePaths(imagePathList)
                    .likeCount(review.getLikeCount())
                    .createdDate(review.getCreatedDate())
                    .memberId(review.getMemberId())
                    .menuPairId(review.getMenuPair().getId())
                    .mainMenuId(review.getMenuPair().getMainMenuId())
                    .subMenuId(review.getMenuPair().getSubMenuId())
                    .build();
        }).toList();

        return ReviewRes.builder()
                .reviewDetailList(reviewDetailList)
                .isLastPage(reviewPage.isLast())
                .build();
    }

    public ReviewRes findMyReviews(Long memberId, int pageNumber, int pageSize) {

        Page<Review> reviewPage = reviewRepository.findByMemberIdOrderByCreatedDateDesc(memberId, PageRequest.of(pageNumber, pageSize));

        List<Review> reviewList = reviewPage.getContent();

        List<ReviewDetailRes> reviewDetailList = reviewList.stream().map(review -> {
            List<String> imagePathList = review.getImages().stream().map(Image::getPath).toList();

            return ReviewDetailRes.builder()
                    .reviewId(review.getId())
                    .comment(review.getComment())
                    .rating(review.getRating())
                    .imagePaths(imagePathList)
                    .likeCount(review.getLikeCount())
                    .createdDate(review.getCreatedDate())
                    .memberId(review.getMemberId())
                    .menuPairId(review.getMenuPair().getId())
                    .mainMenuId(review.getMenuPair().getMainMenuId())
                    .subMenuId(review.getMenuPair().getSubMenuId())
                    .build();
        }).toList();

        return ReviewRes.builder()
                .reviewDetailList(reviewDetailList)
                .isLastPage(reviewPage.isLast())
                .build();
    }

    @Transactional
    public Long delete(Long reviewId) {

        Long menuPairId = null;

        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();

            review.getImages().forEach(image -> {
                boolean result = image.removeImageFromPath();

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
