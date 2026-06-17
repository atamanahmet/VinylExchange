package com.atamanahmet.vinylexchange.dto.order;

import java.util.List;

import lombok.Builder;

@Builder
public record CartValidationResult(
        CartDTO cartDTO,
        List<CartValidationIssue> issues,
        boolean hasErrors
) {}
