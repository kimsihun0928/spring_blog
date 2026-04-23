package com.tenco.blog.repository;

import com.tenco.blog.model.Board;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // IoC + DI
@RequiredArgsConstructor
public class BoardNativeRepository {

    // EntityManager: JPA 핵심 인터페이스
    // 데이터베이스와 모든 작업을 담당
    private final EntityManager em;

    // 트랜잭션 처리
    @Transactional
    public void save(String title, String content, String username) {
        Query query = em.createNativeQuery("insert " +
                "into board_tb(title, content, username, created_at) values (?, ?, ?, now())");

        query.setParameter(1, title);
        query.setParameter(2, content);
        query.setParameter(3, username);

        query.executeUpdate();
    }

    // 게시글 목록 조회 메서드
    public List<Board> findAll() {
        String sql = """
                select * from board_tb order by id desc
                """;
        // while(rs.next) { Board board = new Board(); board.setTitle(rs.getString("title"))
        Query query = em.createNativeQuery(sql, Board.class);
        // while(rs.next()) {}
        return query.getResultList();
    }
}
