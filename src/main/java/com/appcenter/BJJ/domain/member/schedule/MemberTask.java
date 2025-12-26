package com.appcenter.BJJ.domain.member.schedule;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "member_task_tb")
public class MemberTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private MemberTaskStatus memberTaskStatus;

    @Builder
    private MemberTask(Long memberId, LocalDateTime startAt, LocalDateTime endAt, MemberTaskStatus memberTaskStatus) {
        this.memberId = memberId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.memberTaskStatus = memberTaskStatus;
    }

    public static MemberTask create(Long memberId, LocalDateTime startAt, LocalDateTime endAt) {
        return MemberTask.builder()
                .memberId(memberId)
                .startAt(startAt)
                .endAt(endAt)
                .memberTaskStatus(MemberTaskStatus.PENDING)
                .build();
    }

    public void updateEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public void updateMemberTaskStatue(MemberTaskStatus memberTaskStatus) {
        this.memberTaskStatus = memberTaskStatus;
    }
}
