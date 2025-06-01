package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupRocketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRocketRepository extends JpaRepository<GroupRocketEntity,Long> {
    boolean existsByGroup_GroupId(Long groupId);

    @Query(value = "SELECT COALESCE(MAX(rocket_round), 0) FROM group_rocket_tbl WHERE group_id = :groupId", nativeQuery = true)
    int findMaxRocketRoundByGroupId(@Param("groupId") Long groupId);
}
