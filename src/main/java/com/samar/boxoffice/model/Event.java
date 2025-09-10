package com.samar.boxoffice.model;

import lombok.Data;

import java.util.concurrent.locks.ReentrantLock;

@Data
public class Event {
    private String id;
    private String name;
    private int total;
    private int available;
    private int held;
    private int booked;
    private long createdAt;
    private transient ReentrantLock lock = new ReentrantLock();

    // getters & setters
}
