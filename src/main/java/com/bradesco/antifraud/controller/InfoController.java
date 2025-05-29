package com.bradesco.antifraud.controller;


import com.bradesco.antifraud.dto.InfoDTO;
import com.bradesco.antifraud.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController

public class InfoController {
    @Autowired
    private final InfoService infoService = new InfoService();


    @GetMapping("/info")
    public InfoDTO getInfo() {
        return infoService.getAllinfo();
    }


}
