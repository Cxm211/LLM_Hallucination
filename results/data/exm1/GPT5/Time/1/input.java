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

// org.joda.time.TestBasePartial::testSetMethods
    public void testSetMethods() throws Throwable {
        MockPartial mock = new MockPartial();
        assertEquals(1970, mock.getYear());
        assertEquals(1, mock.getMonthOfYear());
        
        mock.setYear(2004);
        assertEquals(2004, mock.getYear());
        assertEquals(1, mock.getMonthOfYear());
        
        mock.setMonthOfYear(6);
        assertEquals(2004, mock.getYear());
        assertEquals(6, mock.getMonthOfYear());
        
        mock.set(2005, 5);
        assertEquals(2005, mock.getYear());
        assertEquals(5, mock.getMonthOfYear());
        
        try {
            mock.setMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(2005, mock.getYear());
        assertEquals(5, mock.getMonthOfYear());
        
        try {
            mock.setMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(2005, mock.getYear());
        assertEquals(5, mock.getMonthOfYear());
    }

// org.joda.time.TestBaseSingleFieldPeriod::testFactory_between_RInstant
    public void testFactory_between_RInstant() {
        
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 12, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 15, 18, 0, 0, 0, PARIS);
        
        assertEquals(3, Single.between(start, end1, DurationFieldType.days()));
        assertEquals(0, Single.between(start, start, DurationFieldType.days()));
        assertEquals(0, Single.between(end1, end1, DurationFieldType.days()));
        assertEquals(-3, Single.between(end1, start, DurationFieldType.days()));
        assertEquals(6, Single.between(start, end2, DurationFieldType.days()));
        try {
            Single.between(start, (ReadableInstant) null, DurationFieldType.days());
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Single.between((ReadableInstant) null, end1, DurationFieldType.days());
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Single.between((ReadableInstant) null, (ReadableInstant) null, DurationFieldType.days());
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestBaseSingleFieldPeriod::testFactory_between_RPartial
    public void testFactory_between_RPartial() {
        LocalDate start = new LocalDate(2006, 6, 9);
        LocalDate end1 = new LocalDate(2006, 6, 12);
        YearMonthDay end2 = new YearMonthDay(2006, 6, 15);
        
        Single zero = new Single(0);
        assertEquals(3, Single.between(start, end1, zero));
        assertEquals(0, Single.between(start, start, zero));
        assertEquals(0, Single.between(end1, end1, zero));
        assertEquals(-3, Single.between(end1, start, zero));
        assertEquals(6, Single.between(start, end2, zero));
        try {
            Single.between(start, (ReadablePartial) null, zero);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Single.between((ReadablePartial) null, end1, zero);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Single.between((ReadablePartial) null, (ReadablePartial) null, zero);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Single.between(start, new LocalTime(), zero);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Single.between(new Partial(DateTimeFieldType.dayOfWeek(), 2), new Partial(DateTimeFieldType.dayOfMonth(), 3), zero);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        Partial p = new Partial(
                new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.hourOfDay()},
                new int[] {1, 2});
        try {
            Single.between(p, p, zero);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestBaseSingleFieldPeriod::testFactory_standardPeriodIn_RPeriod
    public void testFactory_standardPeriodIn_RPeriod() {
        assertEquals(0, Single.standardPeriodIn((ReadablePeriod) null, DateTimeConstants.MILLIS_PER_DAY));
        assertEquals(0, Single.standardPeriodIn(Period.ZERO, DateTimeConstants.MILLIS_PER_DAY));
        assertEquals(1, Single.standardPeriodIn(new Period(0, 0, 0, 1, 0, 0, 0, 0), DateTimeConstants.MILLIS_PER_DAY));
        assertEquals(123, Single.standardPeriodIn(Period.days(123), DateTimeConstants.MILLIS_PER_DAY));
        assertEquals(-987, Single.standardPeriodIn(Period.days(-987), DateTimeConstants.MILLIS_PER_DAY));
        assertEquals(1, Single.standardPeriodIn(Period.hours(47), DateTimeConstants.MILLIS_PER_DAY));
        assertEquals(2, Single.standardPeriodIn(Period.hours(48), DateTimeConstants.MILLIS_PER_DAY));
        assertEquals(2, Single.standardPeriodIn(Period.hours(49), DateTimeConstants.MILLIS_PER_DAY));
        assertEquals(14, Single.standardPeriodIn(Period.weeks(2), DateTimeConstants.MILLIS_PER_DAY));
        try {
            Single.standardPeriodIn(Period.months(1), DateTimeConstants.MILLIS_PER_DAY);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestBaseSingleFieldPeriod::testValueIndexMethods
    public void testValueIndexMethods() {
        Single test = new Single(20);
        assertEquals(1, test.size());
        assertEquals(20, test.getValue(0));
        try {
            test.getValue(1);
            fail();
        } catch (IndexOutOfBoundsException ex) {
            
        }
    }

// org.joda.time.TestBaseSingleFieldPeriod::testFieldTypeIndexMethods
    public void testFieldTypeIndexMethods() {
        Single test = new Single(20);
        assertEquals(1, test.size());
        assertEquals(DurationFieldType.days(), test.getFieldType(0));
        try {
            test.getFieldType(1);
            fail();
        } catch (IndexOutOfBoundsException ex) {
            
        }
    }

// org.joda.time.TestBaseSingleFieldPeriod::testIsSupported
    public void testIsSupported() {
        Single test = new Single(20);
        assertEquals(false, test.isSupported(DurationFieldType.years()));
        assertEquals(false, test.isSupported(DurationFieldType.months()));
        assertEquals(false, test.isSupported(DurationFieldType.weeks()));
        assertEquals(true, test.isSupported(DurationFieldType.days()));
        assertEquals(false, test.isSupported(DurationFieldType.hours()));
        assertEquals(false, test.isSupported(DurationFieldType.minutes()));
        assertEquals(false, test.isSupported(DurationFieldType.seconds()));
        assertEquals(false, test.isSupported(DurationFieldType.millis()));
    }

// org.joda.time.TestBaseSingleFieldPeriod::testGet
    public void testGet() {
        Single test = new Single(20);
        assertEquals(0, test.get(DurationFieldType.years()));
        assertEquals(0, test.get(DurationFieldType.months()));
        assertEquals(0, test.get(DurationFieldType.weeks()));
        assertEquals(20, test.get(DurationFieldType.days()));
        assertEquals(0, test.get(DurationFieldType.hours()));
        assertEquals(0, test.get(DurationFieldType.minutes()));
        assertEquals(0, test.get(DurationFieldType.seconds()));
        assertEquals(0, test.get(DurationFieldType.millis()));
    }

// org.joda.time.TestBaseSingleFieldPeriod::testEqualsHashCode
    public void testEqualsHashCode() {
        Single testA = new Single(20);
        Single testB = new Single(20);
        assertEquals(true, testA.equals(testB));
        assertEquals(true, testB.equals(testA));
        assertEquals(true, testA.equals(testA));
        assertEquals(true, testB.equals(testB));
        assertEquals(true, testA.hashCode() == testB.hashCode());
        assertEquals(true, testA.hashCode() == testA.hashCode());
        assertEquals(true, testB.hashCode() == testB.hashCode());
        
        Single testC = new Single(30);
        assertEquals(false, testA.equals(testC));
        assertEquals(false, testB.equals(testC));
        assertEquals(false, testC.equals(testA));
        assertEquals(false, testC.equals(testB));
        assertEquals(false, testA.hashCode() == testC.hashCode());
        assertEquals(false, testB.hashCode() == testC.hashCode());
        
        assertEquals(true, testA.equals(Days.days(20)));
        assertEquals(true, testA.equals(new Period(0, 0, 0, 20, 0, 0, 0, 0, PeriodType.days())));
        assertEquals(false, testA.equals(Period.days(2)));
        assertEquals(false, testA.equals("Hello"));
        assertEquals(false, testA.equals(Hours.hours(2)));
        assertEquals(false, testA.equals(null));
    }

// org.joda.time.TestBaseSingleFieldPeriod::testCompareTo
    public void testCompareTo() {
        Single test1 = new Single(21);
        Single test2 = new Single(22);
        Single test3 = new Single(23);
        assertEquals(true, test1.compareTo(test1) == 0);
        assertEquals(true, test1.compareTo(test2) < 0);
        assertEquals(true, test1.compareTo(test3) < 0);
        assertEquals(true, test2.compareTo(test1) > 0);
        assertEquals(true, test2.compareTo(test2) == 0);
        assertEquals(true, test2.compareTo(test3) < 0);
        assertEquals(true, test3.compareTo(test1) > 0);
        assertEquals(true, test3.compareTo(test2) > 0);
        assertEquals(true, test3.compareTo(test3) == 0);
        

        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {
            
        }
    }

// org.joda.time.TestBaseSingleFieldPeriod::testToPeriod
    public void testToPeriod() {
        Single test = new Single(20);
        Period expected = Period.days(20);
        assertEquals(expected, test.toPeriod());
    }

// org.joda.time.TestBaseSingleFieldPeriod::testToMutablePeriod
    public void testToMutablePeriod() {
        Single test = new Single(20);
        MutablePeriod expected = new MutablePeriod(0, 0, 0, 20, 0, 0, 0, 0);
        assertEquals(expected, test.toMutablePeriod());
    }

// org.joda.time.TestBaseSingleFieldPeriod::testGetSetValue
    public void testGetSetValue() {
        Single test = new Single(20);
        assertEquals(20, test.getValue());
        test.setValue(10);
        assertEquals(10, test.getValue());
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
            private static final long serialVersionUID = 1L;
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

// org.joda.time.TestDateMidnight_Properties::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetEra
    public void testPropertyGetEra() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().era(), test.era().getField());
        assertEquals("era", test.era().getName());
        assertEquals("Property[era]", test.era().toString());
        assertSame(test, test.era().getDateMidnight());
        assertEquals(1, test.era().get());
        assertEquals("AD", test.era().getAsText());
        assertEquals("ap. J.-C.", test.era().getAsText(Locale.FRENCH));
        assertEquals("AD", test.era().getAsShortText());
        assertEquals("ap. J.-C.", test.era().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().eras(), test.era().getDurationField());
        assertEquals(null, test.era().getRangeDurationField());
        assertEquals(2, test.era().getMaximumTextLength(null));
        assertEquals(9, test.era().getMaximumTextLength(Locale.FRENCH));
        assertEquals(2, test.era().getMaximumShortTextLength(null));
        assertEquals(9, test.era().getMaximumShortTextLength(Locale.FRENCH));
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetYearOfEra
    public void testPropertyGetYearOfEra() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().yearOfEra(), test.yearOfEra().getField());
        assertEquals("yearOfEra", test.yearOfEra().getName());
        assertEquals("Property[yearOfEra]", test.yearOfEra().toString());
        assertSame(test, test.yearOfEra().getDateMidnight());
        assertEquals(2004, test.yearOfEra().get());
        assertEquals("2004", test.yearOfEra().getAsText());
        assertEquals("2004", test.yearOfEra().getAsText(Locale.FRENCH));
        assertEquals("2004", test.yearOfEra().getAsShortText());
        assertEquals("2004", test.yearOfEra().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().years(), test.yearOfEra().getDurationField());
        assertEquals(test.getChronology().eras(), test.yearOfEra().getRangeDurationField());
        assertEquals(9, test.yearOfEra().getMaximumTextLength(null));
        assertEquals(9, test.yearOfEra().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetCenturyOfEra
    public void testPropertyGetCenturyOfEra() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().centuryOfEra(), test.centuryOfEra().getField());
        assertEquals("centuryOfEra", test.centuryOfEra().getName());
        assertEquals("Property[centuryOfEra]", test.centuryOfEra().toString());
        assertSame(test, test.centuryOfEra().getDateMidnight());
        assertEquals(20, test.centuryOfEra().get());
        assertEquals("20", test.centuryOfEra().getAsText());
        assertEquals("20", test.centuryOfEra().getAsText(Locale.FRENCH));
        assertEquals("20", test.centuryOfEra().getAsShortText());
        assertEquals("20", test.centuryOfEra().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().centuries(), test.centuryOfEra().getDurationField());
        assertEquals(test.getChronology().eras(), test.centuryOfEra().getRangeDurationField());
        assertEquals(7, test.centuryOfEra().getMaximumTextLength(null));
        assertEquals(7, test.centuryOfEra().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetYearOfCentury
    public void testPropertyGetYearOfCentury() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().yearOfCentury(), test.yearOfCentury().getField());
        assertEquals("yearOfCentury", test.yearOfCentury().getName());
        assertEquals("Property[yearOfCentury]", test.yearOfCentury().toString());
        assertSame(test, test.yearOfCentury().getDateMidnight());
        assertEquals(4, test.yearOfCentury().get());
        assertEquals("4", test.yearOfCentury().getAsText());
        assertEquals("4", test.yearOfCentury().getAsText(Locale.FRENCH));
        assertEquals("4", test.yearOfCentury().getAsShortText());
        assertEquals("4", test.yearOfCentury().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().years(), test.yearOfCentury().getDurationField());
        assertEquals(test.getChronology().centuries(), test.yearOfCentury().getRangeDurationField());
        assertEquals(2, test.yearOfCentury().getMaximumTextLength(null));
        assertEquals(2, test.yearOfCentury().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetWeekyear
    public void testPropertyGetWeekyear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().weekyear(), test.weekyear().getField());
        assertEquals("weekyear", test.weekyear().getName());
        assertEquals("Property[weekyear]", test.weekyear().toString());
        assertSame(test, test.weekyear().getDateMidnight());
        assertEquals(2004, test.weekyear().get());
        assertEquals("2004", test.weekyear().getAsText());
        assertEquals("2004", test.weekyear().getAsText(Locale.FRENCH));
        assertEquals("2004", test.weekyear().getAsShortText());
        assertEquals("2004", test.weekyear().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().weekyears(), test.weekyear().getDurationField());
        assertEquals(null, test.weekyear().getRangeDurationField());
        assertEquals(9, test.weekyear().getMaximumTextLength(null));
        assertEquals(9, test.weekyear().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetYear
    public void testPropertyGetYear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().year(), test.year().getField());
        assertEquals("year", test.year().getName());
        assertEquals("Property[year]", test.year().toString());
        assertSame(test, test.year().getDateMidnight());
        assertEquals(2004, test.year().get());
        assertEquals("2004", test.year().getAsText());
        assertEquals("2004", test.year().getAsText(Locale.FRENCH));
        assertEquals("2004", test.year().getAsShortText());
        assertEquals("2004", test.year().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().years(), test.year().getDurationField());
        assertEquals(null, test.year().getRangeDurationField());
        assertEquals(9, test.year().getMaximumTextLength(null));
        assertEquals(9, test.year().getMaximumShortTextLength(null));
        assertEquals(-292275054, test.year().getMinimumValue());
        assertEquals(-292275054, test.year().getMinimumValueOverall());
        assertEquals(292278993, test.year().getMaximumValue());
        assertEquals(292278993, test.year().getMaximumValueOverall());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetMonthOfYear
    public void testPropertyGetMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().monthOfYear(), test.monthOfYear().getField());
        assertEquals("monthOfYear", test.monthOfYear().getName());
        assertEquals("Property[monthOfYear]", test.monthOfYear().toString());
        assertSame(test, test.monthOfYear().getDateMidnight());
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
        test = new DateMidnight(2004, 7, 9);
        assertEquals("juillet", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("juil.", test.monthOfYear().getAsShortText(Locale.FRENCH));
        assertEquals(1, test.monthOfYear().getMinimumValue());
        assertEquals(1, test.monthOfYear().getMinimumValueOverall());
        assertEquals(12, test.monthOfYear().getMaximumValue());
        assertEquals(12, test.monthOfYear().getMaximumValueOverall());
        assertEquals(1, test.monthOfYear().getMinimumValue());
        assertEquals(1, test.monthOfYear().getMinimumValueOverall());
        assertEquals(12, test.monthOfYear().getMaximumValue());
        assertEquals(12, test.monthOfYear().getMaximumValueOverall());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertySetMonthOfYear
    public void testPropertySetMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight copy = test.monthOfYear().setCopy(8);
        assertEquals(2004, copy.getYear());
        assertEquals(8, copy.getMonthOfYear());
        assertEquals(9, copy.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertySetTextMonthOfYear
    public void testPropertySetTextMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight copy = test.monthOfYear().setCopy("8");
        assertEquals(2004, copy.getYear());
        assertEquals(8, copy.getMonthOfYear());
        assertEquals(9, copy.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertySetTextLocaleMonthOfYear
    public void testPropertySetTextLocaleMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight copy = test.monthOfYear().setCopy("mars", Locale.FRENCH);
        assertEquals(2004, copy.getYear());
        assertEquals(3, copy.getMonthOfYear());
        assertEquals(9, copy.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyAddMonthOfYear
    public void testPropertyAddMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight copy = test.monthOfYear().addToCopy(8);
        assertEquals(2005, copy.getYear());
        assertEquals(2, copy.getMonthOfYear());
        assertEquals(9, copy.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyAddLongMonthOfYear
    public void testPropertyAddLongMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight copy = test.monthOfYear().addToCopy(8L);
        assertEquals(2005, copy.getYear());
        assertEquals(2, copy.getMonthOfYear());
        assertEquals(9, copy.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyAddWrapFieldMonthOfYear
    public void testPropertyAddWrapFieldMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight copy = test.monthOfYear().addWrapFieldToCopy(8);
        assertEquals(2004, copy.getYear());
        assertEquals(2, copy.getMonthOfYear());
        assertEquals(9, copy.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetDifferenceMonthOfYear
    public void testPropertyGetDifferenceMonthOfYear() {
        DateMidnight test1 = new DateMidnight(2004, 6, 9);
        DateMidnight test2 = new DateMidnight(2004, 8, 9);
        assertEquals(-2, test1.monthOfYear().getDifference(test2));
        assertEquals(2, test2.monthOfYear().getDifference(test1));
        assertEquals(-2L, test1.monthOfYear().getDifferenceAsLong(test2));
        assertEquals(2L, test2.monthOfYear().getDifferenceAsLong(test1));
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyRoundFloorMonthOfYear
    public void testPropertyRoundFloorMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 16);
        DateMidnight copy = test.monthOfYear().roundFloorCopy();
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyRoundCeilingMonthOfYear
    public void testPropertyRoundCeilingMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 16);
        DateMidnight copy = test.monthOfYear().roundCeilingCopy();
        assertEquals("2004-07-01T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyRoundHalfFloorMonthOfYear
    public void testPropertyRoundHalfFloorMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 16);
        DateMidnight copy = test.monthOfYear().roundHalfFloorCopy();
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
        
        test = new DateMidnight(2004, 6, 17);
        copy = test.monthOfYear().roundHalfFloorCopy();
        assertEquals("2004-07-01T00:00:00.000+01:00", copy.toString());
        
        test = new DateMidnight(2004, 6, 15);
        copy = test.monthOfYear().roundHalfFloorCopy();
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyRoundHalfCeilingMonthOfYear
    public void testPropertyRoundHalfCeilingMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 16);
        DateMidnight copy = test.monthOfYear().roundHalfCeilingCopy();
        assertEquals("2004-07-01T00:00:00.000+01:00", copy.toString());
        
        test = new DateMidnight(2004, 6, 17);
        copy = test.monthOfYear().roundHalfCeilingCopy();
        assertEquals("2004-07-01T00:00:00.000+01:00", copy.toString());
        
        test = new DateMidnight(2004, 6, 15);
        copy = test.monthOfYear().roundHalfCeilingCopy();
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyRoundHalfEvenMonthOfYear
    public void testPropertyRoundHalfEvenMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 16);
        DateMidnight copy = test.monthOfYear().roundHalfEvenCopy();
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
        
        test = new DateMidnight(2004, 9, 16);
        copy = test.monthOfYear().roundHalfEvenCopy();
        assertEquals("2004-10-01T00:00:00.000+01:00", copy.toString());
        
        test = new DateMidnight(2004, 6, 17);
        copy = test.monthOfYear().roundHalfEvenCopy();
        assertEquals("2004-07-01T00:00:00.000+01:00", copy.toString());
        
        test = new DateMidnight(2004, 6, 15);
        copy = test.monthOfYear().roundHalfEvenCopy();
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyRemainderMonthOfYear
    public void testPropertyRemainderMonthOfYear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertEquals((9L - 1L) * DateTimeConstants.MILLIS_PER_DAY, test.monthOfYear().remainder());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetDayOfMonth
    public void testPropertyGetDayOfMonth() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().dayOfMonth(), test.dayOfMonth().getField());
        assertEquals("dayOfMonth", test.dayOfMonth().getName());
        assertEquals("Property[dayOfMonth]", test.dayOfMonth().toString());
        assertSame(test, test.dayOfMonth().getDateMidnight());
        assertEquals(9, test.dayOfMonth().get());
        assertEquals("9", test.dayOfMonth().getAsText());
        assertEquals("9", test.dayOfMonth().getAsText(Locale.FRENCH));
        assertEquals("9", test.dayOfMonth().getAsShortText());
        assertEquals("9", test.dayOfMonth().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().days(), test.dayOfMonth().getDurationField());
        assertEquals(test.getChronology().months(), test.dayOfMonth().getRangeDurationField());
        assertEquals(2, test.dayOfMonth().getMaximumTextLength(null));
        assertEquals(2, test.dayOfMonth().getMaximumShortTextLength(null));
        assertEquals(1, test.dayOfMonth().getMinimumValue());
        assertEquals(1, test.dayOfMonth().getMinimumValueOverall());
        assertEquals(30, test.dayOfMonth().getMaximumValue());
        assertEquals(31, test.dayOfMonth().getMaximumValueOverall());
        assertEquals(false, test.dayOfMonth().isLeap());
        assertEquals(0, test.dayOfMonth().getLeapAmount());
        assertEquals(null, test.dayOfMonth().getLeapDurationField());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyWithMaximumValueDayOfMonth
    public void testPropertyWithMaximumValueDayOfMonth() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight copy = test.dayOfMonth().withMaximumValue();
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-30T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyWithMinimumValueDayOfMonth
    public void testPropertyWithMinimumValueDayOfMonth() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        DateMidnight copy = test.dayOfMonth().withMinimumValue();
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetDayOfYear
    public void testPropertyGetDayOfYear() {
        
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().dayOfYear(), test.dayOfYear().getField());
        assertEquals("dayOfYear", test.dayOfYear().getName());
        assertEquals("Property[dayOfYear]", test.dayOfYear().toString());
        assertSame(test, test.dayOfYear().getDateMidnight());
        assertEquals(161, test.dayOfYear().get());
        assertEquals("161", test.dayOfYear().getAsText());
        assertEquals("161", test.dayOfYear().getAsText(Locale.FRENCH));
        assertEquals("161", test.dayOfYear().getAsShortText());
        assertEquals("161", test.dayOfYear().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().days(), test.dayOfYear().getDurationField());
        assertEquals(test.getChronology().years(), test.dayOfYear().getRangeDurationField());
        assertEquals(3, test.dayOfYear().getMaximumTextLength(null));
        assertEquals(3, test.dayOfYear().getMaximumShortTextLength(null));
        assertEquals(false, test.dayOfYear().isLeap());
        assertEquals(0, test.dayOfYear().getLeapAmount());
        assertEquals(null, test.dayOfYear().getLeapDurationField());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetWeekOfWeekyear
    public void testPropertyGetWeekOfWeekyear() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().weekOfWeekyear(), test.weekOfWeekyear().getField());
        assertEquals("weekOfWeekyear", test.weekOfWeekyear().getName());
        assertEquals("Property[weekOfWeekyear]", test.weekOfWeekyear().toString());
        assertSame(test, test.weekOfWeekyear().getDateMidnight());
        assertEquals(24, test.weekOfWeekyear().get());
        assertEquals("24", test.weekOfWeekyear().getAsText());
        assertEquals("24", test.weekOfWeekyear().getAsText(Locale.FRENCH));
        assertEquals("24", test.weekOfWeekyear().getAsShortText());
        assertEquals("24", test.weekOfWeekyear().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().weeks(), test.weekOfWeekyear().getDurationField());
        assertEquals(test.getChronology().weekyears(), test.weekOfWeekyear().getRangeDurationField());
        assertEquals(2, test.weekOfWeekyear().getMaximumTextLength(null));
        assertEquals(2, test.weekOfWeekyear().getMaximumShortTextLength(null));
        assertEquals(false, test.weekOfWeekyear().isLeap());
        assertEquals(0, test.weekOfWeekyear().getLeapAmount());
        assertEquals(null, test.weekOfWeekyear().getLeapDurationField());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyGetDayOfWeek
    public void testPropertyGetDayOfWeek() {
        DateMidnight test = new DateMidnight(2004, 6, 9);
        assertSame(test.getChronology().dayOfWeek(), test.dayOfWeek().getField());
        assertEquals("dayOfWeek", test.dayOfWeek().getName());
        assertEquals("Property[dayOfWeek]", test.dayOfWeek().toString());
        assertSame(test, test.dayOfWeek().getDateMidnight());
        assertEquals(3, test.dayOfWeek().get());
        assertEquals("3", test.dayOfWeek().getAsString());
        assertEquals("Wednesday", test.dayOfWeek().getAsText());
        assertEquals("mercredi", test.dayOfWeek().getAsText(Locale.FRENCH));
        assertEquals("Wed", test.dayOfWeek().getAsShortText());
        assertEquals("mer.", test.dayOfWeek().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().days(), test.dayOfWeek().getDurationField());
        assertEquals(test.getChronology().weeks(), test.dayOfWeek().getRangeDurationField());
        assertEquals(9, test.dayOfWeek().getMaximumTextLength(null));
        assertEquals(8, test.dayOfWeek().getMaximumTextLength(Locale.FRENCH));
        assertEquals(3, test.dayOfWeek().getMaximumShortTextLength(null));
        assertEquals(4, test.dayOfWeek().getMaximumShortTextLength(Locale.FRENCH));
        assertEquals(1, test.dayOfWeek().getMinimumValue());
        assertEquals(1, test.dayOfWeek().getMinimumValueOverall());
        assertEquals(7, test.dayOfWeek().getMaximumValue());
        assertEquals(7, test.dayOfWeek().getMaximumValueOverall());
        assertEquals(false, test.dayOfWeek().isLeap());
        assertEquals(0, test.dayOfWeek().getLeapAmount());
        assertEquals(null, test.dayOfWeek().getLeapDurationField());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyToIntervalYearOfEra
    public void testPropertyToIntervalYearOfEra() {
      DateMidnight test = new DateMidnight(2004, 6, 9);
      Interval testInterval = test.yearOfEra().toInterval();
      assertEquals(new DateMidnight(2004, 1, 1), testInterval.getStart());
      assertEquals(new DateMidnight(2005, 1, 1), testInterval.getEnd());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyToIntervalYearOfCentury
    public void testPropertyToIntervalYearOfCentury() {
      DateMidnight test = new DateMidnight(2004, 6, 9);
      Interval testInterval = test.yearOfCentury().toInterval();
      assertEquals(new DateMidnight(2004, 1, 1), testInterval.getStart());
      assertEquals(new DateMidnight(2005, 1, 1), testInterval.getEnd());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyToIntervalYear
    public void testPropertyToIntervalYear() {
      DateMidnight test = new DateMidnight(2004, 6, 9);
      Interval testInterval = test.year().toInterval();
      assertEquals(new DateMidnight(2004, 1, 1), testInterval.getStart());
      assertEquals(new DateMidnight(2005, 1, 1), testInterval.getEnd());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyToIntervalMonthOfYear
    public void testPropertyToIntervalMonthOfYear() {
      DateMidnight test = new DateMidnight(2004, 6, 9);
      Interval testInterval = test.monthOfYear().toInterval();
      assertEquals(new DateMidnight(2004, 6, 1), testInterval.getStart());
      assertEquals(new DateMidnight(2004, 7, 1), testInterval.getEnd());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyToIntervalDayOfMonth
    public void testPropertyToIntervalDayOfMonth() {
      DateMidnight test = new DateMidnight(2004, 6, 9);
      Interval testInterval = test.dayOfMonth().toInterval();
      assertEquals(new DateMidnight(2004, 6, 9), testInterval.getStart());
      assertEquals(new DateMidnight(2004, 6, 10), testInterval.getEnd());

      DateMidnight febTest = new DateMidnight(2004, 2, 29);
      Interval febTestInterval = febTest.dayOfMonth().toInterval();
      assertEquals(new DateMidnight(2004, 2, 29), febTestInterval.getStart());
      assertEquals(new DateMidnight(2004, 3, 1), febTestInterval.getEnd());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyEqualsHashCodeLenient
    public void testPropertyEqualsHashCodeLenient() {
        DateMidnight test1 = new DateMidnight(1970, 6, 9, LenientChronology.getInstance(COPTIC_PARIS));
        DateMidnight test2 = new DateMidnight(1970, 6, 9, LenientChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(true, test2.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
    }

// org.joda.time.TestDateMidnight_Properties::testPropertyEqualsHashCodeStrict
    public void testPropertyEqualsHashCodeStrict() {
        DateMidnight test1 = new DateMidnight(1970, 6, 9, StrictChronology.getInstance(COPTIC_PARIS));
        DateMidnight test2 = new DateMidnight(1970, 6, 9, StrictChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(true, test2.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
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

// org.joda.time.TestDateTimeFieldType::test_era
    public void test_era() throws Exception {
        assertEquals(DateTimeFieldType.era(), DateTimeFieldType.era());
        assertEquals("era", DateTimeFieldType.era().getName());
        assertEquals(DurationFieldType.eras(), DateTimeFieldType.era().getDurationType());
        assertEquals(null, DateTimeFieldType.era().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().era(), DateTimeFieldType.era().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().era().isSupported(), DateTimeFieldType.era().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.era());
    }

// org.joda.time.TestDateTimeFieldType::test_centuryOfEra
    public void test_centuryOfEra() throws Exception {
        assertEquals(DateTimeFieldType.centuryOfEra(), DateTimeFieldType.centuryOfEra());
        assertEquals("centuryOfEra", DateTimeFieldType.centuryOfEra().getName());
        assertEquals(DurationFieldType.centuries(), DateTimeFieldType.centuryOfEra().getDurationType());
        assertEquals(DurationFieldType.eras(), DateTimeFieldType.centuryOfEra().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().centuryOfEra(), DateTimeFieldType.centuryOfEra().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().centuryOfEra().isSupported(), DateTimeFieldType.centuryOfEra().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.centuryOfEra());
    }

// org.joda.time.TestDateTimeFieldType::test_yearOfCentury
    public void test_yearOfCentury() throws Exception {
        assertEquals(DateTimeFieldType.yearOfCentury(), DateTimeFieldType.yearOfCentury());
        assertEquals("yearOfCentury", DateTimeFieldType.yearOfCentury().getName());
        assertEquals(DurationFieldType.years(), DateTimeFieldType.yearOfCentury().getDurationType());
        assertEquals(DurationFieldType.centuries(), DateTimeFieldType.yearOfCentury().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().yearOfCentury(), DateTimeFieldType.yearOfCentury().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().yearOfCentury().isSupported(), DateTimeFieldType.yearOfCentury().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.yearOfCentury());
    }

// org.joda.time.TestDateTimeFieldType::test_yearOfEra
    public void test_yearOfEra() throws Exception {
        assertEquals(DateTimeFieldType.yearOfEra(), DateTimeFieldType.yearOfEra());
        assertEquals("yearOfEra", DateTimeFieldType.yearOfEra().getName());
        assertEquals(DurationFieldType.years(), DateTimeFieldType.yearOfEra().getDurationType());
        assertEquals(DurationFieldType.eras(), DateTimeFieldType.yearOfEra().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().yearOfEra(), DateTimeFieldType.yearOfEra().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().yearOfEra().isSupported(), DateTimeFieldType.yearOfEra().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.yearOfEra());
    }

// org.joda.time.TestDateTimeFieldType::test_year
    public void test_year() throws Exception {
        assertEquals(DateTimeFieldType.year(), DateTimeFieldType.year());
        assertEquals("year", DateTimeFieldType.year().getName());
        assertEquals(DurationFieldType.years(), DateTimeFieldType.year().getDurationType());
        assertEquals(null, DateTimeFieldType.year().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().year(), DateTimeFieldType.year().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().year().isSupported(), DateTimeFieldType.year().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.year());
    }

// org.joda.time.TestDateTimeFieldType::test_monthOfYear
    public void test_monthOfYear() throws Exception {
        assertEquals(DateTimeFieldType.monthOfYear(), DateTimeFieldType.monthOfYear());
        assertEquals("monthOfYear", DateTimeFieldType.monthOfYear().getName());
        assertEquals(DurationFieldType.months(), DateTimeFieldType.monthOfYear().getDurationType());
        assertEquals(DurationFieldType.years(), DateTimeFieldType.monthOfYear().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().monthOfYear(), DateTimeFieldType.monthOfYear().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().monthOfYear().isSupported(), DateTimeFieldType.monthOfYear().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.monthOfYear());
    }

// org.joda.time.TestDateTimeFieldType::test_weekyearOfCentury
    public void test_weekyearOfCentury() throws Exception {
        assertEquals(DateTimeFieldType.weekyearOfCentury(), DateTimeFieldType.weekyearOfCentury());
        assertEquals("weekyearOfCentury", DateTimeFieldType.weekyearOfCentury().getName());
        assertEquals(DurationFieldType.weekyears(), DateTimeFieldType.weekyearOfCentury().getDurationType());
        assertEquals(DurationFieldType.centuries(), DateTimeFieldType.weekyearOfCentury().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().weekyearOfCentury(), DateTimeFieldType.weekyearOfCentury().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().weekyearOfCentury().isSupported(), DateTimeFieldType.weekyearOfCentury().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.weekyearOfCentury());
    }

// org.joda.time.TestDateTimeFieldType::test_weekyear
    public void test_weekyear() throws Exception {
        assertEquals(DateTimeFieldType.weekyear(), DateTimeFieldType.weekyear());
        assertEquals("weekyear", DateTimeFieldType.weekyear().getName());
        assertEquals(DurationFieldType.weekyears(), DateTimeFieldType.weekyear().getDurationType());
        assertEquals(null, DateTimeFieldType.weekyear().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().weekyear(), DateTimeFieldType.weekyear().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().weekyear().isSupported(), DateTimeFieldType.weekyear().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.weekyear());
    }

// org.joda.time.TestDateTimeFieldType::test_weekOfWeekyear
    public void test_weekOfWeekyear() throws Exception {
        assertEquals(DateTimeFieldType.weekOfWeekyear(), DateTimeFieldType.weekOfWeekyear());
        assertEquals("weekOfWeekyear", DateTimeFieldType.weekOfWeekyear().getName());
        assertEquals(DurationFieldType.weeks(), DateTimeFieldType.weekOfWeekyear().getDurationType());
        assertEquals(DurationFieldType.weekyears(), DateTimeFieldType.weekOfWeekyear().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().weekOfWeekyear(), DateTimeFieldType.weekOfWeekyear().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().weekOfWeekyear().isSupported(), DateTimeFieldType.weekOfWeekyear().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.weekOfWeekyear());
    }

// org.joda.time.TestDateTimeFieldType::test_dayOfYear
    public void test_dayOfYear() throws Exception {
        assertEquals(DateTimeFieldType.dayOfYear(), DateTimeFieldType.dayOfYear());
        assertEquals("dayOfYear", DateTimeFieldType.dayOfYear().getName());
        assertEquals(DurationFieldType.days(), DateTimeFieldType.dayOfYear().getDurationType());
        assertEquals(DurationFieldType.years(), DateTimeFieldType.dayOfYear().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().dayOfYear(), DateTimeFieldType.dayOfYear().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().dayOfYear().isSupported(), DateTimeFieldType.dayOfYear().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.dayOfYear());
    }

// org.joda.time.TestDateTimeFieldType::test_dayOfMonth
    public void test_dayOfMonth() throws Exception {
        assertEquals(DateTimeFieldType.dayOfMonth(), DateTimeFieldType.dayOfMonth());
        assertEquals("dayOfMonth", DateTimeFieldType.dayOfMonth().getName());
        assertEquals(DurationFieldType.days(), DateTimeFieldType.dayOfMonth().getDurationType());
        assertEquals(DurationFieldType.months(), DateTimeFieldType.dayOfMonth().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().dayOfMonth(), DateTimeFieldType.dayOfMonth().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().dayOfMonth().isSupported(), DateTimeFieldType.dayOfMonth().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.dayOfMonth());
    }

// org.joda.time.TestDateTimeFieldType::test_dayOfWeek
    public void test_dayOfWeek() throws Exception {
        assertEquals(DateTimeFieldType.dayOfWeek(), DateTimeFieldType.dayOfWeek());
        assertEquals("dayOfWeek", DateTimeFieldType.dayOfWeek().getName());
        assertEquals(DurationFieldType.days(), DateTimeFieldType.dayOfWeek().getDurationType());
        assertEquals(DurationFieldType.weeks(), DateTimeFieldType.dayOfWeek().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().dayOfWeek(), DateTimeFieldType.dayOfWeek().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().dayOfWeek().isSupported(), DateTimeFieldType.dayOfWeek().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.dayOfWeek());
    }

// org.joda.time.TestDateTimeFieldType::test_halfdayOfDay
    public void test_halfdayOfDay() throws Exception {
        assertEquals(DateTimeFieldType.halfdayOfDay(), DateTimeFieldType.halfdayOfDay());
        assertEquals("halfdayOfDay", DateTimeFieldType.halfdayOfDay().getName());
        assertEquals(DurationFieldType.halfdays(), DateTimeFieldType.halfdayOfDay().getDurationType());
        assertEquals(DurationFieldType.days(), DateTimeFieldType.halfdayOfDay().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().halfdayOfDay(), DateTimeFieldType.halfdayOfDay().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().halfdayOfDay().isSupported(), DateTimeFieldType.halfdayOfDay().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.halfdayOfDay());
    }

// org.joda.time.TestDateTimeFieldType::test_clockhourOfDay
    public void test_clockhourOfDay() throws Exception {
        assertEquals(DateTimeFieldType.clockhourOfDay(), DateTimeFieldType.clockhourOfDay());
        assertEquals("clockhourOfDay", DateTimeFieldType.clockhourOfDay().getName());
        assertEquals(DurationFieldType.hours(), DateTimeFieldType.clockhourOfDay().getDurationType());
        assertEquals(DurationFieldType.days(), DateTimeFieldType.clockhourOfDay().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().clockhourOfDay(), DateTimeFieldType.clockhourOfDay().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().clockhourOfDay().isSupported(), DateTimeFieldType.clockhourOfDay().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.clockhourOfDay());
    }

// org.joda.time.TestDateTimeFieldType::test_clockhourOfHalfday
    public void test_clockhourOfHalfday() throws Exception {
        assertEquals(DateTimeFieldType.clockhourOfHalfday(), DateTimeFieldType.clockhourOfHalfday());
        assertEquals("clockhourOfHalfday", DateTimeFieldType.clockhourOfHalfday().getName());
        assertEquals(DurationFieldType.hours(), DateTimeFieldType.clockhourOfHalfday().getDurationType());
        assertEquals(DurationFieldType.halfdays(), DateTimeFieldType.clockhourOfHalfday().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().clockhourOfHalfday(), DateTimeFieldType.clockhourOfHalfday().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().clockhourOfHalfday().isSupported(), DateTimeFieldType.clockhourOfHalfday().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.clockhourOfHalfday());
    }

// org.joda.time.TestDateTimeFieldType::test_hourOfHalfday
    public void test_hourOfHalfday() throws Exception {
        assertEquals(DateTimeFieldType.hourOfHalfday(), DateTimeFieldType.hourOfHalfday());
        assertEquals("hourOfHalfday", DateTimeFieldType.hourOfHalfday().getName());
        assertEquals(DurationFieldType.hours(), DateTimeFieldType.hourOfHalfday().getDurationType());
        assertEquals(DurationFieldType.halfdays(), DateTimeFieldType.hourOfHalfday().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().hourOfHalfday(), DateTimeFieldType.hourOfHalfday().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().hourOfHalfday().isSupported(), DateTimeFieldType.hourOfHalfday().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.hourOfHalfday());
    }

// org.joda.time.TestDateTimeFieldType::test_hourOfDay
    public void test_hourOfDay() throws Exception {
        assertEquals(DateTimeFieldType.hourOfDay(), DateTimeFieldType.hourOfDay());
        assertEquals("hourOfDay", DateTimeFieldType.hourOfDay().getName());
        assertEquals(DurationFieldType.hours(), DateTimeFieldType.hourOfDay().getDurationType());
        assertEquals(DurationFieldType.days(), DateTimeFieldType.hourOfDay().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().hourOfDay(), DateTimeFieldType.hourOfDay().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().hourOfDay().isSupported(), DateTimeFieldType.hourOfDay().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.hourOfDay());
    }

// org.joda.time.TestDateTimeFieldType::test_minuteOfDay
    public void test_minuteOfDay() throws Exception {
        assertEquals(DateTimeFieldType.minuteOfDay(), DateTimeFieldType.minuteOfDay());
        assertEquals("minuteOfDay", DateTimeFieldType.minuteOfDay().getName());
        assertEquals(DurationFieldType.minutes(), DateTimeFieldType.minuteOfDay().getDurationType());
        assertEquals(DurationFieldType.days(), DateTimeFieldType.minuteOfDay().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().minuteOfDay(), DateTimeFieldType.minuteOfDay().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().minuteOfDay().isSupported(), DateTimeFieldType.minuteOfDay().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.minuteOfDay());
    }

// org.joda.time.TestDateTimeFieldType::test_minuteOfHour
    public void test_minuteOfHour() throws Exception {
        assertEquals(DateTimeFieldType.minuteOfHour(), DateTimeFieldType.minuteOfHour());
        assertEquals("minuteOfHour", DateTimeFieldType.minuteOfHour().getName());
        assertEquals(DurationFieldType.minutes(), DateTimeFieldType.minuteOfHour().getDurationType());
        assertEquals(DurationFieldType.hours(), DateTimeFieldType.minuteOfHour().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().minuteOfHour(), DateTimeFieldType.minuteOfHour().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().minuteOfHour().isSupported(), DateTimeFieldType.minuteOfHour().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.minuteOfHour());
    }

// org.joda.time.TestDateTimeFieldType::test_secondOfDay
    public void test_secondOfDay() throws Exception {
        assertEquals(DateTimeFieldType.secondOfDay(), DateTimeFieldType.secondOfDay());
        assertEquals("secondOfDay", DateTimeFieldType.secondOfDay().getName());
        assertEquals(DurationFieldType.seconds(), DateTimeFieldType.secondOfDay().getDurationType());
        assertEquals(DurationFieldType.days(), DateTimeFieldType.secondOfDay().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().secondOfDay(), DateTimeFieldType.secondOfDay().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().secondOfDay().isSupported(), DateTimeFieldType.secondOfDay().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.secondOfDay());
    }

// org.joda.time.TestDateTimeFieldType::test_secondOfMinute
    public void test_secondOfMinute() throws Exception {
        assertEquals(DateTimeFieldType.secondOfMinute(), DateTimeFieldType.secondOfMinute());
        assertEquals("secondOfMinute", DateTimeFieldType.secondOfMinute().getName());
        assertEquals(DurationFieldType.seconds(), DateTimeFieldType.secondOfMinute().getDurationType());
        assertEquals(DurationFieldType.minutes(), DateTimeFieldType.secondOfMinute().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().secondOfMinute(), DateTimeFieldType.secondOfMinute().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().secondOfMinute().isSupported(), DateTimeFieldType.secondOfMinute().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.secondOfMinute());
    }

// org.joda.time.TestDateTimeFieldType::test_millisOfDay
    public void test_millisOfDay() throws Exception {
        assertEquals(DateTimeFieldType.millisOfDay(), DateTimeFieldType.millisOfDay());
        assertEquals("millisOfDay", DateTimeFieldType.millisOfDay().getName());
        assertEquals(DurationFieldType.millis(), DateTimeFieldType.millisOfDay().getDurationType());
        assertEquals(DurationFieldType.days(), DateTimeFieldType.millisOfDay().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().millisOfDay(), DateTimeFieldType.millisOfDay().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().millisOfDay().isSupported(), DateTimeFieldType.millisOfDay().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.millisOfDay());
    }

// org.joda.time.TestDateTimeFieldType::test_millisOfSecond
    public void test_millisOfSecond() throws Exception {
        assertEquals(DateTimeFieldType.millisOfSecond(), DateTimeFieldType.millisOfSecond());
        assertEquals("millisOfSecond", DateTimeFieldType.millisOfSecond().getName());
        assertEquals(DurationFieldType.millis(), DateTimeFieldType.millisOfSecond().getDurationType());
        assertEquals(DurationFieldType.seconds(), DateTimeFieldType.millisOfSecond().getRangeDurationType());
        assertEquals(CopticChronology.getInstanceUTC().millisOfSecond(), DateTimeFieldType.millisOfSecond().getField(CopticChronology.getInstanceUTC()));
        assertEquals(CopticChronology.getInstanceUTC().millisOfSecond().isSupported(), DateTimeFieldType.millisOfSecond().isSupported(CopticChronology.getInstanceUTC()));
        assertSerialization(DateTimeFieldType.millisOfSecond());
    }

// org.joda.time.TestDateTimeFieldType::test_other
    public void test_other() throws Exception {
        assertEquals(1, DateTimeFieldType.class.getDeclaredClasses().length);
        Class cls = DateTimeFieldType.class.getDeclaredClasses()[0];
        assertEquals(1, cls.getDeclaredConstructors().length);
        Constructor con = cls.getDeclaredConstructors()[0];
        Object[] params = new Object[] {
            "other", new Byte((byte) 128), DurationFieldType.hours(), DurationFieldType.months()};
        con.setAccessible(true);  
        DateTimeFieldType type = (DateTimeFieldType) con.newInstance(params);
        
        assertEquals("other", type.getName());
        assertSame(DurationFieldType.hours(), type.getDurationType());
        assertSame(DurationFieldType.months(), type.getRangeDurationType());
        try {
            type.getField(CopticChronology.getInstanceUTC());
            fail();
        } catch (InternalError ex) {}
        DateTimeFieldType result = doSerialization(type);
        assertEquals(type.getName(), result.getName());
        assertNotSame(type, result);
    }

// org.joda.time.TestDateTimeUtils::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestDateTimeUtils::testClass
    public void testClass() {
        Class<?> cls = DateTimeUtils.class;
        assertEquals(true, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isFinal(cls.getModifiers()));
        
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(cls.getDeclaredConstructors()[0].getModifiers()));
        
        new DateTimeUtils() {};
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
            private static final long serialVersionUID = 1L;

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

// org.joda.time.TestDateTimeUtils::test_julianDay
    public void test_julianDay() {
        DateTime base = new DateTime(1970, 1, 1, 0, 0, DateTimeZone.UTC);
        
        assertEquals(2440587.5d, DateTimeUtils.toJulianDay(base.getMillis()), 0.0001d);
        assertEquals(2440588, DateTimeUtils.toJulianDayNumber(base.getMillis()));
        assertEquals(base.getMillis(), DateTimeUtils.fromJulianDay(2440587.5d));
        
        base = base.plusHours(6);
        assertEquals(2440587.75d, DateTimeUtils.toJulianDay(base.getMillis()), 0.0001d);
        assertEquals(2440588, DateTimeUtils.toJulianDayNumber(base.getMillis()));
        assertEquals(base.getMillis(), DateTimeUtils.fromJulianDay(2440587.75d));
        
        base = base.plusHours(6);
        assertEquals(2440588d, DateTimeUtils.toJulianDay(base.getMillis()), 0.0001d);
        assertEquals(2440588, DateTimeUtils.toJulianDayNumber(base.getMillis()));
        assertEquals(base.getMillis(), DateTimeUtils.fromJulianDay(2440588d));
        
        base = base.plusHours(6);
        assertEquals(2440588.25d, DateTimeUtils.toJulianDay(base.getMillis()), 0.0001d);
        assertEquals(2440588, DateTimeUtils.toJulianDayNumber(base.getMillis()));
        assertEquals(base.getMillis(), DateTimeUtils.fromJulianDay(2440588.25d));
        
        base = base.plusHours(6);
        assertEquals(2440588.5d, DateTimeUtils.toJulianDay(base.getMillis()), 0.0001d);
        assertEquals(2440589, DateTimeUtils.toJulianDayNumber(base.getMillis()));
        assertEquals(base.getMillis(), DateTimeUtils.fromJulianDay(2440588.5d));
        
        base = new DateTime(2012, 8, 31, 23, 50, DateTimeZone.UTC);
        assertEquals(2456171.4930555555, DateTimeUtils.toJulianDay(base.getMillis()), 0.0001d);
        assertEquals(2456171, DateTimeUtils.toJulianDayNumber(base.getMillis()));
        
        base = new DateTime(-4713, 1, 1, 12, 0, JulianChronology.getInstanceUTC());
        assertEquals(0d, DateTimeUtils.toJulianDay(base.getMillis()), 0.0001d);
        assertEquals(0, DateTimeUtils.toJulianDayNumber(base.getMillis()));
        assertEquals(base.getMillis(), DateTimeUtils.fromJulianDay(0d));
        
        base = new DateTime(-4713, 1, 1, 0, 0, JulianChronology.getInstanceUTC());
        assertEquals(-0.5d, DateTimeUtils.toJulianDay(base.getMillis()), 0.0001d);
        assertEquals(0, DateTimeUtils.toJulianDayNumber(base.getMillis()));
        assertEquals(base.getMillis(), DateTimeUtils.fromJulianDay(-0.5d));
    }

// org.joda.time.TestDateTimeZone::testDefault
    public void testDefault() {
        assertNotNull(DateTimeZone.getDefault());
        
        DateTimeZone.setDefault(PARIS);
        assertSame(PARIS, DateTimeZone.getDefault());
        
        try {
            DateTimeZone.setDefault(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZone::testDefaultSecurity
    public void testDefaultSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            DateTimeZone.setDefault(PARIS);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
    }

// org.joda.time.TestDateTimeZone::testForID_String
    public void testForID_String() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID((String) null));
        
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals("Europe/London", zone.getID());
        
        zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forID("+00");
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forID("+01:23");
        assertEquals("+01:23", zone.getID());
        assertEquals(DateTimeConstants.MILLIS_PER_HOUR + (23L * DateTimeConstants.MILLIS_PER_MINUTE),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forID("-02:00");
        assertEquals("-02:00", zone.getID());
        assertEquals((-2L * DateTimeConstants.MILLIS_PER_HOUR),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forID("-07:05:34.0");
        assertEquals("-07:05:34", zone.getID());
        assertEquals((-7L * DateTimeConstants.MILLIS_PER_HOUR) +
                    (-5L * DateTimeConstants.MILLIS_PER_MINUTE) +
                    (-34L * DateTimeConstants.MILLIS_PER_SECOND),
                    zone.getOffset(TEST_TIME_SUMMER));
        
        try {
            DateTimeZone.forID("SST");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("europe/london");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("Europe/UK");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("+");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("+0");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZone::testForID_String_old
    public void testForID_String_old() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("GMT", "UTC");
        map.put("WET", "WET");
        map.put("CET", "CET");
        map.put("MET", "CET");
        map.put("ECT", "CET");
        map.put("EET", "EET");
        map.put("MIT", "Pacific/Apia");
        map.put("HST", "Pacific/Honolulu");
        map.put("AST", "America/Anchorage");
        map.put("PST", "America/Los_Angeles");
        map.put("MST", "America/Denver");
        map.put("PNT", "America/Phoenix");
        map.put("CST", "America/Chicago");
        map.put("EST", "America/New_York");
        map.put("IET", "America/Indiana/Indianapolis");
        map.put("PRT", "America/Puerto_Rico");
        map.put("CNT", "America/St_Johns");
        map.put("AGT", "America/Argentina/Buenos_Aires");
        map.put("BET", "America/Sao_Paulo");
        map.put("ART", "Africa/Cairo");
        map.put("CAT", "Africa/Harare");
        map.put("EAT", "Africa/Addis_Ababa");
        map.put("NET", "Asia/Yerevan");
        map.put("PLT", "Asia/Karachi");
        map.put("IST", "Asia/Kolkata");
        map.put("BST", "Asia/Dhaka");
        map.put("VST", "Asia/Ho_Chi_Minh");
        map.put("CTT", "Asia/Shanghai");
        map.put("JST", "Asia/Tokyo");
        map.put("ACT", "Australia/Darwin");
        map.put("AET", "Australia/Sydney");
        map.put("SST", "Pacific/Guadalcanal");
        map.put("NST", "Pacific/Auckland");
        for (String key : map.keySet()) {
            String value = map.get(key);
            TimeZone juZone = TimeZone.getTimeZone(key);
            DateTimeZone zone = DateTimeZone.forTimeZone(juZone);
            assertEquals(value, zone.getID());

        }
    }

// org.joda.time.TestDateTimeZone::testForOffsetHours_int
    public void testForOffsetHours_int() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertEquals(DateTimeZone.forID("+03:00"), DateTimeZone.forOffsetHours(3));
        assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetHours(-2));
        try {
            DateTimeZone.forOffsetHours(999999);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZone::testForOffsetHoursMinutes_int_int
    public void testForOffsetHoursMinutes_int_int() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
        assertEquals(DateTimeZone.forID("+23:59"), DateTimeZone.forOffsetHoursMinutes(23, 59));
        
        assertEquals(DateTimeZone.forID("+02:15"), DateTimeZone.forOffsetHoursMinutes(2, 15));
        assertEquals(DateTimeZone.forID("+02:00"), DateTimeZone.forOffsetHoursMinutes(2, 0));
        try {
            DateTimeZone.forOffsetHoursMinutes(2, -15);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(DateTimeZone.forID("+00:15"), DateTimeZone.forOffsetHoursMinutes(0, 15));
        assertEquals(DateTimeZone.forID("+00:00"), DateTimeZone.forOffsetHoursMinutes(0, 0));
        assertEquals(DateTimeZone.forID("-00:15"), DateTimeZone.forOffsetHoursMinutes(0, -15));
        
        assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetHoursMinutes(-2, 0));
        assertEquals(DateTimeZone.forID("-02:15"), DateTimeZone.forOffsetHoursMinutes(-2, -15));
        assertEquals(DateTimeZone.forID("-02:15"), DateTimeZone.forOffsetHoursMinutes(-2, 15));
        
        assertEquals(DateTimeZone.forID("-23:59"), DateTimeZone.forOffsetHoursMinutes(-23, 59));
        try {
            DateTimeZone.forOffsetHoursMinutes(2, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(-2, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(24, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(-24, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZone::testForOffsetMillis_int
    public void testForOffsetMillis_int() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
        assertEquals(DateTimeZone.forID("+23:59:59.999"), DateTimeZone.forOffsetMillis((24 * 60 * 60 * 1000) - 1));
        assertEquals(DateTimeZone.forID("+03:00"), DateTimeZone.forOffsetMillis(3 * 60 * 60 * 1000));
        assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetMillis(-2 * 60 * 60 * 1000));
        assertEquals(DateTimeZone.forID("-23:59:59.999"), DateTimeZone.forOffsetMillis((-24 * 60 * 60 * 1000) + 1));
        assertEquals(DateTimeZone.forID("+04:45:17.045"),
                DateTimeZone.forOffsetMillis(
                        4 * 60 * 60 * 1000 + 45 * 60 * 1000 + 17 * 1000 + 45));
    }

// org.joda.time.TestDateTimeZone::testForTimeZone_TimeZone
    public void testForTimeZone_TimeZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone((TimeZone) null));
        
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London"));
        assertEquals("Europe/London", zone.getID());
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("+00:00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+01:23"));
        assertEquals("+01:23", zone.getID());
        assertEquals(DateTimeConstants.MILLIS_PER_HOUR + (23L * DateTimeConstants.MILLIS_PER_MINUTE),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+1:23"));
        assertEquals("+01:23", zone.getID());
        assertEquals(DateTimeConstants.MILLIS_PER_HOUR + (23L * DateTimeConstants.MILLIS_PER_MINUTE),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-02:00"));
        assertEquals("-02:00", zone.getID());
        assertEquals((-2L * DateTimeConstants.MILLIS_PER_HOUR), zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+2"));
        assertEquals("+02:00", zone.getID());
        assertEquals((2L * DateTimeConstants.MILLIS_PER_HOUR), zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", zone.getID());
    }

// org.joda.time.TestDateTimeZone::testTimeZoneConversion
    public void testTimeZoneConversion() {
        TimeZone jdkTimeZone = TimeZone.getTimeZone("GMT-10");
        assertEquals("GMT-10:00", jdkTimeZone.getID());
        
        DateTimeZone jodaTimeZone = DateTimeZone.forTimeZone(jdkTimeZone);
        assertEquals("-10:00", jodaTimeZone.getID());
        assertEquals(jdkTimeZone.getRawOffset(), jodaTimeZone.getOffset(0L));
        
        TimeZone convertedTimeZone = jodaTimeZone.toTimeZone();
        assertEquals("GMT-10:00", jdkTimeZone.getID());
        
        assertEquals(jdkTimeZone.getID(), convertedTimeZone.getID());
        assertEquals(jdkTimeZone.getRawOffset(), convertedTimeZone.getRawOffset());
    }

// org.joda.time.TestDateTimeZone::testGetAvailableIDs
    public void testGetAvailableIDs() {
        assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
    }

// org.joda.time.TestDateTimeZone::testProvider
    public void testProvider() {
        try {
            assertNotNull(DateTimeZone.getProvider());
        
            Provider provider = DateTimeZone.getProvider();
            DateTimeZone.setProvider(null);
            assertEquals(provider.getClass(), DateTimeZone.getProvider().getClass());
        
            try {
                DateTimeZone.setProvider(new MockNullIDSProvider());
                fail();
            } catch (IllegalArgumentException ex) {}
            try {
                DateTimeZone.setProvider(new MockEmptyIDSProvider());
                fail();
            } catch (IllegalArgumentException ex) {}
            try {
                DateTimeZone.setProvider(new MockNoUTCProvider());
                fail();
            } catch (IllegalArgumentException ex) {}
            try {
                DateTimeZone.setProvider(new MockBadUTCProvider());
                fail();
            } catch (IllegalArgumentException ex) {}
        
            Provider prov = new MockOKProvider();
            DateTimeZone.setProvider(prov);
            assertSame(prov, DateTimeZone.getProvider());
            assertEquals(2, DateTimeZone.getAvailableIDs().size());
            assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
            assertTrue(DateTimeZone.getAvailableIDs().contains("Europe/London"));
        } finally {
            DateTimeZone.setProvider(null);
            assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
        }
        
        try {
            System.setProperty("org.joda.time.DateTimeZone.Provider", "org.joda.time.tz.UTCProvider");
            DateTimeZone.setProvider(null);
            assertEquals(UTCProvider.class, DateTimeZone.getProvider().getClass());
        } finally {
            System.getProperties().remove("org.joda.time.DateTimeZone.Provider");
            DateTimeZone.setProvider(null);
            assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
        }
    }

// org.joda.time.TestDateTimeZone::testProvider_badClassName
    public void testProvider_badClassName() {
        try {
            System.setProperty("org.joda.time.DateTimeZone.Provider", "xxx");
            DateTimeZone.setProvider(null);
            
        } catch (RuntimeException ex) {
            
            assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
        } finally {
            System.getProperties().remove("org.joda.time.DateTimeZone.Provider");
            DateTimeZone.setProvider(null);
        }
    }

// org.joda.time.TestDateTimeZone::testProviderSecurity
    public void testProviderSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            DateTimeZone.setProvider(new MockOKProvider());
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
    }

// org.joda.time.TestDateTimeZone::testNameProvider
    public void testNameProvider() {
        try {
            assertNotNull(DateTimeZone.getNameProvider());
        
            NameProvider provider = DateTimeZone.getNameProvider();
            DateTimeZone.setNameProvider(null);
            assertEquals(provider.getClass(), DateTimeZone.getNameProvider().getClass());
        
            provider = new MockOKButNullNameProvider();
            DateTimeZone.setNameProvider(provider);
            assertSame(provider, DateTimeZone.getNameProvider());
            
            assertEquals("+00:00", DateTimeZone.UTC.getShortName(TEST_TIME_SUMMER));
            assertEquals("+00:00", DateTimeZone.UTC.getName(TEST_TIME_SUMMER));
        } finally {
            DateTimeZone.setNameProvider(null);
        }
        
        try {
            System.setProperty("org.joda.time.DateTimeZone.NameProvider", "org.joda.time.tz.DefaultNameProvider");
            DateTimeZone.setNameProvider(null);
            assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
        } finally {
            System.getProperties().remove("org.joda.time.DateTimeZone.NameProvider");
            DateTimeZone.setNameProvider(null);
            assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
        }
    }

// org.joda.time.TestDateTimeZone::testNameProvider_badClassName
    public void testNameProvider_badClassName() {
        try {
            System.setProperty("org.joda.time.DateTimeZone.NameProvider", "xxx");
            DateTimeZone.setProvider(null);
            
        } catch (RuntimeException ex) {
            
            assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
        } finally {
            System.getProperties().remove("org.joda.time.DateTimeZone.NameProvider");
            DateTimeZone.setProvider(null);
        }
    }

// org.joda.time.TestDateTimeZone::testNameProviderSecurity
    public void testNameProviderSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            DateTimeZone.setNameProvider(new MockOKButNullNameProvider());
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
    }

// org.joda.time.TestDateTimeZone::testConstructor
    public void testConstructor() {
        assertEquals(1, DateTimeZone.class.getDeclaredConstructors().length);
        assertTrue(Modifier.isProtected(DateTimeZone.class.getDeclaredConstructors()[0].getModifiers()));
        try {
            new DateTimeZone(null) {
                public String getNameKey(long instant) {
                    return null;
                }
                public int getOffset(long instant) {
                    return 0;
                }
                public int getStandardOffset(long instant) {
                    return 0;
                }
                public boolean isFixed() {
                    return false;
                }
                public long nextTransition(long instant) {
                    return 0;
                }
                public long previousTransition(long instant) {
                    return 0;
                }
                public boolean equals(Object object) {
                    return false;
                }
            };
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZone::testGetID
    public void testGetID() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals("Europe/Paris", zone.getID());
    }

// org.joda.time.TestDateTimeZone::testGetNameKey
    public void testGetNameKey() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals("BST", zone.getNameKey(TEST_TIME_SUMMER));
        assertEquals("GMT", zone.getNameKey(TEST_TIME_WINTER));
    }

// org.joda.time.TestDateTimeZone::testGetShortName
    public void testGetShortName() {}

// org.joda.time.TestDateTimeZone::testGetShortName_berlin
    public void testGetShortName_berlin() {}

// org.joda.time.TestDateTimeZone::testGetShortNameProviderName
    public void testGetShortNameProviderName() {
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(null, "Europe/London", "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(Locale.ENGLISH, null, "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(Locale.ENGLISH, "Europe/London", null));
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(null, null, null));
    }

// org.joda.time.TestDateTimeZone::testGetShortNameNullKey
    public void testGetShortNameNullKey() {
        DateTimeZone zone = new MockDateTimeZone("Europe/London");
        assertEquals("Europe/London", zone.getShortName(TEST_TIME_SUMMER, Locale.ENGLISH));
    }

// org.joda.time.TestDateTimeZone::testGetName
    public void testGetName() {}

// org.joda.time.TestDateTimeZone::testGetName_berlin
    public void testGetName_berlin() {}

// org.joda.time.TestDateTimeZone::testGetNameProviderName
    public void testGetNameProviderName() {
        assertEquals(null, DateTimeZone.getNameProvider().getName(null, "Europe/London", "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getName(Locale.ENGLISH, null, "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getName(Locale.ENGLISH, "Europe/London", null));
        assertEquals(null, DateTimeZone.getNameProvider().getName(null, null, null));
    }

// org.joda.time.TestDateTimeZone::testGetNameNullKey
    public void testGetNameNullKey() {
        DateTimeZone zone = new MockDateTimeZone("Europe/London");
        assertEquals("Europe/London", zone.getName(TEST_TIME_SUMMER, Locale.ENGLISH));
    }

// org.joda.time.TestDateTimeZone::testGetOffset_long
    public void testGetOffset_long() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_WINTER));
        
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_WINTER));
        
        assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_WINTER));
        
        assertEquals(false, zone.isStandardOffset(TEST_TIME_SUMMER));
        assertEquals(true, zone.isStandardOffset(TEST_TIME_WINTER));
    }

// org.joda.time.TestDateTimeZone::testGetOffset_RI
    public void testGetOffset_RI() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_SUMMER)));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_WINTER)));
        
        assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset(null));
    }

// org.joda.time.TestDateTimeZone::testGetOffsetFixed
    public void testGetOffsetFixed() {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_WINTER));
        
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_WINTER));
        
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_WINTER));
        
        assertEquals(true, zone.isStandardOffset(TEST_TIME_SUMMER));
        assertEquals(true, zone.isStandardOffset(TEST_TIME_WINTER));
    }

// org.joda.time.TestDateTimeZone::testGetOffsetFixed_RI
    public void testGetOffsetFixed_RI() {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_SUMMER)));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_WINTER)));
        
        assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset(null));
    }

