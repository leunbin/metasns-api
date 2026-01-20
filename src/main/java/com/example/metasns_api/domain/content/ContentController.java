package com.example.metasns_api.domain.content;

import com.example.metasns_api.common.exception.AuthException;
import com.example.metasns_api.common.response.ApiResponse;
import com.example.metasns_api.domain.content.dto.ContentFileResponse;
import com.example.metasns_api.domain.content.dto.ContentFileStatusResponse;
import com.example.metasns_api.domain.content.dto.DownloadUrlResponse;
import com.example.metasns_api.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/post/{postId}/content")
    public ResponseEntity<ApiResponse<List<ContentFileResponse>>> getContentsByPostId(@PathVariable Long postId){
        List<ContentFileResponse> responses = contentService.getContentsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/me/contents/uploading")
    public ResponseEntity<ApiResponse<List<ContentFileStatusResponse>>> uploadingContents(HttpServletRequest request){
        User user = (User) request.getAttribute("user");

        if(user == null){
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        Long userId = user.getId();
        List<ContentFileStatusResponse> responses = contentService.getContentsInUploading(userId);

        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping("/post/{postId}/contents")
    public ResponseEntity<Void> upload(
            @PathVariable Long postId,
            @RequestPart("file")MultipartFile file,
            HttpServletRequest request
            ){
        User user = (User) request.getAttribute("user");

        if(user == null){
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        contentService.uploadContent(file, postId, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/content/{contentId}")
    public ResponseEntity<ApiResponse<DownloadUrlResponse>> download(@PathVariable Long contentId){
        DownloadUrlResponse response = contentService.getDownloadUrl(contentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
