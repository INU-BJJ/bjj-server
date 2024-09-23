package com.appcenter.BJJ.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "review_tb")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "text")
    private String comment;

    private Integer rating;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    private Long likeCount;

    private LocalDate createdDate;

    private Long memberId;

    private Long menuPairId;

    @Builder
    private Review(String comment, Integer rating, Long memberId, Long menuPairId) {
        this.comment = comment;
        this.rating = rating;
        this.likeCount = 0L;
        this.createdDate = LocalDate.now();
        this.memberId = memberId;
        this.menuPairId = menuPairId;
    }
}
