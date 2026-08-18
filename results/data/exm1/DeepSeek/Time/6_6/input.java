// buggy code
    public static synchronized GJChronology getInstance(
            DateTimeZone zone,
            ReadableInstant gregorianCutover,
            int minDaysInFirstWeek) {
        
        zone = DateTimeUtils.getZone(zone);
        Instant cutoverInstant;
        if (gregorianCutover == null) {
            cutoverInstant = DEFAULT_CUTOVER;
        } else {
            cutoverInstant = gregorianCutover.toInstant();
        }

        GJChronology chrono;
        synchronized (cCache) {
            ArrayList<GJChronology> chronos = cCache.get(zone);
            if (chronos == null) {
                chronos = new ArrayList<GJChronology>(2);
                cCache.put(zone, chronos);
            } else {
                for (int i = chronos.size(); --i >= 0;) {
                    chrono = chronos.get(i);
                    if (minDaysInFirstWeek == chrono.getMinimumDaysInFirstWeek() &&
                        cutoverInstant.equals(chrono.getGregorianCutover())) {
                        
                        return chrono;
                    }
                }
            }
            if (zone == DateTimeZone.UTC) {
                chrono = new GJChronology
                    (JulianChronology.getInstance(zone, minDaysInFirstWeek),
                     GregorianChronology.getInstance(zone, minDaysInFirstWeek),
                     cutoverInstant);
            } else {
                chrono = getInstance(DateTimeZone.UTC, cutoverInstant, minDaysInFirstWeek);
                chrono = new GJChronology
                    (ZonedChronology.getInstance(chrono, zone),
                     chrono.iJulianChronology,
                     chrono.iGregorianChronology,
                     chrono.iCutoverInstant);
            }
            chronos.add(chrono);
        }
        return chrono;
    }

        public long add(long instant, int value) {
            if (instant >= iCutover) {
                instant = iGregorianField.add(instant, value);
                if (instant < iCutover) {
                    // Only adjust if gap fully crossed.
                    if (instant + iGapDuration < iCutover) {
                        instant = gregorianToJulian(instant);
                    }
                }
            } else {
                instant = iJulianField.add(instant, value);
                if (instant >= iCutover) {
                    // Only adjust if gap fully crossed.
                    if (instant - iGapDuration >= iCutover) {
                        // no special handling for year zero as cutover always after year zero
                        instant = julianToGregorian(instant);
                    }
                }
            }
            return instant;
        }

        public long add(long instant, long value) {
            if (instant >= iCutover) {
                instant = iGregorianField.add(instant, value);
                if (instant < iCutover) {
                    // Only adjust if gap fully crossed.
                    if (instant + iGapDuration < iCutover) {
                        instant = gregorianToJulian(instant);
                    }
                }
            } else {
                instant = iJulianField.add(instant, value);
                if (instant >= iCutover) {
                    // Only adjust if gap fully crossed.
                    if (instant - iGapDuration >= iCutover) {
                        // no special handling for year zero as cutover always after year zero
                        instant = julianToGregorian(instant);
                    }
                }
            }
            return instant;
        }

