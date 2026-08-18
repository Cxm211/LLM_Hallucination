// buggy code
        public int parseInto(DateTimeParserBucket bucket, String text, int position) {
            String str = text.substring(position);
            for (String id : ALL_IDS) {
                if (str.startsWith(id)) {
                    bucket.setZone(DateTimeZone.forID(id));
                    return position + id.length();
                }
            }
            return ~position;
        }

// relevant test
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

// org.joda.time.chrono.TestISOChronology::testFactoryUTC
    public void testFactoryUTC() {
        assertEquals(DateTimeZone.UTC, ISOChronology.getInstanceUTC().getZone());
        assertSame(ISOChronology.class, ISOChronology.getInstanceUTC().getClass());
    }

// org.joda.time.chrono.TestISOChronology::testFactory
    public void testFactory() {
        assertEquals(LONDON, ISOChronology.getInstance().getZone());
        assertSame(ISOChronology.class, ISOChronology.getInstance().getClass());
    }

// org.joda.time.chrono.TestISOChronology::testFactory_Zone
    public void testFactory_Zone() {
        assertEquals(TOKYO, ISOChronology.getInstance(TOKYO).getZone());
        assertEquals(PARIS, ISOChronology.getInstance(PARIS).getZone());
        assertEquals(LONDON, ISOChronology.getInstance(null).getZone());
        assertSame(ISOChronology.class, ISOChronology.getInstance(TOKYO).getClass());
    }

// org.joda.time.chrono.TestISOChronology::testEquality
    public void testEquality() {
        assertSame(ISOChronology.getInstance(TOKYO), ISOChronology.getInstance(TOKYO));
        assertSame(ISOChronology.getInstance(LONDON), ISOChronology.getInstance(LONDON));
        assertSame(ISOChronology.getInstance(PARIS), ISOChronology.getInstance(PARIS));
        assertSame(ISOChronology.getInstanceUTC(), ISOChronology.getInstanceUTC());
        assertSame(ISOChronology.getInstance(), ISOChronology.getInstance(LONDON));
    }

// org.joda.time.chrono.TestISOChronology::testWithUTC
    public void testWithUTC() {
        assertSame(ISOChronology.getInstanceUTC(), ISOChronology.getInstance(LONDON).withUTC());
        assertSame(ISOChronology.getInstanceUTC(), ISOChronology.getInstance(TOKYO).withUTC());
        assertSame(ISOChronology.getInstanceUTC(), ISOChronology.getInstanceUTC().withUTC());
        assertSame(ISOChronology.getInstanceUTC(), ISOChronology.getInstance().withUTC());
    }

// org.joda.time.chrono.TestISOChronology::testWithZone
    public void testWithZone() {
        assertSame(ISOChronology.getInstance(TOKYO), ISOChronology.getInstance(TOKYO).withZone(TOKYO));
        assertSame(ISOChronology.getInstance(LONDON), ISOChronology.getInstance(TOKYO).withZone(LONDON));
        assertSame(ISOChronology.getInstance(PARIS), ISOChronology.getInstance(TOKYO).withZone(PARIS));
        assertSame(ISOChronology.getInstance(LONDON), ISOChronology.getInstance(TOKYO).withZone(null));
        assertSame(ISOChronology.getInstance(PARIS), ISOChronology.getInstance().withZone(PARIS));
        assertSame(ISOChronology.getInstance(PARIS), ISOChronology.getInstanceUTC().withZone(PARIS));
    }

// org.joda.time.chrono.TestISOChronology::testToString
    public void testToString() {
        assertEquals("ISOChronology[Europe/London]", ISOChronology.getInstance(LONDON).toString());
        assertEquals("ISOChronology[Asia/Tokyo]", ISOChronology.getInstance(TOKYO).toString());
        assertEquals("ISOChronology[Europe/London]", ISOChronology.getInstance().toString());
        assertEquals("ISOChronology[UTC]", ISOChronology.getInstanceUTC().toString());
    }

