package com.samar.boxoffice.service;

import com.samar.boxoffice.repository.InMemoryStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HoldExpiryService {
    private final HoldService holdService;

    public HoldExpiryService(HoldService holdService) {
        this.holdService = holdService;
    }

    // Run every 2 mins
    @Scheduled(fixedRate = 120_000)
    public void expireHolds() {
        long now = System.currentTimeMillis();
        InMemoryStore.holds.values().forEach(h -> {
            if ("ACTIVE".equals(h.getStatus()) && h.getExpiresAt() < now) {
                holdService.expireHold(h);
            }
        });
    }
}
