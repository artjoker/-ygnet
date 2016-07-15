package com.cygnet.ourdrive.upload;

import java.util.List;

/**
 * Created by IntelliJ IDEA on 11/10/12.
 *
 * @author werneraltewischer
 */
public class UploadServiceException extends Exception {

    private List<Integer> errorCodes;

    public UploadServiceException() {
        super();
    }

    public UploadServiceException(String message) {
        super(message);
    }

    public UploadServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadServiceException(Throwable cause) {
        super(cause);
    }

    public List<Integer> getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(List<Integer> errorCodes) {
        this.errorCodes = errorCodes;
    }
}
