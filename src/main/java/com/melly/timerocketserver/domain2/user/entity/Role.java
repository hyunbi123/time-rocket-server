package com.melly.timerocketserver.domain2.user.entity;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("관리자"),
    USER("일반 사용자");

    private final String description;

    Role(String description) {
        this.description = description;
    }

}
