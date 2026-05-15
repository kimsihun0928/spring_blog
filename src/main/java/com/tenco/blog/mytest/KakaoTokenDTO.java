package com.tenco.blog.mytest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KakaoTokenDTO {
    // 카카오는 JSON 키값을 스네이크 케이스(access_token)로 줍니다.
    // 자바의 카멜 케이스(accessToken) 변수에 담기 위해 @JsonProperty로 이름표를 맞춰줍니다.
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("refresh_token_expires_in")
    private Integer refreshTokenExpiresIn;
}
