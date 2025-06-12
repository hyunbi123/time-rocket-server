package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupRocketContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRocketContentRepository extends JpaRepository<GroupRocketContentEntity,Long> {
    Optional<GroupRocketContentEntity> findByGroup_GroupIdAndGroupRocketIsNullAndUser_UserId(Long groupId, Long userId);
    boolean existsByGroup_GroupIdAndUser_UserIdAndReadyTrueAndGroupRocketIsNull(Long groupId, Long userId);
    List<GroupRocketContentEntity> findAllByGroup_GroupIdAndGroupRocketIsNullAndReadyTrue(Long groupId);

    @Query("SELECT DISTINCT grc.user.userId " +
            "FROM GroupRocketContentEntity grc " +
            "WHERE grc.group.groupId = :groupId " +
            "AND grc.rocketRound = :round " +
            "AND grc.ready = true " +
            "AND grc.isDeleted = false")
    List<Long> findReadyUserIdsByRound(@Param("groupId") Long groupId, @Param("round") Integer round);

    Optional<GroupRocketContentEntity> findByGroup_GroupIdAndUser_UserIdAndRocketRound(Long groupId, Long currentUserId, Integer round);
}
