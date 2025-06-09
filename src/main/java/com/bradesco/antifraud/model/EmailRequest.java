package com.bradesco.antifraud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Builder
@Data
public class EmailRequest {
    private String senderAddress;
    private String subject;

}
