package com.tenco.blog._core.interceptor;

import com.tenco.blog.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component // IoC 처리
public class SessionInterceptor implements HandlerInterceptor {

    // 컨트롤러 로직이 거의 끝나는 시점. 즉 화면이 그려지기 직전에 SessionUser 값을 주입
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);

        // 1. 화면을(View) 반환하는 요청인지 먼저 확인한다. /** <-- 모든 URL 요청 시 동작
        // 데이터만 (JSON) 반환하는 요청일 경우 modelAndView 가 없으므로 건너뜀
        if (modelAndView != null) {
            // 화면을 반환하는 동작이다.
            // request.getSession(true);
            // request.getSession(false);
            // request.getSession() <-- 기본값은 TRUE
            HttpSession session = request.getSession(false);
            // false 로 설정하는 이유!!
            // -> 만약 A라는 사용자가 우리 서버에 최초 요청일 경우 스프링이 자동으로 세션을 만들어라고
            //    동작하게 됨. 성능때문에 매번 세션 메모리를 생성하지마
            if (session != null) {
                User sessionUser = (User) session.getAttribute("sessionUser");
                if (sessionUser != null) {
                    modelAndView.addObject("sessionUser", sessionUser);
                }
            }


        }
    }
}
