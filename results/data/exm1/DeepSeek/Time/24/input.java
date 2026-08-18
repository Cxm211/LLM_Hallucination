// buggy code
    public long computeMillis(boolean resetFields, String text) {
        SavedField[] savedFields = iSavedFields;
        int count = iSavedFieldsCount;
        if (iSavedFieldsShared) {
            iSavedFields = savedFields = (SavedField[])iSavedFields.clone();
            iSavedFieldsShared = false;
        }
        sort(savedFields, count);
        if (count > 0) {
            // alter base year for parsing if first field is month or day
            DurationField months = DurationFieldType.months().getField(iChrono);
            DurationField days = DurationFieldType.days().getField(iChrono);
            DurationField first = savedFields[0].iField.getDurationField();
            if (compareReverse(first, months) >= 0 && compareReverse(first, days) <= 0) {
                saveField(DateTimeFieldType.year(), iDefaultYear);
                return computeMillis(resetFields, text);
            }
        }

        long millis = iMillis;
        try {
            for (int i = 0; i < count; i++) {
                millis = savedFields[i].set(millis, resetFields);
            }
        } catch (IllegalFieldValueException e) {
            if (text != null) {
                e.prependMessage("Cannot parse \"" + text + '"');
            }
            throw e;
        }
        
        if (iZone == null) {
            millis -= iOffset;
        } else {
            int offset = iZone.getOffsetFromLocal(millis);
            millis -= offset;
            if (offset != iZone.getOffset(millis)) {
                String message =
                    "Illegal instant due to time zone offset transition (" + iZone + ')';
                if (text != null) {
                    message = "Cannot parse \"" + text + "\": " + message;
                }
                throw new IllegalArgumentException(message);
            }
        }
        
        return millis;
    }

// relevant test
// org.joda.time.TestDateMidnight_Constructors::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW_UTC).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1_UTC).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2_UTC).toString());
    }

// org.joda.time.TestDateMidnight_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new DateMidnight(2010, 6, 30, ISOChronology.getInstance(LONDON)), DateMidnight.parse("2010-06-30"));
        assertEquals(new DateMidnight(2010, 1, 2, ISOChronology.getInstance(LONDON)), DateMidnight.parse("2010-002"));
    }

