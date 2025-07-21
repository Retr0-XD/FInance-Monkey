package com.financeMonkey.service;

import com.financeMonkey.dto.JwtTokenResponseDto;
import com.financeMonkey.dto.LoginRequestDto;
import com.financeMonkey.dto.UserRegistrationDto;
import com.financeMonkey.exception.EmailAlreadyExistsException;
import com.financeMonkey.model.User;
import com.financeMonkey.repository.UserRepository;
import com.financeMonkey.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .email(registrationDto.getEmail())
                .passwordHash(passwordEncoder.encode(registrationDto.getPassword()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .settings(new java.util.HashMap<>())
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public JwtTokenResponseDto login(LoginRequestDto loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), loginRequest.getPassword()));
            
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            String token = jwtTokenProvider.createToken(user.getEmail(), user.getId());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getId());
            
            return JwtTokenResponseDto.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .build();
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email/password combination");
        }
    }

    @Transactional(readOnly = true)
    public JwtTokenResponseDto refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        String username = jwtTokenProvider.getUsername(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        String newToken = jwtTokenProvider.createToken(username, user.getId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(username, user.getId());
        
        return JwtTokenResponseDto.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }
}
