package com.appcenter.BJJ.domain.member.schedule;

import com.appcenter.BJJ.domain.member.enums.MemberTaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberTaskRepository extends JpaRepository<MemberTask, Long> {
    List<MemberTask> findAllByMemberTaskStatus(MemberTaskStatus memberTaskStatus);

    @Query("SELECT m FROM MemberTask m WHERE m.memberTaskStatus = 'PENDING' AND m.id = :memberId AND m.memberTaskType = :memberTaskType")
    Optional<MemberTask> findPendingByMemberIdAndMemberTaskType(Long memberId, MemberTaskType memberTaskType);


    @Query("SELECT COUNT(m) FROM MemberTask m WHERE m.memberId = :memberId AND m.memberTaskStatus = 'COMPLETE'")
    Long countByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);
}