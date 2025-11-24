package com.firstclub.membership.repo;

import com.firstclub.membership.model.Subscription;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class InMemorySubscriptionRepository implements SubscriptionRepository {
    private final ConcurrentMap<String, Subscription> store = new ConcurrentHashMap<>();

    @Override
    public void save(Subscription s) { store.put(s.getId(), s); }

    @Override
    public Optional<Subscription> findById(String id) { return Optional.ofNullable(store.get(id)); }

    @Override
    public List<Subscription> findByUserId(String userId) {
        return store.values().stream().filter(s -> s.getUserId().equals(userId)).collect(Collectors.toList());
    }

    @Override
    public Collection<Subscription> findAll() { return new ArrayList<>(store.values()); }

    @Override
    public void delete(String id) { store.remove(id); }
}
