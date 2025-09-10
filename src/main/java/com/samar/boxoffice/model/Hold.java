package com.samar.boxoffice.model;


import lombok.Data;

@Data
public class Hold {
    private String id;
    private String eventId;
    private int qty;
    private long expiresAt;
    private String paymentToken;
    private String status; // ACTIVE, EXPIRED, BOOKED
    private String bookingId;
    private long createdAt;

    // getters & setters
}
