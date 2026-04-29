package com.tenco.blog.user;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

// SRP - 단일 책임의 원칙
@Repository // IoC 대상
@RequiredArgsConstructor
public class UserRepository {

    // DI - 스프링 프레임 워크가 주소값 자동 주입
    private final EntityManager em;

    // 회원 가입 요청 시 -- INSERT
    @Transactional
    public User save(User user) {
        // 매개 변수로 들어온 User Object는 비영속 상태이다

        em.persist(user);

        // 리턴 시 User Object 는 영속화된 상태이다
        return user;
    }

    // 사용자 이름 중복 확인
    public User findByUsername(String username) {
        String jpqlStr = """
                SELECT u FROM User u WHERE u.username = :username
                """;

        try {
            return em.createQuery(jpqlStr, User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    // 로그인 요청 시 -- SELECT
    public User findByUsernameAndPassword(String username, String password) {
        String jpqlStr = """
                SELECT u FROM User u WHERE u.username = :username AND u.password = :password 
                """;


        try {
            return em.createQuery(jpqlStr, User.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        }

    }

}
