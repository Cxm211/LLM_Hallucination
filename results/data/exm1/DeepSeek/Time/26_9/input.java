// buggy code
        public long add(long instant, int value) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                long localInstant = iField.add(instant + offset, value);
                return localInstant - offset;
            } else {
               long localInstant = iZone.convertUTCToLocal(instant);
               localInstant = iField.add(localInstant, value);
               return iZone.convertLocalToUTC(localInstant, false);
            }
        }

        public long add(long instant, long value) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                long localInstant = iField.add(instant + offset, value);
                return localInstant - offset;
            } else {
               long localInstant = iZone.convertUTCToLocal(instant);
               localInstant = iField.add(localInstant, value);
               return iZone.convertLocalToUTC(localInstant, false);
            }
        }

        public long addWrapField(long instant, int value) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                long localInstant = iField.addWrapField(instant + offset, value);
                return localInstant - offset;
            } else {
                long localInstant = iZone.convertUTCToLocal(instant);
                localInstant = iField.addWrapField(localInstant, value);
                return iZone.convertLocalToUTC(localInstant, false);
            }
        }

        public long set(long instant, int value) {
            long localInstant = iZone.convertUTCToLocal(instant);
            localInstant = iField.set(localInstant, value);
            long result = iZone.convertLocalToUTC(localInstant, false);
            if (get(result) != value) {
                throw new IllegalFieldValueException(iField.getType(), new Integer(value),
                    "Illegal instant due to time zone offset transition: " +
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").print(new Instant(localInstant)) +
                    " (" + iZone.getID() + ")");
            }
            return result;
        }

        public long set(long instant, String text, Locale locale) {
            // cannot verify that new value stuck because set may be lenient
            long localInstant = iZone.convertUTCToLocal(instant);
            localInstant = iField.set(localInstant, text, locale);
            return iZone.convertLocalToUTC(localInstant, false);
        }

        public long roundFloor(long instant) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                instant = iField.roundFloor(instant + offset);
                return instant - offset;
            } else {
                long localInstant = iZone.convertUTCToLocal(instant);
                localInstant = iField.roundFloor(localInstant);
                return iZone.convertLocalToUTC(localInstant, false);
            }
        }

        public long roundCeiling(long instant) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                instant = iField.roundCeiling(instant + offset);
                return instant - offset;
            } else {
                long localInstant = iZone.convertUTCToLocal(instant);
                localInstant = iField.roundCeiling(localInstant);
                return iZone.convertLocalToUTC(localInstant, false);
            }
        }

    public long convertUTCToLocal(long instantUTC) {
        int offset = getOffset(instantUTC);
        long instantLocal = instantUTC + offset;
        // If there is a sign change, but the two values have the same sign...
        if ((instantUTC ^ instantLocal) < 0 && (instantUTC ^ offset) >= 0) {
            throw new ArithmeticException("Adding time zone offset caused overflow");
        }
        return instantLocal;
    }

    public long set(long instant, int value) {
        // lenient needs to handle time zone chronologies
        // so we do the calculation using local milliseconds
        long localInstant = iBase.getZone().convertUTCToLocal(instant);
        long difference = FieldUtils.safeSubtract(value, get(instant));
        localInstant = getType().getField(iBase.withUTC()).add(localInstant, difference);
        return iBase.getZone().convertLocalToUTC(localInstant, false);
    }

// relevant test
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
    }

// org.joda.time.TestYearMonth_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 30, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        YearMonth test = new YearMonth(LONDON);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        
        test = new YearMonth(PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(7, test.getMonthOfYear());
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
        assertEquals("eras", CopticChronology.getInstance().eras().getName());
        assertEquals("centuries", CopticChronology.getInstance().centuries().getName());
        assertEquals("years", CopticChronology.getInstance().years().getName());
        assertEquals("weekyears", CopticChronology.getInstance().weekyears().getName());
        assertEquals("months", CopticChronology.getInstance().months().getName());
        assertEquals("weeks", CopticChronology.getInstance().weeks().getName());
        assertEquals("days", CopticChronology.getInstance().days().getName());
        assertEquals("halfdays", CopticChronology.getInstance().halfdays().getName());
        assertEquals("hours", CopticChronology.getInstance().hours().getName());
        assertEquals("minutes", CopticChronology.getInstance().minutes().getName());
        assertEquals("seconds", CopticChronology.getInstance().seconds().getName());
        assertEquals("millis", CopticChronology.getInstance().millis().getName());
        
        assertEquals(false, CopticChronology.getInstance().eras().isSupported());
        assertEquals(true, CopticChronology.getInstance().centuries().isSupported());
        assertEquals(true, CopticChronology.getInstance().years().isSupported());
        assertEquals(true, CopticChronology.getInstance().weekyears().isSupported());
        assertEquals(true, CopticChronology.getInstance().months().isSupported());
        assertEquals(true, CopticChronology.getInstance().weeks().isSupported());
        assertEquals(true, CopticChronology.getInstance().days().isSupported());
        assertEquals(true, CopticChronology.getInstance().halfdays().isSupported());
        assertEquals(true, CopticChronology.getInstance().hours().isSupported());
        assertEquals(true, CopticChronology.getInstance().minutes().isSupported());
        assertEquals(true, CopticChronology.getInstance().seconds().isSupported());
        assertEquals(true, CopticChronology.getInstance().millis().isSupported());
        
        assertEquals(false, CopticChronology.getInstance().centuries().isPrecise());
        assertEquals(false, CopticChronology.getInstance().years().isPrecise());
        assertEquals(false, CopticChronology.getInstance().weekyears().isPrecise());
        assertEquals(false, CopticChronology.getInstance().months().isPrecise());
        assertEquals(false, CopticChronology.getInstance().weeks().isPrecise());
        assertEquals(false, CopticChronology.getInstance().days().isPrecise());
        assertEquals(false, CopticChronology.getInstance().halfdays().isPrecise());
        assertEquals(true, CopticChronology.getInstance().hours().isPrecise());
        assertEquals(true, CopticChronology.getInstance().minutes().isPrecise());
        assertEquals(true, CopticChronology.getInstance().seconds().isPrecise());
        assertEquals(true, CopticChronology.getInstance().millis().isPrecise());
        
        assertEquals(false, CopticChronology.getInstanceUTC().centuries().isPrecise());
        assertEquals(false, CopticChronology.getInstanceUTC().years().isPrecise());
        assertEquals(false, CopticChronology.getInstanceUTC().weekyears().isPrecise());
        assertEquals(false, CopticChronology.getInstanceUTC().months().isPrecise());
        assertEquals(true, CopticChronology.getInstanceUTC().weeks().isPrecise());
        assertEquals(true, CopticChronology.getInstanceUTC().days().isPrecise());
        assertEquals(true, CopticChronology.getInstanceUTC().halfdays().isPrecise());
        assertEquals(true, CopticChronology.getInstanceUTC().hours().isPrecise());
        assertEquals(true, CopticChronology.getInstanceUTC().minutes().isPrecise());
        assertEquals(true, CopticChronology.getInstanceUTC().seconds().isPrecise());
        assertEquals(true, CopticChronology.getInstanceUTC().millis().isPrecise());
        
        DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        assertEquals(false, CopticChronology.getInstance(gmt).centuries().isPrecise());
        assertEquals(false, CopticChronology.getInstance(gmt).years().isPrecise());
        assertEquals(false, CopticChronology.getInstance(gmt).weekyears().isPrecise());
        assertEquals(false, CopticChronology.getInstance(gmt).months().isPrecise());
        assertEquals(true, CopticChronology.getInstance(gmt).weeks().isPrecise());
        assertEquals(true, CopticChronology.getInstance(gmt).days().isPrecise());
        assertEquals(true, CopticChronology.getInstance(gmt).halfdays().isPrecise());
        assertEquals(true, CopticChronology.getInstance(gmt).hours().isPrecise());
        assertEquals(true, CopticChronology.getInstance(gmt).minutes().isPrecise());
        assertEquals(true, CopticChronology.getInstance(gmt).seconds().isPrecise());
        assertEquals(true, CopticChronology.getInstance(gmt).millis().isPrecise());
    }

// org.joda.time.chrono.TestCopticChronology::testDateFields
    public void testDateFields() {
        assertEquals("era", CopticChronology.getInstance().era().getName());
        assertEquals("centuryOfEra", CopticChronology.getInstance().centuryOfEra().getName());
        assertEquals("yearOfCentury", CopticChronology.getInstance().yearOfCentury().getName());
        assertEquals("yearOfEra", CopticChronology.getInstance().yearOfEra().getName());
        assertEquals("year", CopticChronology.getInstance().year().getName());
        assertEquals("monthOfYear", CopticChronology.getInstance().monthOfYear().getName());
        assertEquals("weekyearOfCentury", CopticChronology.getInstance().weekyearOfCentury().getName());
        assertEquals("weekyear", CopticChronology.getInstance().weekyear().getName());
        assertEquals("weekOfWeekyear", CopticChronology.getInstance().weekOfWeekyear().getName());
        assertEquals("dayOfYear", CopticChronology.getInstance().dayOfYear().getName());
        assertEquals("dayOfMonth", CopticChronology.getInstance().dayOfMonth().getName());
        assertEquals("dayOfWeek", CopticChronology.getInstance().dayOfWeek().getName());
        
        assertEquals(true, CopticChronology.getInstance().era().isSupported());
        assertEquals(true, CopticChronology.getInstance().centuryOfEra().isSupported());
        assertEquals(true, CopticChronology.getInstance().yearOfCentury().isSupported());
        assertEquals(true, CopticChronology.getInstance().yearOfEra().isSupported());
        assertEquals(true, CopticChronology.getInstance().year().isSupported());
        assertEquals(true, CopticChronology.getInstance().monthOfYear().isSupported());
        assertEquals(true, CopticChronology.getInstance().weekyearOfCentury().isSupported());
        assertEquals(true, CopticChronology.getInstance().weekyear().isSupported());
        assertEquals(true, CopticChronology.getInstance().weekOfWeekyear().isSupported());
        assertEquals(true, CopticChronology.getInstance().dayOfYear().isSupported());
        assertEquals(true, CopticChronology.getInstance().dayOfMonth().isSupported());
        assertEquals(true, CopticChronology.getInstance().dayOfWeek().isSupported());
    }

