package com.cygnet.ourdrive.download;

import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by IntelliJ IDEA on 11/10/12.
 *
 * @author werneraltewischer
 */
public class DownloadService {

    private static final Logger logger = LoggerFactory.getLogger(DownloadService.class);

    private String downlaod_path;

    private final String username;
    private final String password;
    private final String apiKey;
    private final String url;
    private final String targetContainer;

    public DownloadService(String username, String password, String apiKey, String url, String targetContainer) {
        this.username = username;
        this.password = password;
        this.apiKey = apiKey;
        this.url = url;
        this.targetContainer = targetContainer;
    }

    @SuppressWarnings("unchecked")
//    public String download(InputStream inputStream, String filename) throws DownloadServiceException {
    public String download(InputStream inputStream, String filename) {
        return "";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getUrl() {
        return url;
    }

    public String getTargetContainer() {
        return targetContainer;
    }

    //Private methods

    private void setStringParameterIfDefined(MultipartEntity entity, String parameterName, String parameterValue) throws UnsupportedEncodingException {
        if (!StringUtils.isEmpty(parameterValue)) {
            StringBody sb = new StringBody(parameterValue);
            entity.addPart(parameterName, sb);
        }
    }
}
