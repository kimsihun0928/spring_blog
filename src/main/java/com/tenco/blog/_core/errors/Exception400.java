package com.tenco.blog._core.errors;

// 400 Bad Request
public class Exception400 extends RuntimeException {

    // 예외 메시지를 외부에서 받아서 내 부모클래스 RuntimeException 에서 생성자로 전달
    public Exception400(String msg) {
        super(msg);
    }


    // throw new Exceiption400 ("잘못된 요청); 사용 예시
}