// org.joda.time.TestDateTimeZone::testGetMillisKeepLocal
    public void testGetMillisKeepLocal() {
        long millisLondon = TEST_TIME_SUMMER;
        long millisParis = TEST_TIME_SUMMER - 1L * DateTimeConstants.MILLIS_PER_HOUR;
        
        assertEquals(millisLondon, LONDON.getMillisKeepLocal(LONDON, millisLondon));
        assertEquals(millisParis, LONDON.getMillisKeepLocal(LONDON, millisParis));
        assertEquals(millisLondon, PARIS.getMillisKeepLocal(PARIS, millisLondon));
        assertEquals(millisParis, PARIS.getMillisKeepLocal(PARIS, millisParis));
        
        assertEquals(millisParis, LONDON.getMillisKeepLocal(PARIS, millisLondon));
        assertEquals(millisLondon, PARIS.getMillisKeepLocal(LONDON, millisParis));
        
        DateTimeZone zone = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(LONDON);
            assertEquals(millisLondon, PARIS.getMillisKeepLocal(null, millisParis));
        } finally {
            DateTimeZone.setDefault(zone);
        }
    }

// org.joda.time.TestDateTimeZone::testIsFixed
    public void testIsFixed() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals(false, zone.isFixed());
        assertEquals(true, DateTimeZone.UTC.isFixed());
    }

