package com.tenco.blog.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Slf4j
@Controller // IoC
@RequiredArgsConstructor // DI 처리
public class UserController {

    private final UserService userService;

    // 마이페이지 요청 화면

    @GetMapping("/user/detail")
    public String detailPage(Model model, HttpSession session) {

        User sessionUser = (User) session.getAttribute("sessionUser");
        model.addAttribute("user", sessionUser);

        return "user/detail";
    }

    // 프로필 업데이트 기능 요청
    @PostMapping("/user/update")
    public String updateProc(UserRequest.UpdateDTO updateDTO, HttpSession session) {
        updateDTO.validate();
        User sessionUser = (User) session.getAttribute("sessionUser");
        User updateUser = userService.회원정보수정(sessionUser.getId(), updateDTO);
        session.setAttribute("sessionUSer", updateUser);
        return "redirect:/";
    }

    // 프로필 화면 요청
    @GetMapping("/user/update-form")
    public String updateForm(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        User user = userService.회원정보수정화면(sessionUser.getId());
        model.addAttribute("user", user);
        return "user/update-form";
    }

    // 로그인 화면 요청
    // 주소 설계 : http://localhost:80/login-form
    @GetMapping("/login-form")
    public String loginFormPage() {
        // 인증 검사 x, 유효성 검사 x
        return "user/login-form";
    }

    // 로그인 기능 요청
    @PostMapping("/login")
    public String loginProc(UserRequest.LoginDTO reqLoginDTO, HttpSession session) {
        // 인증 검사 x, 유효성 검사 o
        reqLoginDTO.validate();
        User user = userService.로그인(reqLoginDTO);
        session.setAttribute("sessionUser", user);
        return "redirect:/";
    }

    // 로그아웃 기능 요청
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 메모리에 내 정보를 없애버림
        session.invalidate();
        return "redirect:/";
    }


    // 회원가입 화면 요청
    // 주소 설계 : http://localhost:80/join-form
    @GetMapping("/join-form")
    public String joinFormPage() {
        return "user/join-form";
    }

    // 회원가입 기능 요청
    // 주소 설계 : http://localhost:80/join
    @PostMapping("/join")
//    public String joinProc(@RequestParam(name = "username") String username,
//                           @RequestParam(name = "password") String password,
//                           @RequestParam(name = "email") String email) {
    // 메세지 컨버터라는 녀석이 구문을 분석해서 자동으로 파싱 처리 및 매핑해준다.
    // 파싱 전략 1 - key=value 구조 (@RequestParam 사용)
    // 파싱 전략 2 - Object DTO 설계
    public String joinProc(UserRequest.JoinDTO joinDTO){

        // 인증 검사 x, 유효성 검사 o
        joinDTO.validate(); // 유효성 검사 --> 오류 --> 예외 처리 넘어감
        userService.회원가입(joinDTO);
        return "redirect:/login-form";
    }
}