// org.joda.time.chrono.TestCopticChronology::testTimeFields
    public void testTimeFields() {
        assertEquals("halfdayOfDay", CopticChronology.getInstance().halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", CopticChronology.getInstance().clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", CopticChronology.getInstance().hourOfHalfday().getName());
        assertEquals("clockhourOfDay", CopticChronology.getInstance().clockhourOfDay().getName());
        assertEquals("hourOfDay", CopticChronology.getInstance().hourOfDay().getName());
        assertEquals("minuteOfDay", CopticChronology.getInstance().minuteOfDay().getName());
        assertEquals("minuteOfHour", CopticChronology.getInstance().minuteOfHour().getName());
        assertEquals("secondOfDay", CopticChronology.getInstance().secondOfDay().getName());
        assertEquals("secondOfMinute", CopticChronology.getInstance().secondOfMinute().getName());
        assertEquals("millisOfDay", CopticChronology.getInstance().millisOfDay().getName());
        assertEquals("millisOfSecond", CopticChronology.getInstance().millisOfSecond().getName());
        
        assertEquals(true, CopticChronology.getInstance().halfdayOfDay().isSupported());
        assertEquals(true, CopticChronology.getInstance().clockhourOfHalfday().isSupported());
        assertEquals(true, CopticChronology.getInstance().hourOfHalfday().isSupported());
        assertEquals(true, CopticChronology.getInstance().clockhourOfDay().isSupported());
        assertEquals(true, CopticChronology.getInstance().hourOfDay().isSupported());
        assertEquals(true, CopticChronology.getInstance().minuteOfDay().isSupported());
        assertEquals(true, CopticChronology.getInstance().minuteOfHour().isSupported());
        assertEquals(true, CopticChronology.getInstance().secondOfDay().isSupported());
        assertEquals(true, CopticChronology.getInstance().secondOfMinute().isSupported());
        assertEquals(true, CopticChronology.getInstance().millisOfDay().isSupported());
        assertEquals(true, CopticChronology.getInstance().millisOfSecond().isSupported());
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
        assertEquals("eras", EthiopicChronology.getInstance().eras().getName());
        assertEquals("centuries", EthiopicChronology.getInstance().centuries().getName());
        assertEquals("years", EthiopicChronology.getInstance().years().getName());
        assertEquals("weekyears", EthiopicChronology.getInstance().weekyears().getName());
        assertEquals("months", EthiopicChronology.getInstance().months().getName());
        assertEquals("weeks", EthiopicChronology.getInstance().weeks().getName());
        assertEquals("days", EthiopicChronology.getInstance().days().getName());
        assertEquals("halfdays", EthiopicChronology.getInstance().halfdays().getName());
        assertEquals("hours", EthiopicChronology.getInstance().hours().getName());
        assertEquals("minutes", EthiopicChronology.getInstance().minutes().getName());
        assertEquals("seconds", EthiopicChronology.getInstance().seconds().getName());
        assertEquals("millis", EthiopicChronology.getInstance().millis().getName());
        
        assertEquals(false, EthiopicChronology.getInstance().eras().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().centuries().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().years().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().weekyears().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().months().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().weeks().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().days().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().halfdays().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().hours().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().minutes().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().seconds().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().millis().isSupported());
        
        assertEquals(false, EthiopicChronology.getInstance().centuries().isPrecise());
        assertEquals(false, EthiopicChronology.getInstance().years().isPrecise());
        assertEquals(false, EthiopicChronology.getInstance().weekyears().isPrecise());
        assertEquals(false, EthiopicChronology.getInstance().months().isPrecise());
        assertEquals(false, EthiopicChronology.getInstance().weeks().isPrecise());
        assertEquals(false, EthiopicChronology.getInstance().days().isPrecise());
        assertEquals(false, EthiopicChronology.getInstance().halfdays().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance().hours().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance().minutes().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance().seconds().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance().millis().isPrecise());
        
        assertEquals(false, EthiopicChronology.getInstanceUTC().centuries().isPrecise());
        assertEquals(false, EthiopicChronology.getInstanceUTC().years().isPrecise());
        assertEquals(false, EthiopicChronology.getInstanceUTC().weekyears().isPrecise());
        assertEquals(false, EthiopicChronology.getInstanceUTC().months().isPrecise());
        assertEquals(true, EthiopicChronology.getInstanceUTC().weeks().isPrecise());
        assertEquals(true, EthiopicChronology.getInstanceUTC().days().isPrecise());
        assertEquals(true, EthiopicChronology.getInstanceUTC().halfdays().isPrecise());
        assertEquals(true, EthiopicChronology.getInstanceUTC().hours().isPrecise());
        assertEquals(true, EthiopicChronology.getInstanceUTC().minutes().isPrecise());
        assertEquals(true, EthiopicChronology.getInstanceUTC().seconds().isPrecise());
        assertEquals(true, EthiopicChronology.getInstanceUTC().millis().isPrecise());
        
        DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        assertEquals(false, EthiopicChronology.getInstance(gmt).centuries().isPrecise());
        assertEquals(false, EthiopicChronology.getInstance(gmt).years().isPrecise());
        assertEquals(false, EthiopicChronology.getInstance(gmt).weekyears().isPrecise());
        assertEquals(false, EthiopicChronology.getInstance(gmt).months().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance(gmt).weeks().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance(gmt).days().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance(gmt).halfdays().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance(gmt).hours().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance(gmt).minutes().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance(gmt).seconds().isPrecise());
        assertEquals(true, EthiopicChronology.getInstance(gmt).millis().isPrecise());
    }

// org.joda.time.chrono.TestEthiopicChronology::testDateFields
    public void testDateFields() {
        assertEquals("era", EthiopicChronology.getInstance().era().getName());
        assertEquals("centuryOfEra", EthiopicChronology.getInstance().centuryOfEra().getName());
        assertEquals("yearOfCentury", EthiopicChronology.getInstance().yearOfCentury().getName());
        assertEquals("yearOfEra", EthiopicChronology.getInstance().yearOfEra().getName());
        assertEquals("year", EthiopicChronology.getInstance().year().getName());
        assertEquals("monthOfYear", EthiopicChronology.getInstance().monthOfYear().getName());
        assertEquals("weekyearOfCentury", EthiopicChronology.getInstance().weekyearOfCentury().getName());
        assertEquals("weekyear", EthiopicChronology.getInstance().weekyear().getName());
        assertEquals("weekOfWeekyear", EthiopicChronology.getInstance().weekOfWeekyear().getName());
        assertEquals("dayOfYear", EthiopicChronology.getInstance().dayOfYear().getName());
        assertEquals("dayOfMonth", EthiopicChronology.getInstance().dayOfMonth().getName());
        assertEquals("dayOfWeek", EthiopicChronology.getInstance().dayOfWeek().getName());
        
        assertEquals(true, EthiopicChronology.getInstance().era().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().centuryOfEra().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().yearOfCentury().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().yearOfEra().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().year().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().monthOfYear().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().weekyearOfCentury().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().weekyear().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().weekOfWeekyear().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().dayOfYear().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().dayOfMonth().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().dayOfWeek().isSupported());
    }

