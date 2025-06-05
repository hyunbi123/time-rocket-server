package com.melly.timerocketserver.websocket.controller;

import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import com.melly.timerocketserver.websocket.dto.GroupChatNotificationDto;
import com.melly.timerocketserver.websocket.dto.request.GroupChatMsgRequest;
import com.melly.timerocketserver.websocket.dto.response.GroupChatMsgResponse;
import com.melly.timerocketserver.websocket.service.GroupChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
public class GroupChatWebSocketController implements ResponseController {

    private final GroupChatService groupChatService;
    private final SimpMessagingTemplate messagingTemplate;

    public GroupChatWebSocketController(GroupChatService groupChatService, SimpMessagingTemplate messagingTemplate) {
        this.groupChatService = groupChatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/group/{groupId}/chat")
    public void handleGroupChat(@DestinationVariable Long groupId, GroupChatMsgRequest request) {
        GroupChatMsgResponse response = groupChatService.processAndSendMessage(groupId, getCurrentUserId(), request);
        // 메시지 전송
        messagingTemplate.convertAndSend("/topic/group/" + groupId, response);
    }

    // 모임 참여 후 입장 메시지
    @MessageMapping("/group/{groupId}/enter")
    public void handleEnterMessage(@DestinationVariable Long groupId) {
        GroupChatNotificationDto enterDto = groupChatService.createEnterMessage(groupId, getCurrentUserId());
        messagingTemplate.convertAndSend("/topic/group/" + groupId, enterDto);
    }

    @MessageMapping("/group/{groupId}/exit")
    public void handleExitMessage(@DestinationVariable Long groupId) {
        GroupChatNotificationDto exitDto = groupChatService.createExitMessage(groupId, getCurrentUserId());
        messagingTemplate.convertAndSend("/topic/group/" + groupId, exitDto);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser().getUserId();
    }
}
