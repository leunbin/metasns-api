package com.example.metasns_api.domain.user;

import com.example.metasns_api.domain.user.dto.LoginRequest;
import com.example.metasns_api.domain.user.dto.SignupRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("Integration")
@SpringBootTest //실제 스프링 컨텍스트
@AutoConfigureMockMvc //http 레벨 테스트
@ActiveProfiles("test") //h2 / test 설정
@Transactional //테스트 후 롤백
class UserIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void signup_success() throws Exception{
        SignupRequest request = new SignupRequest("test@test.com", "1234", "test");

        mockMvc.perform(
                post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isCreated());
    }

    @Test
    void signup_and_login_success() throws Exception {
        SignupRequest signup = new SignupRequest("test@test.com", "1234", "test");

        mockMvc.perform(
                post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup))
        ).andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("test@test.com", "1234");

        mockMvc.perform(
                post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }
}
