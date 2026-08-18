        public void appendTo(StringBuffer buffer, Calendar calendar) {
            TimeZone calendarTimeZone = calendar.getTimeZone();
            if (calendarTimeZone.useDaylightTime()
                    && calendar.get(Calendar.DST_OFFSET) != 0) {
                buffer.append(getTimeZoneDisplay(calendarTimeZone, true, mStyle, mLocale));
            } else {
                buffer.append(getTimeZoneDisplay(calendarTimeZone, false, mStyle, mLocale));
            }
        }