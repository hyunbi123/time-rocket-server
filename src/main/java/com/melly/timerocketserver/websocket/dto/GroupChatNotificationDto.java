package com.melly.timerocketserver.websocket.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupChatNotificationDto {
    private Long groupId;
    private Long userId;
    private String nickname;
    private String message;
}
