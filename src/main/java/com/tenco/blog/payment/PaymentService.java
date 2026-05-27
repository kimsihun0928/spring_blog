package com.tenco.blog.payment;

import com.tenco.blog._core.errors.Exception404;
import com.tenco.blog.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${portone.store-id}")
    private String storeId;

    @Value("${portone.channel-key}")
    private String channelKey;

    @Value("${portone.api-secret}")
    private String apiSecret;

    /**
     *
     * 결제 사전 요청 생성
     * 프론트엔드가 결제창을 띄우기 전에, 서버로 부터 고유한 결제 건의 식별자(paymentId)를
     * 서버측에서 생성해서 발급시켜준다.
     *
     * - 중복 결제 방지 (paymentId 유니크 설정)
     * - 위변조 방지 (paymentId 를 서버측에서 생성해서 내려줌)
     */


    public PaymentResponse.PrepareDTO 결제요청생성(Integer userId, Integer amount) {

        // 1. 사용자 존재 여부 확인
        if(!userRepository.existsById(userId)) {
            throw new Exception404("사용자를 찾을 수 없습니다.");
        }

        // 2.1. paymentId 생성
        String paymentId = generatePaymentId(userId);

        // 2.2. 중복 방지 확인
        // 2.3 - 만약 중복이 발생 했다면?? 다시 주문번호 생성 --> 다시 확인
        while(paymentRepository.existsByPaymentId(paymentId)) {
            paymentId = generatePaymentId(userId);
        }

        return new PaymentResponse.PrepareDTO(paymentId, amount, storeId, channelKey);

    }

    private String generatePaymentId(Integer userId) {
        return "point_" + userId + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0,8);
    }
}
