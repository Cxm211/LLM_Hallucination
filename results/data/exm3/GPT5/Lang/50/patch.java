public static synchronized FastDateFormat getDateInstance(int style, TimeZone timeZone, Locale locale) {
        Locale effectiveLocale = (locale != null) ? locale : Locale.getDefault();

        Object key = new Integer(style);
        if (timeZone != null) {
            key = new Pair(key, timeZone);
        }
        key = new Pair(key, effectiveLocale);

        FastDateFormat format = (FastDateFormat) cDateInstanceCache.get(key);
        if (format == null) {
            try {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance(style, effectiveLocale);
                String pattern = formatter.toPattern();
                format = getInstance(pattern, timeZone, effectiveLocale);
                cDateInstanceCache.put(key, format);

            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date pattern for locale: " + effectiveLocale);
            }
        }
        return format;
    }