package com.appcenter.BJJ.domain.item.service;

import com.appcenter.BJJ.domain.item.domain.Item;
import com.appcenter.BJJ.domain.item.dto.ItemRes;
import com.appcenter.BJJ.domain.item.dto.ItemVO;
import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemManagementService {
    private final ItemRepository itemRepository;

    @Value("${storage.images.item}")
    private String ITEM_IMG_DIR;

    @Transactional
    public List<ItemRes> uploadItems(MultipartFile infoFile, MultipartFile zipImageFile) throws IOException {

        String itemType = Objects.requireNonNull(infoFile.getOriginalFilename()).split("\\.")[0];

        ItemType type = ItemType.valueOf(itemType.toUpperCase());
        if (itemRepository.existsByItemType(type)) { //아이템 DB 업데이트
            itemRepository.deleteByItemType(type);
        }

        unZip(zipImageFile, itemType);

        List<ItemVO> infoList = convertExcel(infoFile);
        List<Item> itemList = infoList.stream().map(itemVO -> Item.create(itemVO, ItemType.valueOf(itemType.toUpperCase())))
                .toList();
        itemRepository.saveAll(itemList);

        return itemList.stream().map(item -> ItemRes.builder()
                .itemId(item.getId())
                .itemName(item.getItemName())
                .itemType(item.getItemType())
                .itemLevel(item.getItemLevel())
                .build()).collect(Collectors.toList());
    }

    // 압축 해제 및 이미지 저장
    private void unZip(MultipartFile zipImageFile, String itemType) throws IOException {

        InputStream inputStream = zipImageFile.getInputStream();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry;
        String dirName = ITEM_IMG_DIR + itemType;

        try { //디렉토리 내부 파일 삭제 + 디렉토리 삭제
            File dir = new File(dirName);

            while (dir.exists()) {
                File[] files = dir.listFiles();

                for (File file : files) {
                    file.delete();
                }
                if (files.length == 0 && dir.isDirectory()) {
                    dir.delete();
                }
            }
        } catch (Exception e) {
            log.error("[로그] 디렉토리 삭제 실패");
        }


        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.isDirectory()) continue;

            File dir = new File(dirName);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("디렉토리 생성 실패: " + dir.getAbsolutePath());
            }

            String imageFileName = Paths.get(entry.getName()).getFileName().toString();
            File imageFile = new File(dirName, imageFileName);

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
    }

    private List<ItemVO> convertExcel(MultipartFile infoFile) {
        List<ItemVO> itemVO = new ArrayList<>();
        try (InputStream inputStream = infoFile.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); //첫 번째 시트 사용
            int rowCount = sheet.getPhysicalNumberOfRows();

            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                itemVO.add(new ItemVO(row.getCell(0).getStringCellValue(), ItemLevel.valueOf(row.getCell(1).getStringCellValue())));
            }

        } catch (IOException e) {
            log.error("[로그] 아이템 정보 파일 파싱 에러 (EXCEL -> VO)");
        }
        return itemVO;
    }
}
