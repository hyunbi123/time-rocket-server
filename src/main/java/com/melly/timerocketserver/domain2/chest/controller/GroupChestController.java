package com.melly.timerocketserver.domain2.chest.controller;

import com.melly.timerocketserver.domain2.chest.dto.response.GroupChestDetailResponse;
import com.melly.timerocketserver.domain2.chest.dto.response.GroupChestPageResponse;
import com.melly.timerocketserver.domain2.chest.service.GroupChestService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    // 보관함 로켓 상세 조회
    @GetMapping("/{groupChestId}")
    public ResponseEntity<ResponseDto> getGroupChestDetail(@PathVariable @Min(value = 1, message = "groupChestId는 1 이상이어야 합니다.") Long groupChestId){
        GroupChestDetailResponse chestDetail = groupChestService.getChestDetail(getUserId(), groupChestId);
        return makeResponseEntity(HttpStatus.OK, "보관함의 로켓 상세 정보를 불러왔습니다.", chestDetail);
    }

//    // 보관함 로켓 공개 여부 변경
//    @PatchMapping("/{groupChestId}/visibility")
//    public ResponseEntity<ResponseDto> toggleVisibility(@PathVariable @Min(value = 1, message = "groupChestId는 1 이상이어야 합니다.") Long groupChestId){
//        groupChestService.toggleVisibility(getUserId(), groupChestId);
//        return makeResponseEntity(HttpStatus.OK, "로켓의 공개 여부가 변경되었습니다.", null);
//    }

    // 보관함 로켓 논리 삭제
    @PatchMapping("/{groupChestId}/deleted-flag")
    public ResponseEntity<ResponseDto> softDeleteGroupChest(@PathVariable @Min(value = 1, message = "groupChestId는 1 이상이어야 합니다.") Long groupChestId){
        groupChestService.softDeleteChest(getUserId(), groupChestId);
        return makeResponseEntity(HttpStatus.OK, "해당 로켓이 삭제되었습니다.", null);
    }


    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getUserId();
    }
}
