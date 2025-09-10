package com.samar.boxoffice.service;

import com.samar.boxoffice.model.Booking;
import com.samar.boxoffice.model.Event;
import com.samar.boxoffice.model.Hold;
import com.samar.boxoffice.repository.InMemoryStore;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingService{

    private HoldService holdService;

    public BookingService(HoldService holdService){
        this.holdService = holdService;
    }
    public Booking confirmbooking(String holdId, String paymentToken) throws Exception {
        Hold h = InMemoryStore.holds.get(holdId);
        if (h == null) throw new Exception("Hold not found");

        Event event = InMemoryStore.events.get(h.getEventId());
        if (event == null) throw new Exception("Event not found");

        event.getLock().lock();
        try {
            if ("EXPIRED".equals(h.getStatus())) throw new Exception("Hold expired");
            if ("BOOKED".equals(h.getStatus())) {
                // Idempotent response
                return InMemoryStore.bookings.get(h.getBookingId());
            }
            if (!h.getPaymentToken().equals(paymentToken)) throw new Exception("Invalid payment token");
            if (h.getExpiresAt() < System.currentTimeMillis()) {
                holdService.expireHold(h);
                throw new Exception("Hold expired");
            }

            Booking b = new Booking();
            b.setId(UUID.randomUUID().toString());
            b.setHoldId(holdId);
            b.setEventId(h.getEventId());
            b.setQty(h.getQty());
            b.setCreatedAt(System.currentTimeMillis());

            // Mark hold BOOKED first (prevents race)
            h.setStatus("BOOKED");
            h.setBookingId(b.getId());

            // Update event counters
            event.setHeld(event.getHeld() - h.getQty());
            event.setBooked(event.getBooked() + h.getQty());

            // Save booking
            InMemoryStore.bookings.put(b.getId(), b);

            return b;
        } finally {
            event.getLock().unlock();
        }
    }
}



