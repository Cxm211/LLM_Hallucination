// buggy code
    protected BasePeriod(long duration) {
        this(duration, null, null);
        // bug [3264409]
    }

// relevant test
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
        assertEquals("eras", BuddhistChronology.getInstance().eras().getName());
        assertEquals("centuries", BuddhistChronology.getInstance().centuries().getName());
        assertEquals("years", BuddhistChronology.getInstance().years().getName());
        assertEquals("weekyears", BuddhistChronology.getInstance().weekyears().getName());
        assertEquals("months", BuddhistChronology.getInstance().months().getName());
        assertEquals("weeks", BuddhistChronology.getInstance().weeks().getName());
        assertEquals("days", BuddhistChronology.getInstance().days().getName());
        assertEquals("halfdays", GregorianChronology.getInstance().halfdays().getName());
        assertEquals("hours", BuddhistChronology.getInstance().hours().getName());
        assertEquals("minutes", BuddhistChronology.getInstance().minutes().getName());
        assertEquals("seconds", BuddhistChronology.getInstance().seconds().getName());
        assertEquals("millis", BuddhistChronology.getInstance().millis().getName());
        
        assertEquals(false, BuddhistChronology.getInstance().eras().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().centuries().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().years().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().weekyears().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().months().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().weeks().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().days().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().halfdays().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().hours().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().minutes().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().seconds().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().millis().isSupported());
        
        assertEquals(false, BuddhistChronology.getInstance().centuries().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance().years().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance().weekyears().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance().months().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance().weeks().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance().days().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance().halfdays().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance().hours().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance().minutes().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance().seconds().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance().millis().isPrecise());
        
        assertEquals(false, BuddhistChronology.getInstanceUTC().centuries().isPrecise());
        assertEquals(false, BuddhistChronology.getInstanceUTC().years().isPrecise());
        assertEquals(false, BuddhistChronology.getInstanceUTC().weekyears().isPrecise());
        assertEquals(false, BuddhistChronology.getInstanceUTC().months().isPrecise());
        assertEquals(true, BuddhistChronology.getInstanceUTC().weeks().isPrecise());
        assertEquals(true, BuddhistChronology.getInstanceUTC().days().isPrecise());
        assertEquals(true, BuddhistChronology.getInstanceUTC().halfdays().isPrecise());
        assertEquals(true, BuddhistChronology.getInstanceUTC().hours().isPrecise());
        assertEquals(true, BuddhistChronology.getInstanceUTC().minutes().isPrecise());
        assertEquals(true, BuddhistChronology.getInstanceUTC().seconds().isPrecise());
        assertEquals(true, BuddhistChronology.getInstanceUTC().millis().isPrecise());
        
        DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        assertEquals(false, BuddhistChronology.getInstance(gmt).centuries().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance(gmt).years().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance(gmt).weekyears().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance(gmt).months().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance(gmt).weeks().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance(gmt).days().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance(gmt).halfdays().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance(gmt).hours().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance(gmt).minutes().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance(gmt).seconds().isPrecise());
        assertEquals(true, BuddhistChronology.getInstance(gmt).millis().isPrecise());
    }

// org.joda.time.chrono.TestBuddhistChronology::testDateFields
    public void testDateFields() {
        assertEquals("era", BuddhistChronology.getInstance().era().getName());
        assertEquals("centuryOfEra", BuddhistChronology.getInstance().centuryOfEra().getName());
        assertEquals("yearOfCentury", BuddhistChronology.getInstance().yearOfCentury().getName());
        assertEquals("yearOfEra", BuddhistChronology.getInstance().yearOfEra().getName());
        assertEquals("year", BuddhistChronology.getInstance().year().getName());
        assertEquals("monthOfYear", BuddhistChronology.getInstance().monthOfYear().getName());
        assertEquals("weekyearOfCentury", BuddhistChronology.getInstance().weekyearOfCentury().getName());
        assertEquals("weekyear", BuddhistChronology.getInstance().weekyear().getName());
        assertEquals("weekOfWeekyear", BuddhistChronology.getInstance().weekOfWeekyear().getName());
        assertEquals("dayOfYear", BuddhistChronology.getInstance().dayOfYear().getName());
        assertEquals("dayOfMonth", BuddhistChronology.getInstance().dayOfMonth().getName());
        assertEquals("dayOfWeek", BuddhistChronology.getInstance().dayOfWeek().getName());
        
        assertEquals(true, BuddhistChronology.getInstance().era().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().centuryOfEra().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().yearOfCentury().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().yearOfEra().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().year().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().monthOfYear().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().weekyearOfCentury().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().weekyear().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().weekOfWeekyear().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().dayOfYear().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().dayOfMonth().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().dayOfWeek().isSupported());
    }

// org.joda.time.chrono.TestBuddhistChronology::testTimeFields
    public void testTimeFields() {
        assertEquals("halfdayOfDay", BuddhistChronology.getInstance().halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", BuddhistChronology.getInstance().clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", BuddhistChronology.getInstance().hourOfHalfday().getName());
        assertEquals("clockhourOfDay", BuddhistChronology.getInstance().clockhourOfDay().getName());
        assertEquals("hourOfDay", BuddhistChronology.getInstance().hourOfDay().getName());
        assertEquals("minuteOfDay", BuddhistChronology.getInstance().minuteOfDay().getName());
        assertEquals("minuteOfHour", BuddhistChronology.getInstance().minuteOfHour().getName());
        assertEquals("secondOfDay", BuddhistChronology.getInstance().secondOfDay().getName());
        assertEquals("secondOfMinute", BuddhistChronology.getInstance().secondOfMinute().getName());
        assertEquals("millisOfDay", BuddhistChronology.getInstance().millisOfDay().getName());
        assertEquals("millisOfSecond", BuddhistChronology.getInstance().millisOfSecond().getName());
        
        assertEquals(true, BuddhistChronology.getInstance().halfdayOfDay().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().clockhourOfHalfday().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().hourOfHalfday().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().clockhourOfDay().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().hourOfDay().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().minuteOfDay().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().minuteOfHour().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().secondOfDay().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().secondOfMinute().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().millisOfDay().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().millisOfSecond().isSupported());
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
        DateTimeField gjYearOfEra = GJ_UTC.yearOfEra();
        DateTimeField gjEra = GJ_UTC.era();
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
        assertEquals("eras", GJChronology.getInstance().eras().getName());
        assertEquals("centuries", GJChronology.getInstance().centuries().getName());
        assertEquals("years", GJChronology.getInstance().years().getName());
        assertEquals("weekyears", GJChronology.getInstance().weekyears().getName());
        assertEquals("months", GJChronology.getInstance().months().getName());
        assertEquals("weeks", GJChronology.getInstance().weeks().getName());
        assertEquals("halfdays", GJChronology.getInstance().halfdays().getName());
        assertEquals("days", GJChronology.getInstance().days().getName());
        assertEquals("hours", GJChronology.getInstance().hours().getName());
        assertEquals("minutes", GJChronology.getInstance().minutes().getName());
        assertEquals("seconds", GJChronology.getInstance().seconds().getName());
        assertEquals("millis", GJChronology.getInstance().millis().getName());
        
        assertEquals(false, GJChronology.getInstance().eras().isSupported());
        assertEquals(true, GJChronology.getInstance().centuries().isSupported());
        assertEquals(true, GJChronology.getInstance().years().isSupported());
        assertEquals(true, GJChronology.getInstance().weekyears().isSupported());
        assertEquals(true, GJChronology.getInstance().months().isSupported());
        assertEquals(true, GJChronology.getInstance().weeks().isSupported());
        assertEquals(true, GJChronology.getInstance().days().isSupported());
        assertEquals(true, GJChronology.getInstance().halfdays().isSupported());
        assertEquals(true, GJChronology.getInstance().hours().isSupported());
        assertEquals(true, GJChronology.getInstance().minutes().isSupported());
        assertEquals(true, GJChronology.getInstance().seconds().isSupported());
        assertEquals(true, GJChronology.getInstance().millis().isSupported());
        
        assertEquals(false, GJChronology.getInstance().centuries().isPrecise());
        assertEquals(false, GJChronology.getInstance().years().isPrecise());
        assertEquals(false, GJChronology.getInstance().weekyears().isPrecise());
        assertEquals(false, GJChronology.getInstance().months().isPrecise());
        assertEquals(false, GJChronology.getInstance().weeks().isPrecise());
        assertEquals(false, GJChronology.getInstance().days().isPrecise());
        assertEquals(false, GJChronology.getInstance().halfdays().isPrecise());
        assertEquals(true, GJChronology.getInstance().hours().isPrecise());
        assertEquals(true, GJChronology.getInstance().minutes().isPrecise());
        assertEquals(true, GJChronology.getInstance().seconds().isPrecise());
        assertEquals(true, GJChronology.getInstance().millis().isPrecise());
        
        assertEquals(false, GJChronology.getInstanceUTC().centuries().isPrecise());
        assertEquals(false, GJChronology.getInstanceUTC().years().isPrecise());
        assertEquals(false, GJChronology.getInstanceUTC().weekyears().isPrecise());
        assertEquals(false, GJChronology.getInstanceUTC().months().isPrecise());
        assertEquals(true, GJChronology.getInstanceUTC().weeks().isPrecise());
        assertEquals(true, GJChronology.getInstanceUTC().days().isPrecise());
        assertEquals(true, GJChronology.getInstanceUTC().halfdays().isPrecise());
        assertEquals(true, GJChronology.getInstanceUTC().hours().isPrecise());
        assertEquals(true, GJChronology.getInstanceUTC().minutes().isPrecise());
        assertEquals(true, GJChronology.getInstanceUTC().seconds().isPrecise());
        assertEquals(true, GJChronology.getInstanceUTC().millis().isPrecise());
        
        DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        assertEquals(false, GJChronology.getInstance(gmt).centuries().isPrecise());
        assertEquals(false, GJChronology.getInstance(gmt).years().isPrecise());
        assertEquals(false, GJChronology.getInstance(gmt).weekyears().isPrecise());
        assertEquals(false, GJChronology.getInstance(gmt).months().isPrecise());
        assertEquals(true, GJChronology.getInstance(gmt).weeks().isPrecise());
        assertEquals(true, GJChronology.getInstance(gmt).days().isPrecise());
        assertEquals(true, GJChronology.getInstance(gmt).halfdays().isPrecise());
        assertEquals(true, GJChronology.getInstance(gmt).hours().isPrecise());
        assertEquals(true, GJChronology.getInstance(gmt).minutes().isPrecise());
        assertEquals(true, GJChronology.getInstance(gmt).seconds().isPrecise());
        assertEquals(true, GJChronology.getInstance(gmt).millis().isPrecise());
    }

