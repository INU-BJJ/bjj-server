package com.appcenter.BJJ.domain.banner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<BannerRes> findVisibleBanners() {
        return bannerRepository.findAllBySortOrderIsNotNullOrderBySortOrder()
                .stream()
                .map(b -> BannerRes.builder()
                        .pageUri(b.getPageUri())
                        .imageName(b.getImageName())
                        .build())
                .toList();
    }
}
