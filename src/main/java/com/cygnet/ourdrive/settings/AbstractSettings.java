package com.cygnet.ourdrive.settings;

import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA on 12/10/12.
 *
 * @author werneraltewischer
 */
public class AbstractSettings implements Serializable {

    protected String username;
    protected String password;
    protected String serverBaseUrl;
    protected String uploadUri;
    protected String targetContainer;
    protected String apiKey;
    protected UploadMethod uploadMethod;
    protected Boolean moveFilesToTrash;
    protected AbstractSettings parentSettings;
    protected Set<SettingsListener> listeners = new HashSet<SettingsListener>();

    private final ThreadLocal<Boolean> resolvePropertiesFromParent = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return true;
        }
    };

    protected AbstractSettings() {
    }

    public String getUsername() {
        return getProperty("username", String.class);
    }

    public void setUsername(String username) {
        setProperty("username", String.class, username);
    }

    public String getPassword() {
        return getProperty("password", String.class);
    }

    public void setPassword(String password) {
        setProperty("password", String.class, password);
    }

    public String getTargetContainer() {
        return getProperty("targetContainer", String.class);
    }

    public void setTargetContainer(String targetContainer) {
        setProperty("targetContainer", String.class, targetContainer);
    }

    public String getServerBaseUrl() {
        return getProperty("serverBaseUrl", String.class);
    }

    public void setServerBaseUrl(String serverBaseUrl) {
        setProperty("serverBaseUrl", String.class, serverBaseUrl);
    }

    public String getUploadUri() {
        return getProperty("uploadUri", String.class);
    }

    public void setUploadUri(String uploadUri) {
        setProperty("uploadUri", String.class, uploadUri);
    }

    public String getDownloadPath() {
        Path currentRelativePath = Paths.get("");
        return currentRelativePath.toAbsolutePath().toString();
    }

    public String getApiKey() {
        return getProperty("apiKey", String.class);
    }


    public void setUpdownloadPath(String downloadPath) {
        setProperty("downloadPath", String.class, downloadPath);
    }

    public void setApiKey(String apiKey) {
        setProperty("apiKey", String.class, apiKey);
    }

    public UploadMethod getUploadMethod() {
        return getProperty("uploadMethod", UploadMethod.class);
    }

    public void setUploadMethod(UploadMethod uploadMethod) {
        setProperty("uploadMethod", UploadMethod.class, uploadMethod);
    }

    public Boolean getMoveFilesToTrash() {
        return getProperty("moveFilesToTrash", Boolean.class);
    }

    public void setMoveFilesToTrash(Boolean moveFilesToTrash) {
        setProperty("moveFilesToTrash", Boolean.class, moveFilesToTrash);
    }

    private boolean shouldResolvePropertiesFromParent() {
        return resolvePropertiesFromParent.get();
    }

    public void setResolvePropertiesFromParent(boolean resolvePropertiesFromParent) {
        this.resolvePropertiesFromParent.set(resolvePropertiesFromParent);
    }

    public void setParentSettings(AbstractSettings parentSettings) {
        this.parentSettings = parentSettings;
    }

    public String getUploadUrl() {
        String baseUrl = getServerBaseUrl();
        if (baseUrl == null) baseUrl = "";
        return baseUrl + getUploadUri();
    }

    @SuppressWarnings("unchecked")
    protected <T> T getProperty(String fieldName, Class<T> clazz, boolean resolveFromParent) {
        Object fieldValue = null;
        try {
            Field f = getField(getClass(), fieldName);
            fieldValue = f.get(this);
        } catch (Exception e) {
            throw new AssertionError("Illegal field specified: " + fieldName + ": " + e);
        }
        if (resolveFromParent) {
            if ((fieldValue == null || "".equals(fieldValue)) && parentSettings != null) {
                try {
                    fieldValue = PropertyUtils.getProperty(parentSettings, fieldName);
                } catch (Exception e) {
                    throw new AssertionError("Illegal field specified: " + fieldName + ": " + e);
                }
            }
        }
        if (fieldValue != null && !clazz.isInstance(fieldValue)) {
            throw new AssertionError("Illegal class supplied for field '" + fieldName + "': " + clazz);
        }
        return (T) fieldValue;
    }

    protected <T> T getProperty(String fieldName, Class<T> clazz) {
        return getProperty(fieldName, clazz, shouldResolvePropertiesFromParent());
    }

    protected <T> void setProperty(String fieldName, Class<T> clazz, T value) {
        final T currentFieldValue;
        final Field f;
        try {
            f = getField(getClass(), fieldName);
            currentFieldValue = clazz.cast(f.get(this));
        } catch (Exception e) {
            throw new AssertionError("Illegal field specified: " + fieldName + ": " + e);
        }

        if ((currentFieldValue == null && value != null) || (currentFieldValue != null && !currentFieldValue.equals(value))) {
            try {
                f.set(this, value);
            } catch (IllegalAccessException e) {
                throw new AssertionError("Could not access field: " + fieldName + ": " + e);
            }
            signalChange();
        }
    }

    protected void readPreferences(Preferences preferences) throws IOException {
        PreferencesUtils.loadProperties(this, preferences, getDefaults());
    }

    protected Map<String, String> getDefaults() {
        return null;
    }

    protected void writePreferences(Preferences preferences) throws IOException {
        this.setResolvePropertiesFromParent(false);
        PreferencesUtils.saveProperties(this, preferences);
        this.setResolvePropertiesFromParent(true);
    }

    private static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }

    public void addListener(SettingsListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SettingsListener listener) {
        listeners.remove(listener);
    }

    protected void signalChange() {
        for (SettingsListener sl : new ArrayList<SettingsListener>(listeners)) {
            sl.settingsChanged(this);
        }
    }

    protected void signalSave() {
        for (SettingsListener sl : new ArrayList<SettingsListener>(listeners)) {
            sl.settingsSaved(this);
        }
    }

    public static interface SettingsListener<T extends AbstractSettings> {
        void settingsChanged(T settings);

        void settingsSaved(T settings);
    }
}