// org.joda.time.chrono.TestEthiopicChronology::testTimeFields
    public void testTimeFields() {
        assertEquals("halfdayOfDay", EthiopicChronology.getInstance().halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", EthiopicChronology.getInstance().clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", EthiopicChronology.getInstance().hourOfHalfday().getName());
        assertEquals("clockhourOfDay", EthiopicChronology.getInstance().clockhourOfDay().getName());
        assertEquals("hourOfDay", EthiopicChronology.getInstance().hourOfDay().getName());
        assertEquals("minuteOfDay", EthiopicChronology.getInstance().minuteOfDay().getName());
        assertEquals("minuteOfHour", EthiopicChronology.getInstance().minuteOfHour().getName());
        assertEquals("secondOfDay", EthiopicChronology.getInstance().secondOfDay().getName());
        assertEquals("secondOfMinute", EthiopicChronology.getInstance().secondOfMinute().getName());
        assertEquals("millisOfDay", EthiopicChronology.getInstance().millisOfDay().getName());
        assertEquals("millisOfSecond", EthiopicChronology.getInstance().millisOfSecond().getName());
        
        assertEquals(true, EthiopicChronology.getInstance().halfdayOfDay().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().clockhourOfHalfday().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().hourOfHalfday().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().clockhourOfDay().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().hourOfDay().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().minuteOfDay().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().minuteOfHour().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().secondOfDay().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().secondOfMinute().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().millisOfDay().isSupported());
        assertEquals(true, EthiopicChronology.getInstance().millisOfSecond().isSupported());
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

// org.joda.time.chrono.TestGregorianChronology::testFactoryUTC
    public void testFactoryUTC() {
        assertEquals(DateTimeZone.UTC, GregorianChronology.getInstanceUTC().getZone());
        assertSame(GregorianChronology.class, GregorianChronology.getInstanceUTC().getClass());
    }

// org.joda.time.chrono.TestGregorianChronology::testFactory
    public void testFactory() {
        assertEquals(LONDON, GregorianChronology.getInstance().getZone());
        assertSame(GregorianChronology.class, GregorianChronology.getInstance().getClass());
    }

// org.joda.time.chrono.TestGregorianChronology::testFactory_Zone
    public void testFactory_Zone() {
        assertEquals(TOKYO, GregorianChronology.getInstance(TOKYO).getZone());
        assertEquals(PARIS, GregorianChronology.getInstance(PARIS).getZone());
        assertEquals(LONDON, GregorianChronology.getInstance(null).getZone());
        assertSame(GregorianChronology.class, GregorianChronology.getInstance(TOKYO).getClass());
    }

// org.joda.time.chrono.TestGregorianChronology::testFactory_Zone_int
    public void testFactory_Zone_int() {
        GregorianChronology chrono = GregorianChronology.getInstance(TOKYO, 2);
        assertEquals(TOKYO, chrono.getZone());
        assertEquals(2, chrono.getMinimumDaysInFirstWeek());
        
        try {
            GregorianChronology.getInstance(TOKYO, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            GregorianChronology.getInstance(TOKYO, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.chrono.TestGregorianChronology::testEquality
    public void testEquality() {
        assertSame(GregorianChronology.getInstance(TOKYO), GregorianChronology.getInstance(TOKYO));
        assertSame(GregorianChronology.getInstance(LONDON), GregorianChronology.getInstance(LONDON));
        assertSame(GregorianChronology.getInstance(PARIS), GregorianChronology.getInstance(PARIS));
        assertSame(GregorianChronology.getInstanceUTC(), GregorianChronology.getInstanceUTC());
        assertSame(GregorianChronology.getInstance(), GregorianChronology.getInstance(LONDON));
    }

// org.joda.time.chrono.TestGregorianChronology::testWithUTC
    public void testWithUTC() {
        assertSame(GregorianChronology.getInstanceUTC(), GregorianChronology.getInstance(LONDON).withUTC());
        assertSame(GregorianChronology.getInstanceUTC(), GregorianChronology.getInstance(TOKYO).withUTC());
        assertSame(GregorianChronology.getInstanceUTC(), GregorianChronology.getInstanceUTC().withUTC());
        assertSame(GregorianChronology.getInstanceUTC(), GregorianChronology.getInstance().withUTC());
    }

// org.joda.time.chrono.TestGregorianChronology::testWithZone
    public void testWithZone() {
        assertSame(GregorianChronology.getInstance(TOKYO), GregorianChronology.getInstance(TOKYO).withZone(TOKYO));
        assertSame(GregorianChronology.getInstance(LONDON), GregorianChronology.getInstance(TOKYO).withZone(LONDON));
        assertSame(GregorianChronology.getInstance(PARIS), GregorianChronology.getInstance(TOKYO).withZone(PARIS));
        assertSame(GregorianChronology.getInstance(LONDON), GregorianChronology.getInstance(TOKYO).withZone(null));
        assertSame(GregorianChronology.getInstance(PARIS), GregorianChronology.getInstance().withZone(PARIS));
        assertSame(GregorianChronology.getInstance(PARIS), GregorianChronology.getInstanceUTC().withZone(PARIS));
    }

// org.joda.time.chrono.TestGregorianChronology::testToString
    public void testToString() {
        assertEquals("GregorianChronology[Europe/London]", GregorianChronology.getInstance(LONDON).toString());
        assertEquals("GregorianChronology[Asia/Tokyo]", GregorianChronology.getInstance(TOKYO).toString());
        assertEquals("GregorianChronology[Europe/London]", GregorianChronology.getInstance().toString());
        assertEquals("GregorianChronology[UTC]", GregorianChronology.getInstanceUTC().toString());
        assertEquals("GregorianChronology[UTC,mdfw=2]", GregorianChronology.getInstance(DateTimeZone.UTC, 2).toString());
    }

// org.joda.time.chrono.TestGregorianChronology::testDurationFields
    public void testDurationFields() {
        assertEquals("eras", GregorianChronology.getInstance().eras().getName());
        assertEquals("centuries", GregorianChronology.getInstance().centuries().getName());
        assertEquals("years", GregorianChronology.getInstance().years().getName());
        assertEquals("weekyears", GregorianChronology.getInstance().weekyears().getName());
        assertEquals("months", GregorianChronology.getInstance().months().getName());
        assertEquals("weeks", GregorianChronology.getInstance().weeks().getName());
        assertEquals("days", GregorianChronology.getInstance().days().getName());
        assertEquals("halfdays", GregorianChronology.getInstance().halfdays().getName());
        assertEquals("hours", GregorianChronology.getInstance().hours().getName());
        assertEquals("minutes", GregorianChronology.getInstance().minutes().getName());
        assertEquals("seconds", GregorianChronology.getInstance().seconds().getName());
        assertEquals("millis", GregorianChronology.getInstance().millis().getName());
        
        assertEquals(false, GregorianChronology.getInstance().eras().isSupported());
        assertEquals(true, GregorianChronology.getInstance().centuries().isSupported());
        assertEquals(true, GregorianChronology.getInstance().years().isSupported());
        assertEquals(true, GregorianChronology.getInstance().weekyears().isSupported());
        assertEquals(true, GregorianChronology.getInstance().months().isSupported());
        assertEquals(true, GregorianChronology.getInstance().weeks().isSupported());
        assertEquals(true, GregorianChronology.getInstance().days().isSupported());
        assertEquals(true, GregorianChronology.getInstance().halfdays().isSupported());
        assertEquals(true, GregorianChronology.getInstance().hours().isSupported());
        assertEquals(true, GregorianChronology.getInstance().minutes().isSupported());
        assertEquals(true, GregorianChronology.getInstance().seconds().isSupported());
        assertEquals(true, GregorianChronology.getInstance().millis().isSupported());
        
        assertEquals(false, GregorianChronology.getInstance().centuries().isPrecise());
        assertEquals(false, GregorianChronology.getInstance().years().isPrecise());
        assertEquals(false, GregorianChronology.getInstance().weekyears().isPrecise());
        assertEquals(false, GregorianChronology.getInstance().months().isPrecise());
        assertEquals(false, GregorianChronology.getInstance().weeks().isPrecise());
        assertEquals(false, GregorianChronology.getInstance().days().isPrecise());
        assertEquals(false, GregorianChronology.getInstance().halfdays().isPrecise());
        assertEquals(true, GregorianChronology.getInstance().hours().isPrecise());
        assertEquals(true, GregorianChronology.getInstance().minutes().isPrecise());
        assertEquals(true, GregorianChronology.getInstance().seconds().isPrecise());
        assertEquals(true, GregorianChronology.getInstance().millis().isPrecise());
        
        assertEquals(false, GregorianChronology.getInstanceUTC().centuries().isPrecise());
        assertEquals(false, GregorianChronology.getInstanceUTC().years().isPrecise());
        assertEquals(false, GregorianChronology.getInstanceUTC().weekyears().isPrecise());
        assertEquals(false, GregorianChronology.getInstanceUTC().months().isPrecise());
        assertEquals(true, GregorianChronology.getInstanceUTC().weeks().isPrecise());
        assertEquals(true, GregorianChronology.getInstanceUTC().days().isPrecise());
        assertEquals(true, GregorianChronology.getInstanceUTC().halfdays().isPrecise());
        assertEquals(true, GregorianChronology.getInstanceUTC().hours().isPrecise());
        assertEquals(true, GregorianChronology.getInstanceUTC().minutes().isPrecise());
        assertEquals(true, GregorianChronology.getInstanceUTC().seconds().isPrecise());
        assertEquals(true, GregorianChronology.getInstanceUTC().millis().isPrecise());
        
        DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        assertEquals(false, GregorianChronology.getInstance(gmt).centuries().isPrecise());
        assertEquals(false, GregorianChronology.getInstance(gmt).years().isPrecise());
        assertEquals(false, GregorianChronology.getInstance(gmt).weekyears().isPrecise());
        assertEquals(false, GregorianChronology.getInstance(gmt).months().isPrecise());
        assertEquals(true, GregorianChronology.getInstance(gmt).weeks().isPrecise());
        assertEquals(true, GregorianChronology.getInstance(gmt).days().isPrecise());
        assertEquals(true, GregorianChronology.getInstance(gmt).halfdays().isPrecise());
        assertEquals(true, GregorianChronology.getInstance(gmt).hours().isPrecise());
        assertEquals(true, GregorianChronology.getInstance(gmt).minutes().isPrecise());
        assertEquals(true, GregorianChronology.getInstance(gmt).seconds().isPrecise());
        assertEquals(true, GregorianChronology.getInstance(gmt).millis().isPrecise());
    }

// org.joda.time.chrono.TestGregorianChronology::testDateFields
    public void testDateFields() {
        assertEquals("era", GregorianChronology.getInstance().era().getName());
        assertEquals("centuryOfEra", GregorianChronology.getInstance().centuryOfEra().getName());
        assertEquals("yearOfCentury", GregorianChronology.getInstance().yearOfCentury().getName());
        assertEquals("yearOfEra", GregorianChronology.getInstance().yearOfEra().getName());
        assertEquals("year", GregorianChronology.getInstance().year().getName());
        assertEquals("monthOfYear", GregorianChronology.getInstance().monthOfYear().getName());
        assertEquals("weekyearOfCentury", GregorianChronology.getInstance().weekyearOfCentury().getName());
        assertEquals("weekyear", GregorianChronology.getInstance().weekyear().getName());
        assertEquals("weekOfWeekyear", GregorianChronology.getInstance().weekOfWeekyear().getName());
        assertEquals("dayOfYear", GregorianChronology.getInstance().dayOfYear().getName());
        assertEquals("dayOfMonth", GregorianChronology.getInstance().dayOfMonth().getName());
        assertEquals("dayOfWeek", GregorianChronology.getInstance().dayOfWeek().getName());
        
        assertEquals(true, GregorianChronology.getInstance().era().isSupported());
        assertEquals(true, GregorianChronology.getInstance().centuryOfEra().isSupported());
        assertEquals(true, GregorianChronology.getInstance().yearOfCentury().isSupported());
        assertEquals(true, GregorianChronology.getInstance().yearOfEra().isSupported());
        assertEquals(true, GregorianChronology.getInstance().year().isSupported());
        assertEquals(true, GregorianChronology.getInstance().monthOfYear().isSupported());
        assertEquals(true, GregorianChronology.getInstance().weekyearOfCentury().isSupported());
        assertEquals(true, GregorianChronology.getInstance().weekyear().isSupported());
        assertEquals(true, GregorianChronology.getInstance().weekOfWeekyear().isSupported());
        assertEquals(true, GregorianChronology.getInstance().dayOfYear().isSupported());
        assertEquals(true, GregorianChronology.getInstance().dayOfMonth().isSupported());
        assertEquals(true, GregorianChronology.getInstance().dayOfWeek().isSupported());
    }

// org.joda.time.chrono.TestGregorianChronology::testTimeFields
    public void testTimeFields() {
        assertEquals("halfdayOfDay", GregorianChronology.getInstance().halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", GregorianChronology.getInstance().clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", GregorianChronology.getInstance().hourOfHalfday().getName());
        assertEquals("clockhourOfDay", GregorianChronology.getInstance().clockhourOfDay().getName());
        assertEquals("hourOfDay", GregorianChronology.getInstance().hourOfDay().getName());
        assertEquals("minuteOfDay", GregorianChronology.getInstance().minuteOfDay().getName());
        assertEquals("minuteOfHour", GregorianChronology.getInstance().minuteOfHour().getName());
        assertEquals("secondOfDay", GregorianChronology.getInstance().secondOfDay().getName());
        assertEquals("secondOfMinute", GregorianChronology.getInstance().secondOfMinute().getName());
        assertEquals("millisOfDay", GregorianChronology.getInstance().millisOfDay().getName());
        assertEquals("millisOfSecond", GregorianChronology.getInstance().millisOfSecond().getName());
        
        assertEquals(true, GregorianChronology.getInstance().halfdayOfDay().isSupported());
        assertEquals(true, GregorianChronology.getInstance().clockhourOfHalfday().isSupported());
        assertEquals(true, GregorianChronology.getInstance().hourOfHalfday().isSupported());
        assertEquals(true, GregorianChronology.getInstance().clockhourOfDay().isSupported());
        assertEquals(true, GregorianChronology.getInstance().hourOfDay().isSupported());
        assertEquals(true, GregorianChronology.getInstance().minuteOfDay().isSupported());
        assertEquals(true, GregorianChronology.getInstance().minuteOfHour().isSupported());
        assertEquals(true, GregorianChronology.getInstance().secondOfDay().isSupported());
        assertEquals(true, GregorianChronology.getInstance().secondOfMinute().isSupported());
        assertEquals(true, GregorianChronology.getInstance().millisOfDay().isSupported());
        assertEquals(true, GregorianChronology.getInstance().millisOfSecond().isSupported());
    }

// org.joda.time.chrono.TestGregorianChronology::testMaximumValue
    public void testMaximumValue() {
        YearMonthDay ymd1 = new YearMonthDay(1999, DateTimeConstants.FEBRUARY, 1);
        DateMidnight dm1 = new DateMidnight(1999, DateTimeConstants.FEBRUARY, 1);
        Chronology chrono = GregorianChronology.getInstance();
        assertEquals(28, chrono.dayOfMonth().getMaximumValue(ymd1));
        assertEquals(28, chrono.dayOfMonth().getMaximumValue(dm1.getMillis()));
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

// org.joda.time.chrono.TestIslamicChronology::testFactoryUTC
    public void testFactoryUTC() {
        assertEquals(DateTimeZone.UTC, IslamicChronology.getInstanceUTC().getZone());
        assertSame(IslamicChronology.class, IslamicChronology.getInstanceUTC().getClass());
    }

// org.joda.time.chrono.TestIslamicChronology::testFactory
    public void testFactory() {
        assertEquals(LONDON, IslamicChronology.getInstance().getZone());
        assertSame(IslamicChronology.class, IslamicChronology.getInstance().getClass());
    }

// org.joda.time.chrono.TestIslamicChronology::testFactory_Zone
    public void testFactory_Zone() {
        assertEquals(TOKYO, IslamicChronology.getInstance(TOKYO).getZone());
        assertEquals(PARIS, IslamicChronology.getInstance(PARIS).getZone());
        assertEquals(LONDON, IslamicChronology.getInstance(null).getZone());
        assertSame(IslamicChronology.class, IslamicChronology.getInstance(TOKYO).getClass());
    }

// org.joda.time.chrono.TestIslamicChronology::testEquality
    public void testEquality() {
        assertSame(IslamicChronology.getInstance(TOKYO), IslamicChronology.getInstance(TOKYO));
        assertSame(IslamicChronology.getInstance(LONDON), IslamicChronology.getInstance(LONDON));
        assertSame(IslamicChronology.getInstance(PARIS), IslamicChronology.getInstance(PARIS));
        assertSame(IslamicChronology.getInstanceUTC(), IslamicChronology.getInstanceUTC());
        assertSame(IslamicChronology.getInstance(), IslamicChronology.getInstance(LONDON));
    }

// org.joda.time.chrono.TestIslamicChronology::testWithUTC
    public void testWithUTC() {
        assertSame(IslamicChronology.getInstanceUTC(), IslamicChronology.getInstance(LONDON).withUTC());
        assertSame(IslamicChronology.getInstanceUTC(), IslamicChronology.getInstance(TOKYO).withUTC());
        assertSame(IslamicChronology.getInstanceUTC(), IslamicChronology.getInstanceUTC().withUTC());
        assertSame(IslamicChronology.getInstanceUTC(), IslamicChronology.getInstance().withUTC());
    }

// org.joda.time.chrono.TestIslamicChronology::testWithZone
    public void testWithZone() {
        assertSame(IslamicChronology.getInstance(TOKYO), IslamicChronology.getInstance(TOKYO).withZone(TOKYO));
        assertSame(IslamicChronology.getInstance(LONDON), IslamicChronology.getInstance(TOKYO).withZone(LONDON));
        assertSame(IslamicChronology.getInstance(PARIS), IslamicChronology.getInstance(TOKYO).withZone(PARIS));
        assertSame(IslamicChronology.getInstance(LONDON), IslamicChronology.getInstance(TOKYO).withZone(null));
        assertSame(IslamicChronology.getInstance(PARIS), IslamicChronology.getInstance().withZone(PARIS));
        assertSame(IslamicChronology.getInstance(PARIS), IslamicChronology.getInstanceUTC().withZone(PARIS));
    }

// org.joda.time.chrono.TestIslamicChronology::testToString
    public void testToString() {
        assertEquals("IslamicChronology[Europe/London]", IslamicChronology.getInstance(LONDON).toString());
        assertEquals("IslamicChronology[Asia/Tokyo]", IslamicChronology.getInstance(TOKYO).toString());
        assertEquals("IslamicChronology[Europe/London]", IslamicChronology.getInstance().toString());
        assertEquals("IslamicChronology[UTC]", IslamicChronology.getInstanceUTC().toString());
    }

// org.joda.time.chrono.TestIslamicChronology::testDurationFields
    public void testDurationFields() {
        assertEquals("eras", IslamicChronology.getInstance().eras().getName());
        assertEquals("centuries", IslamicChronology.getInstance().centuries().getName());
        assertEquals("years", IslamicChronology.getInstance().years().getName());
        assertEquals("weekyears", IslamicChronology.getInstance().weekyears().getName());
        assertEquals("months", IslamicChronology.getInstance().months().getName());
        assertEquals("weeks", IslamicChronology.getInstance().weeks().getName());
        assertEquals("days", IslamicChronology.getInstance().days().getName());
        assertEquals("halfdays", IslamicChronology.getInstance().halfdays().getName());
        assertEquals("hours", IslamicChronology.getInstance().hours().getName());
        assertEquals("minutes", IslamicChronology.getInstance().minutes().getName());
        assertEquals("seconds", IslamicChronology.getInstance().seconds().getName());
        assertEquals("millis", IslamicChronology.getInstance().millis().getName());
        
        assertEquals(false, IslamicChronology.getInstance().eras().isSupported());
        assertEquals(true, IslamicChronology.getInstance().centuries().isSupported());
        assertEquals(true, IslamicChronology.getInstance().years().isSupported());
        assertEquals(true, IslamicChronology.getInstance().weekyears().isSupported());
        assertEquals(true, IslamicChronology.getInstance().months().isSupported());
        assertEquals(true, IslamicChronology.getInstance().weeks().isSupported());
        assertEquals(true, IslamicChronology.getInstance().days().isSupported());
        assertEquals(true, IslamicChronology.getInstance().halfdays().isSupported());
        assertEquals(true, IslamicChronology.getInstance().hours().isSupported());
        assertEquals(true, IslamicChronology.getInstance().minutes().isSupported());
        assertEquals(true, IslamicChronology.getInstance().seconds().isSupported());
        assertEquals(true, IslamicChronology.getInstance().millis().isSupported());
        
        assertEquals(false, IslamicChronology.getInstance().centuries().isPrecise());
        assertEquals(false, IslamicChronology.getInstance().years().isPrecise());
        assertEquals(false, IslamicChronology.getInstance().weekyears().isPrecise());
        assertEquals(false, IslamicChronology.getInstance().months().isPrecise());
        assertEquals(false, IslamicChronology.getInstance().weeks().isPrecise());
        assertEquals(false, IslamicChronology.getInstance().days().isPrecise());
        assertEquals(false, IslamicChronology.getInstance().halfdays().isPrecise());
        assertEquals(true, IslamicChronology.getInstance().hours().isPrecise());
        assertEquals(true, IslamicChronology.getInstance().minutes().isPrecise());
        assertEquals(true, IslamicChronology.getInstance().seconds().isPrecise());
        assertEquals(true, IslamicChronology.getInstance().millis().isPrecise());
        
        assertEquals(false, IslamicChronology.getInstanceUTC().centuries().isPrecise());
        assertEquals(false, IslamicChronology.getInstanceUTC().years().isPrecise());
        assertEquals(false, IslamicChronology.getInstanceUTC().weekyears().isPrecise());
        assertEquals(false, IslamicChronology.getInstanceUTC().months().isPrecise());
        assertEquals(true, IslamicChronology.getInstanceUTC().weeks().isPrecise());
        assertEquals(true, IslamicChronology.getInstanceUTC().days().isPrecise());
        assertEquals(true, IslamicChronology.getInstanceUTC().halfdays().isPrecise());
        assertEquals(true, IslamicChronology.getInstanceUTC().hours().isPrecise());
        assertEquals(true, IslamicChronology.getInstanceUTC().minutes().isPrecise());
        assertEquals(true, IslamicChronology.getInstanceUTC().seconds().isPrecise());
        assertEquals(true, IslamicChronology.getInstanceUTC().millis().isPrecise());
        
        DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        assertEquals(false, IslamicChronology.getInstance(gmt).centuries().isPrecise());
        assertEquals(false, IslamicChronology.getInstance(gmt).years().isPrecise());
        assertEquals(false, IslamicChronology.getInstance(gmt).weekyears().isPrecise());
        assertEquals(false, IslamicChronology.getInstance(gmt).months().isPrecise());
        assertEquals(true, IslamicChronology.getInstance(gmt).weeks().isPrecise());
        assertEquals(true, IslamicChronology.getInstance(gmt).days().isPrecise());
        assertEquals(true, IslamicChronology.getInstance(gmt).halfdays().isPrecise());
        assertEquals(true, IslamicChronology.getInstance(gmt).hours().isPrecise());
        assertEquals(true, IslamicChronology.getInstance(gmt).minutes().isPrecise());
        assertEquals(true, IslamicChronology.getInstance(gmt).seconds().isPrecise());
        assertEquals(true, IslamicChronology.getInstance(gmt).millis().isPrecise());
    }

// org.joda.time.chrono.TestIslamicChronology::testDateFields
    public void testDateFields() {
        assertEquals("era", IslamicChronology.getInstance().era().getName());
        assertEquals("centuryOfEra", IslamicChronology.getInstance().centuryOfEra().getName());
        assertEquals("yearOfCentury", IslamicChronology.getInstance().yearOfCentury().getName());
        assertEquals("yearOfEra", IslamicChronology.getInstance().yearOfEra().getName());
        assertEquals("year", IslamicChronology.getInstance().year().getName());
        assertEquals("monthOfYear", IslamicChronology.getInstance().monthOfYear().getName());
        assertEquals("weekyearOfCentury", IslamicChronology.getInstance().weekyearOfCentury().getName());
        assertEquals("weekyear", IslamicChronology.getInstance().weekyear().getName());
        assertEquals("weekOfWeekyear", IslamicChronology.getInstance().weekOfWeekyear().getName());
        assertEquals("dayOfYear", IslamicChronology.getInstance().dayOfYear().getName());
        assertEquals("dayOfMonth", IslamicChronology.getInstance().dayOfMonth().getName());
        assertEquals("dayOfWeek", IslamicChronology.getInstance().dayOfWeek().getName());
        
        assertEquals(true, IslamicChronology.getInstance().era().isSupported());
        assertEquals(true, IslamicChronology.getInstance().centuryOfEra().isSupported());
        assertEquals(true, IslamicChronology.getInstance().yearOfCentury().isSupported());
        assertEquals(true, IslamicChronology.getInstance().yearOfEra().isSupported());
        assertEquals(true, IslamicChronology.getInstance().year().isSupported());
        assertEquals(true, IslamicChronology.getInstance().monthOfYear().isSupported());
        assertEquals(true, IslamicChronology.getInstance().weekyearOfCentury().isSupported());
        assertEquals(true, IslamicChronology.getInstance().weekyear().isSupported());
        assertEquals(true, IslamicChronology.getInstance().weekOfWeekyear().isSupported());
        assertEquals(true, IslamicChronology.getInstance().dayOfYear().isSupported());
        assertEquals(true, IslamicChronology.getInstance().dayOfMonth().isSupported());
        assertEquals(true, IslamicChronology.getInstance().dayOfWeek().isSupported());
    }

// org.joda.time.chrono.TestIslamicChronology::testTimeFields
    public void testTimeFields() {
        assertEquals("halfdayOfDay", IslamicChronology.getInstance().halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", IslamicChronology.getInstance().clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", IslamicChronology.getInstance().hourOfHalfday().getName());
        assertEquals("clockhourOfDay", IslamicChronology.getInstance().clockhourOfDay().getName());
        assertEquals("hourOfDay", IslamicChronology.getInstance().hourOfDay().getName());
        assertEquals("minuteOfDay", IslamicChronology.getInstance().minuteOfDay().getName());
        assertEquals("minuteOfHour", IslamicChronology.getInstance().minuteOfHour().getName());
        assertEquals("secondOfDay", IslamicChronology.getInstance().secondOfDay().getName());
        assertEquals("secondOfMinute", IslamicChronology.getInstance().secondOfMinute().getName());
        assertEquals("millisOfDay", IslamicChronology.getInstance().millisOfDay().getName());
        assertEquals("millisOfSecond", IslamicChronology.getInstance().millisOfSecond().getName());
        
        assertEquals(true, IslamicChronology.getInstance().halfdayOfDay().isSupported());
        assertEquals(true, IslamicChronology.getInstance().clockhourOfHalfday().isSupported());
        assertEquals(true, IslamicChronology.getInstance().hourOfHalfday().isSupported());
        assertEquals(true, IslamicChronology.getInstance().clockhourOfDay().isSupported());
        assertEquals(true, IslamicChronology.getInstance().hourOfDay().isSupported());
        assertEquals(true, IslamicChronology.getInstance().minuteOfDay().isSupported());
        assertEquals(true, IslamicChronology.getInstance().minuteOfHour().isSupported());
        assertEquals(true, IslamicChronology.getInstance().secondOfDay().isSupported());
        assertEquals(true, IslamicChronology.getInstance().secondOfMinute().isSupported());
        assertEquals(true, IslamicChronology.getInstance().millisOfDay().isSupported());
        assertEquals(true, IslamicChronology.getInstance().millisOfSecond().isSupported());
    }

// org.joda.time.chrono.TestIslamicChronology::testEpoch
    public void testEpoch() {
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, ISLAMIC_UTC);
        DateTime expectedEpoch = new DateTime(622, 7, 16, 0, 0, 0, 0, JULIAN_UTC);
        assertEquals(expectedEpoch.getMillis(), epoch.getMillis());
    }

// org.joda.time.chrono.TestIslamicChronology::testEra
    public void testEra() {
        assertEquals(1, IslamicChronology.AH);
        try {
            new DateTime(-1, 13, 5, 0, 0, 0, 0, ISLAMIC_UTC);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.chrono.TestIslamicChronology::testFieldConstructor
    public void testFieldConstructor() {
        DateTime date = new DateTime(1364, 12, 6, 0, 0, 0, 0, ISLAMIC_UTC);
        DateTime expectedDate = new DateTime(1945, 11, 12, 0, 0, 0, 0, ISO_UTC);
        assertEquals(expectedDate.getMillis(), date.getMillis());
    }

// org.joda.time.chrono.TestIslamicChronology::testCalendar
    public void testCalendar() {
        if (TestAll.FAST) {
            return;
        }
        System.out.println("\nTestIslamicChronology.testCalendar");
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, ISLAMIC_UTC);
        long millis = epoch.getMillis();
        long end = new DateTime(3000, 1, 1, 0, 0, 0, 0, ISO_UTC).getMillis();
        DateTimeField dayOfWeek = ISLAMIC_UTC.dayOfWeek();
        DateTimeField dayOfYear = ISLAMIC_UTC.dayOfYear();
        DateTimeField dayOfMonth = ISLAMIC_UTC.dayOfMonth();
        DateTimeField monthOfYear = ISLAMIC_UTC.monthOfYear();
        DateTimeField year = ISLAMIC_UTC.year();
        DateTimeField yearOfEra = ISLAMIC_UTC.yearOfEra();
        DateTimeField era = ISLAMIC_UTC.era();
        int expectedDOW = new DateTime(622, 7, 16, 0, 0, 0, 0, JULIAN_UTC).getDayOfWeek();
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
            int dayOfYearLen = dayOfYear.getMaximumValue(millis);
            int monthLen = dayOfMonth.getMaximumValue(millis);
            if (monthValue < 1 || monthValue > 12) {
                fail("Bad month: " + millis);
            }
            
            
            assertEquals(1, era.get(millis));
            assertEquals("AH", era.getAsText(millis));
            assertEquals("AH", era.getAsShortText(millis));
            
            
            assertEquals(expectedDOY, doyValue);
            assertEquals(expectedMonth, monthValue);
            assertEquals(expectedDay, dayValue);
            assertEquals(expectedDOW, dowValue);
            assertEquals(expectedYear, yearValue);
            assertEquals(expectedYear, yearOfEraValue);
            
            
            boolean leap = ((11 * yearValue + 14) % 30) < 11;
            assertEquals(leap, year.isLeap(millis));
            
            
            switch (monthValue) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 9:
                case 11:
                    assertEquals(30, monthLen);
                    break;
                case 2:
                case 4:
                case 6:
                case 8:
                case 10:
                    assertEquals(29, monthLen);
                    break;
                case 12:
                    assertEquals((leap ? 30 : 29), monthLen);
                    break;
            }
            
            
            assertEquals((leap ? 355 : 354), dayOfYearLen);
            
            
            expectedDOW = (((expectedDOW + 1) - 1) % 7) + 1;
            expectedDay++;
            expectedDOY++;
            if (expectedDay > monthLen) {
                expectedDay = 1;
                expectedMonth++;
                if (expectedMonth == 13) {
                    expectedMonth = 1;
                    expectedDOY = 1;
                    expectedYear++;
                }
            }
            millis += SKIP;
        }
    }

// org.joda.time.chrono.TestIslamicChronology::testSampleDate1
    public void testSampleDate1() {
        DateTime dt = new DateTime(1945, 11, 12, 0, 0, 0, 0, ISO_UTC);
        dt = dt.withChronology(ISLAMIC_UTC);
        assertEquals(IslamicChronology.AH, dt.getEra());
        assertEquals(14, dt.getCenturyOfEra());  
        assertEquals(64, dt.getYearOfCentury());
        assertEquals(1364, dt.getYearOfEra());
        
        assertEquals(1364, dt.getYear());
        Property fld = dt.year();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(DurationFieldType.days(), fld.getLeapDurationField().getType());
        assertEquals(new DateTime(1365, 12, 6, 0, 0, 0, 0, ISLAMIC_UTC), fld.addToCopy(1));
        
        assertEquals(12, dt.getMonthOfYear());
        fld = dt.monthOfYear();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(DurationFieldType.days(), fld.getLeapDurationField().getType());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(12, fld.getMaximumValue());
        assertEquals(12, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1365, 1, 6, 0, 0, 0, 0, ISLAMIC_UTC), fld.addToCopy(1));
        assertEquals(new DateTime(1364, 1, 6, 0, 0, 0, 0, ISLAMIC_UTC), fld.addWrapFieldToCopy(1));
        
        assertEquals(6, dt.getDayOfMonth());
        fld = dt.dayOfMonth();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(29, fld.getMaximumValue());
        assertEquals(30, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1364, 12, 7, 0, 0, 0, 0, ISLAMIC_UTC), fld.addToCopy(1));
        
        assertEquals(DateTimeConstants.MONDAY, dt.getDayOfWeek());
        fld = dt.dayOfWeek();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(7, fld.getMaximumValue());
        assertEquals(7, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1364, 12, 7, 0, 0, 0, 0, ISLAMIC_UTC), fld.addToCopy(1));
        
        assertEquals(6 * 30 + 5 * 29 + 6, dt.getDayOfYear());
        fld = dt.dayOfYear();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(354, fld.getMaximumValue());
        assertEquals(355, fld.getMaximumValueOverall());
        assertEquals(new DateTime(1364, 12, 7, 0, 0, 0, 0, ISLAMIC_UTC), fld.addToCopy(1));
        
        assertEquals(0, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
    }

// org.joda.time.chrono.TestIslamicChronology::testSampleDate2
    public void testSampleDate2() {
        DateTime dt = new DateTime(2005, 11, 26, 0, 0, 0, 0, ISO_UTC);
        dt = dt.withChronology(ISLAMIC_UTC);
        assertEquals(IslamicChronology.AH, dt.getEra());
        assertEquals(15, dt.getCenturyOfEra());  
        assertEquals(26, dt.getYearOfCentury());
        assertEquals(1426, dt.getYearOfEra());
        
        assertEquals(1426, dt.getYear());
        Property fld = dt.year();
        assertEquals(true, fld.isLeap());
        assertEquals(1, fld.getLeapAmount());
        assertEquals(DurationFieldType.days(), fld.getLeapDurationField().getType());
        
        assertEquals(10, dt.getMonthOfYear());
        fld = dt.monthOfYear();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(DurationFieldType.days(), fld.getLeapDurationField().getType());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(12, fld.getMaximumValue());
        assertEquals(12, fld.getMaximumValueOverall());
        
        assertEquals(24, dt.getDayOfMonth());
        fld = dt.dayOfMonth();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(29, fld.getMaximumValue());
        assertEquals(30, fld.getMaximumValueOverall());
        
        assertEquals(DateTimeConstants.SATURDAY, dt.getDayOfWeek());
        fld = dt.dayOfWeek();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(7, fld.getMaximumValue());
        assertEquals(7, fld.getMaximumValueOverall());
        
        assertEquals(5 * 30 + 4 * 29 + 24, dt.getDayOfYear());
        fld = dt.dayOfYear();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(355, fld.getMaximumValue());
        assertEquals(355, fld.getMaximumValueOverall());
        
        assertEquals(0, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
    }

// org.joda.time.chrono.TestIslamicChronology::testSampleDate3
    public void testSampleDate3() {
        DateTime dt = new DateTime(1426, 12, 24, 0, 0, 0, 0, ISLAMIC_UTC);
        assertEquals(IslamicChronology.AH, dt.getEra());
        
        assertEquals(1426, dt.getYear());
        Property fld = dt.year();
        assertEquals(true, fld.isLeap());
        assertEquals(1, fld.getLeapAmount());
        assertEquals(DurationFieldType.days(), fld.getLeapDurationField().getType());
        
        assertEquals(12, dt.getMonthOfYear());
        fld = dt.monthOfYear();
        assertEquals(true, fld.isLeap());
        assertEquals(1, fld.getLeapAmount());
        assertEquals(DurationFieldType.days(), fld.getLeapDurationField().getType());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(12, fld.getMaximumValue());
        assertEquals(12, fld.getMaximumValueOverall());
        
        assertEquals(24, dt.getDayOfMonth());
        fld = dt.dayOfMonth();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(30, fld.getMaximumValue());
        assertEquals(30, fld.getMaximumValueOverall());
        
        assertEquals(DateTimeConstants.TUESDAY, dt.getDayOfWeek());
        fld = dt.dayOfWeek();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(7, fld.getMaximumValue());
        assertEquals(7, fld.getMaximumValueOverall());
        
        assertEquals(6 * 30 + 5 * 29 + 24, dt.getDayOfYear());
        fld = dt.dayOfYear();
        assertEquals(false, fld.isLeap());
        assertEquals(0, fld.getLeapAmount());
        assertEquals(null, fld.getLeapDurationField());
        assertEquals(1, fld.getMinimumValue());
        assertEquals(1, fld.getMinimumValueOverall());
        assertEquals(355, fld.getMaximumValue());
        assertEquals(355, fld.getMaximumValueOverall());
        
        assertEquals(0, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
    }

// org.joda.time.chrono.TestIslamicChronology::testSampleDateWithZone
    public void testSampleDateWithZone() {
        DateTime dt = new DateTime(2005, 11, 26, 12, 0, 0, 0, PARIS).withChronology(ISLAMIC_UTC);
        assertEquals(IslamicChronology.AH, dt.getEra());
        assertEquals(1426, dt.getYear());
        assertEquals(10, dt.getMonthOfYear());
        assertEquals(24, dt.getDayOfMonth());
        assertEquals(11, dt.getHourOfDay());  
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
    }

// org.joda.time.chrono.TestIslamicChronology::test15BasedLeapYear
    public void test15BasedLeapYear() {
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(1));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(2));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(3));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(4));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(5));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(6));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(7));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(8));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(9));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(10));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(11));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(12));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(13));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(14));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(15));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(16));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(17));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(18));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(19));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(20));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(21));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(22));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(23));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(24));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(25));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(26));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(27));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(28));
        assertEquals(true, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(29));
        assertEquals(false, IslamicChronology.LEAP_YEAR_15_BASED.isLeapYear(30));
    }

