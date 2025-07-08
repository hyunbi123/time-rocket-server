package com.melly.timerocketserver.domain.group.repository;

import com.melly.timerocketserver.domain.group.entity.GroupEntity;
import com.melly.timerocketserver.domain.group.entity.GroupMemberEntity;
import com.melly.timerocketserver.domain.user.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity,Long> {
    Optional<GroupMemberEntity> findByGroupAndUser(GroupEntity group, UserEntity user);

    Optional<GroupMemberEntity> findByGroup_GroupIdAndUser_UserId(Long groupId, Long userId);
    List<GroupMemberEntity> findByGroup_GroupIdAndKickedFalse(Long groupId);
    int countByGroup_GroupIdAndKickedFalse(Long groupId);

    List<GroupMemberEntity> findAllByGroup_GroupIdAndKickedFalse(Long groupId);

    boolean existsByGroup_GroupIdAndUser_UserId(Long groupId, Long userId);

    @Query("""
    SELECT gm.group FROM GroupMemberEntity gm
    JOIN gm.group g
    LEFT JOIN g.theme t
    WHERE gm.user.userId = :userId
      AND gm.kicked = false
      AND (g.isDeleted IS NULL OR g.isDeleted = false)
      AND (:groupName = '' OR g.groupName LIKE CONCAT('%', :groupName, '%'))
      AND (
          :theme = '' OR (t IS NULL AND :theme = '') OR t.theme = :theme
      )
    ORDER BY g.groupId DESC
    """)
    Slice<GroupEntity> findMyGroups(
            @Param("userId") Long userId,
            @Param("groupName") String groupName,
            @Param("theme") String theme,
            Pageable pageable
    );
}
