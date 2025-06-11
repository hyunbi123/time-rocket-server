package com.melly.timerocketserver.websocket.service;

import com.melly.timerocketserver.websocket.dto.response.ReadyStatusMsgResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GroupRocketWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public GroupRocketWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishReadyStatus(Long groupId, Long userId, int round, boolean isReady) {
        ReadyStatusMsgResponse message = new ReadyStatusMsgResponse(userId, round, isReady);
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/readyStatus", message);
    }

    public void publishRocketSent(Long groupId, Long userId) {
        Map<String, Object> payload = Map.of(
                "type", "rocketSent",
                "senderId", userId
        );
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/send", payload);
    }
}
