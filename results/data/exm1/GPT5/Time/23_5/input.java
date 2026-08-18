// buggy code
    private static synchronized String getConvertedId(String id) {
        Map<String, String> map = cZoneIdConversion;
        if (map == null) {
            // Backwards compatibility with TimeZone.
            map = new HashMap<String, String>();
            map.put("GMT", "UTC");
            map.put("MIT", "Pacific/Apia");
            map.put("HST", "Pacific/Honolulu");  // JDK 1.1 compatible
            map.put("AST", "America/Anchorage");
            map.put("PST", "America/Los_Angeles");
            map.put("MST", "America/Denver");  // JDK 1.1 compatible
            map.put("PNT", "America/Phoenix");
            map.put("CST", "America/Chicago");
            map.put("EST", "America/New_York");  // JDK 1.1 compatible
            map.put("IET", "America/Indianapolis");
            map.put("PRT", "America/Puerto_Rico");
            map.put("CNT", "America/St_Johns");
            map.put("AGT", "America/Buenos_Aires");
            map.put("BET", "America/Sao_Paulo");
            map.put("WET", "Europe/London");
            map.put("ECT", "Europe/Paris");
            map.put("ART", "Africa/Cairo");
            map.put("CAT", "Africa/Harare");
            map.put("EET", "Europe/Bucharest");
            map.put("EAT", "Africa/Addis_Ababa");
            map.put("MET", "Asia/Tehran");
            map.put("NET", "Asia/Yerevan");
            map.put("PLT", "Asia/Karachi");
            map.put("IST", "Asia/Calcutta");
            map.put("BST", "Asia/Dhaka");
            map.put("VST", "Asia/Saigon");
            map.put("CTT", "Asia/Shanghai");
            map.put("JST", "Asia/Tokyo");
            map.put("ACT", "Australia/Darwin");
            map.put("AET", "Australia/Sydney");
            map.put("SST", "Pacific/Guadalcanal");
            map.put("NST", "Pacific/Auckland");
            cZoneIdConversion = map;
        }
        return map.get(id);
    }

// relevant test
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

