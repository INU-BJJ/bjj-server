package com.appcenter.BJJ.repository;

import com.appcenter.BJJ.domain.MenuPair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuPairRepository extends JpaRepository<MenuPair, Long> {

    Optional<MenuPair> findFirstByMainMenuIdAndSubMenuId(Long mainMenuId, Long subMenuId);

}
