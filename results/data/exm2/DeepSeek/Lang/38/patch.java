public StringBuffer format(Calendar calendar, StringBuffer buf) {
    if (mTimeZoneForced) {
        calendar = (Calendar) calendar.clone();
        calendar.setTimeZone(mTimeZone);
        calendar.getTime();
    }
    return applyRules(calendar, buf);
}