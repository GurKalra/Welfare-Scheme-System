package com.welfareconnect.util;

import java.util.Locale;
import java.util.ResourceBundle;

public final class I18n {
    private static Locale currentLocale = Locale.ENGLISH;
    private static ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", currentLocale);

    private I18n() {}

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
    }

    public static String t(String key, Object... args) {
        String pattern = bundle.getString(key);
        return String.format(currentLocale, pattern, args);
    }
}
