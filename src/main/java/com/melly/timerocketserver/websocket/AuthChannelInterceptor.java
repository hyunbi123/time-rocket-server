package com.melly.timerocketserver.websocket;

import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.service.UserService;
import com.melly.timerocketserver.global.jwt.JwtUtil;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthChannelInterceptor(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new IllegalArgumentException("No Authorization header");
            }

            String token = authorization.substring(7);

            if (!jwtUtil.validate(token)) {
                throw new IllegalArgumentException("Invalid Token");
            }

            String username = jwtUtil.getUsername(token);
            UserEntity user = userService.findByEmailOrNickname(username);
            // 권한 리스트 생성
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            // Authentication 객체 생성 (CustomUserDetails로 만들어도 됨)
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    new CustomUserDetails(user),  // principal
                    null,  // credentials
                    authorities
            );

            // setUser에 Authentication 넣기
            accessor.setUser(authentication);
        }
        return message;
    }
}