// org.joda.time.chrono.TestISOChronology::testDurationFields
    public void testDurationFields() {
        assertEquals("eras", ISOChronology.getInstance().eras().getName());
        assertEquals("centuries", ISOChronology.getInstance().centuries().getName());
        assertEquals("years", ISOChronology.getInstance().years().getName());
        assertEquals("weekyears", ISOChronology.getInstance().weekyears().getName());
        assertEquals("months", ISOChronology.getInstance().months().getName());
        assertEquals("weeks", ISOChronology.getInstance().weeks().getName());
        assertEquals("days", ISOChronology.getInstance().days().getName());
        assertEquals("halfdays", ISOChronology.getInstance().halfdays().getName());
        assertEquals("hours", ISOChronology.getInstance().hours().getName());
        assertEquals("minutes", ISOChronology.getInstance().minutes().getName());
        assertEquals("seconds", ISOChronology.getInstance().seconds().getName());
        assertEquals("millis", ISOChronology.getInstance().millis().getName());
        
        assertEquals(false, ISOChronology.getInstance().eras().isSupported());
        assertEquals(true, ISOChronology.getInstance().centuries().isSupported());
        assertEquals(true, ISOChronology.getInstance().years().isSupported());
        assertEquals(true, ISOChronology.getInstance().weekyears().isSupported());
        assertEquals(true, ISOChronology.getInstance().months().isSupported());
        assertEquals(true, ISOChronology.getInstance().weeks().isSupported());
        assertEquals(true, ISOChronology.getInstance().days().isSupported());
        assertEquals(true, ISOChronology.getInstance().halfdays().isSupported());
        assertEquals(true, ISOChronology.getInstance().hours().isSupported());
        assertEquals(true, ISOChronology.getInstance().minutes().isSupported());
        assertEquals(true, ISOChronology.getInstance().seconds().isSupported());
        assertEquals(true, ISOChronology.getInstance().millis().isSupported());
        
        assertEquals(false, ISOChronology.getInstance().centuries().isPrecise());
        assertEquals(false, ISOChronology.getInstance().years().isPrecise());
        assertEquals(false, ISOChronology.getInstance().weekyears().isPrecise());
        assertEquals(false, ISOChronology.getInstance().months().isPrecise());
        assertEquals(false, ISOChronology.getInstance().weeks().isPrecise());
        assertEquals(false, ISOChronology.getInstance().days().isPrecise());
        assertEquals(false, ISOChronology.getInstance().halfdays().isPrecise());
        assertEquals(true, ISOChronology.getInstance().hours().isPrecise());
        assertEquals(true, ISOChronology.getInstance().minutes().isPrecise());
        assertEquals(true, ISOChronology.getInstance().seconds().isPrecise());
        assertEquals(true, ISOChronology.getInstance().millis().isPrecise());
        
        assertEquals(false, ISOChronology.getInstanceUTC().centuries().isPrecise());
        assertEquals(false, ISOChronology.getInstanceUTC().years().isPrecise());
        assertEquals(false, ISOChronology.getInstanceUTC().weekyears().isPrecise());
        assertEquals(false, ISOChronology.getInstanceUTC().months().isPrecise());
        assertEquals(true, ISOChronology.getInstanceUTC().weeks().isPrecise());
        assertEquals(true, ISOChronology.getInstanceUTC().days().isPrecise());
        assertEquals(true, ISOChronology.getInstanceUTC().halfdays().isPrecise());
        assertEquals(true, ISOChronology.getInstanceUTC().hours().isPrecise());
        assertEquals(true, ISOChronology.getInstanceUTC().minutes().isPrecise());
        assertEquals(true, ISOChronology.getInstanceUTC().seconds().isPrecise());
        assertEquals(true, ISOChronology.getInstanceUTC().millis().isPrecise());
        
        DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        assertEquals(false, ISOChronology.getInstance(gmt).centuries().isPrecise());
        assertEquals(false, ISOChronology.getInstance(gmt).years().isPrecise());
        assertEquals(false, ISOChronology.getInstance(gmt).weekyears().isPrecise());
        assertEquals(false, ISOChronology.getInstance(gmt).months().isPrecise());
        assertEquals(true, ISOChronology.getInstance(gmt).weeks().isPrecise());
        assertEquals(true, ISOChronology.getInstance(gmt).days().isPrecise());
        assertEquals(true, ISOChronology.getInstance(gmt).halfdays().isPrecise());
        assertEquals(true, ISOChronology.getInstance(gmt).hours().isPrecise());
        assertEquals(true, ISOChronology.getInstance(gmt).minutes().isPrecise());
        assertEquals(true, ISOChronology.getInstance(gmt).seconds().isPrecise());
        assertEquals(true, ISOChronology.getInstance(gmt).millis().isPrecise());
        
        DateTimeZone offset = DateTimeZone.forOffsetHours(1);
        assertEquals(false, ISOChronology.getInstance(offset).centuries().isPrecise());
        assertEquals(false, ISOChronology.getInstance(offset).years().isPrecise());
        assertEquals(false, ISOChronology.getInstance(offset).weekyears().isPrecise());
        assertEquals(false, ISOChronology.getInstance(offset).months().isPrecise());
        assertEquals(true, ISOChronology.getInstance(offset).weeks().isPrecise());
        assertEquals(true, ISOChronology.getInstance(offset).days().isPrecise());
        assertEquals(true, ISOChronology.getInstance(offset).halfdays().isPrecise());
        assertEquals(true, ISOChronology.getInstance(offset).hours().isPrecise());
        assertEquals(true, ISOChronology.getInstance(offset).minutes().isPrecise());
        assertEquals(true, ISOChronology.getInstance(offset).seconds().isPrecise());
        assertEquals(true, ISOChronology.getInstance(offset).millis().isPrecise());
    }

// org.joda.time.chrono.TestISOChronology::testDateFields
    public void testDateFields() {
        assertEquals("era", ISOChronology.getInstance().era().getName());
        assertEquals("centuryOfEra", ISOChronology.getInstance().centuryOfEra().getName());
        assertEquals("yearOfCentury", ISOChronology.getInstance().yearOfCentury().getName());
        assertEquals("yearOfEra", ISOChronology.getInstance().yearOfEra().getName());
        assertEquals("year", ISOChronology.getInstance().year().getName());
        assertEquals("monthOfYear", ISOChronology.getInstance().monthOfYear().getName());
        assertEquals("weekyearOfCentury", ISOChronology.getInstance().weekyearOfCentury().getName());
        assertEquals("weekyear", ISOChronology.getInstance().weekyear().getName());
        assertEquals("weekOfWeekyear", ISOChronology.getInstance().weekOfWeekyear().getName());
        assertEquals("dayOfYear", ISOChronology.getInstance().dayOfYear().getName());
        assertEquals("dayOfMonth", ISOChronology.getInstance().dayOfMonth().getName());
        assertEquals("dayOfWeek", ISOChronology.getInstance().dayOfWeek().getName());
        
        assertEquals(true, ISOChronology.getInstance().era().isSupported());
        assertEquals(true, ISOChronology.getInstance().centuryOfEra().isSupported());
        assertEquals(true, ISOChronology.getInstance().yearOfCentury().isSupported());
        assertEquals(true, ISOChronology.getInstance().yearOfEra().isSupported());
        assertEquals(true, ISOChronology.getInstance().year().isSupported());
        assertEquals(true, ISOChronology.getInstance().monthOfYear().isSupported());
        assertEquals(true, ISOChronology.getInstance().weekyearOfCentury().isSupported());
        assertEquals(true, ISOChronology.getInstance().weekyear().isSupported());
        assertEquals(true, ISOChronology.getInstance().weekOfWeekyear().isSupported());
        assertEquals(true, ISOChronology.getInstance().dayOfYear().isSupported());
        assertEquals(true, ISOChronology.getInstance().dayOfMonth().isSupported());
        assertEquals(true, ISOChronology.getInstance().dayOfWeek().isSupported());
    }

// org.joda.time.chrono.TestISOChronology::testTimeFields
    public void testTimeFields() {
        assertEquals("halfdayOfDay", ISOChronology.getInstance().halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", ISOChronology.getInstance().clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", ISOChronology.getInstance().hourOfHalfday().getName());
        assertEquals("clockhourOfDay", ISOChronology.getInstance().clockhourOfDay().getName());
        assertEquals("hourOfDay", ISOChronology.getInstance().hourOfDay().getName());
        assertEquals("minuteOfDay", ISOChronology.getInstance().minuteOfDay().getName());
        assertEquals("minuteOfHour", ISOChronology.getInstance().minuteOfHour().getName());
        assertEquals("secondOfDay", ISOChronology.getInstance().secondOfDay().getName());
        assertEquals("secondOfMinute", ISOChronology.getInstance().secondOfMinute().getName());
        assertEquals("millisOfDay", ISOChronology.getInstance().millisOfDay().getName());
        assertEquals("millisOfSecond", ISOChronology.getInstance().millisOfSecond().getName());
        
        assertEquals(true, ISOChronology.getInstance().halfdayOfDay().isSupported());
        assertEquals(true, ISOChronology.getInstance().clockhourOfHalfday().isSupported());
        assertEquals(true, ISOChronology.getInstance().hourOfHalfday().isSupported());
        assertEquals(true, ISOChronology.getInstance().clockhourOfDay().isSupported());
        assertEquals(true, ISOChronology.getInstance().hourOfDay().isSupported());
        assertEquals(true, ISOChronology.getInstance().minuteOfDay().isSupported());
        assertEquals(true, ISOChronology.getInstance().minuteOfHour().isSupported());
        assertEquals(true, ISOChronology.getInstance().secondOfDay().isSupported());
        assertEquals(true, ISOChronology.getInstance().secondOfMinute().isSupported());
        assertEquals(true, ISOChronology.getInstance().millisOfDay().isSupported());
        assertEquals(true, ISOChronology.getInstance().millisOfSecond().isSupported());
    }

