// buggy code
    public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) throws IllegalArgumentException {
        if (hoursOffset == 0 && minutesOffset == 0) {
            return DateTimeZone.UTC;
        }
        if (minutesOffset < 0 || minutesOffset > 59) {
            throw new IllegalArgumentException("Minutes out of range: " + minutesOffset);
        }
        int offset = 0;
        try {
            int hoursInMinutes = FieldUtils.safeMultiply(hoursOffset, 60);
            if (hoursInMinutes < 0) {
                minutesOffset = FieldUtils.safeAdd(hoursInMinutes, -minutesOffset);
            } else {
                minutesOffset = FieldUtils.safeAdd(hoursInMinutes, minutesOffset);
            }
            offset = FieldUtils.safeMultiply(minutesOffset, DateTimeConstants.MILLIS_PER_MINUTE);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
        return forOffsetMillis(offset);
    }

    public static DateTimeZone forOffsetMillis(int millisOffset) {
        String id = printOffset(millisOffset);
        return fixedOffsetZone(id, millisOffset);
    }

// relevant test
// org.joda.time.TestMutableDateTime_Sets::testSetMonthOfYear_int2
    public void testSetMonthOfYear_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfMonth_int1
    public void testSetDayOfMonth_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setDayOfMonth(17);
        assertEquals("2002-06-17T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfMonth_int2
    public void testSetDayOfMonth_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setDayOfMonth(31);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfYear_int1
    public void testSetDayOfYear_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setDayOfYear(3);
        assertEquals("2002-01-03T05:06:07.008Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfYear_int2
    public void testSetDayOfYear_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setDayOfYear(366);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetWeekyear_int1
    public void testSetWeekyear_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setWeekyear(2001);
        assertEquals("2001-06-10T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetWeekOfWeekyear_int1
    public void testSetWeekOfWeekyear_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setWeekOfWeekyear(2);
        assertEquals("2002-01-13T05:06:07.008Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetWeekOfWeekyear_int2
    public void testSetWeekOfWeekyear_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setWeekOfWeekyear(53);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfWeek_int1
    public void testSetDayOfWeek_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setDayOfWeek(5);
        assertEquals("2002-06-07T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfWeek_int2
    public void testSetDayOfWeek_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setDayOfWeek(8);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetHourOfDay_int1
    public void testSetHourOfDay_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setHourOfDay(13);
        assertEquals("2002-06-09T13:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetHourOfDay_int2
    public void testSetHourOfDay_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setHourOfDay(24);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMinuteOfHour_int1
    public void testSetMinuteOfHour_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setMinuteOfHour(13);
        assertEquals("2002-06-09T05:13:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMinuteOfHour_int2
    public void testSetMinuteOfHour_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setMinuteOfHour(60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMinuteOfDay_int1
    public void testSetMinuteOfDay_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setMinuteOfDay(13);
        assertEquals("2002-06-09T00:13:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMinuteOfDay_int2
    public void testSetMinuteOfDay_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setMinuteOfDay(24 * 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetSecondOfMinute_int1
    public void testSetSecondOfMinute_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setSecondOfMinute(13);
        assertEquals("2002-06-09T05:06:13.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetSecondOfMinute_int2
    public void testSetSecondOfMinute_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setSecondOfMinute(60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetSecondOfDay_int1
    public void testSetSecondOfDay_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setSecondOfDay(13);
        assertEquals("2002-06-09T00:00:13.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetSecondOfDay_int2
    public void testSetSecondOfDay_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setSecondOfDay(24 * 60 * 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMilliOfSecond_int1
    public void testSetMilliOfSecond_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setMillisOfSecond(13);
        assertEquals("2002-06-09T05:06:07.013+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMilliOfSecond_int2
    public void testSetMilliOfSecond_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setMillisOfSecond(1000);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMilliOfDay_int1
    public void testSetMilliOfDay_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setMillisOfDay(13);
        assertEquals("2002-06-09T00:00:00.013+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMilliOfDay_int2
    public void testSetMilliOfDay_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setMillisOfDay(24 * 60 * 60 * 1000);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableInterval_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableInterval_Basics::testGetMillis
    public void testGetMillis() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1, test.getStart().getMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
        assertEquals(TEST_TIME2, test.getEnd().getMillis());
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDurationMillis());
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDuration().getMillis());
    }

// org.joda.time.TestMutableInterval_Basics::testGetDuration1
    public void testGetDuration1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDurationMillis());
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDuration().getMillis());
    }

// org.joda.time.TestMutableInterval_Basics::testGetDuration2
    public void testGetDuration2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME1);
        assertSame(Duration.ZERO, test.toDuration());
    }

// org.joda.time.TestMutableInterval_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        MutableInterval test1 = new MutableInterval(TEST_TIME1, TEST_TIME2);
        MutableInterval test2 = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        MutableInterval test3 = new MutableInterval(TEST_TIME_NOW, TEST_TIME2);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        MutableInterval test4 = new MutableInterval(TEST_TIME1, TEST_TIME2, GJChronology.getInstance());
        assertEquals(true, test4.equals(test4));
        assertEquals(false, test1.equals(test4));
        assertEquals(false, test2.equals(test4));
        assertEquals(false, test4.equals(test1));
        assertEquals(false, test4.equals(test2));
        assertEquals(false, test1.hashCode() == test4.hashCode());
        assertEquals(false, test2.hashCode() == test4.hashCode());
        
        MutableInterval test5 = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(true, test1.equals(test5));
        assertEquals(true, test2.equals(test5));
        assertEquals(false, test3.equals(test5));
        assertEquals(true, test5.equals(test1));
        assertEquals(true, test5.equals(test2));
        assertEquals(false, test5.equals(test3));
        assertEquals(true, test1.hashCode() == test5.hashCode());
        assertEquals(true, test2.hashCode() == test5.hashCode());
        assertEquals(false, test3.hashCode() == test5.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInterval()));
        assertEquals(false, test1.equals(new DateTime(TEST_TIME1)));
    }

// org.joda.time.TestMutableInterval_Basics::testContains_long
    public void testContains_long() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(true, test.contains(TEST_TIME1));
        assertEquals(false, test.contains(TEST_TIME1 - 1));
        assertEquals(true, test.contains(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2));
        assertEquals(false, test.contains(TEST_TIME2));
        assertEquals(true, test.contains(TEST_TIME2 - 1));
    }

// org.joda.time.TestMutableInterval_Basics::testContainsNow
    public void testContainsNow() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1);
        assertEquals(true, test.containsNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1 - 1);
        assertEquals(false, test.containsNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2);
        assertEquals(true, test.containsNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2);
        assertEquals(false, test.containsNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2 - 1);
        assertEquals(true, test.containsNow());
    }

// org.joda.time.TestMutableInterval_Basics::testContains_RI
    public void testContains_RI() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(true, test.contains(new Instant(TEST_TIME1)));
        assertEquals(false, test.contains(new Instant(TEST_TIME1 - 1)));
        assertEquals(true, test.contains(new Instant(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2)));
        assertEquals(false, test.contains(new Instant(TEST_TIME2)));
        assertEquals(true, test.contains(new Instant(TEST_TIME2 - 1)));
        assertEquals(true, test.contains((ReadableInstant) null));
    }

// org.joda.time.TestMutableInterval_Basics::testContains_RInterval
    public void testContains_RInterval() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(true, test.contains(new Interval(TEST_TIME1, TEST_TIME1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 1, TEST_TIME1)));
        
        assertEquals(true, test.contains(new Interval(TEST_TIME1, TEST_TIME1 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 1, TEST_TIME1 + 1)));
        assertEquals(true, test.contains(new Interval(TEST_TIME1 + 1, TEST_TIME1 + 1)));
        
        assertEquals(true, test.contains(new Interval(TEST_TIME1, TEST_TIME2)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 1, TEST_TIME2)));
        assertEquals(true, test.contains(new Interval(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2, TEST_TIME2)));
        assertEquals(false, test.contains(new Interval(TEST_TIME2, TEST_TIME2)));
        assertEquals(true, test.contains(new Interval(TEST_TIME2 - 1, TEST_TIME2)));
        
        assertEquals(true, test.contains(new Interval(TEST_TIME1, TEST_TIME2 - 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 1, TEST_TIME2 - 1)));
        assertEquals(true, test.contains(new Interval(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2, TEST_TIME2 - 1)));
        assertEquals(true, test.contains(new Interval(TEST_TIME2 - 1, TEST_TIME2 - 1)));
        assertEquals(true, test.contains(new Interval(TEST_TIME2 - 2, TEST_TIME2 - 1)));
        
        assertEquals(false, test.contains(new Interval(TEST_TIME1, TEST_TIME2 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 1, TEST_TIME2 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2, TEST_TIME2 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME2, TEST_TIME2 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME2 - 1, TEST_TIME2 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 2, TEST_TIME1 - 1)));
        
        assertEquals(true, test.contains((ReadableInterval) null));
    }

// org.joda.time.TestMutableInterval_Basics::testOverlaps_RInterval
    public void testOverlaps_RInterval() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(false, test.overlaps(new Interval(TEST_TIME1, TEST_TIME1)));
        assertEquals(false, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME1)));
        
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1, TEST_TIME1 + 1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME1 + 1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 + 1, TEST_TIME1 + 1)));
        
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1, TEST_TIME2)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME2)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2, TEST_TIME2)));
        assertEquals(false, test.overlaps(new Interval(TEST_TIME2, TEST_TIME2)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME2 - 1, TEST_TIME2)));
        
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1, TEST_TIME2 + 1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME2 + 1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2, TEST_TIME2 + 1)));
        assertEquals(false, test.overlaps(new Interval(TEST_TIME2, TEST_TIME2 + 1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME2 - 1, TEST_TIME2 + 1)));
        
        assertEquals(false, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME1 - 1)));
        assertEquals(false, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME1 + 1)));
        
        assertEquals(true, test.overlaps((ReadableInterval) null));
        
        MutableInterval empty = new MutableInterval(TEST_TIME1, TEST_TIME1);
        assertEquals(false, empty.overlaps(empty));
        assertEquals(false, empty.overlaps(test));
        assertEquals(false, test.overlaps(empty));
    }

