package com.coworking.reservation.service;

import com.coworking.reservation.dto.request.CreateSpaceRequest;
import com.coworking.reservation.dto.request.UpdateSpaceRequest;
import com.coworking.reservation.dto.response.SpaceResponse;
import com.coworking.reservation.entity.Space;
import com.coworking.reservation.exception.ResourceNotFoundException;
import com.coworking.reservation.mapper.SpaceMapper;
import com.coworking.reservation.repository.SpaceRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final SpaceMapper spaceMapper;

    @Transactional
    public SpaceResponse create(CreateSpaceRequest request){
        Space space = spaceMapper.toEntity(request);
        Space savedSpace = spaceRepository.save(space);

        return spaceMapper.toResponse(savedSpace);
    }

    @Transactional(readOnly = true)
    public Page<SpaceResponse> findAllActive(Pageable pageable) {
        return spaceRepository.findByActiveTrue(pageable)
                .map(spaceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SpaceResponse findById(Long id) {
        Space space = findSpaceById(id);
        return spaceMapper.toResponse(space);
    }

    @Transactional
    public SpaceResponse update(Long id, UpdateSpaceRequest request){
        Space space = findSpaceById(id);

        spaceMapper.updateEntity(space, request);

        return spaceMapper.toResponse(space);
    }

    @Transactional
    public void deactivate(Long id){
        Space space = findSpaceById(id);
        space.deactivate();
    }

    private Space findSpaceById(Long id){
        return spaceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "El espacio con id: "+ id + " no fue encontrado"
                        )
                );
    }
}
