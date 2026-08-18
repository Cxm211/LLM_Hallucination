// buggy code
    public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) throws IllegalArgumentException {
        if (hoursOffset == 0 && minutesOffset == 0) {
            return DateTimeZone.UTC;
        }
        if (minutesOffset < 0 || minutesOffset > 59) {
            throw new IllegalArgumentException("Minutes out of range: " + minutesOffset);
        }
        int offset = 0;
        try {
            int hoursInMinutes = FieldUtils.safeMultiply(hoursOffset, 60);
            if (hoursInMinutes < 0) {
                minutesOffset = FieldUtils.safeAdd(hoursInMinutes, -minutesOffset);
            } else {
                minutesOffset = FieldUtils.safeAdd(hoursInMinutes, minutesOffset);
            }
            offset = FieldUtils.safeMultiply(minutesOffset, DateTimeConstants.MILLIS_PER_MINUTE);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
        return forOffsetMillis(offset);
    }

    public static DateTimeZone forOffsetMillis(int millisOffset) {
        String id = printOffset(millisOffset);
        return fixedOffsetZone(id, millisOffset);
    }

// relevant test
// org.joda.time.format.TestPeriodFormatterBuilder::testFormatLiteral
    public void testFormatLiteral() {
        PeriodFormatter f = builder.appendLiteral("HELLO").toFormatter();
        assertEquals("HELLO", f.print(PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(0, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppendFormatter
    public void testFormatAppendFormatter() {
        PeriodFormatter base = builder.appendYears().appendLiteral("-").toFormatter();
        PeriodFormatter f = new PeriodFormatterBuilder().append(base).appendYears().toFormatter();
        assertEquals("1-1", f.print(PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatMinDigits
    public void testFormatMinDigits() {
        PeriodFormatter f = new PeriodFormatterBuilder().minimumPrintedDigits(4).appendYears().toFormatter();
        assertEquals("0001", f.print(PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroDefault
    public void testFormatPrintZeroDefault() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---0", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1---4", f.print(YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---0", f.print(EMPTY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
        
        
        f = new PeriodFormatterBuilder()
                .appendYears().appendLiteral("-")
                .appendYears().toFormatter();
        assertEquals("-0", f.print(EMPTY_PERIOD));
        assertEquals(2, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyLast
    public void testFormatPrintZeroRarelyLast() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .printZeroRarelyLast()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---0", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1---4", f.print(YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---0", f.print(EMPTY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirst
    public void testFormatPrintZeroRarelyFirst() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .printZeroRarelyFirst()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0---", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1---4", f.print(YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0---", f.print(EMPTY_PERIOD));
        assertEquals(4, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(1, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstYears
    public void testFormatPrintZeroRarelyFirstYears() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendYears().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstMonths
    public void testFormatPrintZeroRarelyFirstMonths() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendMonths().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstWeeks
    public void testFormatPrintZeroRarelyFirstWeeks() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendWeeks().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstDays
    public void testFormatPrintZeroRarelyFirstDays() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendDays().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstHours
    public void testFormatPrintZeroRarelyFirstHours() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendHours().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstMinutes
    public void testFormatPrintZeroRarelyFirstMinutes() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendMinutes().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroRarelyFirstSeconds
    public void testFormatPrintZeroRarelyFirstSeconds() {
        PeriodFormatter f = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendSeconds().toFormatter();
        assertEquals("0", f.print(EMPTY_PERIOD));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroIfSupported
    public void testFormatPrintZeroIfSupported() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .printZeroIfSupported()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0---0", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1---4", f.print(YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0-0-0-0", f.print(EMPTY_PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroAlways
    public void testFormatPrintZeroAlways() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .printZeroAlways()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0-0-0-0", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1-0-0-4", f.print(YEAR_DAY_PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("0-0-0-0", f.print(EMPTY_PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatPrintZeroNever
    public void testFormatPrintZeroNever() {
        PeriodFormatter f =
            new PeriodFormatterBuilder()
                .printZeroNever()
                .appendYears().appendLiteral("-")
                .appendMonths().appendLiteral("-")
                .appendWeeks().appendLiteral("-")
                .appendDays().toFormatter();
        assertEquals("1-2-3-4", f.print(PERIOD));
        assertEquals(7, f.getPrinter().calculatePrintedLength(PERIOD, null));
        assertEquals(4, f.getPrinter().countFieldsToPrint(PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---", f.print(EMPTY_YEAR_DAY_PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(EMPTY_YEAR_DAY_PERIOD, null));
        assertEquals(0, f.getPrinter().countFieldsToPrint(EMPTY_YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("1---4", f.print(YEAR_DAY_PERIOD));
        assertEquals(5, f.getPrinter().calculatePrintedLength(YEAR_DAY_PERIOD, null));
        assertEquals(2, f.getPrinter().countFieldsToPrint(YEAR_DAY_PERIOD, Integer.MAX_VALUE, null));
        
        assertEquals("---", f.print(EMPTY_PERIOD));
        assertEquals(3, f.getPrinter().calculatePrintedLength(EMPTY_PERIOD, null));
        assertEquals(0, f.getPrinter().countFieldsToPrint(EMPTY_PERIOD, Integer.MAX_VALUE, null));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParser_null_null
    public void testFormatAppend_PrinterParser_null_null() {
        try {
            new PeriodFormatterBuilder().append(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParser_Printer_null
    public void testFormatAppend_PrinterParser_Printer_null() {
        PeriodPrinter printer = new PeriodFormatterBuilder().appendYears().appendLiteral("-").toPrinter();
        PeriodFormatterBuilder bld = new PeriodFormatterBuilder().append(printer, null).appendMonths();
        assertNotNull(bld.toPrinter());
        assertNull(bld.toParser());
        
        PeriodFormatter f = bld.toFormatter();
        assertEquals("1-2", f.print(PERIOD));
        try {
            f.parsePeriod("1-2");
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParser_null_Parser
    public void testFormatAppend_PrinterParser_null_Parser() {
        PeriodParser parser = new PeriodFormatterBuilder().appendWeeks().appendLiteral("-").toParser();
        PeriodFormatterBuilder bld = new PeriodFormatterBuilder().append(null, parser).appendMonths();
        assertNull(bld.toPrinter());
        assertNotNull(bld.toParser());
        
        PeriodFormatter f = bld.toFormatter();
        try {
            f.print(PERIOD);
            fail();
        } catch (UnsupportedOperationException ex) {}
        assertEquals(new Period(0, 2, 1, 0, 0, 0, 0, 0), f.parsePeriod("1-2"));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParser_PrinterParser
    public void testFormatAppend_PrinterParser_PrinterParser() {
        PeriodPrinter printer = new PeriodFormatterBuilder().appendYears().appendLiteral("-").toPrinter();
        PeriodParser parser = new PeriodFormatterBuilder().appendWeeks().appendLiteral("-").toParser();
        PeriodFormatterBuilder bld = new PeriodFormatterBuilder().append(printer, parser).appendMonths();
        assertNotNull(bld.toPrinter());
        assertNotNull(bld.toParser());
        
        PeriodFormatter f = bld.toFormatter();
        assertEquals("1-2", f.print(PERIOD));
        assertEquals(new Period(0, 2, 1, 0, 0, 0, 0, 0), f.parsePeriod("1-2"));
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParser_Printer_null_null_Parser
    public void testFormatAppend_PrinterParser_Printer_null_null_Parser() {
        PeriodPrinter printer = new PeriodFormatterBuilder().appendYears().appendLiteral("-").toPrinter();
        PeriodParser parser = new PeriodFormatterBuilder().appendWeeks().appendLiteral("-").toParser();
        PeriodFormatterBuilder bld = new PeriodFormatterBuilder().append(printer, null).append(null, parser);
        assertNull(bld.toPrinter());
        assertNull(bld.toParser());
        
        try {
            bld.toFormatter();
            fail();
        } catch (IllegalStateException ex) {}
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testFormatAppend_PrinterParserThenClear
    public void testFormatAppend_PrinterParserThenClear() {
        PeriodPrinter printer = new PeriodFormatterBuilder().appendYears().appendLiteral("-").toPrinter();
        PeriodParser parser = new PeriodFormatterBuilder().appendWeeks().appendLiteral("-").toParser();
        PeriodFormatterBuilder bld = new PeriodFormatterBuilder().append(printer, null).append(null, parser);
        assertNull(bld.toPrinter());
        assertNull(bld.toParser());
        bld.clear();
        bld.appendMonths();
        assertNotNull(bld.toPrinter());
        assertNotNull(bld.toParser());
    }

// org.joda.time.format.TestPeriodFormatterBuilder::testBug2495455
    public void testBug2495455() {
        PeriodFormatter pfmt1 = new PeriodFormatterBuilder()
            .appendLiteral("P")
            .appendYears()
            .appendSuffix("Y")
            .appendMonths()
            .appendSuffix("M")
            .appendWeeks()
            .appendSuffix("W")
            .appendDays()
            .appendSuffix("D")
            .appendSeparatorIfFieldsAfter("T")
            .appendHours()
            .appendSuffix("H")
            .appendMinutes()
            .appendSuffix("M")
            .appendSecondsWithOptionalMillis()
            .appendSuffix("S")
            .toFormatter();
        PeriodFormatter pfmt2 = new PeriodFormatterBuilder()
            .append(ISOPeriodFormat.standard())
            .toFormatter();
        pfmt1.parsePeriod("PT1003199059S");
        pfmt2.parsePeriod("PT1003199059S");
    }

// org.joda.time.format.TestTextFields::testMonthNames_monthStart
    public void testMonthNames_monthStart() {
        DateTimeFormatter printer = DateTimeFormat.forPattern("MMMM");
        for (int i=0; i<ZONES.length; i++) {
            for (int month=1; month<=12; month++) {
                DateTime dt = new DateTime(2004, month, 1, 1, 20, 30, 40, ZONES[i]);
                String monthText = printer.print(dt);
                assertEquals(MONTHS[month], monthText);
            }
        }
    }

// org.joda.time.format.TestTextFields::testMonthNames_monthMiddle
    public void testMonthNames_monthMiddle() {
        DateTimeFormatter printer = DateTimeFormat.forPattern("MMMM");
        for (int i=0; i<ZONES.length; i++) {
            for (int month=1; month<=12; month++) {
                DateTime dt = new DateTime(2004, month, 15, 12, 20, 30, 40, ZONES[i]);
                String monthText = printer.print(dt);
                assertEquals(MONTHS[month], monthText);
            }
        }
    }

// org.joda.time.format.TestTextFields::testMonthNames_monthEnd
    public void testMonthNames_monthEnd() {
        DateTimeFormatter printer = DateTimeFormat.forPattern("MMMM");
        for (int i=0; i<ZONES.length; i++) {
            Chronology chrono = ISOChronology.getInstance(ZONES[i]);
            for (int month=1; month<=12; month++) {
                DateTime dt = new DateTime(2004, month, 1, 23, 20, 30, 40, chrono);
                int lastDay = chrono.dayOfMonth().getMaximumValue(dt.getMillis());
                dt = new DateTime(2004, month, lastDay, 23, 20, 30, 40, chrono);
                String monthText = printer.print(dt);
                assertEquals(MONTHS[month], monthText);
            }
        }
    }

// org.joda.time.format.TestTextFields::testWeekdayNames
    public void testWeekdayNames() {
        DateTimeFormatter printer = DateTimeFormat.forPattern("EEEE");
        for (int i=0; i<ZONES.length; i++) {
            MutableDateTime mdt = new MutableDateTime(2004, 1, 1, 1, 20, 30, 40, ZONES[i]);
            for (int day=1; day<=366; day++) {
                mdt.setDayOfYear(day);
                int weekday = mdt.getDayOfWeek();
                String weekdayText = printer.print(mdt);
                assertEquals(WEEKDAYS[weekday], weekdayText);
            }
        }
    }

// org.joda.time.format.TestTextFields::testHalfdayNames
    public void testHalfdayNames() {
        DateTimeFormatter printer = DateTimeFormat.forPattern("a");
        for (int i=0; i<ZONES.length; i++) {
            Chronology chrono = ISOChronology.getInstance(ZONES[i]);
            MutableDateTime mdt = new MutableDateTime(2004, 5, 30, 0, 20, 30, 40, chrono);
            for (int hour=0; hour<24; hour++) {
                mdt.setHourOfDay(hour);
                int halfday = mdt.get(chrono.halfdayOfDay());
                String halfdayText = printer.print(mdt);
                assertEquals(HALFDAYS[halfday], halfdayText);
            }
        }
    }

// org.joda.time.tz.TestBuilder::testID
    public void testID() {
        DateTimeZone tz = buildAmericaLosAngeles();
        assertEquals("America/Los_Angeles", tz.getID());
        assertEquals(false, tz.isFixed());
    }

// org.joda.time.tz.TestBuilder::testForwardTransitions
    public void testForwardTransitions() {
        DateTimeZone tz = buildAmericaLosAngeles();
        testForwardTransitions(tz, AMERICA_LOS_ANGELES_DATA);
    }

// org.joda.time.tz.TestBuilder::testReverseTransitions
    public void testReverseTransitions() {
        DateTimeZone tz = buildAmericaLosAngeles();
        testReverseTransitions(tz, AMERICA_LOS_ANGELES_DATA);
    }

// org.joda.time.tz.TestBuilder::testSerialization
    public void testSerialization() throws IOException {
        DateTimeZone tz = testSerialization
            (buildAmericaLosAngelesBuilder(), "America/Los_Angeles");

        assertEquals(false, tz.isFixed());
        testForwardTransitions(tz, AMERICA_LOS_ANGELES_DATA);
        testReverseTransitions(tz, AMERICA_LOS_ANGELES_DATA);
    }

// org.joda.time.tz.TestBuilder::testFixed
    public void testFixed() throws IOException {
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder()
            .setStandardOffset(3600000)
            .setFixedSavings("LMT", 0);
        DateTimeZone tz = builder.toDateTimeZone("Test", true);

        for (int i=0; i<2; i++) {
            assertEquals("Test", tz.getID());
            assertEquals(true, tz.isFixed());
            assertEquals(3600000, tz.getOffset(0));
            assertEquals(3600000, tz.getStandardOffset(0));
            assertEquals(0, tz.nextTransition(0));
            assertEquals(0, tz.previousTransition(0));

            tz = testSerialization(builder, "Test");
        }
    }

// org.joda.time.tz.TestCachedDateTimeZone::test_caching
    public void test_caching() throws Exception {
        CachedDateTimeZone zone1 = CachedDateTimeZone.forZone(DateTimeZone.forID("Europe/Paris"));
        CachedDateTimeZone zone2 = CachedDateTimeZone.forZone(DateTimeZone.forID("Europe/Paris"));
        assertSame(zone1, zone2);
    }

// org.joda.time.tz.TestCachedDateTimeZone::testSerialization
    public void testSerialization() throws Exception {
        CachedDateTimeZone test = CachedDateTimeZone.forZone(DateTimeZone.forID("Europe/Paris"));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        CachedDateTimeZone result = (CachedDateTimeZone) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.tz.TestCompiler::testDateTimeZoneBuilder
    public void testDateTimeZoneBuilder() throws Exception {
        
        getTestDataTimeZoneBuilder().toDateTimeZone("TestDTZ1", true);
        final DateTimeZone[] zone = new DateTimeZone[1];
        Thread t = new Thread(new Runnable() {
            public void run() {
                zone[0] = getTestDataTimeZoneBuilder().toDateTimeZone("TestDTZ2", true);
            }
        });
        t.start();
        t.join();
        assertNotNull(zone[0]);
    }

// org.joda.time.tz.TestCompiler::testCompile
    public void testCompile() throws Exception {
        Provider provider = compileAndLoad(AMERICA_LOS_ANGELES_FILE);
        DateTimeZone tz = provider.getZone("America/Los_Angeles");

        assertEquals("America/Los_Angeles", tz.getID());
        assertEquals(false, tz.isFixed());
        TestBuilder.testForwardTransitions(tz, TestBuilder.AMERICA_LOS_ANGELES_DATA);
        TestBuilder.testReverseTransitions(tz, TestBuilder.AMERICA_LOS_ANGELES_DATA);
    }

// org.joda.time.tz.TestCompiler::test_2400_fromDay
    public void test_2400_fromDay() {
        StringTokenizer st = new StringTokenizer("Apr Sun>=1  24:00");
        DateTimeOfYear test = new DateTimeOfYear(st);
        assertEquals(4, test.iMonthOfYear);  
        assertEquals(2, test.iDayOfMonth);   
        assertEquals(1, test.iDayOfWeek);    
        assertEquals(0, test.iMillisOfDay);  
        assertEquals(true, test.iAdvanceDayOfWeek);
    }

// org.joda.time.tz.TestCompiler::test_2400_last
    public void test_2400_last() {
        StringTokenizer st = new StringTokenizer("Mar lastSun 24:00");
        DateTimeOfYear test = new DateTimeOfYear(st);
        assertEquals(4, test.iMonthOfYear);  
        assertEquals(1, test.iDayOfMonth);   
        assertEquals(1, test.iDayOfWeek);    
        assertEquals(0, test.iMillisOfDay);  
        assertEquals(false, test.iAdvanceDayOfWeek);
    }

// org.joda.time.tz.TestCompiler::test_Amman_2003
    public void test_Amman_2003() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Amman");
        DateTime dt = new DateTime(2003, 3, 1, 0, 0, zone);
        long next = zone.nextTransition(dt.getMillis());
        assertEquals(next, new DateTime(2003, 3, 28, 0, 0, DateTimeZone.forOffsetHours(2)).getMillis());
    }

// org.joda.time.tz.TestCompiler::test_Amman_2004
    public void test_Amman_2004() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Amman");
        DateTime dt = new DateTime(2004, 3, 1, 0, 0, zone);
        long next = zone.nextTransition(dt.getMillis());
        assertEquals(next, new DateTime(2004, 3, 26, 0, 0, DateTimeZone.forOffsetHours(2)).getMillis());
    }

// org.joda.time.tz.TestCompiler::test_Amman_2005
    public void test_Amman_2005() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Amman");
        DateTime dt = new DateTime(2005, 3, 1, 0, 0, zone);
        long next = zone.nextTransition(dt.getMillis());
        assertEquals(next, new DateTime(2005, 4, 1, 0, 0, DateTimeZone.forOffsetHours(2)).getMillis());
    }

// org.joda.time.tz.TestCompiler::test_Amman_2006
    public void test_Amman_2006() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Amman");
        DateTime dt = new DateTime(2006, 3, 1, 0, 0, zone);
        long next = zone.nextTransition(dt.getMillis());
        assertEquals(next, new DateTime(2006, 3, 31, 0, 0, DateTimeZone.forOffsetHours(2)).getMillis());
    }

// org.joda.time.tz.TestFixedDateTimeZone::testEquals
    public void testEquals() throws Exception {
        FixedDateTimeZone zone1 = new FixedDateTimeZone("A", "B", 1, 5);
        FixedDateTimeZone zone1b = new FixedDateTimeZone("A", "B", 1, 5);
        FixedDateTimeZone zone2 = new FixedDateTimeZone("A", "C", 1, 5);
        FixedDateTimeZone zone3 = new FixedDateTimeZone("A", "B", 2, 5);
        FixedDateTimeZone zone4 = new FixedDateTimeZone("A", "B", 1, 6);
        
        assertEquals(true, zone1.equals(zone1));
        assertEquals(true, zone1.equals(zone1b));
        assertEquals(true, zone1.equals(zone2));  
        assertEquals(false, zone1.equals(zone3));
        assertEquals(false, zone1.equals(zone4));
    }

// org.joda.time.tz.TestFixedDateTimeZone::testHashCode
    public void testHashCode() throws Exception {
        FixedDateTimeZone zone1 = new FixedDateTimeZone("A", "B", 1, 5);
        FixedDateTimeZone zone1b = new FixedDateTimeZone("A", "B", 1, 5);
        FixedDateTimeZone zone2 = new FixedDateTimeZone("A", "C", 1, 5);
        FixedDateTimeZone zone3 = new FixedDateTimeZone("A", "B", 2, 5);
        FixedDateTimeZone zone4 = new FixedDateTimeZone("A", "B", 1, 6);
        
        assertEquals(true, zone1.hashCode() == zone1.hashCode());
        assertEquals(true, zone1.hashCode() == zone1b.hashCode());
        assertEquals(true, zone1.hashCode() == zone2.hashCode());  
        assertEquals(false, zone1.hashCode() == zone3.hashCode());
        assertEquals(false, zone1.hashCode() == zone4.hashCode());
    }

// org.joda.time.tz.TestFixedDateTimeZone::testToTimeZone1
    public void testToTimeZone1() throws Exception {
        FixedDateTimeZone zone = new FixedDateTimeZone("+00:01", "+00:01", 60000, 60000);
        java.util.TimeZone tz = zone.toTimeZone();
        
        assertEquals(60000, tz.getRawOffset());
        assertEquals(60000, getOffset(tz, 1167638400000L));
        assertEquals(60000, getOffset(tz, 1185951600000L));
    }

// org.joda.time.tz.TestFixedDateTimeZone::testToTimeZone2
    public void testToTimeZone2() throws Exception {
        FixedDateTimeZone zone = new FixedDateTimeZone("A", "B", 1, 5);
        java.util.TimeZone tz = zone.toTimeZone();
        
        assertEquals(1, tz.getRawOffset());
        assertEquals(1, getOffset(tz, 1167638400000L));
        assertEquals(1, getOffset(tz, 1185951600000L));
    }

// org.joda.time.tz.TestUTCProvider::testClass
    public void testClass() throws Exception {
        Class cls = UTCProvider.class;
        assertEquals(true, Modifier.isPublic(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isPublic(con.getModifiers()));
    }

// org.joda.time.tz.TestUTCProvider::testGetAvailableIDs
    public void testGetAvailableIDs() throws Exception {
        Provider p = new UTCProvider();
        Set set = p.getAvailableIDs();
        assertEquals(1, set.size());
        assertEquals("UTC", set.iterator().next());
    }

// org.joda.time.tz.TestUTCProvider::testGetZone_String
    public void testGetZone_String() throws Exception {
        Provider p = new UTCProvider();
        assertSame(DateTimeZone.UTC, p.getZone("UTC"));
        assertEquals(null, p.getZone(null));
        assertEquals(null, p.getZone("Europe/London"));
        assertEquals(null, p.getZone("Blah"));
    }
