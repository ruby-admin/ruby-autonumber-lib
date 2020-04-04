package com.ruby.autonumber.lib.exception;

public class AutoNumberException extends RuntimeException {

    public AutoNumberException(String message) {
        super(message);
    }

    public AutoNumberException(String message, Throwable cause) {
        super(message, cause);
    }

    public AutoNumberException(Throwable cause) {
        super(cause);
    }

}
