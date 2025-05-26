package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.CreateGroupRequest;
import com.melly.timerocketserver.domain.service.GroupService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ResponseDto> createGroup(@Validated @ModelAttribute CreateGroupRequest createGroupRequest) throws IOException {
        groupService.createGroup(getUserId(), createGroupRequest);
        return makeResponseEntity(HttpStatus.CREATED, "모임이 성공적으로 생성되었습니다.", null);
    }

    private Long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser().getUserId();
    }
}
