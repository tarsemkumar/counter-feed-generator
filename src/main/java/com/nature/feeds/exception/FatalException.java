package com.nature.feeds.exception;

public class FatalException extends RuntimeException {

    private static final long serialVersionUID = 979058999309689316L;

    public FatalException() {
        super();
    }

    public FatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public FatalException(String message) {
        super(message);
    }

    public FatalException(Throwable cause) {
        super(cause);
    }
}
