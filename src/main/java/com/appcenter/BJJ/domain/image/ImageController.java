package com.appcenter.BJJ.domain.image;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@RestController
@RequestMapping("/api/images")
@Tag(name = "Image", description = "사진 API")
public class ImageController {

    @Value("${storage.images.review}")
    private String REVIEW_IMG_DIR;

    @Operation(summary = "이미지 경로 조회", deprecated = true)
    @GetMapping
    public ResponseEntity<byte[]> getImageByPath(String name) throws IOException {
        log.info("[로그] GET /api/images?name={}", name);

        byte[] image = Files.readAllBytes(new File(REVIEW_IMG_DIR + name).toPath());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }
}