// org.joda.time.chrono.TestIslamicChronology::test16BasedLeapYear
    public void test16BasedLeapYear() {
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(1));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(2));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(3));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(4));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(5));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(6));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(7));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(8));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(9));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(10));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(11));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(12));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(13));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(14));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(15));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(16));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(17));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(18));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(19));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(20));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(21));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(22));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(23));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(24));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(25));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(26));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(27));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(28));
        assertEquals(true, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(29));
        assertEquals(false, IslamicChronology.LEAP_YEAR_16_BASED.isLeapYear(30));
    }

// org.joda.time.chrono.TestIslamicChronology::testIndianBasedLeapYear
    public void testIndianBasedLeapYear() {
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(1));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(2));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(3));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(4));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(5));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(6));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(7));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(8));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(9));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(10));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(11));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(12));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(13));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(14));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(15));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(16));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(17));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(18));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(19));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(20));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(21));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(22));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(23));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(24));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(25));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(26));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(27));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(28));
        assertEquals(true, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(29));
        assertEquals(false, IslamicChronology.LEAP_YEAR_INDIAN.isLeapYear(30));
    }

// org.joda.time.chrono.TestIslamicChronology::testHabashAlHasibBasedLeapYear
    public void testHabashAlHasibBasedLeapYear() {
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(1));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(2));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(3));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(4));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(5));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(6));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(7));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(8));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(9));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(10));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(11));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(12));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(13));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(14));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(15));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(16));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(17));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(18));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(19));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(20));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(21));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(22));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(23));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(24));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(25));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(26));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(27));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(28));
        assertEquals(false, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(29));
        assertEquals(true, IslamicChronology.LEAP_YEAR_HABASH_AL_HASIB.isLeapYear(30));
    }

