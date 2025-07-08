package com.melly.timerocketserver.domain.group.entity;

import com.melly.timerocketserver.domain.rocket.entity.RocketFileEntity;
import com.melly.timerocketserver.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "group_rocket_tbl")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupRocketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_rocket_id")
    private Long groupRocketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    @Column(name = "rocket_round")
    private Integer rocketRound;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private UserEntity receiverUser;

    @Column(name="rocket_name")
    private String rocketName;

    private String design;

    @Column(name = "is_lock")
    private Boolean isLock;

    @Column(name = "lock_expired_at")
    private LocalDateTime lockExpiredAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @OneToMany(mappedBy = "groupRocket", cascade = CascadeType.ALL)
    private List<GroupRocketContentEntity> grc;

    @OneToMany(mappedBy = "groupRocket", cascade = CascadeType.ALL)
    private List<RocketFileEntity> groupRocketFiles;

}
