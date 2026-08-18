public void setTimeZone(TimeZone tz)
    {
        /* DateFormats are timezone-specific (via Calendar contained),
         * so need to reset instances if timezone changes:
         */
        if (tz == null) {
            tz = DEFAULT_TIMEZONE;
        }
        if (!tz.equals(_timezone)) {
            _clearFormats();
            _timezone = tz;
        }
    }