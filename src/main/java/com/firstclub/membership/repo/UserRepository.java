package com.firstclub.membership.repo;

import com.firstclub.membership.model.User;

import java.util.Optional;

/**
 * Repository interface for managing User entities.
 */
public interface UserRepository {

    void save(User user);

    Optional<User> findById(String id);
}
