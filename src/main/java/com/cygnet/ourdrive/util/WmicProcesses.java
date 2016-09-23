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
                    // ps -Ao %p%a
//                    p = Runtime.getRuntime().exec("ps -Ao '%p,%a'");
//                    p.waitFor();
//
////                    if (!(p == null)) {
//                    BufferedReader linput = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                    while ((process = linput.readLine()) != null) {
//
////                        System.out.println(process);
//                        String arr[] = process.split(",");
//
//                        String filenameWithoutExtension = FilenameUtils.removeExtension(file.getName());
//                        if (arr[1].contains(filenameWithoutExtension)) {
//                            processes.put(arr[0].trim(), file.getAbsoluteFile().toString());
//                        }
//                    }
//                    linput.close();
//
////                    }
                    break;
                case "windows":

                    /*
                    instead of that we should use this:

                    OurDriveService.getUserDataDirectory() + "/" + OurDriveService.getDownloadFolderName()

                    wmic path cim_datafile where "path='\\.ourdrive\\downloadpath\\' and AccessMask is null" get FileName,Extension,FileSize,Path,Readable,Status,Writable /format:csv

                    if all empty it gives : empty line, next line contains 'Node,'
                    otherweise csv list

                     */

                    String command = "wmic path cim_datafile " +
                            "where \"path='\\\\" + OurDriveService.getUserDataDirectory() + "\\\\" + OurDriveService.getDownloadFolderName() + "\\\\' " +
                            "and AccessMask is null\" get FileName,Extension,FileSize,Path,Readable,Status,Writable /format:csv";

                    System.out.println(command);
                    p = Runtime.getRuntime().exec(command);
//                    p = Runtime.getRuntime().exec("tasklist /V /FO \"CSV\" /NH");
                    p.waitFor();

//                    if (p != null) {
                    BufferedReader winput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((process = winput.readLine()) != null) {

                        String arr[] = process.split(",");
                        String[] preparedProcesses = new String[7];

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

                            }
                            i++;
                        }

                        processes.put(preparedProcesses[0].trim(), preparedProcesses);

//                        String filenameWithoutExtension = FilenameUtils.removeExtension(file.getName());
//
//                        if (preparedProcesses[2].contains(filenameWithoutExtension) || Pid.equals(preparedProcesses[1].trim())) {
//
//                            if (Pid.equals("0")) {
//                                Pid = preparedProcesses[1].trim();
//                            }
//
//                            if (titleDocument.equals("")) {
//
//                                setTitleDocument(preparedProcesses[2].trim());
//
//                            } else if (!titleDocument.equals("") && titleNotAvailable.equals("") && !preparedProcesses[2].trim().equals(titleDocument)) {
//
//                                setTitleNotAvailable(preparedProcesses[2].trim());
//
//                            } else if (!titleDocument.equals("") && !titleNotAvailable.equals("") && !preparedProcesses[2].trim().equals(titleDocument) && !preparedProcesses[2].trim().equals(titleNotAvailable)) {
//
//                                /*
//                                if not empty doc title &&
//                                if not empty N/A title &&
//                                process title not equals doc title &&
//                                process title not equals N/A title  &&
//                                 */
//
//                                setTitleOnlyFileClosed(preparedProcesses[2].trim());
//
//                            }
//
////                                    if(preparedProcesses[2].contains(filenameWithoutExtension)) {
////                                        setTitleNotAvailable("");
////                                        setTitleOnlyFileClosed("");
////                                    }
//
//                            processes.put(preparedProcesses[1].trim(), file.getAbsoluteFile().toString().trim());
//
//                        }
                    }
                    winput.close();

//                    }
                    break;

            }

        } catch (Exception err) {
            err.printStackTrace();
        }

        return processes;
    }

}
