package com.cygnet.ourdrive.monitoring.visitor;

import com.cygnet.ourdrive.monitoring.FileHandler;
import com.cygnet.ourdrive.monitoring.exceptions.FileVisitException;
import com.cygnet.ourdrive.upload.DirectoryHandler;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Arrays;

/**
 * Recursive walk of directory tree
 */
public class FilesTreeWalker {

    private final FileVisitor visitor;
    private final boolean isInTray;
    private final FileHandler fileHandler;
    private final DirectoryHandler directoryHandler;

    public FilesTreeWalker(FileVisitor visitor,
                           boolean isInTray,
                           FileHandler fileHandler,
                           DirectoryHandler directoryHandler) {
        this.visitor = visitor;
        this.isInTray = isInTray;
        this.fileHandler = fileHandler;
        this.directoryHandler = directoryHandler;
    }

    /**
     * Recursive walk
     *
     * @param root the root file
     * @param parentContainerID parent container id for upload files and directories
     */
    public void walk(File root, String parentContainerID) {
        File[] list = root.listFiles();
        if (list == null) return;
        Arrays.sort(list, new DirectoriesComparator());

        for (File f : list) {
            if (f.isDirectory()) {
                if (isInTray) {
                    visitor.visitFailed(f, new FileVisitException("Folders can't be uploaded to the Intray"));
                } else {
                    String targetId = visitor.visitDirectory(f, directoryHandler, parentContainerID);
                    if (StringUtils.isNotBlank(targetId)) {
                        walk(f, targetId);
                    }
                }
            } else {
                visitor.visitFile(f, fileHandler, parentContainerID);
            }
        }
    }
}
