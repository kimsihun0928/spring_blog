package com.tenco.blog.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// @Repository = 부모의 클래스에 정의되어있음
public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    // 기본적인 CRUD 자동 완성 및 추가 편의 기능 자동 생성


    //select r.*, b.*, u.*
    //from reply_tb r
    //inner join board_tb b on r.board_id = b.id
    //inner join user_tb u on r.user_id = u.id
    //where r.board_id = 1
    //order by r.created_at asc;
        // JPQL 문법으로 변환
        // 게시글 ID로 댓글 목록 조회(한번에 댓글 작성자 정보 포함 - JOIN FETCH 사용)
    @Query("""
    SELECT r FROM Reply r JOIN FETCH r.user JOIN FETCH r.board WHERE r.board.id = :boardId ORDER BY r.createdAt
""")
    List<Reply> findByBoardIdWithUser(@Param("boardId") Integer boardId);


    /**
     * 이전 수정, 삭제 기능에서 수정은 더티 체킹으로 처리하였고,
     * 삭제는 기본적으로 제공하는 em.remove() 메서드를 사용해서 처리했었다.
     * 지금은 직접 JPQL 쿼리를 선언해서 DELETE 처리하는 구문이라 다른 상황이다.
     * @Query(....) <- JPA가 기본적으로 SELEECT 쿼리로만 인식하기때문에
     * INSERT, UPDATE, DELETE 는 JPA 에게 SELECT 쿼리가 아니야 라고 알려줘야 제대로 동작
     * 그 어노테이션이 @Modifying 이다!! 반드시 기억!
     */
    @Modifying
    @Query("DELETE FROM Reply r WHERE r.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Integer boardId);
}
