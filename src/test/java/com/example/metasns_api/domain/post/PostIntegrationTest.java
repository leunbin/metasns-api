package com.example.metasns_api.domain.post;

import com.example.metasns_api.domain.auth.TokenProvider;
import com.example.metasns_api.domain.post.dto.PostCreateRequest;
import com.example.metasns_api.domain.user.User;
import com.example.metasns_api.domain.user.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Tag("Integration")
@ActiveProfiles("test")
class PostIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TokenProvider tokenProvider;

    private String createToken(String email){
        User user = User.builder()
                .email(email)
                .encodedPassword("password")
                .build();

        userRepository.save(user);

        return tokenProvider.generatedTokenDTO(user).getAccessToken();
    }

    @Test
    void createPost_success() throws Exception{
        String token = createToken("test@test.com");

        PostCreateRequest request = new PostCreateRequest("title", "content");

        mockMvc.perform(post("/api/v1/posts")
                .header("Authorization", "Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isNumber());

        assertThat(postRepository.findAll()).hasSize(1);
    }

    @Test
    void createPost_fail() throws Exception{
        PostCreateRequest request = new PostCreateRequest("title", "content");

        mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isUnauthorized());

        assertThat(postRepository.findAll()).isEmpty();
    }

    @Test
    void getPost_success() throws Exception{
        Post post = Post.builder()
                .title("title")
                .textContent("content")
                .authorId(1L)
                .build();

        postRepository.save(post);

        mockMvc.perform(get("/api/v1/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value("title"));
    }
}
