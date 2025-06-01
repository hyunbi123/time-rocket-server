package com.melly.timerocketserver.domain.dto.response;

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

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MemberDto {
        private Long groupMemberId;
        private Long userId;
        private String nickname;
        private boolean isKicked;
        private boolean isSavedRocket;
    }
}
