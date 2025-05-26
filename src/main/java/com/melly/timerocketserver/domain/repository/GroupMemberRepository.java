package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity,Long> {
}
