package com.appcenter.BJJ.domain.item.service;

import com.appcenter.BJJ.domain.item.domain.Item;
import com.appcenter.BJJ.domain.item.dto.ItemRes;
import com.appcenter.BJJ.domain.item.dto.ItemVO;
import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class ItemManagementService {
    private final ItemRepository itemRepository;

    @Value("${storage.images.item}")
    private String ITEM_IMG_DIR;

    public List<ItemRes> uploadItems(MultipartFile infoFile, MultipartFile zipImageFile) throws IOException {
        if (itemRepository.existsAny()) {
            itemRepository.deleteAllRows();
        }

        String itemType = Objects.requireNonNull(infoFile.getOriginalFilename()).split("\\.")[0];

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

        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.isDirectory()) continue;

            String imageFileName = entry.getName().split("/")[1];
            File imageFile = new File(ITEM_IMG_DIR + itemType + "/" + imageFileName);
            //TODO directory 전체를 지우고 다시 디렉토리를 생성해서 이미지를 저장할까

            File dir = imageFile.getParentFile();
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("디렉토리 생성 실패: " + dir.getAbsolutePath());
            }

            //이미지 파일 write
            try (FileOutputStream outputStream = new FileOutputStream(  imageFile)) {
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
            //
        }
        return itemVO;
    }
}
