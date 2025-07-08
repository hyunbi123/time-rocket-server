package com.melly.timerocketserver.domain2.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequestDto {
    private String status; // ACTIVE, INACTIVE, DELETED
}
