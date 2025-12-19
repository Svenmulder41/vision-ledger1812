package com.pocketvision.ledger.config;

import com.pocketvision.ledger.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        final Long userId;

        // 1. Kiểm tra header có Token không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); // Lấy chuỗi token sau chữ "Bearer "
        
        try {
            userEmail = jwtUtils.extractEmail(jwt);
            userId = jwtUtils.extractUserId(jwt); // Extract user ID từ token (tối ưu)
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Nếu có email nhưng chưa được xác thực trong Context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            
            // Tối ưu: Nếu có userId và role trong token, tạo CustomUserDetails trực tiếp (không query database)
            String role = jwtUtils.extractRole(jwt);
            if (userId != null && role != null) {
                // Tạo CustomUserDetails từ token - KHÔNG query database
                userDetails = new CustomUserDetails(
                    userEmail,
                    "", // Password không cần thiết cho JWT authentication
                    Collections.singleton(new SimpleGrantedAuthority(role)),
                    userId
                );
            } else {
                // Fallback: Query database nếu token cũ không có đủ thông tin
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            }
            
            // 3. Kiểm tra tính hợp lệ của token
            if (jwtUtils.isTokenValid(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 4. Lưu thông tin authentication vào context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}