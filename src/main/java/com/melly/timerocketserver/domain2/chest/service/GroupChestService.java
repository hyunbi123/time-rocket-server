package com.melly.timerocketserver.domain2.chest.service;

import com.melly.timerocketserver.domain.entity.*;
import com.melly.timerocketserver.domain2.chest.dto.response.GroupChestDetailResponse;
import com.melly.timerocketserver.domain2.chest.dto.response.GroupChestPageResponse;
import com.melly.timerocketserver.domain2.chest.dto.response.GroupRocketContentResponse;
import com.melly.timerocketserver.domain2.chest.repository.GroupChestRepository;
import com.melly.timerocketserver.domain.repository.GroupMemberRepository;
import com.melly.timerocketserver.domain2.chest.entity.GroupChestEntity;
import com.melly.timerocketserver.domain2.rocket.dto.response.RocketFileResponse;
import com.melly.timerocketserver.domain2.rocket.entity.RocketFileEntity;
import com.melly.timerocketserver.global.exception.ChestNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GroupChestService {
    private final GroupChestRepository groupChestRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final DisplayService displayService;

    public GroupChestService(GroupChestRepository groupChestRepository, GroupMemberRepository groupMemberRepository,
                             DisplayService displayService) {
        this.groupChestRepository = groupChestRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.displayService = displayService;
    }

    // 고정된 슬롯 리스트
    private static final List<Long> DISPLAY_LOCATIONS = List.of(1L,2L,3L,4L,5L,6L,7L,8L,9L,10L);

    // 모임 로켓 조회
    public GroupChestPageResponse getGroupChestList(Long userId, String groupRocketName, Pageable pageable) {
        // 그룹 로켓 이름이 존재하면 이름으로 검색, 없으면 전체 조회
        Page<GroupChestEntity> findEntity;
        if (groupRocketName != null && !groupRocketName.isBlank()) {
            findEntity = groupChestRepository.findByIsDeletedFalseAndGroupRocket_ReceiverUser_UserIdAndGroupRocket_RocketNameContaining(userId, groupRocketName, pageable);
        } else {
            findEntity = groupChestRepository.findByIsDeletedFalseAndGroupRocket_ReceiverUser_UserId(userId, pageable);
        }

        List<GroupChestPageResponse.GroupChestDto> groupChestDtoList = findEntity.getContent().stream()
                .map(entity -> GroupChestPageResponse.GroupChestDto.builder()
                        .groupChestId(entity.getGroupChestId())
                        .groupRocketId(entity.getGroupRocket().getGroupRocketId())
                        .groupId(entity.getGroupRocket().getGroup().getGroupId())
                        .groupName(entity.getGroupRocket().getGroup().getGroupName())
                        .rocketName(entity.getGroupRocket().getRocketName())
                        .designUrl(entity.getGroupRocket().getDesign())
                        .isLock(Boolean.TRUE.equals(entity.getGroupRocket().getIsLock()))
                        .lockExpiredAt(entity.getGroupRocket().getLockExpiredAt())
                        .isPublic(entity.getIsPublic())
                        .publicAt(entity.getPublicAt())
                        .build())
                .toList();

        // 동적으로 정렬 기준과 정렬 방향을 반환
        String sortBy = findEntity.getSort().stream()
                .map(order -> order.getProperty()) // 정렬 기준 필드명만 추출
                .collect(Collectors.joining(","));

        // 동적으로 정렬 방향을 반환
        String sortDirection = findEntity.getSort().stream()
                .map(order -> order.getDirection().name()) // 정렬 방향 추출 (ASC, DESC)
                .collect(Collectors.joining(","));

        return GroupChestPageResponse.builder()
                .groupChests(groupChestDtoList)
                .currentPage(findEntity.getNumber())
                .pageSize(findEntity.getSize())
                .totalElements(findEntity.getTotalElements())
                .totalPages(findEntity.getTotalPages())
                .first(findEntity.isFirst())
                .last(findEntity.isLast())
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
    
    // 모임 로켓 상세 조회
    public GroupChestDetailResponse getChestDetail(Long userId, Long groupChestId) {
        GroupChestEntity groupChest = groupChestRepository
                .findByGroupChestIdAndIsDeletedFalseAndGroupRocket_ReceiverUser_UserId(groupChestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("해당 모임 보관함이 없거나 삭제된 상태입니다."));


        GroupRocketEntity rocket = groupChest.getGroupRocket();
        GroupEntity group = rocket.getGroup();  // rocket → group 추출


        boolean isLocked = rocket.getIsLock();

        // 파일, 콘텐츠 변환
        List<GroupRocketContentResponse> contentResponses = rocket.getGrc().stream()
                .map(this::toGroupRocketContentResponse)
                .toList();

        // 2) 모든 콘텐츠의 파일을 모아서 변환
        List<RocketFileEntity> allRocketFiles = rocket.getGrc().stream()
                .flatMap(grc -> grc.getFiles() != null ? grc.getFiles().stream() : Stream.empty())
                .collect(Collectors.toList());

        List<RocketFileResponse> fileResponses = toRocketFileResponseList(allRocketFiles);

        // 3) 빌더 세팅
        GroupChestDetailResponse.GroupChestDetailResponseBuilder builder = GroupChestDetailResponse.builder()
                .groupRocketId(rocket.getGroupRocketId())
                .rocketName(rocket.getRocketName())
                .designUrl(rocket.getDesign())
                .sentAt(rocket.getSentAt())
                .isLocked(isLocked)
                .lockExpiredAt(rocket.getLockExpiredAt());

        if (!isLocked) {
            builder.contents(contentResponses);
            builder.rocketFiles(fileResponses);
        }

        return builder.build();
    }

    private GroupRocketContentResponse toGroupRocketContentResponse(GroupRocketContentEntity entity) {
        return GroupRocketContentResponse.builder()
                .grcId(entity.getGrcId())
                .groupRocketId(entity.getGroupRocket().getGroupRocketId())
                .groupId(entity.getGroup().getGroupId())
                .userId(entity.getUser().getUserId())
                .content(entity.getContent())
                .isReady(entity.getReady())
                .isDeleted(entity.getIsDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private List<RocketFileResponse> toRocketFileResponseList(List<RocketFileEntity> entities) {
        if (entities == null) return List.of();

        return entities.stream()
                .map(file -> RocketFileResponse.builder()
                        .fileId(file.getFileId())
                        .originalName(file.getOriginalName())
                        .uniqueName(file.getUniqueName())
                        .savedPath(file.getSavedPath())
                        .fileType(file.getFileType())
                        .fileSize(file.getFileSize())
                        .fileOrder(file.getFileOrder())
                        .uploadedAt(file.getUploadedAt())
                        .build())
                .collect(Collectors.toList());
    }

//    // 모임 보관함 공개 여부 변경 메서드
//    @Transactional
//    public void toggleVisibility(Long userId, Long groupChestId){
//        GroupChestEntity findChest = groupChestRepository.findByGroupChestIdAndIsDeletedFalseAndGroupRocket_ReceiverUser_UserId(groupChestId, userId)
//                .orElseThrow(() -> new ChestNotFoundException("해당 모임 보관함이 없거나 삭제된 상태입니다."));
//
//        GroupRocketEntity rocket = findChest.getGroupRocket();
//
//        if(rocket.getIsLock()){
//            throw new IllegalStateException("해당 로켓은 잠금이 해제되지 않았습니다.");
//        }
//
//        boolean willBePublic = !findChest.getIsPublic();
//
//        if (willBePublic) {
//            // 공개 처리
//            int publicCount = groupChestRepository.countByGroupRocket_ReceiverUser_UserIdAndIsPublicTrueAndIsDeletedFalse(userId);
//
//            if (publicCount >= 10) {
//                throw new IllegalArgumentException("회원당 진열장에 들어갈 로켓 갯수는 최대 10개입니다.");
//            }
//            findChest.setIsPublic(true);
//            findChest.setPublicAt(LocalDateTime.now());
//            // 공개 시 진열장 위치 배정
//            Long displayLoc = generateNextDisplayLocation(userId);
//            findChest.setDisplayLocation(displayLoc);
//        } else {
//            // 비공개 처리
//            findChest.setIsPublic(false);
//            findChest.setPublicAt(null);
//            findChest.setDisplayLocation(null);
//        }
//
//        groupChestRepository.save(findChest);
//        // 진열장 캐시 갱신
//        displayService.updateDisplayCache(findChest.getGroupRocket().getReceiverUser().getUserId());
//    }
//
//    // 로켓 공개 변환 시 작동하는 진열장 배치 저장 메서드
//    private Long generateNextDisplayLocation(Long userId) {
//        // 현재 사용 중인 위치 조회
//        List<Long> usedLocations = groupChestRepository
//                .findDisplayLocationsByUserIdAndIsPublicTrueAndIsDeletedFalse(userId);
//
//        // 빈 슬롯 찾기
//        for (Long loc : DISPLAY_LOCATIONS) {
//            if (!usedLocations.contains(loc)) {
//                return loc;
//            }
//        }
//
//        throw new IllegalStateException("진열장에 더 이상 로켓을 배치할 수 없습니다. (최대 10개)");
//    }

    // 모임 보관함 로켓 논리 삭제
    @Transactional
    public void softDeleteChest(Long userId, Long groupChestId) {
        GroupChestEntity findChest = groupChestRepository.findByGroupChestIdAndIsDeletedFalseAndGroupRocket_ReceiverUser_UserId(groupChestId, userId)
                .orElseThrow(() -> new ChestNotFoundException("해당 모임 보관함이 없거나 삭제된 상태입니다."));
        // 논리 삭제
        if(!findChest.getIsDeleted()){
            findChest.setIsDeleted(true);
            findChest.setDeletedAt(LocalDateTime.now());
            findChest.setDisplayLocation(null);
            findChest.setIsPublic(false);
            findChest.setPublicAt(null);
        }
        groupChestRepository.save(findChest);
        displayService.updateDisplayCache(findChest.getGroupRocket().getReceiverUser().getUserId());
    }
}
