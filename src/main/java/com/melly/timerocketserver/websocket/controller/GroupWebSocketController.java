package com.melly.timerocketserver.websocket.controller;

import com.melly.timerocketserver.domain.service.GroupService;
import com.melly.timerocketserver.domain.service.UserService;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import com.melly.timerocketserver.websocket.dto.request.KickRequestDto;
import com.melly.timerocketserver.websocket.dto.request.ReadyStatusMsgRequest;
import com.melly.timerocketserver.websocket.service.GroupChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
public class GroupWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupService groupService;
    private final UserService userService;

    public GroupWebSocketController(SimpMessagingTemplate messagingTemplate, GroupService groupService,
                                    UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.groupService = groupService;
        this.userService = userService;
    }

    @MessageMapping("/group/{groupId}/kick")
    public void handleReadyStatus(@DestinationVariable Long groupId, @Payload KickRequestDto kickRequest, Principal principal) {
        Long leaderId = getUserIdFromPrincipal(principal);
        String targetUsername = userService.findByUserId(kickRequest.getUserId()).getEmail();
        log.info("targetUsername is {}", targetUsername);
        log.info("principal.getUsername() is {}", principal.getName());
        // 1. 강퇴 처리 (DB 제거 등)
        groupService.kickGroupMember(groupId, kickRequest.getUserId(), leaderId);

        // 2. 멤버들에게 알림
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/kick", kickRequest.getUserId());

        // 3. 해당 유저에게도 알림
        messagingTemplate.convertAndSendToUser(
                String.valueOf(targetUsername), // 대상 유저 세션 키
                "/queue/kick", // 개인 큐
                "강퇴당했습니다"
        );
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
