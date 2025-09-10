package com.samar.boxoffice;

import com.samar.boxoffice.model.Booking;
import com.samar.boxoffice.model.Event;
import com.samar.boxoffice.model.Hold;
import com.samar.boxoffice.repository.InMemoryStore;
import com.samar.boxoffice.service.BookingService;
import com.samar.boxoffice.service.EventService;
import com.samar.boxoffice.service.HoldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceIdempotencyTest {

    private EventService eventService;
    private HoldService holdService;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        InMemoryStore.clear(); // utility reset method
        eventService = new EventService();
        holdService = new HoldService();
        bookingService = new BookingService(holdService);
    }

    @Test
    void confirmBooking_IsIdempotent() throws Exception {
        // 1. Create an event with 10 seats
        Event event = eventService.createEvent("Concert", 10);

        // 2. Create a hold for 2 seats
        Hold hold = holdService.createHold(event.getId(), 2);

        // 3. First confirm booking
        Booking firstBooking = bookingService.confirmbooking(hold.getId(), hold.getPaymentToken());

        assertNotNull(firstBooking);
        assertEquals(2, firstBooking.getQty());
        assertEquals(event.getId(), firstBooking.getEventId());

        // 4. Call confirmBooking again with same hold + token
        Booking secondBooking = bookingService.confirmbooking(hold.getId(), hold.getPaymentToken());

        // 5. Assert: should return the same booking (idempotency check)
        assertEquals(firstBooking.getId(), secondBooking.getId(), "Booking should be idempotent");
        assertEquals("BOOKED", InMemoryStore.holds.get(hold.getId()).getStatus());

        // 6. Verify event counters (no double booking)
        Event updatedEvent = InMemoryStore.events.get(event.getId());
        assertEquals(8, updatedEvent.getAvailable(), "Available should decrease only once");
        assertEquals(2, updatedEvent.getBooked(), "Booked should increase only once");
    }
}
