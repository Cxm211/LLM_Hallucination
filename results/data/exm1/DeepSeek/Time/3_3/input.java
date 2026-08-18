// buggy code
    public void add(DurationFieldType type, int amount) {
        if (type == null) {
            throw new IllegalArgumentException("Field must not be null");
        }
            setMillis(type.getField(getChronology()).add(getMillis(), amount));
    }

    public void addYears(final int years) {
            setMillis(getChronology().years().add(getMillis(), years));
    }

    public void addWeekyears(final int weekyears) {
            setMillis(getChronology().weekyears().add(getMillis(), weekyears));
    }

    public void addMonths(final int months) {
            setMillis(getChronology().months().add(getMillis(), months));
    }

    public void addWeeks(final int weeks) {
            setMillis(getChronology().weeks().add(getMillis(), weeks));
    }

    public void addDays(final int days) {
            setMillis(getChronology().days().add(getMillis(), days));
    }

    public void addHours(final int hours) {
            setMillis(getChronology().hours().add(getMillis(), hours));
    }

    public void addMinutes(final int minutes) {
            setMillis(getChronology().minutes().add(getMillis(), minutes));
    }

    public void addSeconds(final int seconds) {
            setMillis(getChronology().seconds().add(getMillis(), seconds));
    }

    public void addMillis(final int millis) {
            setMillis(getChronology().millis().add(getMillis(), millis));
    }

