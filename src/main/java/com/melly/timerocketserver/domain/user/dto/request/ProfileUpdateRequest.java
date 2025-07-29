package com.melly.timerocketserver.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequest {
    // 닉네임 (변경될 수 있으므로 @Pattern 이나 @Size 추가)
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "닉네임은 한글, 영어 또는 숫자로만 구성되어야 합니다.")
    private String nickname;

    // 한줄 문구 (변경될 수 있음)
    @Size(max = 100, message = "한줄 문구는 100자 이하여야 합니다.")
    private String statusMessage;

    // 프로필 이미지 URL (선택 사항이므로 @NotNull, @NotEmpty 등은 제외)
    private String profileImageUrl;

    // 사용자 대표 색상 (예: HEX 코드 #RRGGBB)
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "유효한 HEX 색상 코드가 아닙니다.")
    private String userColor;
}