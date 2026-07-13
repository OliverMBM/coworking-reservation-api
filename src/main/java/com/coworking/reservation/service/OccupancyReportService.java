package com.coworking.reservation.service;

import com.coworking.reservation.dto.response.OccupancyReportResponse;
import com.coworking.reservation.entity.Reservation;
import com.coworking.reservation.entity.Space;
import com.coworking.reservation.entity.enums.ReservationStatus;
import com.coworking.reservation.exception.InvalidReservationPeriodException;
import com.coworking.reservation.repository.ReservationRepository;
import com.coworking.reservation.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OccupancyReportService {

    private static final int PERCENTAGE_SCALE = 2;

    private final SpaceRepository spaceRepository;
    private final ReservationRepository reservationRepository;

    @Cacheable(
            value = "occupancyReports",
            key = "#startTime.toString() + ':' + #endTime.toString()"
    )
    @Transactional(readOnly = true)
    public List<OccupancyReportResponse> getOccupancyReport(
            LocalDateTime startTime,
            LocalDateTime endTime
    ){
        validatePeriod(startTime, endTime);

        List<Space> spaces = spaceRepository.findByActiveTrue();

        List<Reservation> confirmedReservations =
                reservationRepository.findByStatusOverlappingPeriod(
                        ReservationStatus.CONFIRMED,
                        startTime,
                        endTime
                );

        long totalRangeMinutes = Duration.between(startTime, endTime)
                .toMinutes();

        return spaces.stream()
                .map(space -> buildOccupancyResponse(
                        space,
                        confirmedReservations,
                        startTime,
                        endTime,
                        totalRangeMinutes
                ))
                .toList();
    }

    private OccupancyReportResponse buildOccupancyResponse(
            Space space,
            List<Reservation> confirmedReservations,
            LocalDateTime reportStart,
            LocalDateTime reportEnd,
            long totalRangeMinutes
    ){
        long occupiedMinutes = confirmedReservations.stream()
                .filter(reservation ->
                        reservation.getSpace().getId().equals(space.getId())
                )
                .mapToLong(reservation ->
                        calculateOverlappingMinutes(
                                reservation,
                                reportStart,
                                reportEnd
                        )
                )
                .sum();

        BigDecimal occupancyPercentage = calculatePercentage(
                occupiedMinutes,
                totalRangeMinutes
        );

        return new OccupancyReportResponse(
                space.getId(),
                space.getName(),
                space.getType(),
                occupancyPercentage
        );
    }

    private long calculateOverlappingMinutes(
            Reservation reservation,
            LocalDateTime reportStart,
            LocalDateTime reportEnd
    ){
        LocalDateTime effectiveStart = reservation.getStartTime()
                .isAfter(reportStart)
                ? reservation.getStartTime()
                :reportStart;

        LocalDateTime effectiveEnd = reservation.getEndTime()
                .isBefore(reportEnd)
                ? reservation.getEndTime()
                : reportEnd;

        return Duration.between(effectiveStart, effectiveEnd).toMinutes();
    }

    private BigDecimal calculatePercentage(
            long occupiedMinutes,
            long totalRangeMinutes
    ) {
        if (totalRangeMinutes <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(occupiedMinutes)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(totalRangeMinutes),
                        PERCENTAGE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private void validatePeriod(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidReservationPeriodException(
                    "End time must be after start time"
            );
        }
    }
}
