package com.cygnet.ourdrive.settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by casten on 5/2/16.
 */
public class ReadProperties {

    /**
     *
     */
    private InputStream inputStream;
    private Properties prop;

    /**
     * get propeties from config file
     *
     * @return
     * @throws IOException
     */
    public Properties getProperties() throws IOException {

        try {
            prop = new Properties();
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }
        return prop;
    }
}
