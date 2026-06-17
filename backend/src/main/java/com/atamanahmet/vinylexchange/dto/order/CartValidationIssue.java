package com.atamanahmet.vinylexchange.dto.order;

import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.ErrorType;
import com.atamanahmet.vinylexchange.domain.enums.IssueType;
import lombok.Builder;

@Builder
public record CartValidationIssue(
        UUID cartItemId,
        UUID listingId,
        IssueType type,
        ErrorType errorType,
        String message
) {}
