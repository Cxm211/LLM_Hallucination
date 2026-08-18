// org/joda/time/TestDateTimeZoneCutover.java::testDateTimeCreation_newyork
public void testDateTimeCreation_newyork() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        DateTime base = new DateTime(2011, 11, 6, 1, 15, zone);
        assertEquals("2011-11-06T01:15:00.000-04:00", base.toString());
        assertEquals("2011-11-06T01:15:00.000-05:00", base.plusHours(1).toString());
    }