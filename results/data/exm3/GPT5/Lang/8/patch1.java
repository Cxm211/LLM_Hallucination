public void appendTo(StringBuffer buffer, Calendar calendar) {
            TimeZone tz = calendar.getTimeZone();
            boolean daylight = calendar.get(Calendar.DST_OFFSET) != 0;
            buffer.append(getTimeZoneDisplay(tz, daylight, mStyle, mLocale));
        }