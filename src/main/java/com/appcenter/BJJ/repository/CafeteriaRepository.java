package com.appcenter.BJJ.repository;

import com.appcenter.BJJ.domain.Cafeteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeteriaRepository extends JpaRepository<Cafeteria, Long> {
    Optional<Cafeteria> findByNameAndCorner(String name, String corner);
}
