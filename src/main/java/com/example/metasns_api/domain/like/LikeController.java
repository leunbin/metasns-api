package com.example.metasns_api.domain.like;

import com.example.metasns_api.common.exception.AuthException;
import com.example.metasns_api.common.response.ApiResponse;
import com.example.metasns_api.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/likes/{postId}")
    public ResponseEntity<ApiResponse<Long>> getLikeCount(@PathVariable Long postId){
        Long count = likeService.getLikeCount(postId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> like(
            @PathVariable Long postId,
            HttpServletRequest request
    ){
        User user = (User) request.getAttribute("user");

        if(user == null){
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        likeService.like(postId, user.getId());

        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> unlike(
            @PathVariable Long postId,
            HttpServletRequest request
    ){
        User user = (User) request.getAttribute("user");

        if(user == null){
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        likeService.unlike(postId, user.getId());

        return ResponseEntity.accepted().build();
    }
}