// org.joda.time.TestMutableInterval_Basics::testIsBefore_long
    public void testIsBefore_long() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(false, test.isBefore(TEST_TIME1 - 1));
        assertEquals(false, test.isBefore(TEST_TIME1));
        assertEquals(false, test.isBefore(TEST_TIME1 + 1));
        
        assertEquals(false, test.isBefore(TEST_TIME2 - 1));
        assertEquals(true, test.isBefore(TEST_TIME2));
        assertEquals(true, test.isBefore(TEST_TIME2 + 1));
    }

// org.joda.time.TestMutableInterval_Basics::testIsBeforeNow
    public void testIsBeforeNow() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2 - 1);
        assertEquals(false, test.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2);
        assertEquals(true, test.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2 + 1);
        assertEquals(true, test.isBeforeNow());
    }

// org.joda.time.TestMutableInterval_Basics::testIsBefore_RI
    public void testIsBefore_RI() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(false, test.isBefore(new Instant(TEST_TIME1 - 1)));
        assertEquals(false, test.isBefore(new Instant(TEST_TIME1)));
        assertEquals(false, test.isBefore(new Instant(TEST_TIME1 + 1)));
        
        assertEquals(false, test.isBefore(new Instant(TEST_TIME2 - 1)));
        assertEquals(true, test.isBefore(new Instant(TEST_TIME2)));
        assertEquals(true, test.isBefore(new Instant(TEST_TIME2 + 1)));
        
        assertEquals(false, test.isBefore((ReadableInstant) null));
    }

// org.joda.time.TestMutableInterval_Basics::testIsBefore_RInterval
    public void testIsBefore_RInterval() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(false, test.isBefore(new Interval(Long.MIN_VALUE, TEST_TIME1 - 1)));
        assertEquals(false, test.isBefore(new Interval(Long.MIN_VALUE, TEST_TIME1)));
        assertEquals(false, test.isBefore(new Interval(Long.MIN_VALUE, TEST_TIME1 + 1)));
        
        assertEquals(false, test.isBefore(new Interval(TEST_TIME2 - 1, Long.MAX_VALUE)));
        assertEquals(true, test.isBefore(new Interval(TEST_TIME2, Long.MAX_VALUE)));
        assertEquals(true, test.isBefore(new Interval(TEST_TIME2 + 1, Long.MAX_VALUE)));
        
        assertEquals(false, test.isBefore((ReadableInterval) null));
    }

// org.joda.time.TestMutableInterval_Basics::testIsAfter_long
    public void testIsAfter_long() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(true, test.isAfter(TEST_TIME1 - 1));
        assertEquals(false, test.isAfter(TEST_TIME1));
        assertEquals(false, test.isAfter(TEST_TIME1 + 1));
        
        assertEquals(false, test.isAfter(TEST_TIME2 - 1));
        assertEquals(false, test.isAfter(TEST_TIME2));
        assertEquals(false, test.isAfter(TEST_TIME2 + 1));
    }

// org.joda.time.TestMutableInterval_Basics::testIsAfterNow
    public void testIsAfterNow() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1 - 1);
        assertEquals(true, test.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1);
        assertEquals(false, test.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1 + 1);
        assertEquals(false, test.isAfterNow());
    }

// org.joda.time.TestMutableInterval_Basics::testIsAfter_RI
    public void testIsAfter_RI() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(true, test.isAfter(new Instant(TEST_TIME1 - 1)));
        assertEquals(false, test.isAfter(new Instant(TEST_TIME1)));
        assertEquals(false, test.isAfter(new Instant(TEST_TIME1 + 1)));
        
        assertEquals(false, test.isAfter(new Instant(TEST_TIME2 - 1)));
        assertEquals(false, test.isAfter(new Instant(TEST_TIME2)));
        assertEquals(false, test.isAfter(new Instant(TEST_TIME2 + 1)));
        
        assertEquals(false, test.isAfter((ReadableInstant) null));
    }

// org.joda.time.TestMutableInterval_Basics::testIsAfter_RInterval
    public void testIsAfter_RInterval() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(true, test.isAfter(new Interval(Long.MIN_VALUE, TEST_TIME1 - 1)));
        assertEquals(true, test.isAfter(new Interval(Long.MIN_VALUE, TEST_TIME1)));
        assertEquals(false, test.isAfter(new Interval(Long.MIN_VALUE, TEST_TIME1 + 1)));
        
        assertEquals(false, test.isAfter(new Interval(TEST_TIME2 - 1, Long.MAX_VALUE)));
        assertEquals(false, test.isAfter(new Interval(TEST_TIME2, Long.MAX_VALUE)));
        assertEquals(false, test.isAfter(new Interval(TEST_TIME2 + 1, Long.MAX_VALUE)));
        
        assertEquals(false, test.isAfter((ReadableInterval) null));
    }

// org.joda.time.TestMutableInterval_Basics::testToInterval1
    public void testToInterval1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval result = test.toInterval();
        assertEquals(test, result);
    }

// org.joda.time.TestMutableInterval_Basics::testToMutableInterval1
    public void testToMutableInterval1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        MutableInterval result = test.toMutableInterval();
        assertEquals(test, result);
        assertNotSame(test, result);
    }

// org.joda.time.TestMutableInterval_Basics::testToPeriod
    public void testToPeriod() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, COPTIC_PARIS);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, COPTIC_PARIS);
        MutableInterval base = new MutableInterval(dt1, dt2);
        
        Period test = base.toPeriod();
        Period expected = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(expected, test);
    }

