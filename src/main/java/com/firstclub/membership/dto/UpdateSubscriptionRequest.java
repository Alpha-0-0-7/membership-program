package com.firstclub.membership.dto;

/** Update subscription DTO: newTierId == null means cancel. */
public record UpdateSubscriptionRequest(String subscriptionId, String newTierId) {}
