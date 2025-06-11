package com.melly.timerocketserver.websocket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinedMemberPayload {
    private Long userId;
    private String nickname;
}
