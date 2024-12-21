package com.appcenter.BJJ.domain.member.repository;

import com.appcenter.BJJ.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByProviderId(String providerId);

    Optional<Member> findByEmailAndProviderId(String email, String providerId);

    Optional<Member> findByEmailAndProvider(String email, String provider);

    Optional<Member> findByNickname(String nickname);

    boolean existsByNickname(String nickname);
}
