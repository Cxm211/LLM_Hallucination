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
// org.joda.time.TestTimeOfDay_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(TEST_TIME1, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(TEST_TIME2, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(5 + OFFSET, test.getHourOfDay());
        assertEquals(6, test.getMinuteOfHour());
        assertEquals(7, test.getSecondOfMinute());
        assertEquals(8, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(TEST_TIME1, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        Date date = new Date(TEST_TIME1);
        TimeOfDay test = new TimeOfDay(date);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date(TEST_TIME1));
        TimeOfDay test = new TimeOfDay(cal);
        assertEquals(GJChronology.getInstanceUTC(), test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        TimeOfDay test = new TimeOfDay((Object) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_todObject
    public void testConstructor_todObject() throws Throwable {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, CopticChronology.getInstance(PARIS));
        TimeOfDay test = new TimeOfDay(base);
        assertEquals(CopticChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        TimeOfDay test = new TimeOfDay("10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString2
    public void testConstructor_ObjectString2() throws Throwable {
        TimeOfDay test = new TimeOfDay("10:20:30.040+04:00");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET - 4, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString3
    public void testConstructor_ObjectString3() throws Throwable {
        TimeOfDay test = new TimeOfDay("T10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString4
    public void testConstructor_ObjectString4() throws Throwable {
        TimeOfDay test = new TimeOfDay("T10:20:30.040+04:00");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET - 4, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString5
    public void testConstructor_ObjectString5() throws Throwable {
        TimeOfDay test = new TimeOfDay("10:20");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectString6
    public void testConstructor_ObjectString6() throws Throwable {
        TimeOfDay test = new TimeOfDay("10");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new TimeOfDay("1970-04-06");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new TimeOfDay("1970-04-06T+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectStringEx3
    public void testConstructor_ObjectStringEx3() throws Throwable {
        try {
            new TimeOfDay("1970-04-06T10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_ObjectStringEx4
    public void testConstructor_ObjectStringEx4() throws Throwable {
        try {
            new TimeOfDay("1970-04-06T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        TimeOfDay test = new TimeOfDay(date, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor2_Object_Chronology
    public void testConstructor2_Object_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay("T10:20");
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        
        try {
            new TimeOfDay("T1020");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay((Object) null, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10 + OFFSET, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        TimeOfDay test = new TimeOfDay(date, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1 + OFFSET, test.getHourOfDay());
        assertEquals(2, test.getMinuteOfHour());
        assertEquals(3, test.getSecondOfMinute());
        assertEquals(4, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay((Object) null, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10 + OFFSET, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int
    public void testConstructor_int_int() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_Chronology
    public void testConstructor_int_int_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_nullChronology
    public void testConstructor_int_int_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int
    public void testConstructor_int_int_int() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int_Chronology
    public void testConstructor_int_int_int_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20, 30, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20, 30, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1, 30, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60, 30, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, -1, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 60, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int_nullChronology
    public void testConstructor_int_int_int_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int_int
    public void testConstructor_int_int_int_int() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60, 30, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, -1, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 60, 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 30, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 30, 1000);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_Chronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, JulianChronology.getInstance());
        assertEquals(JulianChronology.getInstanceUTC(), test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        try {
            new TimeOfDay(-1, 20, 30, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(24, 20, 30, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, -1, 30, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 60, 30, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, -1, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 60, 40, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 30, -1, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new TimeOfDay(10, 20, 30, 1000, JulianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Constructors::testConstructor_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_nullChronology() throws Throwable {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
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

// org.joda.time.TestYearMonthDay_Basics::testGet
    public void testGet() {
        YearMonthDay test = new YearMonthDay();
        assertEquals(1970, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.hourOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testSize
    public void testSize() {
        YearMonthDay test = new YearMonthDay();
        assertEquals(3, test.size());
    }

// org.joda.time.TestYearMonthDay_Basics::testGetFieldType
    public void testGetFieldType() {
        YearMonthDay test = new YearMonthDay(COPTIC_PARIS);
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

// org.joda.time.TestYearMonthDay_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        YearMonthDay test = new YearMonthDay(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.year(), fields[0]);
        assertSame(DateTimeFieldType.monthOfYear(), fields[1]);
        assertSame(DateTimeFieldType.dayOfMonth(), fields[2]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestYearMonthDay_Basics::testGetField
    public void testGetField() {
        YearMonthDay test = new YearMonthDay(COPTIC_PARIS);
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

// org.joda.time.TestYearMonthDay_Basics::testGetFields
    public void testGetFields() {
        YearMonthDay test = new YearMonthDay(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertSame(COPTIC_UTC.year(), fields[0]);
        assertSame(COPTIC_UTC.monthOfYear(), fields[1]);
        assertSame(COPTIC_UTC.dayOfMonth(), fields[2]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestYearMonthDay_Basics::testGetValue
    public void testGetValue() {
        YearMonthDay test = new YearMonthDay();
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

// org.joda.time.TestYearMonthDay_Basics::testGetValues
    public void testGetValues() {
        YearMonthDay test = new YearMonthDay();
        int[] values = test.getValues();
        assertEquals(1970, values[0]);
        assertEquals(6, values[1]);
        assertEquals(9, values[2]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestYearMonthDay_Basics::testIsSupported
    public void testIsSupported() {
        YearMonthDay test = new YearMonthDay(COPTIC_PARIS);
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(false, test.isSupported(DateTimeFieldType.hourOfDay()));
    }

// org.joda.time.TestYearMonthDay_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        YearMonthDay test1 = new YearMonthDay(1970, 6, 9, COPTIC_PARIS);
        YearMonthDay test2 = new YearMonthDay(1970, 6, 9, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        YearMonthDay test3 = new YearMonthDay(1971, 6, 9);
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

// org.joda.time.TestYearMonthDay_Basics::testCompareTo
    public void testCompareTo() {
        YearMonthDay test1 = new YearMonthDay(2005, 6, 2);
        YearMonthDay test1a = new YearMonthDay(2005, 6, 2);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        YearMonthDay test2 = new YearMonthDay(2005, 7, 2);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        YearMonthDay test3 = new YearMonthDay(2005, 7, 2, GregorianChronology.getInstanceUTC());
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
            new YearMonthDay(1970, 6, 9).compareTo(partial);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testIsEqual_YMD
    public void testIsEqual_YMD() {
        YearMonthDay test1 = new YearMonthDay(2005, 6, 2);
        YearMonthDay test1a = new YearMonthDay(2005, 6, 2);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        YearMonthDay test2 = new YearMonthDay(2005, 7, 2);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        YearMonthDay test3 = new YearMonthDay(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new YearMonthDay(2005, 7, 2).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testIsBefore_YMD
    public void testIsBefore_YMD() {
        YearMonthDay test1 = new YearMonthDay(2005, 6, 2);
        YearMonthDay test1a = new YearMonthDay(2005, 6, 2);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        YearMonthDay test2 = new YearMonthDay(2005, 7, 2);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        YearMonthDay test3 = new YearMonthDay(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new YearMonthDay(2005, 7, 2).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testIsAfter_YMD
    public void testIsAfter_YMD() {
        YearMonthDay test1 = new YearMonthDay(2005, 6, 2);
        YearMonthDay test1a = new YearMonthDay(2005, 6, 2);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        YearMonthDay test2 = new YearMonthDay(2005, 7, 2);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        YearMonthDay test3 = new YearMonthDay(2005, 7, 2, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new YearMonthDay(2005, 7, 2).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testWithChronologyRetainFields_Chrono
    public void testWithChronologyRetainFields_Chrono() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        YearMonthDay test = base.withChronologyRetainFields(BUDDHIST_TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 2005, 6, 9);
        assertEquals(BUDDHIST_UTC, test.getChronology());
    }

// org.joda.time.TestYearMonthDay_Basics::testWithChronologyRetainFields_sameChrono
    public void testWithChronologyRetainFields_sameChrono() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        YearMonthDay test = base.withChronologyRetainFields(COPTIC_TOKYO);
        assertSame(base, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithChronologyRetainFields_nullChrono
    public void testWithChronologyRetainFields_nullChrono() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        YearMonthDay test = base.withChronologyRetainFields(null);
        check(base, 2005, 6, 9);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 2005, 6, 9);
        assertEquals(ISO_UTC, test.getChronology());
    }

// org.joda.time.TestYearMonthDay_Basics::testWithChronologyRetainFields_invalidInNewChrono
    public void testWithChronologyRetainFields_invalidInNewChrono() {
        YearMonthDay base = new YearMonthDay(2005, 1, 31, ISO_UTC);
        try {
            base.withChronologyRetainFields(COPTIC_UTC);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonthDay_Basics::testWithField1
    public void testWithField1() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        YearMonthDay result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new YearMonthDay(2004, 6, 9), test);
        assertEquals(new YearMonthDay(2006, 6, 9), result);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithField2
    public void testWithField2() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testWithField3
    public void testWithField3() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        try {
            test.withField(DateTimeFieldType.hourOfDay(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testWithField4
    public void testWithField4() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        YearMonthDay result = test.withField(DateTimeFieldType.year(), 2004);
        assertEquals(new YearMonthDay(2004, 6, 9), test);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithFieldAdded1
    public void testWithFieldAdded1() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        YearMonthDay result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new YearMonthDay(2004, 6, 9), test);
        assertEquals(new YearMonthDay(2010, 6, 9), result);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithFieldAdded2
    public void testWithFieldAdded2() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testWithFieldAdded3
    public void testWithFieldAdded3() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testWithFieldAdded4
    public void testWithFieldAdded4() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        YearMonthDay result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithFieldAdded5
    public void testWithFieldAdded5() {
        YearMonthDay test = new YearMonthDay(2004, 6, 9);
        try {
            test.withFieldAdded(DurationFieldType.hours(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testPlus_RP
    public void testPlus_RP() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        YearMonthDay expected = new YearMonthDay(2003, 7, 7, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testPlusYears_int
    public void testPlusYears_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.plusYears(1);
        YearMonthDay expected = new YearMonthDay(2003, 5, 3, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.plusMonths(1);
        YearMonthDay expected = new YearMonthDay(2002, 6, 3, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testPlusDays_int
    public void testPlusDays_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.plusDays(1);
        YearMonthDay expected = new YearMonthDay(2002, 5, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testMinus_RP
    public void testMinus_RP() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        YearMonthDay expected = new YearMonthDay(2001, 4, 2, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testMinusYears_int
    public void testMinusYears_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.minusYears(1);
        YearMonthDay expected = new YearMonthDay(2001, 5, 3, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.minusMonths(1);
        YearMonthDay expected = new YearMonthDay(2002, 4, 3, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testMinusDays_int
    public void testMinusDays_int() {
        YearMonthDay test = new YearMonthDay(2002, 5, 3, BuddhistChronology.getInstance());
        YearMonthDay result = test.minusDays(1);
        YearMonthDay expected = new YearMonthDay(2002, 5, 2, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestYearMonthDay_Basics::testToLocalDate
    public void testToLocalDate() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_UTC);
        LocalDate test = base.toLocalDate();
        assertEquals(new LocalDate(2005, 6, 9, COPTIC_UTC), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtMidnight
    public void testToDateTimeAtMidnight() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight();
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtMidnight_Zone
    public void testToDateTimeAtMidnight_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight(TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_TOKYO), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtMidnight_nullZone
    public void testToDateTimeAtMidnight_nullZone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateTime test = base.toDateTimeAtMidnight((DateTimeZone) null);
        check(base, 2005, 6, 9);
        assertEquals(new DateTime(2005, 6, 9, 0, 0, 0, 0, COPTIC_LONDON), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtCurrentTime
    public void testToDateTimeAtCurrentTime() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
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

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtCurrentTime_Zone
    public void testToDateTimeAtCurrentTime_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
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

// org.joda.time.TestYearMonthDay_Basics::testToDateTimeAtCurrentTime_nullZone
    public void testToDateTimeAtCurrentTime_nullZone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
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

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_TOD
    public void testToDateTime_TOD() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        TimeOfDay tod = new TimeOfDay(12, 13, 14, 15, BUDDHIST_TOKYO);
        
        DateTime test = base.toDateTime(tod);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_nullTOD
    public void testToDateTime_nullTOD() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        long now = new DateTime(2004, 5, 8, 12, 13, 14, 15, COPTIC_LONDON).getMillis();
        DateTimeUtils.setCurrentMillisFixed(now);
        
        DateTime test = base.toDateTime((TimeOfDay) null);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_TOD_Zone
    public void testToDateTime_TOD_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        TimeOfDay tod = new TimeOfDay(12, 13, 14, 15, BUDDHIST_TOKYO);
        
        DateTime test = base.toDateTime(tod, TOKYO);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_TOD_nullZone
    public void testToDateTime_TOD_nullZone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        TimeOfDay tod = new TimeOfDay(12, 13, 14, 15, BUDDHIST_TOKYO);
        
        DateTime test = base.toDateTime(tod, null);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_nullTOD_Zone
    public void testToDateTime_nullTOD_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        long now = new DateTime(2004, 5, 8, 12, 13, 14, 15, COPTIC_TOKYO).getMillis();
        DateTimeUtils.setCurrentMillisFixed(now);
        
        DateTime test = base.toDateTime((TimeOfDay) null, TOKYO);
        check(base, 2005, 6, 9);
        DateTime expected = new DateTime(2005, 6, 9, 12, 13, 14, 15, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateMidnight
    public void testToDateMidnight() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight();
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_LONDON), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateMidnight_Zone
    public void testToDateMidnight_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight(TOKYO);
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_TOKYO), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateMidnight_nullZone
    public void testToDateMidnight_nullZone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        
        DateMidnight test = base.toDateMidnight((DateTimeZone) null);
        check(base, 2005, 6, 9);
        assertEquals(new DateMidnight(2005, 6, 9, COPTIC_LONDON), test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7);
        
        DateTime test = base.toDateTime(dt);
        check(base, 2005, 6, 9);
        DateTime expected = dt;
        expected = expected.year().setCopy(2005);
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(9);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9);
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

// org.joda.time.TestYearMonthDay_Basics::testToInterval
    public void testToInterval() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval();
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTime(TimeOfDay.MIDNIGHT);
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToInterval_Zone
    public void testToInterval_Zone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval(TOKYO);
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTime(TimeOfDay.MIDNIGHT, TOKYO);
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testToInterval_nullZone
    public void testToInterval_nullZone() {
        YearMonthDay base = new YearMonthDay(2005, 6, 9, COPTIC_PARIS); 
        Interval test = base.toInterval(null);
        check(base, 2005, 6, 9);
        DateTime start = base.toDateTime(TimeOfDay.MIDNIGHT, LONDON);
        DateTime end = start.plus(Period.days(1));
        Interval expected = new Interval(start, end);
        assertEquals(expected, test);
    }

// org.joda.time.TestYearMonthDay_Basics::testWithers
    public void testWithers() {
        YearMonthDay test = new YearMonthDay(1970, 6, 9);
        check(test.withYear(2000), 2000, 6, 9);
        check(test.withMonthOfYear(2), 1970, 2, 9);
        check(test.withDayOfMonth(2), 1970, 6, 2);
        try {
            test.withMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testProperty
    public void testProperty() {
        YearMonthDay test = new YearMonthDay(2005, 6, 9);
        assertEquals(test.year(), test.property(DateTimeFieldType.year()));
        assertEquals(test.monthOfYear(), test.property(DateTimeFieldType.monthOfYear()));
        assertEquals(test.dayOfMonth(), test.property(DateTimeFieldType.dayOfMonth()));
        try {
            test.property(DateTimeFieldType.millisOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Basics::testSerialization
    public void testSerialization() throws Exception {
        YearMonthDay test = new YearMonthDay(1972, 6, 9, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        YearMonthDay result = (YearMonthDay) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestYearMonthDay_Basics::testToString
    public void testToString() {
        YearMonthDay test = new YearMonthDay(2002, 6, 9);
        assertEquals("2002-06-09", test.toString());
    }

// org.joda.time.TestYearMonthDay_Basics::testToString_String
    public void testToString_String() {
        YearMonthDay test = new YearMonthDay(2002, 6, 9);
        assertEquals("2002 \ufffd\ufffd", test.toString("yyyy HH"));
        assertEquals("2002-06-09", test.toString((String) null));
    }

// org.joda.time.TestYearMonthDay_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        YearMonthDay test = new YearMonthDay(2002, 6, 9);
        assertEquals("\ufffd 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("\ufffd 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("2002-06-09", test.toString(null, Locale.ENGLISH));
        assertEquals("\ufffd 9/6", test.toString("EEE d/M", null));
        assertEquals("2002-06-09", test.toString(null, null));
    }

// org.joda.time.TestYearMonthDay_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        YearMonthDay test = new YearMonthDay(2002, 6, 9);
        assertEquals("2002 \ufffd\ufffd", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestYearMonthDay_Constructors::testFactory_FromCalendarFields
    public void testFactory_FromCalendarFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        YearMonthDay expected = new YearMonthDay(1970, 2, 3);
        assertEquals(expected, YearMonthDay.fromCalendarFields(cal));
        try {
            YearMonthDay.fromCalendarFields(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Constructors::testFactory_FromDateFields
    public void testFactory_FromDateFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        YearMonthDay expected = new YearMonthDay(1970, 2, 3);
        assertEquals(expected, YearMonthDay.fromDateFields(cal.getTime()));
        try {
            YearMonthDay.fromDateFields(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        YearMonthDay test = new YearMonthDay();
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        YearMonthDay test = new YearMonthDay(LONDON);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(8, test.getDayOfMonth());
        
        test = new YearMonthDay(PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        YearMonthDay test = new YearMonthDay((DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(8, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        YearMonthDay test = new YearMonthDay((Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        YearMonthDay test = new YearMonthDay(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        YearMonthDay test = new YearMonthDay(TEST_TIME2);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(TEST_TIME1, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(TEST_TIME1, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1);
        YearMonthDay test = new YearMonthDay(date);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        YearMonthDay test = new YearMonthDay((Object) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        YearMonthDay test = new YearMonthDay("1972-12-03");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectString2
    public void testConstructor_ObjectString2() throws Throwable {
        YearMonthDay test = new YearMonthDay("1972-12-03T+14:00");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(2, test.getDayOfMonth());  
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectString3
    public void testConstructor_ObjectString3() throws Throwable {
        YearMonthDay test = new YearMonthDay("1972-12-03T10:20:30.040");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectString4
    public void testConstructor_ObjectString4() throws Throwable {
        YearMonthDay test = new YearMonthDay("1972-12-03T10:20:30.040+14:00");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(2, test.getDayOfMonth());  
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectString5
    public void testConstructor_ObjectString5() throws Throwable {
        YearMonthDay test = new YearMonthDay("10");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(10, test.getYear());
        assertEquals(1, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new YearMonthDay("T10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new YearMonthDay("T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectStringEx3
    public void testConstructor_ObjectStringEx3() throws Throwable {
        try {
            new YearMonthDay("10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_ObjectStringEx4
    public void testConstructor_ObjectStringEx4() throws Throwable {
        try {
            new YearMonthDay("10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        YearMonthDay test = new YearMonthDay(date, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        YearMonthDay test = new YearMonthDay((Object) null, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        YearMonthDay test = new YearMonthDay(date, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        YearMonthDay test = new YearMonthDay((Object) null, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_int_int_int
    public void testConstructor_int_int_int() throws Throwable {
        YearMonthDay test = new YearMonthDay(1970, 6, 9);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new YearMonthDay(Integer.MIN_VALUE, 6, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(Integer.MAX_VALUE, 6, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 0, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 13, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 6, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 6, 31);
            fail();
        } catch (IllegalArgumentException ex) {}
        new YearMonthDay(1970, 7, 31);
        try {
            new YearMonthDay(1970, 7, 32);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_int_int_int_Chronology
    public void testConstructor_int_int_int_Chronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(1970, 6, 9, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new YearMonthDay(Integer.MIN_VALUE, 6, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(Integer.MAX_VALUE, 6, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 0, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 13, 9, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 6, 0, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new YearMonthDay(1970, 6, 31, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        new YearMonthDay(1970, 7, 31, GREGORIAN_PARIS);
        try {
            new YearMonthDay(1970, 7, 32, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonthDay_Constructors::testConstructor_int_int_int_nullChronology
    public void testConstructor_int_int_int_nullChronology() throws Throwable {
        YearMonthDay test = new YearMonthDay(1970, 6, 9, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestYearMonth_Basics::testGet
    public void testGet() {
        YearMonth test = new YearMonth();
        assertEquals(1970, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.dayOfMonth());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestYearMonth_Basics::testSize
    public void testSize() {
        YearMonth test = new YearMonth();
        assertEquals(2, test.size());
    }

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

// org.joda.time.chrono.TestGJChronology::testLeapYearRulesConstruction
    public void testLeapYearRulesConstruction() {
        
        DateMidnight dt = new DateMidnight(1500, 2, 29, GJChronology.getInstanceUTC());
        assertEquals(dt.getYear(), 1500);
        assertEquals(dt.getMonthOfYear(), 2);
        assertEquals(dt.getDayOfMonth(), 29);
    }

// org.joda.time.chrono.TestGJChronology::testLeapYearRulesConstructionInvalid
    public void testLeapYearRulesConstructionInvalid() {
        
        try {
            new DateMidnight(1500, 2, 30, GJChronology.getInstanceUTC());
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
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
