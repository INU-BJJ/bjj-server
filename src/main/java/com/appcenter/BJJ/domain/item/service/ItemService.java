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
import java.util.ArrayList;
import java.util.Comparator;
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

        List<String> fileNameList = unZip(imageFile);
        ItemType itemType = getItemType(imageFile);

        List<Item> itemList = jsonToList(infoFile).stream()
                .map(itemVO -> Item.create(itemVO, fileNameList.get(itemVO.getId() - 1), itemType))
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

    private List<String> unZip(MultipartFile zipImageFile) throws IOException {
        InputStream inputStream = zipImageFile.getInputStream();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        ZipEntry entry;
        List<String> fileNameList = new ArrayList<>();
        ItemType itemType = getItemType(zipImageFile);

        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.isDirectory()) continue;

            String imageFileName = entry.getName().split("/")[1];
            fileNameList.add(imageFileName);

            File imageFile = new File(ITEM_IMG_DIR + itemType + "/" + imageFileName);

            //이미지 파일 write
            try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
                byte[] bytes = new byte[1024];
                int length;
                while ((length = zipInputStream.read(bytes)) >= 0) {
                    outputStream.write(bytes, 0, length);
                }
            }
        }
        zipInputStream.closeEntry();

        return fileNameList.stream()
                // 파일의 이름을 기준으로 정렬
                .sorted(Comparator.comparingInt(fileName -> Integer.parseInt(fileName.split("\\.")[0])))
                .toList();
    }

    private List<ItemVO> jsonToList(MultipartFile file) throws IOException {
        String jsonFile = new String(file.getBytes(), StandardCharsets.UTF_8);
        return objectMapper.readValue(jsonFile, new TypeReference<>() {
        });
    }

    private ItemType getItemType(MultipartFile file) {
        return ItemType.valueOf(Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[0].toUpperCase());
    }
}
