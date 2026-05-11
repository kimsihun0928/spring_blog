package com.tenco.blog.board;

import com.tenco.blog._core.errors.Exception403;
import com.tenco.blog._core.errors.Exception404;
import com.tenco.blog.reply.ReplyRepository;
import com.tenco.blog.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 서비스 레이어
 * 핵심 개념 :
 * 1. 서비스 레이어의 역할:
 * - 비즈니스 로직을 처리하는 계층
 * - Controller 와 Response 사이에서 중간 계층을 담당
 * - 트랜잭션 관리
 * - 여러 Repository 를 조합해서 복잡한 비즈니스 로직 처리
 * <p>
 * 2. 계층 구조 (3Tier 아키텍처)
 * Controller -> Service -> Repository -> DB
 * <p>
 * 3. @Service 어노테이션 사용
 * - Spring에서 이 어노테이션을 확인해서 Bean(빈) 으로 등록한다.
 */

@Slf4j
// 서비스 계층은 @Service 어노테이션으로 IoC 처리
@Service // IoC 처리
@RequiredArgsConstructor // DI 처리
@Transactional(readOnly = true)
// 모든 메서드를 읽기 전용 트랜잭션으로 실행(findAll, findById 등 조회에 적합)
// 성능 최적화 (변경 감지 비활성화 됨), 즉 조회 시 데이터 수정 방지
public class BoardService {

    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;

    /**
     * 게시글 목록 조회
     * <p>
     * OSIV false 환경 대응 - 응답 DTO 설계
     *
     */
    public List<BoardResponse.ListDTO> 게시글목록() {
        // 1. 로그 기록 - 게시글 목록 조회
        // 2. 데이터베이스 접근해서 모든 게시글 목록을 조회
        // 3. 로그 기록 - (총 게시글 수)
        // 4. 조회된 게시글 목록을 컨트롤러로 반환

        log.info("게시글 목록 조회 서비스");
        List<Board> boardList = boardRepository.findAllJoinUser();
        log.info("게시글 목록 조회 완료 - 총 : {}", boardList.size());

        // 방법 1 - 메서드 참조 사용
        return boardList.stream()
                .map(BoardResponse.ListDTO::new)
                .collect(Collectors.toList());

        // 방법 2 - 람다 표현식 사용
        // stream().map() --> 기존 LIST 데이터를 변형(가공)해서 새로운 객체를 만들 때 사용하는 녀석
//        boardList.stream().map(board ->
//                        new BoardResponse.ListDTO(board))
//                .collect(Collectors.toList());

        // 방법 3 - for 문 사용 (전통적인 방법)
//        for (Board board : boardList) {
//            BoardResponse.ListDTO dto = new BoardResponse.ListDTO(board);
//            dtoList.add(dto);
//        }
//
//        return boardList;

    }

    /**
     * 게시글 상세 조회
     *
     * @param id (Board PK)
     * @return DeTailDTO 처리 (OSIV 대응)
     */
    public BoardResponse.DetailDTO 게시글상세조회(Integer id) {
        // 1. 로그 기록 - 게시글 상세 조회 (id)
        // 2. 데이터베이스 접근해서 해당 ID의 게시글 조회 (작성자 정보 포함)
        // 3. 게시글이 존재하지 않으면 Exception404 로 예외 발생
        // 4. 조회 성공 시 로그 기록 (제목, 작성자 정보)
        // 5. 조회된 게시글 컨트롤러 단으로 반환

        log.info("게시글 상세 조회 서비스");
        // N + 1 문제를 해결하기 위해 한 번에 Board, User 가지고옴
        Board boardEntity = boardRepository.findByIdJoinUser(id).orElseThrow(() -> {
            log.warn("게시글 조회 실패 - ID : {}", id);
            return new Exception404("해당하는 게시글을 찾을 수 없습니다.");
        });

        log.info("게시글 조회 완료 - 제목 : {}, 작성자 : {}",
                boardEntity.getTitle(), boardEntity.getUser().getUsername());
        return new BoardResponse.DetailDTO(boardEntity);
    }

