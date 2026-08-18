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
// org.joda.time.TestAbstractPartial::testGetValue
    public void testGetValue() throws Throwable {
        MockPartial mock = new MockPartial();
        assertEquals(1970, mock.getValue(0));
        assertEquals(1, mock.getValue(1));
        
        try {
            mock.getValue(-1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            mock.getValue(2);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestAbstractPartial::testGetValues
    public void testGetValues() throws Throwable {
        MockPartial mock = new MockPartial();
        int[] vals = mock.getValues();
        assertEquals(2, vals.length);
        assertEquals(1970, vals[0]);
        assertEquals(1, vals[1]);
    }

// org.joda.time.TestAbstractPartial::testGetField
    public void testGetField() throws Throwable {
        MockPartial mock = new MockPartial();
        assertEquals(BuddhistChronology.getInstanceUTC().year(), mock.getField(0));
        assertEquals(BuddhistChronology.getInstanceUTC().monthOfYear(), mock.getField(1));
        
        try {
            mock.getField(-1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            mock.getField(2);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestAbstractPartial::testGetFieldType
    public void testGetFieldType() throws Throwable {
        MockPartial mock = new MockPartial();
        assertEquals(DateTimeFieldType.year(), mock.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), mock.getFieldType(1));
        
        try {
            mock.getFieldType(-1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            mock.getFieldType(2);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestAbstractPartial::testGetFieldTypes
    public void testGetFieldTypes() throws Throwable {
        MockPartial mock = new MockPartial();
        DateTimeFieldType[] vals = mock.getFieldTypes();
        assertEquals(2, vals.length);
        assertEquals(DateTimeFieldType.year(), vals[0]);
        assertEquals(DateTimeFieldType.monthOfYear(), vals[1]);
    }

// org.joda.time.TestAbstractPartial::testGetPropertyEquals
    public void testGetPropertyEquals() throws Throwable {
        MockProperty0 prop0 = new MockProperty0();
        assertEquals(true, prop0.equals(prop0));
        assertEquals(true, prop0.equals(new MockProperty0()));
        assertEquals(false, prop0.equals(new MockProperty1()));
        assertEquals(false, prop0.equals(new MockProperty0Val()));
        assertEquals(false, prop0.equals(new MockProperty0Field()));
        assertEquals(false, prop0.equals(new MockProperty0Chrono()));
        assertEquals(false, prop0.equals(""));
        assertEquals(false, prop0.equals(null));
    }

// org.joda.time.TestChronology::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestChronology::testEqualsHashCode_ISO
    public void testEqualsHashCode_ISO() {
        Chronology chrono1 = ISOChronology.getInstanceUTC();
        Chronology chrono2 = ISOChronology.getInstanceUTC();
        Chronology chrono3 = ISOChronology.getInstance();
        
        assertEquals(true, chrono1.equals(chrono2));
        assertEquals(false, chrono1.equals(chrono3));
        
        DateTime dt1 = new DateTime(0L, chrono1);
        DateTime dt2 = new DateTime(0L, chrono2);
        DateTime dt3 = new DateTime(0L, chrono3);
        
        assertEquals(true, dt1.equals(dt2));
        assertEquals(false, dt1.equals(dt3));
        
        assertEquals(true, chrono1.hashCode() == chrono2.hashCode());
        assertEquals(false, chrono1.hashCode() == chrono3.hashCode());
    }

// org.joda.time.TestChronology::testEqualsHashCode_Lenient
    public void testEqualsHashCode_Lenient() {
        Chronology chrono1 = LenientChronology.getInstance(ISOChronology.getInstanceUTC());
        Chronology chrono2 = LenientChronology.getInstance(ISOChronology.getInstanceUTC());
        Chronology chrono3 = LenientChronology.getInstance(ISOChronology.getInstance());
        
        assertEquals(true, chrono1.equals(chrono2));
        assertEquals(false, chrono1.equals(chrono3));
        
        DateTime dt1 = new DateTime(0L, chrono1);
        DateTime dt2 = new DateTime(0L, chrono2);
        DateTime dt3 = new DateTime(0L, chrono3);
        
        assertEquals(true, dt1.equals(dt2));
        assertEquals(false, dt1.equals(dt3));
        
        assertEquals(true, chrono1.hashCode() == chrono2.hashCode());
        assertEquals(false, chrono1.hashCode() == chrono3.hashCode());
    }

// org.joda.time.TestChronology::testEqualsHashCode_Strict
    public void testEqualsHashCode_Strict() {
        Chronology chrono1 = StrictChronology.getInstance(ISOChronology.getInstanceUTC());
        Chronology chrono2 = StrictChronology.getInstance(ISOChronology.getInstanceUTC());
        Chronology chrono3 = StrictChronology.getInstance(ISOChronology.getInstance());
        
        assertEquals(true, chrono1.equals(chrono2));
        assertEquals(false, chrono1.equals(chrono3));
        
        DateTime dt1 = new DateTime(0L, chrono1);
        DateTime dt2 = new DateTime(0L, chrono2);
        DateTime dt3 = new DateTime(0L, chrono3);
        
        assertEquals(true, dt1.equals(dt2));
        assertEquals(false, dt1.equals(dt3));
        
        assertEquals(true, chrono1.hashCode() == chrono2.hashCode());
        assertEquals(false, chrono1.hashCode() == chrono3.hashCode());
    }

// org.joda.time.TestChronology::testEqualsHashCode_Limit
    public void testEqualsHashCode_Limit() {
        DateTime lower = new DateTime(0L);
        DateTime higherA = new DateTime(1000000L);
        DateTime higherB = new DateTime(2000000L);
        
        Chronology chrono1 = LimitChronology.getInstance(ISOChronology.getInstanceUTC(), lower, higherA);
        Chronology chrono2A = LimitChronology.getInstance(ISOChronology.getInstanceUTC(), lower, higherA);
        Chronology chrono2B = LimitChronology.getInstance(ISOChronology.getInstanceUTC(), lower, higherB);
        Chronology chrono3 = LimitChronology.getInstance(ISOChronology.getInstance(), lower, higherA);
        
        assertEquals(true, chrono1.equals(chrono2A));
        assertEquals(false, chrono1.equals(chrono2B));
        assertEquals(false, chrono1.equals(chrono3));
        
        DateTime dt1 = new DateTime(0L, chrono1);
        DateTime dt2A = new DateTime(0L, chrono2A);
        DateTime dt2B = new DateTime(0L, chrono2B);
        DateTime dt3 = new DateTime(0L, chrono3);
        
        assertEquals(true, dt1.equals(dt2A));
        assertEquals(false, dt1.equals(dt2B));
        assertEquals(false, dt1.equals(dt3));
        
        assertEquals(true, chrono1.hashCode() == chrono2A.hashCode());
        assertEquals(false, chrono1.hashCode() == chrono2B.hashCode());
        assertEquals(false, chrono1.hashCode() == chrono3.hashCode());
    }

// org.joda.time.TestChronology::testEqualsHashCode_Zoned
    public void testEqualsHashCode_Zoned() {
        DateTimeZone zoneA = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zoneB = DateTimeZone.forID("Asia/Tokyo");
        
        Chronology chrono1 = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), zoneA);
        Chronology chrono2 = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), zoneA);
        Chronology chrono3 = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), zoneB);
        
        assertEquals(true, chrono1.equals(chrono2));
        assertEquals(false, chrono1.equals(chrono3));
        
        DateTime dt1 = new DateTime(0L, chrono1);
        DateTime dt2 = new DateTime(0L, chrono2);
        DateTime dt3 = new DateTime(0L, chrono3);
        
        assertEquals(true, dt1.equals(dt2));
        assertEquals(false, dt1.equals(dt3));
        
        assertEquals(true, chrono1.hashCode() == chrono2.hashCode());
        assertEquals(false, chrono1.hashCode() == chrono3.hashCode());
    }

// org.joda.time.TestChronology::testToString
    public void testToString() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        ISOChronology isoParis = ISOChronology.getInstance(paris);
        
        assertEquals("ISOChronology[Europe/Paris]", isoParis.toString());
        assertEquals("GJChronology[Europe/Paris]", GJChronology.getInstance(paris).toString());
        assertEquals("GregorianChronology[Europe/Paris]", GregorianChronology.getInstance(paris).toString());
        assertEquals("JulianChronology[Europe/Paris]", JulianChronology.getInstance(paris).toString());
        assertEquals("BuddhistChronology[Europe/Paris]", BuddhistChronology.getInstance(paris).toString());
        assertEquals("CopticChronology[Europe/Paris]", CopticChronology.getInstance(paris).toString());
        assertEquals("EthiopicChronology[Europe/Paris]", EthiopicChronology.getInstance(paris).toString());
        assertEquals("IslamicChronology[Europe/Paris]", IslamicChronology.getInstance(paris).toString());
        
        assertEquals("LenientChronology[ISOChronology[Europe/Paris]]", LenientChronology.getInstance(isoParis).toString());
        assertEquals("StrictChronology[ISOChronology[Europe/Paris]]", StrictChronology.getInstance(isoParis).toString());
        assertEquals("LimitChronology[ISOChronology[Europe/Paris], NoLimit, NoLimit]", LimitChronology.getInstance(isoParis, null, null).toString());
        assertEquals("ZonedChronology[ISOChronology[UTC], Europe/Paris]", ZonedChronology.getInstance(isoParis, paris).toString());
    }

// org.joda.time.TestDateMidnight_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW_UTC).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1_UTC).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2_UTC).toString());
    }

