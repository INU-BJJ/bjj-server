package com.appcenter.BJJ.service;

import com.appcenter.BJJ.domain.Image;
import com.appcenter.BJJ.domain.Review;
import com.appcenter.BJJ.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.dto.ReviewRes;
import com.appcenter.BJJ.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public long create(ReviewPost reviewPost, List<Image> images, Long memberId) {

        if (memberId == null) {
            throw new IllegalArgumentException("해당하는 멤버가 존재하지 않습니다.");
        }

        Review review = reviewPost.toEntity(memberId, images);

        return reviewRepository.save(review).getId();
    }

    public List<ReviewRes> findByMenuPair(Long menuPairId) {

        List<Review> reviewList = reviewRepository.findByMenuPairId(menuPairId);

        return reviewList.stream().map(review -> {
            List<String> imagePathList = review.getImages().stream().map(Image::getPath).toList();

            return ReviewRes.builder()
                    .id(review.getId())
                    .comment(review.getComment())
                    .rating(review.getRating())
                    .imagePaths(imagePathList)
                    .likeCount(review.getLikeCount())
                    .createdDate(review.getCreatedDate())
                    .memberId(review.getMemberId())
                    .menuPairId(review.getMenuPairId())
                    .build();
        }).toList();
    }
}