// org.joda.time.TestMutableInterval_Basics::testToPeriod_PeriodType1
    public void testToPeriod_PeriodType1() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, COPTIC_PARIS);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, COPTIC_PARIS);
        MutableInterval base = new MutableInterval(dt1, dt2);
        
        Period test = base.toPeriod(null);
        Period expected = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(expected, test);
    }

// org.joda.time.TestMutableInterval_Basics::testToPeriod_PeriodType2
    public void testToPeriod_PeriodType2() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18);
        MutableInterval base = new MutableInterval(dt1, dt2);
        
        Period test = base.toPeriod(PeriodType.yearWeekDayTime());
        Period expected = new Period(dt1, dt2, PeriodType.yearWeekDayTime());
        assertEquals(expected, test);
    }

// org.joda.time.TestMutableInterval_Basics::testSerialization
    public void testSerialization() throws Exception {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        MutableInterval result = (MutableInterval) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestMutableInterval_Basics::testToString
    public void testToString() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, DateTimeZone.UTC);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, DateTimeZone.UTC);
        MutableInterval test = new MutableInterval(dt1, dt2);
        assertEquals("2004-06-09T07:08:09.010Z/2005-08-13T12:14:16.018Z", test.toString());
    }

// org.joda.time.TestMutableInterval_Basics::testCopy
    public void testCopy() {
        MutableInterval test = new MutableInterval(123L, 456L, COPTIC_PARIS);
        MutableInterval cloned = test.copy();
        assertEquals(test, cloned);
        assertNotSame(test, cloned);
    }

// org.joda.time.TestMutableInterval_Basics::testClone
    public void testClone() {
        MutableInterval test = new MutableInterval(123L, 456L, COPTIC_PARIS);
        MutableInterval cloned = (MutableInterval) test.clone();
        assertEquals(test, cloned);
        assertNotSame(test, cloned);
    }

