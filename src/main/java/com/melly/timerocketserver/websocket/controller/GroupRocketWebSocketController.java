package com.melly.timerocketserver.websocket.controller;

import com.melly.timerocketserver.global.security.CustomUserDetails;
import com.melly.timerocketserver.websocket.dto.request.ReadyStatusMsgRequest;
import com.melly.timerocketserver.websocket.service.GroupRocketWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
public class GroupRocketWebSocketController {

    private final GroupRocketWebSocketService groupRocketWebSocketService;

    public GroupRocketWebSocketController(GroupRocketWebSocketService groupRocketWebSocketService) {
        this.groupRocketWebSocketService = groupRocketWebSocketService;
    }

    @MessageMapping("/group/{groupId}/readyStatus")
    public void handleReadyStatus(@DestinationVariable Long groupId, ReadyStatusMsgRequest message, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        log.info("[ReadyStatus] groupId={} round={} isReady={}", groupId, message.getCurrentRound(), message.getIsReady());

        groupRocketWebSocketService.publishReadyStatus(groupId, userId, message.getCurrentRound(), message.getIsReady());
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal instanceof Authentication authentication) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.info("[ReadyStatus WebSocket] 유저 인증 성공: {}", userDetails.getUsername());
            return userDetails.getUser().getUserId();
        }
        throw new IllegalStateException("WebSocket 인증 실패: 인증되지 않은 사용자입니다.");
    }
}
