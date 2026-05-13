package com.tenco.blog.util;

import com.tenco.blog._core.errors.Exception400;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// IoC 안함 (파일 기능 처리에만 동작할 수 있도록 static 메서드로 구현할 예정)
public class FileUtil {

    // 업로드될 파일 경로를 미지 상수로 지정
    public static final String IMAGES_DIR = "C:\\upload";

    // 1. 파일 저장하는 기능
    public static String saveFile(MultipartFile file, String uploadDir) throws IOException {
        // 1단계 : 파일 유효성 검사 - 파일이 없거나 크기가 0이면 오류
        if (file == null || file.isEmpty()) {
            return null; // 프로필 이미지 업로드는 선택사항임
        }

        // 2단계 : 파일 업로드 경로 생성 (존재 여부 확인)
        // Path : 파일 시스템 경로를 나타내는 객체
        // Path.get() : 문자열 경로를 Path 객체로 변환해주는 객체
        Path uploadPath = Paths.get(IMAGES_DIR);

        // Files.exists() : 파일/디렉토리 여부 확인
        if(Files.exists(uploadPath) == false) {
            // 현재 서버 컴퓨터에 images/* 없는상태
            Files.createDirectories(uploadPath); // 상위 폴더까지 자동 생성해줌
        }

        // 3단계 : 원본 파일 이름 가져오기
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new Exception400("파일명이 없습니다.");
        }

        // 4단계 : UUID 를 사용한 고유 파일명 생성
        String uuid = UUID.randomUUID().toString(); // 난수 발생
        String savedFileName = uuid + "_" + originalFilename;
        // 예 ) "1234123-123e-123_a.png 파일명으로 재생성됨

        // 5단계 : 메모리상에 존재하는 파일 데이터를 로컬 컴퓨터(디스크)에 저장
        // 5-1 - 파일폴더경로 + 재생성한 파일 이름 --> 정확한 위치에 파일이 생성됨
        // 예 :  images/123-2322-123_a.png
        Path filePath = uploadPath.resolve(savedFileName);

        Files.copy(file.getInputStream(), filePath);
        return savedFileName;
    }

    // 2. 파일 삭제하는 기능

    // 3. 편의 기능 만들 예정 (이미지 파일이 맞는지 확인)
    public static boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // pdf, hwp.. <-- 막아줘야함
        String contentType = file.getContentType(); // image/png, application/pdf ~
        boolean isImage = contentType.startsWith("image/");
        return isImage;
    }


}
