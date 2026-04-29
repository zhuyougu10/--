package com.stadium.booking.service;

import com.stadium.booking.dto.response.CourtResponse;
import com.stadium.booking.dto.response.VenueResponse;
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
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private CourtService courtService;

    @Mock
    private AdminVenueAccessService adminVenueAccessService;

    @InjectMocks
    private VenueService venueService;

    @Test
    void publicGetByIdSkipsAdminAccessCheck() {
        Venue venue = buildVenue();
        CourtResponse court = new CourtResponse();
        court.setId(101L);
        court.setVenueId(1L);
        court.setName("1号场");

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(courtRepository.countByVenueId(1L)).thenReturn(1);
        when(courtService.listPublicByVenue(1L)).thenReturn(List.of(court));

        VenueResponse response = venueService.getPublicById(1L);

        assertEquals(1L, response.getId());
        assertEquals(1, response.getCourtCount());
        assertEquals(1, response.getCourts().size());
        assertEquals("1号场", response.getCourts().get(0).getName());
        verify(adminVenueAccessService, never()).checkVenueAccess(1L);
        verify(courtService).listPublicByVenue(1L);
        verify(courtService, never()).listByVenue(1L);
    }

    @Test
    void adminGetByIdKeepsAdminAccessCheck() {
        Venue venue = buildVenue();
        CourtResponse court = new CourtResponse();
        court.setId(201L);
        court.setVenueId(1L);
        court.setName("2号场");

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(courtRepository.countByVenueId(1L)).thenReturn(1);
        when(courtService.listByVenue(1L)).thenReturn(List.of(court));

        VenueResponse response = venueService.getById(1L);

        assertEquals(1L, response.getId());
        assertEquals(1, response.getCourts().size());
        verify(adminVenueAccessService).checkVenueAccess(1L);
        verify(courtService).listByVenue(1L);
        verify(courtService, never()).listPublicByVenue(1L);
    }

    private Venue buildVenue() {
        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("体育馆A馆——篮球馆");
        venue.setCode("V20260426001");
        venue.setSportType("basketball");
        venue.setLocation("田径场左侧体育馆");
        venue.setStatus(1);
        return venue;
    }
}