// org.joda.time.TestDateMidnight_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        assertEquals(new DateMidnight(2010, 6, 30, ISOChronology.getInstance(LONDON)), DateMidnight.parse("2010--30 06", DateTimeFormat.forPattern("yyyy--dd MM")));
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        DateMidnight test = new DateMidnight();
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight((DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        DateMidnight test = new DateMidnight(GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        DateMidnight test = new DateMidnight((Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME2_UTC);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long1_DateTimeZone
    public void testConstructor_long1_DateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long2_DateTimeZone
    public void testConstructor_long2_DateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME2_UTC, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME2_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long_nullDateTimeZone
    public void testConstructor_long_nullDateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME2_UTC, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        DateMidnight test = new DateMidnight(TEST_TIME1_UTC, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1_UTC);
        DateMidnight test = new DateMidnight(date);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_invalidObject
    public void testConstructor_invalidObject() throws Throwable {
        try {
            new DateMidnight(new Object());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        DateMidnight test = new DateMidnight((Object) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_badconverterObject
    public void testConstructor_badconverterObject() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateMidnight test = new DateMidnight(new Integer(0));
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L - DateTimeConstants.MILLIS_PER_HOUR, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Object_DateTimeZone
    public void testConstructor_Object_DateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1_UTC);
        DateMidnight test = new DateMidnight(date, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_invalidObject_DateTimeZone
    public void testConstructor_invalidObject_DateTimeZone() throws Throwable {
        try {
            new DateMidnight(new Object(), PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullObject_DateTimeZone
    public void testConstructor_nullObject_DateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight((Object) null, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW_PARIS, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Object_nullDateTimeZone
    public void testConstructor_Object_nullDateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1_UTC);
        DateMidnight test = new DateMidnight(date, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullObject_nullDateTimeZone
    public void testConstructor_nullObject_nullDateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight((Object) null, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_badconverterObject_DateTimeZone
    public void testConstructor_badconverterObject_DateTimeZone() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateMidnight test = new DateMidnight(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L - DateTimeConstants.MILLIS_PER_HOUR, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1_UTC);
        DateMidnight test = new DateMidnight(date, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_invalidObject_Chronology
    public void testConstructor_invalidObject_Chronology() throws Throwable {
        try {
            new DateMidnight(new Object(), GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        DateMidnight test = new DateMidnight((Object) null, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1_UTC);
        DateMidnight test = new DateMidnight(date, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        DateMidnight test = new DateMidnight((Object) null, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_badconverterObject_Chronology
    public void testConstructor_badconverterObject_Chronology() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateMidnight test = new DateMidnight(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L - DateTimeConstants.MILLIS_PER_HOUR, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_int_int_int
    public void testConstructor_int_int_int() throws Throwable {
        DateMidnight test = new DateMidnight(2002, 6, 9);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new DateMidnight(Integer.MIN_VALUE, 6, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(Integer.MAX_VALUE, 6, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 0, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 13, 9);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 31);
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateMidnight(2002, 7, 31);
        try {
            new DateMidnight(2002, 7, 32);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_DateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(2002, 6, 9, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW_PARIS, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new DateMidnight(Integer.MIN_VALUE, 6, 9, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(Integer.MAX_VALUE, 6, 9, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 0, 9, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 13, 9, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 31, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateMidnight(2002, 7, 31, PARIS);
        try {
            new DateMidnight(2002, 7, 32, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_nullDateTimeZone() throws Throwable {
        DateMidnight test = new DateMidnight(2002, 6, 9, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_int_int_int_Chronology
    public void testConstructor_int_int_int_Chronology() throws Throwable {
        DateMidnight test = new DateMidnight(2002, 6, 9, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        try {
            new DateMidnight(Integer.MIN_VALUE, 6, 9, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(Integer.MAX_VALUE, 6, 9, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 0, 9, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 13, 9, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateMidnight(2002, 6, 31, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateMidnight(2002, 7, 31, GregorianChronology.getInstance());
        try {
            new DateMidnight(2002, 7, 32, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateMidnight_Constructors::testConstructor_int_int_int_nullChronology
    public void testConstructor_int_int_int_nullChronology() throws Throwable {
        DateMidnight test = new DateMidnight(2002, 6, 9, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW_LONDON, test.getMillis());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
    }

// org.joda.time.TestDateTimeComparator::testClass
    public void testClass() {
        assertEquals(true, Modifier.isPublic(DateTimeComparator.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DateTimeComparator.class.getModifiers()));
        assertEquals(1, DateTimeComparator.class.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(DateTimeComparator.class.getDeclaredConstructors()[0].getModifiers()));
    }

// org.joda.time.TestDateTimeComparator::testStaticGetInstance
    public void testStaticGetInstance() {
        DateTimeComparator c = DateTimeComparator.getInstance();
        assertEquals(null, c.getLowerLimit());
        assertEquals(null, c.getUpperLimit());
        assertEquals("DateTimeComparator[]", c.toString());
    }

// org.joda.time.TestDateTimeComparator::testStaticGetDateOnlyInstance
    public void testStaticGetDateOnlyInstance() {
        DateTimeComparator c = DateTimeComparator.getDateOnlyInstance();
        assertEquals(DateTimeFieldType.dayOfYear(), c.getLowerLimit());
        assertEquals(null, c.getUpperLimit());
        assertEquals("DateTimeComparator[dayOfYear-]", c.toString());
        
        assertSame(DateTimeComparator.getDateOnlyInstance(), DateTimeComparator.getDateOnlyInstance());
    }

// org.joda.time.TestDateTimeComparator::testStaticGetTimeOnlyInstance
    public void testStaticGetTimeOnlyInstance() {
        DateTimeComparator c = DateTimeComparator.getTimeOnlyInstance();
        assertEquals(null, c.getLowerLimit());
        assertEquals(DateTimeFieldType.dayOfYear(), c.getUpperLimit());
        assertEquals("DateTimeComparator[-dayOfYear]", c.toString());
        
        assertSame(DateTimeComparator.getTimeOnlyInstance(), DateTimeComparator.getTimeOnlyInstance());
    }

// org.joda.time.TestDateTimeComparator::testStaticGetInstanceLower
    public void testStaticGetInstanceLower() {
        DateTimeComparator c = DateTimeComparator.getInstance(DateTimeFieldType.hourOfDay());
        assertEquals(DateTimeFieldType.hourOfDay(), c.getLowerLimit());
        assertEquals(null, c.getUpperLimit());
        assertEquals("DateTimeComparator[hourOfDay-]", c.toString());
        
        c = DateTimeComparator.getInstance(null);
        assertSame(DateTimeComparator.getInstance(), c);
    }

// org.joda.time.TestDateTimeComparator::testStaticGetInstanceLowerUpper
    public void testStaticGetInstanceLowerUpper() {
        DateTimeComparator c = DateTimeComparator.getInstance(DateTimeFieldType.hourOfDay(), DateTimeFieldType.dayOfYear());
        assertEquals(DateTimeFieldType.hourOfDay(), c.getLowerLimit());
        assertEquals(DateTimeFieldType.dayOfYear(), c.getUpperLimit());
        assertEquals("DateTimeComparator[hourOfDay-dayOfYear]", c.toString());
        
        c = DateTimeComparator.getInstance(DateTimeFieldType.hourOfDay(), DateTimeFieldType.hourOfDay());
        assertEquals(DateTimeFieldType.hourOfDay(), c.getLowerLimit());
        assertEquals(DateTimeFieldType.hourOfDay(), c.getUpperLimit());
        assertEquals("DateTimeComparator[hourOfDay]", c.toString());
        
        c = DateTimeComparator.getInstance(null, null);
        assertSame(DateTimeComparator.getInstance(), c);
        
        c = DateTimeComparator.getInstance(DateTimeFieldType.dayOfYear(), null);
        assertSame(DateTimeComparator.getDateOnlyInstance(), c);
        
        c = DateTimeComparator.getInstance(null, DateTimeFieldType.dayOfYear());
        assertSame(DateTimeComparator.getTimeOnlyInstance(), c);
    }

// org.joda.time.TestDateTimeComparator::testEqualsHashCode
    public void testEqualsHashCode() {
        DateTimeComparator c1 = DateTimeComparator.getInstance();
        assertEquals(true, c1.equals(c1));
        assertEquals(false, c1.equals(null));
        assertEquals(true, c1.hashCode() == c1.hashCode());
        
        DateTimeComparator c2 = DateTimeComparator.getTimeOnlyInstance();
        assertEquals(true, c2.equals(c2));
        assertEquals(false, c2.equals(c1));
        assertEquals(false, c1.equals(c2));
        assertEquals(false, c2.equals(null));
        assertEquals(false, c1.hashCode() == c2.hashCode());
        
        DateTimeComparator c3 = DateTimeComparator.getTimeOnlyInstance();
        assertEquals(true, c3.equals(c3));
        assertEquals(false, c3.equals(c1));
        assertEquals(true, c3.equals(c2));
        assertEquals(false, c1.equals(c3));
        assertEquals(true, c2.equals(c3));
        assertEquals(false, c1.hashCode() == c3.hashCode());
        assertEquals(true, c2.hashCode() == c3.hashCode());
        
        DateTimeComparator c4 = DateTimeComparator.getDateOnlyInstance();
        assertEquals(false, c4.hashCode() == c3.hashCode());
    }

// org.joda.time.TestDateTimeComparator::testSerialization1
    public void testSerialization1() throws Exception {
        DateTimeField f = ISO.dayOfYear();
        f.toString();
        DateTimeComparator c = DateTimeComparator.getInstance(DateTimeFieldType.hourOfDay(), DateTimeFieldType.dayOfYear());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(c);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeComparator result = (DateTimeComparator) ois.readObject();
        ois.close();
        
        assertEquals(c, result);
    }

// org.joda.time.TestDateTimeComparator::testSerialization2
    public void testSerialization2() throws Exception {
        DateTimeComparator c = DateTimeComparator.getInstance();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(c);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeComparator result = (DateTimeComparator) ois.readObject();
        ois.close();
        
        assertSame(c, result);
    }

// org.joda.time.TestDateTimeComparator::testBasicComps1
    public void testBasicComps1() {
        aDateTime = new DateTime( System.currentTimeMillis(), DateTimeZone.UTC );
        bDateTime = new DateTime( aDateTime.getMillis(), DateTimeZone.UTC );
        assertEquals( "getMillis", aDateTime.getMillis(),
            bDateTime.getMillis() );
        assertEquals( "MILLIS", 0, cMillis.compare( aDateTime, bDateTime ) );
        assertEquals( "SECOND", 0, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "MINUTE", 0, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "HOUR", 0, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "DOW", 0, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOM", 0, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOY", 0, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOW", 0, cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WY", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTH", 0, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "YEAR", 0, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DATE", 0, cDate.compare( aDateTime, bDateTime ) );
        assertEquals( "TIME", 0, cTime.compare( aDateTime, bDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testBasicComps2
    public void testBasicComps2() {
        ReadableInstant aDateTime = new DateTime( System.currentTimeMillis(), DateTimeZone.UTC );
        ReadableInstant bDateTime = new DateTime( aDateTime.getMillis(), DateTimeZone.UTC );
        assertEquals( "getMillis", aDateTime.getMillis(),
            bDateTime.getMillis() );
        assertEquals( "MILLIS", 0, cMillis.compare( aDateTime, bDateTime ) );
        assertEquals( "SECOND", 0, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "MINUTE", 0, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "HOUR", 0, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "DOW", 0, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOM", 0, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOY", 0, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOW", 0, cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WY", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTH", 0, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "YEAR", 0, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DATE", 0, cDate.compare( aDateTime, bDateTime ) );
        assertEquals( "TIME", 0, cTime.compare( aDateTime, bDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testBasicComps3
    public void testBasicComps3() {
        Date aDateTime
            = new Date( System.currentTimeMillis() );
        Date bDateTime
            = new Date( aDateTime.getTime() );
        assertEquals( "MILLIS", 0, cMillis.compare( aDateTime, bDateTime ) );
        assertEquals( "SECOND", 0, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "MINUTE", 0, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "HOUR", 0, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "DOW", 0, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOM", 0, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOY", 0, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOW", 0, cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WY", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTH", 0, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "YEAR", 0, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DATE", 0, cDate.compare( aDateTime, bDateTime ) );
        assertEquals( "TIME", 0, cTime.compare( aDateTime, bDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testBasicComps4
    public void testBasicComps4() {
        Long aDateTime
            = new Long( System.currentTimeMillis() );
        Long bDateTime
            = new Long( aDateTime.longValue() );
        assertEquals( "MILLIS", 0, cMillis.compare( aDateTime, bDateTime ) );
        assertEquals( "SECOND", 0, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "MINUTE", 0, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "HOUR", 0, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "DOW", 0, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOM", 0, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOY", 0, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOW", 0, cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WY", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTH", 0, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "YEAR", 0, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DATE", 0, cDate.compare( aDateTime, bDateTime ) );
        assertEquals( "TIME", 0, cTime.compare( aDateTime, bDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testBasicComps5
    public void testBasicComps5() {
        Calendar aDateTime
            = Calendar.getInstance();   
        Calendar bDateTime = aDateTime;
        assertEquals( "MILLIS", 0, cMillis.compare( aDateTime, bDateTime ) );
        assertEquals( "SECOND", 0, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "MINUTE", 0, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "HOUR", 0, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "DOW", 0, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOM", 0, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOY", 0, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOW", 0, cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WY", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTH", 0, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "YEAR", 0, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DATE", 0, cDate.compare( aDateTime, bDateTime ) );
        assertEquals( "TIME", 0, cTime.compare( aDateTime, bDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testMillis
    public void testMillis() {}

// org.joda.time.TestDateTimeComparator::testSecond
    public void testSecond() {
        aDateTime = getADate( "1969-12-31T23:59:58" );
        bDateTime = getADate( "1969-12-31T23:50:59" );
        assertEquals( "SecondM1a", -1, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "SecondP1a", 1, cSecond.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1970-01-01T00:00:00" );
        bDateTime = getADate( "1970-01-01T00:00:01" );
        assertEquals( "SecondM1b", -1, cSecond.compare( aDateTime, bDateTime ) );
        assertEquals( "SecondP1b", 1, cSecond.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testMinute
    public void testMinute() {
        aDateTime = getADate( "1969-12-31T23:58:00" );
        bDateTime = getADate( "1969-12-31T23:59:00" );
        assertEquals( "MinuteM1a", -1, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "MinuteP1a", 1, cMinute.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1970-01-01T00:00:00" );
        bDateTime = getADate( "1970-01-01T00:01:00" );
        assertEquals( "MinuteM1b", -1, cMinute.compare( aDateTime, bDateTime ) );
        assertEquals( "MinuteP1b", 1, cMinute.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testHour
    public void testHour() {
        aDateTime = getADate( "1969-12-31T22:00:00" );
        bDateTime = getADate( "1969-12-31T23:00:00" );
        assertEquals( "HourM1a", -1, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "HourP1a", 1, cHour.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1970-01-01T00:00:00" );
        bDateTime = getADate( "1970-01-01T01:00:00" );
        assertEquals( "HourM1b", -1, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "HourP1b", 1, cHour.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1969-12-31T23:59:59" );
        bDateTime = getADate( "1970-01-01T00:00:00" );
        assertEquals( "HourP1c", 1, cHour.compare( aDateTime, bDateTime ) );
        assertEquals( "HourM1c", -1, cHour.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testDOW
    public void testDOW() {
        
        aDateTime = getADate( "2002-04-12T00:00:00" );
        bDateTime = getADate( "2002-04-13T00:00:00" );
        assertEquals( "DOWM1a", -1, cDayOfWeek.compare( aDateTime, bDateTime ) );
        assertEquals( "DOWP1a", 1, cDayOfWeek.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testDOM
    public void testDOM() {
        aDateTime = getADate( "2002-04-12T00:00:00" );
        bDateTime = getADate( "2002-04-13T00:00:00" );
        assertEquals( "DOMM1a", -1, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOMP1a", 1, cDayOfMonth.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "2000-12-01T00:00:00" );
        bDateTime = getADate( "1814-04-30T00:00:00" );
        assertEquals( "DOMM1b", -1, cDayOfMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "DOMP1b", 1, cDayOfMonth.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testDOY
    public void testDOY() {
        aDateTime = getADate( "2002-04-12T00:00:00" );
        bDateTime = getADate( "2002-04-13T00:00:00" );
        assertEquals( "DOYM1a", -1, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DOYP1a", 1, cDayOfYear.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "2000-02-29T00:00:00" );
        bDateTime = getADate( "1814-11-30T00:00:00" );
        assertEquals( "DOYM1b", -1, cDayOfYear.compare( aDateTime, bDateTime ) );
        assertEquals( "DOYP1b", 1, cDayOfYear.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testWOW
    public void testWOW() {
        
        aDateTime = getADate( "2000-01-04T00:00:00" );
        bDateTime = getADate( "2000-01-11T00:00:00" );
        assertEquals( "WOWM1a", -1,
            cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOWP1a", 1,
            cWeekOfWeekyear.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "2000-01-04T00:00:00" );
        bDateTime = getADate( "1999-12-31T00:00:00" );
        assertEquals( "WOWM1b", -1,
            cWeekOfWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "WOWP1b", 1,
            cWeekOfWeekyear.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testWOYY
    public void testWOYY() {
        
        
        aDateTime = getADate( "1998-12-31T23:59:59" );
        bDateTime = getADate( "1999-01-01T00:00:00" );
        assertEquals( "YOYYZ", 0, cWeekyear.compare( aDateTime, bDateTime ) );
        bDateTime = getADate( "1999-01-04T00:00:00" );
        assertEquals( "YOYYM1", -1, cWeekyear.compare( aDateTime, bDateTime ) );
        assertEquals( "YOYYP1", 1, cWeekyear.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testMonth
    public void testMonth() {
        aDateTime = getADate( "2002-04-30T00:00:00" );
        bDateTime = getADate( "2002-05-01T00:00:00" );
        assertEquals( "MONTHM1a", -1, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTHP1a", 1, cMonth.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1900-01-01T00:00:00" );
        bDateTime = getADate( "1899-12-31T00:00:00" );
        assertEquals( "MONTHM1b", -1, cMonth.compare( aDateTime, bDateTime ) );
        assertEquals( "MONTHP1b", 1, cMonth.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testYear
    public void testYear() {
        aDateTime = getADate( "2000-01-01T00:00:00" );
        bDateTime = getADate( "2001-01-01T00:00:00" );
        assertEquals( "YEARM1a", -1, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "YEARP1a", 1, cYear.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1968-12-31T23:59:59" );
        bDateTime = getADate( "1970-01-01T00:00:00" );
        assertEquals( "YEARM1b", -1, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "YEARP1b", 1, cYear.compare( bDateTime, aDateTime ) );
        aDateTime = getADate( "1969-12-31T23:59:59" );
        bDateTime = getADate( "1970-01-01T00:00:00" );
        assertEquals( "YEARM1c", -1, cYear.compare( aDateTime, bDateTime ) );
        assertEquals( "YEARP1c", 1, cYear.compare( bDateTime, aDateTime ) );
    }

// org.joda.time.TestDateTimeComparator::testListBasic
     public void testListBasic() {
        String[] dtStrs = {
            "1999-02-01T00:00:00",
            "1998-01-20T00:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListBasic", !isSorted1, isSorted2);
     }

// org.joda.time.TestDateTimeComparator::testListMillis
    public void testListMillis() {
        
        List sl = new ArrayList();
        long base = 12345L * 1000L;
        sl.add( new DateTime( base + 999L, DateTimeZone.UTC ) );
        sl.add( new DateTime( base + 222L, DateTimeZone.UTC ) );
        sl.add( new DateTime( base + 456L, DateTimeZone.UTC ) );
        sl.add( new DateTime( base + 888L, DateTimeZone.UTC ) );
        sl.add( new DateTime( base + 123L, DateTimeZone.UTC ) );
        sl.add( new DateTime( base + 000L, DateTimeZone.UTC ) );
        
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cMillis );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListLillis", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListSecond
    public void testListSecond() {
        String[] dtStrs = {
            "1999-02-01T00:00:10",
            "1999-02-01T00:00:30",
            "1999-02-01T00:00:25",
            "1999-02-01T00:00:18",
            "1999-02-01T00:00:01",
            "1999-02-01T00:00:59",
            "1999-02-01T00:00:22"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cSecond );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListSecond", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListMinute
    public void testListMinute() {
        String[] dtStrs = {
            "1999-02-01T00:10:00",
            "1999-02-01T00:30:00",
            "1999-02-01T00:25:00",
            "1999-02-01T00:18:00",
            "1999-02-01T00:01:00",
            "1999-02-01T00:59:00",
            "1999-02-01T00:22:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cMinute );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListMinute", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListHour
    public void testListHour() {
        String[] dtStrs = {
            "1999-02-01T10:00:00",
            "1999-02-01T23:00:00",
            "1999-02-01T01:00:00",
            "1999-02-01T15:00:00",
            "1999-02-01T05:00:00",
            "1999-02-01T20:00:00",
            "1999-02-01T17:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cHour );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListHour", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListDOW
    public void testListDOW() {
        String[] dtStrs = {
            
            "2002-04-21T10:00:00",
            "2002-04-16T10:00:00",
            "2002-04-15T10:00:00",
            "2002-04-17T10:00:00",
            "2002-04-19T10:00:00",
            "2002-04-18T10:00:00",
            "2002-04-20T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cDayOfWeek );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListDOW", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListDOM
    public void testListDOM() {
        String[] dtStrs = {
            
            "2002-04-20T10:00:00",
            "2002-04-16T10:00:00",
            "2002-04-15T10:00:00",
            "2002-04-17T10:00:00",
            "2002-04-19T10:00:00",
            "2002-04-18T10:00:00",
            "2002-04-14T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cDayOfMonth );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListDOM", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListDOY
    public void testListDOY() {
        String[] dtStrs = {
            "2002-04-20T10:00:00",
            "2002-01-16T10:00:00",
            "2002-12-31T10:00:00",
            "2002-09-14T10:00:00",
            "2002-09-19T10:00:00",
            "2002-02-14T10:00:00",
            "2002-10-30T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cDayOfYear );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListDOY", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListWOW
    public void testListWOW() {
        String[] dtStrs = {
            "2002-04-01T10:00:00",
            "2002-01-01T10:00:00",
            "2002-12-01T10:00:00",
            "2002-09-01T10:00:00",
            "2002-09-01T10:00:00",
            "2002-02-01T10:00:00",
            "2002-10-01T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cWeekOfWeekyear );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListWOW", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListYOYY
    public void testListYOYY() {
        
        String[] dtStrs = {
            "2010-04-01T10:00:00",
            "2002-01-01T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cWeekyear );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListYOYY", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListMonth
    public void testListMonth() {
        String[] dtStrs = {
            "2002-04-01T10:00:00",
            "2002-01-01T10:00:00",
            "2002-12-01T10:00:00",
            "2002-09-01T10:00:00",
            "2002-09-01T10:00:00",
            "2002-02-01T10:00:00",
            "2002-10-01T10:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cMonth );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListMonth", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListYear
     public void testListYear() {
        String[] dtStrs = {
            "1999-02-01T00:00:00",
            "1998-02-01T00:00:00",
            "2525-02-01T00:00:00",
            "1776-02-01T00:00:00",
            "1863-02-01T00:00:00",
            "1066-02-01T00:00:00",
            "2100-02-01T00:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cYear );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListYear", !isSorted1, isSorted2);
     }

// org.joda.time.TestDateTimeComparator::testListDate
    public void testListDate() {
        String[] dtStrs = {
            "1999-02-01T00:00:00",
            "1998-10-03T00:00:00",
            "2525-05-20T00:00:00",
            "1776-12-25T00:00:00",
            "1863-01-31T00:00:00",
            "1066-09-22T00:00:00",
            "2100-07-04T00:00:00"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cDate );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListDate", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testListTime
    public void testListTime() {
        String[] dtStrs = {
            "1999-02-01T01:02:05",
            "1999-02-01T22:22:22",
            "1999-02-01T05:30:45",
            "1999-02-01T09:17:59",
            "1999-02-01T09:17:58",
            "1999-02-01T15:30:00",
            "1999-02-01T17:00:44"
        };
        
        List sl = loadAList( dtStrs );
        boolean isSorted1 = isListSorted( sl );
        Collections.sort( sl, cTime );
        boolean isSorted2 = isListSorted( sl );
        assertEquals("ListTime", !isSorted1, isSorted2);
    }

// org.joda.time.TestDateTimeComparator::testNullDT
    public void testNullDT() {
        
        aDateTime = getADate("2000-01-01T00:00:00");
        assertTrue(cYear.compare(null, aDateTime) > 0);
        assertTrue(cYear.compare(aDateTime, null) < 0);
    }

// org.joda.time.TestDateTimeComparator::testInvalidObj
    public void testInvalidObj() {
        aDateTime = getADate("2000-01-01T00:00:00");
        try {
            cYear.compare("FreeBird", aDateTime);
            fail("Invalid object failed");
        } catch (IllegalArgumentException cce) {}
    }

// org.joda.time.TestDateTimeZone::testDefault
    public void testDefault() {
        assertNotNull(DateTimeZone.getDefault());
        
        DateTimeZone.setDefault(PARIS);
        assertSame(PARIS, DateTimeZone.getDefault());
        
        try {
            DateTimeZone.setDefault(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZone::testDefaultSecurity
    public void testDefaultSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            DateTimeZone.setDefault(PARIS);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
    }

// org.joda.time.TestDateTimeZone::testForID_String
    public void testForID_String() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID((String) null));
        
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals("Europe/London", zone.getID());
        
        zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forID("+00");
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forID("+01:23");
        assertEquals("+01:23", zone.getID());
        assertEquals(DateTimeConstants.MILLIS_PER_HOUR + (23L * DateTimeConstants.MILLIS_PER_MINUTE),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forID("-02:00");
        assertEquals("-02:00", zone.getID());
        assertEquals((-2L * DateTimeConstants.MILLIS_PER_HOUR),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forID("-07:05:34.0");
        assertEquals("-07:05:34", zone.getID());
        assertEquals((-7L * DateTimeConstants.MILLIS_PER_HOUR) +
                    (-5L * DateTimeConstants.MILLIS_PER_MINUTE) +
                    (-34L * DateTimeConstants.MILLIS_PER_SECOND),
                    zone.getOffset(TEST_TIME_SUMMER));
        
        try {
            DateTimeZone.forID("SST");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("europe/london");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("Europe/UK");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("+");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("+0");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZone::testForOffsetHours_int
    public void testForOffsetHours_int() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertEquals(DateTimeZone.forID("+03:00"), DateTimeZone.forOffsetHours(3));
        assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetHours(-2));
        try {
            DateTimeZone.forOffsetHours(999999);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZone::testForOffsetHoursMinutes_int_int
    public void testForOffsetHoursMinutes_int_int() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
        assertEquals(DateTimeZone.forID("+03:15"), DateTimeZone.forOffsetHoursMinutes(3, 15));
        assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetHoursMinutes(-2, 0));
        assertEquals(DateTimeZone.forID("-02:30"), DateTimeZone.forOffsetHoursMinutes(-2, 30));
        try {
            DateTimeZone.forOffsetHoursMinutes(2, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(-2, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(2, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(-2, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(999999, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZone::testForOffsetMillis_int
    public void testForOffsetMillis_int() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
        assertEquals(DateTimeZone.forID("+03:00"), DateTimeZone.forOffsetMillis(3 * 60 * 60 * 1000));
        assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetMillis(-2 * 60 * 60 * 1000));
        assertEquals(DateTimeZone.forID("+04:45:17.045"),
                DateTimeZone.forOffsetMillis(
                        4 * 60 * 60 * 1000 + 45 * 60 * 1000 + 17 * 1000 + 45));
    }

// org.joda.time.TestDateTimeZone::testForTimeZone_TimeZone
    public void testForTimeZone_TimeZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone((TimeZone) null));
        
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London"));
        assertEquals("Europe/London", zone.getID());
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("+00:00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+01:23"));
        assertEquals("+01:23", zone.getID());
        assertEquals(DateTimeConstants.MILLIS_PER_HOUR + (23L * DateTimeConstants.MILLIS_PER_MINUTE),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-02:00"));
        assertEquals("-02:00", zone.getID());
        assertEquals((-2L * DateTimeConstants.MILLIS_PER_HOUR), zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", zone.getID());
    }

// org.joda.time.TestDateTimeZone::testTimeZoneConversion
    public void testTimeZoneConversion() {
        TimeZone jdkTimeZone = TimeZone.getTimeZone("GMT-10");
        assertEquals("GMT-10:00", jdkTimeZone.getID());
        
        DateTimeZone jodaTimeZone = DateTimeZone.forTimeZone(jdkTimeZone);
        assertEquals("-10:00", jodaTimeZone.getID());
        assertEquals(jdkTimeZone.getRawOffset(), jodaTimeZone.getOffset(0L));
        
        TimeZone convertedTimeZone = jodaTimeZone.toTimeZone();
        assertEquals("GMT-10:00", jdkTimeZone.getID());
        
        assertEquals(jdkTimeZone.getID(), convertedTimeZone.getID());
        assertEquals(jdkTimeZone.getRawOffset(), convertedTimeZone.getRawOffset());
    }

// org.joda.time.TestDateTimeZone::testGetAvailableIDs
    public void testGetAvailableIDs() {
        assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
    }

// org.joda.time.TestDateTimeZone::testProvider
    public void testProvider() {
        try {
            assertNotNull(DateTimeZone.getProvider());
        
            Provider provider = DateTimeZone.getProvider();
            DateTimeZone.setProvider(null);
            assertEquals(provider.getClass(), DateTimeZone.getProvider().getClass());
        
            try {
                DateTimeZone.setProvider(new MockNullIDSProvider());
                fail();
            } catch (IllegalArgumentException ex) {}
            try {
                DateTimeZone.setProvider(new MockEmptyIDSProvider());
                fail();
            } catch (IllegalArgumentException ex) {}
            try {
                DateTimeZone.setProvider(new MockNoUTCProvider());
                fail();
            } catch (IllegalArgumentException ex) {}
            try {
                DateTimeZone.setProvider(new MockBadUTCProvider());
                fail();
            } catch (IllegalArgumentException ex) {}
        
            Provider prov = new MockOKProvider();
            DateTimeZone.setProvider(prov);
            assertSame(prov, DateTimeZone.getProvider());
            assertEquals(2, DateTimeZone.getAvailableIDs().size());
            assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
            assertTrue(DateTimeZone.getAvailableIDs().contains("Europe/London"));
        } finally {
            DateTimeZone.setProvider(null);
            assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
        }
        
        try {
            System.setProperty("org.joda.time.DateTimeZone.Provider", "org.joda.time.tz.UTCProvider");
            DateTimeZone.setProvider(null);
            assertEquals(UTCProvider.class, DateTimeZone.getProvider().getClass());
        } finally {
            System.getProperties().remove("org.joda.time.DateTimeZone.Provider");
            DateTimeZone.setProvider(null);
            assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
        }
        
        PrintStream syserr = System.err;
        try {
            System.setProperty("org.joda.time.DateTimeZone.Provider", "xxx");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setErr(new PrintStream(baos));
            
            DateTimeZone.setProvider(null);
            
            assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
            String str = new String(baos.toByteArray());
            assertTrue(str.indexOf("java.lang.ClassNotFoundException") >= 0);
        } finally {
            System.setErr(syserr);
            System.getProperties().remove("org.joda.time.DateTimeZone.Provider");
            DateTimeZone.setProvider(null);
            assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
        }
    }

// org.joda.time.TestDateTimeZone::testProviderSecurity
    public void testProviderSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            DateTimeZone.setProvider(new MockOKProvider());
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
    }

// org.joda.time.TestDateTimeZone::testNameProvider
    public void testNameProvider() {
        try {
            assertNotNull(DateTimeZone.getNameProvider());
        
            NameProvider provider = DateTimeZone.getNameProvider();
            DateTimeZone.setNameProvider(null);
            assertEquals(provider.getClass(), DateTimeZone.getNameProvider().getClass());
        
            provider = new MockOKButNullNameProvider();
            DateTimeZone.setNameProvider(provider);
            assertSame(provider, DateTimeZone.getNameProvider());
            
            assertEquals("+00:00", DateTimeZone.UTC.getShortName(TEST_TIME_SUMMER));
            assertEquals("+00:00", DateTimeZone.UTC.getName(TEST_TIME_SUMMER));
        } finally {
            DateTimeZone.setNameProvider(null);
        }
        
        try {
            System.setProperty("org.joda.time.DateTimeZone.NameProvider", "org.joda.time.tz.DefaultNameProvider");
            DateTimeZone.setNameProvider(null);
            assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
        } finally {
            System.getProperties().remove("org.joda.time.DateTimeZone.NameProvider");
            DateTimeZone.setNameProvider(null);
            assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
        }
        
        PrintStream syserr = System.err;
        try {
            System.setProperty("org.joda.time.DateTimeZone.NameProvider", "xxx");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setErr(new PrintStream(baos));
            
            DateTimeZone.setNameProvider(null);
            
            assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
            String str = new String(baos.toByteArray());
            assertTrue(str.indexOf("java.lang.ClassNotFoundException") >= 0);
        } finally {
            System.setErr(syserr);
            System.getProperties().remove("org.joda.time.DateTimeZone.NameProvider");
            DateTimeZone.setNameProvider(null);
            assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
        }
    }

// org.joda.time.TestDateTimeZone::testNameProviderSecurity
    public void testNameProviderSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            DateTimeZone.setNameProvider(new MockOKButNullNameProvider());
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
    }

// org.joda.time.TestDateTimeZone::testConstructor
    public void testConstructor() {
        assertEquals(1, DateTimeZone.class.getDeclaredConstructors().length);
        assertTrue(Modifier.isProtected(DateTimeZone.class.getDeclaredConstructors()[0].getModifiers()));
        try {
            new DateTimeZone(null) {
                public String getNameKey(long instant) {
                    return null;
                }
                public int getOffset(long instant) {
                    return 0;
                }
                public int getStandardOffset(long instant) {
                    return 0;
                }
                public boolean isFixed() {
                    return false;
                }
                public long nextTransition(long instant) {
                    return 0;
                }
                public long previousTransition(long instant) {
                    return 0;
                }
                public boolean equals(Object object) {
                    return false;
                }
            };
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTimeZone::testGetID
    public void testGetID() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals("Europe/Paris", zone.getID());
    }

// org.joda.time.TestDateTimeZone::testGetNameKey
    public void testGetNameKey() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals("BST", zone.getNameKey(TEST_TIME_SUMMER));
        assertEquals("GMT", zone.getNameKey(TEST_TIME_WINTER));
    }

// org.joda.time.TestDateTimeZone::testGetShortName
    public void testGetShortName() {}

// org.joda.time.TestDateTimeZone::testGetShortNameProviderName
    public void testGetShortNameProviderName() {
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(null, "Europe/London", "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(Locale.ENGLISH, null, "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(Locale.ENGLISH, "Europe/London", null));
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(null, null, null));
    }

// org.joda.time.TestDateTimeZone::testGetShortNameNullKey
    public void testGetShortNameNullKey() {
        DateTimeZone zone = new MockDateTimeZone("Europe/London");
        assertEquals("Europe/London", zone.getShortName(TEST_TIME_SUMMER, Locale.ENGLISH));
    }

// org.joda.time.TestDateTimeZone::testGetName
    public void testGetName() {}

// org.joda.time.TestDateTimeZone::testGetNameProviderName
    public void testGetNameProviderName() {
        assertEquals(null, DateTimeZone.getNameProvider().getName(null, "Europe/London", "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getName(Locale.ENGLISH, null, "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getName(Locale.ENGLISH, "Europe/London", null));
        assertEquals(null, DateTimeZone.getNameProvider().getName(null, null, null));
    }

// org.joda.time.TestDateTimeZone::testGetNameNullKey
    public void testGetNameNullKey() {
        DateTimeZone zone = new MockDateTimeZone("Europe/London");
        assertEquals("Europe/London", zone.getName(TEST_TIME_SUMMER, Locale.ENGLISH));
    }

// org.joda.time.TestDateTimeZone::testGetOffset_long
    public void testGetOffset_long() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_WINTER));
        
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_WINTER));
        
        assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_WINTER));
        
        assertEquals(false, zone.isStandardOffset(TEST_TIME_SUMMER));
        assertEquals(true, zone.isStandardOffset(TEST_TIME_WINTER));
    }

// org.joda.time.TestDateTimeZone::testGetOffset_RI
    public void testGetOffset_RI() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_SUMMER)));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_WINTER)));
        
        assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset(null));
    }

// org.joda.time.TestDateTimeZone::testGetOffsetFixed
    public void testGetOffsetFixed() {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_WINTER));
        
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_WINTER));
        
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_WINTER));
        
        assertEquals(true, zone.isStandardOffset(TEST_TIME_SUMMER));
        assertEquals(true, zone.isStandardOffset(TEST_TIME_WINTER));
    }

// org.joda.time.TestDateTimeZone::testGetOffsetFixed_RI
    public void testGetOffsetFixed_RI() {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_SUMMER)));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_WINTER)));
        
        assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset(null));
    }

// org.joda.time.TestDateTimeZone::testGetMillisKeepLocal
    public void testGetMillisKeepLocal() {
        long millisLondon = TEST_TIME_SUMMER;
        long millisParis = TEST_TIME_SUMMER - 1L * DateTimeConstants.MILLIS_PER_HOUR;
        
        assertEquals(millisLondon, LONDON.getMillisKeepLocal(LONDON, millisLondon));
        assertEquals(millisParis, LONDON.getMillisKeepLocal(LONDON, millisParis));
        assertEquals(millisLondon, PARIS.getMillisKeepLocal(PARIS, millisLondon));
        assertEquals(millisParis, PARIS.getMillisKeepLocal(PARIS, millisParis));
        
        assertEquals(millisParis, LONDON.getMillisKeepLocal(PARIS, millisLondon));
        assertEquals(millisLondon, PARIS.getMillisKeepLocal(LONDON, millisParis));
        
        DateTimeZone zone = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(LONDON);
            assertEquals(millisLondon, PARIS.getMillisKeepLocal(null, millisParis));
        } finally {
            DateTimeZone.setDefault(zone);
        }
    }

// org.joda.time.TestDateTimeZone::testIsFixed
    public void testIsFixed() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals(false, zone.isFixed());
        assertEquals(true, DateTimeZone.UTC.isFixed());
    }

// org.joda.time.TestDateTimeZone::testTransitionFixed
    public void testTransitionFixed() {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        assertEquals(TEST_TIME_SUMMER, zone.nextTransition(TEST_TIME_SUMMER));
        assertEquals(TEST_TIME_WINTER, zone.nextTransition(TEST_TIME_WINTER));
        assertEquals(TEST_TIME_SUMMER, zone.previousTransition(TEST_TIME_SUMMER));
        assertEquals(TEST_TIME_WINTER, zone.previousTransition(TEST_TIME_WINTER));
    }

// org.joda.time.TestDateTimeZone::testIsLocalDateTimeGap_Berlin
    public void testIsLocalDateTimeGap_Berlin() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Berlin");
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 1, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 1, 59, 59, 99)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 0)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 30)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 59, 59, 99)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 3, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 4, 0)));
        
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 1, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 2, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 3, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 12, 24, 12, 34)));
    }

// org.joda.time.TestDateTimeZone::testIsLocalDateTimeGap_NewYork
    public void testIsLocalDateTimeGap_NewYork() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 1, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 1, 59, 59, 99)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 0)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 30)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 59, 59, 99)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 3, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 4, 0)));
        
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 0, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 1, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 2, 30)));  
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 12, 24, 12, 34)));
    }

// org.joda.time.TestDateTimeZone::testToTimeZone
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        TimeZone tz = zone.toTimeZone();
        assertEquals("Europe/Paris", tz.getID());
    }

// org.joda.time.TestDateTimeZone::testEqualsHashCode
    public void testEqualsHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Paris");
        assertEquals(true, zone1.equals(zone1));
        assertEquals(true, zone1.equals(zone2));
        assertEquals(true, zone2.equals(zone1));
        assertEquals(true, zone2.equals(zone2));
        assertEquals(true, zone1.hashCode() == zone2.hashCode());
        
        DateTimeZone zone3 = DateTimeZone.forID("Europe/London");
        assertEquals(true, zone3.equals(zone3));
        assertEquals(false, zone1.equals(zone3));
        assertEquals(false, zone2.equals(zone3));
        assertEquals(false, zone3.equals(zone1));
        assertEquals(false, zone3.equals(zone2));
        assertEquals(false, zone1.hashCode() == zone3.hashCode());
        assertEquals(true, zone3.hashCode() == zone3.hashCode());
        
        DateTimeZone zone4 = DateTimeZone.forID("+01:00");
        assertEquals(true, zone4.equals(zone4));
        assertEquals(false, zone1.equals(zone4));
        assertEquals(false, zone2.equals(zone4));
        assertEquals(false, zone3.equals(zone4));
        assertEquals(false, zone4.equals(zone1));
        assertEquals(false, zone4.equals(zone2));
        assertEquals(false, zone4.equals(zone3));
        assertEquals(false, zone1.hashCode() == zone4.hashCode());
        assertEquals(true, zone4.hashCode() == zone4.hashCode());
        
        DateTimeZone zone5 = DateTimeZone.forID("+02:00");
        assertEquals(true, zone5.equals(zone5));
        assertEquals(false, zone1.equals(zone5));
        assertEquals(false, zone2.equals(zone5));
        assertEquals(false, zone3.equals(zone5));
        assertEquals(false, zone4.equals(zone5));
        assertEquals(false, zone5.equals(zone1));
        assertEquals(false, zone5.equals(zone2));
        assertEquals(false, zone5.equals(zone3));
        assertEquals(false, zone5.equals(zone4));
        assertEquals(false, zone1.hashCode() == zone5.hashCode());
        assertEquals(true, zone5.hashCode() == zone5.hashCode());
    }

// org.joda.time.TestDateTimeZone::testToString
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals("Europe/Paris", zone.toString());
        assertEquals("UTC", DateTimeZone.UTC.toString());
    }

// org.joda.time.TestDateTimeZone::testSerialization1
    public void testSerialization1() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone result = (DateTimeZone) ois.readObject();
        ois.close();
        
        assertSame(zone, result);
    }

// org.joda.time.TestDateTimeZone::testSerialization2
    public void testSerialization2() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone result = (DateTimeZone) ois.readObject();
        ois.close();
        
        assertSame(zone, result);
    }

// org.joda.time.TestDateTimeZone::testCommentParse
    public void testCommentParse() throws Exception {
        
        

        DateTimeZone zone = DateTimeZone.forID("Europe/Athens");
        DateTime dt = new DateTime(2005, 5, 5, 20, 10, 15, 0, zone);
        assertEquals(1115313015000L, dt.getMillis());
    }

// org.joda.time.TestDateTimeZone::testPatchedNameKeysLondon
    public void testPatchedNameKeysLondon() throws Exception {
        
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        
        DateTime now = new DateTime(2007, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
    }

// org.joda.time.TestDateTimeZone::testPatchedNameKeysSydney
    public void testPatchedNameKeysSydney() throws Exception {
        
        DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
        
        DateTime now = new DateTime(2007, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
    }

// org.joda.time.TestDateTimeZone::testPatchedNameKeysSydneyHistoric
    public void testPatchedNameKeysSydneyHistoric() throws Exception {
        
        DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
        
        DateTime now = new DateTime(1996, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
    }

// org.joda.time.TestDateTimeZone::testPatchedNameKeysGazaHistoric
    public void testPatchedNameKeysGazaHistoric() throws Exception {
        
        DateTimeZone zone = DateTimeZone.forID("Africa/Johannesburg");
        
        DateTime now = new DateTime(1943, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
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

// org.joda.time.TestDateTime_Constructors::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestDateTime_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new DateTime(2010, 6, 30, 1, 20, ISOChronology.getInstance(DateTimeZone.forOffsetHours(2))), DateTime.parse("2010-06-30T01:20+02:00"));
        assertEquals(new DateTime(2010, 1, 2, 14, 50, ISOChronology.getInstance(LONDON)), DateTime.parse("2010-002T14:50"));
    }

// org.joda.time.TestDateTime_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--dd MM HH").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new DateTime(2010, 6, 30, 13, 0, ISOChronology.getInstance(PARIS)), DateTime.parse("2010--30 06 13", f));
    }

// org.joda.time.TestDateTime_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        DateTime test = new DateTime();
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime((DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        DateTime test = new DateTime(GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        DateTime test = new DateTime((Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        DateTime test = new DateTime(TEST_TIME2);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long1_DateTimeZone
    public void testConstructor_long1_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long2_DateTimeZone
    public void testConstructor_long2_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(TEST_TIME2, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long_nullDateTimeZone
    public void testConstructor_long_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        DateTime test = new DateTime(TEST_TIME2, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_invalidObject
    public void testConstructor_invalidObject() throws Throwable {
        try {
            new DateTime(new Object());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        DateTime test = new DateTime((Object) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_badconverterObject
    public void testConstructor_badconverterObject() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateTime test = new DateTime(new Integer(0));
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        DateTime test = new DateTime("1972-12-03");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString2
    public void testConstructor_ObjectString2() throws Throwable {
        DateTime test = new DateTime("2006-06-03T+14:00");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(2006, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(2, test.getDayOfMonth());  
        assertEquals(11, test.getHourOfDay());  
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString3
    public void testConstructor_ObjectString3() throws Throwable {
        DateTime test = new DateTime("1972-12-03T10:20:30.040");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString4
    public void testConstructor_ObjectString4() throws Throwable {
        DateTime test = new DateTime("2006-06-03T10:20:30.040+14:00");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(2006, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(2, test.getDayOfMonth());  
        assertEquals(21, test.getHourOfDay());  
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString5
    public void testConstructor_ObjectString5() throws Throwable {
        DateTime test = new DateTime("T10:20:30.040");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(1, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString6
    public void testConstructor_ObjectString6() throws Throwable {
        DateTime test = new DateTime("T10:20:30.040+14:00");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1969, test.getYear());  
        assertEquals(12, test.getMonthOfYear());  
        assertEquals(31, test.getDayOfMonth());  
        assertEquals(21, test.getHourOfDay());  
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString7
    public void testConstructor_ObjectString7() throws Throwable {
        DateTime test = new DateTime("10");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(10, test.getYear());
        assertEquals(1, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new DateTime("10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new DateTime("10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_DateTimeZone
    public void testConstructor_Object_DateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_invalidObject_DateTimeZone
    public void testConstructor_invalidObject_DateTimeZone() throws Throwable {
        try {
            new DateTime(new Object(), PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_DateTimeZone
    public void testConstructor_nullObject_DateTimeZone() throws Throwable {
        DateTime test = new DateTime((Object) null, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_nullDateTimeZone
    public void testConstructor_Object_nullDateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_nullDateTimeZone
    public void testConstructor_nullObject_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime((Object) null, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_badconverterObject_DateTimeZone
    public void testConstructor_badconverterObject_DateTimeZone() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateTime test = new DateTime(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_invalidObject_Chronology
    public void testConstructor_invalidObject_Chronology() throws Throwable {
        try {
            new DateTime(new Object(), GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        DateTime test = new DateTime((Object) null, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        DateTime test = new DateTime((Object) null, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_badconverterObject_Chronology
    public void testConstructor_badconverterObject_Chronology() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateTime test = new DateTime(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int
    public void testConstructor_int_int_int_int_int() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_int_int_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 2, 0, PARIS);  
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_int_int_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, (DateTimeZone) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_int_Chronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, GregorianChronology.getInstance());  
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_int_nullChronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, (Chronology) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int
    public void testConstructor_int_int_int_int_int_int() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_int_int_int_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 2, 0, 0, PARIS);  
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_int_int_int_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, (DateTimeZone) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_int_int_Chronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, GregorianChronology.getInstance());  
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_int_int_nullChronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, (Chronology) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int
    public void testConstructor_int_int_int_int_int_int_int() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new DateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 0, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 13, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 0, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 31, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateTime(2002, 7, 31, 0, 0, 0, 0);
        try {
            new DateTime(2002, 7, 32, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_int_int_int_int_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 2, 0, 0, 0, PARIS);  
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new DateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 0, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 13, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 0, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 31, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateTime(2002, 7, 31, 0, 0, 0, 0, PARIS);
        try {
            new DateTime(2002, 7, 32, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_int_int_int_int_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0, (DateTimeZone) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_int_int_int_Chronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0, GregorianChronology.getInstance());  
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new DateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 0, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 13, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 0, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateTime(2002, 7, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
        try {
            new DateTime(2002, 7, 32, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_int_int_int_nullChronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0, (Chronology) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestIllegalFieldValueException::testVerifyValueBounds
    public void testVerifyValueBounds() {
        try {
            FieldUtils.verifyValueBounds(ISOChronology.getInstance().monthOfYear(), -5, 1, 31);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.monthOfYear(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("monthOfYear", e.getFieldName());
            assertEquals(new Integer(-5), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("-5", e.getIllegalValueAsString());
            assertEquals(new Integer(1), e.getLowerBound());
            assertEquals(new Integer(31), e.getUpperBound());
        }

        try {
            FieldUtils.verifyValueBounds(DateTimeFieldType.hourOfDay(), 27, 0, 23);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.hourOfDay(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("hourOfDay", e.getFieldName());
            assertEquals(new Integer(27), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("27", e.getIllegalValueAsString());
            assertEquals(new Integer(0), e.getLowerBound());
            assertEquals(new Integer(23), e.getUpperBound());
        }

        try {
            FieldUtils.verifyValueBounds("foo", 1, 2, 3);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(null, e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("foo", e.getFieldName());
            assertEquals(new Integer(1), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("1", e.getIllegalValueAsString());
            assertEquals(new Integer(2), e.getLowerBound());
            assertEquals(new Integer(3), e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testSkipDateTimeField
    public void testSkipDateTimeField() {
        DateTimeField field = new SkipDateTimeField
            (ISOChronology.getInstanceUTC(), ISOChronology.getInstanceUTC().year(), 1970);
        try {
            field.set(0, 1970);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.year(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("year", e.getFieldName());
            assertEquals(new Integer(1970), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("1970", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testSetText
    public void testSetText() {
        try {
            ISOChronology.getInstanceUTC().year().set(0, null, java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.year(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("year", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("null", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            ISOChronology.getInstanceUTC().year().set(0, "nineteen seventy", java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.year(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("year", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals("nineteen seventy", e.getIllegalStringValue());
            assertEquals("nineteen seventy", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            ISOChronology.getInstanceUTC().era().set(0, "long ago", java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.era(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("era", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals("long ago", e.getIllegalStringValue());
            assertEquals("long ago", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            ISOChronology.getInstanceUTC().monthOfYear().set(0, "spring", java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.monthOfYear(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("monthOfYear", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals("spring", e.getIllegalStringValue());
            assertEquals("spring", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            ISOChronology.getInstanceUTC().dayOfWeek().set(0, "yesterday", java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.dayOfWeek(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("dayOfWeek", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals("yesterday", e.getIllegalStringValue());
            assertEquals("yesterday", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            ISOChronology.getInstanceUTC().halfdayOfDay().set(0, "morning", java.util.Locale.US);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.halfdayOfDay(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("halfdayOfDay", e.getFieldName());
            assertEquals(null, e.getIllegalNumberValue());
            assertEquals("morning", e.getIllegalStringValue());
            assertEquals("morning", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testZoneTransition
    public void testZoneTransition() {
        DateTime dt = new DateTime
            (2005, 4, 3, 1, 0, 0, 0, DateTimeZone.forID("America/Los_Angeles"));
        try {
            dt.hourOfDay().setCopy(2);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.hourOfDay(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("hourOfDay", e.getFieldName());
            assertEquals(new Integer(2), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("2", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testJulianYearZero
    public void testJulianYearZero() {
        DateTime dt = new DateTime(JulianChronology.getInstanceUTC());
        try {
            dt.year().setCopy(0);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.year(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("year", e.getFieldName());
            assertEquals(new Integer(0), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("0", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testGJCutover
    public void testGJCutover() {
        DateTime dt = new DateTime("1582-10-04", GJChronology.getInstanceUTC());
        try {
            dt.dayOfMonth().setCopy(5);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.dayOfMonth(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("dayOfMonth", e.getFieldName());
            assertEquals(new Integer(5), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("5", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        dt = new DateTime("1582-10-15", GJChronology.getInstanceUTC());
        try {
            dt.dayOfMonth().setCopy(14);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.dayOfMonth(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("dayOfMonth", e.getFieldName());
            assertEquals(new Integer(14), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("14", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testReadablePartialValidate
    public void testReadablePartialValidate() {
        try {
            new YearMonthDay(1970, -5, 1);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.monthOfYear(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("monthOfYear", e.getFieldName());
            assertEquals(new Integer(-5), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("-5", e.getIllegalValueAsString());
            assertEquals(new Integer(1), e.getLowerBound());
            assertEquals(null, e.getUpperBound());
        }

        try {
            new YearMonthDay(1970, 500, 1);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.monthOfYear(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("monthOfYear", e.getFieldName());
            assertEquals(new Integer(500), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("500", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(new Integer(12), e.getUpperBound());
        }

        try {
            new YearMonthDay(1970, 2, 30);
            fail();
        } catch (IllegalFieldValueException e) {
            assertEquals(DateTimeFieldType.dayOfMonth(), e.getDateTimeFieldType());
            assertEquals(null, e.getDurationFieldType());
            assertEquals("dayOfMonth", e.getFieldName());
            assertEquals(new Integer(30), e.getIllegalNumberValue());
            assertEquals(null, e.getIllegalStringValue());
            assertEquals("30", e.getIllegalValueAsString());
            assertEquals(null, e.getLowerBound());
            assertEquals(new Integer(28), e.getUpperBound());
        }
    }

// org.joda.time.TestIllegalFieldValueException::testOtherConstructors
    public void testOtherConstructors() {
        IllegalFieldValueException e = new IllegalFieldValueException
            (DurationFieldType.days(), new Integer(1), new Integer(2), new Integer(3));
        assertEquals(null, e.getDateTimeFieldType());
        assertEquals(DurationFieldType.days(), e.getDurationFieldType());
        assertEquals("days", e.getFieldName());
        assertEquals(new Integer(1), e.getIllegalNumberValue());
        assertEquals(null, e.getIllegalStringValue());
        assertEquals("1", e.getIllegalValueAsString());
        assertEquals(new Integer(2), e.getLowerBound());
        assertEquals(new Integer(3), e.getUpperBound());

        e = new IllegalFieldValueException(DurationFieldType.months(), "five");
        assertEquals(null, e.getDateTimeFieldType());
        assertEquals(DurationFieldType.months(), e.getDurationFieldType());
        assertEquals("months", e.getFieldName());
        assertEquals(null, e.getIllegalNumberValue());
        assertEquals("five", e.getIllegalStringValue());
        assertEquals("five", e.getIllegalValueAsString());
        assertEquals(null, e.getLowerBound());
        assertEquals(null, e.getUpperBound());

        e = new IllegalFieldValueException("months", "five");
        assertEquals(null, e.getDateTimeFieldType());
        assertEquals(null, e.getDurationFieldType());
        assertEquals("months", e.getFieldName());
        assertEquals(null, e.getIllegalNumberValue());
        assertEquals("five", e.getIllegalStringValue());
        assertEquals("five", e.getIllegalValueAsString());
        assertEquals(null, e.getLowerBound());
        assertEquals(null, e.getUpperBound());
    }

// org.joda.time.TestInstant_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new DateTime(2010, 6, 30, 0, 20, ISOChronology.getInstance(LONDON)).toInstant(), Instant.parse("2010-06-30T01:20+02:00"));
        assertEquals(new DateTime(2010, 1, 2, 14, 50, ISOChronology.getInstance(LONDON)).toInstant(), Instant.parse("2010-002T14:50"));
    }

// org.joda.time.TestInstant_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--dd MM HH").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new DateTime(2010, 6, 30, 13, 0, ISOChronology.getInstance(PARIS)).toInstant(), Instant.parse("2010--30 06 13", f));
    }

// org.joda.time.TestInstant_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        Instant test = new Instant();
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        Instant test = new Instant(TEST_TIME1);
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        Instant test = new Instant(TEST_TIME2);
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1);
        Instant test = new Instant(date);
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testConstructor_invalidObject
    public void testConstructor_invalidObject() throws Throwable {
        try {
            new Instant(new Object());
            fail();
        } catch (IllegalArgumentException ex) {}
    }
