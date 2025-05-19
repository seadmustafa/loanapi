package com.inghub.loanapi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class ErrorResponse {
    private final int status;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;
}
