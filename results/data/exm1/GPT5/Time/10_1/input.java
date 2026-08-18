// buggy code
    protected static int between(ReadablePartial start, ReadablePartial end, ReadablePeriod zeroInstance) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("ReadablePartial objects must not be null");
        }
        if (start.size() != end.size()) {
            throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields");
        }
        for (int i = 0, isize = start.size(); i < isize; i++) {
            if (start.getFieldType(i) != end.getFieldType(i)) {
                throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields");
            }
        }
        if (DateTimeUtils.isContiguous(start) == false) {
            throw new IllegalArgumentException("ReadablePartial objects must be contiguous");
        }
        Chronology chrono = DateTimeUtils.getChronology(start.getChronology()).withUTC();
        int[] values = chrono.get(zeroInstance, chrono.set(start, 0L), chrono.set(end, 0L));
        return values[0];
    }

// relevant test
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

// org.joda.time.TestWeeks::testConstants
    public void testConstants() {
        assertEquals(0, Weeks.ZERO.getWeeks());
        assertEquals(1, Weeks.ONE.getWeeks());
        assertEquals(2, Weeks.TWO.getWeeks());
        assertEquals(3, Weeks.THREE.getWeeks());
        assertEquals(Integer.MAX_VALUE, Weeks.MAX_VALUE.getWeeks());
        assertEquals(Integer.MIN_VALUE, Weeks.MIN_VALUE.getWeeks());
    }

// org.joda.time.TestWeeks::testFactory_weeks_int
    public void testFactory_weeks_int() {
        assertSame(Weeks.ZERO, Weeks.weeks(0));
        assertSame(Weeks.ONE, Weeks.weeks(1));
        assertSame(Weeks.TWO, Weeks.weeks(2));
        assertSame(Weeks.THREE, Weeks.weeks(3));
        assertSame(Weeks.MAX_VALUE, Weeks.weeks(Integer.MAX_VALUE));
        assertSame(Weeks.MIN_VALUE, Weeks.weeks(Integer.MIN_VALUE));
        assertEquals(-1, Weeks.weeks(-1).getWeeks());
        assertEquals(4, Weeks.weeks(4).getWeeks());
    }

// org.joda.time.TestWeeks::testFactory_weeksBetween_RInstant
    public void testFactory_weeksBetween_RInstant() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 30, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 7, 21, 12, 0, 0, 0, PARIS);
        
        assertEquals(3, Weeks.weeksBetween(start, end1).getWeeks());
        assertEquals(0, Weeks.weeksBetween(start, start).getWeeks());
        assertEquals(0, Weeks.weeksBetween(end1, end1).getWeeks());
        assertEquals(-3, Weeks.weeksBetween(end1, start).getWeeks());
        assertEquals(6, Weeks.weeksBetween(start, end2).getWeeks());
    }

// org.joda.time.TestWeeks::testFactory_weeksBetween_RPartial
    public void testFactory_weeksBetween_RPartial() {
        LocalDate start = new LocalDate(2006, 6, 9);
        LocalDate end1 = new LocalDate(2006, 6, 30);
        YearMonthDay end2 = new YearMonthDay(2006, 7, 21);
        
        assertEquals(3, Weeks.weeksBetween(start, end1).getWeeks());
        assertEquals(0, Weeks.weeksBetween(start, start).getWeeks());
        assertEquals(0, Weeks.weeksBetween(end1, end1).getWeeks());
        assertEquals(-3, Weeks.weeksBetween(end1, start).getWeeks());
        assertEquals(6, Weeks.weeksBetween(start, end2).getWeeks());
    }

// org.joda.time.TestWeeks::testFactory_weeksIn_RInterval
    public void testFactory_weeksIn_RInterval() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 30, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 7, 21, 12, 0, 0, 0, PARIS);
        
        assertEquals(0, Weeks.weeksIn((ReadableInterval) null).getWeeks());
        assertEquals(3, Weeks.weeksIn(new Interval(start, end1)).getWeeks());
        assertEquals(0, Weeks.weeksIn(new Interval(start, start)).getWeeks());
        assertEquals(0, Weeks.weeksIn(new Interval(end1, end1)).getWeeks());
        assertEquals(6, Weeks.weeksIn(new Interval(start, end2)).getWeeks());
    }

