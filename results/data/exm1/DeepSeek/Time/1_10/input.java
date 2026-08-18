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
// org.joda.time.chrono.TestGJDate::test_plusYears_positiveToZero
    public void test_plusYears_positiveToZero() {
        LocalDate date = new LocalDate(3, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(-1, 6, 30, GJ_CHRONOLOGY);
        assertEquals(expected, date.plusYears(-3));
    }

// org.joda.time.chrono.TestGJDate::test_plusYears_positiveToNegative
    public void test_plusYears_positiveToNegative() {
        LocalDate date = new LocalDate(3, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(-2, 6, 30, GJ_CHRONOLOGY);
        assertEquals(expected, date.plusYears(-4));
    }

// org.joda.time.chrono.TestGJDate::test_plusYears_negativeToNegative
    public void test_plusYears_negativeToNegative() {
        LocalDate date = new LocalDate(-3, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(-1, 6, 30, GJ_CHRONOLOGY);
        assertEquals(expected, date.plusYears(2));
    }

// org.joda.time.chrono.TestGJDate::test_plusYears_negativeToZero
    public void test_plusYears_negativeToZero() {
        LocalDate date = new LocalDate(-3, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(1, 6, 30, GJ_CHRONOLOGY);
        assertEquals(expected, date.plusYears(3));
    }

// org.joda.time.chrono.TestGJDate::test_plusYears_negativeToPositive
    public void test_plusYears_negativeToPositive() {
        LocalDate date = new LocalDate(-3, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(2, 6, 30, GJ_CHRONOLOGY);
        assertEquals(expected, date.plusYears(4));
    }

// org.joda.time.chrono.TestGJDate::test_plusYears_positiveToPositive_crossCutover
    public void test_plusYears_positiveToPositive_crossCutover() {
        LocalDate date = new LocalDate(3, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(2007, 6, 30, GJ_CHRONOLOGY);
        assertEquals(expected, date.plusYears(2004));
    }

// org.joda.time.chrono.TestGJDate::test_plusYears_positiveToZero_crossCutover
    public void test_plusYears_positiveToZero_crossCutover() {
        LocalDate date = new LocalDate(2003, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(-1, 6, 30, GJ_CHRONOLOGY);
        assertEquals(expected, date.plusYears(-2003));
    }

// org.joda.time.chrono.TestGJDate::test_plusYears_positiveToNegative_crossCutover
    public void test_plusYears_positiveToNegative_crossCutover() {
        LocalDate date = new LocalDate(2003, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(-2, 6, 30, GJ_CHRONOLOGY);
        assertEquals(expected, date.plusYears(-2004));
    }

// org.joda.time.chrono.TestGJDate::test_plusYears_negativeToPositive_crossCutover
    public void test_plusYears_negativeToPositive_crossCutover() {
        LocalDate date = new LocalDate(-3, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(2002, 6, 30, GJ_CHRONOLOGY);
        assertEquals(expected, date.plusYears(2004));
    }

// org.joda.time.chrono.TestGJDate::test_plusWeekyears_positiveToZero_crossCutover
    public void test_plusWeekyears_positiveToZero_crossCutover() {
        LocalDate date = new LocalDate(2003, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(-1, 6, 30, GJ_CHRONOLOGY).withWeekOfWeekyear(date.getWeekOfWeekyear()).withDayOfWeek(date.getDayOfWeek());
        assertEquals(expected, date.weekyear().addToCopy(-2003));
    }

// org.joda.time.chrono.TestGJDate::test_plusWeekyears_positiveToNegative_crossCutover
    public void test_plusWeekyears_positiveToNegative_crossCutover() {
        LocalDate date = new LocalDate(2003, 6, 30, GJ_CHRONOLOGY);
        LocalDate expected = new LocalDate(-2, 6, 30, GJ_CHRONOLOGY).withWeekOfWeekyear(date.getWeekOfWeekyear()).withDayOfWeek(date.getDayOfWeek());
        assertEquals(expected, date.weekyear().addToCopy(-2004));
    }

// org.joda.time.chrono.TestGJDate::test_cutoverPreZero
    public void test_cutoverPreZero() {
        DateTime cutover = new LocalDate(-2, 6, 30, ISOChronology.getInstanceUTC()).toDateTimeAtStartOfDay(DateTimeZone.UTC);
        try {
            GJChronology.getInstance(DateTimeZone.UTC, cutover);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
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
        final GregorianChronology greg = GregorianChronology.getInstance();
        assertEquals("eras", greg.eras().getName());
        assertEquals("centuries", greg.centuries().getName());
        assertEquals("years", greg.years().getName());
        assertEquals("weekyears", greg.weekyears().getName());
        assertEquals("months", greg.months().getName());
        assertEquals("weeks", greg.weeks().getName());
        assertEquals("days", greg.days().getName());
        assertEquals("halfdays", greg.halfdays().getName());
        assertEquals("hours", greg.hours().getName());
        assertEquals("minutes", greg.minutes().getName());
        assertEquals("seconds", greg.seconds().getName());
        assertEquals("millis", greg.millis().getName());
        
        assertEquals(false, greg.eras().isSupported());
        assertEquals(true, greg.centuries().isSupported());
        assertEquals(true, greg.years().isSupported());
        assertEquals(true, greg.weekyears().isSupported());
        assertEquals(true, greg.months().isSupported());
        assertEquals(true, greg.weeks().isSupported());
        assertEquals(true, greg.days().isSupported());
        assertEquals(true, greg.halfdays().isSupported());
        assertEquals(true, greg.hours().isSupported());
        assertEquals(true, greg.minutes().isSupported());
        assertEquals(true, greg.seconds().isSupported());
        assertEquals(true, greg.millis().isSupported());
        
        assertEquals(false, greg.centuries().isPrecise());
        assertEquals(false, greg.years().isPrecise());
        assertEquals(false, greg.weekyears().isPrecise());
        assertEquals(false, greg.months().isPrecise());
        assertEquals(false, greg.weeks().isPrecise());
        assertEquals(false, greg.days().isPrecise());
        assertEquals(false, greg.halfdays().isPrecise());
        assertEquals(true, greg.hours().isPrecise());
        assertEquals(true, greg.minutes().isPrecise());
        assertEquals(true, greg.seconds().isPrecise());
        assertEquals(true, greg.millis().isPrecise());
        
        final GregorianChronology gregUTC = GregorianChronology.getInstanceUTC();
        assertEquals(false, gregUTC.centuries().isPrecise());
        assertEquals(false, gregUTC.years().isPrecise());
        assertEquals(false, gregUTC.weekyears().isPrecise());
        assertEquals(false, gregUTC.months().isPrecise());
        assertEquals(true, gregUTC.weeks().isPrecise());
        assertEquals(true, gregUTC.days().isPrecise());
        assertEquals(true, gregUTC.halfdays().isPrecise());
        assertEquals(true, gregUTC.hours().isPrecise());
        assertEquals(true, gregUTC.minutes().isPrecise());
        assertEquals(true, gregUTC.seconds().isPrecise());
        assertEquals(true, gregUTC.millis().isPrecise());
        
        final DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        final GregorianChronology gregGMT = GregorianChronology.getInstance(gmt);
        assertEquals(false, gregGMT.centuries().isPrecise());
        assertEquals(false, gregGMT.years().isPrecise());
        assertEquals(false, gregGMT.weekyears().isPrecise());
        assertEquals(false, gregGMT.months().isPrecise());
        assertEquals(true, gregGMT.weeks().isPrecise());
        assertEquals(true, gregGMT.days().isPrecise());
        assertEquals(true, gregGMT.halfdays().isPrecise());
        assertEquals(true, gregGMT.hours().isPrecise());
        assertEquals(true, gregGMT.minutes().isPrecise());
        assertEquals(true, gregGMT.seconds().isPrecise());
        assertEquals(true, gregGMT.millis().isPrecise());
    }

// org.joda.time.chrono.TestGregorianChronology::testDateFields
    public void testDateFields() {
        final GregorianChronology greg = GregorianChronology.getInstance();
        assertEquals("era", greg.era().getName());
        assertEquals("centuryOfEra", greg.centuryOfEra().getName());
        assertEquals("yearOfCentury", greg.yearOfCentury().getName());
        assertEquals("yearOfEra", greg.yearOfEra().getName());
        assertEquals("year", greg.year().getName());
        assertEquals("monthOfYear", greg.monthOfYear().getName());
        assertEquals("weekyearOfCentury", greg.weekyearOfCentury().getName());
        assertEquals("weekyear", greg.weekyear().getName());
        assertEquals("weekOfWeekyear", greg.weekOfWeekyear().getName());
        assertEquals("dayOfYear", greg.dayOfYear().getName());
        assertEquals("dayOfMonth", greg.dayOfMonth().getName());
        assertEquals("dayOfWeek", greg.dayOfWeek().getName());
        
        assertEquals(true, greg.era().isSupported());
        assertEquals(true, greg.centuryOfEra().isSupported());
        assertEquals(true, greg.yearOfCentury().isSupported());
        assertEquals(true, greg.yearOfEra().isSupported());
        assertEquals(true, greg.year().isSupported());
        assertEquals(true, greg.monthOfYear().isSupported());
        assertEquals(true, greg.weekyearOfCentury().isSupported());
        assertEquals(true, greg.weekyear().isSupported());
        assertEquals(true, greg.weekOfWeekyear().isSupported());
        assertEquals(true, greg.dayOfYear().isSupported());
        assertEquals(true, greg.dayOfMonth().isSupported());
        assertEquals(true, greg.dayOfWeek().isSupported());
        
        assertEquals(greg.eras(), greg.era().getDurationField());
        assertEquals(greg.centuries(), greg.centuryOfEra().getDurationField());
        assertEquals(greg.years(), greg.yearOfCentury().getDurationField());
        assertEquals(greg.years(), greg.yearOfEra().getDurationField());
        assertEquals(greg.years(), greg.year().getDurationField());
        assertEquals(greg.months(), greg.monthOfYear().getDurationField());
        assertEquals(greg.weekyears(), greg.weekyearOfCentury().getDurationField());
        assertEquals(greg.weekyears(), greg.weekyear().getDurationField());
        assertEquals(greg.weeks(), greg.weekOfWeekyear().getDurationField());
        assertEquals(greg.days(), greg.dayOfYear().getDurationField());
        assertEquals(greg.days(), greg.dayOfMonth().getDurationField());
        assertEquals(greg.days(), greg.dayOfWeek().getDurationField());
        
        assertEquals(null, greg.era().getRangeDurationField());
        assertEquals(greg.eras(), greg.centuryOfEra().getRangeDurationField());
        assertEquals(greg.centuries(), greg.yearOfCentury().getRangeDurationField());
        assertEquals(greg.eras(), greg.yearOfEra().getRangeDurationField());
        assertEquals(null, greg.year().getRangeDurationField());
        assertEquals(greg.years(), greg.monthOfYear().getRangeDurationField());
        assertEquals(greg.centuries(), greg.weekyearOfCentury().getRangeDurationField());
        assertEquals(null, greg.weekyear().getRangeDurationField());
        assertEquals(greg.weekyears(), greg.weekOfWeekyear().getRangeDurationField());
        assertEquals(greg.years(), greg.dayOfYear().getRangeDurationField());
        assertEquals(greg.months(), greg.dayOfMonth().getRangeDurationField());
        assertEquals(greg.weeks(), greg.dayOfWeek().getRangeDurationField());
    }

// org.joda.time.chrono.TestGregorianChronology::testTimeFields
    public void testTimeFields() {
        final GregorianChronology greg = GregorianChronology.getInstance();
        assertEquals("halfdayOfDay", greg.halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", greg.clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", greg.hourOfHalfday().getName());
        assertEquals("clockhourOfDay", greg.clockhourOfDay().getName());
        assertEquals("hourOfDay", greg.hourOfDay().getName());
        assertEquals("minuteOfDay", greg.minuteOfDay().getName());
        assertEquals("minuteOfHour", greg.minuteOfHour().getName());
        assertEquals("secondOfDay", greg.secondOfDay().getName());
        assertEquals("secondOfMinute", greg.secondOfMinute().getName());
        assertEquals("millisOfDay", greg.millisOfDay().getName());
        assertEquals("millisOfSecond", greg.millisOfSecond().getName());
        
        assertEquals(true, greg.halfdayOfDay().isSupported());
        assertEquals(true, greg.clockhourOfHalfday().isSupported());
        assertEquals(true, greg.hourOfHalfday().isSupported());
        assertEquals(true, greg.clockhourOfDay().isSupported());
        assertEquals(true, greg.hourOfDay().isSupported());
        assertEquals(true, greg.minuteOfDay().isSupported());
        assertEquals(true, greg.minuteOfHour().isSupported());
        assertEquals(true, greg.secondOfDay().isSupported());
        assertEquals(true, greg.secondOfMinute().isSupported());
        assertEquals(true, greg.millisOfDay().isSupported());
        assertEquals(true, greg.millisOfSecond().isSupported());
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
        final ISOChronology iso = ISOChronology.getInstance();
        assertEquals("eras", iso.eras().getName());
        assertEquals("centuries", iso.centuries().getName());
        assertEquals("years", iso.years().getName());
        assertEquals("weekyears", iso.weekyears().getName());
        assertEquals("months", iso.months().getName());
        assertEquals("weeks", iso.weeks().getName());
        assertEquals("days", iso.days().getName());
        assertEquals("halfdays", iso.halfdays().getName());
        assertEquals("hours", iso.hours().getName());
        assertEquals("minutes", iso.minutes().getName());
        assertEquals("seconds", iso.seconds().getName());
        assertEquals("millis", iso.millis().getName());
        
        assertEquals(false, iso.eras().isSupported());
        assertEquals(true, iso.centuries().isSupported());
        assertEquals(true, iso.years().isSupported());
        assertEquals(true, iso.weekyears().isSupported());
        assertEquals(true, iso.months().isSupported());
        assertEquals(true, iso.weeks().isSupported());
        assertEquals(true, iso.days().isSupported());
        assertEquals(true, iso.halfdays().isSupported());
        assertEquals(true, iso.hours().isSupported());
        assertEquals(true, iso.minutes().isSupported());
        assertEquals(true, iso.seconds().isSupported());
        assertEquals(true, iso.millis().isSupported());
        
        assertEquals(false, iso.centuries().isPrecise());
        assertEquals(false, iso.years().isPrecise());
        assertEquals(false, iso.weekyears().isPrecise());
        assertEquals(false, iso.months().isPrecise());
        assertEquals(false, iso.weeks().isPrecise());
        assertEquals(false, iso.days().isPrecise());
        assertEquals(false, iso.halfdays().isPrecise());
        assertEquals(true, iso.hours().isPrecise());
        assertEquals(true, iso.minutes().isPrecise());
        assertEquals(true, iso.seconds().isPrecise());
        assertEquals(true, iso.millis().isPrecise());
        
        final ISOChronology isoUTC = ISOChronology.getInstanceUTC();
        assertEquals(false, isoUTC.centuries().isPrecise());
        assertEquals(false, isoUTC.years().isPrecise());
        assertEquals(false, isoUTC.weekyears().isPrecise());
        assertEquals(false, isoUTC.months().isPrecise());
        assertEquals(true, isoUTC.weeks().isPrecise());
        assertEquals(true, isoUTC.days().isPrecise());
        assertEquals(true, isoUTC.halfdays().isPrecise());
        assertEquals(true, isoUTC.hours().isPrecise());
        assertEquals(true, isoUTC.minutes().isPrecise());
        assertEquals(true, isoUTC.seconds().isPrecise());
        assertEquals(true, isoUTC.millis().isPrecise());
        
        final DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        final ISOChronology isoGMT = ISOChronology.getInstance(gmt);
        assertEquals(false, isoGMT.centuries().isPrecise());
        assertEquals(false, isoGMT.years().isPrecise());
        assertEquals(false, isoGMT.weekyears().isPrecise());
        assertEquals(false, isoGMT.months().isPrecise());
        assertEquals(true, isoGMT.weeks().isPrecise());
        assertEquals(true, isoGMT.days().isPrecise());
        assertEquals(true, isoGMT.halfdays().isPrecise());
        assertEquals(true, isoGMT.hours().isPrecise());
        assertEquals(true, isoGMT.minutes().isPrecise());
        assertEquals(true, isoGMT.seconds().isPrecise());
        assertEquals(true, isoGMT.millis().isPrecise());
        
        final DateTimeZone offset = DateTimeZone.forOffsetHours(1);
        final ISOChronology isoOffset1 = ISOChronology.getInstance(offset);
        assertEquals(false, isoOffset1.centuries().isPrecise());
        assertEquals(false, isoOffset1.years().isPrecise());
        assertEquals(false, isoOffset1.weekyears().isPrecise());
        assertEquals(false, isoOffset1.months().isPrecise());
        assertEquals(true, isoOffset1.weeks().isPrecise());
        assertEquals(true, isoOffset1.days().isPrecise());
        assertEquals(true, isoOffset1.halfdays().isPrecise());
        assertEquals(true, isoOffset1.hours().isPrecise());
        assertEquals(true, isoOffset1.minutes().isPrecise());
        assertEquals(true, isoOffset1.seconds().isPrecise());
        assertEquals(true, isoOffset1.millis().isPrecise());
    }

// org.joda.time.chrono.TestISOChronology::testDateFields
    public void testDateFields() {
        final ISOChronology iso = ISOChronology.getInstance();
        assertEquals("era", iso.era().getName());
        assertEquals("centuryOfEra", iso.centuryOfEra().getName());
        assertEquals("yearOfCentury", iso.yearOfCentury().getName());
        assertEquals("yearOfEra", iso.yearOfEra().getName());
        assertEquals("year", iso.year().getName());
        assertEquals("monthOfYear", iso.monthOfYear().getName());
        assertEquals("weekyearOfCentury", iso.weekyearOfCentury().getName());
        assertEquals("weekyear", iso.weekyear().getName());
        assertEquals("weekOfWeekyear", iso.weekOfWeekyear().getName());
        assertEquals("dayOfYear", iso.dayOfYear().getName());
        assertEquals("dayOfMonth", iso.dayOfMonth().getName());
        assertEquals("dayOfWeek", iso.dayOfWeek().getName());
        
        assertEquals(true, iso.era().isSupported());
        assertEquals(true, iso.centuryOfEra().isSupported());
        assertEquals(true, iso.yearOfCentury().isSupported());
        assertEquals(true, iso.yearOfEra().isSupported());
        assertEquals(true, iso.year().isSupported());
        assertEquals(true, iso.monthOfYear().isSupported());
        assertEquals(true, iso.weekyearOfCentury().isSupported());
        assertEquals(true, iso.weekyear().isSupported());
        assertEquals(true, iso.weekOfWeekyear().isSupported());
        assertEquals(true, iso.dayOfYear().isSupported());
        assertEquals(true, iso.dayOfMonth().isSupported());
        assertEquals(true, iso.dayOfWeek().isSupported());
        
        assertEquals(iso.eras(), iso.era().getDurationField());
        assertEquals(iso.centuries(), iso.centuryOfEra().getDurationField());
        assertEquals(iso.years(), iso.yearOfCentury().getDurationField());
        assertEquals(iso.years(), iso.yearOfEra().getDurationField());
        assertEquals(iso.years(), iso.year().getDurationField());
        assertEquals(iso.months(), iso.monthOfYear().getDurationField());
        assertEquals(iso.weekyears(), iso.weekyearOfCentury().getDurationField());
        assertEquals(iso.weekyears(), iso.weekyear().getDurationField());
        assertEquals(iso.weeks(), iso.weekOfWeekyear().getDurationField());
        assertEquals(iso.days(), iso.dayOfYear().getDurationField());
        assertEquals(iso.days(), iso.dayOfMonth().getDurationField());
        assertEquals(iso.days(), iso.dayOfWeek().getDurationField());
        
        assertEquals(null, iso.era().getRangeDurationField());
        assertEquals(iso.eras(), iso.centuryOfEra().getRangeDurationField());
        assertEquals(iso.centuries(), iso.yearOfCentury().getRangeDurationField());
        assertEquals(iso.eras(), iso.yearOfEra().getRangeDurationField());
        assertEquals(null, iso.year().getRangeDurationField());
        assertEquals(iso.years(), iso.monthOfYear().getRangeDurationField());
        assertEquals(iso.centuries(), iso.weekyearOfCentury().getRangeDurationField());
        assertEquals(null, iso.weekyear().getRangeDurationField());
        assertEquals(iso.weekyears(), iso.weekOfWeekyear().getRangeDurationField());
        assertEquals(iso.years(), iso.dayOfYear().getRangeDurationField());
        assertEquals(iso.months(), iso.dayOfMonth().getRangeDurationField());
        assertEquals(iso.weeks(), iso.dayOfWeek().getRangeDurationField());
    }

// org.joda.time.chrono.TestISOChronology::testTimeFields
    public void testTimeFields() {
        final ISOChronology iso = ISOChronology.getInstance();
        assertEquals("halfdayOfDay", iso.halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", iso.clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", iso.hourOfHalfday().getName());
        assertEquals("clockhourOfDay", iso.clockhourOfDay().getName());
        assertEquals("hourOfDay", iso.hourOfDay().getName());
        assertEquals("minuteOfDay", iso.minuteOfDay().getName());
        assertEquals("minuteOfHour", iso.minuteOfHour().getName());
        assertEquals("secondOfDay", iso.secondOfDay().getName());
        assertEquals("secondOfMinute", iso.secondOfMinute().getName());
        assertEquals("millisOfDay", iso.millisOfDay().getName());
        assertEquals("millisOfSecond", iso.millisOfSecond().getName());
        
        assertEquals(true, iso.halfdayOfDay().isSupported());
        assertEquals(true, iso.clockhourOfHalfday().isSupported());
        assertEquals(true, iso.hourOfHalfday().isSupported());
        assertEquals(true, iso.clockhourOfDay().isSupported());
        assertEquals(true, iso.hourOfDay().isSupported());
        assertEquals(true, iso.minuteOfDay().isSupported());
        assertEquals(true, iso.minuteOfHour().isSupported());
        assertEquals(true, iso.secondOfDay().isSupported());
        assertEquals(true, iso.secondOfMinute().isSupported());
        assertEquals(true, iso.millisOfDay().isSupported());
        assertEquals(true, iso.millisOfSecond().isSupported());
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
        final IslamicChronology islamic = IslamicChronology.getInstance();
        assertEquals("eras", islamic.eras().getName());
        assertEquals("centuries", islamic.centuries().getName());
        assertEquals("years", islamic.years().getName());
        assertEquals("weekyears", islamic.weekyears().getName());
        assertEquals("months", islamic.months().getName());
        assertEquals("weeks", islamic.weeks().getName());
        assertEquals("days", islamic.days().getName());
        assertEquals("halfdays", islamic.halfdays().getName());
        assertEquals("hours", islamic.hours().getName());
        assertEquals("minutes", islamic.minutes().getName());
        assertEquals("seconds", islamic.seconds().getName());
        assertEquals("millis", islamic.millis().getName());
        
        assertEquals(false, islamic.eras().isSupported());
        assertEquals(true, islamic.centuries().isSupported());
        assertEquals(true, islamic.years().isSupported());
        assertEquals(true, islamic.weekyears().isSupported());
        assertEquals(true, islamic.months().isSupported());
        assertEquals(true, islamic.weeks().isSupported());
        assertEquals(true, islamic.days().isSupported());
        assertEquals(true, islamic.halfdays().isSupported());
        assertEquals(true, islamic.hours().isSupported());
        assertEquals(true, islamic.minutes().isSupported());
        assertEquals(true, islamic.seconds().isSupported());
        assertEquals(true, islamic.millis().isSupported());
        
        assertEquals(false, islamic.centuries().isPrecise());
        assertEquals(false, islamic.years().isPrecise());
        assertEquals(false, islamic.weekyears().isPrecise());
        assertEquals(false, islamic.months().isPrecise());
        assertEquals(false, islamic.weeks().isPrecise());
        assertEquals(false, islamic.days().isPrecise());
        assertEquals(false, islamic.halfdays().isPrecise());
        assertEquals(true, islamic.hours().isPrecise());
        assertEquals(true, islamic.minutes().isPrecise());
        assertEquals(true, islamic.seconds().isPrecise());
        assertEquals(true, islamic.millis().isPrecise());
        
        final IslamicChronology islamicUTC = IslamicChronology.getInstanceUTC();
        assertEquals(false, islamicUTC.centuries().isPrecise());
        assertEquals(false, islamicUTC.years().isPrecise());
        assertEquals(false, islamicUTC.weekyears().isPrecise());
        assertEquals(false, islamicUTC.months().isPrecise());
        assertEquals(true, islamicUTC.weeks().isPrecise());
        assertEquals(true, islamicUTC.days().isPrecise());
        assertEquals(true, islamicUTC.halfdays().isPrecise());
        assertEquals(true, islamicUTC.hours().isPrecise());
        assertEquals(true, islamicUTC.minutes().isPrecise());
        assertEquals(true, islamicUTC.seconds().isPrecise());
        assertEquals(true, islamicUTC.millis().isPrecise());
        
        final DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        final IslamicChronology islamicGMT = IslamicChronology.getInstance(gmt);
        assertEquals(false, islamicGMT.centuries().isPrecise());
        assertEquals(false, islamicGMT.years().isPrecise());
        assertEquals(false, islamicGMT.weekyears().isPrecise());
        assertEquals(false, islamicGMT.months().isPrecise());
        assertEquals(true, islamicGMT.weeks().isPrecise());
        assertEquals(true, islamicGMT.days().isPrecise());
        assertEquals(true, islamicGMT.halfdays().isPrecise());
        assertEquals(true, islamicGMT.hours().isPrecise());
        assertEquals(true, islamicGMT.minutes().isPrecise());
        assertEquals(true, islamicGMT.seconds().isPrecise());
        assertEquals(true, islamicGMT.millis().isPrecise());
    }

// org.joda.time.chrono.TestIslamicChronology::testDateFields
    public void testDateFields() {
        final IslamicChronology islamic = IslamicChronology.getInstance();
        assertEquals("era", islamic.era().getName());
        assertEquals("centuryOfEra", islamic.centuryOfEra().getName());
        assertEquals("yearOfCentury", islamic.yearOfCentury().getName());
        assertEquals("yearOfEra", islamic.yearOfEra().getName());
        assertEquals("year", islamic.year().getName());
        assertEquals("monthOfYear", islamic.monthOfYear().getName());
        assertEquals("weekyearOfCentury", islamic.weekyearOfCentury().getName());
        assertEquals("weekyear", islamic.weekyear().getName());
        assertEquals("weekOfWeekyear", islamic.weekOfWeekyear().getName());
        assertEquals("dayOfYear", islamic.dayOfYear().getName());
        assertEquals("dayOfMonth", islamic.dayOfMonth().getName());
        assertEquals("dayOfWeek", islamic.dayOfWeek().getName());
        
        assertEquals(true, islamic.era().isSupported());
        assertEquals(true, islamic.centuryOfEra().isSupported());
        assertEquals(true, islamic.yearOfCentury().isSupported());
        assertEquals(true, islamic.yearOfEra().isSupported());
        assertEquals(true, islamic.year().isSupported());
        assertEquals(true, islamic.monthOfYear().isSupported());
        assertEquals(true, islamic.weekyearOfCentury().isSupported());
        assertEquals(true, islamic.weekyear().isSupported());
        assertEquals(true, islamic.weekOfWeekyear().isSupported());
        assertEquals(true, islamic.dayOfYear().isSupported());
        assertEquals(true, islamic.dayOfMonth().isSupported());
        assertEquals(true, islamic.dayOfWeek().isSupported());
        
        assertEquals(islamic.eras(), islamic.era().getDurationField());
        assertEquals(islamic.centuries(), islamic.centuryOfEra().getDurationField());
        assertEquals(islamic.years(), islamic.yearOfCentury().getDurationField());
        assertEquals(islamic.years(), islamic.yearOfEra().getDurationField());
        assertEquals(islamic.years(), islamic.year().getDurationField());
        assertEquals(islamic.months(), islamic.monthOfYear().getDurationField());
        assertEquals(islamic.weekyears(), islamic.weekyearOfCentury().getDurationField());
        assertEquals(islamic.weekyears(), islamic.weekyear().getDurationField());
        assertEquals(islamic.weeks(), islamic.weekOfWeekyear().getDurationField());
        assertEquals(islamic.days(), islamic.dayOfYear().getDurationField());
        assertEquals(islamic.days(), islamic.dayOfMonth().getDurationField());
        assertEquals(islamic.days(), islamic.dayOfWeek().getDurationField());
        
        assertEquals(null, islamic.era().getRangeDurationField());
        assertEquals(islamic.eras(), islamic.centuryOfEra().getRangeDurationField());
        assertEquals(islamic.centuries(), islamic.yearOfCentury().getRangeDurationField());
        assertEquals(islamic.eras(), islamic.yearOfEra().getRangeDurationField());
        assertEquals(null, islamic.year().getRangeDurationField());
        assertEquals(islamic.years(), islamic.monthOfYear().getRangeDurationField());
        assertEquals(islamic.centuries(), islamic.weekyearOfCentury().getRangeDurationField());
        assertEquals(null, islamic.weekyear().getRangeDurationField());
        assertEquals(islamic.weekyears(), islamic.weekOfWeekyear().getRangeDurationField());
        assertEquals(islamic.years(), islamic.dayOfYear().getRangeDurationField());
        assertEquals(islamic.months(), islamic.dayOfMonth().getRangeDurationField());
        assertEquals(islamic.weeks(), islamic.dayOfWeek().getRangeDurationField());
    }

// org.joda.time.chrono.TestIslamicChronology::testTimeFields
    public void testTimeFields() {
        final IslamicChronology islamic = IslamicChronology.getInstance();
        assertEquals("halfdayOfDay", islamic.halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", islamic.clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", islamic.hourOfHalfday().getName());
        assertEquals("clockhourOfDay", islamic.clockhourOfDay().getName());
        assertEquals("hourOfDay", islamic.hourOfDay().getName());
        assertEquals("minuteOfDay", islamic.minuteOfDay().getName());
        assertEquals("minuteOfHour", islamic.minuteOfHour().getName());
        assertEquals("secondOfDay", islamic.secondOfDay().getName());
        assertEquals("secondOfMinute", islamic.secondOfMinute().getName());
        assertEquals("millisOfDay", islamic.millisOfDay().getName());
        assertEquals("millisOfSecond", islamic.millisOfSecond().getName());
        
        assertEquals(true, islamic.halfdayOfDay().isSupported());
        assertEquals(true, islamic.clockhourOfHalfday().isSupported());
        assertEquals(true, islamic.hourOfHalfday().isSupported());
        assertEquals(true, islamic.clockhourOfDay().isSupported());
        assertEquals(true, islamic.hourOfDay().isSupported());
        assertEquals(true, islamic.minuteOfDay().isSupported());
        assertEquals(true, islamic.minuteOfHour().isSupported());
        assertEquals(true, islamic.secondOfDay().isSupported());
        assertEquals(true, islamic.secondOfMinute().isSupported());
        assertEquals(true, islamic.millisOfDay().isSupported());
        assertEquals(true, islamic.millisOfSecond().isSupported());
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
        final JulianChronology julian = JulianChronology.getInstance();
        assertEquals("eras", julian.eras().getName());
        assertEquals("centuries", julian.centuries().getName());
        assertEquals("years", julian.years().getName());
        assertEquals("weekyears", julian.weekyears().getName());
        assertEquals("months", julian.months().getName());
        assertEquals("weeks", julian.weeks().getName());
        assertEquals("days", julian.days().getName());
        assertEquals("halfdays", julian.halfdays().getName());
        assertEquals("hours", julian.hours().getName());
        assertEquals("minutes", julian.minutes().getName());
        assertEquals("seconds", julian.seconds().getName());
        assertEquals("millis", julian.millis().getName());
        
        assertEquals(false, julian.eras().isSupported());
        assertEquals(true, julian.centuries().isSupported());
        assertEquals(true, julian.years().isSupported());
        assertEquals(true, julian.weekyears().isSupported());
        assertEquals(true, julian.months().isSupported());
        assertEquals(true, julian.weeks().isSupported());
        assertEquals(true, julian.days().isSupported());
        assertEquals(true, julian.halfdays().isSupported());
        assertEquals(true, julian.hours().isSupported());
        assertEquals(true, julian.minutes().isSupported());
        assertEquals(true, julian.seconds().isSupported());
        assertEquals(true, julian.millis().isSupported());
        
        assertEquals(false, julian.centuries().isPrecise());
        assertEquals(false, julian.years().isPrecise());
        assertEquals(false, julian.weekyears().isPrecise());
        assertEquals(false, julian.months().isPrecise());
        assertEquals(false, julian.weeks().isPrecise());
        assertEquals(false, julian.days().isPrecise());
        assertEquals(false, julian.halfdays().isPrecise());
        assertEquals(true, julian.hours().isPrecise());
        assertEquals(true, julian.minutes().isPrecise());
        assertEquals(true, julian.seconds().isPrecise());
        assertEquals(true, julian.millis().isPrecise());
        
        final JulianChronology julianUTC = JulianChronology.getInstanceUTC();
        assertEquals(false, julianUTC.centuries().isPrecise());
        assertEquals(false, julianUTC.years().isPrecise());
        assertEquals(false, julianUTC.weekyears().isPrecise());
        assertEquals(false, julianUTC.months().isPrecise());
        assertEquals(true, julianUTC.weeks().isPrecise());
        assertEquals(true, julianUTC.days().isPrecise());
        assertEquals(true, julianUTC.halfdays().isPrecise());
        assertEquals(true, julianUTC.hours().isPrecise());
        assertEquals(true, julianUTC.minutes().isPrecise());
        assertEquals(true, julianUTC.seconds().isPrecise());
        assertEquals(true, julianUTC.millis().isPrecise());
        
        final DateTimeZone gmt = DateTimeZone.forID("Etc/GMT");
        final JulianChronology julianGMT = JulianChronology.getInstance(gmt);
        assertEquals(false, julianGMT.centuries().isPrecise());
        assertEquals(false, julianGMT.years().isPrecise());
        assertEquals(false, julianGMT.weekyears().isPrecise());
        assertEquals(false, julianGMT.months().isPrecise());
        assertEquals(true, julianGMT.weeks().isPrecise());
        assertEquals(true, julianGMT.days().isPrecise());
        assertEquals(true, julianGMT.halfdays().isPrecise());
        assertEquals(true, julianGMT.hours().isPrecise());
        assertEquals(true, julianGMT.minutes().isPrecise());
        assertEquals(true, julianGMT.seconds().isPrecise());
        assertEquals(true, julianGMT.millis().isPrecise());
    }

// org.joda.time.chrono.TestJulianChronology::testDateFields
    public void testDateFields() {
        final JulianChronology julian = JulianChronology.getInstance();
        assertEquals("era", julian.era().getName());
        assertEquals("centuryOfEra", julian.centuryOfEra().getName());
        assertEquals("yearOfCentury", julian.yearOfCentury().getName());
        assertEquals("yearOfEra", julian.yearOfEra().getName());
        assertEquals("year", julian.year().getName());
        assertEquals("monthOfYear", julian.monthOfYear().getName());
        assertEquals("weekyearOfCentury", julian.weekyearOfCentury().getName());
        assertEquals("weekyear", julian.weekyear().getName());
        assertEquals("weekOfWeekyear", julian.weekOfWeekyear().getName());
        assertEquals("dayOfYear", julian.dayOfYear().getName());
        assertEquals("dayOfMonth", julian.dayOfMonth().getName());
        assertEquals("dayOfWeek", julian.dayOfWeek().getName());
        
        assertEquals(true, julian.era().isSupported());
        assertEquals(true, julian.centuryOfEra().isSupported());
        assertEquals(true, julian.yearOfCentury().isSupported());
        assertEquals(true, julian.yearOfEra().isSupported());
        assertEquals(true, julian.year().isSupported());
        assertEquals(true, julian.monthOfYear().isSupported());
        assertEquals(true, julian.weekyearOfCentury().isSupported());
        assertEquals(true, julian.weekyear().isSupported());
        assertEquals(true, julian.weekOfWeekyear().isSupported());
        assertEquals(true, julian.dayOfYear().isSupported());
        assertEquals(true, julian.dayOfMonth().isSupported());
        assertEquals(true, julian.dayOfWeek().isSupported());
        
        assertEquals(julian.eras(), julian.era().getDurationField());
        assertEquals(julian.centuries(), julian.centuryOfEra().getDurationField());
        assertEquals(julian.years(), julian.yearOfCentury().getDurationField());
        assertEquals(julian.years(), julian.yearOfEra().getDurationField());
        assertEquals(julian.years(), julian.year().getDurationField());
        assertEquals(julian.months(), julian.monthOfYear().getDurationField());
        assertEquals(julian.weekyears(), julian.weekyearOfCentury().getDurationField());
        assertEquals(julian.weekyears(), julian.weekyear().getDurationField());
        assertEquals(julian.weeks(), julian.weekOfWeekyear().getDurationField());
        assertEquals(julian.days(), julian.dayOfYear().getDurationField());
        assertEquals(julian.days(), julian.dayOfMonth().getDurationField());
        assertEquals(julian.days(), julian.dayOfWeek().getDurationField());
        
        assertEquals(null, julian.era().getRangeDurationField());
        assertEquals(julian.eras(), julian.centuryOfEra().getRangeDurationField());
        assertEquals(julian.centuries(), julian.yearOfCentury().getRangeDurationField());
        assertEquals(julian.eras(), julian.yearOfEra().getRangeDurationField());
        assertEquals(null, julian.year().getRangeDurationField());
        assertEquals(julian.years(), julian.monthOfYear().getRangeDurationField());
        assertEquals(julian.centuries(), julian.weekyearOfCentury().getRangeDurationField());
        assertEquals(null, julian.weekyear().getRangeDurationField());
        assertEquals(julian.weekyears(), julian.weekOfWeekyear().getRangeDurationField());
        assertEquals(julian.years(), julian.dayOfYear().getRangeDurationField());
        assertEquals(julian.months(), julian.dayOfMonth().getRangeDurationField());
        assertEquals(julian.weeks(), julian.dayOfWeek().getRangeDurationField());
    }

// org.joda.time.chrono.TestJulianChronology::testTimeFields
    public void testTimeFields() {
        final JulianChronology julian = JulianChronology.getInstance();
        assertEquals("halfdayOfDay", julian.halfdayOfDay().getName());
        assertEquals("clockhourOfHalfday", julian.clockhourOfHalfday().getName());
        assertEquals("hourOfHalfday", julian.hourOfHalfday().getName());
        assertEquals("clockhourOfDay", julian.clockhourOfDay().getName());
        assertEquals("hourOfDay", julian.hourOfDay().getName());
        assertEquals("minuteOfDay", julian.minuteOfDay().getName());
        assertEquals("minuteOfHour", julian.minuteOfHour().getName());
        assertEquals("secondOfDay", julian.secondOfDay().getName());
        assertEquals("secondOfMinute", julian.secondOfMinute().getName());
        assertEquals("millisOfDay", julian.millisOfDay().getName());
        assertEquals("millisOfSecond", julian.millisOfSecond().getName());
        
        assertEquals(true, julian.halfdayOfDay().isSupported());
        assertEquals(true, julian.clockhourOfHalfday().isSupported());
        assertEquals(true, julian.hourOfHalfday().isSupported());
        assertEquals(true, julian.clockhourOfDay().isSupported());
        assertEquals(true, julian.hourOfDay().isSupported());
        assertEquals(true, julian.minuteOfDay().isSupported());
        assertEquals(true, julian.minuteOfHour().isSupported());
        assertEquals(true, julian.secondOfDay().isSupported());
        assertEquals(true, julian.secondOfMinute().isSupported());
        assertEquals(true, julian.millisOfDay().isSupported());
        assertEquals(true, julian.millisOfSecond().isSupported());
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

// org.joda.time.convert.TestDateConverter::testToString
    public void testToString() {
        assertEquals("Converter[java.util.Date]", DateConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestLongConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = LongConverter.class;
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

// org.joda.time.convert.TestLongConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(Long.class, LongConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestLongConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        assertEquals(123L, LongConverter.INSTANCE.getInstantMillis(new Long(123L), JULIAN));
        assertEquals(123L, LongConverter.INSTANCE.getInstantMillis(new Long(123L), (Chronology) null));
    }

// org.joda.time.convert.TestLongConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISO_PARIS, LongConverter.INSTANCE.getChronology(new Long(123L), PARIS));
        assertEquals(ISO, LongConverter.INSTANCE.getChronology(new Long(123L), (DateTimeZone) null));
    }

// org.joda.time.convert.TestLongConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JULIAN, LongConverter.INSTANCE.getChronology(new Long(123L), JULIAN));
        assertEquals(ISO, LongConverter.INSTANCE.getChronology(new Long(123L), (Chronology) null));
    }

// org.joda.time.convert.TestLongConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = ISOChronology.getInstance().get(tod, 12345678L);
        int[] actual = LongConverter.INSTANCE.getPartialValues(tod, new Long(12345678L), ISOChronology.getInstance());
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestLongConverter::testGetDurationMillis_Object
    public void testGetDurationMillis_Object() throws Exception {
        assertEquals(123L, LongConverter.INSTANCE.getDurationMillis(new Long(123L)));
    }

// org.joda.time.convert.TestLongConverter::testToString
    public void testToString() {
        assertEquals("Converter[java.lang.Long]", LongConverter.INSTANCE.toString());
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

// org.joda.time.convert.TestReadableInstantConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ReadableInstantConverter.class;
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

// org.joda.time.convert.TestReadableInstantConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(ReadableInstant.class, ReadableInstantConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestReadableInstantConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        assertEquals(123L, ReadableInstantConverter.INSTANCE.getInstantMillis(new Instant(123L), JULIAN));
        assertEquals(123L, ReadableInstantConverter.INSTANCE.getInstantMillis(new DateTime(123L), JULIAN));
        assertEquals(123L, ReadableInstantConverter.INSTANCE.getInstantMillis(new Instant(123L), (Chronology) null));
        assertEquals(123L, ReadableInstantConverter.INSTANCE.getInstantMillis(new DateTime(123L), (Chronology) null));
    }

// org.joda.time.convert.TestReadableInstantConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISO_PARIS, ReadableInstantConverter.INSTANCE.getChronology(new Instant(123L), PARIS));
        assertEquals(ISO_PARIS, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L), PARIS));
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(new Instant(123L), DateTimeZone.getDefault()));
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L), DateTimeZone.getDefault()));
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(new Instant(123L), (DateTimeZone) null));
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L), (DateTimeZone) null));
        
        assertEquals(ISO_PARIS, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L, new MockBadChronology()), PARIS));
        
        MutableDateTime mdt = new MutableDateTime() {
            public Chronology getChronology() {
                return null; 
            }
        };
        assertEquals(ISO_PARIS, ReadableInstantConverter.INSTANCE.getChronology(mdt, PARIS));
    }

