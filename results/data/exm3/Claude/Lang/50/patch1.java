public static synchronized FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone,
        Locale locale) {

    Object key = new Pair(new Integer(dateStyle), new Integer(timeStyle));
    if (timeZone != null) {
        key = new Pair(key, timeZone);
    }
    
    Locale actualLocale = locale;
    if (actualLocale == null) {
        actualLocale = Locale.getDefault();
    }
    key = new Pair(key, actualLocale);

    FastDateFormat format = (FastDateFormat) cDateTimeInstanceCache.get(key);
    if (format == null) {
        try {
            SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance(dateStyle, timeStyle,
                    actualLocale);
            String pattern = formatter.toPattern();
            format = getInstance(pattern, timeZone, actualLocale);
            cDateTimeInstanceCache.put(key, format);

        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("No date time pattern for locale: " + actualLocale);
        }
    }
    return format;
}