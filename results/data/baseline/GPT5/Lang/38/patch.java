public StringBuffer format(Calendar calendar, StringBuffer buf) {
        if (calendar == null) {
            throw new IllegalArgumentException("calendar must not be null");
        }
        if (buf == null) {
            buf = new StringBuffer();
        }
        if (mTimeZoneForced) {
            calendar = (Calendar) calendar.clone();
            calendar.setTimeZone(mTimeZone);
        }
        return applyRules(calendar, buf);
    }