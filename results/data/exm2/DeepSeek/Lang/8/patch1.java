        public void appendTo(StringBuffer buffer, Calendar calendar) {
            TimeZone timeZone = calendar.getTimeZone();
            if (timeZone.useDaylightTime()
                    && calendar.get(Calendar.DST_OFFSET) != 0) {
                buffer.append(getTimeZoneDisplay(timeZone, true, mStyle, mLocale));
            } else {
                buffer.append(getTimeZoneDisplay(timeZone, false, mStyle, mLocale));
            }
        }