package com.cygnet.ourdrive.monitoring.visitor;

import java.io.File;
import java.util.Comparator;

/**
 * Sort array of files - directories first
 */
public class DirectoriesComparator implements Comparator<File> {

    public int compare(File first, File second) {
        if (first.isDirectory()) {
            return second.isDirectory() ? first.compareTo(second) : -1;
        } else if (second.isDirectory()) {
            return 1;
        }
        return first.compareTo(second);
    }
}
