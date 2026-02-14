package com.stadium.booking.service;

import com.stadium.booking.dto.request.RecommendationRequest;
import com.stadium.booking.dto.response.RecommendationResponse;
import com.stadium.booking.entity.Booking;
import com.stadium.booking.entity.Court;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.CourtRepository;
import com.stadium.booking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;

    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        List<RecommendationResponse.RecommendationItem> items = new ArrayList<>();

        if (request.getVenueId() != null) {
            items.addAll(findInVenue(request, request.getVenueId()));
        }

        if (items.size() < request.getMaxResults() && 
            Boolean.TRUE.equals(request.getAllowAlternativeVenue()) &&
            request.getSportType() != null) {
            items.addAll(findAlternativeVenues(request));
        }

        items.sort((a, b) -> b.getScore().compareTo(a.getScore()));

        items = items.stream()
            .limit(request.getMaxResults())
            .collect(Collectors.toList());

        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendations(items);
        response.setTotalCount(items.size());
        return response;
    }

    private List<RecommendationResponse.RecommendationItem> findInVenue(
            RecommendationRequest request, Long venueId) {
        List<RecommendationResponse.RecommendationItem> items = new ArrayList<>();

        Venue venue = venueRepository.findById(venueId).orElse(null);
        if (venue == null || venue.getStatus() != 1) {
            return items;
        }

        List<Court> courts = courtRepository.findActiveByVenueId(venueId);
        int slotMinutes = venue.getSlotMinutes();
        LocalTime preferredStart = request.getPreferredStartTime();
        if (preferredStart == null) {
            preferredStart = venue.getOpenTime();
        }

        for (Court court : courts) {
            items.addAll(findAvailableSlots(venue, court, request.getDate(), 
                preferredStart, request.getDurationMinutes(), slotMinutes, 
                request.getTimeOffsetSlots()));
        }

        return items;
    }

    private List<RecommendationResponse.RecommendationItem> findAvailableSlots(
            Venue venue, Court court, LocalDate date, LocalTime preferredStart,
            int durationMinutes, int slotMinutes, int timeOffsetSlots) {
        
        List<RecommendationResponse.RecommendationItem> items = new ArrayList<>();
        
        LocalTime searchStart = preferredStart.minusMinutes(timeOffsetSlots * slotMinutes);
        if (searchStart.isBefore(venue.getOpenTime())) {
            searchStart = venue.getOpenTime();
        }

        LocalTime searchEnd = preferredStart.plusMinutes((timeOffsetSlots + 1) * slotMinutes + durationMinutes);
        if (searchEnd.isAfter(venue.getCloseTime())) {
            searchEnd = venue.getCloseTime();
        }

        List<Booking> bookings = bookingRepository.findByCourtIdAndDate(court.getId(), date);
        Set<String> occupiedSlots = bookings.stream()
            .filter(b -> b.getStatus() == 1)
            .map(b -> b.getStartTime().toString() + "-" + b.getEndTime().toString())
            .collect(Collectors.toSet());

        LocalTime currentStart = alignToSlot(searchStart, slotMinutes);
        while (currentStart.plusMinutes(durationMinutes).compareTo(searchEnd) <= 0) {
            LocalTime end = currentStart.plusMinutes(durationMinutes);
            
            if (end.compareTo(venue.getCloseTime()) <= 0 && 
                isSlotAvailable(currentStart, end, occupiedSlots, slotMinutes)) {
                
                RecommendationResponse.RecommendationItem item = createItem(
                    venue, court, date, currentStart, end, preferredStart, slotMinutes);
                items.add(item);
            }
            
            currentStart = currentStart.plusMinutes(slotMinutes);
        }

        return items;
    }

    private boolean isSlotAvailable(LocalTime start, LocalTime end, 
                                   Set<String> occupiedSlots, int slotMinutes) {
        LocalTime current = start;
        while (current.compareTo(end) < 0) {
            LocalTime slotEnd = current.plusMinutes(slotMinutes);
            String slotKey = current.toString() + "-" + slotEnd.toString();
            if (occupiedSlots.contains(slotKey)) {
                return false;
            }
            current = slotEnd;
        }
        return true;
    }

    private LocalTime alignToSlot(LocalTime time, int slotMinutes) {
        int minutes = time.getMinute();
        int alignedMinutes = (minutes / slotMinutes) * slotMinutes;
        return time.withMinute(alignedMinutes).withSecond(0).withNano(0);
    }

    private RecommendationResponse.RecommendationItem createItem(
            Venue venue, Court court, LocalDate date, LocalTime start, LocalTime end,
            LocalTime preferredStart, int slotMinutes) {
        
        RecommendationResponse.RecommendationItem item = new RecommendationResponse.RecommendationItem();
        item.setVenueId(venue.getId());
        item.setVenueName(venue.getName());
        item.setCourtId(court.getId());
        item.setCourtName(court.getName());
        item.setDate(date);
        item.setStartTime(start);
        item.setEndTime(end);

        int timeDiff = (int) Math.abs(java.time.Duration.between(preferredStart, start).toMinutes());
        int slotDiff = timeDiff / slotMinutes;

        if (slotDiff == 0) {
            item.setLabel("期望时间");
            item.setReason("完全匹配您的期望时间");
            item.setScore(100);
        } else if (start.isBefore(preferredStart)) {
            item.setLabel("提前" + slotDiff + "时段");
            item.setReason("比期望时间提前" + slotDiff + "个时段");
            item.setScore(90 - slotDiff * 5);
        } else {
            item.setLabel("延后" + slotDiff + "时段");
            item.setReason("比期望时间延后" + slotDiff + "个时段");
            item.setScore(85 - slotDiff * 5);
        }

        return item;
    }

    private List<RecommendationResponse.RecommendationItem> findAlternativeVenues(
            RecommendationRequest request) {
        List<RecommendationResponse.RecommendationItem> items = new ArrayList<>();

        List<Venue> alternativeVenues = venueRepository.findBySportType(request.getSportType());
        
        for (Venue venue : alternativeVenues) {
            if (request.getVenueId() != null && venue.getId().equals(request.getVenueId())) {
                continue;
            }

            List<RecommendationResponse.RecommendationItem> venueItems = findInVenue(request, venue.getId());
            
            for (RecommendationResponse.RecommendationItem item : venueItems) {
                item.setLabel("替代球馆");
                item.setReason("同运动类型其他球馆: " + venue.getName());
                item.setScore(item.getScore() - 20);
            }
            
            items.addAll(venueItems);
        }

        return items;
    }
}
