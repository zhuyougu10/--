CREATE INDEX idx_booking_conflict ON booking (court_id, booking_date, start_time, end_time, status);

CREATE INDEX idx_user_booking ON booking (user_id, booking_date, status);

CREATE INDEX idx_venue_today ON booking (venue_id, booking_date, status);

CREATE INDEX idx_court_date_status ON booking (court_id, booking_date, status, start_time);
