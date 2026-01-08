package com.appcenter.BJJ.global.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ResourceLoader {

    public static InputStream load(String path) throws IOException {
        File file = new File(path);

        // 1. 로컬/절대경로에서 먼저 시도
        if (file.exists() && file.isFile()) {
            log.info("[로그] 로컬 파일에서 로드: {}", file.getAbsolutePath());
            return new FileInputStream(file);
        }

        // 2. Classpath 리소스에서 시도
        InputStream classpathStream = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
        if (classpathStream != null) {
            log.info("[로그] Classpath 리소스에서 로드: {}", path);
            return classpathStream;
        }

        // 3. 둘 다 실패
        throw new FileNotFoundException("[로그] 리소스를 찾을 수 없습니다: " + path);
    }
}
