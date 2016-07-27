package com.cygnet.ourdrive.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by casten on 5/24/16.
 */
public class Processes {

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
                    p = Runtime.getRuntime().exec("ps -Ao %p;%a");
                    if (p != null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((process = input.readLine()) != null) {
                            System.out.println(process); // <-- Print all Process here line

                            // by line
                            String arr[] = process.split(";");

                            if(getall) {
                                processes.put(arr[0].trim(), arr[1].trim());
                            } else {
                                if (arr[1].contains(file.getName())) {
                                    processes.put(arr[0].trim(), file.getAbsoluteFile().toString());
                                }
                            }
                        }
                        input.close();

                    }
                    break;
                case "windows":
                    //  tasklist /v /FI "STATUS eq running" /FO "CSV" /NH
//                    p = Runtime.getRuntime().exec("tasklist /V /FI \"STATUS eq running\" /FO \"CSV\" /NH");
                    p = Runtime.getRuntime().exec("tasklist /V /FO \"CSV\" /NH");
                    if (p != null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((process = input.readLine()) != null) {

                            System.out.println(process);

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

                            if(getall) {
                                processes.put(preparedProcesses[1].trim(), preparedProcesses[2].trim());
                            } else {
                                if (preparedProcesses[2].contains(file.getName())) {
                                    processes.put(preparedProcesses[1].trim(), file.getAbsoluteFile().toString().trim());
                                }
                            }
                        }
                        input.close();

//                        for(Map.Entry<String, String> processeList : processes.entrySet()){
//                            System.out.println(processeList.getKey() +" :: "+ processeList.getValue());
//                        }
                    }
                    break;

            }


        } catch (Exception err) {
            err.printStackTrace();
        }

        return processes;
    }

}
