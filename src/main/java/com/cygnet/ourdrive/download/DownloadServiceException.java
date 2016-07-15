package com.cygnet.ourdrive.download;

import java.util.List;

/**
 * Created by IntelliJ IDEA on 11/10/12.
 *
 * @author werneraltewischer
 */
public class DownloadServiceException extends Exception {

    private List<Integer> errorCodes;

    public DownloadServiceException() {
        super();
    }

    public DownloadServiceException(String message) {
        super(message);
    }

    public DownloadServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DownloadServiceException(Throwable cause) {
        super(cause);
    }

    public List<Integer> getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(List<Integer> errorCodes) {
        this.errorCodes = errorCodes;
    }
}
