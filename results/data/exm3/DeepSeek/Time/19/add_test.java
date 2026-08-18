// org/joda/time/TestDateTimeZoneCutover.java
public void testDateTimeCreation_newYork_overlap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        DateTime base = new DateTime(2023, 11, 5, 1, 30, zone);
        assertEquals("2023-11-05T01:30:00.000-04:00", base.toString());
        assertEquals("2023-11-05T01:30:00.000-05:00", base.plusHours(1).toString());
    }
