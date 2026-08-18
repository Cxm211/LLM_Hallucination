public StringBuffer format(Calendar calendar, StringBuffer buf) {
        if (mTimeZoneForced) {
            calendar = (Calendar) calendar.clone();
            // Ensure the calendar computes its time based on its original time zone
            // before switching to the formatter's time zone, so the instant is preserved.
            calendar.getTime();
            calendar.setTimeZone(mTimeZone);
        }
        return applyRules(calendar, buf);
    }