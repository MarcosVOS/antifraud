package com.bradesco.antifraud.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class SessionService {
    private final Map<String, SessionData> sessionStore = new ConcurrentHashMap<>();

    public void storeSession(String sessionId, UUID userId) {
        sessionStore.put(sessionId, new SessionData(userId, LocalDateTime.now().plusHours(1)));
    }

    public boolean isSessionValid(String sessionId) {
        SessionData data = sessionStore.get(sessionId);
        return data != null && data.expiry.isAfter(LocalDateTime.now());
    }

    public UUID getUserIdBySession(String sessionId) {
        SessionData data = sessionStore.get(sessionId);
        if (data == null || data.expiry.isBefore(LocalDateTime.now())) {
            return null;
        }
        return data.userId;
    }

    private static class SessionData {
        UUID userId;
        LocalDateTime expiry;

        SessionData(UUID userId, LocalDateTime expiry) {
            this.userId = userId;
            this.expiry = expiry;
        }
    }
}