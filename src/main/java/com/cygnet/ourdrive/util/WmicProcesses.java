package com.cygnet.ourdrive.util;

import com.cygnet.ourdrive.OurDriveService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by casten on 5/24/16.
 */
public class WmicProcesses {

    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);


    /**
     * @param file
     * @param OS
     * @return
     */
    public static String[] GetSystemProcesses(File file, String OS) {

        String process;
        Process p = null;
        String[] processes = new String[9];

        try {

            switch (OS) {
                case "linux":

                    break;
                case "windows":

                    String command = "cmd.exe /c wmic path cim_datafile " +
                            "where \"path='\\\\Users\\\\%username%\\\\.ourdrive\\\\ourdrive_downloads\\\\' " +
                            "and AccessMask is null\" get FileName,Extension,FileSize,Path,Readable,Status,System,Writeable /format:csv";

//                    System.out.println(command);
                    p = Runtime.getRuntime().exec(command);

                    p.waitFor();

                    BufferedReader winput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((process = winput.readLine()) != null) {
                        if(!process.equals("") && !process.startsWith("Node,")) {
                            processes = process.split(",");
                        }
                    }
                    winput.close();

                    break;

            }

        } catch (Exception err) {
            err.printStackTrace();
        }

        return processes;
    }

}
