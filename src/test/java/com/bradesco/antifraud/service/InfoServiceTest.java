package com.bradesco.antifraud.service;

    import com.bradesco.antifraud.dto.InfoDTO;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.test.util.ReflectionTestUtils;

    import static org.junit.jupiter.api.Assertions.*;

    public class InfoServiceTest {

        private InfoService infoService;

        @BeforeEach
        void setUp() {
            infoService = new InfoService();
        }


        @Test
        void getAllinfoTest() {

            String appName = "AntiFraud System";
            String version = "1.0.0";

            ReflectionTestUtils.setField(infoService, "appName", appName);
            ReflectionTestUtils.setField(infoService, "version", version);

            // Act
            InfoDTO result = infoService.getAllinfo();

            // Assert
            assertNotNull(result);
            assertEquals(appName, result.getAplicationName(), "O nome da aplicação");
            assertEquals(version, result.getAplicationVersion(), "versao atual");
            assertEquals("Active", result.getAplicationStatus(), "Status:Active'.");
        }
    }

