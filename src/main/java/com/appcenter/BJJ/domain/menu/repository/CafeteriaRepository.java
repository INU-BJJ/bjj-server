package com.appcenter.BJJ.domain.menu.repository;

import com.appcenter.BJJ.domain.menu.domain.Cafeteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CafeteriaRepository extends JpaRepository<Cafeteria, Long> {
    Optional<Cafeteria> findByNameAndCorner(String name, String corner);
    Optional<Cafeteria> findFirstByName(String name);
    @Query("""
        SELECT c.corner
        FROM Cafeteria c
        JOIN Menu m ON m.cafeteriaId = c.id
        JOIN MenuPair mp ON mp.mainMenuId = m.id
        WHERE mp.id = :menuPairId
    """)
    Optional<String> findCafeteriaCornerByMenuPairId(Long menuPairId);
}
