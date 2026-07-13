package com.coworking.reservation.service.pricing;

import com.coworking.reservation.entity.Space;
import com.coworking.reservation.entity.enums.SpaceType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class MeetingRoomPricingStrategy implements PricingStrategy{

    @Override
    public boolean supports(SpaceType spaceType) {
        return SpaceType.MEETING_ROOM == spaceType;
    }

    @Override
    public BigDecimal calculate(
            Space space,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        BigDecimal billableHours = BillableHoursCalculator.calculate(
                startTime,
                endTime
        );

        return space.getHourlyRate().multiply(billableHours);
    }
}
