// buggy code
    public long getDateTimeMillis(int year, int monthOfYear, int dayOfMonth,
                                  int hourOfDay, int minuteOfHour,
                                  int secondOfMinute, int millisOfSecond)
        throws IllegalArgumentException
    {
        Chronology base;
        if ((base = getBase()) != null) {
            return base.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
        }

        // Assume date is Gregorian.
        long instant;
            instant = iGregorianChronology.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
        if (instant < iCutoverMillis) {
            // Maybe it's Julian.
            instant = iJulianChronology.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
            if (instant >= iCutoverMillis) {
                // Okay, it's in the illegal cutover gap.
                throw new IllegalArgumentException("Specified date does not exist");
            }
        }
        return instant;
    }

// relevant test
// org.joda.time.TestDateTime_Basics::testCompareTo
    public void testCompareTo() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test1a = new DateTime(TEST_TIME1);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        assertEquals(+1, test2.compareTo(new MockInstant()));
        assertEquals(0, test1.compareTo(new MockInstant()));
        
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

    }

// org.joda.time.TestDateTime_Basics::testIsEqual_long
    public void testIsEqual_long() {
        assertEquals(false, new DateTime(TEST_TIME1).isEqual(TEST_TIME2));
        assertEquals(true, new DateTime(TEST_TIME1).isEqual(TEST_TIME1));
        assertEquals(false, new DateTime(TEST_TIME2).isEqual(TEST_TIME1));
    }

// org.joda.time.TestDateTime_Basics::testIsEqualNow
    public void testIsEqualNow() {
        assertEquals(false, new DateTime(TEST_TIME_NOW - 1).isEqualNow());
        assertEquals(true, new DateTime(TEST_TIME_NOW).isEqualNow());
        assertEquals(false, new DateTime(TEST_TIME_NOW + 1).isEqualNow());
    }

// org.joda.time.TestDateTime_Basics::testIsEqual_RI
    public void testIsEqual_RI() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test1a = new DateTime(TEST_TIME1);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        assertEquals(false, test2.isEqual(new MockInstant()));
        assertEquals(true, test1.isEqual(new MockInstant()));
        
        assertEquals(false, new DateTime(TEST_TIME_NOW + 1).isEqual(null));
        assertEquals(true, new DateTime(TEST_TIME_NOW).isEqual(null));
        assertEquals(false, new DateTime(TEST_TIME_NOW - 1).isEqual(null));
    }

// org.joda.time.TestDateTime_Basics::testIsBefore_long
    public void testIsBefore_long() {
        assertEquals(true, new DateTime(TEST_TIME1).isBefore(TEST_TIME2));
        assertEquals(false, new DateTime(TEST_TIME1).isBefore(TEST_TIME1));
        assertEquals(false, new DateTime(TEST_TIME2).isBefore(TEST_TIME1));
    }

// org.joda.time.TestDateTime_Basics::testIsBeforeNow
    public void testIsBeforeNow() {
        assertEquals(true, new DateTime(TEST_TIME_NOW - 1).isBeforeNow());
        assertEquals(false, new DateTime(TEST_TIME_NOW).isBeforeNow());
        assertEquals(false, new DateTime(TEST_TIME_NOW + 1).isBeforeNow());
    }

// org.joda.time.TestDateTime_Basics::testIsBefore_RI
    public void testIsBefore_RI() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test1a = new DateTime(TEST_TIME1);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        assertEquals(false, test2.isBefore(new MockInstant()));
        assertEquals(false, test1.isBefore(new MockInstant()));
        
        assertEquals(false, new DateTime(TEST_TIME_NOW + 1).isBefore(null));
        assertEquals(false, new DateTime(TEST_TIME_NOW).isBefore(null));
        assertEquals(true, new DateTime(TEST_TIME_NOW - 1).isBefore(null));
    }

// org.joda.time.TestDateTime_Basics::testIsAfter_long
    public void testIsAfter_long() {
        assertEquals(false, new DateTime(TEST_TIME1).isAfter(TEST_TIME2));
        assertEquals(false, new DateTime(TEST_TIME1).isAfter(TEST_TIME1));
        assertEquals(true, new DateTime(TEST_TIME2).isAfter(TEST_TIME1));
    }

// org.joda.time.TestDateTime_Basics::testIsAfterNow
    public void testIsAfterNow() {
        assertEquals(false, new DateTime(TEST_TIME_NOW - 1).isAfterNow());
        assertEquals(false, new DateTime(TEST_TIME_NOW).isAfterNow());
        assertEquals(true, new DateTime(TEST_TIME_NOW + 1).isAfterNow());
    }

// org.joda.time.TestDateTime_Basics::testIsAfter_RI
    public void testIsAfter_RI() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test1a = new DateTime(TEST_TIME1);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        assertEquals(true, test2.isAfter(new MockInstant()));
        assertEquals(false, test1.isAfter(new MockInstant()));
        
        assertEquals(true, new DateTime(TEST_TIME_NOW + 1).isAfter(null));
        assertEquals(false, new DateTime(TEST_TIME_NOW).isAfter(null));
        assertEquals(false, new DateTime(TEST_TIME_NOW - 1).isAfter(null));
    }

// org.joda.time.TestDateTime_Basics::testSerialization
    public void testSerialization() throws Exception {
        DateTime test = new DateTime(TEST_TIME_NOW);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTime result = (DateTime) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestDateTime_Basics::testToString
    public void testToString() {
        DateTime test = new DateTime(TEST_TIME_NOW);
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString());
        
        test = new DateTime(TEST_TIME_NOW, PARIS);
        assertEquals("2002-06-09T02:00:00.000+02:00", test.toString());
    }

// org.joda.time.TestDateTime_Basics::testToString_String
    public void testToString_String() {
        DateTime test = new DateTime(TEST_TIME_NOW);
        assertEquals("2002 01", test.toString("yyyy HH"));
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString((String) null));
    }

// org.joda.time.TestDateTime_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        DateTime test = new DateTime(TEST_TIME_NOW);
        assertEquals("Sun 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("dim. 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString(null, Locale.ENGLISH));
        assertEquals("Sun 9/6", test.toString("EEE d/M", null));
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString(null, null));
    }