    /**
     * 게시글 작성
     *
     * @param saveDTO
     * @param sessionUser (세션에서 가져온 사용자 정보)
     */
    @Transactional
    public void 게시글작성(BoardRequest.SaveDTO saveDTO, User sessionUser) {
        // . 로그 기록 - 게시글 저장 요청 정보
        // 2. DTo 를 Entity 로 변환(작성자 정보 포함)
        // 3. 데이터베이스에 게시글 저장
        // 4. 저장 완료, 로그 기록

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
    }

    /**
     * 게시글 상세 화면 요청(인가 처리 필요)
     *
     * @param id          (Board PK)
     * @param sessionUser (로그인한 사용자 정보)
     * @return BoardResponse.DetailDTO
     */
    public BoardResponse.DetailDTO 게시글상세화면및인가처리(Integer id, User sessionUser) {

        log.info("게시글 상세 화면 및 인가 확인");
        BoardResponse.DetailDTO detailDTO = 게시글상세조회(id);
        if (!detailDTO.getUserId().equals(sessionUser.getId())) {
            throw new Exception403("권한없음");
        }

        log.info("게시글 수정 조회 완료 - 제목 : {}, 작성자 : {}",
                detailDTO.getTitle(), detailDTO.getUsername());
        return detailDTO;
    }

    /**
     * 게시글 수정 기능 처리
     *
     * @param id          (Board PK)
     * @param updateDTO
     * @param sessionUser
     * @return
     */
    @Transactional
    public void 게시글수정(Integer id, BoardRequest.UpdateDTO updateDTO, User sessionUser) {
        // 1. 로그 기록 - 게시글 수정 요청 정보 (board pk,새 제목, 요청자)
        // 2. 수정하고자 하는 게시글 조회 (중간 삭제되는 경우도 있음)
        // 3. 권한 확인 (인가 처리)
        // 4. 권한이 없다면 Exception403 예외 발생
        // 5. 더티 체킹으로 게시글 수정 (JPA 영속성 컨텍스트 활용)
        // 6. 수정 완료 로그 기록
        // 7. 수정된 게시글 반환

        log.info("게시글 수정 서비스");
        Board boardEntity = boardRepository.findByIdJoinUser(id).orElseThrow(() -> {
            throw new Exception404("해당 게시글을 찾을 수 없습니다.");
        });

        // 영속화 되어 있었던 객체의 title, content 의 내용이 변경됨
        boardEntity.update(updateDTO);

        log.info("게시글 수정 완료 - ID : {}, 새 제목 : {}",
                boardEntity.getId(), boardEntity.getTitle());

    }

    /**
     * 게시글 삭제 요청
     *
     * @param id          (Board PK)
     * @param sessionUser
     */
    @Transactional
    public void 게시글삭제(Integer id, User sessionUser) {
        // 1. 로그 기록 - 게시글 삭제 요청 정보 (board PK, 요청자)
        // 2. 삭제하려는 게시글 조회
        // 3. 권한 확인 - 게시글 작성자와 요청자가 동일한지 확인
        // 4. 권한이 없다면 Exception403 예외 발생
        // 5. 데이터베이스에서 게시글 삭제 실행
        // 6. 삭제 완료 로그 기록

        log.info("게시글 삭제 서비스");
        log.info("게시글 삭제 서비스");
        Board boardEntity = boardRepository.findById(id).orElseThrow(
                () -> new Exception404("게시글을 찾을 수 없습니다."));

        boardEntity.isOwner(sessionUser.getId());

        // 기존에 작성된 댓글부터 전체 삭제
        // 게시글 삭제 요청 시 해당 게시글에 관련된 댓글 삭제는 어떻게 만들수잇ㅇ<ㅁ?
        replyRepository.deleteByBoardId(boardEntity.getId());

        boardRepository.deleteById(id);
        log.info("게시글 삭제 완료 - ID : {}", id);

    }


}
