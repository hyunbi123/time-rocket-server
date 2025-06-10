package com.melly.timerocketserver.global.config;

import com.melly.timerocketserver.websocket.AuthChannelInterceptor;
import com.melly.timerocketserver.websocket.HttpHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthChannelInterceptor authChannelInterceptor;

    public WebSocketConfig(AuthChannelInterceptor authChannelInterceptor) {
        this.authChannelInterceptor = authChannelInterceptor;
    }

    private static final String ENDPOINT = "/ws";
    private static final String SIMPLE_BROKER_TOPIC = "/topic";
    private static final String PUBLISH = "/app";
    private static final String USER = "/user";
    private static final String SIMPLE_BROKER_QUEUE = "/queue";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(SIMPLE_BROKER_TOPIC, SIMPLE_BROKER_QUEUE);
        registry.setApplicationDestinationPrefixes(PUBLISH);
        registry.setUserDestinationPrefix(USER);      // 개인 메시지 전송용 프리픽스 설정
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(ENDPOINT)
                .addInterceptors(new HttpHandshakeInterceptor())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(4);
        registration.interceptors(authChannelInterceptor);
    }
}