package com.example.metasns_api.domain.user;

import com.example.metasns_api.common.exception.UserException;
import com.example.metasns_api.domain.auth.TokenDto;
import com.example.metasns_api.domain.auth.TokenProvider;
import com.example.metasns_api.domain.user.dto.LoginRequest;
import com.example.metasns_api.domain.user.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public void signup(SignupRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserException(
                    HttpStatus.BAD_REQUEST,
                    "이미 존재하는 이메일입니다."
            );
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .encodedPassword(encodedPassword)
                .build();

        userRepository.save(user);
    }

    public TokenDto login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserException(
                                HttpStatus.UNAUTHORIZED,
                                "이메일 또는 비밀번호가 올바르지 않습니다."
                        )
                );

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new UserException(
                    HttpStatus.UNAUTHORIZED,
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        return tokenProvider.generatedTokenDTO(user);
    }
}
