package com.melly.timerocketserver.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMemberListResponse {
    private List<MemberDto> members;
    private int MemberCount;
    private int MemberLimit;
    private int currentRound;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MemberDto {
        private Long groupMemberId;
        private Long userId;
        private String nickname;
        @JsonProperty("isKicked")
        private boolean isKicked;
        @JsonProperty("isReady")
        private boolean isReady;
    }
}
