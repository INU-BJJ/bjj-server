package com.appcenter.BJJ.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "file_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String type;

    private String path;

    @Builder(access = AccessLevel.PRIVATE)
    private Image(String name, String type, String path) {
        this.name = name;
        this.type = type;
        this.path = path;
    }

    public static Image of(MultipartFile file, String folderPath) throws IOException {

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
                .build();
    }


}