// org.joda.time.TestDateTimeZone::testTransitionFixed
    public void testTransitionFixed() {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        assertEquals(TEST_TIME_SUMMER, zone.nextTransition(TEST_TIME_SUMMER));
        assertEquals(TEST_TIME_WINTER, zone.nextTransition(TEST_TIME_WINTER));
        assertEquals(TEST_TIME_SUMMER, zone.previousTransition(TEST_TIME_SUMMER));
        assertEquals(TEST_TIME_WINTER, zone.previousTransition(TEST_TIME_WINTER));
    }

// org.joda.time.TestDateTimeZone::testIsLocalDateTimeGap_Berlin
    public void testIsLocalDateTimeGap_Berlin() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Berlin");
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 1, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 1, 59, 59, 99)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 0)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 30)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 59, 59, 99)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 3, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 4, 0)));
        
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 1, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 2, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 3, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 12, 24, 12, 34)));
    }

// org.joda.time.TestDateTimeZone::testIsLocalDateTimeGap_NewYork
    public void testIsLocalDateTimeGap_NewYork() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 1, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 1, 59, 59, 99)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 0)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 30)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 59, 59, 99)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 3, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 4, 0)));
        
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 0, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 1, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 2, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 12, 24, 12, 34)));
    }

// org.joda.time.TestDateTimeZone::testToTimeZone
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        TimeZone tz = zone.toTimeZone();
        assertEquals("Europe/Paris", tz.getID());
    }

