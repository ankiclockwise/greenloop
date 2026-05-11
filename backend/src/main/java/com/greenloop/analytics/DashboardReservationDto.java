package com.greenloop.analytics;

import java.time.LocalDateTime;

public class DashboardReservationDto {

    private final Long id;
    private final String status;
    private final String pickupCode;
    private final int quantityReserved;
    private final LocalDateTime reservedAt;
    private final LocalDateTime collectedAt;
    private final ReservedByDto reservedBy;

    public static class ReservedByDto {
        private final Long id;
        private final String name;
        private final String email;

        public ReservedByDto(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    public DashboardReservationDto(Long id, String status, String pickupCode, int quantityReserved,
                                   LocalDateTime reservedAt, LocalDateTime collectedAt,
                                   ReservedByDto reservedBy) {
        this.id = id;
        this.status = status;
        this.pickupCode = pickupCode;
        this.quantityReserved = quantityReserved;
        this.reservedAt = reservedAt;
        this.collectedAt = collectedAt;
        this.reservedBy = reservedBy;
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public String getPickupCode() { return pickupCode; }
    public int getQuantityReserved() { return quantityReserved; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public ReservedByDto getReservedBy() { return reservedBy; }
}
