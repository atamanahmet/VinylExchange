package com.atamanahmet.vinylexchange.dto.order;

import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.ErrorType;
import com.atamanahmet.vinylexchange.domain.enums.IssueType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CartValidationIssue {
    private UUID cartItemId;
    private UUID listingId;
    private IssueType type;
    private ErrorType errorType;
    private String message;
}
