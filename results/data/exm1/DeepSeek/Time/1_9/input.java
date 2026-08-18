// buggy code
    public int compareTo(DurationField durationField) {
        if (durationField.isSupported()) {
            return 1;
        }
        return 0;
    }

    public Partial(DateTimeFieldType[] types, int[] values, Chronology chronology) {
        super();
        chronology = DateTimeUtils.getChronology(chronology).withUTC();
        iChronology = chronology;
        if (types == null) {
            throw new IllegalArgumentException("Types array must not be null");
        }
        if (values == null) {
            throw new IllegalArgumentException("Values array must not be null");
        }
        if (values.length != types.length) {
            throw new IllegalArgumentException("Values array must be the same length as the types array");
        }
        if (types.length == 0) {
            iTypes = types;
            iValues = values;
            return;
        }
        for (int i = 0; i < types.length; i++) {
            if (types[i] == null) {
                throw new IllegalArgumentException("Types array must not contain null: index " + i);
            }
        }
        DurationField lastUnitField = null;
        for (int i = 0; i < types.length; i++) {
            DateTimeFieldType loopType = types[i];
            DurationField loopUnitField = loopType.getDurationType().getField(iChronology);
            if (i > 0) {
                int compare = lastUnitField.compareTo(loopUnitField);
                if (compare < 0) {
                    throw new IllegalArgumentException("Types array must be in order largest-smallest: " +
                            types[i - 1].getName() + " < " + loopType.getName());
                } else if (compare == 0) {
                    if (types[i - 1].getRangeDurationType() == null) {
                        if (loopType.getRangeDurationType() == null) {
                            throw new IllegalArgumentException("Types array must not contain duplicate: " +
                                            types[i - 1].getName() + " and " + loopType.getName());
                        }
                    } else {
                        if (loopType.getRangeDurationType() == null) {
                            throw new IllegalArgumentException("Types array must be in order largest-smallest: " +
                                    types[i - 1].getName() + " < " + loopType.getName());
                        }
                        DurationField lastRangeField = types[i - 1].getRangeDurationType().getField(iChronology);
                        DurationField loopRangeField = loopType.getRangeDurationType().getField(iChronology);
                        if (lastRangeField.compareTo(loopRangeField) < 0) {
                            throw new IllegalArgumentException("Types array must be in order largest-smallest: " +
                                    types[i - 1].getName() + " < " + loopType.getName());
                        }
                        if (lastRangeField.compareTo(loopRangeField) == 0) {
                            throw new IllegalArgumentException("Types array must not contain duplicate: " +
                                            types[i - 1].getName() + " and " + loopType.getName());
                        }
                    }
                }
            }
            lastUnitField = loopUnitField;
        }
        
        iTypes = (DateTimeFieldType[]) types.clone();
        chronology.validate(this, values);
        iValues = (int[]) values.clone();
    }

// relevant test
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

