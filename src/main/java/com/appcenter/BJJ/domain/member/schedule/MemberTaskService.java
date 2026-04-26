package com.appcenter.BJJ.domain.member.schedule;

import com.appcenter.BJJ.domain.member.enums.MemberTaskType;
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


/**
 * 회원의 상태 변경을 예약/실행/관리하는 스케줄링 클래스
 */
@Service
@RequiredArgsConstructor
public class MemberTaskService {
    @Qualifier("taskScheduler")
    private final TaskScheduler taskScheduler;
    private final MemberTaskRepository memberTaskRepository;
    private final MemberTaskHandler memberTaskHandler;
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    /**
     * 애플리케이션 시작 시 DB에 저장된 PENDING 상태의 회원 작업을 조회하여 TaskScheduler에 다시 등록합니다.
     *
     * <p>{@link TaskScheduler}는 메모리 기반으로 동작하기 때문에 서버 재시작 시 기존 스케줄이 모두 사라집니다.
     * 이를 보완하기 위해 미완료 작업을 DB에서 조회하여 재스케줄링합니다.</p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initTask() {
        List<MemberTask> memberTasks = memberTaskRepository.findAllByMemberTaskStatus(MemberTaskStatus.PENDING);
        for (MemberTask memberTask : memberTasks) {
            Runnable task = getTask(memberTask);
            if (memberTask.getEndAt().isAfter(LocalDateTime.now())) {
                taskScheduler.schedule(task, memberTask.getEndAt().atZone(ZONE_ID).toInstant());
            } else {
                taskScheduler.schedule(task, Instant.now());
            }
        }
    }

    public void addOrUpdateTask(Long memberId, LocalDateTime startAt, LocalDateTime endAt, MemberTaskType memberTaskType) {
        MemberTask memberTask = memberTaskRepository.findPendingByMemberIdAndMemberTaskType(memberId, memberTaskType).orElseGet(
                () -> MemberTask.create(memberId, startAt, endAt, memberTaskType)
        );
        //원래 있던 것은 endAt만 update 해주면 됨
        memberTask.updateEndAt(endAt);

        Runnable task = getTask(memberTask);
        taskScheduler.schedule(task, memberTask.getEndAt().atZone(ZONE_ID).toInstant());
        memberTaskRepository.save(memberTask);
    }


    /**
     * 회원 작업 타입에 따라 실행할 Runnable을 생성합니다.
     *
     * <p>정지 해제 또는 회원 탈퇴와 같은 작업을 수행하며,
     * 작업 완료 후 MemberTask 상태를 COMPLETE로 변경합니다.</p>
     *
     * <p>TaskScheduler에서 실행되는 Runnable은 Spring의 트랜잭션(@Transactional) 적용 대상이 아니므로,
     * 엔티티 변경 시 변경 감지(Dirty Checking)가 동작하지 않습니다.
     * 따라서 데이터 변경 사항을 반영하기 위해 Repository의 save()를 직접 호출합니다.</p>
     */
    private Runnable getTask(MemberTask memberTask) {
        return () -> {
            switch (memberTask.getMemberTaskType()) {
                case SUSPENDED -> {
                    // 회원 정지 풀어주기
                    memberTaskHandler.activateMember(memberTask.getMemberId());
                    memberTask.complete();
                    memberTaskRepository.save(memberTask);
                }
                case DELETE -> {
                    // 회원의 탈퇴 작업 진행
                    memberTaskHandler.executeMemberDeletion(memberTask.getMemberId());
                }
            }
        };
    }
}