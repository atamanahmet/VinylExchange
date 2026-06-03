package com.atamanahmet.vinylexchange.service.order;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

@Service
public class PricePreviewService {

        public BigDecimal previewDiscountedPrice(
                        BigDecimal priceTL,
                        BigDecimal discountPercent) {

                if (discountPercent == null || discountPercent.signum() <= 0) {
                        return priceTL.setScale(2, RoundingMode.HALF_UP);
                }

                BigDecimal factor = BigDecimal.ONE.subtract(discountPercent.movePointLeft(2));// 25 to 0.25

                return priceTL
                                .multiply(factor)
                                .setScale(2, RoundingMode.HALF_UP);
        }
}
