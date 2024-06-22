package com.sparta.springproject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String message;

    // 생성자, getter/setter 메서드 등 필요한 코드 추가

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }


    public void setMessage(String message) {
        this.message = message;
    }
}