// org.joda.time.chrono.TestGJChronology::testDateFields
    public void testDateFields() {
        assertEquals("era", GJChronology.getInstance().era().getName());
        assertEquals("centuryOfEra", GJChronology.getInstance().centuryOfEra().getName());
        assertEquals("yearOfCentury", GJChronology.getInstance().yearOfCentury().getName());
        assertEquals("yearOfEra", GJChronology.getInstance().yearOfEra().getName());
        assertEquals("year", GJChronology.getInstance().year().getName());
        assertEquals("monthOfYear", GJChronology.getInstance().monthOfYear().getName());
        assertEquals("weekyearOfCentury", GJChronology.getInstance().weekyearOfCentury().getName());
        assertEquals("weekyear", GJChronology.getInstance().weekyear().getName());
        assertEquals("weekOfWeekyear", GJChronology.getInstance().weekOfWeekyear().getName());
        assertEquals("dayOfYear", GJChronology.getInstance().dayOfYear().getName());
        assertEquals("dayOfMonth", GJChronology.getInstance().dayOfMonth().getName());
        assertEquals("dayOfWeek", GJChronology.getInstance().dayOfWeek().getName());
        
        assertEquals(true, GJChronology.getInstance().era().isSupported());
        assertEquals(true, GJChronology.getInstance().centuryOfEra().isSupported());
        assertEquals(true, GJChronology.getInstance().yearOfCentury().isSupported());
        assertEquals(true, GJChronology.getInstance().yearOfEra().isSupported());
        assertEquals(true, GJChronology.getInstance().year().isSupported());
        assertEquals(true, GJChronology.getInstance().monthOfYear().isSupported());
        assertEquals(true, GJChronology.getInstance().weekyearOfCentury().isSupported());
        assertEquals(true, GJChronology.getInstance().weekyear().isSupported());
        assertEquals(true, GJChronology.getInstance().weekOfWeekyear().isSupported());
        assertEquals(true, GJChronology.getInstance().dayOfYear().isSupported());
        assertEquals(true, GJChronology.getInstance().dayOfMonth().isSupported());
        assertEquals(true, GJChronology.getInstance().dayOfWeek().isSupported());
    }

