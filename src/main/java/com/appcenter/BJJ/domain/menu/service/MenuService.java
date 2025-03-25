package com.appcenter.BJJ.domain.menu.service;

import com.appcenter.BJJ.domain.menu.domain.Menu;
import com.appcenter.BJJ.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional
    public Long getOrCreateMenu(String menuName, Long cafeteriaId) {
        return menuRepository.findFirstByMenuNameAndCafeteriaId(menuName, cafeteriaId)
                .orElseGet(() -> menuRepository.save(Menu.builder()
                        .menuName(menuName)
                        .cafeteriaId(cafeteriaId)
                        .build())
                ).getId();
    }
}
