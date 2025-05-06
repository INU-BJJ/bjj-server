package com.appcenter.BJJ.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ReviewSuspensionException extends RuntimeException {
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
}
