package com.sparta.springproject.controller;

import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.dto.UpdatePasswordDTO;
import com.sparta.springproject.dto.UpdateProfileDTO;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController( MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody MemberDTO memberDTO) {
        try {
            // signup으로 들어오는 모든 user는 기본 회원이라는 가정으로 권한 부여
            memberDTO.setRole("ROLE_USER");
            // 회원 정보 저장
            MemberEntity savedMemberEntity = memberService.registerUser(memberDTO);

            // 저장 성공 시 응답
            String welcomeMessage = "Welcome, " + savedMemberEntity.getEmail() + "!";
            return ResponseEntity.ok(welcomeMessage);
        } catch (IllegalArgumentException e) {
            // 예외 발생 시 BadRequest 응답
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestHeader("Authorization") String accessToken, @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        memberService.updatePassword(accessToken, updatePasswordDTO);
        return ResponseEntity.ok("Password updated successfully.");
    }

    @PutMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@RequestHeader("Authorization")String accessToken, @RequestBody UpdateProfileDTO updateProfileDTO) {
        memberService.updateProfile(accessToken, updateProfileDTO);
        return ResponseEntity.ok("Profile updated successfully");
    }

}
