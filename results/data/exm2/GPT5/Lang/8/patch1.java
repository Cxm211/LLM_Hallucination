        public void appendTo(StringBuffer buffer, Calendar calendar) {
            TimeZone tz = calendar.getTimeZone();
            if (tz.useDaylightTime()
                    && calendar.get(Calendar.DST_OFFSET) != 0) {
                buffer.append(getTimeZoneDisplay(tz, true, mStyle, mLocale));
            } else {
                buffer.append(getTimeZoneDisplay(tz, false, mStyle, mLocale));
            }
        }