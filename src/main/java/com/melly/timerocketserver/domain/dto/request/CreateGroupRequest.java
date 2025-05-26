package com.melly.timerocketserver.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateGroupRequest {
    @NotBlank(message = "모임명은 필수입니다.")
    @Size(max = 255, message = "모임명은 최대 255자까지 가능합니다.")
    private String groupName;

    private String description;

    @NotNull(message = "모임 인원은 필수입니다.")
    @Min(value = 2, message = "모임 인원은 최소 2명입니다.")
    @Max(value = 10, message = "모임 인원은 최대 10명입니다.")
    private Integer memberLimit;

    @NotNull(message = "private 설정은 필수입니다.")
    private Boolean isPrivate;

    private String password;
    private MultipartFile backgroundImage;
}
