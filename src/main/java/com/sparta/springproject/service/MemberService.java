package com.sparta.springproject.service;

import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<MemberEntity> findByEmail(String email) {
        return memberRepository.findByEmail(email);
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

        if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in used.  Please choose another one.");
        }

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setEmail(memberDTO.getEmail());
        memberEntity.setUserName(passwordEncoder.encode(memberDTO.getUserName()));
        memberEntity.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
        memberEntity.setRole("ROLE_USER");

        try {
            return memberRepository.save(memberEntity);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error. Please try again.");
        }
    }
}
