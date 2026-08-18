// org/joda/time/TestDateTimeZoneCutover.java::testSetTextRetainsOffsetInOverlap
public void testSetTextRetainsOffsetInOverlap() {
        DateTimeZone zone = ZONE_NEW_YORK;
        // Time in the DST overlap where 01:30 occurs twice; choose standard time explicitly
        DateTime base = new DateTime("2007-11-04T01:30:00.000-05:00", zone);
        assertEquals("2007-11-04T01:30:00.000-05:00", base.toString());
        // Change month using text to hit set(long, String, Locale) path; should retain the original offset
        DateTime changed = base.monthOfYear().setCopy("November", Locale.ENGLISH);
        assertEquals("2007-11-04T01:30:00.000-05:00", changed.toString());
    }