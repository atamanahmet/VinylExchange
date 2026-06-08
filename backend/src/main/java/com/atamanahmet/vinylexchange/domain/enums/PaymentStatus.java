package com.atamanahmet.vinylexchange.domain.enums;

public enum PaymentStatus {

    PENDING_PAYMENT,

    /** Payment captured from buyer */
    CAPTURED,

    /** Money held, awaiting delivery confirmation */
    HELD,

    /** Payout approved, money being sent to seller */
    RELEASED,

    /** Payout complete, terminal */
    COMPLETED,

    /** Refund issued to buyer, terminal */
    REFUNDED,

    /** Order canceled before capture, terminal */
    CANCELLED
}