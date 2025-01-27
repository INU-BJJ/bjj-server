package com.appcenter.BJJ.domain.item.service;

import com.appcenter.BJJ.domain.item.domain.Item;
import com.appcenter.BJJ.domain.item.dto.ItemRes;
import com.appcenter.BJJ.domain.item.dto.ItemVO;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.repository.ItemRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ObjectMapper objectMapper;
    private final ItemRepository itemRepository;

    @Value("${storage.images.item}")
    private String ITEM_IMG_DIR;

    public List<ItemRes> putItemFile(MultipartFile infoFile, MultipartFile imageFile) throws IOException {
        String fileName = infoFile.getOriginalFilename();
        log.info("putItemFile(): fileName: {}, imageFile: {}", fileName, imageFile.getOriginalFilename());

        InputStream imageInput = imageFile.getInputStream();
        log.info("putItemFile(): " + imageInput.read());

        String itemType = unZip(imageFile);

        List<Item> itemList = jsonToList(infoFile).stream()
                .map(itemVO -> Item.create(itemVO, itemVO.getId() + ".png", ItemType.valueOf(itemType.toUpperCase())))
                .toList();

        itemRepository.saveAll(itemList);

        return itemList.stream()
                .map(item -> ItemRes.builder()
                        .itemId(item.getItemId())
                        .price(item.getPrice())
                        .itemName(item.getImageName())
                        .itemType(item.getItemType())
                        .itemLevel(item.getItemLevel())
                        .build())
                .toList();
    }

    private String unZip(MultipartFile zipImageFile) throws IOException {

        InputStream inputStream = zipImageFile.getInputStream();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry;

        String itemType = Objects.requireNonNull(zipImageFile.getOriginalFilename()).split("\\.")[0];
        log.info("unZip(): " + itemType);

        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.isDirectory()) continue;

            String imageFileName = entry.getName().split("/")[1];
            File imageFile = new File(ITEM_IMG_DIR + itemType + "/" + imageFileName);

            try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
                byte[] bytes = new byte[1024];
                int length;
                while ((length = zipInputStream.read(bytes)) >= 0) {
                    outputStream.write(bytes, 0, length);
                }
            }
        }
        zipInputStream.closeEntry();

        return itemType;
    }

    private List<ItemVO> jsonToList(MultipartFile file) throws IOException {
        String jsonFile = new String(file.getBytes(), StandardCharsets.UTF_8);
        return objectMapper.readValue(jsonFile, new TypeReference<>() {
        });
    }
}
