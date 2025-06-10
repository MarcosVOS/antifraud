package com.bradesco.antifraud.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final Map<String, TokenData> tokenStore = new ConcurrentHashMap<>();

    public void storeToken(String token, UUID userId, Duration ttl) {
        tokenStore.put(token, new TokenData(userId, LocalDateTime.now().plus(ttl)));
    }

    public UUID validateAndConsumeToken(UUID token) {
        TokenData data = tokenStore.get(token.toString());
        if (data == null || data.expiry.isBefore(LocalDateTime.now())) {
            return null;
        }
        tokenStore.remove(token.toString());
        return data.userId;
    }

    private static class TokenData {
        UUID userId;
        LocalDateTime expiry;

        TokenData(UUID userId, LocalDateTime expiry) {
            this.userId = userId;
            this.expiry = expiry;
        }
    }
}

