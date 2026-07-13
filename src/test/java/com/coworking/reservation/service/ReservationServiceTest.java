package com.coworking.reservation.service;

import com.coworking.reservation.dto.request.CreateReservationRequest;
import com.coworking.reservation.dto.response.ReservationResponse;
import com.coworking.reservation.entity.Reservation;
import com.coworking.reservation.entity.Space;
import com.coworking.reservation.entity.UserAccount;
import com.coworking.reservation.entity.enums.ReservationStatus;
import com.coworking.reservation.entity.enums.Role;
import com.coworking.reservation.entity.enums.SpaceType;
import com.coworking.reservation.event.ReservationConfirmedEvent;
import com.coworking.reservation.exception.OverlappingReservationException;
import com.coworking.reservation.mapper.ReservationMapper;
import com.coworking.reservation.repository.ReservationRepository;
import com.coworking.reservation.repository.SpaceRepository;
import com.coworking.reservation.repository.UserAccountRepository;
import com.coworking.reservation.service.payment.PaymentClient;
import com.coworking.reservation.service.payment.PaymentValidationResponse;
import com.coworking.reservation.service.pricing.PricingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PricingService pricingService;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void createShouldRejectOverlappingReservation() {
        UserAccount user = new UserAccount(
                "Test User",
                "user@test.com",
                "encoded-password",
                Role.USER
        );
        user.setId(1L);

        Space space = new Space(
                "Sala de Reuniones A",
                SpaceType.MEETING_ROOM,
                8,
                "Segundo piso",
                BigDecimal.valueOf(25)
        );
        space.setId(1L);

        CreateReservationRequest request = new CreateReservationRequest(
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3),
                "TARJETA"
        );

        when(userAccountRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(spaceRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(space));

        when(reservationRepository.existsOverlappingReservation(
                eq(1L),
                eq(request.startTime()),
                eq(request.endTime()),
                any()
        )).thenReturn(true);

        assertThatThrownBy(() ->
                reservationService.create(request, "user@test.com")
        ).isInstanceOf(OverlappingReservationException.class);

        verify(reservationRepository, never()).saveAndFlush(any());
        verify(paymentClient, never()).validate(any());
    }

    @Test
    void createShouldKeepReservationPendingWhenPaymentFails() {
        UserAccount user = new UserAccount(
                "Test User",
                "user@test.com",
                "encoded-password",
                Role.USER
        );
        user.setId(1L);

        Space space = new Space(
                "Sala de Reuniones A",
                SpaceType.MEETING_ROOM,
                8,
                "Segundo piso",
                BigDecimal.valueOf(25)
        );
        space.setId(1L);

        CreateReservationRequest request = new CreateReservationRequest(
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3),
                "TARJETA"
        );

        when(userAccountRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(spaceRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(space));

        when(reservationRepository.existsOverlappingReservation(
                eq(1L),
                eq(request.startTime()),
                eq(request.endTime()),
                any()
        )).thenReturn(false);

        when(pricingService.calculate(space, request.startTime(), request.endTime()))
                .thenReturn(BigDecimal.valueOf(50));

        when(paymentClient.validate(any()))
                .thenReturn(PaymentValidationResponse.pending(
                        "Servicio de pago no disponible"
                ));

        when(reservationMapper.toResponse(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation reservation = invocation.getArgument(0);

                    return new ReservationResponse(
                            reservation.getId(),
                            null,
                            "Test User",
                            null,
                            "Sala de Reuniones A",
                            SpaceType.MEETING_ROOM,
                            reservation.getStartTime(),
                            reservation.getEndTime(),
                            reservation.getTotalPrice(),
                            reservation.getStatus(),
                            reservation.getPaymentReference(),
                            reservation.getCreatedAt()
                    );
                });

        ReservationResponse response = reservationService.create(
                request,
                "user@test.com"
        );

        assertThat(response.status())
                .isEqualTo(ReservationStatus.PENDING_PAYMENT);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createShouldConfirmReservationWhenPaymentIsApproved() {
        UserAccount user = new UserAccount(
                "Test User",
                "user@test.com",
                "encoded-password",
                Role.USER
        );
        user.setId(1L);

        Space space = new Space(
                "Sala de Reuniones A",
                SpaceType.MEETING_ROOM,
                8,
                "Segundo piso",
                BigDecimal.valueOf(25)
        );
        space.setId(1L);

        CreateReservationRequest request = new CreateReservationRequest(
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3),
                "CARD"
        );

        when(userAccountRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(spaceRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(space));

        when(reservationRepository.existsOverlappingReservation(
                eq(1L),
                eq(request.startTime()),
                eq(request.endTime()),
                any()
        )).thenReturn(false);

        when(pricingService.calculate(space, request.startTime(), request.endTime()))
                .thenReturn(BigDecimal.valueOf(50));

        when(paymentClient.validate(any()))
                .thenReturn(PaymentValidationResponse.approved("PAY-123"));

        when(reservationMapper.toResponse(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation reservation = invocation.getArgument(0);

                    return new ReservationResponse(
                            reservation.getId(),
                            null,
                            "Test User",
                            null,
                            "Sala de Reuniones A",
                            SpaceType.MEETING_ROOM,
                            reservation.getStartTime(),
                            reservation.getEndTime(),
                            reservation.getTotalPrice(),
                            reservation.getStatus(),
                            reservation.getPaymentReference(),
                            reservation.getCreatedAt()
                    );
                });

        ReservationResponse response = reservationService.create(
                request,
                "user@test.com"
        );

        assertThat(response.status())
                .isEqualTo(ReservationStatus.CONFIRMED);

        assertThat(response.paymentReference())
                .isEqualTo("PAY-123");

        ArgumentCaptor<ReservationConfirmedEvent> eventCaptor =
                ArgumentCaptor.forClass(ReservationConfirmedEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());
    }
}
