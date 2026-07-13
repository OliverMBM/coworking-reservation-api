package com.coworking.reservation.service.pricing;

import com.coworking.reservation.entity.enums.SpaceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PricingStrategyResolver {

    private final List<PricingStrategy> strategies;

    public PricingStrategy resolve(SpaceType spaceType){
        return strategies.stream()
                .filter(strategy -> strategy.supports(spaceType))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sin estrategia de precio para tipos de espacios: "
                                + spaceType
                        )
                );
    }
}
