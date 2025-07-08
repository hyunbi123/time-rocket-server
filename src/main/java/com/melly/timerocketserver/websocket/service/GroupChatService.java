package com.melly.timerocketserver.websocket.service;

import com.melly.timerocketserver.domain.entity.GroupChatMsgEntity;
import com.melly.timerocketserver.domain.entity.GroupEntity;
import com.melly.timerocketserver.domain2.user.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.GroupChatMsgRepository;
import com.melly.timerocketserver.domain.service.GroupService;
import com.melly.timerocketserver.domain2.user.service.UserService;
import com.melly.timerocketserver.websocket.dto.GroupChatNotificationDto;
import com.melly.timerocketserver.websocket.dto.request.GroupChatMsgRequest;
import com.melly.timerocketserver.websocket.dto.response.GroupChatMsgResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GroupChatService {
    private final UserService userService;
    private final GroupService groupService;
    private final GroupChatMsgRepository chatRepository;


    public GroupChatService(UserService userService, GroupChatMsgRepository chatRepository,
                            GroupService groupService) {
        this.userService = userService;
        this.chatRepository = chatRepository;
        this.groupService = groupService;
    }

    // 모임 실시간 채팅 전송
    public GroupChatMsgResponse processAndSendMessage(Long groupId, Long userId, GroupChatMsgRequest request) {
        // 닉네임 조회
        String nickname = userService.findNicknameByUserId(userId);

        // 메시지 저장
        saveChatMessage(groupId, userId, request.getMessage());

        // 응답 DTO 생성
        GroupChatMsgResponse response = new GroupChatMsgResponse();
        response.setUserId(userId);
        response.setNickname(nickname);
        response.setMessage(request.getMessage());
        response.setSentAt(LocalDateTime.now());

        return response;
    }

    private void saveChatMessage(Long groupId, Long userId, String message) {
        GroupChatMsgEntity entity = new GroupChatMsgEntity();
        GroupEntity group = groupService.findByIsDeletedFalseAndGroupId(groupId);
        UserEntity user = userService.findByUserId(userId);
        entity.setGroup(group);
        entity.setUser(user);
        entity.setMessage(message);
        entity.setSentAt(LocalDateTime.now());
        chatRepository.save(entity);
    }

    // 모임 참여 후 입장 메시지
    public GroupChatNotificationDto createEnterMessage(Long groupId, Long currentUserId) {
        UserEntity findEntity = userService.findByUserId(currentUserId);
        String nickname = findEntity.getNickname();

        GroupChatNotificationDto dto = new GroupChatNotificationDto();
        dto.setGroupId(groupId);
        dto.setUserId(findEntity.getUserId());
        dto.setNickname(nickname);
        dto.setMessage(nickname + "님이 모임에 참여했습니다.");

        return dto;
    }

    // 모임 퇴장 메시지
    public GroupChatNotificationDto createExitMessage(Long groupId, Long currentUserId) {
        UserEntity findEntity = userService.findByUserId(currentUserId);
        String nickname = findEntity.getNickname();

        GroupChatNotificationDto dto = new GroupChatNotificationDto();
        dto.setGroupId(groupId);
        dto.setUserId(findEntity.getUserId());
        dto.setNickname(nickname);
        dto.setMessage(nickname + "님이 모임에서 나갔습니다.");

        return dto;
    }
}
