package com.appcenter.BJJ.domain.event;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "event_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;


    @Builder
    public Event(Long memberId, EventType eventType) {
        this.memberId = memberId;
        this.eventType = eventType;
    }

    public static Event create(Long memberId, EventType eventType) {
        return Event.builder()
                .memberId(memberId)
                .eventType(eventType)
                .build();
    }
}
