package com.tenco.blog.board;

import com.tenco.blog._core.errors.Exception404;
import com.tenco.blog.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 객체 지향 개념 = 단일 책임의 원칙

@Slf4j
// 서비스 계층은 @Service 어노테이션으로 IoC 처리
@Service // IoC 처리
@RequiredArgsConstructor // DI 처리
@Transactional(readOnly = true)
// 모든 메서드를 읽기 전용 트랜잭션으로 실행(findAll, findById 등 조회에 적합)
// 성능 최적화 (변경 감지 비활성화 됨), 즉 조회 시 데이터 수정 방지
public class BoardService {

    private final BoardRepository boardRepository;

    // 게시글 저장
    // 데이터 수정이 필요하므로 깊은 트랜잭션 처리
    // (읽기 전용 트랜잭션을 해제, 쓰기 전용 트랜잭션으로 변경)
    @Transactional
    public Board save(BoardRequest.SaveDTO saveDTO, User sessionUser) {
        // . 로그 기록 - 게시글 저장 요청 정보
        // 2. DTo 를 Entity 로 변환(작성자 정보 포함)
        // 3. 데이터베이스에 게시글 저장
        // 4. 저장 완료, 로그 기록
        // 5. 저장된 게시글을 컨트롤 단으로 반환

        // 1
        log.info("게시글 저장 서비스 시작 - 제목 : {}, 작성자 : {}",
                saveDTO.getTitle(), sessionUser.getUsername());

        // 2
        Board board = saveDTO.toEntity(sessionUser);
        // 3
        Board savedBoardEntity = boardRepository.save(board);
        // 4
        log.info("게시글 저장 완료 - ID : {}, 제목 : {}",
                savedBoardEntity.getId(), savedBoardEntity.getTitle());
        // 5
        return savedBoardEntity;
    }

    // 게시글 목록 조회
    public List<Board> findAll() {
        // 1. 로그 기록 - 게시글 목록 조회
        // 2. 데이터베이스 접근해서 모든 게시글 목록을 조회
        // 3. 로그 기록 - (총 게시글 수)
        // 4. 조회된 게시글 목록을 컨트롤러로 반환

        log.info("게시글 목록 조회 서비스");
        List<Board> boardList = boardRepository.findAllJoinUser();
        log.info("게시글 목록 조회 완료 - 총 : {}", boardList.size());
        return boardList;

    }

    // 게시글 상세 보기
    public Board findById(Integer id) {
        // 1. 로그 기록 - 게시글 상세 조회 (id)
        // 2. 데이터베이스 접근해서 해당 ID의 게시글 조회 (작성자 정보 포함)
        // 3. 게시글이 존재하지 않으면 Exception404 로 예외 발생
        // 4. 조회 성공 시 로그 기록 (제목, 작성자 정보)
        // 5. 조회된 게시글 컨트롤러 단으로 반환

        log.info("게시글 상세 조회 서비스");
        Board boardEntity = boardRepository.findByIdJoinUser(id).orElseThrow(() -> {
            log.warn("게시글 조회 실패 - ID : {}", id);
            return new Exception404("해당하는 게시글을 찾을 수 없습니다.");
        });

        log.info("게시글 조회 완료 - 제목 : {}, 작성자 : {}",
                boardEntity.getTitle(), boardEntity.getUser().getUsername());

        return boardEntity;
    }

    // 게시글 수정
    @Transactional
    public Board updateById(Integer id, BoardRequest.UpdateDTO updateDTO, User sessionUser) {
        // 1. 로그 기록 - 게시글 수정 요청 정보 (board pk,새 제목, 요청자)
        // 2. 수정하고자 하는 게시글 조회 (중간 삭제되는 경우도 있음)
        // 3. 권한 확인 (인가 처리)
        // 4. 권한이 없다면 Exception403 예외 발생
        // 5. 더티 체킹으로 게시글 수정 (JPA 영속성 컨텍스트 활용)
        // 6. 수정 완료 로그 기록
        // 7. 수정된 게시글 반환

        log.info("게시글 수정 서비스");

        Board boardEntity = findById(id);
        boardEntity.isOwner(sessionUser.getId());

        // 영속화 되어 있었던 객체의 title, content 의 내용이 변경됨
        boardEntity.update(updateDTO);

        log.info("게시글 수정 완료 - ID : {}, 새 제목 : {}",
                boardEntity.getId(), boardEntity.getTitle());

        return boardEntity;
    }

    // 게시글 삭제 (권한 체크 포함)
    @Transactional
    public void deleteById(Integer id, User sessionUser) {
        // 1. 로그 기록 - 게시글 삭제 요청 정보 (board PK, 요청자)
        // 2. 삭제하려는 게시글 조회
        // 3. 권한 확인 - 게시글 작성자와 요청자가 동일한지 확인
        // 4. 권한이 없다면 Exception403 예외 발생
        // 5. 데이터베이스에서 게시글 삭제 실행
        // 6. 삭제 완료 로그 기록

        log.info("게시글 삭제 서비스");
        Board boardEntity = findById(id);
        boardEntity.isOwner(sessionUser.getId());

        boardRepository.deleteById(id);

        log.info("게시글 삭제 완료 - ID : {}", id);

    }

    /**
     * 게시글 수정 화면 요청(인가 처리 필요)
     * @param id (Board PK)
     * @param sessionUser (로그인한 사용자 정보)
     * @return Board
     */
    public Board findByIdAndCheckOwner(Integer id, User sessionUser) {

        log.info("게시글 수정 화면 조회 서비스");
        Board boardEntity = findById(id);
        boardEntity.isOwner(sessionUser.getId());

        log.info("게시글 수정 조회 완료 - 제목 : {}, 작성자 : {}",
                boardEntity.getTitle(), boardEntity.getUser().getUsername());
        return boardEntity;
    }

}
