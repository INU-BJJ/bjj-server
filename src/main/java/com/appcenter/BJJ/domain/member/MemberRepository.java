package com.appcenter.BJJ.domain.member;

import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.notification.dto.NotifiableMemberDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByProviderId(String providerId);

    Optional<Member> findByEmailAndProviderId(String email, String providerId);

    Optional<Member> findByEmailAndProvider(String email, String provider);

    Optional<Member> findByNickname(String nickname);

    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.nickname = :nickname AND m.role = 'ROLE_USER'")
    boolean existsByNickname(String nickname);

    boolean existsByProviderId(String providerId);

    @Query("""
        SELECT new com.appcenter.BJJ.domain.notification.dto.NotifiableMemberDto(
                ml.menuId,
                m.id,
                m.nickname
        )
        FROM Member m
        INNER JOIN MenuLike ml ON m.id = ml.memberId
        WHERE ml.menuId IN :menuIds
            AND m.isNotificationEnabled = true
    """)
    List<NotifiableMemberDto> findNotifiableMembersByLikedMenus(List<Long> menuIds);
}
