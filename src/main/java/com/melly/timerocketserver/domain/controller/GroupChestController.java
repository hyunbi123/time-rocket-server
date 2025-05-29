package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.response.GroupChestPageResponse;
import com.melly.timerocketserver.domain.dto.response.ReceivedChestPageResponse;
import com.melly.timerocketserver.domain.service.GroupChestService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/group-chests")
public class GroupChestController implements ResponseController {
    private final GroupChestService groupChestService;

    public GroupChestController(GroupChestService groupChestService){
        this.groupChestService = groupChestService;
    }

    // 모임 로켓 보관함 조회
    @GetMapping()
    public ResponseEntity<ResponseDto> getGroupChestList(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(defaultValue = "groupChestId") String sort,
                                                         @RequestParam(defaultValue = "desc") String order,
                                                         @RequestParam(name = "group-rocket-name", defaultValue = "")String groupRocketName){
        // 음수 혹은 0 페이지 방지 (최소 1 페이지부터 시작, 음수를 넣어도 1부터 시작)
        page = Math.max(page, 1);
        size = Math.max(size, 1);

        //  정렬 기준을 설정
        Sort sortBy = Sort.by(Sort.Order.by(sort));
        //  정렬 방향을 설정
        sortBy = order.equalsIgnoreCase("desc") ? sortBy.descending() : sortBy.ascending();

        Pageable pageable = PageRequest.of(page - 1, size, sortBy);

        GroupChestPageResponse chestList = groupChestService.getGroupChestList(getUserId(), groupRocketName, pageable);

        return makeResponseEntity(HttpStatus.OK, "모임 보관함에 저장된 로켓 목록을 불러왔습니다.", chestList);
    }

    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getUserId();
    }
}
