// buggy code
    public static synchronized GJChronology getInstance(
            DateTimeZone zone,
            ReadableInstant gregorianCutover,
            int minDaysInFirstWeek) {
        
        zone = DateTimeUtils.getZone(zone);
        Instant cutoverInstant;
        if (gregorianCutover == null) {
            cutoverInstant = DEFAULT_CUTOVER;
        } else {
            cutoverInstant = gregorianCutover.toInstant();
        }

        GJChronology chrono;
        synchronized (cCache) {
            ArrayList<GJChronology> chronos = cCache.get(zone);
            if (chronos == null) {
                chronos = new ArrayList<GJChronology>(2);
                cCache.put(zone, chronos);
            } else {
                for (int i = chronos.size(); --i >= 0;) {
                    chrono = chronos.get(i);
                    if (minDaysInFirstWeek == chrono.getMinimumDaysInFirstWeek() &&
                        cutoverInstant.equals(chrono.getGregorianCutover())) {
                        
                        return chrono;
                    }
                }
            }
            if (zone == DateTimeZone.UTC) {
                chrono = new GJChronology
                    (JulianChronology.getInstance(zone, minDaysInFirstWeek),
                     GregorianChronology.getInstance(zone, minDaysInFirstWeek),
                     cutoverInstant);
            } else {
                chrono = getInstance(DateTimeZone.UTC, cutoverInstant, minDaysInFirstWeek);
                chrono = new GJChronology
                    (ZonedChronology.getInstance(chrono, zone),
                     chrono.iJulianChronology,
                     chrono.iGregorianChronology,
                     chrono.iCutoverInstant);
            }
            chronos.add(chrono);
        }
        return chrono;
    }

        public long add(long instant, int value) {
            if (instant >= iCutover) {
                instant = iGregorianField.add(instant, value);
                if (instant < iCutover) {
                    // Only adjust if gap fully crossed.
                    if (instant + iGapDuration < iCutover) {
                        instant = gregorianToJulian(instant);
                    }
                }
            } else {
                instant = iJulianField.add(instant, value);
                if (instant >= iCutover) {
                    // Only adjust if gap fully crossed.
                    if (instant - iGapDuration >= iCutover) {
                        // no special handling for year zero as cutover always after year zero
                        instant = julianToGregorian(instant);
                    }
                }
            }
            return instant;
        }

        public long add(long instant, long value) {
            if (instant >= iCutover) {
                instant = iGregorianField.add(instant, value);
                if (instant < iCutover) {
                    // Only adjust if gap fully crossed.
                    if (instant + iGapDuration < iCutover) {
                        instant = gregorianToJulian(instant);
                    }
                }
            } else {
                instant = iJulianField.add(instant, value);
                if (instant >= iCutover) {
                    // Only adjust if gap fully crossed.
                    if (instant - iGapDuration >= iCutover) {
                        // no special handling for year zero as cutover always after year zero
                        instant = julianToGregorian(instant);
                    }
                }
            }
            return instant;
        }

// relevant test
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

