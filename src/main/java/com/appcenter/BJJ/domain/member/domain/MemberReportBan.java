package com.appcenter.BJJ.domain.member.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberReportBan {

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public static MemberReportBan create() {
        return MemberReportBan.builder()
                .startAt(null)
                .endAt(null)
                .build();
    }
}
