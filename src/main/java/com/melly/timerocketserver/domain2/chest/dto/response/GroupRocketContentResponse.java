package com.melly.timerocketserver.domain2.chest.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupRocketContentResponse {
    private Long grcId;
    private Long groupRocketId;
    private Long groupId;
    private Long userId;
    private String content;
    private Boolean isReady;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
