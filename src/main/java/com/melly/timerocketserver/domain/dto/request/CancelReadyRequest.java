package com.melly.timerocketserver.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelReadyRequest {
    private Boolean isReady;
    private Integer currentRound;
}
