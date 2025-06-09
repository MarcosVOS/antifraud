package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.dto.InfoDTO;
import com.bradesco.antifraud.service.InfoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InfoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class InfoControllerTest {


    @Autowired
    @Mock
    private MockMvc mockMvc;

    @MockitoBean
    private InfoService infoService;

    @Test
    void getInfo_shouldReturnInfoDTO() throws Exception {

        String appName = "AntiFraud System";
        String appVersion = "1.0.0";
        String appStatus = "Active";
        InfoDTO mockInfoDTO = new InfoDTO(appName, appVersion, appStatus);

        when(infoService.getAllinfo()).thenReturn(mockInfoDTO);


        mockMvc.perform(get("/info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.aplicationName").value(appName))
                .andExpect(jsonPath("$.aplicationVersion").value(appVersion))
                .andExpect(jsonPath("$.aplicationStatus").value(appStatus));
    }
}