package com.cygnet.ourdrive.util;

/**
 * Created by carsten on 5/11/16.
 * com.cygnet.ourdrive.util
 * ourdrive
 */
public class OSDetection {

    /**
     * If we found a linux based system
     */
    public static final String OS_LINUX = "linux";

    /**
     * If we found a mac based system
     */
    public static final String OS_MAC = "mac";

    /**
     * If we found a windows based system
     */
    public static final String OS_WINDOWS = "windows";

    /**
     * If we found a unknown system
     */
    public static final String OS_UNKNOWN = "unknown";


    /**
     * OS Detection.
     *
     * @return True if the client OS is Linux, otherwise False.
     */
    public static String getOs() {
        if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
            return OS_LINUX;
        } else if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            return OS_MAC;
        } else if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            return OS_WINDOWS;
        } else {
            return OS_UNKNOWN;
        }
    }

    /**
     * This method gets the OS information of the client OS system. The informations
     * contains the OS name, OS version and the OS architecture.
     *
     * @return A string with the OS info.
     */
    public static String getOSInfo() {
        String os = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArchitecture = System.getProperty("os.arch");

        return "OS Detection:" + os + "," + osVersion + "," + osArchitecture;
    }
}
