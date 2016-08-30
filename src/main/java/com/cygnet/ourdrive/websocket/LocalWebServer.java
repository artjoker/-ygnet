package com.cygnet.ourdrive.websocket;

import com.cygnet.ourdrive.OurDriveService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by casten on 7/12/16.
 */
public class LocalWebServer {

    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    public void start() {

        Integer port = 44444;

        InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);

        HttpServer server = null;
        try {
            server = HttpServer.create(address, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.createContext("/", new OurdriveUpHandler());
        server.createContext("/ourdrive-id", new OurdriveIdHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    /**
     *
     */
    private static class OurdriveIdHandler implements HttpHandler {
        /**
         *
         * @param t
         * @throws IOException
         */
        @Override
        public void handle(HttpExchange t) throws IOException {
//
//            if(t.getProtocol().equals("https")) {
//                    t.
//            }

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ourdrive_id", OurDriveService.getOurdriveId());

                String response = jsonObject.toString();
                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    private static class OurdriveUpHandler implements HttpHandler {

        /**
         *
         * @param t
         * @throws IOException
         */
        @Override
        public void handle(HttpExchange t) throws IOException {

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ourdrive_running", true);

                String response = jsonObject.toString();
                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
