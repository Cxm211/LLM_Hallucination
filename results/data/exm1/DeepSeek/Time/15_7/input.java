// buggy code
    public static long safeMultiply(long val1, int val2) {
        switch (val2) {
            case -1:
                return -val1;
            case 0:
                return 0L;
            case 1:
                return val1;
        }
        long total = val1 * val2;
        if (total / val2 != val1) {
          throw new ArithmeticException("Multiplication overflows a long: " + val1 + " * " + val2);
        }
        return total;
    }

// relevant test
// org.joda.time.TestPartial_Basics::testSerialization
    public void testSerialization() throws Exception {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Partial result = (Partial) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestPartial_Basics::testGetFormatter1
    public void testGetFormatter1() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals("2005", test.getFormatter().print(test));
        
        test = test.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals("2005-06", test.getFormatter().print(test));
        
        test = test.with(DateTimeFieldType.dayOfMonth(), 25);
        assertEquals("2005-06-25", test.getFormatter().print(test));
        
        test = test.without(DateTimeFieldType.monthOfYear());
        assertEquals("2005--25", test.getFormatter().print(test));
    }

// org.joda.time.TestPartial_Basics::testGetFormatter2
    public void testGetFormatter2() {
        Partial test = new Partial();
        assertEquals(null, test.getFormatter());
        
        test = test.with(DateTimeFieldType.era(), 1);
        assertEquals(null, test.getFormatter());
        
        test = test.with(DateTimeFieldType.halfdayOfDay(), 0);
        assertEquals(null, test.getFormatter());
    }

// org.joda.time.TestPartial_Basics::testGetFormatter3
    public void testGetFormatter3() {
        Partial test = new Partial(DateTimeFieldType.dayOfWeek(), 5);
        assertEquals("-W-5", test.getFormatter().print(test));
        
        
        test = test.with(DateTimeFieldType.dayOfMonth(), 13);
        assertEquals("---13", test.getFormatter().print(test));
    }

