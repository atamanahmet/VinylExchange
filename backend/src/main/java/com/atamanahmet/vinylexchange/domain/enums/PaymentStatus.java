package com.atamanahmet.vinylexchange.domain.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum PaymentStatus {

    PENDING_PAYMENT,

    /** Money held in escrow, awaiting delivery confirmation */
    HELD,

    /** Payout approved, money being sent to seller */
    RELEASED,

    /** Payout complete, terminal */
    COMPLETED,

    /** Refund issued to buyer, terminal */
    REFUNDED,

    /** Cancelled before capture, terminal */
    CANCELLED;

    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = Map.of(
            PENDING_PAYMENT, Set.copyOf(EnumSet.of(HELD, CANCELLED)),
            HELD, Set.copyOf(EnumSet.of(RELEASED, REFUNDED)),
            RELEASED, Set.copyOf(EnumSet.of(COMPLETED)),
            COMPLETED, Set.copyOf(EnumSet.noneOf(PaymentStatus.class)),
            REFUNDED, Set.copyOf(EnumSet.noneOf(PaymentStatus.class)),
            CANCELLED, Set.copyOf(EnumSet.noneOf(PaymentStatus.class)));

    public boolean canTransitionTo(PaymentStatus newStatus) {
        Set<PaymentStatus> allowed = VALID_TRANSITIONS.get(this);
        return allowed != null && allowed.contains(newStatus);
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == REFUNDED || this == CANCELLED;
    }
}
