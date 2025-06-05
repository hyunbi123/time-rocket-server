package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.*;
import com.melly.timerocketserver.domain.entity.GroupChestEntity;
import com.melly.timerocketserver.domain.entity.GroupRocketContentEntity;
import com.melly.timerocketserver.domain.entity.GroupRocketEntity;
import com.melly.timerocketserver.domain.entity.RocketFileEntity;
import com.melly.timerocketserver.domain.repository.GroupChestRepository;
import com.melly.timerocketserver.global.exception.ChestNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GroupChestService {
    private final GroupChestRepository groupChestRepository;

    public GroupChestService(GroupChestRepository groupChestRepository) {
        this.groupChestRepository = groupChestRepository;
    }

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
                .orElseThrow(() -> new ChestNotFoundException("해당 그룹 보관함이 없거나 삭제된 상태입니다."));

        GroupRocketEntity rocket = groupChest.getGroupRocket();
        boolean isLocked = rocket.getIsLock();

        // 파일, 콘텐츠 변환
        List<GroupRocketContentResponse> contentResponses = rocket.getGrc().stream()
                .map(this::toGroupRocketContentResponse)
                .toList();

        // 2) 모든 콘텐츠의 파일을 모아서 변환
        List<RocketFileEntity> allRocketFiles = rocket.getGrc().stream()
                .flatMap(grc -> grc.getRocketFiles() != null ? grc.getRocketFiles().stream() : Stream.empty())
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
}