// org.joda.time.TestMutableInterval_Constructors::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableInterval_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        DateTime start = new DateTime(2010, 6, 30, 12, 30, ISOChronology.getInstance(PARIS));
        DateTime end = new DateTime(2010, 7, 1, 14, 30, ISOChronology.getInstance(PARIS));
        assertEquals(new MutableInterval(start, end), MutableInterval.parse("2010-06-30T12:30/2010-07-01T14:30"));
        assertEquals(new MutableInterval(start, end), MutableInterval.parse("2010-06-30T12:30/P1DT2H"));
        assertEquals(new MutableInterval(start, end), MutableInterval.parse("P1DT2H/2010-07-01T14:30"));
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        MutableInterval test = new MutableInterval();
        assertEquals(0L, test.getStartMillis());
        assertEquals(0L, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_long_long1
    public void testConstructor_long_long1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1.getMillis(), dt2.getMillis());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_long_long2
    public void testConstructor_long_long2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        MutableInterval test = new MutableInterval(dt1.getMillis(), dt1.getMillis());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt1.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_long_long3
    public void testConstructor_long_long3() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        DateTime dt2 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            new MutableInterval(dt1.getMillis(), dt2.getMillis());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_long_long_Chronology1
    public void testConstructor_long_long_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1.getMillis(), dt2.getMillis(), GJChronology.getInstance());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(GJChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_long_long_Chronology2
    public void testConstructor_long_long_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1.getMillis(), dt2.getMillis(), null);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI1
    public void testConstructor_RI_RI1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI2
    public void testConstructor_RI_RI2() throws Throwable {
        Instant dt1 = new Instant(new DateTime(2004, 6, 9, 0, 0, 0, 0));
        Instant dt2 = new Instant(new DateTime(2005, 7, 10, 1, 1, 1, 1));
        MutableInterval test = new MutableInterval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI3
    public void testConstructor_RI_RI3() throws Throwable {
        MutableInterval test = new MutableInterval((ReadableInstant) null, (ReadableInstant) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI4
    public void testConstructor_RI_RI4() throws Throwable {
        DateTime dt1 = new DateTime(2000, 6, 9, 0, 0, 0, 0);
        MutableInterval test = new MutableInterval(dt1, (ReadableInstant) null);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI5
    public void testConstructor_RI_RI5() throws Throwable {
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval((ReadableInstant) null, dt2);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI6
    public void testConstructor_RI_RI6() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        MutableInterval test = new MutableInterval(dt1, dt1);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt1.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI7
    public void testConstructor_RI_RI7() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        DateTime dt2 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            new MutableInterval(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI8
    public void testConstructor_RI_RI8() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, GJChronology.getInstance());
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(GJChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI9
    public void testConstructor_RI_RI9() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, GJChronology.getInstance());
        MutableInterval test = new MutableInterval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RP1
    public void testConstructor_RI_RP1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, 6);
        result = ISOChronology.getInstance().hours().add(result, 1);
        
        MutableInterval test = new MutableInterval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RP2
    public void testConstructor_RI_RP2() throws Throwable {
        Instant dt = new Instant(new DateTime(TEST_TIME_NOW));
        Period dur = new Period(0, 6, 0, 3, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstanceUTC().months().add(result, 6);
        result = ISOChronology.getInstanceUTC().days().add(result, 3);
        result = ISOChronology.getInstanceUTC().hours().add(result, 1);
        
        MutableInterval test = new MutableInterval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RP3
    public void testConstructor_RI_RP3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, ISOChronology.getInstanceUTC());
        Period dur = new Period(0, 6, 0, 3, 1, 0, 0, 0, PeriodType.standard());
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstanceUTC().months().add(result, 6);
        result = ISOChronology.getInstanceUTC().days().add(result, 3);
        result = ISOChronology.getInstanceUTC().hours().add(result, 1);
        
        MutableInterval test = new MutableInterval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RP4
    public void testConstructor_RI_RP4() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(1 * DateTimeConstants.MILLIS_PER_HOUR + 23L);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().hours().add(result, 1);
        result = ISOChronology.getInstance().millis().add(result, 23);
        
        MutableInterval test = new MutableInterval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RP5
    public void testConstructor_RI_RP5() throws Throwable {
        MutableInterval test = new MutableInterval((ReadableInstant) null, (ReadablePeriod) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RP6
    public void testConstructor_RI_RP6() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        MutableInterval test = new MutableInterval(dt, (ReadablePeriod) null);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RP7
    public void testConstructor_RI_RP7() throws Throwable {
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, 6);
        result = ISOChronology.getInstance().hourOfDay().add(result, 1);
        
        MutableInterval test = new MutableInterval((ReadableInstant) null, dur);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RP8
    public void testConstructor_RI_RP8() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        try {
            new MutableInterval(dt, dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI1
    public void testConstructor_RP_RI1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, -6);
        result = ISOChronology.getInstance().hours().add(result, -1);
        
        MutableInterval test = new MutableInterval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI2
    public void testConstructor_RP_RI2() throws Throwable {
        Instant dt = new Instant(new DateTime(TEST_TIME_NOW));
        Period dur = new Period(0, 6, 0, 3, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstanceUTC().months().add(result, -6);
        result = ISOChronology.getInstanceUTC().days().add(result, -3);
        result = ISOChronology.getInstanceUTC().hours().add(result, -1);
        
        MutableInterval test = new MutableInterval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI3
    public void testConstructor_RP_RI3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, ISOChronology.getInstanceUTC());
        Period dur = new Period(0, 6, 0, 3, 1, 0, 0, 0, PeriodType.standard());
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstanceUTC().months().add(result, -6);
        result = ISOChronology.getInstanceUTC().days().add(result, -3);
        result = ISOChronology.getInstanceUTC().hours().add(result, -1);
        
        MutableInterval test = new MutableInterval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI4
    public void testConstructor_RP_RI4() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(1 * DateTimeConstants.MILLIS_PER_HOUR + 23L);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().hours().add(result, -1);
        result = ISOChronology.getInstance().millis().add(result, -23);
        
        MutableInterval test = new MutableInterval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI5
    public void testConstructor_RP_RI5() throws Throwable {
        MutableInterval test = new MutableInterval((ReadablePeriod) null, (ReadableInstant) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI6
    public void testConstructor_RP_RI6() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        MutableInterval test = new MutableInterval((ReadablePeriod) null, dt);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI7
    public void testConstructor_RP_RI7() throws Throwable {
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, -6);
        result = ISOChronology.getInstance().hourOfDay().add(result, -1);
        
        MutableInterval test = new MutableInterval(dur, (ReadableInstant) null);
        assertEquals(result, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI8
    public void testConstructor_RP_RI8() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        try {
            new MutableInterval(dur, dt);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RD1
    public void testConstructor_RI_RD1() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, 6);
        result = ISOChronology.getInstance().hours().add(result, 1);
        
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(result - TEST_TIME_NOW);
        
        MutableInterval test = new MutableInterval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RD2
    public void testConstructor_RI_RD2() throws Throwable {
        MutableInterval test = new MutableInterval((ReadableInstant) null, (ReadableDuration) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RD3
    public void testConstructor_RI_RD3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        MutableInterval test = new MutableInterval(dt, (ReadableDuration) null);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RD4
    public void testConstructor_RI_RD4() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, 6);
        result = ISOChronology.getInstance().hourOfDay().add(result, 1);
        
        Duration dur = new Duration(result - TEST_TIME_NOW);
        
        MutableInterval test = new MutableInterval((ReadableInstant) null, dur);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RD5
    public void testConstructor_RI_RD5() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(-1);
        try {
            new MutableInterval(dt, dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RD_RI1
    public void testConstructor_RD_RI1() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, -6);
        result = ISOChronology.getInstance().hours().add(result, -1);
        
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(TEST_TIME_NOW - result);
        
        MutableInterval test = new MutableInterval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RD_RI2
    public void testConstructor_RD_RI2() throws Throwable {
        MutableInterval test = new MutableInterval((ReadableDuration) null, (ReadableInstant) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RD_RI3
    public void testConstructor_RD_RI3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        MutableInterval test = new MutableInterval((ReadableDuration) null, dt);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RD_RI4
    public void testConstructor_RD_RI4() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, -6);
        result = ISOChronology.getInstance().hourOfDay().add(result, -1);
        
        Duration dur = new Duration(TEST_TIME_NOW - result);
        
        MutableInterval test = new MutableInterval(dur, (ReadableInstant) null);
        assertEquals(result, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RD_RI5
    public void testConstructor_RD_RI5() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(-1);
        try {
            new MutableInterval(dur, dt);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1.toString() + '/' + dt2.toString());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval base = new MutableInterval(dt1, dt2);
        
        MutableInterval test = new MutableInterval(base);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object3
    public void testConstructor_Object3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval base = new Interval(dt1, dt2);
        
        MutableInterval test = new MutableInterval(base);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object4
    public void testConstructor_Object4() throws Throwable {
        MockInterval base = new MockInterval();
        MutableInterval test = new MutableInterval(base);
        assertEquals(base.getStartMillis(), test.getStartMillis());
        assertEquals(base.getEndMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object5
    public void testConstructor_Object5() throws Throwable {
        IntervalConverter oldConv = ConverterManager.getInstance().getIntervalConverter("");
        IntervalConverter conv = new IntervalConverter() {
            public boolean isReadableInterval(Object object, Chronology chrono) {
                return false;
            }
            public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {
                interval.setChronology(chrono);
                interval.setInterval(1234L, 5678L);
            }
            public Class<?> getSupportedType() {
                return String.class;
            }
        };
        try {
            ConverterManager.getInstance().addIntervalConverter(conv);
            DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
            DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
            MutableInterval test = new MutableInterval(dt1.toString() + '/' + dt2.toString());
            assertEquals(1234L, test.getStartMillis());
            assertEquals(5678L, test.getEndMillis());
        } finally {
            ConverterManager.getInstance().addIntervalConverter(oldConv);
        }
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object6
    public void testConstructor_Object6() throws Throwable {
        IntervalConverter oldConv = ConverterManager.getInstance().getIntervalConverter(new MutableInterval(0L, 0L));
        IntervalConverter conv = new IntervalConverter() {
            public boolean isReadableInterval(Object object, Chronology chrono) {
                return false;
            }
            public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {
                interval.setChronology(chrono);
                interval.setInterval(1234L, 5678L);
            }
            public Class<?> getSupportedType() {
                return ReadableInterval.class;
            }
        };
        try {
            ConverterManager.getInstance().addIntervalConverter(conv);
            Interval base = new Interval(-1000L, 1000L);
            MutableInterval test = new MutableInterval(base);
            assertEquals(1234L, test.getStartMillis());
            assertEquals(5678L, test.getEndMillis());
        } finally {
            ConverterManager.getInstance().addIntervalConverter(oldConv);
        }
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object_Chronology1
    public void testConstructor_Object_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval base = new Interval(dt1, dt2);
        
        MutableInterval test = new MutableInterval(base, BuddhistChronology.getInstance());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(BuddhistChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object_Chronology2
    public void testConstructor_Object_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval base = new Interval(dt1, dt2);
        
        MutableInterval test = new MutableInterval(base, null);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Updates::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_long_long1
    public void testSetInterval_long_long1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(TEST_TIME1 - 1, TEST_TIME2 + 1);
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_long_long2
    public void testSetInterval_long_long2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setInterval(TEST_TIME1 - 1, TEST_TIME1 - 2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RI_RI1
    public void testSetInterval_RI_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(new Instant(TEST_TIME1 - 1), new Instant(TEST_TIME2 + 1));
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RI_RI2
    public void testSetInterval_RI_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setInterval(new Instant(TEST_TIME1 - 1), new Instant(TEST_TIME1 - 2));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RI_RI3
    public void testSetInterval_RI_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(null, new Instant(TEST_TIME2 + 1));
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RI_RI4
    public void testSetInterval_RI_RI4() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(new Instant(TEST_TIME1 - 1), null);
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RI_RI5
    public void testSetInterval_RI_RI5() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(null, null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RInterval1
    public void testSetInterval_RInterval1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(new Interval(TEST_TIME1 - 1, TEST_TIME2 + 1));
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RInterval2
    public void testSetInterval_RInterval2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setInterval(new MockBadInterval());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RInterval3
    public void testSetInterval_RInterval3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setInterval(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetStartMillis_long1
    public void testSetStartMillis_long1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setStartMillis(TEST_TIME1 - 1);
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetStartMillis_long2
    public void testSetStartMillis_long2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setStartMillis(TEST_TIME2 + 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetStart_RI1
    public void testSetStart_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setStart(new Instant(TEST_TIME1 - 1));
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetStart_RI2
    public void testSetStart_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setStart(new Instant(TEST_TIME2 + 1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetStart_RI3
    public void testSetStart_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setStart(null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetEndMillis_long1
    public void testSetEndMillis_long1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setEndMillis(TEST_TIME2 + 1);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetEndMillis_long2
    public void testSetEndMillis_long2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setEndMillis(TEST_TIME1 - 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetEnd_RI1
    public void testSetEnd_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setEnd(new Instant(TEST_TIME2 + 1));
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetEnd_RI2
    public void testSetEnd_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setEnd(new Instant(TEST_TIME1 - 1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetEnd_RI3
    public void testSetEnd_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setEnd(null);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationAfterStart_long1
    public void testSetDurationAfterStart_long1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationAfterStart(123L);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1 + 123L, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSeDurationAfterStart_long2
    public void testSeDurationAfterStart_long2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setDurationAfterStart(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationAfterStart_RI1
    public void testSetDurationAfterStart_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationAfterStart(new Duration(123L));
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1 + 123L, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSeDurationAfterStart_RI2
    public void testSeDurationAfterStart_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setDurationAfterStart(new Duration(-1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationAfterStart_RI3
    public void testSetDurationAfterStart_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationAfterStart(null);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationBeforeEnd_long1
    public void testSetDurationBeforeEnd_long1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationBeforeEnd(123L);
        assertEquals(TEST_TIME2 - 123L, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSeDurationBeforeEnd_long2
    public void testSeDurationBeforeEnd_long2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setDurationBeforeEnd(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationBeforeEnd_RI1
    public void testSetDurationBeforeEnd_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationBeforeEnd(new Duration(123L));
        assertEquals(TEST_TIME2 - 123L, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSeDurationBeforeEnd_RI2
    public void testSeDurationBeforeEnd_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setDurationBeforeEnd(new Duration(-1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationBeforeEnd_RI3
    public void testSetDurationBeforeEnd_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationBeforeEnd(null);
        assertEquals(TEST_TIME2, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetPeriodAfterStart_RI1
    public void testSetPeriodAfterStart_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setPeriodAfterStart(new Period(123L));
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1 + 123L, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSePeriodAfterStart_RI2
    public void testSePeriodAfterStart_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setPeriodAfterStart(new Period(-1L));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetPeriodAfterStart_RI3
    public void testSetPeriodAfterStart_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setPeriodAfterStart(null);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetPeriodBeforeEnd_RI1
    public void testSetPeriodBeforeEnd_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setPeriodBeforeEnd(new Period(123L));
        assertEquals(TEST_TIME2 - 123L, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSePeriodBeforeEnd_RI2
    public void testSePeriodBeforeEnd_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setPeriodBeforeEnd(new Period(-1L));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetPeriodBeforeEnd_RI3
    public void testSetPeriodBeforeEnd_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setPeriodBeforeEnd(null);
        assertEquals(TEST_TIME2, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutablePeriod_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutablePeriod_Basics::testGetPeriodType
    public void testGetPeriodType() {
        MutablePeriod test = new MutablePeriod();
        assertEquals(PeriodType.standard(), test.getPeriodType());
    }

// org.joda.time.TestMutablePeriod_Basics::testGetMethods
    public void testGetMethods() {
        MutablePeriod test = new MutablePeriod();
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        MutablePeriod test1 = new MutablePeriod(123L);
        MutablePeriod test2 = new MutablePeriod(123L);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        MutablePeriod test3 = new MutablePeriod(321L);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockMutablePeriod(123L)));
        assertEquals(false, test1.equals(new Period(123L, PeriodType.dayTime())));
    }

// org.joda.time.TestMutablePeriod_Basics::testSerialization
    public void testSerialization() throws Exception {
        MutablePeriod test = new MutablePeriod(123L);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        MutablePeriod result = (MutablePeriod) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestMutablePeriod_Basics::testToString
    public void testToString() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", test.toString());
        
        test = new MutablePeriod(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("PT0S", test.toString());
        
        test = new MutablePeriod(12345L);
        assertEquals("PT12.345S", test.toString());
    }

// org.joda.time.TestMutablePeriod_Basics::testToPeriod
    public void testToPeriod() {
        MutablePeriod test = new MutablePeriod(123L);
        Period result = test.toPeriod();
        assertEquals(test, result);
    }

// org.joda.time.TestMutablePeriod_Basics::testToMutablePeriod
    public void testToMutablePeriod() {
        MutablePeriod test = new MutablePeriod(123L);
        MutablePeriod result = test.toMutablePeriod();
        assertEquals(test, result);
    }

// org.joda.time.TestMutablePeriod_Basics::testToDurationFrom
    public void testToDurationFrom() {
        MutablePeriod test = new MutablePeriod(123L);
        assertEquals(new Duration(123L), test.toDurationFrom(new Instant(0L)));
    }

// org.joda.time.TestMutablePeriod_Basics::testCopy
    public void testCopy() {
        MutablePeriod test = new MutablePeriod(123L);
        MutablePeriod copy = test.copy();
        assertEquals(test.getPeriodType(), copy.getPeriodType());
        assertEquals(test, copy);
    }

// org.joda.time.TestMutablePeriod_Basics::testClone
    public void testClone() {
        MutablePeriod test = new MutablePeriod(123L);
        MutablePeriod copy = (MutablePeriod) test.clone();
        assertEquals(test.getPeriodType(), copy.getPeriodType());
        assertEquals(test, copy);
    }

// org.joda.time.TestMutablePeriod_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 890), MutablePeriod.parse("P1Y2M3W4DT5H6M7.890S"));
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor1
    public void testConstructor1() throws Throwable {
        MutablePeriod test = new MutablePeriod();
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_PeriodType1
    public void testConstructor_PeriodType1() throws Throwable {
        MutablePeriod test = new MutablePeriod(PeriodType.yearMonthDayTime());
        assertEquals(PeriodType.yearMonthDayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_PeriodType2
    public void testConstructor_PeriodType2() throws Throwable {
        MutablePeriod test = new MutablePeriod((PeriodType) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        long length =
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long3
    public void testConstructor_long3() throws Throwable {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        MutablePeriod test = new MutablePeriod(length);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType1
    public void testConstructor_long_PeriodType1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, (PeriodType) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType2
    public void testConstructor_long_PeriodType2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.millis());
        assertEquals(PeriodType.millis(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(length, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType3
    public void testConstructor_long_PeriodType3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.standard());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType4
    public void testConstructor_long_PeriodType4() throws Throwable {
        long length =
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.standard().withMillisRemoved());
        assertEquals(PeriodType.standard().withMillisRemoved(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_Chronology1
    public void testConstructor_long_Chronology1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, ISOChronology.getInstance());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_Chronology2
    public void testConstructor_long_Chronology2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, ISOChronology.getInstanceUTC());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_Chronology3
    public void testConstructor_long_Chronology3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, (Chronology) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType_Chronology1
    public void testConstructor_long_PeriodType_Chronology1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.time().withMillisRemoved(), ISOChronology.getInstance());
        assertEquals(PeriodType.time().withMillisRemoved(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType_Chronology2
    public void testConstructor_long_PeriodType_Chronology2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType_Chronology3
    public void testConstructor_long_PeriodType_Chronology3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.standard(), (Chronology) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType_Chronology4
    public void testConstructor_long_PeriodType_Chronology4() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, (PeriodType) null, (Chronology) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_4int1
    public void testConstructor_4int1() throws Throwable {
        MutablePeriod test = new MutablePeriod(5, 6, 7, 8);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_8int1
    public void testConstructor_8int1() throws Throwable {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_8int__PeriodType1
    public void testConstructor_8int__PeriodType1() throws Throwable {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8, null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_8int__PeriodType2
    public void testConstructor_8int__PeriodType2() throws Throwable {
        MutablePeriod test = new MutablePeriod(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.dayTime());
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_8int__PeriodType3
    public void testConstructor_8int__PeriodType3() throws Throwable {
        try {
            new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.dayTime());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long1
    public void testConstructor_long_long1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long2
    public void testConstructor_long_long2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_PeriodType1
    public void testConstructor_long_long_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), (PeriodType) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_PeriodType2
    public void testConstructor_long_long_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), PeriodType.dayTime());
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(31, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_PeriodType3
    public void testConstructor_long_long_PeriodType3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 6, 9, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), PeriodType.standard().withMillisRemoved());
        assertEquals(PeriodType.standard().withMillisRemoved(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_Chronology1
    public void testConstructor_long_long_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, CopticChronology.getInstance());
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, CopticChronology.getInstance());
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), CopticChronology.getInstance());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_Chronology2
    public void testConstructor_long_long_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), (Chronology) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_PeriodType_Chronology1
    public void testConstructor_long_long_PeriodType_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, CopticChronology.getInstance());
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, CopticChronology.getInstance());
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), (PeriodType) null, CopticChronology.getInstance());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_PeriodType_Chronology2
    public void testConstructor_long_long_PeriodType_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), (PeriodType) null, null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI1
    public void testConstructor_RI_RI1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI2
    public void testConstructor_RI_RI2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI3
    public void testConstructor_RI_RI3() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(3, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI4
    public void testConstructor_RI_RI4() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        DateTime dt2 = null;  
        MutablePeriod test = new MutablePeriod(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(-3, test.getYears());
        assertEquals(-1, test.getMonths());
        assertEquals(-1, test.getWeeks());
        assertEquals(-1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(-1, test.getMinutes());
        assertEquals(-1, test.getSeconds());
        assertEquals(-1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI5
    public void testConstructor_RI_RI5() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = null;  
        MutablePeriod test = new MutablePeriod(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI_PeriodType1
    public void testConstructor_RI_RI_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2, null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI_PeriodType2
    public void testConstructor_RI_RI_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2, PeriodType.dayTime());
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(31, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI_PeriodType3
    public void testConstructor_RI_RI_PeriodType3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 6, 9, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2, PeriodType.standard().withMillisRemoved());
        assertEquals(PeriodType.standard().withMillisRemoved(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI_PeriodType4
    public void testConstructor_RI_RI_PeriodType4() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2, PeriodType.standard());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(3, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI_PeriodType5
    public void testConstructor_RI_RI_PeriodType5() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = null;  
        MutablePeriod test = new MutablePeriod(dt1, dt2, PeriodType.standard());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RD1
    public void testConstructor_RI_RD1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        MutablePeriod test = new MutablePeriod(dt1, dur);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RD2
    public void testConstructor_RI_RD2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        MutablePeriod test = new MutablePeriod(dt1, dur);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RD_PeriodType1
    public void testConstructor_RI_RD_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        MutablePeriod test = new MutablePeriod(dt1, dur, PeriodType.yearDayTime());
        assertEquals(PeriodType.yearDayTime(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(31, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RD_PeriodType2
    public void testConstructor_RI_RD_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        MutablePeriod test = new MutablePeriod(dt1, dur, (PeriodType) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        MutablePeriod test = new MutablePeriod("P1Y2M3D");
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(3, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        MutablePeriod test = new MutablePeriod((Object) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object3
    public void testConstructor_Object3() throws Throwable {
        MutablePeriod test = new MutablePeriod(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()));
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(2, test.getMinutes());
        assertEquals(3, test.getSeconds());
        assertEquals(4, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object4
    public void testConstructor_Object4() throws Throwable {
        Period base = new Period(1, 1, 0, 1, 1, 1, 1, 1, PeriodType.standard());
        MutablePeriod test = new MutablePeriod(base);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_PeriodType1
    public void testConstructor_Object_PeriodType1() throws Throwable {
        MutablePeriod test = new MutablePeriod("P1Y2M3D", PeriodType.yearMonthDayTime());
        assertEquals(PeriodType.yearMonthDayTime(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(3, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_PeriodType2
    public void testConstructor_Object_PeriodType2() throws Throwable {
        MutablePeriod test = new MutablePeriod((Object) null, PeriodType.yearMonthDayTime());
        assertEquals(PeriodType.yearMonthDayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_PeriodType3
    public void testConstructor_Object_PeriodType3() throws Throwable {
        MutablePeriod test = new MutablePeriod(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()), PeriodType.yearMonthDayTime());
        assertEquals(PeriodType.yearMonthDayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(2, test.getMinutes());
        assertEquals(3, test.getSeconds());
        assertEquals(4, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_PeriodType4
    public void testConstructor_Object_PeriodType4() throws Throwable {
        MutablePeriod test = new MutablePeriod(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()), (PeriodType) null);
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(2, test.getMinutes());
        assertEquals(3, test.getSeconds());
        assertEquals(4, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_Chronology1
    public void testConstructor_Object_Chronology1() throws Throwable {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        MutablePeriod test = new MutablePeriod(new Duration(length), ISOChronology.getInstance());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_Chronology2
    public void testConstructor_Object_Chronology2() throws Throwable {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        MutablePeriod test = new MutablePeriod(new Duration(length), ISOChronology.getInstanceUTC());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(64, test.getWeeks());
        assertEquals(2, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutablePeriod_Updates::testClear
    public void testClear() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.clear();
        assertEquals(new MutablePeriod(), test);
        
        test = new MutablePeriod(1, 2, 0, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        test.clear();
        assertEquals(new MutablePeriod(PeriodType.yearMonthDayTime()), test);
    }

// org.joda.time.TestMutablePeriod_Updates::testAddYears
    public void testAddYears() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addYears(10);
        assertEquals(11, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addYears(-10);
        assertEquals(-9, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addYears(0);
        assertEquals(1, test.getYears());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddMonths
    public void testAddMonths() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMonths(10);
        assertEquals(12, test.getMonths());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMonths(-10);
        assertEquals(-8, test.getMonths());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMonths(0);
        assertEquals(2, test.getMonths());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddWeeks
    public void testAddWeeks() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addWeeks(10);
        assertEquals(13, test.getWeeks());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addWeeks(-10);
        assertEquals(-7, test.getWeeks());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addWeeks(0);
        assertEquals(3, test.getWeeks());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddDays
    public void testAddDays() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addDays(10);
        assertEquals(14, test.getDays());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addDays(-10);
        assertEquals(-6, test.getDays());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addDays(0);
        assertEquals(4, test.getDays());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddHours
    public void testAddHours() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addHours(10);
        assertEquals(15, test.getHours());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addHours(-10);
        assertEquals(-5, test.getHours());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addHours(0);
        assertEquals(5, test.getHours());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddMinutes
    public void testAddMinutes() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMinutes(10);
        assertEquals(16, test.getMinutes());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMinutes(-10);
        assertEquals(-4, test.getMinutes());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMinutes(0);
        assertEquals(6, test.getMinutes());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddSeconds
    public void testAddSeconds() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addSeconds(10);
        assertEquals(17, test.getSeconds());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addSeconds(-10);
        assertEquals(-3, test.getSeconds());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addSeconds(0);
        assertEquals(7, test.getSeconds());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddMillis
    public void testAddMillis() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMillis(10);
        assertEquals(18, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMillis(-10);
        assertEquals(-2, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMillis(0);
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetYears
    public void testSetYears() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setYears(10);
        assertEquals(10, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setYears(-10);
        assertEquals(-10, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setYears(0);
        assertEquals(0, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setYears(1);
        assertEquals(1, test.getYears());
        
        test = new MutablePeriod(0, 0, 0, 0, 0, 0, 0, 1, PeriodType.millis());
        try {
            test.setYears(1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutablePeriod_Updates::testSetMonths
    public void testSetMonths() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMonths(10);
        assertEquals(10, test.getMonths());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMonths(-10);
        assertEquals(-10, test.getMonths());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMonths(0);
        assertEquals(0, test.getMonths());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMonths(2);
        assertEquals(2, test.getMonths());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetWeeks
    public void testSetWeeks() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setWeeks(10);
        assertEquals(10, test.getWeeks());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setWeeks(-10);
        assertEquals(-10, test.getWeeks());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setWeeks(0);
        assertEquals(0, test.getWeeks());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setWeeks(3);
        assertEquals(3, test.getWeeks());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetDays
    public void testSetDays() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setDays(10);
        assertEquals(10, test.getDays());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setDays(-10);
        assertEquals(-10, test.getDays());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setDays(0);
        assertEquals(0, test.getDays());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setDays(4);
        assertEquals(4, test.getDays());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetHours
    public void testSetHours() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setHours(10);
        assertEquals(10, test.getHours());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setHours(-10);
        assertEquals(-10, test.getHours());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setHours(0);
        assertEquals(0, test.getHours());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setHours(5);
        assertEquals(5, test.getHours());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetMinutes
    public void testSetMinutes() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMinutes(10);
        assertEquals(10, test.getMinutes());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMinutes(-10);
        assertEquals(-10, test.getMinutes());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMinutes(0);
        assertEquals(0, test.getMinutes());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMinutes(6);
        assertEquals(6, test.getMinutes());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetSeconds
    public void testSetSeconds() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setSeconds(10);
        assertEquals(10, test.getSeconds());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setSeconds(-10);
        assertEquals(-10, test.getSeconds());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setSeconds(0);
        assertEquals(0, test.getSeconds());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setSeconds(7);
        assertEquals(7, test.getSeconds());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetMillis
    public void testSetMillis() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMillis(10);
        assertEquals(10, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMillis(-10);
        assertEquals(-10, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMillis(0);
        assertEquals(0, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMillis(8);
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSet_Field
    public void testSet_Field() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.set(DurationFieldType.years(), 10);
        assertEquals(10, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test.set(null, 10);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_Field
    public void testAdd_Field() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.add(DurationFieldType.years(), 10);
        assertEquals(11, test.getYears());
        
        test = new MutablePeriod(0, 0, 0, 0, 0, 0, 0, 1, PeriodType.millis());
        test.add(DurationFieldType.years(), 0);
        assertEquals(0, test.getYears());
        assertEquals(1, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test.add(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test.add(null, 10);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_8ints1
    public void testSetPeriod_8ints1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod(11, 12, 13, 14, 15, 16, 17, 18);
        assertEquals(11, test.getYears());
        assertEquals(12, test.getMonths());
        assertEquals(13, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_8ints2
    public void testSetPeriod_8ints2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        try {
            test.setPeriod(11, 12, 13, 14, 15, 16, 17, 18);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_8ints3
    public void testSetPeriod_8ints3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        test.setPeriod(0, 0, 0, 0, 0, 0, 0, 18);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_8ints4
    public void testSetPeriod_8ints4() {
        MutablePeriod test = new MutablePeriod(0, 0, 0, 0, 5, 6, 7, 8);
        test.setPeriod(11, 12, 13, 14, 15, 16, 17, 18);
        assertEquals(11, test.getYears());
        assertEquals(12, test.getMonths());
        assertEquals(13, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RP1
    public void testSetPeriod_RP1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod(new MutablePeriod(11, 12, 13, 14, 15, 16, 17, 18));
        assertEquals(11, test.getYears());
        assertEquals(12, test.getMonths());
        assertEquals(13, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RP2
    public void testSetPeriod_RP2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        try {
            test.setPeriod(new MutablePeriod(11, 12, 13, 14, 15, 16, 17, 18));
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RP3
    public void testSetPeriod_RP3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        test.setPeriod(new MutablePeriod(0, 0, 0, 0, 0, 0, 0, 18));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RP4
    public void testSetPeriod_RP4() {
        MutablePeriod test = new MutablePeriod(0, 0, 0, 0, 5, 6, 7, 8);
        test.setPeriod(new MutablePeriod(11, 12, 13, 14, 15, 16, 17, 18));
        assertEquals(11, test.getYears());
        assertEquals(12, test.getMonths());
        assertEquals(13, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RP5
    public void testSetPeriod_RP5() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod((ReadablePeriod) null);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long1
    public void testSetPeriod_long_long1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long2
    public void testSetPeriod_long_long2() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt2.getMillis(), dt1.getMillis());
        assertEquals(-1, test.getYears());
        assertEquals(-1, test.getMonths());
        assertEquals(-1, test.getWeeks());
        assertEquals(-1, test.getDays());
        assertEquals(-1, test.getHours());
        assertEquals(-1, test.getMinutes());
        assertEquals(-1, test.getSeconds());
        assertEquals(-1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long3
    public void testSetPeriod_long_long3() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        test.setPeriod(dt1.getMillis(), dt1.getMillis());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoYears
    public void testSetPeriod_long_long_NoYears() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withYearsRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(0, test.getYears());
        assertEquals(13, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoMonths
    public void testSetPeriod_long_long_NoMonths() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withMonthsRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(5, test.getWeeks());
        assertEquals(3, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoWeeks
    public void testSetPeriod_long_long_NoWeeks() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withWeeksRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(8, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoDays
    public void testSetPeriod_long_long_NoDays() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withDaysRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(25, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoHours
    public void testSetPeriod_long_long_NoHours() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withHoursRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(61, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoMinutes
    public void testSetPeriod_long_long_NoMinutes() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withMinutesRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(61, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoSeconds
    public void testSetPeriod_long_long_NoSeconds() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withSecondsRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(1001, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoMillis
    public void testSetPeriod_long_long_NoMillis() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withMillisRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RI_RI1
    public void testSetPeriod_RI_RI1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1, dt2);
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RI_RI2
    public void testSetPeriod_RI_RI2() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt2, dt1);
        assertEquals(-1, test.getYears());
        assertEquals(-1, test.getMonths());
        assertEquals(-1, test.getWeeks());
        assertEquals(-1, test.getDays());
        assertEquals(-1, test.getHours());
        assertEquals(-1, test.getMinutes());
        assertEquals(-1, test.getSeconds());
        assertEquals(-1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RI_RI3
    public void testSetPeriod_RI_RI3() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        test.setPeriod(dt1, dt1);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RInterval1
    public void testSetPeriod_RInterval1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(new Interval(dt1, dt2));
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RInterval2
    public void testSetPeriod_RInterval2() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod((ReadableInterval) null);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long1
    public void testSetPeriod_long1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod(100L);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long2
    public void testSetPeriod_long2() {
        MutablePeriod test = new MutablePeriod();
        test.setPeriod(
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L);
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RD1
    public void testSetPeriod_RD1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod(new Duration(100L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RD2
    public void testSetPeriod_RD2() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        test.setPeriod(new Duration(length));
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RD3
    public void testSetPeriod_RD3() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod((ReadableDuration) null);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_8ints1
    public void testAdd_8ints1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_8ints2
    public void testAdd_8ints2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.yearMonthDayTime());
        try {
            test.add(1, 2, 3, 4, 5, 6, 7, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long1
    public void testAdd_long1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(100L);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(200, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long2
    public void testAdd_long2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.standard());
        long ms =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        test.add(ms);
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long3
    public void testAdd_long3() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.add(2100L);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(9, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long_Chronology1
    public void testAdd_long_Chronology1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(100L, ISOChronology.getInstance());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(200, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long_Chronology2
    public void testAdd_long_Chronology2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.standard());
        long ms =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        test.add(ms, ISOChronology.getInstance());
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long_Chronology3
    public void testAdd_long_Chronology3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.standard());
        long ms =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        test.add(ms, ISOChronology.getInstanceUTC());
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(64, test.getWeeks());
        assertEquals(2, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RD1
    public void testAdd_RD1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(new Duration(100L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(200, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RD2
    public void testAdd_RD2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.yearMonthDayTime());
        long ms =
            (4L + (3L * 7L)) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        test.add(new Duration(ms));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 + (3 * 7)) * 24 + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RD3
    public void testAdd_RD3() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.add((ReadableDuration) null);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP1
    public void testAdd_RP1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(new Period(100L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(200, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP2
    public void testAdd_RP2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.standard());  
        test.add(new Period(1, 2, 3, 4, 5, 6, 7, 0, PeriodType.standard().withMillisRemoved()));
        
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP3
    public void testAdd_RP3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.standard());
        test.add(new Period(0L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP4
    public void testAdd_RP4() {
        MutablePeriod test = new MutablePeriod(1, 2, 0, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        try {
            test.add(new Period(1, 2, 3, 4, 5, 6, 7, 8));  
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP5
    public void testAdd_RP5() {
        MutablePeriod test = new MutablePeriod(1, 2, 0, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        test.add(new Period(1, 2, 0, 4, 5, 6, 7, 8));  
        assertEquals(2, test.getYears());
        assertEquals(4, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(8, test.getDays());
        assertEquals(10, test.getHours());
        assertEquals(12, test.getMinutes());
        assertEquals(14, test.getSeconds());
        assertEquals(16, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP6
    public void testAdd_RP6() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.add((ReadablePeriod) null);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RInterval1
    public void testAdd_RInterval1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(new Interval(100L, 200L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(200, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RInterval2
    public void testAdd_RInterval2() {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 12, 18, 0, 0, 0, 8);
        MutablePeriod test = new MutablePeriod(100L);  
        test.add(new Interval(dt1, dt2));
        assertEquals(1, test.getYears());  
        assertEquals(6, test.getMonths());  
        assertEquals(1, test.getWeeks());  
        assertEquals(2, test.getDays());  
        assertEquals(0, test.getHours());  
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RInterval3
    public void testAdd_RInterval3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.yearMonthDayTime());
        test.add(new Interval(0L, 0L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RInterval4
    public void testAdd_RInterval4() {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 17, 0, 0, 0, 8);
        MutablePeriod test = new MutablePeriod(100L, PeriodType.yearMonthDayTime());
        test.add(new Interval(dt1, dt2));
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());  
        assertEquals(8, test.getDays());  
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RInterval5
    public void testAdd_RInterval5() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.add((ReadableInterval) null);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testMergePeriod_RP1
    public void testMergePeriod_RP1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.mergePeriod(new MutablePeriod(0, 0, 0, 14, 15, 16, 17, 18, PeriodType.dayTime()));
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testMergePeriod_RP2
    public void testMergePeriod_RP2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        try {
            test.mergePeriod(new MutablePeriod(11, 12, 13, 14, 15, 16, 17, 18));
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testMergePeriod_RP3
    public void testMergePeriod_RP3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        test.mergePeriod(new MutablePeriod(0, 0, 0, 0, 0, 0, 0, 18));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testMergePeriod_RP4
    public void testMergePeriod_RP4() {
        MutablePeriod test = new MutablePeriod(0, 0, 0, 0, 5, 6, 7, 8);
        test.mergePeriod(new MutablePeriod(11, 12, 13, 14, 15, 16, 17, 18));
        assertEquals(11, test.getYears());
        assertEquals(12, test.getMonths());
        assertEquals(13, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testMergePeriod_RP5
    public void testMergePeriod_RP5() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.mergePeriod((ReadablePeriod) null);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPartial_Basics::testGet
    public void testGet() {
        Partial test = createHourMinPartial();
        assertEquals(10, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, test.get(DateTimeFieldType.minuteOfHour()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.secondOfMinute());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Basics::testSize
    public void testSize() {
        Partial test = createHourMinPartial();
        assertEquals(2, test.size());
    }

// org.joda.time.TestPartial_Basics::testGetFieldType
    public void testGetFieldType() {
        Partial test = createHourMinPartial();
        assertSame(DateTimeFieldType.hourOfDay(), test.getFieldType(0));
        assertSame(DateTimeFieldType.minuteOfHour(), test.getFieldType(1));
        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestPartial_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        Partial test = createHourMinPartial();
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertEquals(2, fields.length);
        assertSame(DateTimeFieldType.hourOfDay(), fields[0]);
        assertSame(DateTimeFieldType.minuteOfHour(), fields[1]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestPartial_Basics::testGetField
    public void testGetField() {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        assertSame(CopticChronology.getInstanceUTC().hourOfDay(), test.getField(0));
        assertSame(CopticChronology.getInstanceUTC().minuteOfHour(), test.getField(1));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(5);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestPartial_Basics::testGetFields
    public void testGetFields() {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertEquals(2, fields.length);
        assertSame(CopticChronology.getInstanceUTC().hourOfDay(), fields[0]);
        assertSame(CopticChronology.getInstanceUTC().minuteOfHour(), fields[1]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestPartial_Basics::testGetValue
    public void testGetValue() {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        assertEquals(10, test.getValue(0));
        assertEquals(20, test.getValue(1));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestPartial_Basics::testGetValues
    public void testGetValues() {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        int[] values = test.getValues();
        assertEquals(2, values.length);
        assertEquals(10, values[0]);
        assertEquals(20, values[1]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestPartial_Basics::testIsSupported
    public void testIsSupported() {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(false, test.isSupported(DateTimeFieldType.secondOfMinute()));
        assertEquals(false, test.isSupported(DateTimeFieldType.millisOfSecond()));
        assertEquals(false, test.isSupported(DateTimeFieldType.dayOfMonth()));
    }

// org.joda.time.TestPartial_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        Partial test1 = createHourMinPartial(COPTIC_PARIS);
        Partial test2 = createHourMinPartial(COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        Partial test3 = createHourMinPartial2(COPTIC_PARIS);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
        assertEquals(new TimeOfDay(10, 20, 30, 40), createTODPartial(ISO_UTC));
    }

// org.joda.time.TestPartial_Basics::testCompareTo
    public void testCompareTo() {
        Partial test1 = createHourMinPartial();
        Partial test1a = createHourMinPartial();
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        Partial test2 = createHourMinPartial2(ISO_UTC);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        Partial test3 = createHourMinPartial2(COPTIC_UTC);
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        assertEquals(0, new TimeOfDay(10, 20, 30, 40).compareTo(createTODPartial(ISO_UTC)));
        
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

        try {
            test1.compareTo(new YearMonthDay());
            fail();
        } catch (ClassCastException ex) {}
        try {
            createTODPartial(ISO_UTC).without(DateTimeFieldType.hourOfDay()).compareTo(new YearMonthDay());
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestPartial_Basics::testIsEqual_TOD
    public void testIsEqual_TOD() {
        Partial test1 = createHourMinPartial();
        Partial test1a = createHourMinPartial();
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        Partial test2 = createHourMinPartial2(ISO_UTC);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        Partial test3 = createHourMinPartial2(COPTIC_UTC);
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            createHourMinPartial().isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Basics::testIsBefore_TOD
    public void testIsBefore_TOD() {
        Partial test1 = createHourMinPartial();
        Partial test1a = createHourMinPartial();
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        Partial test2 = createHourMinPartial2(ISO_UTC);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        Partial test3 = createHourMinPartial2(COPTIC_UTC);
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            createHourMinPartial().isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Basics::testIsAfter_TOD
    public void testIsAfter_TOD() {
        Partial test1 = createHourMinPartial();
        Partial test1a = createHourMinPartial();
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        Partial test2 = createHourMinPartial2(ISO_UTC);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        Partial test3 = createHourMinPartial2(COPTIC_UTC);
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            createHourMinPartial().isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Basics::testWithChronologyRetainFields_Chrono
    public void testWithChronologyRetainFields_Chrono() {
        Partial base = createHourMinPartial(COPTIC_PARIS);
        Partial test = base.withChronologyRetainFields(BUDDHIST_TOKYO);
        check(base, 10, 20);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 10, 20);
        assertEquals(BUDDHIST_UTC, test.getChronology());
    }

// org.joda.time.TestPartial_Basics::testWithChronologyRetainFields_sameChrono
    public void testWithChronologyRetainFields_sameChrono() {
        Partial base = createHourMinPartial(COPTIC_PARIS);
        Partial test = base.withChronologyRetainFields(COPTIC_TOKYO);
        assertSame(base, test);
    }

// org.joda.time.TestPartial_Basics::testWithChronologyRetainFields_nullChrono
    public void testWithChronologyRetainFields_nullChrono() {
        Partial base = createHourMinPartial(COPTIC_PARIS);
        Partial test = base.withChronologyRetainFields(null);
        check(base, 10, 20);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 10, 20);
        assertEquals(ISO_UTC, test.getChronology());
    }
