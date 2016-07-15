package com.cygnet.ourdrive.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA on 12/10/12.
 *
 * @author werneraltewischer
 */
public class GlobalSettings extends AbstractSettings implements AbstractSettings.SettingsListener<FolderSettings> {

    private Map<File, FolderSettings> folderSettingsMap = new LinkedHashMap<File, FolderSettings>();

    private static final Logger logger = LoggerFactory.getLogger(GlobalSettings.class);
    private static GlobalSettings instance;

    public static synchronized GlobalSettings getInstance() {
        if (instance == null) {
            instance = new GlobalSettings();
            try {
                instance.load();
            } catch (IOException e) {
                logger.warn("Could not load settings", e);
            }
        }
        return instance;
    }

    public void load() throws IOException {
        load(cygnetRootPreferences());
    }

    public void save() throws IOException {
        save(cygnetRootPreferences());
    }

    protected void load(Preferences preferences) throws IOException {
        readPreferences(preferences);
        try {
            for (String childName : preferences.childrenNames()) {
                Preferences childPreferences = preferences.node(childName);
                FolderSettings fs = new FolderSettings();
                fs.readPreferences(childPreferences);
                addFolderSettingsImpl(fs, false);
            }
        } catch (BackingStoreException e) {
            throw new IOException("Could not read settings", e);
        }
    }

    protected void save(Preferences preferences) throws IOException {
        try {
            HashSet<String> names = new HashSet<String>();
            for (FolderSettings fs : getAllFolderSettings()) {
                names.add(fs.getName());
            }
            for (String childName : preferences.childrenNames()) {
                if (!names.contains(childName)) {
                    Preferences subPrefs = preferences.node(childName);
                    subPrefs.removeNode();
                }
            }
            for (FolderSettings fs : getAllFolderSettings()) {
                Preferences childPreferences = preferences.node(fs.getName());
                fs.writePreferences(childPreferences);
            }
            writePreferences(preferences);
            preferences.flush();
            signalSave();
        } catch (BackingStoreException e) {
            throw new IOException("Could not write settings", e);
        }
    }

    public FolderSettings getFolderSettings(File folder) {
        return folderSettingsMap.get(folder);
    }

    public void addFolderSettings(FolderSettings folderSettings) {
        addFolderSettingsImpl(folderSettings, true);
    }

    private void addFolderSettingsImpl(FolderSettings folderSettings, boolean signalChange) {
        if (folderSettings.getFolder() != null) {
            if (folderSettings.getName() == null) {
                folderSettings.initName();
            }
            FolderSettings previousSettings = folderSettingsMap.put(folderSettings.getFolder(), folderSettings);
            folderSettings.setParentSettings(this);
            folderSettings.addListener(this);
            if (previousSettings != folderSettings && previousSettings != null) {
                previousSettings.setParentSettings(null);
                previousSettings.removeListener(this);
            }
            if (signalChange) {
                signalChange();
            }
        }
    }


    @Override
    protected Map<String, String> getDefaults() {
        Map<String, String> defaults = new HashMap<String, String>();
        defaults.put("serverBaseUrl", "");
        defaults.put("uploadUri", "/xd/doc/new-formdata.php");
        defaults.put("uploadMethod", UploadMethod.POST.toString());
        return defaults;
    }

    public void removeFolderSettings(FolderSettings folderSettings) {
        FolderSettings removedSettings = folderSettingsMap.remove(folderSettings.getFolder());
        if (removedSettings != null) {
            removedSettings.setParentSettings(null);
            removedSettings.removeListener(this);
            signalChange();
        }
    }

    @Override
    public void settingsChanged(FolderSettings settings) {
        signalChange();
    }

    @Override
    public void settingsSaved(FolderSettings settings) {
    }

    public Collection<FolderSettings> getAllFolderSettings() {
        return folderSettingsMap.values();
    }

    private static Preferences cygnetRootPreferences() {
        Preferences rootPreferences = Preferences.userRoot();
        return rootPreferences.node("com.cygnet.ourdrive");
    }
}
