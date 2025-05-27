package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.GroupEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    Slice<GroupEntity> findByIsDeletedFalse(Pageable pageable);
    Slice<GroupEntity> findByIsDeletedFalseAndGroupNameContaining(String groupName, Pageable pageable);
    Slice<GroupEntity> findByIsDeletedFalseAndTheme_Theme(String theme, Pageable pageable);
    Slice<GroupEntity> findByIsDeletedFalseAndGroupNameContainingAndTheme_Theme(String groupName, String theme, Pageable pageable);


}
