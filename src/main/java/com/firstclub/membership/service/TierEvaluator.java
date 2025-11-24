package com.firstclub.membership.service;

import java.util.Map;

public interface TierEvaluator {
    String evaluate(Map<String, Number> metrics);
}
