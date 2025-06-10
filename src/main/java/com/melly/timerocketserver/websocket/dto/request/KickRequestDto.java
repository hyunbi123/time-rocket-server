package com.melly.timerocketserver.websocket.dto.request;

import lombok.Getter;

@Getter
public class KickRequestDto {
    private Long userId; // 강퇴당할 사용자 ID
}