// org.joda.time.TestWeeks::testFactory_standardWeeksIn_RPeriod
    public void testFactory_standardWeeksIn_RPeriod() {
        assertEquals(0, Weeks.standardWeeksIn((ReadablePeriod) null).getWeeks());
        assertEquals(0, Weeks.standardWeeksIn(Period.ZERO).getWeeks());
        assertEquals(1, Weeks.standardWeeksIn(new Period(0, 0, 1, 0, 0, 0, 0, 0)).getWeeks());
        assertEquals(123, Weeks.standardWeeksIn(Period.weeks(123)).getWeeks());
        assertEquals(-987, Weeks.standardWeeksIn(Period.weeks(-987)).getWeeks());
        assertEquals(1, Weeks.standardWeeksIn(Period.days(13)).getWeeks());
        assertEquals(2, Weeks.standardWeeksIn(Period.days(14)).getWeeks());
        assertEquals(2, Weeks.standardWeeksIn(Period.days(15)).getWeeks());
        try {
            Weeks.standardWeeksIn(Period.months(1));
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testFactory_parseWeeks_String
    public void testFactory_parseWeeks_String() {
        assertEquals(0, Weeks.parseWeeks((String) null).getWeeks());
        assertEquals(0, Weeks.parseWeeks("P0W").getWeeks());
        assertEquals(1, Weeks.parseWeeks("P1W").getWeeks());
        assertEquals(-3, Weeks.parseWeeks("P-3W").getWeeks());
        assertEquals(2, Weeks.parseWeeks("P0Y0M2W").getWeeks());
        assertEquals(2, Weeks.parseWeeks("P2WT0H0M").getWeeks());
        try {
            Weeks.parseWeeks("P1Y1D");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Weeks.parseWeeks("P1WT1H");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testGetMethods
    public void testGetMethods() {
        Weeks test = Weeks.weeks(20);
        assertEquals(20, test.getWeeks());
    }

// org.joda.time.TestWeeks::testGetFieldType
    public void testGetFieldType() {
        Weeks test = Weeks.weeks(20);
        assertEquals(DurationFieldType.weeks(), test.getFieldType());
    }

// org.joda.time.TestWeeks::testGetPeriodType
    public void testGetPeriodType() {
        Weeks test = Weeks.weeks(20);
        assertEquals(PeriodType.weeks(), test.getPeriodType());
    }

// org.joda.time.TestWeeks::testIsGreaterThan
    public void testIsGreaterThan() {
        assertEquals(true, Weeks.THREE.isGreaterThan(Weeks.TWO));
        assertEquals(false, Weeks.THREE.isGreaterThan(Weeks.THREE));
        assertEquals(false, Weeks.TWO.isGreaterThan(Weeks.THREE));
        assertEquals(true, Weeks.ONE.isGreaterThan(null));
        assertEquals(false, Weeks.weeks(-1).isGreaterThan(null));
    }

// org.joda.time.TestWeeks::testIsLessThan
    public void testIsLessThan() {
        assertEquals(false, Weeks.THREE.isLessThan(Weeks.TWO));
        assertEquals(false, Weeks.THREE.isLessThan(Weeks.THREE));
        assertEquals(true, Weeks.TWO.isLessThan(Weeks.THREE));
        assertEquals(false, Weeks.ONE.isLessThan(null));
        assertEquals(true, Weeks.weeks(-1).isLessThan(null));
    }

// org.joda.time.TestWeeks::testToString
    public void testToString() {
        Weeks test = Weeks.weeks(20);
        assertEquals("P20W", test.toString());
        
        test = Weeks.weeks(-20);
        assertEquals("P-20W", test.toString());
    }

// org.joda.time.TestWeeks::testSerialization
    public void testSerialization() throws Exception {
        Weeks test = Weeks.THREE;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Weeks result = (Weeks) ois.readObject();
        ois.close();
        
        assertSame(test, result);
    }

// org.joda.time.TestWeeks::testToStandardDays
    public void testToStandardDays() {
        Weeks test = Weeks.weeks(2);
        Days expected = Days.days(14);
        assertEquals(expected, test.toStandardDays());
        
        try {
            Weeks.MAX_VALUE.toStandardDays();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testToStandardHours
    public void testToStandardHours() {
        Weeks test = Weeks.weeks(2);
        Hours expected = Hours.hours(2 * 7 * 24);
        assertEquals(expected, test.toStandardHours());
        
        try {
            Weeks.MAX_VALUE.toStandardHours();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testToStandardMinutes
    public void testToStandardMinutes() {
        Weeks test = Weeks.weeks(2);
        Minutes expected = Minutes.minutes(2 * 7 * 24 * 60);
        assertEquals(expected, test.toStandardMinutes());
        
        try {
            Weeks.MAX_VALUE.toStandardMinutes();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testToStandardSeconds
    public void testToStandardSeconds() {
        Weeks test = Weeks.weeks(2);
        Seconds expected = Seconds.seconds(2 * 7 * 24 * 60 * 60);
        assertEquals(expected, test.toStandardSeconds());
        
        try {
            Weeks.MAX_VALUE.toStandardSeconds();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testToStandardDuration
    public void testToStandardDuration() {
        Weeks test = Weeks.weeks(20);
        Duration expected = new Duration(20L * DateTimeConstants.MILLIS_PER_WEEK);
        assertEquals(expected, test.toStandardDuration());
        
        expected = new Duration(((long) Integer.MAX_VALUE) * DateTimeConstants.MILLIS_PER_WEEK);
        assertEquals(expected, Weeks.MAX_VALUE.toStandardDuration());
    }

// org.joda.time.TestWeeks::testPlus_int
    public void testPlus_int() {
        Weeks test2 = Weeks.weeks(2);
        Weeks result = test2.plus(3);
        assertEquals(2, test2.getWeeks());
        assertEquals(5, result.getWeeks());
        
        assertEquals(1, Weeks.ONE.plus(0).getWeeks());
        
        try {
            Weeks.MAX_VALUE.plus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testPlus_Weeks
    public void testPlus_Weeks() {
        Weeks test2 = Weeks.weeks(2);
        Weeks test3 = Weeks.weeks(3);
        Weeks result = test2.plus(test3);
        assertEquals(2, test2.getWeeks());
        assertEquals(3, test3.getWeeks());
        assertEquals(5, result.getWeeks());
        
        assertEquals(1, Weeks.ONE.plus(Weeks.ZERO).getWeeks());
        assertEquals(1, Weeks.ONE.plus((Weeks) null).getWeeks());
        
        try {
            Weeks.MAX_VALUE.plus(Weeks.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testMinus_int
    public void testMinus_int() {
        Weeks test2 = Weeks.weeks(2);
        Weeks result = test2.minus(3);
        assertEquals(2, test2.getWeeks());
        assertEquals(-1, result.getWeeks());
        
        assertEquals(1, Weeks.ONE.minus(0).getWeeks());
        
        try {
            Weeks.MIN_VALUE.minus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testMinus_Weeks
    public void testMinus_Weeks() {
        Weeks test2 = Weeks.weeks(2);
        Weeks test3 = Weeks.weeks(3);
        Weeks result = test2.minus(test3);
        assertEquals(2, test2.getWeeks());
        assertEquals(3, test3.getWeeks());
        assertEquals(-1, result.getWeeks());
        
        assertEquals(1, Weeks.ONE.minus(Weeks.ZERO).getWeeks());
        assertEquals(1, Weeks.ONE.minus((Weeks) null).getWeeks());
        
        try {
            Weeks.MIN_VALUE.minus(Weeks.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testMultipliedBy_int
    public void testMultipliedBy_int() {
        Weeks test = Weeks.weeks(2);
        assertEquals(6, test.multipliedBy(3).getWeeks());
        assertEquals(2, test.getWeeks());
        assertEquals(-6, test.multipliedBy(-3).getWeeks());
        assertSame(test, test.multipliedBy(1));
        
        Weeks halfMax = Weeks.weeks(Integer.MAX_VALUE / 2 + 1);
        try {
            halfMax.multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testDividedBy_int
    public void testDividedBy_int() {
        Weeks test = Weeks.weeks(12);
        assertEquals(6, test.dividedBy(2).getWeeks());
        assertEquals(12, test.getWeeks());
        assertEquals(4, test.dividedBy(3).getWeeks());
        assertEquals(3, test.dividedBy(4).getWeeks());
        assertEquals(2, test.dividedBy(5).getWeeks());
        assertEquals(2, test.dividedBy(6).getWeeks());
        assertSame(test, test.dividedBy(1));
        
        try {
            Weeks.ONE.dividedBy(0);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testNegated
    public void testNegated() {
        Weeks test = Weeks.weeks(12);
        assertEquals(-12, test.negated().getWeeks());
        assertEquals(12, test.getWeeks());
        
        try {
            Weeks.MIN_VALUE.negated();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestWeeks::testAddToLocalDate
    public void testAddToLocalDate() {
        Weeks test = Weeks.weeks(3);
        LocalDate date = new LocalDate(2006, 6, 1);
        LocalDate expected = new LocalDate(2006, 6, 22);
        assertEquals(expected, date.plus(test));
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
