package com.melly.timerocketserver.domain.group.repository;

import com.melly.timerocketserver.domain.group.entity.GroupChatMsgEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatMsgRepository extends JpaRepository<GroupChatMsgEntity,Long> {
    Slice<GroupChatMsgEntity> findByGroup_GroupIdAndChatMessageIdLessThanOrderByChatMessageIdDesc(Long groupId, Long beforeMessageId, Pageable pageable);
}
