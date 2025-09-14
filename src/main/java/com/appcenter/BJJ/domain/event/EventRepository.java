package com.appcenter.BJJ.domain.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
            SELECT COUNT(*) = 1
            FROM Event e
            WHERE e.memberId = :memberId
            AND e.eventType = :eventType
            """)
    boolean existsByMemberIdAndEventType(Long memberId, EventType eventType);
}
