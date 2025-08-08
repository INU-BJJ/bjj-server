package com.appcenter.BJJ.domain.member.schedule;

import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.enums.MemberStatus;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberTaskService {
    @Qualifier("taskScheduler")
    private final TaskScheduler taskScheduler;
    private final MemberTaskRepository memberTaskRepository;
    private final MemberRepository memberRepository;

    public static final int BAN_COUNT = 3;

    @EventListener(ApplicationReadyEvent.class)
    public void initTask() {
        List<MemberTask> memberTasks = memberTaskRepository.findAllByMemberTaskStatus(MemberTaskStatus.PENDING);
        for (MemberTask memberTask : memberTasks) {
            Runnable task = getTask(memberTask);
            if (memberTask.getEndAt().isAfter(LocalDateTime.now())) {
                taskScheduler.schedule(task, Instant.from(memberTask.getEndAt()));
            } else {
                taskScheduler.schedule(task, Instant.now());
            }
        }
    }

    public void addOrUpdateTask(Long memberId, LocalDateTime startAt, LocalDateTime endAt) {
        MemberTask memberTask = memberTaskRepository.findPendingByMemberId(memberId).orElseGet(
                () -> MemberTask.create(memberId, startAt, endAt)
        );
        //원래 있던 것은 endAt만 update 해주면 됨
        memberTask.updateEndAt(endAt);

        Runnable task = getTask(memberTask);
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        taskScheduler.schedule(task, memberTask.getEndAt().atZone(zoneId).toInstant());
        memberTaskRepository.save(memberTask);
    }

    private Runnable getTask(MemberTask memberTask) {
        return () -> { // 회원 정지 풀어주기
            memberTask.updateMemberTaskStatue(MemberTaskStatus.COMPLETE);
            memberTaskRepository.save(memberTask);

            Member member = memberRepository.findById(memberTask.getMemberId()).orElseThrow(
                    () -> new CustomException(ErrorCode.USER_NOT_FOUND)
            );
            member.updateMemberStatus(MemberStatus.ACTIVE); //회원 활성화
            memberRepository.save(member);
        };
    }
}