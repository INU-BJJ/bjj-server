package com.appcenter.BJJ.domain.member.schedule;

import com.appcenter.BJJ.domain.item.service.InventoryService;
import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.enums.MemberStatus;
import com.appcenter.BJJ.domain.notification.service.DeviceTokenService;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 관련 스케줄링 작업의 실제 비즈니스 로직을 처리하는 핸들러 클래스
 *
 * <p>{@link MemberTaskService}의 TaskScheduler에 의해 호출되며,
 * 트랜잭션이 없는 스케줄러 스레드를 대신하여 각 작업을 트랜잭션 범위 안에서 실행합니다.</p>
 */
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberTaskHandler {
    private final MemberRepository memberRepository;
    private final InventoryService inventoryService;
    private final DeviceTokenService deviceTokenService;
    private final MemberTaskRepository memberTaskRepository;

    /**
     * 회원을 활성화 상태로 변경합니다.
     *
     * <p>회원 정지 기간이 만료되었을 때 TaskScheduler에 의해 호출됩니다.</p>
     *
     * @param memberId 정지 해제할 회원 ID
     * @throws CustomException 회원을 찾을 수 없는 경우 {@link ErrorCode#USER_NOT_FOUND}
     */
    @Transactional
    public void activateMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        member.updateMemberStatus(MemberStatus.ACTIVE);
    }

    /**
     * 탈퇴 유예 기간이 만료된 회원의 관련 도메인 데이터를 삭제합니다.
     *
     * <p>회원 탈퇴 후 한 달이 경과하였을 때 TaskScheduler에 의해 호출됩니다.</p>
     *
     * <ul>
     *     <li>리뷰, 리뷰 좋아요, 리뷰 신고, 이벤트 : 수집용 데이터로써 유지</li>
     *     <li>인벤토리, 디바이스 토큰, 회원의 스케줄링 Task : 하드 삭제</li>
     * </ul>
     *
     * @param memberId 데이터를 삭제할 회원 ID
     */
    @Transactional
    public void executeMemberDeletion(Long memberId) {
        inventoryService.delete((memberId));
        deviceTokenService.delete(memberId);
        memberTaskRepository.deleteAllByMemberId(memberId);
    }
}
