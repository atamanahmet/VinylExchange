package com.atamanahmet.vinylexchange.infrastructure.payment;

import com.atamanahmet.vinylexchange.domain.entity.PaymentTransaction;
import com.atamanahmet.vinylexchange.dto.payment.PaymentInitiateResponse;
import com.atamanahmet.vinylexchange.dto.payment.PaymentVerifyResult;

/**
 * Domain-facing payment port.
 * Services talk to this. Never to iyzico or mock directly.
 */
public interface PaymentPort {


    /**
     * Create Iyzico checkout form, returns HTML content and token
     * Called when user clicks pay, before they enter card info
     */
    PaymentInitiateResponse initiateCheckoutForm(PaymentTransaction payment, String callbackUrl);

    /**
     * Verify callback from Iyzico after user completes 3DS
     * Returns PaymentVerifyResult if payment is successful
     */
    PaymentVerifyResult verifyCallback(String token);

    /**
     * Release held funds to seller after delivery confirmed
     */
    String approvePayoutToSeller(PaymentTransaction payment);

    /**
     * Refund to buyer on cancellation or dispute loss
     */
    void refundToBuyer(PaymentTransaction payment);
}