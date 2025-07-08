package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.*;
import com.melly.timerocketserver.domain.dto.response.*;
import com.melly.timerocketserver.domain.entity.*;
import com.melly.timerocketserver.domain.repository.*;
import com.melly.timerocketserver.domain2.chest.entity.GroupChestEntity;
import com.melly.timerocketserver.domain2.chest.repository.GroupChestRepository;
import com.melly.timerocketserver.domain2.rocket.entity.RocketFileEntity;
import com.melly.timerocketserver.domain2.rocket.repository.RocketFileRepository;
import com.melly.timerocketserver.domain2.file.service.FileService;
import com.melly.timerocketserver.domain2.user.entity.UserEntity;
import com.melly.timerocketserver.domain2.user.repository.UserRepository;
import com.melly.timerocketserver.global.exception.GroupConflictException;
import com.melly.timerocketserver.global.exception.GroupNotFoundException;
import com.melly.timerocketserver.global.exception.GroupThemeNotFoundException;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import com.melly.timerocketserver.websocket.dto.request.RocketConfigRequest;
import com.melly.timerocketserver.websocket.dto.response.GroupChatMsgResponse;
import com.melly.timerocketserver.websocket.dto.response.JoinedMemberPayload;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final GroupThemeRepository groupThemeRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRocketRepository groupRocketRepository;
    private final GroupRocketContentRepository groupRocketContentRepository;
    private final RocketFileRepository rocketFileRepository;
    private final GroupChestRepository groupChestRepository;
    private final GroupChatMsgRepository groupChatMsgRepository;
    private final GroupRocketConfigRepository groupRocketConfigRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository, FileService fileService,
                        GroupThemeRepository groupThemeRepository, GroupMemberRepository groupMemberRepository,
                        GroupRocketRepository groupRocketRepository, GroupRocketContentRepository groupRocketContentRepository,
                        RocketFileRepository rocketFileRepository, GroupChestRepository groupChestRepository,
                        GroupChatMsgRepository groupChatMsgRepository, GroupRocketConfigRepository groupRocketConfigRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.groupThemeRepository = groupThemeRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupRocketRepository = groupRocketRepository;
        this.groupRocketContentRepository = groupRocketContentRepository;
        this.rocketFileRepository = rocketFileRepository;
        this.groupChestRepository = groupChestRepository;
        this.groupChatMsgRepository = groupChatMsgRepository;
        this.groupRocketConfigRepository = groupRocketConfigRepository;
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

        UserEntity user = userRepository.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("로그인한 사용자의 정보를 찾을 수 없습니다."));

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
                .kicked(false)
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
                        .theme(find.getTheme() != null ? find.getTheme().getTheme() : "미선택")
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

    // 본인이 참여한 모임 조회
    public MyGroupPageResponse getMyGroups(Long currentUserId, Pageable pageable, String groupName, String theme) {
        Slice<GroupEntity> findEntity = groupMemberRepository.findMyGroups(currentUserId, groupName, theme, pageable);

        List<MyGroupPageResponse.GroupDto> groupDtoList = findEntity.getContent().stream()
                .map(find -> MyGroupPageResponse.GroupDto.builder()
                        .groupId(find.getGroupId())
                        .groupName(find.getGroupName())
                        .theme(find.getTheme() != null ? find.getTheme().getTheme() : "미선택")
                        .description(find.getDescription())
                        .leaderNickname(find.getLeader().getNickname())
                        .currentMemberCount(find.getCurrentMemberCount())
                        .memberLimit(find.getMemberLimit())
                        .backgroundImage(find.getBackgroundImage())
                        .createdAt(find.getCreatedAt())
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

        return MyGroupPageResponse.builder()
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
                .theme(findEntity.getTheme().getTheme())
                .description(findEntity.getDescription())
                .leaderNickname(findEntity.getLeader().getNickname())
                .leaderId(findEntity.getLeader().getUserId())
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

        UserEntity user = userRepository.findByUserId(userId).orElseThrow(()-> new UserNotFoundException("로그인한 사용자의 정보를 찾을 수 없습니다."));

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
                throw new GroupConflictException("강퇴된 사용자는 다시 모임에 참여할 수 없습니다.");
            } else {
                throw new GroupConflictException("이미 모임에 참여한 사용자입니다.");
            }
        }

        // 인원 초과 체크
        if (group.getCurrentMemberCount() >= group.getMemberLimit()) {
            throw new GroupConflictException("모임 정원이 초과되어 참여할 수 없습니다.");
        }

        // 인원 수 증가
        group.setCurrentMemberCount(group.getCurrentMemberCount() + 1);
        groupRepository.save(group);

        // 참여 정보 저장
        GroupMemberEntity groupMemberEntity = GroupMemberEntity.builder()
                .group(group)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .kicked(false)
                .build();
        groupMemberRepository.save(groupMemberEntity);
    }
    
    // 모임 퇴장
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupEntity group = groupRepository.findByIsDeletedFalseAndGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 모임은 존재하지 않거나 삭제된 모임입니다."));

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("로그인한 사용자의 정보를 찾을 수 없습니다."));

        // 그룹장인지 확인
        if (group.getLeader().getUserId().equals(userId)) {
            throw new GroupConflictException("그룹장은 모임에서 퇴장할 수 없습니다. 모임 삭제만 가능합니다.");
        }

        GroupMemberEntity groupMember = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new GroupConflictException("해당 모임에 참여한 적이 없습니다."));

        groupMemberRepository.delete(groupMember); // 참여 기록 삭제

        // 인원 수 감소
        group.setCurrentMemberCount(group.getCurrentMemberCount() - 1);
        groupRepository.save(group);
    }

    // 모임 참여자 확인
    public GroupMemberListResponse getGroupMemberList(Long groupId, Long userId) {
        GroupEntity group = groupRepository.findByIsDeletedFalseAndGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 모임은 존재하지 않거나 삭제된 모임입니다."));

        GroupMemberEntity member = groupMemberRepository.findByGroup_GroupIdAndUser_UserId(groupId, userId)
                .orElseThrow(() -> new GroupConflictException("해당 모임에 참여한 적이 없습니다."));

        Integer currentRound = groupRocketRepository.findMaxRoundByGroupId(groupId)
                .orElse(0) + 1;

        List<GroupMemberEntity> members = groupMemberRepository.findByGroup_GroupIdAndKickedFalse(groupId);
        int memberCount = groupMemberRepository.countByGroup_GroupIdAndKickedFalse(groupId);

        Set<Long> readyUserIdSet = new HashSet<>(groupRocketContentRepository.findReadyUserIdsByRound(groupId,currentRound));
        System.out.println("Ready user IDs for round " + currentRound + ": " + readyUserIdSet);
        List<GroupMemberListResponse.MemberDto> memberDtos = members.stream()
                .map(m -> {
                    boolean memberReady = readyUserIdSet.contains(m.getUser().getUserId());
                    return GroupMemberListResponse.MemberDto.builder()
                            .groupMemberId(m.getGroupMemberId())
                            .userId(m.getUser().getUserId())
                            .nickname(m.getUser().getNickname())
                            .isKicked(m.isKicked())
                            .isReady(memberReady)
                            .build();
                })
                .toList();

        // 응답 객체 생성 및 반환
        return GroupMemberListResponse.builder()
                .members(memberDtos)
                .MemberCount(memberCount)
                .MemberLimit(group.getMemberLimit())
                .currentRound(currentRound)
                .build();
    }

    // 참여자 강퇴
    @Transactional
    public void kickGroupMember(Long groupId, Long targetId, Long currentUserId) {
        // 그룹 존재 및 삭제 여부 확인
        GroupEntity group = groupRepository.findByIsDeletedFalseAndGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 모임은 존재하지 않거나 삭제된 모임입니다."));

        // 현재 요청한 사용자가 리더인지 확인
        if (!group.getLeader().getUserId().equals(currentUserId)) {
            throw new GroupConflictException("모임의 리더만 강퇴할 수 있습니다.");
        }

        // 대상 유저가 그룹의 참여자인지 확인
        GroupMemberEntity targetMember = groupMemberRepository.findByGroup_GroupIdAndUser_UserId(groupId, targetId)
                .orElseThrow(() -> new UserNotFoundException("요청한 회원은 해당 모임의 참여자가 아닙니다."));

        // 이미 강퇴된 상태인지 확인
        if (targetMember.isKicked()) {
            throw new GroupConflictException("해당 회원은 이미 모임에서 강퇴된 상태입니다.");
        }

        // 리더 본인을 강퇴하지 못하도록 방어
        if (targetId.equals(currentUserId)) {
            throw new GroupConflictException("자기 자신을 강퇴할 수 없습니다.");
        }

        // 강퇴 처리
        targetMember.setKicked(true);
        groupMemberRepository.save(targetMember);

        group.setCurrentMemberCount(group.getCurrentMemberCount() - 1);
        groupRepository.save(group);
    }

    // 모임 로켓 컨텐츠 준비
    @Transactional
    public void readyGroupRocketContent(Long groupId, Long userId, GroupContentRequest request, List<MultipartFile> files) throws IOException {
        // 현재 최대 라운드 가져오기 (만약 없으면 0으로 처리)
        int maxRound = groupRocketRepository.findMaxRocketRoundByGroupId(groupId);
        int currentRound = maxRound + 1;

        GroupEntity group = groupRepository.findByIsDeletedFalseAndGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 모임은 존재하지 않거나 삭제된 모임입니다."));

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다."));

        // 해당 유저가 모임의 멤버인지 확인
        boolean isMember = groupMemberRepository.existsByGroup_GroupIdAndUser_UserId(groupId, userId);
        if (!isMember) {
            throw new GroupConflictException("해당 유저는 이 모임의 멤버가 아닙니다.");
        }

        GroupRocketContentEntity grc = groupRocketContentRepository
                .findByGroup_GroupIdAndGroupRocketIsNullAndUser_UserId(groupId, userId)
                .orElseGet(() -> GroupRocketContentEntity.builder()
                        .groupRocket(null)
                        .group(group)
                        .user(user)
                        .rocketRound(currentRound)
                        .ready(true)
                        .isDeleted(false)
                        .createdAt(LocalDateTime.now())
                        .build());

        // 기존 데이터가 있어도 rocketRound 업데이트
        grc.setRocketRound(currentRound);

        grc.setContent(request.getContent());
        grc.setReady(true);

        // 파일 저장 전, 기존 파일 연결 제거
        grc.getFiles().clear();

        // 파일 저장
        if (files != null && !files.isEmpty()) {
            int order = 1;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String savedPath = fileService.saveRocketFile(file);
                    String uniqueName = savedPath.substring(savedPath.lastIndexOf("/") + 1);

                    RocketFileEntity rocketFile = RocketFileEntity.builder()
                            .originalName(file.getOriginalFilename())
                            .uniqueName(uniqueName)
                            .savedPath(savedPath)
                            .fileType(file.getContentType())
                            .fileSize(file.getSize())
                            .fileOrder(order++)
                            .build();

                    rocketFileRepository.save(rocketFile);

                    // 연관관계 설정
                    grc.getFiles().add(rocketFile);
                }
            }
        }

        groupRocketContentRepository.save(grc);
    }

    // 준비완료 상태의 모임 로켓 전송
    @Transactional
    public void sendGroupRocket(Long groupId, Long currentUserId, GroupRocketRequest request) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 모임이 존재하지 않습니다."));

        if (!group.getLeader().getUserId().equals(currentUserId)) {
            throw new GroupConflictException("리더만 전송할 수 있습니다.");
        }

        // 그룹 멤버 조회
        List<GroupMemberEntity> groupMembers = groupMemberRepository.findAllByGroup_GroupIdAndKickedFalse(groupId);

        // 모든 멤버가 준비됐는지 먼저 확인 (전송 전에)
        for (GroupMemberEntity member : groupMembers) {
            Long memberId = member.getUser().getUserId();
            boolean hasReadyContent = groupRocketContentRepository
                    .existsByGroup_GroupIdAndUser_UserIdAndReadyTrueAndGroupRocketIsNull(groupId, memberId);

            if (!hasReadyContent) {
                throw new GroupConflictException("모든 멤버가 로켓 콘텐츠를 준비해야 전송할 수 있습니다. 준비되지 않은 멤버 있음: " + member.getUser().getNickname());
            }
        }

        int maxRound = groupRocketRepository.findMaxRocketRoundByGroupId(groupId);
        int newRound = maxRound + 1;


        // 공통 콘텐츠 목록 한 번만 조회
        List<GroupRocketContentEntity> allReadyContents = groupRocketContentRepository
                .findAllByGroup_GroupIdAndRocketRoundAndGroupRocketIsNullAndReadyTrue(groupId, newRound);

        for (GroupMemberEntity member : groupMembers) {
            UserEntity receiver = member.getUser();

            GroupRocketEntity rocket = GroupRocketEntity.builder()
                    .group(group)
                    .rocketRound(newRound)
                    .receiverUser(receiver)
                    .rocketName(request.getRocketName())
                    .design(request.getDesign())
                    .isLock(true)
                    .lockExpiredAt(request.getLockExpiredAt())
                    .sentAt(LocalDateTime.now())
                    .build();
            groupRocketRepository.save(rocket);

            // 콘텐츠 복사해서 각각의 로켓에 연결
            for (GroupRocketContentEntity originalContent : allReadyContents) {
                GroupRocketContentEntity copied = GroupRocketContentEntity.builder()
                        .groupRocket(rocket)
                        .group(originalContent.getGroup())
                        .user(originalContent.getUser())
                        .content(originalContent.getContent())
                        .rocketRound(newRound)
                        .ready(true)
                        .isDeleted(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                groupRocketContentRepository.save(copied);

                // 파일 복사 대신 기존 파일 참조만 연결
                if (originalContent.getFiles() != null) {
                    for (RocketFileEntity originalFile : originalContent.getFiles()) {
                        // 연관관계 편의 메서드 있으면 사용하는 게 좋음
                        copied.addFile(originalFile);
                    }
                }
            }

            // 그룹 보관함에 저장
            GroupChestEntity chest = GroupChestEntity.builder()
                    .groupRocket(rocket)
                    .isPublic(false)
                    .displayLocation(null)
                    .isDeleted(false)
                    .build();
            groupChestRepository.save(chest);
        }
    }

    // groupId 로 삭제되지 않은 모임 조회
    public GroupEntity findByIsDeletedFalseAndGroupId(Long groupId) {
        return groupRepository.findByIsDeletedFalseAndGroupId(groupId).orElseThrow(() -> new GroupNotFoundException("해당 모임은 삭제되었거나 존재하지 않습니다."));
    }

    // 모임 로켓 잠금 해제
    public void unlockGroupRocket(Long currentUserId, Long groupRocketId) {
        GroupRocketEntity findEntity = groupRocketRepository.findByGroupRocketIdAndIsLockTrue(groupRocketId)
                .orElseThrow(() -> new GroupNotFoundException("존재하지 않는 모임 로켓입니다."));

        if (!findEntity.getReceiverUser().getUserId().equals(currentUserId)) {
            throw new IllegalStateException("해당 로켓에 대한 권한이 없습니다.");
        }

        // 잠금 해제 가능 조건: lockExpiredAt가 현재 시각 이전 또는 같음
        if (findEntity.getLockExpiredAt().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("해당 로켓의 잠금 해제일이 아직 지나지 않았습니다.");
        }

        // 모임 로켓 잠금 해제 수행
        findEntity.setIsLock(false); // '잠금 해제' 상태로 명시적으로 설정
        groupRocketRepository.save(findEntity);
    }

    public GroupChatHistoryResponse getChatHistory(Long groupId, Long beforeMessageId, int size) {
        Pageable pageable = PageRequest.of(0, size); // 첫 페이지, size개
        Slice<GroupChatMsgEntity> findEntity = groupChatMsgRepository
                .findByGroup_GroupIdAndChatMessageIdLessThanOrderByChatMessageIdDesc(groupId, beforeMessageId, pageable);

        List<GroupChatMsgResponse> messages = findEntity.stream()
                .map(entity -> GroupChatMsgResponse.builder()
                        .chatMessageId(entity.getChatMessageId())
                        .userId(entity.getUser().getUserId())
                        .nickname(entity.getUser().getNickname())
                        .message(entity.getMessage())
                        .sentAt(entity.getSentAt())
                        .build())
                .toList();

        return new GroupChatHistoryResponse(messages, findEntity.hasNext());
    }

    // 모임 로켓 컨텐츠 준비 해제
    public void cancelReadyStatus(Long currentUserId, Long groupId, CancelReadyRequest request) {
        GroupRocketContentEntity grc = groupRocketContentRepository.findByGroup_GroupIdAndUser_UserIdAndRocketRound(groupId, currentUserId, request.getCurrentRound())
                .orElseThrow(() -> new GroupNotFoundException("존재하지 않는 그룹 로켓 컨텐츠입니다."));
        grc.setReady(request.getIsReady());
        groupRocketContentRepository.save(grc);
    }

    public JoinedMemberPayload getJoinedMemberPayload(Long groupId, Long userId) {
        GroupMemberEntity member = groupMemberRepository.findByGroup_GroupIdAndUser_UserId(groupId, userId)
                .orElseThrow(() -> new UserNotFoundException("그룹에 해당 유저가 없습니다."));

        return new JoinedMemberPayload(
                member.getUser().getUserId(),
                member.getUser().getNickname()
        );
    }

    @Transactional
    public void updateRocketConfig(Long groupId, RocketConfigRequest config) {
        // 그룹 조회
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));

        // 현재 라운드 계산
        int maxRound = groupRocketRepository.findMaxRocketRoundByGroupId(groupId);
        int currentRound = maxRound + 1;

        // 현재 라운드에 해당하는 설정 조회 또는 새로 생성
        Optional<GroupRocketConfigEntity> findEntity = groupRocketConfigRepository
                .findByGroup_GroupIdAndRocketRound(groupId, currentRound);

        GroupRocketConfigEntity configEntity;
        if (findEntity.isPresent()) {
            configEntity = findEntity.get();
            configEntity.setRocketName(config.getRocketName());
            configEntity.setDesign(config.getDesign());
            configEntity.setLockExpiredAt(LocalDateTime.parse(config.getLockExpiredAt()));
        } else {
            configEntity = GroupRocketConfigEntity.builder()
                    .group(group)
                    .rocketRound(currentRound)
                    .rocketName(config.getRocketName())
                    .design(config.getDesign())
                    .lockExpiredAt(LocalDateTime.parse(config.getLockExpiredAt()))
                    .build();
        }

        groupRocketConfigRepository.save(configEntity);
    }

    @Transactional(readOnly = true)
    public RocketConfigResponse getCurrentRocketConfig(Long groupId) {
        int maxRound = groupRocketRepository.findMaxRocketRoundByGroupId(groupId);
        int currentRound = maxRound + 1;

        GroupRocketConfigEntity config = groupRocketConfigRepository
                .findByGroup_GroupIdAndRocketRound(groupId, currentRound)
                .orElseThrow(() -> new RuntimeException("현재 라운드의 설정이 존재하지 않습니다."));

        return RocketConfigResponse.builder()
                .rocketName(config.getRocketName())
                .design(config.getDesign())
                .lockExpiredAt(config.getLockExpiredAt().toString())
                .build();
    }
}
