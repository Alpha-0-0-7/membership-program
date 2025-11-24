package com.firstclub.membership.service;

import com.firstclub.membership.DataBootstrap;
import com.firstclub.membership.model.MembershipPlan;
import com.firstclub.membership.model.Subscription;
import com.firstclub.membership.model.SubscriptionStatus;
import com.firstclub.membership.repo.InMemoryPlanRepository;
import com.firstclub.membership.repo.InMemorySubscriptionRepository;
import com.firstclub.membership.repo.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class MembershipServiceTest {

    private InMemoryPlanRepository planRepo;
    private InMemoryUserRepository userRepo;
    private InMemorySubscriptionRepository subRepo;
    private MembershipService service;

    @BeforeEach
    void setup() {
        planRepo = new InMemoryPlanRepository();
        userRepo = new InMemoryUserRepository();
        subRepo = new InMemorySubscriptionRepository();

        DataBootstrap.seed(planRepo, userRepo);
        TierEvaluator evaluator = new SimpleTierEvaluator();

        service = new MembershipService(subRepo, planRepo, userRepo, evaluator);
    }

    // ------------------------------------------------------------
    // Subscription creation
    // ------------------------------------------------------------
    @Test
    void testSubscriptionCreation() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");

        assertNotNull(s.getId());
        assertEquals("user-1", s.getUserId());
        assertEquals("MONTHLY", s.getPlanId());
        assertEquals("SILVER", s.getTierId());
        assertEquals(SubscriptionStatus.ACTIVE, s.getStatus());

        assertTrue(s.getExpiresAt().isAfter(Instant.now()));
    }

    // NEW TEST — Prevent creating more than one ACTIVE subscription
    @Test
    void testPreventMultipleActiveSubscriptions() {
        service.subscribe("user-1", "MONTHLY", "SILVER");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.subscribe("user-1", "YEARLY", "GOLD")
        );

        assertEquals("User already has an ACTIVE subscription.", ex.getMessage());
    }

    // NEW TEST — Allow subscription after cancellation
    @Test
    void testSubscribeAfterCancellation() {
        Subscription s1 = service.subscribe("user-1", "MONTHLY", "SILVER");
        service.cancelSubscription(s1.getId());

        // Now allowed
        Subscription s2 = service.subscribe("user-1", "MONTHLY", "GOLD");

        assertEquals("GOLD", s2.getTierId());
        assertEquals(2, service.getSubscriptionsForUser("user-1").size());
    }

    // NEW TEST — Allow subscription after expiry
    @Test
    void testSubscribeAfterExpiry() {
        Subscription s1 = service.subscribe("user-1", "MONTHLY", "SILVER");

        // Force expiry
        s1.extendExpiry(Instant.now().minusSeconds(10));
        service.expireDueSubscriptions();

        // Now allowed
        Subscription s2 = service.subscribe("user-1", "MONTHLY", "GOLD");

        assertEquals("GOLD", s2.getTierId());
    }

    // ------------------------------------------------------------
    // Upgrade / Downgrade subscription
    // ------------------------------------------------------------
    @Test
    void testUpgradeSubscription() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");
        Subscription updated = service.upgradeOrDowngrade(s.getId(), "PLATINUM");

        assertEquals("PLATINUM", updated.getTierId());
    }

    // NEW TEST — Prevent upgrade on EXPIRED
    @Test
    void testUpgradeFailsOnExpired() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");

        // force expiry
        s.extendExpiry(Instant.now().minusSeconds(10));
        service.expireDueSubscriptions();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.upgradeOrDowngrade(s.getId(), "GOLD")
        );

        assertEquals("Only ACTIVE subscriptions can change tiers.", ex.getMessage());
    }

    // NEW TEST — Prevent upgrade on CANCELLED
    @Test
    void testUpgradeFailsOnCancelled() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");
        service.cancelSubscription(s.getId());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.upgradeOrDowngrade(s.getId(), "GOLD")
        );

        assertEquals("Only ACTIVE subscriptions can change tiers.", ex.getMessage());
    }

    // NEW TEST – Prevent upgrading to tier not in plan
    @Test
    void testUpgradeToInvalidTierFails() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.upgradeOrDowngrade(s.getId(), "INVALID")
        );

        assertTrue(ex.getMessage().contains("not part of plan"));
    }

    // ------------------------------------------------------------
    // Cancel subscription
    // ------------------------------------------------------------
    @Test
    void testCancelSubscription() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");

        service.cancelSubscription(s.getId());
        Subscription cancelled = service.getSubscription(s.getId()).orElseThrow();

        assertEquals(SubscriptionStatus.CANCELLED, cancelled.getStatus());
    }

    // ------------------------------------------------------------
    // List user subscriptions (UPDATED)
    // ------------------------------------------------------------
    @Test
    void testListUserSubscriptions() {
        // First subscription ACTIVE
        Subscription s1 = service.subscribe("user-1", "MONTHLY", "SILVER");

        // Cancel it
        service.cancelSubscription(s1.getId());

        // Second subscription ACTIVE
        Subscription s2 = service.subscribe("user-1", "YEARLY", "GOLD");

        List<Subscription> list = service.getSubscriptionsForUser("user-1");
        assertEquals(2, list.size());
    }

    // ------------------------------------------------------------
    // Tier Evaluation – High metrics (upgrade)
    // ------------------------------------------------------------
    @Test
    void testEvaluateHighMetrics() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");

        Subscription evaluated = service.evaluateAndApplyTier(
                s.getId(),
                Map.of("ordersCount", 12, "orderValue", 60000)
        );

        assertEquals("PLATINUM", evaluated.getTierId());
    }

    // NEW TEST – Prevent evaluating CANCELLED
    @Test
    void testEvaluateFailsOnCancelled() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");
        service.cancelSubscription(s.getId());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.evaluateAndApplyTier(s.getId(), Map.of("ordersCount", 10))
        );

        assertEquals("Cannot evaluate tier for non-ACTIVE subscription.", ex.getMessage());
    }

    // ------------------------------------------------------------
    // Tier Evaluation – Low metrics
    // ------------------------------------------------------------
    @Test
    void testEvaluateLowMetrics() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");

        Subscription evaluated = service.evaluateAndApplyTier(
                s.getId(),
                Map.of("ordersCount", 1, "orderValue", 100)
        );

        assertEquals("SILVER", evaluated.getTierId());
    }

    // ------------------------------------------------------------
    // Expiry Logic
    // ------------------------------------------------------------
    @Test
    void testAutoExpiry() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");

        // Force expiry
        Subscription loaded = service.getSubscription(s.getId()).orElseThrow();
        loaded.extendExpiry(Instant.now().minusSeconds(10));

        service.expireDueSubscriptions();

        Subscription expired = service.getSubscription(s.getId()).orElseThrow();
        assertEquals(SubscriptionStatus.EXPIRED, expired.getStatus());
    }

    // ------------------------------------------------------------
    // Repository Behavior
    // ------------------------------------------------------------
    @Test
    void testRepositorySaveAndFind() {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");

        assertTrue(subRepo.findById(s.getId()).isPresent());
        assertEquals(1, subRepo.findByUserId("user-1").size());
        assertEquals(1, subRepo.findAll().size());
    }

    // ------------------------------------------------------------
    // Thread Safety — Concurrent tier changes
    // ------------------------------------------------------------
    @Test
    void testThreadSafetyForTierChanges() throws Exception {
        Subscription s = service.subscribe("user-1", "MONTHLY", "SILVER");

        ExecutorService executor = Executors.newFixedThreadPool(10);

        Callable<Void> task = () -> {
            service.upgradeOrDowngrade(s.getId(), "GOLD");
            service.upgradeOrDowngrade(s.getId(), "PLATINUM");
            service.upgradeOrDowngrade(s.getId(), "SILVER");
            return null;
        };

        List<Future<Void>> futures =
                executor.invokeAll(java.util.Collections.nCopies(20, task));

        for (Future<Void> f : futures) {
            f.get();
        }

        executor.shutdown();

        Subscription finalState =
                service.getSubscription(s.getId()).orElseThrow();

        assertNotNull(finalState.getTierId());
    }

    // ------------------------------------------------------------
    // Validate plans from DataBootstrap
    // ------------------------------------------------------------
    @Test
    void testPlansLoadedFromBootstrap() {
        assertTrue(planRepo.findById("MONTHLY").isPresent());
        assertTrue(planRepo.findById("YEARLY").isPresent());

        MembershipPlan monthly = planRepo.findById("MONTHLY").orElseThrow();
        assertFalse(monthly.getTiers().isEmpty());
    }
}
