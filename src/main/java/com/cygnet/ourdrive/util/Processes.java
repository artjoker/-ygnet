package com.cygnet.ourdrive.util;

import com.cygnet.ourdrive.OurDriveService;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by casten on 5/24/16.
 */
public class Processes {

    /**
     * Get all available system process ids according to a given file name
     *
     * @param file the file a process has opened
     * @return HashMap List of process ids
     */
    public static HashMap getProcessIdsByFile(File file, String OS) {

        HashMap<Integer, String> processIds = new HashMap<Integer, String>();
        List<ProcessInfo> processesList = JProcesses.getProcessList();

        switch (OS) {
            case "linux":

                for (ProcessInfo processInfo : processesList) {

                    if (processInfo.getCommand().contains(file.getAbsoluteFile().toString())) {
                        processIds.put(Integer.parseInt(processInfo.getPid()), file.getAbsoluteFile().toString());
                    }
                }
                break;
            case "windows":

                for (ProcessInfo processInfo : processesList) {

                    System.out.println(processInfo.getCommand());
                    System.out.println(processInfo.getName());
                    System.out.println(processInfo.getExtraData());
                    System.out.println(processInfo.getPid());
                    System.out.println(processInfo.getUser());
                    System.out.println("----------------------------------");

                    if (processInfo.getCommand().contains(file.getName())) {
                        processIds.put(Integer.parseInt(processInfo.getPid()), OurDriveService.getUserDataDirectory()+"/"+OurDriveService.getDownloadFolderName()+"/"+file.getName());
                    }
                }
                break;
        }

        return processIds;
    }

    public static HashMap GetSystemProcesses(String OS) {

        // windows: System.getenv("windir") +"\\system32\\"+"tasklist.exe /fo csv /nh"
        String process;

        Process p = null;

        // processID, process title (contains the file name)
        HashMap<Integer, String> processes = new HashMap<Integer, String>();

        try {

            switch (OS) {
                case "linux":
                    // ps -Ao %p%a
                    p = Runtime.getRuntime().exec("ps -Ao %p%a");
                    if (p != null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((process = input.readLine()) != null) {
                            System.out.println(process); // <-- Print all Process here line
                            // by line
                            String arr[] = process.split("\t");
                            processes.put(Integer.valueOf(arr[0]), arr[1]);
                        }
                        input.close();

                        System.out.println(processes);
                    }
                    break;
                case "windows":
                    //  tasklist /v /FI "STATUS eq running" /FO "CSV" /NH
//                    p = Runtime.getRuntime().exec("tasklist /V /FI \"STATUS eq running\" /FO \"CSV\" /NH");
                    p = Runtime.getRuntime().exec("tasklist /V /FO \"CSV\" /NH");
                    if (p != null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((process = input.readLine()) != null) {

                            process = process.replace("\"", "");
                            String arr[] = process.split(",");
                            String[] preparedProcesses = new String[3];

                            Integer i = 0;
                            for (String value : arr) {
                                switch (i) {
                                    case 0:
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

                            processes.put(Integer.valueOf(preparedProcesses[1]), preparedProcesses[2]);
                        }
                        input.close();

                        for(Map.Entry<Integer, String> processeList : processes.entrySet()){
                            System.out.println(processeList.getKey() +" :: "+ processeList.getValue());
                        }
                    }
                    break;

            }


        } catch (Exception err) {
            err.printStackTrace();
        }

        return processes;
    }

}
