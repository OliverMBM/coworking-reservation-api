package com.coworking.reservation.service.pricing;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public final class BillableHoursCalculator {

    private BillableHoursCalculator(){}

    public static BigDecimal calculate(
            LocalDateTime startTime,
            LocalDateTime endTime
    ){
        long minutes = Duration.between(startTime, endTime).toMinutes();

        long billableHours = Math.max(1, (long) Math.ceil(minutes / 60.0));

        return BigDecimal.valueOf(billableHours);
    }
}
