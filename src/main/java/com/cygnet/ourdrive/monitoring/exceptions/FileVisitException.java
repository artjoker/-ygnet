package com.cygnet.ourdrive.monitoring.exceptions;

/**
 * Exception when file visiting is failed
 */
public class FileVisitException extends OurdriveException {

    public FileVisitException() {
        super();
    }

    public FileVisitException(String message) {
        super(message);
    }

    public FileVisitException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileVisitException(Throwable cause) {
        super(cause);
    }
}
