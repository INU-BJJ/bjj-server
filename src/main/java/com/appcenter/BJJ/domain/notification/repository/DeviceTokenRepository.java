package com.appcenter.BJJ.domain.notification.repository;

import com.appcenter.BJJ.domain.notification.domain.DeviceToken;
import com.appcenter.BJJ.domain.notification.dto.MemberDeviceTokenDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByToken(String token);

    @Query("""
        SELECT new com.appcenter.BJJ.domain.notification.dto.MemberDeviceTokenDto(
            dt.member.id,
            dt.token
        )
        FROM DeviceToken dt
        WHERE dt.member.id IN :memberIds
            AND dt.isActive = true
    """)
    List<MemberDeviceTokenDto> findActiveTokensInMemberIds(List<Long> memberIds);

    List<DeviceToken> findAllByTokenIn(Collection<String> tokens);
}
