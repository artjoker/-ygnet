package com.cygnet.ourdrive.settings;

import com.cygnet.ourdrive.util.RandomGUID;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA on 12/10/12.
 *
 * @author werneraltewischer
 */
public class FolderSettings extends AbstractSettings {

    private static final long serialVersionUID = 1L;

    public static final long QUIET_PERIOD = 60L * 1000L;
    protected File folder;
    protected String name;
    protected String title;
    private Map<String, Date> fileModificationDates = new HashMap<String, Date>();
    private Map<String, Date> quietDates = new HashMap<String, Date>();
    private Map<String, String> containerIds = new HashMap<String, String>();

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        setProperty("folder", File.class, folder);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        setProperty("title", String.class, title);
    }

    public String getName() {
        return name;
    }

    public void initName() {
        this.name = new RandomGUID().toString();
    }

    public Date getModificationDate(String fileAbsolutePath) {
        return fileModificationDates.get(fileAbsolutePath);
    }

    public void setModificationDate(String fileAbsolutePath, Date date) {
        if (date == null) {
            fileModificationDates.remove(fileAbsolutePath);
        } else {
            fileModificationDates.put(fileAbsolutePath, date);
        }
    }

    public String toString() {
        return getFolder() == null ? "" : getFolder().getAbsolutePath();
    }

    public void setQuietPeriod(String fileAbsolutePath) {
        quietDates.put(fileAbsolutePath, new Date(System.currentTimeMillis() + QUIET_PERIOD));
    }

    public void clearQuietPeriods() {
        quietDates.clear();
    }

    public boolean isQuietPeriodExpired(String fileAbsolutePath) {
        Date d = quietDates.get(fileAbsolutePath);
        boolean ret = (d == null || d.before(new Date()));
        if (ret) {
            quietDates.remove(fileAbsolutePath);
        }
        return ret;
    }

    public String getSavedContainerId(String fullPath) {
        return containerIds.get(compositeKey(getTargetContainer(), fullPath));
    }

    public void setSavedContainerId(String fullPath, String containerId) {
        containerIds.put(compositeKey(getTargetContainer(), fullPath), containerId);
    }

    public void clearSavedContainerIds() {
        containerIds.clear();
    }

    private String compositeKey(String rootContainerId, String fullPath) {
        return rootContainerId + fullPath;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void readPreferences(Preferences preferences) throws IOException {
        super.readPreferences(preferences);
        this.name = preferences.name();

        byte[] bytes = preferences.getByteArray("fileModificationDates", null);
        byte[] foldersIdsBytes = preferences.getByteArray("containerIds", null);

        if (bytes != null) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                Object o = null;
                try {
                    o = ois.readObject();
                } catch (ClassNotFoundException e) {
                    throw new IOException("Could not read object", e);
                }
                if (o instanceof Map) {
                    this.fileModificationDates.clear();
                    this.fileModificationDates.putAll((Map)o);
                }
            } finally {
                IOUtils.closeQuietly(ois);
            }
        }

        if (foldersIdsBytes != null) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new ByteArrayInputStream(foldersIdsBytes));
                Object o = null;
                try {
                    o = ois.readObject();
                } catch (ClassNotFoundException e) {
                    throw new IOException("Could not read object", e);
                }
                if (o instanceof Map) {
                    this.containerIds.clear();
                    this.containerIds.putAll((Map) o);
                }
            } finally {
                IOUtils.closeQuietly(ois);
            }
        }
    }

    @Override
    protected void writePreferences(Preferences preferences) throws IOException {
        super.writePreferences(preferences);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ByteArrayOutputStream bosFoldersIds = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(bos);
            os.writeObject(this.fileModificationDates);
            preferences.putByteArray("fileModificationDates", bos.toByteArray());
        } finally {
            IOUtils.closeQuietly(os);
            os = null;
        }
        try {
            os = new ObjectOutputStream(bosFoldersIds);
            os.writeObject(this.containerIds);
            preferences.putByteArray("containerIds", bosFoldersIds.toByteArray());
        } finally {
            IOUtils.closeQuietly(os);
        }
    }
}