// org.joda.time.TestDateTime_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        DateMidnight test = new DateMidnight(TEST_TIME_NOW);
        assertEquals("2002 00", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09T00:00:00.000+01:00", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestDateTime_Basics::testToInstant
    public void testToInstant() {
        DateTime test = new DateTime(TEST_TIME1);
        Instant result = test.toInstant();
        assertEquals(TEST_TIME1, result.getMillis());
    }

// org.joda.time.TestDateTime_Basics::testToDateTime
    public void testToDateTime() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.toDateTime();
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testToDateTimeISO
    public void testToDateTimeISO() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.toDateTimeISO();
        assertSame(test, result);
        
        test = new DateTime(TEST_TIME1, ISO_PARIS);
        result = test.toDateTimeISO();
        assertSame(DateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
        assertNotSame(test, result);
        
        test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        result = test.toDateTimeISO();
        assertSame(DateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
        assertNotSame(test, result);
        
        test = new DateTime(TEST_TIME1, new MockNullZoneChronology());
        result = test.toDateTimeISO();
        assertSame(DateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
        assertNotSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testToDateTime_DateTimeZone
    public void testToDateTime_DateTimeZone() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.toDateTime(LONDON);
        assertSame(test, result);

        test = new DateTime(TEST_TIME1);
        result = test.toDateTime(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(PARIS, result.getZone());

        test = new DateTime(TEST_TIME1, PARIS);
        result = test.toDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(LONDON, result.getZone());

        test = new DateTime(TEST_TIME1);
        result = test.toDateTime((DateTimeZone) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testToDateTime_Chronology
    public void testToDateTime_Chronology() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.toDateTime(ISO_DEFAULT);
        assertSame(test, result);

        test = new DateTime(TEST_TIME1);
        result = test.toDateTime(GREGORIAN_PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GREGORIAN_PARIS, result.getChronology());

        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.toDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateTime(TEST_TIME1);
        result = test.toDateTime((Chronology) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testToMutableDateTime
    public void testToMutableDateTime() {
        DateTime test = new DateTime(TEST_TIME1, PARIS);
        MutableDateTime result = test.toMutableDateTime();
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
    }

// org.joda.time.TestDateTime_Basics::testToMutableDateTimeISO
    public void testToMutableDateTimeISO() {
        DateTime test = new DateTime(TEST_TIME1, PARIS);
        MutableDateTime result = test.toMutableDateTimeISO();
        assertSame(MutableDateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
    }

// org.joda.time.TestDateTime_Basics::testToMutableDateTime_DateTimeZone
    public void testToMutableDateTime_DateTimeZone() {
        DateTime test = new DateTime(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime(LONDON);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateTime(TEST_TIME1);
        result = test.toMutableDateTime(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());

        test = new DateTime(TEST_TIME1, PARIS);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateTime(TEST_TIME1);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
    }

// org.joda.time.TestDateTime_Basics::testToMutableDateTime_Chronology
    public void testToMutableDateTime_Chronology() {
        DateTime test = new DateTime(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime(ISO_DEFAULT);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateTime(TEST_TIME1);
        result = test.toMutableDateTime(GREGORIAN_PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GREGORIAN_PARIS, result.getChronology());

        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.toMutableDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateTime(TEST_TIME1);
        result = test.toMutableDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
    }

// org.joda.time.TestDateTime_Basics::testToDate
    public void testToDate() {
        DateTime test = new DateTime(TEST_TIME1);
        Date result = test.toDate();
        assertEquals(test.getMillis(), result.getTime());
    }

// org.joda.time.TestDateTime_Basics::testToCalendar_Locale
    public void testToCalendar_Locale() {
        DateTime test = new DateTime(TEST_TIME1);
        Calendar result = test.toCalendar(null);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/London"), result.getTimeZone());

        test = new DateTime(TEST_TIME1, PARIS);
        result = test.toCalendar(null);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());

        test = new DateTime(TEST_TIME1, PARIS);
        result = test.toCalendar(Locale.UK);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());
    }

// org.joda.time.TestDateTime_Basics::testToGregorianCalendar
    public void testToGregorianCalendar() {
        DateTime test = new DateTime(TEST_TIME1);
        GregorianCalendar result = test.toGregorianCalendar();
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/London"), result.getTimeZone());

        test = new DateTime(TEST_TIME1, PARIS);
        result = test.toGregorianCalendar();
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());
    }

// org.joda.time.TestDateTime_Basics::testToDateMidnight
    public void testToDateMidnight() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        DateMidnight test = base.toDateMidnight();
        assertEquals(new DateMidnight(base, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testToYearMonthDay
    public void testToYearMonthDay() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        YearMonthDay test = base.toYearMonthDay();
        assertEquals(new YearMonthDay(TEST_TIME1, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testToTimeOfDay
    public void testToTimeOfDay() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        TimeOfDay test = base.toTimeOfDay();
        assertEquals(new TimeOfDay(TEST_TIME1, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testToLocalDateTime
    public void testToLocalDateTime() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        LocalDateTime test = base.toLocalDateTime();
        assertEquals(new LocalDateTime(TEST_TIME1, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testToLocalDate
    public void testToLocalDate() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        LocalDate test = base.toLocalDate();
        assertEquals(new LocalDate(TEST_TIME1, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testToLocalTime
    public void testToLocalTime() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        LocalTime test = base.toLocalTime();
        assertEquals(new LocalTime(TEST_TIME1, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testWithMillis_long
    public void testWithMillis_long() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.withMillis(TEST_TIME2);
        assertEquals(TEST_TIME2, result.getMillis());
        assertEquals(test.getChronology(), result.getChronology());
        
        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.withMillis(TEST_TIME2);
        assertEquals(TEST_TIME2, result.getMillis());
        assertEquals(test.getChronology(), result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        result = test.withMillis(TEST_TIME1);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithChronology_Chronology
    public void testWithChronology_Chronology() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.withChronology(GREGORIAN_PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GREGORIAN_PARIS, result.getChronology());
        
        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.withChronology(null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        result = test.withChronology(null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        result = test.withChronology(ISO_DEFAULT);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithZone_DateTimeZone
    public void testWithZone_DateTimeZone() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.withZone(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
        
        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.withZone(null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GREGORIAN_DEFAULT, result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        result = test.withZone(null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithZoneRetainFields_DateTimeZone
    public void testWithZoneRetainFields_DateTimeZone() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.withZoneRetainFields(PARIS);
        assertEquals(test.getMillis() - DateTimeConstants.MILLIS_PER_HOUR, result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        result = test.withZoneRetainFields(LONDON);
        assertSame(test, result);
        
        test = new DateTime(TEST_TIME1);
        result = test.withZoneRetainFields(null);
        assertSame(test, result);
        
        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.withZoneRetainFields(null);
        assertEquals(test.getMillis() + DateTimeConstants.MILLIS_PER_HOUR, result.getMillis());
        assertEquals(GREGORIAN_DEFAULT, result.getChronology());
        
        test = new DateTime(TEST_TIME1, new MockNullZoneChronology());
        result = test.withZoneRetainFields(LONDON);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithDate_int_int_int
    public void testWithDate_int_int_int() {
        DateTime test = new DateTime(2002, 4, 5, 1, 2, 3, 4, ISO_UTC);
        DateTime result = test.withDate(2003, 5, 6);
        DateTime expected = new DateTime(2003, 5, 6, 1, 2, 3, 4, ISO_UTC);
        assertEquals(expected, result);
        
        test = new DateTime(TEST_TIME1);
        try {
            test.withDate(2003, 13, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testWithTime_int_int_int
    public void testWithTime_int_int_int() {
        DateTime test = new DateTime(TEST_TIME1 - 12345L, BUDDHIST_UTC);
        DateTime result = test.withTime(12, 24, 0, 0);
        assertEquals(TEST_TIME1, result.getMillis());
        assertEquals(BUDDHIST_UTC, result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        try {
            test.withTime(25, 1, 1, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testWithFields_RPartial
    public void testWithFields_RPartial() {
        DateTime test = new DateTime(2004, 5, 6, 7, 8, 9, 0);
        DateTime result = test.withFields(new YearMonthDay(2003, 4, 5));
        DateTime expected = new DateTime(2003, 4, 5, 7, 8, 9, 0);
        assertEquals(expected, result);
        
        test = new DateTime(TEST_TIME1);
        result = test.withFields(null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithField1
    public void testWithField1() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), test);
        assertEquals(new DateTime(2006, 6, 9, 0, 0, 0, 0), result);
    }

// org.joda.time.TestDateTime_Basics::testWithField2
    public void testWithField2() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testWithFieldAdded1
    public void testWithFieldAdded1() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), test);
        assertEquals(new DateTime(2010, 6, 9, 0, 0, 0, 0), result);
    }

// org.joda.time.TestDateTime_Basics::testWithFieldAdded2
    public void testWithFieldAdded2() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testWithFieldAdded3
    public void testWithFieldAdded3() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testWithFieldAdded4
    public void testWithFieldAdded4() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithDurationAdded_long_int
    public void testWithDurationAdded_long_int() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.withDurationAdded(123456789L, 1);
        DateTime expected = new DateTime(TEST_TIME1 + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(123456789L, 0);
        assertSame(test, result);
        
        result = test.withDurationAdded(123456789L, 2);
        expected = new DateTime(TEST_TIME1 + (2L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(123456789L, -3);
        expected = new DateTime(TEST_TIME1 - (3L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateTime_Basics::testWithDurationAdded_RD_int
    public void testWithDurationAdded_RD_int() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.withDurationAdded(new Duration(123456789L), 1);
        DateTime expected = new DateTime(TEST_TIME1 + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(null, 1);
        assertSame(test, result);
        
        result = test.withDurationAdded(new Duration(123456789L), 0);
        assertSame(test, result);
        
        result = test.withDurationAdded(new Duration(123456789L), 2);
        expected = new DateTime(TEST_TIME1 + (2L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(new Duration(123456789L), -3);
        expected = new DateTime(TEST_TIME1 - (3L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateTime_Basics::testWithDurationAdded_RP_int
    public void testWithDurationAdded_RP_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.withPeriodAdded(new Period(1, 2, 3, 4, 5, 6, 7, 8), 1);
        DateTime expected = new DateTime(2003, 7, 28, 6, 8, 10, 12, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withPeriodAdded(null, 1);
        assertSame(test, result);
        
        result = test.withPeriodAdded(new Period(1, 2, 3, 4, 5, 6, 7, 8), 0);
        assertSame(test, result);
        
        result = test.withPeriodAdded(new Period(1, 2, 0, 4, 5, 6, 7, 8), 3);
        expected = new DateTime(2005, 11, 15, 16, 20, 24, 28, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withPeriodAdded(new Period(1, 2, 0, 1, 1, 2, 3, 4), -1);
        expected = new DateTime(2001, 3, 2, 0, 0, 0, 0, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateTime_Basics::testPlus_long
    public void testPlus_long() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.plus(123456789L);
        DateTime expected = new DateTime(TEST_TIME1 + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateTime_Basics::testPlus_RD
    public void testPlus_RD() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.plus(new Duration(123456789L));
        DateTime expected = new DateTime(TEST_TIME1 + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plus((ReadableDuration) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlus_RP
    public void testPlus_RP() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        DateTime expected = new DateTime(2003, 7, 28, 6, 8, 10, 12, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusYears_int
    public void testPlusYears_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusYears(1);
        DateTime expected = new DateTime(2003, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusMonths(1);
        DateTime expected = new DateTime(2002, 6, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusWeeks_int
    public void testPlusWeeks_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusWeeks(1);
        DateTime expected = new DateTime(2002, 5, 10, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusDays_int
    public void testPlusDays_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusDays(1);
        DateTime expected = new DateTime(2002, 5, 4, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusHours_int
    public void testPlusHours_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusHours(1);
        DateTime expected = new DateTime(2002, 5, 3, 2, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusMinutes_int
    public void testPlusMinutes_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusMinutes(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 3, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusSeconds_int
    public void testPlusSeconds_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusSeconds(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 2, 4, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusMillis_int
    public void testPlusMillis_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusMillis(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 2, 3, 5, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinus_long
    public void testMinus_long() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.minus(123456789L);
        DateTime expected = new DateTime(TEST_TIME1 - 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateTime_Basics::testMinus_RD
    public void testMinus_RD() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.minus(new Duration(123456789L));
        DateTime expected = new DateTime(TEST_TIME1 - 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minus((ReadableDuration) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinus_RP
    public void testMinus_RP() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        DateTime expected = new DateTime(2001, 3, 26, 0, 1, 2, 3, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusYears_int
    public void testMinusYears_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusYears(1);
        DateTime expected = new DateTime(2001, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusMonths(1);
        DateTime expected = new DateTime(2002, 4, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusWeeks_int
    public void testMinusWeeks_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusWeeks(1);
        DateTime expected = new DateTime(2002, 4, 26, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusDays_int
    public void testMinusDays_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusDays(1);
        DateTime expected = new DateTime(2002, 5, 2, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusHours_int
    public void testMinusHours_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusHours(1);
        DateTime expected = new DateTime(2002, 5, 3, 0, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusMinutes_int
    public void testMinusMinutes_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusMinutes(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 1, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusSeconds_int
    public void testMinusSeconds_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusSeconds(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 2, 2, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusMillis_int
    public void testMinusMillis_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusMillis(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 2, 3, 3, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testProperty
    public void testProperty() {
        DateTime test = new DateTime();
        assertEquals(test.year(), test.property(DateTimeFieldType.year()));
        assertEquals(test.dayOfWeek(), test.property(DateTimeFieldType.dayOfWeek()));
        assertEquals(test.secondOfMinute(), test.property(DateTimeFieldType.secondOfMinute()));
        assertEquals(test.millisOfSecond(), test.property(DateTimeFieldType.millisOfSecond()));
        DateTimeFieldType bad = new DateTimeFieldType("bad") {
            public DurationFieldType getDurationType() {
                return DurationFieldType.weeks();
            }
            public DurationFieldType getRangeDurationType() {
                return null;
            }
            public DateTimeField getField(Chronology chronology) {
                return UnsupportedDateTimeField.getInstance(this, UnsupportedDurationField.getInstance(getDurationType()));
            }
        };
        try {
            test.property(bad);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestDateTime_Constructors::test_now
    public void test_now() throws Throwable {
        DateTime test = DateTime.now();
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::test_now_DateTimeZone
    public void test_now_DateTimeZone() throws Throwable {
        DateTime test = DateTime.now(PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::test_now_nullDateTimeZone
    public void test_now_nullDateTimeZone() throws Throwable {
        try {
            DateTime.now((DateTimeZone) null);
            fail();
        } catch (NullPointerException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::test_now_Chronology
    public void test_now_Chronology() throws Throwable {
        DateTime test = DateTime.now(GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::test_now_nullChronology
    public void test_now_nullChronology() throws Throwable {
        try {
            DateTime.now((Chronology) null);
            fail();
        } catch (NullPointerException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new DateTime(2010, 6, 30, 1, 20, ISOChronology.getInstance(DateTimeZone.forOffsetHours(2))), DateTime.parse("2010-06-30T01:20+02:00"));
        assertEquals(new DateTime(2010, 1, 2, 14, 50, ISOChronology.getInstance(LONDON)), DateTime.parse("2010-002T14:50"));
    }

// org.joda.time.TestDateTime_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--dd MM HH").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new DateTime(2010, 6, 30, 13, 0, ISOChronology.getInstance(PARIS)), DateTime.parse("2010--30 06 13", f));
    }

// org.joda.time.TestDateTime_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        DateTime test = new DateTime();
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime((DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        DateTime test = new DateTime(GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        DateTime test = new DateTime((Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        DateTime test = new DateTime(TEST_TIME2);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long1_DateTimeZone
    public void testConstructor_long1_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long2_DateTimeZone
    public void testConstructor_long2_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(TEST_TIME2, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long_nullDateTimeZone
    public void testConstructor_long_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        DateTime test = new DateTime(TEST_TIME2, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_invalidObject
    public void testConstructor_invalidObject() throws Throwable {
        try {
            new DateTime(new Object());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        DateTime test = new DateTime((Object) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_badconverterObject
    public void testConstructor_badconverterObject() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateTime test = new DateTime(new Integer(0));
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        DateTime test = new DateTime("1972-12-03");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString2
    public void testConstructor_ObjectString2() throws Throwable {
        DateTime test = new DateTime("2006-06-03T+14:00");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(2006, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(2, test.getDayOfMonth());  
        assertEquals(11, test.getHourOfDay());  
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString3
    public void testConstructor_ObjectString3() throws Throwable {
        DateTime test = new DateTime("1972-12-03T10:20:30.040");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString4
    public void testConstructor_ObjectString4() throws Throwable {
        DateTime test = new DateTime("2006-06-03T10:20:30.040+14:00");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(2006, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(2, test.getDayOfMonth());  
        assertEquals(21, test.getHourOfDay());  
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString5
    public void testConstructor_ObjectString5() throws Throwable {
        DateTime test = new DateTime("T10:20:30.040");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(1, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString6
    public void testConstructor_ObjectString6() throws Throwable {
        DateTime test = new DateTime("T10:20:30.040+14:00");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1969, test.getYear());  
        assertEquals(12, test.getMonthOfYear());  
        assertEquals(31, test.getDayOfMonth());  
        assertEquals(21, test.getHourOfDay());  
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString7
    public void testConstructor_ObjectString7() throws Throwable {
        DateTime test = new DateTime("10");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(10, test.getYear());
        assertEquals(1, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new DateTime("10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new DateTime("10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_DateTimeZone
    public void testConstructor_Object_DateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_invalidObject_DateTimeZone
    public void testConstructor_invalidObject_DateTimeZone() throws Throwable {
        try {
            new DateTime(new Object(), PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_DateTimeZone
    public void testConstructor_nullObject_DateTimeZone() throws Throwable {
        DateTime test = new DateTime((Object) null, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_nullDateTimeZone
    public void testConstructor_Object_nullDateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_nullDateTimeZone
    public void testConstructor_nullObject_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime((Object) null, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_badconverterObject_DateTimeZone
    public void testConstructor_badconverterObject_DateTimeZone() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateTime test = new DateTime(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_invalidObject_Chronology
    public void testConstructor_invalidObject_Chronology() throws Throwable {
        try {
            new DateTime(new Object(), GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        DateTime test = new DateTime((Object) null, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        DateTime test = new DateTime((Object) null, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_badconverterObject_Chronology
    public void testConstructor_badconverterObject_Chronology() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateTime test = new DateTime(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int
    public void testConstructor_int_int_int_int_int() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_int_int_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 2, 0, PARIS);  
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_int_int_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, (DateTimeZone) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_int_Chronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, GregorianChronology.getInstance());  
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_int_nullChronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, (Chronology) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int
    public void testConstructor_int_int_int_int_int_int() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_int_int_int_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 2, 0, 0, PARIS);  
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_int_int_int_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, (DateTimeZone) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_int_int_Chronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, GregorianChronology.getInstance());  
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_int_int_nullChronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, (Chronology) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int
    public void testConstructor_int_int_int_int_int_int_int() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new DateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 0, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 13, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 0, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 31, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateTime(2002, 7, 31, 0, 0, 0, 0);
        try {
            new DateTime(2002, 7, 32, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_int_int_int_int_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 2, 0, 0, 0, PARIS);  
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new DateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 0, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 13, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 0, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 31, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateTime(2002, 7, 31, 0, 0, 0, 0, PARIS);
        try {
            new DateTime(2002, 7, 32, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_int_int_int_int_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0, (DateTimeZone) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_int_int_int_Chronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0, GregorianChronology.getInstance());  
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new DateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 0, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 13, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 0, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateTime(2002, 7, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
        try {
            new DateTime(2002, 7, 32, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_int_int_int_nullChronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0, (Chronology) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDays::testConstants
    public void testConstants() {
        assertEquals(0, Days.ZERO.getDays());
        assertEquals(1, Days.ONE.getDays());
        assertEquals(2, Days.TWO.getDays());
        assertEquals(3, Days.THREE.getDays());
        assertEquals(4, Days.FOUR.getDays());
        assertEquals(5, Days.FIVE.getDays());
        assertEquals(6, Days.SIX.getDays());
        assertEquals(7, Days.SEVEN.getDays());
        assertEquals(Integer.MAX_VALUE, Days.MAX_VALUE.getDays());
        assertEquals(Integer.MIN_VALUE, Days.MIN_VALUE.getDays());
    }

// org.joda.time.TestDays::testFactory_days_int
    public void testFactory_days_int() {
        assertSame(Days.ZERO, Days.days(0));
        assertSame(Days.ONE, Days.days(1));
        assertSame(Days.TWO, Days.days(2));
        assertSame(Days.THREE, Days.days(3));
        assertSame(Days.FOUR, Days.days(4));
        assertSame(Days.FIVE, Days.days(5));
        assertSame(Days.SIX, Days.days(6));
        assertSame(Days.SEVEN, Days.days(7));
        assertSame(Days.MAX_VALUE, Days.days(Integer.MAX_VALUE));
        assertSame(Days.MIN_VALUE, Days.days(Integer.MIN_VALUE));
        assertEquals(-1, Days.days(-1).getDays());
        assertEquals(8, Days.days(8).getDays());
    }

// org.joda.time.TestDays::testFactory_daysBetween_RInstant
    public void testFactory_daysBetween_RInstant() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 12, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 15, 18, 0, 0, 0, PARIS);
        
        assertEquals(3, Days.daysBetween(start, end1).getDays());
        assertEquals(0, Days.daysBetween(start, start).getDays());
        assertEquals(0, Days.daysBetween(end1, end1).getDays());
        assertEquals(-3, Days.daysBetween(end1, start).getDays());
        assertEquals(6, Days.daysBetween(start, end2).getDays());
    }

// org.joda.time.TestDays::testFactory_daysBetween_RPartial
    public void testFactory_daysBetween_RPartial() {
        LocalDate start = new LocalDate(2006, 6, 9);
        LocalDate end1 = new LocalDate(2006, 6, 12);
        YearMonthDay end2 = new YearMonthDay(2006, 6, 15);
        
        assertEquals(3, Days.daysBetween(start, end1).getDays());
        assertEquals(0, Days.daysBetween(start, start).getDays());
        assertEquals(0, Days.daysBetween(end1, end1).getDays());
        assertEquals(-3, Days.daysBetween(end1, start).getDays());
        assertEquals(6, Days.daysBetween(start, end2).getDays());
    }

// org.joda.time.TestDays::testFactory_daysIn_RInterval
    public void testFactory_daysIn_RInterval() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 12, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 15, 18, 0, 0, 0, PARIS);
        
        assertEquals(0, Days.daysIn((ReadableInterval) null).getDays());
        assertEquals(3, Days.daysIn(new Interval(start, end1)).getDays());
        assertEquals(0, Days.daysIn(new Interval(start, start)).getDays());
        assertEquals(0, Days.daysIn(new Interval(end1, end1)).getDays());
        assertEquals(6, Days.daysIn(new Interval(start, end2)).getDays());
    }

// org.joda.time.TestDays::testFactory_standardDaysIn_RPeriod
    public void testFactory_standardDaysIn_RPeriod() {
        assertEquals(0, Days.standardDaysIn((ReadablePeriod) null).getDays());
        assertEquals(0, Days.standardDaysIn(Period.ZERO).getDays());
        assertEquals(1, Days.standardDaysIn(new Period(0, 0, 0, 1, 0, 0, 0, 0)).getDays());
        assertEquals(123, Days.standardDaysIn(Period.days(123)).getDays());
        assertEquals(-987, Days.standardDaysIn(Period.days(-987)).getDays());
        assertEquals(1, Days.standardDaysIn(Period.hours(47)).getDays());
        assertEquals(2, Days.standardDaysIn(Period.hours(48)).getDays());
        assertEquals(2, Days.standardDaysIn(Period.hours(49)).getDays());
        assertEquals(14, Days.standardDaysIn(Period.weeks(2)).getDays());
        try {
            Days.standardDaysIn(Period.months(1));
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestDays::testFactory_parseDays_String
    public void testFactory_parseDays_String() {
        assertEquals(0, Days.parseDays((String) null).getDays());
        assertEquals(0, Days.parseDays("P0D").getDays());
        assertEquals(1, Days.parseDays("P1D").getDays());
        assertEquals(-3, Days.parseDays("P-3D").getDays());
        assertEquals(2, Days.parseDays("P0Y0M2D").getDays());
        assertEquals(2, Days.parseDays("P2DT0H0M").getDays());
        try {
            Days.parseDays("P1Y1D");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Days.parseDays("P1DT1H");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestDays::testGetMethods
    public void testGetMethods() {
        Days test = Days.days(20);
        assertEquals(20, test.getDays());
    }

// org.joda.time.TestDays::testGetFieldType
    public void testGetFieldType() {
        Days test = Days.days(20);
        assertEquals(DurationFieldType.days(), test.getFieldType());
    }

// org.joda.time.TestDays::testGetPeriodType
    public void testGetPeriodType() {
        Days test = Days.days(20);
        assertEquals(PeriodType.days(), test.getPeriodType());
    }

// org.joda.time.TestDays::testIsGreaterThan
    public void testIsGreaterThan() {
        assertEquals(true, Days.THREE.isGreaterThan(Days.TWO));
        assertEquals(false, Days.THREE.isGreaterThan(Days.THREE));
        assertEquals(false, Days.TWO.isGreaterThan(Days.THREE));
        assertEquals(true, Days.ONE.isGreaterThan(null));
        assertEquals(false, Days.days(-1).isGreaterThan(null));
    }

// org.joda.time.TestDays::testIsLessThan
    public void testIsLessThan() {
        assertEquals(false, Days.THREE.isLessThan(Days.TWO));
        assertEquals(false, Days.THREE.isLessThan(Days.THREE));
        assertEquals(true, Days.TWO.isLessThan(Days.THREE));
        assertEquals(false, Days.ONE.isLessThan(null));
        assertEquals(true, Days.days(-1).isLessThan(null));
    }

// org.joda.time.TestDays::testToString
    public void testToString() {
        Days test = Days.days(20);
        assertEquals("P20D", test.toString());
        
        test = Days.days(-20);
        assertEquals("P-20D", test.toString());
    }

// org.joda.time.TestDays::testSerialization
    public void testSerialization() throws Exception {
        Days test = Days.SEVEN;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Days result = (Days) ois.readObject();
        ois.close();
        
        assertSame(test, result);
    }

// org.joda.time.TestDays::testToStandardWeeks
    public void testToStandardWeeks() {
        Days test = Days.days(14);
        Weeks expected = Weeks.weeks(2);
        assertEquals(expected, test.toStandardWeeks());
    }

// org.joda.time.TestDays::testToStandardHours
    public void testToStandardHours() {
        Days test = Days.days(2);
        Hours expected = Hours.hours(2 * 24);
        assertEquals(expected, test.toStandardHours());
        
        try {
            Days.MAX_VALUE.toStandardHours();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDays::testToStandardMinutes
    public void testToStandardMinutes() {
        Days test = Days.days(2);
        Minutes expected = Minutes.minutes(2 * 24 * 60);
        assertEquals(expected, test.toStandardMinutes());
        
        try {
            Days.MAX_VALUE.toStandardMinutes();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDays::testToStandardSeconds
    public void testToStandardSeconds() {
        Days test = Days.days(2);
        Seconds expected = Seconds.seconds(2 * 24 * 60 * 60);
        assertEquals(expected, test.toStandardSeconds());
        
        try {
            Days.MAX_VALUE.toStandardSeconds();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDays::testToStandardDuration
    public void testToStandardDuration() {
        Days test = Days.days(20);
        Duration expected = new Duration(20L * DateTimeConstants.MILLIS_PER_DAY);
        assertEquals(expected, test.toStandardDuration());
        
        expected = new Duration(((long) Integer.MAX_VALUE) * DateTimeConstants.MILLIS_PER_DAY);
        assertEquals(expected, Days.MAX_VALUE.toStandardDuration());
    }

// org.joda.time.TestDays::testPlus_int
    public void testPlus_int() {
        Days test2 = Days.days(2);
        Days result = test2.plus(3);
        assertEquals(2, test2.getDays());
        assertEquals(5, result.getDays());
        
        assertEquals(1, Days.ONE.plus(0).getDays());
        
        try {
            Days.MAX_VALUE.plus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDays::testPlus_Days
    public void testPlus_Days() {
        Days test2 = Days.days(2);
        Days test3 = Days.days(3);
        Days result = test2.plus(test3);
        assertEquals(2, test2.getDays());
        assertEquals(3, test3.getDays());
        assertEquals(5, result.getDays());
        
        assertEquals(1, Days.ONE.plus(Days.ZERO).getDays());
        assertEquals(1, Days.ONE.plus((Days) null).getDays());
        
        try {
            Days.MAX_VALUE.plus(Days.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDays::testMinus_int
    public void testMinus_int() {
        Days test2 = Days.days(2);
        Days result = test2.minus(3);
        assertEquals(2, test2.getDays());
        assertEquals(-1, result.getDays());
        
        assertEquals(1, Days.ONE.minus(0).getDays());
        
        try {
            Days.MIN_VALUE.minus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDays::testMinus_Days
    public void testMinus_Days() {
        Days test2 = Days.days(2);
        Days test3 = Days.days(3);
        Days result = test2.minus(test3);
        assertEquals(2, test2.getDays());
        assertEquals(3, test3.getDays());
        assertEquals(-1, result.getDays());
        
        assertEquals(1, Days.ONE.minus(Days.ZERO).getDays());
        assertEquals(1, Days.ONE.minus((Days) null).getDays());
        
        try {
            Days.MIN_VALUE.minus(Days.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDays::testMultipliedBy_int
    public void testMultipliedBy_int() {
        Days test = Days.days(2);
        assertEquals(6, test.multipliedBy(3).getDays());
        assertEquals(2, test.getDays());
        assertEquals(-6, test.multipliedBy(-3).getDays());
        assertSame(test, test.multipliedBy(1));
        
        Days halfMax = Days.days(Integer.MAX_VALUE / 2 + 1);
        try {
            halfMax.multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDays::testDividedBy_int
    public void testDividedBy_int() {
        Days test = Days.days(12);
        assertEquals(6, test.dividedBy(2).getDays());
        assertEquals(12, test.getDays());
        assertEquals(4, test.dividedBy(3).getDays());
        assertEquals(3, test.dividedBy(4).getDays());
        assertEquals(2, test.dividedBy(5).getDays());
        assertEquals(2, test.dividedBy(6).getDays());
        assertSame(test, test.dividedBy(1));
        
        try {
            Days.ONE.dividedBy(0);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDays::testNegated
    public void testNegated() {
        Days test = Days.days(12);
        assertEquals(-12, test.negated().getDays());
        assertEquals(12, test.getDays());
        
        try {
            Days.MIN_VALUE.negated();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDays::testAddToLocalDate
    public void testAddToLocalDate() {
        Days test = Days.days(20);
        LocalDate date = new LocalDate(2006, 6, 1);
        LocalDate expected = new LocalDate(2006, 6, 21);
        assertEquals(expected, date.plus(test));
    }

// org.joda.time.TestDuration_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestDuration_Basics::testGetMillis
    public void testGetMillis() {
        Duration test = new Duration(0L);
        assertEquals(0, test.getMillis());
        
        test = new Duration(1234567890L);
        assertEquals(1234567890L, test.getMillis());
    }

// org.joda.time.TestDuration_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        Duration test1 = new Duration(123L);
        Duration test2 = new Duration(123L);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        Duration test3 = new Duration(321L);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockDuration(123L)));
    }

// org.joda.time.TestDuration_Basics::testCompareTo
    public void testCompareTo() {
        Duration test1 = new Duration(123L);
        Duration test1a = new Duration(123L);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        Duration test2 = new Duration(321L);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        assertEquals(+1, test2.compareTo(new MockDuration(123L)));
        assertEquals(0, test1.compareTo(new MockDuration(123L)));
        
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

    }

// org.joda.time.TestDuration_Basics::testIsEqual
    public void testIsEqual() {
        Duration test1 = new Duration(123L);
        Duration test1a = new Duration(123L);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        Duration test2 = new Duration(321L);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        assertEquals(false, test2.isEqual(new MockDuration(123L)));
        assertEquals(true, test1.isEqual(new MockDuration(123L)));
        assertEquals(false, test1.isEqual(null));
        assertEquals(true, new Duration(0L).isEqual(null));
    }

// org.joda.time.TestDuration_Basics::testIsBefore
    public void testIsBefore() {
        Duration test1 = new Duration(123L);
        Duration test1a = new Duration(123L);
        assertEquals(false, test1.isShorterThan(test1a));
        assertEquals(false, test1a.isShorterThan(test1));
        assertEquals(false, test1.isShorterThan(test1));
        assertEquals(false, test1a.isShorterThan(test1a));
        
        Duration test2 = new Duration(321L);
        assertEquals(true, test1.isShorterThan(test2));
        assertEquals(false, test2.isShorterThan(test1));
        
        assertEquals(false, test2.isShorterThan(new MockDuration(123L)));
        assertEquals(false, test1.isShorterThan(new MockDuration(123L)));
        assertEquals(false, test1.isShorterThan(null));
        assertEquals(false, new Duration(0L).isShorterThan(null));
    }

// org.joda.time.TestDuration_Basics::testIsAfter
    public void testIsAfter() {
        Duration test1 = new Duration(123L);
        Duration test1a = new Duration(123L);
        assertEquals(false, test1.isLongerThan(test1a));
        assertEquals(false, test1a.isLongerThan(test1));
        assertEquals(false, test1.isLongerThan(test1));
        assertEquals(false, test1a.isLongerThan(test1a));
        
        Duration test2 = new Duration(321L);
        assertEquals(false, test1.isLongerThan(test2));
        assertEquals(true, test2.isLongerThan(test1));
        
        assertEquals(true, test2.isLongerThan(new MockDuration(123L)));
        assertEquals(false, test1.isLongerThan(new MockDuration(123L)));
        assertEquals(true, test1.isLongerThan(null));
        assertEquals(false, new Duration(0L).isLongerThan(null));
    }

// org.joda.time.TestDuration_Basics::testSerialization
    public void testSerialization() throws Exception {
        Duration test = new Duration(123L);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Duration result = (Duration) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestDuration_Basics::testGetStandardSeconds
    public void testGetStandardSeconds() {
        Duration test = new Duration(0L);
        assertEquals(0, test.getStandardSeconds());
        test = new Duration(1L);
        assertEquals(0, test.getStandardSeconds());
        test = new Duration(999L);
        assertEquals(0, test.getStandardSeconds());
        test = new Duration(1000L);
        assertEquals(1, test.getStandardSeconds());
        test = new Duration(1001L);
        assertEquals(1, test.getStandardSeconds());
        test = new Duration(1999L);
        assertEquals(1, test.getStandardSeconds());
        test = new Duration(2000L);
        assertEquals(2, test.getStandardSeconds());
        test = new Duration(-1L);
        assertEquals(0, test.getStandardSeconds());
        test = new Duration(-999L);
        assertEquals(0, test.getStandardSeconds());
        test = new Duration(-1000L);
        assertEquals(-1, test.getStandardSeconds());
    }

// org.joda.time.TestDuration_Basics::testToString
    public void testToString() {
        long length = (365L + 2L * 30L + 3L * 7L + 4L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 845L;
        Duration test = new Duration(length);
        assertEquals("PT" + (length / 1000) + "." + (length % 1000) + "S", test.toString());
        
        assertEquals("PT0S", new Duration(0L).toString());
        assertEquals("PT10S", new Duration(10000L).toString());
        assertEquals("PT1S", new Duration(1000L).toString());
        assertEquals("PT12.345S", new Duration(12345L).toString());
        assertEquals("PT-12.345S", new Duration(-12345L).toString());
        assertEquals("PT-1.123S", new Duration(-1123L).toString());
        assertEquals("PT-0.123S", new Duration(-123L).toString());
        assertEquals("PT-0.012S", new Duration(-12L).toString());
        assertEquals("PT-0.001S", new Duration(-1L).toString());
    }

// org.joda.time.TestDuration_Basics::testToDuration1
    public void testToDuration1() {
        Duration test = new Duration(123L);
        Duration result = test.toDuration();
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testToDuration2
    public void testToDuration2() {
        MockDuration test = new MockDuration(123L);
        Duration result = test.toDuration();
        assertNotSame(test, result);
        assertEquals(test, result);
    }

// org.joda.time.TestDuration_Basics::testToStandardDays
    public void testToStandardDays() {
        Duration test = new Duration(0L);
        assertEquals(Days.days(0), test.toStandardDays());
        test = new Duration(1L);
        assertEquals(Days.days(0), test.toStandardDays());
        test = new Duration(24 * 60 * 60000L - 1);
        assertEquals(Days.days(0), test.toStandardDays());
        test = new Duration(24 * 60 * 60000L);
        assertEquals(Days.days(1), test.toStandardDays());
        test = new Duration(24 * 60 * 60000L + 1);
        assertEquals(Days.days(1), test.toStandardDays());
        test = new Duration(2 * 24 * 60 * 60000L - 1);
        assertEquals(Days.days(1), test.toStandardDays());
        test = new Duration(2 * 24 * 60 * 60000L);
        assertEquals(Days.days(2), test.toStandardDays());
        test = new Duration(-1L);
        assertEquals(Days.days(0), test.toStandardDays());
        test = new Duration(-24 * 60 * 60000L + 1);
        assertEquals(Days.days(0), test.toStandardDays());
        test = new Duration(-24 * 60 * 60000L);
        assertEquals(Days.days(-1), test.toStandardDays());
    }

// org.joda.time.TestDuration_Basics::testToStandardDays_overflow
    public void testToStandardDays_overflow() {
        Duration test = new Duration((((long) Integer.MAX_VALUE) + 1) * 24L * 60L * 60000L);
        try {
            test.toStandardDays();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDuration_Basics::testToStandardHours
    public void testToStandardHours() {
        Duration test = new Duration(0L);
        assertEquals(Hours.hours(0), test.toStandardHours());
        test = new Duration(1L);
        assertEquals(Hours.hours(0), test.toStandardHours());
        test = new Duration(3600000L - 1);
        assertEquals(Hours.hours(0), test.toStandardHours());
        test = new Duration(3600000L);
        assertEquals(Hours.hours(1), test.toStandardHours());
        test = new Duration(3600000L + 1);
        assertEquals(Hours.hours(1), test.toStandardHours());
        test = new Duration(2 * 3600000L - 1);
        assertEquals(Hours.hours(1), test.toStandardHours());
        test = new Duration(2 * 3600000L);
        assertEquals(Hours.hours(2), test.toStandardHours());
        test = new Duration(-1L);
        assertEquals(Hours.hours(0), test.toStandardHours());
        test = new Duration(-3600000L + 1);
        assertEquals(Hours.hours(0), test.toStandardHours());
        test = new Duration(-3600000L);
        assertEquals(Hours.hours(-1), test.toStandardHours());
    }

// org.joda.time.TestDuration_Basics::testToStandardHours_overflow
    public void testToStandardHours_overflow() {
        Duration test = new Duration(((long) Integer.MAX_VALUE) * 3600000L + 3600000L);
        try {
            test.toStandardHours();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDuration_Basics::testToStandardMinutes
    public void testToStandardMinutes() {
        Duration test = new Duration(0L);
        assertEquals(Minutes.minutes(0), test.toStandardMinutes());
        test = new Duration(1L);
        assertEquals(Minutes.minutes(0), test.toStandardMinutes());
        test = new Duration(60000L - 1);
        assertEquals(Minutes.minutes(0), test.toStandardMinutes());
        test = new Duration(60000L);
        assertEquals(Minutes.minutes(1), test.toStandardMinutes());
        test = new Duration(60000L + 1);
        assertEquals(Minutes.minutes(1), test.toStandardMinutes());
        test = new Duration(2 * 60000L - 1);
        assertEquals(Minutes.minutes(1), test.toStandardMinutes());
        test = new Duration(2 * 60000L);
        assertEquals(Minutes.minutes(2), test.toStandardMinutes());
        test = new Duration(-1L);
        assertEquals(Minutes.minutes(0), test.toStandardMinutes());
        test = new Duration(-60000L + 1);
        assertEquals(Minutes.minutes(0), test.toStandardMinutes());
        test = new Duration(-60000L);
        assertEquals(Minutes.minutes(-1), test.toStandardMinutes());
    }

// org.joda.time.TestDuration_Basics::testToStandardMinutes_overflow
    public void testToStandardMinutes_overflow() {
        Duration test = new Duration(((long) Integer.MAX_VALUE) * 60000L + 60000L);
        try {
            test.toStandardMinutes();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDuration_Basics::testToStandardSeconds
    public void testToStandardSeconds() {
        Duration test = new Duration(0L);
        assertEquals(Seconds.seconds(0), test.toStandardSeconds());
        test = new Duration(1L);
        assertEquals(Seconds.seconds(0), test.toStandardSeconds());
        test = new Duration(999L);
        assertEquals(Seconds.seconds(0), test.toStandardSeconds());
        test = new Duration(1000L);
        assertEquals(Seconds.seconds(1), test.toStandardSeconds());
        test = new Duration(1001L);
        assertEquals(Seconds.seconds(1), test.toStandardSeconds());
        test = new Duration(1999L);
        assertEquals(Seconds.seconds(1), test.toStandardSeconds());
        test = new Duration(2000L);
        assertEquals(Seconds.seconds(2), test.toStandardSeconds());
        test = new Duration(-1L);
        assertEquals(Seconds.seconds(0), test.toStandardSeconds());
        test = new Duration(-999L);
        assertEquals(Seconds.seconds(0), test.toStandardSeconds());
        test = new Duration(-1000L);
        assertEquals(Seconds.seconds(-1), test.toStandardSeconds());
    }

// org.joda.time.TestDuration_Basics::testToStandardSeconds_overflow
    public void testToStandardSeconds_overflow() {
        Duration test = new Duration(((long) Integer.MAX_VALUE) * 1000L + 1000L);
        try {
            test.toStandardSeconds();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestDuration_Basics::testToPeriod
    public void testToPeriod() {
        DateTimeZone zone = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.forID("Europe/Paris"));
            long length =
                (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
                5L * DateTimeConstants.MILLIS_PER_HOUR +
                6L * DateTimeConstants.MILLIS_PER_MINUTE +
                7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
            Duration dur = new Duration(length);
            Period test = dur.toPeriod();
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

// org.joda.time.TestDuration_Basics::testToPeriod_fixedZone
    public void testToPeriod_fixedZone() throws Throwable {
        DateTimeZone zone = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.forOffsetHours(2));
            long length =
                (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
                5L * DateTimeConstants.MILLIS_PER_HOUR +
                6L * DateTimeConstants.MILLIS_PER_MINUTE +
                7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
            Duration dur = new Duration(length);
            Period test = dur.toPeriod();
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

// org.joda.time.TestDuration_Basics::testToPeriod_PeriodType
    public void testToPeriod_PeriodType() {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Duration test = new Duration(length);
        Period result = test.toPeriod(PeriodType.standard().withMillisRemoved());
        assertEquals(new Period(test, PeriodType.standard().withMillisRemoved()), result);
        assertEquals(new Period(test.getMillis(), PeriodType.standard().withMillisRemoved()), result);
    }

// org.joda.time.TestDuration_Basics::testToPeriod_Chronology
    public void testToPeriod_Chronology() {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Duration test = new Duration(length);
        Period result = test.toPeriod(ISOChronology.getInstanceUTC());
        assertEquals(new Period(test, ISOChronology.getInstanceUTC()), result);
        assertEquals(new Period(test.getMillis(), ISOChronology.getInstanceUTC()), result);
    }

// org.joda.time.TestDuration_Basics::testToPeriod_PeriodType_Chronology
    public void testToPeriod_PeriodType_Chronology() {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Duration test = new Duration(length);
        Period result = test.toPeriod(PeriodType.standard().withMillisRemoved(), ISOChronology.getInstanceUTC());
        assertEquals(new Period(test, PeriodType.standard().withMillisRemoved(), ISOChronology.getInstanceUTC()), result);
        assertEquals(new Period(test.getMillis(), PeriodType.standard().withMillisRemoved(), ISOChronology.getInstanceUTC()), result);
    }

// org.joda.time.TestDuration_Basics::testToPeriodFrom
    public void testToPeriodFrom() {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Duration test = new Duration(length);
        DateTime dt = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Period result = test.toPeriodFrom(dt);
        assertEquals(new Period(dt, test), result);
    }

// org.joda.time.TestDuration_Basics::testToPeriodFrom_PeriodType
    public void testToPeriodFrom_PeriodType() {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Duration test = new Duration(length);
        DateTime dt = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Period result = test.toPeriodFrom(dt, PeriodType.standard().withMillisRemoved());
        assertEquals(new Period(dt, test, PeriodType.standard().withMillisRemoved()), result);
    }

// org.joda.time.TestDuration_Basics::testToPeriodTo
    public void testToPeriodTo() {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Duration test = new Duration(length);
        DateTime dt = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Period result = test.toPeriodTo(dt);
        assertEquals(new Period(test, dt), result);
    }

// org.joda.time.TestDuration_Basics::testToPeriodTo_PeriodType
    public void testToPeriodTo_PeriodType() {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Duration test = new Duration(length);
        DateTime dt = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Period result = test.toPeriodTo(dt, PeriodType.standard().withMillisRemoved());
        assertEquals(new Period(test, dt, PeriodType.standard().withMillisRemoved()), result);
    }

// org.joda.time.TestDuration_Basics::testToIntervalFrom
    public void testToIntervalFrom() {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Duration test = new Duration(length);
        DateTime dt = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Interval result = test.toIntervalFrom(dt);
        assertEquals(new Interval(dt, test), result);
    }

// org.joda.time.TestDuration_Basics::testToIntervalTo
    public void testToIntervalTo() {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Duration test = new Duration(length);
        DateTime dt = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Interval result = test.toIntervalTo(dt);
        assertEquals(new Interval(test, dt), result);
    }

// org.joda.time.TestDuration_Basics::testWithMillis1
    public void testWithMillis1() {
        Duration test = new Duration(123L);
        Duration result = test.withMillis(123L);
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testWithMillis2
    public void testWithMillis2() {
        Duration test = new Duration(123L);
        Duration result = test.withMillis(1234567890L);
        assertEquals(1234567890L, result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_long_int1
    public void testWithDurationAdded_long_int1() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(8000L, 1);
        assertEquals(8123L, result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_long_int2
    public void testWithDurationAdded_long_int2() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(8000L, 2);
        assertEquals(16123L, result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_long_int3
    public void testWithDurationAdded_long_int3() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(8000L, -1);
        assertEquals((123L - 8000L), result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_long_int4
    public void testWithDurationAdded_long_int4() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(0L, 1);
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_long_int5
    public void testWithDurationAdded_long_int5() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(8000L, 0);
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testPlus_long1
    public void testPlus_long1() {
        Duration test = new Duration(123L);
        Duration result = test.plus(8000L);
        assertEquals(8123L, result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testPlus_long2
    public void testPlus_long2() {
        Duration test = new Duration(123L);
        Duration result = test.plus(0L);
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testMinus_long1
    public void testMinus_long1() {
        Duration test = new Duration(123L);
        Duration result = test.minus(8000L);
        assertEquals(123L - 8000L, result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testMinus_long2
    public void testMinus_long2() {
        Duration test = new Duration(123L);
        Duration result = test.minus(0L);
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_RD_int1
    public void testWithDurationAdded_RD_int1() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(new Duration(8000L), 1);
        assertEquals(8123L, result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_RD_int2
    public void testWithDurationAdded_RD_int2() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(new Duration(8000L), 2);
        assertEquals(16123L, result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_RD_int3
    public void testWithDurationAdded_RD_int3() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(new Duration(8000L), -1);
        assertEquals((123L - 8000L), result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_RD_int4
    public void testWithDurationAdded_RD_int4() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(new Duration(0L), 1);
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_RD_int5
    public void testWithDurationAdded_RD_int5() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(new Duration(8000L), 0);
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testWithDurationAdded_RD_int6
    public void testWithDurationAdded_RD_int6() {
        Duration test = new Duration(123L);
        Duration result = test.withDurationAdded(null, 0);
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testPlus_RD1
    public void testPlus_RD1() {
        Duration test = new Duration(123L);
        Duration result = test.plus(new Duration(8000L));
        assertEquals(8123L, result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testPlus_RD2
    public void testPlus_RD2() {
        Duration test = new Duration(123L);
        Duration result = test.plus(new Duration(0L));
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testPlus_RD3
    public void testPlus_RD3() {
        Duration test = new Duration(123L);
        Duration result = test.plus(null);
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testMinus_RD1
    public void testMinus_RD1() {
        Duration test = new Duration(123L);
        Duration result = test.minus(new Duration(8000L));
        assertEquals(123L - 8000L, result.getMillis());
    }

// org.joda.time.TestDuration_Basics::testMinus_RD2
    public void testMinus_RD2() {
        Duration test = new Duration(123L);
        Duration result = test.minus(new Duration(0L));
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testMinus_RD3
    public void testMinus_RD3() {
        Duration test = new Duration(123L);
        Duration result = test.minus(null);
        assertSame(test, result);
    }

// org.joda.time.TestDuration_Basics::testMutableDuration
    public void testMutableDuration() {
        
        MockMutableDuration test = new MockMutableDuration(123L);
        assertEquals(123L, test.getMillis());
        
        test.setMillis(2345L);
        assertEquals(2345L, test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testZERO
    public void testZERO() throws Throwable {
        Duration test = Duration.ZERO;
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new Duration(3200), Duration.parse("PT3.2S"));
        assertEquals(new Duration(6000), Duration.parse("PT6S"));
    }

// org.joda.time.TestDuration_Constructors::testFactory_standardDays_long
    public void testFactory_standardDays_long() throws Throwable {
        Duration test = Duration.standardDays(1);
        assertEquals(24L * 60L * 60L * 1000L, test.getMillis());
        
        test = Duration.standardDays(2);
        assertEquals(2L * 24L * 60L * 60L * 1000L, test.getMillis());
        
        test = Duration.standardDays(0);
        assertSame(Duration.ZERO, test);
    }

// org.joda.time.TestDuration_Constructors::testFactory_standardHours_long
    public void testFactory_standardHours_long() throws Throwable {
        Duration test = Duration.standardHours(1);
        assertEquals(60L * 60L * 1000L, test.getMillis());
        
        test = Duration.standardHours(2);
        assertEquals(2L * 60L * 60L * 1000L, test.getMillis());
        
        test = Duration.standardHours(0);
        assertSame(Duration.ZERO, test);
    }

// org.joda.time.TestDuration_Constructors::testFactory_standardMinutes_long
    public void testFactory_standardMinutes_long() throws Throwable {
        Duration test = Duration.standardMinutes(1);
        assertEquals(60L * 1000L, test.getMillis());
        
        test = Duration.standardMinutes(2);
        assertEquals(2L * 60L * 1000L, test.getMillis());
        
        test = Duration.standardMinutes(0);
        assertSame(Duration.ZERO, test);
    }

// org.joda.time.TestDuration_Constructors::testFactory_standardSeconds_long
    public void testFactory_standardSeconds_long() throws Throwable {
        Duration test = Duration.standardSeconds(1);
        assertEquals(1000L, test.getMillis());
        
        test = Duration.standardSeconds(2);
        assertEquals(2L * 1000L, test.getMillis());
        
        test = Duration.standardSeconds(0);
        assertSame(Duration.ZERO, test);
    }

// org.joda.time.TestDuration_Constructors::testFactory_millis_long
    public void testFactory_millis_long() throws Throwable {
        Duration test = Duration.millis(1);
        assertEquals(1L, test.getMillis());
        
        test = Duration.millis(2);
        assertEquals(2L, test.getMillis());
        
        test = Duration.millis(0);
        assertSame(Duration.ZERO, test);
    }

// org.joda.time.TestDuration_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Duration test = new Duration(length);
        assertEquals(length, test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testConstructor_long_long1
    public void testConstructor_long_long1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration test = new Duration(dt1.getMillis(), dt2.getMillis());
        assertEquals(dt2.getMillis() - dt1.getMillis(), test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testConstructor_RI_RI1
    public void testConstructor_RI_RI1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration test = new Duration(dt1, dt2);
        assertEquals(dt2.getMillis() - dt1.getMillis(), test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testConstructor_RI_RI2
    public void testConstructor_RI_RI2() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        Duration test = new Duration(dt1, dt2);
        assertEquals(dt2.getMillis() - TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testConstructor_RI_RI3
    public void testConstructor_RI_RI3() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        DateTime dt2 = null;  
        Duration test = new Duration(dt1, dt2);
        assertEquals(TEST_TIME_NOW - dt1.getMillis(), test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testConstructor_RI_RI4
    public void testConstructor_RI_RI4() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = null;  
        Duration test = new Duration(dt1, dt2);
        assertEquals(0L, test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        Duration test = new Duration("PT72.345S");
        assertEquals(72345, test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        Duration test = new Duration((Object) null);
        assertEquals(0L, test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testConstructor_Object3
    public void testConstructor_Object3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Long base = new Long(length);
        Duration test = new Duration(base);
        assertEquals(length, test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testConstructor_Object4
    public void testConstructor_Object4() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration base = new Duration(dt1, dt2);
        Duration test = new Duration(base);
        assertEquals(dt2.getMillis() - dt1.getMillis(), test.getMillis());
    }

// org.joda.time.TestDuration_Constructors::testConstructor_Object5
    public void testConstructor_Object5() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval base = new Interval(dt1, dt2);
        Duration test = new Duration(base);
        assertEquals(dt2.getMillis() - dt1.getMillis(), test.getMillis());
    }

// org.joda.time.TestHours::testConstants
    public void testConstants() {
        assertEquals(0, Hours.ZERO.getHours());
        assertEquals(1, Hours.ONE.getHours());
        assertEquals(2, Hours.TWO.getHours());
        assertEquals(3, Hours.THREE.getHours());
        assertEquals(4, Hours.FOUR.getHours());
        assertEquals(5, Hours.FIVE.getHours());
        assertEquals(6, Hours.SIX.getHours());
        assertEquals(7, Hours.SEVEN.getHours());
        assertEquals(8, Hours.EIGHT.getHours());
        assertEquals(Integer.MAX_VALUE, Hours.MAX_VALUE.getHours());
        assertEquals(Integer.MIN_VALUE, Hours.MIN_VALUE.getHours());
    }

// org.joda.time.TestHours::testFactory_hours_int
    public void testFactory_hours_int() {
        assertSame(Hours.ZERO, Hours.hours(0));
        assertSame(Hours.ONE, Hours.hours(1));
        assertSame(Hours.TWO, Hours.hours(2));
        assertSame(Hours.THREE, Hours.hours(3));
        assertSame(Hours.FOUR, Hours.hours(4));
        assertSame(Hours.FIVE, Hours.hours(5));
        assertSame(Hours.SIX, Hours.hours(6));
        assertSame(Hours.SEVEN, Hours.hours(7));
        assertSame(Hours.EIGHT, Hours.hours(8));
        assertSame(Hours.MAX_VALUE, Hours.hours(Integer.MAX_VALUE));
        assertSame(Hours.MIN_VALUE, Hours.hours(Integer.MIN_VALUE));
        assertEquals(-1, Hours.hours(-1).getHours());
        assertEquals(9, Hours.hours(9).getHours());
    }

// org.joda.time.TestHours::testFactory_hoursBetween_RInstant
    public void testFactory_hoursBetween_RInstant() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 9, 15, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 9, 18, 0, 0, 0, PARIS);
        
        assertEquals(3, Hours.hoursBetween(start, end1).getHours());
        assertEquals(0, Hours.hoursBetween(start, start).getHours());
        assertEquals(0, Hours.hoursBetween(end1, end1).getHours());
        assertEquals(-3, Hours.hoursBetween(end1, start).getHours());
        assertEquals(6, Hours.hoursBetween(start, end2).getHours());
    }

// org.joda.time.TestHours::testFactory_hoursBetween_RPartial
    public void testFactory_hoursBetween_RPartial() {
        LocalTime start = new LocalTime(12, 0);
        LocalTime end1 = new LocalTime(15, 0);
        TimeOfDay end2 = new TimeOfDay(18, 0);
        
        assertEquals(3, Hours.hoursBetween(start, end1).getHours());
        assertEquals(0, Hours.hoursBetween(start, start).getHours());
        assertEquals(0, Hours.hoursBetween(end1, end1).getHours());
        assertEquals(-3, Hours.hoursBetween(end1, start).getHours());
        assertEquals(6, Hours.hoursBetween(start, end2).getHours());
    }

// org.joda.time.TestHours::testFactory_hoursIn_RInterval
    public void testFactory_hoursIn_RInterval() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 9, 15, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 9, 18, 0, 0, 0, PARIS);
        
        assertEquals(0, Hours.hoursIn((ReadableInterval) null).getHours());
        assertEquals(3, Hours.hoursIn(new Interval(start, end1)).getHours());
        assertEquals(0, Hours.hoursIn(new Interval(start, start)).getHours());
        assertEquals(0, Hours.hoursIn(new Interval(end1, end1)).getHours());
        assertEquals(6, Hours.hoursIn(new Interval(start, end2)).getHours());
    }

// org.joda.time.TestHours::testFactory_standardHoursIn_RPeriod
    public void testFactory_standardHoursIn_RPeriod() {
        assertEquals(0, Hours.standardHoursIn((ReadablePeriod) null).getHours());
        assertEquals(0, Hours.standardHoursIn(Period.ZERO).getHours());
        assertEquals(1, Hours.standardHoursIn(new Period(0, 0, 0, 0, 1, 0, 0, 0)).getHours());
        assertEquals(123, Hours.standardHoursIn(Period.hours(123)).getHours());
        assertEquals(-987, Hours.standardHoursIn(Period.hours(-987)).getHours());
        assertEquals(1, Hours.standardHoursIn(Period.minutes(119)).getHours());
        assertEquals(2, Hours.standardHoursIn(Period.minutes(120)).getHours());
        assertEquals(2, Hours.standardHoursIn(Period.minutes(121)).getHours());
        assertEquals(48, Hours.standardHoursIn(Period.days(2)).getHours());
        try {
            Hours.standardHoursIn(Period.months(1));
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestHours::testFactory_parseHours_String
    public void testFactory_parseHours_String() {
        assertEquals(0, Hours.parseHours((String) null).getHours());
        assertEquals(0, Hours.parseHours("PT0H").getHours());
        assertEquals(1, Hours.parseHours("PT1H").getHours());
        assertEquals(-3, Hours.parseHours("PT-3H").getHours());
        assertEquals(2, Hours.parseHours("P0Y0M0DT2H").getHours());
        assertEquals(2, Hours.parseHours("PT2H0M").getHours());
        try {
            Hours.parseHours("P1Y1D");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Hours.parseHours("P1DT1H");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestHours::testGetMethods
    public void testGetMethods() {
        Hours test = Hours.hours(20);
        assertEquals(20, test.getHours());
    }

// org.joda.time.TestHours::testGetFieldType
    public void testGetFieldType() {
        Hours test = Hours.hours(20);
        assertEquals(DurationFieldType.hours(), test.getFieldType());
    }

// org.joda.time.TestHours::testGetPeriodType
    public void testGetPeriodType() {
        Hours test = Hours.hours(20);
        assertEquals(PeriodType.hours(), test.getPeriodType());
    }

// org.joda.time.TestHours::testIsGreaterThan
    public void testIsGreaterThan() {
        assertEquals(true, Hours.THREE.isGreaterThan(Hours.TWO));
        assertEquals(false, Hours.THREE.isGreaterThan(Hours.THREE));
        assertEquals(false, Hours.TWO.isGreaterThan(Hours.THREE));
        assertEquals(true, Hours.ONE.isGreaterThan(null));
        assertEquals(false, Hours.hours(-1).isGreaterThan(null));
    }

// org.joda.time.TestHours::testIsLessThan
    public void testIsLessThan() {
        assertEquals(false, Hours.THREE.isLessThan(Hours.TWO));
        assertEquals(false, Hours.THREE.isLessThan(Hours.THREE));
        assertEquals(true, Hours.TWO.isLessThan(Hours.THREE));
        assertEquals(false, Hours.ONE.isLessThan(null));
        assertEquals(true, Hours.hours(-1).isLessThan(null));
    }

// org.joda.time.TestHours::testToString
    public void testToString() {
        Hours test = Hours.hours(20);
        assertEquals("PT20H", test.toString());
        
        test = Hours.hours(-20);
        assertEquals("PT-20H", test.toString());
    }

// org.joda.time.TestHours::testSerialization
    public void testSerialization() throws Exception {
        Hours test = Hours.SEVEN;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Hours result = (Hours) ois.readObject();
        ois.close();
        
        assertSame(test, result);
    }

// org.joda.time.TestHours::testToStandardWeeks
    public void testToStandardWeeks() {
        Hours test = Hours.hours(24 * 7 * 2);
        Weeks expected = Weeks.weeks(2);
        assertEquals(expected, test.toStandardWeeks());
    }

// org.joda.time.TestHours::testToStandardDays
    public void testToStandardDays() {
        Hours test = Hours.hours(24 * 2);
        Days expected = Days.days(2);
        assertEquals(expected, test.toStandardDays());
    }

// org.joda.time.TestHours::testToStandardMinutes
    public void testToStandardMinutes() {
        Hours test = Hours.hours(3);
        Minutes expected = Minutes.minutes(3 * 60);
        assertEquals(expected, test.toStandardMinutes());
        
        try {
            Hours.MAX_VALUE.toStandardMinutes();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestHours::testToStandardSeconds
    public void testToStandardSeconds() {
        Hours test = Hours.hours(3);
        Seconds expected = Seconds.seconds(3 * 60 * 60);
        assertEquals(expected, test.toStandardSeconds());
        
        try {
            Hours.MAX_VALUE.toStandardSeconds();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestHours::testToStandardDuration
    public void testToStandardDuration() {
        Hours test = Hours.hours(20);
        Duration expected = new Duration(20L * DateTimeConstants.MILLIS_PER_HOUR);
        assertEquals(expected, test.toStandardDuration());
        
        expected = new Duration(((long) Integer.MAX_VALUE) * DateTimeConstants.MILLIS_PER_HOUR);
        assertEquals(expected, Hours.MAX_VALUE.toStandardDuration());
    }

// org.joda.time.TestHours::testPlus_int
    public void testPlus_int() {
        Hours test2 = Hours.hours(2);
        Hours result = test2.plus(3);
        assertEquals(2, test2.getHours());
        assertEquals(5, result.getHours());
        
        assertEquals(1, Hours.ONE.plus(0).getHours());
        
        try {
            Hours.MAX_VALUE.plus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestHours::testPlus_Hours
    public void testPlus_Hours() {
        Hours test2 = Hours.hours(2);
        Hours test3 = Hours.hours(3);
        Hours result = test2.plus(test3);
        assertEquals(2, test2.getHours());
        assertEquals(3, test3.getHours());
        assertEquals(5, result.getHours());
        
        assertEquals(1, Hours.ONE.plus(Hours.ZERO).getHours());
        assertEquals(1, Hours.ONE.plus((Hours) null).getHours());
        
        try {
            Hours.MAX_VALUE.plus(Hours.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestHours::testMinus_int
    public void testMinus_int() {
        Hours test2 = Hours.hours(2);
        Hours result = test2.minus(3);
        assertEquals(2, test2.getHours());
        assertEquals(-1, result.getHours());
        
        assertEquals(1, Hours.ONE.minus(0).getHours());
        
        try {
            Hours.MIN_VALUE.minus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestHours::testMinus_Hours
    public void testMinus_Hours() {
        Hours test2 = Hours.hours(2);
        Hours test3 = Hours.hours(3);
        Hours result = test2.minus(test3);
        assertEquals(2, test2.getHours());
        assertEquals(3, test3.getHours());
        assertEquals(-1, result.getHours());
        
        assertEquals(1, Hours.ONE.minus(Hours.ZERO).getHours());
        assertEquals(1, Hours.ONE.minus((Hours) null).getHours());
        
        try {
            Hours.MIN_VALUE.minus(Hours.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestHours::testMultipliedBy_int
    public void testMultipliedBy_int() {
        Hours test = Hours.hours(2);
        assertEquals(6, test.multipliedBy(3).getHours());
        assertEquals(2, test.getHours());
        assertEquals(-6, test.multipliedBy(-3).getHours());
        assertSame(test, test.multipliedBy(1));
        
        Hours halfMax = Hours.hours(Integer.MAX_VALUE / 2 + 1);
        try {
            halfMax.multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestHours::testDividedBy_int
    public void testDividedBy_int() {
        Hours test = Hours.hours(12);
        assertEquals(6, test.dividedBy(2).getHours());
        assertEquals(12, test.getHours());
        assertEquals(4, test.dividedBy(3).getHours());
        assertEquals(3, test.dividedBy(4).getHours());
        assertEquals(2, test.dividedBy(5).getHours());
        assertEquals(2, test.dividedBy(6).getHours());
        assertSame(test, test.dividedBy(1));
        
        try {
            Hours.ONE.dividedBy(0);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestHours::testNegated
    public void testNegated() {
        Hours test = Hours.hours(12);
        assertEquals(-12, test.negated().getHours());
        assertEquals(12, test.getHours());
        
        try {
            Hours.MIN_VALUE.negated();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestHours::testAddToLocalDate
    public void testAddToLocalDate() {
        Hours test = Hours.hours(26);
        LocalDateTime date = new LocalDateTime(2006, 6, 1, 0, 0, 0, 0);
        LocalDateTime expected = new LocalDateTime(2006, 6, 2, 2, 0, 0, 0);
        assertEquals(expected, date.plus(test));
    }

// org.joda.time.TestIllegalFieldValueException::testVerifyValueBounds
    public void testVerifyValueBounds() {
        try {
            FieldUtils.verifyValueBounds(ISOChronology.getInstance().monthOfYear(), -5, 1, 31);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.monthOfYear(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("monthOfYear", e.getFieldName());
            assertEquals(new Integer(-5), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("-5", e.getIllegalValueAsString());
            assertEquals(new Integer(1), e.getLowerBound());
            assertEquals(new Integer(31), e.getUpperBound());
        }

        try {
            FieldUtils.verifyValueBounds(DateTimeFieldType.hourOfDay(), 27, 0, 23);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.hourOfDay(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("hourOfDay", e.getFieldName());
            assertEquals(new Integer(27), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("27", e.getIllegalValueAsString());
            assertEquals(new Integer(0), e.getLowerBound());
            assertEquals(new Integer(23), e.getUpperBound());
        }

        try {
            FieldUtils.verifyValueBounds("foo", 1, 2, 3);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(null, e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("foo", e.getFieldName());
            assertEquals(new Integer(1), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("1", e.getIllegalValueAsString());
            assertEquals(new Integer(2), e.getLowerBound());
            assertEquals(new Integer(3), e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testSkipDateTimeField
    public void testSkipDateTimeField() {
        DateTimeField field = new SkipDateTimeField
            (ISOChronology.getInstanceUTC(), ISOChronology.getInstanceUTC().year(), 1970);
        try {
            field.set(0, 1970);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.year(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("year", e.getFieldName());
            assertEquals(new Integer(1970), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("1970", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testSetText
    public void testSetText() {
        try {
            ISOChronology.getInstanceUTC().year().set(0, null, java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.year(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("year", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("null", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            ISOChronology.getInstanceUTC().year().set(0, "nineteen seventy", java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.year(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("year", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals("nineteen seventy", e.getIllegalStringValue());
            assertEquals("nineteen seventy", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            ISOChronology.getInstanceUTC().era().set(0, "long ago", java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.era(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("era", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals("long ago", e.getIllegalStringValue());
            assertEquals("long ago", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            ISOChronology.getInstanceUTC().monthOfYear().set(0, "spring", java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.monthOfYear(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("monthOfYear", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals("spring", e.getIllegalStringValue());
            assertEquals("spring", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            ISOChronology.getInstanceUTC().dayOfWeek().set(0, "yesterday", java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.dayOfWeek(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("dayOfWeek", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals("yesterday", e.getIllegalStringValue());
            assertEquals("yesterday", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            ISOChronology.getInstanceUTC().halfdayOfDay().set(0, "morning", java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.halfdayOfDay(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("halfdayOfDay", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals("morning", e.getIllegalStringValue());
            assertEquals("morning", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testZoneTransition
    public void testZoneTransition() {
        DateTime dt = new DateTime
            (2005, 4, 3, 1, 0, 0, 0, DateTimeZone.forID("America/Los_Angeles"));
        try {
            dt.hourOfDay().setCopy(2);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.hourOfDay(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("hourOfDay", e.getFieldName());
            assertEquals(new Integer(2), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("2", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testJulianYearZero
    public void testJulianYearZero() {
        DateTime dt = new DateTime(JulianChronology.getInstanceUTC());
        try {
            dt.year().setCopy(0);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.year(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("year", e.getFieldName());
            assertEquals(new Integer(0), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("0", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testGJCutover
    public void testGJCutover() {
        DateTime dt = new DateTime("1582-10-04", GJChronology.getInstanceUTC());
        try {
            dt.dayOfMonth().setCopy(5);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.dayOfMonth(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("dayOfMonth", e.getFieldName());
            assertEquals(new Integer(5), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("5", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        dt = new DateTime("1582-10-15", GJChronology.getInstanceUTC());
        try {
            dt.dayOfMonth().setCopy(14);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.dayOfMonth(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("dayOfMonth", e.getFieldName());
            assertEquals(new Integer(14), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("14", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testReadablePartialValidate
    public void testReadablePartialValidate() {
        try {
            new YearMonthDay(1970, -5, 1);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.monthOfYear(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("monthOfYear", e.getFieldName());
            assertEquals(new Integer(-5), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("-5", e.getIllegalValueAsString());
            assertEquals(new Integer(1), e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            new YearMonthDay(1970, 500, 1);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.monthOfYear(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("monthOfYear", e.getFieldName());
            assertEquals(new Integer(500), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("500", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(new Integer(12), e.getUpperBound());
        }

        try {
            new YearMonthDay(1970, 2, 30);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.dayOfMonth(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("dayOfMonth", e.getFieldName());
            assertEquals(new Integer(30), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("30", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(new Integer(28), e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testOtherConstructors
    public void testOtherConstructors() {
        IllegalFieldValueException e = new IllegalFieldValueException
            (DurationFieldType.days(), new Integer(1), new Integer(2), new Integer(3));
        assertEquals(null, e.getDateTimeFieldType());
        assertEquals(DurationFieldType.days(), e.getDurationFieldType());
        assertEquals("days", e.getFieldName());
        assertEquals(new Integer(1), e.getIllegalNumberValue());
        assertEquals(null, e.getIllegalStringValue());
        assertEquals("1", e.getIllegalValueAsString());
        assertEquals(new Integer(2), e.getLowerBound());
        assertEquals(new Integer(3), e.getUpperBound());

        e = new IllegalFieldValueException(DurationFieldType.months(), "five");
        assertEquals(null, e.getDateTimeFieldType());
        assertEquals(DurationFieldType.months(), e.getDurationFieldType());
        assertEquals("months", e.getFieldName());
        assertEquals(null, e.getIllegalNumberValue());
        assertEquals("five", e.getIllegalStringValue());
        assertEquals("five", e.getIllegalValueAsString());
        assertEquals(null, e.getLowerBound());
        assertEquals(null, e.getUpperBound());

        e = new IllegalFieldValueException("months", "five");
        assertEquals(null, e.getDateTimeFieldType());
        assertEquals(null, e.getDurationFieldType());
        assertEquals("months", e.getFieldName());
        assertEquals(null, e.getIllegalNumberValue());
        assertEquals("five", e.getIllegalStringValue());
        assertEquals("five", e.getIllegalValueAsString());
        assertEquals(null, e.getLowerBound());
        assertEquals(null, e.getUpperBound());
    }

// org.joda.time.TestInstant_Constructors::test_now
    public void test_now() throws Throwable {
        Instant test = Instant.now();
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new DateTime(2010, 6, 30, 0, 20, ISOChronology.getInstance(LONDON)).toInstant(), Instant.parse("2010-06-30T01:20+02:00"));
        assertEquals(new DateTime(2010, 1, 2, 14, 50, ISOChronology.getInstance(LONDON)).toInstant(), Instant.parse("2010-002T14:50"));
    }

// org.joda.time.TestInstant_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--dd MM HH").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new DateTime(2010, 6, 30, 13, 0, ISOChronology.getInstance(PARIS)).toInstant(), Instant.parse("2010--30 06 13", f));
    }

// org.joda.time.TestInstant_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        Instant test = new Instant();
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        Instant test = new Instant(TEST_TIME1);
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        Instant test = new Instant(TEST_TIME2);
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1);
        Instant test = new Instant(date);
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testConstructor_invalidObject
    public void testConstructor_invalidObject() throws Throwable {
        try {
            new Instant(new Object());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInstant_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        Instant test = new Instant((Object) null);
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testConstructor_badconverterObject
    public void testConstructor_badconverterObject() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            Instant test = new Instant(new Integer(0));
            assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestInterval_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestInterval_Basics::testGetMillis
    public void testGetMillis() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1, test.getStart().getMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
        assertEquals(TEST_TIME2, test.getEnd().getMillis());
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDurationMillis());
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDuration().getMillis());
    }

// org.joda.time.TestInterval_Basics::testGetDuration1
    public void testGetDuration1() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDurationMillis());
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDuration().getMillis());
    }

// org.joda.time.TestInterval_Basics::testGetDuration2
    public void testGetDuration2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME1);
        assertSame(Duration.ZERO, test.toDuration());
    }

// org.joda.time.TestInterval_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        Interval test1 = new Interval(TEST_TIME1, TEST_TIME2);
        Interval test2 = new Interval(TEST_TIME1, TEST_TIME2);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        Interval test3 = new Interval(TEST_TIME_NOW, TEST_TIME2);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        Interval test4 = new Interval(TEST_TIME1, TEST_TIME2, GJChronology.getInstance());
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

// org.joda.time.TestInterval_Basics::testEqualsHashCodeLenient
    public void testEqualsHashCodeLenient() {
        Interval test1 = new Interval(
                new DateTime(TEST_TIME1, LenientChronology.getInstance(COPTIC_PARIS)),
                new DateTime(TEST_TIME2, LenientChronology.getInstance(COPTIC_PARIS)));
        Interval test2 = new Interval(
                new DateTime(TEST_TIME1, LenientChronology.getInstance(COPTIC_PARIS)),
                new DateTime(TEST_TIME2, LenientChronology.getInstance(COPTIC_PARIS)));
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
    }

// org.joda.time.TestInterval_Basics::testEqualsHashCodeStrict
    public void testEqualsHashCodeStrict() {
        Interval test1 = new Interval(
                new DateTime(TEST_TIME1, LenientChronology.getInstance(COPTIC_PARIS)),
                new DateTime(TEST_TIME2, LenientChronology.getInstance(COPTIC_PARIS)));
        Interval test2 = new Interval(
                new DateTime(TEST_TIME1, LenientChronology.getInstance(COPTIC_PARIS)),
                new DateTime(TEST_TIME2, LenientChronology.getInstance(COPTIC_PARIS)));
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
    }

// org.joda.time.TestInterval_Basics::test_useCase_ContainsOverlapAbutGap
    public void test_useCase_ContainsOverlapAbutGap() {
        
        
        
        
        Interval test1020 = new Interval(10, 20);
        
        
        Interval interval = new Interval(4, 8);
        assertNotNull(test1020.gap(interval));
        assertEquals(false, test1020.abuts(interval));
        assertEquals(false, test1020.overlaps(interval));
        assertEquals(false, test1020.contains(interval));
        assertNotNull(interval.gap(test1020));
        assertEquals(false, interval.abuts(test1020));
        assertEquals(false, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
        
        
        interval = new Interval(6, 10);
        assertNull(test1020.gap(interval));
        assertEquals(true, test1020.abuts(interval));
        assertEquals(false, test1020.overlaps(interval));
        assertEquals(false, test1020.contains(interval));
        assertNull(interval.gap(test1020));
        assertEquals(true, interval.abuts(test1020));
        assertEquals(false, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
        
        
        interval = new Interval(8, 12);
        assertNull(test1020.gap(interval));
        assertEquals(false, test1020.abuts(interval));
        assertEquals(true, test1020.overlaps(interval));
        assertEquals(false, test1020.contains(interval));
        assertNull(interval.gap(test1020));
        assertEquals(false, interval.abuts(test1020));
        assertEquals(true, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
        
        
        interval = new Interval(10, 14);
        assertNull(test1020.gap(interval));
        assertEquals(false, test1020.abuts(interval));
        assertEquals(true, test1020.overlaps(interval));
        assertEquals(true, test1020.contains(interval));
        assertNull(interval.gap(test1020));
        assertEquals(false, interval.abuts(test1020));
        assertEquals(true, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
        
        
        assertNull(test1020.gap(interval));
        assertEquals(false, test1020.abuts(test1020));
        assertEquals(true, test1020.overlaps(test1020));
        assertEquals(true, test1020.contains(test1020));
        
        
        interval = new Interval(16, 20);
        assertNull(test1020.gap(interval));
        assertEquals(false, test1020.abuts(interval));
        assertEquals(true, test1020.overlaps(interval));
        assertEquals(true, test1020.contains(interval));
        assertNull(interval.gap(test1020));
        assertEquals(false, interval.abuts(test1020));
        assertEquals(true, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
        
        
        interval = new Interval(18, 22);
        assertNull(test1020.gap(interval));
        assertEquals(false, test1020.abuts(interval));
        assertEquals(true, test1020.overlaps(interval));
        assertEquals(false, test1020.contains(interval));
        assertNull(interval.gap(test1020));
        assertEquals(false, interval.abuts(test1020));
        assertEquals(true, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
        
        
        interval = new Interval(20, 24);
        assertNull(test1020.gap(interval));
        assertEquals(true, test1020.abuts(interval));
        assertEquals(false, test1020.overlaps(interval));
        assertEquals(false, test1020.contains(interval));
        assertNull(interval.gap(test1020));
        assertEquals(true, interval.abuts(test1020));
        assertEquals(false, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
        
        
        interval = new Interval(22, 26);
        assertNotNull(test1020.gap(interval));
        assertEquals(false, test1020.abuts(interval));
        assertEquals(false, test1020.overlaps(interval));
        assertEquals(false, test1020.contains(interval));
        assertNotNull(interval.gap(test1020));
        assertEquals(false, interval.abuts(test1020));
        assertEquals(false, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
    }

// org.joda.time.TestInterval_Basics::test_useCase_ContainsOverlapAbutGap_zeroDuration
    public void test_useCase_ContainsOverlapAbutGap_zeroDuration() {
        
        
        
        
        
        Interval test1020 = new Interval(10, 20);
        
        
        Interval interval = new Interval(8, 8);
        assertNotNull(test1020.gap(interval));
        assertEquals(false, test1020.abuts(interval));
        assertEquals(false, test1020.overlaps(interval));
        assertEquals(false, test1020.contains(interval));
        assertNotNull(interval.gap(test1020));
        assertEquals(false, interval.abuts(test1020));
        assertEquals(false, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
        
        
        interval = new Interval(10, 10);
        assertNull(test1020.gap(interval));
        assertEquals(true,  test1020.abuts(interval));
        assertEquals(false, test1020.overlaps(interval));  
        assertEquals(true,  test1020.contains(interval));  
        assertNull(interval.gap(test1020));
        assertEquals(true,  interval.abuts(test1020));
        assertEquals(false, interval.overlaps(test1020));  
        assertEquals(false, interval.contains(test1020));  
        
        
        interval = new Interval(12, 12);
        assertNull(test1020.gap(interval));
        assertEquals(false, test1020.abuts(interval));
        assertEquals(true,  test1020.overlaps(interval));
        assertEquals(true,  test1020.contains(interval));  
        assertNull(interval.gap(test1020));
        assertEquals(false, interval.abuts(test1020));
        assertEquals(true,  interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));  
        
        
        interval = new Interval(20, 20);
        assertNull(test1020.gap(interval));
        assertEquals(true,  test1020.abuts(interval));
        assertEquals(false, test1020.overlaps(interval));
        assertEquals(false, test1020.contains(interval));
        assertNull(interval.gap(test1020));
        assertEquals(true,  interval.abuts(test1020));
        assertEquals(false, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
        
        
        interval = new Interval(22, 22);
        assertNotNull(test1020.gap(interval));
        assertEquals(false, test1020.abuts(interval));
        assertEquals(false, test1020.overlaps(interval));
        assertEquals(false, test1020.contains(interval));
        assertNotNull(interval.gap(test1020));
        assertEquals(false, interval.abuts(test1020));
        assertEquals(false, interval.overlaps(test1020));
        assertEquals(false, interval.contains(test1020));
    }

// org.joda.time.TestInterval_Basics::test_useCase_ContainsOverlapAbutGap_bothZeroDuration
    public void test_useCase_ContainsOverlapAbutGap_bothZeroDuration() {
        
        
        
        
        Interval test0808 = new Interval(8, 8);
        Interval test1010 = new Interval(10, 10);
        
        
        assertNotNull(test1010.gap(test0808));
        assertEquals(false, test1010.abuts(test0808));
        assertEquals(false, test1010.overlaps(test0808));
        assertEquals(false, test1010.contains(test0808));
        assertNotNull(test0808.gap(test1010));
        assertEquals(false, test0808.abuts(test1010));
        assertEquals(false, test0808.overlaps(test1010));
        assertEquals(false, test0808.contains(test1010));
        
        
        assertNull(test1010.gap(test1010));
        assertEquals(true,  test1010.abuts(test1010));
        assertEquals(false, test1010.overlaps(test1010));
        assertEquals(false, test1010.contains(test1010));
    }

// org.joda.time.TestInterval_Basics::testContains_long
    public void testContains_long() {
        assertEquals(false, interval37.contains(2));  
        assertEquals(true,  interval37.contains(3));
        assertEquals(true,  interval37.contains(4));
        assertEquals(true,  interval37.contains(5));
        assertEquals(true,  interval37.contains(6));
        assertEquals(false, interval37.contains(7));  
        assertEquals(false, interval37.contains(8));  
    }

// org.joda.time.TestInterval_Basics::testContains_long_zeroDuration
    public void testContains_long_zeroDuration() {
        assertEquals(false, interval33.contains(2));  
        assertEquals(false, interval33.contains(3));  
        assertEquals(false, interval33.contains(4));  
    }

// org.joda.time.TestInterval_Basics::testContainsNow
    public void testContainsNow() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.containsNow());  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(true,  interval37.containsNow());
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(true,  interval37.containsNow());
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(true,  interval37.containsNow());
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.containsNow());  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.containsNow());  
        
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval33.containsNow());  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval33.containsNow());  
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval33.containsNow());  
    }

// org.joda.time.TestInterval_Basics::testContains_RI
    public void testContains_RI() {
        assertEquals(false, interval37.contains(new Instant(2)));  
        assertEquals(true,  interval37.contains(new Instant(3)));
        assertEquals(true,  interval37.contains(new Instant(4)));
        assertEquals(true,  interval37.contains(new Instant(5)));
        assertEquals(true,  interval37.contains(new Instant(6)));
        assertEquals(false, interval37.contains(new Instant(7)));  
        assertEquals(false, interval37.contains(new Instant(8)));  
    }

// org.joda.time.TestInterval_Basics::testContains_RI_null
    public void testContains_RI_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.contains((ReadableInstant) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(true,  interval37.contains((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(true,  interval37.contains((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(true,  interval37.contains((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.contains((ReadableInstant) null));  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.contains((ReadableInstant) null));  
    }

// org.joda.time.TestInterval_Basics::testContains_RI_zeroDuration
    public void testContains_RI_zeroDuration() {
        assertEquals(false, interval33.contains(new Instant(2)));  
        assertEquals(false, interval33.contains(new Instant(3)));  
        assertEquals(false, interval33.contains(new Instant(4)));  
    }

// org.joda.time.TestInterval_Basics::testContains_RInterval
    public void testContains_RInterval() {
        assertEquals(false, interval37.contains(new Interval(1, 2)));  
        assertEquals(false, interval37.contains(new Interval(2, 2)));  
        
        assertEquals(false, interval37.contains(new Interval(2, 3)));  
        assertEquals(true,  interval37.contains(new Interval(3, 3)));
        
        assertEquals(false, interval37.contains(new Interval(2, 4)));  
        assertEquals(true,  interval37.contains(new Interval(3, 4)));
        assertEquals(true,  interval37.contains(new Interval(4, 4)));
        
        assertEquals(false, interval37.contains(new Interval(2, 6)));  
        assertEquals(true,  interval37.contains(new Interval(3, 6)));
        assertEquals(true,  interval37.contains(new Interval(4, 6)));
        assertEquals(true,  interval37.contains(new Interval(5, 6)));
        assertEquals(true,  interval37.contains(new Interval(6, 6)));
        
        assertEquals(false, interval37.contains(new Interval(2, 7)));  
        assertEquals(true,  interval37.contains(new Interval(3, 7)));
        assertEquals(true,  interval37.contains(new Interval(4, 7)));
        assertEquals(true,  interval37.contains(new Interval(5, 7)));
        assertEquals(true,  interval37.contains(new Interval(6, 7)));
        assertEquals(false, interval37.contains(new Interval(7, 7)));  
        
        assertEquals(false, interval37.contains(new Interval(2, 8)));  
        assertEquals(false, interval37.contains(new Interval(3, 8)));  
        assertEquals(false, interval37.contains(new Interval(4, 8)));  
        assertEquals(false, interval37.contains(new Interval(5, 8)));  
        assertEquals(false, interval37.contains(new Interval(6, 8)));  
        assertEquals(false, interval37.contains(new Interval(7, 8)));  
        assertEquals(false, interval37.contains(new Interval(8, 8)));  
        
        assertEquals(false, interval37.contains(new Interval(8, 9)));  
        assertEquals(false, interval37.contains(new Interval(9, 9)));  
    }

// org.joda.time.TestInterval_Basics::testContains_RInterval_null
    public void testContains_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.contains((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(true,  interval37.contains((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(true,  interval37.contains((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(true,  interval37.contains((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.contains((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.contains((ReadableInterval) null));  
    }

// org.joda.time.TestInterval_Basics::testContains_RInterval_zeroDuration
    public void testContains_RInterval_zeroDuration() {
        assertEquals(false, interval33.contains(interval33));  
        assertEquals(false, interval33.contains(interval37));  
        assertEquals(true,  interval37.contains(interval33));
        assertEquals(false, interval33.contains(new Interval(1, 2)));  
        assertEquals(false, interval33.contains(new Interval(8, 9)));  
        assertEquals(false, interval33.contains(new Interval(1, 9)));  
        
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval33.contains((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval33.contains((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval33.contains((ReadableInterval) null));  
    }

// org.joda.time.TestInterval_Basics::testOverlaps_RInterval
    public void testOverlaps_RInterval() {
        assertEquals(false, interval37.overlaps(new Interval(1, 2)));  
        assertEquals(false, interval37.overlaps(new Interval(2, 2)));  
        
        assertEquals(false, interval37.overlaps(new Interval(2, 3)));  
        assertEquals(false, interval37.overlaps(new Interval(3, 3)));  
        
        assertEquals(true,  interval37.overlaps(new Interval(2, 4)));
        assertEquals(true,  interval37.overlaps(new Interval(3, 4)));
        assertEquals(true,  interval37.overlaps(new Interval(4, 4)));
        
        assertEquals(true,  interval37.overlaps(new Interval(2, 6)));
        assertEquals(true,  interval37.overlaps(new Interval(3, 6)));
        assertEquals(true,  interval37.overlaps(new Interval(4, 6)));
        assertEquals(true,  interval37.overlaps(new Interval(5, 6)));
        assertEquals(true,  interval37.overlaps(new Interval(6, 6)));
        
        assertEquals(true,  interval37.overlaps(new Interval(2, 7)));
        assertEquals(true,  interval37.overlaps(new Interval(3, 7)));
        assertEquals(true,  interval37.overlaps(new Interval(4, 7)));
        assertEquals(true,  interval37.overlaps(new Interval(5, 7)));
        assertEquals(true,  interval37.overlaps(new Interval(6, 7)));
        assertEquals(false, interval37.overlaps(new Interval(7, 7)));  
        
        assertEquals(true,  interval37.overlaps(new Interval(2, 8)));
        assertEquals(true,  interval37.overlaps(new Interval(3, 8)));
        assertEquals(true,  interval37.overlaps(new Interval(4, 8)));
        assertEquals(true,  interval37.overlaps(new Interval(5, 8)));
        assertEquals(true,  interval37.overlaps(new Interval(6, 8)));
        assertEquals(false, interval37.overlaps(new Interval(7, 8)));  
        assertEquals(false, interval37.overlaps(new Interval(8, 8)));  
        
        assertEquals(false, interval37.overlaps(new Interval(8, 9)));  
        assertEquals(false, interval37.overlaps(new Interval(9, 9)));  
    }

// org.joda.time.TestInterval_Basics::testOverlaps_RInterval_null
    public void testOverlaps_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.overlaps((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.overlaps((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(true,  interval37.overlaps((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(true,  interval37.overlaps((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.overlaps((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.overlaps((ReadableInterval) null));  
        
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval33.overlaps((ReadableInterval) null));  
    }

// org.joda.time.TestInterval_Basics::testOverlaps_RInterval_zeroDuration
    public void testOverlaps_RInterval_zeroDuration() {
        assertEquals(false, interval33.overlaps(interval33));  
        assertEquals(false, interval33.overlaps(interval37));  
        assertEquals(false, interval37.overlaps(interval33));  
        assertEquals(false, interval33.overlaps(new Interval(1, 2)));
        assertEquals(false, interval33.overlaps(new Interval(8, 9)));
        assertEquals(true,  interval33.overlaps(new Interval(1, 9)));
    }
