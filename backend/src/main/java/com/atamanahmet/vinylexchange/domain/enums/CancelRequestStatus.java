package com.atamanahmet.vinylexchange.domain.enums;

public enum CancelRequestStatus {

    /** Waiting for seller or admin response */
    PENDING,

    /** Approved by seller or admin */
    APPROVED,

    /** Rejected by seller or admin */
    REJECTED,

    /** Buyer withdrew the request */
    WITHDRAWN
}
