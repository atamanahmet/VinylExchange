package com.atamanahmet.vinylexchange.exception;

import com.atamanahmet.vinylexchange.domain.enums.UserStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(UserStatus oldStatus, UserStatus newStatus) {
        super("Cannot transition from " + oldStatus + " to " + newStatus);
    }

}
