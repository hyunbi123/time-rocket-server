package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupChestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupChestRepository extends JpaRepository<GroupChestEntity,Long> {

}
