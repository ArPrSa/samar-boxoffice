package com.samar.boxoffice.service;

import com.samar.boxoffice.model.Event;
import com.samar.boxoffice.repository.InMemoryStore;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventService {
    public Event createEvent(String name, int totalSeats) {
        Event e = new Event();
        e.setId(UUID.randomUUID().toString());
        e.setName(name);
        e.setTotal(totalSeats);
        e.setAvailable(totalSeats);
        e.setHeld(0);
        e.setBooked(0);
        e.setCreatedAt(System.currentTimeMillis());
        InMemoryStore.events.put(e.getId(), e);
        return e;
    }

    public Event getEvent(String id) {
        return InMemoryStore.events.get(id);
    }
}
