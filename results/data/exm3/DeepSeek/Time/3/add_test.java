// org/joda/time/TestMutableDateTime_Adds.java
public void testAddHoursMinutesSecondsMillisWeekyears_int_dstOverlapWinter_addZero() {
        // Test addWeekyears
        MutableDateTime test1 = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
        test1.addHours(1);
        assertEquals("2011-10-30T02:30:00.000+01:00", test1.toString());
        test1.addWeekyears(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test1.toString());
        // Test addHours
        MutableDateTime test2 = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
        test2.addHours(1);
        assertEquals("2011-10-30T02:30:00.000+01:00", test2.toString());
        test2.addHours(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test2.toString());
        // Test addMinutes
        MutableDateTime test3 = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
        test3.addHours(1);
        assertEquals("2011-10-30T02:30:00.000+01:00", test3.toString());
        test3.addMinutes(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test3.toString());
        // Test addSeconds
        MutableDateTime test4 = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
        test4.addHours(1);
        assertEquals("2011-10-30T02:30:00.000+01:00", test4.toString());
        test4.addSeconds(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test4.toString());
        // Test addMillis
        MutableDateTime test5 = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
        test5.addHours(1);
        assertEquals("2011-10-30T02:30:00.000+01:00", test5.toString());
        test5.addMillis(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test5.toString());
    }