// org.joda.time.TestPartial_Basics::testToString1
    public void testToString1() {
        Partial test = createHourMinPartial();
        assertEquals("10:20", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToString2
    public void testToString2() {
        Partial test = new Partial();
        assertEquals("[]", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToString3
    public void testToString3() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals("2005", test.toString());
        
        test = test.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals("2005-06", test.toString());
        
        test = test.with(DateTimeFieldType.dayOfMonth(), 25);
        assertEquals("2005-06-25", test.toString());
        
        test = test.without(DateTimeFieldType.monthOfYear());
        assertEquals("2005--25", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToString4
    public void testToString4() {
        Partial test = new Partial(DateTimeFieldType.dayOfWeek(), 5);
        assertEquals("-W-5", test.toString());
        
        test = test.with(DateTimeFieldType.dayOfMonth(), 13);
        assertEquals("[dayOfMonth=13, dayOfWeek=5]", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToString5
    public void testToString5() {
        Partial test = new Partial(DateTimeFieldType.era(), 1);
        assertEquals("[era=1]", test.toString());
        
        test = test.with(DateTimeFieldType.halfdayOfDay(), 0);
        assertEquals("[era=1, halfdayOfDay=0]", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToString_String
    public void testToString_String() {
        Partial test = createHourMinPartial();
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString("yyyy HH"));
        assertEquals("10:20", test.toString((String) null));
    }

// org.joda.time.TestPartial_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        Partial test = createHourMinPartial();
        assertEquals("10 20", test.toString("H m", Locale.ENGLISH));
        assertEquals("10:20", test.toString(null, Locale.ENGLISH));
        assertEquals("10 20", test.toString("H m", null));
        assertEquals("10:20", test.toString(null, null));
    }

// org.joda.time.TestPartial_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        Partial test = createHourMinPartial();
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("10:20", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestPartial_Match::testIsMatch_Instant
    public void testIsMatch_Instant() {
        
        Partial test = createYMDwPartial(ISO_UTC, 2005, 7, 2);
        DateTime instant = new DateTime(2005, 7, 5, 0, 0, 0, 0);
        assertEquals(true, test.isMatch(instant));
        
        instant = new DateTime(2005, 7, 4, 0, 0, 0, 0);
        assertEquals(false, test.isMatch(instant));
        
        instant = new DateTime(2005, 7, 6, 0, 0, 0, 0);
        assertEquals(false, test.isMatch(instant));
        
        instant = new DateTime(2005, 7, 12, 0, 0, 0, 0);
        assertEquals(true, test.isMatch(instant));
        
        instant = new DateTime(2005, 7, 19, 0, 0, 0, 0);
        assertEquals(true, test.isMatch(instant));
        
        instant = new DateTime(2005, 7, 26, 0, 0, 0, 0);
        assertEquals(true, test.isMatch(instant));
        
        instant = new DateTime(2005, 8, 2, 0, 0, 0, 0);
        assertEquals(false, test.isMatch(instant));
        
        instant = new DateTime(2006, 7, 5, 0, 0, 0, 0);
        assertEquals(false, test.isMatch(instant));
        
        instant = new DateTime(2005, 6, 5, 0, 0, 0, 0);
        assertEquals(false, test.isMatch(instant));
    }

// org.joda.time.TestPartial_Match::testIsMatch_Partial
    public void testIsMatch_Partial() {
        
        Partial test = createYMDwPartial(ISO_UTC, 2005, 7, 2);
        LocalDate partial = new LocalDate(2005, 7, 5);
        assertEquals(true, test.isMatch(partial));
        
        partial = new LocalDate(2005, 7, 4);
        assertEquals(false, test.isMatch(partial));
        
        partial = new LocalDate(2005, 7, 6);
        assertEquals(false, test.isMatch(partial));
        
        partial = new LocalDate(2005, 7, 12);
        assertEquals(true, test.isMatch(partial));
        
        partial = new LocalDate(2005, 7, 19);
        assertEquals(true, test.isMatch(partial));
        
        partial = new LocalDate(2005, 7, 26);
        assertEquals(true, test.isMatch(partial));
        
        partial = new LocalDate(2005, 8, 2);
        assertEquals(false, test.isMatch(partial));
        
        partial = new LocalDate(2006, 7, 5);
        assertEquals(false, test.isMatch(partial));
        
        partial = new LocalDate(2005, 6, 5);
        assertEquals(false, test.isMatch(partial));
        
        try {
            test.isMatch((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Properties::testPropertyGetHour
    public void testPropertyGetHour() {
        Partial test = new Partial(TYPES, VALUES);
        assertSame(test.getChronology().hourOfDay(), test.property(DateTimeFieldType.hourOfDay()).getField());
        assertEquals("hourOfDay", test.property(DateTimeFieldType.hourOfDay()).getName());
        assertEquals("Property[hourOfDay]", test.property(DateTimeFieldType.hourOfDay()).toString());
        assertSame(test, test.property(DateTimeFieldType.hourOfDay()).getReadablePartial());
        assertSame(test, test.property(DateTimeFieldType.hourOfDay()).getPartial());
        assertEquals(10, test.property(DateTimeFieldType.hourOfDay()).get());
        assertEquals("10", test.property(DateTimeFieldType.hourOfDay()).getAsString());
        assertEquals("10", test.property(DateTimeFieldType.hourOfDay()).getAsText());
        assertEquals("10", test.property(DateTimeFieldType.hourOfDay()).getAsText(Locale.FRENCH));
        assertEquals("10", test.property(DateTimeFieldType.hourOfDay()).getAsShortText());
        assertEquals("10", test.property(DateTimeFieldType.hourOfDay()).getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().hours(), test.property(DateTimeFieldType.hourOfDay()).getDurationField());
        assertEquals(test.getChronology().days(), test.property(DateTimeFieldType.hourOfDay()).getRangeDurationField());
        assertEquals(2, test.property(DateTimeFieldType.hourOfDay()).getMaximumTextLength(null));
        assertEquals(2, test.property(DateTimeFieldType.hourOfDay()).getMaximumShortTextLength(null));
    }

// org.joda.time.TestPartial_Properties::testPropertyGetMaxMinValuesHour
    public void testPropertyGetMaxMinValuesHour() {
        Partial test = new Partial(TYPES, VALUES);
        assertEquals(0, test.property(DateTimeFieldType.hourOfDay()).getMinimumValue());
        assertEquals(0, test.property(DateTimeFieldType.hourOfDay()).getMinimumValueOverall());
        assertEquals(23, test.property(DateTimeFieldType.hourOfDay()).getMaximumValue());
        assertEquals(23, test.property(DateTimeFieldType.hourOfDay()).getMaximumValueOverall());
    }

// org.joda.time.TestPartial_Properties::testPropertyAddHour
    public void testPropertyAddHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).addToCopy(9);
        check(test, 10, 20, 30, 40);
        check(copy, 19, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addToCopy(0);
        check(copy, 10, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addToCopy(13);
        check(copy, 23, 20, 30, 40);
        
        try {
            test.property(DateTimeFieldType.hourOfDay()).addToCopy(14);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addToCopy(-10);
        check(copy, 0, 20, 30, 40);
        
        try {
            test.property(DateTimeFieldType.hourOfDay()).addToCopy(-11);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyAddWrapFieldHour
    public void testPropertyAddWrapFieldHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).addWrapFieldToCopy(9);
        check(test, 10, 20, 30, 40);
        check(copy, 19, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addWrapFieldToCopy(0);
        check(copy, 10, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addWrapFieldToCopy(18);
        check(copy, 4, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addWrapFieldToCopy(-15);
        check(copy, 19, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertySetHour
    public void testPropertySetHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).setCopy(12);
        check(test, 10, 20, 30, 40);
        check(copy, 12, 20, 30, 40);
        
        try {
            test.property(DateTimeFieldType.hourOfDay()).setCopy(24);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(DateTimeFieldType.hourOfDay()).setCopy(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Properties::testPropertySetTextHour
    public void testPropertySetTextHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).setCopy("12");
        check(test, 10, 20, 30, 40);
        check(copy, 12, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyWithMaximumValueHour
    public void testPropertyWithMaximumValueHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).withMaximumValue();
        check(test, 10, 20, 30, 40);
        check(copy, 23, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyWithMinimumValueHour
    public void testPropertyWithMinimumValueHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).withMinimumValue();
        check(test, 10, 20, 30, 40);
        check(copy, 0, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyCompareToHour
    public void testPropertyCompareToHour() {
        Partial test1 = new Partial(TYPES, VALUES1);
        Partial test2 = new Partial(TYPES, VALUES2);
        assertEquals(true, test1.property(DateTimeFieldType.hourOfDay()).compareTo(test2) < 0);
        assertEquals(true, test2.property(DateTimeFieldType.hourOfDay()).compareTo(test1) > 0);
        assertEquals(true, test1.property(DateTimeFieldType.hourOfDay()).compareTo(test1) == 0);
        try {
            test1.property(DateTimeFieldType.hourOfDay()).compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.property(DateTimeFieldType.hourOfDay()).compareTo(dt2) < 0);
        assertEquals(true, test2.property(DateTimeFieldType.hourOfDay()).compareTo(dt1) > 0);
        assertEquals(true, test1.property(DateTimeFieldType.hourOfDay()).compareTo(dt1) == 0);
        try {
            test1.property(DateTimeFieldType.hourOfDay()).compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Properties::testPropertyGetMinute
    public void testPropertyGetMinute() {
        Partial test = new Partial(TYPES, VALUES);
        assertSame(test.getChronology().minuteOfHour(), test.property(DateTimeFieldType.minuteOfHour()).getField());
        assertEquals("minuteOfHour", test.property(DateTimeFieldType.minuteOfHour()).getName());
        assertEquals("Property[minuteOfHour]", test.property(DateTimeFieldType.minuteOfHour()).toString());
        assertSame(test, test.property(DateTimeFieldType.minuteOfHour()).getReadablePartial());
        assertSame(test, test.property(DateTimeFieldType.minuteOfHour()).getPartial());
        assertEquals(20, test.property(DateTimeFieldType.minuteOfHour()).get());
        assertEquals("20", test.property(DateTimeFieldType.minuteOfHour()).getAsString());
        assertEquals("20", test.property(DateTimeFieldType.minuteOfHour()).getAsText());
        assertEquals("20", test.property(DateTimeFieldType.minuteOfHour()).getAsText(Locale.FRENCH));
        assertEquals("20", test.property(DateTimeFieldType.minuteOfHour()).getAsShortText());
        assertEquals("20", test.property(DateTimeFieldType.minuteOfHour()).getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().minutes(), test.property(DateTimeFieldType.minuteOfHour()).getDurationField());
        assertEquals(test.getChronology().hours(), test.property(DateTimeFieldType.minuteOfHour()).getRangeDurationField());
        assertEquals(2, test.property(DateTimeFieldType.minuteOfHour()).getMaximumTextLength(null));
        assertEquals(2, test.property(DateTimeFieldType.minuteOfHour()).getMaximumShortTextLength(null));
    }

// org.joda.time.TestPartial_Properties::testPropertyGetMaxMinValuesMinute
    public void testPropertyGetMaxMinValuesMinute() {
        Partial test = new Partial(TYPES, VALUES);
        assertEquals(0, test.property(DateTimeFieldType.minuteOfHour()).getMinimumValue());
        assertEquals(0, test.property(DateTimeFieldType.minuteOfHour()).getMinimumValueOverall());
        assertEquals(59, test.property(DateTimeFieldType.minuteOfHour()).getMaximumValue());
        assertEquals(59, test.property(DateTimeFieldType.minuteOfHour()).getMaximumValueOverall());
    }

// org.joda.time.TestPartial_Properties::testPropertyAddMinute
    public void testPropertyAddMinute() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(9);
        check(test, 10, 20, 30, 40);
        check(copy, 10, 29, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(39);
        check(copy, 10, 59, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(40);
        check(copy, 11, 0, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(1 * 60 + 45);
        check(copy, 12, 5, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(13 * 60 + 39);
        check(copy, 23, 59, 30, 40);
        
        try {
            test.property(DateTimeFieldType.minuteOfHour()).addToCopy(13 * 60 + 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-9);
        check(copy, 10, 11, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-19);
        check(copy, 10, 1, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-20);
        check(copy, 10, 0, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-21);
        check(copy, 9, 59, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-(10 * 60 + 20));
        check(copy, 0, 0, 30, 40);
        
        try {
            test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-(10 * 60 + 21));
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyAddWrapFieldMinute
    public void testPropertyAddWrapFieldMinute() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.minuteOfHour()).addWrapFieldToCopy(9);
        check(test, 10, 20, 30, 40);
        check(copy, 10, 29, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addWrapFieldToCopy(49);
        check(copy, 10, 9, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addWrapFieldToCopy(-47);
        check(copy, 10, 33, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertySetMinute
    public void testPropertySetMinute() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.minuteOfHour()).setCopy(12);
        check(test, 10, 20, 30, 40);
        check(copy, 10, 12, 30, 40);
        
        try {
            test.property(DateTimeFieldType.minuteOfHour()).setCopy(60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(DateTimeFieldType.minuteOfHour()).setCopy(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Properties::testPropertySetTextMinute
    public void testPropertySetTextMinute() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.minuteOfHour()).setCopy("12");
        check(test, 10, 20, 30, 40);
        check(copy, 10, 12, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyCompareToMinute
    public void testPropertyCompareToMinute() {
        Partial test1 = new Partial(TYPES, VALUES1);
        Partial test2 = new Partial(TYPES, VALUES2);
        assertEquals(true, test1.property(DateTimeFieldType.minuteOfHour()).compareTo(test2) < 0);
        assertEquals(true, test2.property(DateTimeFieldType.minuteOfHour()).compareTo(test1) > 0);
        assertEquals(true, test1.property(DateTimeFieldType.minuteOfHour()).compareTo(test1) == 0);
        try {
            test1.property(DateTimeFieldType.minuteOfHour()).compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.property(DateTimeFieldType.minuteOfHour()).compareTo(dt2) < 0);
        assertEquals(true, test2.property(DateTimeFieldType.minuteOfHour()).compareTo(dt1) > 0);
        assertEquals(true, test1.property(DateTimeFieldType.minuteOfHour()).compareTo(dt1) == 0);
        try {
            test1.property(DateTimeFieldType.minuteOfHour()).compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestPeriod_Basics::testGetPeriodType
    public void testGetPeriodType() {
        Period test = new Period(0L);
        assertEquals(PeriodType.standard(), test.getPeriodType());
    }

// org.joda.time.TestPeriod_Basics::testGetMethods
    public void testGetMethods() {
        Period test = new Period(0L);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Basics::testValueIndexMethods
    public void testValueIndexMethods() {
        Period test = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
        assertEquals(6, test.size());
        assertEquals(1, test.getValue(0));
        assertEquals(4, test.getValue(1));
        assertEquals(5, test.getValue(2));
        assertEquals(6, test.getValue(3));
        assertEquals(7, test.getValue(4));
        assertEquals(8, test.getValue(5));
        assertEquals(true, Arrays.equals(new int[] {1, 4, 5, 6, 7, 8}, test.getValues()));
    }

// org.joda.time.TestPeriod_Basics::testTypeIndexMethods
    public void testTypeIndexMethods() {
        Period test = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
        assertEquals(6, test.size());
        assertEquals(DurationFieldType.years(), test.getFieldType(0));
        assertEquals(DurationFieldType.days(), test.getFieldType(1));
        assertEquals(DurationFieldType.hours(), test.getFieldType(2));
        assertEquals(DurationFieldType.minutes(), test.getFieldType(3));
        assertEquals(DurationFieldType.seconds(), test.getFieldType(4));
        assertEquals(DurationFieldType.millis(), test.getFieldType(5));
        assertEquals(true, Arrays.equals(new DurationFieldType[] {
            DurationFieldType.years(), DurationFieldType.days(), DurationFieldType.hours(),
            DurationFieldType.minutes(), DurationFieldType.seconds(), DurationFieldType.millis()},
            test.getFieldTypes()));
    }

// org.joda.time.TestPeriod_Basics::testIsSupported
    public void testIsSupported() {
        Period test = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
        assertEquals(true, test.isSupported(DurationFieldType.years()));
        assertEquals(false, test.isSupported(DurationFieldType.months()));
        assertEquals(false, test.isSupported(DurationFieldType.weeks()));
        assertEquals(true, test.isSupported(DurationFieldType.days()));
        assertEquals(true, test.isSupported(DurationFieldType.hours()));
        assertEquals(true, test.isSupported(DurationFieldType.minutes()));
        assertEquals(true, test.isSupported(DurationFieldType.seconds()));
        assertEquals(true, test.isSupported(DurationFieldType.millis()));
    }

// org.joda.time.TestPeriod_Basics::testIndexOf
    public void testIndexOf() {
        Period test = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
        assertEquals(0, test.indexOf(DurationFieldType.years()));
        assertEquals(-1, test.indexOf(DurationFieldType.months()));
        assertEquals(-1, test.indexOf(DurationFieldType.weeks()));
        assertEquals(1, test.indexOf(DurationFieldType.days()));
        assertEquals(2, test.indexOf(DurationFieldType.hours()));
        assertEquals(3, test.indexOf(DurationFieldType.minutes()));
        assertEquals(4, test.indexOf(DurationFieldType.seconds()));
        assertEquals(5, test.indexOf(DurationFieldType.millis()));
    }

// org.joda.time.TestPeriod_Basics::testGet
    public void testGet() {
        Period test = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
        assertEquals(1, test.get(DurationFieldType.years()));
        assertEquals(0, test.get(DurationFieldType.months()));
        assertEquals(0, test.get(DurationFieldType.weeks()));
        assertEquals(4, test.get(DurationFieldType.days()));
        assertEquals(5, test.get(DurationFieldType.hours()));
        assertEquals(6, test.get(DurationFieldType.minutes()));
        assertEquals(7, test.get(DurationFieldType.seconds()));
        assertEquals(8, test.get(DurationFieldType.millis()));
    }

// org.joda.time.TestPeriod_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        Period test1 = new Period(123L);
        Period test2 = new Period(123L);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        Period test3 = new Period(321L);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockPeriod(123L)));
        assertEquals(false, test1.equals(new Period(123L, PeriodType.dayTime())));
    }

// org.joda.time.TestPeriod_Basics::testSerialization
    public void testSerialization() throws Exception {
        Period test = new Period(123L);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Period result = (Period) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestPeriod_Basics::testToString
    public void testToString() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", test.toString());
        
        test = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("PT0S", test.toString());
        
        test = new Period(12345L);
        assertEquals("PT12.345S", test.toString());
    }

// org.joda.time.TestPeriod_Basics::testToString_PeriodFormatter
    public void testToString_PeriodFormatter() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("1 year, 2 months, 3 weeks, 4 days, 5 hours, 6 minutes, 7 seconds and 8 milliseconds", test.toString(PeriodFormat.getDefault()));
        
        test = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0 milliseconds", test.toString(PeriodFormat.getDefault()));
    }

// org.joda.time.TestPeriod_Basics::testToString_nullPeriodFormatter
    public void testToString_nullPeriodFormatter() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", test.toString((PeriodFormatter) null));
    }

// org.joda.time.TestPeriod_Basics::testToPeriod
    public void testToPeriod() {
        Period test = new Period(123L);
        Period result = test.toPeriod();
        assertSame(test, result);
    }

// org.joda.time.TestPeriod_Basics::testToMutablePeriod
    public void testToMutablePeriod() {
        Period test = new Period(123L);
        MutablePeriod result = test.toMutablePeriod();
        assertEquals(test, result);
    }

// org.joda.time.TestPeriod_Basics::testToDurationFrom
    public void testToDurationFrom() {
        Period test = new Period(123L);
        assertEquals(new Duration(123L), test.toDurationFrom(new Instant(0L)));
    }

// org.joda.time.TestPeriod_Basics::testToDurationTo
    public void testToDurationTo() {
        Period test = new Period(123L);
        assertEquals(new Duration(123L), test.toDurationTo(new Instant(123L)));
    }

// org.joda.time.TestPeriod_Basics::testWithPeriodType1
    public void testWithPeriodType1() {
        Period test = new Period(123L);
        Period result = test.withPeriodType(PeriodType.standard());
        assertSame(test, result);
    }

// org.joda.time.TestPeriod_Basics::testWithPeriodType2
    public void testWithPeriodType2() {
        Period test = new Period(3123L);
        Period result = test.withPeriodType(PeriodType.dayTime());
        assertEquals(3, result.getSeconds());
        assertEquals(123, result.getMillis());
        assertEquals(PeriodType.dayTime(), result.getPeriodType());
    }

// org.joda.time.TestPeriod_Basics::testWithPeriodType3
    public void testWithPeriodType3() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        try {
            test.withPeriodType(PeriodType.dayTime());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testWithPeriodType4
    public void testWithPeriodType4() {
        Period test = new Period(3123L);
        Period result = test.withPeriodType(null);
        assertEquals(3, result.getSeconds());
        assertEquals(123, result.getMillis());
        assertEquals(PeriodType.standard(), result.getPeriodType());
    }

// org.joda.time.TestPeriod_Basics::testWithPeriodType5
    public void testWithPeriodType5() {
        Period test = new Period(1, 2, 0, 4, 5, 6, 7, 8, PeriodType.standard());
        Period result = test.withPeriodType(PeriodType.yearMonthDayTime());
        assertEquals(PeriodType.yearMonthDayTime(), result.getPeriodType());
        assertEquals(1, result.getYears());
        assertEquals(2, result.getMonths());
        assertEquals(0, result.getWeeks());
        assertEquals(4, result.getDays());
        assertEquals(5, result.getHours());
        assertEquals(6, result.getMinutes());
        assertEquals(7, result.getSeconds());
        assertEquals(8, result.getMillis());
    }

// org.joda.time.TestPeriod_Basics::testWithFields1
    public void testWithFields1() {
        Period test1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period test2 = new Period(0, 0, 0, 0, 0, 0, 0, 9, PeriodType.millis());
        Period result = test1.withFields(test2);
        
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), test1);
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 9, PeriodType.millis()), test2);
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 9), result);
    }

// org.joda.time.TestPeriod_Basics::testWithFields2
    public void testWithFields2() {
        Period test1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period test2 = null;
        Period result = test1.withFields(test2);
        
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), test1);
        assertSame(test1, result);
    }

// org.joda.time.TestPeriod_Basics::testWithFields3
    public void testWithFields3() {
        Period test1 = new Period(0, 0, 0, 0, 0, 0, 0, 9, PeriodType.millis());
        Period test2 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test1.withFields(test2);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 9, PeriodType.millis()), test1);
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), test2);
    }

// org.joda.time.TestPeriod_Basics::testWithField1
    public void testWithField1() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = test.withField(DurationFieldType.years(), 6);
        
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), test);
        assertEquals(new Period(6, 2, 3, 4, 5, 6, 7, 8), result);
    }

// org.joda.time.TestPeriod_Basics::testWithField2
    public void testWithField2() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testWithField3
    public void testWithField3() {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.time());
        try {
            test.withField(DurationFieldType.years(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testWithField4
    public void testWithField4() {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.time());
        Period result = test.withField(DurationFieldType.years(), 0);
        assertEquals(test, result);
    }

// org.joda.time.TestPeriod_Basics::testWithFieldAdded1
    public void testWithFieldAdded1() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), test);
        assertEquals(new Period(7, 2, 3, 4, 5, 6, 7, 8), result);
    }

// org.joda.time.TestPeriod_Basics::testWithFieldAdded2
    public void testWithFieldAdded2() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testWithFieldAdded3
    public void testWithFieldAdded3() {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.time());
        try {
            test.withFieldAdded(DurationFieldType.years(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testWithFieldAdded4
    public void testWithFieldAdded4() {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.time());
        Period result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertEquals(test, result);
    }

// org.joda.time.TestPeriod_Basics::testPeriodStatics
    public void testPeriodStatics() {
        Period test;
        test = Period.years(1);
        assertEquals(test, new Period(1, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.months(1);
        assertEquals(test, new Period(0, 1, 0, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.weeks(1);
        assertEquals(test, new Period(0, 0, 1, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.days(1);
        assertEquals(test, new Period(0, 0, 0, 1, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.hours(1);
        assertEquals(test, new Period(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard()));
        test = Period.minutes(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 1, 0, 0, PeriodType.standard()));
        test = Period.seconds(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 0, 1, 0, PeriodType.standard()));
        test = Period.millis(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 0, 0, 1, PeriodType.standard()));
    }

// org.joda.time.TestPeriod_Basics::testWith
    public void testWith() {
        Period test;
        test = Period.years(5).withYears(1);
        assertEquals(test, new Period(1, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.months(5).withMonths(1);
        assertEquals(test, new Period(0, 1, 0, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.weeks(5).withWeeks(1);
        assertEquals(test, new Period(0, 0, 1, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.days(5).withDays(1);
        assertEquals(test, new Period(0, 0, 0, 1, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.hours(5).withHours(1);
        assertEquals(test, new Period(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard()));
        test = Period.minutes(5).withMinutes(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 1, 0, 0, PeriodType.standard()));
        test = Period.seconds(5).withSeconds(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 0, 1, 0, PeriodType.standard()));
        test = Period.millis(5).withMillis(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 0, 0, 1, PeriodType.standard()));
        
        test = new Period(0L, PeriodType.millis());
        try {
            test.withYears(1);
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testPlus
    public void testPlus() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period baseDaysOnly = new Period(0, 0, 0, 10, 0, 0, 0, 0, PeriodType.days());
        
        Period test = base.plus((ReadablePeriod) null);
        assertSame(base, test);
        
        test = base.plus(Period.years(10));
        assertEquals(11, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = base.plus(Years.years(10));
        assertEquals(11, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = base.plus(Period.days(10));
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = baseDaysOnly.plus(Period.years(0));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(10, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
        
        test = baseDaysOnly.plus(baseDaysOnly);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(20, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
        
        try {
            baseDaysOnly.plus(Period.years(1));
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        try {
            Period.days(Integer.MAX_VALUE).plus(Period.days(1));
            fail();
        } catch (ArithmeticException ex) {}
        
        try {
            Period.days(Integer.MIN_VALUE).plus(Period.days(-1));
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testMinus
    public void testMinus() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period baseDaysOnly = new Period(0, 0, 0, 10, 0, 0, 0, 0, PeriodType.days());
        
        Period test = base.minus((ReadablePeriod) null);
        assertSame(base, test);
        
        test = base.minus(Period.years(10));
        assertEquals(-9, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = base.minus(Years.years(10));
        assertEquals(-9, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = base.minus(Period.days(10));
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(-6, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = baseDaysOnly.minus(Period.years(0));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(10, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
        
        test = baseDaysOnly.minus(baseDaysOnly);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
        
        try {
            baseDaysOnly.minus(Period.years(1));
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        try {
            Period.days(Integer.MAX_VALUE).minus(Period.days(-1));
            fail();
        } catch (ArithmeticException ex) {}
        
        try {
            Period.days(Integer.MIN_VALUE).minus(Period.days(1));
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testPlusFields
    public void testPlusFields() {
        Period test;
        test = Period.years(1).plusYears(1);
        assertEquals(new Period(2, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.months(1).plusMonths(1);
        assertEquals(new Period(0, 2, 0, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.weeks(1).plusWeeks(1);
        assertEquals(new Period(0, 0, 2, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.days(1).plusDays(1);
        assertEquals(new Period(0, 0, 0, 2, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.hours(1).plusHours(1);
        assertEquals(new Period(0, 0, 0, 0, 2, 0, 0, 0, PeriodType.standard()), test);
        test = Period.minutes(1).plusMinutes(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 2, 0, 0, PeriodType.standard()), test);
        test = Period.seconds(1).plusSeconds(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 2, 0, PeriodType.standard()), test);
        test = Period.millis(1).plusMillis(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 2, PeriodType.standard()), test);
        
        test = new Period(0L, PeriodType.millis());
        try {
            test.plusYears(1);
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testPlusFieldsZero
    public void testPlusFieldsZero() {
        Period test, result;
        test = Period.years(1);
        result = test.plusYears(0);
        assertSame(test, result);
        test = Period.months(1);
        result = test.plusMonths(0);
        assertSame(test, result);
        test = Period.weeks(1);
        result = test.plusWeeks(0);
        assertSame(test, result);
        test = Period.days(1);
        result = test.plusDays(0);
        assertSame(test, result);
        test = Period.hours(1);
        result = test.plusHours(0);
        assertSame(test, result);
        test = Period.minutes(1);
        result = test.plusMinutes(0);
        assertSame(test, result);
        test = Period.seconds(1);
        result = test.plusSeconds(0);
        assertSame(test, result);
        test = Period.millis(1);
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestPeriod_Basics::testMinusFields
    public void testMinusFields() {
        Period test;
        test = Period.years(3).minusYears(1);
        assertEquals(new Period(2, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.months(3).minusMonths(1);
        assertEquals(new Period(0, 2, 0, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.weeks(3).minusWeeks(1);
        assertEquals(new Period(0, 0, 2, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.days(3).minusDays(1);
        assertEquals(new Period(0, 0, 0, 2, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.hours(3).minusHours(1);
        assertEquals(new Period(0, 0, 0, 0, 2, 0, 0, 0, PeriodType.standard()), test);
        test = Period.minutes(3).minusMinutes(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 2, 0, 0, PeriodType.standard()), test);
        test = Period.seconds(3).minusSeconds(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 2, 0, PeriodType.standard()), test);
        test = Period.millis(3).minusMillis(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 2, PeriodType.standard()), test);
        
        test = new Period(0L, PeriodType.millis());
        try {
            test.minusYears(1);
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testMultipliedBy
    public void testMultipliedBy() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        
        Period test = base.multipliedBy(1);
        assertSame(base, test);
        
        test = base.multipliedBy(0);
        assertEquals(Period.ZERO, test);
        
        test = base.multipliedBy(2);
        assertEquals(2, test.getYears());
        assertEquals(4, test.getMonths());
        assertEquals(6, test.getWeeks());
        assertEquals(8, test.getDays());
        assertEquals(10, test.getHours());
        assertEquals(12, test.getMinutes());
        assertEquals(14, test.getSeconds());
        assertEquals(16, test.getMillis());
        
        test = base.multipliedBy(3);
        assertEquals(3, test.getYears());
        assertEquals(6, test.getMonths());
        assertEquals(9, test.getWeeks());
        assertEquals(12, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(18, test.getMinutes());
        assertEquals(21, test.getSeconds());
        assertEquals(24, test.getMillis());
        
        test = base.multipliedBy(-4);
        assertEquals(-4, test.getYears());
        assertEquals(-8, test.getMonths());
        assertEquals(-12, test.getWeeks());
        assertEquals(-16, test.getDays());
        assertEquals(-20, test.getHours());
        assertEquals(-24, test.getMinutes());
        assertEquals(-28, test.getSeconds());
        assertEquals(-32, test.getMillis());
        
        try {
            Period.days(Integer.MAX_VALUE).multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {}
        
        try {
            Period.days(Integer.MIN_VALUE).multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNegated
    public void testNegated() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        
        Period test = Period.ZERO.negated();
        assertEquals(Period.ZERO, test);
        
        test = base.negated();
        assertEquals(-1, test.getYears());
        assertEquals(-2, test.getMonths());
        assertEquals(-3, test.getWeeks());
        assertEquals(-4, test.getDays());
        assertEquals(-5, test.getHours());
        assertEquals(-6, test.getMinutes());
        assertEquals(-7, test.getSeconds());
        assertEquals(-8, test.getMillis());
        
        test = Period.days(Integer.MAX_VALUE).negated();
        assertEquals(-Integer.MAX_VALUE, test.getDays());
        
        try {
            Period.days(Integer.MIN_VALUE).negated();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardWeeks
    public void testToStandardWeeks() {
        Period test = new Period(0, 0, 3, 4, 5, 6, 7, 8);
        assertEquals(3, test.toStandardWeeks().getWeeks());
        
        test = new Period(0, 0, 3, 7, 0, 0, 0, 0);
        assertEquals(4, test.toStandardWeeks().getWeeks());
        
        test = new Period(0, 0, 0, 6, 23, 59, 59, 1000);
        assertEquals(1, test.toStandardWeeks().getWeeks());
        
        test = new Period(0, 0, Integer.MAX_VALUE, 0, 0, 0, 0, 0);
        assertEquals(Integer.MAX_VALUE, test.toStandardWeeks().getWeeks());
        
        test = new Period(0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        long intMax = Integer.MAX_VALUE;
        BigInteger expected = BigInteger.valueOf(intMax);
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_SECOND));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_MINUTE));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_HOUR));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_DAY));
        expected = expected.divide(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_WEEK));
        assertTrue(expected.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0);
        assertEquals(expected.longValue(), test.toStandardWeeks().getWeeks());
        
        test = new Period(0, 0, Integer.MAX_VALUE, 7, 0, 0, 0, 0);
        try {
            test.toStandardWeeks();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardWeeks_years
    public void testToStandardWeeks_years() {
        Period test = Period.years(1);
        try {
            test.toStandardWeeks();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardWeeks();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardWeeks().getWeeks());
    }

// org.joda.time.TestPeriod_Basics::testToStandardWeeks_months
    public void testToStandardWeeks_months() {
        Period test = Period.months(1);
        try {
            test.toStandardWeeks();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardWeeks();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardWeeks().getWeeks());
    }

// org.joda.time.TestPeriod_Basics::testToStandardDays
    public void testToStandardDays() {
        Period test = new Period(0, 0, 0, 4, 5, 6, 7, 8);
        assertEquals(4, test.toStandardDays().getDays());
        
        test = new Period(0, 0, 1, 4, 0, 0, 0, 0);
        assertEquals(11, test.toStandardDays().getDays());
        
        test = new Period(0, 0, 0, 0, 23, 59, 59, 1000);
        assertEquals(1, test.toStandardDays().getDays());
        
        test = new Period(0, 0, 0, Integer.MAX_VALUE, 0, 0, 0, 0);
        assertEquals(Integer.MAX_VALUE, test.toStandardDays().getDays());
        
        test = new Period(0, 0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        long intMax = Integer.MAX_VALUE;
        BigInteger expected = BigInteger.valueOf(intMax);
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_SECOND));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_MINUTE));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_HOUR));
        expected = expected.divide(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_DAY));
        assertTrue(expected.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0);
        assertEquals(expected.longValue(), test.toStandardDays().getDays());
        
        test = new Period(0, 0, 0, Integer.MAX_VALUE, 24, 0, 0, 0);
        try {
            test.toStandardDays();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardDays_years
    public void testToStandardDays_years() {
        Period test = Period.years(1);
        try {
            test.toStandardDays();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardDays();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardDays().getDays());
    }

// org.joda.time.TestPeriod_Basics::testToStandardDays_months
    public void testToStandardDays_months() {
        Period test = Period.months(1);
        try {
            test.toStandardDays();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardDays();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardDays().getDays());
    }

// org.joda.time.TestPeriod_Basics::testToStandardHours
    public void testToStandardHours() {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8);
        assertEquals(5, test.toStandardHours().getHours());
        
        test = new Period(0, 0, 0, 1, 5, 0, 0, 0);
        assertEquals(29, test.toStandardHours().getHours());
        
        test = new Period(0, 0, 0, 0, 0, 59, 59, 1000);
        assertEquals(1, test.toStandardHours().getHours());
        
        test = new Period(0, 0, 0, 0, Integer.MAX_VALUE, 0, 0, 0);
        assertEquals(Integer.MAX_VALUE, test.toStandardHours().getHours());
        
        test = new Period(0, 0, 0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        long intMax = Integer.MAX_VALUE;
        BigInteger expected = BigInteger.valueOf(intMax);
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_SECOND));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_MINUTE));
        expected = expected.divide(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_HOUR));
        assertTrue(expected.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0);
        assertEquals(expected.longValue(), test.toStandardHours().getHours());
        
        test = new Period(0, 0, 0, 0, Integer.MAX_VALUE, 60, 0, 0);
        try {
            test.toStandardHours();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardHours_years
    public void testToStandardHours_years() {
        Period test = Period.years(1);
        try {
            test.toStandardHours();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardHours();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardHours().getHours());
    }

// org.joda.time.TestPeriod_Basics::testToStandardHours_months
    public void testToStandardHours_months() {
        Period test = Period.months(1);
        try {
            test.toStandardHours();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardHours();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardHours().getHours());
    }

// org.joda.time.TestPeriod_Basics::testToStandardMinutes
    public void testToStandardMinutes() {
        Period test = new Period(0, 0, 0, 0, 0, 6, 7, 8);
        assertEquals(6, test.toStandardMinutes().getMinutes());
        
        test = new Period(0, 0, 0, 0, 1, 6, 0, 0);
        assertEquals(66, test.toStandardMinutes().getMinutes());
        
        test = new Period(0, 0, 0, 0, 0, 0, 59, 1000);
        assertEquals(1, test.toStandardMinutes().getMinutes());
        
        test = new Period(0, 0, 0, 0, 0, Integer.MAX_VALUE, 0, 0);
        assertEquals(Integer.MAX_VALUE, test.toStandardMinutes().getMinutes());
        
        test = new Period(0, 0, 0, 0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        long intMax = Integer.MAX_VALUE;
        BigInteger expected = BigInteger.valueOf(intMax);
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_SECOND));
        expected = expected.divide(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_MINUTE));
        assertTrue(expected.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0);
        assertEquals(expected.longValue(), test.toStandardMinutes().getMinutes());
        
        test = new Period(0, 0, 0, 0, 0, Integer.MAX_VALUE, 60, 0);
        try {
            test.toStandardMinutes();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardMinutes_years
    public void testToStandardMinutes_years() {
        Period test = Period.years(1);
        try {
            test.toStandardMinutes();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardMinutes();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardMinutes().getMinutes());
    }

// org.joda.time.TestPeriod_Basics::testToStandardMinutes_months
    public void testToStandardMinutes_months() {
        Period test = Period.months(1);
        try {
            test.toStandardMinutes();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardMinutes();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardMinutes().getMinutes());
    }

// org.joda.time.TestPeriod_Basics::testToStandardSeconds
    public void testToStandardSeconds() {
        Period test = new Period(0, 0, 0, 0, 0, 0, 7, 8);
        assertEquals(7, test.toStandardSeconds().getSeconds());
        
        test = new Period(0, 0, 0, 0, 0, 1, 3, 0);
        assertEquals(63, test.toStandardSeconds().getSeconds());
        
        test = new Period(0, 0, 0, 0, 0, 0, 0, 1000);
        assertEquals(1, test.toStandardSeconds().getSeconds());
        
        test = new Period(0, 0, 0, 0, 0, 0, Integer.MAX_VALUE, 0);
        assertEquals(Integer.MAX_VALUE, test.toStandardSeconds().getSeconds());
        
        test = new Period(0, 0, 0, 0, 0, 0, 20, Integer.MAX_VALUE);
        long expected = 20;
        expected += ((long) Integer.MAX_VALUE) / DateTimeConstants.MILLIS_PER_SECOND;
        assertEquals(expected, test.toStandardSeconds().getSeconds());
        
        test = new Period(0, 0, 0, 0, 0, 0, Integer.MAX_VALUE, 1000);
        try {
            test.toStandardSeconds();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardSeconds_years
    public void testToStandardSeconds_years() {
        Period test = Period.years(1);
        try {
            test.toStandardSeconds();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardSeconds();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardSeconds().getSeconds());
    }

// org.joda.time.TestPeriod_Basics::testToStandardSeconds_months
    public void testToStandardSeconds_months() {
        Period test = Period.months(1);
        try {
            test.toStandardSeconds();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardSeconds();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardSeconds().getSeconds());
    }

// org.joda.time.TestPeriod_Basics::testToStandardDuration
    public void testToStandardDuration() {
        Period test = new Period(0, 0, 0, 0, 0, 0, 0, 8);
        assertEquals(8, test.toStandardDuration().getMillis());
        
        test = new Period(0, 0, 0, 0, 0, 0, 1, 20);
        assertEquals(1020, test.toStandardDuration().getMillis());
        
        test = new Period(0, 0, 0, 0, 0, 0, 0, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, test.toStandardDuration().getMillis());
        
        test = new Period(0, 0, 0, 0, 0, 10, 20, Integer.MAX_VALUE);
        long expected = Integer.MAX_VALUE;
        expected += 10L * ((long) DateTimeConstants.MILLIS_PER_MINUTE);
        expected += 20L * ((long) DateTimeConstants.MILLIS_PER_SECOND);
        assertEquals(expected, test.toStandardDuration().getMillis());
        
        
        BigInteger intMax = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger exp = intMax;
        exp = exp.add(intMax.multiply(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_SECOND)));
        exp = exp.add(intMax.multiply(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_MINUTE)));
        exp = exp.add(intMax.multiply(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_HOUR)));
        exp = exp.add(intMax.multiply(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_DAY)));
        exp = exp.add(intMax.multiply(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_WEEK)));
        assertTrue(exp.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0);

    }

// org.joda.time.TestPeriod_Basics::testToStandardDuration_years
    public void testToStandardDuration_years() {
        Period test = Period.years(1);
        try {
            test.toStandardDuration();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardDuration();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardDuration().getMillis());
    }

// org.joda.time.TestPeriod_Basics::testToStandardDuration_months
    public void testToStandardDuration_months() {
        Period test = Period.months(1);
        try {
            test.toStandardDuration();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardDuration();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardDuration().getMillis());
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_yearMonth1
    public void testNormalizedStandard_yearMonth1() {
        Period test = new Period(1, 15, 0, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(1, 15, 0, 0, 0, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 0, 0, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_yearMonth2
    public void testNormalizedStandard_yearMonth2() {
        Period test = new Period(Integer.MAX_VALUE, 15, 0, 0, 0, 0, 0, 0);
        try {
            test.normalizedStandard();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_weekDay1
    public void testNormalizedStandard_weekDay1() {
        Period test = new Period(0, 0, 1, 12, 0, 0, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(0, 0, 1, 12, 0, 0, 0, 0), test);
        assertEquals(new Period(0, 0, 2, 5, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_weekDay2
    public void testNormalizedStandard_weekDay2() {
        Period test = new Period(0, 0, Integer.MAX_VALUE, 7, 0, 0, 0, 0);
        try {
            test.normalizedStandard();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_yearMonthWeekDay
    public void testNormalizedStandard_yearMonthWeekDay() {
        Period test = new Period(1, 15, 1, 12, 0, 0, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(1, 15, 1, 12, 0, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 2, 5, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_yearMonthDay
    public void testNormalizedStandard_yearMonthDay() {
        Period test = new Period(1, 15, 0, 36, 0, 0, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(1, 15, 0, 36, 0, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 5, 1, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_negative
    public void testNormalizedStandard_negative() {
        Period test = new Period(0, 0, 0, 0, 2, -10, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(0, 0, 0, 0, 2, -10, 0, 0), test);
        assertEquals(new Period(0, 0, 0, 0, 1, 50, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_fullNegative
    public void testNormalizedStandard_fullNegative() {
        Period test = new Period(0, 0, 0, 0, 1, -70, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(0, 0, 0, 0, 1, -70, 0, 0), test);
        assertEquals(new Period(0, 0, 0, 0, 0, -10, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_yearMonth1
    public void testNormalizedStandard_periodType_yearMonth1() {
        Period test = new Period(1, 15, 0, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard((PeriodType) null);
        assertEquals(new Period(1, 15, 0, 0, 0, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 0, 0, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_yearMonth2
    public void testNormalizedStandard_periodType_yearMonth2() {
        Period test = new Period(Integer.MAX_VALUE, 15, 0, 0, 0, 0, 0, 0);
        try {
            test.normalizedStandard((PeriodType) null);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_yearMonth3
    public void testNormalizedStandard_periodType_yearMonth3() {
        Period test = new Period(1, 15, 3, 4, 0, 0, 0, 0);
        try {
            test.normalizedStandard(PeriodType.dayTime());
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_weekDay1
    public void testNormalizedStandard_periodType_weekDay1() {
        Period test = new Period(0, 0, 1, 12, 0, 0, 0, 0);
        Period result = test.normalizedStandard((PeriodType) null);
        assertEquals(new Period(0, 0, 1, 12, 0, 0, 0, 0), test);
        assertEquals(new Period(0, 0, 2, 5, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_weekDay2
    public void testNormalizedStandard_periodType_weekDay2() {
        Period test = new Period(0, 0, Integer.MAX_VALUE, 7, 0, 0, 0, 0);
        try {
            test.normalizedStandard((PeriodType) null);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_weekDay3
    public void testNormalizedStandard_periodType_weekDay3() {
        Period test = new Period(0, 0, 1, 12, 0, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.dayTime());
        assertEquals(new Period(0, 0, 1, 12, 0, 0, 0, 0), test);
        assertEquals(new Period(0, 0, 0, 19, 0, 0, 0, 0, PeriodType.dayTime()), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_yearMonthWeekDay
    public void testNormalizedStandard_periodType_yearMonthWeekDay() {
        Period test = new Period(1, 15, 1, 12, 0, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.yearMonthDayTime());
        assertEquals(new Period(1, 15, 1, 12, 0, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 0, 19, 0, 0, 0, 0, PeriodType.yearMonthDayTime()), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_yearMonthDay
    public void testNormalizedStandard_periodType_yearMonthDay() {
        Period test = new Period(1, 15, 0, 36, 27, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.yearMonthDayTime());
        assertEquals(new Period(1, 15, 0, 36, 27, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 0, 37, 3, 0, 0, 0, PeriodType.yearMonthDayTime()), result);
    }

// org.joda.time.TestPeriod_Constructors::testConstants
    public void testConstants() throws Throwable {
        Period test = Period.ZERO;
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

// org.joda.time.TestPeriod_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 890), Period.parse("P1Y2M3W4DT5H6M7.890S"));
    }

// org.joda.time.TestPeriod_Constructors::testConstructor1
    public void testConstructor1() throws Throwable {
        Period test = new Period();
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        long length =
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long3
    public void testConstructor_long3() throws Throwable {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Period test = new Period(length);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_fixedZone
    public void testConstructor_long_fixedZone() throws Throwable {
        DateTimeZone zone = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.forOffsetHours(2));
            long length =
                (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
                5L * DateTimeConstants.MILLIS_PER_HOUR +
                6L * DateTimeConstants.MILLIS_PER_MINUTE +
                7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
            Period test = new Period(length);
            assertEquals(PeriodType.standard(), test.getPeriodType());
            
            assertEquals(0, test.getYears());  
            assertEquals(0, test.getMonths());
            assertEquals(0, test.getWeeks());
            assertEquals(0, test.getDays());
            assertEquals((450 * 24) + 5, test.getHours());
            assertEquals(6, test.getMinutes());
            assertEquals(7, test.getSeconds());
            assertEquals(8, test.getMillis());
        } finally {
            DateTimeZone.setDefault(zone);
        }
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType1
    public void testConstructor_long_PeriodType1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, (PeriodType) null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType2
    public void testConstructor_long_PeriodType2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.millis());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType3
    public void testConstructor_long_PeriodType3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.dayTime());
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType4
    public void testConstructor_long_PeriodType4() throws Throwable {
        long length =
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.standard().withMillisRemoved());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_Chronology1
    public void testConstructor_long_Chronology1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, ISOChronology.getInstance());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_Chronology2
    public void testConstructor_long_Chronology2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, ISOChronology.getInstanceUTC());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_Chronology3
    public void testConstructor_long_Chronology3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, (Chronology) null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType_Chronology1
    public void testConstructor_long_PeriodType_Chronology1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.time().withMillisRemoved(), ISOChronology.getInstance());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType_Chronology2
    public void testConstructor_long_PeriodType_Chronology2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.standard(), ISOChronology.getInstanceUTC());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType_Chronology3
    public void testConstructor_long_PeriodType_Chronology3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.standard(), (Chronology) null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType_Chronology4
    public void testConstructor_long_PeriodType_Chronology4() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, (PeriodType) null, (Chronology) null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_4int1
    public void testConstructor_4int1() throws Throwable {
        Period test = new Period(5, 6, 7, 8);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_8int1
    public void testConstructor_8int1() throws Throwable {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_8int__PeriodType1
    public void testConstructor_8int__PeriodType1() throws Throwable {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8, null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_8int__PeriodType2
    public void testConstructor_8int__PeriodType2() throws Throwable {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.dayTime());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_8int__PeriodType3
    public void testConstructor_8int__PeriodType3() throws Throwable {
        try {
            new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.dayTime());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long1
    public void testConstructor_long_long1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long2
    public void testConstructor_long_long2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_PeriodType1
    public void testConstructor_long_long_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), (PeriodType) null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_PeriodType2
    public void testConstructor_long_long_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), PeriodType.dayTime());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_PeriodType3
    public void testConstructor_long_long_PeriodType3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 6, 9, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), PeriodType.standard().withMillisRemoved());
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

// org.joda.time.TestPeriod_Constructors::testToPeriod_PeriodType3
    public void testToPeriod_PeriodType3() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10);
        DateTime dt2 = new DateTime(2005, 6, 9, 12, 14, 16, 18);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), PeriodType.yearWeekDayTime());
        
        assertEquals(PeriodType.yearWeekDayTime(), test.getPeriodType());
        assertEquals(1, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_Chronology1
    public void testConstructor_long_long_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, CopticChronology.getInstance());
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, CopticChronology.getInstance());
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), CopticChronology.getInstance());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_Chronology2
    public void testConstructor_long_long_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), (Chronology) null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_PeriodType_Chronology1
    public void testConstructor_long_long_PeriodType_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, CopticChronology.getInstance());
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, CopticChronology.getInstance());
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), (PeriodType) null, CopticChronology.getInstance());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_PeriodType_Chronology2
    public void testConstructor_long_long_PeriodType_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), (PeriodType) null, null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI1
    public void testConstructor_RI_RI1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI2
    public void testConstructor_RI_RI2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI3
    public void testConstructor_RI_RI3() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI4
    public void testConstructor_RI_RI4() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        DateTime dt2 = null;  
        Period test = new Period(dt1, dt2);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI5
    public void testConstructor_RI_RI5() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = null;  
        Period test = new Period(dt1, dt2);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI_PeriodType1
    public void testConstructor_RI_RI_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2, null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI_PeriodType2
    public void testConstructor_RI_RI_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2, PeriodType.dayTime());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI_PeriodType3
    public void testConstructor_RI_RI_PeriodType3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 6, 9, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2, PeriodType.standard().withMillisRemoved());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI_PeriodType4
    public void testConstructor_RI_RI_PeriodType4() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2, PeriodType.standard());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI_PeriodType5
    public void testConstructor_RI_RI_PeriodType5() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = null;  
        Period test = new Period(dt1, dt2, PeriodType.standard());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP1
    public void testConstructor_RP_RP1() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2004, 6, 9);
        YearMonthDay dt2 = new YearMonthDay(2005, 7, 10);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP2
    public void testConstructor_RP_RP2() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2004, 6, 9);
        YearMonthDay dt2 = new YearMonthDay(2005, 5, 17);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(11, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP2Local
    public void testConstructor_RP_RP2Local() throws Throwable {
        LocalDate dt1 = new LocalDate(2004, 6, 9);
        LocalDate dt2 = new LocalDate(2005, 5, 17);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(11, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP3
    public void testConstructor_RP_RP3() throws Throwable {
        YearMonthDay dt1 = null;
        YearMonthDay dt2 = new YearMonthDay(2005, 7, 17);
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP4
    public void testConstructor_RP_RP4() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2005, 7, 17);
        YearMonthDay dt2 = null;
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP5
    public void testConstructor_RP_RP5() throws Throwable {
        YearMonthDay dt1 = null;
        YearMonthDay dt2 = null;
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP6
    public void testConstructor_RP_RP6() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2005, 7, 17);
        TimeOfDay dt2 = new TimeOfDay(10, 20, 30, 40);
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP7
    public void testConstructor_RP_RP7() throws Throwable {
        Partial dt1 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.monthOfYear(), 12);
        Partial dt2 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 14);
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP8
    public void testConstructor_RP_RP8() throws Throwable {
        Partial dt1 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 12);
        Partial dt2 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 14);
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType1
    public void testConstructor_RP_RP_PeriodType1() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2004, 6, 9);
        YearMonthDay dt2 = new YearMonthDay(2005, 7, 10);
        Period test = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType2
    public void testConstructor_RP_RP_PeriodType2() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2004, 6, 9);
        YearMonthDay dt2 = new YearMonthDay(2005, 5, 17);
        Period test = new Period(dt1, dt2, PeriodType.yearMonthDay());
        assertEquals(PeriodType.yearMonthDay(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(11, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(8, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType2Local
    public void testConstructor_RP_RP_PeriodType2Local() throws Throwable {
        LocalDate dt1 = new LocalDate(2004, 6, 9);
        LocalDate dt2 = new LocalDate(2005, 5, 17);
        Period test = new Period(dt1, dt2, PeriodType.yearMonthDay());
        assertEquals(PeriodType.yearMonthDay(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(11, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(8, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType3
    public void testConstructor_RP_RP_PeriodType3() throws Throwable {
        YearMonthDay dt1 = null;
        YearMonthDay dt2 = new YearMonthDay(2005, 7, 17);
        try {
            new Period(dt1, dt2, PeriodType.standard());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType4
    public void testConstructor_RP_RP_PeriodType4() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2005, 7, 17);
        YearMonthDay dt2 = null;
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType5
    public void testConstructor_RP_RP_PeriodType5() throws Throwable {
        YearMonthDay dt1 = null;
        YearMonthDay dt2 = null;
        try {
            new Period(dt1, dt2, PeriodType.standard());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType6
    public void testConstructor_RP_RP_PeriodType6() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2005, 7, 17);
        TimeOfDay dt2 = new TimeOfDay(10, 20, 30, 40);
        try {
            new Period(dt1, dt2, PeriodType.standard());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType7
    public void testConstructor_RP_RP_PeriodType7() throws Throwable {
        Partial dt1 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.monthOfYear(), 12);
        Partial dt2 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 14);
        try {
            new Period(dt1, dt2, PeriodType.standard());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType8
    public void testConstructor_RP_RP_PeriodType8() throws Throwable {
        Partial dt1 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 12);
        Partial dt2 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 14);
        try {
            new Period(dt1, dt2, PeriodType.standard());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RD1
    public void testConstructor_RI_RD1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        Period test = new Period(dt1, dur);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RD2
    public void testConstructor_RI_RD2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        Period test = new Period(dt1, dur);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RD_PeriodType1
    public void testConstructor_RI_RD_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        Period test = new Period(dt1, dur, PeriodType.yearDayTime());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RD_PeriodType2
    public void testConstructor_RI_RD_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        Period test = new Period(dt1, dur, (PeriodType) null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RD_RI1
    public void testConstructor_RD_RI1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        Period test = new Period(dur, dt2);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RD_RI2
    public void testConstructor_RD_RI2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        Period test = new Period(dur, dt1);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RD_RI_PeriodType1
    public void testConstructor_RD_RI_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        Period test = new Period(dur, dt2, PeriodType.yearDayTime());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_RD_RI_PeriodType2
    public void testConstructor_RD_RI_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        Period test = new Period(dur, dt1, (PeriodType) null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        Period test = new Period("P1Y2M3D");
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

// org.joda.time.TestPeriod_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        Period test = new Period((Object) null);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_Object3
    public void testConstructor_Object3() throws Throwable {
        Period test = new Period(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()));
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

// org.joda.time.TestPeriod_Constructors::testConstructor_Object4
    public void testConstructor_Object4() throws Throwable {
        Period base = new Period(1, 1, 0, 1, 1, 1, 1, 1, PeriodType.standard());
        Period test = new Period(base);
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

// org.joda.time.TestPeriod_Constructors::testConstructor_Object_PeriodType1
    public void testConstructor_Object_PeriodType1() throws Throwable {
        Period test = new Period("P1Y2M3D", PeriodType.yearMonthDayTime());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_Object_PeriodType2
    public void testConstructor_Object_PeriodType2() throws Throwable {
        Period test = new Period((Object) null, PeriodType.yearMonthDayTime());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_Object_PeriodType3
    public void testConstructor_Object_PeriodType3() throws Throwable {
        Period test = new Period(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()), PeriodType.yearMonthDayTime());
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

// org.joda.time.TestPeriod_Constructors::testConstructor_Object_PeriodType4
    public void testConstructor_Object_PeriodType4() throws Throwable {
        Period test = new Period(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()), (PeriodType) null);
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

// org.joda.time.TestPeriod_Constructors::testFactoryYears
    public void testFactoryYears() throws Throwable {
        Period test = Period.years(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(6, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryMonths
    public void testFactoryMonths() throws Throwable {
        Period test = Period.months(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(6, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryWeeks
    public void testFactoryWeeks() throws Throwable {
        Period test = Period.weeks(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(6, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryDays
    public void testFactoryDays() throws Throwable {
        Period test = Period.days(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(6, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryHours
    public void testFactoryHours() throws Throwable {
        Period test = Period.hours(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(6, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryMinutes
    public void testFactoryMinutes() throws Throwable {
        Period test = Period.minutes(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactorySeconds
    public void testFactorySeconds() throws Throwable {
        Period test = Period.seconds(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(6, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryMillis
    public void testFactoryMillis() throws Throwable {
        Period test = Period.millis(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(6, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryFieldDifference1
    public void testFactoryFieldDifference1() throws Throwable {
        YearMonthDay start = new YearMonthDay(2005, 4, 9);
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth(),
        };
        Partial end = new Partial(types, new int[] {2004, 6, 7});
        Period test = Period.fieldDifference(start, end);
        assertEquals(PeriodType.yearMonthDay(), test.getPeriodType());
        assertEquals(-1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(-2, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryFieldDifference2
    public void testFactoryFieldDifference2() throws Throwable {
        YearMonthDay ymd = new YearMonthDay(2005, 4, 9);
        try {
            Period.fieldDifference(ymd, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            Period.fieldDifference((ReadablePartial) null, ymd);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testFactoryFieldDifference3
    public void testFactoryFieldDifference3() throws Throwable {
        YearMonthDay start = new YearMonthDay(2005, 4, 9);
        TimeOfDay endTime = new TimeOfDay(12, 30, 40, 0);
        try {
            Period.fieldDifference(start, endTime);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testFactoryFieldDifference4
    public void testFactoryFieldDifference4() throws Throwable {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfWeek(),
        };
        YearMonthDay start = new YearMonthDay(2005, 4, 9);
        Partial end = new Partial(types, new int[] {1, 2, 3});
        try {
            Period.fieldDifference(start, end);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testFactoryFieldDifference5
    public void testFactoryFieldDifference5() throws Throwable {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.dayOfWeek(),
        };
        Partial start = new Partial(types, new int[] {1, 2, 3});
        Partial end = new Partial(types, new int[] {1, 2, 3});
        try {
            Period.fieldDifference(start, end);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestSeconds::testConstants
    public void testConstants() {
        assertEquals(0, Seconds.ZERO.getSeconds());
        assertEquals(1, Seconds.ONE.getSeconds());
        assertEquals(2, Seconds.TWO.getSeconds());
        assertEquals(3, Seconds.THREE.getSeconds());
        assertEquals(Integer.MAX_VALUE, Seconds.MAX_VALUE.getSeconds());
        assertEquals(Integer.MIN_VALUE, Seconds.MIN_VALUE.getSeconds());
    }

// org.joda.time.TestSeconds::testFactory_seconds_int
    public void testFactory_seconds_int() {
        assertSame(Seconds.ZERO, Seconds.seconds(0));
        assertSame(Seconds.ONE, Seconds.seconds(1));
        assertSame(Seconds.TWO, Seconds.seconds(2));
        assertSame(Seconds.THREE, Seconds.seconds(3));
        assertSame(Seconds.MAX_VALUE, Seconds.seconds(Integer.MAX_VALUE));
        assertSame(Seconds.MIN_VALUE, Seconds.seconds(Integer.MIN_VALUE));
        assertEquals(-1, Seconds.seconds(-1).getSeconds());
        assertEquals(4, Seconds.seconds(4).getSeconds());
    }

// org.joda.time.TestSeconds::testFactory_secondsBetween_RInstant
    public void testFactory_secondsBetween_RInstant() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 3, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 9, 12, 0, 6, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 9, 12, 0, 9, 0, PARIS);
        
        assertEquals(3, Seconds.secondsBetween(start, end1).getSeconds());
        assertEquals(0, Seconds.secondsBetween(start, start).getSeconds());
        assertEquals(0, Seconds.secondsBetween(end1, end1).getSeconds());
        assertEquals(-3, Seconds.secondsBetween(end1, start).getSeconds());
        assertEquals(6, Seconds.secondsBetween(start, end2).getSeconds());
    }

// org.joda.time.TestSeconds::testFactory_secondsBetween_RPartial
    public void testFactory_secondsBetween_RPartial() {
        LocalTime start = new LocalTime(12, 0, 3);
        LocalTime end1 = new LocalTime(12, 0, 6);
        TimeOfDay end2 = new TimeOfDay(12, 0, 9);
        
        assertEquals(3, Seconds.secondsBetween(start, end1).getSeconds());
        assertEquals(0, Seconds.secondsBetween(start, start).getSeconds());
        assertEquals(0, Seconds.secondsBetween(end1, end1).getSeconds());
        assertEquals(-3, Seconds.secondsBetween(end1, start).getSeconds());
        assertEquals(6, Seconds.secondsBetween(start, end2).getSeconds());
    }

// org.joda.time.TestSeconds::testFactory_secondsIn_RInterval
    public void testFactory_secondsIn_RInterval() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 3, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 9, 12, 0, 6, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 9, 12, 0, 9, 0, PARIS);
        
        assertEquals(0, Seconds.secondsIn((ReadableInterval) null).getSeconds());
        assertEquals(3, Seconds.secondsIn(new Interval(start, end1)).getSeconds());
        assertEquals(0, Seconds.secondsIn(new Interval(start, start)).getSeconds());
        assertEquals(0, Seconds.secondsIn(new Interval(end1, end1)).getSeconds());
        assertEquals(6, Seconds.secondsIn(new Interval(start, end2)).getSeconds());
    }

// org.joda.time.TestSeconds::testFactory_standardSecondsIn_RPeriod
    public void testFactory_standardSecondsIn_RPeriod() {
        assertEquals(0, Seconds.standardSecondsIn((ReadablePeriod) null).getSeconds());
        assertEquals(0, Seconds.standardSecondsIn(Period.ZERO).getSeconds());
        assertEquals(1, Seconds.standardSecondsIn(new Period(0, 0, 0, 0, 0, 0, 1, 0)).getSeconds());
        assertEquals(123, Seconds.standardSecondsIn(Period.seconds(123)).getSeconds());
        assertEquals(-987, Seconds.standardSecondsIn(Period.seconds(-987)).getSeconds());
        assertEquals(2 * 24 * 60 * 60, Seconds.standardSecondsIn(Period.days(2)).getSeconds());
        try {
            Seconds.standardSecondsIn(Period.months(1));
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testFactory_parseSeconds_String
    public void testFactory_parseSeconds_String() {
        assertEquals(0, Seconds.parseSeconds((String) null).getSeconds());
        assertEquals(0, Seconds.parseSeconds("PT0S").getSeconds());
        assertEquals(1, Seconds.parseSeconds("PT1S").getSeconds());
        assertEquals(-3, Seconds.parseSeconds("PT-3S").getSeconds());
        assertEquals(2, Seconds.parseSeconds("P0Y0M0DT2S").getSeconds());
        assertEquals(2, Seconds.parseSeconds("PT0H2S").getSeconds());
        try {
            Seconds.parseSeconds("P1Y1D");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Seconds.parseSeconds("P1DT1S");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testGetMethods
    public void testGetMethods() {
        Seconds test = Seconds.seconds(20);
        assertEquals(20, test.getSeconds());
    }

// org.joda.time.TestSeconds::testGetFieldType
    public void testGetFieldType() {
        Seconds test = Seconds.seconds(20);
        assertEquals(DurationFieldType.seconds(), test.getFieldType());
    }

// org.joda.time.TestSeconds::testGetPeriodType
    public void testGetPeriodType() {
        Seconds test = Seconds.seconds(20);
        assertEquals(PeriodType.seconds(), test.getPeriodType());
    }

// org.joda.time.TestSeconds::testIsGreaterThan
    public void testIsGreaterThan() {
        assertEquals(true, Seconds.THREE.isGreaterThan(Seconds.TWO));
        assertEquals(false, Seconds.THREE.isGreaterThan(Seconds.THREE));
        assertEquals(false, Seconds.TWO.isGreaterThan(Seconds.THREE));
        assertEquals(true, Seconds.ONE.isGreaterThan(null));
        assertEquals(false, Seconds.seconds(-1).isGreaterThan(null));
    }

// org.joda.time.TestSeconds::testIsLessThan
    public void testIsLessThan() {
        assertEquals(false, Seconds.THREE.isLessThan(Seconds.TWO));
        assertEquals(false, Seconds.THREE.isLessThan(Seconds.THREE));
        assertEquals(true, Seconds.TWO.isLessThan(Seconds.THREE));
        assertEquals(false, Seconds.ONE.isLessThan(null));
        assertEquals(true, Seconds.seconds(-1).isLessThan(null));
    }

// org.joda.time.TestSeconds::testToString
    public void testToString() {
        Seconds test = Seconds.seconds(20);
        assertEquals("PT20S", test.toString());
        
        test = Seconds.seconds(-20);
        assertEquals("PT-20S", test.toString());
    }

// org.joda.time.TestSeconds::testSerialization
    public void testSerialization() throws Exception {
        Seconds test = Seconds.THREE;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Seconds result = (Seconds) ois.readObject();
        ois.close();
        
        assertSame(test, result);
    }

// org.joda.time.TestSeconds::testToStandardWeeks
    public void testToStandardWeeks() {
        Seconds test = Seconds.seconds(60 * 60 * 24 * 7 * 2);
        Weeks expected = Weeks.weeks(2);
        assertEquals(expected, test.toStandardWeeks());
    }

// org.joda.time.TestSeconds::testToStandardDays
    public void testToStandardDays() {
        Seconds test = Seconds.seconds(60 * 60 * 24 * 2);
        Days expected = Days.days(2);
        assertEquals(expected, test.toStandardDays());
    }

// org.joda.time.TestSeconds::testToStandardHours
    public void testToStandardHours() {
        Seconds test = Seconds.seconds(60 * 60 * 2);
        Hours expected = Hours.hours(2);
        assertEquals(expected, test.toStandardHours());
    }

// org.joda.time.TestSeconds::testToStandardMinutes
    public void testToStandardMinutes() {
        Seconds test = Seconds.seconds(60 * 2);
        Minutes expected = Minutes.minutes(2);
        assertEquals(expected, test.toStandardMinutes());
    }

// org.joda.time.TestSeconds::testToStandardDuration
    public void testToStandardDuration() {
        Seconds test = Seconds.seconds(20);
        Duration expected = new Duration(20L * DateTimeConstants.MILLIS_PER_SECOND);
        assertEquals(expected, test.toStandardDuration());
        
        expected = new Duration(((long) Integer.MAX_VALUE) * DateTimeConstants.MILLIS_PER_SECOND);
        assertEquals(expected, Seconds.MAX_VALUE.toStandardDuration());
    }

// org.joda.time.TestSeconds::testPlus_int
    public void testPlus_int() {
        Seconds test2 = Seconds.seconds(2);
        Seconds result = test2.plus(3);
        assertEquals(2, test2.getSeconds());
        assertEquals(5, result.getSeconds());
        
        assertEquals(1, Seconds.ONE.plus(0).getSeconds());
        
        try {
            Seconds.MAX_VALUE.plus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testPlus_Seconds
    public void testPlus_Seconds() {
        Seconds test2 = Seconds.seconds(2);
        Seconds test3 = Seconds.seconds(3);
        Seconds result = test2.plus(test3);
        assertEquals(2, test2.getSeconds());
        assertEquals(3, test3.getSeconds());
        assertEquals(5, result.getSeconds());
        
        assertEquals(1, Seconds.ONE.plus(Seconds.ZERO).getSeconds());
        assertEquals(1, Seconds.ONE.plus((Seconds) null).getSeconds());
        
        try {
            Seconds.MAX_VALUE.plus(Seconds.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testMinus_int
    public void testMinus_int() {
        Seconds test2 = Seconds.seconds(2);
        Seconds result = test2.minus(3);
        assertEquals(2, test2.getSeconds());
        assertEquals(-1, result.getSeconds());
        
        assertEquals(1, Seconds.ONE.minus(0).getSeconds());
        
        try {
            Seconds.MIN_VALUE.minus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testMinus_Seconds
    public void testMinus_Seconds() {
        Seconds test2 = Seconds.seconds(2);
        Seconds test3 = Seconds.seconds(3);
        Seconds result = test2.minus(test3);
        assertEquals(2, test2.getSeconds());
        assertEquals(3, test3.getSeconds());
        assertEquals(-1, result.getSeconds());
        
        assertEquals(1, Seconds.ONE.minus(Seconds.ZERO).getSeconds());
        assertEquals(1, Seconds.ONE.minus((Seconds) null).getSeconds());
        
        try {
            Seconds.MIN_VALUE.minus(Seconds.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testMultipliedBy_int
    public void testMultipliedBy_int() {
        Seconds test = Seconds.seconds(2);
        assertEquals(6, test.multipliedBy(3).getSeconds());
        assertEquals(2, test.getSeconds());
        assertEquals(-6, test.multipliedBy(-3).getSeconds());
        assertSame(test, test.multipliedBy(1));
        
        Seconds halfMax = Seconds.seconds(Integer.MAX_VALUE / 2 + 1);
        try {
            halfMax.multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testDividedBy_int
    public void testDividedBy_int() {
        Seconds test = Seconds.seconds(12);
        assertEquals(6, test.dividedBy(2).getSeconds());
        assertEquals(12, test.getSeconds());
        assertEquals(4, test.dividedBy(3).getSeconds());
        assertEquals(3, test.dividedBy(4).getSeconds());
        assertEquals(2, test.dividedBy(5).getSeconds());
        assertEquals(2, test.dividedBy(6).getSeconds());
        assertSame(test, test.dividedBy(1));
        
        try {
            Seconds.ONE.dividedBy(0);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testNegated
    public void testNegated() {
        Seconds test = Seconds.seconds(12);
        assertEquals(-12, test.negated().getSeconds());
        assertEquals(12, test.getSeconds());
        
        try {
            Seconds.MIN_VALUE.negated();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testAddToLocalDate
    public void testAddToLocalDate() {
        Seconds test = Seconds.seconds(26);
        LocalDateTime date = new LocalDateTime(2006, 6, 1, 0, 0, 0, 0);
        LocalDateTime expected = new LocalDateTime(2006, 6, 1, 0, 0, 26, 0);
        assertEquals(expected, date.plus(test));
    }

// org.joda.time.TestSerialization::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestSerialization::testSerializedInstant
    public void testSerializedInstant() throws Exception {
        Instant test = new Instant();
        loadAndCompare(test, "Instant", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateTime
    public void testSerializedDateTime() throws Exception {
        DateTime test = new DateTime();
        loadAndCompare(test, "DateTime", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateTimeProperty
    public void testSerializedDateTimeProperty() throws Exception {
        DateTime.Property test = new DateTime().hourOfDay();
        loadAndCompare(test, "DateTimeProperty", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedMutableDateTime
    public void testSerializedMutableDateTime() throws Exception {
        MutableDateTime test = new MutableDateTime();
        loadAndCompare(test, "MutableDateTime", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedMutableDateTimeProperty
    public void testSerializedMutableDateTimeProperty() throws Exception {
        MutableDateTime.Property test = new MutableDateTime().hourOfDay();
        loadAndCompare(test, "MutableDateTimeProperty", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateMidnight
    public void testSerializedDateMidnight() throws Exception {
        DateMidnight test = new DateMidnight();
        loadAndCompare(test, "DateMidnight", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateMidnightProperty
    public void testSerializedDateMidnightProperty() throws Exception {
        DateMidnight.Property test = new DateMidnight().monthOfYear();
        loadAndCompare(test, "DateMidnightProperty", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedLocalDate
    public void testSerializedLocalDate() throws Exception {
        LocalDate test = new LocalDate();
        loadAndCompare(test, "LocalDate", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedLocalDateBuddhist
    public void testSerializedLocalDateBuddhist() throws Exception {
        LocalDate test = new LocalDate(BuddhistChronology.getInstanceUTC());
        loadAndCompare(test, "LocalDateBuddhist", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedLocalTime
    public void testSerializedLocalTime() throws Exception {
        LocalTime test = new LocalTime();
        loadAndCompare(test, "LocalTime", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedLocalDateTime
    public void testSerializedLocalDateTime() throws Exception {
        LocalDateTime test = new LocalDateTime();
        loadAndCompare(test, "LocalDateTime", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedYearMonthDay
    public void testSerializedYearMonthDay() throws Exception {
        YearMonthDay test = new YearMonthDay();
        loadAndCompare(test, "YearMonthDay", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedTimeOfDay
    public void testSerializedTimeOfDay() throws Exception {
        TimeOfDay test = new TimeOfDay();
        loadAndCompare(test, "TimeOfDay", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateTimeZoneUTC
    public void testSerializedDateTimeZoneUTC() throws Exception {
        DateTimeZone test = DateTimeZone.UTC;
        loadAndCompare(test, "DateTimeZoneUTC", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedDateTimeZone
    public void testSerializedDateTimeZone() throws Exception {
        
        
        DateTimeZone test = DateTimeZone.forID("Europe/Paris");
        loadAndCompare(test, "DateTimeZone", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testDuration
    public void testDuration() throws Exception {
        Duration test = Duration.millis(12345);
        loadAndCompare(test, "Duration", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedCopticChronology
    public void testSerializedCopticChronology() throws Exception {
        CopticChronology test = CopticChronology.getInstance(LONDON);
        loadAndCompare(test, "CopticChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedISOChronology
    public void testSerializedISOChronology() throws Exception {
        ISOChronology test = ISOChronology.getInstance(PARIS);
        loadAndCompare(test, "ISOChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedGJChronology
    public void testSerializedGJChronology() throws Exception {
        GJChronology test = GJChronology.getInstance(TOKYO);
        loadAndCompare(test, "GJChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedGJChronologyChangedInternals
    public void testSerializedGJChronologyChangedInternals() throws Exception {
        GJChronology test = GJChronology.getInstance(PARIS, 123L, 2);
        loadAndCompare(test, "GJChronologyChangedInternals", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedGregorianChronology
    public void testSerializedGregorianChronology() throws Exception {
        GregorianChronology test = GregorianChronology.getInstance(PARIS);
        loadAndCompare(test, "GregorianChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedJulianChronology
    public void testSerializedJulianChronology() throws Exception {
        JulianChronology test = JulianChronology.getInstance(PARIS);
        loadAndCompare(test, "JulianChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedBuddhistChronology
    public void testSerializedBuddhistChronology() throws Exception {
        BuddhistChronology test = BuddhistChronology.getInstance(PARIS);
        loadAndCompare(test, "BuddhistChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedPeriodType
    public void testSerializedPeriodType() throws Exception {
        PeriodType test = PeriodType.dayTime();
        loadAndCompare(test, "PeriodType", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateTimeFieldType
    public void testSerializedDateTimeFieldType() throws Exception {
        DateTimeFieldType test = DateTimeFieldType.clockhourOfDay();
        loadAndCompare(test, "DateTimeFieldType", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedUnsupportedDateTimeField
    public void testSerializedUnsupportedDateTimeField() throws Exception {
        UnsupportedDateTimeField test = UnsupportedDateTimeField.getInstance(
                DateTimeFieldType.year(),
                UnsupportedDurationField.getInstance(DurationFieldType.years()));
        loadAndCompare(test, "UnsupportedDateTimeField", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestStringConvert::testDateMidnight
    public void testDateMidnight() {
        DateMidnight test = new DateMidnight(2010, 6, 30, ISOChronology.getInstance(ZONE));
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("2010-06-30T00:00:00.000+02:00", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(DateMidnight.class, str));
    }

// org.joda.time.TestStringConvert::testDateTime
    public void testDateTime() {
        DateTime test = new DateTime(2010, 6, 30, 2, 30, 50, 678, ISOChronology.getInstance(ZONE));
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("2010-06-30T02:30:50.678+02:00", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(DateTime.class, str));
    }

// org.joda.time.TestStringConvert::testMutableDateTime
    public void testMutableDateTime() {
        MutableDateTime test = new MutableDateTime(2010, 6, 30, 2, 30, 50, 678, ISOChronology.getInstance(ZONE));
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("2010-06-30T02:30:50.678+02:00", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(MutableDateTime.class, str));
    }

// org.joda.time.TestStringConvert::testLocalDateTime
    public void testLocalDateTime() {
        LocalDateTime test = new LocalDateTime(2010, 6, 30, 2, 30);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("2010-06-30T02:30:00.000", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(LocalDateTime.class, str));
    }

// org.joda.time.TestStringConvert::testLocalDate
    public void testLocalDate() {
        LocalDate test = new LocalDate(2010, 6, 30);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("2010-06-30", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(LocalDate.class, str));
    }

// org.joda.time.TestStringConvert::testLocalTime
    public void testLocalTime() {
        LocalTime test = new LocalTime(2, 30, 50, 678);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("02:30:50.678", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(LocalTime.class, str));
    }

// org.joda.time.TestStringConvert::testYearMonth
    public void testYearMonth() {
        YearMonth test = new YearMonth(2010, 6);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("2010-06", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(YearMonth.class, str));
    }

// org.joda.time.TestStringConvert::testMonthDay
    public void testMonthDay() {
        MonthDay test = new MonthDay(6, 30);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("--06-30", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(MonthDay.class, str));
    }

// org.joda.time.TestStringConvert::testMonthDay_leapDay
    public void testMonthDay_leapDay() {
        MonthDay test = new MonthDay(2, 29);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("--02-29", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(MonthDay.class, str));
    }

// org.joda.time.TestStringConvert::testTimeZone
    public void testTimeZone() {
        DateTimeZone test = DateTimeZone.forID("Europe/Paris");
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("Europe/Paris", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(DateTimeZone.class, str));
    }

// org.joda.time.TestStringConvert::testDuration
    public void testDuration() {
        Duration test = new Duration(12345678L);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("PT12345.678S", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(Duration.class, str));
    }

// org.joda.time.TestStringConvert::testPeriod
    public void testPeriod() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("P1Y2M3W4DT5H6M7.008S", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(Period.class, str));
    }

// org.joda.time.TestStringConvert::testMutablePeriod
    public void testMutablePeriod() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("P1Y2M3W4DT5H6M7.008S", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(MutablePeriod.class, str));
    }

// org.joda.time.TestStringConvert::testYears
    public void testYears() {
        Years test = Years.years(5);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("P5Y", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(Years.class, str));
    }

// org.joda.time.TestStringConvert::testMonths
    public void testMonths() {
        Months test = Months.months(5);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("P5M", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(Months.class, str));
    }

// org.joda.time.TestStringConvert::testWeeks
    public void testWeeks() {
        Weeks test = Weeks.weeks(5);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("P5W", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(Weeks.class, str));
    }

// org.joda.time.TestStringConvert::testDays
    public void testDays() {
        Days test = Days.days(5);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("P5D", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(Days.class, str));
    }

// org.joda.time.TestStringConvert::testHours
    public void testHours() {
        Hours test = Hours.hours(5);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("PT5H", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(Hours.class, str));
    }

// org.joda.time.TestStringConvert::testMinutes
    public void testMinutes() {
        Minutes test = Minutes.minutes(5);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("PT5M", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(Minutes.class, str));
    }

// org.joda.time.TestStringConvert::testSeconds
    public void testSeconds() {
        Seconds test = Seconds.seconds(5);
        String str = StringConvert.INSTANCE.convertToString(test);
        assertEquals("PT5S", str);
        assertEquals(test, StringConvert.INSTANCE.convertFromString(Seconds.class, str));
    }

// org.joda.time.TestTimeOfDay_Basics::testGet
    public void testGet() {
        TimeOfDay test = new TimeOfDay();
        assertEquals(10 + OFFSET, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(30, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(40, test.get(DateTimeFieldType.millisOfSecond()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.dayOfMonth());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testSize
    public void testSize() {
        TimeOfDay test = new TimeOfDay();
        assertEquals(4, test.size());
    }

// org.joda.time.TestTimeOfDay_Basics::testGetFieldType
    public void testGetFieldType() {
        TimeOfDay test = new TimeOfDay(COPTIC_PARIS);
        assertSame(DateTimeFieldType.hourOfDay(), test.getFieldType(0));
        assertSame(DateTimeFieldType.minuteOfHour(), test.getFieldType(1));
        assertSame(DateTimeFieldType.secondOfMinute(), test.getFieldType(2));
        assertSame(DateTimeFieldType.millisOfSecond(), test.getFieldType(3));
        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(5);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        TimeOfDay test = new TimeOfDay(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.hourOfDay(), fields[0]);
        assertSame(DateTimeFieldType.minuteOfHour(), fields[1]);
        assertSame(DateTimeFieldType.secondOfMinute(), fields[2]);
        assertSame(DateTimeFieldType.millisOfSecond(), fields[3]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestTimeOfDay_Basics::testGetField
    public void testGetField() {
        TimeOfDay test = new TimeOfDay(COPTIC_PARIS);
        assertSame(CopticChronology.getInstanceUTC().hourOfDay(), test.getField(0));
        assertSame(CopticChronology.getInstanceUTC().minuteOfHour(), test.getField(1));
        assertSame(CopticChronology.getInstanceUTC().secondOfMinute(), test.getField(2));
        assertSame(CopticChronology.getInstanceUTC().millisOfSecond(), test.getField(3));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(5);
        } catch (IndexOutOfBoundsException ex) {}
    }

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
