package com.coworking.reservation.controller;

import com.coworking.reservation.dto.request.CreateReservationRequest;
import com.coworking.reservation.dto.response.ReservationResponse;
import com.coworking.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody CreateReservationRequest request,
            Authentication authentication
    ) {
        ReservationResponse response = reservationService.create(
                request,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<ReservationResponse>> findMine(
            Authentication authentication,
            Pageable pageable
    ){
        return ResponseEntity.ok(
                reservationService.findMine(
                        authentication.getName(),
                        pageable
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelMine(
            @PathVariable Long id,
            Authentication authentication
    ){
        reservationService.cancelMine(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
