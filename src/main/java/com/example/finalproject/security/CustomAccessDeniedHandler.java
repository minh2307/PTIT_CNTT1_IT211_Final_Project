package com.example.finalproject.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // Escape backslashes and double quotes in the message
        String message = "Access Denied: " + accessDeniedException.getMessage();
        String escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"");

        String jsonResponse = String.format("{\"code\":%d,\"message\":\"%s\"}", 
                HttpServletResponse.SC_FORBIDDEN, escapedMessage);

        response.getWriter().write(jsonResponse);
    }
}
