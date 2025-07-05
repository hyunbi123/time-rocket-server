package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.SentChestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SentChestRepository extends JpaRepository<SentChestEntity,Long> {
    // 송신 보관함 조회 - 삭제x , 다른사람에게 보낸 경우만
    @Query("""
    SELECT s FROM SentChestEntity s
    WHERE s.isDeleted = false
      AND s.rocket.senderUser.userId = :userId
      AND s.rocket.senderUser.userId <> s.rocket.receiverUser.userId
    """)
    Page<SentChestEntity> findAllExcludingSelfSent(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT s FROM SentChestEntity s
    WHERE s.isDeleted = false
      AND s.rocket.senderUser.userId = :userId
      AND s.rocket.senderUser.userId <> s.rocket.receiverUser.userId
      AND s.rocket.rocketName LIKE %:rocketName%
    """)
    Page<SentChestEntity> findAllExcludingSelfSentWithName(@Param("userId") Long userId, @Param("rocketName") String rocketName, Pageable pageable);
    
    // 송신 보관함의 로켓 갯수 조회
    Long countByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId);
    
    // 송신 보관함 조회 - 삭제x , 보관함 id, 로켓은 not null, 로켓의 송신자 id 로
    Optional<SentChestEntity> findByIsDeletedFalseAndSentChestIdAndRocket_SenderUser_UserId(Long sentChestId, Long userId);


}
