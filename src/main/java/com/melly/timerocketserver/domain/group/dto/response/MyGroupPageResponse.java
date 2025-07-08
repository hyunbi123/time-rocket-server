package com.melly.timerocketserver.domain.group.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyGroupPageResponse {

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
        private String theme;
        private String description;
        private String leaderNickname;
        private Integer memberLimit;
        private Integer currentMemberCount;
        private String backgroundImage;
        private LocalDateTime createdAt;
    }
}
