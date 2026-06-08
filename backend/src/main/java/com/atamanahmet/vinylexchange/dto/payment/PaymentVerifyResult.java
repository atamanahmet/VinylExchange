package com.atamanahmet.vinylexchange.dto.payment;

/**
 * Parsed result from Iyzico CF-Retrieve
 * Carries only fields we care about
 */
public record PaymentVerifyResult(
        boolean success,
        String paymentStatus,
        String providerInternalPaymentId,
        String authCode,
        String hostReference,
        Integer fraudStatus
) {
    public static PaymentVerifyResult failure() {
        return new PaymentVerifyResult(false, "FAILURE", null, null, null, null);
    }
}