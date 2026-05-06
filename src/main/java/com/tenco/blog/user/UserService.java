package com.tenco.blog.user;

import com.tenco.blog._core.errors.Exception400;
import com.tenco.blog._core.errors.Exception404;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public User join(UserRequest.JoinDTO joinDTO) {
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
            throw new Exception400("이미 존재하는 사용명입니다.");
        });

        User user = joinDTO.toEntity();
        User savedUserEntity = userRepository.save(user);
        log.info("회원가입 서비스 완료 -  ID : {}", savedUserEntity.getId());
        return savedUserEntity;

    }

    /**
     * 로그인 처리
     * @param loginDTO (사용자가 요청한 로그인 정보)
     * @return User(조회된 정보 세션 저장용)
     */
    public User login(UserRequest.LoginDTO loginDTO) {
        // 1. 로그 기록 - 로그인 요청 정보(사용자명)
        // 2. 사용자 이름과 비밀번호로 데이터베이스에서 조회
        // 3. 인증 정보가 일치하지 않으면 Exception400 예외 처리
        // 4. 로그 기록 - 로그인 성공 정보
        // 5. 인증된 사용자 정보 컨트롤러 단으로 반환(세션 저장용)

        log.info("로그인 서비스 시작");
        User userEntity = userRepository.findByUsernameAndPassword(loginDTO.getUsername(), loginDTO.getPassword())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자 이름 또는 사용자 비번 잘못 입력");
                    return new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다.");
                });
        log.info("로그인 성공! - 사용자명 : {}", loginDTO.getUsername());

        return userEntity;
    }

    /**
     * 사용자 정보 조회(프로필 정보 보기 활용)
     * @param id (User PK)
     * @return UserEntity
     */
    public User findById(Integer id) {
        log.info("사용자 정보 서비스 시작");

        return userRepository.findById(id).orElseThrow(() -> {
            log.warn("사용자 정보 조회 실패");
            return new Exception404("사용자 정보를 찾을 수 없습니다.");
        });
    }

    /**
     *  사용자 정보 수정 처리 (프로필 업데이트)
     * @param id (User PK)
     * @param updateDTO (사용자가 요청한 데이터)
     * @return User
     */
    @Transactional
    public User updateById(Integer id, UserRequest.UpdateDTO updateDTO) {
        // 1. 로그 기록 - 회원 정보 수정 요청 정보 (ID)
        // 2. 수정하려면 사용자 정보 조회
        // 3. 예외 처리 Exception400
        // 4. 더티 체킹을 통한 사용자 정보 수정(JPA 영속성 컨텍스트 활용)
        // 5. 로그 기록 - 수정 완료 로그 처리
        // 6. 수정된 사용자 정보 컨트롤러 단으로 반환 (세션 동기화용)

        log.info("회원 정보 수정 서비스 시작");
        User userEntity = findById(id);
        userEntity.update(updateDTO);

        log.info("회원정보 수정 완료 - 사용자 ID : {}" , userEntity.getId());

        return userEntity;


    }
}
