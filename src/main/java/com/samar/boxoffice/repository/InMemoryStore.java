package com.samar.boxoffice.repository;

import com.samar.boxoffice.model.Booking;
import com.samar.boxoffice.model.Event;
import com.samar.boxoffice.model.Hold;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStore {
    public static final Map<String, Event> events = new ConcurrentHashMap<>();
    public static final Map<String, Hold> holds = new ConcurrentHashMap<>();
    public static final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    public static void clear() {
        events.clear();
        holds.clear();
        bookings.clear();
    }
}
