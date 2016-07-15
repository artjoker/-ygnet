package com.cygnet.ourdrive;

import com.cygnet.ourdrive.monitoring.FileHandler;
import com.cygnet.ourdrive.monitoring.FolderMonitor;
import com.cygnet.ourdrive.monitoring.FolderMonitorListener;
import com.cygnet.ourdrive.settings.FolderSettings;
import com.cygnet.ourdrive.upload.DirectoryHandler;
import com.cygnet.ourdrive.upload.UploadServiceException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for simple OurDriveService.
 */
public class OurDriveServiceTest {

    /**
     * Test inTray upload - upload only files without directories
     */
    @Test
    public void testInTrayUpload() throws Exception {
        File fixtures = fixtures();
        assertTrue(fixtures.exists());

        createEmptyDirectories(fixtures);

        File inTrayFolder = new File(fixtures, "intray");
        File upload = File.createTempFile("file", ".txt", inTrayFolder);

        FolderSettings settings = new FolderSettings();
        settings.setFolder(inTrayFolder);
        settings.setTargetContainer("");

        FileHandler fileHandler = mock(FileHandler.class);
        when(fileHandler.handle(any(InputStream.class), anyString(), any(FolderSettings.class), anyString())).thenReturn("mocked file handler");

        DirectoryHandler dirHandler = mock(DirectoryHandler.class);
        when(dirHandler.handleDirectory(any(File.class), any(FolderSettings.class), anyString())).thenReturn("mocked dir handler");

        FolderMonitor monitor = new FolderMonitor(settings);

        monitor.checkForModifications(fileHandler, dirHandler);

        verify(dirHandler, never()).handleDirectory(any(File.class), any(FolderSettings.class), anyString());
        verify(fileHandler, times(1)).handle(any(InputStream.class), eq(upload.getName()), any(FolderSettings.class), eq(""));

        assertFalse("Upload file exists", upload.exists());
    }

    /**
     * Test normal upload - upload files and directories
     */
    @Test
    public void testNormalUpload() throws Exception {
        File fixtures = fixtures();
        assertTrue(fixtures.exists());

        createEmptyDirectories(fixtures);

        File importFolder = new File(fixtures, "import");
        File upload = File.createTempFile("file", ".txt", importFolder);
        File upload2 = File.createTempFile("file", ".txt", new File(importFolder, "/lvl1/lvl2/"));

        FolderSettings settings = new FolderSettings();
        settings.setFolder(importFolder);
        settings.setTargetContainer("123");

        FileHandler fileHandler = mock(FileHandler.class);
        when(fileHandler.handle(any(InputStream.class), anyString(), any(FolderSettings.class), anyString())).thenReturn("555");

        DirectoryHandler dirHandler = mock(DirectoryHandler.class);
        when(dirHandler.handleDirectory(any(File.class), any(FolderSettings.class), anyString())).thenReturn("777");

        FolderMonitor monitor = new FolderMonitor(settings);

        monitor.checkForModifications(fileHandler, dirHandler);

        verify(dirHandler, times(1)).handleDirectory(any(File.class), any(FolderSettings.class), eq("123"));
        verify(dirHandler, times(1)).handleDirectory(any(File.class), any(FolderSettings.class), eq("777"));

        verify(fileHandler, times(1)).handle(any(InputStream.class), eq(upload.getName()), any(FolderSettings.class), eq("123"));
        verify(fileHandler, times(1)).handle(any(InputStream.class), eq(upload2.getName()), any(FolderSettings.class), eq("777"));

        assertFalse("Upload file exists", upload.exists());
        assertFalse("Upload file exists", upload2.exists());

        assertTrue("Import folder non empty", importFolder.list().length == 0);
    }


    /**
     * Test normal upload - upload files and directories
     */
    @Test
    public void testUploadServiceFailure() throws Exception {
        File fixtures = fixtures();
        assertTrue(fixtures.exists());

        createEmptyDirectories(fixtures);

        File importFolder = new File(fixtures, "error");
        File upload = new File(importFolder, "test.txt");

        FolderSettings settings = new FolderSettings();
        settings.setFolder(importFolder);
        settings.setTargetContainer("123");

        FileHandler fileHandler = mock(FileHandler.class);
        when(fileHandler.handle(any(InputStream.class), anyString(), any(FolderSettings.class), anyString())).thenThrow(new UploadServiceException("File upload failed"));

        DirectoryHandler dirHandler = mock(DirectoryHandler.class);
        when(dirHandler.handleDirectory(any(File.class), any(FolderSettings.class), anyString())).thenThrow(new UploadServiceException("Dir upload failed"));

        FolderMonitor monitor = new FolderMonitor(settings);
        FolderMonitorListener listener = mock(FolderMonitorListener.class);
        monitor.addListener(listener);

        monitor.checkForModifications(fileHandler, dirHandler);

        //Verify upload failed call 2 times
        verify(listener, times(2)).uploadFailed(any(FolderMonitor.class), any(File.class), any(UploadServiceException.class));

        //Upload file handler not handled
        verify(fileHandler, never()).handle(any(InputStream.class), eq("ignored.txt"), any(FolderSettings.class), eq("123"));

        assertTrue("Upload file removed", upload.exists()); //Assert file is not removed
    }

    private File fixtures() {
        if(new File("./ourdrive").exists()) {
            return new File("./ourdrive/src/test/resources/fixtures/");
        } else if(new File("./ourdrive-project").exists()) {
            return new File("./ourdrive-project/src/test/resources/fixtures/");
        } else  {
            return new File("src/test/resources/fixtures/");
        }
    }

    private void createEmptyDirectories(File fixtures) throws IOException {
        FileUtils.forceMkdir(new File(fixtures, "/intray/lvl1/"));
        FileUtils.forceMkdir(new File(fixtures, "/import/lvl1/lvl2/"));
    }

}
