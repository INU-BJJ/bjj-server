package com.appcenter.BJJ.domain.notification.repository;

import com.appcenter.BJJ.domain.notification.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByToken(String token);
}
