// org/joda/time/TestDateTimeZoneCutover.java
public void testSetMonthInOverlap() {
        DateTimeZone zone = DateTimeZone.forID("US/Central");
        DateTime utc1 = new DateTime(2008, 11, 2, 6, 30, 0, 0, DateTimeZone.UTC);
        DateTime utc2 = new DateTime(2008, 11, 2, 7, 30, 0, 0, DateTimeZone.UTC);
        DateTime local1 = utc1.toDateTime(zone);
        DateTime local2 = utc2.toDateTime(zone);
        assertEquals("2008-11-02T01:30:00.000-05:00", local1.toString());
        assertEquals("2008-11-02T01:30:00.000-06:00", local2.toString());
        DateTime test1 = local1.monthOfYear().setCopy("November");
        DateTime test2 = local2.monthOfYear().setCopy("November");
        assertEquals(local1, test1);
        assertEquals(local2, test2);
    }
