package com.firstclub.membership.model;

import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MembershipPlan {
    private final String id;
    private final String description;
    private final int priceInINR;
    private final Period duration;
    private final List<Tier> tiers;

    public MembershipPlan(String id, String description, int priceInINR, Period duration, List<Tier> tiers) {
        this.id = Objects.requireNonNull(id);
        this.description = Objects.requireNonNull(description);
        this.priceInINR = priceInINR;
        this.duration = Objects.requireNonNull(duration);
        this.tiers = Collections.unmodifiableList(Objects.requireNonNull(tiers));
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public int getPriceInINR() { return priceInINR; }
    public Period getDuration() { return duration; }
    public List<Tier> getTiers() { return tiers; }

    public String brief() {
        return id + " - " + description + " - ₹" + priceInINR + " - " + duration.toString();
    }
}
