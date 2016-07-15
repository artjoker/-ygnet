package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.settings.ReadProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.List;
import java.util.Properties;

/**
 * Created by casten on 4/19/16.
 */
public class FileWatcher {

    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    private Properties properties;

    public FileWatcher() throws IOException {
        ReadProperties readProperties = new ReadProperties();
        properties = readProperties.getProperties();
    }

    /**
     * compare 2 files wih each other
     *
     * @param filea first file
     * @param fileb second file
     * @return
     * @throws IOException
     */
    public boolean compareFiles(final Path filea, final Path fileb) throws IOException {
        if (Files.size(filea) != Files.size(fileb)) {
            return false;
        }

        final long size = Files.size(filea);
        final int mapspan = 4 * 1024 * 1024;

        try (FileChannel chana = (FileChannel) Files.newByteChannel(filea);
             FileChannel chanb = (FileChannel) Files.newByteChannel(fileb)) {

            for (long position = 0; position < size; position += mapspan) {
                if (!mapChannel(chana, position, size, mapspan)
                        .equals(mapChannel(chanb, position, size, mapspan))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * prepare file for comperation using FileChannel
     *
     * @param channel
     * @param position
     * @param size
     * @param mapspan
     * @return
     * @throws IOException
     */
    private MappedByteBuffer mapChannel(FileChannel channel, long position, long size, int mapspan) throws IOException {
        final long end = Math.min(size, position + mapspan);
        final long maplen = (int) (end - position);
        return channel.map(FileChannel.MapMode.READ_ONLY, position, maplen);
    }

    /**
     * Start watching on a given folder for changes around containing files
     *
     * @param path Path object
     * @throws IOException
     */
    public void startWatching(Path path) throws IOException {

        FileSystem fs = FileSystems.getDefault();

        Path watchPath = fs.getPath(path.toAbsolutePath().toString());
        WatchService watcher = fs.newWatchService();

        try {
            watchPath.register(
                    watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
            );

            while (true) {
                WatchKey watckKey = watcher.take();

                List<WatchEvent<?>> events = watckKey.pollEvents();
                for (WatchEvent event : events) {

                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        logger.info("Created: " + event.context().toString());
                    }
                    if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        logger.info("Delete: " + event.context().toString());
                    }
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        logger.info("Modify: " + event.context().toString());

                    }
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        logger.info("Overflow: " + event.context().toString());
                    }
                }

                watckKey.reset(); // Important!
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    private static boolean shouldIgnoreFile(String filename) {
        boolean ignore = filename.startsWith("#");
        ignore = ignore || filename.startsWith("~");
        ignore = ignore || filename.startsWith(".");
        ignore = ignore || filename.startsWith("$");
        ignore = ignore || filename.endsWith(".swp");
        ignore = ignore || filename.endsWith(".swap");
        ignore = ignore || filename.endsWith(".tmp");
        ignore = ignore || filename.endsWith(".temp");
        return ignore;
    }
}
