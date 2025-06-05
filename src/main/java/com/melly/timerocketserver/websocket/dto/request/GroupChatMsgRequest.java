package com.melly.timerocketserver.websocket.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupChatMsgRequest {
    String message;
}
