package com.firstclub.membership.model;

import java.util.Objects;

public final class User {
    private final String id;
    private final String name;

    public User(String id, String name) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
