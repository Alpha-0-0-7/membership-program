package com.firstclub.membership.model;

import java.util.Objects;

public final class Benefit {
    private final String id;
    private final String description;
    private final BenefitType type;
    private final int value;

    private Benefit(String id, String description, BenefitType type, int value) {
        this.id = Objects.requireNonNull(id);
        this.description = Objects.requireNonNull(description);
        this.type = Objects.requireNonNull(type);
        this.value = value;
    }

    public static Benefit freeDelivery(int threshold) {
        return new Benefit("FREE_DELIVERY", "Free delivery over " + threshold, BenefitType.DELIVERY, threshold);
    }

    public static Benefit discountPercent(int percent) {
        return new Benefit("DISCOUNT_PERCENT", percent + "% discount", BenefitType.DISCOUNT, percent);
    }

    public static Benefit earlyAccess() {
        return new Benefit("EARLY_ACCESS", "Early access to deals", BenefitType.EARLY_ACCESS, 0);
    }

    public static Benefit prioritySupport() {
        return new Benefit("PRIORITY_SUPPORT", "Priority customer support", BenefitType.SUPPORT, 0);
    }

    public String id() { return id; }
    public String description() { return description; }
    public BenefitType type() { return type; }
    public int value() { return value; }

    @Override
    public String toString() {
        return id + "(" + description + ")";
    }
}
