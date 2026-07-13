package com.coworking.reservation.service;

import com.coworking.reservation.dto.request.CreateReservationRequest;
import com.coworking.reservation.dto.response.ReservationResponse;
import com.coworking.reservation.entity.Reservation;
import com.coworking.reservation.entity.Space;
import com.coworking.reservation.entity.User;
import com.coworking.reservation.entity.enums.ReservationStatus;
import com.coworking.reservation.event.ReservationConfirmedEvent;
import com.coworking.reservation.exception.*;
import com.coworking.reservation.mapper.ReservationMapper;
import com.coworking.reservation.repository.ReservationRepository;
import com.coworking.reservation.repository.SpaceRepository;
import com.coworking.reservation.repository.UserRepository;
import com.coworking.reservation.service.payment.PaymentClient;
import com.coworking.reservation.service.payment.PaymentValidationRequest;
import com.coworking.reservation.service.payment.PaymentValidationResponse;
import com.coworking.reservation.service.pricing.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final EnumSet<ReservationStatus> BLOCKING_STATUSES =
            EnumSet.of(
                    ReservationStatus.PENDING_PAYMENT,
                    ReservationStatus.CONFIRMED
            );

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;
    private final PaymentClient paymentClient;
    private final ReservationMapper reservationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @CacheEvict(value = "occupancyReports", allEntries = true)
    @Transactional
    public ReservationResponse create(
            CreateReservationRequest request,
            String userEmail
    ){
        User user = findUserByEmail(userEmail);
        Space space = spaceRepository.findByIdForUpdate(request.spaceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Espacio no encontrado bajo el id: " + request.spaceId()
                        )
                );

        validateReservationPeriod(
                request.startTime(),
                request.endTime()
        );

        validateAvailability(
                space.getId(),
                request.startTime(),
                request.endTime()
        );

        BigDecimal totalPrice = pricingService.calculate(
                space,
                request.startTime(),
                request.endTime()
        );

        Reservation reservation = Reservation.pendingPayment(
                user,
                space,
                request.startTime(),
                request.endTime(),
                totalPrice
        );

        reservationRepository.saveAndFlush(reservation);

        PaymentValidationResponse paymentResponse = paymentClient.validate(
                new PaymentValidationRequest(
                        reservation.getId(),
                        totalPrice,
                        request.paymentMethod()
                )
        );

        if (paymentResponse.approved()) {
            reservation.confirm(paymentResponse.reference());
            eventPublisher.publishEvent(
                    new ReservationConfirmedEvent(reservation.getId())
            );
        }

        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> findMine(
            String userEmail,
            Pageable pageable
    ){
        User user = findUserByEmail(userEmail);

        return reservationRepository
                .findByUserIdWithDetails(user.getId(), pageable)
                .map(reservationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> findAll(Pageable pageable){
        return reservationRepository.findAllWithDetails(pageable)
                .map(reservationMapper::toResponse);
    }

    @CacheEvict(value = "occupancyReports", allEntries = true)
    @Transactional
    public void cancelMine(Long reservationId, String userEmail){
        User user = findUserByEmail(userEmail);

        Reservation reservation = findReservationWithDetails(reservationId);

        if (!reservation.belongsTo(user)){
            throw new ReservationAccessDeniedException(
                    "Solo puedes cancelar tus propias reservaciones"
            );
        }

        cancelReservation(reservation);
    }

    @CacheEvict(value = "occupancyReports", allEntries = true)
    @Transactional
    public void cancelAsAdmin(Long reservationId){
        Reservation reservation = findReservationWithDetails(reservationId);
        cancelReservation(reservation);
    }

    private void validateReservationPeriod(
            LocalDateTime startTime,
            LocalDateTime endTime
    ){
        if (!endTime.isAfter(startTime)){
            throw new InvalidReservationPeriodException(
              "Tiempo de finalizacion debe ser despues del tiempo de inicio"
            );
        }
    }

    private void validateAvailability(
            Long spaceId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ){
        boolean overlappingReservationExists =
                reservationRepository.existsOverlappingReservation(
                        spaceId,
                        startTime,
                        endTime,
                        BLOCKING_STATUSES
                );

        if (overlappingReservationExists){
            throw new OverlappingReservationException(
                    "El espacio seleccionado ya esta reservado para este periodo"
            );
        }
    }

    private void cancelReservation(Reservation reservation){
        if (ReservationStatus.CANCELLED == reservation.getStatus()){
            throw new InvalidReservationStatusException(
                    "La reservacion ya esta cancelada"
            );
        }

        reservation.cancel();
    }

    private User findUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado con el correo: " + email
                        )
                );
    }

    private Reservation findReservationWithDetails(Long id){
        return reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservacion no encontrada con el id: " + id
                        )
                );
    }
}
