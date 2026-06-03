package com.atamanahmet.vinylexchange.exception;

import java.util.UUID;

public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(UUID userId) {
        super("Unauthorized action with userId: " + userId);
    }

    public UnauthorizedActionException(String message, UUID userId) {
        super(message + userId);
    }

    public UnauthorizedActionException(String message) {
        super(message);
    }

}
