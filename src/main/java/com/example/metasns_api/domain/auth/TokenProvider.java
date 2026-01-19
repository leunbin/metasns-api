// 유저 정보로 JWT 토큰을 만들거나 토큰을 바탕으로 유저 정보를 가져옴
package com.example.metasns_api.domain.auth;

import com.example.metasns_api.common.exception.AuthException;
import com.example.metasns_api.domain.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;

    private final Key key;

    private Claims parseClaims(String accessToken){
        try{
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public TokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDto generatedTokenDTO(User user){
        // 권한들 가져오기
        String authorities = "ROLE_USER";
        long now = (new Date()).getTime();

        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        //토큰 생성
        String accessToken = Jwts.builder()
                .setSubject(user.getEmail())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        //Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .build();
    }

    public String getAuthentication(String accessToken){
        //토큰 해석하기
        Claims claims = parseClaims(accessToken);

        return claims.getSubject();
    }

    public void validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch(io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "잘못된 JWT 서명입니다."
            );
        } catch(ExpiredJwtException e){
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "만료된 JWT 토큰입니다."
            );
        } catch(UnsupportedJwtException e){
            throw new AuthException(
                    HttpStatus.BAD_REQUEST,
                    "지원되지 않는 JWT 토큰입니다."
            );
        } catch(IllegalArgumentException e){
            throw new AuthException(
                    HttpStatus.BAD_REQUEST,
                    "JWT 토큰이 잘못되었습니다."
            );
        }
    }
}
