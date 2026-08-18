private final static DateFormat _cloneFormat(DateFormat df, String format,
            TimeZone tz, Locale loc, Boolean lenient)
    {
        final boolean useCustomLocale = (loc != null) && !loc.equals(DEFAULT_LOCALE);
        final TimeZone tzToSet = (tz == null) ? DEFAULT_TIMEZONE : tz;
        if (useCustomLocale) {
            df = new SimpleDateFormat(format, loc);
            df.setTimeZone(tzToSet);
        } else {
            df = (DateFormat) df.clone();
            df.setTimeZone(tzToSet);
        }
        return df;
    }