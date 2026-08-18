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
        assertEquals(baseAfter, baseBefore.withLaterOffsetAtOverlap());
        
        assertSame(baseAfter, baseAfter.withLaterOffsetAtOverlap());
        assertEquals(baseBefore, baseAfter.withEarlierOffsetAtOverlap());
    }

// org.joda.time.TestDateTimeZoneCutover::testBug3476684_adjustOffset
    public void testBug3476684_adjustOffset() {
        final DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
        DateTime base = new DateTime(2012, 2, 25, 22, 15, zone);
        DateTime baseBefore = base.plusHours(1);  
        DateTime baseAfter = base.plusHours(2);  
        
        assertSame(base, base.withEarlierOffsetAtOverlap());
        assertSame(base, base.withLaterOffsetAtOverlap());
        
        assertSame(baseBefore, baseBefore.withEarlierOffsetAtOverlap());
        assertEquals(baseAfter, baseBefore.withLaterOffsetAtOverlap());
        
        assertSame(baseAfter, baseAfter.withLaterOffsetAtOverlap());
        assertEquals(baseBefore, baseAfter.withEarlierOffsetAtOverlap());
    }

// org.joda.time.TestDateTimeZoneCutover::testBug3476684_adjustOffset_springGap
    public void testBug3476684_adjustOffset_springGap() {
      final DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
      DateTime base = new DateTime(2011, 10, 15, 22, 15, zone);
      DateTime baseBefore = base.plusHours(1);  
      DateTime baseAfter = base.plusHours(2);  
      
      assertSame(base, base.withEarlierOffsetAtOverlap());
      assertSame(base, base.withLaterOffsetAtOverlap());
      
      assertSame(baseBefore, baseBefore.withEarlierOffsetAtOverlap());
      assertEquals(baseBefore, baseBefore.withLaterOffsetAtOverlap());
      
      assertSame(baseAfter, baseAfter.withLaterOffsetAtOverlap());
      assertEquals(baseAfter, baseAfter.withEarlierOffsetAtOverlap());
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
