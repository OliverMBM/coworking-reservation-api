package com.coworking.reservation.service.pricing;

import com.coworking.reservation.entity.Space;
import com.coworking.reservation.entity.enums.SpaceType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PrivateOfficePricingStrategyTest {

    private final PrivateOfficePricingStrategy strategy =
            new PrivateOfficePricingStrategy();

    @Test
    void calculateShouldApplyDiscountForLongStay() {
        Space space = new Space(
                "Oficina Privada A",
                SpaceType.PRIVATE_OFFICE,
                4,
                "Tercer piso",
                BigDecimal.valueOf(20)
        );

        BigDecimal total = strategy.calculate(
                space,
                LocalDateTime.of(2026, 7, 12, 8, 0),
                LocalDateTime.of(2026, 7, 12, 16, 0)
        );

        assertThat(total).isEqualByComparingTo("144.00");
    }

    @Test
    void calculateShouldChargeAtLeastOneHour() {
        Space space = new Space(
                "Oficina Privada A",
                SpaceType.PRIVATE_OFFICE,
                4,
                "Tercer piso",
                BigDecimal.valueOf(20)
        );

        BigDecimal total = strategy.calculate(
                space,
                LocalDateTime.of(2026, 7, 12, 8, 0),
                LocalDateTime.of(2026, 7, 12, 8, 30)
        );

        assertThat(total).isEqualByComparingTo("20.00");
    }
}
