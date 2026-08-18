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

// org.joda.time.TestInterval_Basics::testOverlap_RInterval
    public void testOverlap_RInterval() {
        assertEquals(null, interval37.overlap(new Interval(1, 2)));  
        assertEquals(null, interval37.overlap(new Interval(2, 2)));  
        
        assertEquals(null, interval37.overlap(new Interval(2, 3)));  
        assertEquals(null, interval37.overlap(new Interval(3, 3)));  
        
        assertEquals(new Interval(3, 4), interval37.overlap(new Interval(2, 4)));  
        assertEquals(new Interval(3, 4), interval37.overlap(new Interval(3, 4)));
        assertEquals(new Interval(4, 4), interval37.overlap(new Interval(4, 4)));
        
        assertEquals(new Interval(3, 7), interval37.overlap(new Interval(2, 7)));  
        assertEquals(new Interval(3, 7), interval37.overlap(new Interval(3, 7)));
        assertEquals(new Interval(4, 7), interval37.overlap(new Interval(4, 7)));
        assertEquals(new Interval(5, 7), interval37.overlap(new Interval(5, 7)));
        assertEquals(new Interval(6, 7), interval37.overlap(new Interval(6, 7)));
        assertEquals(null, interval37.overlap(new Interval(7, 7)));  
        
        assertEquals(new Interval(3, 7), interval37.overlap(new Interval(2, 8)));  
        assertEquals(new Interval(3, 7), interval37.overlap(new Interval(3, 8)));  
        assertEquals(new Interval(4, 7), interval37.overlap(new Interval(4, 8)));  
        assertEquals(new Interval(5, 7), interval37.overlap(new Interval(5, 8)));  
        assertEquals(new Interval(6, 7), interval37.overlap(new Interval(6, 8)));  
        assertEquals(null, interval37.overlap(new Interval(7, 8)));  
        assertEquals(null, interval37.overlap(new Interval(8, 8)));  
    }

