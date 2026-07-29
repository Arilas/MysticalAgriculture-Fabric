package com.blakebr0.mysticalagriculture.network;

final class AOEOffsetLimiter {
    private AOEOffsetLimiter() {
    }

    static int applyOffsetChange(int current, int change, int range) {
        if (range < 0) {
            throw new IllegalArgumentException("range must be non-negative");
        }

        long minimum = -(long) range;
        long maximum = range;
        long boundedCurrent = Math.max(minimum, Math.min(maximum, (long) current));
        long changed = boundedCurrent + change;
        return (int) Math.max(minimum, Math.min(maximum, changed));
    }
}
