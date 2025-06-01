package com.melly.timerocketserver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_chest_tbl")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupChestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_chest_id")
    private Long groupChestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_rocket_id")
    private GroupRocketEntity groupRocket;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "public_at")
    private LocalDateTime publicAt;

    @Column(name = "display_location")
    private String displayLocation;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;




}
