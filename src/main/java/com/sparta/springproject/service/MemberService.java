package com.sparta.springproject.service;

import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.dto.UpdatePasswordDTO;
import com.sparta.springproject.dto.UpdateProfileDTO;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.repository.JpaMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {

    private final JpaMemberRepository jpaMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    public MemberService(JpaMemberRepository jpaMemberRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.jpaMemberRepository = jpaMemberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<MemberEntity> findByEmail(String email) {
        return jpaMemberRepository.findByEmail(email);
    }

    public MemberEntity registerUser(MemberDTO memberDTO) {
        if (memberDTO.getEmail() == null || memberDTO.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        if (memberDTO.getUserName() == null || memberDTO.getUserName().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }

        if (memberDTO.getPassword() == null || memberDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if (jpaMemberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use. Please choose another one.");
        }

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setEmail(memberDTO.getEmail());
        memberEntity.setUserName(passwordEncoder.encode(memberDTO.getUserName()));
        memberEntity.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
        memberEntity.setRole("ROLE_USER");

        try {
            return jpaMemberRepository.save(memberEntity);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error. Please try again.");
        }
    }

    public void updatePassword(String accessToken, UpdatePasswordDTO updatePasswordDTO) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        String email = jwtUtil.getUserInfoFromToken(token).getSubject(); // 토큰에서 이메일 추출
        log.info("update password for email: {}", email);

        MemberEntity memberEntity = jpaMemberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );

        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), memberEntity.getPassword())) {
            throw new IllegalArgumentException("Invalid current password.");
        }

        memberEntity.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        jpaMemberRepository.save(memberEntity);
    }

    public void updateProfile(String accessToken, UpdateProfileDTO updateProfileDTO) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        String email = jwtUtil.getUserInfoFromToken(token).getSubject();
        log.info("update profile for email: {}", email);

        MemberEntity memberEntity = jpaMemberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found!")
        );
        if (updateProfileDTO.getAddress() != null) {
            memberEntity.setAddress(passwordEncoder.encode(updateProfileDTO.getAddress()));
        }
        if (updateProfileDTO.getMobileNo() != null) {
            memberEntity.setPhone(passwordEncoder.encode(updateProfileDTO.getMobileNo()));
        }
        jpaMemberRepository.save(memberEntity);
    }
}