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
public class GroupChestPageResponse {
    // 여러 개의 ChestDto 객체를 포함
    private List<GroupChestPageResponse.GroupChestDto> groupChests;
    private int currentPage;
    private int pageSize;
    private Long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private String sortBy;
    private String sortDirection;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class GroupChestDto {
        private Long groupChestId;
        private Long groupRocketId;
        private Long groupId;
        private String groupName;
        private String rocketName;
        private String designUrl;

        @JsonProperty("isLock")
        private boolean isLock;
        private LocalDateTime lockExpiredAt;

        @JsonProperty("isPublic")
        private boolean isPublic;
        private LocalDateTime publicAt;
    }
}
