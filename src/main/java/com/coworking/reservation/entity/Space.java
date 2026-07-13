package com.coworking.reservation.entity;

import com.coworking.reservation.entity.enums.SpaceType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "spaces")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SpaceType type;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false, length = 180)
    private String location;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false)
    private Boolean active = true;

    protected Space(){}

    public Space(String name, SpaceType type, Integer capacity, String location, BigDecimal hourlyRate) {
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.location = location;
        this.hourlyRate = hourlyRate;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
