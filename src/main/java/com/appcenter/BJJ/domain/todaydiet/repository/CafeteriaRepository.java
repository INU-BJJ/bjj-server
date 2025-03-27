package com.appcenter.BJJ.domain.todaydiet.repository;

import com.appcenter.BJJ.domain.todaydiet.domain.Cafeteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CafeteriaRepository extends JpaRepository<Cafeteria, Long> {

    @Query("SELECT c.id FROM Cafeteria c WHERE c.name = :name AND c.corner = :corner")
    Optional<Long> findIdByNameAndCorner(String name, String corner);
    Optional<Cafeteria> findFirstByName(String name);
}
