package com.precious.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static class GetError extends RuntimeException {
        private final String error;

        public GetError(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}
