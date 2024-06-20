package com.sparta.springproject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String email;
    private String userName;
    private String password;
    private String phone;
    private String address;
}