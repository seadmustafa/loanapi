package com.inghub.loanapi.exception;

public class LoanAlreadyPaidException extends RuntimeException {

    public LoanAlreadyPaidException(String message) {
        super(message);
    }

    public LoanAlreadyPaidException(String message, Throwable cause) {
        super(message, cause);
    }
}