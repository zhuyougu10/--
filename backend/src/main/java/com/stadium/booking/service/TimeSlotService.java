package com.stadium.booking.service;

import com.stadium.booking.dto.response.TimeSlotResponse;
import com.stadium.booking.entity.Booking;
import com.stadium.booking.entity.Court;
import com.stadium.booking.entity.CourtClosure;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.CourtClosureRepository;
import com.stadium.booking.repository.CourtRepository;
import com.stadium.booking.repository.VenueRepository;
import com.stadium.booking.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final CourtClosureRepository courtClosureRepository;

    public List<TimeSlotResponse> getAvailableSlots(Long venueId, Long courtId, LocalDate date) {
        Venue venue = venueRepository.findById(venueId)
            .orElseThrow(() -> new RuntimeException("球馆不存在"));

        Court court = courtRepository.findById(courtId)
            .orElseThrow(() -> new RuntimeException("场地不存在"));

        List<TimeSlotResponse> slots = generateTimeSlots(venue, court, date);

        List<Booking> bookings = bookingRepository.findByCourtIdAndDate(courtId, date);

        LocalDateTime slotStart = date.atTime(venue.getOpenTime());
        LocalDateTime slotEnd = date.atTime(venue.getCloseTime());
        List<CourtClosure> closures = courtClosureRepository.findOverlapping(courtId, slotStart, slotEnd);

        boolean isAdmin = UserContext.isCurrentUserAdmin();
        LocalDateTime now = LocalDateTime.now();
        boolean isToday = date.equals(LocalDate.now());

        for (TimeSlotResponse slot : slots) {
            LocalDateTime slotDateTime = date.atTime(slot.getStartTime());
            
            if (isToday && !slotDateTime.isAfter(now)) {
                slot.setStatus("past");
                continue;
            }
            
            Booking overlappingBooking = findOverlappingBooking(bookings, slot);
            if (overlappingBooking != null) {
                slot.setStatus("occupied");
                if (isAdmin) {
                    TimeSlotResponse.BookingInfo info = new TimeSlotResponse.BookingInfo();
                    info.setBookingNo(overlappingBooking.getBookingNo());
                    info.setUserName(overlappingBooking.getUserName());
                    info.setUserPhone(overlappingBooking.getUserPhone());
                    slot.setBooking(info);
                }
            } else if (isCourtClosed(closures, slot)) {
                slot.setStatus("closed");
            } else {
                slot.setStatus("free");
            }
        }

        return slots;
    }

    private Booking findOverlappingBooking(List<Booking> bookings, TimeSlotResponse slot) {
        for (Booking booking : bookings) {
            if (booking.getStartTime().isBefore(slot.getEndTime()) && 
                booking.getEndTime().isAfter(slot.getStartTime())) {
                return booking;
            }
        }
        return null;
    }

    private List<TimeSlotResponse> generateTimeSlots(Venue venue, Court court, LocalDate date) {
        List<TimeSlotResponse> slots = new ArrayList<>();

        LocalTime currentTime = venue.getOpenTime();
        LocalTime closeTime = venue.getCloseTime();
        int slotMinutes = venue.getSlotMinutes();

        while (currentTime.isBefore(closeTime)) {
            LocalTime endTime = currentTime.plusMinutes(slotMinutes);
            if (endTime.isAfter(closeTime)) {
                break;
            }

            TimeSlotResponse slot = new TimeSlotResponse();
            slot.setCourtId(court.getId());
            slot.setCourtName(court.getName());
            slot.setDate(date);
            slot.setStartTime(currentTime);
            slot.setEndTime(endTime);
            slot.setStatus("free");

            slots.add(slot);
            currentTime = endTime;
        }

        return slots;
    }

    private boolean isCourtClosed(List<CourtClosure> closures, TimeSlotResponse slot) {
        LocalDateTime slotStart = slot.getDate().atTime(slot.getStartTime());
        LocalDateTime slotEnd = slot.getDate().atTime(slot.getEndTime());

        return closures.stream().anyMatch(closure ->
            closure.getStartTime().isBefore(slotEnd) && closure.getEndTime().isAfter(slotStart)
        );
    }
}
