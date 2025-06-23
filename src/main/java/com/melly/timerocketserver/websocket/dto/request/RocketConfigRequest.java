package com.melly.timerocketserver.websocket.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RocketConfigRequest {
    private String rocketName;
    private String lockExpiredAt; // ISO 8601 형식 String (또는 LocalDateTime 사용 가능)
    private String design;
    private Long senderId;
}
