package com.appcenter.BJJ.service;

import com.appcenter.BJJ.domain.Image;
import com.appcenter.BJJ.domain.Review;
import com.appcenter.BJJ.dto.ReviewReq.ReviewPost;
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
    private final ImageService imageService;

    @Transactional
    public long create(ReviewPost reviewPost, List<Image> images, Long memberId) {

        if (memberId == null) {
            throw new IllegalArgumentException("해당하는 멤버가 존재하지 않습니다.");
        }

        Review review = reviewPost.toEntity(memberId, images);

        return reviewRepository.save(review).getId();
    }
}
