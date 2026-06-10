package com.example.finalproject.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Escape backslashes and double quotes in the message
        String message = "Unauthorized: " + authException.getMessage();
        String escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"");

        String jsonResponse = String.format("{\"code\":%d,\"message\":\"%s\"}", 
                HttpServletResponse.SC_UNAUTHORIZED, escapedMessage);

        response.getWriter().write(jsonResponse);
    }
}
