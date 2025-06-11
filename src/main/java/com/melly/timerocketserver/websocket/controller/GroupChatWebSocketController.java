package com.melly.timerocketserver.websocket.controller;

import com.melly.timerocketserver.domain.dto.response.GroupMemberListResponse;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import com.melly.timerocketserver.websocket.dto.GroupChatNotificationDto;
import com.melly.timerocketserver.websocket.dto.request.GroupChatMsgRequest;
import com.melly.timerocketserver.websocket.dto.response.GroupChatMsgResponse;
import com.melly.timerocketserver.websocket.service.GroupChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

// 웹소켓 컨트롤러에서는 반드시 Principal 파라미터로 유저 정보 받아야 함
// SecurityContextHolder.getContext().getAuthentication() 는 웹소켓에서는 동작을 보장하지 않음
@Controller
@Slf4j
public class GroupChatWebSocketController implements ResponseController {

    private final GroupChatService groupChatService;
    private final SimpMessagingTemplate messagingTemplate;

    public GroupChatWebSocketController(GroupChatService groupChatService, SimpMessagingTemplate messagingTemplate) {
        this.groupChatService = groupChatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/group/{groupId}/chat")
    public void handleGroupChat(@DestinationVariable Long groupId,
                                GroupChatMsgRequest request,
                                Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        GroupChatMsgResponse response = groupChatService.processAndSendMessage(groupId, userId, request);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, response);
    }

    @MessageMapping("/group/{groupId}/enter")
    public void handleEnterMessage(@DestinationVariable Long groupId, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("접속한 유저: " + principal.getName());
        Long userId = getUserIdFromPrincipal(principal);
        headerAccessor.getSessionAttributes().put("groupId", groupId);
        GroupChatNotificationDto enterDto = groupChatService.createEnterMessage(groupId, userId);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, enterDto);
    }

    @MessageMapping("/group/{groupId}/exit")
    public void handleExitMessage(@DestinationVariable Long groupId, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        GroupChatNotificationDto exitDto = groupChatService.createExitMessage(groupId, userId);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, exitDto);
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal instanceof Authentication authentication) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.info("[WebSocketController] 유저 인증 성공: {}", userDetails.getUsername());
            return userDetails.getUser().getUserId();
        }
        throw new IllegalStateException("WebSocket 인증 실패: 인증되지 않은 사용자입니다.");
    }
}
