package com.sparta.springproject.controller;

import com.sparta.springproject.dto.MyPageResponseDTO;
import com.sparta.springproject.service.MyPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping("/mypage")
    public ResponseEntity<MyPageResponseDTO> getMyPage(@RequestHeader("Authorization") String accessToken) {
        MyPageResponseDTO myPageData = myPageService.getMyPageData(accessToken);
        return ResponseEntity.ok(myPageData);
    }
}