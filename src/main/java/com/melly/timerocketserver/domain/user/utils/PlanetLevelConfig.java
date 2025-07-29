package com.melly.timerocketserver.domain.user.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 행성/레벨 구성을 정의하는 유틸리티 클래스입니다.
 * 경험치에 따른 레벨 및 행성 매핑, 그리고 상태 메시지 생성 로직을 포함합니다.
 */
public class PlanetLevelConfig {

    // 각 행성 구간에 대한 정의를 새롭게 확장합니다.
    private static final List<PlanetInfo> PLANET_INFOS = Arrays.asList(
            // 행성명, 해당 행성 진입에 필요한 최소 경험치, 해당 행성에 포함되는 레벨 범위 (시작 레벨, 끝 레벨)
            // (총 80 레벨, 최대 경험치 20,000PX 기준)
            new PlanetInfo("지구", 0, 1, 5, "지구 궤도에서 n일차"),               // 초기 시작점: 0PX ~
            new PlanetInfo("수성", 1000, 6, 10, "수성 궤도에서 n일차"),             // 1,000PX ~
            new PlanetInfo("금성", 2000, 11, 15, "금성 대기권에서 n일차"),          // 2,000PX ~
            new PlanetInfo("화성", 3000, 16, 20, "화성 지표에서 n일차"),          // 3,000PX ~
            new PlanetInfo("목성", 4000, 21, 25, "목성 대적점에서 n일차"),          // 4,000PX ~
            new PlanetInfo("토성", 5000, 26, 30, "토성 고리 안에서 n일차"),          // 5,000PX ~
            new PlanetInfo("천왕성", 6000, 31, 35, "천왕성 심층 탐사 n일차"),        // 6,000PX ~
            new PlanetInfo("해왕성", 7000, 36, 40, "해왕성 외곽 순찰 n일차"),        // 7,000PX ~
            new PlanetInfo("명왕성", 8000, 41, 45, "명왕성 빙하 탐사 n일차"),        // 8,000PX ~
            new PlanetInfo("카이퍼 벨트", 9000, 46, 50, "카이퍼 벨트 소행성군 탐사 n일차"), // 9,000PX ~
            new PlanetInfo("오트르 구름", 10500, 51, 55, "오트르 구름 너머로 n일차"),   // 10,500PX ~
            new PlanetInfo("안드로메다", 12000, 56, 60, "안드로메다 은하를 향한 n일차"),  // 12,000PX ~
            new PlanetInfo("블랙홀", 14000, 61, 70, "블랙홀 경계선에서 n일차"),      // 14,000PX ~ (더 긴 레벨 구간)
            new PlanetInfo("웜홀", 17000, 71, 80, "웜홀을 통한 차원 이동 n일차")      // 17,000PX ~ (최고 단계)
    );

    // 내부 클래스: 행성 정보를 저장 (설명 필드 추가)
    @Getter
    @RequiredArgsConstructor
    public static class PlanetInfo {
        private final String name;
        private final int minExperiencePoints;
        private final int minLevel;
        private final int maxLevel;
        private final String statusMessageBase; // 추가: 상태 메시지의 기본 문구
    }

    /**
     * 현재 경험치에 해당하는 행성 정보를 찾습니다.
     * @param experiencePoints 현재 경험치
     * @return 해당 행성 정보. 없으면 (예외 상황) null 반환.
     */
    public static PlanetInfo getPlanetInfoByExperience(int experiencePoints) {
        // 경험치 내림차순으로 정렬하여 가장 높은 경험치 구간부터 확인
        return PLANET_INFOS.stream()
                .filter(info -> experiencePoints >= info.getMinExperiencePoints())
                .max(Comparator.comparingInt(PlanetInfo::getMinExperiencePoints)) // 필터링된 것 중 가장 높은 MinExp 가진 행성
                .orElse(PLANET_INFOS.get(0)); // 경험치가 0이거나 기준 미달 시 첫 번째 행성 (지구) 반환
    }

    /**
     * 경험치에 따른 레벨을 계산합니다.
     * 최대 레벨 80, 최대 경험치 20,000PX 기준 (평균 250PX/레벨)
     * 이 계산 로직은 프로젝트의 레벨 디자인에 따라 조절될 수 있습니다.
     * @param experiencePoints 현재 경험치
     * @return 계산된 레벨
     */
    public static int calculateLevel(int experiencePoints) {
        // 경험치 0일 때 레벨 1이 되도록 (경험치 / 250) + 1
        int level = (int) Math.floor((double) experiencePoints / 250) + 1;
        return Math.min(level, 80); // 최대 레벨 80으로 제한
    }

    /**
     * 현재 행성 정보와 유저의 현재 레벨을 기반으로 상태 메시지를 생성합니다.
     * @param userCurrentLevel 유저의 현재 레벨
     * @param planetInfo 현재 행성 정보
     * @return 생성된 상태 메시지 (예: "수성 궤도에서 1일차")
     */
    public static String getStatusMessage(int userCurrentLevel, PlanetInfo planetInfo) {
        if (planetInfo == null) {
            return "우주 어딘가에서 표류 중..."; // 행성 정보를 찾을 수 없을 때의 기본 메시지
        }
        // 행성 내에서의 상대적 일차 계산
        int dayOffset = userCurrentLevel - planetInfo.getMinLevel() + 1;
        return planetInfo.getStatusMessageBase().replace("n", String.valueOf(dayOffset));
    }
}
