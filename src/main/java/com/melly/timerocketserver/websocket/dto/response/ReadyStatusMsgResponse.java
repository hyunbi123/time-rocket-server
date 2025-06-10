package com.melly.timerocketserver.websocket.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReadyStatusMsgResponse {
    private Long userId;
    private int round;
    @JsonProperty("isReady")
    private boolean isReady;
}
