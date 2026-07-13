package com.coworking.reservation.controller;

import com.coworking.reservation.dto.request.CreateSpaceRequest;
import com.coworking.reservation.dto.request.UpdateSpaceRequest;
import com.coworking.reservation.dto.response.SpaceResponse;
import com.coworking.reservation.service.SpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping
    public ResponseEntity<SpaceResponse> create(
            @Valid @RequestBody CreateSpaceRequest request
    ) {
        SpaceResponse response = spaceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SpaceResponse>> findAllActive(Pageable pageable){
        return ResponseEntity.ok(spaceService.findAllActive(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaceResponse> findById(@PathVariable Long id){
        return ResponseEntity.ok(spaceService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpaceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSpaceRequest request
            ){
        return ResponseEntity.ok(spaceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id){
        spaceService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
