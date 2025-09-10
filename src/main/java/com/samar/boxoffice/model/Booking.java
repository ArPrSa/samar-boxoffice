package com.samar.boxoffice.model;

import lombok.Data;

@Data
public class Booking {
    private String id;
    private String holdId;
    private String eventId;
    private int qty;
    private long createdAt;

    // getters & setters
}
