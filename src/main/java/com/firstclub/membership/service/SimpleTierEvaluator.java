package com.firstclub.membership.service;

import java.util.Map;

/**
 * Simple rules:
 *  - PLATINUM if ordersCount >= 10 || orderValue >= 50000
 *  - GOLD if ordersCount >= 5 || orderValue >= 10000
 *  - else SILVER
 */
public class SimpleTierEvaluator implements TierEvaluator {
    @Override
    public String evaluate(Map<String, Number> metrics) {
        Number ordersN = metrics.getOrDefault("ordersCount", 0);
        Number valueN = metrics.getOrDefault("orderValue", 0);
        int orders = ordersN.intValue();
        double value = valueN.doubleValue();
        if (orders >= 10 || value >= 50000.0) return "PLATINUM";
        if (orders >= 5  || value >= 10000.0) return "GOLD";
        return "SILVER";
    }
}
