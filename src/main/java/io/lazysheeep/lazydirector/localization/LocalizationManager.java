package io.lazysheeep.lazydirector.localization;

import java.util.*;

public class LocalizationManager
{
    private static final Map<Locale, ResourceBundle> resourceBundles = new HashMap<>();

    public void reset()
    {
        resourceBundles.clear();
    }

    public static String GetLocalizedString(String key, Locale locale)
    {
        try
        {
            ResourceBundle resourceBundle = resourceBundles.computeIfAbsent(locale, loc -> ResourceBundle.getBundle("locale/messages", loc));
            return resourceBundle.getString(key);
        }
        catch (MissingResourceException e)
        {
            return key;
        }
    }
}
