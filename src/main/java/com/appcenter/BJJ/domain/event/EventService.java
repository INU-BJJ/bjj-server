package com.appcenter.BJJ.domain.event;

import com.appcenter.BJJ.domain.member.MemberService;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (participatedCount >= 1) {
            throw new CustomException(ErrorCode.EVENT_ALREADY_PARTICIPATED);
        }

        memberService.updatePoint(memberId, WELCOME_EVENT.getRewardValue());
        eventRepository.save(Event.create(memberId, WELCOME_EVENT));
        return true;
    }
}