// org.joda.time.TestDateTimeZone::testEqualsHashCode
    public void testEqualsHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Paris");
        assertEquals(true, zone1.equals(zone1));
        assertEquals(true, zone1.equals(zone2));
        assertEquals(true, zone2.equals(zone1));
        assertEquals(true, zone2.equals(zone2));
        assertEquals(true, zone1.hashCode() == zone2.hashCode());
        
        DateTimeZone zone3 = DateTimeZone.forID("Europe/London");
        assertEquals(true, zone3.equals(zone3));
        assertEquals(false, zone1.equals(zone3));
        assertEquals(false, zone2.equals(zone3));
        assertEquals(false, zone3.equals(zone1));
        assertEquals(false, zone3.equals(zone2));
        assertEquals(false, zone1.hashCode() == zone3.hashCode());
        assertEquals(true, zone3.hashCode() == zone3.hashCode());
        
        DateTimeZone zone4 = DateTimeZone.forID("+01:00");
        assertEquals(true, zone4.equals(zone4));
        assertEquals(false, zone1.equals(zone4));
        assertEquals(false, zone2.equals(zone4));
        assertEquals(false, zone3.equals(zone4));
        assertEquals(false, zone4.equals(zone1));
        assertEquals(false, zone4.equals(zone2));
        assertEquals(false, zone4.equals(zone3));
        assertEquals(false, zone1.hashCode() == zone4.hashCode());
        assertEquals(true, zone4.hashCode() == zone4.hashCode());
        
        DateTimeZone zone5 = DateTimeZone.forID("+02:00");
        assertEquals(true, zone5.equals(zone5));
        assertEquals(false, zone1.equals(zone5));
        assertEquals(false, zone2.equals(zone5));
        assertEquals(false, zone3.equals(zone5));
        assertEquals(false, zone4.equals(zone5));
        assertEquals(false, zone5.equals(zone1));
        assertEquals(false, zone5.equals(zone2));
        assertEquals(false, zone5.equals(zone3));
        assertEquals(false, zone5.equals(zone4));
        assertEquals(false, zone1.hashCode() == zone5.hashCode());
        assertEquals(true, zone5.hashCode() == zone5.hashCode());
    }

