package com.appcenter.BJJ.domain.image;

import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Getter
@Entity
@Table(name = "image_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // 이미지 확장자
    private String type;

    // 이미지 최종 경로 이름 (review, cafeteria..)
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

        // 업로드할 디렉토리가 존재하는지 확인
        File directory = new File(folderPath);
        if (!directory.exists()) {
            log.info("[로그] 업로드할 디렉토리가 존재하지 않음. {} 디렉토리 생성", folderPath);
            // 디렉토리가 존재하지 않을 경우 생성
            boolean created = directory.mkdirs(); // 부모 디렉토리도 포함하여 생성
            if (!created) {
                throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다: " + folderPath);
            }
        }

        // 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_FILE_NAME);
        }
        String fileExtension = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf("."));

        // 고유한 파일 이름 생성 (UUID 사용)
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        // 파일 업로드 로직 (파일 저장 등)
        try {
            File destinationFile = new File(directory, uniqueFileName);
            file.transferTo(destinationFile);
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 중 오류 발생", e);
        }

        String[] parts = folderPath.split("/");

        return Image.builder()
                .name(uniqueFileName)
                .type(file.getContentType())
                .path(parts[parts.length - 1])
                .review(review)
                .build();
    }

    public boolean removeImageFromPath(String folderPath) {
        File file = new File(folderPath, name);
        return file.delete();
    }


}
