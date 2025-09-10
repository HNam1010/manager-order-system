package com.example.auth.security;

import com.example.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    @JsonIgnore // Không trả về password trong response
    private String password;
    private Collection<? extends GrantedAuthority> authorities; // Lưu role

    public UserDetailsImpl(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        // Chuyển đổi Role entity thành GrantedAuthority
        // User có thuộc tính 'role' (ManyToOne) nối chuỗi với ROLE
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getName());

        List<GrantedAuthority> authorities = Collections.singletonList(authority);


        return new UserDetailsImpl(
                user.getSerialId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; } // Logic phức tạp hơn nếu cần

    @Override
    public boolean isAccountNonLocked() { return true; } // Logic khóa tài khoản nếu cần

    @Override
    public boolean isCredentialsNonExpired() { return true; } // Logic hết hạn password nếu cần

    @Override
    public boolean isEnabled() { return true; } // Logic vô hiệu hóa tài khoản nếu cần

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}