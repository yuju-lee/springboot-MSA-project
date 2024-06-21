package com.sparta.springproject.controller;

import com.sparta.springproject.dto.LoginRequestDTO;
import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.model.Member;
import com.sparta.springproject.service.AuthService;
import com.sparta.springproject.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    private AuthService authService;
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody MemberDTO memberDTO) {
        try {
            Member member = memberService.registerUser(memberDTO);
            String welcomeMessage = "Welcome, " + memberDTO.getUserName() + "!";
            return ResponseEntity.ok(welcomeMessage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDTO requestDto, HttpServletResponse res) {
        authService.login(requestDto, res);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        return authService.logout(token.substring(7)); // "Bearer " 부분 제거
    }
}