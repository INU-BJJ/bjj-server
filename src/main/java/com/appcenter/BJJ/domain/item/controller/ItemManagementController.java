package com.appcenter.BJJ.domain.item.controller;

import com.appcenter.BJJ.domain.item.dto.ItemRes;
import com.appcenter.BJJ.domain.item.service.ItemManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
@Tag(name = "Item (Admin)", description = "아이템 API (관리자만 사용)")
public class ItemManagementController {

    private final ItemManagementService itemManagementService;

    @Operation(summary = "아이템 이미지 저장",
            description = "character / backgound 로 파일명 설정하고 이용")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ItemRes>> uploadItems(@RequestPart MultipartFile infoFile, @RequestPart MultipartFile zipImageFile) throws IOException {
        return new ResponseEntity<>(itemManagementService.uploadItems(infoFile, zipImageFile), HttpStatus.CREATED);
    }
}
