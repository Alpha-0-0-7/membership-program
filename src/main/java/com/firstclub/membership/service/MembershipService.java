package com.firstclub.membership.service;

import com.firstclub.membership.model.MembershipPlan;
import com.firstclub.membership.model.Subscription;
import com.firstclub.membership.model.SubscriptionStatus;
import com.firstclub.membership.model.User;
import com.firstclub.membership.repo.PlanRepository;
import com.firstclub.membership.repo.SubscriptionRepository;
import com.firstclub.membership.repo.UserRepository;
import com.firstclub.membership.util.IdGenerator;
import com.firstclub.membership.util.TimeUtil;

import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core business logic. Uses per-subscription synchronization on mutation.
 */
public class MembershipService {
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final TierEvaluator tierEvaluator;

    public MembershipService(SubscriptionRepository subscriptionRepository,
                             PlanRepository planRepository,
                             UserRepository userRepository,
                             TierEvaluator tierEvaluator) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.tierEvaluator = tierEvaluator;
    }

    public List<MembershipPlan> listPlans() {
        return List.copyOf(planRepository.findAll());
    }

    public Optional<MembershipPlan> getPlan(String planId) {
        return planRepository.findById(planId);
    }

    /**
     * Subscribe user to a plan.
     * Rules:
     * - User must exist
     * - Plan & tier must exist
     * - Only ONE ACTIVE subscription is allowed per user
     */
    public Subscription subscribe(String userId, String planId, String tierId) {

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Validate plan exists
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));

        // Validate tier exists in plan (List<Tier>)
        boolean tierExists = plan.getTiers().stream()
                .anyMatch(t -> t.getId().equals(tierId));

        if (!tierExists) {
            throw new IllegalArgumentException("Tier " + tierId + " not part of plan: " + planId);
        }

        // BUSINESS RULE: Only one ACTIVE subscription per user
        List<Subscription> activeSubs = subscriptionRepository.findByUserId(userId).stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();

        if (!activeSubs.isEmpty()) {
            throw new IllegalStateException("User already has an ACTIVE subscription.");
        }

        // Create subscription
        Instant start = Instant.now();
        Period duration = plan.getDuration();
        Instant expiry = TimeUtil.plusPeriod(start, duration);
        String id = IdGenerator.nextId("sub");

        Subscription s = new Subscription(id, user.getId(), plan.getId(), tierId, start, expiry);
        subscriptionRepository.save(s);
        return s;
    }

    /**
     * Upgrade/downgrade subscription tier.
     * Only ACTIVE subscriptions allowed.
     */
    public Subscription upgradeOrDowngrade(String subscriptionId, String newTierId) {

        Subscription s = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + subscriptionId));

        if (s.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE subscriptions can change tiers.");
        }

        // Validate the new tier exists on the plan
        MembershipPlan plan = planRepository.findById(s.getPlanId())
                .orElseThrow(() -> new IllegalStateException("Plan missing for subscription."));

        boolean tierExists = plan.getTiers().stream()
                .anyMatch(t -> t.getId().equals(newTierId));

        if (!tierExists) {
            throw new IllegalArgumentException("Tier " + newTierId + " not part of plan: " + plan.getId());
        }

        synchronized (s) {
            s.changeTier(newTierId);
            subscriptionRepository.save(s);
            return s;
        }
    }

    /**
     * Cancel subscription.
     */
    public void cancelSubscription(String subscriptionId) {
        Subscription s = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + subscriptionId));

        synchronized (s) {
            s.cancel();
            subscriptionRepository.save(s);
        }
    }

    public Optional<Subscription> getSubscription(String subscriptionId) {
        return subscriptionRepository.findById(subscriptionId);
    }

    public List<Subscription> getSubscriptionsForUser(String userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    /**
     * Evaluate tier based on metrics.
     * Only ACTIVE subscriptions allowed.
     */
    public Subscription evaluateAndApplyTier(String subscriptionId, Map<String, Number> metrics) {

        Subscription s = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + subscriptionId));

        if (s.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot evaluate tier for non-ACTIVE subscription.");
        }

        String chosenTier = tierEvaluator.evaluate(metrics);

        // Validate chosen tier is part of the plan
        MembershipPlan plan = planRepository.findById(s.getPlanId())
                .orElseThrow(() -> new IllegalStateException("Plan missing for subscription."));

        boolean tierExists = plan.getTiers().stream()
                .anyMatch(t -> t.getId().equals(chosenTier));

        if (!tierExists) {
            throw new IllegalStateException("Tier evaluator returned invalid tier: " + chosenTier);
        }

        synchronized (s) {
            s.changeTier(chosenTier);
            subscriptionRepository.save(s);
            return s;
        }
    }

    /**
     * Expire all ACTIVE subscriptions whose expiry time has passed.
     */
    public void expireDueSubscriptions() {
        Instant now = Instant.now();

        for (Subscription s : subscriptionRepository.findAll()) {
            if (s.getStatus() == SubscriptionStatus.ACTIVE &&
                    s.getExpiresAt().isBefore(now)) {

                synchronized (s) {
                    s.expire();
                    subscriptionRepository.save(s);
                    System.out.println("Expired subscription: " + s.getId());
                }
            }
        }
    }
}
