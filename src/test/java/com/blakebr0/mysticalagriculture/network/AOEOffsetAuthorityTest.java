package com.blakebr0.mysticalagriculture.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AOEOffsetAuthorityTest {
    @Test
    void serverOffsetApplicationSaturatesBeforeNarrowingAtMaximumThirdPartyRange() {
        assertEquals(
                Integer.MAX_VALUE,
                AOEOffsetLimiter.applyOffsetChange(Integer.MAX_VALUE, 1, Integer.MAX_VALUE)
        );
        assertEquals(
                -Integer.MAX_VALUE,
                AOEOffsetLimiter.applyOffsetChange(-Integer.MAX_VALUE, -1, Integer.MAX_VALUE)
        );
        assertEquals(1, AOEOffsetLimiter.applyOffsetChange(0, 1, Integer.MAX_VALUE));
    }
}
