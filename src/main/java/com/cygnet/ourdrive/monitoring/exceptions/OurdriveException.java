package com.cygnet.ourdrive.monitoring.exceptions;

/**
 * Base exception for uploading process
 */
public class OurdriveException extends Exception {

    public OurdriveException() {
        super();
    }

    public OurdriveException(String message) {
        super(message);
    }

    public OurdriveException(String message, Throwable cause) {
        super(message, cause);
    }

    public OurdriveException(Throwable cause) {
        super(cause);
    }
}
