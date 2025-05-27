package com.melly.timerocketserver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_theme_tbl")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupThemeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_theme_id")
    private Long groupThemeId;
    private String theme;
}
