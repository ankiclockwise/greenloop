package com.greenloop.impact;

import com.greenloop.model.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_impact")
public class UserImpact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "food_donated", nullable = false)
    private int foodDonated = 0;

    @Column(name = "food_received", nullable = false)
    private int foodReceived = 0;

    @Column(name = "donation_count", nullable = false)
    private int donationCount = 0;

    @Column(name = "pickup_count", nullable = false)
    private int pickupCount = 0;

    @Column(name = "completed_pickups", nullable = false)
    private int completedPickups = 0;

    @Column(name = "co2_saved_kg", precision = 10, scale = 2, nullable = false)
    private BigDecimal co2SavedKg = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }

    public static UserImpact forUser(User user) {
        UserImpact impact = new UserImpact();
        impact.user = user;
        return impact;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public int getFoodDonated() { return foodDonated; }
    public void setFoodDonated(int foodDonated) { this.foodDonated = foodDonated; }
    public int getFoodReceived() { return foodReceived; }
    public void setFoodReceived(int foodReceived) { this.foodReceived = foodReceived; }
    public int getDonationCount() { return donationCount; }
    public void setDonationCount(int donationCount) { this.donationCount = donationCount; }
    public int getPickupCount() { return pickupCount; }
    public void setPickupCount(int pickupCount) { this.pickupCount = pickupCount; }
    public int getCompletedPickups() { return completedPickups; }
    public void setCompletedPickups(int completedPickups) { this.completedPickups = completedPickups; }
    public BigDecimal getCo2SavedKg() { return co2SavedKg; }
    public void setCo2SavedKg(BigDecimal co2SavedKg) { this.co2SavedKg = co2SavedKg; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