// org.joda.time.TestLocalDate_Constructors::testConstructor_Object_Chronology_crossChronology
    public void testConstructor_Object_Chronology_crossChronology() throws Throwable {
        LocalDate input = new LocalDate(1970, 4, 6, ISO_UTC);
        LocalDate test = new LocalDate(input, BUDDHIST_UTC);
        assertEquals(BUDDHIST_UTC, test.getChronology());
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
            private static final long serialVersionUID = 1L;
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
            private static final long serialVersionUID = 1L;
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
        @SuppressWarnings("deprecation")
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

// org.joda.time.TestMinutes::testGetFieldType
    public void testGetFieldType() {
        Minutes test = Minutes.minutes(20);
        assertEquals(DurationFieldType.minutes(), test.getFieldType());
    }

// org.joda.time.TestMinutes::testGetPeriodType
    public void testGetPeriodType() {
        Minutes test = Minutes.minutes(20);
        assertEquals(PeriodType.minutes(), test.getPeriodType());
    }

// org.joda.time.TestMinutes::testIsGreaterThan
    public void testIsGreaterThan() {
        assertEquals(true, Minutes.THREE.isGreaterThan(Minutes.TWO));
        assertEquals(false, Minutes.THREE.isGreaterThan(Minutes.THREE));
        assertEquals(false, Minutes.TWO.isGreaterThan(Minutes.THREE));
        assertEquals(true, Minutes.ONE.isGreaterThan(null));
        assertEquals(false, Minutes.minutes(-1).isGreaterThan(null));
    }

// org.joda.time.TestMinutes::testIsLessThan
    public void testIsLessThan() {
        assertEquals(false, Minutes.THREE.isLessThan(Minutes.TWO));
        assertEquals(false, Minutes.THREE.isLessThan(Minutes.THREE));
        assertEquals(true, Minutes.TWO.isLessThan(Minutes.THREE));
        assertEquals(false, Minutes.ONE.isLessThan(null));
        assertEquals(true, Minutes.minutes(-1).isLessThan(null));
    }

// org.joda.time.TestMinutes::testToString
    public void testToString() {
        Minutes test = Minutes.minutes(20);
        assertEquals("PT20M", test.toString());
        
        test = Minutes.minutes(-20);
        assertEquals("PT-20M", test.toString());
    }

// org.joda.time.TestMinutes::testSerialization
    public void testSerialization() throws Exception {
        Minutes test = Minutes.THREE;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Minutes result = (Minutes) ois.readObject();
        ois.close();
        
        assertSame(test, result);
    }

// org.joda.time.TestMinutes::testToStandardWeeks
    public void testToStandardWeeks() {
        Minutes test = Minutes.minutes(60 * 24 * 7 * 2);
        Weeks expected = Weeks.weeks(2);
        assertEquals(expected, test.toStandardWeeks());
    }

// org.joda.time.TestMinutes::testToStandardDays
    public void testToStandardDays() {
        Minutes test = Minutes.minutes(60 * 24 * 2);
        Days expected = Days.days(2);
        assertEquals(expected, test.toStandardDays());
    }

// org.joda.time.TestMinutes::testToStandardHours
    public void testToStandardHours() {
        Minutes test = Minutes.minutes(3 * 60);
        Hours expected = Hours.hours(3);
        assertEquals(expected, test.toStandardHours());
    }

// org.joda.time.TestMinutes::testToStandardSeconds
    public void testToStandardSeconds() {
        Minutes test = Minutes.minutes(3);
        Seconds expected = Seconds.seconds(3 * 60);
        assertEquals(expected, test.toStandardSeconds());
        
        try {
            Minutes.MAX_VALUE.toStandardSeconds();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMinutes::testToStandardDuration
    public void testToStandardDuration() {
        Minutes test = Minutes.minutes(20);
        Duration expected = new Duration(20L * DateTimeConstants.MILLIS_PER_MINUTE);
        assertEquals(expected, test.toStandardDuration());
        
        expected = new Duration(((long) Integer.MAX_VALUE) * DateTimeConstants.MILLIS_PER_MINUTE);
        assertEquals(expected, Minutes.MAX_VALUE.toStandardDuration());
    }

// org.joda.time.TestMinutes::testPlus_int
    public void testPlus_int() {
        Minutes test2 = Minutes.minutes(2);
        Minutes result = test2.plus(3);
        assertEquals(2, test2.getMinutes());
        assertEquals(5, result.getMinutes());
        
        assertEquals(1, Minutes.ONE.plus(0).getMinutes());
        
        try {
            Minutes.MAX_VALUE.plus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMinutes::testPlus_Minutes
    public void testPlus_Minutes() {
        Minutes test2 = Minutes.minutes(2);
        Minutes test3 = Minutes.minutes(3);
        Minutes result = test2.plus(test3);
        assertEquals(2, test2.getMinutes());
        assertEquals(3, test3.getMinutes());
        assertEquals(5, result.getMinutes());
        
        assertEquals(1, Minutes.ONE.plus(Minutes.ZERO).getMinutes());
        assertEquals(1, Minutes.ONE.plus((Minutes) null).getMinutes());
        
        try {
            Minutes.MAX_VALUE.plus(Minutes.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMinutes::testMinus_int
    public void testMinus_int() {
        Minutes test2 = Minutes.minutes(2);
        Minutes result = test2.minus(3);
        assertEquals(2, test2.getMinutes());
        assertEquals(-1, result.getMinutes());
        
        assertEquals(1, Minutes.ONE.minus(0).getMinutes());
        
        try {
            Minutes.MIN_VALUE.minus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMinutes::testMinus_Minutes
    public void testMinus_Minutes() {
        Minutes test2 = Minutes.minutes(2);
        Minutes test3 = Minutes.minutes(3);
        Minutes result = test2.minus(test3);
        assertEquals(2, test2.getMinutes());
        assertEquals(3, test3.getMinutes());
        assertEquals(-1, result.getMinutes());
        
        assertEquals(1, Minutes.ONE.minus(Minutes.ZERO).getMinutes());
        assertEquals(1, Minutes.ONE.minus((Minutes) null).getMinutes());
        
        try {
            Minutes.MIN_VALUE.minus(Minutes.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMinutes::testMultipliedBy_int
    public void testMultipliedBy_int() {
        Minutes test = Minutes.minutes(2);
        assertEquals(6, test.multipliedBy(3).getMinutes());
        assertEquals(2, test.getMinutes());
        assertEquals(-6, test.multipliedBy(-3).getMinutes());
        assertSame(test, test.multipliedBy(1));
        
        Minutes halfMax = Minutes.minutes(Integer.MAX_VALUE / 2 + 1);
        try {
            halfMax.multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMinutes::testDividedBy_int
    public void testDividedBy_int() {
        Minutes test = Minutes.minutes(12);
        assertEquals(6, test.dividedBy(2).getMinutes());
        assertEquals(12, test.getMinutes());
        assertEquals(4, test.dividedBy(3).getMinutes());
        assertEquals(3, test.dividedBy(4).getMinutes());
        assertEquals(2, test.dividedBy(5).getMinutes());
        assertEquals(2, test.dividedBy(6).getMinutes());
        assertSame(test, test.dividedBy(1));
        
        try {
            Minutes.ONE.dividedBy(0);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMinutes::testNegated
    public void testNegated() {
        Minutes test = Minutes.minutes(12);
        assertEquals(-12, test.negated().getMinutes());
        assertEquals(12, test.getMinutes());
        
        try {
            Minutes.MIN_VALUE.negated();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMinutes::testAddToLocalDate
    public void testAddToLocalDate() {
        Minutes test = Minutes.minutes(26);
        LocalDateTime date = new LocalDateTime(2006, 6, 1, 0, 0, 0, 0);
        LocalDateTime expected = new LocalDateTime(2006, 6, 1, 0, 26, 0, 0);
        assertEquals(expected, date.plus(test));
    }

// org.joda.time.TestMonthDay_Basics::testGet
    public void testGet() {
        MonthDay test = new MonthDay();
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.year());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testSize
    public void testSize() {
        MonthDay test = new MonthDay();
        assertEquals(2, test.size());
    }

// org.joda.time.TestMonthDay_Basics::testGetFieldType
    public void testGetFieldType() {
        MonthDay test = new MonthDay(COPTIC_PARIS);
        assertSame(DateTimeFieldType.monthOfYear(), test.getFieldType(0));
        assertSame(DateTimeFieldType.dayOfMonth(), test.getFieldType(1));

        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        MonthDay test = new MonthDay(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertEquals(2, fields.length);
        assertSame(DateTimeFieldType.monthOfYear(), fields[0]);
        assertSame(DateTimeFieldType.dayOfMonth(), fields[1]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestMonthDay_Basics::testGetField
    public void testGetField() {
        MonthDay test = new MonthDay(COPTIC_PARIS);
        assertSame(COPTIC_UTC.monthOfYear(), test.getField(0));
        assertSame(COPTIC_UTC.dayOfMonth(), test.getField(1));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testGetFields
    public void testGetFields() {
        MonthDay test = new MonthDay(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertEquals(2, fields.length);
        assertSame(COPTIC_UTC.monthOfYear(), fields[0]);
        assertSame(COPTIC_UTC.dayOfMonth(), fields[1]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestMonthDay_Basics::testGetValue
    public void testGetValue() {
        MonthDay test = new MonthDay();
        assertEquals(6, test.getValue(0));
        assertEquals(9, test.getValue(1));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testGetValues
    public void testGetValues() {
        MonthDay test = new MonthDay();
        int[] values = test.getValues();
        assertEquals(2, values.length);
        assertEquals(6, values[0]);
        assertEquals(9, values[1]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestMonthDay_Basics::testIsSupported
    public void testIsSupported() {
        MonthDay test = new MonthDay(COPTIC_PARIS);
        assertEquals(false, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(false, test.isSupported(DateTimeFieldType.hourOfDay()));
    }

// org.joda.time.TestMonthDay_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        MonthDay test1 = new MonthDay(10, 6, COPTIC_PARIS);
        MonthDay test2 = new MonthDay(10, 6, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        MonthDay test3 = new MonthDay(10, 6);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockMD()));
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestMonthDay_Basics::testCompareTo
    public void testCompareTo() {
        MonthDay test1 = new MonthDay(6, 6);
        MonthDay test1a = new MonthDay(6, 6);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        MonthDay test2 = new MonthDay(6, 7);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        MonthDay test3 = new MonthDay(6, 7, GregorianChronology.getInstanceUTC());
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {6, 6};
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
            new MonthDay(10, 6).compareTo(partial);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testIsEqual_MD
    public void testIsEqual_MD() {
        MonthDay test1 = new MonthDay(6, 6);
        MonthDay test1a = new MonthDay(6, 6);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        MonthDay test2 = new MonthDay(6, 7);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        MonthDay test3 = new MonthDay(6, 7, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new MonthDay(6, 7).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testIsBefore_MD
    public void testIsBefore_MD() {
        MonthDay test1 = new MonthDay(6, 6);
        MonthDay test1a = new MonthDay(6, 6);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        MonthDay test2 = new MonthDay(6, 7);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        MonthDay test3 = new MonthDay(6, 7, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new MonthDay(6, 7).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testIsAfter_MD
    public void testIsAfter_MD() {
        MonthDay test1 = new MonthDay(6, 6);
        MonthDay test1a = new MonthDay(6, 6);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        MonthDay test2 = new MonthDay(6, 7);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        MonthDay test3 = new MonthDay(6, 7, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new MonthDay(6, 7).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testWithChronologyRetainFields_Chrono
    public void testWithChronologyRetainFields_Chrono() {
        MonthDay base = new MonthDay(6, 6, COPTIC_PARIS);
        MonthDay test = base.withChronologyRetainFields(BUDDHIST_TOKYO);
        check(base, 6, 6);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 6, 6);
        assertEquals(BUDDHIST_UTC, test.getChronology());
    }

// org.joda.time.TestMonthDay_Basics::testWithChronologyRetainFields_sameChrono
    public void testWithChronologyRetainFields_sameChrono() {
        MonthDay base = new MonthDay(6, 6, COPTIC_PARIS);
        MonthDay test = base.withChronologyRetainFields(COPTIC_TOKYO);
        assertSame(base, test);
    }

// org.joda.time.TestMonthDay_Basics::testWithChronologyRetainFields_nullChrono
    public void testWithChronologyRetainFields_nullChrono() {
        MonthDay base = new MonthDay(6, 6, COPTIC_PARIS);
        MonthDay test = base.withChronologyRetainFields(null);
        check(base, 6, 6);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 6, 6);
        assertEquals(ISO_UTC, test.getChronology());
    }

// org.joda.time.TestMonthDay_Basics::testWithField
    public void testWithField() {
        MonthDay test = new MonthDay(9, 6);
        MonthDay result = test.withField(DateTimeFieldType.monthOfYear(), 10);
        
        assertEquals(new MonthDay(9, 6), test);
        assertEquals(new MonthDay(10, 6), result);
    }

// org.joda.time.TestMonthDay_Basics::testWithField_nullField
    public void testWithField_nullField() {
        MonthDay test = new MonthDay(9, 6);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testWithField_unknownField
    public void testWithField_unknownField() {
        MonthDay test = new MonthDay(9, 6);
        try {
            test.withField(DateTimeFieldType.hourOfDay(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testWithField_same
    public void testWithField_same() {
        MonthDay test = new MonthDay(9, 6);
        MonthDay result = test.withField(DateTimeFieldType.monthOfYear(), 9);
        assertEquals(new MonthDay(9, 6), test);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testWithFieldAdded
    public void testWithFieldAdded() {
        MonthDay test = new MonthDay(9, 6);
        MonthDay result = test.withFieldAdded(DurationFieldType.months(), 1);
        
        assertEquals(new MonthDay(9, 6), test);
        assertEquals(new MonthDay(10, 6), result);
    }

// org.joda.time.TestMonthDay_Basics::testWithFieldAdded_nullField_zero
    public void testWithFieldAdded_nullField_zero() {
        MonthDay test = new MonthDay(9, 6);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testWithFieldAdded_nullField_nonZero
    public void testWithFieldAdded_nullField_nonZero() {
        MonthDay test = new MonthDay(9, 6);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testWithFieldAdded_zero
    public void testWithFieldAdded_zero() {
        MonthDay test = new MonthDay(9, 6);
        MonthDay result = test.withFieldAdded(DurationFieldType.months(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testWithFieldAdded_unknownField
    public void testWithFieldAdded_unknownField() {
        MonthDay test = new MonthDay(9, 6);
        try {
            test.withFieldAdded(DurationFieldType.hours(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testPlus_RP
    public void testPlus_RP() {
        MonthDay test = new MonthDay(6, 5, BuddhistChronology.getInstance());
        MonthDay result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        MonthDay expected = new MonthDay(8, 9, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        MonthDay test = new MonthDay(6, 5, BuddhistChronology.getInstance());
        MonthDay result = test.plusMonths(1);
        MonthDay expected = new MonthDay(7, 5, BuddhistChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_fromLeap
    public void testPlusMonths_int_fromLeap() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(1);
        MonthDay expected = new MonthDay(3, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_negativeFromLeap
    public void testPlusMonths_int_negativeFromLeap() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(-1);
        MonthDay expected = new MonthDay(1, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_endOfMonthAdjust
    public void testPlusMonths_int_endOfMonthAdjust() {
        MonthDay test = new MonthDay(3, 31, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(1);
        MonthDay expected = new MonthDay(4, 30, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_negativeEndOfMonthAdjust
    public void testPlusMonths_int_negativeEndOfMonthAdjust() {
        MonthDay test = new MonthDay(3, 31, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(-1);
        MonthDay expected = new MonthDay(2, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_same
    public void testPlusMonths_int_same() {
        MonthDay test = new MonthDay(6, 5, ISO_UTC);
        MonthDay result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_wrap
    public void testPlusMonths_int_wrap() {
        MonthDay test = new MonthDay(6, 5, ISO_UTC);
        MonthDay result = test.plusMonths(10);
        MonthDay expected = new MonthDay(4, 5, ISO_UTC);
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusMonths_int_adjust
    public void testPlusMonths_int_adjust() {
        MonthDay test = new MonthDay(7, 31, ISO_UTC);
        MonthDay result = test.plusMonths(2);
        MonthDay expected = new MonthDay(9, 30, ISO_UTC);
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusDays_int
    public void testPlusDays_int() {
        MonthDay test = new MonthDay(5, 10, BuddhistChronology.getInstance());
        MonthDay result = test.plusDays(1);
        MonthDay expected = new MonthDay(5, 11, BuddhistChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusDays_int_fromLeap
    public void testPlusDays_int_fromLeap() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusDays(1);
        MonthDay expected = new MonthDay(3, 1, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusDays_int_negativeFromLeap
    public void testPlusDays_int_negativeFromLeap() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusDays(-1);
        MonthDay expected = new MonthDay(2, 28, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testPlusDays_same
    public void testPlusDays_same() {
        MonthDay test = new MonthDay(5, 10, BuddhistChronology.getInstance());
        MonthDay result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinus_RP
    public void testMinus_RP() {
        MonthDay test = new MonthDay(6, 5, BuddhistChronology.getInstance());
        MonthDay result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        MonthDay expected = new MonthDay(5, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        MonthDay test = new MonthDay(6, 5, BuddhistChronology.getInstance());
        MonthDay result = test.minusMonths(1);
        MonthDay expected = new MonthDay(5, 5, BuddhistChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusMonths_int_fromLeap
    public void testMinusMonths_int_fromLeap() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.minusMonths(1);
        MonthDay expected = new MonthDay(1, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusMonths_int_negativeFromLeap
    public void testMinusMonths_int_negativeFromLeap() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.minusMonths(-1);
        MonthDay expected = new MonthDay(3, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusMonths_int_endOfMonthAdjust
    public void testMinusMonths_int_endOfMonthAdjust() {
        MonthDay test = new MonthDay(3, 31, ISOChronology.getInstanceUTC());
        MonthDay result = test.minusMonths(1);
        MonthDay expected = new MonthDay(2, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusMonths_int_negativeEndOfMonthAdjust
    public void testMinusMonths_int_negativeEndOfMonthAdjust() {
        MonthDay test = new MonthDay(3, 31, ISOChronology.getInstanceUTC());
        MonthDay result = test.minusMonths(-1);
        MonthDay expected = new MonthDay(4, 30, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusMonths_int_same
    public void testMinusMonths_int_same() {
        MonthDay test = new MonthDay(6, 5, ISO_UTC);
        MonthDay result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusMonths_int_wrap
    public void testMinusMonths_int_wrap() {
        MonthDay test = new MonthDay(6, 5, ISO_UTC);
        MonthDay result = test.minusMonths(10);
        MonthDay expected = new MonthDay(8, 5, ISO_UTC);
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusMonths_int_adjust
    public void testMinusMonths_int_adjust() {
        MonthDay test = new MonthDay(7, 31, ISO_UTC);
        MonthDay result = test.minusMonths(3);
        MonthDay expected = new MonthDay(4, 30, ISO_UTC);
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusDays_int
    public void testMinusDays_int() {
        MonthDay test = new MonthDay(5, 11, BuddhistChronology.getInstance());
        MonthDay result = test.minusDays(1);
        MonthDay expected = new MonthDay(5, 10, BuddhistChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusDays_int_fromLeap
    public void testMinusDays_int_fromLeap() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.minusDays(1);
        MonthDay expected = new MonthDay(2, 28, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusDays_int_negativeFromLeap
    public void testMinusDays_int_negativeFromLeap() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.minusDays(-1);
        MonthDay expected = new MonthDay(3, 1, ISOChronology.getInstance());
        assertEquals(expected, result);
    }

// org.joda.time.TestMonthDay_Basics::testMinusDays_same
    public void testMinusDays_same() {
        MonthDay test = new MonthDay(5, 11, BuddhistChronology.getInstance());
        MonthDay result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestMonthDay_Basics::testToLocalDate
    public void testToLocalDate() {
        MonthDay base = new MonthDay(6, 6, COPTIC_UTC);
        LocalDate test = base.toLocalDate(2009);
        assertEquals(new LocalDate(2009, 6, 6, COPTIC_UTC), test);
        try {
            base.toLocalDate(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        MonthDay base = new MonthDay(6, 6, COPTIC_PARIS);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7);
        
        DateTime test = base.toDateTime(dt);
        check(base, 6, 6);
        DateTime expected = dt;
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(6);
        assertEquals(expected, test);
    }

// org.joda.time.TestMonthDay_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        MonthDay base = new MonthDay(6, 6);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 6, 6);
        DateTime expected = dt;
        expected = expected.monthOfYear().setCopy(6);
        expected = expected.dayOfMonth().setCopy(6);
        assertEquals(expected, test);
    }

// org.joda.time.TestMonthDay_Basics::testWithers
    public void testWithers() {
        MonthDay test = new MonthDay(10, 6);
        check(test.withMonthOfYear(5), 5, 6);
        check(test.withDayOfMonth(2), 10, 2);
        try {
            test.withMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Basics::testProperty
    public void testProperty() {
        MonthDay test = new MonthDay(6, 6);
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

// org.joda.time.TestMonthDay_Basics::testSerialization
    public void testSerialization() throws Exception {
        MonthDay test = new MonthDay(5, 6, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        MonthDay result = (MonthDay) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestMonthDay_Basics::testToString
    public void testToString() {
        MonthDay test = new MonthDay(5, 6);
        assertEquals("--05-06", test.toString());
    }

// org.joda.time.TestMonthDay_Basics::testToString_String
    public void testToString_String() {
        MonthDay test = new MonthDay(5, 6);
        assertEquals("05 \ufffd\ufffd", test.toString("MM HH"));
        assertEquals("--05-06", test.toString((String) null));
    }

// org.joda.time.TestMonthDay_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        MonthDay test = new MonthDay(5, 6);
        assertEquals("\ufffd 6/5", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("\ufffd 6/5", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("--05-06", test.toString(null, Locale.ENGLISH));
        assertEquals("\ufffd 6/5", test.toString("EEE d/M", null));
        assertEquals("--05-06", test.toString(null, null));
    }

// org.joda.time.TestMonthDay_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        MonthDay test = new MonthDay(5, 6);
        assertEquals("05 \ufffd\ufffd", test.toString(DateTimeFormat.forPattern("MM HH")));
        assertEquals("--05-06", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestMonthDay_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new MonthDay(6, 30), MonthDay.parse("--06-30"));
        assertEquals(new MonthDay(2, 29), MonthDay.parse("--02-29"));
        assertEquals(new MonthDay(6, 30), MonthDay.parse("2010-06-30"));
        assertEquals(new MonthDay(1, 2), MonthDay.parse("2010-002"));
    }

// org.joda.time.TestMonthDay_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--dd MM").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new MonthDay(6, 30), MonthDay.parse("2010--30 06", f));
    }

// org.joda.time.TestMonthDay_Constructors::testFactory_FromCalendarFields
    public void testFactory_FromCalendarFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        MonthDay expected = new MonthDay(2, 3);
        assertEquals(expected, MonthDay.fromCalendarFields(cal));
        try {
            MonthDay.fromCalendarFields(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Constructors::testFactory_FromDateFields
    public void testFactory_FromDateFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        MonthDay expected = new MonthDay(2, 3);
        assertEquals(expected, MonthDay.fromDateFields(cal.getTime()));
        try {
            MonthDay.fromDateFields(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        MonthDay test = new MonthDay();
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(test, MonthDay.now());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 30, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        MonthDay test = new MonthDay(LONDON);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(30, test.getDayOfMonth());
        assertEquals(test, MonthDay.now(LONDON));
        
        test = new MonthDay(PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(7, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
        assertEquals(test, MonthDay.now(PARIS));
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 30, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        MonthDay test = new MonthDay((DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(30, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        MonthDay test = new MonthDay(GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(test, MonthDay.now(GREGORIAN_PARIS));
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        MonthDay test = new MonthDay((Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        MonthDay test = new MonthDay(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        MonthDay test = new MonthDay(TEST_TIME2);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        MonthDay test = new MonthDay(TEST_TIME1, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        MonthDay test = new MonthDay(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        MonthDay test = new MonthDay(TEST_TIME1, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1);
        MonthDay test = new MonthDay(date);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        MonthDay test = new MonthDay((Object) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        MonthDay test = new MonthDay("1972-12");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_ObjectString5
    public void testConstructor_ObjectString5() throws Throwable {
        MonthDay test = new MonthDay("10");
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new MonthDay("T10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new MonthDay("T10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_ObjectStringEx3
    public void testConstructor_ObjectStringEx3() throws Throwable {
        try {
            new MonthDay("10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_ObjectStringEx4
    public void testConstructor_ObjectStringEx4() throws Throwable {
        try {
            new MonthDay("10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        MonthDay test = new MonthDay(date, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        MonthDay test = new MonthDay((Object) null, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        MonthDay test = new MonthDay(date, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        MonthDay test = new MonthDay((Object) null, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_int_int
    public void testConstructor_int_int() throws Throwable {
        MonthDay test = new MonthDay(6, 30);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(30, test.getDayOfMonth());
        try {
            new MonthDay(Integer.MIN_VALUE, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MonthDay(Integer.MAX_VALUE, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MonthDay(1970, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MonthDay(1970, 13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }
