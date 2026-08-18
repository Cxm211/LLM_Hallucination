public void appendTo(StringBuffer buffer, Calendar calendar) {
            TimeZone currentZone = calendar.getTimeZone();
            boolean daylight = calendar.get(Calendar.DST_OFFSET) != 0;
            buffer.append(getTimeZoneDisplay(currentZone, daylight, mStyle, mLocale));
        }