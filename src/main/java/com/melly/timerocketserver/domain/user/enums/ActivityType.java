package com.melly.timerocketserver.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityType {
    DAILY_LOGIN(10, "매일 로그인"), // 매일 로그인 10px
    CREATE_POST(5, "게시물 작성"), // 게시물 작성 5px
    ROCKET_LAUNCH(10, "로켓 발사"), // 로켓 발사 10px
    ROCKET_RECEIVE(10, "로켓 수신"), // 로켓 수신 10px
    PROFILE_COMPLETE(20, "프로필 완성"), // 프로필 완성 20px
    EARN_BADGE(15, "배지 획득"), // 배지 획득 15px
    WRITE_COMMENT(3, "댓글 작성"); // 댓글 작성 3px

    private final int experiencePoints; // 이 필드가 있어야 getExperiencePoints() 메소드가 작동
    private final String description; // 이 필드가 있어야 getDescription() 메소드가 작동
}