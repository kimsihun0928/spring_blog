package com.tenco.blog.model;

import com.tenco.blog.util.MyDateUtil;
import jakarta.persistence.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

@Data // get, set, toString... 자동
// @Entity - JPA 가 이 클래스를 데이터베이스 테이블과 매핑하는 객체로 인식하게 설정
// 즉, 이 어노테이션이 있어야 JPA 가 관리함
@Entity
@Table(name = "board_tb")
public class Board {

    // @id : 이 필드가 기본키임을 나타냄
    @Id
    // IDENTITY 전략 : 데이터베이스에 기본 AUTO_INCREMENT 기능 사용
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String title;
    private String content;
    private Timestamp createdAt;

    // createdAt -> 포맷하는 메서드 만들어보기
    public String getTime() {
        return MyDateUtil.timestampFormat(createdAt);
    }

}
