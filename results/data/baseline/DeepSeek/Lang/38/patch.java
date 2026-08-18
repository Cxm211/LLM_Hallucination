public StringBuffer format(Calendar calendar, StringBuffer buf) {
    calendar = (Calendar) calendar.clone();
    if (mTimeZoneForced) {
        calendar.setTimeZone(mTimeZone);
    }
    return applyRules(calendar, buf);
}