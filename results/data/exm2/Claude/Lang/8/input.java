        TimeZoneNameRule(TimeZone timeZone, Locale locale, int style) {
            mLocale = locale;
            mStyle = style;
            zone = timeZone;
            
            mStandard = getTimeZoneDisplay(timeZone, false, style, locale);
            mDaylight = getTimeZoneDisplay(timeZone, true, style, locale);
        }

        public void appendTo(StringBuffer buffer, Calendar calendar) {
            if (zone.useDaylightTime()
                    && calendar.get(Calendar.DST_OFFSET) != 0) {
                buffer.append(getTimeZoneDisplay(zone, true, mStyle, mLocale));
            } else {
                buffer.append(getTimeZoneDisplay(zone, false, mStyle, mLocale));
            }
        }

// trigger testcase
@Test
    public void testCalendarTimezoneRespected() {
        String[] availableZones = TimeZone.getAvailableIDs();
        TimeZone currentZone = TimeZone.getDefault();
        
        TimeZone anotherZone = null;
        for (String zone : availableZones) {
            if (!zone.equals(currentZone.getID())) {
                anotherZone = TimeZone.getTimeZone(zone);
            }
        }
        
        assertNotNull("Cannot find another timezone", anotherZone);
        
        final String pattern = "h:mma z";
        final Calendar cal = Calendar.getInstance(anotherZone);
        
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(anotherZone);
        String expectedValue = sdf.format(cal.getTime());
        String actualValue = FastDateFormat.getInstance(pattern).format(cal);
        assertEquals(expectedValue, actualValue);
    }

@Test
    public void testCalendarTimezoneRespected() {
        String[] availableZones = TimeZone.getAvailableIDs();
        TimeZone currentZone = TimeZone.getDefault();
        
        TimeZone anotherZone = null;
        for (String zone : availableZones) {
            if (!zone.equals(currentZone.getID())) {
                anotherZone = TimeZone.getTimeZone(zone);
            }
        }
        
        assertNotNull("Cannot find another timezone", anotherZone);
        
        final String pattern = "h:mma z";
        final Calendar cal = Calendar.getInstance(anotherZone);
        
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(anotherZone);
        String expectedValue = sdf.format(cal.getTime());
        String actualValue = FastDateFormat.getInstance(pattern).format(cal);
        assertEquals(expectedValue, actualValue);
    }