// org.joda.time.chrono.TestJulianChronology::testFactoryUTC
    public void testFactoryUTC() {
        assertEquals(DateTimeZone.UTC, JulianChronology.getInstanceUTC().getZone());
        assertSame(JulianChronology.class, JulianChronology.getInstanceUTC().getClass());
    }

// org.joda.time.chrono.TestJulianChronology::testFactory
    public void testFactory() {
        assertEquals(LONDON, JulianChronology.getInstance().getZone());
        assertSame(JulianChronology.class, JulianChronology.getInstance().getClass());
    }

// org.joda.time.chrono.TestJulianChronology::testFactory_Zone
    public void testFactory_Zone() {
        assertEquals(TOKYO, JulianChronology.getInstance(TOKYO).getZone());
        assertEquals(PARIS, JulianChronology.getInstance(PARIS).getZone());
        assertEquals(LONDON, JulianChronology.getInstance(null).getZone());
        assertSame(JulianChronology.class, JulianChronology.getInstance(TOKYO).getClass());
    }

// org.joda.time.chrono.TestJulianChronology::testFactory_Zone_int
    public void testFactory_Zone_int() {
        JulianChronology chrono = JulianChronology.getInstance(TOKYO, 2);
        assertEquals(TOKYO, chrono.getZone());
        assertEquals(2, chrono.getMinimumDaysInFirstWeek());
        
        try {
            JulianChronology.getInstance(TOKYO, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            JulianChronology.getInstance(TOKYO, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.chrono.TestJulianChronology::testEquality
    public void testEquality() {
        assertSame(JulianChronology.getInstance(TOKYO), JulianChronology.getInstance(TOKYO));
        assertSame(JulianChronology.getInstance(LONDON), JulianChronology.getInstance(LONDON));
        assertSame(JulianChronology.getInstance(PARIS), JulianChronology.getInstance(PARIS));
        assertSame(JulianChronology.getInstanceUTC(), JulianChronology.getInstanceUTC());
        assertSame(JulianChronology.getInstance(), JulianChronology.getInstance(LONDON));
    }

// org.joda.time.chrono.TestJulianChronology::testWithUTC
    public void testWithUTC() {
        assertSame(JulianChronology.getInstanceUTC(), JulianChronology.getInstance(LONDON).withUTC());
        assertSame(JulianChronology.getInstanceUTC(), JulianChronology.getInstance(TOKYO).withUTC());
        assertSame(JulianChronology.getInstanceUTC(), JulianChronology.getInstanceUTC().withUTC());
        assertSame(JulianChronology.getInstanceUTC(), JulianChronology.getInstance().withUTC());
    }

// org.joda.time.chrono.TestJulianChronology::testWithZone
    public void testWithZone() {
        assertSame(JulianChronology.getInstance(TOKYO), JulianChronology.getInstance(TOKYO).withZone(TOKYO));
        assertSame(JulianChronology.getInstance(LONDON), JulianChronology.getInstance(TOKYO).withZone(LONDON));
        assertSame(JulianChronology.getInstance(PARIS), JulianChronology.getInstance(TOKYO).withZone(PARIS));
        assertSame(JulianChronology.getInstance(LONDON), JulianChronology.getInstance(TOKYO).withZone(null));
        assertSame(JulianChronology.getInstance(PARIS), JulianChronology.getInstance().withZone(PARIS));
        assertSame(JulianChronology.getInstance(PARIS), JulianChronology.getInstanceUTC().withZone(PARIS));
    }

// org.joda.time.chrono.TestJulianChronology::testToString
    public void testToString() {
        assertEquals("JulianChronology[Europe/London]", JulianChronology.getInstance(LONDON).toString());
        assertEquals("JulianChronology[Asia/Tokyo]", JulianChronology.getInstance(TOKYO).toString());
        assertEquals("JulianChronology[Europe/London]", JulianChronology.getInstance().toString());
        assertEquals("JulianChronology[UTC]", JulianChronology.getInstanceUTC().toString());
        assertEquals("JulianChronology[UTC,mdfw=2]", JulianChronology.getInstance(DateTimeZone.UTC, 2).toString());
    }

// org.joda.time.chrono.TestJulianChronology::testDurationFields
    public void testDurationFields() {
        assertEquals("eras", JulianChronology.getInstance().eras().getName());
        assertEquals("centuries", JulianChronology.getInstance().centuries().getName());
        assertEquals("years", JulianChronology.getInstance().years().getName());
        assertEquals("weekyears", JulianChronology.getInstance().weekyears().getName());
        assertEquals("months", JulianChronology.getInstance().months().getName());
        assertEquals("weeks", JulianChronology.getInstance().weeks().getName());
        assertEquals("days", JulianChronology.getInstance().days().getName());
        assertEquals("halfdays", JulianChronology.getInstance().halfdays().getName());
        assertEquals("hours", JulianChronology.getInstance().hours().getName());
        assertEquals("minutes", JulianChronology.getInstance().minutes().getName());
        assertEquals("seconds", JulianChronology.getInstance().seconds().getName());
        assertEquals("millis", JulianChronology.getInstance().millis().getName());
        
        assertEquals(false, JulianChronology.getInstance().eras().isSupported());
        assertEquals(true, JulianChronology.getInstance().centuries().isSupported());
        assertEquals(true, JulianChronology.getInstance().years().isSupported());
        assertEquals(true, JulianChronology.getInstance().weekyears().isSupported());
        assertEquals(true, JulianChronology.getInstance().months().isSupported());
        assertEquals(true, JulianChronology.getInstance().weeks().isSupported());
        assertEquals(true, JulianChronology.getInstance().days().isSupported());
        assertEquals(true, JulianChronology.getInstance().halfdays().isSupported());
        assertEquals(true, JulianChronology.getInstance().hours().isSupported());
        assertEquals(true, JulianChronology.getInstance().minutes().isSupported());
        assertEquals(true, JulianChronology.getInstance().seconds().isSupported());
        assertEquals(true, JulianChronology.getInstance().millis().isSupported());
        
        assertEquals(false, JulianChronology.getInstance().centuries().isPrecise());
        assertEquals(false, JulianChronology.getInstance().years().isPrecise());
        assertEquals(false, JulianChronology.getInstance().weekyears().isPrecise());
        assertEquals(false, JulianChronology.getInstance().months().isPrecise());
        assertEquals(false, JulianChronology.getInstance().weeks().isPrecise());
        assertEquals(false, JulianChronology.getInstance().days().isPrecise());
        assertEquals(false, JulianChronology.getInstance().halfdays().isPrecise());
        assertEquals(true, JulianChronology.getInstance().hours().isPrecise());
        assertEquals(true, JulianChronology.getInstance().minutes().isPrecise());
        assertEquals(true, JulianChronology.getInstance().seconds().isPrecise());
        assertEquals(true, JulianChronology.getInstance().millis().isPrecise());
        
        assertEquals(false, JulianChronology.getInstanceUTC().centuries().isPrecise());
        assertEquals(false, JulianChronology.getInstanceUTC().years().isPrecise());
        assertEquals(false, JulianChronology.getInstanceUTC().weekyears().isPrecise());
        assertEquals(false, JulianChronology.getInstanceUTC().months().isPrecise());
        assertEquals(true, JulianChronology.getInstanceUTC().weeks().isPrecise());
        assertEquals(true, JulianChronology.getInstanceUTC().days().isPrecise());
        assertEquals(true, JulianChronology.getInstanceUTC().halfdays().isPrecise());
        assertEquals(true, JulianChronology.getInstanceUTC().hours().isPrecise());
        assertEquals(true, JulianChronology.getInstanceUTC().minutes().isPrecise());
        assertEquals(true, JulianChronology.getInstanceUTC().seconds().isPrecise());
        assertEquals(true, JulianChronology.getInstanceUTC().millis().isPrecise());
        
        DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        assertEquals(false, JulianChronology.getInstance(gmt).centuries().isPrecise());
        assertEquals(false, JulianChronology.getInstance(gmt).years().isPrecise());
        assertEquals(false, JulianChronology.getInstance(gmt).weekyears().isPrecise());
        assertEquals(false, JulianChronology.getInstance(gmt).months().isPrecise());
        assertEquals(true, JulianChronology.getInstance(gmt).weeks().isPrecise());
        assertEquals(true, JulianChronology.getInstance(gmt).days().isPrecise());
        assertEquals(true, JulianChronology.getInstance(gmt).halfdays().isPrecise());
        assertEquals(true, JulianChronology.getInstance(gmt).hours().isPrecise());
        assertEquals(true, JulianChronology.getInstance(gmt).minutes().isPrecise());
        assertEquals(true, JulianChronology.getInstance(gmt).seconds().isPrecise());
        assertEquals(true, JulianChronology.getInstance(gmt).millis().isPrecise());
    }

// org.joda.time.chrono.TestJulianChronology::testDateFields
    public void testDateFields() {
        assertEquals("era", JulianChronology.getInstance().era().getName());
        assertEquals("centuryOfEra", JulianChronology.getInstance().centuryOfEra().getName());
        assertEquals("yearOfCentury", JulianChronology.getInstance().yearOfCentury().getName());
        assertEquals("yearOfEra", JulianChronology.getInstance().yearOfEra().getName());
        assertEquals("year", JulianChronology.getInstance().year().getName());
        assertEquals("monthOfYear", JulianChronology.getInstance().monthOfYear().getName());
        assertEquals("weekyearOfCentury", JulianChronology.getInstance().weekyearOfCentury().getName());
        assertEquals("weekyear", JulianChronology.getInstance().weekyear().getName());
        assertEquals("weekOfWeekyear", JulianChronology.getInstance().weekOfWeekyear().getName());
        assertEquals("dayOfYear", JulianChronology.getInstance().dayOfYear().getName());
        assertEquals("dayOfMonth", JulianChronology.getInstance().dayOfMonth().getName());
        assertEquals("dayOfWeek", JulianChronology.getInstance().dayOfWeek().getName());
        
        assertEquals(true, JulianChronology.getInstance().era().isSupported());
        assertEquals(true, JulianChronology.getInstance().centuryOfEra().isSupported());
        assertEquals(true, JulianChronology.getInstance().yearOfCentury().isSupported());
        assertEquals(true, JulianChronology.getInstance().yearOfEra().isSupported());
        assertEquals(true, JulianChronology.getInstance().year().isSupported());
        assertEquals(true, JulianChronology.getInstance().monthOfYear().isSupported());
        assertEquals(true, JulianChronology.getInstance().weekyearOfCentury().isSupported());
        assertEquals(true, JulianChronology.getInstance().weekyear().isSupported());
        assertEquals(true, JulianChronology.getInstance().weekOfWeekyear().isSupported());
        assertEquals(true, JulianChronology.getInstance().dayOfYear().isSupported());
        assertEquals(true, JulianChronology.getInstance().dayOfMonth().isSupported());
        assertEquals(true, JulianChronology.getInstance().dayOfWeek().isSupported());
    }

// org.joda.time.chrono.TestJulianChronology::testTimeFields
    public void testTimeFields() {
        assertEquals("halfdayOfDay", JulianChronology.getInstance().halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", JulianChronology.getInstance().clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", JulianChronology.getInstance().hourOfHalfday().getName());
        assertEquals("clockhourOfDay", JulianChronology.getInstance().clockhourOfDay().getName());
        assertEquals("hourOfDay", JulianChronology.getInstance().hourOfDay().getName());
        assertEquals("minuteOfDay", JulianChronology.getInstance().minuteOfDay().getName());
        assertEquals("minuteOfHour", JulianChronology.getInstance().minuteOfHour().getName());
        assertEquals("secondOfDay", JulianChronology.getInstance().secondOfDay().getName());
        assertEquals("secondOfMinute", JulianChronology.getInstance().secondOfMinute().getName());
        assertEquals("millisOfDay", JulianChronology.getInstance().millisOfDay().getName());
        assertEquals("millisOfSecond", JulianChronology.getInstance().millisOfSecond().getName());
        
        assertEquals(true, JulianChronology.getInstance().halfdayOfDay().isSupported());
        assertEquals(true, JulianChronology.getInstance().clockhourOfHalfday().isSupported());
        assertEquals(true, JulianChronology.getInstance().hourOfHalfday().isSupported());
        assertEquals(true, JulianChronology.getInstance().clockhourOfDay().isSupported());
        assertEquals(true, JulianChronology.getInstance().hourOfDay().isSupported());
        assertEquals(true, JulianChronology.getInstance().minuteOfDay().isSupported());
        assertEquals(true, JulianChronology.getInstance().minuteOfHour().isSupported());
        assertEquals(true, JulianChronology.getInstance().secondOfDay().isSupported());
        assertEquals(true, JulianChronology.getInstance().secondOfMinute().isSupported());
        assertEquals(true, JulianChronology.getInstance().millisOfDay().isSupported());
        assertEquals(true, JulianChronology.getInstance().millisOfSecond().isSupported());
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

// org.joda.time.convert.TestCalendarConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = CalendarConverter.class;
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

// org.joda.time.convert.TestCalendarConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(Calendar.class, CalendarConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestCalendarConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(123L));
        assertEquals(123L, CalendarConverter.INSTANCE.getInstantMillis(cal, JULIAN));
        assertEquals(123L, cal.getTime().getTime());
    }

// org.joda.time.convert.TestCalendarConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
        assertEquals(GJChronology.getInstance(MOSCOW), CalendarConverter.INSTANCE.getChronology(cal, MOSCOW));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        assertEquals(GJChronology.getInstance(), CalendarConverter.INSTANCE.getChronology(cal, (DateTimeZone) null));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(0L));
        assertEquals(GJChronology.getInstance(MOSCOW, 0L, 4), CalendarConverter.INSTANCE.getChronology(cal, MOSCOW));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(Long.MAX_VALUE));
        assertEquals(JulianChronology.getInstance(PARIS), CalendarConverter.INSTANCE.getChronology(cal, PARIS));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        assertEquals(GregorianChronology.getInstance(PARIS), CalendarConverter.INSTANCE.getChronology(cal, PARIS));
        
        Calendar uc = new MockUnknownCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        assertEquals(ISOChronology.getInstance(PARIS), CalendarConverter.INSTANCE.getChronology(uc, PARIS));
        
        try {
            Calendar bc = (Calendar) Class.forName("sun.util.BuddhistCalendar").newInstance();
            bc.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
            assertEquals(BuddhistChronology.getInstance(PARIS), CalendarConverter.INSTANCE.getChronology(bc, PARIS));
        } catch (ClassNotFoundException ex) {
            
        }
    }

// org.joda.time.convert.TestCalendarConverter::testGetChronology_Object_nullChronology
    public void testGetChronology_Object_nullChronology() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
        assertEquals(GJChronology.getInstance(PARIS), CalendarConverter.INSTANCE.getChronology(cal, (Chronology) null));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(0L));
        assertEquals(GJChronology.getInstance(MOSCOW, 0L, 4), CalendarConverter.INSTANCE.getChronology(cal, (Chronology) null));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(Long.MAX_VALUE));
        assertEquals(JulianChronology.getInstance(MOSCOW), CalendarConverter.INSTANCE.getChronology(cal, (Chronology) null));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        assertEquals(GregorianChronology.getInstance(MOSCOW), CalendarConverter.INSTANCE.getChronology(cal, (Chronology) null));
        
        cal = new GregorianCalendar(new MockUnknownTimeZone());
        assertEquals(GJChronology.getInstance(), CalendarConverter.INSTANCE.getChronology(cal, (Chronology) null));
        
        Calendar uc = new MockUnknownCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        assertEquals(ISOChronology.getInstance(MOSCOW), CalendarConverter.INSTANCE.getChronology(uc, (Chronology) null));
        
        try {
            Calendar bc = (Calendar) Class.forName("sun.util.BuddhistCalendar").newInstance();
            bc.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
            assertEquals(BuddhistChronology.getInstance(MOSCOW), CalendarConverter.INSTANCE.getChronology(bc, (Chronology) null));
        } catch (ClassNotFoundException ex) {
            
        }
    }

