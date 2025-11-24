package com.firstclub.membership.repo;

import com.firstclub.membership.model.MembershipPlan;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPlanRepository implements PlanRepository {
    private final Map<String, MembershipPlan> store = new ConcurrentHashMap<>();

    @Override
    public void save(MembershipPlan plan) { store.put(plan.getId(), plan); }

    @Override
    public Optional<MembershipPlan> findById(String id) { return Optional.ofNullable(store.get(id)); }

    @Override
    public Collection<MembershipPlan> findAll() { return store.values(); }
}
