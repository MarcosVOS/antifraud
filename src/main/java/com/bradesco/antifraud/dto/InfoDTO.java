package com.bradesco.antifraud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class InfoDTO {
    private String aplicationName;
    private String aplicationVersion;
    private String aplicationStatus;

}
