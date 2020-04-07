package com.ruby.autonumber.lib.exception;

import com.ruby.commons.RubyException;

public class SequenceGenerationException extends RubyException {

    public SequenceGenerationException(String message) {
        super(message);
    }

    public SequenceGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SequenceGenerationException(Throwable cause) {
        super(cause);
    }
}
