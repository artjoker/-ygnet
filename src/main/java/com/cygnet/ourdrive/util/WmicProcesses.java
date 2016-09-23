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
    public static HashMap GetSystemProcesses(File file, String OS) {

        String process;
        Process p = null;
        HashMap<String, String[]> processes = new HashMap<String, String[]>();

        try {

            switch (OS) {
                case "linux":

                    break;
                case "windows":

                    String command = "wmic path cim_datafile " +
                            "where \"path='\\\\Users\\\\%username%\\\\.ourdrive\\\\ourdrive_downloads\\\\' " +
                            "and AccessMask is null\" get FileName,Extension,FileSize,Path,Readable,Status,System,Writable /format:csv";

                    System.out.println(command);
                    p = Runtime.getRuntime().exec(command);

                    BufferedReader winput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((process = winput.readLine()) != null) {

                        if(process.equals("") || process.equals("Node,")) {
                            continue;
                        }

                        String arr[] = process.split(",");
                        String[] preparedProcesses = new String[9];

                        Integer i = 0;
                        for (String value : arr) {
                            switch (i) {
                                case 0:
                                    preparedProcesses[0] = value;
                                    break;
                                case 1:
                                    preparedProcesses[1] = value;
                                    break;
                                case 2:
                                    preparedProcesses[2] = value;
                                    break;
                                case 3:
                                    preparedProcesses[3] = value;
                                    break;
                                case 4:
                                    preparedProcesses[4] = value;
                                    break;
                                case 5:
                                    preparedProcesses[5] = value;
                                    break;
                                case 6:
                                    preparedProcesses[6] = value;
                                    break;
                                case 7:
                                    preparedProcesses[7] = value;
                                    break;
                                case 8:
                                    preparedProcesses[8] = value;
                                    break;

                            }
                            i++;
                        }

                        processes.put(preparedProcesses[0].trim(), preparedProcesses);

                    }
                    p.waitFor();
                    winput.close();

                    break;

            }

        } catch (Exception err) {
            err.printStackTrace();
        }

        return processes;
    }

}
