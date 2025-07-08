package com.melly.timerocketserver.global.security;

import com.melly.timerocketserver.domain.user.entity.Status;
import com.melly.timerocketserver.domain.user.entity.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

// Spring Security 인증 과정에서 사용자 정보를 나타내는 객체
@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {
    private final UserEntity userEntity;
    private Map<String, Object> attributes;
    
    // 일반 로그인
    public CustomUserDetails(UserEntity userEntity) {
        this.userEntity = userEntity;
    }
    
    // 소셜 로그인
    public CustomUserDetails(UserEntity userEntity, Map<String, Object> attributes) {
        this.userEntity = userEntity;
        this.attributes = attributes;
    }

    public UserEntity getUser() {
        return userEntity;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userEntity.getRoleDescription();
            }
        });
        return collect;
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // isAccountNonLocked 가 false 반환 할 경우 LockedException 발생
        // 탈퇴 계정은 잠긴 계정으로 간주
        return userEntity.getStatus() != Status.DELETED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // isEnabled 가 false 반환 할 경우 DisabledException 발생
        // 활성 상태인 경우에만 로그인 허용
        return userEntity.getStatus() == Status.ACTIVE;
    }

    @Override
    public String getName() {
        return "";
    }
}