// org.joda.time.chrono.TestISOChronology::testMaxYear
    public void testMaxYear() {
        final ISOChronology chrono = ISOChronology.getInstanceUTC();
        final int maxYear = chrono.year().getMaximumValue();

        DateTime start = new DateTime(maxYear, 1, 1, 0, 0, 0, 0, chrono);
        DateTime end = new DateTime(maxYear, 12, 31, 23, 59, 59, 999, chrono);
        assertTrue(start.getMillis() > 0);
        assertTrue(end.getMillis() > start.getMillis());
        assertEquals(maxYear, start.getYear());
        assertEquals(maxYear, end.getYear());
        long delta = end.getMillis() - start.getMillis();
        long expectedDelta = 
            (start.year().isLeap() ? 366L : 365L) * DateTimeConstants.MILLIS_PER_DAY - 1;
        assertEquals(expectedDelta, delta);

        assertEquals(start, new DateTime(maxYear + "-01-01T00:00:00.000Z", chrono));
        assertEquals(end, new DateTime(maxYear + "-12-31T23:59:59.999Z", chrono));

        try {
            start.plusYears(1);
            fail();
        } catch (IllegalFieldValueException e) {
        }

        try {
            end.plusYears(1);
            fail();
        } catch (IllegalFieldValueException e) {
        }

        assertEquals(maxYear + 1, chrono.year().get(Long.MAX_VALUE));
    }

// org.joda.time.chrono.TestISOChronology::testMinYear
    public void testMinYear() {
        final ISOChronology chrono = ISOChronology.getInstanceUTC();
        final int minYear = chrono.year().getMinimumValue();

        DateTime start = new DateTime(minYear, 1, 1, 0, 0, 0, 0, chrono);
        DateTime end = new DateTime(minYear, 12, 31, 23, 59, 59, 999, chrono);
        assertTrue(start.getMillis() < 0);
        assertTrue(end.getMillis() > start.getMillis());
        assertEquals(minYear, start.getYear());
        assertEquals(minYear, end.getYear());
        long delta = end.getMillis() - start.getMillis();
        long expectedDelta = 
            (start.year().isLeap() ? 366L : 365L) * DateTimeConstants.MILLIS_PER_DAY - 1;
        assertEquals(expectedDelta, delta);

        assertEquals(start, new DateTime(minYear + "-01-01T00:00:00.000Z", chrono));
        assertEquals(end, new DateTime(minYear + "-12-31T23:59:59.999Z", chrono));

        try {
            start.minusYears(1);
            fail();
        } catch (IllegalFieldValueException e) {
        }

        try {
            end.minusYears(1);
            fail();
        } catch (IllegalFieldValueException e) {
        }

        assertEquals(minYear - 1, chrono.year().get(Long.MIN_VALUE));
    }

// org.joda.time.chrono.TestISOChronology::testCutoverAddYears
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

