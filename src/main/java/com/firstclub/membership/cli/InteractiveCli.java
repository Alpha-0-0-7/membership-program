package com.firstclub.membership.cli;

import com.firstclub.membership.model.MembershipPlan;
import com.firstclub.membership.model.Subscription;
import com.firstclub.membership.model.User;
import com.firstclub.membership.repo.UserRepository;
import com.firstclub.membership.service.MembershipService;
import com.firstclub.membership.service.PlanService;

import java.util.*;

/**
 * InteractiveCli - presents a simple textual menu. Runs on main thread and returns on exit.
 */
public class InteractiveCli {
    private final PlanService planService;
    private final MembershipService membershipService;
    private final UserRepository userRepository;
    private final Scanner scanner = new Scanner(System.in);

    public InteractiveCli(PlanService planService, MembershipService membershipService, UserRepository userRepository) {
        this.planService = planService;
        this.membershipService = membershipService;
        this.userRepository = userRepository;
    }

    public void run() {
        System.out.println("Interactive CLI started. Type number to choose option.\n");
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> listPlans();
                    case "2" -> createSubscription();
                    case "3" -> listUserSubscriptions();
                    case "4" -> upgradeDowngradeSubscription();
                    case "5" -> cancelSubscription();
                    case "6" -> evaluateSubscription();
                    case "7" -> running = false;
                    default -> System.out.println("Unknown option. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }

        System.out.println("Exiting CLI...");
    }

    private void printMenu() {
        System.out.println("\n=== FirstClub CLI ===");
        System.out.println("1) List plans");
        System.out.println("2) Create subscription");
        System.out.println("3) List user's subscriptions");
        System.out.println("4) Upgrade/downgrade subscription");
        System.out.println("5) Cancel subscription");
        System.out.println("6) Evaluate subscription (apply metrics)");
        System.out.println("7) Exit");
        System.out.print("Choose: ");
    }


    // 1. LIST PLANS

    private void listPlans() {
        Collection<MembershipPlan> plans = planService.listPlans();
        if (plans.isEmpty()) {
            System.out.println("No plans available.");
            return;
        }

        System.out.println("Plans:");
        plans.forEach(p -> {
            System.out.println(" - " + p.brief());
            p.getTiers().forEach(t -> System.out.println("    * " + t.brief()));
        });
    }


    // 2. CREATE SUBSCRIPTION

    private void createSubscription() {
        try {
            System.out.print("Enter userId (existing): ");
            String userId = scanner.nextLine().trim();

            Optional<User> u = userRepository.findById(userId);
            if (u.isEmpty()) {
                System.out.println("Error: user not found.");
                return;
            }

            System.out.print("Enter planId: ");
            String planId = scanner.nextLine().trim();

            Optional<MembershipPlan> p = planService.getPlan(planId);
            if (p.isEmpty()) {
                System.out.println("Error: plan not found.");
                return;
            }

            System.out.print("Enter tierId: ");
            String tierId = scanner.nextLine().trim();

            // THIS MAY THROW -> caught below
            Subscription created = membershipService.subscribe(userId, planId, tierId);

            System.out.println("Created:");
            System.out.println(pretty(created));
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }


    // 3. LIST USER SUBSCRIPTIONS

    private void listUserSubscriptions() {
        try {
            System.out.print("Enter userId: ");
            String userId = scanner.nextLine().trim();

            List<Subscription> subs = membershipService.getSubscriptionsForUser(userId);

            if (subs.isEmpty()) {
                System.out.println("No subscriptions found for user.");
                return;
            }

            System.out.println("Subscriptions for user " + userId + ":");
            subs.forEach(s -> System.out.println(pretty(s)));

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }


    // 4. UPGRADE / DOWNGRADE

    private void upgradeDowngradeSubscription() {
        try {
            System.out.print("Enter subscriptionId: ");
            String id = scanner.nextLine().trim();

            System.out.print("Enter newTierId: ");
            String newTierId = scanner.nextLine().trim();

            Subscription updated = membershipService.upgradeOrDowngrade(id, newTierId);

            System.out.println("Updated:");
            System.out.println(pretty(updated));
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }


    // 5. CANCEL SUBSCRIPTION

    private void cancelSubscription() {
        try {
            System.out.print("Enter subscriptionId: ");
            String id = scanner.nextLine().trim();

            membershipService.cancelSubscription(id);

            System.out.println("Cancelled subscription: " + id);
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }


    // 6. EVALUATE METRICS

    private void evaluateSubscription() {
        try {
            System.out.print("Enter subscriptionId: ");
            String id = scanner.nextLine().trim();

            Map<String, Number> metrics = new HashMap<>();

            System.out.print("ordersCount (integer): ");
            String ordersStr = scanner.nextLine().trim();
            if (!ordersStr.isEmpty()) {
                metrics.put("ordersCount", Integer.parseInt(ordersStr));
            }

            System.out.print("orderValue (double): ");
            String valueStr = scanner.nextLine().trim();
            if (!valueStr.isEmpty()) {
                metrics.put("orderValue", Double.parseDouble(valueStr));
            }

            if (metrics.isEmpty()) {
                System.out.println("No metrics provided.");
                return;
            }

            Subscription updated = membershipService.evaluateAndApplyTier(id, metrics);

            System.out.println("After evaluation:");
            System.out.println(pretty(updated));

        } catch (NumberFormatException ex) {
            System.out.println("Invalid numeric input.");
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }


    // PRETTY JSON OUTPUT FOR SUBSCRIPTION

    private String pretty(Subscription s) {
        return "{\n" +
                "  \"id\": \"" + s.getId() + "\",\n" +
                "  \"userId\": \"" + s.getUserId() + "\",\n" +
                "  \"planId\": \"" + s.getPlanId() + "\",\n" +
                "  \"tierId\": \"" + s.getTierId() + "\",\n" +
                "  \"startAt\": \"" + s.getStartAt() + "\",\n" +
                "  \"expiresAt\": \"" + s.getExpiresAt() + "\",\n" +
                "  \"status\": \"" + s.getStatus() + "\"\n" +
                "}";
    }
}
