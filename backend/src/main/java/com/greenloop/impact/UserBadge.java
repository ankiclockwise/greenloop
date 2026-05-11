package com.greenloop.impact;

import com.greenloop.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_id"}))
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "badge_id", nullable = false, length = 50)
    private String badgeId;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    public static UserBadge earn(User user, String badgeId) {
        UserBadge ub = new UserBadge();
        ub.user = user;
        ub.badgeId = badgeId;
        ub.earnedAt = LocalDateTime.now();
        return ub;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getBadgeId() { return badgeId; }
    public LocalDateTime getEarnedAt() { return earnedAt; }
}
