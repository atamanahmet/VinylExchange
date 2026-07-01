package com.atamanahmet.vinylexchange.dto.payment;

public enum PaymentCallbackOutcome {
    PROCESSED,
    ALREADY_HELD,
    REFUND_REVIEW_REQUIRED,
    VERIFICATION_FAILED
}
