package com.appcenter.BJJ.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    WELCOME_EVENT(500);

    private final int rewardValue;
}
