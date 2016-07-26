package com.cygnet.ourdrive.util;

import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

                    if (processInfo.getCommand().contains(file.getAbsoluteFile().toString())) {
                        processIds.put(Integer.parseInt(processInfo.getPid()), file.getAbsoluteFile().toString());
                    }
                }
                break;
        }

        return processIds;
    }

    public static void GetSystemProcess(String OS) {

        // windows: System.getenv("windir") +"\\system32\\"+"tasklist.exe /fo csv /nh"

        try {
            String process;
            // getRuntime: Returns the runtime object associated with the current Java application.
            // exec: Executes the specified string command in a separate process.

            Process p = null;
            ArrayList<String[]> processes = new ArrayList<String[]>();

            switch (OS) {
                case "linux":
                    p = Runtime.getRuntime().exec("ps -Ao %U%t%a");
                    if (p != null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((process = input.readLine()) != null) {
                            System.out.println(process); // <-- Print all Process here line
                            // by line
                            String arr[] = process.split("\t");
                            processes.add(arr);
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
                            processes.add(preparedProcesses);
                        }
                        input.close();

                        for (String[] my_process : processes) {
                            System.out.println(Arrays.toString(my_process));
                        }
                    }
                    break;

            }


        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
