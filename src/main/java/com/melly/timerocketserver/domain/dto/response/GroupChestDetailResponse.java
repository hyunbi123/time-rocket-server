package com.melly.timerocketserver.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String groupName;
    private String rocketName;
    private String designUrl;
    private LocalDateTime sentAt;

    @JsonProperty("isLocked")
    private boolean isLocked;
    private LocalDateTime lockExpiredAt;

    private List<GroupRocketContentResponse> contents;
    private List<RocketFileResponse> rocketFiles;
}
