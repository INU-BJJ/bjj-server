package com.appcenter.BJJ.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmAsyncService {

    @Async("fcmTaskExecutor")
    protected void sendMulticastWithRetry(MulticastMessage message, Long memberId, int attempt) {
        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            int success = response.getSuccessCount();
            int failure = response.getFailureCount();
            log.info("[로그] Member ID: {}, 성공: {}, 실패: {}", memberId, success, failure);

            if (failure > 0) {
                for (SendResponse sendResponse : response.getResponses()) {
                    if (!sendResponse.isSuccessful()) {
                        log.debug("[로그] Member ID: {}, 실패 원인: {}", memberId,
                                sendResponse.getException() != null ? sendResponse.getException().getMessage() : "알 수 없음");
                    }
                }
            }

        } catch (FirebaseMessagingException e) {
            if (attempt < 3) {
                log.warn("[로그] Member ID: {} 전송 실패, {}번째 재시도 중...", memberId, attempt + 1);
                try {
                    Thread.sleep(1000 * 10); // 10초 정도 백오프
                } catch (InterruptedException ignored) {}
                sendMulticastWithRetry(message, memberId, attempt + 1);
            } else {
                log.error("[로그] Member ID: {}, 전송 3회 실패: {}", memberId, e.getMessage());
            }
        }
    }
}