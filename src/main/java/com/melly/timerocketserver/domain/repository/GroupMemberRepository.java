package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupEntity;
import com.melly.timerocketserver.domain.entity.GroupMemberEntity;
import com.melly.timerocketserver.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity,Long> {
    boolean existsByGroupAndUser(GroupEntity group, UserEntity user);

    Optional<GroupMemberEntity> findByGroupAndUser(GroupEntity group, UserEntity user);
}
