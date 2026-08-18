public static synchronized FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone,
            Locale locale) {

        Locale effectiveLocale = (locale != null) ? locale : Locale.getDefault();

        Object key = new Pair(new Integer(dateStyle), new Integer(timeStyle));
        if (timeZone != null) {
            key = new Pair(key, timeZone);
        }
        key = new Pair(key, effectiveLocale);

        FastDateFormat format = (FastDateFormat) cDateTimeInstanceCache.get(key);
        if (format == null) {
            try {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance(dateStyle, timeStyle,
                        effectiveLocale);
                String pattern = formatter.toPattern();
                format = getInstance(pattern, timeZone, effectiveLocale);
                cDateTimeInstanceCache.put(key, format);

            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + effectiveLocale);
            }
        }
        return format;
    }