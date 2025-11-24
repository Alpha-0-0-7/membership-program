package com.firstclub.membership.util;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

public final class TimeUtil {
    private TimeUtil() {}
    public static Instant plusPeriod(Instant now, Period p) {
        Objects.requireNonNull(now);
        Objects.requireNonNull(p);
        ZonedDateTime z = now.atZone(ZoneOffset.UTC).plus(p);
        return z.toInstant();
    }
}
