package com.bradesco.antifraud.service;

import com.bradesco.antifraud.dto.InfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class InfoService {

    @Value("${spring.application.name:AntiFraud System}")
    private String appName;

    @Value("${spring.application.version:Unknown}")
    private String version;

    public InfoDTO getAllinfo() {
        String status = "Active";
        return new InfoDTO(appName,version, status);
    }


}