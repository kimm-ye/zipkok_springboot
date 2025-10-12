package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.NoticeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class NoticeController {


    @PostMapping("/notice/write/action")
    public String writeAction(@RequestBody NoticeDTO boardDTO) {

        System.out.println(boardDTO);

        return null;
    }


}
