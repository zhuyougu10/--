package com.stadium.booking.service;

import com.stadium.booking.dto.response.CourtResponse;
import com.stadium.booking.entity.Court;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.CourtRepository;
import com.stadium.booking.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private AdminVenueAccessService adminVenueAccessService;

    @InjectMocks
    private CourtService courtService;

    @Test
    void publicListByVenueSkipsAdminAccessCheck() {
        Court court = buildCourt();
        when(courtRepository.findByVenueId(1L)).thenReturn(List.of(court));
        when(venueRepository.findById(1L)).thenReturn(Optional.of(buildVenue()));

        List<CourtResponse> response = courtService.listPublicByVenue(1L);

        assertEquals(1, response.size());
        assertEquals("1号场", response.get(0).getName());
        assertEquals("体育馆A馆——篮球馆", response.get(0).getVenueName());
        verify(adminVenueAccessService, never()).checkVenueAccess(1L);
    }

    @Test
    void adminListByVenueKeepsAdminAccessCheck() {
        Court court = buildCourt();
        when(courtRepository.findByVenueId(1L)).thenReturn(List.of(court));
        when(venueRepository.findById(1L)).thenReturn(Optional.of(buildVenue()));

        List<CourtResponse> response = courtService.listByVenue(1L);

        assertEquals(1, response.size());
        assertEquals("1号场", response.get(0).getName());
        verify(adminVenueAccessService).checkVenueAccess(1L);
    }

    private Court buildCourt() {
        Court court = new Court();
        court.setId(100L);
        court.setVenueId(1L);
        court.setName("1号场");
        court.setCourtNo("001");
        court.setSportType("basketball");
        court.setStatus(1);
        court.setSortOrder(1);
        return court;
    }

    private Venue buildVenue() {
        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("体育馆A馆——篮球馆");
        return venue;
    }
}
