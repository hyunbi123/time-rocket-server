package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupRocketContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRocketContentRepository extends JpaRepository<GroupRocketContentEntity,Long> {
    Optional<GroupRocketContentEntity> findByGroup_GroupIdAndGroupRocketIsNullAndUser_UserId(Long groupId, Long userId);

    boolean existsByGroup_GroupIdAndUser_UserIdAndReadyTrue(Long groupId, Long userId);
    List<GroupRocketContentEntity> findAllByGroup_GroupIdAndUser_UserIdAndReadyTrue(Long groupId, Long userId);
}
