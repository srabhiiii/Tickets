package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.repository.AppUserRepository;
import com.dbtraining.reconx.repository.entity.AppUser;
import com.dbtraining.reconx.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserRepository usersRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("ADV072 / ADV076: POST /auth/login with valid credentials returns 200 OK and JWT token")
    void login_validCredentials_returns200OKAndJwt() throws Exception {
        AppUser user = new AppUser();
        when(usersRepository.findByEmail(eq("trader@db.com"))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("trader123"), any())).thenReturn(true);
        when(jwtTokenProvider.generate(any(), any())).thenReturn("mock.jwt.token");
        when(jwtTokenProvider.expirationSeconds()).thenReturn(3600L);

        String jsonPayload = """
                {
                    "email": "trader@db.com",
                    "password": "trader123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600));
    }

    @Test
    @DisplayName("ADV072 / ADV076: POST /auth/login with invalid credentials returns 400 Bad Request")
    void login_invalidCredentials_returns400BadRequest() throws Exception {
        when(usersRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        String jsonPayload = """
                {
                    "email": "unknown@db.com",
                    "password": "wrongpassword"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid credentials"));
    }
}
