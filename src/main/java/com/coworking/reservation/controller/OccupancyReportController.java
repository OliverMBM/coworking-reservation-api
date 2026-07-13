package com.coworking.reservation.controller;

import com.coworking.reservation.dto.response.OccupancyReportResponse;
import com.coworking.reservation.service.OccupancyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class OccupancyReportController {

    private final OccupancyReportService occupancyReportService;

    @GetMapping("/occupancy")
    public ResponseEntity<List<OccupancyReportResponse>> getOccupancyReport(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime
    ){
        return ResponseEntity.ok(
                occupancyReportService.getOccupancyReport(
                        startTime,
                        endTime
                )
        );
    }
}
