package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupRocketContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRocketContentRepository extends JpaRepository<GroupRocketContentEntity,Long> {
}
