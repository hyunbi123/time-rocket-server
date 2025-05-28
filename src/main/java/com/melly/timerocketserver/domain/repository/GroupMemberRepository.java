package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupEntity;
import com.melly.timerocketserver.domain.entity.GroupMemberEntity;
import com.melly.timerocketserver.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity,Long> {
    Optional<GroupMemberEntity> findByGroupAndUser(GroupEntity group, UserEntity user);

    Optional<GroupMemberEntity> findByGroup_GroupIdAndUser_UserId(Long groupId, Long userId);
    List<GroupMemberEntity> findByGroup_GroupIdAndKickedFalse(Long groupId);
    int countByGroup_GroupIdAndKickedFalse(Long groupId);
}
