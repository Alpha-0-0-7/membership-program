package com.firstclub.membership.repo;

import com.firstclub.membership.model.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> store = new ConcurrentHashMap<>();

    @Override
    public void save(User user) { store.put(user.getId(), user); }

    @Override
    public Optional<User> findById(String id) { return Optional.ofNullable(store.get(id)); }
}
