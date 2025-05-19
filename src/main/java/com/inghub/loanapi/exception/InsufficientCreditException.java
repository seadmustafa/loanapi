package com.inghub.loanapi.exception;

public class InsufficientCreditException extends RuntimeException {

    public InsufficientCreditException(String message) {
        super(message);
    }

    public InsufficientCreditException(String message, Throwable cause) {
        super(message, cause);
    }
}