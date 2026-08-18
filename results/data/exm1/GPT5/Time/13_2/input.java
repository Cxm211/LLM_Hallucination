// buggy code
        public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
            long valueLong = getFieldValue(period);
            if (valueLong == Long.MAX_VALUE) {
                return 0;
            }

            int sum = Math.max(FormatUtils.calculateDigitCount(valueLong), iMinPrintedDigits);
            if (iFieldType >= SECONDS_MILLIS) {
                // valueLong contains the seconds and millis fields
                // the minimum output is 0.000, which is 4 or 5 digits with a negative
                sum = Math.max(sum, 4);
                // plus one for the decimal point
                sum++;
                if (iFieldType == SECONDS_OPTIONAL_MILLIS &&
                        (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND) == 0) {
                    sum -= 4; // remove three digits and decimal point
                }
                // reset valueLong to refer to the seconds part for the prefic/suffix calculation
                valueLong = valueLong / DateTimeConstants.MILLIS_PER_SECOND;
            }
            int value = (int) valueLong;

            if (iPrefix != null) {
                sum += iPrefix.calculatePrintedLength(value);
            }
            if (iSuffix != null) {
                sum += iSuffix.calculatePrintedLength(value);
            }

            return sum;
        }

        public void printTo(StringBuffer buf, ReadablePeriod period, Locale locale) {
            long valueLong = getFieldValue(period);
            if (valueLong == Long.MAX_VALUE) {
                return;
            }
            int value = (int) valueLong;
            if (iFieldType >= SECONDS_MILLIS) {
                value = (int) (valueLong / DateTimeConstants.MILLIS_PER_SECOND);
            }

            if (iPrefix != null) {
                iPrefix.printTo(buf, value);
            }
            int minDigits = iMinPrintedDigits;
            if (minDigits <= 1) {
                FormatUtils.appendUnpaddedInteger(buf, value);
            } else {
                FormatUtils.appendPaddedInteger(buf, value, minDigits);
            }
            if (iFieldType >= SECONDS_MILLIS) {
                int dp = (int) (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND);
                if (iFieldType == SECONDS_MILLIS || dp > 0) {
                    buf.append('.');
                    FormatUtils.appendPaddedInteger(buf, dp, 3);
                }
            }
            if (iSuffix != null) {
                iSuffix.printTo(buf, value);
            }
        }

