package com.atamanahmet.vinylexchange.dto.order;

import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.ErrorType;
import com.atamanahmet.vinylexchange.domain.enums.IssueType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartValidationIssue {
    public static Object IssueType;
    private UUID cartItemId;
    private UUID listingId;

    @Enumerated(EnumType.STRING)
    private IssueType type;

    @Enumerated(EnumType.STRING)
    private ErrorType errorType;
    private String message;




}
