package com.appcenter.BJJ.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class NotificationInfoDto {
    private final String menuName;
    private final String cafeteriaName;
    private final String cafeteriaCorner;
    private final List<String> fcmTokens;
}
