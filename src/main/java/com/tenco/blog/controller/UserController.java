package com.tenco.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    // 회원 가입 페이지 요청
    // 주소 설계 : http://localhost:80/join-form
    @GetMapping("/join-form")
    public String joinForm() {

        // 뷰 리졸브 동작
        // classPath: src/main/resources/templates/
        return "user/join-form";
    }

}
