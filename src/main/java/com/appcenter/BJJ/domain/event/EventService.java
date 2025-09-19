package com.appcenter.BJJ.domain.event;

import com.appcenter.BJJ.domain.member.MemberService;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final MemberService memberService;

    private final static EventType WELCOME_EVENT = EventType.WELCOME_EVENT;

    @Transactional
    public boolean welcomePoint(Long memberId) {
        long participatedCount = eventRepository.countAllByMemberIdAndEventType(memberId, WELCOME_EVENT);
        if (participatedCount == 0) {
            memberService.updatePoint(memberId, WELCOME_EVENT.getRewardValue());
            eventRepository.save(Event.create(memberId, WELCOME_EVENT));
            return true;
        } else if (participatedCount == 1) {
            return false;
        }
        log.error("[로그] 웰컴이벤트 포인트 지급 서버 오류 발생. memberId={}, participatedCount={}", memberId, participatedCount);
        throw new CustomException(ErrorCode.SERVER_ERROR);
    }
}
