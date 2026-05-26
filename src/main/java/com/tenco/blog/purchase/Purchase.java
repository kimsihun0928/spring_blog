package com.tenco.blog.purchase;

import com.tenco.blog.board.Board;
import com.tenco.blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * 구매내역 엔티티
 *
 * User 와 Board 의 구매이력 관계를 표현함
 *
 * 한 사람의 사용자는 여러 게시글을 구매할 수 있다.
 * 한 게시글은 여러 사용자에게 판매 될 수 있다.
 * User : Board -- 다대다 관계로 표현됨 - 중간 테이블 (Purchase) 가 생성 되어야함
 * Purchase : User --> @ManyToOne --> join column 이름 지정
 * Purchase : Board --> @ManyToOne --> join column 이름 지정
 * 복합키 설정 방법 확인
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "purchase_tb",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_user_board", columnNames = {"user_id", "board_id"})
        })
public class Purchase {
    // 복합키 .. DB 물리적 구조상 유니크 설정이 필요
    // id 추가
    // 누가 구매를 했는지 정보 저장
    // 어떤 게시글을 구매 했는지 정보 저장
    // 게시글 구매 금액 (500 포인트 고정 예정) 지불한 포인트 이력 관리
    // 언제 구매했는지 시간
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 단방향 관계 " Purchase -> User"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 단방향 관계 " Purchase -> Board"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    // 구매시 지불한 포인트
    private Integer price;

    @CreationTimestamp // pc 에서 db 자동 주입
    private Timestamp createdAt;

    @Builder
    public Purchase(User user, Board board, Integer price) {
        this.user = user;
        this.board = board;
        this.price = price;
    }
}
