package com.melly.timerocketserver.domain.user.service;

import com.melly.timerocketserver.domain.user.dto.request.PasswordRequestDto;
import com.melly.timerocketserver.domain.user.dto.request.SignUpRequestDto;
import com.melly.timerocketserver.domain.user.dto.request.UpdateStatusRequestDto;

import com.melly.timerocketserver.domain.user.entity.Role;
import com.melly.timerocketserver.domain.user.entity.Status;
import com.melly.timerocketserver.domain.user.entity.UserEntity;
import com.melly.timerocketserver.domain.user.repository.UserRepository;
import com.melly.timerocketserver.global.exception.DuplicateNicknameException;
import com.melly.timerocketserver.global.exception.UserNotFoundException;

import com.melly.timerocketserver.domain.user.dto.request.ProfileUpdateRequest;
import com.melly.timerocketserver.domain.user.enums.ActivityType;
import com.melly.timerocketserver.domain.user.utils.PlanetLevelConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 유저 프로필 정보를 조회
    @Transactional(readOnly = true) // 데이터 변경 없이 읽기만 하므로 읽기 전용 트랜잭션 설정
    public UserEntity getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
    }

    // 회원가입 비즈니스 로직
    @Transactional // 명시적 트랜잭션 관리
    public void signUp(SignUpRequestDto signUpRequestDto) {
        // 닉네임과 이메일이 동일한지 확인
        if (signUpRequestDto.getNickname().equalsIgnoreCase(signUpRequestDto.getEmail())) {
            // RuntimeException 은 throws 를 명시하지 않음
            throw new IllegalArgumentException("닉네임은 이메일과 동일할 수 없습니다.");
        }
        // 이메일 중복 검사
        if(userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        // 닉네임 중복 검사
        if(userRepository.existsByNickname(signUpRequestDto.getNickname())) {
            throw new DuplicateNicknameException("이미 존재하는 닉네임입니다.");
        }
        UserEntity userEntity = UserEntity.builder()
                .email(signUpRequestDto.getEmail())
                .password(passwordEncoder.encode(signUpRequestDto.getPassword()))
                .nickname(signUpRequestDto.getNickname())
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        userRepository.save(userEntity);
    }

    // 닉네임 중복체크 비즈니스 로직
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public void duplicateNickname(String nickname) {
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        if (isDuplicate) {
            throw new DuplicateNicknameException("이미 존재하는 닉네임입니다.");
        }
    }

    // 이메일과 닉네임을 통한 유저 찾기
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public UserEntity findByEmailOrNickname(String username) {
        UserEntity user = userRepository.findByEmailOrNickname(username, username)
                .orElseThrow(() -> new UserNotFoundException("해당 회원은 존재하지 않습니다."));
        return user;
    }

    // 이메일을 통한 유저 찾기
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public UserEntity findByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 회원은 존재하지 않습니다."));
        return user;
    }

    // 이메일 중복 여부 확인
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public boolean isEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    // 비밀번호 변경
    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    public void updatePassword(Long userId, PasswordRequestDto passwordRequestDto) {
        String currentPassword = passwordRequestDto.getCurrentPassword();
        String newPassword = passwordRequestDto.getNewPassword();

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("해당 회원은 존재하지 않습니다."));

        // 소셜 로그인 사용자는 비밀번호 변경 불가, 프론트에서 처리 못한 예외처리를 위한 코드
        if (userEntity.getProvider() != null) {
            throw new IllegalStateException("소셜 로그인은 비밀번호 변경이 불가합니다.");
        }

        if(!passwordEncoder.matches(currentPassword,userEntity.getPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity); // 변경 감지(Dirty Checking) 기능으로 save() 호출 없어도 됨
    }

    // 계정 상태 변경
    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    public void updateStatus(Long userId, UpdateStatusRequestDto updateStatusRequestDto) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 회원은 존재하지 않습니다."));

        String newStatus = updateStatusRequestDto.getStatus();
        // status 값에 따라서 처리하는 로직
        if ("DELETED".equals(newStatus)) {
            userEntity.setStatus(Status.DELETED);
            userEntity.setDeletedAt(LocalDateTime.now()); // DELETED 시 deletedAt 설정
        } else if ("INACTIVE".equals(newStatus)) {
            userEntity.setStatus(Status.INACTIVE);
        } else if ("ACTIVE".equals(newStatus)) {
            userEntity.setStatus(Status.ACTIVE);
        } else{
            throw new IllegalArgumentException("잘못된 상태 변경값입니다.");
        }
        userRepository.save(userEntity);
    }

    // user_id 로 회원 닉네임 찾기
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public String findNicknameByUserId(Long userId) {
        return userRepository.findNicknameByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 회원은 존재하지 않습니다."));
    }

    // // user_id 로 회원 객체 찾기
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public UserEntity findByUserId(Long userId){
        return userRepository.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("해당 회원은 존재하지 않습니다."));
    }


    /**
     * 유저 프로필 정보를 업데이트합니다.
     * 닉네임, 상태 메시지, 프로필 이미지 URL, 사용자 색상 등을 변경할 수 있습니다.
     * @param userId 업데이트할 유저의 ID
     * @param request 업데이트할 정보가 담긴 ProfileUpdateRequest DTO
     * @return 업데이트된 UserEntity 객체
     */
    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    public UserEntity updateProfile(Long userId, ProfileUpdateRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            if (!user.getNickname().equals(request.getNickname()) && userRepository.existsByNickname(request.getNickname())) {
                throw new DuplicateNicknameException("이미 존재하는 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }
        // statusMessage는 null, 빈 문자열 모두 허용
        if (request.getStatusMessage() != null) {
            user.setStatusMessage(request.getStatusMessage());
        }
        // profileImageUrl은 null 허용
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        // userColor는 null, 빈 문자열 허용하지 않음 (유효성 검사 추가 가능)
        if (request.getUserColor() != null && !request.getUserColor().isEmpty()) {
            user.setUserColor(request.getUserColor());
        }

        // save()는 변경 감지에 의해 필요 없지만, 명시적으로 호출하여 flush 시점을 조절할 수 있습니다.
        return userRepository.save(user);
    }

    /**
     * 사용자에게 경험치를 부여하고, 레벨 및 행성을 업데이트합니다.
     * @param userId 경험치를 부여할 사용자 ID
     * @param activityType 발생한 활동 유형 (경험치 정보 포함)
     * @return 업데이트된 UserEntity
     */
    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    public UserEntity addExperience(Long userId, ActivityType activityType) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        int gainedExp = activityType.getExperiencePoints();
        int currentExp = user.getCurrentExperiencePoints();
        int newExp = currentExp + gainedExp;

        // 경험치 최대치 (7000px) 제한
        if (newExp > 7000) {
            newExp = 7000;
        }
        user.setCurrentExperiencePoints(newExp);
        log.info("사용자 {} (ID: {}) 이 {} 활동으로 {} 경험치 획득, 현재 경험치: {}", user.getNickname(), userId, activityType.getDescription(), gainedExp, newExp);


        // 레벨 계산 및 업데이트
        int oldLevel = user.getCurrentLevel();
        int newLevel = PlanetLevelConfig.calculateLevel(newExp);
        if (newLevel > oldLevel) {
            log.info("사용자 {} (ID: {}) 레벨업! {} -> {}", user.getNickname(), userId, oldLevel, newLevel);
            user.setCurrentLevel(newLevel);
        }

        // 행성 변경 감지 및 업데이트
        String oldPlanet = user.getCurrentPlanet();
        PlanetLevelConfig.PlanetInfo newPlanetInfo = PlanetLevelConfig.getPlanetInfoByExperience(newExp);
        String newPlanet = (newPlanetInfo != null) ? newPlanetInfo.getName() : user.getCurrentPlanet(); // 행성을 찾지 못하면 기존 유지

        // 행성 변경이 발생했는지 확인
        if (!newPlanet.equals(oldPlanet)) {
            user.setCurrentPlanet(newPlanet);
            user.setPlanetLevelEnteredDate(LocalDate.now()); // 새로운 행성 진입일로 업데이트
            log.info("사용자 {} (ID: {}) 행성 진입! {} -> {}, 진입일: {}", user.getNickname(), userId, oldPlanet, newPlanet, user.getPlanetLevelEnteredDate());
        }

        user.setStatusMessage(PlanetLevelConfig.getStatusMessage(user.getCurrentLevel(), newPlanetInfo));

        // 변경된 UserEntity 저장 (자동 변경 감지에 의해 save()는 생략될 수 있지만, 명시적으로 호출)
        return userRepository.save(user);
    }
}
