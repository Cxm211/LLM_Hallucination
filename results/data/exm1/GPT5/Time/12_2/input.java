// buggy code
    public static LocalDate fromCalendarFields(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("The calendar must not be null");
        }
        int yearOfEra = calendar.get(Calendar.YEAR);
        return new LocalDate(
            yearOfEra,
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    public static LocalDate fromDateFields(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
            // handle years in era BC
        return new LocalDate(
            date.getYear() + 1900,
            date.getMonth() + 1,
            date.getDate()
        );
    }

    public static LocalDateTime fromCalendarFields(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("The calendar must not be null");
        }
        int yearOfEra = calendar.get(Calendar.YEAR);
        return new LocalDateTime(
            yearOfEra,
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
            calendar.get(Calendar.MILLISECOND)
        );
    }

    public static LocalDateTime fromDateFields(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
            // handle years in era BC
        return new LocalDateTime(
            date.getYear() + 1900,
            date.getMonth() + 1,
            date.getDate(),
            date.getHours(),
            date.getMinutes(),
            date.getSeconds(),
            (((int) (date.getTime() % 1000)) + 1000) % 1000
        );
    }

// relevant test
// org.joda.time.TestLocalDateTime_Properties::testPropertyGetMinute
    public void testPropertyGetMinute() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        assertSame(test.getChronology().minuteOfHour(), test.minuteOfHour().getField());
        assertEquals("minuteOfHour", test.minuteOfHour().getName());
        assertEquals("Property[minuteOfHour]", test.minuteOfHour().toString());
        assertSame(test, test.minuteOfHour().getLocalDateTime());
        assertEquals(20, test.minuteOfHour().get());
        assertEquals("20", test.minuteOfHour().getAsString());
        assertEquals("20", test.minuteOfHour().getAsText());
        assertEquals("20", test.minuteOfHour().getAsText(Locale.FRENCH));
        assertEquals("20", test.minuteOfHour().getAsShortText());
        assertEquals("20", test.minuteOfHour().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().minutes(), test.minuteOfHour().getDurationField());
        assertEquals(test.getChronology().hours(), test.minuteOfHour().getRangeDurationField());
        assertEquals(2, test.minuteOfHour().getMaximumTextLength(null));
        assertEquals(2, test.minuteOfHour().getMaximumShortTextLength(null));
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetMaxMinValuesMinute
    public void testPropertyGetMaxMinValuesMinute() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        assertEquals(0, test.minuteOfHour().getMinimumValue());
        assertEquals(0, test.minuteOfHour().getMinimumValueOverall());
        assertEquals(59, test.minuteOfHour().getMaximumValue());
        assertEquals(59, test.minuteOfHour().getMaximumValueOverall());
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyWithMaxMinValueMinute
    public void testPropertyWithMaxMinValueMinute() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        check(test.minuteOfHour().withMaximumValue(), 2005, 6, 9, 10, 59, 30, 40);
        check(test.minuteOfHour().withMinimumValue(), 2005, 6, 9, 10, 0, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddToCopyMinute
    public void testPropertyAddToCopyMinute() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.minuteOfHour().addToCopy(9);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 29, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(39);
        check(copy, 2005, 6, 9, 10, 59, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(40);
        check(copy, 2005, 6, 9, 11, 0, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(1 * 60 + 45);
        check(copy, 2005, 6, 9, 12, 5, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(13 * 60 + 39);
        check(copy, 2005, 6, 9, 23, 59, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(13 * 60 + 40);
        check(copy, 2005, 6, 10, 0, 0, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(-9);
        check(copy, 2005, 6, 9, 10, 11, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(-19);
        check(copy, 2005, 6, 9, 10, 1, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(-20);
        check(copy, 2005, 6, 9, 10, 0, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(-21);
        check(copy, 2005, 6, 9, 9, 59, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(-(10 * 60 + 20));
        check(copy, 2005, 6, 9, 0, 0, 30, 40);
        
        copy = test.minuteOfHour().addToCopy(-(10 * 60 + 21));
        check(copy, 2005, 6, 8, 23, 59, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddWrapFieldToCopyMinute
    public void testPropertyAddWrapFieldToCopyMinute() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.minuteOfHour().addWrapFieldToCopy(9);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 29, 30, 40);
        
        copy = test.minuteOfHour().addWrapFieldToCopy(49);
        check(copy, 2005, 6, 9, 10, 9, 30, 40);
        
        copy = test.minuteOfHour().addWrapFieldToCopy(-47);
        check(copy, 2005, 6, 9, 10, 33, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetMinute
    public void testPropertySetMinute() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.minuteOfHour().setCopy(12);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 12, 30, 40);
        
        try {
            test.minuteOfHour().setCopy(60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.minuteOfHour().setCopy(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetTextMinute
    public void testPropertySetTextMinute() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.minuteOfHour().setCopy("12");
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 12, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyCompareToMinute
    public void testPropertyCompareToMinute() {
        LocalDateTime test1 = new LocalDateTime(TEST_TIME1);
        LocalDateTime test2 = new LocalDateTime(TEST_TIME2);
        assertEquals(true, test1.minuteOfHour().compareTo(test2) < 0);
        assertEquals(true, test2.minuteOfHour().compareTo(test1) > 0);
        assertEquals(true, test1.minuteOfHour().compareTo(test1) == 0);
        try {
            test1.minuteOfHour().compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.minuteOfHour().compareTo(dt2) < 0);
        assertEquals(true, test2.minuteOfHour().compareTo(dt1) > 0);
        assertEquals(true, test1.minuteOfHour().compareTo(dt1) == 0);
        try {
            test1.minuteOfHour().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetSecond
    public void testPropertyGetSecond() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        assertSame(test.getChronology().secondOfMinute(), test.secondOfMinute().getField());
        assertEquals("secondOfMinute", test.secondOfMinute().getName());
        assertEquals("Property[secondOfMinute]", test.secondOfMinute().toString());
        assertSame(test, test.secondOfMinute().getLocalDateTime());
        assertEquals(30, test.secondOfMinute().get());
        assertEquals("30", test.secondOfMinute().getAsString());
        assertEquals("30", test.secondOfMinute().getAsText());
        assertEquals("30", test.secondOfMinute().getAsText(Locale.FRENCH));
        assertEquals("30", test.secondOfMinute().getAsShortText());
        assertEquals("30", test.secondOfMinute().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().seconds(), test.secondOfMinute().getDurationField());
        assertEquals(test.getChronology().minutes(), test.secondOfMinute().getRangeDurationField());
        assertEquals(2, test.secondOfMinute().getMaximumTextLength(null));
        assertEquals(2, test.secondOfMinute().getMaximumShortTextLength(null));
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetMaxMinValuesSecond
    public void testPropertyGetMaxMinValuesSecond() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        assertEquals(0, test.secondOfMinute().getMinimumValue());
        assertEquals(0, test.secondOfMinute().getMinimumValueOverall());
        assertEquals(59, test.secondOfMinute().getMaximumValue());
        assertEquals(59, test.secondOfMinute().getMaximumValueOverall());
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyWithMaxMinValueSecond
    public void testPropertyWithMaxMinValueSecond() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        check(test.secondOfMinute().withMaximumValue(), 2005, 6, 9, 10, 20, 59, 40);
        check(test.secondOfMinute().withMinimumValue(), 2005, 6, 9, 10, 20, 0, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddToCopySecond
    public void testPropertyAddToCopySecond() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.secondOfMinute().addToCopy(9);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 20, 39, 40);
        
        copy = test.secondOfMinute().addToCopy(29);
        check(copy, 2005, 6, 9, 10, 20, 59, 40);
        
        copy = test.secondOfMinute().addToCopy(30);
        check(copy, 2005, 6, 9, 10, 21, 0, 40);
        
        copy = test.secondOfMinute().addToCopy(39 * 60 + 29);
        check(copy, 2005, 6, 9, 10, 59, 59, 40);
        
        copy = test.secondOfMinute().addToCopy(39 * 60 + 30);
        check(copy, 2005, 6, 9, 11, 0, 0, 40);
        
        copy = test.secondOfMinute().addToCopy(13 * 60 * 60 + 39 * 60 + 30);
        check(copy, 2005, 6, 10, 0, 0, 0, 40);
        
        copy = test.secondOfMinute().addToCopy(-9);
        check(copy, 2005, 6, 9, 10, 20, 21, 40);
        
        copy = test.secondOfMinute().addToCopy(-30);
        check(copy, 2005, 6, 9, 10, 20, 0, 40);
        
        copy = test.secondOfMinute().addToCopy(-31);
        check(copy, 2005, 6, 9, 10, 19, 59, 40);
        
        copy = test.secondOfMinute().addToCopy(-(10 * 60 * 60 + 20 * 60 + 30));
        check(copy, 2005, 6, 9, 0, 0, 0, 40);
        
        copy = test.secondOfMinute().addToCopy(-(10 * 60 * 60 + 20 * 60 + 31));
        check(copy, 2005, 6, 8, 23, 59, 59, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddWrapFieldToCopySecond
    public void testPropertyAddWrapFieldToCopySecond() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.secondOfMinute().addWrapFieldToCopy(9);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 20, 39, 40);
        
        copy = test.secondOfMinute().addWrapFieldToCopy(49);
        check(copy, 2005, 6, 9, 10, 20, 19, 40);
        
        copy = test.secondOfMinute().addWrapFieldToCopy(-47);
        check(copy, 2005, 6, 9, 10, 20, 43, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetSecond
    public void testPropertySetSecond() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.secondOfMinute().setCopy(12);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 20, 12, 40);
        
        try {
            test.secondOfMinute().setCopy(60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.secondOfMinute().setCopy(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetTextSecond
    public void testPropertySetTextSecond() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.secondOfMinute().setCopy("12");
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 20, 12, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyCompareToSecond
    public void testPropertyCompareToSecond() {
        LocalDateTime test1 = new LocalDateTime(TEST_TIME1);
        LocalDateTime test2 = new LocalDateTime(TEST_TIME2);
        assertEquals(true, test1.secondOfMinute().compareTo(test2) < 0);
        assertEquals(true, test2.secondOfMinute().compareTo(test1) > 0);
        assertEquals(true, test1.secondOfMinute().compareTo(test1) == 0);
        try {
            test1.secondOfMinute().compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.secondOfMinute().compareTo(dt2) < 0);
        assertEquals(true, test2.secondOfMinute().compareTo(dt1) > 0);
        assertEquals(true, test1.secondOfMinute().compareTo(dt1) == 0);
        try {
            test1.secondOfMinute().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetMilli
    public void testPropertyGetMilli() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        assertSame(test.getChronology().millisOfSecond(), test.millisOfSecond().getField());
        assertEquals("millisOfSecond", test.millisOfSecond().getName());
        assertEquals("Property[millisOfSecond]", test.millisOfSecond().toString());
        assertSame(test, test.millisOfSecond().getLocalDateTime());
        assertEquals(40, test.millisOfSecond().get());
        assertEquals("40", test.millisOfSecond().getAsString());
        assertEquals("40", test.millisOfSecond().getAsText());
        assertEquals("40", test.millisOfSecond().getAsText(Locale.FRENCH));
        assertEquals("40", test.millisOfSecond().getAsShortText());
        assertEquals("40", test.millisOfSecond().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().millis(), test.millisOfSecond().getDurationField());
        assertEquals(test.getChronology().seconds(), test.millisOfSecond().getRangeDurationField());
        assertEquals(3, test.millisOfSecond().getMaximumTextLength(null));
        assertEquals(3, test.millisOfSecond().getMaximumShortTextLength(null));
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetMaxMinValuesMilli
    public void testPropertyGetMaxMinValuesMilli() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        assertEquals(0, test.millisOfSecond().getMinimumValue());
        assertEquals(0, test.millisOfSecond().getMinimumValueOverall());
        assertEquals(999, test.millisOfSecond().getMaximumValue());
        assertEquals(999, test.millisOfSecond().getMaximumValueOverall());
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyWithMaxMinValueMilli
    public void testPropertyWithMaxMinValueMilli() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        check(test.millisOfSecond().withMaximumValue(), 2005, 6, 9, 10, 20, 30, 999);
        check(test.millisOfSecond().withMinimumValue(), 2005, 6, 9, 10, 20, 30, 0);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddToCopyMilli
    public void testPropertyAddToCopyMilli() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.millisOfSecond().addToCopy(9);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 20, 30, 49);
        
        copy = test.millisOfSecond().addToCopy(959);
        check(copy, 2005, 6, 9, 10, 20, 30, 999);
        
        copy = test.millisOfSecond().addToCopy(960);
        check(copy, 2005, 6, 9, 10, 20, 31, 0);
        
        copy = test.millisOfSecond().addToCopy(13 * 60 * 60 * 1000 + 39 * 60 * 1000 + 29 * 1000 + 959);
        check(copy, 2005, 6, 9, 23, 59, 59, 999);
        
        copy = test.millisOfSecond().addToCopy(13 * 60 * 60 * 1000 + 39 * 60 * 1000 + 29 * 1000 + 960);
        check(copy, 2005, 6, 10, 0, 0, 0, 0);
        
        copy = test.millisOfSecond().addToCopy(-9);
        check(copy, 2005, 6, 9, 10, 20, 30, 31);
        
        copy = test.millisOfSecond().addToCopy(-40);
        check(copy, 2005, 6, 9, 10, 20, 30, 0);
        
        copy = test.millisOfSecond().addToCopy(-41);
        check(copy, 2005, 6, 9, 10, 20, 29, 999);
        
        copy = test.millisOfSecond().addToCopy(-(10 * 60 * 60 * 1000 + 20 * 60 * 1000 + 30 * 1000 + 40));
        check(copy, 2005, 6, 9, 0, 0, 0, 0);
        
        copy = test.millisOfSecond().addToCopy(-(10 * 60 * 60 * 1000 + 20 * 60 * 1000 + 30 * 1000 + 41));
        check(copy, 2005, 6, 8, 23, 59, 59, 999);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddWrapFieldToCopyMilli
    public void testPropertyAddWrapFieldToCopyMilli() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.millisOfSecond().addWrapFieldToCopy(9);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 20, 30, 49);
        
        copy = test.millisOfSecond().addWrapFieldToCopy(995);
        check(copy, 2005, 6, 9, 10, 20, 30, 35);
        
        copy = test.millisOfSecond().addWrapFieldToCopy(-47);
        check(copy, 2005, 6, 9, 10, 20, 30, 993);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetMilli
    public void testPropertySetMilli() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.millisOfSecond().setCopy(12);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 20, 30, 12);
        
        try {
            test.millisOfSecond().setCopy(1000);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.millisOfSecond().setCopy(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetTextMilli
    public void testPropertySetTextMilli() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.millisOfSecond().setCopy("12");
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 10, 20, 30, 12);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyCompareToMilli
    public void testPropertyCompareToMilli() {
        LocalDateTime test1 = new LocalDateTime(TEST_TIME1);
        LocalDateTime test2 = new LocalDateTime(TEST_TIME2);
        assertEquals(true, test1.millisOfSecond().compareTo(test2) < 0);
        assertEquals(true, test2.millisOfSecond().compareTo(test1) > 0);
        assertEquals(true, test1.millisOfSecond().compareTo(test1) == 0);
        try {
            test1.millisOfSecond().compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.millisOfSecond().compareTo(dt2) < 0);
        assertEquals(true, test2.millisOfSecond().compareTo(dt1) > 0);
        assertEquals(true, test1.millisOfSecond().compareTo(dt1) == 0);
        try {
            test1.millisOfSecond().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        LocalDate test = new LocalDate();
        assertEquals(1970, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(2, test.get(DateTimeFieldType.dayOfWeek()));
        assertEquals(160, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(24, test.get(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(1970, test.get(DateTimeFieldType.weekyear()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.hourOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testSize
    public void testSize() {
        LocalDate test = new LocalDate();
        assertEquals(3, test.size());
    }

// org.joda.time.TestLocalDate_Basics::testGetFieldType_int
    public void testGetFieldType_int() {
        LocalDate test = new LocalDate(COPTIC_PARIS);
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

// org.joda.time.TestLocalDate_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        LocalDate test = new LocalDate(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.year(), fields[0]);
        assertSame(DateTimeFieldType.monthOfYear(), fields[1]);
        assertSame(DateTimeFieldType.dayOfMonth(), fields[2]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestLocalDate_Basics::testGetField_int
    public void testGetField_int() {
        LocalDate test = new LocalDate(COPTIC_PARIS);
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

// org.joda.time.TestLocalDate_Basics::testGetFields
    public void testGetFields() {
        LocalDate test = new LocalDate(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertSame(COPTIC_UTC.year(), fields[0]);
        assertSame(COPTIC_UTC.monthOfYear(), fields[1]);
        assertSame(COPTIC_UTC.dayOfMonth(), fields[2]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestLocalDate_Basics::testGetValue_int
    public void testGetValue_int() {
        LocalDate test = new LocalDate();
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

// org.joda.time.TestLocalDate_Basics::testGetValues
    public void testGetValues() {
        LocalDate test = new LocalDate();
        int[] values = test.getValues();
        assertEquals(1970, values[0]);
        assertEquals(6, values[1]);
        assertEquals(9, values[2]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestLocalDate_Basics::testIsSupported_DateTimeFieldType
    public void testIsSupported_DateTimeFieldType() {
        LocalDate test = new LocalDate(COPTIC_PARIS);
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfWeek()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfCentury()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.centuryOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekyearOfCentury()));
        assertEquals(true, test.isSupported(DateTimeFieldType.era()));
        assertEquals(false, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(false, test.isSupported((DateTimeFieldType) null));
    }

// org.joda.time.TestLocalDate_Basics::testIsSupported_DurationFieldType
    public void testIsSupported_DurationFieldType() {
        LocalDate test = new LocalDate(1970, 6, 9);
        assertEquals(false, test.isSupported(DurationFieldType.eras()));
        assertEquals(true, test.isSupported(DurationFieldType.centuries()));
        assertEquals(true, test.isSupported(DurationFieldType.years()));
        assertEquals(true, test.isSupported(DurationFieldType.months()));
        assertEquals(true, test.isSupported(DurationFieldType.weekyears()));
        assertEquals(true, test.isSupported(DurationFieldType.weeks()));
        assertEquals(true, test.isSupported(DurationFieldType.days()));
        
        assertEquals(false, test.isSupported(DurationFieldType.hours()));
        assertEquals(false, test.isSupported((DurationFieldType) null));
    }

// org.joda.time.TestLocalDate_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        LocalDate test1 = new LocalDate(1970, 6, 9, COPTIC_PARIS);
        LocalDate test2 = new LocalDate(1970, 6, 9, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        LocalDate test3 = new LocalDate(1971, 6, 9);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(true, test1.equals(new YearMonthDay(1970, 6, 9, COPTIC_PARIS)));
        assertEquals(true, test1.hashCode() == new YearMonthDay(1970, 6, 9, COPTIC_PARIS).hashCode());
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestLocalDate_Basics::testEqualsHashCodeLenient
    public void testEqualsHashCodeLenient() {
        LocalDate test1 = new LocalDate(1970, 6, 9, LenientChronology.getInstance(COPTIC_PARIS));
        LocalDate test2 = new LocalDate(1970, 6, 9, LenientChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
    }

// org.joda.time.TestLocalDate_Basics::testEqualsHashCodeStrict
    public void testEqualsHashCodeStrict() {
        LocalDate test1 = new LocalDate(1970, 6, 9, StrictChronology.getInstance(COPTIC_PARIS));
        LocalDate test2 = new LocalDate(1970, 6, 9, StrictChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
    }

// org.joda.time.TestLocalDate_Basics::testEqualsHashCodeAPI
    public void testEqualsHashCodeAPI() {
        LocalDate test = new LocalDate(1970, 6, 9, COPTIC_PARIS);
        int expected = 157;
        expected = 23 * expected + 1970;
        expected = 23 * expected + COPTIC_UTC.year().getType().hashCode();
        expected = 23 * expected + 6;
        expected = 23 * expected + COPTIC_UTC.monthOfYear().getType().hashCode();
        expected = 23 * expected + 9;
        expected = 23 * expected + COPTIC_UTC.dayOfMonth().getType().hashCode();
        expected += COPTIC_UTC.hashCode();
        assertEquals(expected, test.hashCode());
    }

// org.joda.time.TestLocalDate_Basics::testCompareTo
    public void testCompareTo() {
        LocalDate test1 = new LocalDate(2005, 6, 2);
        LocalDate test1a = new LocalDate(2005, 6, 2);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        LocalDate test2 = new LocalDate(2005, 7, 2);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        LocalDate test3 = new LocalDate(2005, 7, 2, GregorianChronology.getInstanceUTC());
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
        assertEquals(0, test1.compareTo(new YearMonthDay(2005, 6, 2)));
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
            new LocalDate(1970, 6, 9).compareTo(partial);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testIsEqual_LocalDate
    public void testIsEqual_LocalDate() {
        LocalDate test1 = new LocalDate(2005, 6, 2);
        LocalDate test1a = new LocalDate(2005, 6, 2);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        LocalDate test2 = new LocalDate(2005, 7, 2);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        LocalDate test3 = new LocalDate(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new LocalDate(2005, 7, 2).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testIsBefore_LocalDate
    public void testIsBefore_LocalDate() {
        LocalDate test1 = new LocalDate(2005, 6, 2);
        LocalDate test1a = new LocalDate(2005, 6, 2);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        LocalDate test2 = new LocalDate(2005, 7, 2);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        LocalDate test3 = new LocalDate(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new LocalDate(2005, 7, 2).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testIsAfter_LocalDate
    public void testIsAfter_LocalDate() {
        LocalDate test1 = new LocalDate(2005, 6, 2);
        LocalDate test1a = new LocalDate(2005, 6, 2);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        LocalDate test2 = new LocalDate(2005, 7, 2);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        LocalDate test3 = new LocalDate(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new LocalDate(2005, 7, 2).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testWithField_DateTimeFieldType_int_1
    public void testWithField_DateTimeFieldType_int_1() {
        LocalDate test = new LocalDate(2004, 6, 9);
        LocalDate result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new LocalDate(2004, 6, 9), test);
        assertEquals(new LocalDate(2006, 6, 9), result);
    }

// org.joda.time.TestLocalDate_Basics::testWithField_DateTimeFieldType_int_2
    public void testWithField_DateTimeFieldType_int_2() {
        LocalDate test = new LocalDate(2004, 6, 9);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testWithField_DateTimeFieldType_int_3
    public void testWithField_DateTimeFieldType_int_3() {
        LocalDate test = new LocalDate(2004, 6, 9);
        try {
            test.withField(DateTimeFieldType.hourOfDay(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testWithField_DateTimeFieldType_int_4
    public void testWithField_DateTimeFieldType_int_4() {
        LocalDate test = new LocalDate(2004, 6, 9);
        LocalDate result = test.withField(DateTimeFieldType.year(), 2004);
        assertEquals(new LocalDate(2004, 6, 9), test);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testWithFieldAdded_DurationFieldType_int_1
    public void testWithFieldAdded_DurationFieldType_int_1() {
        LocalDate test = new LocalDate(2004, 6, 9);
        LocalDate result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new LocalDate(2004, 6, 9), test);
        assertEquals(new LocalDate(2010, 6, 9), result);
    }

// org.joda.time.TestLocalDate_Basics::testWithFieldAdded_DurationFieldType_int_2
    public void testWithFieldAdded_DurationFieldType_int_2() {
        LocalDate test = new LocalDate(2004, 6, 9);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testWithFieldAdded_DurationFieldType_int_3
    public void testWithFieldAdded_DurationFieldType_int_3() {
        LocalDate test = new LocalDate(2004, 6, 9);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testWithFieldAdded_DurationFieldType_int_4
    public void testWithFieldAdded_DurationFieldType_int_4() {
        LocalDate test = new LocalDate(2004, 6, 9);
        LocalDate result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testWithFieldAdded_DurationFieldType_int_5
    public void testWithFieldAdded_DurationFieldType_int_5() {
        LocalDate test = new LocalDate(2004, 6, 9);
        try {
            test.withFieldAdded(DurationFieldType.hours(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testPlus_RP
    public void testPlus_RP() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.plus(new Period(1, 2, 3, 4, 29, 6, 7, 8));
        LocalDate expected = new LocalDate(2003, 7, 28, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testPlusYears_int
    public void testPlusYears_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.plusYears(1);
        LocalDate expected = new LocalDate(2003, 5, 3, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.plusMonths(1);
        LocalDate expected = new LocalDate(2002, 6, 3, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testPlusWeeks_int
    public void testPlusWeeks_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.plusWeeks(1);
        LocalDate expected = new LocalDate(2002, 5, 10, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testPlusDays_int
    public void testPlusDays_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.plusDays(1);
        LocalDate expected = new LocalDate(2002, 5, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testMinus_RP
    public void testMinus_RP() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        
        
        
        
        LocalDate expected = new LocalDate(2001, 3, 26, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testMinusYears_int
    public void testMinusYears_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.minusYears(1);
        LocalDate expected = new LocalDate(2001, 5, 3, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.minusMonths(1);
        LocalDate expected = new LocalDate(2002, 4, 3, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testMinusWeeks_int
    public void testMinusWeeks_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.minusWeeks(1);
        LocalDate expected = new LocalDate(2002, 4, 26, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testMinusDays_int
    public void testMinusDays_int() {
        LocalDate test = new LocalDate(2002, 5, 3, BUDDHIST_LONDON);
        LocalDate result = test.minusDays(1);
        LocalDate expected = new LocalDate(2002, 5, 2, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDate_Basics::testGetters
    public void testGetters() {
        LocalDate test = new LocalDate(1970, 6, 9, GJ_UTC);
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(160, test.getDayOfYear());
        assertEquals(2, test.getDayOfWeek());
        assertEquals(24, test.getWeekOfWeekyear());
        assertEquals(1970, test.getWeekyear());
        assertEquals(70, test.getYearOfCentury());
        assertEquals(20, test.getCenturyOfEra());
        assertEquals(1970, test.getYearOfEra());
        assertEquals(DateTimeConstants.AD, test.getEra());
    }

// org.joda.time.TestLocalDate_Basics::testWithers
    public void testWithers() {
        LocalDate test = new LocalDate(1970, 6, 9, GJ_UTC);
        check(test.withYear(2000), 2000, 6, 9);
        check(test.withMonthOfYear(2), 1970, 2, 9);
        check(test.withDayOfMonth(2), 1970, 6, 2);
        check(test.withDayOfYear(6), 1970, 1, 6);
        check(test.withDayOfWeek(6), 1970, 6, 13);
        check(test.withWeekOfWeekyear(6), 1970, 2, 3);
        check(test.withWeekyear(1971), 1971, 6, 15);
        check(test.withYearOfCentury(60), 1960, 6, 9);
        check(test.withCenturyOfEra(21), 2070, 6, 9);
        check(test.withYearOfEra(1066), 1066, 6, 9);
        check(test.withEra(DateTimeConstants.BC), -1970, 6, 9);
        try {
            test.withMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtStartOfDay
    public void testToDateTimeAtStartOfDay() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtStartOfDay();
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtStartOfDay_avoidDST
    public void testToDateTimeAtStartOfDay_avoidDST() {
        LocalDate base = new LocalDate(2007, 4, 1);
        
        DateTimeZone.setDefault(MOCK_GAZA);
        DateTime test = base.toDateTimeAtStartOfDay();
        check(base, 2007, 4, 1);
        assertEquals(new DateTime(2007, 4, 1, 1, 0, 0, 0, MOCK_GAZA), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtStartOfDay_Zone
    public void testToDateTimeAtStartOfDay_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtStartOfDay(TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_TOKYO), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtStartOfDay_Zone_avoidDST
    public void testToDateTimeAtStartOfDay_Zone_avoidDST() {
        LocalDate base = new LocalDate(2007, 4, 1);
        
        DateTime test = base.toDateTimeAtStartOfDay(MOCK_GAZA);
        check(base, 2007, 4, 1);
        assertEquals(new DateTime(2007, 4, 1, 1, 0, 0, 0, MOCK_GAZA), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtStartOfDay_nullZone
    public void testToDateTimeAtStartOfDay_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtStartOfDay((DateTimeZone) null);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtMidnight
    public void testToDateTimeAtMidnight() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight();
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtMidnight_Zone
    public void testToDateTimeAtMidnight_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight(TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_TOKYO), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtMidnight_nullZone
    public void testToDateTimeAtMidnight_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight((DateTimeZone) null);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtCurrentTime
    public void testToDateTimeAtCurrentTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
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

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtCurrentTime_Zone
    public void testToDateTimeAtCurrentTime_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
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

// org.joda.time.TestLocalDate_Basics::testToDateTimeAtCurrentTime_nullZone
    public void testToDateTimeAtCurrentTime_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
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

// org.joda.time.TestLocalDate_Basics::testToLocalDateTime_LocalTime
    public void testToLocalDateTime_LocalTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, COPTIC_TOKYO);
        
        LocalDateTime test = base.toLocalDateTime(tod);
        check(base, 2005, 6, 9);
        LocalDateTime expected = new LocalDateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_UTC);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToLocalDateTime_nullLocalTime
    public void testToLocalDateTime_nullLocalTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        
        try {
            base.toLocalDateTime((LocalTime) null);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestLocalDate_Basics::testToLocalDateTime_wrongChronologyLocalTime
    public void testToLocalDateTime_wrongChronologyLocalTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, BUDDHIST_PARIS); 
        
        try {
            base.toLocalDateTime(tod);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_LocalTime
    public void testToDateTime_LocalTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, COPTIC_TOKYO);
        
        DateTime test = base.toDateTime(tod);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_nullLocalTime
    public void testToDateTime_nullLocalTime() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        long now = new DateTime(2004, 5, 8, 12, 13, 14, 15, COPTIC_LONDON).getMillis();
        DateTimeUtils.setCurrentMillisFixed(now);
        
        DateTime test = base.toDateTime((LocalTime) null);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_LocalTime_Zone
    public void testToDateTime_LocalTime_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, COPTIC_TOKYO);
        
        DateTime test = base.toDateTime(tod, TOKYO);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_LocalTime_nullZone
    public void testToDateTime_LocalTime_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, COPTIC_TOKYO);
        
        DateTime test = base.toDateTime(tod, null);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_nullLocalTime_Zone
    public void testToDateTime_nullLocalTime_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        long now = new DateTime(2004, 5, 8, 12, 13, 14, 15, COPTIC_TOKYO).getMillis();
        DateTimeUtils.setCurrentMillisFixed(now);
        
        DateTime test = base.toDateTime((LocalTime) null, TOKYO);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_wrongChronoLocalTime_Zone
    public void testToDateTime_wrongChronoLocalTime_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        LocalTime tod = new LocalTime(12, 13, 14, 15, BUDDHIST_TOKYO);
        
        try {
            base.toDateTime(tod, LONDON);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testToDateMidnight
    public void testToDateMidnight() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight();
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateMidnight_Zone
    public void testToDateMidnight_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight(TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_TOKYO), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateMidnight_nullZone
    public void testToDateMidnight_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight((DateTimeZone) null);
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_LONDON), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7);
        
        DateTime test = base.toDateTime(dt);
        check(base, 2005, 6, 9);
        DateTime expected = dt;
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        LocalDate base = new LocalDate(2005, 6, 9);
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

// org.joda.time.TestLocalDate_Basics::testToInterval
    public void testToInterval() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval();
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTimeAtStartOfDay();
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToInterval_Zone
    public void testToInterval_Zone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval(TOKYO);
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTimeAtStartOfDay(TOKYO);
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToInterval_Zone_noMidnight
    public void testToInterval_Zone_noMidnight() {
        LocalDate base = new LocalDate(2006, 4, 1, ISO_LONDON);  
        DateTimeZone gaza = DateTimeZone.forID("Asia/Gaza");
        Interval test = base.toInterval(gaza);
        check(base, 2006, 4, 1);
        DateTime start = new DateTime(2006, 4, 1, 1, 0, 0, 0, gaza);
        DateTime end = new DateTime(2006, 4, 2, 0, 0, 0, 0, gaza);
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToInterval_nullZone
    public void testToInterval_nullZone() {
        LocalDate base = new LocalDate(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval(null);
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTimeAtStartOfDay(LONDON);
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDate_Basics::testToDate_summer
    public void testToDate_summer() {
        LocalDate base = new LocalDate(2005, 7, 9, COPTIC_PARIS);
        
        Date test = base.toDate();
        check(base, 2005, 7, 9);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JULY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDate_winter
    public void testToDate_winter() {
        LocalDate base = new LocalDate(2005, 1, 9, COPTIC_PARIS);
        
        Date test = base.toDate();
        check(base, 2005, 1, 9);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JANUARY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDate_Basics::testToDate_springDST
    public void testToDate_springDST() {
        LocalDate base = new LocalDate(2007, 4, 2);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 4, 2);
            assertEquals("Mon Apr 02 01:00:00 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDate_Basics::testToDate_springDST_2Hour40Savings
    public void testToDate_springDST_2Hour40Savings() {
        LocalDate base = new LocalDate(2007, 4, 2);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000, (3600000 / 6) * 16);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 4, 2);
            assertEquals("Mon Apr 02 02:40:00 GMT+03:40 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDate_Basics::testToDate_autumnDST
    public void testToDate_autumnDST() {
        LocalDate base = new LocalDate(2007, 10, 2);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 10, 2);
            assertEquals("Tue Oct 02 00:00:00 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDate_Basics::testProperty
    public void testProperty() {
        LocalDate test = new LocalDate(2005, 6, 9, GJ_UTC);
        assertEquals(test.year(), test.property(DateTimeFieldType.year()));
        assertEquals(test.monthOfYear(), test.property(DateTimeFieldType.monthOfYear()));
        assertEquals(test.dayOfMonth(), test.property(DateTimeFieldType.dayOfMonth()));
        assertEquals(test.dayOfWeek(), test.property(DateTimeFieldType.dayOfWeek()));
        assertEquals(test.dayOfYear(), test.property(DateTimeFieldType.dayOfYear()));
        assertEquals(test.weekOfWeekyear(), test.property(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(test.weekyear(), test.property(DateTimeFieldType.weekyear()));
        assertEquals(test.yearOfCentury(), test.property(DateTimeFieldType.yearOfCentury()));
        assertEquals(test.yearOfEra(), test.property(DateTimeFieldType.yearOfEra()));
        assertEquals(test.centuryOfEra(), test.property(DateTimeFieldType.centuryOfEra()));
        assertEquals(test.era(), test.property(DateTimeFieldType.era()));
        try {
            test.property(DateTimeFieldType.millisOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Basics::testSerialization
    public void testSerialization() throws Exception {
        LocalDate test = new LocalDate(1972, 6, 9, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDate result = (LocalDate) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestLocalDate_Basics::testToString
    public void testToString() {
        LocalDate test = new LocalDate(2002, 6, 9);
        assertEquals("2002-06-09", test.toString());
    }

// org.joda.time.TestLocalDate_Basics::testToString_String
    public void testToString_String() {
        LocalDate test = new LocalDate(2002, 6, 9);
        assertEquals("2002 \ufffd\ufffd", test.toString("yyyy HH"));
        assertEquals("2002-06-09", test.toString((String) null));
    }

// org.joda.time.TestLocalDate_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        LocalDate test = new LocalDate(1970, 6, 9);
        assertEquals("Tue 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("mar. 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("1970-06-09", test.toString(null, Locale.ENGLISH));
        assertEquals("Tue 9/6", test.toString("EEE d/M", null));
        assertEquals("1970-06-09", test.toString(null, null));
    }

// org.joda.time.TestLocalDate_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        LocalDate test = new LocalDate(2002, 6, 9);
        assertEquals("2002 \ufffd\ufffd", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestLocalDate_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new LocalDate(2010, 6, 30), LocalDate.parse("2010-06-30"));
        assertEquals(new LocalDate(2010, 1, 2), LocalDate.parse("2010-002"));
    }

// org.joda.time.TestLocalDate_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--dd MM").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new LocalDate(2010, 6, 30), LocalDate.parse("2010--30 06", f));
    }

// org.joda.time.TestLocalDate_Constructors::testFactory_fromCalendarFields
    public void testFactory_fromCalendarFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(1970, 2, 3);
        assertEquals(expected, LocalDate.fromCalendarFields(cal));
    }

// org.joda.time.TestLocalDate_Constructors::testFactory_fromCalendarFields_beforeYearZero1
    public void testFactory_fromCalendarFields_beforeYearZero1() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(0, 2, 3);
        assertEquals(expected, LocalDate.fromCalendarFields(cal));
    }

// org.joda.time.TestLocalDate_Constructors::testFactory_fromCalendarFields_beforeYearZero3
    public void testFactory_fromCalendarFields_beforeYearZero3() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(3, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(-2, 2, 3);
        assertEquals(expected, LocalDate.fromCalendarFields(cal));
    }

// org.joda.time.TestLocalDate_Constructors::testFactory_fromCalendarFields_null
    public void testFactory_fromCalendarFields_null() throws Exception {
        try {
            LocalDate.fromCalendarFields((Calendar) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testFactory_fromDateFields_after1970
    public void testFactory_fromDateFields_after1970() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(1970, 2, 3);
        assertEquals(expected, LocalDate.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestLocalDate_Constructors::testFactory_fromDateFields_before1970
    public void testFactory_fromDateFields_before1970() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1969, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(1969, 2, 3);
        assertEquals(expected, LocalDate.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestLocalDate_Constructors::testFactory_fromDateFields_beforeYearZero1
    public void testFactory_fromDateFields_beforeYearZero1() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(0, 2, 3);
        assertEquals(expected, LocalDate.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestLocalDate_Constructors::testFactory_fromDateFields_beforeYearZero3
    public void testFactory_fromDateFields_beforeYearZero3() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(3, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(-2, 2, 3);
        assertEquals(expected, LocalDate.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestLocalDate_Constructors::testFactory_fromDateFields_null
    public void testFactory_fromDateFields_null() throws Exception {
        try {
            LocalDate.fromDateFields((Date) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        LocalDate test = new LocalDate();
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(test, LocalDate.now());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        LocalDate test = new LocalDate(LONDON);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(8, test.getDayOfMonth());
        assertEquals(test, LocalDate.now(LONDON));
        
        test = new LocalDate(PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(test, LocalDate.now(PARIS));
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        LocalDate test = new LocalDate((DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(8, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        LocalDate test = new LocalDate(GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(test, LocalDate.now(GREGORIAN_PARIS));
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        LocalDate test = new LocalDate((Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        LocalDate test = new LocalDate(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        LocalDate test = new LocalDate(TEST_TIME2);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_long1_DateTimeZone
    public void testConstructor_long1_DateTimeZone() throws Throwable {
        LocalDate test = new LocalDate(TEST_TIME1, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(TEST_TIME1_ROUNDED, test.getLocalMillis());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_long2_DateTimeZone
    public void testConstructor_long2_DateTimeZone() throws Throwable {
        LocalDate test = new LocalDate(TEST_TIME2, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_long3_DateTimeZone
    public void testConstructor_long3_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2006, 6, 9, 0, 0, 0, 0, PARIS);
        DateTime dtUTC = new DateTime(2006, 6, 9, 0, 0, 0, 0, DateTimeZone.UTC);
        
        LocalDate test = new LocalDate(dt.getMillis(), PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2006, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(dtUTC.getMillis(), test.getLocalMillis());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_long4_DateTimeZone
    public void testConstructor_long4_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2006, 6, 9, 23, 59, 59, 999, PARIS);
        DateTime dtUTC = new DateTime(2006, 6, 9, 0, 0, 0, 0, DateTimeZone.UTC);
        
        LocalDate test = new LocalDate(dt.getMillis(), PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2006, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(dtUTC.getMillis(), test.getLocalMillis());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_long_nullDateTimeZone
    public void testConstructor_long_nullDateTimeZone() throws Throwable {
        LocalDate test = new LocalDate(TEST_TIME1, (DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        LocalDate test = new LocalDate(TEST_TIME1, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        LocalDate test = new LocalDate(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        LocalDate test = new LocalDate(TEST_TIME1, (Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalDate test = new LocalDate(date);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        LocalDate test = new LocalDate((Object) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        LocalDate test = new LocalDate("1972-04-06");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectString2
    public void testConstructor_ObjectString2() throws Throwable {
        LocalDate test = new LocalDate("1972-037");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(2, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectString3
    public void testConstructor_ObjectString3() throws Throwable {
        LocalDate test = new LocalDate("1972-02");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(2, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new LocalDate("1970-04-06T+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new LocalDate("1970-04-06T10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectStringEx3
    public void testConstructor_ObjectStringEx3() throws Throwable {
        try {
            new LocalDate("1970-04-06T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectStringEx4
    public void testConstructor_ObjectStringEx4() throws Throwable {
        try {
            new LocalDate("T10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectStringEx5
    public void testConstructor_ObjectStringEx5() throws Throwable {
        try {
            new LocalDate("T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectStringEx6
    public void testConstructor_ObjectStringEx6() throws Throwable {
        try {
            new LocalDate("10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectStringEx7
    public void testConstructor_ObjectStringEx7() throws Throwable {
        try {
            new LocalDate("10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectLocalDate
    public void testConstructor_ObjectLocalDate() throws Throwable {
        LocalDate date = new LocalDate(1970, 4, 6, BUDDHIST_UTC);
        LocalDate test = new LocalDate(date);
        assertEquals(BUDDHIST_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectLocalTime
    public void testConstructor_ObjectLocalTime() throws Throwable {
        LocalTime time = new LocalTime(10, 20, 30, 40, BUDDHIST_UTC);
        try {
            new LocalDate(time);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectLocalDateTime
    public void testConstructor_ObjectLocalDateTime() throws Throwable {
        LocalDateTime dt = new LocalDateTime(1970, 5, 6, 10, 20, 30, 40, BUDDHIST_UTC);
        LocalDate test = new LocalDate(dt);
        assertEquals(BUDDHIST_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_ObjectYearMonthDay
    public void testConstructor_ObjectYearMonthDay() throws Throwable {
        YearMonthDay date = new YearMonthDay(1970, 4, 6, BUDDHIST_UTC);
        LocalDate test = new LocalDate(date);
        assertEquals(BUDDHIST_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_Object_DateTimeZone
    public void testConstructor_Object_DateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalDate test = new LocalDate(date, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_nullObject_DateTimeZone
    public void testConstructor_nullObject_DateTimeZone() throws Throwable {
        LocalDate test = new LocalDate((Object) null, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_Object_nullDateTimeZone
    public void testConstructor_Object_nullDateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalDate test = new LocalDate(date, (DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_nullObject_nullDateTimeZone
    public void testConstructor_nullObject_nullDateTimeZone() throws Throwable {
        LocalDate test = new LocalDate((Object) null, (DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalDate test = new LocalDate(date, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        LocalDate test = new LocalDate((Object) null, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalDate test = new LocalDate(date, (Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        LocalDate test = new LocalDate((Object) null, (Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_int_int_int
    public void testConstructor_int_int_int() throws Throwable {
        LocalDate test = new LocalDate(1970, 6, 9);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new LocalDate(Integer.MIN_VALUE, 6, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDate(Integer.MAX_VALUE, 6, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDate(1970, 0, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDate(1970, 13, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDate(1970, 6, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDate(1970, 6, 31);
            fail();
        } catch (IllegalArgumentException ex) {}
        new LocalDate(1970, 7, 31);
        try {
            new LocalDate(1970, 7, 32);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_int_int_int_Chronology
    public void testConstructor_int_int_int_Chronology() throws Throwable {
        LocalDate test = new LocalDate(1970, 6, 9, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new LocalDate(Integer.MIN_VALUE, 6, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDate(Integer.MAX_VALUE, 6, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDate(1970, 0, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDate(1970, 13, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDate(1970, 6, 0, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDate(1970, 6, 31, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        new LocalDate(1970, 7, 31, GREGORIAN_PARIS);
        try {
            new LocalDate(1970, 7, 32, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDate_Constructors::testConstructor_int_int_int_nullChronology
    public void testConstructor_int_int_int_nullChronology() throws Throwable {
        LocalDate test = new LocalDate(1970, 6, 9, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDate_Properties::testPropertyGetYear
    public void testPropertyGetYear() {
        LocalDate test = new LocalDate(1972, 6, 9);
        assertSame(test.getChronology().year(), test.year().getField());
        assertEquals("year", test.year().getName());
        assertEquals("Property[year]", test.year().toString());
        assertSame(test, test.year().getLocalDate());
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

// org.joda.time.TestLocalDate_Properties::testPropertyGetMaxMinValuesYear
    public void testPropertyGetMaxMinValuesYear() {
        LocalDate test = new LocalDate(1972, 6, 9);
        assertEquals(-292275054, test.year().getMinimumValue());
        assertEquals(-292275054, test.year().getMinimumValueOverall());
        assertEquals(292278993, test.year().getMaximumValue());
        assertEquals(292278993, test.year().getMaximumValueOverall());
    }

// org.joda.time.TestLocalDate_Properties::testPropertyAddToCopyYear
    public void testPropertyAddToCopyYear() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.year().addToCopy(9);
        check(test, 1972, 6, 9);
        check(copy, 1981, 6, 9);
        
        copy = test.year().addToCopy(0);
        check(copy, 1972, 6, 9);
        
        copy = test.year().addToCopy(292278993 - 1972);
        check(copy, 292278993, 6, 9);
        
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

// org.joda.time.TestLocalDate_Properties::testPropertyAddWrapFieldToCopyYear
    public void testPropertyAddWrapFieldToCopyYear() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.year().addWrapFieldToCopy(9);
        check(test, 1972, 6, 9);
        check(copy, 1981, 6, 9);
        
        copy = test.year().addWrapFieldToCopy(0);
        check(copy, 1972, 6, 9);
        
        copy = test.year().addWrapFieldToCopy(292278993 - 1972 + 1);
        check(copy, -292275054, 6, 9);
        
        copy = test.year().addWrapFieldToCopy(-292275054 - 1972 - 1);
        check(copy, 292278993, 6, 9);
    }

// org.joda.time.TestLocalDate_Properties::testPropertySetCopyYear
    public void testPropertySetCopyYear() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.year().setCopy(12);
        check(test, 1972, 6, 9);
        check(copy, 12, 6, 9);
    }

// org.joda.time.TestLocalDate_Properties::testPropertySetCopyTextYear
    public void testPropertySetCopyTextYear() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.year().setCopy("12");
        check(test, 1972, 6, 9);
        check(copy, 12, 6, 9);
    }

// org.joda.time.TestLocalDate_Properties::testPropertyCompareToYear
    public void testPropertyCompareToYear() {
        LocalDate test1 = new LocalDate(TEST_TIME1);
        LocalDate test2 = new LocalDate(TEST_TIME2);
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

// org.joda.time.TestLocalDate_Properties::testPropertyGetMonth
    public void testPropertyGetMonth() {
        LocalDate test = new LocalDate(1972, 6, 9);
        assertSame(test.getChronology().monthOfYear(), test.monthOfYear().getField());
        assertEquals("monthOfYear", test.monthOfYear().getName());
        assertEquals("Property[monthOfYear]", test.monthOfYear().toString());
        assertSame(test, test.monthOfYear().getLocalDate());
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
        test = new LocalDate(1972, 7, 9);
        assertEquals("juillet", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("juil.", test.monthOfYear().getAsShortText(Locale.FRENCH));
    }

// org.joda.time.TestLocalDate_Properties::testPropertyGetMaxMinValuesMonth
    public void testPropertyGetMaxMinValuesMonth() {
        LocalDate test = new LocalDate(1972, 6, 9);
        assertEquals(1, test.monthOfYear().getMinimumValue());
        assertEquals(1, test.monthOfYear().getMinimumValueOverall());
        assertEquals(12, test.monthOfYear().getMaximumValue());
        assertEquals(12, test.monthOfYear().getMaximumValueOverall());
    }

// org.joda.time.TestLocalDate_Properties::testPropertyAddToCopyMonth
    public void testPropertyAddToCopyMonth() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.monthOfYear().addToCopy(6);
        check(test, 1972, 6, 9);
        check(copy, 1972, 12, 9);
        
        copy = test.monthOfYear().addToCopy(7);
        check(copy, 1973, 1, 9);
        
        copy = test.monthOfYear().addToCopy(-5);
        check(copy, 1972, 1, 9);
        
        copy = test.monthOfYear().addToCopy(-6);
        check(copy, 1971, 12, 9);
        
        test = new LocalDate(1972, 1, 31);
        copy = test.monthOfYear().addToCopy(1);
        check(copy, 1972, 2, 29);
        
        copy = test.monthOfYear().addToCopy(2);
        check(copy, 1972, 3, 31);
        
        copy = test.monthOfYear().addToCopy(3);
        check(copy, 1972, 4, 30);
        
        test = new LocalDate(1971, 1, 31);
        copy = test.monthOfYear().addToCopy(1);
        check(copy, 1971, 2, 28);
    }

// org.joda.time.TestLocalDate_Properties::testPropertyAddWrapFieldToCopyMonth
    public void testPropertyAddWrapFieldToCopyMonth() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.monthOfYear().addWrapFieldToCopy(4);
        check(test, 1972, 6, 9);
        check(copy, 1972, 10, 9);
        
        copy = test.monthOfYear().addWrapFieldToCopy(8);
        check(copy, 1972, 2, 9);
        
        copy = test.monthOfYear().addWrapFieldToCopy(-8);
        check(copy, 1972, 10, 9);
        
        test = new LocalDate(1972, 1, 31);
        copy = test.monthOfYear().addWrapFieldToCopy(1);
        check(copy, 1972, 2, 29);
        
        copy = test.monthOfYear().addWrapFieldToCopy(2);
        check(copy, 1972, 3, 31);
        
        copy = test.monthOfYear().addWrapFieldToCopy(3);
        check(copy, 1972, 4, 30);
        
        test = new LocalDate(1971, 1, 31);
        copy = test.monthOfYear().addWrapFieldToCopy(1);
        check(copy, 1971, 2, 28);
    }

// org.joda.time.TestLocalDate_Properties::testPropertySetCopyMonth
    public void testPropertySetCopyMonth() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.monthOfYear().setCopy(12);
        check(test, 1972, 6, 9);
        check(copy, 1972, 12, 9);
        
        test = new LocalDate(1972, 1, 31);
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

// org.joda.time.TestLocalDate_Properties::testPropertySetCopyTextMonth
    public void testPropertySetCopyTextMonth() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.monthOfYear().setCopy("12");
        check(test, 1972, 6, 9);
        check(copy, 1972, 12, 9);
        
        copy = test.monthOfYear().setCopy("December");
        check(test, 1972, 6, 9);
        check(copy, 1972, 12, 9);
        
        copy = test.monthOfYear().setCopy("Dec");
        check(test, 1972, 6, 9);
        check(copy, 1972, 12, 9);
    }

// org.joda.time.TestLocalDate_Properties::testPropertyCompareToMonth
    public void testPropertyCompareToMonth() {
        LocalDate test1 = new LocalDate(TEST_TIME1);
        LocalDate test2 = new LocalDate(TEST_TIME2);
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

// org.joda.time.TestLocalDate_Properties::testPropertyGetDay
    public void testPropertyGetDay() {
        LocalDate test = new LocalDate(1972, 6, 9);
        assertSame(test.getChronology().dayOfMonth(), test.dayOfMonth().getField());
        assertEquals("dayOfMonth", test.dayOfMonth().getName());
        assertEquals("Property[dayOfMonth]", test.dayOfMonth().toString());
        assertSame(test, test.dayOfMonth().getLocalDate());
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

// org.joda.time.TestLocalDate_Properties::testPropertyGetMaxMinValuesDay
    public void testPropertyGetMaxMinValuesDay() {
        LocalDate test = new LocalDate(1972, 6, 9);
        assertEquals(1, test.dayOfMonth().getMinimumValue());
        assertEquals(1, test.dayOfMonth().getMinimumValueOverall());
        assertEquals(30, test.dayOfMonth().getMaximumValue());
        assertEquals(31, test.dayOfMonth().getMaximumValueOverall());
        test = new LocalDate(1972, 7, 9);
        assertEquals(31, test.dayOfMonth().getMaximumValue());
        test = new LocalDate(1972, 2, 9);
        assertEquals(29, test.dayOfMonth().getMaximumValue());
        test = new LocalDate(1971, 2, 9);
        assertEquals(28, test.dayOfMonth().getMaximumValue());
    }

// org.joda.time.TestLocalDate_Properties::testPropertyAddToCopyDay
    public void testPropertyAddToCopyDay() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.dayOfMonth().addToCopy(9);
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

// org.joda.time.TestLocalDate_Properties::testPropertyAddWrapFieldToCopyDay
    public void testPropertyAddWrapFieldToCopyDay() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.dayOfMonth().addWrapFieldToCopy(21);
        check(test, 1972, 6, 9);
        check(copy, 1972, 6, 30);
        
        copy = test.dayOfMonth().addWrapFieldToCopy(22);
        check(copy, 1972, 6, 1);
        
        copy = test.dayOfMonth().addWrapFieldToCopy(-12);
        check(copy, 1972, 6, 27);
        
        test = new LocalDate(1972, 7, 9);
        copy = test.dayOfMonth().addWrapFieldToCopy(21);
        check(copy, 1972, 7, 30);
    
        copy = test.dayOfMonth().addWrapFieldToCopy(22);
        check(copy, 1972, 7, 31);
    
        copy = test.dayOfMonth().addWrapFieldToCopy(23);
        check(copy, 1972, 7, 1);
    
        copy = test.dayOfMonth().addWrapFieldToCopy(-12);
        check(copy, 1972, 7, 28);
    }

// org.joda.time.TestLocalDate_Properties::testPropertySetCopyDay
    public void testPropertySetCopyDay() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.dayOfMonth().setCopy(12);
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

// org.joda.time.TestLocalDate_Properties::testPropertySetCopyTextDay
    public void testPropertySetCopyTextDay() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.dayOfMonth().setCopy("12");
        check(test, 1972, 6, 9);
        check(copy, 1972, 6, 12);
    }

// org.joda.time.TestLocalDate_Properties::testPropertyWithMaximumValueDayOfMonth
    public void testPropertyWithMaximumValueDayOfMonth() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.dayOfMonth().withMaximumValue();
        check(test, 1972, 6, 9);
        check(copy, 1972, 6, 30);
    }

// org.joda.time.TestLocalDate_Properties::testPropertyWithMinimumValueDayOfMonth
    public void testPropertyWithMinimumValueDayOfMonth() {
        LocalDate test = new LocalDate(1972, 6, 9);
        LocalDate copy = test.dayOfMonth().withMinimumValue();
        check(test, 1972, 6, 9);
        check(copy, 1972, 6, 1);
    }

// org.joda.time.TestLocalDate_Properties::testPropertyCompareToDay
    public void testPropertyCompareToDay() {
        LocalDate test1 = new LocalDate(TEST_TIME1);
        LocalDate test2 = new LocalDate(TEST_TIME2);
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

// org.joda.time.TestLocalDate_Properties::testPropertyEquals
    public void testPropertyEquals() {
        LocalDate test1 = new LocalDate(2005, 11, 8);
        LocalDate test2 = new LocalDate(2005, 11, 9);
        LocalDate test3 = new LocalDate(2005, 11, 8, CopticChronology.getInstanceUTC());
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

// org.joda.time.TestLocalDate_Properties::testPropertyHashCode
    public void testPropertyHashCode() {
        LocalDate test1 = new LocalDate(2005, 11, 8);
        LocalDate test2 = new LocalDate(2005, 11, 9);
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(false, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.monthOfYear().hashCode() == test1.monthOfYear().hashCode());
        assertEquals(true, test1.monthOfYear().hashCode() == test2.monthOfYear().hashCode());
    }

// org.joda.time.TestLocalDate_Properties::testPropertyEqualsHashCodeLenient
    public void testPropertyEqualsHashCodeLenient() {
        LocalDate test1 = new LocalDate(1970, 6, 9, LenientChronology.getInstance(COPTIC_PARIS));
        LocalDate test2 = new LocalDate(1970, 6, 9, LenientChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(true, test2.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
    }

// org.joda.time.TestLocalDate_Properties::testPropertyEqualsHashCodeStrict
    public void testPropertyEqualsHashCodeStrict() {
        LocalDate test1 = new LocalDate(1970, 6, 9, StrictChronology.getInstance(COPTIC_PARIS));
        LocalDate test2 = new LocalDate(1970, 6, 9, StrictChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(true, test2.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
    }

// org.joda.time.TestLocalTime_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(10, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(30, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(40, test.get(DateTimeFieldType.millisOfSecond()));
        assertEquals(TEST_TIME_NOW / 60000 , test.get(DateTimeFieldType.minuteOfDay()));
        assertEquals(TEST_TIME_NOW / 1000 , test.get(DateTimeFieldType.secondOfDay()));
        assertEquals(TEST_TIME_NOW , test.get(DateTimeFieldType.millisOfDay()));
        assertEquals(10, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(DateTimeConstants.AM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalTime(12, 30);
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.PM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalTime(14, 30);
        assertEquals(2, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(2, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(14, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.PM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalTime(0, 30);
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(24, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.AM, test.get(DateTimeFieldType.halfdayOfDay()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.dayOfMonth());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testSize
    public void testSize() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(4, test.size());
    }

// org.joda.time.TestLocalTime_Basics::testGetFieldType_int
    public void testGetFieldType_int() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
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

// org.joda.time.TestLocalTime_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.hourOfDay(), fields[0]);
        assertSame(DateTimeFieldType.minuteOfHour(), fields[1]);
        assertSame(DateTimeFieldType.secondOfMinute(), fields[2]);
        assertSame(DateTimeFieldType.millisOfSecond(), fields[3]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestLocalTime_Basics::testGetField_int
    public void testGetField_int() {
        LocalTime test = new LocalTime(10, 20, 30, 40, COPTIC_UTC);
        assertSame(COPTIC_UTC.hourOfDay(), test.getField(0));
        assertSame(COPTIC_UTC.minuteOfHour(), test.getField(1));
        assertSame(COPTIC_UTC.secondOfMinute(), test.getField(2));
        assertSame(COPTIC_UTC.millisOfSecond(), test.getField(3));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(5);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testGetFields
    public void testGetFields() {
        LocalTime test = new LocalTime(10, 20, 30, 40, COPTIC_UTC);
        DateTimeField[] fields = test.getFields();
        assertSame(COPTIC_UTC.hourOfDay(), fields[0]);
        assertSame(COPTIC_UTC.minuteOfHour(), fields[1]);
        assertSame(COPTIC_UTC.secondOfMinute(), fields[2]);
        assertSame(COPTIC_UTC.millisOfSecond(), fields[3]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestLocalTime_Basics::testGetValue_int
    public void testGetValue_int() {
        LocalTime test = new LocalTime(10, 20, 30, 40, COPTIC_PARIS);
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

// org.joda.time.TestLocalTime_Basics::testGetValues
    public void testGetValues() {
        LocalTime test = new LocalTime(10, 20, 30, 40, COPTIC_UTC);
        int[] values = test.getValues();
        assertEquals(10, values[0]);
        assertEquals(20, values[1]);
        assertEquals(30, values[2]);
        assertEquals(40, values[3]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestLocalTime_Basics::testIsSupported_DateTimeFieldType
    public void testIsSupported_DateTimeFieldType() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfMinute()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfSecond()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfDay()));
        
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfHalfday()));
        assertEquals(true, test.isSupported(DateTimeFieldType.halfdayOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(true, test.isSupported(DateTimeFieldType.clockhourOfDay()));
        
        assertEquals(false, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(false, test.isSupported((DateTimeFieldType) null));
        
        DateTimeFieldType d = new DateTimeFieldType("hours") {
            public DurationFieldType getDurationType() {
                return DurationFieldType.hours();
            }
            public DurationFieldType getRangeDurationType() {
                return null;
            }
            public DateTimeField getField(Chronology chronology) {
                return chronology.hourOfDay();
            }
        };
        assertEquals(false, test.isSupported(d));
        
        d = new DateTimeFieldType("hourOfYear") {
            public DurationFieldType getDurationType() {
                return DurationFieldType.hours();
            }
            public DurationFieldType getRangeDurationType() {
                return DurationFieldType.years();
            }
            public DateTimeField getField(Chronology chronology) {
                return chronology.hourOfDay();
            }
        };
        assertEquals(false, test.isSupported(d));
    }

// org.joda.time.TestLocalTime_Basics::testIsSupported_DurationFieldType
    public void testIsSupported_DurationFieldType() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(true, test.isSupported(DurationFieldType.hours()));
        assertEquals(true, test.isSupported(DurationFieldType.minutes()));
        assertEquals(true, test.isSupported(DurationFieldType.seconds()));
        assertEquals(true, test.isSupported(DurationFieldType.millis()));
        assertEquals(true, test.isSupported(DurationFieldType.halfdays()));
        
        assertEquals(false, test.isSupported(DurationFieldType.days()));
        assertEquals(false, test.isSupported((DurationFieldType) null));
    }

// org.joda.time.TestLocalTime_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        LocalTime test1 = new LocalTime(10, 20, 30, 40, COPTIC_PARIS);
        LocalTime test2 = new LocalTime(10, 20, 30, 40, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        LocalTime test3 = new LocalTime(15, 20, 30, 40);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new TimeOfDay(10, 20, 30, 40, COPTIC_UTC)));
        assertEquals(true, test1.hashCode() == new TimeOfDay(10, 20, 30, 40, COPTIC_UTC).hashCode());
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestLocalTime_Basics::testCompareTo
    public void testCompareTo() {
        LocalTime test1 = new LocalTime(10, 20, 30, 40);
        LocalTime test1a = new LocalTime(10, 20, 30, 40);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        LocalTime test2 = new LocalTime(10, 20, 35, 40);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        LocalTime test3 = new LocalTime(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
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
        assertEquals(0, test1.compareTo(new TimeOfDay(10, 20, 30, 40)));
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

    }

// org.joda.time.TestLocalTime_Basics::testIsEqual_LocalTime
    public void testIsEqual_LocalTime() {
        LocalTime test1 = new LocalTime(10, 20, 30, 40);
        LocalTime test1a = new LocalTime(10, 20, 30, 40);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        LocalTime test2 = new LocalTime(10, 20, 35, 40);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        LocalTime test3 = new LocalTime(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new LocalTime(10, 20, 35, 40).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testIsBefore_LocalTime
    public void testIsBefore_LocalTime() {
        LocalTime test1 = new LocalTime(10, 20, 30, 40);
        LocalTime test1a = new LocalTime(10, 20, 30, 40);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        LocalTime test2 = new LocalTime(10, 20, 35, 40);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        LocalTime test3 = new LocalTime(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new LocalTime(10, 20, 35, 40).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testIsAfter_LocalTime
    public void testIsAfter_LocalTime() {
        LocalTime test1 = new LocalTime(10, 20, 30, 40);
        LocalTime test1a = new LocalTime(10, 20, 30, 40);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        LocalTime test2 = new LocalTime(10, 20, 35, 40);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        LocalTime test3 = new LocalTime(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new LocalTime(10, 20, 35, 40).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithField_DateTimeFieldType_int_1
    public void testWithField_DateTimeFieldType_int_1() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        LocalTime result = test.withField(DateTimeFieldType.hourOfDay(), 15);
        
        assertEquals(new LocalTime(10, 20, 30, 40), test);
        assertEquals(new LocalTime(15, 20, 30, 40), result);
    }

// org.joda.time.TestLocalTime_Basics::testWithField_DateTimeFieldType_int_2
    public void testWithField_DateTimeFieldType_int_2() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithField_DateTimeFieldType_int_3
    public void testWithField_DateTimeFieldType_int_3() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        try {
            test.withField(DateTimeFieldType.dayOfMonth(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithField_DateTimeFieldType_int_4
    public void testWithField_DateTimeFieldType_int_4() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        LocalTime result = test.withField(DateTimeFieldType.hourOfDay(), 10);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_1
    public void testWithFieldAdded_DurationFieldType_int_1() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        LocalTime result = test.withFieldAdded(DurationFieldType.hours(), 6);
        
        assertEquals(new LocalTime(10, 20, 30, 40), test);
        assertEquals(new LocalTime(16, 20, 30, 40), result);
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_2
    public void testWithFieldAdded_DurationFieldType_int_2() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_3
    public void testWithFieldAdded_DurationFieldType_int_3() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_4
    public void testWithFieldAdded_DurationFieldType_int_4() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        LocalTime result = test.withFieldAdded(DurationFieldType.hours(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_5
    public void testWithFieldAdded_DurationFieldType_int_5() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        try {
            test.withFieldAdded(DurationFieldType.days(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_6
    public void testWithFieldAdded_DurationFieldType_int_6() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        LocalTime result = test.withFieldAdded(DurationFieldType.hours(), 16);
        
        assertEquals(new LocalTime(10, 20, 30, 40), test);
        assertEquals(new LocalTime(2, 20, 30, 40), result);
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_7
    public void testWithFieldAdded_DurationFieldType_int_7() {
        LocalTime test = new LocalTime(23, 59, 59, 999);
        LocalTime result = test.withFieldAdded(DurationFieldType.millis(), 1);
        assertEquals(new LocalTime(0, 0, 0, 0), result);
        
        test = new LocalTime(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.seconds(), 1);
        assertEquals(new LocalTime(0, 0, 0, 999), result);
        
        test = new LocalTime(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.minutes(), 1);
        assertEquals(new LocalTime(0, 0, 59, 999), result);
        
        test = new LocalTime(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.hours(), 1);
        assertEquals(new LocalTime(0, 59, 59, 999), result);
    }

// org.joda.time.TestLocalTime_Basics::testWithFieldAdded_DurationFieldType_int_8
    public void testWithFieldAdded_DurationFieldType_int_8() {
        LocalTime test = new LocalTime(0, 0, 0, 0);
        LocalTime result = test.withFieldAdded(DurationFieldType.millis(), -1);
        assertEquals(new LocalTime(23, 59, 59, 999), result);
        
        test = new LocalTime(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.seconds(), -1);
        assertEquals(new LocalTime(23, 59, 59, 0), result);
        
        test = new LocalTime(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.minutes(), -1);
        assertEquals(new LocalTime(23, 59, 0, 0), result);
        
        test = new LocalTime(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.hours(), -1);
        assertEquals(new LocalTime(23, 0, 0, 0), result);
    }

// org.joda.time.TestLocalTime_Basics::testPlus_RP
    public void testPlus_RP() {
        LocalTime test = new LocalTime(10, 20, 30, 40, BUDDHIST_LONDON);
        LocalTime result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        LocalTime expected = new LocalTime(15, 26, 37, 48, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testPlusHours_int
    public void testPlusHours_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.plusHours(1);
        LocalTime expected = new LocalTime(2, 2, 3, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testPlusMinutes_int
    public void testPlusMinutes_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.plusMinutes(1);
        LocalTime expected = new LocalTime(1, 3, 3, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testPlusSeconds_int
    public void testPlusSeconds_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.plusSeconds(1);
        LocalTime expected = new LocalTime(1, 2, 4, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testPlusMillis_int
    public void testPlusMillis_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.plusMillis(1);
        LocalTime expected = new LocalTime(1, 2, 3, 5, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testMinus_RP
    public void testMinus_RP() {
        LocalTime test = new LocalTime(10, 20, 30, 40, BUDDHIST_LONDON);
        LocalTime result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        LocalTime expected = new LocalTime(9, 19, 29, 39, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testMinusHours_int
    public void testMinusHours_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.minusHours(1);
        LocalTime expected = new LocalTime(0, 2, 3, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testMinusMinutes_int
    public void testMinusMinutes_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.minusMinutes(1);
        LocalTime expected = new LocalTime(1, 1, 3, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testMinusSeconds_int
    public void testMinusSeconds_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.minusSeconds(1);
        LocalTime expected = new LocalTime(1, 2, 2, 4, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testMinusMillis_int
    public void testMinusMillis_int() {
        LocalTime test = new LocalTime(1, 2, 3, 4, BUDDHIST_LONDON);
        LocalTime result = test.minusMillis(1);
        LocalTime expected = new LocalTime(1, 2, 3, 3, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalTime_Basics::testGetters
    public void testGetters() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(TEST_TIME_NOW, test.getMillisOfDay());
    }

// org.joda.time.TestLocalTime_Basics::testWithers
    public void testWithers() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        check(test.withHourOfDay(6), 6, 20, 30, 40);
        check(test.withMinuteOfHour(6), 10, 6, 30, 40);
        check(test.withSecondOfMinute(6), 10, 20, 6, 40);
        check(test.withMillisOfSecond(6), 10, 20, 30, 6);
        check(test.withMillisOfDay(61234), 0, 1, 1, 234);
        try {
            test.withHourOfDay(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withHourOfDay(24);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testToDateTimeTodayDefaultZone
    public void testToDateTimeTodayDefaultZone() {
        LocalTime base = new LocalTime(10, 20, 30, 40, COPTIC_PARIS); 
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

// org.joda.time.TestLocalTime_Basics::testToDateTimeToday_Zone
    public void testToDateTimeToday_Zone() {
        LocalTime base = new LocalTime(10, 20, 30, 40, COPTIC_PARIS); 
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

// org.joda.time.TestLocalTime_Basics::testToDateTimeToday_nullZone
    public void testToDateTimeToday_nullZone() {
        LocalTime base = new LocalTime(10, 20, 30, 40, COPTIC_PARIS); 
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

// org.joda.time.TestLocalTime_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        LocalTime base = new LocalTime(10, 20, 30, 40, COPTIC_PARIS);
        DateTime dt = new DateTime(0L); 
        assertEquals("1970-01-01T01:00:00.000+01:00", dt.toString());
        
        DateTime test = base.toDateTime(dt);
        check(base, 10, 20, 30, 40);
        assertEquals("1970-01-01T01:00:00.000+01:00", dt.toString());
        assertEquals("1970-01-01T10:20:30.040+01:00", test.toString());
    }

// org.joda.time.TestLocalTime_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        LocalTime base = new LocalTime(1, 2, 3, 4);
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2);
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 1, 2, 3, 4);
        assertEquals("1970-01-02T01:02:03.004+01:00", test.toString());
    }

// org.joda.time.TestLocalTime_Basics::testProperty
    public void testProperty() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(test.hourOfDay(), test.property(DateTimeFieldType.hourOfDay()));
        assertEquals(test.minuteOfHour(), test.property(DateTimeFieldType.minuteOfHour()));
        assertEquals(test.secondOfMinute(), test.property(DateTimeFieldType.secondOfMinute()));
        assertEquals(test.millisOfSecond(), test.property(DateTimeFieldType.millisOfSecond()));
        assertEquals(test.millisOfDay(), test.property(DateTimeFieldType.millisOfDay()));
        
        assertEquals(test, test.property(DateTimeFieldType.minuteOfDay()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.secondOfDay()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.millisOfDay()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.hourOfHalfday()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.halfdayOfDay()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.clockhourOfHalfday()).getLocalTime());
        assertEquals(test, test.property(DateTimeFieldType.clockhourOfDay()).getLocalTime());
        
        try {
            test.property(DateTimeFieldType.dayOfWeek());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Basics::testSerialization
    public void testSerialization() throws Exception {
        LocalTime test = new LocalTime(10, 20, 30, 40, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalTime result = (LocalTime) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestLocalTime_Basics::testToString
    public void testToString() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals("10:20:30.040", test.toString());
    }

// org.joda.time.TestLocalTime_Basics::testToString_String
    public void testToString_String() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString("yyyy HH"));
        assertEquals("10:20:30.040", test.toString((String) null));
    }

// org.joda.time.TestLocalTime_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals("10 20", test.toString("H m", Locale.ENGLISH));
        assertEquals("10:20:30.040", test.toString(null, Locale.ENGLISH));
        assertEquals("10 20", test.toString("H m", null));
        assertEquals("10:20:30.040", test.toString(null, null));
    }

// org.joda.time.TestLocalTime_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("10:20:30.040", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestLocalTime_Constructors::testConstantMidnight
    public void testConstantMidnight() throws Throwable {
        LocalTime test = LocalTime.MIDNIGHT;
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new LocalTime(1, 20), LocalTime.parse("01:20"));
        assertEquals(new LocalTime(14, 50, 30, 432), LocalTime.parse("14:50:30.432"));
    }

// org.joda.time.TestLocalTime_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("HH mm").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new LocalTime(13, 30), LocalTime.parse("13 30", f));
    }

// org.joda.time.TestLocalTime_Constructors::testFactory_FromCalendarFields_Calendar
    public void testFactory_FromCalendarFields_Calendar() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalTime expected = new LocalTime(4, 5, 6, 7);
        assertEquals(expected, LocalTime.fromCalendarFields(cal));
        try {
            LocalTime.fromCalendarFields((Calendar) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testFactory_FromDateFields_after1970
    public void testFactory_FromDateFields_after1970() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalTime expected = new LocalTime(4, 5, 6, 7);
        assertEquals(expected, LocalTime.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestLocalTime_Constructors::testFactory_FromDateFields_before1970
    public void testFactory_FromDateFields_before1970() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1969, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalTime expected = new LocalTime(4, 5, 6, 7);
        assertEquals(expected, LocalTime.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestLocalTime_Constructors::testFactory_FromDateFields_null
    public void testFactory_FromDateFields_null() throws Exception {
        try {
            LocalTime.fromDateFields((Date) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testFactoryMillisOfDay_long
    public void testFactoryMillisOfDay_long() throws Throwable {
        LocalTime test = LocalTime.fromMillisOfDay(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testFactoryMillisOfDay_long_Chronology
    public void testFactoryMillisOfDay_long_Chronology() throws Throwable {
        LocalTime test = LocalTime.fromMillisOfDay(TEST_TIME1, JULIAN_LONDON);
        assertEquals(JULIAN_UTC, test.getChronology());
        assertEquals(1, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testFactoryMillisOfDay_long_nullChronology
    public void testFactoryMillisOfDay_long_nullChronology() throws Throwable {
        LocalTime test = LocalTime.fromMillisOfDay(TEST_TIME1, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        LocalTime test = new LocalTime();
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(test, LocalTime.now());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 30, 40, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        LocalTime test = new LocalTime(LONDON);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(23, test.getHourOfDay());
        assertEquals(59, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(test, LocalTime.now(LONDON));
        
        test = new LocalTime(PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(0, test.getHourOfDay());
        assertEquals(59, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(test, LocalTime.now(PARIS));
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 30, 40, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        LocalTime test = new LocalTime((DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(23, test.getHourOfDay());
        assertEquals(59, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        LocalTime test = new LocalTime(JULIAN_LONDON);
        assertEquals(JULIAN_UTC, test.getChronology());
        assertEquals(10 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(test, LocalTime.now(JULIAN_LONDON));
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        LocalTime test = new LocalTime((Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        LocalTime test = new LocalTime(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        LocalTime test = new LocalTime(TEST_TIME2);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(5 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(6, test.getMinuteOfHour());
        assertEquals(7, test.getSecondOfMinute());
        assertEquals(8, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_long_DateTimeZone
    public void testConstructor_long_DateTimeZone() throws Throwable {
        LocalTime test = new LocalTime(TEST_TIME1, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_long_DateTimeZone_2
    public void testConstructor_long_DateTimeZone_2() throws Throwable {
        DateTime dt = new DateTime(2007, 6, 9, 1, 2, 3, 4, PARIS);
        DateTime dtUTC = new DateTime(1970, 1, 1, 1, 2, 3, 4, DateTimeZone.UTC);
        
        LocalTime test = new LocalTime(dt.getMillis(), PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
        assertEquals(dtUTC.getMillis(), test.getLocalMillis());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_long_nullDateTimeZone
    public void testConstructor_long_nullDateTimeZone() throws Throwable {
        LocalTime test = new LocalTime(TEST_TIME1, (DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        LocalTime test = new LocalTime(TEST_TIME1, JULIAN_PARIS);
        assertEquals(JULIAN_UTC, test.getChronology());
        assertEquals(1 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        LocalTime test = new LocalTime(TEST_TIME2, JULIAN_LONDON);
        assertEquals(JULIAN_UTC, test.getChronology());
        assertEquals(5 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(6, test.getMinuteOfHour());
        assertEquals(7, test.getSecondOfMinute());
        assertEquals(8, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        LocalTime test = new LocalTime(TEST_TIME1, (Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalTime test = new LocalTime(date);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date(TEST_TIME1));
        LocalTime test = new LocalTime(cal);
        assertEquals(GJChronology.getInstanceUTC(), test.getChronology());
        assertEquals(1 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        LocalTime test = new LocalTime((Object) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        LocalTime test = new LocalTime("10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectString1Tokyo
    public void testConstructor_ObjectString1Tokyo() throws Throwable {
        DateTimeZone.setDefault(TOKYO);
        LocalTime test = new LocalTime("10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectString1NewYork
    public void testConstructor_ObjectString1NewYork() throws Throwable {
        DateTimeZone.setDefault(NEW_YORK);
        LocalTime test = new LocalTime("10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectString2
    public void testConstructor_ObjectString2() throws Throwable {
        LocalTime test = new LocalTime("T10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectString3
    public void testConstructor_ObjectString3() throws Throwable {
        LocalTime test = new LocalTime("10:20");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectString4
    public void testConstructor_ObjectString4() throws Throwable {
        LocalTime test = new LocalTime("10");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new LocalTime("1970-04-06");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new LocalTime("1970-04-06T+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectStringEx3
    public void testConstructor_ObjectStringEx3() throws Throwable {
        try {
            new LocalTime("1970-04-06T10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectStringEx4
    public void testConstructor_ObjectStringEx4() throws Throwable {
        try {
            new LocalTime("1970-04-06T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectStringEx5
    public void testConstructor_ObjectStringEx5() throws Throwable {
        try {
            new LocalTime("T10:20:30.040+04:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectStringEx6
    public void testConstructor_ObjectStringEx6() throws Throwable {
        try {
            new LocalTime("10:20:30.040+04:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectLocalTime
    public void testConstructor_ObjectLocalTime() throws Throwable {
        LocalTime time = new LocalTime(10, 20, 30, 40, BUDDHIST_UTC);
        LocalTime test = new LocalTime(time);
        assertEquals(BUDDHIST_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectLocalDate
    public void testConstructor_ObjectLocalDate() throws Throwable {
        LocalDate date = new LocalDate(1970, 4, 6, BUDDHIST_UTC);
        try {
            new LocalTime(date);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectLocalDateTime
    public void testConstructor_ObjectLocalDateTime() throws Throwable {
        LocalDateTime dt = new LocalDateTime(1970, 5, 6, 10, 20, 30, 40, BUDDHIST_UTC);
        LocalTime test = new LocalTime(dt);
        assertEquals(BUDDHIST_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectTimeOfDay
    public void testConstructor_ObjectTimeOfDay() throws Throwable {
        TimeOfDay time = new TimeOfDay(10, 20, 30, 40, BUDDHIST_UTC);
        LocalTime test = new LocalTime(time);
        assertEquals(BUDDHIST_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_Object1_DateTimeZone
    public void testConstructor_Object1_DateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalTime test = new LocalTime(date, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectString_DateTimeZoneLondon
    public void testConstructor_ObjectString_DateTimeZoneLondon() throws Throwable {
        LocalTime test = new LocalTime("04:20", LONDON);
        assertEquals(4, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectString_DateTimeZoneTokyo
    public void testConstructor_ObjectString_DateTimeZoneTokyo() throws Throwable {
        LocalTime test = new LocalTime("04:20", TOKYO);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(4, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_ObjectString_DateTimeZoneNewYork
    public void testConstructor_ObjectString_DateTimeZoneNewYork() throws Throwable {
        LocalTime test = new LocalTime("04:20", NEW_YORK);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(4, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_nullObject_DateTimeZone
    public void testConstructor_nullObject_DateTimeZone() throws Throwable {
        LocalTime test = new LocalTime((Object) null, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_Object_nullDateTimeZone
    public void testConstructor_Object_nullDateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalTime test = new LocalTime(date, (DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_nullObject_nullDateTimeZone
    public void testConstructor_nullObject_nullDateTimeZone() throws Throwable {
        LocalTime test = new LocalTime((Object) null, (DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_Object1_Chronology
    public void testConstructor_Object1_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalTime test = new LocalTime(date, JULIAN_LONDON);
        assertEquals(JULIAN_UTC, test.getChronology());
        assertEquals(1 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_Object2_Chronology
    public void testConstructor_Object2_Chronology() throws Throwable {
        LocalTime test = new LocalTime("T10:20");
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        
        try {
            new LocalTime("T1020");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        LocalTime test = new LocalTime((Object) null, JULIAN_LONDON);
        assertEquals(JULIAN_UTC, test.getChronology());
        assertEquals(10 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalTime test = new LocalTime(date, (Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        LocalTime test = new LocalTime((Object) null, (Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET_LONDON, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_int_int
    public void testConstructor_int_int() throws Throwable {
        LocalTime test = new LocalTime(10, 20);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        try {
            new LocalTime(-1, 20);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(24, 20);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_int_int_int
    public void testConstructor_int_int_int() throws Throwable {
        LocalTime test = new LocalTime(10, 20, 30);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        try {
            new LocalTime(-1, 20, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(24, 20, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, -1, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 60, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 20, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 20, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_int_int_int_int
    public void testConstructor_int_int_int_int() throws Throwable {
        LocalTime test = new LocalTime(10, 20, 30, 40);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        try {
            new LocalTime(-1, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(24, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, -1, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 60, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 20, -1, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 20, 60, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 20, 30, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 20, 30, 1000);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_Chronology() throws Throwable {
        LocalTime test = new LocalTime(10, 20, 30, 40, JULIAN_LONDON);
        assertEquals(JULIAN_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        try {
            new LocalTime(-1, 20, 30, 40, JULIAN_LONDON);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(24, 20, 30, 40, JULIAN_LONDON);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, -1, 30, 40, JULIAN_LONDON);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 60, 30, 40, JULIAN_LONDON);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 20, -1, 40, JULIAN_LONDON);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 20, 60, 40, JULIAN_LONDON);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 20, 30, -1, JULIAN_LONDON);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalTime(10, 20, 30, 1000, JULIAN_LONDON);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalTime_Constructors::testConstructor_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_nullChronology() throws Throwable {
        LocalTime test = new LocalTime(10, 20, 30, 40, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestMinutes::testConstants
    public void testConstants() {
        assertEquals(0, Minutes.ZERO.getMinutes());
        assertEquals(1, Minutes.ONE.getMinutes());
        assertEquals(2, Minutes.TWO.getMinutes());
        assertEquals(3, Minutes.THREE.getMinutes());
        assertEquals(Integer.MAX_VALUE, Minutes.MAX_VALUE.getMinutes());
        assertEquals(Integer.MIN_VALUE, Minutes.MIN_VALUE.getMinutes());
    }

// org.joda.time.TestMinutes::testFactory_minutes_int
    public void testFactory_minutes_int() {
        assertSame(Minutes.ZERO, Minutes.minutes(0));
        assertSame(Minutes.ONE, Minutes.minutes(1));
        assertSame(Minutes.TWO, Minutes.minutes(2));
        assertSame(Minutes.THREE, Minutes.minutes(3));
        assertSame(Minutes.MAX_VALUE, Minutes.minutes(Integer.MAX_VALUE));
        assertSame(Minutes.MIN_VALUE, Minutes.minutes(Integer.MIN_VALUE));
        assertEquals(-1, Minutes.minutes(-1).getMinutes());
        assertEquals(4, Minutes.minutes(4).getMinutes());
    }

// org.joda.time.TestMinutes::testFactory_minutesBetween_RInstant
    public void testFactory_minutesBetween_RInstant() {
        DateTime start = new DateTime(2006, 6, 9, 12, 3, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 9, 12, 6, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 9, 12, 9, 0, 0, PARIS);
        
        assertEquals(3, Minutes.minutesBetween(start, end1).getMinutes());
        assertEquals(0, Minutes.minutesBetween(start, start).getMinutes());
        assertEquals(0, Minutes.minutesBetween(end1, end1).getMinutes());
        assertEquals(-3, Minutes.minutesBetween(end1, start).getMinutes());
        assertEquals(6, Minutes.minutesBetween(start, end2).getMinutes());
    }

// org.joda.time.TestMinutes::testFactory_minutesBetween_RPartial
    public void testFactory_minutesBetween_RPartial() {
        LocalTime start = new LocalTime(12, 3);
        LocalTime end1 = new LocalTime(12, 6);
        TimeOfDay end2 = new TimeOfDay(12, 9);
        
        assertEquals(3, Minutes.minutesBetween(start, end1).getMinutes());
        assertEquals(0, Minutes.minutesBetween(start, start).getMinutes());
        assertEquals(0, Minutes.minutesBetween(end1, end1).getMinutes());
        assertEquals(-3, Minutes.minutesBetween(end1, start).getMinutes());
        assertEquals(6, Minutes.minutesBetween(start, end2).getMinutes());
    }

// org.joda.time.TestMinutes::testFactory_minutesIn_RInterval
    public void testFactory_minutesIn_RInterval() {
        DateTime start = new DateTime(2006, 6, 9, 12, 3, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 9, 12, 6, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 9, 12, 9, 0, 0, PARIS);
        
        assertEquals(0, Minutes.minutesIn((ReadableInterval) null).getMinutes());
        assertEquals(3, Minutes.minutesIn(new Interval(start, end1)).getMinutes());
        assertEquals(0, Minutes.minutesIn(new Interval(start, start)).getMinutes());
        assertEquals(0, Minutes.minutesIn(new Interval(end1, end1)).getMinutes());
        assertEquals(6, Minutes.minutesIn(new Interval(start, end2)).getMinutes());
    }

// org.joda.time.TestMinutes::testFactory_standardMinutesIn_RPeriod
    public void testFactory_standardMinutesIn_RPeriod() {
        assertEquals(0, Minutes.standardMinutesIn((ReadablePeriod) null).getMinutes());
        assertEquals(0, Minutes.standardMinutesIn(Period.ZERO).getMinutes());
        assertEquals(1, Minutes.standardMinutesIn(new Period(0, 0, 0, 0, 0, 1, 0, 0)).getMinutes());
        assertEquals(123, Minutes.standardMinutesIn(Period.minutes(123)).getMinutes());
        assertEquals(-987, Minutes.standardMinutesIn(Period.minutes(-987)).getMinutes());
        assertEquals(1, Minutes.standardMinutesIn(Period.seconds(119)).getMinutes());
        assertEquals(2, Minutes.standardMinutesIn(Period.seconds(120)).getMinutes());
        assertEquals(2, Minutes.standardMinutesIn(Period.seconds(121)).getMinutes());
        assertEquals(120, Minutes.standardMinutesIn(Period.hours(2)).getMinutes());
        try {
            Minutes.standardMinutesIn(Period.months(1));
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestMinutes::testFactory_parseMinutes_String
    public void testFactory_parseMinutes_String() {
        assertEquals(0, Minutes.parseMinutes((String) null).getMinutes());
        assertEquals(0, Minutes.parseMinutes("PT0M").getMinutes());
        assertEquals(1, Minutes.parseMinutes("PT1M").getMinutes());
        assertEquals(-3, Minutes.parseMinutes("PT-3M").getMinutes());
        assertEquals(2, Minutes.parseMinutes("P0Y0M0DT2M").getMinutes());
        assertEquals(2, Minutes.parseMinutes("PT0H2M").getMinutes());
        try {
            Minutes.parseMinutes("P1Y1D");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Minutes.parseMinutes("P1DT1M");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestMinutes::testGetMethods
    public void testGetMethods() {
        Minutes test = Minutes.minutes(20);
        assertEquals(20, test.getMinutes());
    }
