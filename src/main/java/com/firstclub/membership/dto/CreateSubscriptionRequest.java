package com.firstclub.membership.dto;

/** Create subscription DTO (used by potential programmatic callers). */
public record CreateSubscriptionRequest(String userId, String planId, String tierId) {}
