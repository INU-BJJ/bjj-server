package com.appcenter.BJJ.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseInitializer {

    @Value("${firebase.sdk.path}")
    private String FIREBASE_SDK_PATH;

    @PostConstruct // 애플리케이션이 시작될 때 자동으로 Firebase를 초기화
    public void initialize() {
        System.out.println("FIREBASE_SDK_PATH = " + FIREBASE_SDK_PATH);
        // json 파일을 저장한 위치 : (resources/)XXX-firebase-adminsdk.json
        try (InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(FIREBASE_SDK_PATH)) {

            if (serviceAccount == null) {
                throw new FileNotFoundException("Firebase service account file not found in classpath: XXX-firebase-adminsdk.json");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}