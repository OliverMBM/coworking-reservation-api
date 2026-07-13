package com.coworking.reservation.mapper;

import com.coworking.reservation.dto.request.CreateSpaceRequest;
import com.coworking.reservation.dto.request.UpdateSpaceRequest;
import com.coworking.reservation.dto.response.SpaceResponse;
import com.coworking.reservation.entity.Space;
import org.springframework.stereotype.Component;

@Component
public class SpaceMapper {

    public Space toEntity(CreateSpaceRequest request) {
        return new Space(
                request.name(),
                request.type(),
                request.capacity(),
                request.location(),
                request.hourlyRate()
        );
    }

    public void updateEntity(Space space, UpdateSpaceRequest request) {
        space.setName(request.name());
        space.setType(request.type());
        space.setCapacity(request.capacity());
        space.setLocation(request.location());
        space.setHourlyRate(request.hourlyRate());
        space.setActive(request.active());
    }

    public SpaceResponse toResponse(Space space) {
        return new SpaceResponse(
                space.getId(),
                space.getName(),
                space.getType(),
                space.getCapacity(),
                space.getLocation(),
                space.getHourlyRate(),
                space.getActive()
        );
    }
}
