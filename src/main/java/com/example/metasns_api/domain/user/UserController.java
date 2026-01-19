package com.example.metasns_api.domain.user;

import com.example.metasns_api.common.response.ApiResponse;
import com.example.metasns_api.domain.auth.TokenDto;
import com.example.metasns_api.domain.user.dto.LoginRequest;
import com.example.metasns_api.domain.user.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest request){
        userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> login(@RequestBody LoginRequest request){
        TokenDto token = userService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(token));
    }
}
