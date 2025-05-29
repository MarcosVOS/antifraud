package com.bradesco.antifraud;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger {

    private static final Logger logger = LoggerFactory.getLogger(StartupLogger.class);

    @PostConstruct
    public void logStartup() {
        logger.info("Servidor Spring Boot iniciado com sucesso!");
        logger.debug("Este é um log de DEBUG (não aparece se você não ativar no application.properties)");
    }
}
