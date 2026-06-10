package com.example.finalproject.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {
    // lấy thuộc tính từ file properties
    @Value("${jwt.secret-key}")
    private String jwtSecret;
    @Value("${jwt.expired}")
    private long expiredTime;
    // Sinh jwt
    // accessToken : 1 ngày
    public String generateAccessToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date()) // thời gian bắt đầu token hoạt động
                .setExpiration(new Date(new Date().getTime() + expiredTime))
//                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .signWith(getKey(), SignatureAlgorithm.HS256) // nghiên cứu
                .compact();
    }
    public String generateToken(String username, long expiredTime){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date()) // thời gian bắt đầu token hoạt động
                .setExpiration(new Date(new Date().getTime() + expiredTime))
//                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .signWith(getKey(), SignatureAlgorithm.HS256) // nghiên cứu
                .compact();
    }
    private Key getKey(){
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        System.out.println("key : "+ Arrays.toString(key.getEncoded()));
        return key;// base 64
    }
    // Sử dụng Key để mã hóa base 64 chứ ko dùng chuỗi raw secret
    // refreshToken : 7 ngày
    public String generateRefreshToken(UserDetails userDetails){
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
//                .setPayload(userDetails.getAuthorities().toString())
                .setIssuedAt(new Date()) // thời gian bắt đầu token hoạt động
                .setExpiration(new Date(new Date().getTime() + expiredTime*7))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

    }
    // Xác minh token hợp lệ
    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch (MalformedJwtException e){
            // token ko đúng loại
            log.error("Invalid token ",e.getMessage());
        }catch (UnsupportedJwtException e){
            log.error("Unsupported token ",e.getMessage()); // ko hỗ trợ token
        }catch (ExpiredJwtException e){
            // token hêt hạn
            log.error("Expired token ",e.getMessage());
        }
        catch (IllegalArgumentException e){
            log.error("Jwt key string invalid ",e.getMessage()); // secret-key ko hợp lệ
        }
        return false;
    }

    // Giải mã token lấy thông tin
    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token).getBody().getSubject();
    }
}