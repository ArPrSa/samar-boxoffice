package com.samar.boxoffice.service;

import com.samar.boxoffice.model.Event;
import com.samar.boxoffice.model.Hold;
import com.samar.boxoffice.repository.InMemoryStore;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HoldService {
    private static final int HOLD_TTL = 120_000; // 2 minutes

    public Hold createHold(String eventId, int qty) throws Exception {
        Event event = InMemoryStore.events.get(eventId);
        if (event == null) throw new Exception("Event not found");

        event.getLock().lock();
        try {
            if (qty > event.getAvailable()) throw new Exception("Not enough seats");
            event.setAvailable(event.getAvailable() - qty);
            event.setHeld(event.getHeld() + qty);

            Hold h = new Hold();
            h.setId(UUID.randomUUID().toString());
            h.setEventId(eventId);
            h.setQty(qty);
            h.setExpiresAt(System.currentTimeMillis() + HOLD_TTL);
            h.setPaymentToken(UUID.randomUUID().toString());
            h.setStatus("ACTIVE");
            h.setCreatedAt(System.currentTimeMillis());

            InMemoryStore.holds.put(h.getId(), h);
            return h;
        } finally {
            event.getLock().unlock();
        }
    }

    public void expireHold(Hold h) {
        Event event = InMemoryStore.events.get(h.getEventId());
        if (event != null && "ACTIVE".equals(h.getStatus())) {
            event.getLock().lock();
            try {
                event.setHeld(event.getHeld() - h.getQty());
                event.setAvailable(event.getAvailable() + h.getQty());
                h.setStatus("EXPIRED");
            } finally {
                event.getLock().unlock();
            }
        }
    }
}
