package com.firstclub.membership.repo;

import com.firstclub.membership.model.Subscription;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for subscriptions.
 * Supports CRUD-like operations and user-based lookups.
 */
public interface SubscriptionRepository {

    void save(Subscription subscription);

    Optional<Subscription> findById(String id);

    List<Subscription> findByUserId(String userId);

    Collection<Subscription> findAll();

    void delete(String id);
}