// org.joda.time.chrono.TestGJChronology::testTimeFields
    public void testTimeFields() {
        assertEquals("halfdayOfDay", GJChronology.getInstance().halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", GJChronology.getInstance().clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", GJChronology.getInstance().hourOfHalfday().getName());
        assertEquals("clockhourOfDay", GJChronology.getInstance().clockhourOfDay().getName());
        assertEquals("hourOfDay", GJChronology.getInstance().hourOfDay().getName());
        assertEquals("minuteOfDay", GJChronology.getInstance().minuteOfDay().getName());
        assertEquals("minuteOfHour", GJChronology.getInstance().minuteOfHour().getName());
        assertEquals("secondOfDay", GJChronology.getInstance().secondOfDay().getName());
        assertEquals("secondOfMinute", GJChronology.getInstance().secondOfMinute().getName());
        assertEquals("millisOfDay", GJChronology.getInstance().millisOfDay().getName());
        assertEquals("millisOfSecond", GJChronology.getInstance().millisOfSecond().getName());
        
        assertEquals(true, GJChronology.getInstance().halfdayOfDay().isSupported());
        assertEquals(true, GJChronology.getInstance().clockhourOfHalfday().isSupported());
        assertEquals(true, GJChronology.getInstance().hourOfHalfday().isSupported());
        assertEquals(true, GJChronology.getInstance().clockhourOfDay().isSupported());
        assertEquals(true, GJChronology.getInstance().hourOfDay().isSupported());
        assertEquals(true, GJChronology.getInstance().minuteOfDay().isSupported());
        assertEquals(true, GJChronology.getInstance().minuteOfHour().isSupported());
        assertEquals(true, GJChronology.getInstance().secondOfDay().isSupported());
        assertEquals(true, GJChronology.getInstance().secondOfMinute().isSupported());
        assertEquals(true, GJChronology.getInstance().millisOfDay().isSupported());
        assertEquals(true, GJChronology.getInstance().millisOfSecond().isSupported());
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

// org.joda.time.convert.TestConverterManager::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ConverterManager.class;
        assertEquals(true, Modifier.isPublic(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(true, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestConverterManager::testGetInstantConverter
    public void testGetInstantConverter() {
        InstantConverter c = ConverterManager.getInstance().getInstantConverter(new Long(0L));
        assertEquals(Long.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getInstantConverter(new DateTime());
        assertEquals(ReadableInstant.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getInstantConverter("");
        assertEquals(String.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getInstantConverter(new Date());
        assertEquals(Date.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getInstantConverter(new GregorianCalendar());
        assertEquals(Calendar.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getInstantConverter(null);
        assertEquals(null, c.getSupportedType());
        
        try {
            ConverterManager.getInstance().getInstantConverter(Boolean.TRUE);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestConverterManager::testGetInstantConverterRemovedNull
    public void testGetInstantConverterRemovedNull() {
        try {
            ConverterManager.getInstance().removeInstantConverter(NullConverter.INSTANCE);
            try {
                ConverterManager.getInstance().getInstantConverter(null);
                fail();
            } catch (IllegalArgumentException ex) {}
        } finally {
            ConverterManager.getInstance().addInstantConverter(NullConverter.INSTANCE);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetInstantConverterOKMultipleMatches
    public void testGetInstantConverterOKMultipleMatches() {
        InstantConverter c = new InstantConverter() {
            public long getInstantMillis(Object object, Chronology chrono) {return 0;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return ReadableDateTime.class;}
        };
        try {
            ConverterManager.getInstance().addInstantConverter(c);
            InstantConverter ok = ConverterManager.getInstance().getInstantConverter(new DateTime());
            
            assertEquals(ReadableDateTime.class, ok.getSupportedType());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(c);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetInstantConverterBadMultipleMatches
    public void testGetInstantConverterBadMultipleMatches() {
        InstantConverter c = new InstantConverter() {
            public long getInstantMillis(Object object, Chronology chrono) {return 0;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Serializable.class;}
        };
        try {
            ConverterManager.getInstance().addInstantConverter(c);
            try {
                ConverterManager.getInstance().getInstantConverter(new DateTime());
                fail();
            } catch (IllegalStateException ex) {
                
            }
        } finally {
            ConverterManager.getInstance().removeInstantConverter(c);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetInstantConverters
    public void testGetInstantConverters() {
        InstantConverter[] array = ConverterManager.getInstance().getInstantConverters();
        assertEquals(6, array.length);
    }

// org.joda.time.convert.TestConverterManager::testAddInstantConverter1
    public void testAddInstantConverter1() {
        InstantConverter c = new InstantConverter() {
            public long getInstantMillis(Object object, Chronology chrono) {return 0;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        try {
            InstantConverter removed = ConverterManager.getInstance().addInstantConverter(c);
            assertEquals(null, removed);
            assertEquals(Boolean.class, ConverterManager.getInstance().getInstantConverter(Boolean.TRUE).getSupportedType());
            assertEquals(7, ConverterManager.getInstance().getInstantConverters().length);
        } finally {
            ConverterManager.getInstance().removeInstantConverter(c);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddInstantConverter2
    public void testAddInstantConverter2() {
        InstantConverter c = new InstantConverter() {
            public long getInstantMillis(Object object, Chronology chrono) {return 0;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return String.class;}
        };
        try {
            InstantConverter removed = ConverterManager.getInstance().addInstantConverter(c);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(String.class, ConverterManager.getInstance().getInstantConverter("").getSupportedType());
            assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
        } finally {
            ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddInstantConverter3
    public void testAddInstantConverter3() {
        InstantConverter removed = ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
        assertEquals(null, removed);
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddInstantConverter4
    public void testAddInstantConverter4() {
        InstantConverter removed = ConverterManager.getInstance().addInstantConverter(null);
        assertEquals(null, removed);
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddInstantConverterSecurity
    public void testAddInstantConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveInstantConverter1
    public void testRemoveInstantConverter1() {
        try {
            InstantConverter removed = ConverterManager.getInstance().removeInstantConverter(StringConverter.INSTANCE);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(5, ConverterManager.getInstance().getInstantConverters().length);
        } finally {
            ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveInstantConverter2
    public void testRemoveInstantConverter2() {
        InstantConverter c = new InstantConverter() {
            public long getInstantMillis(Object object, Chronology chrono) {return 0;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        InstantConverter removed = ConverterManager.getInstance().removeInstantConverter(c);
        assertEquals(null, removed);
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveInstantConverter3
    public void testRemoveInstantConverter3() {
        InstantConverter removed = ConverterManager.getInstance().removeInstantConverter(null);
        assertEquals(null, removed);
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveInstantConverterSecurity
    public void testRemoveInstantConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().removeInstantConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPartialConverter
    public void testGetPartialConverter() {
        PartialConverter c = ConverterManager.getInstance().getPartialConverter(new Long(0L));
        assertEquals(Long.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter(new TimeOfDay());
        assertEquals(ReadablePartial.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter(new DateTime());
        assertEquals(ReadableInstant.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter("");
        assertEquals(String.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter(new Date());
        assertEquals(Date.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter(new GregorianCalendar());
        assertEquals(Calendar.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter(null);
        assertEquals(null, c.getSupportedType());
        
        try {
            ConverterManager.getInstance().getPartialConverter(Boolean.TRUE);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestConverterManager::testGetPartialConverterRemovedNull
    public void testGetPartialConverterRemovedNull() {
        try {
            ConverterManager.getInstance().removePartialConverter(NullConverter.INSTANCE);
            try {
                ConverterManager.getInstance().getPartialConverter(null);
                fail();
            } catch (IllegalArgumentException ex) {}
        } finally {
            ConverterManager.getInstance().addPartialConverter(NullConverter.INSTANCE);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPartialConverterOKMultipleMatches
    public void testGetPartialConverterOKMultipleMatches() {
        PartialConverter c = new PartialConverter() {
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {return null;}
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {return null;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return ReadableDateTime.class;}
        };
        try {
            ConverterManager.getInstance().addPartialConverter(c);
            PartialConverter ok = ConverterManager.getInstance().getPartialConverter(new DateTime());
            
            assertEquals(ReadableDateTime.class, ok.getSupportedType());
        } finally {
            ConverterManager.getInstance().removePartialConverter(c);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPartialConverterBadMultipleMatches
    public void testGetPartialConverterBadMultipleMatches() {
        PartialConverter c = new PartialConverter() {
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {return null;}
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {return null;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Serializable.class;}
        };
        try {
            ConverterManager.getInstance().addPartialConverter(c);
            try {
                ConverterManager.getInstance().getPartialConverter(new DateTime());
                fail();
            } catch (IllegalStateException ex) {
                
            }
        } finally {
            ConverterManager.getInstance().removePartialConverter(c);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPartialConverters
    public void testGetPartialConverters() {
        PartialConverter[] array = ConverterManager.getInstance().getPartialConverters();
        assertEquals(PARTIAL_SIZE, array.length);
    }

// org.joda.time.convert.TestConverterManager::testAddPartialConverter1
    public void testAddPartialConverter1() {
        PartialConverter c = new PartialConverter() {
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {return null;}
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {return null;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        try {
            PartialConverter removed = ConverterManager.getInstance().addPartialConverter(c);
            assertEquals(null, removed);
            assertEquals(Boolean.class, ConverterManager.getInstance().getPartialConverter(Boolean.TRUE).getSupportedType());
            assertEquals(PARTIAL_SIZE + 1, ConverterManager.getInstance().getPartialConverters().length);
        } finally {
            ConverterManager.getInstance().removePartialConverter(c);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPartialConverter2
    public void testAddPartialConverter2() {
        PartialConverter c = new PartialConverter() {
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {return null;}
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {return null;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return String.class;}
        };
        try {
            PartialConverter removed = ConverterManager.getInstance().addPartialConverter(c);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(String.class, ConverterManager.getInstance().getPartialConverter("").getSupportedType());
            assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
        } finally {
            ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPartialConverter3
    public void testAddPartialConverter3() {
        PartialConverter removed = ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
        assertEquals(null, removed);
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPartialConverter4
    public void testAddPartialConverter4() {
        PartialConverter removed = ConverterManager.getInstance().addPartialConverter(null);
        assertEquals(null, removed);
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPartialConverterSecurity
    public void testAddPartialConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePartialConverter1
    public void testRemovePartialConverter1() {
        try {
            PartialConverter removed = ConverterManager.getInstance().removePartialConverter(StringConverter.INSTANCE);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(PARTIAL_SIZE - 1, ConverterManager.getInstance().getPartialConverters().length);
        } finally {
            ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePartialConverter2
    public void testRemovePartialConverter2() {
        PartialConverter c = new PartialConverter() {
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {return null;}
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {return null;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        PartialConverter removed = ConverterManager.getInstance().removePartialConverter(c);
        assertEquals(null, removed);
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePartialConverter3
    public void testRemovePartialConverter3() {
        PartialConverter removed = ConverterManager.getInstance().removePartialConverter(null);
        assertEquals(null, removed);
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePartialConverterSecurity
    public void testRemovePartialConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().removeInstantConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetDurationConverter
    public void testGetDurationConverter() {
        DurationConverter c = ConverterManager.getInstance().getDurationConverter(new Long(0L));
        assertEquals(Long.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getDurationConverter(new Duration(123L));
        assertEquals(ReadableDuration.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getDurationConverter(new Interval(0L, 1000L));
        assertEquals(ReadableInterval.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getDurationConverter("");
        assertEquals(String.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getDurationConverter(null);
        assertEquals(null, c.getSupportedType());
        
        try {
            ConverterManager.getInstance().getDurationConverter(Boolean.TRUE);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestConverterManager::testGetDurationConverterRemovedNull
    public void testGetDurationConverterRemovedNull() {
        try {
            ConverterManager.getInstance().removeDurationConverter(NullConverter.INSTANCE);
            try {
                ConverterManager.getInstance().getDurationConverter(null);
                fail();
            } catch (IllegalArgumentException ex) {}
        } finally {
            ConverterManager.getInstance().addDurationConverter(NullConverter.INSTANCE);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetDurationConverters
    public void testGetDurationConverters() {
        DurationConverter[] array = ConverterManager.getInstance().getDurationConverters();
        assertEquals(DURATION_SIZE, array.length);
    }

// org.joda.time.convert.TestConverterManager::testAddDurationConverter1
    public void testAddDurationConverter1() {
        DurationConverter c = new DurationConverter() {
            public long getDurationMillis(Object object) {return 0;}
            public Class getSupportedType() {return Boolean.class;}
        };
        try {
            DurationConverter removed = ConverterManager.getInstance().addDurationConverter(c);
            assertEquals(null, removed);
            assertEquals(Boolean.class, ConverterManager.getInstance().getDurationConverter(Boolean.TRUE).getSupportedType());
            assertEquals(DURATION_SIZE + 1, ConverterManager.getInstance().getDurationConverters().length);
        } finally {
            ConverterManager.getInstance().removeDurationConverter(c);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddDurationConverter2
    public void testAddDurationConverter2() {
        DurationConverter c = new DurationConverter() {
            public long getDurationMillis(Object object) {return 0;}
            public Class getSupportedType() {return String.class;}
        };
        try {
            DurationConverter removed = ConverterManager.getInstance().addDurationConverter(c);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(String.class, ConverterManager.getInstance().getDurationConverter("").getSupportedType());
            assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
        } finally {
            ConverterManager.getInstance().addDurationConverter(StringConverter.INSTANCE);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddDurationConverter3
    public void testAddDurationConverter3() {
        DurationConverter removed = ConverterManager.getInstance().addDurationConverter(null);
        assertEquals(null, removed);
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddDurationConverterSecurity
    public void testAddDurationConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().addDurationConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveDurationConverter1
    public void testRemoveDurationConverter1() {
        try {
            DurationConverter removed = ConverterManager.getInstance().removeDurationConverter(StringConverter.INSTANCE);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(DURATION_SIZE - 1, ConverterManager.getInstance().getDurationConverters().length);
        } finally {
            ConverterManager.getInstance().addDurationConverter(StringConverter.INSTANCE);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveDurationConverter2
    public void testRemoveDurationConverter2() {
        DurationConverter c = new DurationConverter() {
            public long getDurationMillis(Object object) {return 0;}
            public Class getSupportedType() {return Boolean.class;}
        };
        DurationConverter removed = ConverterManager.getInstance().removeDurationConverter(c);
        assertEquals(null, removed);
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveDurationConverter3
    public void testRemoveDurationConverter3() {
        DurationConverter removed = ConverterManager.getInstance().removeDurationConverter(null);
        assertEquals(null, removed);
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveDurationConverterSecurity
    public void testRemoveDurationConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().removeDurationConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPeriodConverter
    public void testGetPeriodConverter() {
        PeriodConverter c = ConverterManager.getInstance().getPeriodConverter(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        assertEquals(ReadablePeriod.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPeriodConverter(new Duration(123L));
        assertEquals(ReadableDuration.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPeriodConverter(new Interval(0L, 1000L));
        assertEquals(ReadableInterval.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPeriodConverter("");
        assertEquals(String.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPeriodConverter(null);
        assertEquals(null, c.getSupportedType());
        
        try {
            ConverterManager.getInstance().getPeriodConverter(Boolean.TRUE);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestConverterManager::testGetPeriodConverterRemovedNull
    public void testGetPeriodConverterRemovedNull() {
        try {
            ConverterManager.getInstance().removePeriodConverter(NullConverter.INSTANCE);
            try {
                ConverterManager.getInstance().getPeriodConverter(null);
                fail();
            } catch (IllegalArgumentException ex) {}
        } finally {
            ConverterManager.getInstance().addPeriodConverter(NullConverter.INSTANCE);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPeriodConverters
    public void testGetPeriodConverters() {
        PeriodConverter[] array = ConverterManager.getInstance().getPeriodConverters();
        assertEquals(PERIOD_SIZE, array.length);
    }

// org.joda.time.convert.TestConverterManager::testAddPeriodConverter1
    public void testAddPeriodConverter1() {
        PeriodConverter c = new PeriodConverter() {
            public void setInto(ReadWritablePeriod duration, Object object, Chronology c) {}
            public PeriodType getPeriodType(Object object) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        try {
            PeriodConverter removed = ConverterManager.getInstance().addPeriodConverter(c);
            assertEquals(null, removed);
            assertEquals(Boolean.class, ConverterManager.getInstance().getPeriodConverter(Boolean.TRUE).getSupportedType());
            assertEquals(PERIOD_SIZE + 1, ConverterManager.getInstance().getPeriodConverters().length);
        } finally {
            ConverterManager.getInstance().removePeriodConverter(c);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPeriodConverter2
    public void testAddPeriodConverter2() {
        PeriodConverter c = new PeriodConverter() {
            public void setInto(ReadWritablePeriod duration, Object object, Chronology c) {}
            public PeriodType getPeriodType(Object object) {return null;}
            public Class getSupportedType() {return String.class;}
        };
        try {
            PeriodConverter removed = ConverterManager.getInstance().addPeriodConverter(c);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(String.class, ConverterManager.getInstance().getPeriodConverter("").getSupportedType());
            assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
        } finally {
            ConverterManager.getInstance().addPeriodConverter(StringConverter.INSTANCE);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPeriodConverter3
    public void testAddPeriodConverter3() {
        PeriodConverter removed = ConverterManager.getInstance().addPeriodConverter(null);
        assertEquals(null, removed);
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPeriodConverterSecurity
    public void testAddPeriodConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().addPeriodConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePeriodConverter1
    public void testRemovePeriodConverter1() {
        try {
            PeriodConverter removed = ConverterManager.getInstance().removePeriodConverter(StringConverter.INSTANCE);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(PERIOD_SIZE - 1, ConverterManager.getInstance().getPeriodConverters().length);
        } finally {
            ConverterManager.getInstance().addPeriodConverter(StringConverter.INSTANCE);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePeriodConverter2
    public void testRemovePeriodConverter2() {
        PeriodConverter c = new PeriodConverter() {
            public void setInto(ReadWritablePeriod duration, Object object, Chronology c) {}
            public PeriodType getPeriodType(Object object) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        PeriodConverter removed = ConverterManager.getInstance().removePeriodConverter(c);
        assertEquals(null, removed);
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePeriodConverter3
    public void testRemovePeriodConverter3() {
        PeriodConverter removed = ConverterManager.getInstance().removePeriodConverter(null);
        assertEquals(null, removed);
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePeriodConverterSecurity
    public void testRemovePeriodConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().removePeriodConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetIntervalConverter
    public void testGetIntervalConverter() {
        IntervalConverter c = ConverterManager.getInstance().getIntervalConverter(new Interval(0L, 1000L));
        assertEquals(ReadableInterval.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getIntervalConverter("");
        assertEquals(String.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getIntervalConverter(null);
        assertEquals(null, c.getSupportedType());
        
        try {
            ConverterManager.getInstance().getIntervalConverter(Boolean.TRUE);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ConverterManager.getInstance().getIntervalConverter(new Long(0));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestConverterManager::testGetIntervalConverterRemovedNull
    public void testGetIntervalConverterRemovedNull() {
        try {
            ConverterManager.getInstance().removeIntervalConverter(NullConverter.INSTANCE);
            try {
                ConverterManager.getInstance().getIntervalConverter(null);
                fail();
            } catch (IllegalArgumentException ex) {}
        } finally {
            ConverterManager.getInstance().addIntervalConverter(NullConverter.INSTANCE);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetIntervalConverters
    public void testGetIntervalConverters() {
        IntervalConverter[] array = ConverterManager.getInstance().getIntervalConverters();
        assertEquals(INTERVAL_SIZE, array.length);
    }

// org.joda.time.convert.TestConverterManager::testAddIntervalConverter1
    public void testAddIntervalConverter1() {
        IntervalConverter c = new IntervalConverter() {
            public boolean isReadableInterval(Object object, Chronology chrono) {return false;}
            public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {}
            public Class getSupportedType() {return Boolean.class;}
        };
        try {
            IntervalConverter removed = ConverterManager.getInstance().addIntervalConverter(c);
            assertEquals(null, removed);
            assertEquals(Boolean.class, ConverterManager.getInstance().getIntervalConverter(Boolean.TRUE).getSupportedType());
            assertEquals(INTERVAL_SIZE + 1, ConverterManager.getInstance().getIntervalConverters().length);
        } finally {
            ConverterManager.getInstance().removeIntervalConverter(c);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddIntervalConverter2
    public void testAddIntervalConverter2() {
        IntervalConverter c = new IntervalConverter() {
            public boolean isReadableInterval(Object object, Chronology chrono) {return false;}
            public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {}
            public Class getSupportedType() {return String.class;}
        };
        try {
            IntervalConverter removed = ConverterManager.getInstance().addIntervalConverter(c);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(String.class, ConverterManager.getInstance().getIntervalConverter("").getSupportedType());
            assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
        } finally {
            ConverterManager.getInstance().addIntervalConverter(StringConverter.INSTANCE);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddIntervalConverter3
    public void testAddIntervalConverter3() {
        IntervalConverter removed = ConverterManager.getInstance().addIntervalConverter(null);
        assertEquals(null, removed);
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddIntervalConverterSecurity
    public void testAddIntervalConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().addIntervalConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveIntervalConverter1
    public void testRemoveIntervalConverter1() {
        try {
            IntervalConverter removed = ConverterManager.getInstance().removeIntervalConverter(StringConverter.INSTANCE);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(INTERVAL_SIZE - 1, ConverterManager.getInstance().getIntervalConverters().length);
        } finally {
            ConverterManager.getInstance().addIntervalConverter(StringConverter.INSTANCE);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveIntervalConverter2
    public void testRemoveIntervalConverter2() {
        IntervalConverter c = new IntervalConverter() {
            public boolean isReadableInterval(Object object, Chronology chrono) {return false;}
            public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {}
            public Class getSupportedType() {return Boolean.class;}
        };
        IntervalConverter removed = ConverterManager.getInstance().removeIntervalConverter(c);
        assertEquals(null, removed);
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveIntervalConverter3
    public void testRemoveIntervalConverter3() {
        IntervalConverter removed = ConverterManager.getInstance().removeIntervalConverter(null);
        assertEquals(null, removed);
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveIntervalConverterSecurity
    public void testRemoveIntervalConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().removeIntervalConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testToString
    public void testToString() {
        assertEquals("ConverterManager[6 instant,7 partial,5 duration,5 period,3 interval]", ConverterManager.getInstance().toString());
    }

// org.joda.time.convert.TestNullConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = NullConverter.class;
        assertEquals(false, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isProtected(cls.getModifiers()));
        assertEquals(false, Modifier.isPrivate(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(false, Modifier.isPublic(fld.getModifiers()));
        assertEquals(false, Modifier.isProtected(fld.getModifiers()));
        assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestNullConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(null, NullConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestNullConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        assertEquals(TEST_TIME_NOW, NullConverter.INSTANCE.getInstantMillis(null, JULIAN));
        assertEquals(TEST_TIME_NOW, NullConverter.INSTANCE.getInstantMillis(null, (Chronology) null));
    }

// org.joda.time.convert.TestNullConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISO_PARIS, NullConverter.INSTANCE.getChronology(null, PARIS));
        assertEquals(ISO, NullConverter.INSTANCE.getChronology(null, (DateTimeZone) null));
    }

// org.joda.time.convert.TestNullConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JULIAN, NullConverter.INSTANCE.getChronology(null, JULIAN));
        assertEquals(ISO, NullConverter.INSTANCE.getChronology(null, (Chronology) null));
    }

// org.joda.time.convert.TestNullConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = new int[] {10 + 1, 20, 30, 40}; 
        int[] actual = NullConverter.INSTANCE.getPartialValues(tod, null, ISOChronology.getInstance());
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestNullConverter::testGetDurationMillis_Object
    public void testGetDurationMillis_Object() throws Exception {
        assertEquals(0L, NullConverter.INSTANCE.getDurationMillis(null));
    }

// org.joda.time.convert.TestNullConverter::testGetPeriodType_Object
    public void testGetPeriodType_Object() throws Exception {
        assertEquals(PeriodType.standard(),
            NullConverter.INSTANCE.getPeriodType(null));
    }

// org.joda.time.convert.TestNullConverter::testSetInto_Object
    public void testSetInto_Object() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.millis());
        NullConverter.INSTANCE.setInto(m, null, null);
        assertEquals(0L, m.getMillis());
    }

// org.joda.time.convert.TestNullConverter::testIsReadableInterval_Object_Chronology
    public void testIsReadableInterval_Object_Chronology() throws Exception {
        assertEquals(false, NullConverter.INSTANCE.isReadableInterval(null, null));
    }

// org.joda.time.convert.TestNullConverter::testSetInto_Object_Chronology1
    public void testSetInto_Object_Chronology1() throws Exception {
        MutableInterval m = new MutableInterval(1000L, 2000L, GJChronology.getInstance());
        NullConverter.INSTANCE.setInto(m, null, null);
        assertEquals(TEST_TIME_NOW, m.getStartMillis());
        assertEquals(TEST_TIME_NOW, m.getEndMillis());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestNullConverter::testSetInto_Object_Chronology2
    public void testSetInto_Object_Chronology2() throws Exception {
        MutableInterval m = new MutableInterval(1000L, 2000L, GJChronology.getInstance());
        NullConverter.INSTANCE.setInto(m, null, CopticChronology.getInstance());
        assertEquals(TEST_TIME_NOW, m.getStartMillis());
        assertEquals(TEST_TIME_NOW, m.getEndMillis());
        assertEquals(CopticChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestNullConverter::testToString
    public void testToString() {
        assertEquals("Converter[null]", NullConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestReadableDurationConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ReadableDurationConverter.class;
        assertEquals(false, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isProtected(cls.getModifiers()));
        assertEquals(false, Modifier.isPrivate(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(false, Modifier.isPublic(fld.getModifiers()));
        assertEquals(false, Modifier.isProtected(fld.getModifiers()));
        assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestReadableDurationConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(ReadableDuration.class, ReadableDurationConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestReadableDurationConverter::testGetDurationMillis_Object
    public void testGetDurationMillis_Object() throws Exception {
        assertEquals(123L, ReadableDurationConverter.INSTANCE.getDurationMillis(new Duration(123L)));
    }

// org.joda.time.convert.TestReadableDurationConverter::testGetPeriodType_Object
    public void testGetPeriodType_Object() throws Exception {
        assertEquals(PeriodType.standard(),
            ReadableDurationConverter.INSTANCE.getPeriodType(new Duration(123L)));
    }

// org.joda.time.convert.TestReadableDurationConverter::testSetInto_Object
    public void testSetInto_Object() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearMonthDayTime());
        ReadableDurationConverter.INSTANCE.setInto(m, new Duration(
            3L * DateTimeConstants.MILLIS_PER_DAY +
            4L * DateTimeConstants.MILLIS_PER_MINUTE + 5L
        ), null);
        assertEquals(0, m.getYears());
        assertEquals(0, m.getMonths());
        assertEquals(0, m.getWeeks());
        assertEquals(0, m.getDays());
        assertEquals(3 * 24, m.getHours());
        assertEquals(4, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(5, m.getMillis());
    }

// org.joda.time.convert.TestReadableDurationConverter::testToString
    public void testToString() {
        assertEquals("Converter[org.joda.time.ReadableDuration]", ReadableDurationConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ReadableIntervalConverter.class;
        assertEquals(false, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isProtected(cls.getModifiers()));
        assertEquals(false, Modifier.isPrivate(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(false, Modifier.isPublic(fld.getModifiers()));
        assertEquals(false, Modifier.isProtected(fld.getModifiers()));
        assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(ReadableInterval.class, ReadableIntervalConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testGetDurationMillis_Object
    public void testGetDurationMillis_Object() throws Exception {
        Interval i = new Interval(100L, 223L);
        assertEquals(123L, ReadableIntervalConverter.INSTANCE.getDurationMillis(i));
    }

// org.joda.time.convert.TestReadableIntervalConverter::testGetPeriodType_Object
    public void testGetPeriodType_Object() throws Exception {
        Interval i = new Interval(100L, 223L);
        assertEquals(PeriodType.standard(),
            ReadableIntervalConverter.INSTANCE.getPeriodType(i));
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoPeriod_Object1
    public void testSetIntoPeriod_Object1() throws Exception {
        Interval i = new Interval(100L, 223L);
        MutablePeriod m = new MutablePeriod(PeriodType.millis());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, null);
        assertEquals(0, m.getYears());
        assertEquals(0, m.getMonths());
        assertEquals(0, m.getWeeks());
        assertEquals(0, m.getDays());
        assertEquals(0, m.getHours());
        assertEquals(0, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(123, m.getMillis());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoPeriod_Object2
    public void testSetIntoPeriod_Object2() throws Exception {
        Interval i = new Interval(100L, 223L);
        MutablePeriod m = new MutablePeriod(PeriodType.millis());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, CopticChronology.getInstance());
        assertEquals(0, m.getYears());
        assertEquals(0, m.getMonths());
        assertEquals(0, m.getWeeks());
        assertEquals(0, m.getDays());
        assertEquals(0, m.getHours());
        assertEquals(0, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(123, m.getMillis());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testIsReadableInterval_Object_Chronology
    public void testIsReadableInterval_Object_Chronology() throws Exception {
        Interval i = new Interval(1234L, 5678L);
        assertEquals(true, ReadableIntervalConverter.INSTANCE.isReadableInterval(i, null));
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoInterval_Object1
    public void testSetIntoInterval_Object1() throws Exception {
        Interval i = new Interval(0L, 123L, CopticChronology.getInstance());
        MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, null);
        assertEquals(0L, m.getStartMillis());
        assertEquals(123L, m.getEndMillis());
        assertEquals(CopticChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoInterval_Object2
    public void testSetIntoInterval_Object2() throws Exception {
        Interval i = new Interval(0L, 123L, CopticChronology.getInstance());
        MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, GJChronology.getInstance());
        assertEquals(0L, m.getStartMillis());
        assertEquals(123L, m.getEndMillis());
        assertEquals(GJChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoInterval_Object3
    public void testSetIntoInterval_Object3() throws Exception {
        MutableInterval i = new MutableInterval(0L, 123L) {
            public Chronology getChronology() {
                return null; 
            }
        };
        MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, GJChronology.getInstance());
        assertEquals(0L, m.getStartMillis());
        assertEquals(123L, m.getEndMillis());
        assertEquals(GJChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoInterval_Object4
    public void testSetIntoInterval_Object4() throws Exception {
        MutableInterval i = new MutableInterval(0L, 123L) {
            public Chronology getChronology() {
                return null; 
            }
        };
        MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, null);
        assertEquals(0L, m.getStartMillis());
        assertEquals(123L, m.getEndMillis());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testToString
    public void testToString() {
        assertEquals("Converter[org.joda.time.ReadableInterval]", ReadableIntervalConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestReadablePeriodConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ReadablePeriodConverter.class;
        assertEquals(false, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isProtected(cls.getModifiers()));
        assertEquals(false, Modifier.isPrivate(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(false, Modifier.isPublic(fld.getModifiers()));
        assertEquals(false, Modifier.isProtected(fld.getModifiers()));
        assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestReadablePeriodConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(ReadablePeriod.class, ReadablePeriodConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestReadablePeriodConverter::testGetPeriodType_Object
    public void testGetPeriodType_Object() throws Exception {
        assertEquals(PeriodType.standard(),
            ReadablePeriodConverter.INSTANCE.getPeriodType(new Period(123L, PeriodType.standard())));
        assertEquals(PeriodType.yearMonthDayTime(),
            ReadablePeriodConverter.INSTANCE.getPeriodType(new Period(123L, PeriodType.yearMonthDayTime())));
    }

// org.joda.time.convert.TestReadablePeriodConverter::testSetInto_Object
    public void testSetInto_Object() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearMonthDayTime());
        ReadablePeriodConverter.INSTANCE.setInto(m, new Period(0, 0, 0, 3, 0, 4, 0, 5), null);
        assertEquals(0, m.getYears());
        assertEquals(0, m.getMonths());
        assertEquals(0, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(0, m.getHours());
        assertEquals(4, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(5, m.getMillis());
    }

// org.joda.time.convert.TestReadablePeriodConverter::testToString
    public void testToString() {
        assertEquals("Converter[org.joda.time.ReadablePeriod]", ReadablePeriodConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestStringConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = StringConverter.class;
        assertEquals(false, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isProtected(cls.getModifiers()));
        assertEquals(false, Modifier.isPrivate(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(false, Modifier.isPublic(fld.getModifiers()));
        assertEquals(false, Modifier.isProtected(fld.getModifiers()));
        assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestStringConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(String.class, StringConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestStringConverter::testGetInstantMillis_Object
    public void testGetInstantMillis_Object() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 1, 1, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 1, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-161T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-W24-3T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 7, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-W24T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 30, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12.5+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 30, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24.5+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 500, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.5+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 501);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501", ISO));
    }

// org.joda.time.convert.TestStringConverter::testGetInstantMillis_Object_Zone
    public void testGetInstantMillis_Object_Zone() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, PARIS);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501+02:00", ISO_PARIS));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, PARIS);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501", ISO_PARIS));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, LONDON);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501+01:00", ISO_LONDON));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, LONDON);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501", ISO_LONDON));
    }

// org.joda.time.convert.TestStringConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, JulianChronology.getInstance(LONDON));
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501+01:00", JULIAN));
    }

// org.joda.time.convert.TestStringConverter::testGetInstantMillisInvalid
    public void testGetInstantMillisInvalid() {
        try {
            StringConverter.INSTANCE.getInstantMillis("", (Chronology) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getInstantMillis("X", (Chronology) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISOChronology.getInstance(PARIS), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501+01:00", PARIS));
        assertEquals(ISOChronology.getInstance(PARIS), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501", PARIS));
        assertEquals(ISOChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501+01:00", (DateTimeZone) null));
        assertEquals(ISOChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501", (DateTimeZone) null));
    }

// org.joda.time.convert.TestStringConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JulianChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501+01:00", JULIAN));
        assertEquals(JulianChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501", JULIAN));
        assertEquals(ISOChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501+01:00", (Chronology) null));
        assertEquals(ISOChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501", (Chronology) null));
    }

// org.joda.time.convert.TestStringConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = new int[] {3, 4, 5, 6};
        int[] actual = StringConverter.INSTANCE.getPartialValues(tod, "T03:04:05.006", ISOChronology.getInstance());
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime
    public void testGetDateTime() throws Exception {
        DateTime base = new DateTime(2004, 6, 9, 12, 24, 48, 501, PARIS);
        DateTime test = new DateTime(base.toString(), PARIS);
        assertEquals(base, test);
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime1
    public void testGetDateTime1() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501+01:00");
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(LONDON, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime2
    public void testGetDateTime2() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501");
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(LONDON, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime3
    public void testGetDateTime3() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501+02:00", PARIS);
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(PARIS, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime4
    public void testGetDateTime4() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501", PARIS);
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(PARIS, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime5
    public void testGetDateTime5() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501+02:00", JulianChronology.getInstance(PARIS));
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(PARIS, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime6
    public void testGetDateTime6() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501", JulianChronology.getInstance(PARIS));
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(PARIS, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDurationMillis_Object1
    public void testGetDurationMillis_Object1() throws Exception {
        long millis = StringConverter.INSTANCE.getDurationMillis("PT12.345S");
        assertEquals(12345, millis);
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt12.345s");
        assertEquals(12345, millis);
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt12s");
        assertEquals(12000, millis);
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt12.s");
        assertEquals(12000, millis);
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt-12.32s");
        assertEquals(-12320, millis);
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt12.3456s");
        assertEquals(12345, millis);
    }

// org.joda.time.convert.TestStringConverter::testGetDurationMillis_Object2
    public void testGetDurationMillis_Object2() throws Exception {
        try {
            StringConverter.INSTANCE.getDurationMillis("P2Y6M9DXYZ");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PTS");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("XT0S");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PX0S");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PT0X");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PTXS");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PT0.0.0S");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PT0-00S");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testGetPeriodType_Object
    public void testGetPeriodType_Object() throws Exception {
        assertEquals(PeriodType.standard(),
            StringConverter.INSTANCE.getPeriodType("P2Y6M9D"));
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object1
    public void testSetIntoPeriod_Object1() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearMonthDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y6M9DT12H24M48S", null);
        assertEquals(2, m.getYears());
        assertEquals(6, m.getMonths());
        assertEquals(9, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(48, m.getSeconds());
        assertEquals(0, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object2
    public void testSetIntoPeriod_Object2() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M48S", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(48, m.getSeconds());
        assertEquals(0, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object3
    public void testSetIntoPeriod_Object3() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M48.034S", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(48, m.getSeconds());
        assertEquals(34, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object4
    public void testSetIntoPeriod_Object4() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M.056S", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(56, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object5
    public void testSetIntoPeriod_Object5() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M56.S", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(56, m.getSeconds());
        assertEquals(0, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object6
    public void testSetIntoPeriod_Object6() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M56.1234567S", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(56, m.getSeconds());
        assertEquals(123, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object7
    public void testSetIntoPeriod_Object7() throws Exception {
        MutablePeriod m = new MutablePeriod(1, 0, 1, 1, 1, 1, 1, 1, PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3D", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(0, m.getHours());
        assertEquals(0, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(0, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object8
    public void testSetIntoPeriod_Object8() throws Exception {
        MutablePeriod m = new MutablePeriod();
        try {
            StringConverter.INSTANCE.setInto(m, "", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        try {
            StringConverter.INSTANCE.setInto(m, "PXY", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        try {
            StringConverter.INSTANCE.setInto(m, "PT0SXY", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M48SX", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testIsReadableInterval_Object_Chronology
    public void testIsReadableInterval_Object_Chronology() throws Exception {
        assertEquals(false, StringConverter.INSTANCE.isReadableInterval("", null));
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology1
    public void testSetIntoInterval_Object_Chronology1() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2004-06-09/P1Y2M", null);
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), m.getStart());
        assertEquals(new DateTime(2005, 8, 9, 0, 0, 0, 0), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology2
    public void testSetIntoInterval_Object_Chronology2() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "P1Y2M/2004-06-09", null);
        assertEquals(new DateTime(2003, 4, 9, 0, 0, 0, 0), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology3
    public void testSetIntoInterval_Object_Chronology3() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09/2004-06-09", null);
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology4
    public void testSetIntoInterval_Object_Chronology4() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2004-06-09T+06:00/P1Y2M", null);
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getStart());
        assertEquals(new DateTime(2005, 8, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology5
    public void testSetIntoInterval_Object_Chronology5() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "P1Y2M/2004-06-09T+06:00", null);
        assertEquals(new DateTime(2003, 4, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology6
    public void testSetIntoInterval_Object_Chronology6() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09T+06:00/2004-06-09T+07:00", null);
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, SEVEN).withChronology(null), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology7
    public void testSetIntoInterval_Object_Chronology7() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09/2004-06-09", BuddhistChronology.getInstance());
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0, BuddhistChronology.getInstance()), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, BuddhistChronology.getInstance()), m.getEnd());
        assertEquals(BuddhistChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology8
    public void testSetIntoInterval_Object_Chronology8() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09T+06:00/2004-06-09T+07:00", BuddhistChronology.getInstance(EIGHT));
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0, BuddhistChronology.getInstance(SIX)).withZone(EIGHT), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, BuddhistChronology.getInstance(SEVEN)).withZone(EIGHT), m.getEnd());
        assertEquals(BuddhistChronology.getInstance(EIGHT), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology1
    public void testSetIntoIntervalEx_Object_Chronology1() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology2
    public void testSetIntoIntervalEx_Object_Chronology2() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "/", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology3
    public void testSetIntoIntervalEx_Object_Chronology3() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "P1Y/", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology4
    public void testSetIntoIntervalEx_Object_Chronology4() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "/P1Y", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology5
    public void testSetIntoIntervalEx_Object_Chronology5() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "P1Y/P2Y", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testToString
    public void testToString() {
        assertEquals("Converter[java.lang.String]", StringConverter.INSTANCE.toString());
    }

// org.joda.time.format.TestISOPeriodFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        ISOPeriodFormat f = new ISOPeriodFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatStandard
    public void testFormatStandard() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", ISOPeriodFormat.standard().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P1Y2M3W4DT5H6M7S", ISOPeriodFormat.standard().print(p));
        
        p = new Period(0);
        assertEquals("PT0S", ISOPeriodFormat.standard().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("PT0M", ISOPeriodFormat.standard().print(p));
        
        assertEquals("P1Y4DT5H6M7.008S", ISOPeriodFormat.standard().print(YEAR_DAY_PERIOD));
        assertEquals("PT0S", ISOPeriodFormat.standard().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P1Y2M3W4D", ISOPeriodFormat.standard().print(DATE_PERIOD));
        assertEquals("PT5H6M7.008S", ISOPeriodFormat.standard().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternate
    public void testFormatAlternate() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P00010204T050607.008", ISOPeriodFormat.alternate().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P00010204T050607", ISOPeriodFormat.alternate().print(p));
        
        p = new Period(0);
        assertEquals("P00000000T000000", ISOPeriodFormat.alternate().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P00000000T000000", ISOPeriodFormat.alternate().print(p));
        
        assertEquals("P00010004T050607.008", ISOPeriodFormat.alternate().print(YEAR_DAY_PERIOD));
        assertEquals("P00000000T000000", ISOPeriodFormat.alternate().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P00010204T000000", ISOPeriodFormat.alternate().print(DATE_PERIOD));
        assertEquals("P00000000T050607.008", ISOPeriodFormat.alternate().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternateExtended
    public void testFormatAlternateExtended() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P0001-02-04T05:06:07.008", ISOPeriodFormat.alternateExtended().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P0001-02-04T05:06:07", ISOPeriodFormat.alternateExtended().print(p));
        
        p = new Period(0);
        assertEquals("P0000-00-00T00:00:00", ISOPeriodFormat.alternateExtended().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P0000-00-00T00:00:00", ISOPeriodFormat.alternateExtended().print(p));
        
        assertEquals("P0001-00-04T05:06:07.008", ISOPeriodFormat.alternateExtended().print(YEAR_DAY_PERIOD));
        assertEquals("P0000-00-00T00:00:00", ISOPeriodFormat.alternateExtended().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P0001-02-04T00:00:00", ISOPeriodFormat.alternateExtended().print(DATE_PERIOD));
        assertEquals("P0000-00-00T05:06:07.008", ISOPeriodFormat.alternateExtended().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternateWithWeeks
    public void testFormatAlternateWithWeeks() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P0001W0304T050607.008", ISOPeriodFormat.alternateWithWeeks().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P0001W0304T050607", ISOPeriodFormat.alternateWithWeeks().print(p));
        
        p = new Period(0);
        assertEquals("P0000W0000T000000", ISOPeriodFormat.alternateWithWeeks().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P0000W0000T000000", ISOPeriodFormat.alternateWithWeeks().print(p));
        
        assertEquals("P0001W0004T050607.008", ISOPeriodFormat.alternateWithWeeks().print(YEAR_DAY_PERIOD));
        assertEquals("P0000W0000T000000", ISOPeriodFormat.alternateWithWeeks().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P0001W0304T000000", ISOPeriodFormat.alternateWithWeeks().print(DATE_PERIOD));
        assertEquals("P0000W0000T050607.008", ISOPeriodFormat.alternateWithWeeks().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternateExtendedWithWeeks
    public void testFormatAlternateExtendedWithWeeks() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P0001-W03-04T05:06:07.008", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P0001-W03-04T05:06:07", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        
        p = new Period(0);
        assertEquals("P0000-W00-00T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P0000-W00-00T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        
        assertEquals("P0001-W00-04T05:06:07.008", ISOPeriodFormat.alternateExtendedWithWeeks().print(YEAR_DAY_PERIOD));
        assertEquals("P0000-W00-00T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P0001-W03-04T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(DATE_PERIOD));
        assertEquals("P0000-W00-00T05:06:07.008", ISOPeriodFormat.alternateExtendedWithWeeks().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard1
    public void testParseStandard1() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P1Y2M3W4DT5H6M7.008S");
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard2
    public void testParseStandard2() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0Y0M0W0DT5H6M7.008S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard3
    public void testParseStandard3() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0DT5H6M7.008S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard4
    public void testParseStandard4() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P2Y3DT5H6M7.008S");
        assertEquals(new Period(2, 0, 0, 3, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard5
    public void testParseStandard5() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P2YT5H6M7.008S");
        assertEquals(new Period(2, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard6
    public void testParseStandard6() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("PT5H6M7.008S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard7
    public void testParseStandard7() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P1Y2M3W4D");
        assertEquals(new Period(1, 2, 3, 4, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard8
    public void testParseStandard8() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("PT5H6M7S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard9
    public void testParseStandard9() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("PT0S");
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard10
    public void testParseStandard10() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0D");
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard11
    public void testParseStandard11() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0Y");
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail1
    public void testParseStandardFail1() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("P1Y2S");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail2
    public void testParseStandardFail2() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("PS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail3
    public void testParseStandardFail3() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("PTS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail4
    public void testParseStandardFail4() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("PXS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        PeriodFormat f = new PeriodFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_formatStandard
    public void test_getDefault_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 day, 5 hours, 6 minutes, 7 seconds and 8 milliseconds", PeriodFormat.getDefault().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_FormatOneField
    public void test_getDefault_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 days", PeriodFormat.getDefault().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_formatTwoFields
    public void test_getDefault_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 days and 5 hours", PeriodFormat.getDefault().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_parseOneField
    public void test_getDefault_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.getDefault().parsePeriod("2 days"));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_parseTwoFields
    public void test_getDefault_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.getDefault().parsePeriod("2 days and 5 hours"));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_cached
    public void test_getDefault_cached() {
        assertSame(PeriodFormat.getDefault(), PeriodFormat.getDefault());
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_formatStandard
    public void test_wordBased_fr_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 jour, 5 heures, 6 minutes, 7 secondes et 8 millisecondes", PeriodFormat.wordBased(FR).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_FormatOneField
    public void test_wordBased_fr_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 jours", PeriodFormat.wordBased(FR).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_formatTwoFields
    public void test_wordBased_fr_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 jours et 5 heures", PeriodFormat.wordBased(FR).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_parseOneField
    public void test_wordBased_fr_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(FR).parsePeriod("2 jours"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_parseTwoFields
    public void test_wordBased_fr_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(FR).parsePeriod("2 jours et 5 heures"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_cached
    public void test_wordBased_fr_cached() {
        assertSame(PeriodFormat.wordBased(FR), PeriodFormat.wordBased(FR));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_formatStandard
    public void test_wordBased_pt_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 dia, 5 horas, 6 minutos, 7 segundos e 8 milissegundos", PeriodFormat.wordBased(PT).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_FormatOneField
    public void test_wordBased_pt_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 dias", PeriodFormat.wordBased(PT).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_formatTwoFields
    public void test_wordBased_pt_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 dias e 5 horas", PeriodFormat.wordBased(PT).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_parseOneField
    public void test_wordBased_pt_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(PT).parsePeriod("2 dias"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_parseTwoFields
    public void test_wordBased_pt_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(PT).parsePeriod("2 dias e 5 horas"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_cached
    public void test_wordBased_pt_cached() {
        assertSame(PeriodFormat.wordBased(PT), PeriodFormat.wordBased(PT));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_formatStandard
    public void test_wordBased_es_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 dia, 5 horas, 6 minutos, 7 segundos y 8 milisegundos", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_FormatOneField
    public void test_wordBased_es_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 dias", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_formatTwoFields
    public void test_wordBased_es_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 dias y 5 horas", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_parseOneField
    public void test_wordBased_es_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(ES).parsePeriod("2 dias"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_parseTwoFields
    public void test_wordBased_es_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(ES).parsePeriod("2 dias y 5 horas"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_cached
    public void test_wordBased_es_cached() {
        assertSame(PeriodFormat.wordBased(ES), PeriodFormat.wordBased(ES));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_formatStandard
    public void test_wordBased_de_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 Tag, 5 Stunden, 6 Minuten, 7 Sekunden und 8 Millisekunden", PeriodFormat.wordBased(DE).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_FormatOneField
    public void test_wordBased_de_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 Tage", PeriodFormat.wordBased(DE).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_formatTwoFields
    public void test_wordBased_de_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 Tage und 5 Stunden", PeriodFormat.wordBased(DE).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_parseOneField
    public void test_wordBased_de_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(DE).parsePeriod("2 Tage"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_parseTwoFields
    public void test_wordBased_de_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(DE).parsePeriod("2 Tage und 5 Stunden"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_cached
    public void test_wordBased_de_cached() {
        assertSame(PeriodFormat.wordBased(DE), PeriodFormat.wordBased(DE));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_formatStandard
    public void test_wordBased_nl_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 dag, 5 uur, 6 minuten, 7 seconden en 8 milliseconden", PeriodFormat.wordBased(NL).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_FormatOneField
    public void test_wordBased_nl_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 dagen", PeriodFormat.wordBased(NL).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_formatTwoFields
    public void test_wordBased_nl_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 dagen en 5 uur", PeriodFormat.wordBased(NL).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_parseOneField
    public void test_wordBased_nl_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(NL).parsePeriod("2 dagen"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_parseTwoFields
    public void test_wordBased_nl_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(NL).parsePeriod("2 dagen en 5 uur"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_cached
    public void test_wordBased_nl_cached() {
        assertSame(PeriodFormat.wordBased(NL), PeriodFormat.wordBased(NL));
    }

// org.joda.time.format.TestPeriodFormatParsing::testParseStandard1
    public void testParseStandard1() {}

// org.joda.time.format.TestPeriodFormatParsing::testParseCustom1
    public void testParseCustom1() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .printZeroAlways()
            .appendHours()
            .appendSuffix(":")
            .minimumPrintedDigits(2)
            .appendMinutes()
            .toFormatter();

        Period p;

        p = new Period(47, 55, 0, 0);
        assertEquals("47:55", formatter.print(p));
        assertEquals(p, formatter.parsePeriod("47:55"));
        assertEquals(p, formatter.parsePeriod("047:055"));

        p = new Period(7, 5, 0, 0);
        assertEquals("7:05", formatter.print(p));
        assertEquals(p, formatter.parsePeriod("7:05"));
        assertEquals(p, formatter.parsePeriod("7:5"));
        assertEquals(p, formatter.parsePeriod("07:05"));

        p = new Period(0, 5, 0, 0);
        assertEquals("0:05", formatter.print(p));
        assertEquals(p, formatter.parsePeriod("0:05"));
        assertEquals(p, formatter.parsePeriod("0:5"));
        assertEquals(p, formatter.parsePeriod("00:005"));
        assertEquals(p, formatter.parsePeriod("0:005"));

        p = new Period(0, 0, 0, 0);
        assertEquals("0:00", formatter.print(p));
        assertEquals(p, formatter.parsePeriod("0:00"));
        assertEquals(p, formatter.parsePeriod("0:0"));
        assertEquals(p, formatter.parsePeriod("00:00"));
    }

// org.joda.time.format.TestPeriodFormatter::testPrint_simple
    public void testPrint_simple() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", f.print(p));
    }
