package com.appcenter.BJJ.domain.banner;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "banner_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 배너 이미지 이름
    private String imageName;

    // 배너 페이지 URI
    private String pageUri;

    // 배너 노출 순서 (null일 경우 노출 x)
    private Integer sortOrder;

    @Builder
    private Banner(String imageName, String pageUri, Integer sortOrder) {
        this.imageName = imageName;
        this.pageUri = pageUri;
        this.sortOrder = sortOrder;
    }

    public void updatePageUri(String pageUri) {
        this.pageUri = pageUri;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
