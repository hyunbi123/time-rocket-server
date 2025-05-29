package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupRocketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRocketRepository extends JpaRepository<GroupRocketEntity,Long> {
    boolean existsByGroup_GroupId(Long groupId);
}