// org.joda.time.TestDateMidnight_Basics::testGet_DateTimeField
    public void testGet_DateTimeField() {
        DateMidnight test = new DateMidnight();
        assertEquals(1, test.get(ISO_DEFAULT.era()));
        assertEquals(20, test.get(ISO_DEFAULT.centuryOfEra()));
        assertEquals(2, test.get(ISO_DEFAULT.yearOfCentury()));
        assertEquals(2002, test.get(ISO_DEFAULT.yearOfEra()));
        assertEquals(2002, test.get(ISO_DEFAULT.year()));
        assertEquals(6, test.get(ISO_DEFAULT.monthOfYear()));
        assertEquals(9, test.get(ISO_DEFAULT.dayOfMonth()));
        assertEquals(2002, test.get(ISO_DEFAULT.weekyear()));
        assertEquals(23, test.get(ISO_DEFAULT.weekOfWeekyear()));
        assertEquals(7, test.get(ISO_DEFAULT.dayOfWeek()));
        assertEquals(160, test.get(ISO_DEFAULT.dayOfYear()));
        assertEquals(0, test.get(ISO_DEFAULT.halfdayOfDay()));
        assertEquals(0, test.get(ISO_DEFAULT.hourOfHalfday()));
        assertEquals(24, test.get(ISO_DEFAULT.clockhourOfDay()));
        assertEquals(12, test.get(ISO_DEFAULT.clockhourOfHalfday()));
        assertEquals(0, test.get(ISO_DEFAULT.hourOfDay()));
        assertEquals(0, test.get(ISO_DEFAULT.minuteOfHour()));
        assertEquals(0, test.get(ISO_DEFAULT.minuteOfDay()));
        assertEquals(0, test.get(ISO_DEFAULT.secondOfMinute()));
        assertEquals(0, test.get(ISO_DEFAULT.secondOfDay()));
        assertEquals(0, test.get(ISO_DEFAULT.millisOfSecond()));
        assertEquals(0, test.get(ISO_DEFAULT.millisOfDay()));
        try {
            test.get((DateTimeField) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        DateMidnight test = new DateMidnight();
        assertEquals(1, test.get(DateTimeFieldType.era()));
        assertEquals(20, test.get(DateTimeFieldType.centuryOfEra()));
        assertEquals(2, test.get(DateTimeFieldType.yearOfCentury()));
        assertEquals(2002, test.get(DateTimeFieldType.yearOfEra()));
        assertEquals(2002, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(2002, test.get(DateTimeFieldType.weekyear()));
        assertEquals(23, test.get(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(7, test.get(DateTimeFieldType.dayOfWeek()));
        assertEquals(160, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(0, test.get(DateTimeFieldType.halfdayOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(24, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(0, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(0, test.get(DateTimeFieldType.minuteOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(0, test.get(DateTimeFieldType.secondOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.millisOfSecond()));
        assertEquals(0, test.get(DateTimeFieldType.millisOfDay()));
        try {
            test.get((DateTimeFieldType) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Basics::testGetters
    public void testGetters() {
        DateMidnight test = new DateMidnight();
        
        assertEquals(ISO_DEFAULT, test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        
        assertEquals(1, test.getEra());
        assertEquals(20, test.getCenturyOfEra());
        assertEquals(2, test.getYearOfCentury());
        assertEquals(2002, test.getYearOfEra());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(2002, test.getWeekyear());
        assertEquals(23, test.getWeekOfWeekyear());
        assertEquals(7, test.getDayOfWeek());
        assertEquals(160, test.getDayOfYear());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getMinuteOfDay());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getSecondOfDay());
        assertEquals(0, test.getMillisOfSecond());
        assertEquals(0, test.getMillisOfDay());
    }

// org.joda.time.TestDateMidnight_Basics::testWithers
    public void testWithers() {
        DateMidnight test = new DateMidnight(1970, 6, 9, GJ_DEFAULT);
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

// org.joda.time.TestDateMidnight_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        DateMidnight test1 = new DateMidnight(TEST_TIME1_UTC);
        DateMidnight test2 = new DateMidnight(TEST_TIME1_UTC);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        DateMidnight test3 = new DateMidnight(TEST_TIME2_UTC);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(false, test1.equals(new DateMidnight(TEST_TIME1_UTC, GREGORIAN_DEFAULT)));
    }

// org.joda.time.TestDateMidnight_Basics::testCompareTo
    public void testCompareTo() {
        DateMidnight test1 = new DateMidnight(TEST_TIME1_UTC);
        DateMidnight test1a = new DateMidnight(TEST_TIME1_UTC);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        DateMidnight test2 = new DateMidnight(TEST_TIME2_UTC);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        DateMidnight test3 = new DateMidnight(TEST_TIME2_UTC, GREGORIAN_PARIS);
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(-1, test3.compareTo(test2));  
        
        assertEquals(+1, test2.compareTo(new MockInstant()));
        assertEquals(0, test1.compareTo(new MockInstant()));
        
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

    }

// org.joda.time.TestDateMidnight_Basics::testIsEqual
    public void testIsEqual() {
        DateMidnight test1 = new DateMidnight(TEST_TIME1_UTC);
        DateMidnight test1a = new DateMidnight(TEST_TIME1_UTC);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        DateMidnight test2 = new DateMidnight(TEST_TIME2_UTC);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        DateMidnight test3 = new DateMidnight(TEST_TIME2_UTC, GREGORIAN_PARIS);
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(false, test3.isEqual(test2));  
        
        assertEquals(false, test2.isEqual(new MockInstant()));
        assertEquals(true, test1.isEqual(new MockInstant()));
        
        assertEquals(false, new DateMidnight(TEST_TIME_NOW_UTC + DateTimeConstants.MILLIS_PER_DAY, DateTimeZone.UTC).isEqual(null));
        assertEquals(true, new DateMidnight(TEST_TIME_NOW_UTC, DateTimeZone.UTC).isEqual(null));
        assertEquals(false, new DateMidnight(TEST_TIME_NOW_UTC - DateTimeConstants.MILLIS_PER_DAY, DateTimeZone.UTC).isEqual(null));
        
        assertEquals(false, new DateMidnight(2004, 6, 9).isEqual(new DateTime(2004, 6, 8, 23, 59, 59, 999)));
        assertEquals(true, new DateMidnight(2004, 6, 9).isEqual(new DateTime(2004, 6, 9, 0, 0, 0, 0)));
        assertEquals(false, new DateMidnight(2004, 6, 9).isEqual(new DateTime(2004, 6, 9, 0, 0, 0, 1)));
    }

// org.joda.time.TestDateMidnight_Basics::testIsBefore
    public void testIsBefore() {
        DateMidnight test1 = new DateMidnight(TEST_TIME1_UTC);
        DateMidnight test1a = new DateMidnight(TEST_TIME1_UTC);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        DateMidnight test2 = new DateMidnight(TEST_TIME2_UTC);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        DateMidnight test3 = new DateMidnight(TEST_TIME2_UTC, GREGORIAN_PARIS);
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(true, test3.isBefore(test2));  
        
        assertEquals(false, test2.isBefore(new MockInstant()));
        assertEquals(false, test1.isBefore(new MockInstant()));
        
        assertEquals(false, new DateMidnight(TEST_TIME_NOW_UTC + DateTimeConstants.MILLIS_PER_DAY, DateTimeZone.UTC).isBefore(null));
        assertEquals(false, new DateMidnight(TEST_TIME_NOW_UTC, DateTimeZone.UTC).isBefore(null));
        assertEquals(true, new DateMidnight(TEST_TIME_NOW_UTC - DateTimeConstants.MILLIS_PER_DAY, DateTimeZone.UTC).isBefore(null));
        
        assertEquals(false, new DateMidnight(2004, 6, 9).isBefore(new DateTime(2004, 6, 8, 23, 59, 59, 999)));
        assertEquals(false, new DateMidnight(2004, 6, 9).isBefore(new DateTime(2004, 6, 9, 0, 0, 0, 0)));
        assertEquals(true, new DateMidnight(2004, 6, 9).isBefore(new DateTime(2004, 6, 9, 0, 0, 0, 1)));
    }

// org.joda.time.TestDateMidnight_Basics::testIsAfter
    public void testIsAfter() {
        DateMidnight test1 = new DateMidnight(TEST_TIME1_UTC);
        DateMidnight test1a = new DateMidnight(TEST_TIME1_UTC);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        DateMidnight test2 = new DateMidnight(TEST_TIME2_UTC);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        DateMidnight test3 = new DateMidnight(TEST_TIME2_UTC, GREGORIAN_PARIS);
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));  
        
        assertEquals(true, test2.isAfter(new MockInstant()));
        assertEquals(false, test1.isAfter(new MockInstant()));
        
        assertEquals(true, new DateMidnight(TEST_TIME_NOW_UTC + DateTimeConstants.MILLIS_PER_DAY, DateTimeZone.UTC).isAfter(null));
        assertEquals(false, new DateMidnight(TEST_TIME_NOW_UTC, DateTimeZone.UTC).isAfter(null));
        assertEquals(false, new DateMidnight(TEST_TIME_NOW_UTC - DateTimeConstants.MILLIS_PER_DAY, DateTimeZone.UTC).isAfter(null));
        
        assertEquals(true, new DateMidnight(2004, 6, 9).isAfter(new DateTime(2004, 6, 8, 23, 59, 59, 999)));
        assertEquals(false, new DateMidnight(2004, 6, 9).isAfter(new DateTime(2004, 6, 9, 0, 0, 0, 0)));
        assertEquals(false, new DateMidnight(2004, 6, 9).isAfter(new DateTime(2004, 6, 9, 0, 0, 0, 1)));
    }

// org.joda.time.TestDateMidnight_Basics::testSerialization
    public void testSerialization() throws Exception {
        DateMidnight test = new DateMidnight(TEST_TIME_NOW_UTC);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateMidnight result = (DateMidnight) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testToString
    public void testToString() {
        DateMidnight test = new DateMidnight(TEST_TIME_NOW_UTC);
        assertEquals("2002-06-09T00:00:00.000+01:00", test.toString());
        
        test = new DateMidnight(TEST_TIME_NOW_UTC, PARIS);
        assertEquals("2002-06-09T00:00:00.000+02:00", test.toString());
        
        test = new DateMidnight(TEST_TIME_NOW_UTC, NEWYORK);
        assertEquals("2002-06-08T00:00:00.000-04:00", test.toString());  
    }

// org.joda.time.TestDateMidnight_Basics::testToString_String
    public void testToString_String() {
        DateMidnight test = new DateMidnight(TEST_TIME_NOW_UTC);
        assertEquals("2002 00", test.toString("yyyy HH"));
        assertEquals("2002-06-09T00:00:00.000+01:00", test.toString((String) null));
    }

// org.joda.time.TestDateMidnight_Basics::testToString_String_String
    public void testToString_String_String() {
        DateMidnight test = new DateMidnight(TEST_TIME_NOW_UTC);
        assertEquals("Sun 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("dim. 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("2002-06-09T00:00:00.000+01:00", test.toString(null, Locale.ENGLISH));
        assertEquals("Sun 9/6", test.toString("EEE d/M", null));
        assertEquals("2002-06-09T00:00:00.000+01:00", test.toString(null, null));
    }

// org.joda.time.TestDateMidnight_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        DateMidnight test = new DateMidnight(TEST_TIME_NOW_UTC);
        assertEquals("2002 00", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09T00:00:00.000+01:00", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestDateMidnight_Basics::testToInstant
    public void testToInstant() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        Instant result = test.toInstant();
        assertEquals(TEST_TIME1_LONDON, result.getMillis());
    }

// org.joda.time.TestDateMidnight_Basics::testToDateTime
    public void testToDateTime() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        DateTime result = test.toDateTime();
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(TEST_TIME1_PARIS, result.getMillis());
        assertEquals(PARIS, result.getZone());
    }

// org.joda.time.TestDateMidnight_Basics::testToDateTimeISO
    public void testToDateTimeISO() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        DateTime result = test.toDateTimeISO();
        assertSame(DateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
    }

// org.joda.time.TestDateMidnight_Basics::testToDateTime_DateTimeZone
    public void testToDateTime_DateTimeZone() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        DateTime result = test.toDateTime(LONDON);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(TEST_TIME1_LONDON, result.getMillis());
        assertEquals(LONDON, result.getZone());

        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.toDateTime(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(TEST_TIME1_LONDON, result.getMillis());
        assertEquals(PARIS, result.getZone());

        test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        result = test.toDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(TEST_TIME1_PARIS, result.getMillis());
        assertEquals(LONDON, result.getZone());

        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.toDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(TEST_TIME1_LONDON, result.getMillis());
        assertEquals(LONDON, result.getZone());
    }

// org.joda.time.TestDateMidnight_Basics::testToDateTime_Chronology
    public void testToDateTime_Chronology() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        DateTime result = test.toDateTime(ISO_DEFAULT);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(TEST_TIME1_LONDON, result.getMillis());
        assertEquals(LONDON, result.getZone());

        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.toDateTime(GREGORIAN_PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(TEST_TIME1_LONDON, result.getMillis());
        assertEquals(GREGORIAN_PARIS, result.getChronology());

        test = new DateMidnight(TEST_TIME1_UTC, GREGORIAN_PARIS);
        result = test.toDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(TEST_TIME1_PARIS, result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.toDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(TEST_TIME1_LONDON, result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
    }

// org.joda.time.TestDateMidnight_Basics::testToMutableDateTime
    public void testToMutableDateTime() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        MutableDateTime result = test.toMutableDateTime();
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
    }

// org.joda.time.TestDateMidnight_Basics::testToMutableDateTimeISO
    public void testToMutableDateTimeISO() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        MutableDateTime result = test.toMutableDateTimeISO();
        assertSame(MutableDateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
    }

// org.joda.time.TestDateMidnight_Basics::testToMutableDateTime_DateTimeZone
    public void testToMutableDateTime_DateTimeZone() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        MutableDateTime result = test.toMutableDateTime(LONDON);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.toMutableDateTime(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());

        test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
    }

// org.joda.time.TestDateMidnight_Basics::testToMutableDateTime_Chronology
    public void testToMutableDateTime_Chronology() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        MutableDateTime result = test.toMutableDateTime(ISO_DEFAULT);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.toMutableDateTime(GREGORIAN_PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GREGORIAN_PARIS, result.getChronology());

        test = new DateMidnight(TEST_TIME1_UTC, GREGORIAN_PARIS);
        result = test.toMutableDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.toMutableDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
    }

// org.joda.time.TestDateMidnight_Basics::testToDate
    public void testToDate() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        Date result = test.toDate();
        assertEquals(test.getMillis(), result.getTime());
    }

// org.joda.time.TestDateMidnight_Basics::testToCalendar_Locale
    public void testToCalendar_Locale() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        Calendar result = test.toCalendar(null);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/London"), result.getTimeZone());

        test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        result = test.toCalendar(null);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());

        test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        result = test.toCalendar(Locale.UK);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());
    }

// org.joda.time.TestDateMidnight_Basics::testToGregorianCalendar
    public void testToGregorianCalendar() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        GregorianCalendar result = test.toGregorianCalendar();
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/London"), result.getTimeZone());

        test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        result = test.toGregorianCalendar();
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());
    }

// org.joda.time.TestDateMidnight_Basics::testToYearMonthDay
    public void testToYearMonthDay() {
        DateMidnight base = new DateMidnight(TEST_TIME1_UTC, COPTIC_DEFAULT);
        YearMonthDay test = base.toYearMonthDay();
        assertEquals(new YearMonthDay(TEST_TIME1_UTC, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateMidnight_Basics::testToLocalDate
    public void testToLocalDate() {
        DateMidnight base = new DateMidnight(TEST_TIME1_UTC, COPTIC_DEFAULT);
        LocalDate test = base.toLocalDate();
        assertEquals(new LocalDate(TEST_TIME1_UTC, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateMidnight_Basics::testToInterval
    public void testToInterval() {
        DateMidnight base = new DateMidnight(TEST_TIME1_UTC, COPTIC_DEFAULT);
        Interval test = base.toInterval();
        DateMidnight end = base.plus(Period.days(1));
        assertEquals(new Interval(base, end), test);
    }

// org.joda.time.TestDateMidnight_Basics::testWithMillis_long
    public void testWithMillis_long() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        DateMidnight result = test.withMillis(TEST_TIME2_UTC);
        assertEquals(TEST_TIME2_LONDON, result.getMillis());
        assertEquals(test.getChronology(), result.getChronology());
        
        test = new DateMidnight(TEST_TIME1_UTC, GREGORIAN_PARIS);
        result = test.withMillis(TEST_TIME2_UTC);
        assertEquals(TEST_TIME2_PARIS, result.getMillis());
        assertEquals(test.getChronology(), result.getChronology());
        
        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.withMillis(TEST_TIME1_UTC);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testWithChronology_Chronology
    public void testWithChronology_Chronology() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        DateMidnight result = test.withChronology(GREGORIAN_PARIS);
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
        assertEquals(TEST_TIME1_PARIS, result.getMillis());
        assertEquals(GREGORIAN_PARIS, result.getChronology());
        
        test = new DateMidnight(TEST_TIME1_UTC, GREGORIAN_PARIS);
        result = test.withChronology(null);
        assertEquals(TEST_TIME1_PARIS, test.getMillis());
        
        assertEquals(TEST_TIME1_LONDON - DateTimeConstants.MILLIS_PER_DAY, result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
        
        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.withChronology(null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
        
        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.withChronology(ISO_DEFAULT);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testWithZoneRetainFields_DateTimeZone
    public void testWithZoneRetainFields_DateTimeZone() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        DateMidnight result = test.withZoneRetainFields(PARIS);
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
        assertEquals(TEST_TIME1_PARIS, result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
        
        test = new DateMidnight(TEST_TIME1_UTC, GREGORIAN_PARIS);
        result = test.withZoneRetainFields(null);
        assertEquals(TEST_TIME1_PARIS, test.getMillis());
        assertEquals(TEST_TIME1_LONDON, result.getMillis());
        assertEquals(GREGORIAN_DEFAULT, result.getChronology());
        
        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.withZoneRetainFields(LONDON);
        assertSame(test, result);
        
        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.withZoneRetainFields(null);
        assertSame(test, result);
        
        test = new DateMidnight(TEST_TIME1_UTC, new MockNullZoneChronology());
        result = test.withZoneRetainFields(LONDON);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testWithFields_RPartial
    public void testWithFields_RPartial() {
        DateMidnight test = new DateMidnight(2004, 5, 6);
        DateMidnight result = test.withFields(new YearMonthDay(2003, 4, 5));
        DateMidnight expected = new DateMidnight(2003, 4, 5);
        assertEquals(expected, result);
        
        test = new DateMidnight(TEST_TIME1_UTC);
        result = test.withFields(null);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testWithField1
    public void testWithField1() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new DateMidnight(2004, 6, 9), test);
        assertEquals(new DateMidnight(2006, 6, 9), result);
    }

// org.joda.time.TestDateMidnight_Basics::testWithField2
    public void testWithField2() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Basics::testWithFieldAdded1
    public void testWithFieldAdded1() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new DateMidnight(2004, 6, 9), test);
        assertEquals(new DateMidnight(2010, 6, 9), result);
    }

// org.joda.time.TestDateMidnight_Basics::testWithFieldAdded2
    public void testWithFieldAdded2() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Basics::testWithFieldAdded3
    public void testWithFieldAdded3() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Basics::testWithFieldAdded4
    public void testWithFieldAdded4() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testWithDurationAdded_long_int
    public void testWithDurationAdded_long_int() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, BUDDHIST_DEFAULT);
        DateMidnight result = test.withDurationAdded(123456789L, 1);
        DateMidnight expected = new DateMidnight(test.getMillis() + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(123456789L, 0);
        assertSame(test, result);
        
        result = test.withDurationAdded(123456789L, 2);
        expected = new DateMidnight(test.getMillis() + (2L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(123456789L, -3);
        expected = new DateMidnight(test.getMillis() - (3L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateMidnight_Basics::testWithDurationAdded_RD_int
    public void testWithDurationAdded_RD_int() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, BUDDHIST_DEFAULT);
        DateMidnight result = test.withDurationAdded(new Duration(123456789L), 1);
        DateMidnight expected = new DateMidnight(test.getMillis() + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(null, 1);
        assertSame(test, result);
        
        result = test.withDurationAdded(new Duration(123456789L), 0);
        assertSame(test, result);
        
        result = test.withDurationAdded(new Duration(123456789L), 2);
        expected = new DateMidnight(test.getMillis() + (2L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(new Duration(123456789L), -3);
        expected = new DateMidnight(test.getMillis() - (3L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateMidnight_Basics::testWithDurationAdded_RP_int
    public void testWithDurationAdded_RP_int() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.withPeriodAdded(new Period(1, 2, 3, 4, 5, 6, 7, 8), 1);
        DateMidnight expected = new DateMidnight(2003, 7, 28, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withPeriodAdded(null, 1);
        assertSame(test, result);
        
        result = test.withPeriodAdded(new Period(1, 2, 3, 4, 5, 6, 7, 8), 0);
        assertSame(test, result);
        
        result = test.withPeriodAdded(new Period(1, 2, 0, 4, 5, 6, 7, 8), 3);
        expected = new DateMidnight(2005, 11, 15, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withPeriodAdded(new Period(1, 2, 0, 1, 1, 2, 3, 4), -1);
        expected = new DateMidnight(2001, 3, 1, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateMidnight_Basics::testPlus_long
    public void testPlus_long() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, BUDDHIST_DEFAULT);
        DateMidnight result = test.plus(123456789L);
        DateMidnight expected = new DateMidnight(test.getMillis() + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateMidnight_Basics::testPlus_RD
    public void testPlus_RD() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, BUDDHIST_DEFAULT);
        DateMidnight result = test.plus(new Duration(123456789L));
        DateMidnight expected = new DateMidnight(test.getMillis() + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plus((ReadableDuration) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testPlus_RP
    public void testPlus_RP() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        DateMidnight expected = new DateMidnight(2003, 7, 28, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testPlusYears_int
    public void testPlusYears_int() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.plusYears(1);
        DateMidnight expected = new DateMidnight(2003, 5, 3, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.plusMonths(1);
        DateMidnight expected = new DateMidnight(2002, 6, 3, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testPlusWeeks_int
    public void testPlusWeeks_int() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.plusWeeks(1);
        DateMidnight expected = new DateMidnight(2002, 5, 10, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testPlusDays_int
    public void testPlusDays_int() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.plusDays(1);
        DateMidnight expected = new DateMidnight(2002, 5, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testMinus_long
    public void testMinus_long() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, BUDDHIST_DEFAULT);
        DateMidnight result = test.minus(123456789L);
        DateMidnight expected = new DateMidnight(test.getMillis() - 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateMidnight_Basics::testMinus_RD
    public void testMinus_RD() {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, BUDDHIST_DEFAULT);
        DateMidnight result = test.minus(new Duration(123456789L));
        DateMidnight expected = new DateMidnight(test.getMillis() - 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minus((ReadableDuration) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testMinus_RP
    public void testMinus_RP() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        DateMidnight expected = new DateMidnight(2001, 3, 25, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testMinusYears_int
    public void testMinusYears_int() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.minusYears(1);
        DateMidnight expected = new DateMidnight(2001, 5, 3, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.minusMonths(1);
        DateMidnight expected = new DateMidnight(2002, 4, 3, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testMinusWeeks_int
    public void testMinusWeeks_int() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.minusWeeks(1);
        DateMidnight expected = new DateMidnight(2002, 4, 26, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testMinusDays_int
    public void testMinusDays_int() {
        DateMidnight test = new DateMidnight(2002, 5, 3, BUDDHIST_DEFAULT);
        DateMidnight result = test.minusDays(1);
        DateMidnight expected = new DateMidnight(2002, 5, 2, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateMidnight_Basics::testProperty
    public void testProperty() {
        DateMidnight test = new DateMidnight();
        assertEquals(test.year(), test.property(DateTimeFieldType.year()));
        assertEquals(test.dayOfWeek(), test.property(DateTimeFieldType.dayOfWeek()));
        assertEquals(test.weekOfWeekyear(), test.property(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(test.property(DateTimeFieldType.millisOfSecond()), test.property(DateTimeFieldType.millisOfSecond()));
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

// org.joda.time.TestDateMidnight_Constructors::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW_UTC).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1_UTC).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2_UTC).toString());
    }

// org.joda.time.TestDateMidnight_Constructors::test_now
    public void test_now() throws Throwable {
        DateMidnight test = DateMidnight.now();
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Constructors::test_now_DateTimeZone
    public void test_now_DateTimeZone() throws Throwable {
        DateMidnight test = DateMidnight.now(PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::test_now_nullDateTimeZone
    public void test_now_nullDateTimeZone() throws Throwable {
        try {
            DateMidnight.now((DateTimeZone) null);
            fail();
        } catch (NullPointerException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::test_now_Chronology
    public void test_now_Chronology() throws Throwable {
        DateMidnight test = DateMidnight.now(GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::test_now_nullChronology
    public void test_now_nullChronology() throws Throwable {
        try {
            DateMidnight.now((Chronology) null);
            fail();
        } catch (NullPointerException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new DateMidnight(2010, 6, 30, ISOChronology.getInstance(LONDON)), DateMidnight.parse("2010-06-30"));
        assertEquals(new DateMidnight(2010, 1, 2, ISOChronology.getInstance(LONDON)), DateMidnight.parse("2010-002"));
    }

// org.joda.time.TestDateMidnight_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        assertEquals(new DateMidnight(2010, 6, 30, ISOChronology.getInstance(LONDON)), DateMidnight.parse("2010--30 06", DateTimeFormat.forPattern("yyyy--dd MM")));
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        DateMidnight test = new DateMidnight();
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight((DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        DateMidnight test = new DateMidnight(GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        DateMidnight test = new DateMidnight((Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME2_UTC);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long1_DateTimeZone
    public void testConstructor_long1_DateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long2_DateTimeZone
    public void testConstructor_long2_DateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME2_UTC, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME2_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long_nullDateTimeZone
    public void testConstructor_long_nullDateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME2_UTC, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1_UTC);
        DateMidnight test = new DateMidnight(date);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_invalidObject
    public void testConstructor_invalidObject() throws Throwable {
        try {
            new DateMidnight(new Object());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        DateMidnight test = new DateMidnight((Object) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_badconverterObject
    public void testConstructor_badconverterObject() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateMidnight test = new DateMidnight(new Integer(0));
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L - DateTimeConstants.MILLIS_PER_HOUR, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Object_DateTimeZone
    public void testConstructor_Object_DateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1_UTC);
        DateMidnight test = new DateMidnight(date, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_invalidObject_DateTimeZone
    public void testConstructor_invalidObject_DateTimeZone() throws Throwable {
        try {
            new DateMidnight(new Object(), PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullObject_DateTimeZone
    public void testConstructor_nullObject_DateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight((Object) null, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Object_nullDateTimeZone
    public void testConstructor_Object_nullDateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1_UTC);
        DateMidnight test = new DateMidnight(date, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullObject_nullDateTimeZone
    public void testConstructor_nullObject_nullDateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight((Object) null, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_badconverterObject_DateTimeZone
    public void testConstructor_badconverterObject_DateTimeZone() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateMidnight test = new DateMidnight(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L - DateTimeConstants.MILLIS_PER_HOUR, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1_UTC);
        DateMidnight test = new DateMidnight(date, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_invalidObject_Chronology
    public void testConstructor_invalidObject_Chronology() throws Throwable {
        try {
            new DateMidnight(new Object(), GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        DateMidnight test = new DateMidnight((Object) null, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1_UTC);
        DateMidnight test = new DateMidnight(date, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        DateMidnight test = new DateMidnight((Object) null, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_badconverterObject_Chronology
    public void testConstructor_badconverterObject_Chronology() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateMidnight test = new DateMidnight(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L - DateTimeConstants.MILLIS_PER_HOUR, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_int_int_int
    public void testConstructor_int_int_int() throws Throwable {
        DateMidnight test = new DateMidnight(2002, 6, 9);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new DateMidnight(Integer.MIN_VALUE, 6, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(Integer.MAX_VALUE, 6, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 0, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 13, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 31);
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateMidnight(2002, 7, 31);
        try {
            new DateMidnight(2002, 7, 32);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_DateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(2002, 6, 9, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW_PARIS, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new DateMidnight(Integer.MIN_VALUE, 6, 9, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(Integer.MAX_VALUE, 6, 9, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 0, 9, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 13, 9, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 31, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateMidnight(2002, 7, 31, PARIS);
        try {
            new DateMidnight(2002, 7, 32, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_nullDateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(2002, 6, 9, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_int_int_int_Chronology
    public void testConstructor_int_int_int_Chronology() throws Throwable {
        DateMidnight test = new DateMidnight(2002, 6, 9, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new DateMidnight(Integer.MIN_VALUE, 6, 9, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(Integer.MAX_VALUE, 6, 9, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 0, 9, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 13, 9, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 31, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateMidnight(2002, 7, 31, GregorianChronology.getInstance());
        try {
            new DateMidnight(2002, 7, 32, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_int_int_int_nullChronology
    public void testConstructor_int_int_int_nullChronology() throws Throwable {
        DateMidnight test = new DateMidnight(2002, 6, 9, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestDateTimeComparator::testClass
    public void testClass() {
        assertEquals(true, Modifier.isPublic(DateTimeComparator.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DateTimeComparator.class.getModifiers()));
        assertEquals(1, DateTimeComparator.class.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(DateTimeComparator.class.getDeclaredConstructors()[0].getModifiers()));
    }

// org.joda.time.TestDateTimeComparator::testStaticGetInstance
    public void testStaticGetInstance() {
        DateTimeComparator c = DateTimeComparator.getInstance();
        assertEquals(null, c.getLowerLimit());
        assertEquals(null, c.getUpperLimit());
        assertEquals("DateTimeComparator[]", c.toString());
    }

// org.joda.time.TestDateTimeComparator::testStaticGetDateOnlyInstance
    public void testStaticGetDateOnlyInstance() {
        DateTimeComparator c = DateTimeComparator.getDateOnlyInstance();
        assertEquals(DateTimeFieldType.dayOfYear(), c.getLowerLimit());
        assertEquals(null, c.getUpperLimit());
        assertEquals("DateTimeComparator[dayOfYear-]", c.toString());
        
        assertSame(DateTimeComparator.getDateOnlyInstance(), DateTimeComparator.getDateOnlyInstance());
    }

// org.joda.time.TestDateTimeComparator::testStaticGetTimeOnlyInstance
    public void testStaticGetTimeOnlyInstance() {
        DateTimeComparator c = DateTimeComparator.getTimeOnlyInstance();
        assertEquals(null, c.getLowerLimit());
        assertEquals(DateTimeFieldType.dayOfYear(), c.getUpperLimit());
        assertEquals("DateTimeComparator[-dayOfYear]", c.toString());
        
        assertSame(DateTimeComparator.getTimeOnlyInstance(), DateTimeComparator.getTimeOnlyInstance());
    }

// org.joda.time.TestDateTimeComparator::testStaticGetInstanceLower
    public void testStaticGetInstanceLower() {
        DateTimeComparator c = DateTimeComparator.getInstance(DateTimeFieldType.hourOfDay());
        assertEquals(DateTimeFieldType.hourOfDay(), c.getLowerLimit());
        assertEquals(null, c.getUpperLimit());
        assertEquals("DateTimeComparator[hourOfDay-]", c.toString());
        
        c = DateTimeComparator.getInstance(null);
        assertSame(DateTimeComparator.getInstance(), c);
    }

// org.joda.time.TestDateTimeComparator::testStaticGetInstanceLowerUpper
    public void testStaticGetInstanceLowerUpper() {
        DateTimeComparator c = DateTimeComparator.getInstance(DateTimeFieldType.hourOfDay(), DateTimeFieldType.dayOfYear());
        assertEquals(DateTimeFieldType.hourOfDay(), c.getLowerLimit());
        assertEquals(DateTimeFieldType.dayOfYear(), c.getUpperLimit());
        assertEquals("DateTimeComparator[hourOfDay-dayOfYear]", c.toString());
        
        c = DateTimeComparator.getInstance(DateTimeFieldType.hourOfDay(), DateTimeFieldType.hourOfDay());
        assertEquals(DateTimeFieldType.hourOfDay(), c.getLowerLimit());
        assertEquals(DateTimeFieldType.hourOfDay(), c.getUpperLimit());
        assertEquals("DateTimeComparator[hourOfDay]", c.toString());
        
        c = DateTimeComparator.getInstance(null, null);
        assertSame(DateTimeComparator.getInstance(), c);
        
        c = DateTimeComparator.getInstance(DateTimeFieldType.dayOfYear(), null);
        assertSame(DateTimeComparator.getDateOnlyInstance(), c);
        
        c = DateTimeComparator.getInstance(null, DateTimeFieldType.dayOfYear());
        assertSame(DateTimeComparator.getTimeOnlyInstance(), c);
    }

// org.joda.time.TestDateTimeComparator::testEqualsHashCode
    public void testEqualsHashCode() {
        DateTimeComparator c1 = DateTimeComparator.getInstance();
        assertEquals(true, c1.equals(c1));
        assertEquals(false, c1.equals(null));
        assertEquals(true, c1.hashCode() == c1.hashCode());
        
        DateTimeComparator c2 = DateTimeComparator.getTimeOnlyInstance();
        assertEquals(true, c2.equals(c2));
        assertEquals(false, c2.equals(c1));
        assertEquals(false, c1.equals(c2));
        assertEquals(false, c2.equals(null));
        assertEquals(false, c1.hashCode() == c2.hashCode());
        
        DateTimeComparator c3 = DateTimeComparator.getTimeOnlyInstance();
        assertEquals(true, c3.equals(c3));
        assertEquals(false, c3.equals(c1));
        assertEquals(true, c3.equals(c2));
        assertEquals(false, c1.equals(c3));
        assertEquals(true, c2.equals(c3));
        assertEquals(false, c1.hashCode() == c3.hashCode());
        assertEquals(true, c2.hashCode() == c3.hashCode());
        
        DateTimeComparator c4 = DateTimeComparator.getDateOnlyInstance();
        assertEquals(false, c4.hashCode() == c3.hashCode());
    }

// org.joda.time.TestDateTimeComparator::testSerialization1
    public void testSerialization1() throws Exception {
        DateTimeField f = ISO.dayOfYear();
        f.toString();
        DateTimeComparator c = DateTimeComparator.getInstance(DateTimeFieldType.hourOfDay(), DateTimeFieldType.dayOfYear());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(c);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeComparator result = (DateTimeComparator) ois.readObject();
        ois.close();
        
        assertEquals(c, result);
    }

// org.joda.time.TestDateTimeComparator::testSerialization2
    public void testSerialization2() throws Exception {
        DateTimeComparator c = DateTimeComparator.getInstance();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(c);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeComparator result = (DateTimeComparator) ois.readObject();
        ois.close();
        
        assertSame(c, result);
    }

// org.joda.time.TestDateTimeComparator::testBasicComps1
    public void testBasicComps1() {
        aDateTime = new DateTime( System.currentTimeMillis(), DateTimeZone.UTC );
        bDateTime = new DateTime( aDateTime.getMillis(), DateTimeZone.UTC );
        assertEquals( "getMillis", aDateTime.getMillis(),
            bDateTime.getMillis() );
        assertEquals( "MILLIS", 0, cMillis.compare( aDateTime, bDateTime ) );
        assertEquals( "SECOND", 0, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "MINUTE", 0, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "HOUR", 0, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "DOW", 0, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOM", 0, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOY", 0, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOW", 0, cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WY", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTH", 0, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "YEAR", 0, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DATE", 0, cDate.compare( aDateTime, bDateTime ) );
        assertEquals( "TIME", 0, cTime.compare( aDateTime, bDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testBasicComps2
    public void testBasicComps2() {
        ReadableInstant aDateTime = new DateTime( System.currentTimeMillis(), DateTimeZone.UTC );
        ReadableInstant bDateTime = new DateTime( aDateTime.getMillis(), DateTimeZone.UTC );
        assertEquals( "getMillis", aDateTime.getMillis(),
            bDateTime.getMillis() );
        assertEquals( "MILLIS", 0, cMillis.compare( aDateTime, bDateTime ) );
        assertEquals( "SECOND", 0, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "MINUTE", 0, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "HOUR", 0, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "DOW", 0, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOM", 0, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOY", 0, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOW", 0, cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WY", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTH", 0, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "YEAR", 0, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DATE", 0, cDate.compare( aDateTime, bDateTime ) );
        assertEquals( "TIME", 0, cTime.compare( aDateTime, bDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testBasicComps3
    public void testBasicComps3() {
        Date aDateTime
            = new Date( System.currentTimeMillis() );
        Date bDateTime
            = new Date( aDateTime.getTime() );
        assertEquals( "MILLIS", 0, cMillis.compare( aDateTime, bDateTime ) );
        assertEquals( "SECOND", 0, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "MINUTE", 0, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "HOUR", 0, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "DOW", 0, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOM", 0, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOY", 0, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOW", 0, cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WY", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTH", 0, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "YEAR", 0, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DATE", 0, cDate.compare( aDateTime, bDateTime ) );
        assertEquals( "TIME", 0, cTime.compare( aDateTime, bDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testBasicComps4
    public void testBasicComps4() {
        Long aDateTime
            = new Long( System.currentTimeMillis() );
        Long bDateTime
            = new Long( aDateTime.longValue() );
        assertEquals( "MILLIS", 0, cMillis.compare( aDateTime, bDateTime ) );
        assertEquals( "SECOND", 0, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "MINUTE", 0, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "HOUR", 0, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "DOW", 0, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOM", 0, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOY", 0, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOW", 0, cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WY", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTH", 0, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "YEAR", 0, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DATE", 0, cDate.compare( aDateTime, bDateTime ) );
        assertEquals( "TIME", 0, cTime.compare( aDateTime, bDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testBasicComps5
    public void testBasicComps5() {
        Calendar aDateTime
            = Calendar.getInstance();   
        Calendar bDateTime = aDateTime;
        assertEquals( "MILLIS", 0, cMillis.compare( aDateTime, bDateTime ) );
        assertEquals( "SECOND", 0, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "MINUTE", 0, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "HOUR", 0, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "DOW", 0, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOM", 0, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOY", 0, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOW", 0, cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WY", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTH", 0, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "YEAR", 0, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DATE", 0, cDate.compare( aDateTime, bDateTime ) );
        assertEquals( "TIME", 0, cTime.compare( aDateTime, bDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testMillis
    public void testMillis() {}

// org.joda.time.TestDateTimeComparator::testSecond
    public void testSecond() {
        aDateTime = getADate( "1969-12-31T23:59:58" );
        bDateTime = getADate( "1969-12-31T23:50:59" );
        assertEquals( "SecondM1a", -1, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "SecondP1a", 1, cSecond.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1970-01-01T00:00:00" );
        bDateTime = getADate( "1970-01-01T00:00:01" );
        assertEquals( "SecondM1b", -1, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "SecondP1b", 1, cSecond.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testMinute
    public void testMinute() {
        aDateTime = getADate( "1969-12-31T23:58:00" );
        bDateTime = getADate( "1969-12-31T23:59:00" );
        assertEquals( "MinuteM1a", -1, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "MinuteP1a", 1, cMinute.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1970-01-01T00:00:00" );
        bDateTime = getADate( "1970-01-01T00:01:00" );
        assertEquals( "MinuteM1b", -1, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "MinuteP1b", 1, cMinute.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testHour
    public void testHour() {
        aDateTime = getADate( "1969-12-31T22:00:00" );
        bDateTime = getADate( "1969-12-31T23:00:00" );
        assertEquals( "HourM1a", -1, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "HourP1a", 1, cHour.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1970-01-01T00:00:00" );
        bDateTime = getADate( "1970-01-01T01:00:00" );
        assertEquals( "HourM1b", -1, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "HourP1b", 1, cHour.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1969-12-31T23:59:59" );
        bDateTime = getADate( "1970-01-01T00:00:00" );
        assertEquals( "HourP1c", 1, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "HourM1c", -1, cHour.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testDOW
    public void testDOW() {
        
        aDateTime = getADate( "2002-04-12T00:00:00" );
        bDateTime = getADate( "2002-04-13T00:00:00" );
        assertEquals( "DOWM1a", -1, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOWP1a", 1, cDayOfWeek.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testDOM
    public void testDOM() {
        aDateTime = getADate( "2002-04-12T00:00:00" );
        bDateTime = getADate( "2002-04-13T00:00:00" );
        assertEquals( "DOMM1a", -1, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOMP1a", 1, cDayOfMonth.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "2000-12-01T00:00:00" );
        bDateTime = getADate( "1814-04-30T00:00:00" );
        assertEquals( "DOMM1b", -1, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOMP1b", 1, cDayOfMonth.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testDOY
    public void testDOY() {
        aDateTime = getADate( "2002-04-12T00:00:00" );
        bDateTime = getADate( "2002-04-13T00:00:00" );
        assertEquals( "DOYM1a", -1, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DOYP1a", 1, cDayOfYear.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "2000-02-29T00:00:00" );
        bDateTime = getADate( "1814-11-30T00:00:00" );
        assertEquals( "DOYM1b", -1, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DOYP1b", 1, cDayOfYear.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testWOW
    public void testWOW() {
        
        aDateTime = getADate( "2000-01-04T00:00:00" );
        bDateTime = getADate( "2000-01-11T00:00:00" );
        assertEquals( "WOWM1a", -1,
            cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOWP1a", 1,
            cWeekOfWeekyear.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "2000-01-04T00:00:00" );
        bDateTime = getADate( "1999-12-31T00:00:00" );
        assertEquals( "WOWM1b", -1,
            cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOWP1b", 1,
            cWeekOfWeekyear.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testWOYY
    public void testWOYY() {
        
        
        aDateTime = getADate( "1998-12-31T23:59:59" );
        bDateTime = getADate( "1999-01-01T00:00:00" );
        assertEquals( "YOYYZ", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        bDateTime = getADate( "1999-01-04T00:00:00" );
        assertEquals( "YOYYM1", -1, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "YOYYP1", 1, cWeekyear.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testMonth
    public void testMonth() {
        aDateTime = getADate( "2002-04-30T00:00:00" );
        bDateTime = getADate( "2002-05-01T00:00:00" );
        assertEquals( "MONTHM1a", -1, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTHP1a", 1, cMonth.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1900-01-01T00:00:00" );
        bDateTime = getADate( "1899-12-31T00:00:00" );
        assertEquals( "MONTHM1b", -1, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTHP1b", 1, cMonth.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testYear
    public void testYear() {
        aDateTime = getADate( "2000-01-01T00:00:00" );
        bDateTime = getADate( "2001-01-01T00:00:00" );
        assertEquals( "YEARM1a", -1, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "YEARP1a", 1, cYear.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1968-12-31T23:59:59" );
        bDateTime = getADate( "1970-01-01T00:00:00" );
        assertEquals( "YEARM1b", -1, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "YEARP1b", 1, cYear.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1969-12-31T23:59:59" );
        bDateTime = getADate( "1970-01-01T00:00:00" );
        assertEquals( "YEARM1c", -1, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "YEARP1c", 1, cYear.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testListBasic
     public void testListBasic() {
        String[] dtStrs = {
            "1999-02-01T00:00:00",
            "1998-01-20T00:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListBasic", !isSorted1, isSorted2);
     }

// org.joda.time.TestDateTimeComparator::testListMillis
    public void testListMillis() {
        
        List sl = new ArrayList();
        long base = 12345L * 1000L;
        sl.add( new DateTime( base + 999L, DateTimeZone.UTC ) );
        sl.add( new DateTime( base + 222L, DateTimeZone.UTC ) );
        sl.add( new DateTime( base + 456L, DateTimeZone.UTC ) );
        sl.add( new DateTime( base + 888L, DateTimeZone.UTC ) );
        sl.add( new DateTime( base + 123L, DateTimeZone.UTC ) );
        sl.add( new DateTime( base + 000L, DateTimeZone.UTC ) );
        
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cMillis );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListLillis", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListSecond
    public void testListSecond() {
        String[] dtStrs = {
            "1999-02-01T00:00:10",
            "1999-02-01T00:00:30",
            "1999-02-01T00:00:25",
            "1999-02-01T00:00:18",
            "1999-02-01T00:00:01",
            "1999-02-01T00:00:59",
            "1999-02-01T00:00:22"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cSecond );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListSecond", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListMinute
    public void testListMinute() {
        String[] dtStrs = {
            "1999-02-01T00:10:00",
            "1999-02-01T00:30:00",
            "1999-02-01T00:25:00",
            "1999-02-01T00:18:00",
            "1999-02-01T00:01:00",
            "1999-02-01T00:59:00",
            "1999-02-01T00:22:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cMinute );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListMinute", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListHour
    public void testListHour() {
        String[] dtStrs = {
            "1999-02-01T10:00:00",
            "1999-02-01T23:00:00",
            "1999-02-01T01:00:00",
            "1999-02-01T15:00:00",
            "1999-02-01T05:00:00",
            "1999-02-01T20:00:00",
            "1999-02-01T17:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cHour );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListHour", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListDOW
    public void testListDOW() {
        String[] dtStrs = {
            
            "2002-04-21T10:00:00",
            "2002-04-16T10:00:00",
            "2002-04-15T10:00:00",
            "2002-04-17T10:00:00",
            "2002-04-19T10:00:00",
            "2002-04-18T10:00:00",
            "2002-04-20T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cDayOfWeek );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListDOW", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListDOM
    public void testListDOM() {
        String[] dtStrs = {
            
            "2002-04-20T10:00:00",
            "2002-04-16T10:00:00",
            "2002-04-15T10:00:00",
            "2002-04-17T10:00:00",
            "2002-04-19T10:00:00",
            "2002-04-18T10:00:00",
            "2002-04-14T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cDayOfMonth );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListDOM", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListDOY
    public void testListDOY() {
        String[] dtStrs = {
            "2002-04-20T10:00:00",
            "2002-01-16T10:00:00",
            "2002-12-31T10:00:00",
            "2002-09-14T10:00:00",
            "2002-09-19T10:00:00",
            "2002-02-14T10:00:00",
            "2002-10-30T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cDayOfYear );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListDOY", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListWOW
    public void testListWOW() {
        String[] dtStrs = {
            "2002-04-01T10:00:00",
            "2002-01-01T10:00:00",
            "2002-12-01T10:00:00",
            "2002-09-01T10:00:00",
            "2002-09-01T10:00:00",
            "2002-02-01T10:00:00",
            "2002-10-01T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cWeekOfWeekyear );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListWOW", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListYOYY
    public void testListYOYY() {
        
        String[] dtStrs = {
            "2010-04-01T10:00:00",
            "2002-01-01T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cWeekyear );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListYOYY", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListMonth
    public void testListMonth() {
        String[] dtStrs = {
            "2002-04-01T10:00:00",
            "2002-01-01T10:00:00",
            "2002-12-01T10:00:00",
            "2002-09-01T10:00:00",
            "2002-09-01T10:00:00",
            "2002-02-01T10:00:00",
            "2002-10-01T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cMonth );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListMonth", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListYear
     public void testListYear() {
        String[] dtStrs = {
            "1999-02-01T00:00:00",
            "1998-02-01T00:00:00",
            "2525-02-01T00:00:00",
            "1776-02-01T00:00:00",
            "1863-02-01T00:00:00",
            "1066-02-01T00:00:00",
            "2100-02-01T00:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cYear );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListYear", !isSorted1, isSorted2);
     }

// org.joda.time.TestDateTimeComparator::testListDate
    public void testListDate() {
        String[] dtStrs = {
            "1999-02-01T00:00:00",
            "1998-10-03T00:00:00",
            "2525-05-20T00:00:00",
            "1776-12-25T00:00:00",
            "1863-01-31T00:00:00",
            "1066-09-22T00:00:00",
            "2100-07-04T00:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cDate );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListDate", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListTime
    public void testListTime() {
        String[] dtStrs = {
            "1999-02-01T01:02:05",
            "1999-02-01T22:22:22",
            "1999-02-01T05:30:45",
            "1999-02-01T09:17:59",
            "1999-02-01T09:17:58",
            "1999-02-01T15:30:00",
            "1999-02-01T17:00:44"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cTime );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListTime", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testNullDT
    public void testNullDT() {
        
        aDateTime = getADate("2000-01-01T00:00:00");
        assertTrue(cYear.compare(null, aDateTime) > 0);
        assertTrue(cYear.compare(aDateTime, null) < 0);
    }

// org.joda.time.TestDateTimeComparator::testInvalidObj
    public void testInvalidObj() {
        aDateTime = getADate("2000-01-01T00:00:00");
        try {
            cYear.compare("FreeBird", aDateTime);
            fail("Invalid object failed");
        } catch (IllegalArgumentException cce) {}
    }

// org.joda.time.TestDateTimeUtils::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestDateTimeUtils::testClass
    public void testClass() {
        Class cls = DateTimeUtils.class;
        assertEquals(true, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isFinal(cls.getModifiers()));
        
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(cls.getDeclaredConstructors()[0].getModifiers()));
        
        DateTimeUtils utils = new DateTimeUtils() {};
    }

// org.joda.time.TestDateTimeUtils::testSystemMillis
    public void testSystemMillis() {
        long nowSystem = System.currentTimeMillis();
        long now = DateTimeUtils.currentTimeMillis();
        assertTrue((now >= nowSystem));
        assertTrue((now - nowSystem) < 10000L);
    }

// org.joda.time.TestDateTimeUtils::testSystemMillisSecurity
    public void testSystemMillisSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            try {
                Policy.setPolicy(RESTRICT);
                System.setSecurityManager(new SecurityManager());
                DateTimeUtils.setCurrentMillisSystem();
                fail();
            } catch (SecurityException ex) {
                
            } finally {
                System.setSecurityManager(null);
                Policy.setPolicy(ALLOW);
            }
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

// org.joda.time.TestDateTimeUtils::testFixedMillis
    public void testFixedMillis() {
        try {
            DateTimeUtils.setCurrentMillisFixed(0L);
            assertEquals(0L, DateTimeUtils.currentTimeMillis());
            assertEquals(0L, DateTimeUtils.currentTimeMillis());
            assertEquals(0L, DateTimeUtils.currentTimeMillis());
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
        long nowSystem = System.currentTimeMillis();
        long now = DateTimeUtils.currentTimeMillis();
        assertTrue((now >= nowSystem));
        assertTrue((now - nowSystem) < 10000L);
    }

// org.joda.time.TestDateTimeUtils::testFixedMillisSecurity
    public void testFixedMillisSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            try {
                Policy.setPolicy(RESTRICT);
                System.setSecurityManager(new SecurityManager());
                DateTimeUtils.setCurrentMillisFixed(0L);
                fail();
            } catch (SecurityException ex) {
                
            } finally {
                System.setSecurityManager(null);
                Policy.setPolicy(ALLOW);
            }
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

// org.joda.time.TestDateTimeUtils::testOffsetMillis
    public void testOffsetMillis() {
        try {
            
            DateTimeUtils.setCurrentMillisOffset(-24 * 60 *  60 * 1000);
            long nowSystem = System.currentTimeMillis();
            long now = DateTimeUtils.currentTimeMillis();
            long nowAdjustDay = now + (24 * 60 *  60 * 1000);
            assertTrue((now < nowSystem));
            assertTrue((nowAdjustDay >= nowSystem));
            assertTrue((nowAdjustDay - nowSystem) < 10000L);
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
        long nowSystem = System.currentTimeMillis();
        long now = DateTimeUtils.currentTimeMillis();
        assertTrue((now >= nowSystem));
        assertTrue((now - nowSystem) < 10000L);
    }

// org.joda.time.TestDateTimeUtils::testOffsetMillisToZero
    public void testOffsetMillisToZero() {}

// org.joda.time.TestDateTimeUtils::testOffsetMillisSecurity
    public void testOffsetMillisSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            try {
                Policy.setPolicy(RESTRICT);
                System.setSecurityManager(new SecurityManager());
                DateTimeUtils.setCurrentMillisOffset(-24 * 60 *  60 * 1000);
                fail();
            } catch (SecurityException ex) {
                
            } finally {
                System.setSecurityManager(null);
                Policy.setPolicy(ALLOW);
            }
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

// org.joda.time.TestDateTimeUtils::testMillisProvider
    public void testMillisProvider() {
        try {
            DateTimeUtils.setCurrentMillisProvider(new MillisProvider() {
                public long getMillis() {
                    return 1L;
                }
            });
            assertEquals(1L, DateTimeUtils.currentTimeMillis());
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

// org.joda.time.TestDateTimeUtils::testMillisProvider_null
    public void testMillisProvider_null() {
        try {
            DateTimeUtils.setCurrentMillisProvider(null);
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestDateTimeUtils::testMillisProviderSecurity
    public void testMillisProviderSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            try {
                Policy.setPolicy(RESTRICT);
                System.setSecurityManager(new SecurityManager());
                DateTimeUtils.setCurrentMillisProvider(new MillisProvider() {
                    public long getMillis() {
                        return 0L;
                    }
                });
                fail();
            } catch (SecurityException ex) {
                
            } finally {
                System.setSecurityManager(null);
                Policy.setPolicy(ALLOW);
            }
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

// org.joda.time.TestDateTimeUtils::testGetInstantMillis_RI
    public void testGetInstantMillis_RI() {
        Instant i = new Instant(123L);
        assertEquals(123L, DateTimeUtils.getInstantMillis(i));
        try {
            DateTimeUtils.setCurrentMillisFixed(TEST_TIME_NOW);
            assertEquals(TEST_TIME_NOW, DateTimeUtils.getInstantMillis(null));
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

// org.joda.time.TestDateTimeUtils::testGetInstantChronology_RI
    public void testGetInstantChronology_RI() {
        DateTime dt = new DateTime(123L, BuddhistChronology.getInstance());
        assertEquals(BuddhistChronology.getInstance(), DateTimeUtils.getInstantChronology(dt));
        
        Instant i = new Instant(123L);
        assertEquals(ISOChronology.getInstanceUTC(), DateTimeUtils.getInstantChronology(i));
        
        AbstractInstant ai = new AbstractInstant() {
            public long getMillis() {
                return 0L;
            }
            public Chronology getChronology() {
                return null; 
            }
        };
        assertEquals(ISOChronology.getInstance(), DateTimeUtils.getInstantChronology(ai));
        
        assertEquals(ISOChronology.getInstance(), DateTimeUtils.getInstantChronology(null));
    }

// org.joda.time.TestDateTimeUtils::testGetIntervalChronology_RInterval
    public void testGetIntervalChronology_RInterval() {
        Interval dt = new Interval(123L, 456L, BuddhistChronology.getInstance());
        assertEquals(BuddhistChronology.getInstance(), DateTimeUtils.getIntervalChronology(dt));
        
        assertEquals(ISOChronology.getInstance(), DateTimeUtils.getIntervalChronology(null));
        
        MutableInterval ai = new MutableInterval() {
            public Chronology getChronology() {
                return null; 
            }
        };
        assertEquals(ISOChronology.getInstance(), DateTimeUtils.getIntervalChronology(ai));
    }

// org.joda.time.TestDateTimeUtils::testGetIntervalChronology_RI_RI
    public void testGetIntervalChronology_RI_RI() {
        DateTime dt1 = new DateTime(123L, BuddhistChronology.getInstance());
        DateTime dt2 = new DateTime(123L, CopticChronology.getInstance());
        assertEquals(BuddhistChronology.getInstance(), DateTimeUtils.getIntervalChronology(dt1, dt2));
        assertEquals(BuddhistChronology.getInstance(), DateTimeUtils.getIntervalChronology(dt1, null));
        assertEquals(CopticChronology.getInstance(), DateTimeUtils.getIntervalChronology(null, dt2));
        assertEquals(ISOChronology.getInstance(), DateTimeUtils.getIntervalChronology(null, null));
    }

// org.joda.time.TestDateTimeUtils::testGetReadableInterval_ReadableInterval
    public void testGetReadableInterval_ReadableInterval() {
        ReadableInterval input = new Interval(0, 100L);
        assertEquals(input, DateTimeUtils.getReadableInterval(input));
        
        try {
            DateTimeUtils.setCurrentMillisFixed(TEST_TIME_NOW);
            assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME_NOW), DateTimeUtils.getReadableInterval(null));
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

// org.joda.time.TestDateTimeUtils::testGetChronology_Chronology
    public void testGetChronology_Chronology() {
        assertEquals(BuddhistChronology.getInstance(), DateTimeUtils.getChronology(BuddhistChronology.getInstance()));
        assertEquals(ISOChronology.getInstance(), DateTimeUtils.getChronology(null));
    }

// org.joda.time.TestDateTimeUtils::testGetZone_Zone
    public void testGetZone_Zone() {
        assertEquals(PARIS, DateTimeUtils.getZone(PARIS));
        assertEquals(DateTimeZone.getDefault(), DateTimeUtils.getZone(null));
    }

// org.joda.time.TestDateTimeUtils::testGetPeriodType_PeriodType
    public void testGetPeriodType_PeriodType() {
        assertEquals(PeriodType.dayTime(), DateTimeUtils.getPeriodType(PeriodType.dayTime()));
        assertEquals(PeriodType.standard(), DateTimeUtils.getPeriodType(null));
    }

// org.joda.time.TestDateTimeUtils::testGetDurationMillis_RI
    public void testGetDurationMillis_RI() {
        Duration dur = new Duration(123L);
        assertEquals(123L, DateTimeUtils.getDurationMillis(dur));
        assertEquals(0L, DateTimeUtils.getDurationMillis(null));
    }

// org.joda.time.TestDateTimeUtils::testIsContiguous_RP
    public void testIsContiguous_RP() {
        YearMonthDay ymd = new YearMonthDay(2005, 6, 9);
        assertEquals(true, DateTimeUtils.isContiguous(ymd));
        TimeOfDay tod = new TimeOfDay(12, 20, 30, 0);
        assertEquals(true, DateTimeUtils.isContiguous(tod));
        Partial year = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals(true, DateTimeUtils.isContiguous(year));
        Partial hourOfDay = new Partial(DateTimeFieldType.hourOfDay(), 12);
        assertEquals(true, DateTimeUtils.isContiguous(hourOfDay));
        Partial yearHour = year.with(DateTimeFieldType.hourOfDay(), 12);
        assertEquals(false, DateTimeUtils.isContiguous(yearHour));
        Partial ymdd = new Partial(ymd).with(DateTimeFieldType.dayOfWeek(), 2);
        assertEquals(false, DateTimeUtils.isContiguous(ymdd));
        Partial dd = new Partial(DateTimeFieldType.dayOfMonth(), 13).with(DateTimeFieldType.dayOfWeek(), 5);
        assertEquals(false, DateTimeUtils.isContiguous(dd));
        
        try {
            DateTimeUtils.isContiguous((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeUtils::testIsContiguous_RP_GJChronology
    public void testIsContiguous_RP_GJChronology() {
        YearMonthDay ymd = new YearMonthDay(2005, 6, 9, GJ);
        assertEquals(true, DateTimeUtils.isContiguous(ymd));
        TimeOfDay tod = new TimeOfDay(12, 20, 30, 0, GJ);
        assertEquals(true, DateTimeUtils.isContiguous(tod));
        Partial year = new Partial(DateTimeFieldType.year(), 2005, GJ);
        assertEquals(true, DateTimeUtils.isContiguous(year));
        Partial hourOfDay = new Partial(DateTimeFieldType.hourOfDay(), 12, GJ);
        assertEquals(true, DateTimeUtils.isContiguous(hourOfDay));
        Partial yearHour = year.with(DateTimeFieldType.hourOfDay(), 12);
        assertEquals(false, DateTimeUtils.isContiguous(yearHour));
        Partial ymdd = new Partial(ymd).with(DateTimeFieldType.dayOfWeek(), 2);
        assertEquals(false, DateTimeUtils.isContiguous(ymdd));
        Partial dd = new Partial(DateTimeFieldType.dayOfMonth(), 13).with(DateTimeFieldType.dayOfWeek(), 5);
        assertEquals(false, DateTimeUtils.isContiguous(dd));
        
        try {
            DateTimeUtils.isContiguous((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZoneCutover::test_MockGazaIsCorrect
    public void test_MockGazaIsCorrect() {
        DateTime pre = new DateTime(CUTOVER_GAZA - 1L, MOCK_GAZA);
        assertEquals("2007-03-31T23:59:59.999+02:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_GAZA, MOCK_GAZA);
        assertEquals("2007-04-01T01:00:00.000+03:00", at.toString());
        DateTime post = new DateTime(CUTOVER_GAZA + 1L, MOCK_GAZA);
        assertEquals("2007-04-01T01:00:00.001+03:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Gaza
    public void test_getOffsetFromLocal_Gaza() {
        doTest_getOffsetFromLocal_Gaza(-1, 23, 0, "2007-03-31T23:00:00.000+02:00");
        doTest_getOffsetFromLocal_Gaza(-1, 23, 30, "2007-03-31T23:30:00.000+02:00");
        doTest_getOffsetFromLocal_Gaza(0, 0, 0, "2007-04-01T01:00:00.000+03:00");
        doTest_getOffsetFromLocal_Gaza(0, 0, 30, "2007-04-01T01:30:00.000+03:00");
        doTest_getOffsetFromLocal_Gaza(0, 1, 0, "2007-04-01T01:00:00.000+03:00");
        doTest_getOffsetFromLocal_Gaza(0, 1, 30, "2007-04-01T01:30:00.000+03:00");
        doTest_getOffsetFromLocal_Gaza(0, 2, 0, "2007-04-01T02:00:00.000+03:00");
        doTest_getOffsetFromLocal_Gaza(0, 3, 0, "2007-04-01T03:00:00.000+03:00");
        doTest_getOffsetFromLocal_Gaza(0, 4, 0, "2007-04-01T04:00:00.000+03:00");
        doTest_getOffsetFromLocal_Gaza(0, 5, 0, "2007-04-01T05:00:00.000+03:00");
        doTest_getOffsetFromLocal_Gaza(0, 6, 0, "2007-04-01T06:00:00.000+03:00");
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_Gaza
    public void test_DateTime_roundFloor_Gaza() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_GAZA);
        assertEquals("2007-04-01T08:00:00.000+03:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-04-01T01:00:00.000+03:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_Gaza
    public void test_DateTime_roundCeiling_Gaza() {
        DateTime dt = new DateTime(2007, 3, 31, 20, 0, 0, 0, MOCK_GAZA);
        assertEquals("2007-03-31T20:00:00.000+02:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-04-01T01:00:00.000+03:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourZero_Gaza
    public void test_DateTime_setHourZero_Gaza() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_GAZA);
        assertEquals("2007-04-01T08:00:00.000+03:00", dt.toString());
        try {
            dt.hourOfDay().setCopy(0);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_withHourZero_Gaza
    public void test_DateTime_withHourZero_Gaza() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_GAZA);
        assertEquals("2007-04-01T08:00:00.000+03:00", dt.toString());
        try {
            dt.withHourOfDay(0);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_withDay_Gaza
    public void test_DateTime_withDay_Gaza() {
        DateTime dt = new DateTime(2007, 4, 2, 0, 0, 0, 0, MOCK_GAZA);
        assertEquals("2007-04-02T00:00:00.000+03:00", dt.toString());
        DateTime res = dt.withDayOfMonth(1);
        assertEquals("2007-04-01T01:00:00.000+03:00", res.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusHour_Gaza
    public void test_DateTime_minusHour_Gaza() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_GAZA);
        assertEquals("2007-04-01T08:00:00.000+03:00", dt.toString());
        
        DateTime minus7 = dt.minusHours(7);
        assertEquals("2007-04-01T01:00:00.000+03:00", minus7.toString());
        DateTime minus8 = dt.minusHours(8);
        assertEquals("2007-03-31T23:00:00.000+02:00", minus8.toString());
        DateTime minus9 = dt.minusHours(9);
        assertEquals("2007-03-31T22:00:00.000+02:00", minus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusHour_Gaza
    public void test_DateTime_plusHour_Gaza() {
        DateTime dt = new DateTime(2007, 3, 31, 16, 0, 0, 0, MOCK_GAZA);
        assertEquals("2007-03-31T16:00:00.000+02:00", dt.toString());
        
        DateTime plus7 = dt.plusHours(7);
        assertEquals("2007-03-31T23:00:00.000+02:00", plus7.toString());
        DateTime plus8 = dt.plusHours(8);
        assertEquals("2007-04-01T01:00:00.000+03:00", plus8.toString());
        DateTime plus9 = dt.plusHours(9);
        assertEquals("2007-04-01T02:00:00.000+03:00", plus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusDay_Gaza
    public void test_DateTime_minusDay_Gaza() {
        DateTime dt = new DateTime(2007, 4, 2, 0, 0, 0, 0, MOCK_GAZA);
        assertEquals("2007-04-02T00:00:00.000+03:00", dt.toString());
        
        DateTime minus1 = dt.minusDays(1);
        assertEquals("2007-04-01T01:00:00.000+03:00", minus1.toString());
        DateTime minus2 = dt.minusDays(2);
        assertEquals("2007-03-31T00:00:00.000+02:00", minus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusDay_Gaza
    public void test_DateTime_plusDay_Gaza() {
        DateTime dt = new DateTime(2007, 3, 31, 0, 0, 0, 0, MOCK_GAZA);
        assertEquals("2007-03-31T00:00:00.000+02:00", dt.toString());
        
        DateTime plus1 = dt.plusDays(1);
        assertEquals("2007-04-01T01:00:00.000+03:00", plus1.toString());
        DateTime plus2 = dt.plusDays(2);
        assertEquals("2007-04-02T00:00:00.000+03:00", plus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusDayMidGap_Gaza
    public void test_DateTime_plusDayMidGap_Gaza() {
        DateTime dt = new DateTime(2007, 3, 31, 0, 30, 0, 0, MOCK_GAZA);
        assertEquals("2007-03-31T00:30:00.000+02:00", dt.toString());
        
        DateTime plus1 = dt.plusDays(1);
        assertEquals("2007-04-01T01:30:00.000+03:00", plus1.toString());
        DateTime plus2 = dt.plusDays(2);
        assertEquals("2007-04-02T00:30:00.000+03:00", plus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_addWrapFieldDay_Gaza
    public void test_DateTime_addWrapFieldDay_Gaza() {
        DateTime dt = new DateTime(2007, 4, 30, 0, 0, 0, 0, MOCK_GAZA);
        assertEquals("2007-04-30T00:00:00.000+03:00", dt.toString());
        
        DateTime plus1 = dt.dayOfMonth().addWrapFieldToCopy(1);
        assertEquals("2007-04-01T01:00:00.000+03:00", plus1.toString());
        DateTime plus2 = dt.dayOfMonth().addWrapFieldToCopy(2);
        assertEquals("2007-04-02T00:00:00.000+03:00", plus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_withZoneRetainFields_Gaza
    public void test_DateTime_withZoneRetainFields_Gaza() {
        DateTime dt = new DateTime(2007, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals("2007-04-01T00:00:00.000Z", dt.toString());
        
        DateTime res = dt.withZoneRetainFields(MOCK_GAZA);
        assertEquals("2007-04-01T01:00:00.000+03:00", res.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_MutableDateTime_withZoneRetainFields_Gaza
    public void test_MutableDateTime_withZoneRetainFields_Gaza() {
        MutableDateTime dt = new MutableDateTime(2007, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals("2007-04-01T00:00:00.000Z", dt.toString());
        
        dt.setZoneRetainFields(MOCK_GAZA);
        assertEquals("2007-04-01T01:00:00.000+03:00", dt.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_LocalDate_new_Gaza
    public void test_LocalDate_new_Gaza() {
        LocalDate date1 = new LocalDate(CUTOVER_GAZA, MOCK_GAZA);
        assertEquals("2007-04-01", date1.toString());
        
        LocalDate date2 = new LocalDate(CUTOVER_GAZA - 1, MOCK_GAZA);
        assertEquals("2007-03-31", date2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_LocalDate_toDateMidnight_Gaza
    public void test_LocalDate_toDateMidnight_Gaza() {
        LocalDate date = new LocalDate(2007, 4, 1);
        try {
            date.toDateMidnight(MOCK_GAZA);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().startsWith("Illegal instant due to time zone offset transition"));
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_new_Gaza
    public void test_DateTime_new_Gaza() {
        try {
            new DateTime(2007, 4, 1, 0, 0, 0, 0, MOCK_GAZA);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().indexOf("Illegal instant due to time zone offset transition") >= 0);
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_newValid_Gaza
    public void test_DateTime_newValid_Gaza() {
        new DateTime(2007, 3, 31, 19, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 3, 31, 20, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 3, 31, 21, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 3, 31, 22, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 3, 31, 23, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 4, 1, 1, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 4, 1, 2, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 4, 1, 3, 0, 0, 0, MOCK_GAZA);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_parse_Gaza
    public void test_DateTime_parse_Gaza() {
        try {
            new DateTime("2007-04-01T00:00", MOCK_GAZA);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().indexOf("Illegal instant due to time zone offset transition") >= 0);
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_MockTurkIsCorrect
    public void test_MockTurkIsCorrect() {
        DateTime pre = new DateTime(CUTOVER_TURK - 1L, MOCK_TURK);
        assertEquals("2007-03-31T23:59:59.999-05:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_TURK, MOCK_TURK);
        assertEquals("2007-04-01T01:00:00.000-04:00", at.toString());
        DateTime post = new DateTime(CUTOVER_TURK + 1L, MOCK_TURK);
        assertEquals("2007-04-01T01:00:00.001-04:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Turk
    public void test_getOffsetFromLocal_Turk() {
        doTest_getOffsetFromLocal_Turk(-1, 23, 0, "2007-03-31T23:00:00.000-05:00");
        doTest_getOffsetFromLocal_Turk(-1, 23, 30, "2007-03-31T23:30:00.000-05:00");
        doTest_getOffsetFromLocal_Turk(0, 0, 0, "2007-04-01T01:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 0, 30, "2007-04-01T01:30:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 1, 0, "2007-04-01T01:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 1, 30, "2007-04-01T01:30:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 2, 0, "2007-04-01T02:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 3, 0, "2007-04-01T03:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 4, 0, "2007-04-01T04:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 5, 0, "2007-04-01T05:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 6, 0, "2007-04-01T06:00:00.000-04:00");
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_Turk
    public void test_DateTime_roundFloor_Turk() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-01T08:00:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-04-01T01:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloorNotDST_Turk
    public void test_DateTime_roundFloorNotDST_Turk() {
        DateTime dt = new DateTime(2007, 4, 2, 8, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-02T08:00:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-04-02T00:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_Turk
    public void test_DateTime_roundCeiling_Turk() {
        DateTime dt = new DateTime(2007, 3, 31, 20, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-03-31T20:00:00.000-05:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-04-01T01:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourZero_Turk
    public void test_DateTime_setHourZero_Turk() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-01T08:00:00.000-04:00", dt.toString());
        try {
            dt.hourOfDay().setCopy(0);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_withHourZero_Turk
    public void test_DateTime_withHourZero_Turk() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-01T08:00:00.000-04:00", dt.toString());
        try {
            dt.withHourOfDay(0);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_withDay_Turk
    public void test_DateTime_withDay_Turk() {
        DateTime dt = new DateTime(2007, 4, 2, 0, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-02T00:00:00.000-04:00", dt.toString());
        DateTime res = dt.withDayOfMonth(1);
        assertEquals("2007-04-01T01:00:00.000-04:00", res.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusHour_Turk
    public void test_DateTime_minusHour_Turk() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-01T08:00:00.000-04:00", dt.toString());
        
        DateTime minus7 = dt.minusHours(7);
        assertEquals("2007-04-01T01:00:00.000-04:00", minus7.toString());
        DateTime minus8 = dt.minusHours(8);
        assertEquals("2007-03-31T23:00:00.000-05:00", minus8.toString());
        DateTime minus9 = dt.minusHours(9);
        assertEquals("2007-03-31T22:00:00.000-05:00", minus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusHour_Turk
    public void test_DateTime_plusHour_Turk() {
        DateTime dt = new DateTime(2007, 3, 31, 16, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-03-31T16:00:00.000-05:00", dt.toString());
        
        DateTime plus7 = dt.plusHours(7);
        assertEquals("2007-03-31T23:00:00.000-05:00", plus7.toString());
        DateTime plus8 = dt.plusHours(8);
        assertEquals("2007-04-01T01:00:00.000-04:00", plus8.toString());
        DateTime plus9 = dt.plusHours(9);
        assertEquals("2007-04-01T02:00:00.000-04:00", plus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusDay_Turk
    public void test_DateTime_minusDay_Turk() {
        DateTime dt = new DateTime(2007, 4, 2, 0, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-02T00:00:00.000-04:00", dt.toString());
        
        DateTime minus1 = dt.minusDays(1);
        assertEquals("2007-04-01T01:00:00.000-04:00", minus1.toString());
        DateTime minus2 = dt.minusDays(2);
        assertEquals("2007-03-31T00:00:00.000-05:00", minus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusDay_Turk
    public void test_DateTime_plusDay_Turk() {
        DateTime dt = new DateTime(2007, 3, 31, 0, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-03-31T00:00:00.000-05:00", dt.toString());
        
        DateTime plus1 = dt.plusDays(1);
        assertEquals("2007-04-01T01:00:00.000-04:00", plus1.toString());
        DateTime plus2 = dt.plusDays(2);
        assertEquals("2007-04-02T00:00:00.000-04:00", plus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusDayMidGap_Turk
    public void test_DateTime_plusDayMidGap_Turk() {
        DateTime dt = new DateTime(2007, 3, 31, 0, 30, 0, 0, MOCK_TURK);
        assertEquals("2007-03-31T00:30:00.000-05:00", dt.toString());
        
        DateTime plus1 = dt.plusDays(1);
        assertEquals("2007-04-01T01:30:00.000-04:00", plus1.toString());
        DateTime plus2 = dt.plusDays(2);
        assertEquals("2007-04-02T00:30:00.000-04:00", plus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_addWrapFieldDay_Turk
    public void test_DateTime_addWrapFieldDay_Turk() {
        DateTime dt = new DateTime(2007, 4, 30, 0, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-30T00:00:00.000-04:00", dt.toString());
        
        DateTime plus1 = dt.dayOfMonth().addWrapFieldToCopy(1);
        assertEquals("2007-04-01T01:00:00.000-04:00", plus1.toString());
        DateTime plus2 = dt.dayOfMonth().addWrapFieldToCopy(2);
        assertEquals("2007-04-02T00:00:00.000-04:00", plus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_withZoneRetainFields_Turk
    public void test_DateTime_withZoneRetainFields_Turk() {
        DateTime dt = new DateTime(2007, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals("2007-04-01T00:00:00.000Z", dt.toString());
        
        DateTime res = dt.withZoneRetainFields(MOCK_TURK);
        assertEquals("2007-04-01T01:00:00.000-04:00", res.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_MutableDateTime_setZoneRetainFields_Turk
    public void test_MutableDateTime_setZoneRetainFields_Turk() {
        MutableDateTime dt = new MutableDateTime(2007, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals("2007-04-01T00:00:00.000Z", dt.toString());
        
        dt.setZoneRetainFields(MOCK_TURK);
        assertEquals("2007-04-01T01:00:00.000-04:00", dt.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_LocalDate_new_Turk
    public void test_LocalDate_new_Turk() {
        LocalDate date1 = new LocalDate(CUTOVER_TURK, MOCK_TURK);
        assertEquals("2007-04-01", date1.toString());
        
        LocalDate date2 = new LocalDate(CUTOVER_TURK - 1, MOCK_TURK);
        assertEquals("2007-03-31", date2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_LocalDate_toDateMidnight_Turk
    public void test_LocalDate_toDateMidnight_Turk() {
        LocalDate date = new LocalDate(2007, 4, 1);
        try {
            date.toDateMidnight(MOCK_TURK);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().startsWith("Illegal instant due to time zone offset transition"));
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_new_Turk
    public void test_DateTime_new_Turk() {
        try {
            new DateTime(2007, 4, 1, 0, 0, 0, 0, MOCK_TURK);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().indexOf("Illegal instant due to time zone offset transition") >= 0);
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_newValid_Turk
    public void test_DateTime_newValid_Turk() {
        new DateTime(2007, 3, 31, 23, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 1, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 2, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 3, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 4, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 5, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 6, 0, 0, 0, MOCK_TURK);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_parse_Turk
    public void test_DateTime_parse_Turk() {
        try {
            new DateTime("2007-04-01T00:00", MOCK_TURK);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().indexOf("Illegal instant due to time zone offset transition") >= 0);
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_NewYorkIsCorrect_Spring
    public void test_NewYorkIsCorrect_Spring() {
        DateTime pre = new DateTime(CUTOVER_NEW_YORK_SPRING - 1L, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:59:59.999-05:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_NEW_YORK_SPRING, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:00:00.000-04:00", at.toString());
        DateTime post = new DateTime(CUTOVER_NEW_YORK_SPRING + 1L, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:00:00.001-04:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_NewYork_Spring
    public void test_getOffsetFromLocal_NewYork_Spring() {
        doTest_getOffsetFromLocal(3, 11, 1, 0, "2007-03-11T01:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 1,30, "2007-03-11T01:30:00.000-05:00", ZONE_NEW_YORK);
        
        doTest_getOffsetFromLocal(3, 11, 2, 0, "2007-03-11T03:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 2,30, "2007-03-11T03:30:00.000-04:00", ZONE_NEW_YORK);
        
        doTest_getOffsetFromLocal(3, 11, 3, 0, "2007-03-11T03:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 3,30, "2007-03-11T03:30:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 4, 0, "2007-03-11T04:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 5, 0, "2007-03-11T05:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 6, 0, "2007-03-11T06:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 7, 0, "2007-03-11T07:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 8, 0, "2007-03-11T08:00:00.000-04:00", ZONE_NEW_YORK);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourAcross_NewYork_Spring
    public void test_DateTime_setHourAcross_NewYork_Spring() {
        DateTime dt = new DateTime(2007, 3, 11, 0, 0, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T00:00:00.000-05:00", dt.toString());
        DateTime res = dt.hourOfDay().setCopy(4);
        assertEquals("2007-03-11T04:00:00.000-04:00", res.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourForward_NewYork_Spring
    public void test_DateTime_setHourForward_NewYork_Spring() {
        DateTime dt = new DateTime(2007, 3, 11, 0, 0, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T00:00:00.000-05:00", dt.toString());
        
        try {
            dt.hourOfDay().setCopy(2);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourBack_NewYork_Spring
    public void test_DateTime_setHourBack_NewYork_Spring() {
        DateTime dt = new DateTime(2007, 3, 11, 8, 0, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T08:00:00.000-04:00", dt.toString());
        
        try {
            dt.hourOfDay().setCopy(2);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_day_NewYork_Spring_preCutover
    public void test_DateTime_roundFloor_day_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-03-11T00:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_day_NewYork_Spring_postCutover
    public void test_DateTime_roundFloor_day_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-03-11T00:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_hour_NewYork_Spring_preCutover
    public void test_DateTime_roundFloor_hour_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundFloorCopy();
        assertEquals("2007-03-11T01:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_hour_NewYork_Spring_postCutover
    public void test_DateTime_roundFloor_hour_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundFloorCopy();
        assertEquals("2007-03-11T03:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_minute_NewYork_Spring_preCutover
    public void test_DateTime_roundFloor_minute_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:40.000-05:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundFloorCopy();
        assertEquals("2007-03-11T01:30:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_minute_NewYork_Spring_postCutover
    public void test_DateTime_roundFloor_minute_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:40.000-04:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundFloorCopy();
        assertEquals("2007-03-11T03:30:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_day_NewYork_Spring_preCutover
    public void test_DateTime_roundCeiling_day_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-03-12T00:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_day_NewYork_Spring_postCutover
    public void test_DateTime_roundCeiling_day_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-03-12T00:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_hour_NewYork_Spring_preCutover
    public void test_DateTime_roundCeiling_hour_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundCeilingCopy();
        assertEquals("2007-03-11T03:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_hour_NewYork_Spring_postCutover
    public void test_DateTime_roundCeiling_hour_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundCeilingCopy();
        assertEquals("2007-03-11T04:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_minute_NewYork_Spring_preCutover
    public void test_DateTime_roundCeiling_minute_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:40.000-05:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundCeilingCopy();
        assertEquals("2007-03-11T01:31:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_minute_NewYork_Spring_postCutover
    public void test_DateTime_roundCeiling_minute_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:40.000-04:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundCeilingCopy();
        assertEquals("2007-03-11T03:31:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_NewYorkIsCorrect_Autumn
    public void test_NewYorkIsCorrect_Autumn() {
        DateTime pre = new DateTime(CUTOVER_NEW_YORK_AUTUMN - 1L, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:59:59.999-04:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_NEW_YORK_AUTUMN, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:00:00.000-05:00", at.toString());
        DateTime post = new DateTime(CUTOVER_NEW_YORK_AUTUMN + 1L, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:00:00.001-05:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_NewYork_Autumn
    public void test_getOffsetFromLocal_NewYork_Autumn() {
        doTest_getOffsetFromLocal(11, 4, 0, 0, "2007-11-04T00:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 0,30, "2007-11-04T00:30:00.000-04:00", ZONE_NEW_YORK);
        
        doTest_getOffsetFromLocal(11, 4, 1, 0, "2007-11-04T01:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 1,30, "2007-11-04T01:30:00.000-04:00", ZONE_NEW_YORK);
        
        doTest_getOffsetFromLocal(11, 4, 2, 0, "2007-11-04T02:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 2,30, "2007-11-04T02:30:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 3, 0, "2007-11-04T03:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 3,30, "2007-11-04T03:30:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 4, 0, "2007-11-04T04:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 5, 0, "2007-11-04T05:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 6, 0, "2007-11-04T06:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 7, 0, "2007-11-04T07:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 8, 0, "2007-11-04T08:00:00.000-05:00", ZONE_NEW_YORK);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_constructor_NewYork_Autumn
    public void test_DateTime_constructor_NewYork_Autumn() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.000-04:00", dt.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusHour_NewYork_Autumn
    public void test_DateTime_plusHour_NewYork_Autumn() {
        DateTime dt = new DateTime(2007, 11, 3, 18, 0, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-03T18:00:00.000-04:00", dt.toString());
        
        DateTime plus6 = dt.plusHours(6);
        assertEquals("2007-11-04T00:00:00.000-04:00", plus6.toString());
        DateTime plus7 = dt.plusHours(7);
        assertEquals("2007-11-04T01:00:00.000-04:00", plus7.toString());
        DateTime plus8 = dt.plusHours(8);
        assertEquals("2007-11-04T01:00:00.000-05:00", plus8.toString());
        DateTime plus9 = dt.plusHours(9);
        assertEquals("2007-11-04T02:00:00.000-05:00", plus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusHour_NewYork_Autumn
    public void test_DateTime_minusHour_NewYork_Autumn() {
        DateTime dt = new DateTime(2007, 11, 4, 8, 0, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T08:00:00.000-05:00", dt.toString());
        
        DateTime minus6 = dt.minusHours(6);
        assertEquals("2007-11-04T02:00:00.000-05:00", minus6.toString());
        DateTime minus7 = dt.minusHours(7);
        assertEquals("2007-11-04T01:00:00.000-05:00", minus7.toString());
        DateTime minus8 = dt.minusHours(8);
        assertEquals("2007-11-04T01:00:00.000-04:00", minus8.toString());
        DateTime minus9 = dt.minusHours(9);
        assertEquals("2007-11-04T00:00:00.000-04:00", minus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_day_NewYork_Autumn_preCutover
    public void test_DateTime_roundFloor_day_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-11-04T00:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_day_NewYork_Autumn_postCutover
    public void test_DateTime_roundFloor_day_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-11-04T00:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_hourOfDay_NewYork_Autumn_preCutover
    public void test_DateTime_roundFloor_hourOfDay_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundFloorCopy();
        assertEquals("2007-11-04T01:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_hourOfDay_NewYork_Autumn_postCutover
    public void test_DateTime_roundFloor_hourOfDay_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundFloorCopy();
        assertEquals("2007-11-04T01:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_minuteOfHour_NewYork_Autumn_preCutover
    public void test_DateTime_roundFloor_minuteOfHour_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:40.000-04:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundFloorCopy();
        assertEquals("2007-11-04T01:30:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_minuteOfHour_NewYork_Autumn_postCutover
    public void test_DateTime_roundFloor_minuteOfHour_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:40.000-05:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundFloorCopy();
        assertEquals("2007-11-04T01:30:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_secondOfMinute_NewYork_Autumn_preCutover
    public void test_DateTime_roundFloor_secondOfMinute_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 500, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:40.500-04:00", dt.toString());
        DateTime rounded = dt.secondOfMinute().roundFloorCopy();
        assertEquals("2007-11-04T01:30:40.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_secondOfMinute_NewYork_Autumn_postCutover
    public void test_DateTime_roundFloor_secondOfMinute_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 500, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:40.500-05:00", dt.toString());
        DateTime rounded = dt.secondOfMinute().roundFloorCopy();
        assertEquals("2007-11-04T01:30:40.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_day_NewYork_Autumn_preCutover
    public void test_DateTime_roundCeiling_day_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-11-05T00:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_day_NewYork_Autumn_postCutover
    public void test_DateTime_roundCeiling_day_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-11-05T00:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_hourOfDay_NewYork_Autumn_preCutover
    public void test_DateTime_roundCeiling_hourOfDay_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundCeilingCopy();
        assertEquals("2007-11-04T01:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_hourOfDay_NewYork_Autumn_postCutover
    public void test_DateTime_roundCeiling_hourOfDay_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundCeilingCopy();
        assertEquals("2007-11-04T02:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_minuteOfHour_NewYork_Autumn_preCutover
    public void test_DateTime_roundCeiling_minuteOfHour_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:40.000-04:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundCeilingCopy();
        assertEquals("2007-11-04T01:31:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_minuteOfHour_NewYork_Autumn_postCutover
    public void test_DateTime_roundCeiling_minuteOfHour_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:40.000-05:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundCeilingCopy();
        assertEquals("2007-11-04T01:31:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_secondOfMinute_NewYork_Autumn_preCutover
    public void test_DateTime_roundCeiling_secondOfMinute_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 500, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:40.500-04:00", dt.toString());
        DateTime rounded = dt.secondOfMinute().roundCeilingCopy();
        assertEquals("2007-11-04T01:30:41.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_secondOfMinute_NewYork_Autumn_postCutover
    public void test_DateTime_roundCeiling_secondOfMinute_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 500, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:40.500-05:00", dt.toString());
        DateTime rounded = dt.secondOfMinute().roundCeilingCopy();
        assertEquals("2007-11-04T01:30:41.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_MoscowIsCorrect_Spring
    public void test_MoscowIsCorrect_Spring() {

        DateTime pre = new DateTime(CUTOVER_MOSCOW_SPRING - 1L, ZONE_MOSCOW);
        assertEquals("2007-03-25T01:59:59.999+03:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_MOSCOW_SPRING, ZONE_MOSCOW);
        assertEquals("2007-03-25T03:00:00.000+04:00", at.toString());
        DateTime post = new DateTime(CUTOVER_MOSCOW_SPRING + 1L, ZONE_MOSCOW);
        assertEquals("2007-03-25T03:00:00.001+04:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Moscow_Spring
    public void test_getOffsetFromLocal_Moscow_Spring() {
        doTest_getOffsetFromLocal(3, 25, 1, 0, "2007-03-25T01:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 1,30, "2007-03-25T01:30:00.000+03:00", ZONE_MOSCOW);
        
        doTest_getOffsetFromLocal(3, 25, 2, 0, "2007-03-25T03:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 2,30, "2007-03-25T03:30:00.000+04:00", ZONE_MOSCOW);
        
        doTest_getOffsetFromLocal(3, 25, 3, 0, "2007-03-25T03:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 3,30, "2007-03-25T03:30:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 4, 0, "2007-03-25T04:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 5, 0, "2007-03-25T05:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 6, 0, "2007-03-25T06:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 7, 0, "2007-03-25T07:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 8, 0, "2007-03-25T08:00:00.000+04:00", ZONE_MOSCOW);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourAcross_Moscow_Spring
    public void test_DateTime_setHourAcross_Moscow_Spring() {
        DateTime dt = new DateTime(2007, 3, 25, 0, 0, 0, 0, ZONE_MOSCOW);
        assertEquals("2007-03-25T00:00:00.000+03:00", dt.toString());
        DateTime res = dt.hourOfDay().setCopy(4);
        assertEquals("2007-03-25T04:00:00.000+04:00", res.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourForward_Moscow_Spring
    public void test_DateTime_setHourForward_Moscow_Spring() {
        DateTime dt = new DateTime(2007, 3, 25, 0, 0, 0, 0, ZONE_MOSCOW);
        assertEquals("2007-03-25T00:00:00.000+03:00", dt.toString());
        
        try {
            dt.hourOfDay().setCopy(2);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourBack_Moscow_Spring
    public void test_DateTime_setHourBack_Moscow_Spring() {
        DateTime dt = new DateTime(2007, 3, 25, 8, 0, 0, 0, ZONE_MOSCOW);
        assertEquals("2007-03-25T08:00:00.000+04:00", dt.toString());
        
        try {
            dt.hourOfDay().setCopy(2);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_MoscowIsCorrect_Autumn
    public void test_MoscowIsCorrect_Autumn() {
        DateTime pre = new DateTime(CUTOVER_MOSCOW_AUTUMN - 1L, ZONE_MOSCOW);
        assertEquals("2007-10-28T02:59:59.999+04:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_MOSCOW_AUTUMN, ZONE_MOSCOW);
        assertEquals("2007-10-28T02:00:00.000+03:00", at.toString());
        DateTime post = new DateTime(CUTOVER_MOSCOW_AUTUMN + 1L, ZONE_MOSCOW);
        assertEquals("2007-10-28T02:00:00.001+03:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Moscow_Autumn
    public void test_getOffsetFromLocal_Moscow_Autumn() {
        doTest_getOffsetFromLocal(10, 28, 0, 0, "2007-10-28T00:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 0,30, "2007-10-28T00:30:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 1, 0, "2007-10-28T01:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 1,30, "2007-10-28T01:30:00.000+04:00", ZONE_MOSCOW);
        
        doTest_getOffsetFromLocal(10, 28, 2, 0, "2007-10-28T02:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 2,30, "2007-10-28T02:30:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 2,30,59,999, "2007-10-28T02:30:59.999+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 2,59,59,998, "2007-10-28T02:59:59.998+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 2,59,59,999, "2007-10-28T02:59:59.999+04:00", ZONE_MOSCOW);
        
        doTest_getOffsetFromLocal(10, 28, 3, 0, "2007-10-28T03:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 3,30, "2007-10-28T03:30:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 4, 0, "2007-10-28T04:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 5, 0, "2007-10-28T05:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 6, 0, "2007-10-28T06:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 7, 0, "2007-10-28T07:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 8, 0, "2007-10-28T08:00:00.000+03:00", ZONE_MOSCOW);
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Moscow_Autumn_overlap_mins
    public void test_getOffsetFromLocal_Moscow_Autumn_overlap_mins() {
        for (int min = 0; min < 60; min++) {
            if (min < 10) {
                doTest_getOffsetFromLocal(10, 28, 2, min, "2007-10-28T02:0" + min + ":00.000+04:00", ZONE_MOSCOW);
            } else {
                doTest_getOffsetFromLocal(10, 28, 2, min, "2007-10-28T02:" + min + ":00.000+04:00", ZONE_MOSCOW);
            }
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_constructor_Moscow_Autumn
    public void test_DateTime_constructor_Moscow_Autumn() {
        DateTime dt = new DateTime(2007, 10, 28, 2, 30, ZONE_MOSCOW);
        assertEquals("2007-10-28T02:30:00.000+04:00", dt.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusHour_Moscow_Autumn
    public void test_DateTime_plusHour_Moscow_Autumn() {
        DateTime dt = new DateTime(2007, 10, 27, 19, 0, 0, 0, ZONE_MOSCOW);
        assertEquals("2007-10-27T19:00:00.000+04:00", dt.toString());
        
        DateTime plus6 = dt.plusHours(6);
        assertEquals("2007-10-28T01:00:00.000+04:00", plus6.toString());
        DateTime plus7 = dt.plusHours(7);
        assertEquals("2007-10-28T02:00:00.000+04:00", plus7.toString());
        DateTime plus8 = dt.plusHours(8);
        assertEquals("2007-10-28T02:00:00.000+03:00", plus8.toString());
        DateTime plus9 = dt.plusHours(9);
        assertEquals("2007-10-28T03:00:00.000+03:00", plus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusHour_Moscow_Autumn
    public void test_DateTime_minusHour_Moscow_Autumn() {
        DateTime dt = new DateTime(2007, 10, 28, 9, 0, 0, 0, ZONE_MOSCOW);
        assertEquals("2007-10-28T09:00:00.000+03:00", dt.toString());
        
        DateTime minus6 = dt.minusHours(6);
        assertEquals("2007-10-28T03:00:00.000+03:00", minus6.toString());
        DateTime minus7 = dt.minusHours(7);
        assertEquals("2007-10-28T02:00:00.000+03:00", minus7.toString());
        DateTime minus8 = dt.minusHours(8);
        assertEquals("2007-10-28T02:00:00.000+04:00", minus8.toString());
        DateTime minus9 = dt.minusHours(9);
        assertEquals("2007-10-28T01:00:00.000+04:00", minus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_GuatemataIsCorrect_Autumn
    public void test_GuatemataIsCorrect_Autumn() {
        DateTime pre = new DateTime(CUTOVER_GUATEMALA_AUTUMN - 1L, ZONE_GUATEMALA);
        assertEquals("2006-09-30T23:59:59.999-05:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_GUATEMALA_AUTUMN, ZONE_GUATEMALA);
        assertEquals("2006-09-30T23:00:00.000-06:00", at.toString());
        DateTime post = new DateTime(CUTOVER_GUATEMALA_AUTUMN + 1L, ZONE_GUATEMALA);
        assertEquals("2006-09-30T23:00:00.001-06:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Guatemata_Autumn
    public void test_getOffsetFromLocal_Guatemata_Autumn() {
        doTest_getOffsetFromLocal( 2006, 9,30,23, 0,
                                  "2006-09-30T23:00:00.000-05:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006, 9,30,23,30,
                                  "2006-09-30T23:30:00.000-05:00", ZONE_GUATEMALA);
        
        doTest_getOffsetFromLocal( 2006, 9,30,23, 0,
                                  "2006-09-30T23:00:00.000-05:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006, 9,30,23,30,
                                  "2006-09-30T23:30:00.000-05:00", ZONE_GUATEMALA);
        
        doTest_getOffsetFromLocal( 2006,10, 1, 0, 0,
                                  "2006-10-01T00:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 0,30,
                                  "2006-10-01T00:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 1, 0,
                                  "2006-10-01T01:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 1,30,
                                  "2006-10-01T01:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 2, 0,
                                  "2006-10-01T02:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 2,30,
                                  "2006-10-01T02:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 3, 0,
                                  "2006-10-01T03:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 3,30,
                                  "2006-10-01T03:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 4, 0,
                                  "2006-10-01T04:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 4,30,
                                  "2006-10-01T04:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 5, 0,
                                  "2006-10-01T05:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 5,30,
                                  "2006-10-01T05:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 6, 0,
                                  "2006-10-01T06:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 6,30,
                                  "2006-10-01T06:30:00.000-06:00", ZONE_GUATEMALA);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusHour_Guatemata_Autumn
    public void test_DateTime_plusHour_Guatemata_Autumn() {
        DateTime dt = new DateTime(2006, 9, 30, 20, 0, 0, 0, ZONE_GUATEMALA);
        assertEquals("2006-09-30T20:00:00.000-05:00", dt.toString());
        
        DateTime plus1 = dt.plusHours(1);
        assertEquals("2006-09-30T21:00:00.000-05:00", plus1.toString());
        DateTime plus2 = dt.plusHours(2);
        assertEquals("2006-09-30T22:00:00.000-05:00", plus2.toString());
        DateTime plus3 = dt.plusHours(3);
        assertEquals("2006-09-30T23:00:00.000-05:00", plus3.toString());
        DateTime plus4 = dt.plusHours(4);
        assertEquals("2006-09-30T23:00:00.000-06:00", plus4.toString());
        DateTime plus5 = dt.plusHours(5);
        assertEquals("2006-10-01T00:00:00.000-06:00", plus5.toString());
        DateTime plus6 = dt.plusHours(6);
        assertEquals("2006-10-01T01:00:00.000-06:00", plus6.toString());
        DateTime plus7 = dt.plusHours(7);
        assertEquals("2006-10-01T02:00:00.000-06:00", plus7.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusHour_Guatemata_Autumn
    public void test_DateTime_minusHour_Guatemata_Autumn() {
        DateTime dt = new DateTime(2006, 10, 1, 2, 0, 0, 0, ZONE_GUATEMALA);
        assertEquals("2006-10-01T02:00:00.000-06:00", dt.toString());
        
        DateTime minus1 = dt.minusHours(1);
        assertEquals("2006-10-01T01:00:00.000-06:00", minus1.toString());
        DateTime minus2 = dt.minusHours(2);
        assertEquals("2006-10-01T00:00:00.000-06:00", minus2.toString());
        DateTime minus3 = dt.minusHours(3);
        assertEquals("2006-09-30T23:00:00.000-06:00", minus3.toString());
        DateTime minus4 = dt.minusHours(4);
        assertEquals("2006-09-30T23:00:00.000-05:00", minus4.toString());
        DateTime minus5 = dt.minusHours(5);
        assertEquals("2006-09-30T22:00:00.000-05:00", minus5.toString());
        DateTime minus6 = dt.minusHours(6);
        assertEquals("2006-09-30T21:00:00.000-05:00", minus6.toString());
        DateTime minus7 = dt.minusHours(7);
        assertEquals("2006-09-30T20:00:00.000-05:00", minus7.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_JustAfterLastEverOverlap
    public void test_DateTime_JustAfterLastEverOverlap() {
        
        DateTimeZone zone = new DateTimeZoneBuilder()
            .setStandardOffset(-3 * DateTimeConstants.MILLIS_PER_HOUR)
            .addRecurringSavings("SUMMER", 1 * DateTimeConstants.MILLIS_PER_HOUR, 2000, 2008,
                                    'w', 4, 10, 0, true, 23 * DateTimeConstants.MILLIS_PER_HOUR)
            .addRecurringSavings("WINTER", 0, 2000, 2008,
                                    'w', 8, 10, 0, true, 0 * DateTimeConstants.MILLIS_PER_HOUR)
            .toDateTimeZone("Zone", false);
        
        LocalDate date = new LocalDate(2008, 8, 10);
        assertEquals("2008-08-10", date.toString());
        
        DateTime dt = date.toDateTimeAtStartOfDay(zone);
        assertEquals("2008-08-10T00:00:00.000-03:00", dt.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMinuteOfHourInDstChange_mockZone
    public void testWithMinuteOfHourInDstChange_mockZone() {
        DateTime cutover = new DateTime(2010, 10, 31, 1, 15, DateTimeZone.forOffsetHoursMinutes(0, 30));
        assertEquals("2010-10-31T01:15:00.000+00:30", cutover.toString());
        DateTimeZone halfHourZone = new MockZone(cutover.getMillis(), 3600000, -1800);
        DateTime pre = new DateTime(2010, 10, 31, 1, 0, halfHourZone);
        assertEquals("2010-10-31T01:00:00.000+01:00", pre.toString());
        DateTime post = new DateTime(2010, 10, 31, 1, 59, halfHourZone);
        assertEquals("2010-10-31T01:59:00.000+00:30", post.toString());
        
        DateTime testPre1 = pre.withMinuteOfHour(30);
        assertEquals("2010-10-31T01:30:00.000+01:00", testPre1.toString());  
        DateTime testPre2 = pre.withMinuteOfHour(50);
        assertEquals("2010-10-31T01:50:00.000+00:30", testPre2.toString());
        
        DateTime testPost1 = post.withMinuteOfHour(30);
        assertEquals("2010-10-31T01:30:00.000+00:30", testPost1.toString());  
        DateTime testPost2 = post.withMinuteOfHour(10);
        assertEquals("2010-10-31T01:10:00.000+01:00", testPost2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithHourOfDayInDstChange
    public void testWithHourOfDayInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.withHourOfDay(2);
        assertEquals("2010-10-31T02:30:10.123+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMinuteOfHourInDstChange
    public void testWithMinuteOfHourInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.withMinuteOfHour(0);
        assertEquals("2010-10-31T02:00:10.123+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithSecondOfMinuteInDstChange
    public void testWithSecondOfMinuteInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.withSecondOfMinute(0);
        assertEquals("2010-10-31T02:30:00.123+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMillisOfSecondInDstChange_Paris_summer
    public void testWithMillisOfSecondInDstChange_Paris_summer() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.withMillisOfSecond(0);
        assertEquals("2010-10-31T02:30:10.000+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMillisOfSecondInDstChange_Paris_winter
    public void testWithMillisOfSecondInDstChange_Paris_winter() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+01:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+01:00", dateTime.toString());
        DateTime test = dateTime.withMillisOfSecond(0);
        assertEquals("2010-10-31T02:30:10.000+01:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMillisOfSecondInDstChange_NewYork_summer
    public void testWithMillisOfSecondInDstChange_NewYork_summer() {
        DateTime dateTime = new DateTime("2007-11-04T01:30:00.123-04:00", ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.123-04:00", dateTime.toString());
        DateTime test = dateTime.withMillisOfSecond(0);
        assertEquals("2007-11-04T01:30:00.000-04:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMillisOfSecondInDstChange_NewYork_winter
    public void testWithMillisOfSecondInDstChange_NewYork_winter() {
        DateTime dateTime = new DateTime("2007-11-04T01:30:00.123-05:00", ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.123-05:00", dateTime.toString());
        DateTime test = dateTime.withMillisOfSecond(0);
        assertEquals("2007-11-04T01:30:00.000-05:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testPlusMinutesInDstChange
    public void testPlusMinutesInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.plusMinutes(1);
        assertEquals("2010-10-31T02:31:10.123+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testPlusSecondsInDstChange
    public void testPlusSecondsInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.plusSeconds(1);
        assertEquals("2010-10-31T02:30:11.123+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testPlusMillisInDstChange
    public void testPlusMillisInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.plusMillis(1);
        assertEquals("2010-10-31T02:30:10.124+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testBug2182444_usCentral
    public void testBug2182444_usCentral() {
        Chronology chronUSCentral = GregorianChronology.getInstance(DateTimeZone.forID("US/Central"));
        Chronology chronUTC = GregorianChronology.getInstance(DateTimeZone.UTC);
        DateTime usCentralStandardInUTC = new DateTime(2008, 11, 2, 7, 0, 0, 0, chronUTC);
        DateTime usCentralDaylightInUTC = new DateTime(2008, 11, 2, 6, 0, 0, 0, chronUTC);
        assertTrue("Should be standard time", chronUSCentral.getZone().isStandardOffset(usCentralStandardInUTC.getMillis()));
        assertFalse("Should be daylight time", chronUSCentral.getZone().isStandardOffset(usCentralDaylightInUTC.getMillis()));
        
        DateTime usCentralStandardInUSCentral = usCentralStandardInUTC.toDateTime(chronUSCentral);
        DateTime usCentralDaylightInUSCentral = usCentralDaylightInUTC.toDateTime(chronUSCentral);
        assertEquals(1, usCentralStandardInUSCentral.getHourOfDay());
        assertEquals(usCentralStandardInUSCentral.getHourOfDay(), usCentralDaylightInUSCentral.getHourOfDay());
        assertTrue(usCentralStandardInUSCentral.getMillis() != usCentralDaylightInUSCentral.getMillis());
        assertEquals(usCentralStandardInUSCentral, usCentralStandardInUSCentral.withHourOfDay(1));
        assertEquals(usCentralStandardInUSCentral.getMillis() + 3, usCentralStandardInUSCentral.withMillisOfSecond(3).getMillis());
        assertEquals(usCentralDaylightInUSCentral, usCentralDaylightInUSCentral.withHourOfDay(1));
        assertEquals(usCentralDaylightInUSCentral.getMillis() + 3, usCentralDaylightInUSCentral.withMillisOfSecond(3).getMillis());
    }

// org.joda.time.TestDateTimeZoneCutover::testBug2182444_ausNSW
    public void testBug2182444_ausNSW() {
        Chronology chronAusNSW = GregorianChronology.getInstance(DateTimeZone.forID("Australia/NSW"));
        Chronology chronUTC = GregorianChronology.getInstance(DateTimeZone.UTC);
        DateTime australiaNSWStandardInUTC = new DateTime(2008, 4, 5, 16, 0, 0, 0, chronUTC);
        DateTime australiaNSWDaylightInUTC = new DateTime(2008, 4, 5, 15, 0, 0, 0, chronUTC);
        assertTrue("Should be standard time", chronAusNSW.getZone().isStandardOffset(australiaNSWStandardInUTC.getMillis()));
        assertFalse("Should be daylight time", chronAusNSW.getZone().isStandardOffset(australiaNSWDaylightInUTC.getMillis()));
        
        DateTime australiaNSWStandardInAustraliaNSW = australiaNSWStandardInUTC.toDateTime(chronAusNSW);
        DateTime australiaNSWDaylightInAusraliaNSW = australiaNSWDaylightInUTC.toDateTime(chronAusNSW);
        assertEquals(2, australiaNSWStandardInAustraliaNSW.getHourOfDay());
        assertEquals(australiaNSWStandardInAustraliaNSW.getHourOfDay(), australiaNSWDaylightInAusraliaNSW.getHourOfDay());
        assertTrue(australiaNSWStandardInAustraliaNSW.getMillis() != australiaNSWDaylightInAusraliaNSW.getMillis());
        assertEquals(australiaNSWStandardInAustraliaNSW, australiaNSWStandardInAustraliaNSW.withHourOfDay(2));
        assertEquals(australiaNSWStandardInAustraliaNSW.getMillis() + 3, australiaNSWStandardInAustraliaNSW.withMillisOfSecond(3).getMillis());
        assertEquals(australiaNSWDaylightInAusraliaNSW, australiaNSWDaylightInAusraliaNSW.withHourOfDay(2));
        assertEquals(australiaNSWDaylightInAusraliaNSW.getMillis() + 3, australiaNSWDaylightInAusraliaNSW.withMillisOfSecond(3).getMillis());
    }

// org.joda.time.TestDateTimeZoneCutover::testPeriod
    public void testPeriod() {
        DateTime a = new DateTime("2010-10-31T02:00:00.000+02:00", ZONE_PARIS);
        DateTime b = new DateTime("2010-10-31T02:01:00.000+02:00", ZONE_PARIS);
        Period period = new Period(a, b, PeriodType.standard());
        assertEquals("PT1M", period.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testForum4013394_retainOffsetWhenRetainFields_sameOffsetsDifferentZones
    public void testForum4013394_retainOffsetWhenRetainFields_sameOffsetsDifferentZones() {
        final DateTimeZone fromDTZ = DateTimeZone.forID("Europe/London");
        final DateTimeZone toDTZ = DateTimeZone.forID("Europe/Lisbon");
        DateTime baseBefore = new DateTime(2007, 10, 28, 1, 15, fromDTZ).minusHours(1);
        DateTime baseAfter = new DateTime(2007, 10, 28, 1, 15, fromDTZ);
        DateTime testBefore = baseBefore.withZoneRetainFields(toDTZ);
        DateTime testAfter = baseAfter.withZoneRetainFields(toDTZ);
        
        assertEquals(baseBefore.toString(), testBefore.toString());
        assertEquals(baseAfter.toString(), testAfter.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testBug3192457_adjustOffset
    public void testBug3192457_adjustOffset() {
        final DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTime base = new DateTime(2007, 10, 28, 3, 15, zone);
        DateTime baseBefore = base.minusHours(2);
        DateTime baseAfter = base.minusHours(1);
        
        assertSame(base, base.withEarlierOffsetAtOverlap());
        assertSame(base, base.withLaterOffsetAtOverlap());
        assertSame(baseBefore, baseBefore.withEarlierOffsetAtOverlap());
        assertSame(baseAfter, baseAfter.withLaterOffsetAtOverlap());
        
        assertEquals(baseBefore, baseAfter.withEarlierOffsetAtOverlap());
        assertEquals(baseAfter, baseAfter.withLaterOffsetAtOverlap());
    }

// org.joda.time.TestDateTimeZoneCutover::testDateTimeCreation_athens
    public void testDateTimeCreation_athens() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Athens");
        DateTime base = new DateTime(2011, 10, 30, 3, 15, zone);
        assertEquals("2011-10-30T03:15:00.000+03:00", base.toString());
        assertEquals("2011-10-30T03:15:00.000+02:00", base.plusHours(1).toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testDateTimeCreation_paris
    public void testDateTimeCreation_paris() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTime base = new DateTime(2011, 10, 30, 2, 15, zone);
        assertEquals("2011-10-30T02:15:00.000+02:00", base.toString());
        assertEquals("2011-10-30T02:15:00.000+01:00", base.plusHours(1).toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testDateTimeCreation_london
    public void testDateTimeCreation_london() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime base = new DateTime(2011, 10, 30, 1, 15, zone);
        assertEquals("2011-10-30T01:15:00.000+01:00", base.toString());
        assertEquals("2011-10-30T01:15:00.000Z", base.plusHours(1).toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testDateTimeCreation_newYork
    public void testDateTimeCreation_newYork() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        DateTime base = new DateTime(2010, 11, 7, 1, 15, zone);
        assertEquals("2010-11-07T01:15:00.000-04:00", base.toString());
        assertEquals("2010-11-07T01:15:00.000-05:00", base.plusHours(1).toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testDateTimeCreation_losAngeles
    public void testDateTimeCreation_losAngeles() {
        DateTimeZone zone = DateTimeZone.forID("America/Los_Angeles");
        DateTime base = new DateTime(2010, 11, 7, 1, 15, zone);
        assertEquals("2010-11-07T01:15:00.000-07:00", base.toString());
        assertEquals("2010-11-07T01:15:00.000-08:00", base.plusHours(1).toString());
    }

// org.joda.time.TestDateTime_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestDateTime_Basics::testGet_DateTimeField
    public void testGet_DateTimeField() {
        DateTime test = new DateTime();
        assertEquals(1, test.get(ISO_DEFAULT.era()));
        assertEquals(20, test.get(ISO_DEFAULT.centuryOfEra()));
        assertEquals(2, test.get(ISO_DEFAULT.yearOfCentury()));
        assertEquals(2002, test.get(ISO_DEFAULT.yearOfEra()));
        assertEquals(2002, test.get(ISO_DEFAULT.year()));
        assertEquals(6, test.get(ISO_DEFAULT.monthOfYear()));
        assertEquals(9, test.get(ISO_DEFAULT.dayOfMonth()));
        assertEquals(2002, test.get(ISO_DEFAULT.weekyear()));
        assertEquals(23, test.get(ISO_DEFAULT.weekOfWeekyear()));
        assertEquals(7, test.get(ISO_DEFAULT.dayOfWeek()));
        assertEquals(160, test.get(ISO_DEFAULT.dayOfYear()));
        assertEquals(0, test.get(ISO_DEFAULT.halfdayOfDay()));
        assertEquals(1, test.get(ISO_DEFAULT.hourOfHalfday()));
        assertEquals(1, test.get(ISO_DEFAULT.clockhourOfDay()));
        assertEquals(1, test.get(ISO_DEFAULT.clockhourOfHalfday()));
        assertEquals(1, test.get(ISO_DEFAULT.hourOfDay()));
        assertEquals(0, test.get(ISO_DEFAULT.minuteOfHour()));
        assertEquals(60, test.get(ISO_DEFAULT.minuteOfDay()));
        assertEquals(0, test.get(ISO_DEFAULT.secondOfMinute()));
        assertEquals(60 * 60, test.get(ISO_DEFAULT.secondOfDay()));
        assertEquals(0, test.get(ISO_DEFAULT.millisOfSecond()));
        assertEquals(60 * 60 * 1000, test.get(ISO_DEFAULT.millisOfDay()));
        try {
            test.get((DateTimeField) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        DateTime test = new DateTime();
        assertEquals(1, test.get(DateTimeFieldType.era()));
        assertEquals(20, test.get(DateTimeFieldType.centuryOfEra()));
        assertEquals(2, test.get(DateTimeFieldType.yearOfCentury()));
        assertEquals(2002, test.get(DateTimeFieldType.yearOfEra()));
        assertEquals(2002, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(2002, test.get(DateTimeFieldType.weekyear()));
        assertEquals(23, test.get(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(7, test.get(DateTimeFieldType.dayOfWeek()));
        assertEquals(160, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(0, test.get(DateTimeFieldType.halfdayOfDay()));
        assertEquals(1, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(1, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(1, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(1, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(60, test.get(DateTimeFieldType.minuteOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(60 * 60, test.get(DateTimeFieldType.secondOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.millisOfSecond()));
        assertEquals(60 * 60 * 1000, test.get(DateTimeFieldType.millisOfDay()));
        try {
            test.get((DateTimeFieldType) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testIsSupported_DateTimeFieldType
    public void testIsSupported_DateTimeFieldType() {
        DateTime test = new DateTime();
        assertEquals(true, test.isSupported(DateTimeFieldType.era()));
        assertEquals(true, test.isSupported(DateTimeFieldType.centuryOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfCentury()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfWeek()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.halfdayOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfHalfday()));
        assertEquals(true, test.isSupported(DateTimeFieldType.clockhourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfMinute()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfSecond()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfDay()));
        assertEquals(false, test.isSupported(null));
    }

// org.joda.time.TestDateTime_Basics::testGetters
    public void testGetters() {
        DateTime test = new DateTime();
        
        assertEquals(ISO_DEFAULT, test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        
        assertEquals(1, test.getEra());
        assertEquals(20, test.getCenturyOfEra());
        assertEquals(2, test.getYearOfCentury());
        assertEquals(2002, test.getYearOfEra());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(2002, test.getWeekyear());
        assertEquals(23, test.getWeekOfWeekyear());
        assertEquals(7, test.getDayOfWeek());
        assertEquals(160, test.getDayOfYear());
        assertEquals(1, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(60, test.getMinuteOfDay());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(60 * 60, test.getSecondOfDay());
        assertEquals(0, test.getMillisOfSecond());
        assertEquals(60 * 60 * 1000, test.getMillisOfDay());
    }

// org.joda.time.TestDateTime_Basics::testWithers
    public void testWithers() {
        DateTime test = new DateTime(1970, 6, 9, 10, 20, 30, 40, GJ_DEFAULT);
        check(test.withYear(2000), 2000, 6, 9, 10, 20, 30, 40);
        check(test.withMonthOfYear(2), 1970, 2, 9, 10, 20, 30, 40);
        check(test.withDayOfMonth(2), 1970, 6, 2, 10, 20, 30, 40);
        check(test.withDayOfYear(6), 1970, 1, 6, 10, 20, 30, 40);
        check(test.withDayOfWeek(6), 1970, 6, 13, 10, 20, 30, 40);
        check(test.withWeekOfWeekyear(6), 1970, 2, 3, 10, 20, 30, 40);
        check(test.withWeekyear(1971), 1971, 6, 15, 10, 20, 30, 40);
        check(test.withYearOfCentury(60), 1960, 6, 9, 10, 20, 30, 40);
        check(test.withCenturyOfEra(21), 2070, 6, 9, 10, 20, 30, 40);
        check(test.withYearOfEra(1066), 1066, 6, 9, 10, 20, 30, 40);
        check(test.withEra(DateTimeConstants.BC), -1970, 6, 9, 10, 20, 30, 40);
        check(test.withHourOfDay(6), 1970, 6, 9, 6, 20, 30, 40);
        check(test.withMinuteOfHour(6), 1970, 6, 9, 10, 6, 30, 40);
        check(test.withSecondOfMinute(6), 1970, 6, 9, 10, 20, 6, 40);
        check(test.withMillisOfSecond(6), 1970, 6, 9, 10, 20, 30, 6);
        check(test.withMillisOfDay(61234), 1970, 6, 9, 0, 1, 1, 234);
        
        try {
            test.withMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test2 = new DateTime(TEST_TIME1);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        DateTime test3 = new DateTime(TEST_TIME2);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(false, test1.equals(new DateTime(TEST_TIME1, GREGORIAN_DEFAULT)));
        assertEquals(true, new DateTime(TEST_TIME1, new MockEqualsChronology()).equals(new DateTime(TEST_TIME1, new MockEqualsChronology())));
        assertEquals(false, new DateTime(TEST_TIME1, new MockEqualsChronology()).equals(new DateTime(TEST_TIME1, ISO_DEFAULT)));
    }