// relevant test
// org.joda.time.TestPeriod_Basics::testNormalizedStandard_yearMonth2
    public void testNormalizedStandard_yearMonth2() {
        Period test = new Period(Integer.MAX_VALUE, 15, 0, 0, 0, 0, 0, 0);
        try {
            test.normalizedStandard();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_weekDay1
    public void testNormalizedStandard_weekDay1() {
        Period test = new Period(0, 0, 1, 12, 0, 0, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(0, 0, 1, 12, 0, 0, 0, 0), test);
        assertEquals(new Period(0, 0, 2, 5, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_weekDay2
    public void testNormalizedStandard_weekDay2() {
        Period test = new Period(0, 0, Integer.MAX_VALUE, 7, 0, 0, 0, 0);
        try {
            test.normalizedStandard();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_yearMonthWeekDay
    public void testNormalizedStandard_yearMonthWeekDay() {
        Period test = new Period(1, 15, 1, 12, 0, 0, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(1, 15, 1, 12, 0, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 2, 5, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_yearMonthDay
    public void testNormalizedStandard_yearMonthDay() {
        Period test = new Period(1, 15, 0, 36, 0, 0, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(1, 15, 0, 36, 0, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 5, 1, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_negative
    public void testNormalizedStandard_negative() {
        Period test = new Period(0, 0, 0, 0, 2, -10, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(0, 0, 0, 0, 2, -10, 0, 0), test);
        assertEquals(new Period(0, 0, 0, 0, 1, 50, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_fullNegative
    public void testNormalizedStandard_fullNegative() {
        Period test = new Period(0, 0, 0, 0, 1, -70, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(0, 0, 0, 0, 1, -70, 0, 0), test);
        assertEquals(new Period(0, 0, 0, 0, 0, -10, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_yearMonth1
    public void testNormalizedStandard_periodType_yearMonth1() {
        Period test = new Period(1, 15, 0, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard((PeriodType) null);
        assertEquals(new Period(1, 15, 0, 0, 0, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 0, 0, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_yearMonth2
    public void testNormalizedStandard_periodType_yearMonth2() {
        Period test = new Period(Integer.MAX_VALUE, 15, 0, 0, 0, 0, 0, 0);
        try {
            test.normalizedStandard((PeriodType) null);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_yearMonth3
    public void testNormalizedStandard_periodType_yearMonth3() {
        Period test = new Period(1, 15, 3, 4, 0, 0, 0, 0);
        try {
            test.normalizedStandard(PeriodType.dayTime());
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_weekDay1
    public void testNormalizedStandard_periodType_weekDay1() {
        Period test = new Period(0, 0, 1, 12, 0, 0, 0, 0);
        Period result = test.normalizedStandard((PeriodType) null);
        assertEquals(new Period(0, 0, 1, 12, 0, 0, 0, 0), test);
        assertEquals(new Period(0, 0, 2, 5, 0, 0, 0, 0), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_weekDay2
    public void testNormalizedStandard_periodType_weekDay2() {
        Period test = new Period(0, 0, Integer.MAX_VALUE, 7, 0, 0, 0, 0);
        try {
            test.normalizedStandard((PeriodType) null);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_weekDay3
    public void testNormalizedStandard_periodType_weekDay3() {
        Period test = new Period(0, 0, 1, 12, 0, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.dayTime());
        assertEquals(new Period(0, 0, 1, 12, 0, 0, 0, 0), test);
        assertEquals(new Period(0, 0, 0, 19, 0, 0, 0, 0, PeriodType.dayTime()), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_yearMonthWeekDay
    public void testNormalizedStandard_periodType_yearMonthWeekDay() {
        Period test = new Period(1, 15, 1, 12, 0, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.yearMonthDayTime());
        assertEquals(new Period(1, 15, 1, 12, 0, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 0, 19, 0, 0, 0, 0, PeriodType.yearMonthDayTime()), result);
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_periodType_yearMonthDay
    public void testNormalizedStandard_periodType_yearMonthDay() {
        Period test = new Period(1, 15, 0, 36, 27, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.yearMonthDayTime());
        assertEquals(new Period(1, 15, 0, 36, 27, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 0, 37, 3, 0, 0, 0, PeriodType.yearMonthDayTime()), result);
    }

// org.joda.time.TestPeriod_Constructors::testConstants
    public void testConstants() throws Throwable {
        Period test = Period.ZERO;
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 890), Period.parse("P1Y2M3W4DT5H6M7.890S"));
    }

// org.joda.time.TestPeriod_Constructors::testConstructor1
    public void testConstructor1() throws Throwable {
        Period test = new Period();
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        long length =
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long3
    public void testConstructor_long3() throws Throwable {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        Period test = new Period(length);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_fixedZone
    public void testConstructor_long_fixedZone() throws Throwable {
        DateTimeZone zone = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.forOffsetHours(2));
            long length =
                (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
                5L * DateTimeConstants.MILLIS_PER_HOUR +
                6L * DateTimeConstants.MILLIS_PER_MINUTE +
                7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
            Period test = new Period(length);
            assertEquals(PeriodType.standard(), test.getPeriodType());
            
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

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType1
    public void testConstructor_long_PeriodType1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, (PeriodType) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType2
    public void testConstructor_long_PeriodType2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.millis());
        assertEquals(PeriodType.millis(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(length, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType3
    public void testConstructor_long_PeriodType3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.dayTime());
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType4
    public void testConstructor_long_PeriodType4() throws Throwable {
        long length =
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.standard().withMillisRemoved());
        assertEquals(PeriodType.standard().withMillisRemoved(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_Chronology1
    public void testConstructor_long_Chronology1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, ISOChronology.getInstance());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_Chronology2
    public void testConstructor_long_Chronology2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, ISOChronology.getInstanceUTC());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_Chronology3
    public void testConstructor_long_Chronology3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, (Chronology) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType_Chronology1
    public void testConstructor_long_PeriodType_Chronology1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.time().withMillisRemoved(), ISOChronology.getInstance());
        assertEquals(PeriodType.time().withMillisRemoved(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType_Chronology2
    public void testConstructor_long_PeriodType_Chronology2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType_Chronology3
    public void testConstructor_long_PeriodType_Chronology3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, PeriodType.standard(), (Chronology) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_PeriodType_Chronology4
    public void testConstructor_long_PeriodType_Chronology4() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        Period test = new Period(length, (PeriodType) null, (Chronology) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_4int1
    public void testConstructor_4int1() throws Throwable {
        Period test = new Period(5, 6, 7, 8);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_8int1
    public void testConstructor_8int1() throws Throwable {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_8int__PeriodType1
    public void testConstructor_8int__PeriodType1() throws Throwable {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8, null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_8int__PeriodType2
    public void testConstructor_8int__PeriodType2() throws Throwable {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.dayTime());
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_8int__PeriodType3
    public void testConstructor_8int__PeriodType3() throws Throwable {
        try {
            new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.dayTime());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long1
    public void testConstructor_long_long1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long2
    public void testConstructor_long_long2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_PeriodType1
    public void testConstructor_long_long_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), (PeriodType) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_PeriodType2
    public void testConstructor_long_long_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), PeriodType.dayTime());
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(31, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_PeriodType3
    public void testConstructor_long_long_PeriodType3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 6, 9, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), PeriodType.standard().withMillisRemoved());
        assertEquals(PeriodType.standard().withMillisRemoved(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testToPeriod_PeriodType3
    public void testToPeriod_PeriodType3() {
        DateTime dt1 = new DateTime(2004, 6, 9, 7, 8, 9, 10);
        DateTime dt2 = new DateTime(2005, 6, 9, 12, 14, 16, 18);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), PeriodType.yearWeekDayTime());
        
        assertEquals(PeriodType.yearWeekDayTime(), test.getPeriodType());
        assertEquals(1, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_Chronology1
    public void testConstructor_long_long_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, CopticChronology.getInstance());
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, CopticChronology.getInstance());
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), CopticChronology.getInstance());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_Chronology2
    public void testConstructor_long_long_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), (Chronology) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_PeriodType_Chronology1
    public void testConstructor_long_long_PeriodType_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, CopticChronology.getInstance());
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, CopticChronology.getInstance());
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), (PeriodType) null, CopticChronology.getInstance());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_long_long_PeriodType_Chronology2
    public void testConstructor_long_long_PeriodType_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1.getMillis(), dt2.getMillis(), (PeriodType) null, null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI1
    public void testConstructor_RI_RI1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI2
    public void testConstructor_RI_RI2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI3
    public void testConstructor_RI_RI3() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(3, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI4
    public void testConstructor_RI_RI4() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        DateTime dt2 = null;  
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(-3, test.getYears());
        assertEquals(-1, test.getMonths());
        assertEquals(-1, test.getWeeks());
        assertEquals(-1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(-1, test.getMinutes());
        assertEquals(-1, test.getSeconds());
        assertEquals(-1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI5
    public void testConstructor_RI_RI5() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = null;  
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI_PeriodType1
    public void testConstructor_RI_RI_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2, null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI_PeriodType2
    public void testConstructor_RI_RI_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 7, 10, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2, PeriodType.dayTime());
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(31, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI_PeriodType3
    public void testConstructor_RI_RI_PeriodType3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 6, 9, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2, PeriodType.standard().withMillisRemoved());
        assertEquals(PeriodType.standard().withMillisRemoved(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI_PeriodType4
    public void testConstructor_RI_RI_PeriodType4() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        Period test = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(3, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RI_PeriodType5
    public void testConstructor_RI_RI_PeriodType5() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = null;  
        Period test = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP1
    public void testConstructor_RP_RP1() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2004, 6, 9);
        YearMonthDay dt2 = new YearMonthDay(2005, 7, 10);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP2
    public void testConstructor_RP_RP2() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2004, 6, 9);
        YearMonthDay dt2 = new YearMonthDay(2005, 5, 17);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(11, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP2Local
    public void testConstructor_RP_RP2Local() throws Throwable {
        LocalDate dt1 = new LocalDate(2004, 6, 9);
        LocalDate dt2 = new LocalDate(2005, 5, 17);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(11, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP3
    public void testConstructor_RP_RP3() throws Throwable {
        YearMonthDay dt1 = null;
        YearMonthDay dt2 = new YearMonthDay(2005, 7, 17);
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP4
    public void testConstructor_RP_RP4() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2005, 7, 17);
        YearMonthDay dt2 = null;
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP5
    public void testConstructor_RP_RP5() throws Throwable {
        YearMonthDay dt1 = null;
        YearMonthDay dt2 = null;
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP6
    public void testConstructor_RP_RP6() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2005, 7, 17);
        TimeOfDay dt2 = new TimeOfDay(10, 20, 30, 40);
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP7
    public void testConstructor_RP_RP7() throws Throwable {
        Partial dt1 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.monthOfYear(), 12);
        Partial dt2 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 14);
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP8
    public void testConstructor_RP_RP8() throws Throwable {
        Partial dt1 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 12);
        Partial dt2 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 14);
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType1
    public void testConstructor_RP_RP_PeriodType1() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2004, 6, 9);
        YearMonthDay dt2 = new YearMonthDay(2005, 7, 10);
        Period test = new Period(dt1, dt2, PeriodType.standard());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType2
    public void testConstructor_RP_RP_PeriodType2() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2004, 6, 9);
        YearMonthDay dt2 = new YearMonthDay(2005, 5, 17);
        Period test = new Period(dt1, dt2, PeriodType.yearMonthDay());
        assertEquals(PeriodType.yearMonthDay(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(11, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(8, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType2Local
    public void testConstructor_RP_RP_PeriodType2Local() throws Throwable {
        LocalDate dt1 = new LocalDate(2004, 6, 9);
        LocalDate dt2 = new LocalDate(2005, 5, 17);
        Period test = new Period(dt1, dt2, PeriodType.yearMonthDay());
        assertEquals(PeriodType.yearMonthDay(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(11, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(8, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType3
    public void testConstructor_RP_RP_PeriodType3() throws Throwable {
        YearMonthDay dt1 = null;
        YearMonthDay dt2 = new YearMonthDay(2005, 7, 17);
        try {
            new Period(dt1, dt2, PeriodType.standard());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType4
    public void testConstructor_RP_RP_PeriodType4() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2005, 7, 17);
        YearMonthDay dt2 = null;
        try {
            new Period(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType5
    public void testConstructor_RP_RP_PeriodType5() throws Throwable {
        YearMonthDay dt1 = null;
        YearMonthDay dt2 = null;
        try {
            new Period(dt1, dt2, PeriodType.standard());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType6
    public void testConstructor_RP_RP_PeriodType6() throws Throwable {
        YearMonthDay dt1 = new YearMonthDay(2005, 7, 17);
        TimeOfDay dt2 = new TimeOfDay(10, 20, 30, 40);
        try {
            new Period(dt1, dt2, PeriodType.standard());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType7
    public void testConstructor_RP_RP_PeriodType7() throws Throwable {
        Partial dt1 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.monthOfYear(), 12);
        Partial dt2 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 14);
        try {
            new Period(dt1, dt2, PeriodType.standard());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RP_RP_PeriodType8
    public void testConstructor_RP_RP_PeriodType8() throws Throwable {
        Partial dt1 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 12);
        Partial dt2 = new Partial().with(DateTimeFieldType.year(), 2005).with(DateTimeFieldType.hourOfDay(), 14);
        try {
            new Period(dt1, dt2, PeriodType.standard());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RD1
    public void testConstructor_RI_RD1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        Period test = new Period(dt1, dur);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RD2
    public void testConstructor_RI_RD2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        Period test = new Period(dt1, dur);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RD_PeriodType1
    public void testConstructor_RI_RD_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        Period test = new Period(dt1, dur, PeriodType.yearDayTime());
        assertEquals(PeriodType.yearDayTime(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(31, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RI_RD_PeriodType2
    public void testConstructor_RI_RD_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        Period test = new Period(dt1, dur, (PeriodType) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RD_RI1
    public void testConstructor_RD_RI1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        Period test = new Period(dur, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RD_RI2
    public void testConstructor_RD_RI2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        Period test = new Period(dur, dt1);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RD_RI_PeriodType1
    public void testConstructor_RD_RI_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        Period test = new Period(dur, dt2, PeriodType.yearDayTime());
        assertEquals(PeriodType.yearDayTime(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(31, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_RD_RI_PeriodType2
    public void testConstructor_RD_RI_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        Period test = new Period(dur, dt1, (PeriodType) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        Period test = new Period("P1Y2M3D");
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(3, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        Period test = new Period((Object) null);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_Object3
    public void testConstructor_Object3() throws Throwable {
        Period test = new Period(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()));
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(2, test.getMinutes());
        assertEquals(3, test.getSeconds());
        assertEquals(4, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_Object4
    public void testConstructor_Object4() throws Throwable {
        Period base = new Period(1, 1, 0, 1, 1, 1, 1, 1, PeriodType.standard());
        Period test = new Period(base);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_Object_PeriodType1
    public void testConstructor_Object_PeriodType1() throws Throwable {
        Period test = new Period("P1Y2M3D", PeriodType.yearMonthDayTime());
        assertEquals(PeriodType.yearMonthDayTime(), test.getPeriodType());
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(3, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_Object_PeriodType2
    public void testConstructor_Object_PeriodType2() throws Throwable {
        Period test = new Period((Object) null, PeriodType.yearMonthDayTime());
        assertEquals(PeriodType.yearMonthDayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_Object_PeriodType3
    public void testConstructor_Object_PeriodType3() throws Throwable {
        Period test = new Period(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()), PeriodType.yearMonthDayTime());
        assertEquals(PeriodType.yearMonthDayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(2, test.getMinutes());
        assertEquals(3, test.getSeconds());
        assertEquals(4, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_Object_PeriodType4
    public void testConstructor_Object_PeriodType4() throws Throwable {
        Period test = new Period(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()), (PeriodType) null);
        assertEquals(PeriodType.dayTime(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(2, test.getMinutes());
        assertEquals(3, test.getSeconds());
        assertEquals(4, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryYears
    public void testFactoryYears() throws Throwable {
        Period test = Period.years(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(6, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryMonths
    public void testFactoryMonths() throws Throwable {
        Period test = Period.months(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(6, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryWeeks
    public void testFactoryWeeks() throws Throwable {
        Period test = Period.weeks(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(6, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryDays
    public void testFactoryDays() throws Throwable {
        Period test = Period.days(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(6, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryHours
    public void testFactoryHours() throws Throwable {
        Period test = Period.hours(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(6, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryMinutes
    public void testFactoryMinutes() throws Throwable {
        Period test = Period.minutes(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactorySeconds
    public void testFactorySeconds() throws Throwable {
        Period test = Period.seconds(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(6, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryMillis
    public void testFactoryMillis() throws Throwable {
        Period test = Period.millis(6);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(6, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryFieldDifference1
    public void testFactoryFieldDifference1() throws Throwable {
        YearMonthDay start = new YearMonthDay(2005, 4, 9);
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth(),
        };
        Partial end = new Partial(types, new int[] {2004, 6, 7});
        Period test = Period.fieldDifference(start, end);
        assertEquals(PeriodType.yearMonthDay(), test.getPeriodType());
        assertEquals(-1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(-2, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Constructors::testFactoryFieldDifference2
    public void testFactoryFieldDifference2() throws Throwable {
        YearMonthDay ymd = new YearMonthDay(2005, 4, 9);
        try {
            Period.fieldDifference(ymd, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            Period.fieldDifference((ReadablePartial) null, ymd);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testFactoryFieldDifference3
    public void testFactoryFieldDifference3() throws Throwable {
        YearMonthDay start = new YearMonthDay(2005, 4, 9);
        TimeOfDay endTime = new TimeOfDay(12, 30, 40, 0);
        try {
            Period.fieldDifference(start, endTime);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testFactoryFieldDifference4
    public void testFactoryFieldDifference4() throws Throwable {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfWeek(),
        };
        YearMonthDay start = new YearMonthDay(2005, 4, 9);
        Partial end = new Partial(types, new int[] {1, 2, 3});
        try {
            Period.fieldDifference(start, end);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Constructors::testFactoryFieldDifference5
    public void testFactoryFieldDifference5() throws Throwable {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.dayOfWeek(),
        };
        Partial start = new Partial(types, new int[] {1, 2, 3});
        Partial end = new Partial(types, new int[] {1, 2, 3});
        try {
            Period.fieldDifference(start, end);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestSeconds::testConstants
    public void testConstants() {
        assertEquals(0, Seconds.ZERO.getSeconds());
        assertEquals(1, Seconds.ONE.getSeconds());
        assertEquals(2, Seconds.TWO.getSeconds());
        assertEquals(3, Seconds.THREE.getSeconds());
        assertEquals(Integer.MAX_VALUE, Seconds.MAX_VALUE.getSeconds());
        assertEquals(Integer.MIN_VALUE, Seconds.MIN_VALUE.getSeconds());
    }

// org.joda.time.TestSeconds::testFactory_seconds_int
    public void testFactory_seconds_int() {
        assertSame(Seconds.ZERO, Seconds.seconds(0));
        assertSame(Seconds.ONE, Seconds.seconds(1));
        assertSame(Seconds.TWO, Seconds.seconds(2));
        assertSame(Seconds.THREE, Seconds.seconds(3));
        assertSame(Seconds.MAX_VALUE, Seconds.seconds(Integer.MAX_VALUE));
        assertSame(Seconds.MIN_VALUE, Seconds.seconds(Integer.MIN_VALUE));
        assertEquals(-1, Seconds.seconds(-1).getSeconds());
        assertEquals(4, Seconds.seconds(4).getSeconds());
    }

// org.joda.time.TestSeconds::testFactory_secondsBetween_RInstant
    public void testFactory_secondsBetween_RInstant() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 3, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 9, 12, 0, 6, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 9, 12, 0, 9, 0, PARIS);
        
        assertEquals(3, Seconds.secondsBetween(start, end1).getSeconds());
        assertEquals(0, Seconds.secondsBetween(start, start).getSeconds());
        assertEquals(0, Seconds.secondsBetween(end1, end1).getSeconds());
        assertEquals(-3, Seconds.secondsBetween(end1, start).getSeconds());
        assertEquals(6, Seconds.secondsBetween(start, end2).getSeconds());
    }

// org.joda.time.TestSeconds::testFactory_secondsBetween_RPartial
    public void testFactory_secondsBetween_RPartial() {
        LocalTime start = new LocalTime(12, 0, 3);
        LocalTime end1 = new LocalTime(12, 0, 6);
        TimeOfDay end2 = new TimeOfDay(12, 0, 9);
        
        assertEquals(3, Seconds.secondsBetween(start, end1).getSeconds());
        assertEquals(0, Seconds.secondsBetween(start, start).getSeconds());
        assertEquals(0, Seconds.secondsBetween(end1, end1).getSeconds());
        assertEquals(-3, Seconds.secondsBetween(end1, start).getSeconds());
        assertEquals(6, Seconds.secondsBetween(start, end2).getSeconds());
    }

// org.joda.time.TestSeconds::testFactory_secondsIn_RInterval
    public void testFactory_secondsIn_RInterval() {
        DateTime start = new DateTime(2006, 6, 9, 12, 0, 3, 0, PARIS);
        DateTime end1 = new DateTime(2006, 6, 9, 12, 0, 6, 0, PARIS);
        DateTime end2 = new DateTime(2006, 6, 9, 12, 0, 9, 0, PARIS);
        
        assertEquals(0, Seconds.secondsIn((ReadableInterval) null).getSeconds());
        assertEquals(3, Seconds.secondsIn(new Interval(start, end1)).getSeconds());
        assertEquals(0, Seconds.secondsIn(new Interval(start, start)).getSeconds());
        assertEquals(0, Seconds.secondsIn(new Interval(end1, end1)).getSeconds());
        assertEquals(6, Seconds.secondsIn(new Interval(start, end2)).getSeconds());
    }

// org.joda.time.TestSeconds::testFactory_standardSecondsIn_RPeriod
    public void testFactory_standardSecondsIn_RPeriod() {
        assertEquals(0, Seconds.standardSecondsIn((ReadablePeriod) null).getSeconds());
        assertEquals(0, Seconds.standardSecondsIn(Period.ZERO).getSeconds());
        assertEquals(1, Seconds.standardSecondsIn(new Period(0, 0, 0, 0, 0, 0, 1, 0)).getSeconds());
        assertEquals(123, Seconds.standardSecondsIn(Period.seconds(123)).getSeconds());
        assertEquals(-987, Seconds.standardSecondsIn(Period.seconds(-987)).getSeconds());
        assertEquals(2 * 24 * 60 * 60, Seconds.standardSecondsIn(Period.days(2)).getSeconds());
        try {
            Seconds.standardSecondsIn(Period.months(1));
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testFactory_parseSeconds_String
    public void testFactory_parseSeconds_String() {
        assertEquals(0, Seconds.parseSeconds((String) null).getSeconds());
        assertEquals(0, Seconds.parseSeconds("PT0S").getSeconds());
        assertEquals(1, Seconds.parseSeconds("PT1S").getSeconds());
        assertEquals(-3, Seconds.parseSeconds("PT-3S").getSeconds());
        assertEquals(2, Seconds.parseSeconds("P0Y0M0DT2S").getSeconds());
        assertEquals(2, Seconds.parseSeconds("PT0H2S").getSeconds());
        try {
            Seconds.parseSeconds("P1Y1D");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            Seconds.parseSeconds("P1DT1S");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testGetMethods
    public void testGetMethods() {
        Seconds test = Seconds.seconds(20);
        assertEquals(20, test.getSeconds());
    }

// org.joda.time.TestSeconds::testGetFieldType
    public void testGetFieldType() {
        Seconds test = Seconds.seconds(20);
        assertEquals(DurationFieldType.seconds(), test.getFieldType());
    }

// org.joda.time.TestSeconds::testGetPeriodType
    public void testGetPeriodType() {
        Seconds test = Seconds.seconds(20);
        assertEquals(PeriodType.seconds(), test.getPeriodType());
    }

// org.joda.time.TestSeconds::testIsGreaterThan
    public void testIsGreaterThan() {
        assertEquals(true, Seconds.THREE.isGreaterThan(Seconds.TWO));
        assertEquals(false, Seconds.THREE.isGreaterThan(Seconds.THREE));
        assertEquals(false, Seconds.TWO.isGreaterThan(Seconds.THREE));
        assertEquals(true, Seconds.ONE.isGreaterThan(null));
        assertEquals(false, Seconds.seconds(-1).isGreaterThan(null));
    }

// org.joda.time.TestSeconds::testIsLessThan
    public void testIsLessThan() {
        assertEquals(false, Seconds.THREE.isLessThan(Seconds.TWO));
        assertEquals(false, Seconds.THREE.isLessThan(Seconds.THREE));
        assertEquals(true, Seconds.TWO.isLessThan(Seconds.THREE));
        assertEquals(false, Seconds.ONE.isLessThan(null));
        assertEquals(true, Seconds.seconds(-1).isLessThan(null));
    }

// org.joda.time.TestSeconds::testToString
    public void testToString() {
        Seconds test = Seconds.seconds(20);
        assertEquals("PT20S", test.toString());
        
        test = Seconds.seconds(-20);
        assertEquals("PT-20S", test.toString());
    }

// org.joda.time.TestSeconds::testSerialization
    public void testSerialization() throws Exception {
        Seconds test = Seconds.THREE;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Seconds result = (Seconds) ois.readObject();
        ois.close();
        
        assertSame(test, result);
    }

// org.joda.time.TestSeconds::testToStandardWeeks
    public void testToStandardWeeks() {
        Seconds test = Seconds.seconds(60 * 60 * 24 * 7 * 2);
        Weeks expected = Weeks.weeks(2);
        assertEquals(expected, test.toStandardWeeks());
    }

// org.joda.time.TestSeconds::testToStandardDays
    public void testToStandardDays() {
        Seconds test = Seconds.seconds(60 * 60 * 24 * 2);
        Days expected = Days.days(2);
        assertEquals(expected, test.toStandardDays());
    }

// org.joda.time.TestSeconds::testToStandardHours
    public void testToStandardHours() {
        Seconds test = Seconds.seconds(60 * 60 * 2);
        Hours expected = Hours.hours(2);
        assertEquals(expected, test.toStandardHours());
    }

// org.joda.time.TestSeconds::testToStandardMinutes
    public void testToStandardMinutes() {
        Seconds test = Seconds.seconds(60 * 2);
        Minutes expected = Minutes.minutes(2);
        assertEquals(expected, test.toStandardMinutes());
    }

// org.joda.time.TestSeconds::testToStandardDuration
    public void testToStandardDuration() {
        Seconds test = Seconds.seconds(20);
        Duration expected = new Duration(20L * DateTimeConstants.MILLIS_PER_SECOND);
        assertEquals(expected, test.toStandardDuration());
        
        expected = new Duration(((long) Integer.MAX_VALUE) * DateTimeConstants.MILLIS_PER_SECOND);
        assertEquals(expected, Seconds.MAX_VALUE.toStandardDuration());
    }

// org.joda.time.TestSeconds::testPlus_int
    public void testPlus_int() {
        Seconds test2 = Seconds.seconds(2);
        Seconds result = test2.plus(3);
        assertEquals(2, test2.getSeconds());
        assertEquals(5, result.getSeconds());
        
        assertEquals(1, Seconds.ONE.plus(0).getSeconds());
        
        try {
            Seconds.MAX_VALUE.plus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testPlus_Seconds
    public void testPlus_Seconds() {
        Seconds test2 = Seconds.seconds(2);
        Seconds test3 = Seconds.seconds(3);
        Seconds result = test2.plus(test3);
        assertEquals(2, test2.getSeconds());
        assertEquals(3, test3.getSeconds());
        assertEquals(5, result.getSeconds());
        
        assertEquals(1, Seconds.ONE.plus(Seconds.ZERO).getSeconds());
        assertEquals(1, Seconds.ONE.plus((Seconds) null).getSeconds());
        
        try {
            Seconds.MAX_VALUE.plus(Seconds.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testMinus_int
    public void testMinus_int() {
        Seconds test2 = Seconds.seconds(2);
        Seconds result = test2.minus(3);
        assertEquals(2, test2.getSeconds());
        assertEquals(-1, result.getSeconds());
        
        assertEquals(1, Seconds.ONE.minus(0).getSeconds());
        
        try {
            Seconds.MIN_VALUE.minus(1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testMinus_Seconds
    public void testMinus_Seconds() {
        Seconds test2 = Seconds.seconds(2);
        Seconds test3 = Seconds.seconds(3);
        Seconds result = test2.minus(test3);
        assertEquals(2, test2.getSeconds());
        assertEquals(3, test3.getSeconds());
        assertEquals(-1, result.getSeconds());
        
        assertEquals(1, Seconds.ONE.minus(Seconds.ZERO).getSeconds());
        assertEquals(1, Seconds.ONE.minus((Seconds) null).getSeconds());
        
        try {
            Seconds.MIN_VALUE.minus(Seconds.ONE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testMultipliedBy_int
    public void testMultipliedBy_int() {
        Seconds test = Seconds.seconds(2);
        assertEquals(6, test.multipliedBy(3).getSeconds());
        assertEquals(2, test.getSeconds());
        assertEquals(-6, test.multipliedBy(-3).getSeconds());
        assertSame(test, test.multipliedBy(1));
        
        Seconds halfMax = Seconds.seconds(Integer.MAX_VALUE / 2 + 1);
        try {
            halfMax.multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testDividedBy_int
    public void testDividedBy_int() {
        Seconds test = Seconds.seconds(12);
        assertEquals(6, test.dividedBy(2).getSeconds());
        assertEquals(12, test.getSeconds());
        assertEquals(4, test.dividedBy(3).getSeconds());
        assertEquals(3, test.dividedBy(4).getSeconds());
        assertEquals(2, test.dividedBy(5).getSeconds());
        assertEquals(2, test.dividedBy(6).getSeconds());
        assertSame(test, test.dividedBy(1));
        
        try {
            Seconds.ONE.dividedBy(0);
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testNegated
    public void testNegated() {
        Seconds test = Seconds.seconds(12);
        assertEquals(-12, test.negated().getSeconds());
        assertEquals(12, test.getSeconds());
        
        try {
            Seconds.MIN_VALUE.negated();
            fail();
        } catch (ArithmeticException ex) {
            
        }
    }

// org.joda.time.TestSeconds::testAddToLocalDate
    public void testAddToLocalDate() {
        Seconds test = Seconds.seconds(26);
        LocalDateTime date = new LocalDateTime(2006, 6, 1, 0, 0, 0, 0);
        LocalDateTime expected = new LocalDateTime(2006, 6, 1, 0, 0, 26, 0);
        assertEquals(expected, date.plus(test));
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

// org.joda.time.format.TestISOPeriodFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        ISOPeriodFormat f = new ISOPeriodFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatStandard
    public void testFormatStandard() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", ISOPeriodFormat.standard().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P1Y2M3W4DT5H6M7S", ISOPeriodFormat.standard().print(p));
        
        p = new Period(0);
        assertEquals("PT0S", ISOPeriodFormat.standard().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("PT0M", ISOPeriodFormat.standard().print(p));
        
        assertEquals("P1Y4DT5H6M7.008S", ISOPeriodFormat.standard().print(YEAR_DAY_PERIOD));
        assertEquals("PT0S", ISOPeriodFormat.standard().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P1Y2M3W4D", ISOPeriodFormat.standard().print(DATE_PERIOD));
        assertEquals("PT5H6M7.008S", ISOPeriodFormat.standard().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatStandard_negative
    public void testFormatStandard_negative() {
        Period p = new Period(-1, -2, -3, -4, -5, -6, -7, -8);
        assertEquals("P-1Y-2M-3W-4DT-5H-6M-7.008S", ISOPeriodFormat.standard().print(p));
        
        p = Period.years(-54);
        assertEquals("P-54Y", ISOPeriodFormat.standard().print(p));
        
        p = Period.seconds(4).withMillis(-8);
        assertEquals("PT3.992S", ISOPeriodFormat.standard().print(p));
        
        p = Period.seconds(-4).withMillis(8);
        assertEquals("PT-3.992S", ISOPeriodFormat.standard().print(p));
        
        p = Period.seconds(-23);
        assertEquals("PT-23S", ISOPeriodFormat.standard().print(p));
        
        p = Period.millis(-8);
        assertEquals("PT-0.008S", ISOPeriodFormat.standard().print(p));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternate
    public void testFormatAlternate() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P00010204T050607.008", ISOPeriodFormat.alternate().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P00010204T050607", ISOPeriodFormat.alternate().print(p));
        
        p = new Period(0);
        assertEquals("P00000000T000000", ISOPeriodFormat.alternate().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P00000000T000000", ISOPeriodFormat.alternate().print(p));
        
        assertEquals("P00010004T050607.008", ISOPeriodFormat.alternate().print(YEAR_DAY_PERIOD));
        assertEquals("P00000000T000000", ISOPeriodFormat.alternate().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P00010204T000000", ISOPeriodFormat.alternate().print(DATE_PERIOD));
        assertEquals("P00000000T050607.008", ISOPeriodFormat.alternate().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternateExtended
    public void testFormatAlternateExtended() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P0001-02-04T05:06:07.008", ISOPeriodFormat.alternateExtended().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P0001-02-04T05:06:07", ISOPeriodFormat.alternateExtended().print(p));
        
        p = new Period(0);
        assertEquals("P0000-00-00T00:00:00", ISOPeriodFormat.alternateExtended().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P0000-00-00T00:00:00", ISOPeriodFormat.alternateExtended().print(p));
        
        assertEquals("P0001-00-04T05:06:07.008", ISOPeriodFormat.alternateExtended().print(YEAR_DAY_PERIOD));
        assertEquals("P0000-00-00T00:00:00", ISOPeriodFormat.alternateExtended().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P0001-02-04T00:00:00", ISOPeriodFormat.alternateExtended().print(DATE_PERIOD));
        assertEquals("P0000-00-00T05:06:07.008", ISOPeriodFormat.alternateExtended().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternateWithWeeks
    public void testFormatAlternateWithWeeks() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P0001W0304T050607.008", ISOPeriodFormat.alternateWithWeeks().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P0001W0304T050607", ISOPeriodFormat.alternateWithWeeks().print(p));
        
        p = new Period(0);
        assertEquals("P0000W0000T000000", ISOPeriodFormat.alternateWithWeeks().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P0000W0000T000000", ISOPeriodFormat.alternateWithWeeks().print(p));
        
        assertEquals("P0001W0004T050607.008", ISOPeriodFormat.alternateWithWeeks().print(YEAR_DAY_PERIOD));
        assertEquals("P0000W0000T000000", ISOPeriodFormat.alternateWithWeeks().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P0001W0304T000000", ISOPeriodFormat.alternateWithWeeks().print(DATE_PERIOD));
        assertEquals("P0000W0000T050607.008", ISOPeriodFormat.alternateWithWeeks().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormat::testFormatAlternateExtendedWithWeeks
    public void testFormatAlternateExtendedWithWeeks() {
        Period p = new Period(1, 2, 3, 4, 5, 6 ,7, 8);
        assertEquals("P0001-W03-04T05:06:07.008", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        p = new Period(1, 2, 3, 4, 5, 6 ,7, 0);
        assertEquals("P0001-W03-04T05:06:07", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        
        p = new Period(0);
        assertEquals("P0000-W00-00T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        p = new Period(0, PeriodType.standard().withMillisRemoved().withSecondsRemoved());
        assertEquals("P0000-W00-00T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(p));
        
        assertEquals("P0001-W00-04T05:06:07.008", ISOPeriodFormat.alternateExtendedWithWeeks().print(YEAR_DAY_PERIOD));
        assertEquals("P0000-W00-00T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals("P0001-W03-04T00:00:00", ISOPeriodFormat.alternateExtendedWithWeeks().print(DATE_PERIOD));
        assertEquals("P0000-W00-00T05:06:07.008", ISOPeriodFormat.alternateExtendedWithWeeks().print(TIME_PERIOD));
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard1
    public void testParseStandard1() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P1Y2M3W4DT5H6M7.008S");
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard2
    public void testParseStandard2() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0Y0M0W0DT5H6M7.008S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard3
    public void testParseStandard3() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0DT5H6M7.008S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard4
    public void testParseStandard4() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P2Y3DT5H6M7.008S");
        assertEquals(new Period(2, 0, 0, 3, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard5
    public void testParseStandard5() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P2YT5H6M7.008S");
        assertEquals(new Period(2, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard6
    public void testParseStandard6() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("PT5H6M7.008S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 8), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard7
    public void testParseStandard7() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P1Y2M3W4D");
        assertEquals(new Period(1, 2, 3, 4, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard8
    public void testParseStandard8() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("PT5H6M7S");
        assertEquals(new Period(0, 0, 0, 0, 5, 6, 7, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard9
    public void testParseStandard9() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("PT0S");
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard10
    public void testParseStandard10() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0D");
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandard11
    public void testParseStandard11() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        Period p = parser.parsePeriod("P0Y");
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 0), p);
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail1
    public void testParseStandardFail1() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("P1Y2S");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail2
    public void testParseStandardFail2() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("PS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail3
    public void testParseStandardFail3() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("PTS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestISOPeriodFormatParsing::testParseStandardFail4
    public void testParseStandardFail4() {
        PeriodFormatter parser = ISOPeriodFormat.standard();
        try {
            parser.parsePeriod("PXS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        PeriodFormat f = new PeriodFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_formatStandard
    public void test_getDefault_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 day, 5 hours, 6 minutes, 7 seconds and 8 milliseconds", PeriodFormat.getDefault().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_FormatOneField
    public void test_getDefault_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 days", PeriodFormat.getDefault().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_formatTwoFields
    public void test_getDefault_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 days and 5 hours", PeriodFormat.getDefault().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_parseOneField
    public void test_getDefault_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.getDefault().parsePeriod("2 days"));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_parseTwoFields
    public void test_getDefault_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.getDefault().parsePeriod("2 days and 5 hours"));
    }

// org.joda.time.format.TestPeriodFormat::test_getDefault_cached
    public void test_getDefault_cached() {
        assertSame(PeriodFormat.getDefault(), PeriodFormat.getDefault());
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_default
    public void test_wordBased_default() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 Tag, 5 Stunden, 6 Minuten, 7 Sekunden und 8 Millisekunden", PeriodFormat.wordBased().print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_formatStandard
    public void test_wordBased_fr_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 jour, 5 heures, 6 minutes, 7 secondes et 8 millisecondes", PeriodFormat.wordBased(FR).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_FormatOneField
    public void test_wordBased_fr_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 jours", PeriodFormat.wordBased(FR).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_formatTwoFields
    public void test_wordBased_fr_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 jours et 5 heures", PeriodFormat.wordBased(FR).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_parseOneField
    public void test_wordBased_fr_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(FR).parsePeriod("2 jours"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_parseTwoFields
    public void test_wordBased_fr_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(FR).parsePeriod("2 jours et 5 heures"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_fr_cached
    public void test_wordBased_fr_cached() {
        assertSame(PeriodFormat.wordBased(FR), PeriodFormat.wordBased(FR));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_formatStandard
    public void test_wordBased_pt_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 dia, 5 horas, 6 minutos, 7 segundos e 8 milissegundos", PeriodFormat.wordBased(PT).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_FormatOneField
    public void test_wordBased_pt_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 dias", PeriodFormat.wordBased(PT).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_formatTwoFields
    public void test_wordBased_pt_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 dias e 5 horas", PeriodFormat.wordBased(PT).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_parseOneField
    public void test_wordBased_pt_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(PT).parsePeriod("2 dias"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_parseTwoFields
    public void test_wordBased_pt_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(PT).parsePeriod("2 dias e 5 horas"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_pt_cached
    public void test_wordBased_pt_cached() {
        assertSame(PeriodFormat.wordBased(PT), PeriodFormat.wordBased(PT));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_formatStandard
    public void test_wordBased_es_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 dia, 5 horas, 6 minutos, 7 segundos y 8 milisegundos", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_FormatOneField
    public void test_wordBased_es_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 dias", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_formatTwoFields
    public void test_wordBased_es_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 dias y 5 horas", PeriodFormat.wordBased(ES).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_parseOneField
    public void test_wordBased_es_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(ES).parsePeriod("2 dias"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_parseTwoFields
    public void test_wordBased_es_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(ES).parsePeriod("2 dias y 5 horas"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_es_cached
    public void test_wordBased_es_cached() {
        assertSame(PeriodFormat.wordBased(ES), PeriodFormat.wordBased(ES));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_formatStandard
    public void test_wordBased_de_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 Tag, 5 Stunden, 6 Minuten, 7 Sekunden und 8 Millisekunden", PeriodFormat.wordBased(DE).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_FormatOneField
    public void test_wordBased_de_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 Tage", PeriodFormat.wordBased(DE).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_formatTwoFields
    public void test_wordBased_de_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 Tage und 5 Stunden", PeriodFormat.wordBased(DE).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_parseOneField
    public void test_wordBased_de_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(DE).parsePeriod("2 Tage"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_parseTwoFields
    public void test_wordBased_de_parseTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals(p, PeriodFormat.wordBased(DE).parsePeriod("2 Tage und 5 Stunden"));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_de_cached
    public void test_wordBased_de_cached() {
        assertSame(PeriodFormat.wordBased(DE), PeriodFormat.wordBased(DE));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_formatStandard
    public void test_wordBased_nl_formatStandard() {
        Period p = new Period(0, 0, 0, 1, 5, 6 ,7, 8);
        assertEquals("1 dag, 5 uur, 6 minuten, 7 seconden en 8 milliseconden", PeriodFormat.wordBased(NL).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_FormatOneField
    public void test_wordBased_nl_FormatOneField() {
        Period p = Period.days(2);
        assertEquals("2 dagen", PeriodFormat.wordBased(NL).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_formatTwoFields
    public void test_wordBased_nl_formatTwoFields() {
        Period p = Period.days(2).withHours(5);
        assertEquals("2 dagen en 5 uur", PeriodFormat.wordBased(NL).print(p));
    }

// org.joda.time.format.TestPeriodFormat::test_wordBased_nl_parseOneField
    public void test_wordBased_nl_parseOneField() {
        Period p = Period.days(2);
        assertEquals(p, PeriodFormat.wordBased(NL).parsePeriod("2 dagen"));
    }
