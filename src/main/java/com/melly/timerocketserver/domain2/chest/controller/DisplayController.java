package com.melly.timerocketserver.domain2.chest.controller;

import com.melly.timerocketserver.domain2.chest.dto.request.DisplayLocationMoveRequest;
import com.melly.timerocketserver.domain2.chest.dto.response.DisplayDetailResponse;
import com.melly.timerocketserver.domain2.chest.dto.response.DisplayDto;
import com.melly.timerocketserver.domain2.chest.service.DisplayService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/displays")
public class DisplayController implements ResponseController {
    private final DisplayService displayService;

    public DisplayController(DisplayService displayService) {
        this.displayService = displayService;
    }

    @GetMapping()
    public ResponseEntity<ResponseDto> getDisplayList() {
        List<DisplayDto> displayList = displayService.getDisplayList(getUserId());
        return makeResponseEntity(HttpStatus.OK, "진열장에 저장된 로켓 조회 성공", displayList);
    }

    @GetMapping("/{receivedChestId}")
    public ResponseEntity<ResponseDto> getDisplayDetail(@PathVariable @Min(value = 1, message = "chestId는 1 이상이어야 합니다.") Long receivedChestId){
        DisplayDetailResponse displayDetail = displayService.getDisplayDetail(getUserId(), receivedChestId);
        return makeResponseEntity(HttpStatus.OK, "진열장의 로켓 상세 정보를 불러왔습니다.", displayDetail);
    }

    // 진열장 로켓 배치 이동
    @PatchMapping("/location")
    public ResponseEntity<ResponseDto> moveLocation(@RequestBody DisplayLocationMoveRequest request) {
        displayService.moveLocation(request.getSourceChestId(), request.getTargetChestId(), request.getTargetDisplayLocation(), getUserId());
        return makeResponseEntity(HttpStatus.OK, "진열장의 로켓 배치이동이 완료되었습니다.", null);
    }

    private Long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getUserId();
    }
}
