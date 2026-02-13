package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.CourtCreateRequest;
import com.stadium.booking.dto.response.CourtResponse;
import com.stadium.booking.entity.Court;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.CourtRepository;
import com.stadium.booking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtService {
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;

    public List<CourtResponse> listByVenue(Long venueId) {
        return courtRepository.findByVenueId(venueId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public CourtResponse getById(Long id) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场地不存在"));
        return toResponse(court);
    }

    @Transactional
    public CourtResponse create(CourtCreateRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));

        Court court = new Court();
        court.setVenueId(request.getVenueId());
        court.setName(request.getName());
        court.setCourtNo(request.getCourtNo());
        court.setSportType(request.getSportType() != null ? request.getSportType() : venue.getSportType());
        court.setFloorType(request.getFloorType());
        court.setFeatures(request.getFeatures());
        court.setSortOrder(request.getSortOrder());
        court.setStatus(1);

        courtRepository.insert(court);
        return toResponse(court);
    }

    @Transactional
    public CourtResponse update(Long id, CourtCreateRequest request) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场地不存在"));

        court.setName(request.getName());
        court.setCourtNo(request.getCourtNo());
        court.setSportType(request.getSportType());
        court.setFloorType(request.getFloorType());
        court.setFeatures(request.getFeatures());
        court.setSortOrder(request.getSortOrder());

        courtRepository.updateById(court);
        return toResponse(court);
    }

    @Transactional
    public void updateStatus(Long id, Integer status, String reason) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场地不存在"));
        court.setStatus(status);
        court.setStatusReason(reason);
        courtRepository.updateById(court);
    }

    @Transactional
    public void delete(Long id) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场地不存在"));
        courtRepository.deleteById(id);
    }

    private CourtResponse toResponse(Court court) {
        CourtResponse response = new CourtResponse();
        response.setId(court.getId());
        response.setVenueId(court.getVenueId());
        response.setName(court.getName());
        response.setCourtNo(court.getCourtNo());
        response.setSportType(court.getSportType());
        response.setFloorType(court.getFloorType());
        response.setFeatures(court.getFeatures());
        response.setStatus(court.getStatus());
        response.setStatusReason(court.getStatusReason());
        response.setStatusUntil(court.getStatusUntil());
        response.setSortOrder(court.getSortOrder());

        venueRepository.findById(court.getVenueId())
            .ifPresent(venue -> response.setVenueName(venue.getName()));

        return response;
    }
}