// org.joda.time.TestDateTimeZone::testToString
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals("Europe/Paris", zone.toString());
        assertEquals("UTC", DateTimeZone.UTC.toString());
    }

// org.joda.time.TestDateTimeZone::testSerialization1
    public void testSerialization1() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone result = (DateTimeZone) ois.readObject();
        ois.close();
        
        assertSame(zone, result);
    }

// org.joda.time.TestDateTimeZone::testSerialization2
    public void testSerialization2() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone result = (DateTimeZone) ois.readObject();
        ois.close();
        
        assertSame(zone, result);
    }

// org.joda.time.TestDateTimeZone::testCommentParse
    public void testCommentParse() throws Exception {
        
        

        DateTimeZone zone = DateTimeZone.forID("Europe/Athens");
        DateTime dt = new DateTime(2005, 5, 5, 20, 10, 15, 0, zone);
        assertEquals(1115313015000L, dt.getMillis());
    }

// org.joda.time.TestDateTimeZone::testPatchedNameKeysLondon
    public void testPatchedNameKeysLondon() throws Exception {
        
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        
        DateTime now = new DateTime(2007, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
    }

// org.joda.time.TestDateTimeZone::testPatchedNameKeysSydney
    public void testPatchedNameKeysSydney() throws Exception {
        
        DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
        
        DateTime now = new DateTime(2007, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
    }

// org.joda.time.TestDateTimeZone::testPatchedNameKeysSydneyHistoric
    public void testPatchedNameKeysSydneyHistoric() throws Exception {
        
        DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
        
        DateTime now = new DateTime(1996, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
    }

// org.joda.time.TestDateTimeZone::testPatchedNameKeysGazaHistoric
    public void testPatchedNameKeysGazaHistoric() throws Exception {
        
        DateTimeZone zone = DateTimeZone.forID("Africa/Johannesburg");
        
        DateTime now = new DateTime(1943, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
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
