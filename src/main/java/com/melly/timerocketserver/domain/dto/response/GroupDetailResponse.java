package com.melly.timerocketserver.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDetailResponse {
    private Long groupId;
    private String groupName;
    private String description;
    private String leaderNickname;
    private Long leaderId;
    private Integer memberLimit;
    private Integer currentMemberCount;
    @JsonProperty("isPrivate")
    private Boolean isPrivate;
    private String backgroundImage;
}
