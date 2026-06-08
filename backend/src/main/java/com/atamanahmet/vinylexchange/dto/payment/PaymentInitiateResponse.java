package com.atamanahmet.vinylexchange.dto.payment;

/**
 * Result of a payment initiation attempt
 */
public record PaymentInitiateResponse(
        boolean success,
        String providerPaymentId,
        String checkoutFormContent,
        String errorMessage
) {

    public static PaymentInitiateResponse success(String providerPaymentId, String checkoutFormContent) {
        return new PaymentInitiateResponse(true, providerPaymentId, checkoutFormContent, null);
    }

    public static PaymentInitiateResponse failure(String errorMessage) {
        return new PaymentInitiateResponse(false, null, null, errorMessage);
    }
}