package com.melly.timerocketserver.websocket.controller;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import com.melly.timerocketserver.websocket.dto.request.JoinMemberRequest;
import com.melly.timerocketserver.websocket.dto.request.KickRequestDto;
import com.melly.timerocketserver.websocket.service.GroupWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import java.security.Principal;


@Controller
@Slf4j
public class GroupWebSocketController {
    private final GroupWebSocketService groupWebSocketService;

    public GroupWebSocketController(GroupWebSocketService groupWebSocketService) {
       this.groupWebSocketService = groupWebSocketService;
    }

    @MessageMapping("/group/{groupId}/kick")
    public void handleKick(
            @DestinationVariable Long groupId,
            @Payload KickRequestDto kickRequest,
            Principal principal
    ) {
        Long leaderId = getUserIdFromPrincipal(principal);
        groupWebSocketService.kickAndNotify(groupId, kickRequest.getUserId(), leaderId);
    }

    @MessageMapping("/group/{groupId}/join_member")
    public void handleMemberChange(@DestinationVariable Long groupId, @Payload JoinMemberRequest message, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        groupWebSocketService.broadcastJoinedMember(groupId, userId, message.isReady());
    }

    @MessageMapping("/group/{groupId}/leave_member")
    public void handleMemberLeave(@DestinationVariable Long groupId, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        groupWebSocketService.broadcastLeaveMember(groupId, userId);
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
