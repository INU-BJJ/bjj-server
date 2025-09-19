package com.appcenter.BJJ.domain.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    long countAllByMemberIdAndEventType(Long memberId, EventType eventType);
}
