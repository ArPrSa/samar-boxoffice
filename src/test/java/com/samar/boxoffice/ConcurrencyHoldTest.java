package com.samar.boxoffice;

import com.samar.boxoffice.model.Event;
import com.samar.boxoffice.repository.InMemoryStore;
import com.samar.boxoffice.service.EventService;
import com.samar.boxoffice.service.HoldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyHoldTest {

    private EventService eventService;
    private HoldService holdService;

    @BeforeEach
    void setup() {
        InMemoryStore.events.clear();
        InMemoryStore.holds.clear();
        InMemoryStore.bookings.clear();
        eventService = new EventService();
        holdService = new HoldService();
    }

    @Test
    void parallelHoldsShouldNotOverbook() throws InterruptedException {
        Event e = eventService.createEvent("Concert", 20);

        int threads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        // Many threads trying to hold 5 seats each
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                try {
                    holdService.createHold(e.getId(), 5);
                } catch (Exception ignored) {
                    // Expected for some threads: not enough seats
                }
            }));
        }

        // Wait for all to finish
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (ExecutionException ignored) {
            }
        }
        executor.shutdown();

        // Invariant check: no overbooking
        int total = e.getAvailable() + e.getHeld() + e.getBooked();
        assertEquals(e.getTotal(), total);

        // Additional sanity checks
        assertTrue(e.getAvailable() >= 0, "Available seats should never go negative");
        assertTrue(e.getHeld() >= 0, "Held seats should never go negative");
        assertTrue(e.getBooked() >= 0, "Booked seats should never go negative");
        assertTrue(e.getHeld() <= e.getTotal(), "Held seats should not exceed total");
    }
}
