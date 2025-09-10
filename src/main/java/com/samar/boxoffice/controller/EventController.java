package com.samar.boxoffice.controller;

import com.samar.boxoffice.model.Booking;
import com.samar.boxoffice.model.Event;
import com.samar.boxoffice.model.Hold;
import com.samar.boxoffice.service.BookingService;
import com.samar.boxoffice.service.EventService;
import com.samar.boxoffice.service.HoldService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/")
public class EventController {
    private final EventService eventService = new EventService();
    private final HoldService holdService = new HoldService();
    private final BookingService bookingService = new BookingService(holdService);

    @PostMapping("/events")
    public Event createEvent(@RequestBody Map<String, Object> body) throws Exception {
        return eventService.createEvent((String) body.get("name"), (Integer) body.get("total_seats"));
    }

    @GetMapping("/events/{id}")
    public Event getEvent(@PathVariable String id) {
        return eventService.getEvent(id);
    }

    @PostMapping("/holds")
    public Hold createHold(@RequestBody Map<String, Object> body) throws Exception {
        return holdService.createHold((String) body.get("event_id"), (Integer) body.get("qty"));
    }

    @PostMapping("/book")
    public Booking book(@RequestBody Map<String, Object> body) throws Exception {
        return bookingService.confirmbooking((String) body.get("hold_id"), (String) body.get("payment_token"));
    }
}
