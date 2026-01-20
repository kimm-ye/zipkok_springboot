package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // 모든 메서드에 ADMIN 권한 필요
public class AdminController {

    private final AdminService adminService;

    /**
    * 관리자 대시보드
     */
    @GetMapping({""})
    public ModelAndView dashboard() {
        ModelAndView mv = new ModelAndView("admin/dashboard");


        return mv;
    }
}
