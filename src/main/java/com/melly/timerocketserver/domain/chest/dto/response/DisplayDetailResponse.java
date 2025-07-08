package com.melly.timerocketserver.domain.chest.dto.response;

import com.melly.timerocketserver.domain.rocket.dto.response.RocketFileResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisplayDetailResponse {
    private Long rocketId;
    private String rocketName;
    private String designUrl;
    private String senderEmail;
    private LocalDateTime sentAt;
    private String content;
    private List<RocketFileResponse> rocketFiles;
}
