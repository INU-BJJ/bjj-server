package com.appcenter.BJJ.domain.review.service;

import com.appcenter.BJJ.domain.image.Image;
import com.appcenter.BJJ.domain.image.ImageRepository;
import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.enums.MemberStatus;
import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.menu.repository.MenuPairRepository;
import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.*;
import com.appcenter.BJJ.domain.review.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
import com.appcenter.BJJ.domain.review.utils.ReviewPolicy;
import com.appcenter.BJJ.domain.todaydiet.repository.CafeteriaRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import com.appcenter.BJJ.global.exception.ReviewSuspensionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
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
    private final MemberRepository memberRepository;
    private final CafeteriaRepository cafeteriaRepository;
    private final ReviewPolicy reviewPolicy;

    @Value("${storage.images.review}")
    private String REVIEW_IMG_DIR;

    @Transactional
    public long create(ReviewPost reviewPost, List<MultipartFile> files, Long memberId) {
        log.info("[로그] create(), REVIEW_IMG_DIR : {}", REVIEW_IMG_DIR);

        //정지 당한 회원의 리뷰 작성 제재
        if (memberRepository.existsByIdAndMemberStatus(memberId, MemberStatus.SUSPENDED)) {
            Member member = memberRepository.findById(memberId).get();
            throw new ReviewSuspensionException(member.getSuspensionPeriod().getStartAt(), member.getSuspensionPeriod().getEndAt());
        }

        MenuPair menuPair = menuPairRepository.findById(reviewPost.getMenuPairId())
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_PAIR_NOT_FOUND));

        String cafeteriaCorner = cafeteriaRepository.findCafeteriaCornerByMenuPairId(reviewPost.getMenuPairId())
                .orElseThrow(() -> new CustomException(ErrorCode.CAFETERIA_NOT_FOUND));

        // 리뷰 작성 가능한 시간인지 확인
        if (!reviewPolicy.isReviewableTime(cafeteriaCorner, LocalTime.now())) {
            throw new CustomException(ErrorCode.INVALID_REVIEW_TIME);
        }

        // 이미 동일 식사 시간 대 삭제되지 않은 작성된 리뷰가 있는지 여부 확인
        boolean isAlreadyWritten;
        if ("조식".equals(cafeteriaCorner) || "석식".equals(cafeteriaCorner)) {
            log.debug("[로그] 삭제되지 않은 조식/석식 리뷰 존재 여부 확인");
            isAlreadyWritten = reviewRepository.existsTodayUndeletedMyReviewByCafeteriaCorner(memberId, cafeteriaCorner);
        } else {
            log.debug("[로그] 삭제되지 않은 중식 리뷰 존재 여부 확인");
            isAlreadyWritten = reviewRepository.existsTodayUndeletedMyReviewExcludingBreakfastAndDinner(memberId);
        }
        if (isAlreadyWritten) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 이미 동일 식사 시간 대 리뷰를 작성한 적 있는지 여부 확인
        boolean isAlreadyGiven;
        if ("조식".equals(cafeteriaCorner) || "석식".equals(cafeteriaCorner)) {
            log.debug("[로그] 조식/석식 리뷰 존재 여부 확인 (포인트 지급 여부)");
            isAlreadyGiven = reviewRepository.existsTodayMyReviewByCafeteriaCorner(memberId, cafeteriaCorner);
        } else {
            log.debug("[로그] 중식 리뷰 존재 여부 확인 (포인트 지급 여부)");
            isAlreadyGiven = reviewRepository.existsTodayReviewExcludingBreakfastAndDinner(memberId);
        }

        // 리뷰를 작성한 적 없으면 포인트 지급
        if (!isAlreadyGiven) {
            boolean isPhotoReview = (files != null) && (!files.isEmpty());
            int point = isPhotoReview ? 100 : 50;

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            member.increasePoint(point);
        }

        Review review = reviewPost.toEntity(memberId, menuPair);

        // .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.multipart.MaxUploadSizeExceededException: Maximum upload size exceeded]
        // 파일 최대 용량 초과 에러에 대한 예외 처리 필요 (CustomExceptionHandler에서 처리)
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

    public ReviewsPagedRes findByMenuPair(Long memberId, Long menuPairId, int pageNumber, int pageSize, Sort sort, Boolean isWithImages) {
        log.info("[로그] findByMenuPair(), memberId : {}", memberId);

        MenuPair menuPair = menuPairRepository.findById(menuPairId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_PAIR_NOT_FOUND));

        log.debug("[로그] menuPair.getMainMenuId() = {}, menuPair.getSubMenuId() = {}  ", menuPair.getMainMenuId(), menuPair.getSubMenuId());

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

        return ReviewsPagedRes.builder()
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

            review.deleteReview();

            menuPairId = review.getMenuPair().getId();

            // 이미지 및 리뷰 Hard Delete
            /*review.getImages().forEach(image -> {
                boolean result = image.removeImageFromPath(REVIEW_IMG_DIR);

                if (!result) {
                    log.info("이미지 {} 삭제에 실패했습니다.", image.getName());
                }
            });

            reviewRepository.delete(review);*/
        }

        return menuPairId;
    }

    public ReviewImagesPagedRes findReviewImagesByMenuPairId(Long menuPairId, int pageNumber, int pageSize) {
        log.info("[로그] findReviewImagesByMenuPairId(), menuPairId : {}, pageNumber : {}, pageSize: {}", menuPairId, pageNumber, pageSize);

        MenuPair menuPair = menuPairRepository.findById(menuPairId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_PAIR_NOT_FOUND));

        Slice<ReviewImageDetailRes> reviewImageDetailResSlice = reviewRepository.findReviewImagesByMenuPairId(menuPair.getMainMenuId(), menuPair.getSubMenuId(), PageRequest.of(pageNumber, pageSize));

        return ReviewImagesPagedRes.builder()
                .reviewImageDetailList(reviewImageDetailResSlice.getContent())
                .isLastPage(reviewImageDetailResSlice.isLast())
                .build();
    }

    public ReviewDetailRes findReviewWithDetail(long reviewId, long memberId) {
        log.info("[로그] findReviewWithDetail(), reviewId : {}, memberId: {}", reviewId, memberId);

        ReviewDetailRes reviewDetailRes = reviewRepository.findReviewWithMenuAndMemberDetails(reviewId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_DETAIL_NOT_FOUND));

        List<String> imageNames = imageRepository.findByReviewId(reviewId).stream().map(Image::getName).toList();
        reviewDetailRes.setImageNames(imageNames);

        return reviewDetailRes;
    }
}
