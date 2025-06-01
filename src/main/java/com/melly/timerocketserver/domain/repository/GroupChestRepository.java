package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupChestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupChestRepository extends JpaRepository<GroupChestEntity,Long> {
    Page<GroupChestEntity> findByGroupRocket_ReceiverUser_UserIdAndGroupRocket_RocketNameContaining(Long userId, String groupRocketName, Pageable pageable);
    Page<GroupChestEntity> findByGroupRocket_ReceiverUser_UserId(Long userId, Pageable pageable);

    Optional<GroupChestEntity> findByGroupChestIdAndIsDeletedFalseAndGroupRocket_ReceiverUser_UserId(Long groupChestId, Long userId);
}
