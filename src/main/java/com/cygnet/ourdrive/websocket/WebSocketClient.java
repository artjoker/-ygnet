package com.cygnet.ourdrive.websocket;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.gui.Desktop;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.settings.ReadProperties;
import com.cygnet.ourdrive.util.StringDecoder;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 *
 */
public class WebSocketClient {

    public static final String OURDRIVE_FILE_DOWNLOAD = "ourdrive_file_download";
    public static final String OURDRIVE_FILE_DOWNLOAD_SUCCESSFUL = "ourdrive_file_download_successful";
    public static final String OURDRIVE_FILE_UPLOAD = "ourdrive_file_upload";
    public static final String OURDRIVE_FILE_UPLOAD_SUCCESSFUL = "ourdrive_file_upload_successful";
    public static final String OURDRIVE_JOIN_ROOM = "room";

    private static WebSocketClient instance;

    private Properties properties;
    private Path downloadPath;
    private GlobalSettings globalSettings;

    private Socket socket;
    private String socket_uri;

    private Desktop desktop = null;

    private Boolean isSocketConnection = false;

    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    public static synchronized WebSocketClient getInstance() {
        if (instance == null) {
            try {
                instance = new WebSocketClient();
            } catch (Exception e) {
                logger.error("Get instance of web socket failed: "+e.getMessage());
            }
        }
        return instance;
    }

    /**
     * instantiate web socket client
     *
     * @throws Exception
     */
    public WebSocketClient() throws Exception {

        ReadProperties readProperties = new ReadProperties();
        properties = readProperties.getProperties();

        globalSettings = GlobalSettings.getInstance();

        downloadPath = Paths.get(OurDriveService.getUserDataDirectory() + "/" + OurDriveService.getDownloadFolderName());

        socket_uri = properties.getProperty("socket.protocol") + "://" + properties.getProperty("socket.host") + ":" + properties.getProperty("socket.port");

        logger.info("SSL Socket Server URL: " + socket_uri);

        socket = IO.socket(socket_uri);

    }

