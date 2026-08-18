// buggy code
    public void setTimeZone(TimeZone tz)
    {
        /* DateFormats are timezone-specific (via Calendar contained),
         * so need to reset instances if timezone changes:
         */
        TimeZone newTz = (tz == null) ? DEFAULT_TIMEZONE : tz;
        if (_timezone == null ? newTz != null : !_timezone.equals(newTz)) {
            _clearFormats();
        }
        _timezone = newTz;
    }

    private final static DateFormat _cloneFormat(DateFormat df, String format,
            TimeZone tz, Locale loc, Boolean lenient)
    {
        Locale useLoc = (loc == null) ? DEFAULT_LOCALE : loc;
        if (!useLoc.equals(DEFAULT_LOCALE)) {
            df = new SimpleDateFormat(format, useLoc);
        } else {
            df = (DateFormat) df.clone();
        }
        if (tz != null) {
            df.setTimeZone(tz);
        }
        if (lenient != null) {
            df.setLenient(lenient.booleanValue());
        }
        return df;
    }