public static synchronized FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone, Locale locale) {
    Object key = new Pair(new Integer(dateStyle), new Integer(timeStyle));
    key = new Pair(key, (timeZone != null ? timeZone : TimeZone.getDefault()));
    key = new Pair(key, (locale != null ? locale : Locale.getDefault()));
    FastDateFormat format = (FastDateFormat) cDateTimeInstanceCache.get(key);
    if (format == null) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        try {
            SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
            String pattern = formatter.toPattern();
            format = getInstance(pattern, timeZone, locale);
            cDateTimeInstanceCache.put(key, format);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("No date time pattern for locale: " + locale);
        }
    }
    return format;
}