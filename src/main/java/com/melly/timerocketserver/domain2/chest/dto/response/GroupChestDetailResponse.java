package com.melly.timerocketserver.domain2.chest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melly.timerocketserver.domain2.rocket.dto.response.RocketFileResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupChestDetailResponse {
    private Long groupRocketId;
    private String rocketName;
    private String designUrl;
    private LocalDateTime sentAt;

    @JsonProperty("isLocked")
    private boolean isLocked;
    private LocalDateTime lockExpiredAt;

    private List<GroupRocketContentResponse> contents;
    private List<RocketFileResponse> rocketFiles;
}
