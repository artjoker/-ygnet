package com.cygnet.ourdrive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA on 11/11/12.
 *
 * @author werneraltewischer
 */
public class UploadEventLogger {

    private static final Logger logger = LoggerFactory.getLogger(UploadEventLogger.class);

    public void logSuccess(String username, File file, String docId) {
        String folderName = file.getParent();
        String fileName = file.getName();
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        String dateString = df.format(date);
        logger.debug(dateString + "\t" + username + "\t" + folderName + "\t" + fileName + "\t" + docId);
    }

    public void logFailure(String username, File file, Exception e) {
    }
}
