public static synchronized FastDateFormat getDateInstance(int style, TimeZone timeZone, Locale locale) {
    Object key = new Integer(style);
    if (timeZone != null) {
        key = new Pair(key, timeZone);
    }

    Locale actualLocale = locale;
    if (actualLocale == null) {
        actualLocale = Locale.getDefault();
    }
    key = new Pair(key, actualLocale);

    FastDateFormat format = (FastDateFormat) cDateInstanceCache.get(key);
    if (format == null) {
        try {
            SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance(style, actualLocale);
            String pattern = formatter.toPattern();
            format = getInstance(pattern, timeZone, actualLocale);
            cDateInstanceCache.put(key, format);
            
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("No date pattern for locale: " + actualLocale);
        }
    }
    return format;
}