// org.joda.time.convert.TestReadableInstantConverter::testGetChronology_Object_nullChronology
    public void testGetChronology_Object_nullChronology() throws Exception {
        assertEquals(ISO.withUTC(), ReadableInstantConverter.INSTANCE.getChronology(new Instant(123L), (Chronology) null));
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L), (Chronology) null));
        
        MutableDateTime mdt = new MutableDateTime() {
            public Chronology getChronology() {
                return null; 
            }
        };
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(mdt, (Chronology) null));
    }

// org.joda.time.convert.TestReadableInstantConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JULIAN, ReadableInstantConverter.INSTANCE.getChronology(new Instant(123L), JULIAN));
        assertEquals(JULIAN, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L), JULIAN));
    }

// org.joda.time.convert.TestReadableInstantConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = ISOChronology.getInstance().get(tod, 12345678L);
        int[] actual = ReadableInstantConverter.INSTANCE.getPartialValues(tod, new Instant(12345678L), ISOChronology.getInstance());
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestReadableInstantConverter::testToString
    public void testToString() {
        assertEquals("Converter[org.joda.time.ReadableInstant]", ReadableInstantConverter.INSTANCE.toString());
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

// org.joda.time.convert.TestReadablePartialConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ReadablePartialConverter.class;
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

// org.joda.time.convert.TestReadablePartialConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(ReadablePartial.class, ReadablePartialConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestReadablePartialConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISO_PARIS, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), PARIS));
        assertEquals(ISO, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), DateTimeZone.getDefault()));
        assertEquals(ISO, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), (DateTimeZone) null));
    }