// org.joda.time.TestYearMonthDay_Properties::testPropertyGetYear
    public void testPropertyGetYear() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        assertSame(test.getChronology().year(), test.year().getField());
        assertEquals("year", test.year().getName());
        assertEquals("Property[year]", test.year().toString());
        assertSame(test, test.year().getReadablePartial());
        assertSame(test, test.year().getYearMonthDay());
        assertEquals(1972, test.year().get());
        assertEquals("1972", test.year().getAsString());
        assertEquals("1972", test.year().getAsText());
        assertEquals("1972", test.year().getAsText(Locale.FRENCH));
        assertEquals("1972", test.year().getAsShortText());
        assertEquals("1972", test.year().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().years(), test.year().getDurationField());
        assertEquals(null, test.year().getRangeDurationField());
        assertEquals(9, test.year().getMaximumTextLength(null));
        assertEquals(9, test.year().getMaximumShortTextLength(null));
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyGetMaxMinValuesYear
    public void testPropertyGetMaxMinValuesYear() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        assertEquals(-292275054, test.year().getMinimumValue());
        assertEquals(-292275054, test.year().getMinimumValueOverall());
        assertEquals(292278993, test.year().getMaximumValue());
        assertEquals(292278993, test.year().getMaximumValueOverall());
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyAddYear
    public void testPropertyAddYear() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.year().addToCopy(9);
        check(test, 1972, 6, 9);
        check(copy, 1981, 6, 9);
        
        copy = test.year().addToCopy(0);
        check(copy, 1972, 6, 9);
        
        copy = test.year().addToCopy(292277023 - 1972);
        check(copy, 292277023, 6, 9);
        
        try {
            test.year().addToCopy(292278993 - 1972 + 1);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 1972, 6, 9);
        
        copy = test.year().addToCopy(-1972);
        check(copy, 0, 6, 9);
        
        copy = test.year().addToCopy(-1973);
        check(copy, -1, 6, 9);
        
        try {
            test.year().addToCopy(-292275054 - 1972 - 1);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 1972, 6, 9);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyAddWrapFieldYear
    public void testPropertyAddWrapFieldYear() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.year().addWrapFieldToCopy(9);
        check(test, 1972, 6, 9);
        check(copy, 1981, 6, 9);
        
        copy = test.year().addWrapFieldToCopy(0);
        check(copy, 1972, 6, 9);
        
        copy = test.year().addWrapFieldToCopy(292278993 - 1972 + 1);
        check(copy, -292275054, 6, 9);
        
        copy = test.year().addWrapFieldToCopy(-292275054 - 1972 - 1);
        check(copy, 292278993, 6, 9);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertySetYear
    public void testPropertySetYear() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.year().setCopy(12);
        check(test, 1972, 6, 9);
        check(copy, 12, 6, 9);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertySetTextYear
    public void testPropertySetTextYear() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.year().setCopy("12");
        check(test, 1972, 6, 9);
        check(copy, 12, 6, 9);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyCompareToYear
    public void testPropertyCompareToYear() {
        YearMonthDay test1 = new YearMonthDay(TEST_TIME1);
        YearMonthDay test2 = new YearMonthDay(TEST_TIME2);
        assertEquals(true, test1.year().compareTo(test2) < 0);
        assertEquals(true, test2.year().compareTo(test1) > 0);
        assertEquals(true, test1.year().compareTo(test1) == 0);
        try {
            test1.year().compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.year().compareTo(dt2) < 0);
        assertEquals(true, test2.year().compareTo(dt1) > 0);
        assertEquals(true, test1.year().compareTo(dt1) == 0);
        try {
            test1.year().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyGetMonth
    public void testPropertyGetMonth() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        assertSame(test.getChronology().monthOfYear(), test.monthOfYear().getField());
        assertEquals("monthOfYear", test.monthOfYear().getName());
        assertEquals("Property[monthOfYear]", test.monthOfYear().toString());
        assertSame(test, test.monthOfYear().getReadablePartial());
        assertSame(test, test.monthOfYear().getYearMonthDay());
        assertEquals(6, test.monthOfYear().get());
        assertEquals("6", test.monthOfYear().getAsString());
        assertEquals("June", test.monthOfYear().getAsText());
        assertEquals("juin", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("Jun", test.monthOfYear().getAsShortText());
        assertEquals("juin", test.monthOfYear().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().months(), test.monthOfYear().getDurationField());
        assertEquals(test.getChronology().years(), test.monthOfYear().getRangeDurationField());
        assertEquals(9, test.monthOfYear().getMaximumTextLength(null));
        assertEquals(3, test.monthOfYear().getMaximumShortTextLength(null));
        test = new YearMonthDay(1972, 7, 9);
        assertEquals("juillet", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("juil.", test.monthOfYear().getAsShortText(Locale.FRENCH));
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyGetMaxMinValuesMonth
    public void testPropertyGetMaxMinValuesMonth() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        assertEquals(1, test.monthOfYear().getMinimumValue());
        assertEquals(1, test.monthOfYear().getMinimumValueOverall());
        assertEquals(12, test.monthOfYear().getMaximumValue());
        assertEquals(12, test.monthOfYear().getMaximumValueOverall());
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyAddMonth
    public void testPropertyAddMonth() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.monthOfYear().addToCopy(6);
        check(test, 1972, 6, 9);
        check(copy, 1972, 12, 9);
        
        copy = test.monthOfYear().addToCopy(7);
        check(copy, 1973, 1, 9);
        
        copy = test.monthOfYear().addToCopy(-5);
        check(copy, 1972, 1, 9);
        
        copy = test.monthOfYear().addToCopy(-6);
        check(copy, 1971, 12, 9);
        
        test = new YearMonthDay(1972, 1, 31);
        copy = test.monthOfYear().addToCopy(1);
        check(copy, 1972, 2, 29);
        
        copy = test.monthOfYear().addToCopy(2);
        check(copy, 1972, 3, 31);
        
        copy = test.monthOfYear().addToCopy(3);
        check(copy, 1972, 4, 30);
        
        test = new YearMonthDay(1971, 1, 31);
        copy = test.monthOfYear().addToCopy(1);
        check(copy, 1971, 2, 28);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyAddWrapFieldMonth
    public void testPropertyAddWrapFieldMonth() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.monthOfYear().addWrapFieldToCopy(4);
        check(test, 1972, 6, 9);
        check(copy, 1972, 10, 9);
        
        copy = test.monthOfYear().addWrapFieldToCopy(8);
        check(copy, 1972, 2, 9);
        
        copy = test.monthOfYear().addWrapFieldToCopy(-8);
        check(copy, 1972, 10, 9);
        
        test = new YearMonthDay(1972, 1, 31);
        copy = test.monthOfYear().addWrapFieldToCopy(1);
        check(copy, 1972, 2, 29);
        
        copy = test.monthOfYear().addWrapFieldToCopy(2);
        check(copy, 1972, 3, 31);
        
        copy = test.monthOfYear().addWrapFieldToCopy(3);
        check(copy, 1972, 4, 30);
        
        test = new YearMonthDay(1971, 1, 31);
        copy = test.monthOfYear().addWrapFieldToCopy(1);
        check(copy, 1971, 2, 28);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertySetMonth
    public void testPropertySetMonth() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.monthOfYear().setCopy(12);
        check(test, 1972, 6, 9);
        check(copy, 1972, 12, 9);
        
        test = new YearMonthDay(1972, 1, 31);
        copy = test.monthOfYear().setCopy(2);
        check(copy, 1972, 2, 29);
        
        try {
            test.monthOfYear().setCopy(13);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.monthOfYear().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertySetTextMonth
    public void testPropertySetTextMonth() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.monthOfYear().setCopy("12");
        check(test, 1972, 6, 9);
        check(copy, 1972, 12, 9);
        
        copy = test.monthOfYear().setCopy("December");
        check(test, 1972, 6, 9);
        check(copy, 1972, 12, 9);
        
        copy = test.monthOfYear().setCopy("Dec");
        check(test, 1972, 6, 9);
        check(copy, 1972, 12, 9);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyCompareToMonth
    public void testPropertyCompareToMonth() {
        YearMonthDay test1 = new YearMonthDay(TEST_TIME1);
        YearMonthDay test2 = new YearMonthDay(TEST_TIME2);
        assertEquals(true, test1.monthOfYear().compareTo(test2) < 0);
        assertEquals(true, test2.monthOfYear().compareTo(test1) > 0);
        assertEquals(true, test1.monthOfYear().compareTo(test1) == 0);
        try {
            test1.monthOfYear().compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.monthOfYear().compareTo(dt2) < 0);
        assertEquals(true, test2.monthOfYear().compareTo(dt1) > 0);
        assertEquals(true, test1.monthOfYear().compareTo(dt1) == 0);
        try {
            test1.monthOfYear().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyGetDay
    public void testPropertyGetDay() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        assertSame(test.getChronology().dayOfMonth(), test.dayOfMonth().getField());
        assertEquals("dayOfMonth", test.dayOfMonth().getName());
        assertEquals("Property[dayOfMonth]", test.dayOfMonth().toString());
        assertSame(test, test.dayOfMonth().getReadablePartial());
        assertSame(test, test.dayOfMonth().getYearMonthDay());
        assertEquals(9, test.dayOfMonth().get());
        assertEquals("9", test.dayOfMonth().getAsString());
        assertEquals("9", test.dayOfMonth().getAsText());
        assertEquals("9", test.dayOfMonth().getAsText(Locale.FRENCH));
        assertEquals("9", test.dayOfMonth().getAsShortText());
        assertEquals("9", test.dayOfMonth().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().days(), test.dayOfMonth().getDurationField());
        assertEquals(test.getChronology().months(), test.dayOfMonth().getRangeDurationField());
        assertEquals(2, test.dayOfMonth().getMaximumTextLength(null));
        assertEquals(2, test.dayOfMonth().getMaximumShortTextLength(null));
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyGetMaxMinValuesDay
    public void testPropertyGetMaxMinValuesDay() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        assertEquals(1, test.dayOfMonth().getMinimumValue());
        assertEquals(1, test.dayOfMonth().getMinimumValueOverall());
        assertEquals(30, test.dayOfMonth().getMaximumValue());
        assertEquals(31, test.dayOfMonth().getMaximumValueOverall());
        test = new YearMonthDay(1972, 7, 9);
        assertEquals(31, test.dayOfMonth().getMaximumValue());
        test = new YearMonthDay(1972, 2, 9);
        assertEquals(29, test.dayOfMonth().getMaximumValue());
        test = new YearMonthDay(1971, 2, 9);
        assertEquals(28, test.dayOfMonth().getMaximumValue());
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyAddDay
    public void testPropertyAddDay() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.dayOfMonth().addToCopy(9);
        check(test, 1972, 6, 9);
        check(copy, 1972, 6, 18);
        
        copy = test.dayOfMonth().addToCopy(21);
        check(copy, 1972, 6, 30);
        
        copy = test.dayOfMonth().addToCopy(22);
        check(copy, 1972, 7, 1);
        
        copy = test.dayOfMonth().addToCopy(22 + 30);
        check(copy, 1972, 7, 31);
        
        copy = test.dayOfMonth().addToCopy(22 + 31);
        check(copy, 1972, 8, 1);

        copy = test.dayOfMonth().addToCopy(21 + 31 + 31 + 30 + 31 + 30 + 31);
        check(copy, 1972, 12, 31);
        
        copy = test.dayOfMonth().addToCopy(22 + 31 + 31 + 30 + 31 + 30 + 31);
        check(copy, 1973, 1, 1);
        
        copy = test.dayOfMonth().addToCopy(-8);
        check(copy, 1972, 6, 1);
        
        copy = test.dayOfMonth().addToCopy(-9);
        check(copy, 1972, 5, 31);
        
        copy = test.dayOfMonth().addToCopy(-8 - 31 - 30 - 31 - 29 - 31);
        check(copy, 1972, 1, 1);
        
        copy = test.dayOfMonth().addToCopy(-9 - 31 - 30 - 31 - 29 - 31);
        check(copy, 1971, 12, 31);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyAddWrapFieldDay
    public void testPropertyAddWrapFieldDay() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.dayOfMonth().addWrapFieldToCopy(21);
        check(test, 1972, 6, 9);
        check(copy, 1972, 6, 30);
        
        copy = test.dayOfMonth().addWrapFieldToCopy(22);
        check(copy, 1972, 6, 1);
        
        copy = test.dayOfMonth().addWrapFieldToCopy(-12);
        check(copy, 1972, 6, 27);
        
        test = new YearMonthDay(1972, 7, 9);
        copy = test.dayOfMonth().addWrapFieldToCopy(21);
        check(copy, 1972, 7, 30);
    
        copy = test.dayOfMonth().addWrapFieldToCopy(22);
        check(copy, 1972, 7, 31);
    
        copy = test.dayOfMonth().addWrapFieldToCopy(23);
        check(copy, 1972, 7, 1);
    
        copy = test.dayOfMonth().addWrapFieldToCopy(-12);
        check(copy, 1972, 7, 28);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertySetDay
    public void testPropertySetDay() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.dayOfMonth().setCopy(12);
        check(test, 1972, 6, 9);
        check(copy, 1972, 6, 12);
        
        try {
            test.dayOfMonth().setCopy(31);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.dayOfMonth().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertySetTextDay
    public void testPropertySetTextDay() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.dayOfMonth().setCopy("12");
        check(test, 1972, 6, 9);
        check(copy, 1972, 6, 12);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyWithMaximumValueDayOfMonth
    public void testPropertyWithMaximumValueDayOfMonth() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.dayOfMonth().withMaximumValue();
        check(test, 1972, 6, 9);
        check(copy, 1972, 6, 30);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyWithMinimumValueDayOfMonth
    public void testPropertyWithMinimumValueDayOfMonth() {
        YearMonthDay test = new YearMonthDay(1972, 6, 9);
        YearMonthDay copy = test.dayOfMonth().withMinimumValue();
        check(test, 1972, 6, 9);
        check(copy, 1972, 6, 1);
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyCompareToDay
    public void testPropertyCompareToDay() {
        YearMonthDay test1 = new YearMonthDay(TEST_TIME1);
        YearMonthDay test2 = new YearMonthDay(TEST_TIME2);
        assertEquals(true, test1.dayOfMonth().compareTo(test2) < 0);
        assertEquals(true, test2.dayOfMonth().compareTo(test1) > 0);
        assertEquals(true, test1.dayOfMonth().compareTo(test1) == 0);
        try {
            test1.dayOfMonth().compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.dayOfMonth().compareTo(dt2) < 0);
        assertEquals(true, test2.dayOfMonth().compareTo(dt1) > 0);
        assertEquals(true, test1.dayOfMonth().compareTo(dt1) == 0);
        try {
            test1.dayOfMonth().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyEquals
    public void testPropertyEquals() {
        YearMonthDay test1 = new YearMonthDay(2005, 11, 8);
        YearMonthDay test2 = new YearMonthDay(2005, 11, 9);
        YearMonthDay test3 = new YearMonthDay(2005, 11, 8, CopticChronology.getInstanceUTC());
        assertEquals(false, test1.dayOfMonth().equals(test1.year()));
        assertEquals(false, test1.dayOfMonth().equals(test1.monthOfYear()));
        assertEquals(true, test1.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(false, test1.dayOfMonth().equals(test2.year()));
        assertEquals(false, test1.dayOfMonth().equals(test2.monthOfYear()));
        assertEquals(false, test1.dayOfMonth().equals(test2.dayOfMonth()));
        
        assertEquals(false, test1.monthOfYear().equals(test1.year()));
        assertEquals(true, test1.monthOfYear().equals(test1.monthOfYear()));
        assertEquals(false, test1.monthOfYear().equals(test1.dayOfMonth()));
        assertEquals(false, test1.monthOfYear().equals(test2.year()));
        assertEquals(true, test1.monthOfYear().equals(test2.monthOfYear()));
        assertEquals(false, test1.monthOfYear().equals(test2.dayOfMonth()));
        
        assertEquals(false, test1.dayOfMonth().equals(null));
        assertEquals(false, test1.dayOfMonth().equals("any"));
        
        
        assertEquals(false, test1.dayOfMonth().equals(test3.dayOfMonth()));
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyHashCode
    public void testPropertyHashCode() {
        YearMonthDay test1 = new YearMonthDay(2005, 11, 8);
        YearMonthDay test2 = new YearMonthDay(2005, 11, 9);
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(false, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.monthOfYear().hashCode() == test1.monthOfYear().hashCode());
        assertEquals(true, test1.monthOfYear().hashCode() == test2.monthOfYear().hashCode());
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyEqualsHashCodeLenient
    public void testPropertyEqualsHashCodeLenient() {
        YearMonthDay test1 = new YearMonthDay(1970, 6, 9, LenientChronology.getInstance(COPTIC_PARIS));
        YearMonthDay test2 = new YearMonthDay(1970, 6, 9, LenientChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(true, test2.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
    }

// org.joda.time.TestYearMonthDay_Properties::testPropertyEqualsHashCodeStrict
    public void testPropertyEqualsHashCodeStrict() {
        YearMonthDay test1 = new YearMonthDay(1970, 6, 9, StrictChronology.getInstance(COPTIC_PARIS));
        YearMonthDay test2 = new YearMonthDay(1970, 6, 9, StrictChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(true, test2.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
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

// org.joda.time.TestYearMonth_Properties::testPropertyGetYear
    public void testPropertyGetYear() {
        YearMonth test = new YearMonth(1972, 6);
        assertSame(test.getChronology().year(), test.year().getField());
        assertEquals("year", test.year().getName());
        assertEquals("Property[year]", test.year().toString());
        assertSame(test, test.year().getReadablePartial());
        assertSame(test, test.year().getYearMonth());
        assertEquals(1972, test.year().get());
        assertEquals("1972", test.year().getAsString());
        assertEquals("1972", test.year().getAsText());
        assertEquals("1972", test.year().getAsText(Locale.FRENCH));
        assertEquals("1972", test.year().getAsShortText());
        assertEquals("1972", test.year().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().years(), test.year().getDurationField());
        assertEquals(null, test.year().getRangeDurationField());
        assertEquals(9, test.year().getMaximumTextLength(null));
        assertEquals(9, test.year().getMaximumShortTextLength(null));
    }

// org.joda.time.TestYearMonth_Properties::testPropertyGetMaxMinValuesYear
    public void testPropertyGetMaxMinValuesYear() {
        YearMonth test = new YearMonth(1972, 6);
        assertEquals(-292275054, test.year().getMinimumValue());
        assertEquals(-292275054, test.year().getMinimumValueOverall());
        assertEquals(292278993, test.year().getMaximumValue());
        assertEquals(292278993, test.year().getMaximumValueOverall());
    }

// org.joda.time.TestYearMonth_Properties::testPropertyAddYear
    public void testPropertyAddYear() {
        YearMonth test = new YearMonth(1972, 6);
        YearMonth copy = test.year().addToCopy(9);
        check(test, 1972, 6);
        check(copy, 1981, 6);
        
        copy = test.year().addToCopy(0);
        check(copy, 1972, 6);
        
        copy = test.year().addToCopy(292277023 - 1972);
        check(copy, 292277023, 6);
        
        try {
            test.year().addToCopy(292278993 - 1972 + 1);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 1972, 6);
        
        copy = test.year().addToCopy(-1972);
        check(copy, 0, 6);
        
        copy = test.year().addToCopy(-1973);
        check(copy, -1, 6);
        
        try {
            test.year().addToCopy(-292275054 - 1972 - 1);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 1972, 6);
    }

// org.joda.time.TestYearMonth_Properties::testPropertyAddWrapFieldYear
    public void testPropertyAddWrapFieldYear() {
        YearMonth test = new YearMonth(1972, 6);
        YearMonth copy = test.year().addWrapFieldToCopy(9);
        check(test, 1972, 6);
        check(copy, 1981, 6);
        
        copy = test.year().addWrapFieldToCopy(0);
        check(copy, 1972, 6);
        
        copy = test.year().addWrapFieldToCopy(292278993 - 1972 + 1);
        check(copy, -292275054, 6);
        
        copy = test.year().addWrapFieldToCopy(-292275054 - 1972 - 1);
        check(copy, 292278993, 6);
    }

// org.joda.time.TestYearMonth_Properties::testPropertySetYear
    public void testPropertySetYear() {
        YearMonth test = new YearMonth(1972, 6);
        YearMonth copy = test.year().setCopy(12);
        check(test, 1972, 6);
        check(copy, 12, 6);
    }

// org.joda.time.TestYearMonth_Properties::testPropertySetTextYear
    public void testPropertySetTextYear() {
        YearMonth test = new YearMonth(1972, 6);
        YearMonth copy = test.year().setCopy("12");
        check(test, 1972, 6);
        check(copy, 12, 6);
    }

// org.joda.time.TestYearMonth_Properties::testPropertyCompareToYear
    public void testPropertyCompareToYear() {
        YearMonth test1 = new YearMonth(TEST_TIME1);
        YearMonth test2 = new YearMonth(TEST_TIME2);
        assertEquals(true, test1.year().compareTo(test2) < 0);
        assertEquals(true, test2.year().compareTo(test1) > 0);
        assertEquals(true, test1.year().compareTo(test1) == 0);
        try {
            test1.year().compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.year().compareTo(dt2) < 0);
        assertEquals(true, test2.year().compareTo(dt1) > 0);
        assertEquals(true, test1.year().compareTo(dt1) == 0);
        try {
            test1.year().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Properties::testPropertyGetMonth
    public void testPropertyGetMonth() {
        YearMonth test = new YearMonth(1972, 6);
        assertSame(test.getChronology().monthOfYear(), test.monthOfYear().getField());
        assertEquals("monthOfYear", test.monthOfYear().getName());
        assertEquals("Property[monthOfYear]", test.monthOfYear().toString());
        assertSame(test, test.monthOfYear().getReadablePartial());
        assertSame(test, test.monthOfYear().getYearMonth());
        assertEquals(6, test.monthOfYear().get());
        assertEquals("6", test.monthOfYear().getAsString());
        assertEquals("June", test.monthOfYear().getAsText());
        assertEquals("juin", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("Jun", test.monthOfYear().getAsShortText());
        assertEquals("juin", test.monthOfYear().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().months(), test.monthOfYear().getDurationField());
        assertEquals(test.getChronology().years(), test.monthOfYear().getRangeDurationField());
        assertEquals(9, test.monthOfYear().getMaximumTextLength(null));
        assertEquals(3, test.monthOfYear().getMaximumShortTextLength(null));
        test = new YearMonth(1972, 7);
        assertEquals("juillet", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("juil.", test.monthOfYear().getAsShortText(Locale.FRENCH));
    }

// org.joda.time.TestYearMonth_Properties::testPropertyGetMaxMinValuesMonth
    public void testPropertyGetMaxMinValuesMonth() {
        YearMonth test = new YearMonth(1972, 6);
        assertEquals(1, test.monthOfYear().getMinimumValue());
        assertEquals(1, test.monthOfYear().getMinimumValueOverall());
        assertEquals(12, test.monthOfYear().getMaximumValue());
        assertEquals(12, test.monthOfYear().getMaximumValueOverall());
    }

// org.joda.time.TestYearMonth_Properties::testPropertyAddMonth
    public void testPropertyAddMonth() {
        YearMonth test = new YearMonth(1972, 6);
        YearMonth copy = test.monthOfYear().addToCopy(6);
        check(test, 1972, 6);
        check(copy, 1972, 12);
        
        copy = test.monthOfYear().addToCopy(7);
        check(copy, 1973, 1);
        
        copy = test.monthOfYear().addToCopy(-5);
        check(copy, 1972, 1);
        
        copy = test.monthOfYear().addToCopy(-6);
        check(copy, 1971, 12);
    }

// org.joda.time.TestYearMonth_Properties::testPropertyAddWrapFieldMonth
    public void testPropertyAddWrapFieldMonth() {
        YearMonth test = new YearMonth(1972, 6);
        YearMonth copy = test.monthOfYear().addWrapFieldToCopy(4);
        check(test, 1972, 6);
        check(copy, 1972, 10);
        
        copy = test.monthOfYear().addWrapFieldToCopy(8);
        check(copy, 1972, 2);
        
        copy = test.monthOfYear().addWrapFieldToCopy(-8);
        check(copy, 1972, 10);
    }

// org.joda.time.TestYearMonth_Properties::testPropertySetMonth
    public void testPropertySetMonth() {
        YearMonth test = new YearMonth(1972, 6);
        YearMonth copy = test.monthOfYear().setCopy(12);
        check(test, 1972, 6);
        check(copy, 1972, 12);
        
        try {
            test.monthOfYear().setCopy(13);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.monthOfYear().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Properties::testPropertySetTextMonth
    public void testPropertySetTextMonth() {
        YearMonth test = new YearMonth(1972, 6);
        YearMonth copy = test.monthOfYear().setCopy("12");
        check(test, 1972, 6);
        check(copy, 1972, 12);
        
        copy = test.monthOfYear().setCopy("December");
        check(test, 1972, 6);
        check(copy, 1972, 12);
        
        copy = test.monthOfYear().setCopy("Dec");
        check(test, 1972, 6);
        check(copy, 1972, 12);
    }

// org.joda.time.TestYearMonth_Properties::testPropertyCompareToMonth
    public void testPropertyCompareToMonth() {
        YearMonth test1 = new YearMonth(TEST_TIME1);
        YearMonth test2 = new YearMonth(TEST_TIME2);
        assertEquals(true, test1.monthOfYear().compareTo(test2) < 0);
        assertEquals(true, test2.monthOfYear().compareTo(test1) > 0);
        assertEquals(true, test1.monthOfYear().compareTo(test1) == 0);
        try {
            test1.monthOfYear().compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.monthOfYear().compareTo(dt2) < 0);
        assertEquals(true, test2.monthOfYear().compareTo(dt1) > 0);
        assertEquals(true, test1.monthOfYear().compareTo(dt1) == 0);
        try {
            test1.monthOfYear().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Properties::testPropertyEquals
    public void testPropertyEquals() {
        YearMonth test1 = new YearMonth(11, 11);
        YearMonth test2 = new YearMonth(11, 12);
        YearMonth test3 = new YearMonth(11, 11, CopticChronology.getInstanceUTC());
        assertEquals(true, test1.monthOfYear().equals(test1.monthOfYear()));
        assertEquals(false, test1.monthOfYear().equals(test1.year()));
        assertEquals(false, test1.monthOfYear().equals(test2.monthOfYear()));
        assertEquals(false, test1.monthOfYear().equals(test2.year()));
        
        assertEquals(false, test1.year().equals(test1.monthOfYear()));
        assertEquals(true, test1.year().equals(test1.year()));
        assertEquals(false, test1.year().equals(test2.monthOfYear()));
        assertEquals(true, test1.year().equals(test2.year()));
        
        assertEquals(false, test1.monthOfYear().equals(null));
        assertEquals(false, test1.monthOfYear().equals("any"));
        
        
        assertEquals(false, test1.monthOfYear().equals(test3.monthOfYear()));
    }

// org.joda.time.TestYearMonth_Properties::testPropertyHashCode
    public void testPropertyHashCode() {
        YearMonth test1 = new YearMonth(2005, 11);
        YearMonth test2 = new YearMonth(2005, 12);
        assertEquals(true, test1.monthOfYear().hashCode() == test1.monthOfYear().hashCode());
        assertEquals(false, test1.monthOfYear().hashCode() == test2.monthOfYear().hashCode());
        assertEquals(true, test1.year().hashCode() == test1.year().hashCode());
        assertEquals(true, test1.year().hashCode() == test2.year().hashCode());
    }

// org.joda.time.TestYearMonth_Properties::testPropertyEqualsHashCodeLenient
    public void testPropertyEqualsHashCodeLenient() {
        YearMonth test1 = new YearMonth(1970, 6, LenientChronology.getInstance(COPTIC_PARIS));
        YearMonth test2 = new YearMonth(1970, 6, LenientChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.monthOfYear().equals(test2.monthOfYear()));
        assertEquals(true, test2.monthOfYear().equals(test1.monthOfYear()));
        assertEquals(true, test1.monthOfYear().equals(test1.monthOfYear()));
        assertEquals(true, test2.monthOfYear().equals(test2.monthOfYear()));
        assertEquals(true, test1.monthOfYear().hashCode() == test2.monthOfYear().hashCode());
        assertEquals(true, test1.monthOfYear().hashCode() == test1.monthOfYear().hashCode());
        assertEquals(true, test2.monthOfYear().hashCode() == test2.monthOfYear().hashCode());
    }

// org.joda.time.TestYearMonth_Properties::testPropertyEqualsHashCodeStrict
    public void testPropertyEqualsHashCodeStrict() {
        YearMonth test1 = new YearMonth(1970, 6, StrictChronology.getInstance(COPTIC_PARIS));
        YearMonth test2 = new YearMonth(1970, 6, StrictChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.monthOfYear().equals(test2.monthOfYear()));
        assertEquals(true, test2.monthOfYear().equals(test1.monthOfYear()));
        assertEquals(true, test1.monthOfYear().equals(test1.monthOfYear()));
        assertEquals(true, test2.monthOfYear().equals(test2.monthOfYear()));
        assertEquals(true, test1.monthOfYear().hashCode() == test2.monthOfYear().hashCode());
        assertEquals(true, test1.monthOfYear().hashCode() == test1.monthOfYear().hashCode());
        assertEquals(true, test2.monthOfYear().hashCode() == test2.monthOfYear().hashCode());
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

// org.joda.time.TestYears::testPlus_int
    public void testPlus_int() {
        Years test2 = Years.years(2);
        Years result = test2.plus(3);
        assertEquals(2, test2.getYears());
        assertEquals(5, result.getYears());
        
        assertEquals(1, Years.ONE.plus(0).getYears());
        
        try {
            Years.MAX_VALUE.plus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestYears::testPlus_Years
    public void testPlus_Years() {
        Years test2 = Years.years(2);
        Years test3 = Years.years(3);
        Years result = test2.plus(test3);
        assertEquals(2, test2.getYears());
        assertEquals(3, test3.getYears());
        assertEquals(5, result.getYears());
        
        assertEquals(1, Years.ONE.plus(Years.ZERO).getYears());
        assertEquals(1, Years.ONE.plus((Years) null).getYears());
        
        try {
            Years.MAX_VALUE.plus(Years.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestYears::testMinus_int
    public void testMinus_int() {
        Years test2 = Years.years(2);
        Years result = test2.minus(3);
        assertEquals(2, test2.getYears());
        assertEquals(-1, result.getYears());
        
        assertEquals(1, Years.ONE.minus(0).getYears());
        
        try {
            Years.MIN_VALUE.minus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestYears::testMinus_Years
    public void testMinus_Years() {
        Years test2 = Years.years(2);
        Years test3 = Years.years(3);
        Years result = test2.minus(test3);
        assertEquals(2, test2.getYears());
        assertEquals(3, test3.getYears());
        assertEquals(-1, result.getYears());
        
        assertEquals(1, Years.ONE.minus(Years.ZERO).getYears());
        assertEquals(1, Years.ONE.minus((Years) null).getYears());
        
        try {
            Years.MIN_VALUE.minus(Years.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestYears::testMultipliedBy_int
    public void testMultipliedBy_int() {
        Years test = Years.years(2);
        assertEquals(6, test.multipliedBy(3).getYears());
        assertEquals(2, test.getYears());
        assertEquals(-6, test.multipliedBy(-3).getYears());
        assertSame(test, test.multipliedBy(1));
        
        Years halfMax = Years.years(Integer.MAX_VALUE / 2 + 1);
        try {
            halfMax.multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestYears::testDividedBy_int
    public void testDividedBy_int() {
        Years test = Years.years(12);
        assertEquals(6, test.dividedBy(2).getYears());
        assertEquals(12, test.getYears());
        assertEquals(4, test.dividedBy(3).getYears());
        assertEquals(3, test.dividedBy(4).getYears());
        assertEquals(2, test.dividedBy(5).getYears());
        assertEquals(2, test.dividedBy(6).getYears());
        assertSame(test, test.dividedBy(1));
        
        try {
            Years.ONE.dividedBy(0);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestYears::testNegated
    public void testNegated() {
        Years test = Years.years(12);
        assertEquals(-12, test.negated().getYears());
        assertEquals(12, test.getYears());
        
        try {
            Years.MIN_VALUE.negated();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestYears::testAddToLocalDate
    public void testAddToLocalDate() {
        Years test = Years.years(3);
        LocalDate date = new LocalDate(2006, 6, 1);
        LocalDate expected = new LocalDate(2009, 6, 1);
        assertEquals(expected, date.plus(test));
    }

// org.joda.time.chrono.TestBuddhistChronology::testFactoryUTC
    public void testFactoryUTC() {
        assertEquals(DateTimeZone.UTC, BuddhistChronology.getInstanceUTC().getZone());
        assertSame(BuddhistChronology.class, BuddhistChronology.getInstanceUTC().getClass());
    }

// org.joda.time.chrono.TestBuddhistChronology::testFactory
    public void testFactory() {
        assertEquals(LONDON, BuddhistChronology.getInstance().getZone());
        assertSame(BuddhistChronology.class, BuddhistChronology.getInstance().getClass());
    }

// org.joda.time.chrono.TestBuddhistChronology::testFactory_Zone
    public void testFactory_Zone() {
        assertEquals(TOKYO, BuddhistChronology.getInstance(TOKYO).getZone());
        assertEquals(PARIS, BuddhistChronology.getInstance(PARIS).getZone());
        assertEquals(LONDON, BuddhistChronology.getInstance(null).getZone());
        assertSame(BuddhistChronology.class, BuddhistChronology.getInstance(TOKYO).getClass());
    }

// org.joda.time.chrono.TestBuddhistChronology::testEquality
    public void testEquality() {
        assertSame(BuddhistChronology.getInstance(TOKYO), BuddhistChronology.getInstance(TOKYO));
        assertSame(BuddhistChronology.getInstance(LONDON), BuddhistChronology.getInstance(LONDON));
        assertSame(BuddhistChronology.getInstance(PARIS), BuddhistChronology.getInstance(PARIS));
        assertSame(BuddhistChronology.getInstanceUTC(), BuddhistChronology.getInstanceUTC());
        assertSame(BuddhistChronology.getInstance(), BuddhistChronology.getInstance(LONDON));
    }

// org.joda.time.chrono.TestBuddhistChronology::testWithUTC
    public void testWithUTC() {
        assertSame(BuddhistChronology.getInstanceUTC(), BuddhistChronology.getInstance(LONDON).withUTC());
        assertSame(BuddhistChronology.getInstanceUTC(), BuddhistChronology.getInstance(TOKYO).withUTC());
        assertSame(BuddhistChronology.getInstanceUTC(), BuddhistChronology.getInstanceUTC().withUTC());
        assertSame(BuddhistChronology.getInstanceUTC(), BuddhistChronology.getInstance().withUTC());
    }

// org.joda.time.chrono.TestBuddhistChronology::testWithZone
    public void testWithZone() {
        assertSame(BuddhistChronology.getInstance(TOKYO), BuddhistChronology.getInstance(TOKYO).withZone(TOKYO));
        assertSame(BuddhistChronology.getInstance(LONDON), BuddhistChronology.getInstance(TOKYO).withZone(LONDON));
        assertSame(BuddhistChronology.getInstance(PARIS), BuddhistChronology.getInstance(TOKYO).withZone(PARIS));
        assertSame(BuddhistChronology.getInstance(LONDON), BuddhistChronology.getInstance(TOKYO).withZone(null));
        assertSame(BuddhistChronology.getInstance(PARIS), BuddhistChronology.getInstance().withZone(PARIS));
        assertSame(BuddhistChronology.getInstance(PARIS), BuddhistChronology.getInstanceUTC().withZone(PARIS));
    }

// org.joda.time.chrono.TestBuddhistChronology::testToString
    public void testToString() {
        assertEquals("BuddhistChronology[Europe/London]", BuddhistChronology.getInstance(LONDON).toString());
        assertEquals("BuddhistChronology[Asia/Tokyo]", BuddhistChronology.getInstance(TOKYO).toString());
        assertEquals("BuddhistChronology[Europe/London]", BuddhistChronology.getInstance().toString());
        assertEquals("BuddhistChronology[UTC]", BuddhistChronology.getInstanceUTC().toString());
    }

// org.joda.time.chrono.TestBuddhistChronology::testDurationFields
    public void testDurationFields() {
        final BuddhistChronology buddhist = BuddhistChronology.getInstance();
        assertEquals("eras", buddhist.eras().getName());
        assertEquals("centuries", buddhist.centuries().getName());
        assertEquals("years", buddhist.years().getName());
        assertEquals("weekyears", buddhist.weekyears().getName());
        assertEquals("months", buddhist.months().getName());
        assertEquals("weeks", buddhist.weeks().getName());
        assertEquals("days", buddhist.days().getName());
        assertEquals("halfdays", GregorianChronology.getInstance().halfdays().getName());
        assertEquals("hours", buddhist.hours().getName());
        assertEquals("minutes", buddhist.minutes().getName());
        assertEquals("seconds", buddhist.seconds().getName());
        assertEquals("millis", buddhist.millis().getName());
        
        assertEquals(false, buddhist.eras().isSupported());
        assertEquals(true, buddhist.centuries().isSupported());
        assertEquals(true, buddhist.years().isSupported());
        assertEquals(true, buddhist.weekyears().isSupported());
        assertEquals(true, buddhist.months().isSupported());
        assertEquals(true, buddhist.weeks().isSupported());
        assertEquals(true, buddhist.days().isSupported());
        assertEquals(true, buddhist.halfdays().isSupported());
        assertEquals(true, buddhist.hours().isSupported());
        assertEquals(true, buddhist.minutes().isSupported());
        assertEquals(true, buddhist.seconds().isSupported());
        assertEquals(true, buddhist.millis().isSupported());
        
        assertEquals(false, buddhist.centuries().isPrecise());
        assertEquals(false, buddhist.years().isPrecise());
        assertEquals(false, buddhist.weekyears().isPrecise());
        assertEquals(false, buddhist.months().isPrecise());
        assertEquals(false, buddhist.weeks().isPrecise());
        assertEquals(false, buddhist.days().isPrecise());
        assertEquals(false, buddhist.halfdays().isPrecise());
        assertEquals(true, buddhist.hours().isPrecise());
        assertEquals(true, buddhist.minutes().isPrecise());
        assertEquals(true, buddhist.seconds().isPrecise());
        assertEquals(true, buddhist.millis().isPrecise());
        
        final BuddhistChronology buddhistUTC = BuddhistChronology.getInstanceUTC();
        assertEquals(false, buddhistUTC.centuries().isPrecise());
        assertEquals(false, buddhistUTC.years().isPrecise());
        assertEquals(false, buddhistUTC.weekyears().isPrecise());
        assertEquals(false, buddhistUTC.months().isPrecise());
        assertEquals(true, buddhistUTC.weeks().isPrecise());
        assertEquals(true, buddhistUTC.days().isPrecise());
        assertEquals(true, buddhistUTC.halfdays().isPrecise());
        assertEquals(true, buddhistUTC.hours().isPrecise());
        assertEquals(true, buddhistUTC.minutes().isPrecise());
        assertEquals(true, buddhistUTC.seconds().isPrecise());
        assertEquals(true, buddhistUTC.millis().isPrecise());
        
        final DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        final BuddhistChronology buddhistGMT = BuddhistChronology.getInstance(gmt);
        assertEquals(false, buddhistGMT.centuries().isPrecise());
        assertEquals(false, buddhistGMT.years().isPrecise());
        assertEquals(false, buddhistGMT.weekyears().isPrecise());
        assertEquals(false, buddhistGMT.months().isPrecise());
        assertEquals(true, buddhistGMT.weeks().isPrecise());
        assertEquals(true, buddhistGMT.days().isPrecise());
        assertEquals(true, buddhistGMT.halfdays().isPrecise());
        assertEquals(true, buddhistGMT.hours().isPrecise());
        assertEquals(true, buddhistGMT.minutes().isPrecise());
        assertEquals(true, buddhistGMT.seconds().isPrecise());
        assertEquals(true, buddhistGMT.millis().isPrecise());
    }

// org.joda.time.chrono.TestBuddhistChronology::testDateFields
    public void testDateFields() {
        final BuddhistChronology buddhist = BuddhistChronology.getInstance();
        assertEquals("era", buddhist.era().getName());
        assertEquals("centuryOfEra", buddhist.centuryOfEra().getName());
        assertEquals("yearOfCentury", buddhist.yearOfCentury().getName());
        assertEquals("yearOfEra", buddhist.yearOfEra().getName());
        assertEquals("year", buddhist.year().getName());
        assertEquals("monthOfYear", buddhist.monthOfYear().getName());
        assertEquals("weekyearOfCentury", buddhist.weekyearOfCentury().getName());
        assertEquals("weekyear", buddhist.weekyear().getName());
        assertEquals("weekOfWeekyear", buddhist.weekOfWeekyear().getName());
        assertEquals("dayOfYear", buddhist.dayOfYear().getName());
        assertEquals("dayOfMonth", buddhist.dayOfMonth().getName());
        assertEquals("dayOfWeek", buddhist.dayOfWeek().getName());
        
        assertEquals(true, buddhist.era().isSupported());
        assertEquals(true, buddhist.centuryOfEra().isSupported());
        assertEquals(true, buddhist.yearOfCentury().isSupported());
        assertEquals(true, buddhist.yearOfEra().isSupported());
        assertEquals(true, buddhist.year().isSupported());
        assertEquals(true, buddhist.monthOfYear().isSupported());
        assertEquals(true, buddhist.weekyearOfCentury().isSupported());
        assertEquals(true, buddhist.weekyear().isSupported());
        assertEquals(true, buddhist.weekOfWeekyear().isSupported());
        assertEquals(true, buddhist.dayOfYear().isSupported());
        assertEquals(true, buddhist.dayOfMonth().isSupported());
        assertEquals(true, buddhist.dayOfWeek().isSupported());
        
        assertEquals(buddhist.eras(), buddhist.era().getDurationField());
        assertEquals(buddhist.centuries(), buddhist.centuryOfEra().getDurationField());
        assertEquals(buddhist.years(), buddhist.yearOfCentury().getDurationField());
        assertEquals(buddhist.years(), buddhist.yearOfEra().getDurationField());
        assertEquals(buddhist.years(), buddhist.year().getDurationField());
        assertEquals(buddhist.months(), buddhist.monthOfYear().getDurationField());
        assertEquals(buddhist.weekyears(), buddhist.weekyearOfCentury().getDurationField());
        assertEquals(buddhist.weekyears(), buddhist.weekyear().getDurationField());
        assertEquals(buddhist.weeks(), buddhist.weekOfWeekyear().getDurationField());
        assertEquals(buddhist.days(), buddhist.dayOfYear().getDurationField());
        assertEquals(buddhist.days(), buddhist.dayOfMonth().getDurationField());
        assertEquals(buddhist.days(), buddhist.dayOfWeek().getDurationField());
        
        assertEquals(null, buddhist.era().getRangeDurationField());
        assertEquals(buddhist.eras(), buddhist.centuryOfEra().getRangeDurationField());
        assertEquals(buddhist.centuries(), buddhist.yearOfCentury().getRangeDurationField());
        assertEquals(buddhist.eras(), buddhist.yearOfEra().getRangeDurationField());
        assertEquals(null, buddhist.year().getRangeDurationField());
        assertEquals(buddhist.years(), buddhist.monthOfYear().getRangeDurationField());
        assertEquals(buddhist.centuries(), buddhist.weekyearOfCentury().getRangeDurationField());
        assertEquals(null, buddhist.weekyear().getRangeDurationField());
        assertEquals(buddhist.weekyears(), buddhist.weekOfWeekyear().getRangeDurationField());
        assertEquals(buddhist.years(), buddhist.dayOfYear().getRangeDurationField());
        assertEquals(buddhist.months(), buddhist.dayOfMonth().getRangeDurationField());
        assertEquals(buddhist.weeks(), buddhist.dayOfWeek().getRangeDurationField());
    }

// org.joda.time.chrono.TestBuddhistChronology::testTimeFields
    public void testTimeFields() {
        final BuddhistChronology buddhist = BuddhistChronology.getInstance();
        assertEquals("halfdayOfDay", buddhist.halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", buddhist.clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", buddhist.hourOfHalfday().getName());
        assertEquals("clockhourOfDay", buddhist.clockhourOfDay().getName());
        assertEquals("hourOfDay", buddhist.hourOfDay().getName());
        assertEquals("minuteOfDay", buddhist.minuteOfDay().getName());
        assertEquals("minuteOfHour", buddhist.minuteOfHour().getName());
        assertEquals("secondOfDay", buddhist.secondOfDay().getName());
        assertEquals("secondOfMinute", buddhist.secondOfMinute().getName());
        assertEquals("millisOfDay", buddhist.millisOfDay().getName());
        assertEquals("millisOfSecond", buddhist.millisOfSecond().getName());
        
        assertEquals(true, buddhist.halfdayOfDay().isSupported());
        assertEquals(true, buddhist.clockhourOfHalfday().isSupported());
        assertEquals(true, buddhist.hourOfHalfday().isSupported());
        assertEquals(true, buddhist.clockhourOfDay().isSupported());
        assertEquals(true, buddhist.hourOfDay().isSupported());
        assertEquals(true, buddhist.minuteOfDay().isSupported());
        assertEquals(true, buddhist.minuteOfHour().isSupported());
        assertEquals(true, buddhist.secondOfDay().isSupported());
        assertEquals(true, buddhist.secondOfMinute().isSupported());
        assertEquals(true, buddhist.millisOfDay().isSupported());
        assertEquals(true, buddhist.millisOfSecond().isSupported());
    }

// org.joda.time.chrono.TestBuddhistChronology::testEpoch
    public void testEpoch() {
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, BUDDHIST_UTC);
        assertEquals(new DateTime(-543, 1, 1, 0, 0, 0, 0, JULIAN_UTC), epoch.withChronology(JULIAN_UTC));
    }

// org.joda.time.chrono.TestBuddhistChronology::testEra
    public void testEra() {
        assertEquals(1, BuddhistChronology.BE);
        try {
            new DateTime(-1, 13, 5, 0, 0, 0, 0, BUDDHIST_UTC);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.chrono.TestBuddhistChronology::testKeyYears
    public void testKeyYears() {
        DateTime bd = new DateTime(2513, 1, 1, 0, 0, 0, 0, BUDDHIST_UTC);
        DateTime jd = new DateTime(1970, 1, 1, 0, 0, 0, 0, GJ_UTC);
        assertEquals(jd, bd.withChronology(GJ_UTC));
        assertEquals(2513, bd.getYear());
        assertEquals(2513, bd.getYearOfEra());
        assertEquals(2513, bd.plus(Period.weeks(1)).getWeekyear());
        
        bd = new DateTime(2126, 1, 1, 0, 0, 0, 0, BUDDHIST_UTC);
        jd = new DateTime(1583, 1, 1, 0, 0, 0, 0, GJ_UTC);
        assertEquals(jd, bd.withChronology(GJ_UTC));
        assertEquals(2126, bd.getYear());
        assertEquals(2126, bd.getYearOfEra());
        assertEquals(2126, bd.plus(Period.weeks(1)).getWeekyear());
        
        bd = new DateTime(2125, 1, 1, 0, 0, 0, 0, BUDDHIST_UTC);
        jd = new DateTime(1582, 1, 1, 0, 0, 0, 0, GJ_UTC);
        assertEquals(jd, bd.withChronology(GJ_UTC));
        assertEquals(2125, bd.getYear());
        assertEquals(2125, bd.getYearOfEra());
        assertEquals(2125, bd.plus(Period.weeks(1)).getWeekyear());
        
        bd = new DateTime(544, 1, 1, 0, 0, 0, 0, BUDDHIST_UTC);
        jd = new DateTime(1, 1, 1, 0, 0, 0, 0, GJ_UTC);
        assertEquals(jd, bd.withChronology(GJ_UTC));
        assertEquals(544, bd.getYear());
        assertEquals(544, bd.getYearOfEra());
        assertEquals(544, bd.plus(Period.weeks(1)).getWeekyear());
        
        bd = new DateTime(543, 1, 1, 0, 0, 0, 0, BUDDHIST_UTC);
        jd = new DateTime(-1, 1, 1, 0, 0, 0, 0, GJ_UTC);
        assertEquals(jd, bd.withChronology(GJ_UTC));
        assertEquals(543, bd.getYear());
        assertEquals(543, bd.getYearOfEra());
        assertEquals(543, bd.plus(Period.weeks(1)).getWeekyear());
        
        bd = new DateTime(1, 1, 1, 0, 0, 0, 0, BUDDHIST_UTC);
        jd = new DateTime(-543, 1, 1, 0, 0, 0, 0, GJ_UTC);
        assertEquals(jd, bd.withChronology(GJ_UTC));
        assertEquals(1, bd.getYear());
        assertEquals(1, bd.getYearOfEra());
        assertEquals(1, bd.plus(Period.weeks(1)).getWeekyear());
    }

// org.joda.time.chrono.TestBuddhistChronology::testCalendar
    public void testCalendar() {
        if (TestAll.FAST) {
            return;
        }
        System.out.println("\nTestBuddhistChronology.testCalendar");
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, BUDDHIST_UTC);
        long millis = epoch.getMillis();
        long end = new DateTime(3000, 1, 1, 0, 0, 0, 0, ISO_UTC).getMillis();
        DateTimeField dayOfWeek = BUDDHIST_UTC.dayOfWeek();
        DateTimeField weekOfWeekyear = GJ_UTC.weekOfWeekyear();
        DateTimeField dayOfYear = BUDDHIST_UTC.dayOfYear();
        DateTimeField dayOfMonth = BUDDHIST_UTC.dayOfMonth();
        DateTimeField monthOfYear = BUDDHIST_UTC.monthOfYear();
        DateTimeField year = BUDDHIST_UTC.year();
        DateTimeField yearOfEra = BUDDHIST_UTC.yearOfEra();
        DateTimeField era = BUDDHIST_UTC.era();
        DateTimeField gjDayOfWeek = GJ_UTC.dayOfWeek();
        DateTimeField gjWeekOfWeekyear = GJ_UTC.weekOfWeekyear();
        DateTimeField gjDayOfYear = GJ_UTC.dayOfYear();
        DateTimeField gjDayOfMonth = GJ_UTC.dayOfMonth();
        DateTimeField gjMonthOfYear = GJ_UTC.monthOfYear();
        DateTimeField gjYear = GJ_UTC.year();
        while (millis < end) {
            assertEquals(gjDayOfWeek.get(millis), dayOfWeek.get(millis));
            assertEquals(gjDayOfYear.get(millis), dayOfYear.get(millis));
            assertEquals(gjDayOfMonth.get(millis), dayOfMonth.get(millis));
            assertEquals(gjMonthOfYear.get(millis), monthOfYear.get(millis));
            assertEquals(gjWeekOfWeekyear.get(millis), weekOfWeekyear.get(millis));
            assertEquals(1, era.get(millis));
            int yearValue = gjYear.get(millis);
            if (yearValue <= 0) {
                yearValue++;
            }
            yearValue += 543;
            assertEquals(yearValue, year.get(millis));
            assertEquals(yearValue, yearOfEra.get(millis));
            millis += SKIP;
        }
    }

// org.joda.time.chrono.TestCopticChronology::testFactoryUTC
    public void testFactoryUTC() {
        assertEquals(DateTimeZone.UTC, CopticChronology.getInstanceUTC().getZone());
        assertSame(CopticChronology.class, CopticChronology.getInstanceUTC().getClass());
    }

// org.joda.time.chrono.TestCopticChronology::testFactory
    public void testFactory() {
        assertEquals(LONDON, CopticChronology.getInstance().getZone());
        assertSame(CopticChronology.class, CopticChronology.getInstance().getClass());
    }

// org.joda.time.chrono.TestCopticChronology::testFactory_Zone
    public void testFactory_Zone() {
        assertEquals(TOKYO, CopticChronology.getInstance(TOKYO).getZone());
        assertEquals(PARIS, CopticChronology.getInstance(PARIS).getZone());
        assertEquals(LONDON, CopticChronology.getInstance(null).getZone());
        assertSame(CopticChronology.class, CopticChronology.getInstance(TOKYO).getClass());
    }

// org.joda.time.chrono.TestCopticChronology::testEquality
    public void testEquality() {
        assertSame(CopticChronology.getInstance(TOKYO), CopticChronology.getInstance(TOKYO));
        assertSame(CopticChronology.getInstance(LONDON), CopticChronology.getInstance(LONDON));
        assertSame(CopticChronology.getInstance(PARIS), CopticChronology.getInstance(PARIS));
        assertSame(CopticChronology.getInstanceUTC(), CopticChronology.getInstanceUTC());
        assertSame(CopticChronology.getInstance(), CopticChronology.getInstance(LONDON));
    }

// org.joda.time.chrono.TestCopticChronology::testWithUTC
    public void testWithUTC() {
        assertSame(CopticChronology.getInstanceUTC(), CopticChronology.getInstance(LONDON).withUTC());
        assertSame(CopticChronology.getInstanceUTC(), CopticChronology.getInstance(TOKYO).withUTC());
        assertSame(CopticChronology.getInstanceUTC(), CopticChronology.getInstanceUTC().withUTC());
        assertSame(CopticChronology.getInstanceUTC(), CopticChronology.getInstance().withUTC());
    }

// org.joda.time.chrono.TestCopticChronology::testWithZone
    public void testWithZone() {
        assertSame(CopticChronology.getInstance(TOKYO), CopticChronology.getInstance(TOKYO).withZone(TOKYO));
        assertSame(CopticChronology.getInstance(LONDON), CopticChronology.getInstance(TOKYO).withZone(LONDON));
        assertSame(CopticChronology.getInstance(PARIS), CopticChronology.getInstance(TOKYO).withZone(PARIS));
        assertSame(CopticChronology.getInstance(LONDON), CopticChronology.getInstance(TOKYO).withZone(null));
        assertSame(CopticChronology.getInstance(PARIS), CopticChronology.getInstance().withZone(PARIS));
        assertSame(CopticChronology.getInstance(PARIS), CopticChronology.getInstanceUTC().withZone(PARIS));
    }

// org.joda.time.chrono.TestCopticChronology::testToString
    public void testToString() {
        assertEquals("CopticChronology[Europe/London]", CopticChronology.getInstance(LONDON).toString());
        assertEquals("CopticChronology[Asia/Tokyo]", CopticChronology.getInstance(TOKYO).toString());
        assertEquals("CopticChronology[Europe/London]", CopticChronology.getInstance().toString());
        assertEquals("CopticChronology[UTC]", CopticChronology.getInstanceUTC().toString());
    }

// org.joda.time.chrono.TestCopticChronology::testDurationFields
    public void testDurationFields() {
        final CopticChronology coptic = CopticChronology.getInstance();
        assertEquals("eras", coptic.eras().getName());
        assertEquals("centuries", coptic.centuries().getName());
        assertEquals("years", coptic.years().getName());
        assertEquals("weekyears", coptic.weekyears().getName());
        assertEquals("months", coptic.months().getName());
        assertEquals("weeks", coptic.weeks().getName());
        assertEquals("days", coptic.days().getName());
        assertEquals("halfdays", coptic.halfdays().getName());
        assertEquals("hours", coptic.hours().getName());
        assertEquals("minutes", coptic.minutes().getName());
        assertEquals("seconds", coptic.seconds().getName());
        assertEquals("millis", coptic.millis().getName());
        
        assertEquals(false, coptic.eras().isSupported());
        assertEquals(true, coptic.centuries().isSupported());
        assertEquals(true, coptic.years().isSupported());
        assertEquals(true, coptic.weekyears().isSupported());
        assertEquals(true, coptic.months().isSupported());
        assertEquals(true, coptic.weeks().isSupported());
        assertEquals(true, coptic.days().isSupported());
        assertEquals(true, coptic.halfdays().isSupported());
        assertEquals(true, coptic.hours().isSupported());
        assertEquals(true, coptic.minutes().isSupported());
        assertEquals(true, coptic.seconds().isSupported());
        assertEquals(true, coptic.millis().isSupported());
        
        assertEquals(false, coptic.centuries().isPrecise());
        assertEquals(false, coptic.years().isPrecise());
        assertEquals(false, coptic.weekyears().isPrecise());
        assertEquals(false, coptic.months().isPrecise());
        assertEquals(false, coptic.weeks().isPrecise());
        assertEquals(false, coptic.days().isPrecise());
        assertEquals(false, coptic.halfdays().isPrecise());
        assertEquals(true, coptic.hours().isPrecise());
        assertEquals(true, coptic.minutes().isPrecise());
        assertEquals(true, coptic.seconds().isPrecise());
        assertEquals(true, coptic.millis().isPrecise());
        
        final CopticChronology copticUTC = CopticChronology.getInstanceUTC();
        assertEquals(false, copticUTC.centuries().isPrecise());
        assertEquals(false, copticUTC.years().isPrecise());
        assertEquals(false, copticUTC.weekyears().isPrecise());
        assertEquals(false, copticUTC.months().isPrecise());
        assertEquals(true, copticUTC.weeks().isPrecise());
        assertEquals(true, copticUTC.days().isPrecise());
        assertEquals(true, copticUTC.halfdays().isPrecise());
        assertEquals(true, copticUTC.hours().isPrecise());
        assertEquals(true, copticUTC.minutes().isPrecise());
        assertEquals(true, copticUTC.seconds().isPrecise());
        assertEquals(true, copticUTC.millis().isPrecise());
        
        final DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        final CopticChronology copticGMT = CopticChronology.getInstance(gmt);
        assertEquals(false, copticGMT.centuries().isPrecise());
        assertEquals(false, copticGMT.years().isPrecise());
        assertEquals(false, copticGMT.weekyears().isPrecise());
        assertEquals(false, copticGMT.months().isPrecise());
        assertEquals(true, copticGMT.weeks().isPrecise());
        assertEquals(true, copticGMT.days().isPrecise());
        assertEquals(true, copticGMT.halfdays().isPrecise());
        assertEquals(true, copticGMT.hours().isPrecise());
        assertEquals(true, copticGMT.minutes().isPrecise());
        assertEquals(true, copticGMT.seconds().isPrecise());
        assertEquals(true, copticGMT.millis().isPrecise());
    }

// org.joda.time.chrono.TestCopticChronology::testDateFields
    public void testDateFields() {
        final CopticChronology coptic = CopticChronology.getInstance();
        assertEquals("era", coptic.era().getName());
        assertEquals("centuryOfEra", coptic.centuryOfEra().getName());
        assertEquals("yearOfCentury", coptic.yearOfCentury().getName());
        assertEquals("yearOfEra", coptic.yearOfEra().getName());
        assertEquals("year", coptic.year().getName());
        assertEquals("monthOfYear", coptic.monthOfYear().getName());
        assertEquals("weekyearOfCentury", coptic.weekyearOfCentury().getName());
        assertEquals("weekyear", coptic.weekyear().getName());
        assertEquals("weekOfWeekyear", coptic.weekOfWeekyear().getName());
        assertEquals("dayOfYear", coptic.dayOfYear().getName());
        assertEquals("dayOfMonth", coptic.dayOfMonth().getName());
        assertEquals("dayOfWeek", coptic.dayOfWeek().getName());
        
        assertEquals(true, coptic.era().isSupported());
        assertEquals(true, coptic.centuryOfEra().isSupported());
        assertEquals(true, coptic.yearOfCentury().isSupported());
        assertEquals(true, coptic.yearOfEra().isSupported());
        assertEquals(true, coptic.year().isSupported());
        assertEquals(true, coptic.monthOfYear().isSupported());
        assertEquals(true, coptic.weekyearOfCentury().isSupported());
        assertEquals(true, coptic.weekyear().isSupported());
        assertEquals(true, coptic.weekOfWeekyear().isSupported());
        assertEquals(true, coptic.dayOfYear().isSupported());
        assertEquals(true, coptic.dayOfMonth().isSupported());
        assertEquals(true, coptic.dayOfWeek().isSupported());
        
        assertEquals(coptic.eras(), coptic.era().getDurationField());
        assertEquals(coptic.centuries(), coptic.centuryOfEra().getDurationField());
        assertEquals(coptic.years(), coptic.yearOfCentury().getDurationField());
        assertEquals(coptic.years(), coptic.yearOfEra().getDurationField());
        assertEquals(coptic.years(), coptic.year().getDurationField());
        assertEquals(coptic.months(), coptic.monthOfYear().getDurationField());
        assertEquals(coptic.weekyears(), coptic.weekyearOfCentury().getDurationField());
        assertEquals(coptic.weekyears(), coptic.weekyear().getDurationField());
        assertEquals(coptic.weeks(), coptic.weekOfWeekyear().getDurationField());
        assertEquals(coptic.days(), coptic.dayOfYear().getDurationField());
        assertEquals(coptic.days(), coptic.dayOfMonth().getDurationField());
        assertEquals(coptic.days(), coptic.dayOfWeek().getDurationField());
        
        assertEquals(null, coptic.era().getRangeDurationField());
        assertEquals(coptic.eras(), coptic.centuryOfEra().getRangeDurationField());
        assertEquals(coptic.centuries(), coptic.yearOfCentury().getRangeDurationField());
        assertEquals(coptic.eras(), coptic.yearOfEra().getRangeDurationField());
        assertEquals(null, coptic.year().getRangeDurationField());
        assertEquals(coptic.years(), coptic.monthOfYear().getRangeDurationField());
        assertEquals(coptic.centuries(), coptic.weekyearOfCentury().getRangeDurationField());
        assertEquals(null, coptic.weekyear().getRangeDurationField());
        assertEquals(coptic.weekyears(), coptic.weekOfWeekyear().getRangeDurationField());
        assertEquals(coptic.years(), coptic.dayOfYear().getRangeDurationField());
        assertEquals(coptic.months(), coptic.dayOfMonth().getRangeDurationField());
        assertEquals(coptic.weeks(), coptic.dayOfWeek().getRangeDurationField());
    }

// org.joda.time.chrono.TestCopticChronology::testTimeFields
    public void testTimeFields() {
        final CopticChronology coptic = CopticChronology.getInstance();
        assertEquals("halfdayOfDay", coptic.halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", coptic.clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", coptic.hourOfHalfday().getName());
        assertEquals("clockhourOfDay", coptic.clockhourOfDay().getName());
        assertEquals("hourOfDay", coptic.hourOfDay().getName());
        assertEquals("minuteOfDay", coptic.minuteOfDay().getName());
        assertEquals("minuteOfHour", coptic.minuteOfHour().getName());
        assertEquals("secondOfDay", coptic.secondOfDay().getName());
        assertEquals("secondOfMinute", coptic.secondOfMinute().getName());
        assertEquals("millisOfDay", coptic.millisOfDay().getName());
        assertEquals("millisOfSecond", coptic.millisOfSecond().getName());
        
        assertEquals(true, coptic.halfdayOfDay().isSupported());
        assertEquals(true, coptic.clockhourOfHalfday().isSupported());
        assertEquals(true, coptic.hourOfHalfday().isSupported());
        assertEquals(true, coptic.clockhourOfDay().isSupported());
        assertEquals(true, coptic.hourOfDay().isSupported());
        assertEquals(true, coptic.minuteOfDay().isSupported());
        assertEquals(true, coptic.minuteOfHour().isSupported());
        assertEquals(true, coptic.secondOfDay().isSupported());
        assertEquals(true, coptic.secondOfMinute().isSupported());
        assertEquals(true, coptic.millisOfDay().isSupported());
        assertEquals(true, coptic.millisOfSecond().isSupported());
    }

// org.joda.time.chrono.TestCopticChronology::testEpoch
    public void testEpoch() {
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, COPTIC_UTC);
        assertEquals(new DateTime(284, 8, 29, 0, 0, 0, 0, JULIAN_UTC), epoch.withChronology(JULIAN_UTC));
    }

// org.joda.time.chrono.TestCopticChronology::testEra
    public void testEra() {
        assertEquals(1, CopticChronology.AM);
        try {
            new DateTime(-1, 13, 5, 0, 0, 0, 0, COPTIC_UTC);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.chrono.TestCopticChronology::testCalendar
    public void testCalendar() {
        if (TestAll.FAST) {
            return;
        }
        System.out.println("\nTestCopticChronology.testCalendar");
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, COPTIC_UTC);
        long millis = epoch.getMillis();
        long end = new DateTime(3000, 1, 1, 0, 0, 0, 0, ISO_UTC).getMillis();
        DateTimeField dayOfWeek = COPTIC_UTC.dayOfWeek();
        DateTimeField dayOfYear = COPTIC_UTC.dayOfYear();
        DateTimeField dayOfMonth = COPTIC_UTC.dayOfMonth();
        DateTimeField monthOfYear = COPTIC_UTC.monthOfYear();
        DateTimeField year = COPTIC_UTC.year();
        DateTimeField yearOfEra = COPTIC_UTC.yearOfEra();
        DateTimeField era = COPTIC_UTC.era();
        int expectedDOW = new DateTime(284, 8, 29, 0, 0, 0, 0, JULIAN_UTC).getDayOfWeek();
        int expectedDOY = 1;
        int expectedDay = 1;
        int expectedMonth = 1;
        int expectedYear = 1;
        while (millis < end) {
            int dowValue = dayOfWeek.get(millis);
            int doyValue = dayOfYear.get(millis);
            int dayValue = dayOfMonth.get(millis);
            int monthValue = monthOfYear.get(millis);
            int yearValue = year.get(millis);
            int yearOfEraValue = yearOfEra.get(millis);
            int monthLen = dayOfMonth.getMaximumValue(millis);
            if (monthValue < 1 || monthValue > 13) {
                fail("Bad month: " + millis);
            }
            
            
            assertEquals(1, era.get(millis));
            assertEquals("AM", era.getAsText(millis));
            assertEquals("AM", era.getAsShortText(millis));
            
            
            assertEquals(expectedYear, yearValue);
            assertEquals(expectedYear, yearOfEraValue);
            assertEquals(expectedMonth, monthValue);
            assertEquals(expectedDay, dayValue);
            assertEquals(expectedDOW, dowValue);
            assertEquals(expectedDOY, doyValue);
            
            
            assertEquals(yearValue % 4 == 3, year.isLeap(millis));
            
            
            if (monthValue == 13) {
                assertEquals(yearValue % 4 == 3, monthOfYear.isLeap(millis));
                if (yearValue % 4 == 3) {
                    assertEquals(6, monthLen);
                } else {
                    assertEquals(5, monthLen);
                }
            } else {
                assertEquals(30, monthLen);
            }
            
            
            expectedDOW = (((expectedDOW + 1) - 1) % 7) + 1;
            expectedDay++;
            expectedDOY++;
            if (expectedDay == 31 && expectedMonth < 13) {
                expectedDay = 1;
                expectedMonth++;
            } else if (expectedMonth == 13) {
                if (expectedYear % 4 == 3 && expectedDay == 7) {
                    expectedDay = 1;
                    expectedMonth = 1;
                    expectedYear++;
                    expectedDOY = 1;
                } else if (expectedYear % 4 != 3 && expectedDay == 6) {
                    expectedDay = 1;
                    expectedMonth = 1;
                    expectedYear++;
                    expectedDOY = 1;
                }
            }
            millis += SKIP;
        }
    }

// org.joda.time.chrono.TestCopticChronology::testSampleDate
    public void testSampleDate() {
        DateTime dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, ISO_UTC).withChronology(COPTIC_UTC);
        assertEquals(CopticChronology.AM, dt.getEra());
        assertEquals(18, dt.getCenturyOfEra());  
        assertEquals(20, dt.getYearOfCentury());
        assertEquals(1720, dt.getYearOfEra());
        
        assertEquals(1720, dt.getYear());
        Property fld = dt.year();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(DurationFieldType.days(), fld.getLeapDurationField().getType());
        assertEquals(new DateTime(1721, 10, 2, 0, 0, 0, 0, COPTIC_UTC), fld.addToCopy(1));
        
        assertEquals(10, dt.getMonthOfYear());
        fld = dt.monthOfYear();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(DurationFieldType.days(), fld.getLeapDurationField().getType());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(13, fld.getMaximumValue());
        assertEquals(13, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1721, 1, 2, 0, 0, 0, 0, COPTIC_UTC), fld.addToCopy(4));
        assertEquals(new DateTime(1720, 1, 2, 0, 0, 0, 0, COPTIC_UTC), fld.addWrapFieldToCopy(4));
        
        assertEquals(2, dt.getDayOfMonth());
        fld = dt.dayOfMonth();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(30, fld.getMaximumValue());
        assertEquals(30, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1720, 10, 3, 0, 0, 0, 0, COPTIC_UTC), fld.addToCopy(1));
        
        assertEquals(DateTimeConstants.WEDNESDAY, dt.getDayOfWeek());
        fld = dt.dayOfWeek();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(7, fld.getMaximumValue());
        assertEquals(7, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1720, 10, 3, 0, 0, 0, 0, COPTIC_UTC), fld.addToCopy(1));
        
        assertEquals(9 * 30 + 2, dt.getDayOfYear());
        fld = dt.dayOfYear();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(365, fld.getMaximumValue());
        assertEquals(366, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1720, 10, 3, 0, 0, 0, 0, COPTIC_UTC), fld.addToCopy(1));
        
        assertEquals(0, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
    }

// org.joda.time.chrono.TestCopticChronology::testSampleDateWithZone
    public void testSampleDateWithZone() {
        DateTime dt = new DateTime(2004, 6, 9, 12, 0, 0, 0, PARIS).withChronology(COPTIC_UTC);
        assertEquals(CopticChronology.AM, dt.getEra());
        assertEquals(1720, dt.getYear());
        assertEquals(1720, dt.getYearOfEra());
        assertEquals(10, dt.getMonthOfYear());
        assertEquals(2, dt.getDayOfMonth());
        assertEquals(10, dt.getHourOfDay());  
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
    }

// org.joda.time.chrono.TestCopticChronology::testDurationYear
    public void testDurationYear() {
        
        DateTime dt20 = new DateTime(1720, 10, 2, 0, 0, 0, 0, COPTIC_UTC);
        DateTime dt21 = new DateTime(1721, 10, 2, 0, 0, 0, 0, COPTIC_UTC);
        DateTime dt22 = new DateTime(1722, 10, 2, 0, 0, 0, 0, COPTIC_UTC);
        DateTime dt23 = new DateTime(1723, 10, 2, 0, 0, 0, 0, COPTIC_UTC);
        DateTime dt24 = new DateTime(1724, 10, 2, 0, 0, 0, 0, COPTIC_UTC);
        
        DurationField fld = dt20.year().getDurationField();
        assertEquals(COPTIC_UTC.years(), fld);
        assertEquals(1L * 365L * MILLIS_PER_DAY, fld.getMillis(1, dt20.getMillis()));
        assertEquals(2L * 365L * MILLIS_PER_DAY, fld.getMillis(2, dt20.getMillis()));
        assertEquals(3L * 365L * MILLIS_PER_DAY, fld.getMillis(3, dt20.getMillis()));
        assertEquals((4L * 365L + 1L) * MILLIS_PER_DAY, fld.getMillis(4, dt20.getMillis()));
        
        assertEquals(((4L * 365L + 1L) * MILLIS_PER_DAY) / 4, fld.getMillis(1));
        assertEquals(((4L * 365L + 1L) * MILLIS_PER_DAY) / 2, fld.getMillis(2));
        
        assertEquals(1L * 365L * MILLIS_PER_DAY, fld.getMillis(1L, dt20.getMillis()));
        assertEquals(2L * 365L * MILLIS_PER_DAY, fld.getMillis(2L, dt20.getMillis()));
        assertEquals(3L * 365L * MILLIS_PER_DAY, fld.getMillis(3L, dt20.getMillis()));
        assertEquals((4L * 365L + 1L) * MILLIS_PER_DAY, fld.getMillis(4L, dt20.getMillis()));
        
        assertEquals(((4L * 365L + 1L) * MILLIS_PER_DAY) / 4, fld.getMillis(1L));
        assertEquals(((4L * 365L + 1L) * MILLIS_PER_DAY) / 2, fld.getMillis(2L));
        
        assertEquals(((4L * 365L + 1L) * MILLIS_PER_DAY) / 4, fld.getUnitMillis());
        
        assertEquals(0, fld.getValue(1L * 365L * MILLIS_PER_DAY - 1L, dt20.getMillis()));
        assertEquals(1, fld.getValue(1L * 365L * MILLIS_PER_DAY, dt20.getMillis()));
        assertEquals(1, fld.getValue(1L * 365L * MILLIS_PER_DAY + 1L, dt20.getMillis()));
        assertEquals(1, fld.getValue(2L * 365L * MILLIS_PER_DAY - 1L, dt20.getMillis()));
        assertEquals(2, fld.getValue(2L * 365L * MILLIS_PER_DAY, dt20.getMillis()));
        assertEquals(2, fld.getValue(2L * 365L * MILLIS_PER_DAY + 1L, dt20.getMillis()));
        assertEquals(2, fld.getValue(3L * 365L * MILLIS_PER_DAY - 1L, dt20.getMillis()));
        assertEquals(3, fld.getValue(3L * 365L * MILLIS_PER_DAY, dt20.getMillis()));
        assertEquals(3, fld.getValue(3L * 365L * MILLIS_PER_DAY + 1L, dt20.getMillis()));
        assertEquals(3, fld.getValue((4L * 365L + 1L) * MILLIS_PER_DAY - 1L, dt20.getMillis()));
        assertEquals(4, fld.getValue((4L * 365L + 1L) * MILLIS_PER_DAY, dt20.getMillis()));
        assertEquals(4, fld.getValue((4L * 365L + 1L) * MILLIS_PER_DAY + 1L, dt20.getMillis()));
        
        assertEquals(dt21.getMillis(), fld.add(dt20.getMillis(), 1));
        assertEquals(dt22.getMillis(), fld.add(dt20.getMillis(), 2));
        assertEquals(dt23.getMillis(), fld.add(dt20.getMillis(), 3));
        assertEquals(dt24.getMillis(), fld.add(dt20.getMillis(), 4));
        
        assertEquals(dt21.getMillis(), fld.add(dt20.getMillis(), 1L));
        assertEquals(dt22.getMillis(), fld.add(dt20.getMillis(), 2L));
        assertEquals(dt23.getMillis(), fld.add(dt20.getMillis(), 3L));
        assertEquals(dt24.getMillis(), fld.add(dt20.getMillis(), 4L));
    }

// org.joda.time.chrono.TestCopticChronology::testDurationMonth
    public void testDurationMonth() {
        
        DateTime dt11 = new DateTime(1723, 11, 2, 0, 0, 0, 0, COPTIC_UTC);
        DateTime dt12 = new DateTime(1723, 12, 2, 0, 0, 0, 0, COPTIC_UTC);
        DateTime dt13 = new DateTime(1723, 13, 2, 0, 0, 0, 0, COPTIC_UTC);
        DateTime dt01 = new DateTime(1724, 1, 2, 0, 0, 0, 0, COPTIC_UTC);
        
        DurationField fld = dt11.monthOfYear().getDurationField();
        assertEquals(COPTIC_UTC.months(), fld);
        assertEquals(1L * 30L * MILLIS_PER_DAY, fld.getMillis(1, dt11.getMillis()));
        assertEquals(2L * 30L * MILLIS_PER_DAY, fld.getMillis(2, dt11.getMillis()));
        assertEquals((2L * 30L + 6L) * MILLIS_PER_DAY, fld.getMillis(3, dt11.getMillis()));
        assertEquals((3L * 30L + 6L) * MILLIS_PER_DAY, fld.getMillis(4, dt11.getMillis()));
        
        assertEquals(1L * 30L * MILLIS_PER_DAY, fld.getMillis(1));
        assertEquals(2L * 30L * MILLIS_PER_DAY, fld.getMillis(2));
        assertEquals(13L * 30L * MILLIS_PER_DAY, fld.getMillis(13));
        
        assertEquals(1L * 30L * MILLIS_PER_DAY, fld.getMillis(1L, dt11.getMillis()));
        assertEquals(2L * 30L * MILLIS_PER_DAY, fld.getMillis(2L, dt11.getMillis()));
        assertEquals((2L * 30L + 6L) * MILLIS_PER_DAY, fld.getMillis(3L, dt11.getMillis()));
        assertEquals((3L * 30L + 6L) * MILLIS_PER_DAY, fld.getMillis(4L, dt11.getMillis()));
        
        assertEquals(1L * 30L * MILLIS_PER_DAY, fld.getMillis(1L));
        assertEquals(2L * 30L * MILLIS_PER_DAY, fld.getMillis(2L));
        assertEquals(13L * 30L * MILLIS_PER_DAY, fld.getMillis(13L));
        
        assertEquals(0, fld.getValue(1L * 30L * MILLIS_PER_DAY - 1L, dt11.getMillis()));
        assertEquals(1, fld.getValue(1L * 30L * MILLIS_PER_DAY, dt11.getMillis()));
        assertEquals(1, fld.getValue(1L * 30L * MILLIS_PER_DAY + 1L, dt11.getMillis()));
        assertEquals(1, fld.getValue(2L * 30L * MILLIS_PER_DAY - 1L, dt11.getMillis()));
        assertEquals(2, fld.getValue(2L * 30L * MILLIS_PER_DAY, dt11.getMillis()));
        assertEquals(2, fld.getValue(2L * 30L * MILLIS_PER_DAY + 1L, dt11.getMillis()));
        assertEquals(2, fld.getValue((2L * 30L + 6L) * MILLIS_PER_DAY - 1L, dt11.getMillis()));
        assertEquals(3, fld.getValue((2L * 30L + 6L) * MILLIS_PER_DAY, dt11.getMillis()));
        assertEquals(3, fld.getValue((2L * 30L + 6L) * MILLIS_PER_DAY + 1L, dt11.getMillis()));
        assertEquals(3, fld.getValue((3L * 30L + 6L) * MILLIS_PER_DAY - 1L, dt11.getMillis()));
        assertEquals(4, fld.getValue((3L * 30L + 6L) * MILLIS_PER_DAY, dt11.getMillis()));
        assertEquals(4, fld.getValue((3L * 30L + 6L) * MILLIS_PER_DAY + 1L, dt11.getMillis()));
        
        assertEquals(dt12.getMillis(), fld.add(dt11.getMillis(), 1));
        assertEquals(dt13.getMillis(), fld.add(dt11.getMillis(), 2));
        assertEquals(dt01.getMillis(), fld.add(dt11.getMillis(), 3));
        
        assertEquals(dt12.getMillis(), fld.add(dt11.getMillis(), 1L));
        assertEquals(dt13.getMillis(), fld.add(dt11.getMillis(), 2L));
        assertEquals(dt01.getMillis(), fld.add(dt11.getMillis(), 3L));
    }

// org.joda.time.chrono.TestEthiopicChronology::testFactoryUTC
    public void testFactoryUTC() {
        assertEquals(DateTimeZone.UTC, EthiopicChronology.getInstanceUTC().getZone());
        assertSame(EthiopicChronology.class, EthiopicChronology.getInstanceUTC().getClass());
    }

// org.joda.time.chrono.TestEthiopicChronology::testFactory
    public void testFactory() {
        assertEquals(LONDON, EthiopicChronology.getInstance().getZone());
        assertSame(EthiopicChronology.class, EthiopicChronology.getInstance().getClass());
    }

// org.joda.time.chrono.TestEthiopicChronology::testFactory_Zone
    public void testFactory_Zone() {
        assertEquals(TOKYO, EthiopicChronology.getInstance(TOKYO).getZone());
        assertEquals(PARIS, EthiopicChronology.getInstance(PARIS).getZone());
        assertEquals(LONDON, EthiopicChronology.getInstance(null).getZone());
        assertSame(EthiopicChronology.class, EthiopicChronology.getInstance(TOKYO).getClass());
    }

// org.joda.time.chrono.TestEthiopicChronology::testEquality
    public void testEquality() {
        assertSame(EthiopicChronology.getInstance(TOKYO), EthiopicChronology.getInstance(TOKYO));
        assertSame(EthiopicChronology.getInstance(LONDON), EthiopicChronology.getInstance(LONDON));
        assertSame(EthiopicChronology.getInstance(PARIS), EthiopicChronology.getInstance(PARIS));
        assertSame(EthiopicChronology.getInstanceUTC(), EthiopicChronology.getInstanceUTC());
        assertSame(EthiopicChronology.getInstance(), EthiopicChronology.getInstance(LONDON));
    }

// org.joda.time.chrono.TestEthiopicChronology::testWithUTC
    public void testWithUTC() {
        assertSame(EthiopicChronology.getInstanceUTC(), EthiopicChronology.getInstance(LONDON).withUTC());
        assertSame(EthiopicChronology.getInstanceUTC(), EthiopicChronology.getInstance(TOKYO).withUTC());
        assertSame(EthiopicChronology.getInstanceUTC(), EthiopicChronology.getInstanceUTC().withUTC());
        assertSame(EthiopicChronology.getInstanceUTC(), EthiopicChronology.getInstance().withUTC());
    }

// org.joda.time.chrono.TestEthiopicChronology::testWithZone
    public void testWithZone() {
        assertSame(EthiopicChronology.getInstance(TOKYO), EthiopicChronology.getInstance(TOKYO).withZone(TOKYO));
        assertSame(EthiopicChronology.getInstance(LONDON), EthiopicChronology.getInstance(TOKYO).withZone(LONDON));
        assertSame(EthiopicChronology.getInstance(PARIS), EthiopicChronology.getInstance(TOKYO).withZone(PARIS));
        assertSame(EthiopicChronology.getInstance(LONDON), EthiopicChronology.getInstance(TOKYO).withZone(null));
        assertSame(EthiopicChronology.getInstance(PARIS), EthiopicChronology.getInstance().withZone(PARIS));
        assertSame(EthiopicChronology.getInstance(PARIS), EthiopicChronology.getInstanceUTC().withZone(PARIS));
    }

// org.joda.time.chrono.TestEthiopicChronology::testToString
    public void testToString() {
        assertEquals("EthiopicChronology[Europe/London]", EthiopicChronology.getInstance(LONDON).toString());
        assertEquals("EthiopicChronology[Asia/Tokyo]", EthiopicChronology.getInstance(TOKYO).toString());
        assertEquals("EthiopicChronology[Europe/London]", EthiopicChronology.getInstance().toString());
        assertEquals("EthiopicChronology[UTC]", EthiopicChronology.getInstanceUTC().toString());
    }

// org.joda.time.chrono.TestEthiopicChronology::testDurationFields
    public void testDurationFields() {
        final EthiopicChronology ethiopic = EthiopicChronology.getInstance();
        assertEquals("eras", ethiopic.eras().getName());
        assertEquals("centuries", ethiopic.centuries().getName());
        assertEquals("years", ethiopic.years().getName());
        assertEquals("weekyears", ethiopic.weekyears().getName());
        assertEquals("months", ethiopic.months().getName());
        assertEquals("weeks", ethiopic.weeks().getName());
        assertEquals("days", ethiopic.days().getName());
        assertEquals("halfdays", ethiopic.halfdays().getName());
        assertEquals("hours", ethiopic.hours().getName());
        assertEquals("minutes", ethiopic.minutes().getName());
        assertEquals("seconds", ethiopic.seconds().getName());
        assertEquals("millis", ethiopic.millis().getName());
        
        assertEquals(false, ethiopic.eras().isSupported());
        assertEquals(true, ethiopic.centuries().isSupported());
        assertEquals(true, ethiopic.years().isSupported());
        assertEquals(true, ethiopic.weekyears().isSupported());
        assertEquals(true, ethiopic.months().isSupported());
        assertEquals(true, ethiopic.weeks().isSupported());
        assertEquals(true, ethiopic.days().isSupported());
        assertEquals(true, ethiopic.halfdays().isSupported());
        assertEquals(true, ethiopic.hours().isSupported());
        assertEquals(true, ethiopic.minutes().isSupported());
        assertEquals(true, ethiopic.seconds().isSupported());
        assertEquals(true, ethiopic.millis().isSupported());
        
        assertEquals(false, ethiopic.centuries().isPrecise());
        assertEquals(false, ethiopic.years().isPrecise());
        assertEquals(false, ethiopic.weekyears().isPrecise());
        assertEquals(false, ethiopic.months().isPrecise());
        assertEquals(false, ethiopic.weeks().isPrecise());
        assertEquals(false, ethiopic.days().isPrecise());
        assertEquals(false, ethiopic.halfdays().isPrecise());
        assertEquals(true, ethiopic.hours().isPrecise());
        assertEquals(true, ethiopic.minutes().isPrecise());
        assertEquals(true, ethiopic.seconds().isPrecise());
        assertEquals(true, ethiopic.millis().isPrecise());
        
        final EthiopicChronology ethiopicUTC = EthiopicChronology.getInstanceUTC();
        assertEquals(false, ethiopicUTC.centuries().isPrecise());
        assertEquals(false, ethiopicUTC.years().isPrecise());
        assertEquals(false, ethiopicUTC.weekyears().isPrecise());
        assertEquals(false, ethiopicUTC.months().isPrecise());
        assertEquals(true, ethiopicUTC.weeks().isPrecise());
        assertEquals(true, ethiopicUTC.days().isPrecise());
        assertEquals(true, ethiopicUTC.halfdays().isPrecise());
        assertEquals(true, ethiopicUTC.hours().isPrecise());
        assertEquals(true, ethiopicUTC.minutes().isPrecise());
        assertEquals(true, ethiopicUTC.seconds().isPrecise());
        assertEquals(true, ethiopicUTC.millis().isPrecise());
        
        final DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        final EthiopicChronology ethiopicGMT = EthiopicChronology.getInstance(gmt);
        assertEquals(false, ethiopicGMT.centuries().isPrecise());
        assertEquals(false, ethiopicGMT.years().isPrecise());
        assertEquals(false, ethiopicGMT.weekyears().isPrecise());
        assertEquals(false, ethiopicGMT.months().isPrecise());
        assertEquals(true, ethiopicGMT.weeks().isPrecise());
        assertEquals(true, ethiopicGMT.days().isPrecise());
        assertEquals(true, ethiopicGMT.halfdays().isPrecise());
        assertEquals(true, ethiopicGMT.hours().isPrecise());
        assertEquals(true, ethiopicGMT.minutes().isPrecise());
        assertEquals(true, ethiopicGMT.seconds().isPrecise());
        assertEquals(true, ethiopicGMT.millis().isPrecise());
    }

// org.joda.time.chrono.TestEthiopicChronology::testDateFields
    public void testDateFields() {
        final EthiopicChronology ethiopic = EthiopicChronology.getInstance();
        assertEquals("era", ethiopic.era().getName());
        assertEquals("centuryOfEra", ethiopic.centuryOfEra().getName());
        assertEquals("yearOfCentury", ethiopic.yearOfCentury().getName());
        assertEquals("yearOfEra", ethiopic.yearOfEra().getName());
        assertEquals("year", ethiopic.year().getName());
        assertEquals("monthOfYear", ethiopic.monthOfYear().getName());
        assertEquals("weekyearOfCentury", ethiopic.weekyearOfCentury().getName());
        assertEquals("weekyear", ethiopic.weekyear().getName());
        assertEquals("weekOfWeekyear", ethiopic.weekOfWeekyear().getName());
        assertEquals("dayOfYear", ethiopic.dayOfYear().getName());
        assertEquals("dayOfMonth", ethiopic.dayOfMonth().getName());
        assertEquals("dayOfWeek", ethiopic.dayOfWeek().getName());
        
        assertEquals(true, ethiopic.era().isSupported());
        assertEquals(true, ethiopic.centuryOfEra().isSupported());
        assertEquals(true, ethiopic.yearOfCentury().isSupported());
        assertEquals(true, ethiopic.yearOfEra().isSupported());
        assertEquals(true, ethiopic.year().isSupported());
        assertEquals(true, ethiopic.monthOfYear().isSupported());
        assertEquals(true, ethiopic.weekyearOfCentury().isSupported());
        assertEquals(true, ethiopic.weekyear().isSupported());
        assertEquals(true, ethiopic.weekOfWeekyear().isSupported());
        assertEquals(true, ethiopic.dayOfYear().isSupported());
        assertEquals(true, ethiopic.dayOfMonth().isSupported());
        assertEquals(true, ethiopic.dayOfWeek().isSupported());
        
        assertEquals(ethiopic.eras(), ethiopic.era().getDurationField());
        assertEquals(ethiopic.centuries(), ethiopic.centuryOfEra().getDurationField());
        assertEquals(ethiopic.years(), ethiopic.yearOfCentury().getDurationField());
        assertEquals(ethiopic.years(), ethiopic.yearOfEra().getDurationField());
        assertEquals(ethiopic.years(), ethiopic.year().getDurationField());
        assertEquals(ethiopic.months(), ethiopic.monthOfYear().getDurationField());
        assertEquals(ethiopic.weekyears(), ethiopic.weekyearOfCentury().getDurationField());
        assertEquals(ethiopic.weekyears(), ethiopic.weekyear().getDurationField());
        assertEquals(ethiopic.weeks(), ethiopic.weekOfWeekyear().getDurationField());
        assertEquals(ethiopic.days(), ethiopic.dayOfYear().getDurationField());
        assertEquals(ethiopic.days(), ethiopic.dayOfMonth().getDurationField());
        assertEquals(ethiopic.days(), ethiopic.dayOfWeek().getDurationField());
        
        assertEquals(null, ethiopic.era().getRangeDurationField());
        assertEquals(ethiopic.eras(), ethiopic.centuryOfEra().getRangeDurationField());
        assertEquals(ethiopic.centuries(), ethiopic.yearOfCentury().getRangeDurationField());
        assertEquals(ethiopic.eras(), ethiopic.yearOfEra().getRangeDurationField());
        assertEquals(null, ethiopic.year().getRangeDurationField());
        assertEquals(ethiopic.years(), ethiopic.monthOfYear().getRangeDurationField());
        assertEquals(ethiopic.centuries(), ethiopic.weekyearOfCentury().getRangeDurationField());
        assertEquals(null, ethiopic.weekyear().getRangeDurationField());
        assertEquals(ethiopic.weekyears(), ethiopic.weekOfWeekyear().getRangeDurationField());
        assertEquals(ethiopic.years(), ethiopic.dayOfYear().getRangeDurationField());
        assertEquals(ethiopic.months(), ethiopic.dayOfMonth().getRangeDurationField());
        assertEquals(ethiopic.weeks(), ethiopic.dayOfWeek().getRangeDurationField());
    }

// org.joda.time.chrono.TestEthiopicChronology::testTimeFields
    public void testTimeFields() {
        final EthiopicChronology ethiopic = EthiopicChronology.getInstance();
        assertEquals("halfdayOfDay", ethiopic.halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", ethiopic.clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", ethiopic.hourOfHalfday().getName());
        assertEquals("clockhourOfDay", ethiopic.clockhourOfDay().getName());
        assertEquals("hourOfDay", ethiopic.hourOfDay().getName());
        assertEquals("minuteOfDay", ethiopic.minuteOfDay().getName());
        assertEquals("minuteOfHour", ethiopic.minuteOfHour().getName());
        assertEquals("secondOfDay", ethiopic.secondOfDay().getName());
        assertEquals("secondOfMinute", ethiopic.secondOfMinute().getName());
        assertEquals("millisOfDay", ethiopic.millisOfDay().getName());
        assertEquals("millisOfSecond", ethiopic.millisOfSecond().getName());
        
        assertEquals(true, ethiopic.halfdayOfDay().isSupported());
        assertEquals(true, ethiopic.clockhourOfHalfday().isSupported());
        assertEquals(true, ethiopic.hourOfHalfday().isSupported());
        assertEquals(true, ethiopic.clockhourOfDay().isSupported());
        assertEquals(true, ethiopic.hourOfDay().isSupported());
        assertEquals(true, ethiopic.minuteOfDay().isSupported());
        assertEquals(true, ethiopic.minuteOfHour().isSupported());
        assertEquals(true, ethiopic.secondOfDay().isSupported());
        assertEquals(true, ethiopic.secondOfMinute().isSupported());
        assertEquals(true, ethiopic.millisOfDay().isSupported());
        assertEquals(true, ethiopic.millisOfSecond().isSupported());
    }

// org.joda.time.chrono.TestEthiopicChronology::testEpoch
    public void testEpoch() {
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, ETHIOPIC_UTC);
        assertEquals(new DateTime(8, 8, 29, 0, 0, 0, 0, JULIAN_UTC), epoch.withChronology(JULIAN_UTC));
    }

// org.joda.time.chrono.TestEthiopicChronology::testEra
    public void testEra() {
        assertEquals(1, EthiopicChronology.EE);
        try {
            new DateTime(-1, 13, 5, 0, 0, 0, 0, ETHIOPIC_UTC);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.chrono.TestEthiopicChronology::testCalendar
    public void testCalendar() {
        if (TestAll.FAST) {
            return;
        }
        System.out.println("\nTestEthiopicChronology.testCalendar");
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, ETHIOPIC_UTC);
        long millis = epoch.getMillis();
        long end = new DateTime(3000, 1, 1, 0, 0, 0, 0, ISO_UTC).getMillis();
        DateTimeField dayOfWeek = ETHIOPIC_UTC.dayOfWeek();
        DateTimeField dayOfYear = ETHIOPIC_UTC.dayOfYear();
        DateTimeField dayOfMonth = ETHIOPIC_UTC.dayOfMonth();
        DateTimeField monthOfYear = ETHIOPIC_UTC.monthOfYear();
        DateTimeField year = ETHIOPIC_UTC.year();
        DateTimeField yearOfEra = ETHIOPIC_UTC.yearOfEra();
        DateTimeField era = ETHIOPIC_UTC.era();
        int expectedDOW = new DateTime(8, 8, 29, 0, 0, 0, 0, JULIAN_UTC).getDayOfWeek();
        int expectedDOY = 1;
        int expectedDay = 1;
        int expectedMonth = 1;
        int expectedYear = 1;
        while (millis < end) {
            int dowValue = dayOfWeek.get(millis);
            int doyValue = dayOfYear.get(millis);
            int dayValue = dayOfMonth.get(millis);
            int monthValue = monthOfYear.get(millis);
            int yearValue = year.get(millis);
            int yearOfEraValue = yearOfEra.get(millis);
            int monthLen = dayOfMonth.getMaximumValue(millis);
            if (monthValue < 1 || monthValue > 13) {
                fail("Bad month: " + millis);
            }
            
            
            assertEquals(1, era.get(millis));
            assertEquals("EE", era.getAsText(millis));
            assertEquals("EE", era.getAsShortText(millis));
            
            
            assertEquals(expectedYear, yearValue);
            assertEquals(expectedYear, yearOfEraValue);
            assertEquals(expectedMonth, monthValue);
            assertEquals(expectedDay, dayValue);
            assertEquals(expectedDOW, dowValue);
            assertEquals(expectedDOY, doyValue);
            
            
            assertEquals(yearValue % 4 == 3, year.isLeap(millis));
            
            
            if (monthValue == 13) {
                assertEquals(yearValue % 4 == 3, monthOfYear.isLeap(millis));
                if (yearValue % 4 == 3) {
                    assertEquals(6, monthLen);
                } else {
                    assertEquals(5, monthLen);
                }
            } else {
                assertEquals(30, monthLen);
            }
            
            
            expectedDOW = (((expectedDOW + 1) - 1) % 7) + 1;
            expectedDay++;
            expectedDOY++;
            if (expectedDay == 31 && expectedMonth < 13) {
                expectedDay = 1;
                expectedMonth++;
            } else if (expectedMonth == 13) {
                if (expectedYear % 4 == 3 && expectedDay == 7) {
                    expectedDay = 1;
                    expectedMonth = 1;
                    expectedYear++;
                    expectedDOY = 1;
                } else if (expectedYear % 4 != 3 && expectedDay == 6) {
                    expectedDay = 1;
                    expectedMonth = 1;
                    expectedYear++;
                    expectedDOY = 1;
                }
            }
            millis += SKIP;
        }
    }

// org.joda.time.chrono.TestEthiopicChronology::testSampleDate
    public void testSampleDate() {
        DateTime dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, ISO_UTC).withChronology(ETHIOPIC_UTC);
        assertEquals(EthiopicChronology.EE, dt.getEra());
        assertEquals(20, dt.getCenturyOfEra());  
        assertEquals(96, dt.getYearOfCentury());
        assertEquals(1996, dt.getYearOfEra());
        
        assertEquals(1996, dt.getYear());
        Property fld = dt.year();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(DurationFieldType.days(), fld.getLeapDurationField().getType());
        assertEquals(new DateTime(1997, 10, 2, 0, 0, 0, 0, ETHIOPIC_UTC), fld.addToCopy(1));
        
        assertEquals(10, dt.getMonthOfYear());
        fld = dt.monthOfYear();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(DurationFieldType.days(), fld.getLeapDurationField().getType());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(13, fld.getMaximumValue());
        assertEquals(13, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1997, 1, 2, 0, 0, 0, 0, ETHIOPIC_UTC), fld.addToCopy(4));
        assertEquals(new DateTime(1996, 1, 2, 0, 0, 0, 0, ETHIOPIC_UTC), fld.addWrapFieldToCopy(4));
        
        assertEquals(2, dt.getDayOfMonth());
        fld = dt.dayOfMonth();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(30, fld.getMaximumValue());
        assertEquals(30, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1996, 10, 3, 0, 0, 0, 0, ETHIOPIC_UTC), fld.addToCopy(1));
        
        assertEquals(DateTimeConstants.WEDNESDAY, dt.getDayOfWeek());
        fld = dt.dayOfWeek();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(7, fld.getMaximumValue());
        assertEquals(7, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1996, 10, 3, 0, 0, 0, 0, ETHIOPIC_UTC), fld.addToCopy(1));
        
        assertEquals(9 * 30 + 2, dt.getDayOfYear());
        fld = dt.dayOfYear();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(365, fld.getMaximumValue());
        assertEquals(366, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1996, 10, 3, 0, 0, 0, 0, ETHIOPIC_UTC), fld.addToCopy(1));
        
        assertEquals(0, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
    }

// org.joda.time.chrono.TestEthiopicChronology::testSampleDateWithZone
    public void testSampleDateWithZone() {
        DateTime dt = new DateTime(2004, 6, 9, 12, 0, 0, 0, PARIS).withChronology(ETHIOPIC_UTC);
        assertEquals(EthiopicChronology.EE, dt.getEra());
        assertEquals(1996, dt.getYear());
        assertEquals(1996, dt.getYearOfEra());
        assertEquals(10, dt.getMonthOfYear());
        assertEquals(2, dt.getDayOfMonth());
        assertEquals(10, dt.getHourOfDay());  
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
    }

// org.joda.time.chrono.TestEthiopicChronology::testDurationYear
    public void testDurationYear() {
        
        DateTime dt96 = new DateTime(1996, 10, 2, 0, 0, 0, 0, ETHIOPIC_UTC);
        DateTime dt97 = new DateTime(1997, 10, 2, 0, 0, 0, 0, ETHIOPIC_UTC);
        DateTime dt98 = new DateTime(1998, 10, 2, 0, 0, 0, 0, ETHIOPIC_UTC);
        DateTime dt99 = new DateTime(1999, 10, 2, 0, 0, 0, 0, ETHIOPIC_UTC);
        DateTime dt00 = new DateTime(2000, 10, 2, 0, 0, 0, 0, ETHIOPIC_UTC);
        
        DurationField fld = dt96.year().getDurationField();
        assertEquals(ETHIOPIC_UTC.years(), fld);
        assertEquals(1L * 365L * MILLIS_PER_DAY, fld.getMillis(1, dt96.getMillis()));
        assertEquals(2L * 365L * MILLIS_PER_DAY, fld.getMillis(2, dt96.getMillis()));
        assertEquals(3L * 365L * MILLIS_PER_DAY, fld.getMillis(3, dt96.getMillis()));
        assertEquals((4L * 365L + 1L) * MILLIS_PER_DAY, fld.getMillis(4, dt96.getMillis()));
        
        assertEquals(((4L * 365L + 1L) * MILLIS_PER_DAY) / 4, fld.getMillis(1));
        assertEquals(((4L * 365L + 1L) * MILLIS_PER_DAY) / 2, fld.getMillis(2));
        
        assertEquals(1L * 365L * MILLIS_PER_DAY, fld.getMillis(1L, dt96.getMillis()));
        assertEquals(2L * 365L * MILLIS_PER_DAY, fld.getMillis(2L, dt96.getMillis()));
        assertEquals(3L * 365L * MILLIS_PER_DAY, fld.getMillis(3L, dt96.getMillis()));
        assertEquals((4L * 365L + 1L) * MILLIS_PER_DAY, fld.getMillis(4L, dt96.getMillis()));
        
        assertEquals(((4L * 365L + 1L) * MILLIS_PER_DAY) / 4, fld.getMillis(1L));
        assertEquals(((4L * 365L + 1L) * MILLIS_PER_DAY) / 2, fld.getMillis(2L));
        
        assertEquals(((4L * 365L + 1L) * MILLIS_PER_DAY) / 4, fld.getUnitMillis());
        
        assertEquals(0, fld.getValue(1L * 365L * MILLIS_PER_DAY - 1L, dt96.getMillis()));
        assertEquals(1, fld.getValue(1L * 365L * MILLIS_PER_DAY, dt96.getMillis()));
        assertEquals(1, fld.getValue(1L * 365L * MILLIS_PER_DAY + 1L, dt96.getMillis()));
        assertEquals(1, fld.getValue(2L * 365L * MILLIS_PER_DAY - 1L, dt96.getMillis()));
        assertEquals(2, fld.getValue(2L * 365L * MILLIS_PER_DAY, dt96.getMillis()));
        assertEquals(2, fld.getValue(2L * 365L * MILLIS_PER_DAY + 1L, dt96.getMillis()));
        assertEquals(2, fld.getValue(3L * 365L * MILLIS_PER_DAY - 1L, dt96.getMillis()));
        assertEquals(3, fld.getValue(3L * 365L * MILLIS_PER_DAY, dt96.getMillis()));
        assertEquals(3, fld.getValue(3L * 365L * MILLIS_PER_DAY + 1L, dt96.getMillis()));
        assertEquals(3, fld.getValue((4L * 365L + 1L) * MILLIS_PER_DAY - 1L, dt96.getMillis()));
        assertEquals(4, fld.getValue((4L * 365L + 1L) * MILLIS_PER_DAY, dt96.getMillis()));
        assertEquals(4, fld.getValue((4L * 365L + 1L) * MILLIS_PER_DAY + 1L, dt96.getMillis()));
        
        assertEquals(dt97.getMillis(), fld.add(dt96.getMillis(), 1));
        assertEquals(dt98.getMillis(), fld.add(dt96.getMillis(), 2));
        assertEquals(dt99.getMillis(), fld.add(dt96.getMillis(), 3));
        assertEquals(dt00.getMillis(), fld.add(dt96.getMillis(), 4));
        
        assertEquals(dt97.getMillis(), fld.add(dt96.getMillis(), 1L));
        assertEquals(dt98.getMillis(), fld.add(dt96.getMillis(), 2L));
        assertEquals(dt99.getMillis(), fld.add(dt96.getMillis(), 3L));
        assertEquals(dt00.getMillis(), fld.add(dt96.getMillis(), 4L));
    }

// org.joda.time.chrono.TestEthiopicChronology::testDurationMonth
    public void testDurationMonth() {
        
        DateTime dt11 = new DateTime(1999, 11, 2, 0, 0, 0, 0, ETHIOPIC_UTC);
        DateTime dt12 = new DateTime(1999, 12, 2, 0, 0, 0, 0, ETHIOPIC_UTC);
        DateTime dt13 = new DateTime(1999, 13, 2, 0, 0, 0, 0, ETHIOPIC_UTC);
        DateTime dt01 = new DateTime(2000, 1, 2, 0, 0, 0, 0, ETHIOPIC_UTC);
        
        DurationField fld = dt11.monthOfYear().getDurationField();
        assertEquals(ETHIOPIC_UTC.months(), fld);
        assertEquals(1L * 30L * MILLIS_PER_DAY, fld.getMillis(1, dt11.getMillis()));
        assertEquals(2L * 30L * MILLIS_PER_DAY, fld.getMillis(2, dt11.getMillis()));
        assertEquals((2L * 30L + 6L) * MILLIS_PER_DAY, fld.getMillis(3, dt11.getMillis()));
        assertEquals((3L * 30L + 6L) * MILLIS_PER_DAY, fld.getMillis(4, dt11.getMillis()));
        
        assertEquals(1L * 30L * MILLIS_PER_DAY, fld.getMillis(1));
        assertEquals(2L * 30L * MILLIS_PER_DAY, fld.getMillis(2));
        assertEquals(13L * 30L * MILLIS_PER_DAY, fld.getMillis(13));
        
        assertEquals(1L * 30L * MILLIS_PER_DAY, fld.getMillis(1L, dt11.getMillis()));
        assertEquals(2L * 30L * MILLIS_PER_DAY, fld.getMillis(2L, dt11.getMillis()));
        assertEquals((2L * 30L + 6L) * MILLIS_PER_DAY, fld.getMillis(3L, dt11.getMillis()));
        assertEquals((3L * 30L + 6L) * MILLIS_PER_DAY, fld.getMillis(4L, dt11.getMillis()));
        
        assertEquals(1L * 30L * MILLIS_PER_DAY, fld.getMillis(1L));
        assertEquals(2L * 30L * MILLIS_PER_DAY, fld.getMillis(2L));
        assertEquals(13L * 30L * MILLIS_PER_DAY, fld.getMillis(13L));
        
        assertEquals(0, fld.getValue(1L * 30L * MILLIS_PER_DAY - 1L, dt11.getMillis()));
        assertEquals(1, fld.getValue(1L * 30L * MILLIS_PER_DAY, dt11.getMillis()));
        assertEquals(1, fld.getValue(1L * 30L * MILLIS_PER_DAY + 1L, dt11.getMillis()));
        assertEquals(1, fld.getValue(2L * 30L * MILLIS_PER_DAY - 1L, dt11.getMillis()));
        assertEquals(2, fld.getValue(2L * 30L * MILLIS_PER_DAY, dt11.getMillis()));
        assertEquals(2, fld.getValue(2L * 30L * MILLIS_PER_DAY + 1L, dt11.getMillis()));
        assertEquals(2, fld.getValue((2L * 30L + 6L) * MILLIS_PER_DAY - 1L, dt11.getMillis()));
        assertEquals(3, fld.getValue((2L * 30L + 6L) * MILLIS_PER_DAY, dt11.getMillis()));
        assertEquals(3, fld.getValue((2L * 30L + 6L) * MILLIS_PER_DAY + 1L, dt11.getMillis()));
        assertEquals(3, fld.getValue((3L * 30L + 6L) * MILLIS_PER_DAY - 1L, dt11.getMillis()));
        assertEquals(4, fld.getValue((3L * 30L + 6L) * MILLIS_PER_DAY, dt11.getMillis()));
        assertEquals(4, fld.getValue((3L * 30L + 6L) * MILLIS_PER_DAY + 1L, dt11.getMillis()));
        
        assertEquals(dt12.getMillis(), fld.add(dt11.getMillis(), 1));
        assertEquals(dt13.getMillis(), fld.add(dt11.getMillis(), 2));
        assertEquals(dt01.getMillis(), fld.add(dt11.getMillis(), 3));
        
        assertEquals(dt12.getMillis(), fld.add(dt11.getMillis(), 1L));
        assertEquals(dt13.getMillis(), fld.add(dt11.getMillis(), 2L));
        assertEquals(dt01.getMillis(), fld.add(dt11.getMillis(), 3L));
    }

// org.joda.time.chrono.TestGJChronology::testFactoryUTC
    public void testFactoryUTC() {
        assertEquals(DateTimeZone.UTC, GJChronology.getInstanceUTC().getZone());
        assertSame(GJChronology.class, GJChronology.getInstanceUTC().getClass());
    }

// org.joda.time.chrono.TestGJChronology::testFactory
    public void testFactory() {
        assertEquals(LONDON, GJChronology.getInstance().getZone());
        assertSame(GJChronology.class, GJChronology.getInstance().getClass());
    }

// org.joda.time.chrono.TestGJChronology::testFactory_Zone
    public void testFactory_Zone() {
        assertEquals(TOKYO, GJChronology.getInstance(TOKYO).getZone());
        assertEquals(PARIS, GJChronology.getInstance(PARIS).getZone());
        assertEquals(LONDON, GJChronology.getInstance(null).getZone());
        assertSame(GJChronology.class, GJChronology.getInstance(TOKYO).getClass());
    }

// org.joda.time.chrono.TestGJChronology::testFactory_Zone_long_int
    public void testFactory_Zone_long_int() {
        GJChronology chrono = GJChronology.getInstance(TOKYO, 0L, 2);
        assertEquals(TOKYO, chrono.getZone());
        assertEquals(new Instant(0L), chrono.getGregorianCutover());
        assertEquals(2, chrono.getMinimumDaysInFirstWeek());
        assertSame(GJChronology.class, GJChronology.getInstance(TOKYO, 0L, 2).getClass());
        
        try {
            GJChronology.getInstance(TOKYO, 0L, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            GJChronology.getInstance(TOKYO, 0L, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.chrono.TestGJChronology::testFactory_Zone_RI
    public void testFactory_Zone_RI() {
        GJChronology chrono = GJChronology.getInstance(TOKYO, new Instant(0L));
        assertEquals(TOKYO, chrono.getZone());
        assertEquals(new Instant(0L), chrono.getGregorianCutover());
        assertSame(GJChronology.class, GJChronology.getInstance(TOKYO, new Instant(0L)).getClass());
        
        DateTime cutover = new DateTime(1582, 10, 15, 0, 0, 0, 0, DateTimeZone.UTC);
        chrono = GJChronology.getInstance(TOKYO, null);
        assertEquals(TOKYO, chrono.getZone());
        assertEquals(cutover.toInstant(), chrono.getGregorianCutover());
    }

// org.joda.time.chrono.TestGJChronology::testFactory_Zone_RI_int
    public void testFactory_Zone_RI_int() {
        GJChronology chrono = GJChronology.getInstance(TOKYO, new Instant(0L), 2);
        assertEquals(TOKYO, chrono.getZone());
        assertEquals(new Instant(0L), chrono.getGregorianCutover());
        assertEquals(2, chrono.getMinimumDaysInFirstWeek());
        assertSame(GJChronology.class, GJChronology.getInstance(TOKYO, new Instant(0L), 2).getClass());
        
        DateTime cutover = new DateTime(1582, 10, 15, 0, 0, 0, 0, DateTimeZone.UTC);
        chrono = GJChronology.getInstance(TOKYO, null, 2);
        assertEquals(TOKYO, chrono.getZone());
        assertEquals(cutover.toInstant(), chrono.getGregorianCutover());
        assertEquals(2, chrono.getMinimumDaysInFirstWeek());
        
        try {
            GJChronology.getInstance(TOKYO, new Instant(0L), 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            GJChronology.getInstance(TOKYO, new Instant(0L), 8);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.chrono.TestGJChronology::testEquality
    public void testEquality() {
        assertSame(GJChronology.getInstance(TOKYO), GJChronology.getInstance(TOKYO));
        assertSame(GJChronology.getInstance(LONDON), GJChronology.getInstance(LONDON));
        assertSame(GJChronology.getInstance(PARIS), GJChronology.getInstance(PARIS));
        assertSame(GJChronology.getInstanceUTC(), GJChronology.getInstanceUTC());
        assertSame(GJChronology.getInstance(), GJChronology.getInstance(LONDON));
    }

// org.joda.time.chrono.TestGJChronology::testWithUTC
    public void testWithUTC() {
        assertSame(GJChronology.getInstanceUTC(), GJChronology.getInstance(LONDON).withUTC());
        assertSame(GJChronology.getInstanceUTC(), GJChronology.getInstance(TOKYO).withUTC());
        assertSame(GJChronology.getInstanceUTC(), GJChronology.getInstanceUTC().withUTC());
        assertSame(GJChronology.getInstanceUTC(), GJChronology.getInstance().withUTC());
    }

// org.joda.time.chrono.TestGJChronology::testWithZone
    public void testWithZone() {
        assertSame(GJChronology.getInstance(TOKYO), GJChronology.getInstance(TOKYO).withZone(TOKYO));
        assertSame(GJChronology.getInstance(LONDON), GJChronology.getInstance(TOKYO).withZone(LONDON));
        assertSame(GJChronology.getInstance(PARIS), GJChronology.getInstance(TOKYO).withZone(PARIS));
        assertSame(GJChronology.getInstance(LONDON), GJChronology.getInstance(TOKYO).withZone(null));
        assertSame(GJChronology.getInstance(PARIS), GJChronology.getInstance().withZone(PARIS));
        assertSame(GJChronology.getInstance(PARIS), GJChronology.getInstanceUTC().withZone(PARIS));
    }

// org.joda.time.chrono.TestGJChronology::testToString
    public void testToString() {
        assertEquals("GJChronology[Europe/London]", GJChronology.getInstance(LONDON).toString());
        assertEquals("GJChronology[Asia/Tokyo]", GJChronology.getInstance(TOKYO).toString());
        assertEquals("GJChronology[Europe/London]", GJChronology.getInstance().toString());
        assertEquals("GJChronology[UTC]", GJChronology.getInstanceUTC().toString());
        assertEquals("GJChronology[UTC,cutover=1970-01-01]", GJChronology.getInstance(DateTimeZone.UTC, 0L, 4).toString());
        assertEquals("GJChronology[UTC,cutover=1970-01-01T00:00:00.001Z,mdfw=2]", GJChronology.getInstance(DateTimeZone.UTC, 1L, 2).toString());
    }

// org.joda.time.chrono.TestGJChronology::testDurationFields
    public void testDurationFields() {
        final GJChronology gj = GJChronology.getInstance();
        assertEquals("eras", gj.eras().getName());
        assertEquals("centuries", gj.centuries().getName());
        assertEquals("years", gj.years().getName());
        assertEquals("weekyears", gj.weekyears().getName());
        assertEquals("months", gj.months().getName());
        assertEquals("weeks", gj.weeks().getName());
        assertEquals("halfdays", gj.halfdays().getName());
        assertEquals("days", gj.days().getName());
        assertEquals("hours", gj.hours().getName());
        assertEquals("minutes", gj.minutes().getName());
        assertEquals("seconds", gj.seconds().getName());
        assertEquals("millis", gj.millis().getName());
        
        assertEquals(false, gj.eras().isSupported());
        assertEquals(true, gj.centuries().isSupported());
        assertEquals(true, gj.years().isSupported());
        assertEquals(true, gj.weekyears().isSupported());
        assertEquals(true, gj.months().isSupported());
        assertEquals(true, gj.weeks().isSupported());
        assertEquals(true, gj.days().isSupported());
        assertEquals(true, gj.halfdays().isSupported());
        assertEquals(true, gj.hours().isSupported());
        assertEquals(true, gj.minutes().isSupported());
        assertEquals(true, gj.seconds().isSupported());
        assertEquals(true, gj.millis().isSupported());
        
        assertEquals(false, gj.centuries().isPrecise());
        assertEquals(false, gj.years().isPrecise());
        assertEquals(false, gj.weekyears().isPrecise());
        assertEquals(false, gj.months().isPrecise());
        assertEquals(false, gj.weeks().isPrecise());
        assertEquals(false, gj.days().isPrecise());
        assertEquals(false, gj.halfdays().isPrecise());
        assertEquals(true, gj.hours().isPrecise());
        assertEquals(true, gj.minutes().isPrecise());
        assertEquals(true, gj.seconds().isPrecise());
        assertEquals(true, gj.millis().isPrecise());
        
        final GJChronology gjUTC = GJChronology.getInstanceUTC();
        assertEquals(false, gjUTC.centuries().isPrecise());
        assertEquals(false, gjUTC.years().isPrecise());
        assertEquals(false, gjUTC.weekyears().isPrecise());
        assertEquals(false, gjUTC.months().isPrecise());
        assertEquals(true, gjUTC.weeks().isPrecise());
        assertEquals(true, gjUTC.days().isPrecise());
        assertEquals(true, gjUTC.halfdays().isPrecise());
        assertEquals(true, gjUTC.hours().isPrecise());
        assertEquals(true, gjUTC.minutes().isPrecise());
        assertEquals(true, gjUTC.seconds().isPrecise());
        assertEquals(true, gjUTC.millis().isPrecise());
        
        final DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        final GJChronology gjGMT = GJChronology.getInstance(gmt);
        assertEquals(false, gjGMT.centuries().isPrecise());
        assertEquals(false, gjGMT.years().isPrecise());
        assertEquals(false, gjGMT.weekyears().isPrecise());
        assertEquals(false, gjGMT.months().isPrecise());
        assertEquals(true, gjGMT.weeks().isPrecise());
        assertEquals(true, gjGMT.days().isPrecise());
        assertEquals(true, gjGMT.halfdays().isPrecise());
        assertEquals(true, gjGMT.hours().isPrecise());
        assertEquals(true, gjGMT.minutes().isPrecise());
        assertEquals(true, gjGMT.seconds().isPrecise());
        assertEquals(true, gjGMT.millis().isPrecise());
    }

// org.joda.time.chrono.TestGJChronology::testDateFields
    public void testDateFields() {
        final GJChronology gj = GJChronology.getInstance();
        assertEquals("era", gj.era().getName());
        assertEquals("centuryOfEra", gj.centuryOfEra().getName());
        assertEquals("yearOfCentury", gj.yearOfCentury().getName());
        assertEquals("yearOfEra", gj.yearOfEra().getName());
        assertEquals("year", gj.year().getName());
        assertEquals("monthOfYear", gj.monthOfYear().getName());
        assertEquals("weekyearOfCentury", gj.weekyearOfCentury().getName());
        assertEquals("weekyear", gj.weekyear().getName());
        assertEquals("weekOfWeekyear", gj.weekOfWeekyear().getName());
        assertEquals("dayOfYear", gj.dayOfYear().getName());
        assertEquals("dayOfMonth", gj.dayOfMonth().getName());
        assertEquals("dayOfWeek", gj.dayOfWeek().getName());
        
        assertEquals(true, gj.era().isSupported());
        assertEquals(true, gj.centuryOfEra().isSupported());
        assertEquals(true, gj.yearOfCentury().isSupported());
        assertEquals(true, gj.yearOfEra().isSupported());
        assertEquals(true, gj.year().isSupported());
        assertEquals(true, gj.monthOfYear().isSupported());
        assertEquals(true, gj.weekyearOfCentury().isSupported());
        assertEquals(true, gj.weekyear().isSupported());
        assertEquals(true, gj.weekOfWeekyear().isSupported());
        assertEquals(true, gj.dayOfYear().isSupported());
        assertEquals(true, gj.dayOfMonth().isSupported());
        assertEquals(true, gj.dayOfWeek().isSupported());
        
        assertEquals(gj.eras(), gj.era().getDurationField());
        assertEquals(gj.centuries(), gj.centuryOfEra().getDurationField());
        assertEquals(gj.years(), gj.yearOfCentury().getDurationField());
        assertEquals(gj.years(), gj.yearOfEra().getDurationField());
        assertEquals(gj.years(), gj.year().getDurationField());
        assertEquals(gj.months(), gj.monthOfYear().getDurationField());
        assertEquals(gj.weekyears(), gj.weekyearOfCentury().getDurationField());
        assertEquals(gj.weekyears(), gj.weekyear().getDurationField());
        assertEquals(gj.weeks(), gj.weekOfWeekyear().getDurationField());
        assertEquals(gj.days(), gj.dayOfYear().getDurationField());
        assertEquals(gj.days(), gj.dayOfMonth().getDurationField());
        assertEquals(gj.days(), gj.dayOfWeek().getDurationField());
        
        assertEquals(null, gj.era().getRangeDurationField());
        assertEquals(gj.eras(), gj.centuryOfEra().getRangeDurationField());
        assertEquals(gj.centuries(), gj.yearOfCentury().getRangeDurationField());
        assertEquals(gj.eras(), gj.yearOfEra().getRangeDurationField());
        assertEquals(null, gj.year().getRangeDurationField());
        assertEquals(gj.years(), gj.monthOfYear().getRangeDurationField());
        assertEquals(gj.centuries(), gj.weekyearOfCentury().getRangeDurationField());
        assertEquals(null, gj.weekyear().getRangeDurationField());
        assertEquals(gj.weekyears(), gj.weekOfWeekyear().getRangeDurationField());
        assertEquals(gj.years(), gj.dayOfYear().getRangeDurationField());
        assertEquals(gj.months(), gj.dayOfMonth().getRangeDurationField());
        assertEquals(gj.weeks(), gj.dayOfWeek().getRangeDurationField());
    }

// org.joda.time.chrono.TestGJChronology::testTimeFields
    public void testTimeFields() {
        final GJChronology gj = GJChronology.getInstance();
        assertEquals("halfdayOfDay", gj.halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", gj.clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", gj.hourOfHalfday().getName());
        assertEquals("clockhourOfDay", gj.clockhourOfDay().getName());
        assertEquals("hourOfDay", gj.hourOfDay().getName());
        assertEquals("minuteOfDay", gj.minuteOfDay().getName());
        assertEquals("minuteOfHour", gj.minuteOfHour().getName());
        assertEquals("secondOfDay", gj.secondOfDay().getName());
        assertEquals("secondOfMinute", gj.secondOfMinute().getName());
        assertEquals("millisOfDay", gj.millisOfDay().getName());
        assertEquals("millisOfSecond", gj.millisOfSecond().getName());
        
        assertEquals(true, gj.halfdayOfDay().isSupported());
        assertEquals(true, gj.clockhourOfHalfday().isSupported());
        assertEquals(true, gj.hourOfHalfday().isSupported());
        assertEquals(true, gj.clockhourOfDay().isSupported());
        assertEquals(true, gj.hourOfDay().isSupported());
        assertEquals(true, gj.minuteOfDay().isSupported());
        assertEquals(true, gj.minuteOfHour().isSupported());
        assertEquals(true, gj.secondOfDay().isSupported());
        assertEquals(true, gj.secondOfMinute().isSupported());
        assertEquals(true, gj.millisOfDay().isSupported());
        assertEquals(true, gj.millisOfSecond().isSupported());
    }

// org.joda.time.chrono.TestGJChronology::testIllegalDates
    public void testIllegalDates() {
        try {
            new DateTime(1582, 10, 5, 0, 0, 0, 0, GJChronology.getInstance(DateTimeZone.UTC));
            fail("Constructed illegal date");
        } catch (IllegalArgumentException e) {  }

        try {
            new DateTime(1582, 10, 14, 0, 0, 0, 0, GJChronology.getInstance(DateTimeZone.UTC));
            fail("Constructed illegal date");
        } catch (IllegalArgumentException e) {  }
    }

// org.joda.time.chrono.TestGJChronology::testParseEquivalence
    public void testParseEquivalence() {
        testParse("1581-01-01T01:23:45.678", 1581, 1, 1, 1, 23, 45, 678);
        testParse("1581-06-30", 1581, 6, 30, 0, 0, 0, 0);
        testParse("1582-01-01T01:23:45.678", 1582, 1, 1, 1, 23, 45, 678);
        testParse("1582-06-30T01:23:45.678", 1582, 6, 30, 1, 23, 45, 678);
        testParse("1582-10-04", 1582, 10, 4, 0, 0, 0, 0);
        testParse("1582-10-15", 1582, 10, 15, 0, 0, 0, 0);
        testParse("1582-12-31", 1582, 12, 31, 0, 0, 0, 0);
        testParse("1583-12-31", 1583, 12, 31, 0, 0, 0, 0);
    }

// org.joda.time.chrono.TestGJChronology::testCutoverAddYears
    public void testCutoverAddYears() {
        testAdd("1582-01-01", DurationFieldType.years(), 1, "1583-01-01");
        testAdd("1582-02-15", DurationFieldType.years(), 1, "1583-02-15");
        testAdd("1582-02-28", DurationFieldType.years(), 1, "1583-02-28");
        testAdd("1582-03-01", DurationFieldType.years(), 1, "1583-03-01");
        testAdd("1582-09-30", DurationFieldType.years(), 1, "1583-09-30");
        testAdd("1582-10-01", DurationFieldType.years(), 1, "1583-10-01");
        testAdd("1582-10-04", DurationFieldType.years(), 1, "1583-10-04");
        testAdd("1582-10-15", DurationFieldType.years(), 1, "1583-10-15");
        testAdd("1582-10-16", DurationFieldType.years(), 1, "1583-10-16");

        
        testAdd("1580-01-01", DurationFieldType.years(), 4, "1584-01-01");
        testAdd("1580-02-29", DurationFieldType.years(), 4, "1584-02-29");
        testAdd("1580-10-01", DurationFieldType.years(), 4, "1584-10-01");
        testAdd("1580-10-10", DurationFieldType.years(), 4, "1584-10-10");
        testAdd("1580-10-15", DurationFieldType.years(), 4, "1584-10-15");
        testAdd("1580-12-31", DurationFieldType.years(), 4, "1584-12-31");
    }

// org.joda.time.chrono.TestGJChronology::testCutoverAddWeekyears
    public void testCutoverAddWeekyears() {
        testAdd("1582-W01-1", DurationFieldType.weekyears(), 1, "1583-W01-1");
        testAdd("1582-W39-1", DurationFieldType.weekyears(), 1, "1583-W39-1");
        testAdd("1583-W45-1", DurationFieldType.weekyears(), 1, "1584-W45-1");

        
        
        
        
        
        
        
        
        

        
        testAdd("1580-W01-1", DurationFieldType.weekyears(), 4, "1584-W01-1");
        testAdd("1580-W30-7", DurationFieldType.weekyears(), 4, "1584-W30-7");
        testAdd("1580-W50-7", DurationFieldType.weekyears(), 4, "1584-W50-7");
    }

// org.joda.time.chrono.TestGJChronology::testCutoverAddMonths
    public void testCutoverAddMonths() {
        testAdd("1582-01-01", DurationFieldType.months(), 1, "1582-02-01");
        testAdd("1582-01-01", DurationFieldType.months(), 6, "1582-07-01");
        testAdd("1582-01-01", DurationFieldType.months(), 12, "1583-01-01");
        testAdd("1582-11-15", DurationFieldType.months(), 1, "1582-12-15");

        testAdd("1582-09-04", DurationFieldType.months(), 2, "1582-11-04");
        testAdd("1582-09-05", DurationFieldType.months(), 2, "1582-11-05");
        testAdd("1582-09-10", DurationFieldType.months(), 2, "1582-11-10");
        testAdd("1582-09-15", DurationFieldType.months(), 2, "1582-11-15");

        
        testAdd("1580-01-01", DurationFieldType.months(), 48, "1584-01-01");
        testAdd("1580-02-29", DurationFieldType.months(), 48, "1584-02-29");
        testAdd("1580-10-01", DurationFieldType.months(), 48, "1584-10-01");
        testAdd("1580-10-10", DurationFieldType.months(), 48, "1584-10-10");
        testAdd("1580-10-15", DurationFieldType.months(), 48, "1584-10-15");
        testAdd("1580-12-31", DurationFieldType.months(), 48, "1584-12-31");
    }

// org.joda.time.chrono.TestGJChronology::testCutoverAddWeeks
    public void testCutoverAddWeeks() {
        testAdd("1582-01-01", DurationFieldType.weeks(), 1, "1582-01-08");
        testAdd("1583-01-01", DurationFieldType.weeks(), 1, "1583-01-08");

        
        testAdd("1582-10-01", DurationFieldType.weeks(), 2, "1582-10-25");
        testAdd("1582-W01-1", DurationFieldType.weeks(), 51, "1583-W01-1");
    }

// org.joda.time.chrono.TestGJChronology::testCutoverAddDays
    public void testCutoverAddDays() {
        testAdd("1582-10-03", DurationFieldType.days(), 1, "1582-10-04");
        testAdd("1582-10-04", DurationFieldType.days(), 1, "1582-10-15");
        testAdd("1582-10-15", DurationFieldType.days(), 1, "1582-10-16");

        testAdd("1582-09-30", DurationFieldType.days(), 10, "1582-10-20");
        testAdd("1582-10-04", DurationFieldType.days(), 10, "1582-10-24");
        testAdd("1582-10-15", DurationFieldType.days(), 10, "1582-10-25");
    }

// org.joda.time.chrono.TestGJChronology::testYearEndAddDays
    public void testYearEndAddDays() {
        testAdd("1582-11-05", DurationFieldType.days(), 28, "1582-12-03");
        testAdd("1582-12-05", DurationFieldType.days(), 28, "1583-01-02");
        
        testAdd("2005-11-05", DurationFieldType.days(), 28, "2005-12-03");
        testAdd("2005-12-05", DurationFieldType.days(), 28, "2006-01-02");
    }

// org.joda.time.chrono.TestGJChronology::testSubtractDays
    public void testSubtractDays() {
        
        
        
        DateTime dt = new DateTime
            (1112306400000L, GJChronology.getInstance(DateTimeZone.forID("Europe/Berlin")));
        YearMonthDay ymd = dt.toYearMonthDay();
        while (ymd.toDateTimeAtMidnight().getDayOfWeek() != DateTimeConstants.MONDAY) { 
            ymd = ymd.minus(Period.days(1));
        }
    }

// org.joda.time.chrono.TestGJChronology::testTimeOfDayAdd
    public void testTimeOfDayAdd() {
        TimeOfDay start = new TimeOfDay(12, 30, GJChronology.getInstance());
        TimeOfDay end = new TimeOfDay(10, 30, GJChronology.getInstance());
        assertEquals(end, start.plusHours(22));
        assertEquals(start, end.minusHours(22));
        assertEquals(end, start.plusMinutes(22 * 60));
        assertEquals(start, end.minusMinutes(22 * 60));
    }

// org.joda.time.chrono.TestGJChronology::testMaximumValue
    public void testMaximumValue() {
        DateMidnight dt = new DateMidnight(1570, 1, 1, GJChronology.getInstance());
        while (dt.getYear() < 1590) {
            dt = dt.plusDays(1);
            YearMonthDay ymd = dt.toYearMonthDay();
            assertEquals(dt.year().getMaximumValue(), ymd.year().getMaximumValue());
            assertEquals(dt.monthOfYear().getMaximumValue(), ymd.monthOfYear().getMaximumValue());
            assertEquals(dt.dayOfMonth().getMaximumValue(), ymd.dayOfMonth().getMaximumValue());
        }
    }

// org.joda.time.chrono.TestGJChronology::testPartialGetAsText
    public void testPartialGetAsText() {
        GJChronology chrono = GJChronology.getInstance(TOKYO);
        assertEquals("January", new YearMonthDay("2005-01-01", chrono).monthOfYear().getAsText());
        assertEquals("Jan", new YearMonthDay("2005-01-01", chrono).monthOfYear().getAsShortText());
    }

// org.joda.time.chrono.TestGJChronology::testLeapYearRulesConstruction
    public void testLeapYearRulesConstruction() {
        
        DateMidnight dt = new DateMidnight(1500, 2, 29, GJChronology.getInstanceUTC());
        assertEquals(dt.getYear(), 1500);
        assertEquals(dt.getMonthOfYear(), 2);
        assertEquals(dt.getDayOfMonth(), 29);
    }

// org.joda.time.chrono.TestGJChronology::testLeapYearRulesConstructionInvalid
    public void testLeapYearRulesConstructionInvalid() {
        
        try {
            new DateMidnight(1500, 2, 30, GJChronology.getInstanceUTC());
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.chrono.TestGJDate::test_plusYears_positiveToPositive
    public void test_plusYears_positiveToPositive() {
        LocalDate date = new LocalDate(3, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(7, 6, 30, GJ_CHRONOLOGY);
        assertEquals(expected, date.plusYears(4));
    }
