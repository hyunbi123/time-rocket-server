package com.melly.timerocketserver.domain.dto.response;

import com.melly.timerocketserver.websocket.dto.response.GroupChatMsgResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GroupChatHistoryResponse {
    private List<GroupChatMsgResponse> messages;
    private boolean hasNext;
}
