package com.atamanahmet.vinylexchange.infrastructure.payment;

import com.atamanahmet.vinylexchange.domain.entity.PaymentTransaction;
import com.atamanahmet.vinylexchange.dto.payment.PaymentInitiateResponse;
import com.atamanahmet.vinylexchange.dto.payment.PaymentVerifyResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Demo adapter, active when payment.provider=mock (default)
 * Generates fake IDs, logs everything, no network calls
 */
@Slf4j
@Component
@ConditionalOnProperty(
        name = "payment.provider",
        havingValue = "mock",
        matchIfMissing = true
)
public class MockPaymentAdapter implements PaymentPort {

    @Override
    public PaymentInitiateResponse initiateCheckoutForm(PaymentTransaction payment, String callbackUrl) {
        String fakeToken = "MOCK-TOKEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[MOCK] Checkout form initiated payment={} token={}", payment.getId(), fakeToken);
        return PaymentInitiateResponse.success(fakeToken, "<div>MOCK PAYMENT FORM</div>");
    }

    @Override
    public PaymentVerifyResult verifyCallback(String token) {
        log.info("[MOCK] Callback verified token={}", token);
        return new PaymentVerifyResult(true, "SUCCESS", "MOCK-PAY-ID", "MOCK-AUTH", "MOCK-HOST-REF", 1);
    }

    @Override
    public String approvePayoutToSeller(PaymentTransaction payment) {
        String fakePayoutId = "MOCK-PAYOUT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[MOCK] Payout approved payment={} payoutId={}", payment.getId(), fakePayoutId);
        return fakePayoutId;
    }

    @Override
    public void refundToBuyer(PaymentTransaction payment) {
        log.info("[MOCK] Refund issued payment={} amount={}kurus",
                payment.getId(), payment.getAmountKurus());
    }
}