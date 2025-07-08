package com.melly.timerocketserver.domain.group.repository;

import com.melly.timerocketserver.domain.group.entity.GroupThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupThemeRepository extends JpaRepository<GroupThemeEntity,Long> {
    Optional<GroupThemeEntity> findByTheme(String theme);
}
