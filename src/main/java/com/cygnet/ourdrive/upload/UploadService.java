package com.cygnet.ourdrive.upload;

import com.cygnet.ourdrive.util.HttpUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Cygnet upload service
 *
 * @author werneraltewischer
 */
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
    private static final String DIRECTORY_MIME = "application/directory";
    private static final String DEFAULT_CHARSET = "utf-8";

    private static final String USER_PARAM = "user";
    private static final String PASSWORD_PARAM = "password";
    private static final String API_KEY_PARAM = "apiKey";
    private static final String TARGET_PARAM = "target";
    private static final String DOCUMENT_PARAM = "document";

    private static final String DOC_ID_PARAM = "docId";
    private static final String ERRORS_PARAM = "errors";
    private static final String ERROR_ID_PARAM = "id";
    private static final String ERROR_DESCRIPTION_PARAM = "description";

    private static final int SC_OK = 200;

    private final String username;
    private final String password;
    private final String apiKey;
    private final String url;
    private final String targetContainer;

    public UploadService(String username, String password, String apiKey, String url, String targetContainer) {
        this.username = username;
        this.password = password;
        this.apiKey = apiKey;
        this.url = url;
        this.targetContainer = targetContainer;
    }

    /**
     * Create multipart entry. Fill default parameters
     *
     * @return the multipart entry
     * @throws UnsupportedEncodingException
     */
    private MultipartEntity createRequest() throws UnsupportedEncodingException {
        MultipartEntity reqEntity = new MultipartEntity();

        setStringParameterIfDefined(reqEntity, USER_PARAM, getUsername());

        if (!StringUtils.isEmpty(getPassword()) || StringUtils.isEmpty(getApiKey())) {
            setStringParameterIfDefined(reqEntity, PASSWORD_PARAM, getPassword());
        } else {
            setStringParameterIfDefined(reqEntity, API_KEY_PARAM, getApiKey());
        }
        setStringParameterIfDefined(reqEntity, TARGET_PARAM, getTargetContainer());
        return reqEntity;
    }


    /**
     * Upload file
     *
     * @param inputStream file stream
     * @param filename filename
     * @return ID of uploaded object
     * @throws UploadServiceException
     */
    @SuppressWarnings("unchecked")
    public String uploadFile(InputStream inputStream, String filename) throws UploadServiceException {
        String docId = null;
        HttpClient httpClient = null;
        try {
            httpClient = new HttpUtils().getAllHostVerifiedClient();
            HttpPost httpPost = new HttpPost(getUrl());
            MultipartEntity reqEntity = createRequest();

            logger.info("Uploading file '" + filename + "' for user " + getUsername() + " into " + StringUtils.defaultIfBlank(getTargetContainer(), "InTray"));

            InputStreamBody document = new InputStreamBody(inputStream, filename);
            reqEntity.addPart(DOCUMENT_PARAM, document);

            httpPost.setEntity(reqEntity);

            logger.info("Executing request: " + httpPost.getRequestLine());
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            logger.info("Response status: " + response.getStatusLine());
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != SC_OK) {
                throw new UploadServiceException("Upload was not successful, http status code returned was: " + statusCode);
            } else if (resEntity != null) {
                logger.info("Response content length: " + resEntity.getContentLength());

                final InputStream responseStream = resEntity.getContent();
                docId = processResponse(responseStream);
                IOUtils.closeQuietly(responseStream);
            } else {
                throw new UploadServiceException("No valid response received");
            }
        } catch (UploadServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Exception occurred during uploading", e);
            throw new UploadServiceException("Could not upload: " + e.getMessage(), e);
        } finally {
            try {
                if (httpClient != null) httpClient.getConnectionManager().shutdown();
            } catch (Exception ignore) {
                logger.info("http client close warning:", ignore);
            }
        }
        return docId;
    }

    /**
     * Upload directory
     *
     * @param dir directory for upload
     * @return ID of created container
     * @throws UploadServiceException
     */
    @SuppressWarnings("unchecked")
    public String uploadDirectory(File dir) throws UploadServiceException {
        File tempDirectory = null;
        HttpClient httpClient = null;
        String docId = null;
        try {
            httpClient = new HttpUtils().getAllHostVerifiedClient();
            HttpPost httpPost = new HttpPost(getUrl());
            MultipartEntity reqEntity = createRequest();

            logger.info("Uploading directory '" + dir.getAbsolutePath() + "' for user " + getUsername() + " into " + getTargetContainer());

            tempDirectory = File.createTempFile(prefix(dir.getName()), ".dir");
            FileBody document = new FileBody(tempDirectory, dir.getName(), DIRECTORY_MIME, DEFAULT_CHARSET);
            reqEntity.addPart(DOCUMENT_PARAM, document);

            httpPost.setEntity(reqEntity);

            logger.info("Executing request: " + httpPost.getRequestLine());
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            logger.info("Response status: " + response.getStatusLine());
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != SC_OK) {
                throw new UploadServiceException("Upload was not successful, http status code returned was: " + statusCode);
            } else if (resEntity != null) {
                logger.info("Response content length: " + resEntity.getContentLength());

                final InputStream responseStream = resEntity.getContent();
                docId = processResponse(responseStream);
                IOUtils.closeQuietly(responseStream);
            } else {
                throw new UploadServiceException("No valid response received");
            }
        } catch (UploadServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Exception occurred during uploading", e);
            throw new UploadServiceException("Could not upload: " + e.getMessage(), e);
        } finally {
            try {
                if (httpClient != null) httpClient.getConnectionManager().shutdown();
                //Drop temporary file
                if(tempDirectory != null) {
                    tempDirectory.delete();
                }
            } catch (Exception ignore) {
                //ignore
            }
        }
        return docId;
    }

    private String prefix(String name) {
        return String.valueOf(System.currentTimeMillis()) + name;
    }

    /**
     * Process upload response
     *
     * @param responseStream the stream
     * @return ID of created object
     * @throws java.io.IOException
     * @throws UploadServiceException
     */
    private String processResponse(InputStream responseStream) throws java.io.IOException, UploadServiceException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map rootAsMap = objectMapper.readValue(responseStream, Map.class);

        Object o = rootAsMap.get(DOC_ID_PARAM);
        String docId = o == null ? null : o.toString();
        o = rootAsMap.get(ERRORS_PARAM);
        Collection<Map> errors = null;
        if (o instanceof Collection) {
            errors = (Collection<Map>) o;
        }
        List<Integer> errorCodes = new ArrayList<Integer>();
        if (errors != null && errors.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map errorMap : errors) {
                Integer errorCode = null;
                Object errorCodeObject = errorMap.get(ERROR_ID_PARAM);
                try {
                    errorCode = errorCodeObject != null ? Integer.parseInt(errorCodeObject.toString()) : null;
                } catch (NumberFormatException e) {
                    logger.warn("Could not parse error code: " + e);
                }
                final String errorString = "\nError " + errorCode + ": " + errorMap.get(ERROR_DESCRIPTION_PARAM);
                sb.append(errorString);
                if (errorCode != null) {
                    errorCodes.add(errorCode);
                }
            }
            final UploadServiceException uploadServiceException = new UploadServiceException("Server returned errors: " + sb.toString());
            uploadServiceException.setErrorCodes(errorCodes);
            throw uploadServiceException;
        } else if (docId == null) {
            throw new UploadServiceException("Upload did not return a valid doc ID");
        } else {
            logger.info("Successfully uploaded document with ID: " + docId);
        }
        return docId;
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
