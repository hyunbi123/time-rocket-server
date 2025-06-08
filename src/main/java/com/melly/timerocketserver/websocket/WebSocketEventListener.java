package com.melly.timerocketserver.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.security.core.Authentication;
import com.melly.timerocketserver.global.security.CustomUserDetails;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(final SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Authentication auth = (Authentication) accessor.getUser();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            String nickname = userDetails.getUser().getNickname();

            // 로그 출력
            log.info("사용자 퇴장 감지됨: {}", nickname);

            // 예시: 채팅방 ID를 세션에서 꺼내거나, 사용자 객체에 저장해두어야 함
            Long groupId = (Long) accessor.getSessionAttributes().get("groupId");
            Map<String, Object> leaveMessage = new HashMap<>();
            if (groupId != null) {
                leaveMessage.put("message",  nickname + "님이 퇴장하셨습니다.");
                messagingTemplate.convertAndSend("/topic/group/" + groupId, leaveMessage);
            }
        } else {
            log.info("인증되지 않은 사용자의 세션 종료 감지됨");
        }
    }
}