// relevant test
// org.joda.time.TestTimeOfDay_Basics::testGetFields
    public void testGetFields() {
        TimeOfDay test = new TimeOfDay(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertSame(CopticChronology.getInstanceUTC().hourOfDay(), fields[0]);
        assertSame(CopticChronology.getInstanceUTC().minuteOfHour(), fields[1]);
        assertSame(CopticChronology.getInstanceUTC().secondOfMinute(), fields[2]);
        assertSame(CopticChronology.getInstanceUTC().millisOfSecond(), fields[3]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestTimeOfDay_Basics::testGetValue
    public void testGetValue() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        assertEquals(10, test.getValue(0));
        assertEquals(20, test.getValue(1));
        assertEquals(30, test.getValue(2));
        assertEquals(40, test.getValue(3));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(5);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testGetValues
    public void testGetValues() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        int[] values = test.getValues();
        assertEquals(10, values[0]);
        assertEquals(20, values[1]);
        assertEquals(30, values[2]);
        assertEquals(40, values[3]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestTimeOfDay_Basics::testIsSupported
    public void testIsSupported() {
        TimeOfDay test = new TimeOfDay(COPTIC_PARIS);
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfMinute()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfSecond()));
        assertEquals(false, test.isSupported(DateTimeFieldType.dayOfMonth()));
    }

// org.joda.time.TestTimeOfDay_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        TimeOfDay test1 = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        TimeOfDay test2 = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        TimeOfDay test3 = new TimeOfDay(15, 20, 30, 40);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestTimeOfDay_Basics::testCompareTo
    public void testCompareTo() {
        TimeOfDay test1 = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay test1a = new TimeOfDay(10, 20, 30, 40);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        TimeOfDay test2 = new TimeOfDay(10, 20, 35, 40);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        TimeOfDay test3 = new TimeOfDay(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.hourOfDay(),
            DateTimeFieldType.minuteOfHour(),
            DateTimeFieldType.secondOfMinute(),
            DateTimeFieldType.millisOfSecond(),
        };
        int[] values = new int[] {10, 20, 30, 40};
        Partial p = new Partial(types, values);
        assertEquals(0, test1.compareTo(p));
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

    }

// org.joda.time.TestTimeOfDay_Basics::testIsEqual_TOD
    public void testIsEqual_TOD() {
        TimeOfDay test1 = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay test1a = new TimeOfDay(10, 20, 30, 40);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        TimeOfDay test2 = new TimeOfDay(10, 20, 35, 40);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        TimeOfDay test3 = new TimeOfDay(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new TimeOfDay(10, 20, 35, 40).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testIsBefore_TOD
    public void testIsBefore_TOD() {
        TimeOfDay test1 = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay test1a = new TimeOfDay(10, 20, 30, 40);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        TimeOfDay test2 = new TimeOfDay(10, 20, 35, 40);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        TimeOfDay test3 = new TimeOfDay(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new TimeOfDay(10, 20, 35, 40).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testIsAfter_TOD
    public void testIsAfter_TOD() {
        TimeOfDay test1 = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay test1a = new TimeOfDay(10, 20, 30, 40);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        TimeOfDay test2 = new TimeOfDay(10, 20, 35, 40);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        TimeOfDay test3 = new TimeOfDay(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new TimeOfDay(10, 20, 35, 40).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithChronologyRetainFields_Chrono
    public void testWithChronologyRetainFields_Chrono() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        TimeOfDay test = base.withChronologyRetainFields(BUDDHIST_TOKYO);
        check(base, 10, 20, 30, 40);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 10, 20, 30, 40);
        assertEquals(BUDDHIST_UTC, test.getChronology());
    }

// org.joda.time.TestTimeOfDay_Basics::testWithChronologyRetainFields_sameChrono
    public void testWithChronologyRetainFields_sameChrono() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        TimeOfDay test = base.withChronologyRetainFields(COPTIC_TOKYO);
        assertSame(base, test);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithChronologyRetainFields_nullChrono
    public void testWithChronologyRetainFields_nullChrono() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        TimeOfDay test = base.withChronologyRetainFields(null);
        check(base, 10, 20, 30, 40);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 10, 20, 30, 40);
        assertEquals(ISO_UTC, test.getChronology());
    }

// org.joda.time.TestTimeOfDay_Basics::testWithField1
    public void testWithField1() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay result = test.withField(DateTimeFieldType.hourOfDay(), 15);
        
        assertEquals(new TimeOfDay(10, 20, 30, 40), test);
        assertEquals(new TimeOfDay(15, 20, 30, 40), result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithField2
    public void testWithField2() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithField3
    public void testWithField3() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        try {
            test.withField(DateTimeFieldType.dayOfMonth(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithField4
    public void testWithField4() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay result = test.withField(DateTimeFieldType.hourOfDay(), 10);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded1
    public void testWithFieldAdded1() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay result = test.withFieldAdded(DurationFieldType.hours(), 6);
        
        assertEquals(new TimeOfDay(10, 20, 30, 40), test);
        assertEquals(new TimeOfDay(16, 20, 30, 40), result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded2
    public void testWithFieldAdded2() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded3
    public void testWithFieldAdded3() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded4
    public void testWithFieldAdded4() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay result = test.withFieldAdded(DurationFieldType.hours(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded5
    public void testWithFieldAdded5() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        try {
            test.withFieldAdded(DurationFieldType.days(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded6
    public void testWithFieldAdded6() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay result = test.withFieldAdded(DurationFieldType.hours(), 16);
        
        assertEquals(new TimeOfDay(10, 20, 30, 40), test);
        assertEquals(new TimeOfDay(2, 20, 30, 40), result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded7
    public void testWithFieldAdded7() {
        TimeOfDay test = new TimeOfDay(23, 59, 59, 999);
        TimeOfDay result = test.withFieldAdded(DurationFieldType.millis(), 1);
        assertEquals(new TimeOfDay(0, 0, 0, 0), result);
        
        test = new TimeOfDay(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.seconds(), 1);
        assertEquals(new TimeOfDay(0, 0, 0, 999), result);
        
        test = new TimeOfDay(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.minutes(), 1);
        assertEquals(new TimeOfDay(0, 0, 59, 999), result);
        
        test = new TimeOfDay(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.hours(), 1);
        assertEquals(new TimeOfDay(0, 59, 59, 999), result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded8
    public void testWithFieldAdded8() {
        TimeOfDay test = new TimeOfDay(0, 0, 0, 0);
        TimeOfDay result = test.withFieldAdded(DurationFieldType.millis(), -1);
        assertEquals(new TimeOfDay(23, 59, 59, 999), result);
        
        test = new TimeOfDay(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.seconds(), -1);
        assertEquals(new TimeOfDay(23, 59, 59, 0), result);
        
        test = new TimeOfDay(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.minutes(), -1);
        assertEquals(new TimeOfDay(23, 59, 0, 0), result);
        
        test = new TimeOfDay(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.hours(), -1);
        assertEquals(new TimeOfDay(23, 0, 0, 0), result);
    }

// org.joda.time.TestTimeOfDay_Basics::testPlus_RP
    public void testPlus_RP() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, BuddhistChronology.getInstance());
        TimeOfDay result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        TimeOfDay expected = new TimeOfDay(15, 26, 37, 48, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testPlusHours_int
    public void testPlusHours_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.plusHours(1);
        TimeOfDay expected = new TimeOfDay(2, 2, 3, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testPlusMinutes_int
    public void testPlusMinutes_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.plusMinutes(1);
        TimeOfDay expected = new TimeOfDay(1, 3, 3, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testPlusSeconds_int
    public void testPlusSeconds_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.plusSeconds(1);
        TimeOfDay expected = new TimeOfDay(1, 2, 4, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testPlusMillis_int
    public void testPlusMillis_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.plusMillis(1);
        TimeOfDay expected = new TimeOfDay(1, 2, 3, 5, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testMinus_RP
    public void testMinus_RP() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, BuddhistChronology.getInstance());
        TimeOfDay result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        TimeOfDay expected = new TimeOfDay(9, 19, 29, 39, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testMinusHours_int
    public void testMinusHours_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.minusHours(1);
        TimeOfDay expected = new TimeOfDay(0, 2, 3, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testMinusMinutes_int
    public void testMinusMinutes_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.minusMinutes(1);
        TimeOfDay expected = new TimeOfDay(1, 1, 3, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testMinusSeconds_int
    public void testMinusSeconds_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.minusSeconds(1);
        TimeOfDay expected = new TimeOfDay(1, 2, 2, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testMinusMillis_int
    public void testMinusMillis_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.minusMillis(1);
        TimeOfDay expected = new TimeOfDay(1, 2, 3, 3, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testToLocalTime
    public void testToLocalTime() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_UTC);
        LocalTime test = base.toLocalTime();
        assertEquals(new LocalTime(10, 20, 30, 40, COPTIC_UTC), test);
    }

// org.joda.time.TestTimeOfDay_Basics::testToDateTimeToday
    public void testToDateTimeToday() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeToday();
        check(base, 10, 20, 30, 40);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_LONDON);
        expected = expected.hourOfDay().setCopy(10);
        expected = expected.minuteOfHour().setCopy(20);
        expected = expected.secondOfMinute().setCopy(30);
        expected = expected.millisOfSecond().setCopy(40);
        assertEquals(expected, test);
    }

// org.joda.time.TestTimeOfDay_Basics::testToDateTimeToday_Zone
    public void testToDateTimeToday_Zone() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeToday(TOKYO);
        check(base, 10, 20, 30, 40);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_TOKYO);
        expected = expected.hourOfDay().setCopy(10);
        expected = expected.minuteOfHour().setCopy(20);
        expected = expected.secondOfMinute().setCopy(30);
        expected = expected.millisOfSecond().setCopy(40);
        assertEquals(expected, test);
    }

// org.joda.time.TestTimeOfDay_Basics::testToDateTimeToday_nullZone
    public void testToDateTimeToday_nullZone() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeToday((DateTimeZone) null);
        check(base, 10, 20, 30, 40);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_LONDON);
        expected = expected.hourOfDay().setCopy(10);
        expected = expected.minuteOfHour().setCopy(20);
        expected = expected.secondOfMinute().setCopy(30);
        expected = expected.millisOfSecond().setCopy(40);
        assertEquals(expected, test);
    }

// org.joda.time.TestTimeOfDay_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        DateTime dt = new DateTime(0L); 
        assertEquals("1970-01-01T01:00:00.000+01:00", dt.toString());
        
        DateTime test = base.toDateTime(dt);
        check(base, 10, 20, 30, 40);
        assertEquals("1970-01-01T01:00:00.000+01:00", dt.toString());
        assertEquals("1970-01-01T10:20:30.040+01:00", test.toString());
    }

// org.joda.time.TestTimeOfDay_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        TimeOfDay base = new TimeOfDay(1, 2, 3, 4);
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2);
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 1, 2, 3, 4);
        assertEquals("1970-01-02T01:02:03.004+01:00", test.toString());
    }

// org.joda.time.TestTimeOfDay_Basics::testWithers
    public void testWithers() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        check(test.withHourOfDay(6), 6, 20, 30, 40);
        check(test.withMinuteOfHour(6), 10, 6, 30, 40);
        check(test.withSecondOfMinute(6), 10, 20, 6, 40);
        check(test.withMillisOfSecond(6), 10, 20, 30, 6);
        try {
            test.withHourOfDay(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withHourOfDay(24);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testProperty
    public void testProperty() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals(test.hourOfDay(), test.property(DateTimeFieldType.hourOfDay()));
        assertEquals(test.minuteOfHour(), test.property(DateTimeFieldType.minuteOfHour()));
        assertEquals(test.secondOfMinute(), test.property(DateTimeFieldType.secondOfMinute()));
        assertEquals(test.millisOfSecond(), test.property(DateTimeFieldType.millisOfSecond()));
        try {
            test.property(DateTimeFieldType.millisOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testSerialization
    public void testSerialization() throws Exception {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        TimeOfDay result = (TimeOfDay) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestTimeOfDay_Basics::testToString
    public void testToString() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals("T10:20:30.040", test.toString());
    }

// org.joda.time.TestTimeOfDay_Basics::testToString_String
    public void testToString_String() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString("yyyy HH"));
        assertEquals("T10:20:30.040", test.toString((String) null));
    }

// org.joda.time.TestTimeOfDay_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals("10 20", test.toString("H m", Locale.ENGLISH));
        assertEquals("T10:20:30.040", test.toString(null, Locale.ENGLISH));
        assertEquals("10 20", test.toString("H m", null));
        assertEquals("T10:20:30.040", test.toString(null, null));
    }

// org.joda.time.TestTimeOfDay_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("T10:20:30.040", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstantMidnight
    public void testConstantMidnight() throws Throwable {
        TimeOfDay test = TimeOfDay.MIDNIGHT;
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testFactory_FromCalendarFields
    public void testFactory_FromCalendarFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        TimeOfDay expected = new TimeOfDay(4, 5, 6, 7);
        assertEquals(expected, TimeOfDay.fromCalendarFields(cal));
        try {
            TimeOfDay.fromCalendarFields(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testFactory_FromDateFields_after1970
    public void testFactory_FromDateFields_after1970() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        TimeOfDay expected = new TimeOfDay(4, 5, 6, 7);
        assertEquals(expected, TimeOfDay.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestTimeOfDay_Constructors::testFactory_FromDateFields_before1970
    public void testFactory_FromDateFields_before1970() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1969, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        TimeOfDay expected = new TimeOfDay(4, 5, 6, 7);
        assertEquals(expected, TimeOfDay.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestTimeOfDay_Constructors::testFactory_FromDateFields_null
    public void testFactory_FromDateFields_null() throws Exception {
        try {
            TimeOfDay.fromDateFields(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testFactoryMillisOfDay_long1
    public void testFactoryMillisOfDay_long1() throws Throwable {
        TimeOfDay test = TimeOfDay.fromMillisOfDay(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testFactoryMillisOfDay_long1_Chronology
    public void testFactoryMillisOfDay_long1_Chronology() throws Throwable {
        TimeOfDay test = TimeOfDay.fromMillisOfDay(TEST_TIME1, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(1, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testFactoryMillisOfDay_long_nullChronology
    public void testFactoryMillisOfDay_long_nullChronology() throws Throwable {
        TimeOfDay test = TimeOfDay.fromMillisOfDay(TEST_TIME1, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        TimeOfDay test = new TimeOfDay();
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 30, 40, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        TimeOfDay test = new TimeOfDay(LONDON);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(23, test.getHourOfDay());
        assertEquals(59, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        
        test = new TimeOfDay(PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(0, test.getHourOfDay());
        assertEquals(59, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 30, 40, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        TimeOfDay test = new TimeOfDay((DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(23, test.getHourOfDay());
        assertEquals(59, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10 + OFFSET, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay((Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        TimeOfDay test = new TimeOfDay(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        TimeOfDay test = new TimeOfDay(TEST_TIME2);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(5 + OFFSET, test.getHourOfDay());
        assertEquals(6, test.getMinuteOfHour());
        assertEquals(7, test.getSecondOfMinute());
        assertEquals(8, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(TEST_TIME1, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(TEST_TIME2, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(5 + OFFSET, test.getHourOfDay());
        assertEquals(6, test.getMinuteOfHour());
        assertEquals(7, test.getSecondOfMinute());
        assertEquals(8, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(TEST_TIME1, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        Date date = new Date(TEST_TIME1);
        TimeOfDay test = new TimeOfDay(date);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date(TEST_TIME1));
        TimeOfDay test = new TimeOfDay(cal);
        assertEquals(GJChronology.getInstanceUTC(), test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        TimeOfDay test = new TimeOfDay((Object) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_todObject
    public void testConstructor_todObject() throws Throwable {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, CopticChronology.getInstance(PARIS));
        TimeOfDay test = new TimeOfDay(base);
        assertEquals(CopticChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        TimeOfDay test = new TimeOfDay("10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString2
    public void testConstructor_ObjectString2() throws Throwable {
        TimeOfDay test = new TimeOfDay("10:20:30.040+04:00");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET - 4, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString3
    public void testConstructor_ObjectString3() throws Throwable {
        TimeOfDay test = new TimeOfDay("T10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString4
    public void testConstructor_ObjectString4() throws Throwable {
        TimeOfDay test = new TimeOfDay("T10:20:30.040+04:00");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET - 4, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString5
    public void testConstructor_ObjectString5() throws Throwable {
        TimeOfDay test = new TimeOfDay("10:20");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString6
    public void testConstructor_ObjectString6() throws Throwable {
        TimeOfDay test = new TimeOfDay("10");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new TimeOfDay("1970-04-06");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new TimeOfDay("1970-04-06T+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectStringEx3
    public void testConstructor_ObjectStringEx3() throws Throwable {
        try {
            new TimeOfDay("1970-04-06T10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectStringEx4
    public void testConstructor_ObjectStringEx4() throws Throwable {
        try {
            new TimeOfDay("1970-04-06T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        TimeOfDay test = new TimeOfDay(date, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor2_Object_Chronology
    public void testConstructor2_Object_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay("T10:20");
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        
        try {
            new TimeOfDay("T1020");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay((Object) null, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10 + OFFSET, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        TimeOfDay test = new TimeOfDay(date, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay((Object) null, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int
    public void testConstructor_int_int() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_Chronology
    public void testConstructor_int_int_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_nullChronology
    public void testConstructor_int_int_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int
    public void testConstructor_int_int_int() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int_Chronology
    public void testConstructor_int_int_int_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20, 30, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20, 30, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1, 30, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60, 30, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, -1, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 60, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int_nullChronology
    public void testConstructor_int_int_int_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int_int
    public void testConstructor_int_int_int_int() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, -1, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 60, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 30, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 30, 1000);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20, 30, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20, 30, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1, 30, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60, 30, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, -1, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 60, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 30, -1, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 30, 1000, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestWeeks::testConstants
    public void testConstants() {
        assertEquals(0, Weeks.ZERO.getWeeks());
        assertEquals(1, Weeks.ONE.getWeeks());
        assertEquals(2, Weeks.TWO.getWeeks());
        assertEquals(3, Weeks.THREE.getWeeks());
        assertEquals(Integer.MAX_VALUE, Weeks.MAX_VALUE.getWeeks());
        assertEquals(Integer.MIN_VALUE, Weeks.MIN_VALUE.getWeeks());
    }

// org.joda.time.TestWeeks::testFactory_weeks_int
    public void testFactory_weeks_int() {
        assertSame(Weeks.ZERO, Weeks.weeks(0));
        assertSame(Weeks.ONE, Weeks.weeks(1));
        assertSame(Weeks.TWO, Weeks.weeks(2));
        assertSame(Weeks.THREE, Weeks.weeks(3));
        assertSame(Weeks.MAX_VALUE, Weeks.weeks(Integer.MAX_VALUE));
        assertSame(Weeks.MIN_VALUE, Weeks.weeks(Integer.MIN_VALUE));
        assertEquals(-1, Weeks.weeks(-1).getWeeks());
        assertEquals(4, Weeks.weeks(4).getWeeks());
    }

// org.joda.time.TestWeeks::testFactory_weeksBetween_RInstant
    public void testFactory_weeksBetween_RInstant() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 30, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 7, 21, 12, 0, 0, 0, PARIS);
        
        assertEquals(3, Weeks.weeksBetween(start, end1).getWeeks());
        assertEquals(0, Weeks.weeksBetween(start, start).getWeeks());
        assertEquals(0, Weeks.weeksBetween(end1, end1).getWeeks());
        assertEquals(-3, Weeks.weeksBetween(end1, start).getWeeks());
        assertEquals(6, Weeks.weeksBetween(start, end2).getWeeks());
    }

// org.joda.time.TestWeeks::testFactory_weeksBetween_RPartial
    public void testFactory_weeksBetween_RPartial() {
        LocalDate start = new LocalDate(2006, 6, 9);
        LocalDate end1 = new LocalDate(2006, 6, 30);
        YearMonthDay end2 = new YearMonthDay(2006, 7, 21);
        
        assertEquals(3, Weeks.weeksBetween(start, end1).getWeeks());
        assertEquals(0, Weeks.weeksBetween(start, start).getWeeks());
        assertEquals(0, Weeks.weeksBetween(end1, end1).getWeeks());
        assertEquals(-3, Weeks.weeksBetween(end1, start).getWeeks());
        assertEquals(6, Weeks.weeksBetween(start, end2).getWeeks());
    }

// org.joda.time.TestWeeks::testFactory_weeksIn_RInterval
    public void testFactory_weeksIn_RInterval() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 30, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 7, 21, 12, 0, 0, 0, PARIS);
        
        assertEquals(0, Weeks.weeksIn((ReadableInterval) null).getWeeks());
        assertEquals(3, Weeks.weeksIn(new Interval(start, end1)).getWeeks());
        assertEquals(0, Weeks.weeksIn(new Interval(start, start)).getWeeks());
        assertEquals(0, Weeks.weeksIn(new Interval(end1, end1)).getWeeks());
        assertEquals(6, Weeks.weeksIn(new Interval(start, end2)).getWeeks());
    }

// org.joda.time.TestWeeks::testFactory_standardWeeksIn_RPeriod
    public void testFactory_standardWeeksIn_RPeriod() {
        assertEquals(0, Weeks.standardWeeksIn((ReadablePeriod) null).getWeeks());
        assertEquals(0, Weeks.standardWeeksIn(Period.ZERO).getWeeks());
        assertEquals(1, Weeks.standardWeeksIn(new Period(0, 0, 1, 0, 0, 0, 0, 0)).getWeeks());
        assertEquals(123, Weeks.standardWeeksIn(Period.weeks(123)).getWeeks());
        assertEquals(-987, Weeks.standardWeeksIn(Period.weeks(-987)).getWeeks());
        assertEquals(1, Weeks.standardWeeksIn(Period.days(13)).getWeeks());
        assertEquals(2, Weeks.standardWeeksIn(Period.days(14)).getWeeks());
        assertEquals(2, Weeks.standardWeeksIn(Period.days(15)).getWeeks());
        try {
            Weeks.standardWeeksIn(Period.months(1));
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testFactory_parseWeeks_String
    public void testFactory_parseWeeks_String() {
        assertEquals(0, Weeks.parseWeeks((String) null).getWeeks());
        assertEquals(0, Weeks.parseWeeks("P0W").getWeeks());
        assertEquals(1, Weeks.parseWeeks("P1W").getWeeks());
        assertEquals(-3, Weeks.parseWeeks("P-3W").getWeeks());
        assertEquals(2, Weeks.parseWeeks("P0Y0M2W").getWeeks());
        assertEquals(2, Weeks.parseWeeks("P2WT0H0M").getWeeks());
        try {
            Weeks.parseWeeks("P1Y1D");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Weeks.parseWeeks("P1WT1H");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testGetMethods
    public void testGetMethods() {
        Weeks test = Weeks.weeks(20);
        assertEquals(20, test.getWeeks());
    }

// org.joda.time.TestWeeks::testGetFieldType
    public void testGetFieldType() {
        Weeks test = Weeks.weeks(20);
        assertEquals(DurationFieldType.weeks(), test.getFieldType());
    }

// org.joda.time.TestWeeks::testGetPeriodType
    public void testGetPeriodType() {
        Weeks test = Weeks.weeks(20);
        assertEquals(PeriodType.weeks(), test.getPeriodType());
    }

// org.joda.time.TestWeeks::testIsGreaterThan
    public void testIsGreaterThan() {
        assertEquals(true, Weeks.THREE.isGreaterThan(Weeks.TWO));
        assertEquals(false, Weeks.THREE.isGreaterThan(Weeks.THREE));
        assertEquals(false, Weeks.TWO.isGreaterThan(Weeks.THREE));
        assertEquals(true, Weeks.ONE.isGreaterThan(null));
        assertEquals(false, Weeks.weeks(-1).isGreaterThan(null));
    }

// org.joda.time.TestWeeks::testIsLessThan
    public void testIsLessThan() {
        assertEquals(false, Weeks.THREE.isLessThan(Weeks.TWO));
        assertEquals(false, Weeks.THREE.isLessThan(Weeks.THREE));
        assertEquals(true, Weeks.TWO.isLessThan(Weeks.THREE));
        assertEquals(false, Weeks.ONE.isLessThan(null));
        assertEquals(true, Weeks.weeks(-1).isLessThan(null));
    }

// org.joda.time.TestWeeks::testToString
    public void testToString() {
        Weeks test = Weeks.weeks(20);
        assertEquals("P20W", test.toString());
        
        test = Weeks.weeks(-20);
        assertEquals("P-20W", test.toString());
    }

// org.joda.time.TestWeeks::testSerialization
    public void testSerialization() throws Exception {
        Weeks test = Weeks.THREE;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Weeks result = (Weeks) ois.readObject();
        ois.close();
        
        assertSame(test, result);
    }

// org.joda.time.TestWeeks::testToStandardDays
    public void testToStandardDays() {
        Weeks test = Weeks.weeks(2);
        Days expected = Days.days(14);
        assertEquals(expected, test.toStandardDays());
        
        try {
            Weeks.MAX_VALUE.toStandardDays();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testToStandardHours
    public void testToStandardHours() {
        Weeks test = Weeks.weeks(2);
        Hours expected = Hours.hours(2 * 7 * 24);
        assertEquals(expected, test.toStandardHours());
        
        try {
            Weeks.MAX_VALUE.toStandardHours();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testToStandardMinutes
    public void testToStandardMinutes() {
        Weeks test = Weeks.weeks(2);
        Minutes expected = Minutes.minutes(2 * 7 * 24 * 60);
        assertEquals(expected, test.toStandardMinutes());
        
        try {
            Weeks.MAX_VALUE.toStandardMinutes();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testToStandardSeconds
    public void testToStandardSeconds() {
        Weeks test = Weeks.weeks(2);
        Seconds expected = Seconds.seconds(2 * 7 * 24 * 60 * 60);
        assertEquals(expected, test.toStandardSeconds());
        
        try {
            Weeks.MAX_VALUE.toStandardSeconds();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testToStandardDuration
    public void testToStandardDuration() {
        Weeks test = Weeks.weeks(20);
        Duration expected = new Duration(20L * DateTimeConstants.MILLIS_PER_WEEK);
        assertEquals(expected, test.toStandardDuration());
        
        expected = new Duration(((long) Integer.MAX_VALUE) * DateTimeConstants.MILLIS_PER_WEEK);
        assertEquals(expected, Weeks.MAX_VALUE.toStandardDuration());
    }

// org.joda.time.TestWeeks::testPlus_int
    public void testPlus_int() {
        Weeks test2 = Weeks.weeks(2);
        Weeks result = test2.plus(3);
        assertEquals(2, test2.getWeeks());
        assertEquals(5, result.getWeeks());
        
        assertEquals(1, Weeks.ONE.plus(0).getWeeks());
        
        try {
            Weeks.MAX_VALUE.plus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testPlus_Weeks
    public void testPlus_Weeks() {
        Weeks test2 = Weeks.weeks(2);
        Weeks test3 = Weeks.weeks(3);
        Weeks result = test2.plus(test3);
        assertEquals(2, test2.getWeeks());
        assertEquals(3, test3.getWeeks());
        assertEquals(5, result.getWeeks());
        
        assertEquals(1, Weeks.ONE.plus(Weeks.ZERO).getWeeks());
        assertEquals(1, Weeks.ONE.plus((Weeks) null).getWeeks());
        
        try {
            Weeks.MAX_VALUE.plus(Weeks.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testMinus_int
    public void testMinus_int() {
        Weeks test2 = Weeks.weeks(2);
        Weeks result = test2.minus(3);
        assertEquals(2, test2.getWeeks());
        assertEquals(-1, result.getWeeks());
        
        assertEquals(1, Weeks.ONE.minus(0).getWeeks());
        
        try {
            Weeks.MIN_VALUE.minus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testMinus_Weeks
    public void testMinus_Weeks() {
        Weeks test2 = Weeks.weeks(2);
        Weeks test3 = Weeks.weeks(3);
        Weeks result = test2.minus(test3);
        assertEquals(2, test2.getWeeks());
        assertEquals(3, test3.getWeeks());
        assertEquals(-1, result.getWeeks());
        
        assertEquals(1, Weeks.ONE.minus(Weeks.ZERO).getWeeks());
        assertEquals(1, Weeks.ONE.minus((Weeks) null).getWeeks());
        
        try {
            Weeks.MIN_VALUE.minus(Weeks.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testMultipliedBy_int
    public void testMultipliedBy_int() {
        Weeks test = Weeks.weeks(2);
        assertEquals(6, test.multipliedBy(3).getWeeks());
        assertEquals(2, test.getWeeks());
        assertEquals(-6, test.multipliedBy(-3).getWeeks());
        assertSame(test, test.multipliedBy(1));
        
        Weeks halfMax = Weeks.weeks(Integer.MAX_VALUE / 2 + 1);
        try {
            halfMax.multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testDividedBy_int
    public void testDividedBy_int() {
        Weeks test = Weeks.weeks(12);
        assertEquals(6, test.dividedBy(2).getWeeks());
        assertEquals(12, test.getWeeks());
        assertEquals(4, test.dividedBy(3).getWeeks());
        assertEquals(3, test.dividedBy(4).getWeeks());
        assertEquals(2, test.dividedBy(5).getWeeks());
        assertEquals(2, test.dividedBy(6).getWeeks());
        assertSame(test, test.dividedBy(1));
        
        try {
            Weeks.ONE.dividedBy(0);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testNegated
    public void testNegated() {
        Weeks test = Weeks.weeks(12);
        assertEquals(-12, test.negated().getWeeks());
        assertEquals(12, test.getWeeks());
        
        try {
            Weeks.MIN_VALUE.negated();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testAddToLocalDate
    public void testAddToLocalDate() {
        Weeks test = Weeks.weeks(3);
        LocalDate date = new LocalDate(2006, 6, 1);
        LocalDate expected = new LocalDate(2006, 6, 22);
        assertEquals(expected, date.plus(test));
    }

// org.joda.time.TestYearMonthDay_Basics::testGet
    public void testGet() {
        YearMonthDay test = new YearMonthDay();
        assertEquals(1970, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.hourOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testSize
    public void testSize() {
        YearMonthDay test = new YearMonthDay();
        assertEquals(3, test.size());
    }

// org.joda.time.TestYearMonthDay_Basics::testGetFieldType
    public void testGetFieldType() {
        YearMonthDay test = new YearMonthDay(COPTIC_PARIS);
        assertSame(DateTimeFieldType.year(), test.getFieldType(0));
        assertSame(DateTimeFieldType.monthOfYear(), test.getFieldType(1));
        assertSame(DateTimeFieldType.dayOfMonth(), test.getFieldType(2));
        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        YearMonthDay test = new YearMonthDay(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.year(), fields[0]);
        assertSame(DateTimeFieldType.monthOfYear(), fields[1]);
        assertSame(DateTimeFieldType.dayOfMonth(), fields[2]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestYearMonthDay_Basics::testGetField
    public void testGetField() {
        YearMonthDay test = new YearMonthDay(COPTIC_PARIS);
        assertSame(COPTIC_UTC.year(), test.getField(0));
        assertSame(COPTIC_UTC.monthOfYear(), test.getField(1));
        assertSame(COPTIC_UTC.dayOfMonth(), test.getField(2));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testGetFields
    public void testGetFields() {
        YearMonthDay test = new YearMonthDay(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertSame(COPTIC_UTC.year(), fields[0]);
        assertSame(COPTIC_UTC.monthOfYear(), fields[1]);
        assertSame(COPTIC_UTC.dayOfMonth(), fields[2]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestYearMonthDay_Basics::testGetValue
    public void testGetValue() {
        YearMonthDay test = new YearMonthDay();
        assertEquals(1970, test.getValue(0));
        assertEquals(6, test.getValue(1));
        assertEquals(9, test.getValue(2));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testGetValues
    public void testGetValues() {
        YearMonthDay test = new YearMonthDay();
        int[] values = test.getValues();
        assertEquals(1970, values[0]);
        assertEquals(6, values[1]);
        assertEquals(9, values[2]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestYearMonthDay_Basics::testIsSupported
    public void testIsSupported() {
        YearMonthDay test = new YearMonthDay(COPTIC_PARIS);
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(false, test.isSupported(DateTimeFieldType.hourOfDay()));
    }

// org.joda.time.TestYearMonthDay_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        YearMonthDay test1 = new YearMonthDay(1970, 6, 9, COPTIC_PARIS);
        YearMonthDay test2 = new YearMonthDay(1970, 6, 9, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        YearMonthDay test3 = new YearMonthDay(1971, 6, 9);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestYearMonthDay_Basics::testCompareTo
    public void testCompareTo() {
        YearMonthDay test1 = new YearMonthDay(2005, 6, 2);
        YearMonthDay test1a = new YearMonthDay(2005, 6, 2);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        YearMonthDay test2 = new YearMonthDay(2005, 7, 2);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        YearMonthDay test3 = new YearMonthDay(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth(),
        };
        int[] values = new int[] {2005, 6, 2};
        Partial p = new Partial(types, values);
        assertEquals(0, test1.compareTo(p));
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

        try {
            test1.compareTo(new TimeOfDay());
            fail();
        } catch (ClassCastException ex) {}
        Partial partial = new Partial()
            .with(DateTimeFieldType.centuryOfEra(), 1)
            .with(DateTimeFieldType.halfdayOfDay(), 0)
            .with(DateTimeFieldType.dayOfMonth(), 9);
        try {
            new YearMonthDay(1970, 6, 9).compareTo(partial);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testIsEqual_YMD
    public void testIsEqual_YMD() {
        YearMonthDay test1 = new YearMonthDay(2005, 6, 2);
        YearMonthDay test1a = new YearMonthDay(2005, 6, 2);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        YearMonthDay test2 = new YearMonthDay(2005, 7, 2);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        YearMonthDay test3 = new YearMonthDay(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new YearMonthDay(2005, 7, 2).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testIsBefore_YMD
    public void testIsBefore_YMD() {
        YearMonthDay test1 = new YearMonthDay(2005, 6, 2);
        YearMonthDay test1a = new YearMonthDay(2005, 6, 2);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        YearMonthDay test2 = new YearMonthDay(2005, 7, 2);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        YearMonthDay test3 = new YearMonthDay(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new YearMonthDay(2005, 7, 2).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testIsAfter_YMD
    public void testIsAfter_YMD() {
        YearMonthDay test1 = new YearMonthDay(2005, 6, 2);
        YearMonthDay test1a = new YearMonthDay(2005, 6, 2);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        YearMonthDay test2 = new YearMonthDay(2005, 7, 2);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        YearMonthDay test3 = new YearMonthDay(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new YearMonthDay(2005, 7, 2).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testWithChronologyRetainFields_Chrono
    public void testWithChronologyRetainFields_Chrono() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        YearMonthDay test = base.withChronologyRetainFields(BUDDHIST_TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 2005, 6, 9);
        assertEquals(BUDDHIST_UTC, test.getChronology());
    }

// org.joda.time.TestYearMonthDay_Basics::testWithChronologyRetainFields_sameChrono
    public void testWithChronologyRetainFields_sameChrono() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        YearMonthDay test = base.withChronologyRetainFields(COPTIC_TOKYO);
        assertSame(base, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithChronologyRetainFields_nullChrono
    public void testWithChronologyRetainFields_nullChrono() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        YearMonthDay test = base.withChronologyRetainFields(null);
        check(base, 2005, 6, 9);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 2005, 6, 9);
        assertEquals(ISO_UTC, test.getChronology());
    }

// org.joda.time.TestYearMonthDay_Basics::testWithChronologyRetainFields_invalidInNewChrono
    public void testWithChronologyRetainFields_invalidInNewChrono() {
        YearMonthDay base = new YearMonthDay(2005, 1, 31, ISO_UTC);
        try {
            base.withChronologyRetainFields(COPTIC_UTC);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonthDay_Basics::testWithField1
    public void testWithField1() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        YearMonthDay result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new YearMonthDay(2004, 6, 9), test);
        assertEquals(new YearMonthDay(2006, 6, 9), result);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithField2
    public void testWithField2() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testWithField3
    public void testWithField3() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        try {
            test.withField(DateTimeFieldType.hourOfDay(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testWithField4
    public void testWithField4() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        YearMonthDay result = test.withField(DateTimeFieldType.year(), 2004);
        assertEquals(new YearMonthDay(2004, 6, 9), test);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithFieldAdded1
    public void testWithFieldAdded1() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        YearMonthDay result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new YearMonthDay(2004, 6, 9), test);
        assertEquals(new YearMonthDay(2010, 6, 9), result);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithFieldAdded2
    public void testWithFieldAdded2() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testWithFieldAdded3
    public void testWithFieldAdded3() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testWithFieldAdded4
    public void testWithFieldAdded4() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        YearMonthDay result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithFieldAdded5
    public void testWithFieldAdded5() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        try {
            test.withFieldAdded(DurationFieldType.hours(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testPlus_RP
    public void testPlus_RP() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        YearMonthDay expected = new YearMonthDay(2003, 7, 7, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testPlusYears_int
    public void testPlusYears_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.plusYears(1);
        YearMonthDay expected = new YearMonthDay(2003, 5, 3, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.plusMonths(1);
        YearMonthDay expected = new YearMonthDay(2002, 6, 3, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testPlusDays_int
    public void testPlusDays_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.plusDays(1);
        YearMonthDay expected = new YearMonthDay(2002, 5, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testMinus_RP
    public void testMinus_RP() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        YearMonthDay expected = new YearMonthDay(2001, 4, 2, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testMinusYears_int
    public void testMinusYears_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.minusYears(1);
        YearMonthDay expected = new YearMonthDay(2001, 5, 3, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.minusMonths(1);
        YearMonthDay expected = new YearMonthDay(2002, 4, 3, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testMinusDays_int
    public void testMinusDays_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.minusDays(1);
        YearMonthDay expected = new YearMonthDay(2002, 5, 2, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testToLocalDate
    public void testToLocalDate() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_UTC);
        LocalDate test = base.toLocalDate();
        assertEquals(new LocalDate(2005, 6, 9, COPTIC_UTC), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtMidnight
    public void testToDateTimeAtMidnight() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight();
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtMidnight_Zone
    public void testToDateTimeAtMidnight_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight(TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_TOKYO), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtMidnight_nullZone
    public void testToDateTimeAtMidnight_nullZone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight((DateTimeZone) null);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtCurrentTime
    public void testToDateTimeAtCurrentTime() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeAtCurrentTime();
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_LONDON);
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtCurrentTime_Zone
    public void testToDateTimeAtCurrentTime_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeAtCurrentTime(TOKYO);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_TOKYO);
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtCurrentTime_nullZone
    public void testToDateTimeAtCurrentTime_nullZone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        DateTime dt = new DateTime(2004, 6, 9, 6, 7, 8, 9);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTimeAtCurrentTime((DateTimeZone) null);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(dt.getMillis(), COPTIC_LONDON);
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_TOD
    public void testToDateTime_TOD() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        TimeOfDay tod = new TimeOfDay(12, 13, 14, 15, BUDDHIST_TOKYO);
        
        DateTime test = base.toDateTime(tod);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_nullTOD
    public void testToDateTime_nullTOD() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        long now = new DateTime(2004, 5, 8, 12, 13, 14, 15, COPTIC_LONDON).getMillis();
        DateTimeUtils.setCurrentMillisFixed(now);
        
        DateTime test = base.toDateTime((TimeOfDay) null);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_TOD_Zone
    public void testToDateTime_TOD_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        TimeOfDay tod = new TimeOfDay(12, 13, 14, 15, BUDDHIST_TOKYO);
        
        DateTime test = base.toDateTime(tod, TOKYO);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_TOD_nullZone
    public void testToDateTime_TOD_nullZone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        TimeOfDay tod = new TimeOfDay(12, 13, 14, 15, BUDDHIST_TOKYO);
        
        DateTime test = base.toDateTime(tod, null);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_nullTOD_Zone
    public void testToDateTime_nullTOD_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        long now = new DateTime(2004, 5, 8, 12, 13, 14, 15, COPTIC_TOKYO).getMillis();
        DateTimeUtils.setCurrentMillisFixed(now);
        
        DateTime test = base.toDateTime((TimeOfDay) null, TOKYO);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateMidnight
    public void testToDateMidnight() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight();
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_LONDON), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateMidnight_Zone
    public void testToDateMidnight_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight(TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_TOKYO), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateMidnight_nullZone
    public void testToDateMidnight_nullZone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight((DateTimeZone) null);
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_LONDON), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7);
        
        DateTime test = base.toDateTime(dt);
        check(base, 2005, 6, 9);
        DateTime expected = dt;
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 2005, 6, 9);
        DateTime expected = dt;
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToInterval
    public void testToInterval() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval();
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTime(TimeOfDay.MIDNIGHT);
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToInterval_Zone
    public void testToInterval_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval(TOKYO);
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTime(TimeOfDay.MIDNIGHT, TOKYO);
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToInterval_nullZone
    public void testToInterval_nullZone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval(null);
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTime(TimeOfDay.MIDNIGHT, LONDON);
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithers
    public void testWithers() {
        YearMonthDay test = new YearMonthDay(1970, 6, 9);
        check(test.withYear(2000), 2000, 6, 9);
        check(test.withMonthOfYear(2), 1970, 2, 9);
        check(test.withDayOfMonth(2), 1970, 6, 2);
        try {
            test.withMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testProperty
    public void testProperty() {
        YearMonthDay test = new YearMonthDay(2005, 6, 9);
        assertEquals(test.year(), test.property(DateTimeFieldType.year()));
        assertEquals(test.monthOfYear(), test.property(DateTimeFieldType.monthOfYear()));
        assertEquals(test.dayOfMonth(), test.property(DateTimeFieldType.dayOfMonth()));
        try {
            test.property(DateTimeFieldType.millisOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testSerialization
    public void testSerialization() throws Exception {
        YearMonthDay test = new YearMonthDay(1972, 6, 9, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        YearMonthDay result = (YearMonthDay) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestYearMonthDay_Basics::testToString
    public void testToString() {
        YearMonthDay test = new YearMonthDay(2002, 6, 9);
        assertEquals("2002-06-09", test.toString());
    }

// org.joda.time.TestYearMonthDay_Basics::testToString_String
    public void testToString_String() {
        YearMonthDay test = new YearMonthDay(2002, 6, 9);
        assertEquals("2002 \ufffd\ufffd", test.toString("yyyy HH"));
        assertEquals("2002-06-09", test.toString((String) null));
    }

// org.joda.time.TestYearMonthDay_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        YearMonthDay test = new YearMonthDay(2002, 6, 9);
        assertEquals("\ufffd 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("\ufffd 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("2002-06-09", test.toString(null, Locale.ENGLISH));
        assertEquals("\ufffd 9/6", test.toString("EEE d/M", null));
        assertEquals("2002-06-09", test.toString(null, null));
    }

// org.joda.time.TestYearMonthDay_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        YearMonthDay test = new YearMonthDay(2002, 6, 9);
        assertEquals("2002 \ufffd\ufffd", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestYearMonthDay_Constructors::testFactory_FromCalendarFields
    public void testFactory_FromCalendarFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        YearMonthDay expected = new YearMonthDay(1970, 2, 3);
        assertEquals(expected, YearMonthDay.fromCalendarFields(cal));
        try {
            YearMonthDay.fromCalendarFields(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Constructors::testFactory_FromDateFields
    public void testFactory_FromDateFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        YearMonthDay expected = new YearMonthDay(1970, 2, 3);
        assertEquals(expected, YearMonthDay.fromDateFields(cal.getTime()));
        try {
            YearMonthDay.fromDateFields(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        YearMonthDay test = new YearMonthDay();
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        YearMonthDay test = new YearMonthDay(LONDON);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(8, test.getDayOfMonth());
        
        test = new YearMonthDay(PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        YearMonthDay test = new YearMonthDay((DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(8, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        YearMonthDay test = new YearMonthDay((Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        YearMonthDay test = new YearMonthDay(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        YearMonthDay test = new YearMonthDay(TEST_TIME2);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(TEST_TIME1, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(TEST_TIME1, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1);
        YearMonthDay test = new YearMonthDay(date);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        YearMonthDay test = new YearMonthDay((Object) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        YearMonthDay test = new YearMonthDay("1972-12-03");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectString2
    public void testConstructor_ObjectString2() throws Throwable {
        YearMonthDay test = new YearMonthDay("1972-12-03T+14:00");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(2, test.getDayOfMonth());  
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectString3
    public void testConstructor_ObjectString3() throws Throwable {
        YearMonthDay test = new YearMonthDay("1972-12-03T10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectString4
    public void testConstructor_ObjectString4() throws Throwable {
        YearMonthDay test = new YearMonthDay("1972-12-03T10:20:30.040+14:00");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(2, test.getDayOfMonth());  
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectString5
    public void testConstructor_ObjectString5() throws Throwable {
        YearMonthDay test = new YearMonthDay("10");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getYear());
        assertEquals(1, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new YearMonthDay("T10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new YearMonthDay("T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectStringEx3
    public void testConstructor_ObjectStringEx3() throws Throwable {
        try {
            new YearMonthDay("10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectStringEx4
    public void testConstructor_ObjectStringEx4() throws Throwable {
        try {
            new YearMonthDay("10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        YearMonthDay test = new YearMonthDay(date, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        YearMonthDay test = new YearMonthDay((Object) null, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        YearMonthDay test = new YearMonthDay(date, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        YearMonthDay test = new YearMonthDay((Object) null, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_int_int_int
    public void testConstructor_int_int_int() throws Throwable {
        YearMonthDay test = new YearMonthDay(1970, 6, 9);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new YearMonthDay(Integer.MIN_VALUE, 6, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(Integer.MAX_VALUE, 6, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 0, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 13, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 6, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 6, 31);
            fail();
        } catch (IllegalArgumentException ex) {}
        new YearMonthDay(1970, 7, 31);
        try {
            new YearMonthDay(1970, 7, 32);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_int_int_int_Chronology
    public void testConstructor_int_int_int_Chronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(1970, 6, 9, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new YearMonthDay(Integer.MIN_VALUE, 6, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(Integer.MAX_VALUE, 6, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 0, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 13, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 6, 0, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 6, 31, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        new YearMonthDay(1970, 7, 31, GREGORIAN_PARIS);
        try {
            new YearMonthDay(1970, 7, 32, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_int_int_int_nullChronology
    public void testConstructor_int_int_int_nullChronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(1970, 6, 9, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonth_Basics::testGet
    public void testGet() {
        YearMonth test = new YearMonth();
        assertEquals(1970, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.dayOfMonth());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testSize
    public void testSize() {
        YearMonth test = new YearMonth();
        assertEquals(2, test.size());
    }

// org.joda.time.TestYearMonth_Basics::testGetFieldType
    public void testGetFieldType() {
        YearMonth test = new YearMonth(COPTIC_PARIS);
        assertSame(DateTimeFieldType.year(), test.getFieldType(0));
        assertSame(DateTimeFieldType.monthOfYear(), test.getFieldType(1));
        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        YearMonth test = new YearMonth(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertEquals(2, fields.length);
        assertSame(DateTimeFieldType.year(), fields[0]);
        assertSame(DateTimeFieldType.monthOfYear(), fields[1]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestYearMonth_Basics::testGetField
    public void testGetField() {
        YearMonth test = new YearMonth(COPTIC_PARIS);
        assertSame(COPTIC_UTC.year(), test.getField(0));
        assertSame(COPTIC_UTC.monthOfYear(), test.getField(1));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testGetFields
    public void testGetFields() {
        YearMonth test = new YearMonth(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertEquals(2, fields.length);
        assertSame(COPTIC_UTC.year(), fields[0]);
        assertSame(COPTIC_UTC.monthOfYear(), fields[1]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestYearMonth_Basics::testGetValue
    public void testGetValue() {
        YearMonth test = new YearMonth();
        assertEquals(1970, test.getValue(0));
        assertEquals(6, test.getValue(1));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testGetValues
    public void testGetValues() {
        YearMonth test = new YearMonth();
        int[] values = test.getValues();
        assertEquals(2, values.length);
        assertEquals(1970, values[0]);
        assertEquals(6, values[1]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestYearMonth_Basics::testIsSupported
    public void testIsSupported() {
        YearMonth test = new YearMonth(COPTIC_PARIS);
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(false, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(false, test.isSupported(DateTimeFieldType.hourOfDay()));
    }

// org.joda.time.TestYearMonth_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        YearMonth test1 = new YearMonth(1970, 6, COPTIC_PARIS);
        YearMonth test2 = new YearMonth(1970, 6, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        YearMonth test3 = new YearMonth(1971, 6);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockYM()));
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestYearMonth_Basics::testCompareTo
    public void testCompareTo() {
        YearMonth test1 = new YearMonth(2005, 6);
        YearMonth test1a = new YearMonth(2005, 6);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        YearMonth test2 = new YearMonth(2005, 7);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        YearMonth test3 = new YearMonth(2005, 7, GregorianChronology.getInstanceUTC());
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
        };
        int[] values = new int[] {2005, 6};
        Partial p = new Partial(types, values);
        assertEquals(0, test1.compareTo(p));
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            test1.compareTo(new LocalTime());
            fail();
        } catch (ClassCastException ex) {}
        Partial partial = new Partial()
            .with(DateTimeFieldType.centuryOfEra(), 1)
            .with(DateTimeFieldType.halfdayOfDay(), 0)
            .with(DateTimeFieldType.dayOfMonth(), 9);
        try {
            new YearMonth(1970, 6).compareTo(partial);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testIsEqual_YM
    public void testIsEqual_YM() {
        YearMonth test1 = new YearMonth(2005, 6);
        YearMonth test1a = new YearMonth(2005, 6);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        YearMonth test2 = new YearMonth(2005, 7);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        YearMonth test3 = new YearMonth(2005, 7, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new YearMonth(2005, 7).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testIsBefore_YM
    public void testIsBefore_YM() {
        YearMonth test1 = new YearMonth(2005, 6);
        YearMonth test1a = new YearMonth(2005, 6);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        YearMonth test2 = new YearMonth(2005, 7);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        YearMonth test3 = new YearMonth(2005, 7, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new YearMonth(2005, 7).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testIsAfter_YM
    public void testIsAfter_YM() {
        YearMonth test1 = new YearMonth(2005, 6);
        YearMonth test1a = new YearMonth(2005, 6);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        YearMonth test2 = new YearMonth(2005, 7);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        YearMonth test3 = new YearMonth(2005, 7, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new YearMonth(2005, 7).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testWithChronologyRetainFields_Chrono
    public void testWithChronologyRetainFields_Chrono() {
        YearMonth base = new YearMonth(2005, 6, COPTIC_PARIS);
        YearMonth test = base.withChronologyRetainFields(BUDDHIST_TOKYO);
        check(base, 2005, 6);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 2005, 6);
        assertEquals(BUDDHIST_UTC, test.getChronology());
    }

// org.joda.time.TestYearMonth_Basics::testWithChronologyRetainFields_sameChrono
    public void testWithChronologyRetainFields_sameChrono() {
        YearMonth base = new YearMonth(2005, 6, COPTIC_PARIS);
        YearMonth test = base.withChronologyRetainFields(COPTIC_TOKYO);
        assertSame(base, test);
    }

// org.joda.time.TestYearMonth_Basics::testWithChronologyRetainFields_nullChrono
    public void testWithChronologyRetainFields_nullChrono() {
        YearMonth base = new YearMonth(2005, 6, COPTIC_PARIS);
        YearMonth test = base.withChronologyRetainFields(null);
        check(base, 2005, 6);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 2005, 6);
        assertEquals(ISO_UTC, test.getChronology());
    }

// org.joda.time.TestYearMonth_Basics::testWithChronologyRetainFields_invalidInNewChrono
    public void testWithChronologyRetainFields_invalidInNewChrono() {
        YearMonth base = new YearMonth(2005, 13, COPTIC_UTC);
        try {
            base.withChronologyRetainFields(ISO_UTC);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonth_Basics::testWithField
    public void testWithField() {
        YearMonth test = new YearMonth(2004, 6);
        YearMonth result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new YearMonth(2004, 6), test);
        assertEquals(new YearMonth(2006, 6), result);
    }

// org.joda.time.TestYearMonth_Basics::testWithField_nullField
    public void testWithField_nullField() {
        YearMonth test = new YearMonth(2004, 6);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testWithField_unknownField
    public void testWithField_unknownField() {
        YearMonth test = new YearMonth(2004, 6);
        try {
            test.withField(DateTimeFieldType.hourOfDay(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testWithField_same
    public void testWithField_same() {
        YearMonth test = new YearMonth(2004, 6);
        YearMonth result = test.withField(DateTimeFieldType.year(), 2004);
        assertEquals(new YearMonth(2004, 6), test);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonth_Basics::testWithFieldAdded
    public void testWithFieldAdded() {
        YearMonth test = new YearMonth(2004, 6);
        YearMonth result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new YearMonth(2004, 6), test);
        assertEquals(new YearMonth(2010, 6), result);
    }

// org.joda.time.TestYearMonth_Basics::testWithFieldAdded_nullField_zero
    public void testWithFieldAdded_nullField_zero() {
        YearMonth test = new YearMonth(2004, 6);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testWithFieldAdded_nullField_nonZero
    public void testWithFieldAdded_nullField_nonZero() {
        YearMonth test = new YearMonth(2004, 6);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testWithFieldAdded_zero
    public void testWithFieldAdded_zero() {
        YearMonth test = new YearMonth(2004, 6);
        YearMonth result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonth_Basics::testWithFieldAdded_unknownField
    public void testWithFieldAdded_unknownField() {
        YearMonth test = new YearMonth(2004, 6);
        try {
            test.withFieldAdded(DurationFieldType.hours(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testPlus_RP
    public void testPlus_RP() {
        YearMonth test = new YearMonth(2002, 5, BuddhistChronology.getInstance());
        YearMonth result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        YearMonth expected = new YearMonth(2003, 7, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonth_Basics::testPlusYears_int
    public void testPlusYears_int() {
        YearMonth test = new YearMonth(2002, 5, BuddhistChronology.getInstance());
        YearMonth result = test.plusYears(1);
        YearMonth expected = new YearMonth(2003, 5, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonth_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        YearMonth test = new YearMonth(2002, 5, BuddhistChronology.getInstance());
        YearMonth result = test.plusMonths(1);
        YearMonth expected = new YearMonth(2002, 6, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonth_Basics::testMinus_RP
    public void testMinus_RP() {
        YearMonth test = new YearMonth(2002, 5, BuddhistChronology.getInstance());
        YearMonth result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        YearMonth expected = new YearMonth(2001, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonth_Basics::testMinusYears_int
    public void testMinusYears_int() {
        YearMonth test = new YearMonth(2002, 5, BuddhistChronology.getInstance());
        YearMonth result = test.minusYears(1);
        YearMonth expected = new YearMonth(2001, 5, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonth_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        YearMonth test = new YearMonth(2002, 5, BuddhistChronology.getInstance());
        YearMonth result = test.minusMonths(1);
        YearMonth expected = new YearMonth(2002, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonth_Basics::testToLocalDate
    public void testToLocalDate() {
        YearMonth base = new YearMonth(2005, 6, COPTIC_UTC);
        LocalDate test = base.toLocalDate(9);
        assertEquals(new LocalDate(2005, 6, 9, COPTIC_UTC), test);
        try {
            base.toLocalDate(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        YearMonth base = new YearMonth(2005, 6, COPTIC_PARIS);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7);
        
        DateTime test = base.toDateTime(dt);
        check(base, 2005, 6);
        DateTime expected = dt;
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonth_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        YearMonth base = new YearMonth(2005, 6);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 2005, 6);
        DateTime expected = dt;
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonth_Basics::testToInterval
    public void testToInterval() {
        YearMonth base = new YearMonth(2005, 6, COPTIC_PARIS); 
        Interval test = base.toInterval();
        check(base, 2005, 6);
        DateTime start = new DateTime(2005, 6, 1, 0, 0, COPTIC_LONDON);
        DateTime end = new DateTime(2005, 7, 1, 0, 0, COPTIC_LONDON);
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonth_Basics::testToInterval_Zone
    public void testToInterval_Zone() {
        YearMonth base = new YearMonth(2005, 6, COPTIC_PARIS); 
        Interval test = base.toInterval(TOKYO);
        check(base, 2005, 6);
        DateTime start = new DateTime(2005, 6, 1, 0, 0, COPTIC_TOKYO);
        DateTime end = new DateTime(2005, 7, 1, 0, 0, COPTIC_TOKYO);
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonth_Basics::testToInterval_nullZone
    public void testToInterval_nullZone() {
        YearMonth base = new YearMonth(2005, 6, COPTIC_PARIS); 
        Interval test = base.toInterval(null);
        check(base, 2005, 6);
        DateTime start = new DateTime(2005, 6, 1, 0, 0, COPTIC_LONDON);
        DateTime end = new DateTime(2005, 7, 1, 0, 0, COPTIC_LONDON);
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonth_Basics::testWithers
    public void testWithers() {
        YearMonth test = new YearMonth(1970, 6);
        check(test.withYear(2000), 2000, 6);
        check(test.withMonthOfYear(2), 1970, 2);
        try {
            test.withMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testProperty
    public void testProperty() {
        YearMonth test = new YearMonth(2005, 6);
        assertEquals(test.year(), test.property(DateTimeFieldType.year()));
        assertEquals(test.monthOfYear(), test.property(DateTimeFieldType.monthOfYear()));
        try {
            test.property(DateTimeFieldType.millisOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testSerialization
    public void testSerialization() throws Exception {
        YearMonth test = new YearMonth(1972, 6, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        YearMonth result = (YearMonth) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestYearMonth_Basics::testToString
    public void testToString() {
        YearMonth test = new YearMonth(2002, 6);
        assertEquals("2002-06", test.toString());
    }

// org.joda.time.TestYearMonth_Basics::testToString_String
    public void testToString_String() {
        YearMonth test = new YearMonth(2002, 6);
        assertEquals("2002 \ufffd\ufffd", test.toString("yyyy HH"));
        assertEquals("2002-06", test.toString((String) null));
    }

// org.joda.time.TestYearMonth_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        YearMonth test = new YearMonth(2002, 6);
        assertEquals("\ufffd \ufffd/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("\ufffd \ufffd/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("2002-06", test.toString(null, Locale.ENGLISH));
        assertEquals("\ufffd \ufffd/6", test.toString("EEE d/M", null));
        assertEquals("2002-06", test.toString(null, null));
    }

// org.joda.time.TestYearMonth_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        YearMonth test = new YearMonth(2002, 6);
        assertEquals("2002 \ufffd\ufffd", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestYearMonth_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new YearMonth(2010, 6), YearMonth.parse("2010-06-30"));
        assertEquals(new YearMonth(2010, 1), YearMonth.parse("2010-002"));
    }

// org.joda.time.TestYearMonth_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--MM").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new YearMonth(2010, 6), YearMonth.parse("2010--06", f));
    }

// org.joda.time.TestYearMonth_Constructors::testFactory_FromCalendarFields
    public void testFactory_FromCalendarFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        YearMonth expected = new YearMonth(1970, 2);
        assertEquals(expected, YearMonth.fromCalendarFields(cal));
        try {
            YearMonth.fromCalendarFields(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Constructors::testFactory_FromDateFields
    public void testFactory_FromDateFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        YearMonth expected = new YearMonth(1970, 2);
        assertEquals(expected, YearMonth.fromDateFields(cal.getTime()));
        try {
            YearMonth.fromDateFields(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        YearMonth test = new YearMonth();
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(test, YearMonth.now());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 30, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        YearMonth test = new YearMonth(LONDON);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(test, YearMonth.now(LONDON));
        
        test = new YearMonth(PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(7, test.getMonthOfYear());
        assertEquals(test, YearMonth.now(PARIS));
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 30, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        YearMonth test = new YearMonth((DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        YearMonth test = new YearMonth(GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(test, YearMonth.now(GREGORIAN_PARIS));
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        YearMonth test = new YearMonth((Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        YearMonth test = new YearMonth(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        YearMonth test = new YearMonth(TEST_TIME2);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        YearMonth test = new YearMonth(TEST_TIME1, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        YearMonth test = new YearMonth(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        YearMonth test = new YearMonth(TEST_TIME1, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1);
        YearMonth test = new YearMonth(date);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        YearMonth test = new YearMonth((Object) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        YearMonth test = new YearMonth("1972-12");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_ObjectString5
    public void testConstructor_ObjectString5() throws Throwable {
        YearMonth test = new YearMonth("10");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getYear());
        assertEquals(1, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new YearMonth("T10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new YearMonth("T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_ObjectStringEx3
    public void testConstructor_ObjectStringEx3() throws Throwable {
        try {
            new YearMonth("10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_ObjectStringEx4
    public void testConstructor_ObjectStringEx4() throws Throwable {
        try {
            new YearMonth("10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        YearMonth test = new YearMonth(date, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        YearMonth test = new YearMonth((Object) null, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        YearMonth test = new YearMonth(date, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        YearMonth test = new YearMonth((Object) null, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_int_int
    public void testConstructor_int_int() throws Throwable {
        YearMonth test = new YearMonth(1970, 6);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        try {
            new YearMonth(Integer.MIN_VALUE, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonth(Integer.MAX_VALUE, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonth(1970, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonth(1970, 13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_int_int_Chronology
    public void testConstructor_int_int_Chronology() throws Throwable {
        YearMonth test = new YearMonth(1970, 6, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        try {
            new YearMonth(Integer.MIN_VALUE, 6, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonth(Integer.MAX_VALUE, 6, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonth(1970, 0, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonth(1970, 13, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_int_int_nullChronology
    public void testConstructor_int_int_nullChronology() throws Throwable {
        YearMonth test = new YearMonth(1970, 6, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
    }

// org.joda.time.TestYears::testConstants
    public void testConstants() {
        assertEquals(0, Years.ZERO.getYears());
        assertEquals(1, Years.ONE.getYears());
        assertEquals(2, Years.TWO.getYears());
        assertEquals(3, Years.THREE.getYears());
        assertEquals(Integer.MAX_VALUE, Years.MAX_VALUE.getYears());
        assertEquals(Integer.MIN_VALUE, Years.MIN_VALUE.getYears());
    }

// org.joda.time.TestYears::testFactory_years_int
    public void testFactory_years_int() {
        assertSame(Years.ZERO, Years.years(0));
        assertSame(Years.ONE, Years.years(1));
        assertSame(Years.TWO, Years.years(2));
        assertSame(Years.THREE, Years.years(3));
        assertSame(Years.MAX_VALUE, Years.years(Integer.MAX_VALUE));
        assertSame(Years.MIN_VALUE, Years.years(Integer.MIN_VALUE));
        assertEquals(-1, Years.years(-1).getYears());
        assertEquals(4, Years.years(4).getYears());
    }

// org.joda.time.TestYears::testFactory_yearsBetween_RInstant
    public void testFactory_yearsBetween_RInstant() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2009, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2012, 6, 9, 12, 0, 0, 0, PARIS);
        
        assertEquals(3, Years.yearsBetween(start, end1).getYears());
        assertEquals(0, Years.yearsBetween(start, start).getYears());
        assertEquals(0, Years.yearsBetween(end1, end1).getYears());
        assertEquals(-3, Years.yearsBetween(end1, start).getYears());
        assertEquals(6, Years.yearsBetween(start, end2).getYears());
    }

// org.joda.time.TestYears::testFactory_yearsBetween_RPartial
    public void testFactory_yearsBetween_RPartial() {
        LocalDate start = new LocalDate(2006, 6, 9);
        LocalDate end1 = new LocalDate(2009, 6, 9);
        YearMonthDay end2 = new YearMonthDay(2012, 6, 9);
        
        assertEquals(3, Years.yearsBetween(start, end1).getYears());
        assertEquals(0, Years.yearsBetween(start, start).getYears());
        assertEquals(0, Years.yearsBetween(end1, end1).getYears());
        assertEquals(-3, Years.yearsBetween(end1, start).getYears());
        assertEquals(6, Years.yearsBetween(start, end2).getYears());
    }

// org.joda.time.TestYears::testFactory_yearsIn_RInterval
    public void testFactory_yearsIn_RInterval() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2009, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2012, 6, 9, 12, 0, 0, 0, PARIS);
        
        assertEquals(0, Years.yearsIn((ReadableInterval) null).getYears());
        assertEquals(3, Years.yearsIn(new Interval(start, end1)).getYears());
        assertEquals(0, Years.yearsIn(new Interval(start, start)).getYears());
        assertEquals(0, Years.yearsIn(new Interval(end1, end1)).getYears());
        assertEquals(6, Years.yearsIn(new Interval(start, end2)).getYears());
    }

// org.joda.time.TestYears::testFactory_parseYears_String
    public void testFactory_parseYears_String() {
        assertEquals(0, Years.parseYears((String) null).getYears());
        assertEquals(0, Years.parseYears("P0Y").getYears());
        assertEquals(1, Years.parseYears("P1Y").getYears());
        assertEquals(-3, Years.parseYears("P-3Y").getYears());
        assertEquals(2, Years.parseYears("P2Y0M").getYears());
        assertEquals(2, Years.parseYears("P2YT0H0M").getYears());
        try {
            Years.parseYears("P1M1D");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Years.parseYears("P1YT1H");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYears::testGetMethods
    public void testGetMethods() {
        Years test = Years.years(20);
        assertEquals(20, test.getYears());
    }

// org.joda.time.TestYears::testGetFieldType
    public void testGetFieldType() {
        Years test = Years.years(20);
        assertEquals(DurationFieldType.years(), test.getFieldType());
    }

// org.joda.time.TestYears::testGetPeriodType
    public void testGetPeriodType() {
        Years test = Years.years(20);
        assertEquals(PeriodType.years(), test.getPeriodType());
    }

// org.joda.time.TestYears::testIsGreaterThan
    public void testIsGreaterThan() {
        assertEquals(true, Years.THREE.isGreaterThan(Years.TWO));
        assertEquals(false, Years.THREE.isGreaterThan(Years.THREE));
        assertEquals(false, Years.TWO.isGreaterThan(Years.THREE));
        assertEquals(true, Years.ONE.isGreaterThan(null));
        assertEquals(false, Years.years(-1).isGreaterThan(null));
    }

// org.joda.time.TestYears::testIsLessThan
    public void testIsLessThan() {
        assertEquals(false, Years.THREE.isLessThan(Years.TWO));
        assertEquals(false, Years.THREE.isLessThan(Years.THREE));
        assertEquals(true, Years.TWO.isLessThan(Years.THREE));
        assertEquals(false, Years.ONE.isLessThan(null));
        assertEquals(true, Years.years(-1).isLessThan(null));
    }

// org.joda.time.TestYears::testToString
    public void testToString() {
        Years test = Years.years(20);
        assertEquals("P20Y", test.toString());
        
        test = Years.years(-20);
        assertEquals("P-20Y", test.toString());
    }

// org.joda.time.TestYears::testSerialization
    public void testSerialization() throws Exception {
        Years test = Years.THREE;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Years result = (Years) ois.readObject();
        ois.close();
        
        assertSame(test, result);
    }
