    private final static DateFormat _cloneFormat(DateFormat df, String format,
            TimeZone tz, Locale loc, Boolean lenient)
    {
        if (!loc.equals(DEFAULT_LOCALE)) {
            df = new SimpleDateFormat(format, loc);
            if (tz == null) {
                df.setTimeZone(DEFAULT_TIMEZONE);
            } else {
                df.setTimeZone(tz);
            }
            if (lenient != null) {
                df.setLenient(lenient);
            }
        } else {
            df = (DateFormat) df.clone();
            if (tz != null) {
                df.setTimeZone(tz);
            } else {
                df.setTimeZone(DEFAULT_TIMEZONE);
            }
            if (lenient != null) {
                df.setLenient(lenient);
            }
        }
        return df;
    }