// org.joda.time.chrono.TestISOChronology::testAddMonths
    public void testAddMonths() {
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

// org.joda.time.chrono.TestISOChronology::testTimeOfDayAdd
    public void testTimeOfDayAdd() {
        TimeOfDay start = new TimeOfDay(12, 30);
        TimeOfDay end = new TimeOfDay(10, 30);
        assertEquals(end, start.plusHours(22));
        assertEquals(start, end.minusHours(22));
        assertEquals(end, start.plusMinutes(22 * 60));
        assertEquals(start, end.minusMinutes(22 * 60));
    }

// org.joda.time.chrono.TestISOChronology::testPartialDayOfYearAdd
    public void testPartialDayOfYearAdd() {
        Partial start = new Partial().with(DateTimeFieldType.year(), 2000).with(DateTimeFieldType.dayOfYear(), 366);
        Partial end = new Partial().with(DateTimeFieldType.year(), 2004).with(DateTimeFieldType.dayOfYear(), 366);
        assertEquals(end, start.withFieldAdded(DurationFieldType.days(), 365 + 365 + 365 + 366));
        assertEquals(start, end.withFieldAdded(DurationFieldType.days(), -(365 + 365 + 365 + 366)));
    }

// org.joda.time.chrono.TestISOChronology::testMaximumValue
    public void testMaximumValue() {
        DateMidnight dt = new DateMidnight(1570, 1, 1);
        while (dt.getYear() < 1590) {
            dt = dt.plusDays(1);
            YearMonthDay ymd = dt.toYearMonthDay();
            assertEquals(dt.year().getMaximumValue(), ymd.year().getMaximumValue());
            assertEquals(dt.monthOfYear().getMaximumValue(), ymd.monthOfYear().getMaximumValue());
            assertEquals(dt.dayOfMonth().getMaximumValue(), ymd.dayOfMonth().getMaximumValue());
        }
    }

// org.joda.time.chrono.TestLenientChronology::test_setYear
    public void test_setYear() {
        Chronology zone = LenientChronology.getInstance(ISOChronology.getInstanceUTC());
        DateTime dt = new DateTime(2007, 1, 1, 0, 0 ,0, 0, zone);
        assertEquals("2007-01-01T00:00:00.000Z", dt.toString());
        dt = dt.withYear(2008);
        assertEquals("2008-01-01T00:00:00.000Z", dt.toString());
    }

// org.joda.time.chrono.TestLenientChronology::test_setMonthOfYear
    public void test_setMonthOfYear() {
        Chronology zone = LenientChronology.getInstance(ISOChronology.getInstanceUTC());
        DateTime dt = new DateTime(2007, 1, 1, 0, 0 ,0, 0, zone);
        assertEquals("2007-01-01T00:00:00.000Z", dt.toString());
        dt = dt.withMonthOfYear(13);
        assertEquals("2008-01-01T00:00:00.000Z", dt.toString());
        dt = dt.withMonthOfYear(0);
        assertEquals("2007-12-01T00:00:00.000Z", dt.toString());
    }

// org.joda.time.chrono.TestLenientChronology::test_setDayOfMonth
    public void test_setDayOfMonth() {
        Chronology zone = LenientChronology.getInstance(ISOChronology.getInstanceUTC());
        DateTime dt = new DateTime(2007, 1, 1, 0, 0 ,0, 0, zone);
        assertEquals("2007-01-01T00:00:00.000Z", dt.toString());
        dt = dt.withDayOfMonth(32);
        assertEquals("2007-02-01T00:00:00.000Z", dt.toString());
        dt = dt.withDayOfMonth(0);
        assertEquals("2007-01-31T00:00:00.000Z", dt.toString());
    }

// org.joda.time.chrono.TestLenientChronology::test_setHourOfDay
    public void test_setHourOfDay() {
        Chronology zone = LenientChronology.getInstance(ISOChronology.getInstanceUTC());
        DateTime dt = new DateTime(2007, 1, 1, 0, 0 ,0, 0, zone);
        assertEquals("2007-01-01T00:00:00.000Z", dt.toString());
        dt = dt.withHourOfDay(24);
        assertEquals("2007-01-02T00:00:00.000Z", dt.toString());
        dt = dt.withHourOfDay(-1);
        assertEquals("2007-01-01T23:00:00.000Z", dt.toString());
    }

// org.joda.time.chrono.TestLenientChronology::testNearDstTransition
    public void testNearDstTransition() {
        

        int hour = 23;
        DateTime dt;

        dt = new DateTime(2006, 10, 29, hour, 0, 0, 0,
                          ISOChronology.getInstance(DateTimeZone.forID("America/Los_Angeles")));
        assertEquals(hour, dt.getHourOfDay()); 

        dt = new DateTime(2006, 10, 29, hour, 0, 0, 0,
                          LenientChronology.getInstance
                          (ISOChronology.getInstance(DateTimeZone.forOffsetHours(-8))));
        assertEquals(hour, dt.getHourOfDay()); 

        dt = new DateTime(2006, 10, 29, hour, 0, 0, 0,
                          LenientChronology.getInstance
                          (ISOChronology.getInstance(DateTimeZone.forID("America/Los_Angeles"))));

        assertEquals(hour, dt.getHourOfDay()); 
    }

// org.joda.time.chrono.TestLenientChronology::test_MockTurkIsCorrect
    public void test_MockTurkIsCorrect() {
        DateTime pre = new DateTime(CUTOVER_TURK - 1L, MOCK_TURK);
        assertEquals("2007-03-31T23:59:59.999-05:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_TURK, MOCK_TURK);
        assertEquals("2007-04-01T01:00:00.000-04:00", at.toString());
        DateTime post = new DateTime(CUTOVER_TURK + 1L, MOCK_TURK);
        assertEquals("2007-04-01T01:00:00.001-04:00", post.toString());
    }

// org.joda.time.chrono.TestLenientChronology::test_lenientChrononolgy_Chicago
    public void test_lenientChrononolgy_Chicago() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        Chronology lenient = LenientChronology.getInstance(ISOChronology.getInstance(zone));
        DateTime dt = new DateTime(2007, 3, 11, 2, 30, 0, 0, lenient);
        assertEquals("2007-03-11T03:30:00.000-05:00", dt.toString());
    }

// org.joda.time.chrono.TestLenientChronology::test_lenientChrononolgy_Turk
    public void test_lenientChrononolgy_Turk() {
        Chronology lenient = LenientChronology.getInstance(ISOChronology.getInstance(MOCK_TURK));
        DateTime dt = new DateTime(2007, 4, 1, 0, 30, 0, 0, lenient);
        assertEquals("2007-04-01T01:30:00.000-04:00", dt.toString());
    }

// org.joda.time.chrono.TestLenientChronology::test_strictChrononolgy_Chicago
    public void test_strictChrononolgy_Chicago() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        Chronology lenient = StrictChronology.getInstance(ISOChronology.getInstance(zone));
        try {
            new DateTime(2007, 3, 11, 2, 30, 0, 0, lenient);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.chrono.TestLenientChronology::test_isoChrononolgy_Chicago
    public void test_isoChrononolgy_Chicago() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        Chronology lenient = ISOChronology.getInstance(zone);
        try {
            new DateTime(2007, 3, 11, 2, 30, 0, 0, lenient);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
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

// org.joda.time.format.TestDateTimeFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        DateTimeFormat f = new DateTimeFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_era
    public void testFormat_era() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("G").withLocale(Locale.UK);
        assertEquals(dt.toString(), "AD", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "AD", f.print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals(dt.toString(), "AD", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_centuryOfEra
    public void testFormat_centuryOfEra() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("C").withLocale(Locale.UK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "1", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_yearOfEra
    public void testFormat_yearOfEra() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("Y").withLocale(Locale.UK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "124", f.print(dt));  
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_yearOfEra_twoDigit
    public void testFormat_yearOfEra_twoDigit() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("YY").withLocale(Locale.UK);
        assertEquals(dt.toString(), "04", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "23", f.print(dt));
        
        
        f = f.withZoneUTC();
        DateTime expect = null;
        expect = new DateTime(2004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));
        
        expect = new DateTime(1922, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("22"));
        
        expect = new DateTime(2021, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("21"));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = f.withPivotYear(new Integer(2050));
        expect = new DateTime(2000, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("00"));

        expect = new DateTime(2099, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("99"));

        
        f = DateTimeFormat.forPattern("YY").withLocale(Locale.UK);
        f = f.withZoneUTC();
        f.parseDateTime("5");
        f.parseDateTime("005");
        f.parseDateTime("+50");
        f.parseDateTime("-50");
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_yearOfEraParse
    public void testFormat_yearOfEraParse() {
        Chronology chrono = GJChronology.getInstanceUTC();

        DateTimeFormatter f = DateTimeFormat
            .forPattern("YYYY-MM GG")
            .withChronology(chrono)
            .withLocale(Locale.UK);

        DateTime dt = new DateTime(2005, 10, 1, 0, 0, 0, 0, chrono);
        assertEquals(dt, f.parseDateTime("2005-10 AD"));
        assertEquals(dt, f.parseDateTime("2005-10 CE"));

        dt = new DateTime(-2005, 10, 1, 0, 0, 0, 0, chrono);
        assertEquals(dt, f.parseDateTime("2005-10 BC"));
        assertEquals(dt, f.parseDateTime("2005-10 BCE"));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_year
    public void testFormat_year() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("y").withLocale(Locale.UK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "-123", f.print(dt));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_year_twoDigit
    public void testFormat_year_twoDigit() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("yy").withLocale(Locale.UK);
        assertEquals(dt.toString(), "04", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "23", f.print(dt));
        
        
        f = f.withZoneUTC();
        DateTime expect = null;
        expect = new DateTime(2004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));
        
        expect = new DateTime(1922, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("22"));
        
        expect = new DateTime(2021, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("21"));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = f.withPivotYear(new Integer(2050));
        expect = new DateTime(2000, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("00"));

        expect = new DateTime(2099, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("99"));

        
        
        f = new DateTimeFormatterBuilder().appendTwoDigitYear(2000).toFormatter();
        f = f.withZoneUTC();
        try {
            f.parseDateTime("5");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("005");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("+50");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("-50");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = DateTimeFormat.forPattern("yy").withLocale(Locale.UK);
        f = f.withZoneUTC();
        f.parseDateTime("5");
        f.parseDateTime("005");
        f.parseDateTime("+50");
        f.parseDateTime("-50");

        
        f = new DateTimeFormatterBuilder().appendTwoDigitYear(2000, true).toFormatter();
        f = f.withZoneUTC();
        expect = new DateTime(2004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+04"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-04"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("4"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-4"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("004"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+004"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-004"));

        expect = new DateTime(3004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("3004"));

        expect = new DateTime(3004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+3004"));

        expect = new DateTime(-3004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-3004"));

        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_year_long
    public void testFormat_year_long() {
        DateTime dt = new DateTime(278004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy");
        assertEquals(dt.toString(), "278004", f.print(dt));
        
        
        f = DateTimeFormat.forPattern("yyyyMMdd");
        assertEquals(dt.toString(), "2780040609", f.print(dt));
        
        
        f = DateTimeFormat.forPattern("yyyyddMM");
        assertEquals(dt.toString(), "2780040906", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_weekyear
    public void testFormat_weekyear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("x").withLocale(Locale.UK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "-123", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_weekyearOfEra_twoDigit
    public void testFormat_weekyearOfEra_twoDigit() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("xx").withLocale(Locale.UK);
        assertEquals(dt.toString(), "04", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "23", f.print(dt));
        
        
        f = f.withZoneUTC();
        DateTime expect = null;
        expect = new DateTime(2003, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));
        
        expect = new DateTime(1922, 1, 2, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("22"));
        
        expect = new DateTime(2021, 1, 4, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("21"));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = f.withPivotYear(new Integer(2050));
        expect = new DateTime(2000, 1, 3, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals(expect, f.parseDateTime("00"));

        expect = new DateTime(2098, 12, 29, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals(expect, f.parseDateTime("99"));

        
        
        f = new DateTimeFormatterBuilder().appendTwoDigitWeekyear(2000).toFormatter();
        f = f.withZoneUTC();
        try {
            f.parseDateTime("5");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("005");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("+50");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("-50");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = DateTimeFormat.forPattern("xx").withLocale(Locale.UK);
        f = f.withZoneUTC();
        f.parseDateTime("5");
        f.parseDateTime("005");
        f.parseDateTime("+50");
        f.parseDateTime("-50");

        
        f = new DateTimeFormatterBuilder().appendTwoDigitWeekyear(2000, true).toFormatter();
        f = f.withZoneUTC();
        expect = new DateTime(2003, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+04"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-04"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("4"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-4"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("004"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+004"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-004"));

        expect = new DateTime(3004, 1, 2, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("3004"));

        expect = new DateTime(3004, 1, 2, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+3004"));

        expect = new DateTime(-3004, 1, 4, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-3004"));

        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_weekOfWeekyear
    public void testFormat_weekOfWeekyear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("w").withLocale(Locale.UK);
        assertEquals(dt.toString(), "24", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "24", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "24", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfWeek
    public void testFormat_dayOfWeek() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("e").withLocale(Locale.UK);
        assertEquals(dt.toString(), "3", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "3", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "3", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfWeekShortText
    public void testFormat_dayOfWeekShortText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("E").withLocale(Locale.UK);
        assertEquals(dt.toString(), "Wed", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "Wed", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Wed", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "mer.", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfWeekText
    public void testFormat_dayOfWeekText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("EEEE").withLocale(Locale.UK);
        assertEquals(dt.toString(), "Wednesday", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "Wednesday", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Wednesday", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "mercredi", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfYearText
    public void testFormat_dayOfYearText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("D").withLocale(Locale.UK);
        assertEquals(dt.toString(), "161", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "161", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "161", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_monthOfYear
    public void testFormat_monthOfYear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("M").withLocale(Locale.UK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "6", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_monthOfYearShortText
    public void testFormat_monthOfYearShortText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("MMM").withLocale(Locale.UK);
        assertEquals(dt.toString(), "Jun", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "Jun", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Jun", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "juin", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_monthOfYearText
    public void testFormat_monthOfYearText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("MMMM").withLocale(Locale.UK);
        assertEquals(dt.toString(), "June", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "June", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "June", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "juin", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfMonth
    public void testFormat_dayOfMonth() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("d").withLocale(Locale.UK);
        assertEquals(dt.toString(), "9", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "9", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "9", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_halfdayOfDay
    public void testFormat_halfdayOfDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("a").withLocale(Locale.UK);
        assertEquals(dt.toString(), "am", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "am", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "pm", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_hourOfHalfday
    public void testFormat_hourOfHalfday() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("K").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "7", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "0", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_clockhourOfHalfday
    public void testFormat_clockhourOfHalfday() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("h").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "7", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "12", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_hourOfDay
    public void testFormat_hourOfDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("H").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "19", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "0", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_clockhourOfDay
    public void testFormat_clockhourOfDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("k").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "19", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "24", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_minute
    public void testFormat_minute() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("m").withLocale(Locale.UK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "20", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_second
    public void testFormat_second() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("s").withLocale(Locale.UK);
        assertEquals(dt.toString(), "30", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "30", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "30", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_fractionOfSecond
    public void testFormat_fractionOfSecond() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("SSS").withLocale(Locale.UK);
        assertEquals(dt.toString(), "040", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "040", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "040", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_fractionOfSecondLong
    public void testFormat_fractionOfSecondLong() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("SSSSSS").withLocale(Locale.UK);
        assertEquals(dt.toString(), "040000", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "040000", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "040000", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneText
    public void testFormat_zoneText() {}

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneLongText
    public void testFormat_zoneLongText() {}

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneAmount
    public void testFormat_zoneAmount() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("Z").withLocale(Locale.UK);
        assertEquals(dt.toString(), "+0000", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "-0400", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "+0900", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneAmountColon
    public void testFormat_zoneAmountColon() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("ZZ").withLocale(Locale.UK);
        assertEquals(dt.toString(), "+00:00", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "-04:00", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "+09:00", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneAmountID
    public void testFormat_zoneAmountID() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("ZZZ").withLocale(Locale.UK);
        assertEquals(dt.toString(), "UTC", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "America/New_York", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Asia/Tokyo", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_other
    public void testFormat_other() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("'Hello' ''");
        assertEquals("Hello '", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_invalid
    public void testFormat_invalid() {
        try {
            DateTimeFormat.forPattern(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forPattern("");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forPattern("A");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forPattern("dd/mm/AA");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_samples
    public void testFormat_samples() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH.mm.ss");
        assertEquals("2004-06-09 10.20.30", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_shortBasicParse
    public void testFormat_shortBasicParse() {
        
        

        DateTime dt = new DateTime(2004, 3, 9, 0, 0, 0, 0);

        DateTimeFormatter f = DateTimeFormat.forPattern("yyMMdd");
        assertEquals(dt, f.parseDateTime("040309"));
        try {
            assertEquals(dt, f.parseDateTime("20040309"));
            fail();
        } catch (IllegalArgumentException ex) {}

        f = DateTimeFormat.forPattern("yy/MM/dd");
        assertEquals(dt, f.parseDateTime("04/03/09"));
        assertEquals(dt, f.parseDateTime("2004/03/09"));
    }

// org.joda.time.format.TestDateTimeFormat::testParse_pivotYear
    public void testParse_pivotYear() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd.MM.yy").withPivotYear(2050).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("25.12.15");
        assertEquals(date.getYear(), 2015);
        
        date = dateFormatter.parseDateTime("25.12.00");
        assertEquals(date.getYear(), 2000);
        
        date = dateFormatter.parseDateTime("25.12.99");
        assertEquals(date.getYear(), 2099);
    }

// org.joda.time.format.TestDateTimeFormat::testParse_pivotYear_ignored4DigitYear
    public void testParse_pivotYear_ignored4DigitYear() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd.MM.yyyy").withPivotYear(2050).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("25.12.15");
        assertEquals(date.getYear(), 15);
        
        date = dateFormatter.parseDateTime("25.12.00");
        assertEquals(date.getYear(), 0);
        
        date = dateFormatter.parseDateTime("25.12.99");
        assertEquals(date.getYear(), 99);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShort_UK
    public void testFormatParse_textMonthJanShort_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 1, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals(str, "23 Jan 2007");
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShortLowerCase_UK
    public void testFormatParse_textMonthJanShortLowerCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 jan 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShortUpperCase_UK
    public void testFormatParse_textMonthJanShortUpperCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 JAN 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testParse_textMonthJanLong_UK
    public void testParse_textMonthJanLong_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("23 January 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanLongLowerCase_UK
    public void testFormatParse_textMonthJanLongLowerCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 january 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanLongUpperCase_UK
    public void testFormatParse_textMonthJanLongUpperCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 JANUARY 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShort_France
    public void testFormatParse_textMonthJanShort_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 1, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 janv. 2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanLong_France
    public void testFormatParse_textMonthJanLong_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("23 janvier 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthApr_France
    public void testFormatParse_textMonthApr_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 2, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 f\u00E9vr. 2007", str);  
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 2, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthAtEnd_France
    public void testFormatParse_textMonthAtEnd_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 juin", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2000, 6, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthAtEnd_France_withSpecifiedDefault
    public void testFormatParse_textMonthAtEnd_France_withSpecifiedDefault() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM")
            .withLocale(Locale.FRANCE).withZoneUTC().withDefaultYear(1980);
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 juin", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 1980, 6, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthApr_Korean
    public void testFormatParse_textMonthApr_Korean() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("EEEE, d MMMM yyyy HH:mm")
            .withLocale(Locale.KOREAN).withZoneUTC();
        
        String str = new DateTime(2007, 3, 8, 22, 0, 0, 0, UTC).toString(dateFormatter);
        DateTime date = dateFormatter.parseDateTime(str);
        assertEquals(new DateTime(2007, 3, 8, 22, 0, 0, 0, UTC), date);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textHalfdayAM_UK
    public void testFormatParse_textHalfdayAM_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendClockhourOfHalfday(2)
            .appendLiteral('-')
            .appendHalfdayOfDayText()
            .appendLiteral('-')
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 18, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$06-pm-2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textHalfdayAM_France
    public void testFormatParse_textHalfdayAM_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendClockhourOfHalfday(2)
            .appendLiteral('-')
            .appendHalfdayOfDayText()
            .appendLiteral('-')
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 18, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$06-PM-2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textEraAD_UK
    public void testFormatParse_textEraAD_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendEraText()
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$AD2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textEraAD_France
    public void testFormatParse_textEraAD_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendEraText()
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$ap. J.-C.2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textEraBC_France
    public void testFormatParse_textEraBC_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendEraText()
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(-1, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$av. J.-C.-0001", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, -1, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textYear_UK
    public void testFormatParse_textYear_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendText(DateTimeFieldType.year())
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$2007", str);
        try {
            dateFormatter.parseDateTime(str);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textYear_France
    public void testFormatParse_textYear_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendText(DateTimeFieldType.year())
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$2007", str);
        try {
            dateFormatter.parseDateTime(str);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textAdjoiningHelloWorld_UK
    public void testFormatParse_textAdjoiningHelloWorld_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendDayOfMonth(2)
            .appendMonthOfYearShortText()
            .appendLiteral("HelloWorld")
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$23JunHelloWorld", str);
        dateFormatter.parseDateTime(str);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textAdjoiningMonthDOW_UK
    public void testFormatParse_textAdjoiningMonthDOW_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendDayOfMonth(2)
            .appendMonthOfYearShortText()
            .appendDayOfWeekShortText()
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$23JunSat", str);
        dateFormatter.parseDateTime(str);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_noColon
    public void testFormatParse_zoneId_noColon() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm Z").withZoneUTC();
        String str = new DateTime(2007, 6, 23, 1, 2, 0, 0, UTC).toString(dateFormatter);
        assertEquals("01:02 +0000", str);
        DateTime parsed = dateFormatter.parseDateTime(str);
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_noColon_parseZ
    public void testFormatParse_zoneId_noColon_parseZ() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm Z").withZoneUTC();
        DateTime parsed = dateFormatter.parseDateTime("01:02 Z");
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_colon
    public void testFormatParse_zoneId_colon() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm ZZ").withZoneUTC();
        String str = new DateTime(2007, 6, 23, 1, 2, 0, 0, UTC).toString(dateFormatter);
        assertEquals("01:02 +00:00", str);
        DateTime parsed = dateFormatter.parseDateTime(str);
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_colon_parseZ
    public void testFormatParse_zoneId_colon_parseZ() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm ZZ").withZoneUTC();
        DateTime parsed = dateFormatter.parseDateTime("01:02 Z");
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_stringLengths
    public void testForStyle_stringLengths() {
        try {
            DateTimeFormat.forStyle(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("S");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("SSS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_invalidStrings
    public void testForStyle_invalidStrings() {
        try {
            DateTimeFormat.forStyle("AA");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("--");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("ss");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortDate
    public void testForStyle_shortDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("S-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        DateTime date = new DateTime(
                DateFormat.getDateInstance(DateFormat.SHORT, FRANCE).parse(expect));
        assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortTime
    public void testForStyle_shortTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("-S");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getTimeInstance(DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        if (TimeZone.getDefault() instanceof SimpleTimeZone) {
            
        } else {
            DateTime date = new DateTime(
                DateFormat.getTimeInstance(DateFormat.SHORT, FRANCE).parse(expect));
            assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
        }
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortDateTime
    public void testForStyle_shortDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortDateTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("SS");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        DateTime date = new DateTime(
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, FRANCE).parse(expect));
        assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumDate
    public void testForStyle_mediumDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("M-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumTime
    public void testForStyle_mediumTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("-M");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumDateTime
    public void testForStyle_mediumDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumDateTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("MM");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longDate
    public void testForStyle_longDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.longDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("L-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.LONG, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.LONG, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.LONG, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longTime
    public void testForStyle_longTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longDateTime
    public void testForStyle_longDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullDate
    public void testForStyle_fullDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.fullDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("F-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.FULL, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.FULL, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.FULL, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullTime
    public void testForStyle_fullTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullDateTime
    public void testForStyle_fullDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortMediumDateTime
    public void testForStyle_shortMediumDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.forStyle("SM");
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortLongDateTime
    public void testForStyle_shortLongDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortFullDateTime
    public void testForStyle_shortFullDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumShortDateTime
    public void testForStyle_mediumShortDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.forStyle("MS");
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumLongDateTime
    public void testForStyle_mediumLongDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumFullDateTime
    public void testForStyle_mediumFullDateTime() {}

// org.joda.time.format.TestDateTimeFormatter::testPrint_simple
    public void testPrint_simple() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T10:20:30Z", f.print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_locale
    public void testPrint_locale() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("mer. 2004-06-09T10:20:30Z", f.withLocale(Locale.FRENCH).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withLocale(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_zone
    public void testPrint_zone() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withZone(null).print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withZoneUTC().print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_chrono
    public void testPrint_chrono() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withChronology(ISO_PARIS).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(BUDDHIST_PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withChronology(null).print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withChronology(ISO_PARIS).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(BUDDHIST_PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withChronology(ISO_UTC).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_bufferMethods
    public void testPrint_bufferMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", buf.toString());
        
        buf = new StringBuffer();
        f.printTo(buf, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", buf.toString());
        
        buf = new StringBuffer();
        ISODateTimeFormat.yearMonthDay().printTo(buf, dt.toYearMonthDay());
        assertEquals("2004-06-09", buf.toString());
        
        buf = new StringBuffer();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(buf, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_writerMethods
    public void testPrint_writerMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        CharArrayWriter out = new CharArrayWriter();
        f.printTo(out, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", out.toString());
        
        out = new CharArrayWriter();
        f.printTo(out, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", out.toString());
        
        out = new CharArrayWriter();
        ISODateTimeFormat.yearMonthDay().printTo(out, dt.toYearMonthDay());
        assertEquals("2004-06-09", out.toString());
        
        out = new CharArrayWriter();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(out, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_appendableMethods
    public void testPrint_appendableMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        StringBuilder buf = new StringBuilder();
        f.printTo(buf, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", buf.toString());
        
        buf = new StringBuilder();
        f.printTo(buf, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", buf.toString());
        
        buf = new StringBuilder();
        ISODateTimeFormat.yearMonthDay().printTo(buf, dt.toLocalDate());
        assertEquals("2004-06-09", buf.toString());
        
        buf = new StringBuilder();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(buf, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_chrono_and_zone
    public void testPrint_chrono_and_zone() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T10:20:30Z",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
        
        dt = dt.withChronology(ISO_PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2547-06-09T12:20:30+02:00",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2547-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetLocale
    public void testWithGetLocale() {
        DateTimeFormatter f2 = f.withLocale(Locale.FRENCH);
        assertEquals(Locale.FRENCH, f2.getLocale());
        assertSame(f2, f2.withLocale(Locale.FRENCH));
        
        f2 = f.withLocale(null);
        assertEquals(null, f2.getLocale());
        assertSame(f2, f2.withLocale(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetZone
    public void testWithGetZone() {
        DateTimeFormatter f2 = f.withZone(PARIS);
        assertEquals(PARIS, f2.getZone());
        assertSame(f2, f2.withZone(PARIS));
        
        f2 = f.withZone(null);
        assertEquals(null, f2.getZone());
        assertSame(f2, f2.withZone(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetChronology
    public void testWithGetChronology() {
        DateTimeFormatter f2 = f.withChronology(BUDDHIST_PARIS);
        assertEquals(BUDDHIST_PARIS, f2.getChronology());
        assertSame(f2, f2.withChronology(BUDDHIST_PARIS));
        
        f2 = f.withChronology(null);
        assertEquals(null, f2.getChronology());
        assertSame(f2, f2.withChronology(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetPivotYear
    public void testWithGetPivotYear() {
        DateTimeFormatter f2 = f.withPivotYear(13);
        assertEquals(new Integer(13), f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(13));
        
        f2 = f.withPivotYear(new Integer(14));
        assertEquals(new Integer(14), f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(new Integer(14)));
        
        f2 = f.withPivotYear(null);
        assertEquals(null, f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetOffsetParsedMethods
    public void testWithGetOffsetParsedMethods() {
        DateTimeFormatter f2 = f;
        assertEquals(false, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f.withOffsetParsed();
        assertEquals(true, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f2.withZone(PARIS);
        assertEquals(false, f2.isOffsetParsed());
        assertEquals(PARIS, f2.getZone());
        
        f2 = f2.withOffsetParsed();
        assertEquals(true, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f.withOffsetParsed();
        assertNotSame(f, f2);
        DateTimeFormatter f3 = f2.withOffsetParsed();
        assertSame(f2, f3);
    }

// org.joda.time.format.TestDateTimeFormatter::testPrinterParserMethods
    public void testPrinterParserMethods() {
        DateTimeFormatter f2 = new DateTimeFormatter(f.getPrinter(), f.getParser());
        assertEquals(f.getPrinter(), f2.getPrinter());
        assertEquals(f.getParser(), f2.getParser());
        assertEquals(true, f2.isPrinter());
        assertEquals(true, f2.isParser());
        assertNotNull(f2.print(0L));
        assertNotNull(f2.parseDateTime("Thu 1970-01-01T00:00:00Z"));
        
        f2 = new DateTimeFormatter(f.getPrinter(), null);
        assertEquals(f.getPrinter(), f2.getPrinter());
        assertEquals(null, f2.getParser());
        assertEquals(true, f2.isPrinter());
        assertEquals(false, f2.isParser());
        assertNotNull(f2.print(0L));
        try {
            f2.parseDateTime("Thu 1970-01-01T00:00:00Z");
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        f2 = new DateTimeFormatter(null, f.getParser());
        assertEquals(null, f2.getPrinter());
        assertEquals(f.getParser(), f2.getParser());
        assertEquals(false, f2.isPrinter());
        assertEquals(true, f2.isParser());
        try {
            f2.print(0L);
            fail();
        } catch (UnsupportedOperationException ex) {}
        assertNotNull(f2.parseDateTime("Thu 1970-01-01T00:00:00Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_simple
    public void testParseLocalDate_simple() {
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30Z"));
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalDate(2004, 6, 9, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalDate("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_yearOfEra
    public void testParseLocalDate_yearOfEra() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("YYYY-MM GG")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        LocalDate date = new LocalDate(2005, 10, 1, chrono);
        assertEquals(date, f.parseLocalDate("2005-10 AD"));
        assertEquals(date, f.parseLocalDate("2005-10 CE"));
        
        date = new LocalDate(-2005, 10, 1, chrono);
        assertEquals(date, f.parseLocalDate("2005-10 BC"));
        assertEquals(date, f.parseLocalDate("2005-10 BCE"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_yearOfCentury
    public void testParseLocalDate_yearOfCentury() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("yy M d")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withPivotYear(2050);
        
        LocalDate date = new LocalDate(2050, 8, 4, chrono);
        assertEquals(date, f.parseLocalDate("50 8 4"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_monthDay_feb29
    public void testParseLocalDate_monthDay_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        assertEquals(new LocalDate(2000, 2, 29, chrono), f.parseLocalDate("2 29"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_monthDay_withDefaultYear_feb29
    public void testParseLocalDate_monthDay_withDefaultYear_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withDefaultYear(2012);
        
        assertEquals(new LocalDate(2012, 2, 29, chrono), f.parseLocalDate("2 29"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_weekyear_month_week_2010
    public void testParseLocalDate_weekyear_month_week_2010() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2010, 1, 4, chrono), f.parseLocalDate("2010-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_weekyear_month_week_2011
    public void testParseLocalDate_weekyear_month_week_2011() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2011, 1, 3, chrono), f.parseLocalDate("2011-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_weekyear_month_week_2012
    public void testParseLocalDate_weekyear_month_week_2012() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2012, 1, 2, chrono), f.parseLocalDate("2012-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2010
    public void testParseLocalDate_year_month_week_2010() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2010, 1, 4, chrono), f.parseLocalDate("2010-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2011
    public void testParseLocalDate_year_month_week_2011() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2011, 1, 3, chrono), f.parseLocalDate("2011-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2012
    public void testParseLocalDate_year_month_week_2012() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2012, 1, 2, chrono), f.parseLocalDate("2012-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2013
    public void testParseLocalDate_year_month_week_2013() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2012, 12, 31, chrono), f.parseLocalDate("2013-01-01"));  
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2014
    public void testParseLocalDate_year_month_week_2014() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2013, 12, 30, chrono), f.parseLocalDate("2014-01-01"));  
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2015
    public void testParseLocalDate_year_month_week_2015() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2014, 12, 29, chrono), f.parseLocalDate("2015-01-01"));  
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2016
    public void testParseLocalDate_year_month_week_2016() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2016, 1, 4, chrono), f.parseLocalDate("2016-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalTime_simple
    public void testParseLocalTime_simple() {
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30Z"));
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalTime(10, 20, 30, 0, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalTime("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_simple
    public void testParseLocalDateTime_simple() {
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30Z"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 0, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalDateTime("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_monthDay_feb29
    public void testParseLocalDateTime_monthDay_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d H m")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        assertEquals(new LocalDateTime(2000, 2, 29, 13, 40, 0, 0, chrono), f.parseLocalDateTime("2 29 13 40"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_monthDay_withDefaultYear_feb29
    public void testParseLocalDateTime_monthDay_withDefaultYear_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d H m")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withDefaultYear(2012);
        
        assertEquals(new LocalDateTime(2012, 2, 29, 13, 40, 0, 0, chrono), f.parseLocalDateTime("2 29 13 40"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_simple
    public void testParseDateTime_simple() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.parseDateTime("2004-06-09T10:20:30Z"));
        
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone
    public void testParseDateTime_zone() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone2
    public void testParseDateTime_zone2() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseDateTime("2004-06-09T06:20:30-04:00"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone3
    public void testParseDateTime_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(LONDON).parseDateTime("2004-06-09T10:20:30"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(null).parseDateTime("2004-06-09T10:20:30"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        assertEquals(expect, h.withZone(PARIS).parseDateTime("2004-06-09T10:20:30"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_simple_precedence
    public void testParseDateTime_simple_precedence() {
        DateTime expect = null;
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, f.parseDateTime("Wed 2004-06-09T10:20:30Z"));
        
        
        expect = new DateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        
        assertEquals(expect, f.parseDateTime("Mon 2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_offsetParsed
    public void testParseDateTime_offsetParsed() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withOffsetParsed().parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        assertEquals(expect, g.withOffsetParsed().parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withZone(PARIS).withOffsetParsed().parseDateTime("2004-06-09T10:20:30Z"));
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withOffsetParsed().withZone(PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_chrono
    public void testParseDateTime_chrono() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withChronology(ISO_PARIS).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0,LONDON);
        assertEquals(expect, g.withChronology(null).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseDateTime("2547-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS); 
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_simple
    public void testParseMutableDateTime_simple() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        try {
            g.parseMutableDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone
    public void testParseMutableDateTime_zone() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone2
    public void testParseMutableDateTime_zone2() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone3
    public void testParseMutableDateTime_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(LONDON).parseMutableDateTime("2004-06-09T10:20:30"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(null).parseMutableDateTime("2004-06-09T10:20:30"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        assertEquals(expect, h.withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_simple_precedence
    public void testParseMutableDateTime_simple_precedence() {
        MutableDateTime expect = null;
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, f.parseDateTime("Wed 2004-06-09T10:20:30Z"));
        
        
        expect = new MutableDateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        
        assertEquals(expect, f.parseDateTime("Mon 2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_offsetParsed
    public void testParseMutableDateTime_offsetParsed() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withOffsetParsed().parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        assertEquals(expect, g.withOffsetParsed().parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withZone(PARIS).withOffsetParsed().parseMutableDateTime("2004-06-09T10:20:30Z"));
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withOffsetParsed().withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_chrono
    public void testParseMutableDateTime_chrono() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withChronology(ISO_PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0,LONDON);
        assertEquals(expect, g.withChronology(null).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseMutableDateTime("2547-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS); 
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_simple
    public void testParseInto_simple() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        MutableDateTime result = new MutableDateTime(0L);
        assertEquals(20, g.parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        try {
            g.parseInto(null, "2004-06-09T10:20:30Z", 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(~0, g.parseInto(result, "ABC", 0));
        assertEquals(~10, g.parseInto(result, "2004-06-09", 0));
        assertEquals(~13, g.parseInto(result, "XX2004-06-09T", 2));
    }
