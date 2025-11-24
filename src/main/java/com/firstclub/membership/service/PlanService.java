package com.firstclub.membership.service;

import com.firstclub.membership.model.MembershipPlan;
import com.firstclub.membership.repo.PlanRepository;

import java.util.Collection;
import java.util.Optional;

public class PlanService {
    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) { this.planRepository = planRepository; }

    public void createPlan(MembershipPlan plan) { planRepository.save(plan); }

    public Collection<MembershipPlan> listPlans() { return planRepository.findAll(); }

    public Optional<MembershipPlan> getPlan(String id) { return planRepository.findById(id); }
}
