package com.appcenter.BJJ.domain.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long>, ImageRepositoryCustom{

    @Query("SELECT i FROM Image i JOIN FETCH i.review r WHERE i.review.id IN :reviewIdList")
    List<Image> findByReviewIdList(List<Long> reviewIdList);
}
