package com.melly.timerocketserver.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupPageResponse {
    private List<GroupDto> groups;
    private int currentPage;
    private int pageSize;
    private boolean hasNext; // 다음 페이지가 있는지 여부
    private String sortBy;
    private String sortDirection;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class GroupDto {
        private Long groupId;
        private String groupName;
        private String description;
        private String leaderNickname;
        private Integer memberLimit;
        private Integer currentMemberCount;
        @JsonProperty("isPrivate")
        private Boolean isPrivate;
        private String backgroundImage;
    }
}
