package com.firstclub.membership;

import com.firstclub.membership.cli.InteractiveCli;
import com.firstclub.membership.repo.InMemoryPlanRepository;
import com.firstclub.membership.repo.InMemorySubscriptionRepository;
import com.firstclub.membership.repo.InMemoryUserRepository;
import com.firstclub.membership.service.MembershipService;
import com.firstclub.membership.service.PlanService;
import com.firstclub.membership.service.SimpleTierEvaluator;
import com.firstclub.membership.service.TierEvaluator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main - application entrypoint. Boots data, scheduler and interactive CLI.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting FirstClub Membership Program\n");

        // Repositories
        var planRepo = new InMemoryPlanRepository();
        var userRepo = new InMemoryUserRepository();
        var subRepo = new InMemorySubscriptionRepository();

        // Services
        PlanService planService = new PlanService(planRepo);
        TierEvaluator tierEvaluator = new SimpleTierEvaluator();
        MembershipService membershipService = new MembershipService(subRepo, planRepo, userRepo, tierEvaluator);

        // Seed some data
        DataBootstrap.seed(planRepo, userRepo);

        // Start scheduler
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                membershipService.expireDueSubscriptions();
            } catch (Throwable t) {
                System.err.println("Error in scheduler: " + t.getMessage());
            }
        }, 10, 30, TimeUnit.SECONDS); // initial delay 10s, repeat every 30s

        // Add shutdown hook to stop scheduler
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutdown requested. Stopping scheduler...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) scheduler.shutdownNow();
            } catch (InterruptedException ignored) {
                scheduler.shutdownNow();
            }
            System.out.println("Shutdown complete.");
        }));

        // Interactive CLI (blocks until user chooses exit)
        InteractiveCli cli = new InteractiveCli(planService, membershipService, userRepo);
        cli.run();

        // After CLI exits, shut down scheduler and exit
        scheduler.shutdown();
        try { scheduler.awaitTermination(2, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
        System.out.println("Application exiting.");
    }
}
