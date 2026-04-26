package com.appcenter.BJJ.domain.member.schedule;

import com.appcenter.BJJ.domain.member.enums.MemberTaskType;
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

    @Enumerated(EnumType.STRING)
    private MemberTaskType memberTaskType;

    @Builder
    private MemberTask(Long memberId, LocalDateTime startAt, LocalDateTime endAt, MemberTaskStatus memberTaskStatus, MemberTaskType memberTaskType) {
        this.memberId = memberId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.memberTaskStatus = memberTaskStatus;
        this.memberTaskType = memberTaskType;
    }

    public static MemberTask create(Long memberId, LocalDateTime startAt, LocalDateTime endAt, MemberTaskType memberTaskType) {
        return MemberTask.builder()
                .memberId(memberId)
                .startAt(startAt)
                .endAt(endAt)
                .memberTaskStatus(MemberTaskStatus.PENDING)
                .memberTaskType(memberTaskType)
                .build();
    }

    public void updateEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public void complete() {
        this.memberTaskStatus = MemberTaskStatus.COMPLETE;
    }
}
