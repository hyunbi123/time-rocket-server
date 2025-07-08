package com.melly.timerocketserver.websocket.service;

import com.melly.timerocketserver.domain.service.GroupService;
import com.melly.timerocketserver.domain2.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GroupWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupService groupService;
    private final UserService userService;

    public GroupWebSocketService(SimpMessagingTemplate messagingTemplate, GroupService groupService,
                                    UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.groupService = groupService;
        this.userService = userService;
    }

    public void kickAndNotify(Long groupId, Long targetUserId, Long leaderId) {
        String targetUsername = userService.findByUserId(targetUserId).getEmail();
        log.info("targetUsername is {}", targetUsername);

        // 1. 강퇴 처리
        groupService.kickGroupMember(groupId, targetUserId, leaderId);

        // 2. 멤버들에게 알림
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/kick", targetUserId);

        // 3. 해당 유저에게도 알림
        messagingTemplate.convertAndSendToUser(
                targetUsername, // 대상 유저 세션 키
                "/queue/kick",
                "강퇴당했습니다"
        );
    }

    public void broadcastJoinedMember(Long groupId, Long userId, boolean isReady) {
        String nickname = userService.findByUserId(userId).getNickname();

        Map<String, Object> memberPayload = new HashMap<>();
        memberPayload.put("userId", userId);
        memberPayload.put("nickname", nickname);
        memberPayload.put("isReady", isReady);

        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/members", Map.of("member", memberPayload));
    }

    public void broadcastLeaveMember(Long groupId, Long userId) {
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/members", Map.of("leaveUserId", userId));
    }
}
