package com.atamanahmet.vinylexchange.controller.payment;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.dto.payment.RefundReviewPaymentDto;
import com.atamanahmet.vinylexchange.service.payment.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/refund-review")
    public ResponseEntity<List<RefundReviewPaymentDto>> listRefundReviewPayments() {
        return ResponseEntity.ok(paymentService.listRefundReviewRequired());
    }
}
