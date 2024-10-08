package com.appcenter.BJJ.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Image", description = "사진 API")
public class ImageController {

    @Operation(summary = "이미지 경로 조회")
    @GetMapping
    public ResponseEntity<byte[]> getImageByPath(String path) throws IOException {

        byte[] image = Files.readAllBytes(new File(path).toPath());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }
}
