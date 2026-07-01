package com.atamanahmet.vinylexchange.mapper;

import java.util.List;

import com.atamanahmet.vinylexchange.domain.entity.PaymentStatusHistory;
import com.atamanahmet.vinylexchange.domain.entity.PaymentTransaction;
import com.atamanahmet.vinylexchange.dto.payment.PaymentHistoryDto;
import com.atamanahmet.vinylexchange.dto.payment.PaymentHistoryEventDto;

public final class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentHistoryDto toHistoryDto(
            PaymentTransaction payment,
            String sellerUsername,
            List<PaymentHistoryEventDto> events) {
        return new PaymentHistoryDto(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getOrder().getOrderNumber(),
                payment.getStatus(),
                payment.getAmountKurus(),
                payment.getCapturedAt(),
                payment.getCreatedAt(),
                payment.isRefundReviewRequired(),
                sellerUsername,
                events);
    }

    public static PaymentHistoryEventDto toEventDto(PaymentStatusHistory history) {
        return new PaymentHistoryEventDto(
                history.getFromStatus(),
                history.getToStatus(),
                history.getOccurredAt(),
                history.getNote());
    }
}
