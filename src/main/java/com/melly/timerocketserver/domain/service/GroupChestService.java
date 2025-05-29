package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.GroupChestPageResponse;
import com.melly.timerocketserver.domain.dto.response.ReceivedChestPageResponse;
import com.melly.timerocketserver.domain.entity.GroupChestEntity;
import com.melly.timerocketserver.domain.repository.GroupChestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupChestService {
    private final GroupChestRepository groupChestRepository;

    public GroupChestService(GroupChestRepository groupChestRepository) {
        this.groupChestRepository = groupChestRepository;
    }

    public GroupChestPageResponse getGroupChestList(Long userId, String groupRocketName, Pageable pageable) {
        // 그룹 로켓 이름이 존재하면 이름으로 검색, 없으면 전체 조회
        Page<GroupChestEntity> findEntity;
        if (groupRocketName != null && !groupRocketName.isBlank()) {
            findEntity = groupChestRepository.findByGroupRocket_ReceiverUser_UserIdAndGroupRocket_RocketNameContaining(userId, groupRocketName, pageable);
        } else {
            findEntity = groupChestRepository.findByGroupRocket_ReceiverUser_UserId(userId, pageable);
        }

        List<GroupChestPageResponse.GroupChestDto> groupChestDtoList = findEntity.getContent().stream()
                .map(entity -> GroupChestPageResponse.GroupChestDto.builder()
                        .groupChestId(entity.getGroupChestId())
                        .groupRocketId(entity.getGroupRocket().getGroupRocketId())
                        .groupId(entity.getGroupRocket().getGroup().getGroupId())
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
}
