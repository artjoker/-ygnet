package com.cygnet.ourdrive.util;

import com.cygnet.ourdrive.OurDriveService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by casten on 5/24/16.
 */
public class Processes {

    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    private static String Pid = "0";

    private static String titleDocument = "";

    private static String titleNotAvailable = "";

    private static String titleOnlyFileClosed = "";

    private static Boolean isMsOffice = false;

    /**
     * get process ids
     * @param file
     * @param OS
     * @param getall
     * @return
     */
    public static HashMap GetSystemProcesses(File file, String OS, Boolean getall) {

        String process;
        Process p = null;
        HashMap<String, String> processes = new HashMap<String, String>();

        try {

            switch (OS) {
                case "linux":
                    // ps -Ao %p%a
                    p = Runtime.getRuntime().exec("ps -Ao '%p,%a'");
                    p.waitFor();

//                    if (!(p == null)) {
                    BufferedReader linput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((process = linput.readLine()) != null) {

//                        System.out.println(process);
                        String arr[] = process.split(",");

                        if (getall) {
                            processes.put(arr[0].trim(), arr[1].trim());
                        } else {
                            String filenameWithoutExtension = FilenameUtils.removeExtension(file.getName());
                            if (arr[1].contains(filenameWithoutExtension)) {
                                processes.put(arr[0].trim(), file.getAbsoluteFile().toString());
                            }
                        }
                    }
                    linput.close();

//                    }
                    break;
                case "windows":

                    /*
                    instead of that we should use this:

                    wmic path cim_datafile where "path='\\.ourdrive\\downloadpath\\' and AccessMask is null" get Caption,AccessMask,Extension,FileSize

                    if all empty it gives : empty line, next line contains 'Node,'
                    otherweise csv list

                     */

                    p = Runtime.getRuntime().exec("tasklist /V /FO \"CSV\" /FI \"STATUS eq running\" /NH");
//                    p = Runtime.getRuntime().exec("tasklist /V /FO \"CSV\" /NH");
                    p.waitFor();

//                    if (p != null) {
                    BufferedReader winput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((process = winput.readLine()) != null) {

                        String arr[] = process.split("\",\"");
                        String[] preparedProcesses = new String[3];

                        Integer i = 0;
                        for (String value : arr) {
                            switch (i) {
                                case 0:
                                    if(value.contains("soffice") || value.contains("WINWORD")) {
//                                            System.out.println(process);
                                    }
                                    break;
                                case 1:
                                    preparedProcesses[1] = value;
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                                case 5:
                                    break;
                                case 6:
                                    preparedProcesses[0] = value;
                                    break;
                                case 7:
                                    break;
                                case 8:
                                    preparedProcesses[2] = value;
                                    break;
                            }
                            i++;
                        }

                        if (getall) {
                            processes.put(preparedProcesses[1].trim(), preparedProcesses[2].trim());
                        } else {

                            String filenameWithoutExtension = FilenameUtils.removeExtension(file.getName());

                            if (preparedProcesses[2].contains(filenameWithoutExtension) || Pid.equals(preparedProcesses[1].trim())) {

                                if(Pid.equals("0")) {
                                    Pid = preparedProcesses[1].trim();
                                }

                                if(titleDocument.equals("")) {

                                    setTitleDocument(preparedProcesses[2].trim());

                                } else if(!titleDocument.equals("") && titleNotAvailable.equals("") && !preparedProcesses[2].trim().equals(titleDocument)) {

                                    setTitleNotAvailable(preparedProcesses[2].trim());

                                } else if(!titleDocument.equals("") && !titleNotAvailable.equals("") && !preparedProcesses[2].trim().equals(titleDocument) && !preparedProcesses[2].trim().equals(titleNotAvailable)) {

                                    /*
                                    if not empty doc title &&
                                    if not empty N/A title &&
                                    process title not equals doc title &&
                                    process title not equals N/A title  &&
                                     */

                                    setTitleOnlyFileClosed(preparedProcesses[2].trim());

                                }

//                                    if(preparedProcesses[2].contains(filenameWithoutExtension)) {
//                                        setTitleNotAvailable("");
//                                        setTitleOnlyFileClosed("");
//                                    }

                                processes.put(preparedProcesses[1].trim(), file.getAbsoluteFile().toString().trim());

                            }
                        }
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

    public static String getPid() {
        return Pid;
    }

    public static void setPid(String pid) {
        Pid = pid;
    }

    public static String getTitleDocument() {
        return titleDocument;
    }

    public static void setTitleDocument(String titleDocument) {
        Processes.titleDocument = titleDocument;
    }

    public static String getTitleNotAvailable() {
        return titleNotAvailable;
    }

    public static void setTitleNotAvailable(String titleNotAvailable) {
        Processes.titleNotAvailable = titleNotAvailable;
    }

    public static String getTitleOnlyFileClosed() {
        return titleOnlyFileClosed;
    }

    public static void setTitleOnlyFileClosed(String titleOnlyFileClosed) {
        Processes.titleOnlyFileClosed = titleOnlyFileClosed;
    }

    public static Boolean getIsMsOffice() {
        return isMsOffice;
    }

    public static void setIsMsOffice(Boolean isMsOffice) {
        Processes.isMsOffice = isMsOffice;
    }
}
