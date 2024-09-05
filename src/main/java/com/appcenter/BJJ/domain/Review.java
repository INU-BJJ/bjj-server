package com.appcenter.BJJ.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Entity
@Table(name = "review_tb")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue
    private Long id;

    private String comment;

    private Integer rating;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "review_id")
    private List<Image> images;

    private Long likeCount;

    private LocalDate createdDate;

    private Long memberId;

    private Long menuPairId;

    @Builder
    private Review(String comment, Integer rating, List<Image> images, Long memberId, Long menuPairId) {
        this.comment = comment;
        this.rating = rating;
        this.images = images;
        this.likeCount = 0L;
        this.createdDate = LocalDate.now();
        this.memberId = memberId;
        this.menuPairId = menuPairId;
    }
}
