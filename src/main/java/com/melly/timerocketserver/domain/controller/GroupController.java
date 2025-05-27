package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.CreateGroupRequest;
import com.melly.timerocketserver.domain.dto.response.GroupPageResponse;
import com.melly.timerocketserver.domain.service.GroupService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

@Validated
@RestController
@RequestMapping("/api/groups")
public class GroupController implements ResponseController {
    private final GroupService groupService;

    public GroupController(GroupService groupService){
        this.groupService = groupService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> createGroup(@Validated @RequestPart(value = "data") CreateGroupRequest createGroupRequest,
                                                   @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        groupService.createGroup(getUserId(), createGroupRequest, file);
        return makeResponseEntity(HttpStatus.CREATED, "모임이 성공적으로 생성되었습니다.", null);
    }

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


    private Long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser().getUserId();
    }
}
