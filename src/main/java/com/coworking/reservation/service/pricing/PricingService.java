package com.coworking.reservation.service.pricing;

import com.coworking.reservation.entity.Space;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingStrategyResolver strategyResolver;

    public BigDecimal calculate(
            Space space,
            LocalDateTime startTime,
            LocalDateTime endTime
    ){
        PricingStrategy strategy = strategyResolver.resolve(space.getType());

        return strategy.calculate(space, startTime, endTime);
    }
}
