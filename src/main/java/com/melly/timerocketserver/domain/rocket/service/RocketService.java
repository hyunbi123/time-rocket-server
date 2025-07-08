package com.melly.timerocketserver.domain.rocket.service;

import com.melly.timerocketserver.domain.chest.entity.ReceivedChestEntity;
import com.melly.timerocketserver.domain.chest.entity.SentChestEntity;
import com.melly.timerocketserver.domain.chest.repository.ReceivedChestRepository;
import com.melly.timerocketserver.domain.chest.repository.SentChestRepository;
import com.melly.timerocketserver.domain.file.service.FileService;
import com.melly.timerocketserver.domain.rocket.dto.request.RocketRequestDto;
import com.melly.timerocketserver.domain.rocket.dto.response.RocketFileResponse;
import com.melly.timerocketserver.domain.rocket.dto.response.RocketResponse;
import com.melly.timerocketserver.domain.rocket.entity.RocketEntity;
import com.melly.timerocketserver.domain.rocket.entity.RocketFileEntity;
import com.melly.timerocketserver.domain.rocket.repository.RocketFileRepository;
import com.melly.timerocketserver.domain.rocket.repository.RocketRepository;
import com.melly.timerocketserver.domain.user.entity.UserEntity;
import com.melly.timerocketserver.domain.user.repository.UserRepository;
import com.melly.timerocketserver.global.exception.RocketNotFoundException;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RocketService {
    private final RocketRepository rocketRepository;
    private final RocketFileRepository rocketFileRepository;
    private final UserRepository userRepository;
    private final ReceivedChestRepository receivedChestRepository;
    private final SentChestRepository sentChestRepository;
    private final FileService fileService;

    public RocketService(RocketRepository rocketRepository, RocketFileRepository rocketFileRepository,
                         UserRepository userRepository, ReceivedChestRepository receivedChestRepository,
                         SentChestRepository sentChestRepository, FileService fileService) {
        this.rocketRepository = rocketRepository;
        this.rocketFileRepository = rocketFileRepository;
        this.userRepository = userRepository;
        this.receivedChestRepository = receivedChestRepository;
        this.sentChestRepository = sentChestRepository;
        this.fileService = fileService;
    }

    // 로켓 전송
    @Transactional
    public void sendRocket(Long userId, RocketRequestDto rocketRequestDto, List<MultipartFile> files) throws IOException {
        String rocketName = rocketRequestDto.getRocketName();
        String rocketDesign = rocketRequestDto.getDesign();
        LocalDateTime rocketLockExpiredAt = rocketRequestDto.getLockExpiredAt();
        String rocketReceiverType = rocketRequestDto.getReceiverType();
        String rocketReceiverEmail = rocketRequestDto.getReceiverEmail();
        String rocketContent = rocketRequestDto.getContent();

        // 수신자, 발신자, 그룹 정보 가져오기 (예시: 이메일로 유저 찾기)
        UserEntity sender = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("송신 회원을 찾을 수 없습니다."));
        UserEntity receiver = userRepository.findByEmail(rocketReceiverEmail)
                .orElseThrow(() -> new UserNotFoundException("수신자 이메일을 찾을 수 없습니다."));

        // 나에게 보내는 로켓인데, 송신자 != 수신자인 경우
        if ("self".equalsIgnoreCase(rocketReceiverType) && !sender.getUserId().equals(receiver.getUserId())) {
            throw new IllegalArgumentException("자기 자신에게 보내는 로켓에서 수신자와 송신자가 달라서는 안 됩니다.");
        }

        // RocketEntity 생성
        RocketEntity rocket = RocketEntity.builder()
                .rocketName(rocketName)
                .design(rocketDesign)
                .lockExpiredAt(rocketLockExpiredAt)
                .receiverType(rocketReceiverType)
                .senderUser(sender)
                .receiverUser(receiver)
                .content(rocketContent)
                .isLock(true)
                .isTemp(false)
                .sentAt(LocalDateTime.now())
                .build();
        rocketRepository.save(rocket);

        int order = 1;
        List<String> existingFileNames = rocketRequestDto.getExistingFileNames();
        // 1. 기존 임시 저장 파일 복사
        if (existingFileNames != null && !existingFileNames.isEmpty()) {
            List<RocketFileEntity> tempFiles = rocketFileRepository
                    .findByRocket_SenderUser_UserIdAndUniqueNameIn(userId, existingFileNames);

            for (RocketFileEntity tempFile : tempFiles) {
                // 실제 파일 복사 (optional) - 파일 시스템에 있는 파일 복사하거나, 그대로 사용 가능
                // 여기서 파일 복사가 필요하면 fileService에 복사 메서드 추가
                // 예를 들어: String newSavedPath = fileService.copyFile(tempFile.getSavedPath());

                String newSavedPath = fileService.copyFile(tempFile.getSavedPath());
                String newUniqueName = newSavedPath.substring(newSavedPath.lastIndexOf("/") + 1);

                // 실제 파일 복사 코드가 있으면 여기서 복사 수행 (fileService.copyFile() 같은)

                RocketFileEntity newFile = RocketFileEntity.builder()
                        .rocket(rocket)
                        .isTemp(false)
                        .originalName(tempFile.getOriginalName())
                        .uniqueName(newUniqueName)
                        .savedPath(newSavedPath)
                        .fileType(tempFile.getFileType())
                        .fileSize(tempFile.getFileSize())
                        .fileOrder(order++)
                        .build();

                rocketFileRepository.save(newFile);
            }
        }


        // 2. 새로 업로드한 파일 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String savedPath = fileService.saveRocketFile(file);
                    String uniqueName = savedPath.substring(savedPath.lastIndexOf("/") + 1);

                    RocketFileEntity rocketFile = RocketFileEntity.builder()
                            .rocket(rocket)
                            .isTemp(false)
                            .originalName(file.getOriginalFilename())
                            .uniqueName(uniqueName)
                            .savedPath(savedPath)
                            .fileType(file.getContentType())
                            .fileSize(file.getSize())
                            .fileOrder(order++) // 이어서 순서 부여
                            .build();
                    rocketFileRepository.save(rocketFile);
                }
            }
        }

        // ChestEntity 생성 및 저장
        ReceivedChestEntity chest = ReceivedChestEntity.builder()
                .rocket(rocket)
                .isPublic(false)
                .publicAt(null)
                .isDeleted(false)
                .build();
        receivedChestRepository.save(chest);

        // 보낸 로켓 관리 엔티티 저장
        SentChestEntity rocketSent = SentChestEntity.builder()
                .rocket(rocket)
                .isDeleted(false)
                .build();
        sentChestRepository.save(rocketSent);
    }

    // 로켓 임시저장
    @Transactional
    public void saveTempRocket(Long userId, RocketRequestDto rocketRequestDto, List<MultipartFile> files) throws IOException {
        String rocketName = rocketRequestDto.getRocketName();
        String rocketDesign = rocketRequestDto.getDesign();
        LocalDateTime rocketLockExpiredAt = rocketRequestDto.getLockExpiredAt();
        String rocketReceiverType = rocketRequestDto.getReceiverType();
        String rocketReceiverEmail = rocketRequestDto.getReceiverEmail();
        String rocketContent = rocketRequestDto.getContent();

        UserEntity sender = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("송신 회원을 찾을 수 없습니다."));

        UserEntity receiver = userRepository.findByEmail(rocketReceiverEmail)
                .orElse(null);

        // 기존 임시 저장 로켓이 있는지 확인
        Optional<RocketEntity> existingTemp = rocketRepository
                .findBySenderUser_UserIdAndIsTemp(userId, true);

        RocketEntity tempRocket = existingTemp
                .orElseGet(() -> new RocketEntity());
        if (existingTemp.isPresent()) {
            log.info("기존 임시저장 로켓을 업데이트합니다.");
        } else {
            log.info("새로운 임시저장 로켓을 생성합니다.");
        }
        // 값 세팅 (새로 만들든, 기존 걸 업데이트하든)
        tempRocket.setRocketName(rocketName);
        tempRocket.setDesign(rocketDesign);
        tempRocket.setLockExpiredAt(rocketLockExpiredAt);
        tempRocket.setReceiverType(rocketReceiverType);
        tempRocket.setSenderUser(sender);
        tempRocket.setReceiverUser(receiver);
        tempRocket.setContent(rocketContent);
        tempRocket.setIsLock(null);
        tempRocket.setIsTemp(true);
        tempRocket.setSentAt(null);
        tempRocket.setTempCreatedAt(LocalDateTime.now());
        rocketRepository.save(tempRocket); // insert or update

        // 파일 임시 저장 로직
        if (files != null && !files.isEmpty()) {
            // 기존 임시 파일 삭제 (있는 경우)
            rocketFileRepository.deleteByRocket(tempRocket.getRocketId());

            int order = 1;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // 파일 저장 (파일 시스템에 저장하고 경로 얻기)
                    String savedPath = fileService.saveRocketFile(file);  // 예: /upload/uuid_filename.png
                    String uniqueName = savedPath.substring(savedPath.lastIndexOf("/") + 1); // 예: uuid_filename.png

                    RocketFileEntity rocketFile = RocketFileEntity.builder()
                            .rocket(tempRocket)  // 로켓과 연관
                            .isTemp(true)
                            .originalName(file.getOriginalFilename())
                            .uniqueName(uniqueName)
                            .savedPath(savedPath)
                            .fileType(file.getContentType())
                            .fileSize(file.getSize())
                            .fileOrder(order++)
                            .build();
                    rocketFileRepository.save(rocketFile);
                }
            }
        }
    }

    // 로켓 임시저장 불러오기
    public RocketResponse getTempRocket(Long userId) {
        RocketEntity findEntity = rocketRepository.findBySenderUser_UserIdAndIsTemp(userId, true)
                .orElseThrow(() -> new RocketNotFoundException("해당 회원은 임시 저장된 로켓이 존재하지 않습니다."));

        String receiverEmail = findEntity.getReceiverUser() != null
                ? findEntity.getReceiverUser().getEmail()
                : null;

        // RocketFileEntity 리스트 조회
        List<RocketFileResponse> fileResponses = findEntity.getRocketFiles() // assuming RocketEntity has List<RocketFileEntity> rocketFiles
                .stream()
                .map(file -> RocketFileResponse.builder()
                        .originalName(file.getOriginalName())
                        .uniqueName(file.getUniqueName())
                        .savedPath(file.getSavedPath())
                        .fileType(file.getFileType())
                        .fileSize(file.getFileSize())
                        .fileOrder(file.getFileOrder())
                        .build())
                .collect(Collectors.toList());

        return RocketResponse.builder()
                .rocketName(findEntity.getRocketName())
                .design(findEntity.getDesign())
                .lockExpiredAt(findEntity.getLockExpiredAt())
                .receiverType(findEntity.getReceiverType())
                .receiverEmail(receiverEmail)
                .content(findEntity.getContent())
                .files(fileResponses)
                .build();
    }

    public void unlockRocket(Long userId, Long rocketId) {
        RocketEntity findEntity = rocketRepository.findByRocketIdAndIsLockTrue(rocketId)
                .orElseThrow(()-> new RocketNotFoundException("해당 로켓은 존재하지 않거나 이미 잠금이 해제된 로켓입니다."));

        if (!findEntity.getReceiverUser().getUserId().equals(userId)) {
            throw new IllegalStateException("해당 로켓에 대한 권한이 없습니다.");
        }

        // 잠금 해제 가능 조건: lockExpiredAt가 현재 시각 이전 또는 같음
        if (findEntity.getLockExpiredAt().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("해당 로켓의 잠금 해제일이 아직 지나지 않았습니다.");
        }

        // 잠금 해제 수행
        findEntity.setIsLock(false); // '잠금 해제' 상태로 명시적으로 설정
        rocketRepository.save(findEntity);
    }
}
