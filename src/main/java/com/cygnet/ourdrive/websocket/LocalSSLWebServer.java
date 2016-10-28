package com.cygnet.ourdrive.websocket;

import com.cygnet.ourdrive.OurDriveService;
import com.sun.net.httpserver.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;

/**
 * Created by carsten on 4/23/16.
 * com.cygnet.ourdrive.websocket
 * ourdrive
 */
public class LocalSSLWebServer {

    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    public void start() {

        Integer port = 44444;
        try {

            String certFileName = "cert/ourdrive.jks";
            InputStream certInputStream = getClass().getClassLoader().getResourceAsStream(certFileName);

            char[] password = "ourdrive123".toCharArray();

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(certInputStream, password);

            // setup the socket address
            InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);

            // initialise the HTTPS server
            HttpsServer httpsServer = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("SSL");

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);

                    } catch (Exception ex) {
                        logger.error("Failed to create HTTPS port");
                    }
                }
            });
            httpsServer.createContext("/ourdrive-id", new ResponseHandler());
            httpsServer.createContext("/", new UpHandler());
            httpsServer.setExecutor(null); // creates a default executor
            httpsServer.start();

            logger.info(httpsServer.getAddress().getHostString());

        } catch (Exception exception) {
            logger.error("Failed to create HTTPS server on port " + port + " of localhost");
            exception.printStackTrace();

        }
    }

    /**
     * get the ourdrive id by using this uri
     */
    private static class ResponseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ourdrive_id", OurDriveService.getOurdriveId());

                String response = jsonObject.toString();
                HttpsExchange httpsExchange = (HttpsExchange) t;
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
     * check by calling this uri if localhost is up and running
     */
    private static class UpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ourdrive_running", true);

                String response = jsonObject.toString();
                HttpsExchange httpsExchange = (HttpsExchange) t;
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
