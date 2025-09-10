package com.samar.boxoffice;

import com.samar.boxoffice.model.Event;
import com.samar.boxoffice.model.Hold;
import com.samar.boxoffice.repository.InMemoryStore;
import com.samar.boxoffice.service.EventService;
import com.samar.boxoffice.service.HoldService;
import com.samar.boxoffice.service.HoldExpiryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class HoldExpiryConcurrencyTest {

    private EventService eventService;
    private HoldService holdService;
    private HoldExpiryService expiryService;

    @BeforeEach
    void setup() {
        InMemoryStore.events.clear();
        InMemoryStore.holds.clear();
        InMemoryStore.bookings.clear();
        eventService = new EventService();
        holdService = new HoldService();
        expiryService = new HoldExpiryService(holdService);
    }

    @Test
    void holdsShouldExpireAndReleaseSeatsAfterConcurrency() throws InterruptedException {
        Event e = eventService.createEvent("Theatre Show", 20);

        int threads = 30;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Hold>> futures = new ArrayList<>();

        // Many threads trying to hold 5 seats each
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                try {
                    return holdService.createHold(e.getId(), 5);
                } catch (Exception ignored) {
                    return null; // some will fail due to insufficient seats
                }
            }));
        }

        List<Hold> createdHolds = new ArrayList<>();
        for (Future<Hold> f : futures) {
            try {
                Hold h = f.get();
                if (h != null) {
                    createdHolds.add(h);
                }
            } catch (ExecutionException ignored) {}
        }
        executor.shutdown();

        // At this point, some seats are held
        assertTrue(e.getHeld() > 0 && e.getHeld() <= e.getTotal());
        assertEquals(e.getTotal(), e.getAvailable() + e.getHeld() + e.getBooked());

        // Force expiry of all holds
        createdHolds.forEach(h -> h.setExpiresAt(System.currentTimeMillis() - 1));

        // Run expiry sweep
        expiryService.expireHolds();

        // After sweep, all should be released
        assertEquals(e.getTotal(), e.getAvailable());
        assertEquals(0, e.getHeld());

        // Verify all holds are marked expired
        createdHolds.forEach(h -> assertEquals("EXPIRED", h.getStatus()));
    }
}
