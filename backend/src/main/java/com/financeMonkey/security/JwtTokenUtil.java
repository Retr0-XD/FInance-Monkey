package com.financeMonkey.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class for JWT token operations
 * Used by controllers to extract user information from tokens
 */
@Component
public class JwtTokenUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    public JwtTokenUtil(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * Extracts the user ID from a JWT token
     * 
     * @param token JWT token (with or without 'Bearer ' prefix)
     * @return User ID as string
     */
    public String extractUserId(String token) {
        if (token == null) {
            return null;
        }
        
        // Remove Bearer prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            return jwtTokenProvider.getUserId(token).toString();
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Failed to extract user ID from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extracts the username from a JWT token
     * 
     * @param token JWT token (with or without 'Bearer ' prefix)
     * @return Username
     */
    public String extractUsername(String token) {
        if (token == null) {
            return null;
        }
        
        // Remove Bearer prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            return jwtTokenProvider.getUsername(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Validates a JWT token
     * 
     * @param token JWT token (with or without 'Bearer ' prefix)
     * @return True if valid, false otherwise
     */
    public boolean validateToken(String token) {
        if (token == null) {
            return false;
        }
        
        // Remove Bearer prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return jwtTokenProvider.validateToken(token);
    }
}
