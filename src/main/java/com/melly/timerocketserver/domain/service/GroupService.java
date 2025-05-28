package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.CreateGroupRequest;
import com.melly.timerocketserver.domain.dto.request.JoinGroupPasswordRequest;
import com.melly.timerocketserver.domain.dto.response.GroupDetailResponse;
import com.melly.timerocketserver.domain.dto.response.GroupPageResponse;
import com.melly.timerocketserver.domain.entity.GroupEntity;
import com.melly.timerocketserver.domain.entity.GroupMemberEntity;
import com.melly.timerocketserver.domain.entity.GroupThemeEntity;
import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.GroupMemberRepository;
import com.melly.timerocketserver.domain.repository.GroupRepository;
import com.melly.timerocketserver.domain.repository.GroupThemeRepository;
import com.melly.timerocketserver.domain.repository.UserRepository;
import com.melly.timerocketserver.global.exception.GroupJoinConflictException;
import com.melly.timerocketserver.global.exception.GroupNotFoundException;
import com.melly.timerocketserver.global.exception.GroupThemeNotFoundException;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final GroupThemeRepository groupThemeRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository, FileService fileService,
                        GroupThemeRepository groupThemeRepository, GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.groupThemeRepository = groupThemeRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    // 모임 생성
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

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("로그인한 사용자의 정보를 찾을 수 없습니다."));

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
                .currentMemberCount(1)
                .isPrivate(createGroupRequest.getIsPrivate())
                .password(createGroupRequest.getIsPrivate() ? createGroupRequest.getPassword() : null)
                .backgroundImage(backgroundImageUrl)
                .isDeleted(false)
                .build();
        groupRepository.save(groupEntity);

        GroupMemberEntity leaderMember = GroupMemberEntity.builder()
                .group(groupEntity)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .isKicked(false)
                .build();
        groupMemberRepository.save(leaderMember);
    }

    // 모임 조회
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

        if(findEntity == null|| findEntity.isEmpty()){
            throw new GroupNotFoundException("해당 조건에 맞는 모임이 존재하지 않습니다.");
        }

        List<GroupPageResponse.GroupDto> groupDtoList = findEntity.getContent().stream()
                .map(find -> GroupPageResponse.GroupDto.builder()
                        .groupId(find.getGroupId())
                        .groupName(find.getGroupName())
                        .description(find.getDescription())
                        .leaderNickname(find.getLeader().getNickname())
                        .memberLimit(find.getMemberLimit())
                        .currentMemberCount(find.getCurrentMemberCount())
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

    // 모임 상세 조회
    public GroupDetailResponse getGroupDetail(Long groupId) {
        GroupEntity findEntity = groupRepository.findByIsDeletedFalseAndGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 모임은 존재하지 않거나 삭제된 모임입니다."));
        return GroupDetailResponse.builder()
                .groupId(findEntity.getGroupId())
                .groupName(findEntity.getGroupName())
                .description(findEntity.getDescription())
                .leaderNickname(findEntity.getLeader().getNickname())
                .memberLimit(findEntity.getMemberLimit())
                .currentMemberCount(findEntity.getCurrentMemberCount())
                .isPrivate(findEntity.getIsPrivate())
                .backgroundImage(findEntity.getBackgroundImage())
                .build();
    }
    
    // 모임 참가
    @Transactional
    public void joinGroup(Long groupId, Long userId, JoinGroupPasswordRequest request) {
        GroupEntity group = groupRepository.findByIsDeletedFalseAndGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 모임은 존재하지 않거나 삭제된 모임입니다."));

        UserEntity user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("로그인한 사용자의 정보를 찾을 수 없습니다."));

        // 비공개 모임의 경우 비밀번호 체크
        if (group.getIsPrivate()) {
            if (request == null || request.getPassword() == null) {
                throw new IllegalArgumentException("비공개 모임에는 비밀번호가 필요합니다.");
            }
            if (!group.getPassword().equals(request.getPassword())) {
                throw new IllegalArgumentException("비공개 모임 비밀번호가 일치하지 않습니다.");
            }
        }

        Optional<GroupMemberEntity> existingMembership = groupMemberRepository.findByGroupAndUser(group, user);
        if (existingMembership.isPresent()) {
            if (existingMembership.get().isKicked()) {
                throw new GroupJoinConflictException("강퇴된 사용자는 다시 모임에 참여할 수 없습니다.");
            } else {
                throw new GroupJoinConflictException("이미 모임에 참여한 사용자입니다.");
            }
        }

        // 인원 초과 체크
        if (group.getCurrentMemberCount() >= group.getMemberLimit()) {
            throw new GroupJoinConflictException("모임 정원이 초과되어 참여할 수 없습니다.");
        }

        // 인원 수 증가
        group.setCurrentMemberCount(group.getCurrentMemberCount() + 1);
        groupRepository.save(group);

        // 참여 정보 저장
        GroupMemberEntity groupMemberEntity = GroupMemberEntity.builder()
                .group(group)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .isKicked(false)
                .build();
        groupMemberRepository.save(groupMemberEntity);
    }
    
    // 모임 퇴장
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupEntity group = groupRepository.findByIsDeletedFalseAndGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 모임은 존재하지 않거나 삭제된 모임입니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("로그인한 사용자의 정보를 찾을 수 없습니다."));

        // 그룹장인지 확인
        if (group.getLeader().getUserId().equals(userId)) {
            throw new GroupJoinConflictException("그룹장은 모임에서 퇴장할 수 없습니다. 모임 삭제만 가능합니다.");
        }

        GroupMemberEntity groupMember = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new GroupJoinConflictException("해당 모임에 참여한 적이 없습니다."));

        groupMemberRepository.delete(groupMember); // 참여 기록 삭제

        // 인원 수 감소
        group.setCurrentMemberCount(group.getCurrentMemberCount() - 1);
        groupRepository.save(group);
    }
}