// org.joda.time.TestInterval_Basics::testOverlap_RInterval_null
    public void testOverlap_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(null, interval37.overlap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(null, interval37.overlap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(new Interval(4, 4), interval37.overlap((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(new Interval(6, 6), interval37.overlap((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(null, interval37.overlap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(null, interval37.overlap((ReadableInterval) null));  
        
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(null, interval33.overlap((ReadableInterval) null));  
    }

// org.joda.time.TestInterval_Basics::testOverlap_RInterval_zone
    public void testOverlap_RInterval_zone() {
        Interval testA = new Interval(new DateTime(3, LONDON), new DateTime(7, LONDON));
        assertEquals(ISOChronology.getInstance(LONDON), testA.getChronology());
        
        Interval testB = new Interval(new DateTime(4, MOSCOW), new DateTime(8, MOSCOW));
        assertEquals(ISOChronology.getInstance(MOSCOW), testB.getChronology());
        
        Interval resultAB = testA.overlap(testB);
        assertEquals(ISOChronology.getInstance(LONDON), resultAB.getChronology());
        
        Interval resultBA = testB.overlap(testA);
        assertEquals(ISOChronology.getInstance(MOSCOW), resultBA.getChronology());
    }

// org.joda.time.TestInterval_Basics::testOverlap_RInterval_zoneUTC
    public void testOverlap_RInterval_zoneUTC() {
        Interval testA = new Interval(new Instant(3), new Instant(7));
        assertEquals(ISOChronology.getInstanceUTC(), testA.getChronology());
        
        Interval testB = new Interval(new Instant(4), new Instant(8));
        assertEquals(ISOChronology.getInstanceUTC(), testB.getChronology());
        
        Interval result = testA.overlap(testB);
        assertEquals(ISOChronology.getInstanceUTC(), result.getChronology());
    }

// org.joda.time.TestInterval_Basics::testGap_RInterval
    public void testGap_RInterval() {
        assertEquals(new Interval(1, 3), interval37.gap(new Interval(0, 1)));
        assertEquals(new Interval(1, 3), interval37.gap(new Interval(1, 1)));
        
        assertEquals(null, interval37.gap(new Interval(2, 3)));  
        assertEquals(null, interval37.gap(new Interval(3, 3)));  
        
        assertEquals(null, interval37.gap(new Interval(4, 6)));  
        
        assertEquals(null, interval37.gap(new Interval(3, 7)));  
        assertEquals(null, interval37.gap(new Interval(6, 7)));  
        assertEquals(null, interval37.gap(new Interval(7, 7)));  
        
        assertEquals(null, interval37.gap(new Interval(6, 8)));  
        assertEquals(null, interval37.gap(new Interval(7, 8)));  
        assertEquals(new Interval(7, 8), interval37.gap(new Interval(8, 8)));
        
        assertEquals(null, interval37.gap(new Interval(6, 9)));  
        assertEquals(null, interval37.gap(new Interval(7, 9)));  
        assertEquals(new Interval(7, 8), interval37.gap(new Interval(8, 9)));
        assertEquals(new Interval(7, 9), interval37.gap(new Interval(9, 9)));
    }

// org.joda.time.TestInterval_Basics::testGap_RInterval_null
    public void testGap_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(new Interval(2, 3),  interval37.gap((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(null,  interval37.gap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(null,  interval37.gap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(null,  interval37.gap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(null,  interval37.gap((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(new Interval(7, 8),  interval37.gap((ReadableInterval) null));
    }

// org.joda.time.TestInterval_Basics::testGap_RInterval_zone
    public void testGap_RInterval_zone() {
        Interval testA = new Interval(new DateTime(3, LONDON), new DateTime(7, LONDON));
        assertEquals(ISOChronology.getInstance(LONDON), testA.getChronology());
        
        Interval testB = new Interval(new DateTime(1, MOSCOW), new DateTime(2, MOSCOW));
        assertEquals(ISOChronology.getInstance(MOSCOW), testB.getChronology());
        
        Interval resultAB = testA.gap(testB);
        assertEquals(ISOChronology.getInstance(LONDON), resultAB.getChronology());
        
        Interval resultBA = testB.gap(testA);
        assertEquals(ISOChronology.getInstance(MOSCOW), resultBA.getChronology());
    }

// org.joda.time.TestInterval_Basics::testGap_RInterval_zoneUTC
    public void testGap_RInterval_zoneUTC() {
        Interval testA = new Interval(new Instant(3), new Instant(7));
        assertEquals(ISOChronology.getInstanceUTC(), testA.getChronology());
        
        Interval testB = new Interval(new Instant(1), new Instant(2));
        assertEquals(ISOChronology.getInstanceUTC(), testB.getChronology());
        
        Interval result = testA.gap(testB);
        assertEquals(ISOChronology.getInstanceUTC(), result.getChronology());
    }

// org.joda.time.TestInterval_Basics::testAbuts_RInterval
    public void testAbuts_RInterval() {
        assertEquals(false, interval37.abuts(new Interval(1, 2)));  
        assertEquals(false, interval37.abuts(new Interval(2, 2)));  
        
        assertEquals(true,  interval37.abuts(new Interval(2, 3)));
        assertEquals(true,  interval37.abuts(new Interval(3, 3)));
        
        assertEquals(false, interval37.abuts(new Interval(2, 4)));  
        assertEquals(false, interval37.abuts(new Interval(3, 4)));  
        assertEquals(false, interval37.abuts(new Interval(4, 4)));  
        
        assertEquals(false, interval37.abuts(new Interval(2, 6)));  
        assertEquals(false, interval37.abuts(new Interval(3, 6)));  
        assertEquals(false, interval37.abuts(new Interval(4, 6)));  
        assertEquals(false, interval37.abuts(new Interval(5, 6)));  
        assertEquals(false, interval37.abuts(new Interval(6, 6)));  
        
        assertEquals(false, interval37.abuts(new Interval(2, 7)));  
        assertEquals(false, interval37.abuts(new Interval(3, 7)));  
        assertEquals(false, interval37.abuts(new Interval(4, 7)));  
        assertEquals(false, interval37.abuts(new Interval(5, 7)));  
        assertEquals(false, interval37.abuts(new Interval(6, 7)));  
        assertEquals(true,  interval37.abuts(new Interval(7, 7)));
        
        assertEquals(false, interval37.abuts(new Interval(2, 8)));  
        assertEquals(false, interval37.abuts(new Interval(3, 8)));  
        assertEquals(false, interval37.abuts(new Interval(4, 8)));  
        assertEquals(false, interval37.abuts(new Interval(5, 8)));  
        assertEquals(false, interval37.abuts(new Interval(6, 8)));  
        assertEquals(true,  interval37.abuts(new Interval(7, 8)));
        assertEquals(false, interval37.abuts(new Interval(8, 8)));  
        
        assertEquals(false, interval37.abuts(new Interval(8, 9)));  
        assertEquals(false, interval37.abuts(new Interval(9, 9)));  
    }

// org.joda.time.TestInterval_Basics::testAbuts_RInterval_null
    public void testAbuts_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false,  interval37.abuts((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(true,  interval37.abuts((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false,  interval37.abuts((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false,  interval37.abuts((ReadableInterval) null));  
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(true,  interval37.abuts((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false,  interval37.abuts((ReadableInterval) null));  
    }

// org.joda.time.TestInterval_Basics::testIsBefore_long
    public void testIsBefore_long() {
        assertEquals(false, interval37.isBefore(2));
        assertEquals(false, interval37.isBefore(3));
        assertEquals(false, interval37.isBefore(4));
        assertEquals(false, interval37.isBefore(5));
        assertEquals(false, interval37.isBefore(6));
        assertEquals(true,  interval37.isBefore(7));
        assertEquals(true,  interval37.isBefore(8));
    }

// org.joda.time.TestInterval_Basics::testIsBeforeNow
    public void testIsBeforeNow() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(true, interval37.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(true, interval37.isBeforeNow());
    }

// org.joda.time.TestInterval_Basics::testIsBefore_RI
    public void testIsBefore_RI() {
        assertEquals(false, interval37.isBefore(new Instant(2)));
        assertEquals(false, interval37.isBefore(new Instant(3)));
        assertEquals(false, interval37.isBefore(new Instant(4)));
        assertEquals(false, interval37.isBefore(new Instant(5)));
        assertEquals(false, interval37.isBefore(new Instant(6)));
        assertEquals(true,  interval37.isBefore(new Instant(7)));
        assertEquals(true,  interval37.isBefore(new Instant(8)));
    }

// org.joda.time.TestInterval_Basics::testIsBefore_RI_null
    public void testIsBefore_RI_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.isBefore((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.isBefore((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isBefore((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isBefore((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(true, interval37.isBefore((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(true, interval37.isBefore((ReadableInstant) null));
    }

// org.joda.time.TestInterval_Basics::testIsBefore_RInterval
    public void testIsBefore_RInterval() {
        assertEquals(false, interval37.isBefore(new Interval(Long.MIN_VALUE, 2)));
        assertEquals(false, interval37.isBefore(new Interval(Long.MIN_VALUE, 3)));
        assertEquals(false, interval37.isBefore(new Interval(Long.MIN_VALUE, 4)));
        
        assertEquals(false, interval37.isBefore(new Interval(6, Long.MAX_VALUE)));
        assertEquals(true, interval37.isBefore(new Interval(7, Long.MAX_VALUE)));
        assertEquals(true, interval37.isBefore(new Interval(8, Long.MAX_VALUE)));
    }

// org.joda.time.TestInterval_Basics::testIsBefore_RInterval_null
    public void testIsBefore_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(false, interval37.isBefore((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.isBefore((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isBefore((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isBefore((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(true, interval37.isBefore((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(true, interval37.isBefore((ReadableInterval) null));
    }

// org.joda.time.TestInterval_Basics::testIsAfter_long
    public void testIsAfter_long() {
        assertEquals(true,  interval37.isAfter(2));
        assertEquals(false, interval37.isAfter(3));
        assertEquals(false, interval37.isAfter(4));
        assertEquals(false, interval37.isAfter(5));
        assertEquals(false, interval37.isAfter(6));
        assertEquals(false, interval37.isAfter(7));
        assertEquals(false, interval37.isAfter(8));
    }

// org.joda.time.TestInterval_Basics::testIsAfterNow
    public void testIsAfterNow() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(true, interval37.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.isAfterNow());
    }

// org.joda.time.TestInterval_Basics::testIsAfter_RI
    public void testIsAfter_RI() {
        assertEquals(true,  interval37.isAfter(new Instant(2)));
        assertEquals(false, interval37.isAfter(new Instant(3)));
        assertEquals(false, interval37.isAfter(new Instant(4)));
        assertEquals(false, interval37.isAfter(new Instant(5)));
        assertEquals(false, interval37.isAfter(new Instant(6)));
        assertEquals(false, interval37.isAfter(new Instant(7)));
        assertEquals(false, interval37.isAfter(new Instant(8)));
    }

// org.joda.time.TestInterval_Basics::testIsAfter_RI_null
    public void testIsAfter_RI_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(true, interval37.isAfter((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(false, interval37.isAfter((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isAfter((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isAfter((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.isAfter((ReadableInstant) null));
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.isAfter((ReadableInstant) null));
    }

// org.joda.time.TestInterval_Basics::testIsAfter_RInterval
    public void testIsAfter_RInterval() {
        assertEquals(true, interval37.isAfter(new Interval(Long.MIN_VALUE, 2)));
        assertEquals(true, interval37.isAfter(new Interval(Long.MIN_VALUE, 3)));
        assertEquals(false, interval37.isAfter(new Interval(Long.MIN_VALUE, 4)));
        
        assertEquals(false, interval37.isAfter(new Interval(6, Long.MAX_VALUE)));
        assertEquals(false, interval37.isAfter(new Interval(7, Long.MAX_VALUE)));
        assertEquals(false, interval37.isAfter(new Interval(8, Long.MAX_VALUE)));
    }

// org.joda.time.TestInterval_Basics::testIsAfter_RInterval_null
    public void testIsAfter_RInterval_null() {
        DateTimeUtils.setCurrentMillisFixed(2);
        assertEquals(true, interval37.isAfter((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(3);
        assertEquals(true, interval37.isAfter((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(4);
        assertEquals(false, interval37.isAfter((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(6);
        assertEquals(false, interval37.isAfter((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(7);
        assertEquals(false, interval37.isAfter((ReadableInterval) null));
        DateTimeUtils.setCurrentMillisFixed(8);
        assertEquals(false, interval37.isAfter((ReadableInterval) null));
    }

// org.joda.time.TestInterval_Basics::testToInterval1
    public void testToInterval1() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval result = test.toInterval();
        assertSame(test, result);
    }

// org.joda.time.TestInterval_Basics::testToMutableInterval1
    public void testToMutableInterval1() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        MutableInterval result = test.toMutableInterval();
        assertEquals(test, result);
    }

// org.joda.time.TestInterval_Basics::testToPeriod
    public void testToPeriod() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, COPTIC_PARIS);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, COPTIC_PARIS);
        Interval base = new Interval(dt1, dt2);
        
        Period test = base.toPeriod();
        Period expected = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(expected, test);
    }

// org.joda.time.TestInterval_Basics::testToPeriod_PeriodType1
    public void testToPeriod_PeriodType1() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, COPTIC_PARIS);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, COPTIC_PARIS);
        Interval base = new Interval(dt1, dt2);
        
        Period test = base.toPeriod(null);
        Period expected = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(expected, test);
    }

// org.joda.time.TestInterval_Basics::testToPeriod_PeriodType2
    public void testToPeriod_PeriodType2() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18);
        Interval base = new Interval(dt1, dt2);
        
        Period test = base.toPeriod(PeriodType.yearWeekDayTime());
        Period expected = new Period(dt1, dt2, PeriodType.yearWeekDayTime());
        assertEquals(expected, test);
    }

// org.joda.time.TestInterval_Basics::testSerialization
    public void testSerialization() throws Exception {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Interval result = (Interval) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestInterval_Basics::testToString
    public void testToString() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, DateTimeZone.UTC);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, DateTimeZone.UTC);
        Interval test = new Interval(dt1, dt2);
        assertEquals("2004-06-09T07:08:09.010Z/2005-08-13T12:14:16.018Z", test.toString());
    }

// org.joda.time.TestInterval_Basics::testToString_reparse
    public void testToString_reparse() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, DateTimeZone.getDefault());
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, DateTimeZone.getDefault());
        Interval test = new Interval(dt1, dt2);
        assertEquals(test, new Interval(test.toString()));
    }

// org.joda.time.TestInterval_Basics::testWithChronology1
    public void testWithChronology1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withChronology(BuddhistChronology.getInstance());
        assertEquals(new Interval(TEST_TIME1, TEST_TIME2, BuddhistChronology.getInstance()), test);
    }

// org.joda.time.TestInterval_Basics::testWithChronology2
    public void testWithChronology2() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withChronology(null);
        assertEquals(new Interval(TEST_TIME1, TEST_TIME2, ISOChronology.getInstance()), test);
    }

// org.joda.time.TestInterval_Basics::testWithChronology3
    public void testWithChronology3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withChronology(COPTIC_PARIS);
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithStartMillis_long1
    public void testWithStartMillis_long1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withStartMillis(TEST_TIME1 - 1);
        assertEquals(new Interval(TEST_TIME1 - 1, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithStartMillis_long2
    public void testWithStartMillis_long2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        try {
            test.withStartMillis(TEST_TIME2 + 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithStartMillis_long3
    public void testWithStartMillis_long3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withStartMillis(TEST_TIME1);
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithStartInstant_RI1
    public void testWithStartInstant_RI1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withStart(new Instant(TEST_TIME1 - 1));
        assertEquals(new Interval(TEST_TIME1 - 1, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithStartInstant_RI2
    public void testWithStartInstant_RI2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        try {
            test.withStart(new Instant(TEST_TIME2 + 1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithStartInstant_RI3
    public void testWithStartInstant_RI3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withStart(null);
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithEndMillis_long1
    public void testWithEndMillis_long1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEndMillis(TEST_TIME2 - 1);
        assertEquals(new Interval(TEST_TIME1, TEST_TIME2 - 1, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithEndMillis_long2
    public void testWithEndMillis_long2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        try {
            test.withEndMillis(TEST_TIME1 - 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithEndMillis_long3
    public void testWithEndMillis_long3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEndMillis(TEST_TIME2);
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithEndInstant_RI1
    public void testWithEndInstant_RI1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEnd(new Instant(TEST_TIME2 - 1));
        assertEquals(new Interval(TEST_TIME1, TEST_TIME2 - 1, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithEndInstant_RI2
    public void testWithEndInstant_RI2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        try {
            test.withEnd(new Instant(TEST_TIME1 - 1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithEndInstant_RI3
    public void testWithEndInstant_RI3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEnd(null);
        assertEquals(new Interval(TEST_TIME1, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart1
    public void testWithDurationAfterStart1() throws Throwable {
        Duration dur = new Duration(TEST_TIME2 - TEST_TIME_NOW);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS);
        Interval test = base.withDurationAfterStart(dur);
        
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart2
    public void testWithDurationAfterStart2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationAfterStart(null);
        
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart3
    public void testWithDurationAfterStart3() throws Throwable {
        Duration dur = new Duration(-1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withDurationAfterStart(dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart4
    public void testWithDurationAfterStart4() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationAfterStart(base.toDuration());
        
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd1
    public void testWithDurationBeforeEnd1() throws Throwable {
        Duration dur = new Duration(TEST_TIME_NOW - TEST_TIME1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS);
        Interval test = base.withDurationBeforeEnd(dur);
        
        assertEquals(new Interval(TEST_TIME1, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd2
    public void testWithDurationBeforeEnd2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationBeforeEnd(null);
        
        assertEquals(new Interval(TEST_TIME2, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd3
    public void testWithDurationBeforeEnd3() throws Throwable {
        Duration dur = new Duration(-1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withDurationBeforeEnd(dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd4
    public void testWithDurationBeforeEnd4() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationBeforeEnd(base.toDuration());
        
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodAfterStart1
    public void testWithPeriodAfterStart1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, COPTIC_PARIS);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        
        Interval base = new Interval(dt, dt);
        Interval test = base.withPeriodAfterStart(dur);
        assertEquals(new Interval(dt, dur), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodAfterStart2
    public void testWithPeriodAfterStart2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withPeriodAfterStart(null);
        
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodAfterStart3
    public void testWithPeriodAfterStart3() throws Throwable {
        Period per = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withPeriodAfterStart(per);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithPeriodBeforeEnd1
    public void testWithPeriodBeforeEnd1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, COPTIC_PARIS);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        
        Interval base = new Interval(dt, dt);
        Interval test = base.withPeriodBeforeEnd(dur);
        assertEquals(new Interval(dur, dt), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodBeforeEnd2
    public void testWithPeriodBeforeEnd2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withPeriodBeforeEnd(null);
        
        assertEquals(new Interval(TEST_TIME2, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodBeforeEnd3
    public void testWithPeriodBeforeEnd3() throws Throwable {
        Period per = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withPeriodBeforeEnd(per);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40);
        assertEquals(1970, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(2, test.get(DateTimeFieldType.dayOfWeek()));
        assertEquals(160, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(24, test.get(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(1970, test.get(DateTimeFieldType.weekyear()));
        assertEquals(10, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(30, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(40, test.get(DateTimeFieldType.millisOfSecond()));
        assertEquals(MILLIS_OF_DAY_UTC / 60000 , test.get(DateTimeFieldType.minuteOfDay()));
        assertEquals(MILLIS_OF_DAY_UTC / 1000 , test.get(DateTimeFieldType.secondOfDay()));
        assertEquals(MILLIS_OF_DAY_UTC , test.get(DateTimeFieldType.millisOfDay()));
        assertEquals(10, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(DateTimeConstants.AM, test.get(DateTimeFieldType.halfdayOfDay()));
        
        test = new LocalDateTime(1970, 6, 9, 12, 30);
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.PM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalDateTime(1970, 6, 9, 14, 30);
        assertEquals(2, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(2, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(14, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.PM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalDateTime(1970, 6, 9, 0, 30);
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(24, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.AM, test.get(DateTimeFieldType.halfdayOfDay()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testSize
    public void testSize() {
        LocalDateTime test = new LocalDateTime();
        assertEquals(4, test.size());
    }

// org.joda.time.TestLocalDateTime_Basics::testGetFieldType_int
    public void testGetFieldType_int() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        assertSame(DateTimeFieldType.year(), test.getFieldType(0));
        assertSame(DateTimeFieldType.monthOfYear(), test.getFieldType(1));
        assertSame(DateTimeFieldType.dayOfMonth(), test.getFieldType(2));
        assertSame(DateTimeFieldType.millisOfDay(), test.getFieldType(3));
        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.year(), fields[0]);
        assertSame(DateTimeFieldType.monthOfYear(), fields[1]);
        assertSame(DateTimeFieldType.dayOfMonth(), fields[2]);
        assertSame(DateTimeFieldType.millisOfDay(), fields[3]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestLocalDateTime_Basics::testGetField_int
    public void testGetField_int() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        assertSame(COPTIC_UTC.year(), test.getField(0));
        assertSame(COPTIC_UTC.monthOfYear(), test.getField(1));
        assertSame(COPTIC_UTC.dayOfMonth(), test.getField(2));
        assertSame(COPTIC_UTC.millisOfDay(), test.getField(3));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGetFields
    public void testGetFields() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertSame(COPTIC_UTC.year(), fields[0]);
        assertSame(COPTIC_UTC.monthOfYear(), fields[1]);
        assertSame(COPTIC_UTC.dayOfMonth(), fields[2]);
        assertSame(COPTIC_UTC.millisOfDay(), fields[3]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestLocalDateTime_Basics::testGetValue_int
    public void testGetValue_int() {
        LocalDateTime test = new LocalDateTime(ISO_UTC);
        assertEquals(1970, test.getValue(0));
        assertEquals(6, test.getValue(1));
        assertEquals(9, test.getValue(2));
        assertEquals(MILLIS_OF_DAY_UTC, test.getValue(3));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGetValues
    public void testGetValues() {
        LocalDateTime test = new LocalDateTime(ISO_UTC);
        int[] values = test.getValues();
        assertEquals(1970, values[0]);
        assertEquals(6, values[1]);
        assertEquals(9, values[2]);
        assertEquals(MILLIS_OF_DAY_UTC, values[3]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestLocalDateTime_Basics::testIsSupported_DateTimeFieldType
    public void testIsSupported_DateTimeFieldType() {
        LocalDateTime test = new LocalDateTime();
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
        
        assertEquals(false, test.isSupported((DateTimeFieldType) null));
    }

// org.joda.time.TestLocalDateTime_Basics::testIsSupported_DurationFieldType
    public void testIsSupported_DurationFieldType() {
        LocalDateTime test = new LocalDateTime();
        assertEquals(false, test.isSupported(DurationFieldType.eras()));
        assertEquals(true, test.isSupported(DurationFieldType.centuries()));
        assertEquals(true, test.isSupported(DurationFieldType.years()));
        assertEquals(true, test.isSupported(DurationFieldType.months()));
        assertEquals(true, test.isSupported(DurationFieldType.weekyears()));
        assertEquals(true, test.isSupported(DurationFieldType.weeks()));
        assertEquals(true, test.isSupported(DurationFieldType.days()));
        
        assertEquals(true, test.isSupported(DurationFieldType.hours()));
        assertEquals(true, test.isSupported(DurationFieldType.minutes()));
        assertEquals(true, test.isSupported(DurationFieldType.seconds()));
        assertEquals(true, test.isSupported(DurationFieldType.millis()));
        assertEquals(true, test.isSupported(DurationFieldType.halfdays()));
        
        assertEquals(false, test.isSupported((DurationFieldType) null));
    }

// org.joda.time.TestLocalDateTime_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        LocalDateTime test1 = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        LocalDateTime test2 = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        LocalDateTime test3 = new LocalDateTime(1971, 6, 9, 10, 20, 30, 40);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        Partial partial = new Partial(
                new DateTimeFieldType[] {
                        DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(),
                        DateTimeFieldType.dayOfMonth(), DateTimeFieldType.millisOfDay()},
                new int[] {1970, 6, 9, MILLIS_OF_DAY_UTC}, COPTIC_PARIS);
        assertEquals(true, test1.equals(partial));
        assertEquals(true, test1.hashCode() == partial.hashCode());
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestLocalDateTime_Basics::testCompareTo
    public void testCompareTo() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.millisOfDay(),
        };
        int[] values = new int[] {2005, 6, 2, MILLIS_OF_DAY_UTC};
        Partial p = new Partial(types, values);
        assertEquals(0, test1.compareTo(p));
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

        try {
            test1.compareTo(new YearMonthDay());
            fail();
        } catch (ClassCastException ex) {}
        try {
            test1.compareTo(new TimeOfDay());
            fail();
        } catch (ClassCastException ex) {}
        Partial partial = new Partial()
            .with(DateTimeFieldType.centuryOfEra(), 1)
            .with(DateTimeFieldType.halfdayOfDay(), 0)
            .with(DateTimeFieldType.dayOfMonth(), 9);
        try {
            new LocalDateTime(1970, 6, 9, 10, 20, 30, 40).compareTo(partial);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testIsEqual_LocalDateTime
    public void testIsEqual_LocalDateTime() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new LocalDateTime(2005, 7, 2, 10, 20, 30, 40).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testIsBefore_LocalDateTime
    public void testIsBefore_LocalDateTime() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new LocalDateTime(2005, 7, 2, 10, 20, 30, 40).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testIsAfter_LocalDateTime
    public void testIsAfter_LocalDateTime() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new LocalDateTime(2005, 7, 2, 10, 20, 30, 40).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithDate
    public void testWithDate() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withDate(2006, 2, 1);
        
        check(test, 2004, 6, 9, 10, 20, 30, 40);
        check(result, 2006, 2, 1, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithTime
    public void testWithTime() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withTime(9, 8, 7, 6);
        
        check(test, 2004, 6, 9, 10, 20, 30, 40);
        check(result, 2004, 6, 9, 9, 8, 7, 6);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithField_DateTimeFieldType_int_1
    public void testWithField_DateTimeFieldType_int_1() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 40), test);
        assertEquals(new LocalDateTime(2006, 6, 9, 10, 20, 30, 40), result);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithField_DateTimeFieldType_int_2
    public void testWithField_DateTimeFieldType_int_2() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithField_DateTimeFieldType_int_3
    public void testWithField_DateTimeFieldType_int_3() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withField(DateTimeFieldType.year(), 2004);
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 40), test);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_1
    public void testWithFieldAdded_DurationFieldType_int_1() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 40), test);
        assertEquals(new LocalDateTime(2010, 6, 9, 10, 20, 30, 40), result);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_2
    public void testWithFieldAdded_DurationFieldType_int_2() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_3
    public void testWithFieldAdded_DurationFieldType_int_3() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_4
    public void testWithFieldAdded_DurationFieldType_int_4() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlus_RP
    public void testPlus_RP() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plus(new Period(1, 2, 3, 4, 29, 6, 7, 8));
        LocalDateTime expected = new LocalDateTime(2003, 7, 29, 15, 26, 37, 48, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusYears_int
    public void testPlusYears_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusYears(1);
        LocalDateTime expected = new LocalDateTime(2003, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusMonths(1);
        LocalDateTime expected = new LocalDateTime(2002, 6, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusWeeks_int
    public void testPlusWeeks_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusWeeks(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 10, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusDays_int
    public void testPlusDays_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusDays(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 4, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusHours_int
    public void testPlusHours_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusHours(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 11, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusMinutes_int
    public void testPlusMinutes_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusMinutes(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 21, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusSeconds_int
    public void testPlusSeconds_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusSeconds(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 31, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusMillis_int
    public void testPlusMillis_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusMillis(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 30, 41, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinus_RP
    public void testMinus_RP() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        
        LocalDateTime expected = new LocalDateTime(2001, 3, 26, 9, 19, 29, 39, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusYears_int
    public void testMinusYears_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusYears(1);
        LocalDateTime expected = new LocalDateTime(2001, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusMonths(1);
        LocalDateTime expected = new LocalDateTime(2002, 4, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusWeeks_int
    public void testMinusWeeks_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusWeeks(1);
        LocalDateTime expected = new LocalDateTime(2002, 4, 26, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusDays_int
    public void testMinusDays_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusDays(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 2, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusHours_int
    public void testMinusHours_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusHours(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 9, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusMinutes_int
    public void testMinusMinutes_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusMinutes(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 19, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusSeconds_int
    public void testMinusSeconds_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusSeconds(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 29, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusMillis_int
    public void testMinusMillis_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusMillis(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 30, 39, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testGetters
    public void testGetters() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, GJ_UTC);
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
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(MILLIS_OF_DAY_UTC, test.getMillisOfDay());
    }

// org.joda.time.TestLocalDateTime_Basics::testWithers
    public void testWithers() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, GJ_UTC);
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

// org.joda.time.TestLocalDateTime_Basics::testToDateTime
    public void testToDateTime() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        
        DateTime test = base.toDateTime();
        check(base, 2005, 6, 9, 6, 7, 8, 9);
        DateTime expected = new DateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_Zone
    public void testToDateTime_Zone() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        
        DateTime test = base.toDateTime(TOKYO);
        check(base, 2005, 6, 9, 6, 7, 8, 9);
        DateTime expected = new DateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_nullZone
    public void testToDateTime_nullZone() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        
        DateTime test = base.toDateTime((DateTimeZone) null);
        check(base, 2005, 6, 9, 6, 7, 8, 9);
        DateTime expected = new DateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToLocalDate
    public void testToLocalDate() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        LocalDate expected = new LocalDate(2005, 6, 9, COPTIC_LONDON);
        assertEquals(expected,base.toLocalDate());
    }

// org.joda.time.TestLocalDateTime_Basics::testToLocalTime
    public void testToLocalTime() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        LocalTime expected = new LocalTime(6, 7, 8, 9, COPTIC_LONDON);
        assertEquals(expected,base.toLocalTime());
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7, BUDDHIST_TOKYO);
        
        DateTime test = base.toDateTime(dt);
        check(base, 2005, 6, 9, 10, 20, 30, 40);
        DateTime expected = new DateTime(2005, 6, 9, 10, 20, 30, 40, BUDDHIST_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 2005, 6, 9, 10, 20, 30, 40);
        DateTime expected = new DateTime(2005, 6, 9, 10, 20, 30, 40, ISO_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_summer
    public void testToDate_summer() {
        LocalDateTime base = new LocalDateTime(2005, 7, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        Date test = base.toDate();
        check(base, 2005, 7, 9, 10, 20, 30, 40);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JULY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        gcal.set(Calendar.HOUR_OF_DAY, 10);
        gcal.set(Calendar.MINUTE, 20);
        gcal.set(Calendar.SECOND, 30);
        gcal.set(Calendar.MILLISECOND, 40);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_winter
    public void testToDate_winter() {
        LocalDateTime base = new LocalDateTime(2005, 1, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        Date test = base.toDate();
        check(base, 2005, 1, 9, 10, 20, 30, 40);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JANUARY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        gcal.set(Calendar.HOUR_OF_DAY, 10);
        gcal.set(Calendar.MINUTE, 20);
        gcal.set(Calendar.SECOND, 30);
        gcal.set(Calendar.MILLISECOND, 40);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_springDST
    public void testToDate_springDST() {
        LocalDateTime base = new LocalDateTime(2007, 4, 2, 0, 20, 0, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 4, 2, 0, 20, 0, 0);
            assertEquals("Mon Apr 02 01:00:00 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_springDST_2Hour40Savings
    public void testToDate_springDST_2Hour40Savings() {
        LocalDateTime base = new LocalDateTime(2007, 4, 2, 0, 20, 0, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000, (3600000 / 6) * 16);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 4, 2, 0, 20, 0, 0);
            assertEquals("Mon Apr 02 02:40:00 GMT+03:40 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_autumnDST
    public void testToDate_autumnDST() {
        LocalDateTime base = new LocalDateTime(2007, 10, 2, 0, 20, 30, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 10, 2, 0, 20, 30, 0);
            assertEquals("Tue Oct 02 00:20:30 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testProperty
    public void testProperty() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, GJ_UTC);
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
        assertEquals(test.hourOfDay(), test.property(DateTimeFieldType.hourOfDay()));
        assertEquals(test.minuteOfHour(), test.property(DateTimeFieldType.minuteOfHour()));
        assertEquals(test.secondOfMinute(), test.property(DateTimeFieldType.secondOfMinute()));
        assertEquals(test.millisOfSecond(), test.property(DateTimeFieldType.millisOfSecond()));
        assertEquals(test.millisOfDay(), test.property(DateTimeFieldType.millisOfDay()));
        
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(test, test.property(DateTimeFieldType.minuteOfDay()).getLocalDateTime());
    }

// org.joda.time.TestLocalDateTime_Basics::testSerialization
    public void testSerialization() throws Exception {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDateTime result = (LocalDateTime) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
        assertTrue(result.isSupported(DateTimeFieldType.dayOfMonth()));  
    }

// org.joda.time.TestLocalDateTime_Basics::testToString
    public void testToString() {
        LocalDateTime test = new LocalDateTime(2002, 6, 9, 10, 20, 30, 40);
        assertEquals("2002-06-09T10:20:30.040", test.toString());
    }

// org.joda.time.TestLocalDateTime_Basics::testToString_String
    public void testToString_String() {
        LocalDateTime test = new LocalDateTime(2002, 6, 9, 10, 20, 30, 40);
        assertEquals("2002 10", test.toString("yyyy HH"));
        assertEquals("2002-06-09T10:20:30.040", test.toString((String) null));
    }

// org.joda.time.TestLocalDateTime_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40);
        assertEquals("Tue 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("mar. 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("1970-06-09T10:20:30.040", test.toString(null, Locale.ENGLISH));
        assertEquals("Tue 9/6", test.toString("EEE d/M", null));
        assertEquals("1970-06-09T10:20:30.040", test.toString(null, null));
    }

// org.joda.time.TestLocalDateTime_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        LocalDateTime test = new LocalDateTime(2002, 6, 9, 10, 20, 30, 40);
        assertEquals("2002 10", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09T10:20:30.040", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestLocalDateTime_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new LocalDateTime(2010, 6, 30, 1, 20), LocalDateTime.parse("2010-06-30T01:20"));
        assertEquals(new LocalDateTime(2010, 1, 2, 14, 50, 30, 432), LocalDateTime.parse("2010-002T14:50:30.432"));
    }

// org.joda.time.TestLocalDateTime_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--dd MM HH").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new LocalDateTime(2010, 6, 30, 13, 0), LocalDateTime.parse("2010--30 06 13", f));
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_fromCalendarFields
    public void testFactory_fromCalendarFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(1970, 2, 3, 4, 5, 6, 7);
        assertEquals(expected, LocalDateTime.fromCalendarFields(cal));
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_fromCalendarFields_beforeYearZero1
    public void testFactory_fromCalendarFields_beforeYearZero1() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(0, 2, 3, 4, 5, 6, 7);
        assertEquals(expected, LocalDateTime.fromCalendarFields(cal));
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_fromCalendarFields_beforeYearZero3
    public void testFactory_fromCalendarFields_beforeYearZero3() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(3, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(-2, 2, 3, 4, 5, 6, 7);
        assertEquals(expected, LocalDateTime.fromCalendarFields(cal));
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_fromCalendarFields_null
    public void testFactory_fromCalendarFields_null() throws Exception {
        try {
            LocalDateTime.fromCalendarFields((Calendar) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_fromDateFields_after1970
    public void testFactory_fromDateFields_after1970() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(1970, 2, 3, 4, 5 ,6, 7);
        assertEquals(expected, LocalDateTime.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_fromDateFields_before1970
    public void testFactory_fromDateFields_before1970() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1969, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(1969, 2, 3, 4, 5 ,6, 7);
        assertEquals(expected, LocalDateTime.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_fromDateFields_beforeYearZero1
    public void testFactory_fromDateFields_beforeYearZero1() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(0, 2, 3, 4, 5, 6, 7);
        assertEquals(expected, LocalDateTime.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_fromDateFields_beforeYearZero3
    public void testFactory_fromDateFields_beforeYearZero3() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(3, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(-2, 2, 3, 4, 5, 6, 7);
        assertEquals(expected, LocalDateTime.fromDateFields(cal.getTime()));
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_fromDateFields_null
    public void testFactory_fromDateFields_null() throws Exception {
        try {
            LocalDateTime.fromDateFields((Date) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        LocalDateTime test = new LocalDateTime();
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(test, LocalDateTime.now());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        LocalDateTime test = new LocalDateTime(LONDON);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(8, test.getDayOfMonth());
        assertEquals(23, test.getHourOfDay());
        assertEquals(59, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        assertEquals(test, LocalDateTime.now(LONDON));
        
        test = new LocalDateTime(PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(59, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        assertEquals(test, LocalDateTime.now(PARIS));
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        LocalDateTime test = new LocalDateTime((DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        LocalDateTime test = new LocalDateTime(GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(test, LocalDateTime.now(GREGORIAN_PARIS));
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        LocalDateTime test = new LocalDateTime((Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME2);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
        assertEquals(14 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(28, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long1_DateTimeZone
    public void testConstructor_long1_DateTimeZone() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME1, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long2_DateTimeZone
    public void testConstructor_long2_DateTimeZone() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME2, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
        assertEquals(14 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(28, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long_nullDateTimeZone
    public void testConstructor_long_nullDateTimeZone() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME1, (DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME1, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
        assertEquals(14 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(28, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME1, (Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalDateTime test = new LocalDateTime(date);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        LocalDateTime test = new LocalDateTime((Object) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        LocalDateTime test = new LocalDateTime("1972-04-06");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectString2
    public void testConstructor_ObjectString2() throws Throwable {
        LocalDateTime test = new LocalDateTime("1972-037");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(2, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectString3
    public void testConstructor_ObjectString3() throws Throwable {
        LocalDateTime test = new LocalDateTime("1972-04-06T10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectString4
    public void testConstructor_ObjectString4() throws Throwable {
        LocalDateTime test = new LocalDateTime("1972-04-06T10:20");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new LocalDateTime("1970-04-06T+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new LocalDateTime("1970-04-06T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectStringEx3
    public void testConstructor_ObjectStringEx3() throws Throwable {
        try {
            new LocalDateTime("T10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectStringEx4
    public void testConstructor_ObjectStringEx4() throws Throwable {
        try {
            new LocalDateTime("T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectStringEx5
    public void testConstructor_ObjectStringEx5() throws Throwable {
        try {
            new LocalDateTime("10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectStringEx6
    public void testConstructor_ObjectStringEx6() throws Throwable {
        try {
            new LocalDateTime("10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectLocalDateTime
    public void testConstructor_ObjectLocalDateTime() throws Throwable {
        LocalDateTime dt = new LocalDateTime(1970, 5, 6, 10, 20, 30, 40, BUDDHIST_UTC);
        LocalDateTime test = new LocalDateTime(dt);
        assertEquals(BUDDHIST_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectLocalDate
    public void testConstructor_ObjectLocalDate() throws Throwable {
        LocalDate date = new LocalDate(1970, 5, 6);
        try {
            new LocalDateTime(date);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_ObjectLocalTime
    public void testConstructor_ObjectLocalTime() throws Throwable {
        LocalTime time = new LocalTime(10, 20, 30, 40);
        try {
            new LocalDateTime(time);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Object_DateTimeZone
    public void testConstructor_Object_DateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalDateTime test = new LocalDateTime(date, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Object_DateTimeZoneMoscow
    public void testConstructor_Object_DateTimeZoneMoscow() throws Throwable {
        LocalDateTime test = new LocalDateTime("1970-04-06T12:24:00", MOSCOW);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Object_DateTimeZoneMoscowBadDateTime
    public void testConstructor_Object_DateTimeZoneMoscowBadDateTime() throws Throwable {
        
        
        
        
        LocalDateTime test = new LocalDateTime("1981-04-01T00:30:00", MOSCOW);  
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1981, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(30, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_nullObject_DateTimeZone
    public void testConstructor_nullObject_DateTimeZone() throws Throwable {
        LocalDateTime test = new LocalDateTime((Object) null, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Object_nullDateTimeZone
    public void testConstructor_Object_nullDateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalDateTime test = new LocalDateTime(date, (DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_nullObject_nullDateTimeZone
    public void testConstructor_nullObject_nullDateTimeZone() throws Throwable {
        LocalDateTime test = new LocalDateTime((Object) null, (DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalDateTime test = new LocalDateTime(date, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Object_ChronologyMoscow
    public void testConstructor_Object_ChronologyMoscow() throws Throwable {
        LocalDateTime test = new LocalDateTime("1970-04-06T12:24:00", GREGORIAN_MOSCOW);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Object_ChronologyMoscowBadDateTime
    public void testConstructor_Object_ChronologyMoscowBadDateTime() throws Throwable {
        
        
        
        
        LocalDateTime test = new LocalDateTime("1981-04-01T00:30:00", GREGORIAN_MOSCOW);  
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1981, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(30, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        LocalDateTime test = new LocalDateTime((Object) null, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        LocalDateTime test = new LocalDateTime(date, (Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        LocalDateTime test = new LocalDateTime((Object) null, (Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_int_int_int_int_int
    public void testConstructor_int_int_int_int_int() throws Throwable {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_int_int_int_int_int_int
    public void testConstructor_int_int_int_int_int_int() throws Throwable {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_int_int_int_int_int_int_int
    public void testConstructor_int_int_int_int_int_int_int() throws Throwable {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        try {
            new LocalDateTime(Integer.MIN_VALUE, 6, 9, 10, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDateTime(Integer.MAX_VALUE, 6, 9, 10, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDateTime(2005, 0, 9, 10, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDateTime(2005, 13, 9, 10, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDateTime(2005, 6, 0, 10, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDateTime(2005, 6, 31, 10, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        new LocalDateTime(2005, 7, 31, 10, 20, 30, 40);
        try {
            new LocalDateTime(2005, 7, 32, 10, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_int_int_int_Chronology
    public void testConstructor_int_int_int_Chronology() throws Throwable {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());  
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        try {
            new LocalDateTime(Integer.MIN_VALUE, 6, 9, 10, 20, 30, 40, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDateTime(Integer.MAX_VALUE, 6, 9, 10, 20, 30, 40, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDateTime(2005, 0, 9, 10, 20, 30, 40, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDateTime(2005, 13, 9, 10, 20, 30, 40, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDateTime(2005, 6, 0, 10, 20, 30, 40, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LocalDateTime(2005, 6, 31, 10, 20, 30, 40, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        new LocalDateTime(2005, 7, 31, 10, 20, 30, 40, GREGORIAN_PARIS);
        try {
            new LocalDateTime(2005, 7, 32, 10, 20, 30, 40, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_int_int_int_nullChronology
    public void testConstructor_int_int_int_nullChronology() throws Throwable {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetYear
    public void testPropertyGetYear() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        assertSame(test.getChronology().year(), test.year().getField());
        assertEquals("year", test.year().getName());
        assertEquals("Property[year]", test.year().toString());
        assertSame(test, test.year().getLocalDateTime());
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

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetMaxMinValuesYear
    public void testPropertyGetMaxMinValuesYear() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        assertEquals(-292275054, test.year().getMinimumValue());
        assertEquals(-292275054, test.year().getMinimumValueOverall());
        assertEquals(292278993, test.year().getMaximumValue());
        assertEquals(292278993, test.year().getMaximumValueOverall());
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddToCopyYear
    public void testPropertyAddToCopyYear() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.year().addToCopy(9);
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1981, 6, 9, 10, 20, 30, 40);
        
        copy = test.year().addToCopy(0);
        check(copy, 1972, 6, 9, 10, 20, 30, 40);
        
        copy = test.year().addToCopy(292278993 - 1972);
        check(copy, 292278993, 6, 9, 10, 20, 30, 40);
        
        try {
            test.year().addToCopy(292278993 - 1972 + 1);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        
        copy = test.year().addToCopy(-1972);
        check(copy, 0, 6, 9, 10, 20, 30, 40);
        
        copy = test.year().addToCopy(-1973);
        check(copy, -1, 6, 9, 10, 20, 30, 40);
        
        try {
            test.year().addToCopy(-292275054 - 1972 - 1);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 1972, 6, 9, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddWrapFieldToCopyYear
    public void testPropertyAddWrapFieldToCopyYear() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.year().addWrapFieldToCopy(9);
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1981, 6, 9, 10, 20, 30, 40);
        
        copy = test.year().addWrapFieldToCopy(0);
        check(copy, 1972, 6, 9, 10, 20, 30, 40);
        
        copy = test.year().addWrapFieldToCopy(292278993 - 1972 + 1);
        check(copy, -292275054, 6, 9, 10, 20, 30, 40);
        
        copy = test.year().addWrapFieldToCopy(-292275054 - 1972 - 1);
        check(copy, 292278993, 6, 9, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetCopyYear
    public void testPropertySetCopyYear() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.year().setCopy(12);
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 12, 6, 9, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetCopyTextYear
    public void testPropertySetCopyTextYear() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.year().setCopy("12");
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 12, 6, 9, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyCompareToYear
    public void testPropertyCompareToYear() {
        LocalDateTime test1 = new LocalDateTime(TEST_TIME1);
        LocalDateTime test2 = new LocalDateTime(TEST_TIME2);
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

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetMonth
    public void testPropertyGetMonth() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        assertSame(test.getChronology().monthOfYear(), test.monthOfYear().getField());
        assertEquals("monthOfYear", test.monthOfYear().getName());
        assertEquals("Property[monthOfYear]", test.monthOfYear().toString());
        assertSame(test, test.monthOfYear().getLocalDateTime());
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
        test = new LocalDateTime(1972, 7, 9, 10, 20, 30, 40);
        assertEquals("juillet", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("juil.", test.monthOfYear().getAsShortText(Locale.FRENCH));
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetMaxMinValuesMonth
    public void testPropertyGetMaxMinValuesMonth() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        assertEquals(1, test.monthOfYear().getMinimumValue());
        assertEquals(1, test.monthOfYear().getMinimumValueOverall());
        assertEquals(12, test.monthOfYear().getMaximumValue());
        assertEquals(12, test.monthOfYear().getMaximumValueOverall());
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddToCopyMonth
    public void testPropertyAddToCopyMonth() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.monthOfYear().addToCopy(6);
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 12, 9, 10, 20, 30, 40);
        
        copy = test.monthOfYear().addToCopy(7);
        check(copy, 1973, 1, 9, 10, 20, 30, 40);
        
        copy = test.monthOfYear().addToCopy(-5);
        check(copy, 1972, 1, 9, 10, 20, 30, 40);
        
        copy = test.monthOfYear().addToCopy(-6);
        check(copy, 1971, 12, 9, 10, 20, 30, 40);
        
        test = new LocalDateTime(1972, 1, 31, 10, 20, 30, 40);
        copy = test.monthOfYear().addToCopy(1);
        check(copy, 1972, 2, 29, 10, 20, 30, 40);
        
        copy = test.monthOfYear().addToCopy(2);
        check(copy, 1972, 3, 31, 10, 20, 30, 40);
        
        copy = test.monthOfYear().addToCopy(3);
        check(copy, 1972, 4, 30, 10, 20, 30, 40);
        
        test = new LocalDateTime(1971, 1, 31, 10, 20, 30, 40);
        copy = test.monthOfYear().addToCopy(1);
        check(copy, 1971, 2, 28, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddWrapFieldToCopyMonth
    public void testPropertyAddWrapFieldToCopyMonth() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.monthOfYear().addWrapFieldToCopy(4);
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 10, 9, 10, 20, 30, 40);
        
        copy = test.monthOfYear().addWrapFieldToCopy(8);
        check(copy, 1972, 2, 9, 10, 20, 30, 40);
        
        copy = test.monthOfYear().addWrapFieldToCopy(-8);
        check(copy, 1972, 10, 9, 10, 20, 30, 40);
        
        test = new LocalDateTime(1972, 1, 31, 10, 20, 30, 40);
        copy = test.monthOfYear().addWrapFieldToCopy(1);
        check(copy, 1972, 2, 29, 10, 20, 30, 40);
        
        copy = test.monthOfYear().addWrapFieldToCopy(2);
        check(copy, 1972, 3, 31, 10, 20, 30, 40);
        
        copy = test.monthOfYear().addWrapFieldToCopy(3);
        check(copy, 1972, 4, 30, 10, 20, 30, 40);
        
        test = new LocalDateTime(1971, 1, 31, 10, 20, 30, 40);
        copy = test.monthOfYear().addWrapFieldToCopy(1);
        check(copy, 1971, 2, 28, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetCopyMonth
    public void testPropertySetCopyMonth() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.monthOfYear().setCopy(12);
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 12, 9, 10, 20, 30, 40);
        
        test = new LocalDateTime(1972, 1, 31, 10, 20, 30, 40);
        copy = test.monthOfYear().setCopy(2);
        check(copy, 1972, 2, 29, 10, 20, 30, 40);
        
        try {
            test.monthOfYear().setCopy(13);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.monthOfYear().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetCopyTextMonth
    public void testPropertySetCopyTextMonth() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.monthOfYear().setCopy("12");
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 12, 9, 10, 20, 30, 40);
        
        copy = test.monthOfYear().setCopy("December");
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 12, 9, 10, 20, 30, 40);
        
        copy = test.monthOfYear().setCopy("Dec");
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 12, 9, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyCompareToMonth
    public void testPropertyCompareToMonth() {
        LocalDateTime test1 = new LocalDateTime(TEST_TIME1);
        LocalDateTime test2 = new LocalDateTime(TEST_TIME2);
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

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetDay
    public void testPropertyGetDay() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        assertSame(test.getChronology().dayOfMonth(), test.dayOfMonth().getField());
        assertEquals("dayOfMonth", test.dayOfMonth().getName());
        assertEquals("Property[dayOfMonth]", test.dayOfMonth().toString());
        assertSame(test, test.dayOfMonth().getLocalDateTime());
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

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetMaxMinValuesDay
    public void testPropertyGetMaxMinValuesDay() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        assertEquals(1, test.dayOfMonth().getMinimumValue());
        assertEquals(1, test.dayOfMonth().getMinimumValueOverall());
        assertEquals(30, test.dayOfMonth().getMaximumValue());
        assertEquals(31, test.dayOfMonth().getMaximumValueOverall());
        test = new LocalDateTime(1972, 7, 9, 10, 20, 30, 40);
        assertEquals(31, test.dayOfMonth().getMaximumValue());
        test = new LocalDateTime(1972, 2, 9, 10, 20, 30, 40);
        assertEquals(29, test.dayOfMonth().getMaximumValue());
        test = new LocalDateTime(1971, 2, 9, 10, 20, 30, 40);
        assertEquals(28, test.dayOfMonth().getMaximumValue());
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddToCopyDay
    public void testPropertyAddToCopyDay() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.dayOfMonth().addToCopy(9);
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 6, 18, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addToCopy(21);
        check(copy, 1972, 6, 30, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addToCopy(22);
        check(copy, 1972, 7, 1, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addToCopy(22 + 30);
        check(copy, 1972, 7, 31, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addToCopy(22 + 31);
        check(copy, 1972, 8, 1, 10, 20, 30, 40);

        copy = test.dayOfMonth().addToCopy(21 + 31 + 31 + 30 + 31 + 30 + 31);
        check(copy, 1972, 12, 31, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addToCopy(22 + 31 + 31 + 30 + 31 + 30 + 31);
        check(copy, 1973, 1, 1, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addToCopy(-8);
        check(copy, 1972, 6, 1, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addToCopy(-9);
        check(copy, 1972, 5, 31, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addToCopy(-8 - 31 - 30 - 31 - 29 - 31);
        check(copy, 1972, 1, 1, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addToCopy(-9 - 31 - 30 - 31 - 29 - 31);
        check(copy, 1971, 12, 31, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddWrapFieldToCopyDay
    public void testPropertyAddWrapFieldToCopyDay() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.dayOfMonth().addWrapFieldToCopy(21);
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 6, 30, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addWrapFieldToCopy(22);
        check(copy, 1972, 6, 1, 10, 20, 30, 40);
        
        copy = test.dayOfMonth().addWrapFieldToCopy(-12);
        check(copy, 1972, 6, 27, 10, 20, 30, 40);
        
        test = new LocalDateTime(1972, 7, 9, 10, 20, 30, 40);
        copy = test.dayOfMonth().addWrapFieldToCopy(21);
        check(copy, 1972, 7, 30, 10, 20, 30, 40);
    
        copy = test.dayOfMonth().addWrapFieldToCopy(22);
        check(copy, 1972, 7, 31, 10, 20, 30, 40);
    
        copy = test.dayOfMonth().addWrapFieldToCopy(23);
        check(copy, 1972, 7, 1, 10, 20, 30, 40);
    
        copy = test.dayOfMonth().addWrapFieldToCopy(-12);
        check(copy, 1972, 7, 28, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetCopyDay
    public void testPropertySetCopyDay() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.dayOfMonth().setCopy(12);
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 6, 12, 10, 20, 30, 40);
        
        try {
            test.dayOfMonth().setCopy(31);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.dayOfMonth().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetCopyTextDay
    public void testPropertySetCopyTextDay() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.dayOfMonth().setCopy("12");
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 6, 12, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyWithMaximumValueDayOfMonth
    public void testPropertyWithMaximumValueDayOfMonth() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.dayOfMonth().withMaximumValue();
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 6, 30, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyWithMinimumValueDayOfMonth
    public void testPropertyWithMinimumValueDayOfMonth() {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.dayOfMonth().withMinimumValue();
        check(test, 1972, 6, 9, 10, 20, 30, 40);
        check(copy, 1972, 6, 1, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyCompareToDay
    public void testPropertyCompareToDay() {
        LocalDateTime test1 = new LocalDateTime(TEST_TIME1);
        LocalDateTime test2 = new LocalDateTime(TEST_TIME2);
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

// org.joda.time.TestLocalDateTime_Properties::testPropertyEquals
    public void testPropertyEquals() {
        LocalDateTime test1 = new LocalDateTime(2005, 11, 8, 10, 20, 30, 40);
        LocalDateTime test2 = new LocalDateTime(2005, 11, 9, 10, 20, 30, 40);
        LocalDateTime test3 = new LocalDateTime(2005, 11, 8, 10, 20, 30, 40, COPTIC_UTC);
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

// org.joda.time.TestLocalDateTime_Properties::testPropertyHashCode
    public void testPropertyHashCode() {
        LocalDateTime test1 = new LocalDateTime(2005, 11, 8, 10, 20, 30, 40);
        LocalDateTime test2 = new LocalDateTime(2005, 11, 9, 10, 20, 30, 40);
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(false, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.monthOfYear().hashCode() == test1.monthOfYear().hashCode());
        assertEquals(true, test1.monthOfYear().hashCode() == test2.monthOfYear().hashCode());
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetHour
    public void testPropertyGetHour() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        assertSame(test.getChronology().hourOfDay(), test.hourOfDay().getField());
        assertEquals("hourOfDay", test.hourOfDay().getName());
        assertEquals("Property[hourOfDay]", test.hourOfDay().toString());
        assertSame(test, test.hourOfDay().getLocalDateTime());
        assertEquals(10, test.hourOfDay().get());
        assertEquals("10", test.hourOfDay().getAsString());
        assertEquals("10", test.hourOfDay().getAsText());
        assertEquals("10", test.hourOfDay().getAsText(Locale.FRENCH));
        assertEquals("10", test.hourOfDay().getAsShortText());
        assertEquals("10", test.hourOfDay().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().hours(), test.hourOfDay().getDurationField());
        assertEquals(test.getChronology().days(), test.hourOfDay().getRangeDurationField());
        assertEquals(2, test.hourOfDay().getMaximumTextLength(null));
        assertEquals(2, test.hourOfDay().getMaximumShortTextLength(null));
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyRoundHour
    public void testPropertyRoundHour() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20);
        check(test.hourOfDay().roundCeilingCopy(), 2005, 6, 9, 11, 0, 0, 0);
        check(test.hourOfDay().roundFloorCopy(), 2005, 6, 9, 10, 0, 0, 0);
        check(test.hourOfDay().roundHalfCeilingCopy(), 2005, 6, 9, 10, 0, 0, 0);
        check(test.hourOfDay().roundHalfFloorCopy(), 2005, 6, 9, 10, 0, 0, 0);
        check(test.hourOfDay().roundHalfEvenCopy(), 2005, 6, 9, 10, 0, 0, 0);
        
        test = new LocalDateTime(2005, 6, 9, 10, 40);
        check(test.hourOfDay().roundCeilingCopy(), 2005, 6, 9, 11, 0, 0, 0);
        check(test.hourOfDay().roundFloorCopy(), 2005, 6, 9, 10, 0, 0, 0);
        check(test.hourOfDay().roundHalfCeilingCopy(), 2005, 6, 9, 11, 0, 0, 0);
        check(test.hourOfDay().roundHalfFloorCopy(), 2005, 6, 9, 11, 0, 0, 0);
        check(test.hourOfDay().roundHalfEvenCopy(), 2005, 6, 9, 11, 0, 0, 0);
        
        test = new LocalDateTime(2005, 6, 9, 10, 30);
        check(test.hourOfDay().roundCeilingCopy(), 2005, 6, 9, 11, 0, 0, 0);
        check(test.hourOfDay().roundFloorCopy(), 2005, 6, 9, 10, 0, 0, 0);
        check(test.hourOfDay().roundHalfCeilingCopy(), 2005, 6, 9, 11, 0, 0, 0);
        check(test.hourOfDay().roundHalfFloorCopy(), 2005, 6, 9, 10, 0, 0, 0);
        check(test.hourOfDay().roundHalfEvenCopy(), 2005, 6, 9, 10, 0, 0, 0);
        
        test = new LocalDateTime(2005, 6, 9, 11, 30);
        check(test.hourOfDay().roundCeilingCopy(), 2005, 6, 9, 12, 0, 0, 0);
        check(test.hourOfDay().roundFloorCopy(), 2005, 6, 9, 11, 0, 0, 0);
        check(test.hourOfDay().roundHalfCeilingCopy(), 2005, 6, 9, 12, 0, 0, 0);
        check(test.hourOfDay().roundHalfFloorCopy(), 2005, 6, 9, 11, 0, 0, 0);
        check(test.hourOfDay().roundHalfEvenCopy(), 2005, 6, 9, 12, 0, 0, 0);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyGetMaxMinValuesHour
    public void testPropertyGetMaxMinValuesHour() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        assertEquals(0, test.hourOfDay().getMinimumValue());
        assertEquals(0, test.hourOfDay().getMinimumValueOverall());
        assertEquals(23, test.hourOfDay().getMaximumValue());
        assertEquals(23, test.hourOfDay().getMaximumValueOverall());
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyWithMaxMinValueHour
    public void testPropertyWithMaxMinValueHour() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 0, 20, 30, 40);
        check(test.hourOfDay().withMaximumValue(), 2005, 6, 9, 23, 20, 30, 40);
        check(test.hourOfDay().withMinimumValue(), 2005, 6, 9, 0, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddToCopyHour
    public void testPropertyAddToCopyHour() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.hourOfDay().addToCopy(9);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 19, 20, 30, 40);
        
        copy = test.hourOfDay().addToCopy(0);
        check(copy, 2005, 6, 9, 10, 20, 30, 40);
        
        copy = test.hourOfDay().addToCopy(13);
        check(copy, 2005, 6, 9, 23, 20, 30, 40);
        
        copy = test.hourOfDay().addToCopy(14);
        check(copy, 2005, 6, 10, 0, 20, 30, 40);
        
        copy = test.hourOfDay().addToCopy(-10);
        check(copy, 2005, 6, 9, 0, 20, 30, 40);
        
        copy = test.hourOfDay().addToCopy(-11);
        check(copy, 2005, 6, 8, 23, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyAddWrapFieldToCopyHour
    public void testPropertyAddWrapFieldToCopyHour() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.hourOfDay().addWrapFieldToCopy(9);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 19, 20, 30, 40);
        
        copy = test.hourOfDay().addWrapFieldToCopy(0);
        check(copy, 2005, 6, 9, 10, 20, 30, 40);
        
        copy = test.hourOfDay().addWrapFieldToCopy(18);
        check(copy, 2005, 6, 9, 4, 20, 30, 40);
        
        copy = test.hourOfDay().addWrapFieldToCopy(-15);
        check(copy, 2005, 6, 9, 19, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetHour
    public void testPropertySetHour() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.hourOfDay().setCopy(12);
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 12, 20, 30, 40);
        
        try {
            test.hourOfDay().setCopy(24);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.hourOfDay().setCopy(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertySetTextHour
    public void testPropertySetTextHour() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.hourOfDay().setCopy("12");
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 12, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyWithMaximumValueHour
    public void testPropertyWithMaximumValueHour() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.hourOfDay().withMaximumValue();
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 23, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyWithMinimumValueHour
    public void testPropertyWithMinimumValueHour() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40);
        LocalDateTime copy = test.hourOfDay().withMinimumValue();
        check(test, 2005, 6, 9, 10, 20, 30, 40);
        check(copy, 2005, 6, 9, 0, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Properties::testPropertyCompareToHour
    public void testPropertyCompareToHour() {
        LocalDateTime test1 = new LocalDateTime(TEST_TIME1);
        LocalDateTime test2 = new LocalDateTime(TEST_TIME2);
        assertEquals(true, test1.hourOfDay().compareTo(test2) < 0);
        assertEquals(true, test2.hourOfDay().compareTo(test1) > 0);
        assertEquals(true, test1.hourOfDay().compareTo(test1) == 0);
        try {
            test1.hourOfDay().compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.hourOfDay().compareTo(dt2) < 0);
        assertEquals(true, test2.hourOfDay().compareTo(dt1) > 0);
        assertEquals(true, test1.hourOfDay().compareTo(dt1) == 0);
        try {
            test1.hourOfDay().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }
