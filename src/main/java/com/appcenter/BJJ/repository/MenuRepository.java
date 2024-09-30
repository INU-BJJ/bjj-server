package com.appcenter.BJJ.repository;

import com.appcenter.BJJ.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findFirstByMenuNameAndCafeteriaId(String menuName, Long cafeteriaId);
}