// org.joda.time.convert.TestCalendarConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
        assertEquals(JULIAN, CalendarConverter.INSTANCE.getChronology(cal, JULIAN));
    }

// org.joda.time.convert.TestCalendarConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(12345678L));
        TimeOfDay tod = new TimeOfDay();
        int[] expected = ISO.get(tod, 12345678L);
        int[] actual = CalendarConverter.INSTANCE.getPartialValues(tod, cal, ISO);
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestCalendarConverter::testToString
    public void testToString() {
        assertEquals("Converter[java.util.Calendar]", CalendarConverter.INSTANCE.toString());
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

// org.joda.time.convert.TestDateConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = DateConverter.class;
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

// org.joda.time.convert.TestDateConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(Date.class, DateConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestDateConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        Date date = new Date(123L);
        long millis = DateConverter.INSTANCE.getInstantMillis(date, JULIAN);
        assertEquals(123L, millis);
        assertEquals(123L, DateConverter.INSTANCE.getInstantMillis(date, (Chronology) null));
    }

// org.joda.time.convert.TestDateConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISO_PARIS, DateConverter.INSTANCE.getChronology(new Date(123L), PARIS));
        assertEquals(ISO, DateConverter.INSTANCE.getChronology(new Date(123L), (DateTimeZone) null));
    }

// org.joda.time.convert.TestDateConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JULIAN, DateConverter.INSTANCE.getChronology(new Date(123L), JULIAN));
        assertEquals(ISO, DateConverter.INSTANCE.getChronology(new Date(123L), (Chronology) null));
    }

// org.joda.time.convert.TestDateConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = COPTIC.get(tod, 12345678L);
        int[] actual = DateConverter.INSTANCE.getPartialValues(tod, new Date(12345678L), COPTIC);
        assertEquals(true, Arrays.equals(expected, actual));
    }
