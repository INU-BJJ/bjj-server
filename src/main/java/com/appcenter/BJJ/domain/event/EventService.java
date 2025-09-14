package com.appcenter.BJJ.domain.event;

import com.appcenter.BJJ.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final MemberService memberService;

    private final static EventType WELCOME_EVENT = EventType.WELCOME_EVENT;

    public boolean welcomePoint(Long memberId) {
        boolean isParticipated = eventRepository.existsByMemberIdAndEventType(memberId, WELCOME_EVENT);
        if (isParticipated) return false; // 이미 참여 완료

        memberService.updatePoint(memberId, WELCOME_EVENT.getRewardValue());
        eventRepository.save(Event.create(memberId, WELCOME_EVENT));
        return true;
    }
}
