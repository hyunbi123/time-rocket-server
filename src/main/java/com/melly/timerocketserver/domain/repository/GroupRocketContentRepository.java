package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupRocketContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRocketContentRepository extends JpaRepository<GroupRocketContentEntity,Long> {
//    Optional<GroupRocketContentEntity> findByGroup_GroupIdAndUser_UserIdAndIsDeletedFalse(Long groupId, Long userId);
//
//    List<GroupRocketContentEntity> findByGroup_GroupIdAndReadyTrueAndIsDeletedFalse(Long groupId);
}