// org.joda.time.TestMonthDay_Constructors::testConstructor_int_int_Chronology
    public void testConstructor_int_int_Chronology() throws Throwable {
        MonthDay test = new MonthDay(6, 30, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(30, test.getDayOfMonth());
        try {
            new MonthDay(Integer.MIN_VALUE, 6, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MonthDay(Integer.MAX_VALUE, 6, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MonthDay(1970, 0, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MonthDay(1970, 13, GREGORIAN_PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Constructors::testConstructor_int_int_nullChronology
    public void testConstructor_int_int_nullChronology() throws Throwable {
        MonthDay test = new MonthDay(6, 30, null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(30, test.getDayOfMonth());
    }

// org.joda.time.TestMonthDay_Properties::testPropertyGetMonthOfYear
    public void testPropertyGetMonthOfYear() {
        MonthDay test = new MonthDay(9, 6);
        assertSame(test.getChronology().monthOfYear(), test.monthOfYear().getField());
        assertEquals("monthOfYear", test.monthOfYear().getName());
        assertEquals("Property[monthOfYear]", test.monthOfYear().toString());
        assertSame(test, test.monthOfYear().getReadablePartial());
        assertSame(test, test.monthOfYear().getMonthDay());
        assertEquals(9, test.monthOfYear().get());
        assertEquals("9", test.monthOfYear().getAsString());
        assertEquals("September", test.monthOfYear().getAsText());
        assertEquals("septembre", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("Sep", test.monthOfYear().getAsShortText());
        assertEquals("sept.", test.monthOfYear().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().months(), test.monthOfYear().getDurationField());
        
        assertEquals(9, test.monthOfYear().getMaximumTextLength(null));
        assertEquals(3, test.monthOfYear().getMaximumShortTextLength(null));
    }

// org.joda.time.TestMonthDay_Properties::testPropertyGetMaxMinValuesMonthOfYear
    public void testPropertyGetMaxMinValuesMonthOfYear() {
        MonthDay test = new MonthDay(10, 6);
        assertEquals(1, test.monthOfYear().getMinimumValue());
        assertEquals(1, test.monthOfYear().getMinimumValueOverall());
        assertEquals(12, test.monthOfYear().getMaximumValue());
        assertEquals(12, test.monthOfYear().getMaximumValueOverall());
    }

// org.joda.time.TestMonthDay_Properties::testPropertyAddMonthOfYear
    public void testPropertyAddMonthOfYear() {
        MonthDay test = new MonthDay(3, 6);
        MonthDay copy = test.monthOfYear().addToCopy(9);
        check(test, 3, 6);
        check(copy, 12, 6);
        
        copy = test.monthOfYear().addToCopy(0);
        check(copy, 3, 6);

        check(test, 3, 6);
        
        copy = test.monthOfYear().addToCopy(-3);
        check(copy, 12, 6);
        check(test, 3, 6);
    }

// org.joda.time.TestMonthDay_Properties::testPropertyAddWrapFieldMonthOfYear
    public void testPropertyAddWrapFieldMonthOfYear() {
        MonthDay test = new MonthDay(5, 6);
        MonthDay copy = test.monthOfYear().addWrapFieldToCopy(2);
        check(test, 5, 6);
        check(copy, 7, 6);
        
        copy = test.monthOfYear().addWrapFieldToCopy(2);
        check(copy, 7, 6);
        
        copy = test.monthOfYear().addWrapFieldToCopy(292278993 - 4 + 1);
        check(copy, 11, 6);
        
        copy = test.monthOfYear().addWrapFieldToCopy(-292275054 - 4 - 1);
        check(copy, 6, 6);
    }

// org.joda.time.TestMonthDay_Properties::testPropertySetMonthOfYear
    public void testPropertySetMonthOfYear() {
        MonthDay test = new MonthDay(10, 6);
        MonthDay copy = test.monthOfYear().setCopy(12);
        check(test, 10, 6);
        check(copy, 12, 6);
    }

// org.joda.time.TestMonthDay_Properties::testPropertySetTextMonthOfYear
    public void testPropertySetTextMonthOfYear() {
        MonthDay test = new MonthDay(10, 6);
        MonthDay copy = test.monthOfYear().setCopy("12");
        check(test, 10, 6);
        check(copy, 12, 6);
    }

// org.joda.time.TestMonthDay_Properties::testPropertyCompareToMonthOfYear
    public void testPropertyCompareToMonthOfYear() {
        MonthDay test1 = new MonthDay(TEST_TIME1);
        MonthDay test2 = new MonthDay(TEST_TIME2);
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

// org.joda.time.TestMonthDay_Properties::testPropertyGetDayOfMonth
    public void testPropertyGetDayOfMonth() {
        MonthDay test = new MonthDay(4, 6);
        assertSame(test.getChronology().dayOfMonth(), test.dayOfMonth().getField());
        assertEquals("dayOfMonth", test.dayOfMonth().getName());
        assertEquals("Property[dayOfMonth]", test.dayOfMonth().toString());
        assertSame(test, test.dayOfMonth().getReadablePartial());
        assertSame(test, test.dayOfMonth().getMonthDay());
        assertEquals(6, test.dayOfMonth().get());
        assertEquals("6", test.dayOfMonth().getAsString());
        assertEquals("6", test.dayOfMonth().getAsText());
        assertEquals("6", test.dayOfMonth().getAsText(Locale.FRENCH));
        assertEquals("6", test.dayOfMonth().getAsShortText());
        assertEquals("6", test.dayOfMonth().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().days(), test.dayOfMonth().getDurationField());
        assertEquals(test.getChronology().months(), test.dayOfMonth().getRangeDurationField());
        assertEquals(2, test.dayOfMonth().getMaximumTextLength(null));
        assertEquals(2, test.dayOfMonth().getMaximumShortTextLength(null));
        test = new MonthDay(4, 7);
        assertEquals("7", test.dayOfMonth().getAsText(Locale.FRENCH));
        assertEquals("7", test.dayOfMonth().getAsShortText(Locale.FRENCH));
    }

// org.joda.time.TestMonthDay_Properties::testPropertyGetMaxMinValuesDayOfMonth
    public void testPropertyGetMaxMinValuesDayOfMonth() {
        MonthDay test = new MonthDay(4, 6);
        assertEquals(1, test.dayOfMonth().getMinimumValue());
        assertEquals(1, test.dayOfMonth().getMinimumValueOverall());
        assertEquals(30, test.dayOfMonth().getMaximumValue());
        assertEquals(31, test.dayOfMonth().getMaximumValueOverall());
    }

// org.joda.time.TestMonthDay_Properties::testPropertyAddDayOfMonth
    public void testPropertyAddDayOfMonth() {
        MonthDay test = new MonthDay(4, 6);
        MonthDay copy = test.dayOfMonth().addToCopy(6);
        check(test, 4, 6);
        check(copy, 4, 12);
        
        copy = test.dayOfMonth().addToCopy(7);
        check(copy, 4, 13);
        
        copy = test.dayOfMonth().addToCopy(-5);
        check(copy, 4, 1);
        
        copy = test.dayOfMonth().addToCopy(-6);
        check(copy, 3, 31);
    }

// org.joda.time.TestMonthDay_Properties::testPropertyAddWrapFieldDayOfMonth
    public void testPropertyAddWrapFieldDayOfMonth() {
        MonthDay test = new MonthDay(4, 6);
        MonthDay copy = test.dayOfMonth().addWrapFieldToCopy(4);
        check(test, 4, 6);
        check(copy, 4, 10);
        
        copy = test.dayOfMonth().addWrapFieldToCopy(8);
        check(copy, 4, 14);
        
        copy = test.dayOfMonth().addWrapFieldToCopy(-8);
        check(copy, 4, 28);
    }

// org.joda.time.TestMonthDay_Properties::testPropertySetDayOfMonth
    public void testPropertySetDayOfMonth() {
        MonthDay test = new MonthDay(4, 6);
        MonthDay copy = test.dayOfMonth().setCopy(12);
        check(test, 4, 6);
        check(copy, 4, 12);
        
        try {
            test.dayOfMonth().setCopy(33);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.dayOfMonth().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMonthDay_Properties::testPropertySetTextDayOfMonth
    public void testPropertySetTextDayOfMonth() {
        MonthDay test = new MonthDay(4, 6);
        MonthDay copy = test.dayOfMonth().setCopy("12");
        check(test, 4, 6);
        check(copy, 4, 12);
        
        copy = test.dayOfMonth().setCopy("2");
        check(test, 4, 6);
        check(copy, 4, 2);
        
        copy = test.dayOfMonth().setCopy("4");
        check(test, 4, 6);
        check(copy, 4, 4);
    }

// org.joda.time.TestMonthDay_Properties::testPropertyCompareToDayOfMonth
    public void testPropertyCompareToDayOfMonth() {
        MonthDay test1 = new MonthDay(TEST_TIME1);
        MonthDay test2 = new MonthDay(TEST_TIME2);
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

// org.joda.time.TestMonthDay_Properties::testPropertyEquals
    public void testPropertyEquals() {
        MonthDay test1 = new MonthDay(11, 11);
        MonthDay test2 = new MonthDay(11, 12);
        MonthDay test3 = new MonthDay(11, 11, CopticChronology.getInstanceUTC());
        assertEquals(true, test1.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(false, test1.dayOfMonth().equals(test1.monthOfYear()));
        assertEquals(false, test1.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(false, test1.dayOfMonth().equals(test2.monthOfYear()));
        
        assertEquals(false, test1.monthOfYear().equals(test1.dayOfMonth()));
        assertEquals(true, test1.monthOfYear().equals(test1.monthOfYear()));
        assertEquals(false, test1.monthOfYear().equals(test2.dayOfMonth()));
        assertEquals(true, test1.monthOfYear().equals(test2.monthOfYear()));
        
        assertEquals(false, test1.dayOfMonth().equals(null));
        assertEquals(false, test1.dayOfMonth().equals("any"));
        
        
        assertEquals(false, test1.dayOfMonth().equals(test3.dayOfMonth()));
    }

// org.joda.time.TestMonthDay_Properties::testPropertyHashCode
    public void testPropertyHashCode() {
        MonthDay test1 = new MonthDay(5, 11);
        MonthDay test2 = new MonthDay(5, 12);
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(false, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.monthOfYear().hashCode() == test1.monthOfYear().hashCode());
        assertEquals(true, test1.monthOfYear().hashCode() == test2.monthOfYear().hashCode());
    }

// org.joda.time.TestMonthDay_Properties::testPropertyEqualsHashCodeLenient
    public void testPropertyEqualsHashCodeLenient() {
        MonthDay test1 = new MonthDay(5, 6, LenientChronology.getInstance(COPTIC_PARIS));
        MonthDay test2 = new MonthDay(5, 6, LenientChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(true, test2.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
    }

// org.joda.time.TestMonthDay_Properties::testPropertyEqualsHashCodeStrict
    public void testPropertyEqualsHashCodeStrict() {
        MonthDay test1 = new MonthDay(5, 6, StrictChronology.getInstance(COPTIC_PARIS));
        MonthDay test2 = new MonthDay(5, 6, StrictChronology.getInstance(COPTIC_PARIS));
        assertEquals(true, test1.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().equals(test1.dayOfMonth()));
        assertEquals(true, test2.dayOfMonth().equals(test2.dayOfMonth()));
        assertEquals(true, test1.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
        assertEquals(true, test1.dayOfMonth().hashCode() == test1.dayOfMonth().hashCode());
        assertEquals(true, test2.dayOfMonth().hashCode() == test2.dayOfMonth().hashCode());
    }

// org.joda.time.TestMonths::testConstants
    public void testConstants() {
        assertEquals(0, Months.ZERO.getMonths());
        assertEquals(1, Months.ONE.getMonths());
        assertEquals(2, Months.TWO.getMonths());
        assertEquals(3, Months.THREE.getMonths());
        assertEquals(4, Months.FOUR.getMonths());
        assertEquals(5, Months.FIVE.getMonths());
        assertEquals(6, Months.SIX.getMonths());
        assertEquals(7, Months.SEVEN.getMonths());
        assertEquals(8, Months.EIGHT.getMonths());
        assertEquals(9, Months.NINE.getMonths());
        assertEquals(10, Months.TEN.getMonths());
        assertEquals(11, Months.ELEVEN.getMonths());
        assertEquals(12, Months.TWELVE.getMonths());
        assertEquals(Integer.MAX_VALUE, Months.MAX_VALUE.getMonths());
        assertEquals(Integer.MIN_VALUE, Months.MIN_VALUE.getMonths());
    }

// org.joda.time.TestMonths::testFactory_months_int
    public void testFactory_months_int() {
        assertSame(Months.ZERO, Months.months(0));
        assertSame(Months.ONE, Months.months(1));
        assertSame(Months.TWO, Months.months(2));
        assertSame(Months.THREE, Months.months(3));
        assertSame(Months.FOUR, Months.months(4));
        assertSame(Months.FIVE, Months.months(5));
        assertSame(Months.SIX, Months.months(6));
        assertSame(Months.SEVEN, Months.months(7));
        assertSame(Months.EIGHT, Months.months(8));
        assertSame(Months.NINE, Months.months(9));
        assertSame(Months.TEN, Months.months(10));
        assertSame(Months.ELEVEN, Months.months(11));
        assertSame(Months.TWELVE, Months.months(12));
        assertSame(Months.MAX_VALUE, Months.months(Integer.MAX_VALUE));
        assertSame(Months.MIN_VALUE, Months.months(Integer.MIN_VALUE));
        assertEquals(-1, Months.months(-1).getMonths());
        assertEquals(13, Months.months(13).getMonths());
    }

// org.joda.time.TestMonths::testFactory_monthsBetween_RInstant
    public void testFactory_monthsBetween_RInstant() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 9, 9, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 12, 9, 12, 0, 0, 0, PARIS);
        
        assertEquals(3, Months.monthsBetween(start, end1).getMonths());
        assertEquals(0, Months.monthsBetween(start, start).getMonths());
        assertEquals(0, Months.monthsBetween(end1, end1).getMonths());
        assertEquals(-3, Months.monthsBetween(end1, start).getMonths());
        assertEquals(6, Months.monthsBetween(start, end2).getMonths());
    }

// org.joda.time.TestMonths::testFactory_monthsBetween_RPartial
    public void testFactory_monthsBetween_RPartial() {
        LocalDate start = new LocalDate(2006, 6, 9);
        LocalDate end1 = new LocalDate(2006, 9, 9);
        YearMonthDay end2 = new YearMonthDay(2006, 12, 9);
        
        assertEquals(3, Months.monthsBetween(start, end1).getMonths());
        assertEquals(0, Months.monthsBetween(start, start).getMonths());
        assertEquals(0, Months.monthsBetween(end1, end1).getMonths());
        assertEquals(-3, Months.monthsBetween(end1, start).getMonths());
        assertEquals(6, Months.monthsBetween(start, end2).getMonths());
    }

// org.joda.time.TestMonths::testFactory_monthsIn_RInterval
    public void testFactory_monthsIn_RInterval() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 0, 0, PARIS);
        DateTime end1 = new DateTime(2006, 9, 9, 12, 0, 0, 0, PARIS);
        DateTime end2 = new DateTime(2006, 12, 9, 12, 0, 0, 0, PARIS);
        
        assertEquals(0, Months.monthsIn((ReadableInterval) null).getMonths());
        assertEquals(3, Months.monthsIn(new Interval(start, end1)).getMonths());
        assertEquals(0, Months.monthsIn(new Interval(start, start)).getMonths());
        assertEquals(0, Months.monthsIn(new Interval(end1, end1)).getMonths());
        assertEquals(6, Months.monthsIn(new Interval(start, end2)).getMonths());
    }

// org.joda.time.TestMonths::testFactory_parseMonths_String
    public void testFactory_parseMonths_String() {
        assertEquals(0, Months.parseMonths((String) null).getMonths());
        assertEquals(0, Months.parseMonths("P0M").getMonths());
        assertEquals(1, Months.parseMonths("P1M").getMonths());
        assertEquals(-3, Months.parseMonths("P-3M").getMonths());
        assertEquals(2, Months.parseMonths("P0Y2M").getMonths());
        assertEquals(2, Months.parseMonths("P2MT0H0M").getMonths());
        try {
            Months.parseMonths("P1Y1D");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Months.parseMonths("P1MT1H");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestMonths::testGetMethods
    public void testGetMethods() {
        Months test = Months.months(20);
        assertEquals(20, test.getMonths());
    }

// org.joda.time.TestMonths::testGetFieldType
    public void testGetFieldType() {
        Months test = Months.months(20);
        assertEquals(DurationFieldType.months(), test.getFieldType());
    }

// org.joda.time.TestMonths::testGetPeriodType
    public void testGetPeriodType() {
        Months test = Months.months(20);
        assertEquals(PeriodType.months(), test.getPeriodType());
    }

// org.joda.time.TestMonths::testIsGreaterThan
    public void testIsGreaterThan() {
        assertEquals(true, Months.THREE.isGreaterThan(Months.TWO));
        assertEquals(false, Months.THREE.isGreaterThan(Months.THREE));
        assertEquals(false, Months.TWO.isGreaterThan(Months.THREE));
        assertEquals(true, Months.ONE.isGreaterThan(null));
        assertEquals(false, Months.months(-1).isGreaterThan(null));
    }

// org.joda.time.TestMonths::testIsLessThan
    public void testIsLessThan() {
        assertEquals(false, Months.THREE.isLessThan(Months.TWO));
        assertEquals(false, Months.THREE.isLessThan(Months.THREE));
        assertEquals(true, Months.TWO.isLessThan(Months.THREE));
        assertEquals(false, Months.ONE.isLessThan(null));
        assertEquals(true, Months.months(-1).isLessThan(null));
    }

// org.joda.time.TestMonths::testToString
    public void testToString() {
        Months test = Months.months(20);
        assertEquals("P20M", test.toString());
        
        test = Months.months(-20);
        assertEquals("P-20M", test.toString());
    }

// org.joda.time.TestMonths::testSerialization
    public void testSerialization() throws Exception {
        Months test = Months.THREE;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Months result = (Months) ois.readObject();
        ois.close();
        
        assertSame(test, result);
    }

// org.joda.time.TestMonths::testPlus_int
    public void testPlus_int() {
        Months test2 = Months.months(2);
        Months result = test2.plus(3);
        assertEquals(2, test2.getMonths());
        assertEquals(5, result.getMonths());
        
        assertEquals(1, Months.ONE.plus(0).getMonths());
        
        try {
            Months.MAX_VALUE.plus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMonths::testPlus_Months
    public void testPlus_Months() {
        Months test2 = Months.months(2);
        Months test3 = Months.months(3);
        Months result = test2.plus(test3);
        assertEquals(2, test2.getMonths());
        assertEquals(3, test3.getMonths());
        assertEquals(5, result.getMonths());
        
        assertEquals(1, Months.ONE.plus(Months.ZERO).getMonths());
        assertEquals(1, Months.ONE.plus((Months) null).getMonths());
        
        try {
            Months.MAX_VALUE.plus(Months.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMonths::testMinus_int
    public void testMinus_int() {
        Months test2 = Months.months(2);
        Months result = test2.minus(3);
        assertEquals(2, test2.getMonths());
        assertEquals(-1, result.getMonths());
        
        assertEquals(1, Months.ONE.minus(0).getMonths());
        
        try {
            Months.MIN_VALUE.minus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMonths::testMinus_Months
    public void testMinus_Months() {
        Months test2 = Months.months(2);
        Months test3 = Months.months(3);
        Months result = test2.minus(test3);
        assertEquals(2, test2.getMonths());
        assertEquals(3, test3.getMonths());
        assertEquals(-1, result.getMonths());
        
        assertEquals(1, Months.ONE.minus(Months.ZERO).getMonths());
        assertEquals(1, Months.ONE.minus((Months) null).getMonths());
        
        try {
            Months.MIN_VALUE.minus(Months.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMonths::testMultipliedBy_int
    public void testMultipliedBy_int() {
        Months test = Months.months(2);
        assertEquals(6, test.multipliedBy(3).getMonths());
        assertEquals(2, test.getMonths());
        assertEquals(-6, test.multipliedBy(-3).getMonths());
        assertSame(test, test.multipliedBy(1));
        
        Months halfMax = Months.months(Integer.MAX_VALUE / 2 + 1);
        try {
            halfMax.multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMonths::testDividedBy_int
    public void testDividedBy_int() {
        Months test = Months.months(12);
        assertEquals(6, test.dividedBy(2).getMonths());
        assertEquals(12, test.getMonths());
        assertEquals(4, test.dividedBy(3).getMonths());
        assertEquals(3, test.dividedBy(4).getMonths());
        assertEquals(2, test.dividedBy(5).getMonths());
        assertEquals(2, test.dividedBy(6).getMonths());
        assertSame(test, test.dividedBy(1));
        
        try {
            Months.ONE.dividedBy(0);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMonths::testNegated
    public void testNegated() {
        Months test = Months.months(12);
        assertEquals(-12, test.negated().getMonths());
        assertEquals(12, test.getMonths());
        
        try {
            Months.MIN_VALUE.negated();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestMonths::testAddToLocalDate
    public void testAddToLocalDate() {
        Months test = Months.months(3);
        LocalDate date = new LocalDate(2006, 6, 1);
        LocalDate expected = new LocalDate(2006, 9, 1);
        assertEquals(expected, date.plus(test));
    }

// org.joda.time.TestMutableDateTime_Adds::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_long1
    public void testAdd_long1() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.add(123456L);
        assertEquals(TEST_TIME1 + 123456L, test.getMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_RD1
    public void testAdd_RD1() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.add(new Duration(123456L));
        assertEquals(TEST_TIME1 + 123456L, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_RD2
    public void testAdd_RD2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.add((ReadableDuration) null);
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_RD_int1
    public void testAdd_RD_int1() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.add(new Duration(123456L), -2);
        assertEquals(TEST_TIME1 - (2L * 123456L), test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_RD_int2
    public void testAdd_RD_int2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.add((ReadableDuration) null, 1);
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_RP1
    public void testAdd_RP1() {
        Period d = new Period(1, 1, 0, 1, 1, 1, 1, 1);
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
        test.add(d);
        assertEquals("2003-07-10T06:07:08.009+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_RP2
    public void testAdd_RP2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.add((ReadablePeriod) null);
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_RP_int1
    public void testAdd_RP_int1() {
        Period d = new Period(0, 0, 0, 0, 0, 0, 1, 2);
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.add(d, -2);
        assertEquals(TEST_TIME1 - (2L * 1002L), test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_RP_int2
    public void testAdd_RP_int2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.add((ReadablePeriod) null, 1);
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_DurationFieldType_int1
    public void testAdd_DurationFieldType_int1() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.add(DurationFieldType.years(), 8);
        assertEquals(2010, test.getYear());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_DurationFieldType_int2
    public void testAdd_DurationFieldType_int2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        try {
            test.add((DurationFieldType) null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Adds::testAdd_DurationFieldType_int3
    public void testAdd_DurationFieldType_int3() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        try {
            test.add((DurationFieldType) null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Adds::testAddYears_int1
    public void testAddYears_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.addYears(8);
        assertEquals("2010-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Adds::testAddMonths_int1
    public void testAddMonths_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.addMonths(6);
        assertEquals("2002-12-09T05:06:07.008Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Adds::testAddDays_int1
    public void testAddDays_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.addDays(17);
        assertEquals("2002-06-26T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Adds::testAddWeekyears_int1
    public void testAddWeekyears_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.addWeekyears(-1);
        assertEquals("2001-06-10T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Adds::testAddWeeks_int1
    public void testAddWeeks_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.addWeeks(-21);
        assertEquals("2002-01-13T05:06:07.008Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Adds::testAddHours_int1
    public void testAddHours_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.addHours(13);
        assertEquals("2002-06-09T18:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Adds::testAddMinutes_int1
    public void testAddMinutes_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.addMinutes(13);
        assertEquals("2002-06-09T05:19:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Adds::testAddSeconds_int1
    public void testAddSeconds_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.addSeconds(13);
        assertEquals("2002-06-09T05:06:20.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Adds::testAddMillis_int1
    public void testAddMillis_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.addMillis(13);
        assertEquals("2002-06-09T05:06:07.021+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableDateTime_Basics::testGet_DateTimeField
    public void testGet_DateTimeField() {
        MutableDateTime test = new MutableDateTime();
        assertEquals(1, test.get(ISOChronology.getInstance().era()));
        assertEquals(20, test.get(ISOChronology.getInstance().centuryOfEra()));
        assertEquals(2, test.get(ISOChronology.getInstance().yearOfCentury()));
        assertEquals(2002, test.get(ISOChronology.getInstance().yearOfEra()));
        assertEquals(2002, test.get(ISOChronology.getInstance().year()));
        assertEquals(6, test.get(ISOChronology.getInstance().monthOfYear()));
        assertEquals(9, test.get(ISOChronology.getInstance().dayOfMonth()));
        assertEquals(2002, test.get(ISOChronology.getInstance().weekyear()));
        assertEquals(23, test.get(ISOChronology.getInstance().weekOfWeekyear()));
        assertEquals(7, test.get(ISOChronology.getInstance().dayOfWeek()));
        assertEquals(160, test.get(ISOChronology.getInstance().dayOfYear()));
        assertEquals(0, test.get(ISOChronology.getInstance().halfdayOfDay()));
        assertEquals(1, test.get(ISOChronology.getInstance().hourOfHalfday()));
        assertEquals(1, test.get(ISOChronology.getInstance().clockhourOfDay()));
        assertEquals(1, test.get(ISOChronology.getInstance().clockhourOfHalfday()));
        assertEquals(1, test.get(ISOChronology.getInstance().hourOfDay()));
        assertEquals(0, test.get(ISOChronology.getInstance().minuteOfHour()));
        assertEquals(60, test.get(ISOChronology.getInstance().minuteOfDay()));
        assertEquals(0, test.get(ISOChronology.getInstance().secondOfMinute()));
        assertEquals(60 * 60, test.get(ISOChronology.getInstance().secondOfDay()));
        assertEquals(0, test.get(ISOChronology.getInstance().millisOfSecond()));
        assertEquals(60 * 60 * 1000, test.get(ISOChronology.getInstance().millisOfDay()));
        try {
            test.get((DateTimeField) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableDateTime_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        MutableDateTime test = new MutableDateTime();
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

// org.joda.time.TestMutableDateTime_Basics::testGetMethods
    public void testGetMethods() {
        MutableDateTime test = new MutableDateTime();
        
        assertEquals(ISOChronology.getInstance(), test.getChronology());
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

// org.joda.time.TestMutableDateTime_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        MutableDateTime test1 = new MutableDateTime(TEST_TIME1);
        MutableDateTime test2 = new MutableDateTime(TEST_TIME1);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        MutableDateTime test3 = new MutableDateTime(TEST_TIME2);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        DateTime test4 = new DateTime(TEST_TIME2);
        assertEquals(true, test4.equals(test3));
        assertEquals(true, test3.equals(test4));
        assertEquals(false, test4.equals(test1));
        assertEquals(false, test1.equals(test4));
        assertEquals(true, test3.hashCode() == test4.hashCode());
        assertEquals(false, test1.hashCode() == test4.hashCode());
        
        MutableDateTime test5 = new MutableDateTime(TEST_TIME2);
        test5.setRounding(ISOChronology.getInstance().millisOfSecond());
        assertEquals(true, test5.equals(test3));
        assertEquals(true, test5.equals(test4));
        assertEquals(true, test3.equals(test5));
        assertEquals(true, test4.equals(test5));
        assertEquals(true, test3.hashCode() == test5.hashCode());
        assertEquals(true, test4.hashCode() == test5.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(false, test1.equals(new MutableDateTime(TEST_TIME1, GregorianChronology.getInstance())));
        assertEquals(true, new MutableDateTime(TEST_TIME1, new MockEqualsChronology()).equals(new MutableDateTime(TEST_TIME1, new MockEqualsChronology())));
        assertEquals(false, new MutableDateTime(TEST_TIME1, new MockEqualsChronology()).equals(new MutableDateTime(TEST_TIME1, ISOChronology.getInstance())));
    }

// org.joda.time.TestMutableDateTime_Basics::testCompareTo
    public void testCompareTo() {
        MutableDateTime test1 = new MutableDateTime(TEST_TIME1);
        MutableDateTime test1a = new MutableDateTime(TEST_TIME1);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        MutableDateTime test2 = new MutableDateTime(TEST_TIME2);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        MutableDateTime test3 = new MutableDateTime(TEST_TIME2, GregorianChronology.getInstance(PARIS));
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

// org.joda.time.TestMutableDateTime_Basics::testIsEqual
    public void testIsEqual() {
        MutableDateTime test1 = new MutableDateTime(TEST_TIME1);
        MutableDateTime test1a = new MutableDateTime(TEST_TIME1);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        MutableDateTime test2 = new MutableDateTime(TEST_TIME2);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        MutableDateTime test3 = new MutableDateTime(TEST_TIME2, GregorianChronology.getInstance(PARIS));
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        assertEquals(false, test2.isEqual(new MockInstant()));
        assertEquals(true, test1.isEqual(new MockInstant()));
        
        assertEquals(false, new MutableDateTime(TEST_TIME_NOW + 1).isEqual(null));
        assertEquals(true, new MutableDateTime(TEST_TIME_NOW).isEqual(null));
        assertEquals(false, new MutableDateTime(TEST_TIME_NOW - 1).isEqual(null));
    }

// org.joda.time.TestMutableDateTime_Basics::testIsBefore
    public void testIsBefore() {
        MutableDateTime test1 = new MutableDateTime(TEST_TIME1);
        MutableDateTime test1a = new MutableDateTime(TEST_TIME1);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        MutableDateTime test2 = new MutableDateTime(TEST_TIME2);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        MutableDateTime test3 = new MutableDateTime(TEST_TIME2, GregorianChronology.getInstance(PARIS));
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        assertEquals(false, test2.isBefore(new MockInstant()));
        assertEquals(false, test1.isBefore(new MockInstant()));
        
        assertEquals(false, new MutableDateTime(TEST_TIME_NOW + 1).isBefore(null));
        assertEquals(false, new MutableDateTime(TEST_TIME_NOW).isBefore(null));
        assertEquals(true, new MutableDateTime(TEST_TIME_NOW - 1).isBefore(null));
    }

// org.joda.time.TestMutableDateTime_Basics::testIsAfter
    public void testIsAfter() {
        MutableDateTime test1 = new MutableDateTime(TEST_TIME1);
        MutableDateTime test1a = new MutableDateTime(TEST_TIME1);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        MutableDateTime test2 = new MutableDateTime(TEST_TIME2);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        MutableDateTime test3 = new MutableDateTime(TEST_TIME2, GregorianChronology.getInstance(PARIS));
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        assertEquals(true, test2.isAfter(new MockInstant()));
        assertEquals(false, test1.isAfter(new MockInstant()));
        
        assertEquals(true, new MutableDateTime(TEST_TIME_NOW + 1).isAfter(null));
        assertEquals(false, new MutableDateTime(TEST_TIME_NOW).isAfter(null));
        assertEquals(false, new MutableDateTime(TEST_TIME_NOW - 1).isAfter(null));
    }

// org.joda.time.TestMutableDateTime_Basics::testSerialization
    public void testSerialization() throws Exception {
        MutableDateTime test = new MutableDateTime(TEST_TIME_NOW);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        MutableDateTime result = (MutableDateTime) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestMutableDateTime_Basics::testToString
    public void testToString() {
        MutableDateTime test = new MutableDateTime(TEST_TIME_NOW);
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(TEST_TIME_NOW, PARIS);
        assertEquals("2002-06-09T02:00:00.000+02:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Basics::testToString_String
    public void testToString_String() {
        MutableDateTime test = new MutableDateTime(TEST_TIME_NOW);
        assertEquals("2002 01", test.toString("yyyy HH"));
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString((String) null));
    }

// org.joda.time.TestMutableDateTime_Basics::testToString_String_String
    public void testToString_String_String() {
        MutableDateTime test = new MutableDateTime(TEST_TIME_NOW);
        assertEquals("Sun 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("dim. 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString(null, Locale.ENGLISH));
        assertEquals("Sun 9/6", test.toString("EEE d/M", null));
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString(null, null));
    }

// org.joda.time.TestMutableDateTime_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        DateMidnight test = new DateMidnight(TEST_TIME_NOW);
        assertEquals("2002 00", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09T00:00:00.000+01:00", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestMutableDateTime_Basics::testToInstant
    public void testToInstant() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        Instant result = test.toInstant();
        assertEquals(TEST_TIME1, result.getMillis());
    }

// org.joda.time.TestMutableDateTime_Basics::testToDateTime
    public void testToDateTime() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, PARIS);
        DateTime result = test.toDateTime();
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(PARIS), result.getChronology());
    }

// org.joda.time.TestMutableDateTime_Basics::testToDateTimeISO
    public void testToDateTimeISO() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, PARIS);
        DateTime result = test.toDateTimeISO();
        assertSame(DateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(PARIS), result.getChronology());
    }

// org.joda.time.TestMutableDateTime_Basics::testToDateTime_DateTimeZone
    public void testToDateTime_DateTimeZone() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime(LONDON);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(test.getChronology(), result.getChronology());
        assertEquals(LONDON, result.getZone());

        test = new MutableDateTime(TEST_TIME1);
        result = test.toMutableDateTime(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(PARIS, result.getZone());

        test = new MutableDateTime(TEST_TIME1, GregorianChronology.getInstance(PARIS));
        result = test.toMutableDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GregorianChronology.getInstance(LONDON), result.getChronology());

        test = new MutableDateTime(TEST_TIME1, PARIS);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(LONDON, result.getZone());

        test = new MutableDateTime(TEST_TIME1);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(LONDON, result.getZone());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestMutableDateTime_Basics::testToDateTime_Chronology
    public void testToDateTime_Chronology() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime(ISOChronology.getInstance());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());

        test = new MutableDateTime(TEST_TIME1);
        result = test.toMutableDateTime(GregorianChronology.getInstance(PARIS));
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GregorianChronology.getInstance(PARIS), result.getChronology());

        test = new MutableDateTime(TEST_TIME1, GregorianChronology.getInstance(PARIS));
        result = test.toMutableDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());

        test = new MutableDateTime(TEST_TIME1);
        result = test.toMutableDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestMutableDateTime_Basics::testToMutableDateTime
    public void testToMutableDateTime() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, PARIS);
        MutableDateTime result = test.toMutableDateTime();
        assertTrue(test != result);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(PARIS), result.getChronology());
    }

// org.joda.time.TestMutableDateTime_Basics::testToMutableDateTimeISO
    public void testToMutableDateTimeISO() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, PARIS);
        MutableDateTime result = test.toMutableDateTimeISO();
        assertSame(MutableDateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(PARIS), result.getChronology());
        assertNotSame(test, result);
    }

// org.joda.time.TestMutableDateTime_Basics::testToMutableDateTime_DateTimeZone
    public void testToMutableDateTime_DateTimeZone() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime(LONDON);
        assertTrue(test != result);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(LONDON), result.getChronology());

        test = new MutableDateTime(TEST_TIME1);
        result = test.toMutableDateTime(PARIS);
        assertTrue(test != result);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(PARIS), result.getChronology());

        test = new MutableDateTime(TEST_TIME1, PARIS);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertTrue(test != result);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());

        test = new MutableDateTime(TEST_TIME1);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertTrue(test != result);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestMutableDateTime_Basics::testToMutableDateTime_Chronology
    public void testToMutableDateTime_Chronology() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime(ISOChronology.getInstance());
        assertTrue(test != result);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());

        test = new MutableDateTime(TEST_TIME1);
        result = test.toMutableDateTime(GregorianChronology.getInstance(PARIS));
        assertTrue(test != result);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GregorianChronology.getInstance(PARIS), result.getChronology());

        test = new MutableDateTime(TEST_TIME1, GregorianChronology.getInstance(PARIS));
        result = test.toMutableDateTime((Chronology) null);
        assertTrue(test != result);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());

        test = new MutableDateTime(TEST_TIME1);
        result = test.toMutableDateTime((Chronology) null);
        assertTrue(test != result);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestMutableDateTime_Basics::testToDate
    public void testToDate() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        Date result = test.toDate();
        assertEquals(test.getMillis(), result.getTime());
    }

// org.joda.time.TestMutableDateTime_Basics::testToCalendar_Locale
    public void testToCalendar_Locale() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        Calendar result = test.toCalendar(null);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/London"), result.getTimeZone());

        test = new MutableDateTime(TEST_TIME1, PARIS);
        result = test.toCalendar(null);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());

        test = new MutableDateTime(TEST_TIME1, PARIS);
        result = test.toCalendar(Locale.UK);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());
    }

// org.joda.time.TestMutableDateTime_Basics::testToGregorianCalendar
    public void testToGregorianCalendar() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        GregorianCalendar result = test.toGregorianCalendar();
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/London"), result.getTimeZone());

        test = new MutableDateTime(TEST_TIME1, PARIS);
        result = test.toGregorianCalendar();
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());
    }

// org.joda.time.TestMutableDateTime_Basics::testClone
    public void testClone() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        MutableDateTime result = (MutableDateTime) test.clone();
        assertEquals(true, test.equals(result));
        assertEquals(true, test != result);
    }

// org.joda.time.TestMutableDateTime_Basics::testCopy
    public void testCopy() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        MutableDateTime result = test.copy();
        assertEquals(true, test.equals(result));
        assertEquals(true, test != result);
    }

// org.joda.time.TestMutableDateTime_Basics::testRounding1
    public void testRounding1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setRounding(ISOChronology.getInstance().hourOfDay());
        assertEquals("2002-06-09T05:00:00.000+01:00", test.toString());
        assertEquals(MutableDateTime.ROUND_FLOOR, test.getRoundingMode());
        assertEquals(ISOChronology.getInstance().hourOfDay(), test.getRoundingField());
    }

// org.joda.time.TestMutableDateTime_Basics::testRounding2
    public void testRounding2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_CEILING);
        assertEquals("2002-06-09T06:00:00.000+01:00", test.toString());
        assertEquals(MutableDateTime.ROUND_CEILING, test.getRoundingMode());
        assertEquals(ISOChronology.getInstance().hourOfDay(), test.getRoundingField());
    }

// org.joda.time.TestMutableDateTime_Basics::testRounding3
    public void testRounding3() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_HALF_CEILING);
        assertEquals("2002-06-09T05:00:00.000+01:00", test.toString());
        assertEquals(MutableDateTime.ROUND_HALF_CEILING, test.getRoundingMode());
        assertEquals(ISOChronology.getInstance().hourOfDay(), test.getRoundingField());
        
        test = new MutableDateTime(2002, 6, 9, 5, 30, 0, 0);
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_HALF_CEILING);
        assertEquals("2002-06-09T06:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Basics::testRounding4
    public void testRounding4() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_HALF_FLOOR);
        assertEquals("2002-06-09T05:00:00.000+01:00", test.toString());
        assertEquals(MutableDateTime.ROUND_HALF_FLOOR, test.getRoundingMode());
        assertEquals(ISOChronology.getInstance().hourOfDay(), test.getRoundingField());
        
        test = new MutableDateTime(2002, 6, 9, 5, 30, 0, 0);
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_HALF_FLOOR);
        assertEquals("2002-06-09T05:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Basics::testRounding5
    public void testRounding5() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_HALF_EVEN);
        assertEquals("2002-06-09T05:00:00.000+01:00", test.toString());
        assertEquals(MutableDateTime.ROUND_HALF_EVEN, test.getRoundingMode());
        assertEquals(ISOChronology.getInstance().hourOfDay(), test.getRoundingField());
        
        test = new MutableDateTime(2002, 6, 9, 5, 30, 0, 0);
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_HALF_EVEN);
        assertEquals("2002-06-09T06:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2002, 6, 9, 4, 30, 0, 0);
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_HALF_EVEN);
        assertEquals("2002-06-09T04:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Basics::testRounding6
    public void testRounding6() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_NONE);
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
        assertEquals(MutableDateTime.ROUND_NONE, test.getRoundingMode());
        assertEquals(null, test.getRoundingField());
    }

// org.joda.time.TestMutableDateTime_Basics::testRounding7
    public void testRounding7() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setRounding(ISOChronology.getInstance().hourOfDay(), -1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableDateTime_Basics::testRounding8
    public void testRounding8() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        assertEquals(MutableDateTime.ROUND_NONE, test.getRoundingMode());
        assertEquals(null, test.getRoundingField());
        
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_CEILING);
        assertEquals(MutableDateTime.ROUND_CEILING, test.getRoundingMode());
        assertEquals(ISOChronology.getInstance().hourOfDay(), test.getRoundingField());
        
        test.setRounding(ISOChronology.getInstance().hourOfDay(), MutableDateTime.ROUND_NONE);
        assertEquals(MutableDateTime.ROUND_NONE, test.getRoundingMode());
        assertEquals(null, test.getRoundingField());
        
        test.setRounding(null, -1);
        assertEquals(MutableDateTime.ROUND_NONE, test.getRoundingMode());
        assertEquals(null, test.getRoundingField());
        
        test.setRounding(ISOChronology.getInstance().hourOfDay());
        assertEquals(MutableDateTime.ROUND_FLOOR, test.getRoundingMode());
        assertEquals(ISOChronology.getInstance().hourOfDay(), test.getRoundingField());
        
        test.setRounding(null);
        assertEquals(MutableDateTime.ROUND_NONE, test.getRoundingMode());
        assertEquals(null, test.getRoundingField());
    }

// org.joda.time.TestMutableDateTime_Basics::testProperty
    public void testProperty() {
        MutableDateTime test = new MutableDateTime();
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

// org.joda.time.TestMutableDateTime_Constructors::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableDateTime_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new MutableDateTime(2010, 6, 30, 1, 20, 0, 0, ISOChronology.getInstance(DateTimeZone.forOffsetHours(2))), MutableDateTime.parse("2010-06-30T01:20+02:00"));
        assertEquals(new MutableDateTime(2010, 1, 2, 14, 50, 0, 0, ISOChronology.getInstance(LONDON)), MutableDateTime.parse("2010-002T14:50"));
    }

// org.joda.time.TestMutableDateTime_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--dd MM HH").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new MutableDateTime(2010, 6, 30, 13, 0, 0, 0, ISOChronology.getInstance(PARIS)), MutableDateTime.parse("2010--30 06 13", f));
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        MutableDateTime test = new MutableDateTime();
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        MutableDateTime test = new MutableDateTime(PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        MutableDateTime test = new MutableDateTime((DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        MutableDateTime test = new MutableDateTime(GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        MutableDateTime test = new MutableDateTime((Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        MutableDateTime test = new MutableDateTime(TEST_TIME2);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_long1_DateTimeZone
    public void testConstructor_long1_DateTimeZone() throws Throwable {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_long2_DateTimeZone
    public void testConstructor_long2_DateTimeZone() throws Throwable {
        MutableDateTime test = new MutableDateTime(TEST_TIME2, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_long_nullDateTimeZone
    public void testConstructor_long_nullDateTimeZone() throws Throwable {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        MutableDateTime test = new MutableDateTime(TEST_TIME2, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1);
        MutableDateTime test = new MutableDateTime(date);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_invalidObject
    public void testConstructor_invalidObject() throws Throwable {
        try {
            new MutableDateTime(new Object());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        MutableDateTime test = new MutableDateTime((Object) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_badconverterObject
    public void testConstructor_badconverterObject() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            MutableDateTime test = new MutableDateTime(new Integer(0));
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_Object_DateTimeZone
    public void testConstructor_Object_DateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        MutableDateTime test = new MutableDateTime(date, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_invalidObject_DateTimeZone
    public void testConstructor_invalidObject_DateTimeZone() throws Throwable {
        try {
            new MutableDateTime(new Object(), PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_nullObject_DateTimeZone
    public void testConstructor_nullObject_DateTimeZone() throws Throwable {
        MutableDateTime test = new MutableDateTime((Object) null, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_Object_nullDateTimeZone
    public void testConstructor_Object_nullDateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        MutableDateTime test = new MutableDateTime(date, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_nullObject_nullDateTimeZone
    public void testConstructor_nullObject_nullDateTimeZone() throws Throwable {
        MutableDateTime test = new MutableDateTime((Object) null, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_badconverterObject_DateTimeZone
    public void testConstructor_badconverterObject_DateTimeZone() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            MutableDateTime test = new MutableDateTime(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        MutableDateTime test = new MutableDateTime(date, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_invalidObject_Chronology
    public void testConstructor_invalidObject_Chronology() throws Throwable {
        try {
            new MutableDateTime(new Object(), GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        MutableDateTime test = new MutableDateTime((Object) null, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        MutableDateTime test = new MutableDateTime(date, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        MutableDateTime test = new MutableDateTime((Object) null, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_badconverterObject_Chronology
    public void testConstructor_badconverterObject_Chronology() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            MutableDateTime test = new MutableDateTime(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_int_int_int_int_int_int_int
    public void testConstructor_int_int_int_int_int_int_int() throws Throwable {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 1, 0, 0, 0);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new MutableDateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 0, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 13, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 6, 0, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 6, 31, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        new MutableDateTime(2002, 7, 31, 0, 0, 0, 0);
        try {
            new MutableDateTime(2002, 7, 32, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_int_int_int_int_DateTimeZone() throws Throwable {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 2, 0, 0, 0, PARIS);  
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new MutableDateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 0, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 13, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 6, 0, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 6, 31, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        new MutableDateTime(2002, 7, 31, 0, 0, 0, 0, PARIS);
        try {
            new MutableDateTime(2002, 7, 32, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_int_int_int_int_nullDateTimeZone() throws Throwable {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 1, 0, 0, 0, (DateTimeZone) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_int_int_int_Chronology() throws Throwable {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 1, 0, 0, 0, GregorianChronology.getInstance());  
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new MutableDateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 0, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 13, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 6, 0, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MutableDateTime(2002, 6, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        new MutableDateTime(2002, 7, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
        try {
            new MutableDateTime(2002, 7, 32, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_int_int_int_nullChronology() throws Throwable {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 1, 0, 0, 0, (Chronology) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Properties::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetEra
    public void testPropertyGetEra() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().era(), test.era().getField());
        assertEquals("era", test.era().getName());
        assertEquals("Property[era]", test.era().toString());
        assertSame(test, test.era().getMutableDateTime());
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

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetYearOfEra
    public void testPropertyGetYearOfEra() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().yearOfEra(), test.yearOfEra().getField());
        assertEquals("yearOfEra", test.yearOfEra().getName());
        assertEquals("Property[yearOfEra]", test.yearOfEra().toString());
        assertEquals(2004, test.yearOfEra().get());
        assertEquals("2004", test.yearOfEra().getAsText());
        assertEquals("2004", test.yearOfEra().getAsText(Locale.FRENCH));
        assertEquals("2004", test.yearOfEra().getAsShortText());
        assertEquals("2004", test.yearOfEra().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().years(), test.yearOfEra().getDurationField());
        assertEquals(null, test.yearOfEra().getRangeDurationField());
        assertEquals(9, test.yearOfEra().getMaximumTextLength(null));
        assertEquals(9, test.yearOfEra().getMaximumShortTextLength(null));
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetCenturyOfEra
    public void testPropertyGetCenturyOfEra() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().centuryOfEra(), test.centuryOfEra().getField());
        assertEquals("centuryOfEra", test.centuryOfEra().getName());
        assertEquals("Property[centuryOfEra]", test.centuryOfEra().toString());
        assertEquals(20, test.centuryOfEra().get());
        assertEquals("20", test.centuryOfEra().getAsText());
        assertEquals("20", test.centuryOfEra().getAsText(Locale.FRENCH));
        assertEquals("20", test.centuryOfEra().getAsShortText());
        assertEquals("20", test.centuryOfEra().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().centuries(), test.centuryOfEra().getDurationField());
        assertEquals(null, test.centuryOfEra().getRangeDurationField());
        assertEquals(7, test.centuryOfEra().getMaximumTextLength(null));
        assertEquals(7, test.centuryOfEra().getMaximumShortTextLength(null));
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetYearOfCentury
    public void testPropertyGetYearOfCentury() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().yearOfCentury(), test.yearOfCentury().getField());
        assertEquals("yearOfCentury", test.yearOfCentury().getName());
        assertEquals("Property[yearOfCentury]", test.yearOfCentury().toString());
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

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetWeekyear
    public void testPropertyGetWeekyear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().weekyear(), test.weekyear().getField());
        assertEquals("weekyear", test.weekyear().getName());
        assertEquals("Property[weekyear]", test.weekyear().toString());
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

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetYear
    public void testPropertyGetYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().year(), test.year().getField());
        assertEquals("year", test.year().getName());
        assertEquals("Property[year]", test.year().toString());
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

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddYear
    public void testPropertyAddYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.year().add(9);
        assertEquals("2013-06-09T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddWrapFieldYear
    public void testPropertyAddWrapFieldYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.year().addWrapField(9);
        assertEquals("2013-06-09T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetYear
    public void testPropertySetYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.year().set(1960);
        assertEquals("1960-06-09T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetTextYear
    public void testPropertySetTextYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.year().set("1960");
        assertEquals("1960-06-09T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetMonthOfYear
    public void testPropertyGetMonthOfYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().monthOfYear(), test.monthOfYear().getField());
        assertEquals("monthOfYear", test.monthOfYear().getName());
        assertEquals("Property[monthOfYear]", test.monthOfYear().toString());
        assertEquals(6, test.monthOfYear().get());
        assertEquals("June", test.monthOfYear().getAsText());
        assertEquals("juin", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("Jun", test.monthOfYear().getAsShortText());
        assertEquals("juin", test.monthOfYear().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().months(), test.monthOfYear().getDurationField());
        assertEquals(test.getChronology().years(), test.monthOfYear().getRangeDurationField());
        assertEquals(9, test.monthOfYear().getMaximumTextLength(null));
        assertEquals(3, test.monthOfYear().getMaximumShortTextLength(null));
        test = new MutableDateTime(2004, 7, 9, 0, 0, 0, 0);
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

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddMonthOfYear
    public void testPropertyAddMonthOfYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.monthOfYear().add(6);
        assertEquals("2004-12-09T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddWrapFieldMonthOfYear
    public void testPropertyAddWrapFieldMonthOfYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.monthOfYear().addWrapField(8);
        assertEquals("2004-02-09T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetMonthOfYear
    public void testPropertySetMonthOfYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.monthOfYear().set(12);
        assertEquals("2004-12-09T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetTextMonthOfYear
    public void testPropertySetTextMonthOfYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.monthOfYear().set("12");
        assertEquals("2004-12-09T00:00:00.000Z", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.monthOfYear().set("December");
        assertEquals("2004-12-09T00:00:00.000Z", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.monthOfYear().set("Dec");
        assertEquals("2004-12-09T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetDayOfMonth
    public void testPropertyGetDayOfMonth() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().dayOfMonth(), test.dayOfMonth().getField());
        assertEquals("dayOfMonth", test.dayOfMonth().getName());
        assertEquals("Property[dayOfMonth]", test.dayOfMonth().toString());
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

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddDayOfMonth
    public void testPropertyAddDayOfMonth() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfMonth().add(9);
        assertEquals("2004-06-18T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddWrapFieldDayOfMonth
    public void testPropertyAddWrapFieldDayOfMonth() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfMonth().addWrapField(22);
        assertEquals("2004-06-01T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetDayOfMonth
    public void testPropertySetDayOfMonth() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfMonth().set(12);
        assertEquals("2004-06-12T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetTextDayOfMonth
    public void testPropertySetTextDayOfMonth() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfMonth().set("12");
        assertEquals("2004-06-12T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetDayOfYear
    public void testPropertyGetDayOfYear() {
        
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().dayOfYear(), test.dayOfYear().getField());
        assertEquals("dayOfYear", test.dayOfYear().getName());
        assertEquals("Property[dayOfYear]", test.dayOfYear().toString());
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

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddDayOfYear
    public void testPropertyAddDayOfYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfYear().add(9);
        assertEquals("2004-06-18T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddWrapFieldDayOfYear
    public void testPropertyAddWrapFieldDayOfYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfYear().addWrapField(206);
        assertEquals("2004-01-01T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetDayOfYear
    public void testPropertySetDayOfYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfYear().set(12);
        assertEquals("2004-01-12T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetTextDayOfYear
    public void testPropertySetTextDayOfYear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfYear().set("12");
        assertEquals("2004-01-12T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetWeekOfWeekyear
    public void testPropertyGetWeekOfWeekyear() {
        
        
        
        
        
        
        
        
        
        
        
        
        
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().weekOfWeekyear(), test.weekOfWeekyear().getField());
        assertEquals("weekOfWeekyear", test.weekOfWeekyear().getName());
        assertEquals("Property[weekOfWeekyear]", test.weekOfWeekyear().toString());
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

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddWeekOfWeekyear
    public void testPropertyAddWeekOfWeekyear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 7, 0, 0, 0, 0);
        test.weekOfWeekyear().add(1);
        assertEquals("2004-06-14T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddWrapFieldWeekOfWeekyear
    public void testPropertyAddWrapFieldWeekOfWeekyear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 7, 0, 0, 0, 0);
        test.weekOfWeekyear().addWrapField(30);
        assertEquals("2003-12-29T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetWeekOfWeekyear
    public void testPropertySetWeekOfWeekyear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 7, 0, 0, 0, 0);
        test.weekOfWeekyear().set(4);
        assertEquals("2004-01-19T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetTextWeekOfWeekyear
    public void testPropertySetTextWeekOfWeekyear() {
        MutableDateTime test = new MutableDateTime(2004, 6, 7, 0, 0, 0, 0);
        test.weekOfWeekyear().set("4");
        assertEquals("2004-01-19T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetDayOfWeek
    public void testPropertyGetDayOfWeek() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().dayOfWeek(), test.dayOfWeek().getField());
        assertEquals("dayOfWeek", test.dayOfWeek().getName());
        assertEquals("Property[dayOfWeek]", test.dayOfWeek().toString());
        assertEquals(3, test.dayOfWeek().get());
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

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddDayOfWeek
    public void testPropertyAddDayOfWeek() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfWeek().add(1);
        assertEquals("2004-06-10T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddLongDayOfWeek
    public void testPropertyAddLongDayOfWeek() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfWeek().add(1L);
        assertEquals("2004-06-10T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyAddWrapFieldDayOfWeek
    public void testPropertyAddWrapFieldDayOfWeek() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);  
        test.dayOfWeek().addWrapField(5);
        assertEquals("2004-06-07T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetDayOfWeek
    public void testPropertySetDayOfWeek() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfWeek().set(4);
        assertEquals("2004-06-10T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertySetTextDayOfWeek
    public void testPropertySetTextDayOfWeek() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfWeek().set("4");
        assertEquals("2004-06-10T00:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfWeek().set("Mon");
        assertEquals("2004-06-07T00:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfWeek().set("Tuesday");
        assertEquals("2004-06-08T00:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 0, 0, 0, 0);
        test.dayOfWeek().set("lundi", Locale.FRENCH);
        assertEquals("2004-06-07T00:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetHourOfDay
    public void testPropertyGetHourOfDay() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().hourOfDay(), test.hourOfDay().getField());
        assertEquals("hourOfDay", test.hourOfDay().getName());
        assertEquals("Property[hourOfDay]", test.hourOfDay().toString());
        assertEquals(13, test.hourOfDay().get());
        assertEquals("13", test.hourOfDay().getAsText());
        assertEquals("13", test.hourOfDay().getAsText(Locale.FRENCH));
        assertEquals("13", test.hourOfDay().getAsShortText());
        assertEquals("13", test.hourOfDay().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().hours(), test.hourOfDay().getDurationField());
        assertEquals(test.getChronology().days(), test.hourOfDay().getRangeDurationField());
        assertEquals(2, test.hourOfDay().getMaximumTextLength(null));
        assertEquals(2, test.hourOfDay().getMaximumShortTextLength(null));
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyRoundFloorHourOfDay
    public void testPropertyRoundFloorHourOfDay() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 30, 0, 0);
        test.hourOfDay().roundFloor();
        assertEquals("2004-06-09T13:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyRoundCeilingHourOfDay
    public void testPropertyRoundCeilingHourOfDay() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 30, 0, 0);
        test.hourOfDay().roundCeiling();
        assertEquals("2004-06-09T14:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyRoundHalfFloorHourOfDay
    public void testPropertyRoundHalfFloorHourOfDay() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 30, 0, 0);
        test.hourOfDay().roundHalfFloor();
        assertEquals("2004-06-09T13:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 13, 30, 0, 1);
        test.hourOfDay().roundHalfFloor();
        assertEquals("2004-06-09T14:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 13, 29, 59, 999);
        test.hourOfDay().roundHalfFloor();
        assertEquals("2004-06-09T13:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyRoundHalfCeilingHourOfDay
    public void testPropertyRoundHalfCeilingHourOfDay() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 30, 0, 0);
        test.hourOfDay().roundHalfCeiling();
        assertEquals("2004-06-09T14:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 13, 30, 0, 1);
        test.hourOfDay().roundHalfCeiling();
        assertEquals("2004-06-09T14:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 13, 29, 59, 999);
        test.hourOfDay().roundHalfCeiling();
        assertEquals("2004-06-09T13:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyRoundHalfEvenHourOfDay
    public void testPropertyRoundHalfEvenHourOfDay() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 30, 0, 0);
        test.hourOfDay().roundHalfEven();
        assertEquals("2004-06-09T14:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 14, 30, 0, 0);
        test.hourOfDay().roundHalfEven();
        assertEquals("2004-06-09T14:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 13, 30, 0, 1);
        test.hourOfDay().roundHalfEven();
        assertEquals("2004-06-09T14:00:00.000+01:00", test.toString());
        
        test = new MutableDateTime(2004, 6, 9, 13, 29, 59, 999);
        test.hourOfDay().roundHalfEven();
        assertEquals("2004-06-09T13:00:00.000+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyRemainderHourOfDay
    public void testPropertyRemainderHourOfDay() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 30, 0, 0);
        assertEquals(30L * DateTimeConstants.MILLIS_PER_MINUTE, test.hourOfDay().remainder());
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetMinuteOfHour
    public void testPropertyGetMinuteOfHour() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().minuteOfHour(), test.minuteOfHour().getField());
        assertEquals("minuteOfHour", test.minuteOfHour().getName());
        assertEquals("Property[minuteOfHour]", test.minuteOfHour().toString());
        assertEquals(23, test.minuteOfHour().get());
        assertEquals("23", test.minuteOfHour().getAsText());
        assertEquals("23", test.minuteOfHour().getAsText(Locale.FRENCH));
        assertEquals("23", test.minuteOfHour().getAsShortText());
        assertEquals("23", test.minuteOfHour().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().minutes(), test.minuteOfHour().getDurationField());
        assertEquals(test.getChronology().hours(), test.minuteOfHour().getRangeDurationField());
        assertEquals(2, test.minuteOfHour().getMaximumTextLength(null));
        assertEquals(2, test.minuteOfHour().getMaximumShortTextLength(null));
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetMinuteOfDay
    public void testPropertyGetMinuteOfDay() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().minuteOfDay(), test.minuteOfDay().getField());
        assertEquals("minuteOfDay", test.minuteOfDay().getName());
        assertEquals("Property[minuteOfDay]", test.minuteOfDay().toString());
        assertEquals(803, test.minuteOfDay().get());
        assertEquals("803", test.minuteOfDay().getAsText());
        assertEquals("803", test.minuteOfDay().getAsText(Locale.FRENCH));
        assertEquals("803", test.minuteOfDay().getAsShortText());
        assertEquals("803", test.minuteOfDay().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().minutes(), test.minuteOfDay().getDurationField());
        assertEquals(test.getChronology().days(), test.minuteOfDay().getRangeDurationField());
        assertEquals(4, test.minuteOfDay().getMaximumTextLength(null));
        assertEquals(4, test.minuteOfDay().getMaximumShortTextLength(null));
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetSecondOfMinute
    public void testPropertyGetSecondOfMinute() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().secondOfMinute(), test.secondOfMinute().getField());
        assertEquals("secondOfMinute", test.secondOfMinute().getName());
        assertEquals("Property[secondOfMinute]", test.secondOfMinute().toString());
        assertEquals(43, test.secondOfMinute().get());
        assertEquals("43", test.secondOfMinute().getAsText());
        assertEquals("43", test.secondOfMinute().getAsText(Locale.FRENCH));
        assertEquals("43", test.secondOfMinute().getAsShortText());
        assertEquals("43", test.secondOfMinute().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().seconds(), test.secondOfMinute().getDurationField());
        assertEquals(test.getChronology().minutes(), test.secondOfMinute().getRangeDurationField());
        assertEquals(2, test.secondOfMinute().getMaximumTextLength(null));
        assertEquals(2, test.secondOfMinute().getMaximumShortTextLength(null));
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetSecondOfDay
    public void testPropertyGetSecondOfDay() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().secondOfDay(), test.secondOfDay().getField());
        assertEquals("secondOfDay", test.secondOfDay().getName());
        assertEquals("Property[secondOfDay]", test.secondOfDay().toString());
        assertEquals(48223, test.secondOfDay().get());
        assertEquals("48223", test.secondOfDay().getAsText());
        assertEquals("48223", test.secondOfDay().getAsText(Locale.FRENCH));
        assertEquals("48223", test.secondOfDay().getAsShortText());
        assertEquals("48223", test.secondOfDay().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().seconds(), test.secondOfDay().getDurationField());
        assertEquals(test.getChronology().days(), test.secondOfDay().getRangeDurationField());
        assertEquals(5, test.secondOfDay().getMaximumTextLength(null));
        assertEquals(5, test.secondOfDay().getMaximumShortTextLength(null));
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetMillisOfSecond
    public void testPropertyGetMillisOfSecond() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().millisOfSecond(), test.millisOfSecond().getField());
        assertEquals("millisOfSecond", test.millisOfSecond().getName());
        assertEquals("Property[millisOfSecond]", test.millisOfSecond().toString());
        assertEquals(53, test.millisOfSecond().get());
        assertEquals("53", test.millisOfSecond().getAsText());
        assertEquals("53", test.millisOfSecond().getAsText(Locale.FRENCH));
        assertEquals("53", test.millisOfSecond().getAsShortText());
        assertEquals("53", test.millisOfSecond().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().millis(), test.millisOfSecond().getDurationField());
        assertEquals(test.getChronology().seconds(), test.millisOfSecond().getRangeDurationField());
        assertEquals(3, test.millisOfSecond().getMaximumTextLength(null));
        assertEquals(3, test.millisOfSecond().getMaximumShortTextLength(null));
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyGetMillisOfDay
    public void testPropertyGetMillisOfDay() {
        MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().millisOfDay(), test.millisOfDay().getField());
        assertEquals("millisOfDay", test.millisOfDay().getName());
        assertEquals("Property[millisOfDay]", test.millisOfDay().toString());
        assertEquals(48223053, test.millisOfDay().get());
        assertEquals("48223053", test.millisOfDay().getAsText());
        assertEquals("48223053", test.millisOfDay().getAsText(Locale.FRENCH));
        assertEquals("48223053", test.millisOfDay().getAsShortText());
        assertEquals("48223053", test.millisOfDay().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().millis(), test.millisOfDay().getDurationField());
        assertEquals(test.getChronology().days(), test.millisOfDay().getRangeDurationField());
        assertEquals(8, test.millisOfDay().getMaximumTextLength(null));
        assertEquals(8, test.millisOfDay().getMaximumShortTextLength(null));
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyToIntervalYearOfEra
    public void testPropertyToIntervalYearOfEra() {
      MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.yearOfEra().toInterval();
      assertEquals(new MutableDateTime(2004, 1, 1, 0, 0, 0, 0), testInterval.getStart());
      assertEquals(new MutableDateTime(2005, 1, 1, 0, 0, 0, 0), testInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 53), test);
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyToIntervalYearOfCentury
    public void testPropertyToIntervalYearOfCentury() {
      MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.yearOfCentury().toInterval();
      assertEquals(new MutableDateTime(2004, 1, 1, 0, 0, 0, 0), testInterval.getStart());
      assertEquals(new MutableDateTime(2005, 1, 1, 0, 0, 0, 0), testInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 53), test);
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyToIntervalYear
    public void testPropertyToIntervalYear() {
      MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.year().toInterval();
      assertEquals(new MutableDateTime(2004, 1, 1, 0, 0, 0, 0), testInterval.getStart());
      assertEquals(new MutableDateTime(2005, 1, 1, 0, 0, 0, 0), testInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 53), test);
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyToIntervalMonthOfYear
    public void testPropertyToIntervalMonthOfYear() {
      MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.monthOfYear().toInterval();
      assertEquals(new MutableDateTime(2004, 6, 1, 0, 0, 0, 0), testInterval.getStart());
      assertEquals(new MutableDateTime(2004, 7, 1, 0, 0, 0, 0), testInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 53), test);
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyToIntervalDayOfMonth
    public void testPropertyToIntervalDayOfMonth() {
      MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.dayOfMonth().toInterval();
      assertEquals(new MutableDateTime(2004, 6, 9, 0, 0, 0, 0), testInterval.getStart());
      assertEquals(new MutableDateTime(2004, 6, 10, 0, 0, 0, 0), testInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 53), test);

      MutableDateTime febTest = new MutableDateTime(2004, 2, 29, 13, 23, 43, 53);
      Interval febTestInterval = febTest.dayOfMonth().toInterval();
      assertEquals(new MutableDateTime(2004, 2, 29, 0, 0, 0, 0), febTestInterval.getStart());
      assertEquals(new MutableDateTime(2004, 3, 1, 0, 0, 0, 0), febTestInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 2, 29, 13, 23, 43, 53), febTest);
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyToIntervalHourOfDay
    public void testPropertyToIntervalHourOfDay() {
      MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.hourOfDay().toInterval();
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 0, 0, 0), testInterval.getStart());
      assertEquals(new MutableDateTime(2004, 6, 9, 14, 0, 0, 0), testInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 53), test);

      MutableDateTime midnightTest = new MutableDateTime(2004, 6, 9, 23, 23, 43, 53);
      Interval midnightTestInterval = midnightTest.hourOfDay().toInterval();
      assertEquals(new MutableDateTime(2004, 6, 9, 23, 0, 0, 0), midnightTestInterval.getStart());
      assertEquals(new MutableDateTime(2004, 6, 10, 0, 0, 0, 0), midnightTestInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 6, 9, 23, 23, 43, 53), midnightTest);
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyToIntervalMinuteOfHour
    public void testPropertyToIntervalMinuteOfHour() {
      MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.minuteOfHour().toInterval();
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 0, 0), testInterval.getStart());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 24, 0, 0), testInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 53), test);
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyToIntervalSecondOfMinute
    public void testPropertyToIntervalSecondOfMinute() {
      MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.secondOfMinute().toInterval();
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 0), testInterval.getStart());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 44, 0), testInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 53), test);
    }

// org.joda.time.TestMutableDateTime_Properties::testPropertyToIntervalMillisOfSecond
    public void testPropertyToIntervalMillisOfSecond() {
      MutableDateTime test = new MutableDateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.millisOfSecond().toInterval();
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 53), testInterval.getStart());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 54), testInterval.getEnd());
      assertEquals(new MutableDateTime(2004, 6, 9, 13, 23, 43, 53), test);
    }

// org.joda.time.TestMutableDateTime_Sets::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMillis_long1
    public void testSetMillis_long1() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.setMillis(TEST_TIME2);
        assertEquals(TEST_TIME2, test.getMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetChronology_Chronology1
    public void testSetChronology_Chronology1() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.setChronology(GregorianChronology.getInstance(PARIS));
        assertEquals(TEST_TIME1, test.getMillis());
        assertEquals(GregorianChronology.getInstance(PARIS), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetChronology_Chronology2
    public void testSetChronology_Chronology2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.setChronology(null);
        assertEquals(TEST_TIME1, test.getMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetZone_DateTimeZone1
    public void testSetZone_DateTimeZone1() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.setZone(PARIS);
        assertEquals(TEST_TIME1, test.getMillis());
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetZone_DateTimeZone2
    public void testSetZone_DateTimeZone2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.setZone(null);
        assertEquals(TEST_TIME1, test.getMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetZoneRetainFields_DateTimeZone1
    public void testSetZoneRetainFields_DateTimeZone1() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.setZoneRetainFields(PARIS);
        assertEquals(TEST_TIME1 - DateTimeConstants.MILLIS_PER_HOUR, test.getMillis());
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetZoneRetainFields_DateTimeZone2
    public void testSetZoneRetainFields_DateTimeZone2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.setZoneRetainFields(null);
        assertEquals(TEST_TIME1, test.getMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetZoneRetainFields_DateTimeZone3
    public void testSetZoneRetainFields_DateTimeZone3() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, GregorianChronology.getInstance(PARIS));
        test.setZoneRetainFields(null);
        assertEquals(TEST_TIME1 + DateTimeConstants.MILLIS_PER_HOUR, test.getMillis());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetZoneRetainFields_DateTimeZone4
    public void testSetZoneRetainFields_DateTimeZone4() {
        Chronology chrono = new MockNullZoneChronology();
        MutableDateTime test = new MutableDateTime(TEST_TIME1, chrono);
        test.setZoneRetainFields(PARIS);
        assertEquals(TEST_TIME1 - DateTimeConstants.MILLIS_PER_HOUR, test.getMillis());
        assertSame(chrono, test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMillis_RI1
    public void testSetMillis_RI1() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, BuddhistChronology.getInstance());
        test.setMillis(new Instant(TEST_TIME2));
        assertEquals(TEST_TIME2, test.getMillis());
        assertEquals(BuddhistChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMillis_RI2
    public void testSetMillis_RI2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1, BuddhistChronology.getInstance());
        test.setMillis(null);
        assertEquals(TEST_TIME_NOW, test.getMillis());
        assertEquals(BuddhistChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableDateTime_Sets::testSet_DateTimeFieldType_int1
    public void testSet_DateTimeFieldType_int1() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        test.set(DateTimeFieldType.year(), 2010);
        assertEquals(2010, test.getYear());
    }

// org.joda.time.TestMutableDateTime_Sets::testSet_DateTimeFieldType_int2
    public void testSet_DateTimeFieldType_int2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        try {
            test.set(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Sets::testSet_DateTimeFieldType_int3
    public void testSet_DateTimeFieldType_int3() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        try {
            test.set(DateTimeFieldType.monthOfYear(), 13);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDate_int_int_int1
    public void testSetDate_int_int_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 12, 24, 48, 501);
        test.setDate(2010, 12, 3);
        assertEquals(2010, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDate_int_int_int2
    public void testSetDate_int_int_int2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        try {
            test.setDate(2010, 13, 3);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDate_long1
    public void testSetDate_long1() {
        long setter = new DateTime(2010, 12, 3, 5, 7, 9, 501).getMillis();
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 12, 24, 48, 501);
        test.setDate(setter);
        assertEquals(2010, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDate_RI1
    public void testSetDate_RI1() {
        DateTime setter = new DateTime(2010, 12, 3, 5, 7, 9, 501);
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 12, 24, 48, 501);
        test.setDate(setter);
        assertEquals(2010, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDate_RI2
    public void testSetDate_RI2() {
        MutableDateTime test = new MutableDateTime(2010, 7, 8, 12, 24, 48, 501);
        test.setDate(null);  
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetTime_int_int_int_int1
    public void testSetTime_int_int_int_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 12, 24, 48, 501);
        test.setTime(5, 6, 7, 8);
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(5, test.getHourOfDay());
        assertEquals(6, test.getMinuteOfHour());
        assertEquals(7, test.getSecondOfMinute());
        assertEquals(8, test.getMillisOfSecond());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetTime_int_int_int2
    public void testSetTime_int_int_int2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        try {
            test.setTime(60, 6, 7, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetTime_long1
    public void testSetTime_long1() {
        long setter = new DateTime(2010, 12, 3, 5, 7, 9, 11).getMillis();
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 12, 24, 48, 501);
        test.setTime(setter);
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(5, test.getHourOfDay());
        assertEquals(7, test.getMinuteOfHour());
        assertEquals(9, test.getSecondOfMinute());
        assertEquals(11, test.getMillisOfSecond());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetTime_RI1
    public void testSetTime_RI1() {
        DateTime setter = new DateTime(2010, 12, 3, 5, 7, 9, 11);
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 12, 24, 48, 501);
        test.setTime(setter);
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(5, test.getHourOfDay());
        assertEquals(7, test.getMinuteOfHour());
        assertEquals(9, test.getSecondOfMinute());
        assertEquals(11, test.getMillisOfSecond());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetTime_RI2
    public void testSetTime_RI2() {
        MutableDateTime test = new MutableDateTime(2010, 7, 8, 12, 24, 48, 501);
        test.setTime(null);  
        assertEquals(2010, test.getYear());
        assertEquals(7, test.getMonthOfYear());
        assertEquals(8, test.getDayOfMonth());
        assertEquals(new DateTime(TEST_TIME_NOW).getHourOfDay(), test.getHourOfDay());
        assertEquals(new DateTime(TEST_TIME_NOW).getMinuteOfHour(), test.getMinuteOfHour());
        assertEquals(new DateTime(TEST_TIME_NOW).getSecondOfMinute(), test.getSecondOfMinute());
        assertEquals(new DateTime(TEST_TIME_NOW).getMillisOfSecond(), test.getMillisOfSecond());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetTime_Object3
    public void testSetTime_Object3() {
        DateTime temp = new DateTime(2010, 12, 3, 5, 7, 9, 11);
        DateTime setter = new DateTime(temp.getMillis(), new MockNullZoneChronology());
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 12, 24, 48, 501);
        test.setTime(setter);
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(5, test.getHourOfDay());
        assertEquals(7, test.getMinuteOfHour());
        assertEquals(9, test.getSecondOfMinute());
        assertEquals(11, test.getMillisOfSecond());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDateTime_int_int_int_int_int_int_int1
    public void testSetDateTime_int_int_int_int_int_int_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 12, 24, 48, 501);
        test.setDateTime(2010, 12, 3, 5, 6, 7, 8);
        assertEquals(2010, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(5, test.getHourOfDay());
        assertEquals(6, test.getMinuteOfHour());
        assertEquals(7, test.getSecondOfMinute());
        assertEquals(8, test.getMillisOfSecond());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDateTime_int_int_int_int_int_int_int2
    public void testSetDateTime_int_int_int_int_int_int_int2() {
        MutableDateTime test = new MutableDateTime(TEST_TIME1);
        try {
            test.setDateTime(2010, 13, 3, 5, 6, 7, 8);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetYear_int1
    public void testSetYear_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setYear(2010);
        assertEquals("2010-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMonthOfYear_int1
    public void testSetMonthOfYear_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setMonthOfYear(12);
        assertEquals("2002-12-09T05:06:07.008Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMonthOfYear_int2
    public void testSetMonthOfYear_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfMonth_int1
    public void testSetDayOfMonth_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setDayOfMonth(17);
        assertEquals("2002-06-17T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfMonth_int2
    public void testSetDayOfMonth_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setDayOfMonth(31);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfYear_int1
    public void testSetDayOfYear_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setDayOfYear(3);
        assertEquals("2002-01-03T05:06:07.008Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfYear_int2
    public void testSetDayOfYear_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setDayOfYear(366);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetWeekyear_int1
    public void testSetWeekyear_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setWeekyear(2001);
        assertEquals("2001-06-10T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetWeekOfWeekyear_int1
    public void testSetWeekOfWeekyear_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setWeekOfWeekyear(2);
        assertEquals("2002-01-13T05:06:07.008Z", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetWeekOfWeekyear_int2
    public void testSetWeekOfWeekyear_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setWeekOfWeekyear(53);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfWeek_int1
    public void testSetDayOfWeek_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setDayOfWeek(5);
        assertEquals("2002-06-07T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetDayOfWeek_int2
    public void testSetDayOfWeek_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setDayOfWeek(8);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetHourOfDay_int1
    public void testSetHourOfDay_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setHourOfDay(13);
        assertEquals("2002-06-09T13:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetHourOfDay_int2
    public void testSetHourOfDay_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setHourOfDay(24);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMinuteOfHour_int1
    public void testSetMinuteOfHour_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setMinuteOfHour(13);
        assertEquals("2002-06-09T05:13:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMinuteOfHour_int2
    public void testSetMinuteOfHour_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setMinuteOfHour(60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMinuteOfDay_int1
    public void testSetMinuteOfDay_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setMinuteOfDay(13);
        assertEquals("2002-06-09T00:13:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMinuteOfDay_int2
    public void testSetMinuteOfDay_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setMinuteOfDay(24 * 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetSecondOfMinute_int1
    public void testSetSecondOfMinute_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setSecondOfMinute(13);
        assertEquals("2002-06-09T05:06:13.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetSecondOfMinute_int2
    public void testSetSecondOfMinute_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setSecondOfMinute(60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetSecondOfDay_int1
    public void testSetSecondOfDay_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setSecondOfDay(13);
        assertEquals("2002-06-09T00:00:13.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetSecondOfDay_int2
    public void testSetSecondOfDay_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setSecondOfDay(24 * 60 * 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMilliOfSecond_int1
    public void testSetMilliOfSecond_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setMillisOfSecond(13);
        assertEquals("2002-06-09T05:06:07.013+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMilliOfSecond_int2
    public void testSetMilliOfSecond_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setMillisOfSecond(1000);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMilliOfDay_int1
    public void testSetMilliOfDay_int1() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        test.setMillisOfDay(13);
        assertEquals("2002-06-09T00:00:00.013+01:00", test.toString());
    }

// org.joda.time.TestMutableDateTime_Sets::testSetMilliOfDay_int2
    public void testSetMilliOfDay_int2() {
        MutableDateTime test = new MutableDateTime(2002, 6, 9, 5, 6, 7, 8);
        try {
            test.setMillisOfDay(24 * 60 * 60 * 1000);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals("2002-06-09T05:06:07.008+01:00", test.toString());
    }

// org.joda.time.TestMutableInterval_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableInterval_Basics::testGetMillis
    public void testGetMillis() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1, test.getStart().getMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
        assertEquals(TEST_TIME2, test.getEnd().getMillis());
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDurationMillis());
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDuration().getMillis());
    }

// org.joda.time.TestMutableInterval_Basics::testGetDuration1
    public void testGetDuration1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDurationMillis());
        assertEquals(TEST_TIME2 - TEST_TIME1, test.toDuration().getMillis());
    }

// org.joda.time.TestMutableInterval_Basics::testGetDuration2
    public void testGetDuration2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME1);
        assertSame(Duration.ZERO, test.toDuration());
    }

// org.joda.time.TestMutableInterval_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        MutableInterval test1 = new MutableInterval(TEST_TIME1, TEST_TIME2);
        MutableInterval test2 = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        MutableInterval test3 = new MutableInterval(TEST_TIME_NOW, TEST_TIME2);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        MutableInterval test4 = new MutableInterval(TEST_TIME1, TEST_TIME2, GJChronology.getInstance());
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

// org.joda.time.TestMutableInterval_Basics::testContains_long
    public void testContains_long() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(true, test.contains(TEST_TIME1));
        assertEquals(false, test.contains(TEST_TIME1 - 1));
        assertEquals(true, test.contains(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2));
        assertEquals(false, test.contains(TEST_TIME2));
        assertEquals(true, test.contains(TEST_TIME2 - 1));
    }

// org.joda.time.TestMutableInterval_Basics::testContainsNow
    public void testContainsNow() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1);
        assertEquals(true, test.containsNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1 - 1);
        assertEquals(false, test.containsNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2);
        assertEquals(true, test.containsNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2);
        assertEquals(false, test.containsNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2 - 1);
        assertEquals(true, test.containsNow());
    }

// org.joda.time.TestMutableInterval_Basics::testContains_RI
    public void testContains_RI() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        assertEquals(true, test.contains(new Instant(TEST_TIME1)));
        assertEquals(false, test.contains(new Instant(TEST_TIME1 - 1)));
        assertEquals(true, test.contains(new Instant(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2)));
        assertEquals(false, test.contains(new Instant(TEST_TIME2)));
        assertEquals(true, test.contains(new Instant(TEST_TIME2 - 1)));
        assertEquals(true, test.contains((ReadableInstant) null));
    }

// org.joda.time.TestMutableInterval_Basics::testContains_RInterval
    public void testContains_RInterval() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(true, test.contains(new Interval(TEST_TIME1, TEST_TIME1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 1, TEST_TIME1)));
        
        assertEquals(true, test.contains(new Interval(TEST_TIME1, TEST_TIME1 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 1, TEST_TIME1 + 1)));
        assertEquals(true, test.contains(new Interval(TEST_TIME1 + 1, TEST_TIME1 + 1)));
        
        assertEquals(true, test.contains(new Interval(TEST_TIME1, TEST_TIME2)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 1, TEST_TIME2)));
        assertEquals(true, test.contains(new Interval(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2, TEST_TIME2)));
        assertEquals(false, test.contains(new Interval(TEST_TIME2, TEST_TIME2)));
        assertEquals(true, test.contains(new Interval(TEST_TIME2 - 1, TEST_TIME2)));
        
        assertEquals(true, test.contains(new Interval(TEST_TIME1, TEST_TIME2 - 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 1, TEST_TIME2 - 1)));
        assertEquals(true, test.contains(new Interval(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2, TEST_TIME2 - 1)));
        assertEquals(true, test.contains(new Interval(TEST_TIME2 - 1, TEST_TIME2 - 1)));
        assertEquals(true, test.contains(new Interval(TEST_TIME2 - 2, TEST_TIME2 - 1)));
        
        assertEquals(false, test.contains(new Interval(TEST_TIME1, TEST_TIME2 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 1, TEST_TIME2 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2, TEST_TIME2 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME2, TEST_TIME2 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME2 - 1, TEST_TIME2 + 1)));
        assertEquals(false, test.contains(new Interval(TEST_TIME1 - 2, TEST_TIME1 - 1)));
        
        assertEquals(true, test.contains((ReadableInterval) null));
    }

// org.joda.time.TestMutableInterval_Basics::testOverlaps_RInterval
    public void testOverlaps_RInterval() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(false, test.overlaps(new Interval(TEST_TIME1, TEST_TIME1)));
        assertEquals(false, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME1)));
        
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1, TEST_TIME1 + 1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME1 + 1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 + 1, TEST_TIME1 + 1)));
        
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1, TEST_TIME2)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME2)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2, TEST_TIME2)));
        assertEquals(false, test.overlaps(new Interval(TEST_TIME2, TEST_TIME2)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME2 - 1, TEST_TIME2)));
        
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1, TEST_TIME2 + 1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME2 + 1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 + (TEST_TIME2 - TEST_TIME1) / 2, TEST_TIME2 + 1)));
        assertEquals(false, test.overlaps(new Interval(TEST_TIME2, TEST_TIME2 + 1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME2 - 1, TEST_TIME2 + 1)));
        
        assertEquals(false, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME1 - 1)));
        assertEquals(false, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME1)));
        assertEquals(true, test.overlaps(new Interval(TEST_TIME1 - 1, TEST_TIME1 + 1)));
        
        assertEquals(true, test.overlaps((ReadableInterval) null));
        
        MutableInterval empty = new MutableInterval(TEST_TIME1, TEST_TIME1);
        assertEquals(false, empty.overlaps(empty));
        assertEquals(false, empty.overlaps(test));
        assertEquals(false, test.overlaps(empty));
    }

// org.joda.time.TestMutableInterval_Basics::testIsBefore_long
    public void testIsBefore_long() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(false, test.isBefore(TEST_TIME1 - 1));
        assertEquals(false, test.isBefore(TEST_TIME1));
        assertEquals(false, test.isBefore(TEST_TIME1 + 1));
        
        assertEquals(false, test.isBefore(TEST_TIME2 - 1));
        assertEquals(true, test.isBefore(TEST_TIME2));
        assertEquals(true, test.isBefore(TEST_TIME2 + 1));
    }

// org.joda.time.TestMutableInterval_Basics::testIsBeforeNow
    public void testIsBeforeNow() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2 - 1);
        assertEquals(false, test.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2);
        assertEquals(true, test.isBeforeNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2 + 1);
        assertEquals(true, test.isBeforeNow());
    }

// org.joda.time.TestMutableInterval_Basics::testIsBefore_RI
    public void testIsBefore_RI() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(false, test.isBefore(new Instant(TEST_TIME1 - 1)));
        assertEquals(false, test.isBefore(new Instant(TEST_TIME1)));
        assertEquals(false, test.isBefore(new Instant(TEST_TIME1 + 1)));
        
        assertEquals(false, test.isBefore(new Instant(TEST_TIME2 - 1)));
        assertEquals(true, test.isBefore(new Instant(TEST_TIME2)));
        assertEquals(true, test.isBefore(new Instant(TEST_TIME2 + 1)));
        
        assertEquals(false, test.isBefore((ReadableInstant) null));
    }

// org.joda.time.TestMutableInterval_Basics::testIsBefore_RInterval
    public void testIsBefore_RInterval() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(false, test.isBefore(new Interval(Long.MIN_VALUE, TEST_TIME1 - 1)));
        assertEquals(false, test.isBefore(new Interval(Long.MIN_VALUE, TEST_TIME1)));
        assertEquals(false, test.isBefore(new Interval(Long.MIN_VALUE, TEST_TIME1 + 1)));
        
        assertEquals(false, test.isBefore(new Interval(TEST_TIME2 - 1, Long.MAX_VALUE)));
        assertEquals(true, test.isBefore(new Interval(TEST_TIME2, Long.MAX_VALUE)));
        assertEquals(true, test.isBefore(new Interval(TEST_TIME2 + 1, Long.MAX_VALUE)));
        
        assertEquals(false, test.isBefore((ReadableInterval) null));
    }

// org.joda.time.TestMutableInterval_Basics::testIsAfter_long
    public void testIsAfter_long() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(true, test.isAfter(TEST_TIME1 - 1));
        assertEquals(false, test.isAfter(TEST_TIME1));
        assertEquals(false, test.isAfter(TEST_TIME1 + 1));
        
        assertEquals(false, test.isAfter(TEST_TIME2 - 1));
        assertEquals(false, test.isAfter(TEST_TIME2));
        assertEquals(false, test.isAfter(TEST_TIME2 + 1));
    }

// org.joda.time.TestMutableInterval_Basics::testIsAfterNow
    public void testIsAfterNow() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1 - 1);
        assertEquals(true, test.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1);
        assertEquals(false, test.isAfterNow());
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME1 + 1);
        assertEquals(false, test.isAfterNow());
    }

// org.joda.time.TestMutableInterval_Basics::testIsAfter_RI
    public void testIsAfter_RI() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(true, test.isAfter(new Instant(TEST_TIME1 - 1)));
        assertEquals(false, test.isAfter(new Instant(TEST_TIME1)));
        assertEquals(false, test.isAfter(new Instant(TEST_TIME1 + 1)));
        
        assertEquals(false, test.isAfter(new Instant(TEST_TIME2 - 1)));
        assertEquals(false, test.isAfter(new Instant(TEST_TIME2)));
        assertEquals(false, test.isAfter(new Instant(TEST_TIME2 + 1)));
        
        assertEquals(false, test.isAfter((ReadableInstant) null));
    }

// org.joda.time.TestMutableInterval_Basics::testIsAfter_RInterval
    public void testIsAfter_RInterval() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        assertEquals(true, test.isAfter(new Interval(Long.MIN_VALUE, TEST_TIME1 - 1)));
        assertEquals(true, test.isAfter(new Interval(Long.MIN_VALUE, TEST_TIME1)));
        assertEquals(false, test.isAfter(new Interval(Long.MIN_VALUE, TEST_TIME1 + 1)));
        
        assertEquals(false, test.isAfter(new Interval(TEST_TIME2 - 1, Long.MAX_VALUE)));
        assertEquals(false, test.isAfter(new Interval(TEST_TIME2, Long.MAX_VALUE)));
        assertEquals(false, test.isAfter(new Interval(TEST_TIME2 + 1, Long.MAX_VALUE)));
        
        assertEquals(false, test.isAfter((ReadableInterval) null));
    }

// org.joda.time.TestMutableInterval_Basics::testToInterval1
    public void testToInterval1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval result = test.toInterval();
        assertEquals(test, result);
    }

// org.joda.time.TestMutableInterval_Basics::testToMutableInterval1
    public void testToMutableInterval1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        MutableInterval result = test.toMutableInterval();
        assertEquals(test, result);
        assertNotSame(test, result);
    }

// org.joda.time.TestMutableInterval_Basics::testToPeriod
    public void testToPeriod() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, COPTIC_PARIS);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, COPTIC_PARIS);
        MutableInterval base = new MutableInterval(dt1, dt2);
        
        Period test = base.toPeriod();
        Period expected = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(expected, test);
    }

// org.joda.time.TestMutableInterval_Basics::testToPeriod_PeriodType1
    public void testToPeriod_PeriodType1() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, COPTIC_PARIS);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, COPTIC_PARIS);
        MutableInterval base = new MutableInterval(dt1, dt2);
        
        Period test = base.toPeriod(null);
        Period expected = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(expected, test);
    }

// org.joda.time.TestMutableInterval_Basics::testToPeriod_PeriodType2
    public void testToPeriod_PeriodType2() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18);
        MutableInterval base = new MutableInterval(dt1, dt2);
        
        Period test = base.toPeriod(PeriodType.yearWeekDayTime());
        Period expected = new Period(dt1, dt2, PeriodType.yearWeekDayTime());
        assertEquals(expected, test);
    }

// org.joda.time.TestMutableInterval_Basics::testSerialization
    public void testSerialization() throws Exception {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        MutableInterval result = (MutableInterval) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestMutableInterval_Basics::testToString
    public void testToString() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10, DateTimeZone.UTC);
        DateTime dt2 = new DateTime(2005, 8, 13, 12, 14, 16, 18, DateTimeZone.UTC);
        MutableInterval test = new MutableInterval(dt1, dt2);
        assertEquals("2004-06-09T07:08:09.010/2005-08-13T12:14:16.018", test.toString());
    }

// org.joda.time.TestMutableInterval_Basics::testCopy
    public void testCopy() {
        MutableInterval test = new MutableInterval(123L, 456L, COPTIC_PARIS);
        MutableInterval cloned = test.copy();
        assertEquals(test, cloned);
        assertNotSame(test, cloned);
    }

// org.joda.time.TestMutableInterval_Basics::testClone
    public void testClone() {
        MutableInterval test = new MutableInterval(123L, 456L, COPTIC_PARIS);
        MutableInterval cloned = (MutableInterval) test.clone();
        assertEquals(test, cloned);
        assertNotSame(test, cloned);
    }

// org.joda.time.TestMutableInterval_Constructors::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableInterval_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        DateTime start = new DateTime(2010, 6, 30, 12, 30, ISOChronology.getInstance(PARIS));
        DateTime end = new DateTime(2010, 7, 1, 14, 30, ISOChronology.getInstance(PARIS));
        assertEquals(new MutableInterval(start, end), MutableInterval.parse("2010-06-30T12:30/2010-07-01T14:30"));
        assertEquals(new MutableInterval(start, end), MutableInterval.parse("2010-06-30T12:30/P1DT2H"));
        assertEquals(new MutableInterval(start, end), MutableInterval.parse("P1DT2H/2010-07-01T14:30"));
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        MutableInterval test = new MutableInterval();
        assertEquals(0L, test.getStartMillis());
        assertEquals(0L, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_long_long1
    public void testConstructor_long_long1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1.getMillis(), dt2.getMillis());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_long_long2
    public void testConstructor_long_long2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        MutableInterval test = new MutableInterval(dt1.getMillis(), dt1.getMillis());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt1.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_long_long3
    public void testConstructor_long_long3() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        DateTime dt2 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            new MutableInterval(dt1.getMillis(), dt2.getMillis());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_long_long_Chronology1
    public void testConstructor_long_long_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1.getMillis(), dt2.getMillis(), GJChronology.getInstance());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(GJChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_long_long_Chronology2
    public void testConstructor_long_long_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1.getMillis(), dt2.getMillis(), null);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI1
    public void testConstructor_RI_RI1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI2
    public void testConstructor_RI_RI2() throws Throwable {
        Instant dt1 = new Instant(new DateTime(2004, 6, 9, 0, 0, 0, 0));
        Instant dt2 = new Instant(new DateTime(2005, 7, 10, 1, 1, 1, 1));
        MutableInterval test = new MutableInterval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI3
    public void testConstructor_RI_RI3() throws Throwable {
        MutableInterval test = new MutableInterval((ReadableInstant) null, (ReadableInstant) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI4
    public void testConstructor_RI_RI4() throws Throwable {
        DateTime dt1 = new DateTime(2000, 6, 9, 0, 0, 0, 0);
        MutableInterval test = new MutableInterval(dt1, (ReadableInstant) null);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI5
    public void testConstructor_RI_RI5() throws Throwable {
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval((ReadableInstant) null, dt2);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI6
    public void testConstructor_RI_RI6() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        MutableInterval test = new MutableInterval(dt1, dt1);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt1.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RI7
    public void testConstructor_RI_RI7() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        DateTime dt2 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            new MutableInterval(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }
