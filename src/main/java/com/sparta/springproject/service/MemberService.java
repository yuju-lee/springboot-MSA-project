package com.sparta.springproject.service;

import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.model.Member;
import com.sparta.springproject.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member registerUser(MemberDTO memberDTO) {
        if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        Member member = new Member();
        member.setEmail(memberDTO.getEmail());
        member.setUserName(passwordEncoder.encode(memberDTO.getUserName()));
        member.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
        return memberRepository.save(member);
    }

}