package com.melly.timerocketserver.websocket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReadyStatusMsgRequest {
    private Long groupId;
    private Integer currentRound;
    private Boolean isReady;
}
