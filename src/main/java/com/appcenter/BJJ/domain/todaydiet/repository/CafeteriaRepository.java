package com.appcenter.BJJ.domain.todaydiet.repository;

import com.appcenter.BJJ.domain.todaydiet.domain.Cafeteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CafeteriaRepository extends JpaRepository<Cafeteria, Long> {

    @Query("SELECT c.id FROM Cafeteria c WHERE c.name = :name AND c.corner = :corner")
    Optional<Long> findIdByNameAndCorner(String name, String corner);

    List<Cafeteria> findByName(String name);

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
