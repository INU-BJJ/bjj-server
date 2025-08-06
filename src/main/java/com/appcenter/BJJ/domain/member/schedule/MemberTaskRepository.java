package com.appcenter.BJJ.domain.member.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberTaskRepository extends JpaRepository<MemberTask, Long> {
    List<MemberTask> findAllByMemberTaskStatus(MemberTaskStatus memberTaskStatus);

    @Query("SELECT m FROM MemberTask m WHERE m.memberTaskStatus = 'PENDING'")
    Optional<MemberTask> findPendingByMemberId(Long memberId);

    Long countByMemberId(Long memberId);
}
