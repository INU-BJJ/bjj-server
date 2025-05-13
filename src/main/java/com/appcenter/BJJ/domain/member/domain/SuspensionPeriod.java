package com.appcenter.BJJ.domain.member.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Embeddable
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SuspensionPeriod {

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public static SuspensionPeriod init() {
        return new SuspensionPeriod(
                LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0),
                LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0)
        );
    }

    public void suspend(LocalDateTime startAt, LocalDateTime endAt){
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
