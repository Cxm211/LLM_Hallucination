public void setTimeZone(TimeZone tz)
    {
        /* DateFormats are timezone-specific (via Calendar contained),
         * so need to reset instances if timezone changes:
         */
        if (!java.util.Objects.equals(tz, _timezone)) {
            _clearFormats();
            _timezone = tz;
        }
    }