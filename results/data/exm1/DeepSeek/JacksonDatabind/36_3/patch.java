    public void setTimeZone(TimeZone tz)
    {
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