package com.firstclub.membership.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Subscription is mutable and thread-safe for mutations (synchronized).
 */
public final class Subscription {
    private final String id;
    private final String userId;
    private final String planId;
    private volatile String tierId;
    private final Instant startAt;
    private volatile Instant expiresAt;
    private volatile SubscriptionStatus status;

    public Subscription(String id, String userId, String planId, String tierId, Instant startAt, Instant expiresAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.planId = Objects.requireNonNull(planId);
        this.tierId = Objects.requireNonNull(tierId);
        this.startAt = Objects.requireNonNull(startAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.status = SubscriptionStatus.ACTIVE;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getPlanId() { return planId; }
    public String getTierId() { return tierId; }
    public Instant getStartAt() { return startAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public SubscriptionStatus getStatus() { return status; }

    public synchronized void changeTier(String newTierId) {
        this.tierId = Objects.requireNonNull(newTierId);
    }

    public synchronized void extendExpiry(Instant newExpiry) {
        this.expiresAt = Objects.requireNonNull(newExpiry);
    }

    public synchronized void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }

    public synchronized void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }
}
