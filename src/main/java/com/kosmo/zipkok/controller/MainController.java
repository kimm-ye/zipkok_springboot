package com.kosmo.zipkok.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // 메인 페이지
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 마이페이지
    @GetMapping("/mypage")
    public String mypage() {
        return "member/mypage";
    }

    // 심부름 요청 페이지
    @GetMapping("/mission")
    public String missionSelect() {
        return "mission/registration";
    }
}