package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.CreateGroupRequest;
import com.melly.timerocketserver.domain.dto.response.GroupPageResponse;
import com.melly.timerocketserver.domain.entity.GroupEntity;
import com.melly.timerocketserver.domain.entity.GroupThemeEntity;
import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.GroupRepository;
import com.melly.timerocketserver.domain.repository.GroupThemeRepository;
import com.melly.timerocketserver.domain.repository.UserRepository;
import com.melly.timerocketserver.global.exception.GroupThemeNotFoundException;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final GroupThemeRepository groupThemeRepository;
    public GroupService(GroupRepository groupRepository, UserRepository userRepository, FileService fileService,
                        GroupThemeRepository groupThemeRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.groupThemeRepository = groupThemeRepository;
    }

    @Transactional
    public void createGroup(Long userId, CreateGroupRequest createGroupRequest, MultipartFile file) throws IOException {
        // 비공개 그룹일 때 비밀번호 체크
        if (Boolean.TRUE.equals(createGroupRequest.getIsPrivate())) {
            String password = createGroupRequest.getPassword();
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("비공개 그룹일 경우 비밀번호는 필수입니다.");
            }
            if (password.length() < 4 || password.length() > 20) {
                throw new IllegalArgumentException("비밀번호는 4자 이상 20자 이하로 입력해야 합니다.");
            }
        }

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("회원이 존재하지 않습니다."));

        GroupThemeEntity theme = null;
        if (createGroupRequest.getTheme() != null && !createGroupRequest.getTheme().isBlank()) {

            theme = groupThemeRepository.findByTheme(createGroupRequest.getTheme().trim())
                    .orElseThrow(() -> new GroupThemeNotFoundException("해당 모임 주제는 존재하지 않습니다."));
        }

        String backgroundImageUrl = null;
        if (file != null && !file.isEmpty()) {
            backgroundImageUrl = fileService.saveBackgroundImageFile(file);
        }

        GroupEntity groupEntity = GroupEntity.builder()
                .groupName(createGroupRequest.getGroupName())
                .description(createGroupRequest.getDescription())
                .theme(theme)
                .leader(user)
                .memberLimit(createGroupRequest.getMemberLimit())
                .isPrivate(createGroupRequest.getIsPrivate())
                .password(createGroupRequest.getIsPrivate() ? createGroupRequest.getPassword() : null)
                .backgroundImage(backgroundImageUrl)
                .isDeleted(false)
                .build();
        groupRepository.save(groupEntity);
    }

    public GroupPageResponse getGroupList(Pageable pageable, String groupName, String theme) {
        Slice<GroupEntity> findEntity = null;

        if (groupName != null && !groupName.isBlank() && theme != null && !theme.isBlank()) {
            findEntity = groupRepository.findByIsDeletedFalseAndGroupNameContainingAndTheme_Theme(groupName, theme, pageable);
        } else if (groupName != null && !groupName.isBlank()) {
            findEntity = groupRepository.findByIsDeletedFalseAndGroupNameContaining(groupName, pageable);
        } else if (theme != null && !theme.isBlank()) {
            findEntity = groupRepository.findByIsDeletedFalseAndTheme_Theme(theme, pageable);
        } else {
            findEntity = groupRepository.findByIsDeletedFalse(pageable);
        }

        List<GroupPageResponse.GroupDto> groupDtoList = findEntity.getContent().stream()
                .map(find -> GroupPageResponse.GroupDto.builder()
                        .groupId(find.getGroupId())
                        .groupName(find.getGroupName())
                        .description(find.getDescription())
                        .leaderNickname(find.getLeader().getNickname())
                        .memberLimit(find.getMemberLimit())
                        .isPrivate(find.getIsPrivate())
                        .backgroundImage(find.getBackgroundImage())
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

        return GroupPageResponse.builder()
                .groups(groupDtoList)
                .currentPage(findEntity.getNumber())
                .pageSize(findEntity.getSize())
                .hasNext(findEntity.hasNext())
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
