package com.cygnet.ourdrive.monitoring.visitor;

import com.cygnet.ourdrive.monitoring.FileHandler;
import com.cygnet.ourdrive.upload.DirectoryHandler;

import java.io.File;

public interface FileVisitor {

    /**
     * Invoked for a file in a directory.
     *
     * @param file a reference to the file
     * @param handler file handler
     * @param containerId parent container ID of file
     */
    public void visitFile(File file, FileHandler handler, String containerId);

    /**
     * Invoked for a directory before entries in the directory are visited.
     *
     * @param dir a reference to the directory
     * @param handler directory handler
     * @param containerId parent container ID of directory
     * @return created container ID of that directory or null
     */
    public String visitDirectory(File dir, DirectoryHandler handler, String containerId);

    /**
     * Invoked for a file or directory that could not be visited
     *
     * @param file a reference to the file or directory
     * @param ex error description of visit failure
     */
    public void visitFailed(File file, Exception ex);

}
