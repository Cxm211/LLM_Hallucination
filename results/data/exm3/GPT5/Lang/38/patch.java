public StringBuffer format(Calendar calendar, StringBuffer buf) {
        if (mTimeZoneForced) {
            calendar = (Calendar) calendar.clone();
            // Force calculation of time based on the calendar's current time zone
            // before switching to the formatter's time zone to preserve the instant.
            calendar.getTime();
            calendar.setTimeZone(mTimeZone);
        }
        return applyRules(calendar, buf);
    }