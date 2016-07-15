package com.cygnet.ourdrive.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Useful utilities
 */
public class DirUtils {

    /**
     * Recursive find and remove empty dirs
     * @param dir root directory
     */
   public static void removeEmptyDirs(File dir) {
         while (true) {
           List<File> emptyDirs = listFiles(dir,
                   FileFilterUtils.andFileFilter(DirectoryFileFilter.DIRECTORY, EmptyFileFilter.EMPTY),
                   TrueFileFilter.TRUE);

           if (emptyDirs.isEmpty()) {
               // no more empty dirs
               break;
           }

           for (File emptyDir : emptyDirs) {
               // remove empty dirs
               if(!emptyDir.delete()) {
                   break;
               }
           }
       }


   }


    /**
     * Finds files and directories within a given directory (and optionally its
     * subdirectories). All files/dirs found are filtered by an IOFileFilter.
     *
     * @param directory  the directory to search in.
     * @param fileFilter the filter to apply to files and directories.
     * @param dirFilter  in which dirs the algorithm should traverse
     * @return the list of found file objects
     */
    public static List<File> listFiles(File directory,
                                       IOFileFilter fileFilter,
                                       IOFileFilter dirFilter) {
        List<File> files = new ArrayList<File>();

        File[] found = directory.listFiles();
        if (found != null) {
            for (File file : found) {
                if (fileFilter.accept(file)) {
                    files.add(file);
                }

                if (file.isDirectory() && dirFilter.accept(file)) {
                    files.addAll(listFiles(file, fileFilter, dirFilter));
                }
            }
        }
        return files;
    }
}
