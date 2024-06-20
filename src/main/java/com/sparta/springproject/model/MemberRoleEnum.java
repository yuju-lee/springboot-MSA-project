package com.sparta.springproject.model;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
public enum MemberRoleEnum {
    USER(Authority.USER),  // 사용자 권한
    ADMIN(Authority.ADMIN);  // 관리자 권한

    private final String authority;

    MemberRoleEnum(String authority) {
        this.authority = authority;
    }

    @Getter
    @Column(name = "role")
    private String role;

    public static class Authority {
        public static final String USER = "ROLE_USER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}