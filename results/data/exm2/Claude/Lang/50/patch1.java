public static synchronized FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone,
            Locale locale) {

        Object key;
        if (locale == null) {
            locale = Locale.getDefault();
        }
        key = new Pair(new Pair(new Integer(dateStyle), new Integer(timeStyle)), locale);
        if (timeZone != null) {
            key = new Pair(key, timeZone);
        }

        FastDateFormat format = (FastDateFormat) cDateTimeInstanceCache.get(key);
        if (format == null) {
            try {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance(dateStyle, timeStyle,
                        locale);
                String pattern = formatter.toPattern();
                format = getInstance(pattern, timeZone, locale);
                cDateTimeInstanceCache.put(key, format);

            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + locale);
            }
        }
        return format;
    }