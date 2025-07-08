package com.melly.timerocketserver.domain.chest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisplayLocationMoveRequest {
    @NotNull
    private Long sourceChestId;

    private Long targetChestId; // 선택적으로 존재

    @NotNull
    private Long targetDisplayLocation; // 이동할 위치 (1~10 중 하나)
}
