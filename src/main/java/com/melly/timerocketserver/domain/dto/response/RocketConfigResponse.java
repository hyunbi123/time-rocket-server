package com.melly.timerocketserver.domain.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RocketConfigResponse {
    private String rocketName;
    private String design;
    private String lockExpiredAt; // ISO-8601 string
}
