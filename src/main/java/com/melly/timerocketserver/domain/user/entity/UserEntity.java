package com.melly.timerocketserver.domain.user.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="user_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long userId;

    private String email;
    private String password;
    private String nickname;

    // =========================
    @Column(name = "profile_image_url") // 프로필 사진 URL
    private String profileImageUrl;

    @Column(name = "status_message", length = 200) // 한줄 문구 (길이를 넉넉하게 설정)
    private String statusMessage;

    @Column(name = "current_experience_points") // 현재 경험치 (PX)
    private int currentExperiencePoints;

    @Column(name = "current_level") // 현재 레벨
    private int currentLevel;

    @Column(name = "current_planet", length = 50) // 현재 행성/천체 이름
    private String currentPlanet;

    @Column(name = "planet_level_entered_date") // 해당 행성/레벨 진입 날짜
    private LocalDate planetLevelEnteredDate;

    @Column(name = "user_color", length = 10) // 사용자 대표 색상 (예: #RRGGBB)
    private String userColor;
    // =========================

    @Enumerated(EnumType.STRING)
    private Role role;

    public String getRoleDescription() {
        return role.getDescription();
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    public String getStatusDescription(){
        return status.getDescription();
    }

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    @Column(name="last_login_at")
    private LocalDateTime lastLoginAt;

    private String provider;
    @Column(name="provider_id")
    private String providerId;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        // 새롭게 추가된 필드들에 대한 초기값 설정
        if (this.currentExperiencePoints == 0) { // Builder 패턴으로 객체 생성 시 초기화가 안되어있을 경우
            this.currentExperiencePoints = 0;
        }
        if (this.currentLevel == 0) { // 기본 레벨 1로 설정
            this.currentLevel = 1;
        }
        if (this.currentPlanet == null || this.currentPlanet.isEmpty()) { // 초기 행성 설정
            this.currentPlanet = "수성";
        }
        if (this.planetLevelEnteredDate == null) { // 초기 진입 날짜 설정
            this.planetLevelEnteredDate = LocalDate.now();
        }
        if (this.userColor == null || this.userColor.isEmpty()) { // 기본 사용자 색상 설정 (예: 초기값)
            this.userColor = "#468dd5"; // 파란색 계열로 임의 설정(변경 가능)
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static UserEntity createNewUser(String email, String password, String nickname, Role role, Status status) {
        return UserEntity.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .role(role)
                .status(status)
                .currentExperiencePoints(0)
                .currentLevel(1)
                .currentPlanet("수성") // 초기 행성 명칭
                .planetLevelEnteredDate(LocalDate.now())
                .userColor("#468dd5") // 기본 색상
                .build();
    }
}
