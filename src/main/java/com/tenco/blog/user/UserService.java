package com.tenco.blog.user;

import com.tenco.blog._core.errors.Exception400;
import com.tenco.blog._core.errors.Exception403;
import com.tenco.blog._core.errors.Exception404;
import com.tenco.blog._core.errors.Exception500;
import com.tenco.blog._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * User 관련 비즈니스 로직을 처리하는 Service 계층
 * Controller 와 Repository 사이에서 실제 업무 로직을 담당
 */

@Slf4j
@Service // IoC
@RequiredArgsConstructor // DI
@Transactional(readOnly = true) // 기본적인 읽기 전용 트랜잭션 처리, 조회 시 더티체킹 안 일어남
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원 가입 처리
     *
     * @param joinDTO (사용자 회원가입 요청 정보)
     * @return User (저장된 사용자 정보)
     */
    @Transactional
    public User 회원가입(UserRequest.JoinDTO joinDTO) {
        // 1. 로그 기록 - 회원가입 요청 정보
        // 2. 사용자명 중복 검사 (데이터베이스 조회)
        // 3. username 존재하면 Exception400 예외 발생
        // 4. JoinDTO --> User 객체로 변환 처리
        // 5. 데이터베이스에 사용자 정보 저장
        // 6. 로그 기록 - 회원 가입 완료
        // 7. 저장된 사용자 정보 컨트롤러로 반환

        log.info("회원가입 서비스 시작");
        // 조건 - 중복된 사용자 이름이 없는 것이 정상 동작
        userRepository.findByUsername(joinDTO.getUsername()).ifPresent(user -> {
            log.warn("회원가입 실패 -  중복된 사용자명 : {}", user.getUsername());
            throw new Exception400("이미 존재하는 사용자명입니다.");
        });

        // 프로필 이미지 저장 기능 구현 (선택사항임)
        String profileImageFilename = null;
        if (joinDTO.getProfileImage() != null && joinDTO.getProfileImage().isEmpty() == false) {
            // 사용자가 프로필 이미지를 업로드 한 경우
            // 이미지 파일이 맞는지 확인
            try {
                if (FileUtil.isImageFile(joinDTO.getProfileImage()) == false) {
                    throw new Exception400("이미지 파일만 업로드 가능합니다");
                }
                profileImageFilename = FileUtil.saveFile(joinDTO.getProfileImage(), FileUtil.IMAGES_DIR);
            } catch (Exception e) {
                // 디스크 공간이 없거나, 권한 없음
                throw new Exception500("프로필 이미지 저장 실패");
            }


        }

        User userEntity = joinDTO.toEntity(profileImageFilename);
        userEntity.addRole(Role.USER);
        User savedUserEntity = userRepository.save(userEntity);
        log.info("회원가입 서비스 완료 -  ID : {}", savedUserEntity.getId());
        return savedUserEntity;

    }

    /**
     * 로그인 처리
     *
     * @param loginDTO (사용자가 요청한 로그인 정보)
     * @return User(조회된 정보 세션 저장용)
     */
    public User 로그인(UserRequest.LoginDTO loginDTO) {
        // 1. 로그 기록 - 로그인 요청 정보(사용자명)
        // 2. 사용자 이름과 비밀번호로 데이터베이스에서 조회
        // 3. 인증 정보가 일치하지 않으면 Exception400 예외 처리
        // 4. 로그 기록 - 로그인 성공 정보
        // 5. 인증된 사용자 정보 컨트롤러 단으로 반환(세션 저장용)

        log.info("로그인 서비스 시작");
        User userEntity = userRepository.findByUsernameAndPasswordWithRoles(loginDTO.getUsername(), loginDTO.getPassword())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자 이름 또는 사용자 비번 잘못 입력");
                    return new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다.");
                });
        log.info("로그인 성공! - 사용자명 : {}", loginDTO.getUsername());

        return userEntity;
    }

    /**
     * 사용자 정보 조회(프로필 정보 보기 활용)
     *
     * @param id (User PK)
     * @return UserEntity
     */
    public User 회원정보수정화면(Integer id) {
        log.info("사용자 정보 서비스 시작");

        User userEntity = userRepository.findById(id).orElseThrow(() -> {
            log.warn("사용자 정보 조회 실패");
            return new Exception404("사용자 정보를 찾을 수 없습니다.");
        });

        return userEntity;
    }

    /**
     * 사용자 정보 수정 처리 (프로필 업데이트)
     *
     * @param id        (User PK)
     * @param updateDTO (사용자가 요청한 데이터)
     * @return User
     */
    @Transactional
    public User 회원정보수정(Integer id, UserRequest.UpdateDTO updateDTO) {
        // 1. 로그 기록 - 회원 정보 수정 요청 정보 (ID)
        // 2. 수정하려면 사용자 정보 조회
        // 3. 예외 처리 Exception400
        // 4. 더티 체킹을 통한 사용자 정보 수정(JPA 영속성 컨텍스트 활용)
        // 5. 로그 기록 - 수정 완료 로그 처리
        // 6. 수정된 사용자 정보 컨트롤러 단으로 반환 (세션 동기화용)

        log.info("회원 정보 수정 서비스 시작");
        User userEntity = userRepository.findById(id).orElseThrow(
                () -> new Exception404("사용자 정보를 찾을 수 없습니다."));

        // 프로필 이미지 처리 (사용자가 이미지를 보냈다면)
        String uuidImageFileName = null;
        if (updateDTO.getProfileImage() != null && !updateDTO.getProfileImage().isEmpty()) {
            // 새 프로필 정보 수정 요청
            // 1. 기존 프로필 사진이 있다면 삭제하고 새로 저장 (디스크, db수정)
            // 2. 기존에는 프로필 이미지가 null 인 경우
            String oldProfileImage = userEntity.getProfileImage(); // null, 기존 이미지명
            // String newProfileImage = updateDTO.getProfileImage().getOriginalFilename();

            if (!FileUtil.isImageFile(updateDTO.getProfileImage())) {
                throw new Exception400("이미지 파일만 업로드 가능합니다.");
            }

            // 신규 이미지 저장
            try {
                uuidImageFileName = FileUtil.saveFile(updateDTO.getProfileImage(), FileUtil.IMAGES_DIR);
                // 기존 이미지 삭제 처리 (있다면)
                if (oldProfileImage != null) {
                    FileUtil.deleteFile(oldProfileImage, FileUtil.IMAGES_DIR);
                }

            } catch (IOException e) {
                throw new Exception500("프로필 이미지 파일 저장 실패");
            }
        }
        // 더티 체킹 활용
        userEntity.update(updateDTO, uuidImageFileName);
        return userEntity;
    }

    @Transactional
    public User 프로필이미지삭제(Integer id) {
        User userEntity = userRepository.findById(id).orElseThrow(
                () -> new Exception404("사용자를 찾을 수 없습니다."));

        // 인가 처리
        if (userEntity.getId().equals(id) == false) {
            throw new Exception403("프로필 이미지 삭제 권한 없음");
        }

        // 3. 이미지가 등록되어 있으면 삭제 처리
        String profileImage = userEntity.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            // 내 서버 컴퓨터에 저장된(C://upload) 파일 삭제
            try {
                FileUtil.deleteFile(profileImage, FileUtil.IMAGES_DIR);
            } catch (IOException e) {
                System.err.println("프로필 이미지 삭제시 오류 발생" + e.getMessage());
            }
        }
        // 1차 캐시에 저장된 User 정보 수정 - 트랜잭션이 종료되면 반영(더티 체킹)
        userEntity.setProfileImage(null);

        return userEntity;

    }
}
