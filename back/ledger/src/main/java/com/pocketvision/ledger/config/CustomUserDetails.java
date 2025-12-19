package com.pocketvision.ledger.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Custom UserDetails chứa user ID để tránh query database mỗi lần
 */
public class CustomUserDetails implements UserDetails {
    
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Long userId; // Lưu user ID để tránh query database

    public CustomUserDetails(String email, String password, Collection<? extends GrantedAuthority> authorities, Long userId) {
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}


