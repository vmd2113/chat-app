package com.duongw.chatapp.security.auth;

import com.duongw.chatapp.model.dto.response.role.RoleResponseDTO;
import com.duongw.chatapp.model.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data

public class AuthUserDetails implements UserDetails {

    private final Users user;
    private final Set<GrantedAuthority> authorities;


    public AuthUserDetails(Users users, Set<String> roles, Set<String> permissions) {
        this.user = users;
        Set<GrantedAuthority> auths = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        auths.addAll(permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet()));

        this.authorities = auths;
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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