// relevant test
// org.joda.time.TestPartial_Basics::testWithout3
    public void testWithout3() {
        Partial test = createHourMinPartial();
        Partial result = test.without(DateTimeFieldType.hourOfDay());
        check(test, 10, 20);
        assertEquals(1, result.size());
        assertEquals(false, result.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, result.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(DateTimeFieldType.minuteOfHour(), result.getFieldType(0));
    }

// org.joda.time.TestPartial_Basics::testWithout4
    public void testWithout4() {
        Partial test = createHourMinPartial();
        Partial result = test.without(DateTimeFieldType.minuteOfHour());
        check(test, 10, 20);
        assertEquals(1, result.size());
        assertEquals(true, result.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(false, result.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(DateTimeFieldType.hourOfDay(), result.getFieldType(0));
    }

// org.joda.time.TestPartial_Basics::testWithout5
    public void testWithout5() {
        Partial test = new Partial(DateTimeFieldType.hourOfDay(), 12);
        Partial result = test.without(DateTimeFieldType.hourOfDay());
        assertEquals(0, result.size());
        assertEquals(false, result.isSupported(DateTimeFieldType.hourOfDay()));
    }

// org.joda.time.TestPartial_Basics::testWithField1
    public void testWithField1() {
        Partial test = createHourMinPartial();
        Partial result = test.withField(DateTimeFieldType.hourOfDay(), 15);
        check(test, 10, 20);
        check(result, 15, 20);
    }

// org.joda.time.TestPartial_Basics::testWithField2
    public void testWithField2() {
        Partial test = createHourMinPartial();
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWithField3
    public void testWithField3() {
        Partial test = createHourMinPartial();
        try {
            test.withField(DateTimeFieldType.dayOfMonth(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWithField4
    public void testWithField4() {
        Partial test = createHourMinPartial();
        Partial result = test.withField(DateTimeFieldType.hourOfDay(), 10);
        assertSame(test, result);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAdded1
    public void testWithFieldAdded1() {
        Partial test = createHourMinPartial();
        Partial result = test.withFieldAdded(DurationFieldType.hours(), 6);
        
        assertEquals(createHourMinPartial(), test);
        check(test, 10, 20);
        check(result, 16, 20);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAdded2
    public void testWithFieldAdded2() {
        Partial test = createHourMinPartial();
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAdded3
    public void testWithFieldAdded3() {
        Partial test = createHourMinPartial();
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAdded4
    public void testWithFieldAdded4() {
        Partial test = createHourMinPartial();
        Partial result = test.withFieldAdded(DurationFieldType.hours(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAdded5
    public void testWithFieldAdded5() {
        Partial test = createHourMinPartial();
        try {
            test.withFieldAdded(DurationFieldType.days(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAdded6
    public void testWithFieldAdded6() {
        Partial test = createHourMinPartial();
        try {
            test.withFieldAdded(DurationFieldType.hours(), 16);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        check(test, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAdded7
    public void testWithFieldAdded7() {
        Partial test = createHourMinPartial(23, 59, ISO_UTC);
        try {
            test.withFieldAdded(DurationFieldType.minutes(), 1);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        check(test, 23, 59);
        
        test = createHourMinPartial(23, 59, ISO_UTC);
        try {
            test.withFieldAdded(DurationFieldType.hours(), 1);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        check(test, 23, 59);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAdded8
    public void testWithFieldAdded8() {
        Partial test = createHourMinPartial(0, 0, ISO_UTC);
        try {
            test.withFieldAdded(DurationFieldType.minutes(), -1);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        check(test, 0, 0);
        
        test = createHourMinPartial(0, 0, ISO_UTC);
        try {
            test.withFieldAdded(DurationFieldType.hours(), -1);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        check(test, 0, 0);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAddWrapped1
    public void testWithFieldAddWrapped1() {
        Partial test = createHourMinPartial();
        Partial result = test.withFieldAddWrapped(DurationFieldType.hours(), 6);
        
        assertEquals(createHourMinPartial(), test);
        check(test, 10, 20);
        check(result, 16, 20);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAddWrapped2
    public void testWithFieldAddWrapped2() {
        Partial test = createHourMinPartial();
        try {
            test.withFieldAddWrapped(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAddWrapped3
    public void testWithFieldAddWrapped3() {
        Partial test = createHourMinPartial();
        try {
            test.withFieldAddWrapped(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAddWrapped4
    public void testWithFieldAddWrapped4() {
        Partial test = createHourMinPartial();
        Partial result = test.withFieldAddWrapped(DurationFieldType.hours(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAddWrapped5
    public void testWithFieldAddWrapped5() {
        Partial test = createHourMinPartial();
        try {
            test.withFieldAddWrapped(DurationFieldType.days(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAddWrapped6
    public void testWithFieldAddWrapped6() {
        Partial test = createHourMinPartial();
        Partial result = test.withFieldAddWrapped(DurationFieldType.hours(), 16);
        
        assertEquals(createHourMinPartial(), test);
        check(test, 10, 20);
        check(result, 2, 20);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAddWrapped7
    public void testWithFieldAddWrapped7() {
        Partial test = createHourMinPartial(23, 59, ISO_UTC);
        Partial result = test.withFieldAddWrapped(DurationFieldType.minutes(), 1);
        check(test, 23, 59);
        check(result, 0, 0);
        
        test = createHourMinPartial(23, 59, ISO_UTC);
        result = test.withFieldAddWrapped(DurationFieldType.hours(), 1);
        check(test, 23, 59);
        check(result, 0, 59);
    }

// org.joda.time.TestPartial_Basics::testWithFieldAddWrapped8
    public void testWithFieldAddWrapped8() {
        Partial test = createHourMinPartial(0, 0, ISO_UTC);
        Partial result = test.withFieldAddWrapped(DurationFieldType.minutes(), -1);
        check(test, 0, 0);
        check(result, 23, 59);
        
        test = createHourMinPartial(0, 0, ISO_UTC);
        result = test.withFieldAddWrapped(DurationFieldType.hours(), -1);
        check(test, 0, 0);
        check(result, 23, 0);
    }

// org.joda.time.TestPartial_Basics::testPlus_RP
    public void testPlus_RP() {
        Partial test = createHourMinPartial(BUDDHIST_LONDON);
        Partial result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        check(test, 10, 20);
        check(result, 15, 26);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestPartial_Basics::testMinus_RP
    public void testMinus_RP() {
        Partial test = createHourMinPartial(BUDDHIST_LONDON);
        Partial result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        check(test, 10, 20);
        check(result, 9, 19);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestPartial_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        Partial base = createHourMinPartial(COPTIC_PARIS);
        DateTime dt = new DateTime(0L); 
        assertEquals("1970-01-01T01:00:00.000+01:00", dt.toString());
        
        DateTime test = base.toDateTime(dt);
        check(base, 10, 20);
        assertEquals("1970-01-01T01:00:00.000+01:00", dt.toString());
        assertEquals("1970-01-01T10:20:00.000+01:00", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        Partial base = createHourMinPartial(1, 2, ISO_UTC);
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2);
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 1, 2);
        assertEquals("1970-01-02T01:02:07.008+01:00", test.toString());
    }

// org.joda.time.TestPartial_Basics::testProperty
    public void testProperty() {
        Partial test = createHourMinPartial();
        assertNotNull(test.property(DateTimeFieldType.hourOfDay()));
        assertNotNull(test.property(DateTimeFieldType.minuteOfHour()));
        try {
            test.property(DateTimeFieldType.secondOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Basics::testSerialization
    public void testSerialization() throws Exception {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Partial result = (Partial) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestPartial_Basics::testGetFormatter1
    public void testGetFormatter1() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals("2005", test.getFormatter().print(test));
        
        test = test.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals("2005-06", test.getFormatter().print(test));
        
        test = test.with(DateTimeFieldType.dayOfMonth(), 25);
        assertEquals("2005-06-25", test.getFormatter().print(test));
        
        test = test.without(DateTimeFieldType.monthOfYear());
        assertEquals("2005--25", test.getFormatter().print(test));
    }

// org.joda.time.TestPartial_Basics::testGetFormatter2
    public void testGetFormatter2() {
        Partial test = new Partial();
        assertEquals(null, test.getFormatter());
        
        test = test.with(DateTimeFieldType.era(), 1);
        assertEquals(null, test.getFormatter());
        
        test = test.with(DateTimeFieldType.halfdayOfDay(), 0);
        assertEquals(null, test.getFormatter());
    }

// org.joda.time.TestPartial_Basics::testGetFormatter3
    public void testGetFormatter3() {
        Partial test = new Partial(DateTimeFieldType.dayOfWeek(), 5);
        assertEquals("-W-5", test.getFormatter().print(test));
        
        
        test = test.with(DateTimeFieldType.dayOfMonth(), 13);
        assertEquals("---13", test.getFormatter().print(test));
    }

// org.joda.time.TestPartial_Basics::testToString1
    public void testToString1() {
        Partial test = createHourMinPartial();
        assertEquals("10:20", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToString2
    public void testToString2() {
        Partial test = new Partial();
        assertEquals("[]", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToString3
    public void testToString3() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals("2005", test.toString());
        
        test = test.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals("2005-06", test.toString());
        
        test = test.with(DateTimeFieldType.dayOfMonth(), 25);
        assertEquals("2005-06-25", test.toString());
        
        test = test.without(DateTimeFieldType.monthOfYear());
        assertEquals("2005--25", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToString4
    public void testToString4() {
        Partial test = new Partial(DateTimeFieldType.dayOfWeek(), 5);
        assertEquals("-W-5", test.toString());
        
        test = test.with(DateTimeFieldType.dayOfMonth(), 13);
        assertEquals("[dayOfMonth=13, dayOfWeek=5]", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToString5
    public void testToString5() {
        Partial test = new Partial(DateTimeFieldType.era(), 1);
        assertEquals("[era=1]", test.toString());
        
        test = test.with(DateTimeFieldType.halfdayOfDay(), 0);
        assertEquals("[era=1, halfdayOfDay=0]", test.toString());
    }

// org.joda.time.TestPartial_Basics::testToString_String
    public void testToString_String() {
        Partial test = createHourMinPartial();
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString("yyyy HH"));
        assertEquals("10:20", test.toString((String) null));
    }

// org.joda.time.TestPartial_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        Partial test = createHourMinPartial();
        assertEquals("10 20", test.toString("H m", Locale.ENGLISH));
        assertEquals("10:20", test.toString(null, Locale.ENGLISH));
        assertEquals("10 20", test.toString("H m", null));
        assertEquals("10:20", test.toString(null, null));
    }

// org.joda.time.TestPartial_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        Partial test = createHourMinPartial();
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("10:20", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestSerialization::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestSerialization::testSerializedInstant
    public void testSerializedInstant() throws Exception {
        Instant test = new Instant();
        loadAndCompare(test, "Instant", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateTime
    public void testSerializedDateTime() throws Exception {
        DateTime test = new DateTime();
        loadAndCompare(test, "DateTime", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateTimeProperty
    public void testSerializedDateTimeProperty() throws Exception {
        DateTime.Property test = new DateTime().hourOfDay();
        loadAndCompare(test, "DateTimeProperty", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedMutableDateTime
    public void testSerializedMutableDateTime() throws Exception {
        MutableDateTime test = new MutableDateTime();
        loadAndCompare(test, "MutableDateTime", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedMutableDateTimeProperty
    public void testSerializedMutableDateTimeProperty() throws Exception {
        MutableDateTime.Property test = new MutableDateTime().hourOfDay();
        loadAndCompare(test, "MutableDateTimeProperty", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateMidnight
    public void testSerializedDateMidnight() throws Exception {
        DateMidnight test = new DateMidnight();
        loadAndCompare(test, "DateMidnight", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateMidnightProperty
    public void testSerializedDateMidnightProperty() throws Exception {
        DateMidnight.Property test = new DateMidnight().monthOfYear();
        loadAndCompare(test, "DateMidnightProperty", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedLocalDate
    public void testSerializedLocalDate() throws Exception {
        LocalDate test = new LocalDate();
        loadAndCompare(test, "LocalDate", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedLocalDateBuddhist
    public void testSerializedLocalDateBuddhist() throws Exception {
        LocalDate test = new LocalDate(BuddhistChronology.getInstanceUTC());
        loadAndCompare(test, "LocalDateBuddhist", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedLocalTime
    public void testSerializedLocalTime() throws Exception {
        LocalTime test = new LocalTime();
        loadAndCompare(test, "LocalTime", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedLocalDateTime
    public void testSerializedLocalDateTime() throws Exception {
        LocalDateTime test = new LocalDateTime();
        loadAndCompare(test, "LocalDateTime", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedYearMonthDay
    public void testSerializedYearMonthDay() throws Exception {
        YearMonthDay test = new YearMonthDay();
        loadAndCompare(test, "YearMonthDay", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedTimeOfDay
    public void testSerializedTimeOfDay() throws Exception {
        TimeOfDay test = new TimeOfDay();
        loadAndCompare(test, "TimeOfDay", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedDateTimeZoneUTC
    public void testSerializedDateTimeZoneUTC() throws Exception {
        DateTimeZone test = DateTimeZone.UTC;
        loadAndCompare(test, "DateTimeZoneUTC", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedDateTimeZone
    public void testSerializedDateTimeZone() throws Exception {
        
        
        DateTimeZone test = DateTimeZone.forID("Europe/Paris");
        loadAndCompare(test, "DateTimeZone", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testDuration
    public void testDuration() throws Exception {
        Duration test = Duration.millis(12345);
        loadAndCompare(test, "Duration", false);
        inlineCompare(test, false);
    }

// org.joda.time.TestSerialization::testSerializedCopticChronology
    public void testSerializedCopticChronology() throws Exception {
        CopticChronology test = CopticChronology.getInstance(LONDON);
        loadAndCompare(test, "CopticChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedISOChronology
    public void testSerializedISOChronology() throws Exception {
        ISOChronology test = ISOChronology.getInstance(PARIS);
        loadAndCompare(test, "ISOChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedGJChronology
    public void testSerializedGJChronology() throws Exception {
        GJChronology test = GJChronology.getInstance(TOKYO);
        loadAndCompare(test, "GJChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedGJChronologyChangedInternals
    public void testSerializedGJChronologyChangedInternals() throws Exception {
        GJChronology test = GJChronology.getInstance(PARIS, 123L, 2);
        loadAndCompare(test, "GJChronologyChangedInternals", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedGregorianChronology
    public void testSerializedGregorianChronology() throws Exception {
        GregorianChronology test = GregorianChronology.getInstance(PARIS);
        loadAndCompare(test, "GregorianChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedJulianChronology
    public void testSerializedJulianChronology() throws Exception {
        JulianChronology test = JulianChronology.getInstance(PARIS);
        loadAndCompare(test, "JulianChronology", true);
        inlineCompare(test, true);
    }

// org.joda.time.TestSerialization::testSerializedBuddhistChronology
    public void testSerializedBuddhistChronology() throws Exception {
        BuddhistChronology test = BuddhistChronology.getInstance(PARIS);
        loadAndCompare(test, "BuddhistChronology", true);
        inlineCompare(test, true);
    }

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

// org.joda.time.TestTimeOfDay_Basics::testGet
    public void testGet() {
        TimeOfDay test = new TimeOfDay();
        assertEquals(10 + OFFSET, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(30, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(40, test.get(DateTimeFieldType.millisOfSecond()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.dayOfMonth());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testSize
    public void testSize() {
        TimeOfDay test = new TimeOfDay();
        assertEquals(4, test.size());
    }

// org.joda.time.TestTimeOfDay_Basics::testGetFieldType
    public void testGetFieldType() {
        TimeOfDay test = new TimeOfDay(COPTIC_PARIS);
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

// org.joda.time.TestTimeOfDay_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        TimeOfDay test = new TimeOfDay(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.hourOfDay(), fields[0]);
        assertSame(DateTimeFieldType.minuteOfHour(), fields[1]);
        assertSame(DateTimeFieldType.secondOfMinute(), fields[2]);
        assertSame(DateTimeFieldType.millisOfSecond(), fields[3]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestTimeOfDay_Basics::testGetField
    public void testGetField() {
        TimeOfDay test = new TimeOfDay(COPTIC_PARIS);
        assertSame(CopticChronology.getInstanceUTC().hourOfDay(), test.getField(0));
        assertSame(CopticChronology.getInstanceUTC().minuteOfHour(), test.getField(1));
        assertSame(CopticChronology.getInstanceUTC().secondOfMinute(), test.getField(2));
        assertSame(CopticChronology.getInstanceUTC().millisOfSecond(), test.getField(3));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(5);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testGetFields
    public void testGetFields() {
        TimeOfDay test = new TimeOfDay(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertSame(CopticChronology.getInstanceUTC().hourOfDay(), fields[0]);
        assertSame(CopticChronology.getInstanceUTC().minuteOfHour(), fields[1]);
        assertSame(CopticChronology.getInstanceUTC().secondOfMinute(), fields[2]);
        assertSame(CopticChronology.getInstanceUTC().millisOfSecond(), fields[3]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestTimeOfDay_Basics::testGetValue
    public void testGetValue() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
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

// org.joda.time.TestTimeOfDay_Basics::testGetValues
    public void testGetValues() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        int[] values = test.getValues();
        assertEquals(10, values[0]);
        assertEquals(20, values[1]);
        assertEquals(30, values[2]);
        assertEquals(40, values[3]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestTimeOfDay_Basics::testIsSupported
    public void testIsSupported() {
        TimeOfDay test = new TimeOfDay(COPTIC_PARIS);
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfMinute()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfSecond()));
        assertEquals(false, test.isSupported(DateTimeFieldType.dayOfMonth()));
    }

// org.joda.time.TestTimeOfDay_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        TimeOfDay test1 = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        TimeOfDay test2 = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        TimeOfDay test3 = new TimeOfDay(15, 20, 30, 40);
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

// org.joda.time.TestTimeOfDay_Basics::testCompareTo
    public void testCompareTo() {
        TimeOfDay test1 = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay test1a = new TimeOfDay(10, 20, 30, 40);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        TimeOfDay test2 = new TimeOfDay(10, 20, 35, 40);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        TimeOfDay test3 = new TimeOfDay(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
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
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

    }

// org.joda.time.TestTimeOfDay_Basics::testIsEqual_TOD
    public void testIsEqual_TOD() {
        TimeOfDay test1 = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay test1a = new TimeOfDay(10, 20, 30, 40);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        TimeOfDay test2 = new TimeOfDay(10, 20, 35, 40);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        TimeOfDay test3 = new TimeOfDay(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new TimeOfDay(10, 20, 35, 40).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testIsBefore_TOD
    public void testIsBefore_TOD() {
        TimeOfDay test1 = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay test1a = new TimeOfDay(10, 20, 30, 40);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        TimeOfDay test2 = new TimeOfDay(10, 20, 35, 40);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        TimeOfDay test3 = new TimeOfDay(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new TimeOfDay(10, 20, 35, 40).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testIsAfter_TOD
    public void testIsAfter_TOD() {
        TimeOfDay test1 = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay test1a = new TimeOfDay(10, 20, 30, 40);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        TimeOfDay test2 = new TimeOfDay(10, 20, 35, 40);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        TimeOfDay test3 = new TimeOfDay(10, 20, 35, 40, GregorianChronology.getInstanceUTC());
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new TimeOfDay(10, 20, 35, 40).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithChronologyRetainFields_Chrono
    public void testWithChronologyRetainFields_Chrono() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        TimeOfDay test = base.withChronologyRetainFields(BUDDHIST_TOKYO);
        check(base, 10, 20, 30, 40);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 10, 20, 30, 40);
        assertEquals(BUDDHIST_UTC, test.getChronology());
    }

// org.joda.time.TestTimeOfDay_Basics::testWithChronologyRetainFields_sameChrono
    public void testWithChronologyRetainFields_sameChrono() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        TimeOfDay test = base.withChronologyRetainFields(COPTIC_TOKYO);
        assertSame(base, test);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithChronologyRetainFields_nullChrono
    public void testWithChronologyRetainFields_nullChrono() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        TimeOfDay test = base.withChronologyRetainFields(null);
        check(base, 10, 20, 30, 40);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 10, 20, 30, 40);
        assertEquals(ISO_UTC, test.getChronology());
    }

// org.joda.time.TestTimeOfDay_Basics::testWithField1
    public void testWithField1() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay result = test.withField(DateTimeFieldType.hourOfDay(), 15);
        
        assertEquals(new TimeOfDay(10, 20, 30, 40), test);
        assertEquals(new TimeOfDay(15, 20, 30, 40), result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithField2
    public void testWithField2() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithField3
    public void testWithField3() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        try {
            test.withField(DateTimeFieldType.dayOfMonth(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithField4
    public void testWithField4() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay result = test.withField(DateTimeFieldType.hourOfDay(), 10);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded1
    public void testWithFieldAdded1() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay result = test.withFieldAdded(DurationFieldType.hours(), 6);
        
        assertEquals(new TimeOfDay(10, 20, 30, 40), test);
        assertEquals(new TimeOfDay(16, 20, 30, 40), result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded2
    public void testWithFieldAdded2() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded3
    public void testWithFieldAdded3() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded4
    public void testWithFieldAdded4() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay result = test.withFieldAdded(DurationFieldType.hours(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded5
    public void testWithFieldAdded5() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        try {
            test.withFieldAdded(DurationFieldType.days(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded6
    public void testWithFieldAdded6() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        TimeOfDay result = test.withFieldAdded(DurationFieldType.hours(), 16);
        
        assertEquals(new TimeOfDay(10, 20, 30, 40), test);
        assertEquals(new TimeOfDay(2, 20, 30, 40), result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded7
    public void testWithFieldAdded7() {
        TimeOfDay test = new TimeOfDay(23, 59, 59, 999);
        TimeOfDay result = test.withFieldAdded(DurationFieldType.millis(), 1);
        assertEquals(new TimeOfDay(0, 0, 0, 0), result);
        
        test = new TimeOfDay(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.seconds(), 1);
        assertEquals(new TimeOfDay(0, 0, 0, 999), result);
        
        test = new TimeOfDay(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.minutes(), 1);
        assertEquals(new TimeOfDay(0, 0, 59, 999), result);
        
        test = new TimeOfDay(23, 59, 59, 999);
        result = test.withFieldAdded(DurationFieldType.hours(), 1);
        assertEquals(new TimeOfDay(0, 59, 59, 999), result);
    }

// org.joda.time.TestTimeOfDay_Basics::testWithFieldAdded8
    public void testWithFieldAdded8() {
        TimeOfDay test = new TimeOfDay(0, 0, 0, 0);
        TimeOfDay result = test.withFieldAdded(DurationFieldType.millis(), -1);
        assertEquals(new TimeOfDay(23, 59, 59, 999), result);
        
        test = new TimeOfDay(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.seconds(), -1);
        assertEquals(new TimeOfDay(23, 59, 59, 0), result);
        
        test = new TimeOfDay(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.minutes(), -1);
        assertEquals(new TimeOfDay(23, 59, 0, 0), result);
        
        test = new TimeOfDay(0, 0, 0, 0);
        result = test.withFieldAdded(DurationFieldType.hours(), -1);
        assertEquals(new TimeOfDay(23, 0, 0, 0), result);
    }

// org.joda.time.TestTimeOfDay_Basics::testPlus_RP
    public void testPlus_RP() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, BuddhistChronology.getInstance());
        TimeOfDay result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        TimeOfDay expected = new TimeOfDay(15, 26, 37, 48, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testPlusHours_int
    public void testPlusHours_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.plusHours(1);
        TimeOfDay expected = new TimeOfDay(2, 2, 3, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testPlusMinutes_int
    public void testPlusMinutes_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.plusMinutes(1);
        TimeOfDay expected = new TimeOfDay(1, 3, 3, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testPlusSeconds_int
    public void testPlusSeconds_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.plusSeconds(1);
        TimeOfDay expected = new TimeOfDay(1, 2, 4, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testPlusMillis_int
    public void testPlusMillis_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.plusMillis(1);
        TimeOfDay expected = new TimeOfDay(1, 2, 3, 5, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testMinus_RP
    public void testMinus_RP() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, BuddhistChronology.getInstance());
        TimeOfDay result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        TimeOfDay expected = new TimeOfDay(9, 19, 29, 39, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testMinusHours_int
    public void testMinusHours_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.minusHours(1);
        TimeOfDay expected = new TimeOfDay(0, 2, 3, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testMinusMinutes_int
    public void testMinusMinutes_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.minusMinutes(1);
        TimeOfDay expected = new TimeOfDay(1, 1, 3, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testMinusSeconds_int
    public void testMinusSeconds_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.minusSeconds(1);
        TimeOfDay expected = new TimeOfDay(1, 2, 2, 4, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testMinusMillis_int
    public void testMinusMillis_int() {
        TimeOfDay test = new TimeOfDay(1, 2, 3, 4, BuddhistChronology.getInstance());
        TimeOfDay result = test.minusMillis(1);
        TimeOfDay expected = new TimeOfDay(1, 2, 3, 3, BuddhistChronology.getInstance());
        assertEquals(expected, result);
        
        result = test.minusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestTimeOfDay_Basics::testToLocalTime
    public void testToLocalTime() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_UTC);
        LocalTime test = base.toLocalTime();
        assertEquals(new LocalTime(10, 20, 30, 40, COPTIC_UTC), test);
    }

// org.joda.time.TestTimeOfDay_Basics::testToDateTimeToday
    public void testToDateTimeToday() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS); 
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

// org.joda.time.TestTimeOfDay_Basics::testToDateTimeToday_Zone
    public void testToDateTimeToday_Zone() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS); 
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

// org.joda.time.TestTimeOfDay_Basics::testToDateTimeToday_nullZone
    public void testToDateTimeToday_nullZone() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS); 
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

// org.joda.time.TestTimeOfDay_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        TimeOfDay base = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        DateTime dt = new DateTime(0L); 
        assertEquals("1970-01-01T01:00:00.000+01:00", dt.toString());
        
        DateTime test = base.toDateTime(dt);
        check(base, 10, 20, 30, 40);
        assertEquals("1970-01-01T01:00:00.000+01:00", dt.toString());
        assertEquals("1970-01-01T10:20:30.040+01:00", test.toString());
    }

// org.joda.time.TestTimeOfDay_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        TimeOfDay base = new TimeOfDay(1, 2, 3, 4);
        DateTimeUtils.setCurrentMillisFixed(TEST_TIME2);
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 1, 2, 3, 4);
        assertEquals("1970-01-02T01:02:03.004+01:00", test.toString());
    }

// org.joda.time.TestTimeOfDay_Basics::testWithers
    public void testWithers() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        check(test.withHourOfDay(6), 6, 20, 30, 40);
        check(test.withMinuteOfHour(6), 10, 6, 30, 40);
        check(test.withSecondOfMinute(6), 10, 20, 6, 40);
        check(test.withMillisOfSecond(6), 10, 20, 30, 6);
        try {
            test.withHourOfDay(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withHourOfDay(24);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testProperty
    public void testProperty() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals(test.hourOfDay(), test.property(DateTimeFieldType.hourOfDay()));
        assertEquals(test.minuteOfHour(), test.property(DateTimeFieldType.minuteOfHour()));
        assertEquals(test.secondOfMinute(), test.property(DateTimeFieldType.secondOfMinute()));
        assertEquals(test.millisOfSecond(), test.property(DateTimeFieldType.millisOfSecond()));
        try {
            test.property(DateTimeFieldType.millisOfDay());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestTimeOfDay_Basics::testSerialization
    public void testSerialization() throws Exception {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        TimeOfDay result = (TimeOfDay) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
    }

// org.joda.time.TestTimeOfDay_Basics::testToString
    public void testToString() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals("T10:20:30.040", test.toString());
    }

// org.joda.time.TestTimeOfDay_Basics::testToString_String
    public void testToString_String() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString("yyyy HH"));
        assertEquals("T10:20:30.040", test.toString((String) null));
    }

// org.joda.time.TestTimeOfDay_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals("10 20", test.toString("H m", Locale.ENGLISH));
        assertEquals("T10:20:30.040", test.toString(null, Locale.ENGLISH));
        assertEquals("10 20", test.toString("H m", null));
        assertEquals("T10:20:30.040", test.toString(null, null));
    }

// org.joda.time.TestTimeOfDay_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        TimeOfDay test = new TimeOfDay(10, 20, 30, 40);
        assertEquals("\ufffd\ufffd\ufffd\ufffd 10", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("T10:20:30.040", test.toString((DateTimeFormatter) null));
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

// org.joda.time.format.TestDateTimeFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        DateTimeFormat f = new DateTimeFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_era
    public void testFormat_era() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("G").withLocale(Locale.UK);
        assertEquals(dt.toString(), "AD", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "AD", f.print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals(dt.toString(), "AD", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_centuryOfEra
    public void testFormat_centuryOfEra() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("C").withLocale(Locale.UK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "1", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_yearOfEra
    public void testFormat_yearOfEra() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("Y").withLocale(Locale.UK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "124", f.print(dt));  
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_yearOfEra_twoDigit
    public void testFormat_yearOfEra_twoDigit() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("YY").withLocale(Locale.UK);
        assertEquals(dt.toString(), "04", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "23", f.print(dt));
        
        
        f = f.withZoneUTC();
        DateTime expect = null;
        expect = new DateTime(2004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));
        
        expect = new DateTime(1922, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("22"));
        
        expect = new DateTime(2021, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("21"));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = f.withPivotYear(new Integer(2050));
        expect = new DateTime(2000, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("00"));

        expect = new DateTime(2099, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("99"));

        
        f = DateTimeFormat.forPattern("YY").withLocale(Locale.UK);
        f = f.withZoneUTC();
        f.parseDateTime("5");
        f.parseDateTime("005");
        f.parseDateTime("+50");
        f.parseDateTime("-50");
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_yearOfEraParse
    public void testFormat_yearOfEraParse() {
        Chronology chrono = GJChronology.getInstanceUTC();

        DateTimeFormatter f = DateTimeFormat
            .forPattern("YYYY-MM GG")
            .withChronology(chrono)
            .withLocale(Locale.UK);

        DateTime dt = new DateTime(2005, 10, 1, 0, 0, 0, 0, chrono);
        assertEquals(dt, f.parseDateTime("2005-10 AD"));
        assertEquals(dt, f.parseDateTime("2005-10 CE"));

        dt = new DateTime(-2005, 10, 1, 0, 0, 0, 0, chrono);
        assertEquals(dt, f.parseDateTime("2005-10 BC"));
        assertEquals(dt, f.parseDateTime("2005-10 BCE"));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_year
    public void testFormat_year() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("y").withLocale(Locale.UK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "-123", f.print(dt));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_year_twoDigit
    public void testFormat_year_twoDigit() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("yy").withLocale(Locale.UK);
        assertEquals(dt.toString(), "04", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "23", f.print(dt));
        
        
        f = f.withZoneUTC();
        DateTime expect = null;
        expect = new DateTime(2004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));
        
        expect = new DateTime(1922, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("22"));
        
        expect = new DateTime(2021, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("21"));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = f.withPivotYear(new Integer(2050));
        expect = new DateTime(2000, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("00"));

        expect = new DateTime(2099, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("99"));

        
        
        f = new DateTimeFormatterBuilder().appendTwoDigitYear(2000).toFormatter();
        f = f.withZoneUTC();
        try {
            f.parseDateTime("5");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("005");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("+50");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("-50");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = DateTimeFormat.forPattern("yy").withLocale(Locale.UK);
        f = f.withZoneUTC();
        f.parseDateTime("5");
        f.parseDateTime("005");
        f.parseDateTime("+50");
        f.parseDateTime("-50");

        
        f = new DateTimeFormatterBuilder().appendTwoDigitYear(2000, true).toFormatter();
        f = f.withZoneUTC();
        expect = new DateTime(2004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+04"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-04"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("4"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-4"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("004"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+004"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-004"));

        expect = new DateTime(3004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("3004"));

        expect = new DateTime(3004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+3004"));

        expect = new DateTime(-3004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-3004"));

        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_year_long
    public void testFormat_year_long() {
        DateTime dt = new DateTime(278004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy");
        assertEquals(dt.toString(), "278004", f.print(dt));
        
        
        f = DateTimeFormat.forPattern("yyyyMMdd");
        assertEquals(dt.toString(), "2780040609", f.print(dt));
        
        
        f = DateTimeFormat.forPattern("yyyyddMM");
        assertEquals(dt.toString(), "2780040906", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_weekyear
    public void testFormat_weekyear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("x").withLocale(Locale.UK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "-123", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_weekyearOfEra_twoDigit
    public void testFormat_weekyearOfEra_twoDigit() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("xx").withLocale(Locale.UK);
        assertEquals(dt.toString(), "04", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "23", f.print(dt));
        
        
        f = f.withZoneUTC();
        DateTime expect = null;
        expect = new DateTime(2003, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));
        
        expect = new DateTime(1922, 1, 2, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("22"));
        
        expect = new DateTime(2021, 1, 4, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("21"));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = f.withPivotYear(new Integer(2050));
        expect = new DateTime(2000, 1, 3, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals(expect, f.parseDateTime("00"));

        expect = new DateTime(2098, 12, 29, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals(expect, f.parseDateTime("99"));

        
        
        f = new DateTimeFormatterBuilder().appendTwoDigitWeekyear(2000).toFormatter();
        f = f.withZoneUTC();
        try {
            f.parseDateTime("5");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("005");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("+50");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("-50");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = DateTimeFormat.forPattern("xx").withLocale(Locale.UK);
        f = f.withZoneUTC();
        f.parseDateTime("5");
        f.parseDateTime("005");
        f.parseDateTime("+50");
        f.parseDateTime("-50");

        
        f = new DateTimeFormatterBuilder().appendTwoDigitWeekyear(2000, true).toFormatter();
        f = f.withZoneUTC();
        expect = new DateTime(2003, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+04"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-04"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("4"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-4"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("004"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+004"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-004"));

        expect = new DateTime(3004, 1, 2, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("3004"));

        expect = new DateTime(3004, 1, 2, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+3004"));

        expect = new DateTime(-3004, 1, 4, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-3004"));

        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_weekOfWeekyear
    public void testFormat_weekOfWeekyear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("w").withLocale(Locale.UK);
        assertEquals(dt.toString(), "24", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "24", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "24", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfWeek
    public void testFormat_dayOfWeek() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("e").withLocale(Locale.UK);
        assertEquals(dt.toString(), "3", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "3", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "3", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfWeekShortText
    public void testFormat_dayOfWeekShortText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("E").withLocale(Locale.UK);
        assertEquals(dt.toString(), "Wed", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "Wed", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Wed", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "mer.", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfWeekText
    public void testFormat_dayOfWeekText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("EEEE").withLocale(Locale.UK);
        assertEquals(dt.toString(), "Wednesday", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "Wednesday", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Wednesday", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "mercredi", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfYearText
    public void testFormat_dayOfYearText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("D").withLocale(Locale.UK);
        assertEquals(dt.toString(), "161", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "161", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "161", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_monthOfYear
    public void testFormat_monthOfYear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("M").withLocale(Locale.UK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "6", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_monthOfYearShortText
    public void testFormat_monthOfYearShortText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("MMM").withLocale(Locale.UK);
        assertEquals(dt.toString(), "Jun", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "Jun", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Jun", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "juin", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_monthOfYearText
    public void testFormat_monthOfYearText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("MMMM").withLocale(Locale.UK);
        assertEquals(dt.toString(), "June", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "June", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "June", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "juin", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfMonth
    public void testFormat_dayOfMonth() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("d").withLocale(Locale.UK);
        assertEquals(dt.toString(), "9", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "9", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "9", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_halfdayOfDay
    public void testFormat_halfdayOfDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("a").withLocale(Locale.UK);
        assertEquals(dt.toString(), "am", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "am", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "pm", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_hourOfHalfday
    public void testFormat_hourOfHalfday() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("K").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "7", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "0", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_clockhourOfHalfday
    public void testFormat_clockhourOfHalfday() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("h").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "7", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "12", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_hourOfDay
    public void testFormat_hourOfDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("H").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "19", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "0", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_clockhourOfDay
    public void testFormat_clockhourOfDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("k").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "19", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "24", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_minute
    public void testFormat_minute() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("m").withLocale(Locale.UK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "20", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_second
    public void testFormat_second() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("s").withLocale(Locale.UK);
        assertEquals(dt.toString(), "30", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "30", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "30", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_fractionOfSecond
    public void testFormat_fractionOfSecond() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("SSS").withLocale(Locale.UK);
        assertEquals(dt.toString(), "040", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "040", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "040", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_fractionOfSecondLong
    public void testFormat_fractionOfSecondLong() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("SSSSSS").withLocale(Locale.UK);
        assertEquals(dt.toString(), "040000", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "040000", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "040000", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneText
    public void testFormat_zoneText() {}

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneLongText
    public void testFormat_zoneLongText() {}

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneAmount
    public void testFormat_zoneAmount() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("Z").withLocale(Locale.UK);
        assertEquals(dt.toString(), "+0000", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "-0400", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "+0900", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneAmountColon
    public void testFormat_zoneAmountColon() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("ZZ").withLocale(Locale.UK);
        assertEquals(dt.toString(), "+00:00", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "-04:00", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "+09:00", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneAmountID
    public void testFormat_zoneAmountID() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("ZZZ").withLocale(Locale.UK);
        assertEquals(dt.toString(), "UTC", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "America/New_York", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Asia/Tokyo", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_other
    public void testFormat_other() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("'Hello' ''");
        assertEquals("Hello '", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_invalid
    public void testFormat_invalid() {
        try {
            DateTimeFormat.forPattern(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forPattern("");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forPattern("A");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forPattern("dd/mm/AA");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_samples
    public void testFormat_samples() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH.mm.ss");
        assertEquals("2004-06-09 10.20.30", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_shortBasicParse
    public void testFormat_shortBasicParse() {
        
        

        DateTime dt = new DateTime(2004, 3, 9, 0, 0, 0, 0);

        DateTimeFormatter f = DateTimeFormat.forPattern("yyMMdd");
        assertEquals(dt, f.parseDateTime("040309"));
        try {
            assertEquals(dt, f.parseDateTime("20040309"));
            fail();
        } catch (IllegalArgumentException ex) {}

        f = DateTimeFormat.forPattern("yy/MM/dd");
        assertEquals(dt, f.parseDateTime("04/03/09"));
        assertEquals(dt, f.parseDateTime("2004/03/09"));
    }

// org.joda.time.format.TestDateTimeFormat::testParse_pivotYear
    public void testParse_pivotYear() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd.MM.yy").withPivotYear(2050).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("25.12.15");
        assertEquals(date.getYear(), 2015);
        
        date = dateFormatter.parseDateTime("25.12.00");
        assertEquals(date.getYear(), 2000);
        
        date = dateFormatter.parseDateTime("25.12.99");
        assertEquals(date.getYear(), 2099);
    }

// org.joda.time.format.TestDateTimeFormat::testParse_pivotYear_ignored4DigitYear
    public void testParse_pivotYear_ignored4DigitYear() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd.MM.yyyy").withPivotYear(2050).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("25.12.15");
        assertEquals(date.getYear(), 15);
        
        date = dateFormatter.parseDateTime("25.12.00");
        assertEquals(date.getYear(), 0);
        
        date = dateFormatter.parseDateTime("25.12.99");
        assertEquals(date.getYear(), 99);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShort_UK
    public void testFormatParse_textMonthJanShort_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 1, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals(str, "23 Jan 2007");
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShortLowerCase_UK
    public void testFormatParse_textMonthJanShortLowerCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 jan 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShortUpperCase_UK
    public void testFormatParse_textMonthJanShortUpperCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 JAN 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testParse_textMonthJanLong_UK
    public void testParse_textMonthJanLong_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("23 January 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanLongLowerCase_UK
    public void testFormatParse_textMonthJanLongLowerCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 january 2007");
        check(date, 2007, 1, 23);
    }
