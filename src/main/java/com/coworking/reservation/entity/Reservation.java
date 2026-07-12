package com.coworking.reservation.entity;

import com.coworking.reservation.entity.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "reservations",
        indexes = {
                @Index(
                        name = "idx_reservations_space_period",
                        columnList = "space_id,start_time,end_time"
                ),
                @Index(
                        name = "idx_reservations_user",
                        columnList = "user_id"
                )
        }
)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_reservations_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "space_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_reservations_space")
    )
    private Space space;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Column(length = 120)
    private String paymentReference;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Reservation() {
    }

    private Reservation(
            User user,
            Space space,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal totalPrice
    ) {
        this.user = user;
        this.space = space;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPrice = totalPrice;
        this.status = ReservationStatus.PENDING_PAYMENT;
        this.createdAt = LocalDateTime.now();
    }

    public static Reservation pendingPayment(
            User user,
            Space space,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal totalPrice
    ) {
        return new Reservation(user, space, startTime, endTime, totalPrice);
    }

    public void confirm(String paymentReference) {
        this.status = ReservationStatus.CONFIRMED;
        this.paymentReference = paymentReference;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean belongsTo(User user) {
        return this.user.getId().equals(user.getId());
    }
}