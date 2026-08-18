private final static DateFormat _cloneFormat(DateFormat df, String format,
            TimeZone tz, Locale loc, Boolean lenient)
    {
        if (loc == null) {
            loc = DEFAULT_LOCALE;
        }
        TimeZone tzToUse = (tz == null) ? DEFAULT_TIMEZONE : tz;
        if (!loc.equals(DEFAULT_LOCALE)) {
            df = new SimpleDateFormat(format, loc);
            df.setTimeZone(tzToUse);
        } else {
            df = (DateFormat) df.clone();
            df.setTimeZone(tzToUse);
        }
        return df;
    }