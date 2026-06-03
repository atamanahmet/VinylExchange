package com.atamanahmet.vinylexchange.exception;

public class TokenExpireException extends RuntimeException {
    public TokenExpireException() {
        super("Token Expired. Revoking token..");
    }

    public TokenExpireException(String message) {
        super(message);
    }
}
