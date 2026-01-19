package com.example.metasns_api.domain.user;

import com.example.metasns_api.domain.auth.TokenDto;
import com.example.metasns_api.domain.user.dto.LoginRequest;
import com.example.metasns_api.domain.user.dto.SignupRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Tag("unit")
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void signup_success() throws Exception{
        SignupRequest request = new SignupRequest("test@test.com","1234","test");

        mockMvc.perform(
                post("/api/v1/users/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isCreated());
    }

    @Test
    void login_success() throws Exception{
        LoginRequest login = new LoginRequest("test@test.com", "1234");

        given(userService.login(any()))
                .willReturn(TokenDto.builder()
                        .accessToken("access")
                        .refreshToken("refresh")
                        .build());

        mockMvc.perform(
                post("/api/v1/users/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(login))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
    }
}
