package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.CreateGroupRequest;
import com.melly.timerocketserver.domain.entity.GroupEntity;
import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.GroupRepository;
import com.melly.timerocketserver.domain.repository.UserRepository;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository, FileService fileService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    @Transactional
    public void createGroup(Long userId, CreateGroupRequest createGroupRequest) throws IOException {
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

        String backgroundImageUrl = null;
        if (createGroupRequest.getBackgroundImage() != null && !createGroupRequest.getBackgroundImage().isEmpty()) {
            backgroundImageUrl = fileService.saveBackgroundImageFile(createGroupRequest.getBackgroundImage());
        }

        GroupEntity groupEntity = GroupEntity.builder()
                .groupName(createGroupRequest.getGroupName())
                .description(createGroupRequest.getDescription())
                .leader(user)
                .memberLimit(createGroupRequest.getMemberLimit())
                .isPrivate(createGroupRequest.getIsPrivate())
                .password(createGroupRequest.getIsPrivate() ? createGroupRequest.getPassword() : null)
                .backgroundImage(backgroundImageUrl)
                .isDeleted(false)
                .build();
        groupRepository.save(groupEntity);
    }
}
