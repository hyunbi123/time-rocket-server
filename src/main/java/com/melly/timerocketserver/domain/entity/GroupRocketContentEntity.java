package com.melly.timerocketserver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "group_rocket_content_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRocketContentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grc_id")
    private Long grcId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_rocket_id")
    private GroupRocketEntity groupRocket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "content")
    private String content;

    @Column(name = "rocket_round")
    private Integer rocketRound;

    @Column(name = "is_ready")
    private Boolean ready;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @ManyToMany
    @JoinTable(
            name = "group_rocket_content_file_tbl",
            joinColumns = @JoinColumn(name = "grc_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    @Builder.Default
    private Set<RocketFileEntity> files = new HashSet<>();

    // 연관관계 편의 메서드 (양방향 연관관계에서, 양쪽 컬렉션을 동기화해줌)
    public void addFile(RocketFileEntity file) {
        files.add(file);
        file.getGroupRocketContents().add(this);
    }

    public void removeFile(RocketFileEntity file) {
        files.remove(file);
        file.getGroupRocketContents().remove(this);
    }
}