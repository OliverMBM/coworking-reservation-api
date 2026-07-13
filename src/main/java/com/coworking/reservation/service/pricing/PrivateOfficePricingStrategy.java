package com.coworking.reservation.service.pricing;

import com.coworking.reservation.entity.Space;
import com.coworking.reservation.entity.enums.SpaceType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
public class PrivateOfficePricingStrategy implements PricingStrategy{

    private static final BigDecimal LONG_STAY_DISCOUNT = BigDecimal.valueOf(0.10);
    private static final BigDecimal ONE_HUNDRED_PERCENT = BigDecimal.ONE;

    @Override
    public boolean supports(SpaceType spaceType) {
        return SpaceType.PRIVATE_OFFICE == spaceType;
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

        BigDecimal total = space.getHourlyRate().multiply(billableHours);

        if (billableHours.compareTo(BigDecimal.valueOf(8)) >= 0){
            BigDecimal discountMultiplier =
                    ONE_HUNDRED_PERCENT.subtract(LONG_STAY_DISCOUNT);

            return total.multiply(discountMultiplier)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
