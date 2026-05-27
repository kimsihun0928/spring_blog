package com.tenco.blog.payment;

import com.tenco.blog._core.util.Define;
import com.tenco.blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    // /api/payment/prepare
    @PostMapping("/api/payment/prepare")
    public ResponseEntity<?> preparePayment(@RequestBody PaymentRequest.PrepareDTO reqDTO,
                                            HttpSession session) {
        // 1. 인증 검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        if (sessionUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        // 2. 유효성 검사
        reqDTO.validate();

        PaymentResponse.PrepareDTO prepareDTO = paymentService.결제요청생성(sessionUser.getId(), reqDTO.getAmount());

        return ResponseEntity.ok().body(
                Map.of("paymentId", prepareDTO.getPaymentId(),
                        "amount", prepareDTO.getAmount(),
                        "storeId", prepareDTO.getStoreId(),
                        "channelKey", prepareDTO.getChannelKey()));
    }


}
