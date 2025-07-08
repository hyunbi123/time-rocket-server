package com.melly.timerocketserver.domain2.rocket.repository;

import com.melly.timerocketserver.domain2.rocket.entity.RocketFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RocketFileRepository extends JpaRepository<RocketFileEntity,Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM RocketFileEntity f WHERE f.rocket.rocketId = :rocketId")
    void deleteByRocket(Long rocketId);

    List<RocketFileEntity> findByRocket_SenderUser_UserIdAndUniqueNameIn(Long userId, List<String> existingFileNames);

}
