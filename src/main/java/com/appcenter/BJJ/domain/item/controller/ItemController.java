package com.appcenter.BJJ.domain.item.controller;

import com.appcenter.BJJ.domain.item.dto.ItemRes;
import com.appcenter.BJJ.domain.item.service.ItemService;
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
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Item", description = "아이템 API")
public class ItemController {
    // 운영자만 사용하는 api (아이템 추가, 수정 등의 유지보수를 위해)

    private final ItemService itemService;

    @Operation(summary = "아이템 이미지 저장",
            description = "character / backgound 로 파일명 설정하고 이용")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ItemRes>> postItem(@RequestPart MultipartFile infoFile, @RequestPart MultipartFile imageFile) throws IOException {
        return new ResponseEntity<>(itemService.putItemFile(infoFile, imageFile), HttpStatus.CREATED);
    }

}
