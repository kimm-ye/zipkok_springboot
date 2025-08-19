package com.kosmo.zipkok.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // 메인 페이지
    @GetMapping("/")
    public String zipkokMain() {
        System.out.println("123123231231333");
        return "index"; // templates/index.html
    }

    // 마이페이지
    @GetMapping("/mypage")
    public String mypage() {
        return "member/mypage"; // templates/member/mypage.html
    }

    // 심부름 요청 페이지
    @GetMapping("/mission")
    public String missionSelect() {
        return "mission/registration"; // templates/mission/registration.html
    }
}