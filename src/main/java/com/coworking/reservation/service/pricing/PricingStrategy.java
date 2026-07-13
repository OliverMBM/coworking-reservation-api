package com.coworking.reservation.service.pricing;

import com.coworking.reservation.entity.Space;
import com.coworking.reservation.entity.enums.SpaceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PricingStrategy {

    boolean supports(SpaceType spaceType);

    BigDecimal calculate(
            Space space,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
