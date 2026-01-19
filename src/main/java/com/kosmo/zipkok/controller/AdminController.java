package com.kosmo.zipkok.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // 모든 메서드에 ADMIN 권한 필요
public class AdminController {

}
