// org/joda/time/TestDateTimeZoneCutover.java
public void testAddDayInOverlap() {
        DateTimeZone zone = DateTimeZone.forID("US/Central");
        DateTime utc1 = new DateTime(2008, 11, 2, 6, 30, 0, 0, DateTimeZone.UTC);
        DateTime utc2 = new DateTime(2008, 11, 2, 7, 30, 0, 0, DateTimeZone.UTC);
        DateTime local1 = utc1.toDateTime(zone);
        DateTime local2 = utc2.toDateTime(zone);
        assertEquals("2008-11-02T01:30:00.000-05:00", local1.toString());
        assertEquals("2008-11-02T01:30:00.000-06:00", local2.toString());
        DateTime test1 = local1.plusDays(0);
        DateTime test2 = local2.plusDays(0);
        assertEquals(local1, test1);
        assertEquals(local2, test2);
        assertEquals(local1.getZone().getOffset(local1.getMillis()), test1.getZone().getOffset(test1.getMillis()));
        assertEquals(local2.getZone().getOffset(local2.getMillis()), test2.getZone().getOffset(test2.getMillis()));
    }
