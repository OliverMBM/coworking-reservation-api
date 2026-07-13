package com.coworking.reservation.controller;

import com.coworking.reservation.dto.response.ReservationResponse;
import com.coworking.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> findAll(
            Pageable pageable
    ){
        return ResponseEntity.ok(reservationService.findAll(pageable));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAsAdmin(@PathVariable Long id){
        reservationService.cancelAsAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
