public void appendTo(StringBuffer buffer, Calendar calendar) {
    if (zone.inDaylightTime(calendar.getTime())) {
        buffer.append(mDaylight);
    } else {
        buffer.append(mStandard);
    }
}