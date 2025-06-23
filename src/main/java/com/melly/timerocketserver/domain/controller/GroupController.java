package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.*;
import com.melly.timerocketserver.domain.dto.response.*;
import com.melly.timerocketserver.domain.service.GroupService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import com.melly.timerocketserver.websocket.dto.request.RocketConfigRequest;
import com.melly.timerocketserver.websocket.dto.response.GroupChatMsgResponse;
import com.melly.timerocketserver.websocket.service.GroupChatService;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/groups")
public class GroupController implements ResponseController {
    private final GroupService groupService;

    public GroupController(GroupService groupService){
        this.groupService = groupService;
    }

    // 모임 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> createGroup(@Validated @RequestPart(value = "data") CreateGroupRequest createGroupRequest,
                                                   @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        groupService.createGroup(getCurrentUserId(), createGroupRequest, file);
        return makeResponseEntity(HttpStatus.CREATED, "모임이 성공적으로 생성되었습니다.", null);
    }

    // 모임 조회
    @GetMapping()
    public ResponseEntity<ResponseDto> getGroupList(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(name = "group-name", defaultValue = "") String groupName,
                                                    @RequestParam(name = "group-theme", defaultValue = "") String theme){
        // 음수 혹은 0 페이지 방지 (최소 1 페이지부터 시작, 음수를 넣어도 1부터 시작)
        page = Math.max(page, 1);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("groupId").descending());

        GroupPageResponse groupList = groupService.getGroupList(pageable, groupName, theme);

        return makeResponseEntity(HttpStatus.OK, "모임의 목록을 조회하는데 성공했습니다.", groupList);
    }

    // 본인이 참여한 모임 조회
    @GetMapping("/me")
    public ResponseEntity<ResponseDto> getMyGroups(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(name = "group-name", defaultValue = "") String groupName,
                                                   @RequestParam(name = "group-theme", defaultValue = "") String theme) {
        page = Math.max(page, 1);
        size = Math.max(size, 1);
        Pageable pageable = PageRequest.of(page - 1, size);

        MyGroupPageResponse myGroups = groupService.getMyGroups(getCurrentUserId(), pageable, groupName, theme);

        return makeResponseEntity(HttpStatus.OK, "내가 참여한 모임 목록 조회에 성공했습니다.", myGroups);
    }

    // 모임 상세 조회
    @GetMapping("/{groupId}")
    public ResponseEntity<ResponseDto> getGroupDetail(@PathVariable @Min(value = 1, message = "groupId는 1 이상이어야 합니다.") Long groupId){
        GroupDetailResponse groupDetail = groupService.getGroupDetail(groupId);
        return makeResponseEntity(HttpStatus.OK, "해당 모임을 상세 조회하는데 성공했습니다.", groupDetail);
    }

    // 모임 참가
    @PostMapping("/{groupId}/members")
    public ResponseEntity<ResponseDto> joinGroup(@PathVariable @Min(value = 1, message = "groupId는 1 이상이어야 합니다.") Long groupId,
                                                 @RequestBody(required = false) JoinGroupPasswordRequest request){
        groupService.joinGroup(groupId, getCurrentUserId(), request);
        return makeResponseEntity(HttpStatus.OK, "해당 모임 참석에 성공했습니다.", null);
    }

    // 모임 퇴장
    @DeleteMapping("/{groupId}/members/me")
    public ResponseEntity<ResponseDto> leaveGroup(@PathVariable @Min(value = 1, message = "groupId는 1 이상이어야 합니다.") Long groupId) {
        groupService.leaveGroup(groupId, getCurrentUserId());
        return makeResponseEntity(HttpStatus.OK, "모임을 성공적으로 퇴장했습니다.", null);
    }

    // 모임 참여자 확인
    @GetMapping("/{groupId}/members")
    public ResponseEntity<ResponseDto> getGroupMemberList(@PathVariable @Min(value = 1, message = "groupId는 1 이상이어야 합니다.") Long groupId){
        GroupMemberListResponse groupMemberList = groupService.getGroupMemberList(groupId, getCurrentUserId());
        return makeResponseEntity(HttpStatus.OK, "해당 모임의 참여자 목록 조회를 성공했습니다.", groupMemberList);
    }

    // 모임 참여자 강퇴
    @PatchMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ResponseDto> kickGroupMember(@PathVariable @Min(value = 1, message = "groupId는 1 이상이어야 합니다.") Long groupId,
                                                       @PathVariable @Min(value = 1, message = "userId는 1 이상이어야 합니다.") Long userId){
        groupService.kickGroupMember(groupId, userId, getCurrentUserId());
        return makeResponseEntity(HttpStatus.OK, "해당 참여 회원이 강제 퇴장 처리되었습니다.", null);
    }

    // 모임 로켓 컨텐츠 준비 및 저장
    @PostMapping("/{groupId}/rockets/contents")
    public ResponseEntity<ResponseDto> readyGroupRocketContent(@PathVariable @Min(value = 1, message = "groupId는 1 이상이어야 합니다.") Long groupId,
                                                               @RequestPart("data") GroupContentRequest request,
                                                               @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        groupService.readyGroupRocketContent(groupId, getCurrentUserId(), request, files);
        return makeResponseEntity(HttpStatus.OK, "모임 로켓의 Content 준비를 완료했습니다.", null);
    }

    // 모임 로켓 전송
    @PostMapping("/{groupId}/rockets")
    public ResponseEntity<ResponseDto> sendGroupRocket(@PathVariable @Min(value = 1, message = "groupId는 1 이상이어야 합니다.") Long groupId,
                                                       @RequestBody GroupRocketRequest request){
        groupService.sendGroupRocket(groupId, getCurrentUserId(), request);
        return makeResponseEntity(HttpStatus.OK, "모임 로켓을 성공적으로 전송했습니다.", null);
    }

    // 모임 로켓 잠금 해제
    @PatchMapping("/{groupId}/rockets/{groupRocketId}/unlock")
    public ResponseEntity<ResponseDto> unlockGroupRocket(@PathVariable @Min(value = 1, message = "groupId는 1 이상이어야 합니다.") Long groupId,
                                                         @PathVariable @Min(value = 1, message = "groupRocketId는 1 이상이어야 합니다.") Long groupRocketId){
        groupService.unlockGroupRocket(getCurrentUserId(), groupRocketId);
        return makeResponseEntity(HttpStatus.OK, "모임 로켓의 잠금이 해제되었습니다.", null);
    }

    // 모임 실시간 채팅 히스토리 조회
    @GetMapping("/{groupId}/chats/history")
    public ResponseEntity<ResponseDto> getChatHistory(@PathVariable @Min(value = 1, message = "groupId는 1 이상이어야 합니다.") Long groupId,
                                                      @RequestParam(required = false) Long beforeMessageId,
                                                      @RequestParam(defaultValue = "5") int size) {
        GroupChatHistoryResponse history = groupService.getChatHistory(groupId, beforeMessageId != null ? beforeMessageId : Long.MAX_VALUE, size);
        return makeResponseEntity(HttpStatus.OK, "히스토리 조회 성공", history);
    }

    // 모임 로켓 컨텐츠 준비 해제
    @PatchMapping("/{groupId}/readyStatus")
    public ResponseEntity<ResponseDto> cancelReadyStatus(@PathVariable @Min(value = 1, message = "groupId는 1 이상이어야 합니다.") Long groupId,
                                                         @RequestBody @Validated CancelReadyRequest request){
        groupService.cancelReadyStatus(getCurrentUserId(), groupId, request);
        return makeResponseEntity(HttpStatus.OK, "로켓 컨텐츠 준비를 해제했습니다.", null);
    }

    // 현재 라운드의 모임 로켓 설정 변경
    @PutMapping("/{groupId}/rocket-config")
    public ResponseEntity<?> updateRocketConfig(@PathVariable Long groupId, @RequestBody RocketConfigRequest config) {
        // DB 저장 로직 호출
        groupService.updateRocketConfig(groupId, config);
        return makeResponseEntity(HttpStatus.OK, "로켓 설정을 확정지었습니다.", null);
    }

    // 현재 라운드의 모임 로켓 설정 조회
    @GetMapping("/{groupId}/rocket-config")
    public ResponseEntity<?> getCurrentRocketConfig(@PathVariable Long groupId) {
        RocketConfigResponse config = groupService.getCurrentRocketConfig(groupId);
        return makeResponseEntity(HttpStatus.OK, "현재 라운드의 로켓 설정을 불러왔습니다.", config);
    }

    private Long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser().getUserId();
    }
}
