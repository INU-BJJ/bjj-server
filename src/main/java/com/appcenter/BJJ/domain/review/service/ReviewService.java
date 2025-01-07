package com.appcenter.BJJ.domain.review.service;

import com.appcenter.BJJ.domain.image.Image;
import com.appcenter.BJJ.domain.image.ImageRepository;
import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.menu.repository.MenuPairRepository;
import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.*;
import com.appcenter.BJJ.domain.review.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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

    @Value("${storage.images.review}")
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

        Slice<ReviewDetailRes> reviewDetailResSlice = reviewRepository
                .findReviewsWithImagesAndMemberDetails(memberId, menuPair.getMainMenuId(), menuPair.getSubMenuId(), sort, isWithImages, PageRequest.of(pageNumber, pageSize));
        List<ReviewDetailRes> reviewDetailResList = reviewDetailResSlice.getContent();
        boolean isLast = reviewDetailResSlice.isLast();

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

        return ReviewRes.builder()
                .reviewDetailList(reviewDetailResList)
                .isLastPage(isLast)
                .build();
    }

    public MyReviewsGroupedRes findMyReviews(Long memberId) {
        log.info("[로그] findMyReviews(), memberId : {}", memberId);

        Map<String, List<MyReviewDetailRes>> myReviewDetailResListMap = reviewRepository.findMyReviewsWithImagesAndMemberDetailsAndCafeteria(memberId);

        myReviewDetailResListMap.values().forEach(myReviewDetailResList -> {
            List<Long> reviewIdList = myReviewDetailResList.stream().map(MyReviewDetailRes::getReviewId).toList();

            List<Image> images = imageRepository.findByReviewIdList(reviewIdList);
            Map<Long, List<Image>> imageListMap = images
                    .stream().collect(Collectors.groupingBy(image -> image.getReview().getId()));

            myReviewDetailResList.forEach(myReviewDetailRes -> {
                List<String> imageNameList = imageListMap
                        .getOrDefault(myReviewDetailRes.getReviewId(), List.of())
                        .stream().map(Image::getName).toList();
                myReviewDetailRes.setImageNames(imageNameList);
            });
        });

        return MyReviewsGroupedRes.builder()
                .myReviewDetailList(myReviewDetailResListMap)
                .build();
    }

    public MyReviewsPagedRes findMyReviewsByCafeteria(Long memberId, String cafeteriaName, int pageNumber, int pageSize) {
        log.info("[로그] findMyReviewsByCafeteria(), memberId : {}", memberId);

        Slice<MyReviewDetailRes> myReviewDetailResSlice = reviewRepository
                .findMyReviewsWithImagesAndMemberDetailsByCafeteria(memberId, cafeteriaName, PageRequest.of(pageNumber, pageSize));
        List<MyReviewDetailRes> myReviewDetailResList = myReviewDetailResSlice.getContent();
        boolean isLast = myReviewDetailResSlice.isLast();

        List<Long> reviewIdList = myReviewDetailResList.stream().map(MyReviewDetailRes::getReviewId).toList();

        List<Image> images = imageRepository.findByReviewIdList(reviewIdList);
        Map<Long, List<Image>> imageListMap = images
                .stream().collect(Collectors.groupingBy(image -> image.getReview().getId()));

        myReviewDetailResList.forEach(reviewDetailRes -> {
            List<String> imageNameList = imageListMap
                    .getOrDefault(reviewDetailRes.getReviewId(), List.of())
                    .stream().map(Image::getName).toList();
            reviewDetailRes.setImageNames(imageNameList);
        });

        return MyReviewsPagedRes.builder()
                .myReviewDetailList(myReviewDetailResList)
                .isLastPage(isLast)
                .build();
    }

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