    public void connect() {

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                logger.info("Successful connected to websocket: " + socket_uri + " to room: " + OurDriveService.getOurdriveId());
                logger.info("Your are working on a '" + System.getProperty("os.name").toLowerCase() + "' machine");
            }

        }).on(Socket.EVENT_MESSAGE, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    logger.info(obj.getString(Socket.EVENT_MESSAGE));
                } catch (JSONException e) {
                    logger.error("Enabling socket even listener failed: "+e.getMessage());
                }
            }

        }).on(OURDRIVE_FILE_UPLOAD_SUCCESSFUL, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
//                onOurdriveFileUploadSuccessful(obj);
            }

        }).on(OURDRIVE_FILE_DOWNLOAD, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                onOurdriveFileDownload(obj);
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                logger.info("Websocket connection closed.");
            }

        });

        socket.connect();
        joinRoom(OurDriveService.getOurdriveId(), OurDriveService.getOurdriveId(), "OurDrive Client APP");
        isSocketConnection = socket.connected();

    }

    public Boolean getSocketConnection() {
        return isSocketConnection;
    }

    public String getSocketUri() {
        return socket_uri;
    }

    public void processEnded() {
        logger.error("Process ended");
    }


    /**
     * @param room
     */
    public void joinRoom(String room, String id, String name) {
        JSONObject roomObj = new JSONObject();
        try {
            roomObj.put("room", room);
            roomObj.put("id", id);
            roomObj.put("name", name);

            try {
                if (socket != null) {
                    socket.emit(OURDRIVE_JOIN_ROOM, roomObj);
                } else {
                    logger.error("Could not emit event your room connection, because socket is NULL.");
                }
            } catch (Exception e) {
                logger.error("Emitting socket "+OURDRIVE_JOIN_ROOM+" failed: "+e.getMessage());
            }

        } catch (JSONException e) {
            logger.error("JSON emitting to socket "+OURDRIVE_JOIN_ROOM+" failed: "+e.getMessage());
        }
    }

    /**
     *
     */
    public boolean emitDownloadSuccessful() {

        Boolean response = false;
        try {

            JSONObject client = new JSONObject();
            JSONObject JsonObj = new JSONObject();

            client.put("id", OurDriveService.getOurdriveId());
            client.put("name", OurDriveService.getOurdriveId());
            client.put("room", OurDriveService.getOurdriveId());

            JsonObj.put("client", client);
            JsonObj.put("message", "Download of file to Ourdrive was successful.");

            try {
                if (socket != null) {
                    socket.emit(OURDRIVE_FILE_DOWNLOAD_SUCCESSFUL, JsonObj);
                    response = true;
                } else {
                    logger.error("Could not emit event 'upload_as_new_version', because socket is NULL.");
                }
            } catch (Exception e) {
                logger.error("Emitting socket "+OURDRIVE_FILE_DOWNLOAD_SUCCESSFUL+" failed: "+e.getMessage());
            }

        } catch (JSONException e) {
            logger.error("JSON emitting to socket "+OURDRIVE_FILE_DOWNLOAD_SUCCESSFUL+" failed: "+e.getMessage());
        }

        return response;
    }

    /**
     * @param file
     * @param unlock
     * @return
     */
    public boolean uploadAsNewVersionRequest(File file, Boolean unlock) {
        // upload_as_new_version
        // read json file, get data form that containing json and sen all back to cygnet
        File jsonFile = new File(downloadPath.toAbsolutePath().toString() + File.separator + "." + file.getName() + ".json");

        Boolean returnVal = false;

        FileInputStream jsonTargetFile = null;

        try {
            if(jsonFile.exists()) {
                jsonTargetFile = new FileInputStream(jsonFile);

                if(file.exists()) {
                    try {

                        String targetFileStr = IOUtils.toString(jsonTargetFile);
                        jsonTargetFile.close();

                        byte[] bytes = Files.readAllBytes(Paths.get(downloadPath.toAbsolutePath().toString() + File.separator + file.getName()));

                        byte[] dataFileStr = StringDecoder.encode(bytes, StringDecoder.ENCODE_BASE64);

                        JSONObject JsonObj = null;
                        JSONObject client = new JSONObject();
                        try {

                            client.put("id", OurDriveService.getOurdriveId());
                            client.put("name", OurDriveService.getOurdriveId());
                            client.put("room", OurDriveService.getOurdriveId());

                            JsonObj = new JSONObject(targetFileStr);
                            JsonObj.getJSONObject("file_data").remove("client");
                            JsonObj.getJSONObject("file_data").put("content", new String(dataFileStr, "UTF-8"));
                            JsonObj.getJSONObject("file_data").put("unlock", unlock);
                            JsonObj.getJSONObject("file_data").put("base64", true);
//                            JsonObj.getJSONObject("file_data").put("process_name", processName);

                            if (unlock) {
                                JsonObj.getJSONObject("file_data").put("edit", 1);
                                JsonObj.getJSONObject("file_data").put("next_action", "close");
                            } else {
                                JsonObj.getJSONObject("file_data").put("next_action", "nothing");
                            }

                            JsonObj.put("client", client);
                            try {
                                if (socket != null) {
                                    socket.emit(OURDRIVE_FILE_UPLOAD, JsonObj);
                                    returnVal = true;
                                } else {
                                    logger.error("Could not emit event 'upload_as_new_version', because socket is NULL.");
                                }
                            } catch (Exception e) {
                                logger.error("uploadAsNewVersionRequest failed: " + e.getMessage());
                            }

                        } catch (JSONException e) {
                            logger.error("JSON for uploadAsNewVersionRequest failed: " + e.getMessage());
                        }

                    } catch (IOException e) {
                        logger.warn("IO for uploadAsNewVersionRequest failed: " + e.getMessage());
                    }
                } else {
                    logger.warn(file.getAbsolutePath()+" does not exists anymore.");
                }
            } else {
                logger.warn(jsonFile.getAbsolutePath()+" does not exists anymore.");
            }
        } catch (FileNotFoundException e) {
            logger.error("File not found for uploadAsNewVersionRequest failed: "+e.getMessage());
        }

        return returnVal;

    }

    /**
     * @param obj
     */
    private void onOurdriveFileDownload(JSONObject obj) {

        if (!obj.has("content")) {
            logger.info("No message to initiate download event.");
        } else {

            try {
                byte[] valueDecoded = Base64.decodeBase64(obj.getString("content"));

                try {

                    String absolutFileName = downloadPath.toAbsolutePath().toString() + File.separator + obj.getString("file_name");
                    OutputStream stream = new FileOutputStream(absolutFileName);
                    stream.write(valueDecoded);
                    stream.close();

                    logger.info("Successfully saved: " + absolutFileName);

                    try {
                        desktop = new Desktop(this);

                        try {

                            File file = new File(absolutFileName);

                            // save json data into file
                            obj.remove("content");
                            JSONObject fileData = new JSONObject();
                            fileData.put("file_data", obj);

                            FileUtils.writeStringToFile(new File(downloadPath.toAbsolutePath().toString() + File.separator + "." + obj.getString("file_name") + ".json"), fileData.toString());
                            logger.info("Successfully saved JSON Object to: " + downloadPath.toAbsolutePath().toString() + File.separator + "." + obj.getString("file_name") + ".json");

                            emitDownloadSuccessful();

                            /*
                             * try to open file with appropriate local application
                             * we add a change watcher to this file
                             */
                            new Thread(() -> {
                                try {
                                    desktop.open(file);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }, "DesktopOpener").start();

                        } catch (Exception e) {
                            logger.error("OurdriveFileDownload failed: "+e.getMessage());
                        }

                    } catch (Exception e) {
                        logger.error("OurdriveFileDownload failed: "+e.getMessage());
                    }

                } catch (Exception e) {
                    logger.error("OurdriveFileDownload failed: "+e.getMessage());
                }

            } catch (JSONException e) {
                logger.error("JSON OurdriveFileDownload failed: "+e.getMessage());
            }
        }
    }

    public void disconnect() {
        socket.close();
    }

    public Socket getSocket() {
        return socket;
    }
}