// org.joda.time.convert.TestReadablePartialConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JULIAN, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L, BUDDHIST), JULIAN));
        assertEquals(JULIAN, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), JULIAN));
        assertEquals(BUDDHIST.withUTC(), ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L, BUDDHIST), (Chronology) null));
    }

// org.joda.time.convert.TestReadablePartialConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = new int[] {1, 2, 3, 4};
        int[] actual = ReadablePartialConverter.INSTANCE.getPartialValues(tod, new TimeOfDay(1, 2, 3, 4), ISOChronology.getInstance(PARIS));
        assertEquals(true, Arrays.equals(expected, actual));
        
        try {
            ReadablePartialConverter.INSTANCE.getPartialValues(tod, new YearMonthDay(2005, 6, 9), JULIAN);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ReadablePartialConverter.INSTANCE.getPartialValues(tod, new MockTOD(), JULIAN);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestReadablePartialConverter::testToString
    public void testToString() {
        assertEquals("Converter[org.joda.time.ReadablePartial]", ReadablePartialConverter.INSTANCE.toString());
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
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt-0.32s");
        assertEquals(-320, millis);

        millis = StringConverter.INSTANCE.getDurationMillis("pt-0.0s");
        assertEquals(0, millis);

        millis = StringConverter.INSTANCE.getDurationMillis("pt0.0s");
        assertEquals(0, millis);

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
        try {
            StringConverter.INSTANCE.getDurationMillis("PT-.001S");
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

// org.joda.time.field.TestBaseDateTimeField::test_constructor
    public void test_constructor() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(DateTimeFieldType.secondOfMinute(), field.getType());
        try {
            field = new MockBaseDateTimeField(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestBaseDateTimeField::test_getType
    public void test_getType() {
        BaseDateTimeField field = new MockBaseDateTimeField(DateTimeFieldType.secondOfDay());
        assertEquals(DateTimeFieldType.secondOfDay(), field.getType());
    }

// org.joda.time.field.TestBaseDateTimeField::test_getName
    public void test_getName() {
        BaseDateTimeField field = new MockBaseDateTimeField(DateTimeFieldType.secondOfDay());
        assertEquals("secondOfDay", field.getName());
    }

// org.joda.time.field.TestBaseDateTimeField::test_toString
    public void test_toString() {
        BaseDateTimeField field = new MockBaseDateTimeField(DateTimeFieldType.secondOfDay());
        assertEquals("DateTimeField[secondOfDay]", field.toString());
    }

// org.joda.time.field.TestBaseDateTimeField::test_isSupported
    public void test_isSupported() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(true, field.isSupported());
    }

// org.joda.time.field.TestBaseDateTimeField::test_get
    public void test_get() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.get(0));
        assertEquals(1, field.get(60));
        assertEquals(2, field.get(123));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsText_long_Locale
    public void test_getAsText_long_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("29", field.getAsText(60L * 29, Locale.ENGLISH));
        assertEquals("29", field.getAsText(60L * 29, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsText_long
    public void test_getAsText_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("29", field.getAsText(60L * 29));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsText_RP_int_Locale
    public void test_getAsText_RP_int_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("20", field.getAsText(new TimeOfDay(12, 30, 40, 50), 20, Locale.ENGLISH));
        assertEquals("20", field.getAsText(new TimeOfDay(12, 30, 40, 50), 20, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsText_RP_Locale
    public void test_getAsText_RP_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("40", field.getAsText(new TimeOfDay(12, 30, 40, 50), Locale.ENGLISH));
        assertEquals("40", field.getAsText(new TimeOfDay(12, 30, 40, 50), null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsText_int_Locale
    public void test_getAsText_int_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("80", field.getAsText(80, Locale.ENGLISH));
        assertEquals("80", field.getAsText(80, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsShortText_long_Locale
    public void test_getAsShortText_long_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("29", field.getAsShortText(60L * 29, Locale.ENGLISH));
        assertEquals("29", field.getAsShortText(60L * 29, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsShortText_long
    public void test_getAsShortText_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("29", field.getAsShortText(60L * 29));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsShortText_RP_int_Locale
    public void test_getAsShortText_RP_int_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("20", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), 20, Locale.ENGLISH));
        assertEquals("20", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), 20, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsShortText_RP_Locale
    public void test_getAsShortText_RP_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("40", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), Locale.ENGLISH));
        assertEquals("40", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsShortText_int_Locale
    public void test_getAsShortText_int_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("80", field.getAsShortText(80, Locale.ENGLISH));
        assertEquals("80", field.getAsShortText(80, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_add_long_int
    public void test_add_long_int() {
        MockCountingDurationField.add_int = 0;
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(61, field.add(1L, 1));
        assertEquals(1, MockCountingDurationField.add_int);
    }

// org.joda.time.field.TestBaseDateTimeField::test_add_long_long
    public void test_add_long_long() {
        MockCountingDurationField.add_long = 0;
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(61, field.add(1L, 1L));
        assertEquals(1, MockCountingDurationField.add_long);
    }

// org.joda.time.field.TestBaseDateTimeField::test_add_RP_int_intarray_int
    public void test_add_RP_int_intarray_int() {
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        BaseDateTimeField field = new MockStandardBaseDateTimeField();
        int[] result = field.add(new TimeOfDay(), 2, values, 0);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 31, 40};
        result = field.add(new TimeOfDay(), 2, values, 1);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 21, 0, 40};
        result = field.add(new TimeOfDay(), 2, values, 30);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {23, 59, 30, 40};
        try {
            field.add(new TimeOfDay(), 2, values, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.add(new TimeOfDay(), 2, values, -1);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 19, 59, 40};
        result = field.add(new TimeOfDay(), 2, values, -31);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {0, 0, 30, 40};
        try {
            field.add(new TimeOfDay(), 2, values, -31);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        values = new int[] {0, 0};
        try {
            field.add(new MockPartial(), 0, values, 1000);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        values = new int[] {1, 0};
        try {
            field.add(new MockPartial(), 0, values, -1000);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestBaseDateTimeField::test_addWrapField_long_int
    public void test_addWrapField_long_int() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(1029, field.addWrapField(60L * 29, 0));
        assertEquals(1059, field.addWrapField(60L * 29, 30));
        assertEquals(1000, field.addWrapField(60L * 29, 31));
    }

// org.joda.time.field.TestBaseDateTimeField::test_addWrapField_RP_int_intarray_int
    public void test_addWrapField_RP_int_intarray_int() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.addWrapField(new TimeOfDay(), 2, values, 0);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 59, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 29);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 0, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 30);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 1, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 31);
        assertEquals(true, Arrays.equals(result, expected));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getDifference_long_long
    public void test_getDifference_long_long() {
        MockCountingDurationField.difference_long = 0;
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(30, field.getDifference(0L, 0L));
        assertEquals(1, MockCountingDurationField.difference_long);
    }

// org.joda.time.field.TestBaseDateTimeField::test_getDifferenceAsLong_long_long
    public void test_getDifferenceAsLong_long_long() {
        MockCountingDurationField.difference_long = 0;
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(30, field.getDifferenceAsLong(0L, 0L));
        assertEquals(1, MockCountingDurationField.difference_long);
    }

// org.joda.time.field.TestBaseDateTimeField::test_set_long_int
    public void test_set_long_int() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(1000, field.set(0L, 0));
        assertEquals(1029, field.set(0L, 29));
    }

// org.joda.time.field.TestBaseDateTimeField::test_set_RP_int_intarray_int
    public void test_set_RP_int_intarray_int() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.set(new TimeOfDay(), 2, values, 30);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.set(new TimeOfDay(), 2, values, 29);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
    }

// org.joda.time.field.TestBaseDateTimeField::test_set_long_String_Locale
    public void test_set_long_String_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(1000, field.set(0L, "0", null));
        assertEquals(1029, field.set(0L, "29", Locale.ENGLISH));
    }

// org.joda.time.field.TestBaseDateTimeField::test_set_long_String
    public void test_set_long_String() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(1000, field.set(0L, "0"));
        assertEquals(1029, field.set(0L, "29"));
    }

// org.joda.time.field.TestBaseDateTimeField::test_set_RP_int_intarray_String_Locale
    public void test_set_RP_int_intarray_String_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.set(new TimeOfDay(), 2, values, "30", null);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.set(new TimeOfDay(), 2, values, "29", Locale.ENGLISH);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, "60", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, "-1", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
    }

// org.joda.time.field.TestBaseDateTimeField::test_convertText
    public void test_convertText() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.convertText("0", null));
        assertEquals(29, field.convertText("29", null));
        try {
            field.convertText("2A", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            field.convertText(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestBaseDateTimeField::test_isLeap_long
    public void test_isLeap_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(false, field.isLeap(0L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getLeapAmount_long
    public void test_getLeapAmount_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.getLeapAmount(0L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getLeapDurationField
    public void test_getLeapDurationField() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(null, field.getLeapDurationField());
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMinimumValue
    public void test_getMinimumValue() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.getMinimumValue());
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMinimumValue_long
    public void test_getMinimumValue_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.getMinimumValue(0L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMinimumValue_RP
    public void test_getMinimumValue_RP() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.getMinimumValue(new TimeOfDay()));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMinimumValue_RP_intarray
    public void test_getMinimumValue_RP_intarray() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.getMinimumValue(new TimeOfDay(), new int[4]));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMaximumValue
    public void test_getMaximumValue() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(59, field.getMaximumValue());
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMaximumValue_long
    public void test_getMaximumValue_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(59, field.getMaximumValue(0L));
    }
