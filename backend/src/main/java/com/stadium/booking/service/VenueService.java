package com.stadium.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.VenueCreateRequest;
import com.stadium.booking.dto.response.VenueResponse;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.VenueRepository;
import com.stadium.booking.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;

    public List<VenueResponse> listAll() {
        return venueRepository.findAllActive().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public IPage<VenueResponse> listPage(Integer current, Integer size, String sportType, Integer status) {
        LambdaQueryWrapper<Venue> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Venue::getDeletedAt);
        if (sportType != null && !sportType.isEmpty()) {
            wrapper.eq(Venue::getSportType, sportType);
        }
        if (status != null) {
            wrapper.eq(Venue::getStatus, status);
        }
        wrapper.orderByDesc(Venue::getCreatedAt);

        IPage<Venue> page = venueRepository.selectPage(new Page<>(current, size), wrapper);
        return page.convert(this::toResponse);
    }

    public VenueResponse getById(Long id) {
        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));
        return toResponse(venue);
    }

    @Transactional
    public VenueResponse create(VenueCreateRequest request) {
        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = generateVenueCode();
        } else if (venueRepository.findByCode(code).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "球馆编码已存在");
        }

        Venue venue = new Venue();
        venue.setCampusId(1L);
        venue.setName(request.getName());
        venue.setCode(code);
        venue.setSportType(request.getSportType());
        venue.setLocation(request.getLocation());
        venue.setDescription(request.getDescription());
        venue.setImageUrl(request.getImageUrl());
        venue.setOpenDays(request.getOpenDays());
        venue.setOpenTime(request.getOpenTime());
        venue.setCloseTime(request.getCloseTime());
        venue.setSlotMinutes(request.getSlotMinutes());
        venue.setBookAheadDays(request.getBookAheadDays());
        venue.setCancelCutoffMinutes(request.getCancelCutoffMinutes());
        venue.setCheckinWindowBefore(request.getCheckinWindowBefore());
        venue.setNoShowGraceMinutes(request.getNoShowGraceMinutes());
        venue.setDailySlotLimit(request.getDailySlotLimit());
        venue.setWeeklySlotLimit(request.getWeeklySlotLimit());
        venue.setGroupBookingEnabled(request.getGroupBookingEnabled());
        venue.setGroupMaxCourts(request.getGroupMaxCourts());
        venue.setGroupMaxHours(request.getGroupMaxHours());
        venue.setStatus(1);

        venueRepository.insert(venue);
        return toResponse(venue);
    }

    @Transactional
    public VenueResponse update(Long id, VenueCreateRequest request) {
        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));

        venue.setName(request.getName());
        venue.setSportType(request.getSportType());
        venue.setLocation(request.getLocation());
        venue.setDescription(request.getDescription());
        venue.setImageUrl(request.getImageUrl());
        venue.setOpenDays(request.getOpenDays());
        venue.setOpenTime(request.getOpenTime());
        venue.setCloseTime(request.getCloseTime());
        venue.setSlotMinutes(request.getSlotMinutes());
        venue.setBookAheadDays(request.getBookAheadDays());
        venue.setCancelCutoffMinutes(request.getCancelCutoffMinutes());
        venue.setCheckinWindowBefore(request.getCheckinWindowBefore());
        venue.setNoShowGraceMinutes(request.getNoShowGraceMinutes());
        venue.setDailySlotLimit(request.getDailySlotLimit());
        venue.setWeeklySlotLimit(request.getWeeklySlotLimit());
        venue.setGroupBookingEnabled(request.getGroupBookingEnabled());
        venue.setGroupMaxCourts(request.getGroupMaxCourts());
        venue.setGroupMaxHours(request.getGroupMaxHours());

        venueRepository.updateById(venue);
        return toResponse(venue);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));
        venue.setStatus(status);
        venueRepository.updateById(venue);
    }

    @Transactional
    public void delete(Long id) {
        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "球馆不存在"));
        venueRepository.deleteById(id);
    }

    private String generateVenueCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "V" + dateStr;
        String maxCode = venueRepository.findMaxCodeByPrefix(prefix + "%");
        
        int sequence = 1;
        if (maxCode != null && maxCode.length() >= prefix.length() + 3) {
            try {
                sequence = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        return prefix + String.format("%03d", sequence);
    }

    private VenueResponse toResponse(Venue venue) {
        VenueResponse response = new VenueResponse();
        response.setId(venue.getId());
        response.setName(venue.getName());
        response.setCode(venue.getCode());
        response.setSportType(venue.getSportType());
        response.setLocation(venue.getLocation());
        response.setDescription(venue.getDescription());
        response.setImageUrl(venue.getImageUrl());
        response.setOpenDays(venue.getOpenDays());
        response.setOpenTime(venue.getOpenTime());
        response.setCloseTime(venue.getCloseTime());
        response.setSlotMinutes(venue.getSlotMinutes());
        response.setBookAheadDays(venue.getBookAheadDays());
        response.setCancelCutoffMinutes(venue.getCancelCutoffMinutes());
        response.setDailySlotLimit(venue.getDailySlotLimit());
        response.setStatus(venue.getStatus());
        response.setCourtCount(courtRepository.countByVenueId(venue.getId()));
        return response;
    }
}
