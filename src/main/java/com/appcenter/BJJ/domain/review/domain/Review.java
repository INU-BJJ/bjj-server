package com.appcenter.BJJ.domain.review.domain;

import com.appcenter.BJJ.domain.image.Image;
import com.appcenter.BJJ.domain.menu.domain.MenuPair;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_pair_id")
    private MenuPair menuPair;

    @Builder
    private Review(String comment, Integer rating, Long memberId, MenuPair menuPair) {
        this.comment = comment;
        this.rating = rating;
        this.likeCount = 0L;
        this.createdDate = LocalDate.now();
        this.memberId = memberId;
        this.menuPair = menuPair;
    }
}
