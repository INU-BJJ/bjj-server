package com.appcenter.BJJ.domain.member.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SuspensionPeriod {

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public static SuspensionPeriod create() {
        return SuspensionPeriod.builder()
                .startAt(LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0))
                .endAt(LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0))
                .build();
    }

    public void suspend(LocalDateTime startAt, LocalDateTime endAt){
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
