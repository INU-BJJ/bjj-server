package com.appcenter.BJJ.global.config;

import com.appcenter.BJJ.global.util.ResourceLoader;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class FirebaseInitializer {

    @Value("${firebase.sdk.path}")
    private String FIREBASE_SDK_PATH;

    @PostConstruct // 애플리케이션이 시작될 때 자동으로 Firebase를 초기화
    public void initialize() {
        // 로컬에서 SDK 파일을 저장한 위치 : (resources/)XXX-firebase-adminsdk.json
        try (InputStream serviceAccount = ResourceLoader.load(FIREBASE_SDK_PATH)) {

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            log.error("[로그] Firebase SDK 초기화 실패 - 경로: {}", FIREBASE_SDK_PATH, e);
        }
    }
}