public void setTimeZone(TimeZone tz)
    {
        /* DateFormats are timezone-specific (via Calendar contained),
         * so need to reset instances if timezone changes:
         */
        if (tz == null) {
            if (_timezone != null) {
                _clearFormats();
                _timezone = null;
            }
        } else if (!tz.equals(_timezone)) {
            _clearFormats();
            _timezone = tz;
        }
    }