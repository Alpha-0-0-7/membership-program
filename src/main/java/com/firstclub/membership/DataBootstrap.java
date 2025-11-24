package com.firstclub.membership;

import com.firstclub.membership.model.Benefit;
import com.firstclub.membership.model.MembershipPlan;
import com.firstclub.membership.model.Tier;
import com.firstclub.membership.model.User;
import com.firstclub.membership.repo.PlanRepository;
import com.firstclub.membership.repo.UserRepository;

import java.time.Period;
import java.util.List;

/**
 * Put seed data into repositories for demo and CLI convenience.
 */
public final class DataBootstrap {
    private DataBootstrap() {}

    public static void seed(PlanRepository planRepo, UserRepository userRepo) {
        Tier silver = new Tier("SILVER", "Silver", List.of(
                Benefit.freeDelivery(100), Benefit.discountPercent(5)
        ));
        Tier gold = new Tier("GOLD", "Gold", List.of(
                Benefit.freeDelivery(0), Benefit.discountPercent(10), Benefit.earlyAccess()
        ));
        Tier platinum = new Tier("PLATINUM", "Platinum", List.of(
                Benefit.freeDelivery(0), Benefit.discountPercent(15), Benefit.earlyAccess(), Benefit.prioritySupport()
        ));

        MembershipPlan monthly = new MembershipPlan("MONTHLY", "Monthly membership", 199, Period.ofMonths(1), List.of(silver, gold, platinum));
        MembershipPlan yearly = new MembershipPlan("YEARLY", "Yearly membership", 1799, Period.ofYears(1), List.of(silver, gold, platinum));

        planRepo.save(monthly);
        planRepo.save(yearly);

        userRepo.save(new User("user-1", "Demo User"));
    }
}
