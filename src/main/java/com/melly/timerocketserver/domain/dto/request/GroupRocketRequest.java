package com.melly.timerocketserver.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GroupRocketRequest {
    @NotBlank(message = "로켓 이름은 필수 항목입니다.")
    private String rocketName;

    @NotBlank(message = "로켓 디자인은 필수 항목입니다.")
    private String design;

    @NotNull(message = "잠금 해제일은 필수 항목입니다.")
    private LocalDateTime lockExpiredAt;

    private String receiverEmail;

    private List<MultipartFile> files;
}
