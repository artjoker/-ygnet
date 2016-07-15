package com.cygnet.ourdrive.settings;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA on 17/10/12.
 *
 * @author werneraltewischer
 */
public final class PreferencesUtils {

    private static final Logger logger = LoggerFactory.getLogger(PreferencesUtils.class);

    private PreferencesUtils() {
    }

    public static void saveProperties(Object source, Preferences preferences) {
        syncProperties(source, preferences, true, null);
    }

    public static void loadProperties(Object source, Preferences preferences, Map<String, String> defaults) {
        syncProperties(source, preferences, false, defaults);
    }

    private static void syncProperties(Object source, Preferences preferences, boolean save, Map<String, String> defaults) {
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(source);
        for (PropertyDescriptor pd : propertyDescriptors) {
            if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                //Only process read/write methods
                Method readMethod = pd.getReadMethod();
                Object value = null;
                final String name = pd.getName();
                if (save) {
                    try {
                        value = readMethod.invoke(source);
                    } catch (Exception e) {
                        logger.warn("Could not invoke property getter", e);
                    }

                    if (value == null) {
                        preferences.remove(name);
                    } else {
                        preferences.put(name, value.toString());
                    }
                } else {
                    String defaultValue = null;
                    if (defaults != null) {
                        defaultValue = defaults.get(name);
                    }
                    String prefValue = preferences.get(name, defaultValue);
                    Class clazz = pd.getPropertyType();
                    Object fieldValue = null;
                    try {
                        if (prefValue != null && !String.class.isAssignableFrom(clazz)) {
                            try {
                                Constructor constructor = clazz.getConstructor(String.class);
                                fieldValue = constructor.newInstance(prefValue);
                            } catch (NoSuchMethodException e) {
                                //Try static constructor (for enums)
                                Method m = clazz.getDeclaredMethod("valueOf", String.class);
                                fieldValue = clazz.cast(m.invoke(clazz, prefValue));
                            }
                        } else {
                            fieldValue = clazz.cast(prefValue);
                        }
                    } catch (Exception e) {
                        logger.warn("Could not convert field value", e);
                    }
                    try {
                        Method writeMethod = pd.getWriteMethod();
                        writeMethod.invoke(source, fieldValue);
                    } catch (Exception e) {
                        logger.warn("Could not invoke property setter", e);
                    }
                }
            }
        }
    }
}
