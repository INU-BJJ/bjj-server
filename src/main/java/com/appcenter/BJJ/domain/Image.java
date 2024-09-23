package com.appcenter.BJJ.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "image_tb")
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String type;

    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Builder(access = AccessLevel.PRIVATE)
    private Image(String name, String type, String path, Review review) {
        this.name = name;
        this.type = type;
        this.path = path;
        this.review = review;
    }

    public static Image of(MultipartFile file, Review review, String folderPath) throws IOException {

        // 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String fileExtension = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf("."));

        // 고유한 파일 이름 생성 (UUID 사용)
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        String filePath = folderPath + uniqueFileName;

        file.transferTo(new File(filePath));

        return Image.builder()
                .name(uniqueFileName)
                .type(file.getContentType())
                .path(filePath)
                .review(review)
                .build();
    }


}
