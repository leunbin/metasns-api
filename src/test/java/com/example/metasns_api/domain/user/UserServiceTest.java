package com.example.metasns_api.domain.user;

import com.example.metasns_api.common.exception.UserException;
import com.example.metasns_api.domain.auth.TokenDto;
import com.example.metasns_api.domain.auth.TokenProvider;
import com.example.metasns_api.domain.user.dto.LoginRequest;
import com.example.metasns_api.domain.user.dto.SignupRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    TokenProvider tokenProvider;

    @InjectMocks
    UserService userService;

    @Test
    void signup_success(){
        SignupRequest request = new SignupRequest("test@test.com", "1234", "testname");

        //userRepository가 이렇게 동작한다고 가정하겠다
        //userRepository에게 existsByEmail("test@test.com") 이 호출되면 false를 반환한다고 가정한다
        given(userRepository.existsByEmail("test@test.com"))
                .willReturn(false);

        given(passwordEncoder.encode("1234"))
                .willReturn("encoded");

        userService.signup(request);

        //메서드가 호출이 되었는지를 확인
        verify(userRepository).save(any(User.class));
    }

    @Test
    void singup_fail_duplicate_email(){
        SignupRequest request = new SignupRequest("test@test.com", "1234", "test");

        given(userRepository.existsByEmail("test@test.com"))
                .willReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(UserException.class)
                .hasMessage("이미 존재하는 이메일입니다.");

        //이 메서드는 호출되어서는 안된다.
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success(){
        LoginRequest request = new LoginRequest("test@test.com", "1234");

        User user = User.builder()
                .email("test@test.com")
                .encodedPassword("encoded")
                .build();

        given(userRepository.findByEmail("test@test.com"))
                .willReturn(Optional.of(user));

        given(passwordEncoder.matches("1234", "encoded"))
                .willReturn(true);

        TokenDto tokenDto = TokenDto.builder()
                .accessToken("access-token")
                .build();

        given(tokenProvider.generatedTokenDTO(user))
                .willReturn(tokenDto);

        TokenDto result = userService.login(request);

        //실행결과가 내가 기대한 것과 같은지를 확인
        assertThat(result.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void login_fail_email_not_found(){
        LoginRequest request = new LoginRequest("test@test.com", "1234");

        given(userRepository.findByEmail("test@test.com"))
                .willReturn(Optional.empty());

        //이 코드를 실행했을 때 특정 예외가 특정 메시지로 발생한다.
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UserException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_fail_password_mismatch(){
        LoginRequest request = new LoginRequest("test@test.com", "1234");

        User user = User.builder()
                .email("test@test.com")
                .encodedPassword("encoded")
                .build();

        given(userRepository.findByEmail("test@test.com"))
                .willReturn(Optional.of(user));

        given(passwordEncoder.matches("1234", "encoded"))
                .willReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UserException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        verify(tokenProvider, never()).generatedTokenDTO(any());
    }
}
