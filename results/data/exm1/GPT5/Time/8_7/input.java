// buggy code
    public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) throws IllegalArgumentException {
        if (hoursOffset == 0 && minutesOffset == 0) {
            return DateTimeZone.UTC;
        }
        if (hoursOffset < -23 || hoursOffset > 23) {
            throw new IllegalArgumentException("Hours out of range: " + hoursOffset);
        }
        if (minutesOffset < 0 || minutesOffset > 59) {
            throw new IllegalArgumentException("Minutes out of range: " + minutesOffset);
        }
        int offset = 0;
        try {
            int hoursInMinutes = hoursOffset * 60;
            if (hoursInMinutes < 0) {
                minutesOffset = hoursInMinutes - minutesOffset;
            } else {
                minutesOffset = hoursInMinutes + minutesOffset;
            }
            offset = FieldUtils.safeMultiply(minutesOffset, DateTimeConstants.MILLIS_PER_MINUTE);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
        return forOffsetMillis(offset);
    }

// relevant test
// org.joda.time.TestPartial_Basics::testWith1
    public void testWith1() {
        Partial test = createHourMinPartial();
        Partial result = test.with(DateTimeFieldType.hourOfDay(), 15);
        check(test, 10, 20);
        check(result, 15, 20);
    }

// org.joda.time.TestPartial_Basics::testWith2
    public void testWith2() {
        Partial test = createHourMinPartial();
        try {
            test.with(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWith3a
    public void testWith3a() {
        Partial test = createHourMinPartial();
        Partial result = test.with(DateTimeFieldType.secondOfMinute(), 15);
        check(test, 10, 20);
        assertEquals(3, result.size());
        assertEquals(true, result.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, result.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(true, result.isSupported(DateTimeFieldType.secondOfMinute()));
        assertEquals(DateTimeFieldType.hourOfDay(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.minuteOfHour(), result.getFieldType(1));
        assertEquals(DateTimeFieldType.secondOfMinute(), result.getFieldType(2));
        assertEquals(10, result.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, result.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(15, result.get(DateTimeFieldType.secondOfMinute()));
    }

// org.joda.time.TestPartial_Basics::testWith3b
    public void testWith3b() {
        Partial test = createHourMinPartial();
        Partial result = test.with(DateTimeFieldType.minuteOfDay(), 15);
        check(test, 10, 20);
        assertEquals(3, result.size());
        assertEquals(true, result.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, result.isSupported(DateTimeFieldType.minuteOfDay()));
        assertEquals(true, result.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(DateTimeFieldType.hourOfDay(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.minuteOfDay(), result.getFieldType(1));
        assertEquals(DateTimeFieldType.minuteOfHour(), result.getFieldType(2));
        assertEquals(10, result.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, result.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(15, result.get(DateTimeFieldType.minuteOfDay()));
    }

// org.joda.time.TestPartial_Basics::testWith3c
    public void testWith3c() {
        Partial test = createHourMinPartial();
        Partial result = test.with(DateTimeFieldType.dayOfMonth(), 15);
        check(test, 10, 20);
        assertEquals(3, result.size());
        assertEquals(true, result.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(true, result.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, result.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(DateTimeFieldType.dayOfMonth(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.hourOfDay(), result.getFieldType(1));
        assertEquals(DateTimeFieldType.minuteOfHour(), result.getFieldType(2));
        assertEquals(10, result.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, result.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(15, result.get(DateTimeFieldType.dayOfMonth()));
    }

// org.joda.time.TestPartial_Basics::testWith3d
    public void testWith3d() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = test.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals(2, result.size());
        assertEquals(2005, result.get(DateTimeFieldType.year()));
        assertEquals(6, result.get(DateTimeFieldType.monthOfYear()));
    }

// org.joda.time.TestPartial_Basics::testWith3e
    public void testWith3e() {
        Partial test = new Partial(DateTimeFieldType.era(), 1);
        Partial result = test.with(DateTimeFieldType.halfdayOfDay(), 0);
        assertEquals(2, result.size());
        assertEquals(1, result.get(DateTimeFieldType.era()));
        assertEquals(0, result.get(DateTimeFieldType.halfdayOfDay()));
        assertEquals(0, result.indexOf(DateTimeFieldType.era()));
        assertEquals(1, result.indexOf(DateTimeFieldType.halfdayOfDay()));
    }

// org.joda.time.TestPartial_Basics::testWith3f
    public void testWith3f() {
        Partial test = new Partial(DateTimeFieldType.halfdayOfDay(), 0);
        Partial result = test.with(DateTimeFieldType.era(), 1);
        assertEquals(2, result.size());
        assertEquals(1, result.get(DateTimeFieldType.era()));
        assertEquals(0, result.get(DateTimeFieldType.halfdayOfDay()));
        assertEquals(0, result.indexOf(DateTimeFieldType.era()));
        assertEquals(1, result.indexOf(DateTimeFieldType.halfdayOfDay()));
    }

// org.joda.time.TestPartial_Basics::testWith4
    public void testWith4() {
        Partial test = createHourMinPartial();
        Partial result = test.with(DateTimeFieldType.hourOfDay(), 10);
        assertSame(test, result);
    }

// org.joda.time.TestPartial_Basics::testWithout1
    public void testWithout1() {
        Partial test = createHourMinPartial();
        Partial result = test.without(DateTimeFieldType.year());
        check(test, 10, 20);
        check(result, 10, 20);
    }

// org.joda.time.TestPartial_Basics::testWithout2
    public void testWithout2() {
        Partial test = createHourMinPartial();
        Partial result = test.without((DateTimeFieldType) null);
        check(test, 10, 20);
        check(result, 10, 20);
    }

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

// org.joda.time.TestPartial_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        Partial test = new Partial();
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(0, test.size());
    }

// org.joda.time.TestPartial_Constructors::testConstructor_Chrono
    public void testConstructor_Chrono() throws Throwable {
        Partial test = new Partial((Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(0, test.size());
        
        test = new Partial(GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(0, test.size());
    }

// org.joda.time.TestPartial_Constructors::testConstructor_Type_int
    public void testConstructor_Type_int() throws Throwable {
        Partial test = new Partial(DateTimeFieldType.dayOfYear(), 4);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1, test.size());
        assertEquals(4, test.getValue(0));
        assertEquals(4, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfYear()));
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx1_Type_int
    public void testConstructorEx1_Type_int() throws Throwable {
        try {
            new Partial(null, 4);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must not be null");
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx2_Type_int
    public void testConstructorEx2_Type_int() throws Throwable {
        try {
            new Partial(DateTimeFieldType.dayOfYear(), 0);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructor_Type_int_Chrono
    public void testConstructor_Type_int_Chrono() throws Throwable {
        Partial test = new Partial(DateTimeFieldType.dayOfYear(), 4, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1, test.size());
        assertEquals(4, test.getValue(0));
        assertEquals(4, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfYear()));
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx_Type_int_Chrono
    public void testConstructorEx_Type_int_Chrono() throws Throwable {
        try {
            new Partial(null, 4, ISO_UTC);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must not be null");
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx2_Type_int_Chrono
    public void testConstructorEx2_Type_int_Chrono() throws Throwable {
        try {
            new Partial(DateTimeFieldType.dayOfYear(), 0, ISO_UTC);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructor_TypeArray_intArray
    public void testConstructor_TypeArray_intArray() throws Throwable {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.dayOfYear()
        };
        int[] values = new int[] {2005, 33};
        Partial test = new Partial(types, values);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2, test.size());
        assertEquals(2005, test.getValue(0));
        assertEquals(2005, test.get(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(33, test.getValue(1));
        assertEquals(33, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfYear()));
        assertEquals(true, Arrays.equals(test.getFieldTypes(), types));
        assertEquals(true, Arrays.equals(test.getValues(), values));
    }

// org.joda.time.TestPartial_Constructors::testConstructor2_TypeArray_intArray
    public void testConstructor2_TypeArray_intArray() throws Throwable {
        DateTimeFieldType[] types = new DateTimeFieldType[0];
        int[] values = new int[0];
        Partial test = new Partial(types, values);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(0, test.size());
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx1_TypeArray_intArray
    public void testConstructorEx1_TypeArray_intArray() throws Throwable {
        try {
            new Partial((DateTimeFieldType[]) null, new int[] {1});
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must not be null");
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx3_TypeArray_intArray
    public void testConstructorEx3_TypeArray_intArray() throws Throwable {
        try {
            new Partial(new DateTimeFieldType[] {DateTimeFieldType.dayOfYear()}, null);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must not be null");
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx5_TypeArray_intArray
    public void testConstructorEx5_TypeArray_intArray() throws Throwable {
        try {
            new Partial(new DateTimeFieldType[] {DateTimeFieldType.dayOfYear()}, new int[2]);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "same length");
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx6_TypeArray_intArray
    public void testConstructorEx6_TypeArray_intArray() throws Throwable {
        try {
            new Partial(new DateTimeFieldType[] {null, DateTimeFieldType.dayOfYear()}, new int[2]);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "contain null");
        }
        try {
            new Partial(new DateTimeFieldType[] {DateTimeFieldType.dayOfYear(), null}, new int[2]);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "contain null");
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx7_TypeArray_intArray
    public void testConstructorEx7_TypeArray_intArray() throws Throwable {
        int[] values = new int[] {1, 1, 1};
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfMonth(), DateTimeFieldType.year(), DateTimeFieldType.monthOfYear() };
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must be in order", "largest-smallest");
        }
        
        types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.dayOfMonth(), DateTimeFieldType.monthOfYear() };
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must be in order", "largest-smallest");
        }
        
        types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.era(), DateTimeFieldType.monthOfYear() };
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must be in order", "largest-smallest");
        }
        
        types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.dayOfMonth(), DateTimeFieldType.era() };
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must be in order", "largest-smallest");
        }
        
        types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.dayOfMonth(), DateTimeFieldType.dayOfYear() };
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must be in order", "largest-smallest");
        }
        
        types = new DateTimeFieldType[] {
            DateTimeFieldType.yearOfEra(), DateTimeFieldType.year(), DateTimeFieldType.dayOfYear() };
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must be in order", "largest-smallest");
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx8_TypeArray_intArray
    public void testConstructorEx8_TypeArray_intArray() throws Throwable {
        int[] values = new int[] {1, 1, 1};
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.era(), DateTimeFieldType.year(), DateTimeFieldType.year() };
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must not", "duplicate");
        }
        
        types = new DateTimeFieldType[] {
            DateTimeFieldType.era(), DateTimeFieldType.era(), DateTimeFieldType.monthOfYear() };
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must not", "duplicate");
        }
        
        types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfYear(), DateTimeFieldType.dayOfMonth(), DateTimeFieldType.dayOfMonth() };
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must not", "duplicate");
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx9_TypeArray_intArray
    public void testConstructorEx9_TypeArray_intArray() throws Throwable {
        int[] values = new int[] {3, 0};
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfMonth(), DateTimeFieldType.dayOfWeek()};
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestPartial_Constructors::testConstructor_TypeArray_intArray_Chrono
    public void testConstructor_TypeArray_intArray_Chrono() throws Throwable {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.dayOfYear()
        };
        int[] values = new int[] {2005, 33};
        Partial test = new Partial(types, values, GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(2, test.size());
        assertEquals(2005, test.getValue(0));
        assertEquals(2005, test.get(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(33, test.getValue(1));
        assertEquals(33, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfYear()));
        assertEquals(true, Arrays.equals(test.getFieldTypes(), types));
        assertEquals(true, Arrays.equals(test.getValues(), values));
    }

// org.joda.time.TestPartial_Constructors::testConstructor_Partial
    public void testConstructor_Partial() throws Throwable {
        YearMonthDay ymd = new YearMonthDay(2005, 6, 25, GREGORIAN_PARIS);
        Partial test = new Partial(ymd);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(3, test.size());
        assertEquals(2005, test.getValue(0));
        assertEquals(2005, test.get(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(6, test.getValue(1));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(25, test.getValue(2));
        assertEquals(25, test.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
    }

// org.joda.time.TestPartial_Constructors::testConstructorEx_Partial
    public void testConstructorEx_Partial() throws Throwable {
        try {
            new Partial((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must not be null");
        }
    }

// org.joda.time.TestPartial_Match::testIsMatch_Instant
    public void testIsMatch_Instant() {
        
        Partial test = createYMDwPartial(ISO_UTC, 2005, 7, 2);
        DateTime instant = new DateTime(2005, 7, 5, 0, 0, 0, 0);
        assertEquals(true, test.isMatch(instant));
        
        instant = new DateTime(2005, 7, 4, 0, 0, 0, 0);
        assertEquals(false, test.isMatch(instant));
        
        instant = new DateTime(2005, 7, 6, 0, 0, 0, 0);
        assertEquals(false, test.isMatch(instant));
        
        instant = new DateTime(2005, 7, 12, 0, 0, 0, 0);
        assertEquals(true, test.isMatch(instant));
        
        instant = new DateTime(2005, 7, 19, 0, 0, 0, 0);
        assertEquals(true, test.isMatch(instant));
        
        instant = new DateTime(2005, 7, 26, 0, 0, 0, 0);
        assertEquals(true, test.isMatch(instant));
        
        instant = new DateTime(2005, 8, 2, 0, 0, 0, 0);
        assertEquals(false, test.isMatch(instant));
        
        instant = new DateTime(2006, 7, 5, 0, 0, 0, 0);
        assertEquals(false, test.isMatch(instant));
        
        instant = new DateTime(2005, 6, 5, 0, 0, 0, 0);
        assertEquals(false, test.isMatch(instant));
    }

// org.joda.time.TestPartial_Match::testIsMatch_Partial
    public void testIsMatch_Partial() {
        
        Partial test = createYMDwPartial(ISO_UTC, 2005, 7, 2);
        LocalDate partial = new LocalDate(2005, 7, 5);
        assertEquals(true, test.isMatch(partial));
        
        partial = new LocalDate(2005, 7, 4);
        assertEquals(false, test.isMatch(partial));
        
        partial = new LocalDate(2005, 7, 6);
        assertEquals(false, test.isMatch(partial));
        
        partial = new LocalDate(2005, 7, 12);
        assertEquals(true, test.isMatch(partial));
        
        partial = new LocalDate(2005, 7, 19);
        assertEquals(true, test.isMatch(partial));
        
        partial = new LocalDate(2005, 7, 26);
        assertEquals(true, test.isMatch(partial));
        
        partial = new LocalDate(2005, 8, 2);
        assertEquals(false, test.isMatch(partial));
        
        partial = new LocalDate(2006, 7, 5);
        assertEquals(false, test.isMatch(partial));
        
        partial = new LocalDate(2005, 6, 5);
        assertEquals(false, test.isMatch(partial));
        
        try {
            test.isMatch((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Properties::testPropertyGetHour
    public void testPropertyGetHour() {
        Partial test = new Partial(TYPES, VALUES);
        assertSame(test.getChronology().hourOfDay(), test.property(DateTimeFieldType.hourOfDay()).getField());
        assertEquals("hourOfDay", test.property(DateTimeFieldType.hourOfDay()).getName());
        assertEquals("Property[hourOfDay]", test.property(DateTimeFieldType.hourOfDay()).toString());
        assertSame(test, test.property(DateTimeFieldType.hourOfDay()).getReadablePartial());
        assertSame(test, test.property(DateTimeFieldType.hourOfDay()).getPartial());
        assertEquals(10, test.property(DateTimeFieldType.hourOfDay()).get());
        assertEquals("10", test.property(DateTimeFieldType.hourOfDay()).getAsString());
        assertEquals("10", test.property(DateTimeFieldType.hourOfDay()).getAsText());
        assertEquals("10", test.property(DateTimeFieldType.hourOfDay()).getAsText(Locale.FRENCH));
        assertEquals("10", test.property(DateTimeFieldType.hourOfDay()).getAsShortText());
        assertEquals("10", test.property(DateTimeFieldType.hourOfDay()).getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().hours(), test.property(DateTimeFieldType.hourOfDay()).getDurationField());
        assertEquals(test.getChronology().days(), test.property(DateTimeFieldType.hourOfDay()).getRangeDurationField());
        assertEquals(2, test.property(DateTimeFieldType.hourOfDay()).getMaximumTextLength(null));
        assertEquals(2, test.property(DateTimeFieldType.hourOfDay()).getMaximumShortTextLength(null));
    }

// org.joda.time.TestPartial_Properties::testPropertyGetMaxMinValuesHour
    public void testPropertyGetMaxMinValuesHour() {
        Partial test = new Partial(TYPES, VALUES);
        assertEquals(0, test.property(DateTimeFieldType.hourOfDay()).getMinimumValue());
        assertEquals(0, test.property(DateTimeFieldType.hourOfDay()).getMinimumValueOverall());
        assertEquals(23, test.property(DateTimeFieldType.hourOfDay()).getMaximumValue());
        assertEquals(23, test.property(DateTimeFieldType.hourOfDay()).getMaximumValueOverall());
    }

// org.joda.time.TestPartial_Properties::testPropertyAddHour
    public void testPropertyAddHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).addToCopy(9);
        check(test, 10, 20, 30, 40);
        check(copy, 19, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addToCopy(0);
        check(copy, 10, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addToCopy(13);
        check(copy, 23, 20, 30, 40);
        
        try {
            test.property(DateTimeFieldType.hourOfDay()).addToCopy(14);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addToCopy(-10);
        check(copy, 0, 20, 30, 40);
        
        try {
            test.property(DateTimeFieldType.hourOfDay()).addToCopy(-11);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyAddWrapFieldHour
    public void testPropertyAddWrapFieldHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).addWrapFieldToCopy(9);
        check(test, 10, 20, 30, 40);
        check(copy, 19, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addWrapFieldToCopy(0);
        check(copy, 10, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addWrapFieldToCopy(18);
        check(copy, 4, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.hourOfDay()).addWrapFieldToCopy(-15);
        check(copy, 19, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertySetHour
    public void testPropertySetHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).setCopy(12);
        check(test, 10, 20, 30, 40);
        check(copy, 12, 20, 30, 40);
        
        try {
            test.property(DateTimeFieldType.hourOfDay()).setCopy(24);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(DateTimeFieldType.hourOfDay()).setCopy(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Properties::testPropertySetTextHour
    public void testPropertySetTextHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).setCopy("12");
        check(test, 10, 20, 30, 40);
        check(copy, 12, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyWithMaximumValueHour
    public void testPropertyWithMaximumValueHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).withMaximumValue();
        check(test, 10, 20, 30, 40);
        check(copy, 23, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyWithMinimumValueHour
    public void testPropertyWithMinimumValueHour() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.hourOfDay()).withMinimumValue();
        check(test, 10, 20, 30, 40);
        check(copy, 0, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyCompareToHour
    public void testPropertyCompareToHour() {
        Partial test1 = new Partial(TYPES, VALUES1);
        Partial test2 = new Partial(TYPES, VALUES2);
        assertEquals(true, test1.property(DateTimeFieldType.hourOfDay()).compareTo(test2) < 0);
        assertEquals(true, test2.property(DateTimeFieldType.hourOfDay()).compareTo(test1) > 0);
        assertEquals(true, test1.property(DateTimeFieldType.hourOfDay()).compareTo(test1) == 0);
        try {
            test1.property(DateTimeFieldType.hourOfDay()).compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.property(DateTimeFieldType.hourOfDay()).compareTo(dt2) < 0);
        assertEquals(true, test2.property(DateTimeFieldType.hourOfDay()).compareTo(dt1) > 0);
        assertEquals(true, test1.property(DateTimeFieldType.hourOfDay()).compareTo(dt1) == 0);
        try {
            test1.property(DateTimeFieldType.hourOfDay()).compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Properties::testPropertyGetMinute
    public void testPropertyGetMinute() {
        Partial test = new Partial(TYPES, VALUES);
        assertSame(test.getChronology().minuteOfHour(), test.property(DateTimeFieldType.minuteOfHour()).getField());
        assertEquals("minuteOfHour", test.property(DateTimeFieldType.minuteOfHour()).getName());
        assertEquals("Property[minuteOfHour]", test.property(DateTimeFieldType.minuteOfHour()).toString());
        assertSame(test, test.property(DateTimeFieldType.minuteOfHour()).getReadablePartial());
        assertSame(test, test.property(DateTimeFieldType.minuteOfHour()).getPartial());
        assertEquals(20, test.property(DateTimeFieldType.minuteOfHour()).get());
        assertEquals("20", test.property(DateTimeFieldType.minuteOfHour()).getAsString());
        assertEquals("20", test.property(DateTimeFieldType.minuteOfHour()).getAsText());
        assertEquals("20", test.property(DateTimeFieldType.minuteOfHour()).getAsText(Locale.FRENCH));
        assertEquals("20", test.property(DateTimeFieldType.minuteOfHour()).getAsShortText());
        assertEquals("20", test.property(DateTimeFieldType.minuteOfHour()).getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().minutes(), test.property(DateTimeFieldType.minuteOfHour()).getDurationField());
        assertEquals(test.getChronology().hours(), test.property(DateTimeFieldType.minuteOfHour()).getRangeDurationField());
        assertEquals(2, test.property(DateTimeFieldType.minuteOfHour()).getMaximumTextLength(null));
        assertEquals(2, test.property(DateTimeFieldType.minuteOfHour()).getMaximumShortTextLength(null));
    }

// org.joda.time.TestPartial_Properties::testPropertyGetMaxMinValuesMinute
    public void testPropertyGetMaxMinValuesMinute() {
        Partial test = new Partial(TYPES, VALUES);
        assertEquals(0, test.property(DateTimeFieldType.minuteOfHour()).getMinimumValue());
        assertEquals(0, test.property(DateTimeFieldType.minuteOfHour()).getMinimumValueOverall());
        assertEquals(59, test.property(DateTimeFieldType.minuteOfHour()).getMaximumValue());
        assertEquals(59, test.property(DateTimeFieldType.minuteOfHour()).getMaximumValueOverall());
    }

// org.joda.time.TestPartial_Properties::testPropertyAddMinute
    public void testPropertyAddMinute() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(9);
        check(test, 10, 20, 30, 40);
        check(copy, 10, 29, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(39);
        check(copy, 10, 59, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(40);
        check(copy, 11, 0, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(1 * 60 + 45);
        check(copy, 12, 5, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(13 * 60 + 39);
        check(copy, 23, 59, 30, 40);
        
        try {
            test.property(DateTimeFieldType.minuteOfHour()).addToCopy(13 * 60 + 40);
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-9);
        check(copy, 10, 11, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-19);
        check(copy, 10, 1, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-20);
        check(copy, 10, 0, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-21);
        check(copy, 9, 59, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-(10 * 60 + 20));
        check(copy, 0, 0, 30, 40);
        
        try {
            test.property(DateTimeFieldType.minuteOfHour()).addToCopy(-(10 * 60 + 21));
            fail();
        } catch (IllegalArgumentException ex) {}
        check(test, 10, 20, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyAddWrapFieldMinute
    public void testPropertyAddWrapFieldMinute() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.minuteOfHour()).addWrapFieldToCopy(9);
        check(test, 10, 20, 30, 40);
        check(copy, 10, 29, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addWrapFieldToCopy(49);
        check(copy, 10, 9, 30, 40);
        
        copy = test.property(DateTimeFieldType.minuteOfHour()).addWrapFieldToCopy(-47);
        check(copy, 10, 33, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertySetMinute
    public void testPropertySetMinute() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.minuteOfHour()).setCopy(12);
        check(test, 10, 20, 30, 40);
        check(copy, 10, 12, 30, 40);
        
        try {
            test.property(DateTimeFieldType.minuteOfHour()).setCopy(60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(DateTimeFieldType.minuteOfHour()).setCopy(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Properties::testPropertySetTextMinute
    public void testPropertySetTextMinute() {
        Partial test = new Partial(TYPES, VALUES);
        Partial copy = test.property(DateTimeFieldType.minuteOfHour()).setCopy("12");
        check(test, 10, 20, 30, 40);
        check(copy, 10, 12, 30, 40);
    }

// org.joda.time.TestPartial_Properties::testPropertyCompareToMinute
    public void testPropertyCompareToMinute() {
        Partial test1 = new Partial(TYPES, VALUES1);
        Partial test2 = new Partial(TYPES, VALUES2);
        assertEquals(true, test1.property(DateTimeFieldType.minuteOfHour()).compareTo(test2) < 0);
        assertEquals(true, test2.property(DateTimeFieldType.minuteOfHour()).compareTo(test1) > 0);
        assertEquals(true, test1.property(DateTimeFieldType.minuteOfHour()).compareTo(test1) == 0);
        try {
            test1.property(DateTimeFieldType.minuteOfHour()).compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.property(DateTimeFieldType.minuteOfHour()).compareTo(dt2) < 0);
        assertEquals(true, test2.property(DateTimeFieldType.minuteOfHour()).compareTo(dt1) > 0);
        assertEquals(true, test1.property(DateTimeFieldType.minuteOfHour()).compareTo(dt1) == 0);
        try {
            test1.property(DateTimeFieldType.minuteOfHour()).compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriodType::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestPeriodType::testStandard
    public void testStandard() throws Exception {
        PeriodType type = PeriodType.standard();
        assertEquals(8, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.months(), type.getFieldType(1));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(2));
        assertEquals(DurationFieldType.days(), type.getFieldType(3));
        assertEquals(DurationFieldType.hours(), type.getFieldType(4));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(5));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(6));
        assertEquals(DurationFieldType.millis(), type.getFieldType(7));
        assertEquals("Standard", type.getName());
        assertEquals("PeriodType[Standard]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.standard());
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.standard().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testYearMonthDayTime
    public void testYearMonthDayTime() throws Exception {
        PeriodType type = PeriodType.yearMonthDayTime();
        assertEquals(7, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.months(), type.getFieldType(1));
        assertEquals(DurationFieldType.days(), type.getFieldType(2));
        assertEquals(DurationFieldType.hours(), type.getFieldType(3));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(4));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(5));
        assertEquals(DurationFieldType.millis(), type.getFieldType(6));
        assertEquals("YearMonthDayTime", type.getName());
        assertEquals("PeriodType[YearMonthDayTime]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.yearMonthDayTime());
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.yearMonthDayTime().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testYearMonthDay
    public void testYearMonthDay() throws Exception {
        PeriodType type = PeriodType.yearMonthDay();
        assertEquals(3, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.months(), type.getFieldType(1));
        assertEquals(DurationFieldType.days(), type.getFieldType(2));
        assertEquals("YearMonthDay", type.getName());
        assertEquals("PeriodType[YearMonthDay]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.yearMonthDay());
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.yearMonthDay().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testYearWeekDayTime
    public void testYearWeekDayTime() throws Exception {
        PeriodType type = PeriodType.yearWeekDayTime();
        assertEquals(7, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(1));
        assertEquals(DurationFieldType.days(), type.getFieldType(2));
        assertEquals(DurationFieldType.hours(), type.getFieldType(3));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(4));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(5));
        assertEquals(DurationFieldType.millis(), type.getFieldType(6));
        assertEquals("YearWeekDayTime", type.getName());
        assertEquals("PeriodType[YearWeekDayTime]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.yearWeekDayTime());
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.yearWeekDayTime().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testYearWeekDay
    public void testYearWeekDay() throws Exception {
        PeriodType type = PeriodType.yearWeekDay();
        assertEquals(3, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(1));
        assertEquals(DurationFieldType.days(), type.getFieldType(2));
        assertEquals("YearWeekDay", type.getName());
        assertEquals("PeriodType[YearWeekDay]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.yearWeekDay());
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.yearWeekDay().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testYearDayTime
    public void testYearDayTime() throws Exception {
        PeriodType type = PeriodType.yearDayTime();
        assertEquals(6, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.days(), type.getFieldType(1));
        assertEquals(DurationFieldType.hours(), type.getFieldType(2));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(3));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(4));
        assertEquals(DurationFieldType.millis(), type.getFieldType(5));
        assertEquals("YearDayTime", type.getName());
        assertEquals("PeriodType[YearDayTime]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.yearDayTime());
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.yearDayTime().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testYearDay
    public void testYearDay() throws Exception {
        PeriodType type = PeriodType.yearDay();
        assertEquals(2, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.days(), type.getFieldType(1));
        assertEquals("YearDay", type.getName());
        assertEquals("PeriodType[YearDay]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.yearDay());
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.yearDay().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testDayTime
    public void testDayTime() throws Exception {
        PeriodType type = PeriodType.dayTime();
        assertEquals(5, type.size());
        assertEquals(DurationFieldType.days(), type.getFieldType(0));
        assertEquals(DurationFieldType.hours(), type.getFieldType(1));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(2));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(3));
        assertEquals(DurationFieldType.millis(), type.getFieldType(4));
        assertEquals("DayTime", type.getName());
        assertEquals("PeriodType[DayTime]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.dayTime());
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.dayTime().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testTime
    public void testTime() throws Exception {
        PeriodType type = PeriodType.time();
        assertEquals(4, type.size());
        assertEquals(DurationFieldType.hours(), type.getFieldType(0));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(1));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(2));
        assertEquals(DurationFieldType.millis(), type.getFieldType(3));
        assertEquals("Time", type.getName());
        assertEquals("PeriodType[Time]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.time());
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.time().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testYears
    public void testYears() throws Exception {
        PeriodType type = PeriodType.years();
        assertEquals(1, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals("Years", type.getName());
        assertEquals("PeriodType[Years]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.years());
        assertEquals(false, type.equals(PeriodType.standard()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.years().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.standard().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMonths
    public void testMonths() throws Exception {
        PeriodType type = PeriodType.months();
        assertEquals(1, type.size());
        assertEquals(DurationFieldType.months(), type.getFieldType(0));
        assertEquals("Months", type.getName());
        assertEquals("PeriodType[Months]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.months());
        assertEquals(false, type.equals(PeriodType.standard()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.months().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.standard().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testWeeks
    public void testWeeks() throws Exception {
        PeriodType type = PeriodType.weeks();
        assertEquals(1, type.size());
        assertEquals(DurationFieldType.weeks(), type.getFieldType(0));
        assertEquals("Weeks", type.getName());
        assertEquals("PeriodType[Weeks]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.weeks());
        assertEquals(false, type.equals(PeriodType.standard()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.weeks().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.standard().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testDays
    public void testDays() throws Exception {
        PeriodType type = PeriodType.days();
        assertEquals(1, type.size());
        assertEquals(DurationFieldType.days(), type.getFieldType(0));
        assertEquals("Days", type.getName());
        assertEquals("PeriodType[Days]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.days());
        assertEquals(false, type.equals(PeriodType.standard()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.days().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.standard().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testHours
    public void testHours() throws Exception {
        PeriodType type = PeriodType.hours();
        assertEquals(1, type.size());
        assertEquals(DurationFieldType.hours(), type.getFieldType(0));
        assertEquals("Hours", type.getName());
        assertEquals("PeriodType[Hours]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.hours());
        assertEquals(false, type.equals(PeriodType.standard()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.hours().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.standard().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMinutes
    public void testMinutes() throws Exception {
        PeriodType type = PeriodType.minutes();
        assertEquals(1, type.size());
        assertEquals(DurationFieldType.minutes(), type.getFieldType(0));
        assertEquals("Minutes", type.getName());
        assertEquals("PeriodType[Minutes]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.minutes());
        assertEquals(false, type.equals(PeriodType.standard()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.minutes().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.standard().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testSeconds
    public void testSeconds() throws Exception {
        PeriodType type = PeriodType.seconds();
        assertEquals(1, type.size());
        assertEquals(DurationFieldType.seconds(), type.getFieldType(0));
        assertEquals("Seconds", type.getName());
        assertEquals("PeriodType[Seconds]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.seconds());
        assertEquals(false, type.equals(PeriodType.standard()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.seconds().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.standard().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMillis
    public void testMillis() throws Exception {
        PeriodType type = PeriodType.millis();
        assertEquals(1, type.size());
        assertEquals(DurationFieldType.millis(), type.getFieldType(0));
        assertEquals("Millis", type.getName());
        assertEquals("PeriodType[Millis]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.millis());
        assertEquals(false, type.equals(PeriodType.standard()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.millis().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.standard().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testForFields1
    public void testForFields1() throws Exception {
        PeriodType type = PeriodType.forFields(new DurationFieldType[] {
            DurationFieldType.years(),
        });
        assertSame(PeriodType.years(), type);
        type = PeriodType.forFields(new DurationFieldType[] {
            DurationFieldType.months(),
        });
        assertSame(PeriodType.months(), type);
        type = PeriodType.forFields(new DurationFieldType[] {
                DurationFieldType.weeks(),
        });
        assertSame(PeriodType.weeks(), type);
        type = PeriodType.forFields(new DurationFieldType[] {
                DurationFieldType.days(),
        });
        assertSame(PeriodType.days(), type);
        type = PeriodType.forFields(new DurationFieldType[] {
                DurationFieldType.hours(),
        });
        assertSame(PeriodType.hours(), type);
        type = PeriodType.forFields(new DurationFieldType[] {
                DurationFieldType.minutes(),
        });
        assertSame(PeriodType.minutes(), type);
        type = PeriodType.forFields(new DurationFieldType[] {
                DurationFieldType.seconds(),
        });
        assertSame(PeriodType.seconds(), type);
        type = PeriodType.forFields(new DurationFieldType[] {
                DurationFieldType.millis(),
        });
        assertSame(PeriodType.millis(), type);
    }

// org.joda.time.TestPeriodType::testForFields2
    public void testForFields2() throws Exception {
        DurationFieldType[] types = new DurationFieldType[] {
            DurationFieldType.years(),
            DurationFieldType.hours(),
        };
        PeriodType type = PeriodType.forFields(types);
        assertEquals(2, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.hours(), type.getFieldType(1));
        assertEquals("StandardNoMonthsNoWeeksNoDaysNoMinutesNoSecondsNoMillis", type.getName());
        assertEquals("PeriodType[StandardNoMonthsNoWeeksNoDaysNoMinutesNoSecondsNoMillis]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.forFields(types));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.forFields(types).hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testForFields3
    public void testForFields3() throws Exception {
        DurationFieldType[] types = new DurationFieldType[] {
            DurationFieldType.months(),
            DurationFieldType.weeks(),
        };
        PeriodType type = PeriodType.forFields(types);
        assertEquals(2, type.size());
        assertEquals(DurationFieldType.months(), type.getFieldType(0));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(1));
        assertEquals("StandardNoYearsNoDaysNoHoursNoMinutesNoSecondsNoMillis", type.getName());
        assertEquals("PeriodType[StandardNoYearsNoDaysNoHoursNoMinutesNoSecondsNoMillis]", type.toString());
        assertEquals(true, type.equals(type));
        assertEquals(true, type == PeriodType.forFields(types));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.forFields(types).hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertSameAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testForFields4
    public void testForFields4() throws Exception {
        DurationFieldType[] types = new DurationFieldType[] {
            DurationFieldType.weeks(),
            DurationFieldType.days(),  
            DurationFieldType.months(),
        };
        DurationFieldType[] types2 = new DurationFieldType[] {
            DurationFieldType.months(),
            DurationFieldType.days(),
            DurationFieldType.weeks(),
        };
        PeriodType type = PeriodType.forFields(types);
        PeriodType type2 = PeriodType.forFields(types2);
        assertEquals(true, type == type2);
    }

// org.joda.time.TestPeriodType::testForFields5
    public void testForFields5() throws Exception {
        DurationFieldType[] types = new DurationFieldType[] {
            DurationFieldType.centuries(),
            DurationFieldType.months(),
        };
        try {
            PeriodType.forFields(types);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            PeriodType.forFields(types);  
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestPeriodType::testForFields6
    public void testForFields6() throws Exception {
        DurationFieldType[] types = null;
        try {
            PeriodType.forFields(types);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        
        types = new DurationFieldType[0];
        try {
            PeriodType.forFields(types);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        
        types = new DurationFieldType[] {
            null,
            DurationFieldType.months(),
        };
        try {
            PeriodType.forFields(types);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        
        types = new DurationFieldType[] {
            DurationFieldType.months(),
            null,
        };
        try {
            PeriodType.forFields(types);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestPeriodType::testForFields7
    public void testForFields7() throws Exception {
        DurationFieldType[] types = new DurationFieldType[] {
            DurationFieldType.weeks(),
            DurationFieldType.months(),
        };
        DurationFieldType[] types2 = new DurationFieldType[] {
            DurationFieldType.seconds(),
        };
        PeriodType type = PeriodType.forFields(types);
        PeriodType type2 = PeriodType.forFields(types2);
        assertEquals(false, type == type2);
        assertEquals(false, type.equals(type2));
        assertEquals(false, type.hashCode() == type2.hashCode());
    }

// org.joda.time.TestPeriodType::testMaskYears
    public void testMaskYears() throws Exception {
        PeriodType type = PeriodType.standard().withYearsRemoved();
        assertEquals(7, type.size());
        assertEquals(DurationFieldType.months(), type.getFieldType(0));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(1));
        assertEquals(DurationFieldType.days(), type.getFieldType(2));
        assertEquals(DurationFieldType.hours(), type.getFieldType(3));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(4));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(5));
        assertEquals(DurationFieldType.millis(), type.getFieldType(6));
        assertEquals(true, type.equals(type));
        assertEquals(true, type.equals(PeriodType.standard().withYearsRemoved()));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.standard().withYearsRemoved().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertEquals("StandardNoYears", type.getName());
        assertEquals("PeriodType[StandardNoYears]", type.toString());
        assertEqualsAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMaskMonths
    public void testMaskMonths() throws Exception {
        PeriodType type = PeriodType.standard().withMonthsRemoved();
        assertEquals(7, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(1));
        assertEquals(DurationFieldType.days(), type.getFieldType(2));
        assertEquals(DurationFieldType.hours(), type.getFieldType(3));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(4));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(5));
        assertEquals(DurationFieldType.millis(), type.getFieldType(6));
        assertEquals(true, type.equals(type));
        assertEquals(true, type.equals(PeriodType.standard().withMonthsRemoved()));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.standard().withMonthsRemoved().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertEquals("StandardNoMonths", type.getName());
        assertEquals("PeriodType[StandardNoMonths]", type.toString());
        assertEqualsAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMaskWeeks
    public void testMaskWeeks() throws Exception {
        PeriodType type = PeriodType.standard().withWeeksRemoved();
        assertEquals(7, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.months(), type.getFieldType(1));
        assertEquals(DurationFieldType.days(), type.getFieldType(2));
        assertEquals(DurationFieldType.hours(), type.getFieldType(3));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(4));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(5));
        assertEquals(DurationFieldType.millis(), type.getFieldType(6));
        assertEquals(true, type.equals(type));
        assertEquals(true, type.equals(PeriodType.standard().withWeeksRemoved()));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.standard().withWeeksRemoved().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertEquals("StandardNoWeeks", type.getName());
        assertEquals("PeriodType[StandardNoWeeks]", type.toString());
        assertEqualsAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMaskDays
    public void testMaskDays() throws Exception {
        PeriodType type = PeriodType.standard().withDaysRemoved();
        assertEquals(7, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.months(), type.getFieldType(1));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(2));
        assertEquals(DurationFieldType.hours(), type.getFieldType(3));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(4));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(5));
        assertEquals(DurationFieldType.millis(), type.getFieldType(6));
        assertEquals(true, type.equals(type));
        assertEquals(true, type.equals(PeriodType.standard().withDaysRemoved()));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.standard().withDaysRemoved().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertEquals("StandardNoDays", type.getName());
        assertEquals("PeriodType[StandardNoDays]", type.toString());
        assertEqualsAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMaskHours
    public void testMaskHours() throws Exception {
        PeriodType type = PeriodType.standard().withHoursRemoved();
        assertEquals(7, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.months(), type.getFieldType(1));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(2));
        assertEquals(DurationFieldType.days(), type.getFieldType(3));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(4));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(5));
        assertEquals(DurationFieldType.millis(), type.getFieldType(6));
        assertEquals(true, type.equals(type));
        assertEquals(true, type.equals(PeriodType.standard().withHoursRemoved()));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.standard().withHoursRemoved().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertEquals("StandardNoHours", type.getName());
        assertEquals("PeriodType[StandardNoHours]", type.toString());
        assertEqualsAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMaskMinutes
    public void testMaskMinutes() throws Exception {
        PeriodType type = PeriodType.standard().withMinutesRemoved();
        assertEquals(7, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.months(), type.getFieldType(1));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(2));
        assertEquals(DurationFieldType.days(), type.getFieldType(3));
        assertEquals(DurationFieldType.hours(), type.getFieldType(4));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(5));
        assertEquals(DurationFieldType.millis(), type.getFieldType(6));
        assertEquals(true, type.equals(type));
        assertEquals(true, type.equals(PeriodType.standard().withMinutesRemoved()));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.standard().withMinutesRemoved().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertEquals("StandardNoMinutes", type.getName());
        assertEquals("PeriodType[StandardNoMinutes]", type.toString());
        assertEqualsAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMaskSeconds
    public void testMaskSeconds() throws Exception {
        PeriodType type = PeriodType.standard().withSecondsRemoved();
        assertEquals(7, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.months(), type.getFieldType(1));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(2));
        assertEquals(DurationFieldType.days(), type.getFieldType(3));
        assertEquals(DurationFieldType.hours(), type.getFieldType(4));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(5));
        assertEquals(DurationFieldType.millis(), type.getFieldType(6));
        assertEquals(true, type.equals(type));
        assertEquals(true, type.equals(PeriodType.standard().withSecondsRemoved()));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.standard().withSecondsRemoved().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertEquals("StandardNoSeconds", type.getName());
        assertEquals("PeriodType[StandardNoSeconds]", type.toString());
        assertEqualsAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMaskMillis
    public void testMaskMillis() throws Exception {
        PeriodType type = PeriodType.standard().withMillisRemoved();
        assertEquals(7, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.months(), type.getFieldType(1));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(2));
        assertEquals(DurationFieldType.days(), type.getFieldType(3));
        assertEquals(DurationFieldType.hours(), type.getFieldType(4));
        assertEquals(DurationFieldType.minutes(), type.getFieldType(5));
        assertEquals(DurationFieldType.seconds(), type.getFieldType(6));
        assertEquals(true, type.equals(type));
        assertEquals(true, type.equals(PeriodType.standard().withMillisRemoved()));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.standard().withMillisRemoved().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertEquals("StandardNoMillis", type.getName());
        assertEquals("PeriodType[StandardNoMillis]", type.toString());
        assertEqualsAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMaskHoursMinutesSeconds
    public void testMaskHoursMinutesSeconds() throws Exception {
        PeriodType type = PeriodType.standard().withHoursRemoved().withMinutesRemoved().withSecondsRemoved();
        assertEquals(5, type.size());
        assertEquals(DurationFieldType.years(), type.getFieldType(0));
        assertEquals(DurationFieldType.months(), type.getFieldType(1));
        assertEquals(DurationFieldType.weeks(), type.getFieldType(2));
        assertEquals(DurationFieldType.days(), type.getFieldType(3));
        assertEquals(DurationFieldType.millis(), type.getFieldType(4));
        assertEquals(true, type.equals(type));
        assertEquals(true, type.equals(PeriodType.standard().withHoursRemoved().withMinutesRemoved().withSecondsRemoved()));
        assertEquals(false, type.equals(PeriodType.millis()));
        assertEquals(true, type.hashCode() == type.hashCode());
        assertEquals(true, type.hashCode() == PeriodType.standard().withHoursRemoved().withMinutesRemoved().withSecondsRemoved().hashCode());
        assertEquals(false, type.hashCode() == PeriodType.millis().hashCode());
        assertEquals("StandardNoHoursNoMinutesNoSeconds", type.getName());
        assertEquals("PeriodType[StandardNoHoursNoMinutesNoSeconds]", type.toString());
        assertEqualsAfterSerialization(type);
    }

// org.joda.time.TestPeriodType::testMaskTwice1
    public void testMaskTwice1() throws Exception {
        PeriodType type = PeriodType.standard().withYearsRemoved();
        PeriodType type2 = type.withYearsRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.standard().withMonthsRemoved();
        type2 = type.withMonthsRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.standard().withWeeksRemoved();
        type2 = type.withWeeksRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.standard().withDaysRemoved();
        type2 = type.withDaysRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.standard().withHoursRemoved();
        type2 = type.withHoursRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.standard().withMinutesRemoved();
        type2 = type.withMinutesRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.standard().withSecondsRemoved();
        type2 = type.withSecondsRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.standard().withMillisRemoved();
        type2 = type.withMillisRemoved();
        assertEquals(true, type == type2);
    }

// org.joda.time.TestPeriodType::testMaskTwice2
    public void testMaskTwice2() throws Exception {
        PeriodType type = PeriodType.dayTime();
        PeriodType type2 = type.withYearsRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.dayTime();
        type2 = type.withMonthsRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.dayTime();
        type2 = type.withWeeksRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.millis();
        type2 = type.withDaysRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.millis();
        type2 = type.withHoursRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.millis();
        type2 = type.withMinutesRemoved();
        assertEquals(true, type == type2);
        
        type = PeriodType.millis();
        type2 = type.withSecondsRemoved();
        assertEquals(true, type == type2);
    }

// org.joda.time.TestPeriodType::testEquals
    public void testEquals() throws Exception {
        PeriodType type = PeriodType.dayTime().withMillisRemoved();
        assertEquals(true, type.equals(type));
        assertEquals(true, type.equals(PeriodType.dayTime().withMillisRemoved()));
        assertEquals(false, type.equals(null));
        assertEquals(false, type.equals(""));
    }

// org.joda.time.TestPeriodType::testHashCode
    public void testHashCode() throws Exception {
        PeriodType type = PeriodType.dayTime().withMillisRemoved();
        assertEquals(type.hashCode(), type.hashCode());
    }

// org.joda.time.TestPeriodType::testIsSupported
    public void testIsSupported() throws Exception {
        PeriodType type = PeriodType.dayTime().withMillisRemoved();
        assertEquals(false, type.isSupported(DurationFieldType.years()));
        assertEquals(false, type.isSupported(DurationFieldType.months()));
        assertEquals(false, type.isSupported(DurationFieldType.weeks()));
        assertEquals(true, type.isSupported(DurationFieldType.days()));
        assertEquals(true, type.isSupported(DurationFieldType.hours()));
        assertEquals(true, type.isSupported(DurationFieldType.minutes()));
        assertEquals(true, type.isSupported(DurationFieldType.seconds()));
        assertEquals(false, type.isSupported(DurationFieldType.millis()));
    }

// org.joda.time.TestPeriodType::testIndexOf
    public void testIndexOf() throws Exception {
        PeriodType type = PeriodType.dayTime().withMillisRemoved();
        assertEquals(-1, type.indexOf(DurationFieldType.years()));
        assertEquals(-1, type.indexOf(DurationFieldType.months()));
        assertEquals(-1, type.indexOf(DurationFieldType.weeks()));
        assertEquals(0, type.indexOf(DurationFieldType.days()));
        assertEquals(1, type.indexOf(DurationFieldType.hours()));
        assertEquals(2, type.indexOf(DurationFieldType.minutes()));
        assertEquals(3, type.indexOf(DurationFieldType.seconds()));
        assertEquals(-1, type.indexOf(DurationFieldType.millis()));
    }

// org.joda.time.TestPeriod_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestPeriod_Basics::testGetPeriodType
    public void testGetPeriodType() {
        Period test = new Period(0L);
        assertEquals(PeriodType.standard(), test.getPeriodType());
    }

// org.joda.time.TestPeriod_Basics::testGetMethods
    public void testGetMethods() {
        Period test = new Period(0L);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestPeriod_Basics::testValueIndexMethods
    public void testValueIndexMethods() {
        Period test = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
        assertEquals(6, test.size());
        assertEquals(1, test.getValue(0));
        assertEquals(4, test.getValue(1));
        assertEquals(5, test.getValue(2));
        assertEquals(6, test.getValue(3));
        assertEquals(7, test.getValue(4));
        assertEquals(8, test.getValue(5));
        assertEquals(true, Arrays.equals(new int[] {1, 4, 5, 6, 7, 8}, test.getValues()));
    }

// org.joda.time.TestPeriod_Basics::testTypeIndexMethods
    public void testTypeIndexMethods() {
        Period test = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
        assertEquals(6, test.size());
        assertEquals(DurationFieldType.years(), test.getFieldType(0));
        assertEquals(DurationFieldType.days(), test.getFieldType(1));
        assertEquals(DurationFieldType.hours(), test.getFieldType(2));
        assertEquals(DurationFieldType.minutes(), test.getFieldType(3));
        assertEquals(DurationFieldType.seconds(), test.getFieldType(4));
        assertEquals(DurationFieldType.millis(), test.getFieldType(5));
        assertEquals(true, Arrays.equals(new DurationFieldType[] {
            DurationFieldType.years(), DurationFieldType.days(), DurationFieldType.hours(),
            DurationFieldType.minutes(), DurationFieldType.seconds(), DurationFieldType.millis()},
            test.getFieldTypes()));
    }

// org.joda.time.TestPeriod_Basics::testIsSupported
    public void testIsSupported() {
        Period test = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
        assertEquals(true, test.isSupported(DurationFieldType.years()));
        assertEquals(false, test.isSupported(DurationFieldType.months()));
        assertEquals(false, test.isSupported(DurationFieldType.weeks()));
        assertEquals(true, test.isSupported(DurationFieldType.days()));
        assertEquals(true, test.isSupported(DurationFieldType.hours()));
        assertEquals(true, test.isSupported(DurationFieldType.minutes()));
        assertEquals(true, test.isSupported(DurationFieldType.seconds()));
        assertEquals(true, test.isSupported(DurationFieldType.millis()));
    }

// org.joda.time.TestPeriod_Basics::testIndexOf
    public void testIndexOf() {
        Period test = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
        assertEquals(0, test.indexOf(DurationFieldType.years()));
        assertEquals(-1, test.indexOf(DurationFieldType.months()));
        assertEquals(-1, test.indexOf(DurationFieldType.weeks()));
        assertEquals(1, test.indexOf(DurationFieldType.days()));
        assertEquals(2, test.indexOf(DurationFieldType.hours()));
        assertEquals(3, test.indexOf(DurationFieldType.minutes()));
        assertEquals(4, test.indexOf(DurationFieldType.seconds()));
        assertEquals(5, test.indexOf(DurationFieldType.millis()));
    }

// org.joda.time.TestPeriod_Basics::testGet
    public void testGet() {
        Period test = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
        assertEquals(1, test.get(DurationFieldType.years()));
        assertEquals(0, test.get(DurationFieldType.months()));
        assertEquals(0, test.get(DurationFieldType.weeks()));
        assertEquals(4, test.get(DurationFieldType.days()));
        assertEquals(5, test.get(DurationFieldType.hours()));
        assertEquals(6, test.get(DurationFieldType.minutes()));
        assertEquals(7, test.get(DurationFieldType.seconds()));
        assertEquals(8, test.get(DurationFieldType.millis()));
    }

// org.joda.time.TestPeriod_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        Period test1 = new Period(123L);
        Period test2 = new Period(123L);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        Period test3 = new Period(321L);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockPeriod(123L)));
        assertEquals(false, test1.equals(new Period(123L, PeriodType.dayTime())));
    }

// org.joda.time.TestPeriod_Basics::testSerialization
    public void testSerialization() throws Exception {
        Period test = new Period(123L);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Period result = (Period) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestPeriod_Basics::testToString
    public void testToString() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", test.toString());
        
        test = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("PT0S", test.toString());
        
        test = new Period(12345L);
        assertEquals("PT12.345S", test.toString());
    }

// org.joda.time.TestPeriod_Basics::testToString_PeriodFormatter
    public void testToString_PeriodFormatter() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("1 year, 2 months, 3 weeks, 4 days, 5 hours, 6 minutes, 7 seconds and 8 milliseconds", test.toString(PeriodFormat.getDefault()));
        
        test = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0 milliseconds", test.toString(PeriodFormat.getDefault()));
    }

// org.joda.time.TestPeriod_Basics::testToString_nullPeriodFormatter
    public void testToString_nullPeriodFormatter() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", test.toString((PeriodFormatter) null));
    }

// org.joda.time.TestPeriod_Basics::testToPeriod
    public void testToPeriod() {
        Period test = new Period(123L);
        Period result = test.toPeriod();
        assertSame(test, result);
    }

// org.joda.time.TestPeriod_Basics::testToMutablePeriod
    public void testToMutablePeriod() {
        Period test = new Period(123L);
        MutablePeriod result = test.toMutablePeriod();
        assertEquals(test, result);
    }

// org.joda.time.TestPeriod_Basics::testToDurationFrom
    public void testToDurationFrom() {
        Period test = new Period(123L);
        assertEquals(new Duration(123L), test.toDurationFrom(new Instant(0L)));
    }

// org.joda.time.TestPeriod_Basics::testToDurationTo
    public void testToDurationTo() {
        Period test = new Period(123L);
        assertEquals(new Duration(123L), test.toDurationTo(new Instant(123L)));
    }

// org.joda.time.TestPeriod_Basics::testWithPeriodType1
    public void testWithPeriodType1() {
        Period test = new Period(123L);
        Period result = test.withPeriodType(PeriodType.standard());
        assertSame(test, result);
    }

// org.joda.time.TestPeriod_Basics::testWithPeriodType2
    public void testWithPeriodType2() {
        Period test = new Period(3123L);
        Period result = test.withPeriodType(PeriodType.dayTime());
        assertEquals(3, result.getSeconds());
        assertEquals(123, result.getMillis());
        assertEquals(PeriodType.dayTime(), result.getPeriodType());
    }

// org.joda.time.TestPeriod_Basics::testWithPeriodType3
    public void testWithPeriodType3() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        try {
            test.withPeriodType(PeriodType.dayTime());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testWithPeriodType4
    public void testWithPeriodType4() {
        Period test = new Period(3123L);
        Period result = test.withPeriodType(null);
        assertEquals(3, result.getSeconds());
        assertEquals(123, result.getMillis());
        assertEquals(PeriodType.standard(), result.getPeriodType());
    }

// org.joda.time.TestPeriod_Basics::testWithPeriodType5
    public void testWithPeriodType5() {
        Period test = new Period(1, 2, 0, 4, 5, 6, 7, 8, PeriodType.standard());
        Period result = test.withPeriodType(PeriodType.yearMonthDayTime());
        assertEquals(PeriodType.yearMonthDayTime(), result.getPeriodType());
        assertEquals(1, result.getYears());
        assertEquals(2, result.getMonths());
        assertEquals(0, result.getWeeks());
        assertEquals(4, result.getDays());
        assertEquals(5, result.getHours());
        assertEquals(6, result.getMinutes());
        assertEquals(7, result.getSeconds());
        assertEquals(8, result.getMillis());
    }

// org.joda.time.TestPeriod_Basics::testWithFields1
    public void testWithFields1() {
        Period test1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period test2 = new Period(0, 0, 0, 0, 0, 0, 0, 9, PeriodType.millis());
        Period result = test1.withFields(test2);
        
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), test1);
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 9, PeriodType.millis()), test2);
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 9), result);
    }

// org.joda.time.TestPeriod_Basics::testWithFields2
    public void testWithFields2() {
        Period test1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period test2 = null;
        Period result = test1.withFields(test2);
        
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), test1);
        assertSame(test1, result);
    }

// org.joda.time.TestPeriod_Basics::testWithFields3
    public void testWithFields3() {
        Period test1 = new Period(0, 0, 0, 0, 0, 0, 0, 9, PeriodType.millis());
        Period test2 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test1.withFields(test2);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 9, PeriodType.millis()), test1);
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), test2);
    }

// org.joda.time.TestPeriod_Basics::testWithField1
    public void testWithField1() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = test.withField(DurationFieldType.years(), 6);
        
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), test);
        assertEquals(new Period(6, 2, 3, 4, 5, 6, 7, 8), result);
    }

// org.joda.time.TestPeriod_Basics::testWithField2
    public void testWithField2() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testWithField3
    public void testWithField3() {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.time());
        try {
            test.withField(DurationFieldType.years(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testWithField4
    public void testWithField4() {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.time());
        Period result = test.withField(DurationFieldType.years(), 0);
        assertEquals(test, result);
    }

// org.joda.time.TestPeriod_Basics::testWithFieldAdded1
    public void testWithFieldAdded1() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new Period(1, 2, 3, 4, 5, 6, 7, 8), test);
        assertEquals(new Period(7, 2, 3, 4, 5, 6, 7, 8), result);
    }

// org.joda.time.TestPeriod_Basics::testWithFieldAdded2
    public void testWithFieldAdded2() {
        Period test = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testWithFieldAdded3
    public void testWithFieldAdded3() {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.time());
        try {
            test.withFieldAdded(DurationFieldType.years(), 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testWithFieldAdded4
    public void testWithFieldAdded4() {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.time());
        Period result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertEquals(test, result);
    }

// org.joda.time.TestPeriod_Basics::testPeriodStatics
    public void testPeriodStatics() {
        Period test;
        test = Period.years(1);
        assertEquals(test, new Period(1, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.months(1);
        assertEquals(test, new Period(0, 1, 0, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.weeks(1);
        assertEquals(test, new Period(0, 0, 1, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.days(1);
        assertEquals(test, new Period(0, 0, 0, 1, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.hours(1);
        assertEquals(test, new Period(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard()));
        test = Period.minutes(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 1, 0, 0, PeriodType.standard()));
        test = Period.seconds(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 0, 1, 0, PeriodType.standard()));
        test = Period.millis(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 0, 0, 1, PeriodType.standard()));
    }

// org.joda.time.TestPeriod_Basics::testWith
    public void testWith() {
        Period test;
        test = Period.years(5).withYears(1);
        assertEquals(test, new Period(1, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.months(5).withMonths(1);
        assertEquals(test, new Period(0, 1, 0, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.weeks(5).withWeeks(1);
        assertEquals(test, new Period(0, 0, 1, 0, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.days(5).withDays(1);
        assertEquals(test, new Period(0, 0, 0, 1, 0, 0, 0, 0, PeriodType.standard()));
        test = Period.hours(5).withHours(1);
        assertEquals(test, new Period(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard()));
        test = Period.minutes(5).withMinutes(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 1, 0, 0, PeriodType.standard()));
        test = Period.seconds(5).withSeconds(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 0, 1, 0, PeriodType.standard()));
        test = Period.millis(5).withMillis(1);
        assertEquals(test, new Period(0, 0, 0, 0, 0, 0, 0, 1, PeriodType.standard()));
        
        test = new Period(0L, PeriodType.millis());
        try {
            test.withYears(1);
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testPlus
    public void testPlus() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period baseDaysOnly = new Period(0, 0, 0, 10, 0, 0, 0, 0, PeriodType.days());
        
        Period test = base.plus((ReadablePeriod) null);
        assertSame(base, test);
        
        test = base.plus(Period.years(10));
        assertEquals(11, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = base.plus(Years.years(10));
        assertEquals(11, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = base.plus(Period.days(10));
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = baseDaysOnly.plus(Period.years(0));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(10, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
        
        test = baseDaysOnly.plus(baseDaysOnly);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(20, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
        
        try {
            baseDaysOnly.plus(Period.years(1));
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        try {
            Period.days(Integer.MAX_VALUE).plus(Period.days(1));
            fail();
        } catch (ArithmeticException ex) {}
        
        try {
            Period.days(Integer.MIN_VALUE).plus(Period.days(-1));
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testMinus
    public void testMinus() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period baseDaysOnly = new Period(0, 0, 0, 10, 0, 0, 0, 0, PeriodType.days());
        
        Period test = base.minus((ReadablePeriod) null);
        assertSame(base, test);
        
        test = base.minus(Period.years(10));
        assertEquals(-9, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = base.minus(Years.years(10));
        assertEquals(-9, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = base.minus(Period.days(10));
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(-6, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
        
        test = baseDaysOnly.minus(Period.years(0));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(10, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
        
        test = baseDaysOnly.minus(baseDaysOnly);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
        
        try {
            baseDaysOnly.minus(Period.years(1));
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        try {
            Period.days(Integer.MAX_VALUE).minus(Period.days(-1));
            fail();
        } catch (ArithmeticException ex) {}
        
        try {
            Period.days(Integer.MIN_VALUE).minus(Period.days(1));
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testPlusFields
    public void testPlusFields() {
        Period test;
        test = Period.years(1).plusYears(1);
        assertEquals(new Period(2, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.months(1).plusMonths(1);
        assertEquals(new Period(0, 2, 0, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.weeks(1).plusWeeks(1);
        assertEquals(new Period(0, 0, 2, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.days(1).plusDays(1);
        assertEquals(new Period(0, 0, 0, 2, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.hours(1).plusHours(1);
        assertEquals(new Period(0, 0, 0, 0, 2, 0, 0, 0, PeriodType.standard()), test);
        test = Period.minutes(1).plusMinutes(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 2, 0, 0, PeriodType.standard()), test);
        test = Period.seconds(1).plusSeconds(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 2, 0, PeriodType.standard()), test);
        test = Period.millis(1).plusMillis(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 2, PeriodType.standard()), test);
        
        test = new Period(0L, PeriodType.millis());
        try {
            test.plusYears(1);
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testPlusFieldsZero
    public void testPlusFieldsZero() {
        Period test, result;
        test = Period.years(1);
        result = test.plusYears(0);
        assertSame(test, result);
        test = Period.months(1);
        result = test.plusMonths(0);
        assertSame(test, result);
        test = Period.weeks(1);
        result = test.plusWeeks(0);
        assertSame(test, result);
        test = Period.days(1);
        result = test.plusDays(0);
        assertSame(test, result);
        test = Period.hours(1);
        result = test.plusHours(0);
        assertSame(test, result);
        test = Period.minutes(1);
        result = test.plusMinutes(0);
        assertSame(test, result);
        test = Period.seconds(1);
        result = test.plusSeconds(0);
        assertSame(test, result);
        test = Period.millis(1);
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestPeriod_Basics::testMinusFields
    public void testMinusFields() {
        Period test;
        test = Period.years(3).minusYears(1);
        assertEquals(new Period(2, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.months(3).minusMonths(1);
        assertEquals(new Period(0, 2, 0, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.weeks(3).minusWeeks(1);
        assertEquals(new Period(0, 0, 2, 0, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.days(3).minusDays(1);
        assertEquals(new Period(0, 0, 0, 2, 0, 0, 0, 0, PeriodType.standard()), test);
        test = Period.hours(3).minusHours(1);
        assertEquals(new Period(0, 0, 0, 0, 2, 0, 0, 0, PeriodType.standard()), test);
        test = Period.minutes(3).minusMinutes(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 2, 0, 0, PeriodType.standard()), test);
        test = Period.seconds(3).minusSeconds(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 2, 0, PeriodType.standard()), test);
        test = Period.millis(3).minusMillis(1);
        assertEquals(new Period(0, 0, 0, 0, 0, 0, 0, 2, PeriodType.standard()), test);
        
        test = new Period(0L, PeriodType.millis());
        try {
            test.minusYears(1);
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testMultipliedBy
    public void testMultipliedBy() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        
        Period test = base.multipliedBy(1);
        assertSame(base, test);
        
        test = base.multipliedBy(0);
        assertEquals(Period.ZERO, test);
        
        test = base.multipliedBy(2);
        assertEquals(2, test.getYears());
        assertEquals(4, test.getMonths());
        assertEquals(6, test.getWeeks());
        assertEquals(8, test.getDays());
        assertEquals(10, test.getHours());
        assertEquals(12, test.getMinutes());
        assertEquals(14, test.getSeconds());
        assertEquals(16, test.getMillis());
        
        test = base.multipliedBy(3);
        assertEquals(3, test.getYears());
        assertEquals(6, test.getMonths());
        assertEquals(9, test.getWeeks());
        assertEquals(12, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(18, test.getMinutes());
        assertEquals(21, test.getSeconds());
        assertEquals(24, test.getMillis());
        
        test = base.multipliedBy(-4);
        assertEquals(-4, test.getYears());
        assertEquals(-8, test.getMonths());
        assertEquals(-12, test.getWeeks());
        assertEquals(-16, test.getDays());
        assertEquals(-20, test.getHours());
        assertEquals(-24, test.getMinutes());
        assertEquals(-28, test.getSeconds());
        assertEquals(-32, test.getMillis());
        
        try {
            Period.days(Integer.MAX_VALUE).multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {}
        
        try {
            Period.days(Integer.MIN_VALUE).multipliedBy(2);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testNegated
    public void testNegated() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        
        Period test = Period.ZERO.negated();
        assertEquals(Period.ZERO, test);
        
        test = base.negated();
        assertEquals(-1, test.getYears());
        assertEquals(-2, test.getMonths());
        assertEquals(-3, test.getWeeks());
        assertEquals(-4, test.getDays());
        assertEquals(-5, test.getHours());
        assertEquals(-6, test.getMinutes());
        assertEquals(-7, test.getSeconds());
        assertEquals(-8, test.getMillis());
        
        test = Period.days(Integer.MAX_VALUE).negated();
        assertEquals(-Integer.MAX_VALUE, test.getDays());
        
        try {
            Period.days(Integer.MIN_VALUE).negated();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardWeeks
    public void testToStandardWeeks() {
        Period test = new Period(0, 0, 3, 4, 5, 6, 7, 8);
        assertEquals(3, test.toStandardWeeks().getWeeks());
        
        test = new Period(0, 0, 3, 7, 0, 0, 0, 0);
        assertEquals(4, test.toStandardWeeks().getWeeks());
        
        test = new Period(0, 0, 0, 6, 23, 59, 59, 1000);
        assertEquals(1, test.toStandardWeeks().getWeeks());
        
        test = new Period(0, 0, Integer.MAX_VALUE, 0, 0, 0, 0, 0);
        assertEquals(Integer.MAX_VALUE, test.toStandardWeeks().getWeeks());
        
        test = new Period(0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        long intMax = Integer.MAX_VALUE;
        BigInteger expected = BigInteger.valueOf(intMax);
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_SECOND));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_MINUTE));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_HOUR));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_DAY));
        expected = expected.divide(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_WEEK));
        assertTrue(expected.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0);
        assertEquals(expected.longValue(), test.toStandardWeeks().getWeeks());
        
        test = new Period(0, 0, Integer.MAX_VALUE, 7, 0, 0, 0, 0);
        try {
            test.toStandardWeeks();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardWeeks_years
    public void testToStandardWeeks_years() {
        Period test = Period.years(1);
        try {
            test.toStandardWeeks();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardWeeks();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardWeeks().getWeeks());
    }

// org.joda.time.TestPeriod_Basics::testToStandardWeeks_months
    public void testToStandardWeeks_months() {
        Period test = Period.months(1);
        try {
            test.toStandardWeeks();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardWeeks();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardWeeks().getWeeks());
    }

// org.joda.time.TestPeriod_Basics::testToStandardDays
    public void testToStandardDays() {
        Period test = new Period(0, 0, 0, 4, 5, 6, 7, 8);
        assertEquals(4, test.toStandardDays().getDays());
        
        test = new Period(0, 0, 1, 4, 0, 0, 0, 0);
        assertEquals(11, test.toStandardDays().getDays());
        
        test = new Period(0, 0, 0, 0, 23, 59, 59, 1000);
        assertEquals(1, test.toStandardDays().getDays());
        
        test = new Period(0, 0, 0, Integer.MAX_VALUE, 0, 0, 0, 0);
        assertEquals(Integer.MAX_VALUE, test.toStandardDays().getDays());
        
        test = new Period(0, 0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        long intMax = Integer.MAX_VALUE;
        BigInteger expected = BigInteger.valueOf(intMax);
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_SECOND));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_MINUTE));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_HOUR));
        expected = expected.divide(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_DAY));
        assertTrue(expected.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0);
        assertEquals(expected.longValue(), test.toStandardDays().getDays());
        
        test = new Period(0, 0, 0, Integer.MAX_VALUE, 24, 0, 0, 0);
        try {
            test.toStandardDays();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardDays_years
    public void testToStandardDays_years() {
        Period test = Period.years(1);
        try {
            test.toStandardDays();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardDays();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardDays().getDays());
    }

// org.joda.time.TestPeriod_Basics::testToStandardDays_months
    public void testToStandardDays_months() {
        Period test = Period.months(1);
        try {
            test.toStandardDays();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardDays();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardDays().getDays());
    }

// org.joda.time.TestPeriod_Basics::testToStandardHours
    public void testToStandardHours() {
        Period test = new Period(0, 0, 0, 0, 5, 6, 7, 8);
        assertEquals(5, test.toStandardHours().getHours());
        
        test = new Period(0, 0, 0, 1, 5, 0, 0, 0);
        assertEquals(29, test.toStandardHours().getHours());
        
        test = new Period(0, 0, 0, 0, 0, 59, 59, 1000);
        assertEquals(1, test.toStandardHours().getHours());
        
        test = new Period(0, 0, 0, 0, Integer.MAX_VALUE, 0, 0, 0);
        assertEquals(Integer.MAX_VALUE, test.toStandardHours().getHours());
        
        test = new Period(0, 0, 0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        long intMax = Integer.MAX_VALUE;
        BigInteger expected = BigInteger.valueOf(intMax);
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_SECOND));
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_MINUTE));
        expected = expected.divide(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_HOUR));
        assertTrue(expected.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0);
        assertEquals(expected.longValue(), test.toStandardHours().getHours());
        
        test = new Period(0, 0, 0, 0, Integer.MAX_VALUE, 60, 0, 0);
        try {
            test.toStandardHours();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardHours_years
    public void testToStandardHours_years() {
        Period test = Period.years(1);
        try {
            test.toStandardHours();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardHours();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardHours().getHours());
    }

// org.joda.time.TestPeriod_Basics::testToStandardHours_months
    public void testToStandardHours_months() {
        Period test = Period.months(1);
        try {
            test.toStandardHours();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardHours();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardHours().getHours());
    }

// org.joda.time.TestPeriod_Basics::testToStandardMinutes
    public void testToStandardMinutes() {
        Period test = new Period(0, 0, 0, 0, 0, 6, 7, 8);
        assertEquals(6, test.toStandardMinutes().getMinutes());
        
        test = new Period(0, 0, 0, 0, 1, 6, 0, 0);
        assertEquals(66, test.toStandardMinutes().getMinutes());
        
        test = new Period(0, 0, 0, 0, 0, 0, 59, 1000);
        assertEquals(1, test.toStandardMinutes().getMinutes());
        
        test = new Period(0, 0, 0, 0, 0, Integer.MAX_VALUE, 0, 0);
        assertEquals(Integer.MAX_VALUE, test.toStandardMinutes().getMinutes());
        
        test = new Period(0, 0, 0, 0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        long intMax = Integer.MAX_VALUE;
        BigInteger expected = BigInteger.valueOf(intMax);
        expected = expected.add(BigInteger.valueOf(intMax * DateTimeConstants.MILLIS_PER_SECOND));
        expected = expected.divide(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_MINUTE));
        assertTrue(expected.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0);
        assertEquals(expected.longValue(), test.toStandardMinutes().getMinutes());
        
        test = new Period(0, 0, 0, 0, 0, Integer.MAX_VALUE, 60, 0);
        try {
            test.toStandardMinutes();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardMinutes_years
    public void testToStandardMinutes_years() {
        Period test = Period.years(1);
        try {
            test.toStandardMinutes();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardMinutes();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardMinutes().getMinutes());
    }

// org.joda.time.TestPeriod_Basics::testToStandardMinutes_months
    public void testToStandardMinutes_months() {
        Period test = Period.months(1);
        try {
            test.toStandardMinutes();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardMinutes();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardMinutes().getMinutes());
    }

// org.joda.time.TestPeriod_Basics::testToStandardSeconds
    public void testToStandardSeconds() {
        Period test = new Period(0, 0, 0, 0, 0, 0, 7, 8);
        assertEquals(7, test.toStandardSeconds().getSeconds());
        
        test = new Period(0, 0, 0, 0, 0, 1, 3, 0);
        assertEquals(63, test.toStandardSeconds().getSeconds());
        
        test = new Period(0, 0, 0, 0, 0, 0, 0, 1000);
        assertEquals(1, test.toStandardSeconds().getSeconds());
        
        test = new Period(0, 0, 0, 0, 0, 0, Integer.MAX_VALUE, 0);
        assertEquals(Integer.MAX_VALUE, test.toStandardSeconds().getSeconds());
        
        test = new Period(0, 0, 0, 0, 0, 0, 20, Integer.MAX_VALUE);
        long expected = 20;
        expected += ((long) Integer.MAX_VALUE) / DateTimeConstants.MILLIS_PER_SECOND;
        assertEquals(expected, test.toStandardSeconds().getSeconds());
        
        test = new Period(0, 0, 0, 0, 0, 0, Integer.MAX_VALUE, 1000);
        try {
            test.toStandardSeconds();
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.TestPeriod_Basics::testToStandardSeconds_years
    public void testToStandardSeconds_years() {
        Period test = Period.years(1);
        try {
            test.toStandardSeconds();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardSeconds();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardSeconds().getSeconds());
    }

// org.joda.time.TestPeriod_Basics::testToStandardSeconds_months
    public void testToStandardSeconds_months() {
        Period test = Period.months(1);
        try {
            test.toStandardSeconds();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardSeconds();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardSeconds().getSeconds());
    }

// org.joda.time.TestPeriod_Basics::testToStandardDuration
    public void testToStandardDuration() {
        Period test = new Period(0, 0, 0, 0, 0, 0, 0, 8);
        assertEquals(8, test.toStandardDuration().getMillis());
        
        test = new Period(0, 0, 0, 0, 0, 0, 1, 20);
        assertEquals(1020, test.toStandardDuration().getMillis());
        
        test = new Period(0, 0, 0, 0, 0, 0, 0, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, test.toStandardDuration().getMillis());
        
        test = new Period(0, 0, 0, 0, 0, 10, 20, Integer.MAX_VALUE);
        long expected = Integer.MAX_VALUE;
        expected += 10L * ((long) DateTimeConstants.MILLIS_PER_MINUTE);
        expected += 20L * ((long) DateTimeConstants.MILLIS_PER_SECOND);
        assertEquals(expected, test.toStandardDuration().getMillis());
        
        
        BigInteger intMax = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger exp = intMax;
        exp = exp.add(intMax.multiply(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_SECOND)));
        exp = exp.add(intMax.multiply(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_MINUTE)));
        exp = exp.add(intMax.multiply(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_HOUR)));
        exp = exp.add(intMax.multiply(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_DAY)));
        exp = exp.add(intMax.multiply(BigInteger.valueOf(DateTimeConstants.MILLIS_PER_WEEK)));
        assertTrue(exp.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0);

    }

// org.joda.time.TestPeriod_Basics::testToStandardDuration_years
    public void testToStandardDuration_years() {
        Period test = Period.years(1);
        try {
            test.toStandardDuration();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(-1);
        try {
            test.toStandardDuration();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.years(0);
        assertEquals(0, test.toStandardDuration().getMillis());
    }

// org.joda.time.TestPeriod_Basics::testToStandardDuration_months
    public void testToStandardDuration_months() {
        Period test = Period.months(1);
        try {
            test.toStandardDuration();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(-1);
        try {
            test.toStandardDuration();
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        test = Period.months(0);
        assertEquals(0, test.toStandardDuration().getMillis());
    }

// org.joda.time.TestPeriod_Basics::testNormalizedStandard_yearMonth1
    public void testNormalizedStandard_yearMonth1() {
        Period test = new Period(1, 15, 0, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(new Period(1, 15, 0, 0, 0, 0, 0, 0), test);
        assertEquals(new Period(2, 3, 0, 0, 0, 0, 0, 0), result);
    }

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

// org.joda.time.TestPeriod_Constructors::testConstructor_trickyDifferences_RI_RI_toFeb_standardYear
    public void testConstructor_trickyDifferences_RI_RI_toFeb_standardYear() throws Throwable {
        DateTime dt1 = new DateTime(2011, 1, 1, 0, 0);
        DateTime dt2 = new DateTime(2011, 2, 28, 0, 0);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(new Period(0, 1, 3, 6, 0, 0, 0, 0), test);
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_trickyDifferences_RI_RI_toFeb_leapYear
    public void testConstructor_trickyDifferences_RI_RI_toFeb_leapYear() throws Throwable {
        DateTime dt1 = new DateTime(2012, 1, 1, 0, 0);
        DateTime dt2 = new DateTime(2012, 2, 29, 0, 0);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(new Period(0, 1, 4, 0, 0, 0, 0, 0), test);
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_trickyDifferences_RI_RI_toFeb_exactMonths
    public void testConstructor_trickyDifferences_RI_RI_toFeb_exactMonths() throws Throwable {
        DateTime dt1 = new DateTime(2004, 12, 28, 0, 0);
        DateTime dt2 = new DateTime(2005, 2, 28, 0, 0);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(new Period(0, 2, 0, 0, 0, 0, 0, 0), test);
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_trickyDifferences_RI_RI_toFeb_endOfMonth1
    public void testConstructor_trickyDifferences_RI_RI_toFeb_endOfMonth1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 12, 29, 0, 0);
        DateTime dt2 = new DateTime(2005, 2, 28, 0, 0);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(new Period(0, 2, 0, 0, 0, 0, 0, 0), test);
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_trickyDifferences_RI_RI_toFeb_endOfMonth2
    public void testConstructor_trickyDifferences_RI_RI_toFeb_endOfMonth2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 12, 30, 0, 0);
        DateTime dt2 = new DateTime(2005, 2, 28, 0, 0);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(new Period(0, 2, 0, 0, 0, 0, 0, 0), test);
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_trickyDifferences_RI_RI_toFeb_endOfMonth3
    public void testConstructor_trickyDifferences_RI_RI_toFeb_endOfMonth3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 12, 31, 0, 0);
        DateTime dt2 = new DateTime(2005, 2, 28, 0, 0);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(new Period(0, 2, 0, 0, 0, 0, 0, 0), test);
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_trickyDifferences_RI_RI_toMar_endOfMonth1
    public void testConstructor_trickyDifferences_RI_RI_toMar_endOfMonth1() throws Throwable {
        DateTime dt1 = new DateTime(2013, 1, 31, 0, 0);
        DateTime dt2 = new DateTime(2013, 3, 30, 0, 0);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(new Period(0, 1, 4, 2, 0, 0, 0, 0), test);
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_trickyDifferences_RI_RI_toMar_endOfMonth2
    public void testConstructor_trickyDifferences_RI_RI_toMar_endOfMonth2() throws Throwable {
        DateTime dt1 = new DateTime(2013, 1, 31, 0, 0);
        DateTime dt2 = new DateTime(2013, 3, 31, 0, 0);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(new Period(0, 2, 0, 0, 0, 0, 0, 0), test);
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_trickyDifferences_LD_LD_toFeb_standardYear
    public void testConstructor_trickyDifferences_LD_LD_toFeb_standardYear() throws Throwable {
        LocalDate dt1 = new LocalDate(2011, 1, 1);
        LocalDate dt2 = new LocalDate(2011, 2, 28);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(new Period(0, 1, 3, 6, 0, 0, 0, 0), test);
    }

// org.joda.time.TestPeriod_Constructors::testConstructor_trickyDifferences_LD_LD_toFeb_leapYear
    public void testConstructor_trickyDifferences_LD_LD_toFeb_leapYear() throws Throwable {
        LocalDate dt1 = new LocalDate(2012, 1, 1);
        LocalDate dt2 = new LocalDate(2012, 2, 29);
        Period test = new Period(dt1, dt2);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(new Period(0, 1, 4, 0, 0, 0, 0, 0), test);
    }
