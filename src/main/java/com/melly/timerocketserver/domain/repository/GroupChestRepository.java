package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupChestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupChestRepository extends JpaRepository<GroupChestEntity,Long> {
    Page<GroupChestEntity> findByIsDeletedFalseAndGroupRocket_ReceiverUser_UserIdAndGroupRocket_RocketNameContaining(Long userId, String groupRocketName, Pageable pageable);
    Page<GroupChestEntity> findByIsDeletedFalseAndGroupRocket_ReceiverUser_UserId(Long userId, Pageable pageable);

    Optional<GroupChestEntity> findByGroupChestIdAndIsDeletedFalseAndGroupRocket_ReceiverUser_UserId(Long groupChestId, Long userId);

//    int countByGroupRocket_ReceiverUser_UserIdAndIsPublicTrueAndIsDeletedFalse(Long userId);

//    // displayLocation 조회 - 수신자 ID 기준, 삭제되지 않은 보관함 대상
//    @Query("SELECT g.displayLocation FROM GroupChestEntity g " +
//            "WHERE g.groupRocket.receiverUser.userId = :userId AND g.isPublic = true AND g.isDeleted = false")
//    List<Long> findDisplayLocationsByUserIdAndIsPublicTrueAndIsDeletedFalse(@Param("userId") Long userId);

}
