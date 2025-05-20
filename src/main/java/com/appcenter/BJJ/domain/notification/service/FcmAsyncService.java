package com.appcenter.BJJ.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmAsyncService {

    @Async("fcmTaskExecutor")
    protected CompletableFuture<List<String>> sendMulticastWithRetry(MulticastMessage message, List<String> tokens, String menuName, int attempt) {
        List<String> invalidTokens = new ArrayList<>();
        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            int success = response.getSuccessCount();
            int failure = response.getFailureCount();
            log.info("[로그] 메뉴 이름: {}, 성공: {}, 실패: {}", menuName, success, failure);

            if (failure > 0) {
                List<SendResponse> responses = response.getResponses();

                for (int i = 0; i < responses.size(); i++) {
                    SendResponse sendResponse = responses.get(i);
                    if (!sendResponse.isSuccessful()) {
                        FirebaseMessagingException ex = sendResponse.getException();
                        MessagingErrorCode errorCode = ex != null ? ex.getMessagingErrorCode() : null;
                        if (MessagingErrorCode.UNREGISTERED.equals(errorCode) || MessagingErrorCode.INVALID_ARGUMENT.equals(errorCode)) {
                            invalidTokens.add(tokens.get(i));
                        }

                        log.debug("[로그] 메뉴 이름: {}, 실패 원인: {}", menuName,
                                sendResponse.getException() != null ? sendResponse.getException().getMessage() : "알 수 없음");
                    }
                }
            }

        } catch (FirebaseMessagingException e) {
            if (attempt < 3) {
                log.warn("[로그] 메뉴 이름: {} 전송 실패, {}번째 재시도 중...", menuName, attempt + 1);
                try {
                    Thread.sleep(1000 * 10); // 10초 정도 백오프
                } catch (InterruptedException ignored) {}
                sendMulticastWithRetry(message, tokens, menuName, attempt + 1);
            } else {
                log.error("[로그] 메뉴 이름: {}, 전송 3회 실패: {}", menuName, e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(invalidTokens);
    }
}