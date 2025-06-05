package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupChatMsgEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatMsgRepository extends JpaRepository<GroupChatMsgEntity,Long> {

}
