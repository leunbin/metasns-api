//요청 앞단에 붙이는 필터(미들웨어)
package com.example.metasns_api.domain.auth;

import com.example.metasns_api.common.exception.AuthException;
import com.example.metasns_api.domain.user.User;
import com.example.metasns_api.domain.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer";

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    //헤더에서 토큰 정보 가져오기
    private String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)){
            return bearerToken.split(" ")[1].trim();
        }

        return null;
    }

    // 실제 필터링 로직은 doFilterInternal에 들어감
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //1. 요청 헤더에서 토큰 꺼내기
        String jwt = resolveToken(request);

        //2. validateToken으로 유효성 검사
        if(StringUtils.hasText(jwt)){
            //3. 유효성 검사(실패시 AuthException 바로 던지기)
            tokenProvider.validateToken(jwt);

            //4. 토큰에서 사용자 정보 추출하기
            String authentication = tokenProvider.getAuthentication(jwt);

            User user = userRepository.findByEmail(authentication)
                    .orElseThrow(() ->
                            new AuthException(
                                    HttpStatus.UNAUTHORIZED,
                                    "인증된 사용자를 찾을 수 없습니다."
                            )
                    );

            request.setAttribute("user", user);
        }
        filterChain.doFilter(request, response);
    }
}
