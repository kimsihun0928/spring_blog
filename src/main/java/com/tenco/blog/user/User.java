package com.tenco.blog.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Table(name = "user_tb")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // 사용자명 중복 방지를 위한 유니크 제약 조건 설정
    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email;
    // 엔티티가 영속화 될 때 자동으로 현재 시간을 주입해라 pc --> db
    @CreationTimestamp
    private Timestamp createdAt;

    // User 테이블에는 이미지 파일명만 저장할 예정 (실제 데이터는 내 서버 컴퓨터 로컬에 저장할 예정)
    @Column(nullable = true) // null 허용, 기본값
    private String profileImage; // 프로필 이미지는 선택사항 (회원가입 시)

    // User : UserRole 연관 관계를 단방향 1 : N 구조 설계
    // JPA 에서 1 : N 구조일 경우 (User, UserRole), @JoinColumn(name="user_id") 의미는
    // 여기 테이블에 컬럼 user_id 생성해 라는 의미이다. 그런데 1 : N 구조에서 FK컬럼이
    // 1 쪽 테이블에 생성되는 경우는 없음, 무조건 N 쪽에 FK 컬럼이 만들어져야하기 때문에
    // 자동으로 User 테이블에 @JoinColumn 명시하더라도 알아서 UserRole 컬럼을 자기가 생성함

    /**
     * 사용자 권한 목록
     * User (1) : UserRole (N) 연관관계 정의함
     * <p>
     * 1. @OneToMany + @JoinColumn(name = "user_id_")
     * - User 가 UserRole 리스트를 관리한다. (단방향)
     * - 실제 DB user_role_tb 테이블에 FK 컬럼은 user_id 명이 user_role_tb 생성된다.
     * <p>
     * 2. CascadeType.ALL (운명 공동체)
     * Java 기준에서 User 저장하면 Role 도 자동 저장되고, User 삭제하면 가지고 있던
     * Role 도 다 삭제가됨.  DB 에서 실제 delete 쿼리가 발생됨
     * <p>
     * 3. orphanRemoval (리스트와 DB를 동기화)
     * DB 에서 실제 delete 쿼리가 발생됩니다. = true 처리
     * <p>
     * 4. fetch = FetchType.EAGER (특별 취급)
     * 데이터 양이 얼마 되지 않습니다. 그래서 한번에 데이터를 채워서 가지고 오는 것이
     * 편리하다.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private List<UserRole> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // null 허용 안함
    @ColumnDefault("'LOCAL'") // 어노테이션으로 디폴트값 선언 방법 ( 문자열 일 경우 ' ' 반드시 사용)
    private OAuthProvider oAuthProvider;

    @Builder
    public User(Integer id, String username, String password,
                String email, Timestamp createdAt,
                String profileImage, OAuthProvider oAuthProvider) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
        this.profileImage = profileImage;

        this.oAuthProvider = (oAuthProvider != null) ? oAuthProvider : OAuthProvider.LOCAL;
    }

    // 편의 기능 추가 - 회원 정보 수정
    public void update(UserRequest.UpdateDTO updateDTO) {
        if (updateDTO.getPassword() != null) {
            this.password = updateDTO.getPassword();
        }
        if (updateDTO.getProfileImageName() != null) {
            this.profileImage = updateDTO.getProfileImageName();
        }
    }


    // User 엔티티의 권한 관련 편의 기능 만들어 보기
    // Role.ADMIN, Role.USER
    public void addRole(Role role) {

        this.roles.add(UserRole
                .builder()
                .role(role)
                .build());
    }

    // 해당 Role 을 가지고 있는 여부 확인
    // boolean isAdmin = user.hasRole(Role.ADMIN);
    public boolean hasRole(Role role) {
        // 1. 방어적 코드 작성
        if (this.roles == null || this.roles.isEmpty()) {
            // Role 자체가 설정되지 않은 상태
            return false;
        }

        for (UserRole userRole : this.roles) {
            if (userRole.getRole() == role) {
                return true;
            }
        }
        return false;
    }

    // 관리자 여부 확인 편의 메서드 - 머스태치에서 is 생략하고 admin 으로 접근 가능함
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    // 머스태치 화면에서 사용할 편의 메서드
    public String getRoleDisplay() {
        return isAdmin() ? "ADMIN" : "USER";
    }

    // 머스태치 화면엥서 사용할 편의 메서드2
    public String getProfilePath() {
        if (this.profileImage == null) {
            return null;
        }
        // 이미지 경로가 http 로 시작 (소셜 가입)
        if (this.profileImage.startsWith("http")) {
            return this.profileImage;
        }
        // 로컬 이미지
        return "/images/" + this.profileImage;
    }

    // 머스태치 화면에서 사용할 편의 메서드 3
    public boolean isLocal() {
        // true -> 이메일 가입자를 의미 함
        return this.oAuthProvider == OAuthProvider.LOCAL;
    }
}

