package com.example.finalproject.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter { // tạo 1 bộ lọc request
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // chặn reqquest ở đây
        // B1 : lấy token từ header
        String token = getTokenFromRequest(request);
        // B2 : Verify
        if (token!=null && jwtUtils.validateToken(token)){
            // B3 : Giải mã
            String username = jwtUtils.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // B4 : Lưu lại vào Context của Security
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication); // lưu vào context
        }
        // reuqest đi tiếp vào các fillter khác
        filterChain.doFilter(request,response);

    }

    private String getTokenFromRequest(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if (authorization!=null && authorization.startsWith("Bearer ")){
            return authorization.substring(7);
        }
        return null;
    }
}