package com.firstclub.membership.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Tier {
    private final String id;
    private final String name;
    private final List<Benefit> benefits;

    public Tier(String id, String name, List<Benefit> benefits) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.benefits = Collections.unmodifiableList(Objects.requireNonNull(benefits));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Benefit> getBenefits() { return benefits; }

    public String brief() {
        StringBuilder sb = new StringBuilder(name + " [" + id + "]: ");
        for (int i = 0; i < benefits.size(); i++) {
            sb.append(benefits.get(i));
            if (i < benefits.size()-1) sb.append(", ");
        }
        return sb.toString();
    }
}
