package com.atamanahmet.vinylexchange.controller.order;

import com.atamanahmet.vinylexchange.dto.payment.PaymentCallbackOutcome;
import com.atamanahmet.vinylexchange.dto.payment.PaymentInitiateRequest;
import com.atamanahmet.vinylexchange.dto.payment.PaymentInitiateResponse;
import com.atamanahmet.vinylexchange.service.payment.PaymentService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${app.frontend-url}")
    private String frontendBaseUrl;

    private final PaymentService paymentService;

    /**
     * Frontend calls this after checkout to get Iyzico form HTML
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @RequestBody @Valid PaymentInitiateRequest request) {
        return ResponseEntity.ok(
                paymentService.initiatePayment(request.orderId(), UserUtil.getCurrentUserId()));
    }

    /**
     * Iyzico POSTs here after user's payment completes or fails
     * Not authenticated, Iyzico server calls this, not the user
     */
    @PostMapping("/callback")
    public ResponseEntity<Void> paymentCallback(
            @RequestParam(required = false) String token,
            HttpServletRequest request) {

        log.info("Payment callback received token={}", token);

        if (token == null) {
            return ResponseEntity.badRequest().build();
        }

        PaymentCallbackOutcome outcome = paymentService.handleCallback(token);

        String frontendUrl = switch (outcome) {
            case PROCESSED, ALREADY_HELD ->
                    frontendBaseUrl + "/payment/result?status=success";
            case REFUND_REVIEW_REQUIRED ->
                    frontendBaseUrl + "/payment/result?status=refund-review";
            case VERIFICATION_FAILED ->
                    frontendBaseUrl + "/payment/result?status=failure";
        };

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendUrl)
                .build();
    }
}