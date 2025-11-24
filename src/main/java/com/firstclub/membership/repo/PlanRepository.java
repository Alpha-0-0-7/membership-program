package com.firstclub.membership.repo;

import com.firstclub.membership.model.MembershipPlan;

import java.util.Collection;
import java.util.Optional;

/**
 * Repository interface for storing and retrieving membership plans.
 */
public interface PlanRepository {

    void save(MembershipPlan plan);

    Optional<MembershipPlan> findById(String id);

    Collection<MembershipPlan> findAll();
}
