package com.inghub.loanapi.exception;

public class InvalidLoanParameterException extends RuntimeException {

    public InvalidLoanParameterException(String message) {
        super(message);
    }

    public InvalidLoanParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
