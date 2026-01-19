package com.example.metasns_api.domain.post;

import com.example.metasns_api.common.exception.AuthException;
import com.example.metasns_api.common.response.ApiResponse;
import com.example.metasns_api.domain.post.dto.PostCreateRequest;
import com.example.metasns_api.domain.post.dto.PostGetResponse;
import com.example.metasns_api.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostGetResponse>> getPost(@PathVariable Long postId){
        PostGetResponse response = postService.getPostById(postId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(
            HttpServletRequest request,
            @RequestBody PostCreateRequest body
    ){
        User user = (User) request.getAttribute("user");

        if(user == null){
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        Long postId = postService.createPost(body, user.getId());

        return ResponseEntity.ok(ApiResponse.ok(postId));
    }
}
