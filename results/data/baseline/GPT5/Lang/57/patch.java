public static boolean isAvailableLocale(Locale locale) {
        if (locale == null) {
            return false;
        }
        return cAvailableLocaleSet != null && cAvailableLocaleSet.contains(locale);
    }