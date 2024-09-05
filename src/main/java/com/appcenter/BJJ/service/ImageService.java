package com.appcenter.BJJ.service;

import com.appcenter.BJJ.domain.Image;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ImageService {

    private final String REVIEW_FOLDER_PATH = "C:\\BJJ\\ReviewImages\\";

    public List<Image> transformToReviewImage(List<MultipartFile> files) {
        return files.stream().map(file -> {
            try {
                return Image.of(file, REVIEW_FOLDER_PATH);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }
}
