package com.melly.timerocketserver.websocket.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupChatMsgResponse {
    private Long chatMessageId;
    private Long userId;
    private String nickname;
    private String message;
    private LocalDateTime sentAt;
}
