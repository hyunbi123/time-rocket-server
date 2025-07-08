package com.melly.timerocketserver.domain.group.repository;

import com.melly.timerocketserver.domain.group.entity.GroupRocketConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRocketConfigRepository extends JpaRepository<GroupRocketConfigEntity, Long> {
    Optional<GroupRocketConfigEntity> findByGroup_GroupIdAndRocketRound(Long groupId, int currentRound);
}
