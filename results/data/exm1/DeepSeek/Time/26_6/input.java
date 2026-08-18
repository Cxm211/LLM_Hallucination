// buggy code
        public long add(long instant, int value) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                long localInstant = iField.add(instant + offset, value);
                return localInstant - offset;
            } else {
               long localInstant = iZone.convertUTCToLocal(instant);
               localInstant = iField.add(localInstant, value);
               return iZone.convertLocalToUTC(localInstant, false);
            }
        }

        public long add(long instant, long value) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                long localInstant = iField.add(instant + offset, value);
                return localInstant - offset;
            } else {
               long localInstant = iZone.convertUTCToLocal(instant);
               localInstant = iField.add(localInstant, value);
               return iZone.convertLocalToUTC(localInstant, false);
            }
        }

        public long addWrapField(long instant, int value) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                long localInstant = iField.addWrapField(instant + offset, value);
                return localInstant - offset;
            } else {
                long localInstant = iZone.convertUTCToLocal(instant);
                localInstant = iField.addWrapField(localInstant, value);
                return iZone.convertLocalToUTC(localInstant, false);
            }
        }

        public long set(long instant, int value) {
            long localInstant = iZone.convertUTCToLocal(instant);
            localInstant = iField.set(localInstant, value);
            long result = iZone.convertLocalToUTC(localInstant, false);
            if (get(result) != value) {
                throw new IllegalFieldValueException(iField.getType(), new Integer(value),
                    "Illegal instant due to time zone offset transition: " +
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").print(new Instant(localInstant)) +
                    " (" + iZone.getID() + ")");
            }
            return result;
        }

        public long set(long instant, String text, Locale locale) {
            // cannot verify that new value stuck because set may be lenient
            long localInstant = iZone.convertUTCToLocal(instant);
            localInstant = iField.set(localInstant, text, locale);
            return iZone.convertLocalToUTC(localInstant, false);
        }

        public long roundFloor(long instant) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                instant = iField.roundFloor(instant + offset);
                return instant - offset;
            } else {
                long localInstant = iZone.convertUTCToLocal(instant);
                localInstant = iField.roundFloor(localInstant);
                return iZone.convertLocalToUTC(localInstant, false);
            }
        }

        public long roundCeiling(long instant) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                instant = iField.roundCeiling(instant + offset);
                return instant - offset;
            } else {
                long localInstant = iZone.convertUTCToLocal(instant);
                localInstant = iField.roundCeiling(localInstant);
                return iZone.convertLocalToUTC(localInstant, false);
            }
        }

    public long convertUTCToLocal(long instantUTC) {
        int offset = getOffset(instantUTC);
        long instantLocal = instantUTC + offset;
        // If there is a sign change, but the two values have the same sign...
        if ((instantUTC ^ instantLocal) < 0 && (instantUTC ^ offset) >= 0) {
            throw new ArithmeticException("Adding time zone offset caused overflow");
        }
        return instantLocal;
    }

    public long set(long instant, int value) {
        // lenient needs to handle time zone chronologies
        // so we do the calculation using local milliseconds
        long localInstant = iBase.getZone().convertUTCToLocal(instant);
        long difference = FieldUtils.safeSubtract(value, get(instant));
        localInstant = getType().getField(iBase.withUTC()).add(localInstant, difference);
        return iBase.getZone().convertLocalToUTC(localInstant, false);
    }

// relevant test
// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RP7
    public void testConstructor_RI_RP7() throws Throwable {
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, 6);
        result = ISOChronology.getInstance().hourOfDay().add(result, 1);
        
        MutableInterval test = new MutableInterval((ReadableInstant) null, dur);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RP8
    public void testConstructor_RI_RP8() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        try {
            new MutableInterval(dt, dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI1
    public void testConstructor_RP_RI1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, -6);
        result = ISOChronology.getInstance().hours().add(result, -1);
        
        MutableInterval test = new MutableInterval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI2
    public void testConstructor_RP_RI2() throws Throwable {
        Instant dt = new Instant(new DateTime(TEST_TIME_NOW));
        Period dur = new Period(0, 6, 0, 3, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstanceUTC().months().add(result, -6);
        result = ISOChronology.getInstanceUTC().days().add(result, -3);
        result = ISOChronology.getInstanceUTC().hours().add(result, -1);
        
        MutableInterval test = new MutableInterval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI3
    public void testConstructor_RP_RI3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, ISOChronology.getInstanceUTC());
        Period dur = new Period(0, 6, 0, 3, 1, 0, 0, 0, PeriodType.standard());
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstanceUTC().months().add(result, -6);
        result = ISOChronology.getInstanceUTC().days().add(result, -3);
        result = ISOChronology.getInstanceUTC().hours().add(result, -1);
        
        MutableInterval test = new MutableInterval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI4
    public void testConstructor_RP_RI4() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(1 * DateTimeConstants.MILLIS_PER_HOUR + 23L);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().hours().add(result, -1);
        result = ISOChronology.getInstance().millis().add(result, -23);
        
        MutableInterval test = new MutableInterval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI5
    public void testConstructor_RP_RI5() throws Throwable {
        MutableInterval test = new MutableInterval((ReadablePeriod) null, (ReadableInstant) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI6
    public void testConstructor_RP_RI6() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        MutableInterval test = new MutableInterval((ReadablePeriod) null, dt);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI7
    public void testConstructor_RP_RI7() throws Throwable {
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, -6);
        result = ISOChronology.getInstance().hourOfDay().add(result, -1);
        
        MutableInterval test = new MutableInterval(dur, (ReadableInstant) null);
        assertEquals(result, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RP_RI8
    public void testConstructor_RP_RI8() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        try {
            new MutableInterval(dur, dt);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RD1
    public void testConstructor_RI_RD1() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, 6);
        result = ISOChronology.getInstance().hours().add(result, 1);
        
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(result - TEST_TIME_NOW);
        
        MutableInterval test = new MutableInterval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RD2
    public void testConstructor_RI_RD2() throws Throwable {
        MutableInterval test = new MutableInterval((ReadableInstant) null, (ReadableDuration) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RD3
    public void testConstructor_RI_RD3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        MutableInterval test = new MutableInterval(dt, (ReadableDuration) null);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RD4
    public void testConstructor_RI_RD4() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, 6);
        result = ISOChronology.getInstance().hourOfDay().add(result, 1);
        
        Duration dur = new Duration(result - TEST_TIME_NOW);
        
        MutableInterval test = new MutableInterval((ReadableInstant) null, dur);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RI_RD5
    public void testConstructor_RI_RD5() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(-1);
        try {
            new MutableInterval(dt, dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RD_RI1
    public void testConstructor_RD_RI1() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, -6);
        result = ISOChronology.getInstance().hours().add(result, -1);
        
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(TEST_TIME_NOW - result);
        
        MutableInterval test = new MutableInterval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RD_RI2
    public void testConstructor_RD_RI2() throws Throwable {
        MutableInterval test = new MutableInterval((ReadableDuration) null, (ReadableInstant) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RD_RI3
    public void testConstructor_RD_RI3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        MutableInterval test = new MutableInterval((ReadableDuration) null, dt);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RD_RI4
    public void testConstructor_RD_RI4() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, -6);
        result = ISOChronology.getInstance().hourOfDay().add(result, -1);
        
        Duration dur = new Duration(TEST_TIME_NOW - result);
        
        MutableInterval test = new MutableInterval(dur, (ReadableInstant) null);
        assertEquals(result, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_RD_RI5
    public void testConstructor_RD_RI5() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(-1);
        try {
            new MutableInterval(dur, dt);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval test = new MutableInterval(dt1.toString() + '/' + dt2.toString());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval base = new MutableInterval(dt1, dt2);
        
        MutableInterval test = new MutableInterval(base);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object3
    public void testConstructor_Object3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval base = new Interval(dt1, dt2);
        
        MutableInterval test = new MutableInterval(base);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object4
    public void testConstructor_Object4() throws Throwable {
        MockInterval base = new MockInterval();
        MutableInterval test = new MutableInterval(base);
        assertEquals(base.getStartMillis(), test.getStartMillis());
        assertEquals(base.getEndMillis(), test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object5
    public void testConstructor_Object5() throws Throwable {
        IntervalConverter oldConv = ConverterManager.getInstance().getIntervalConverter("");
        IntervalConverter conv = new IntervalConverter() {
            public boolean isReadableInterval(Object object, Chronology chrono) {
                return false;
            }
            public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {
                interval.setChronology(chrono);
                interval.setInterval(1234L, 5678L);
            }
            public Class<?> getSupportedType() {
                return String.class;
            }
        };
        try {
            ConverterManager.getInstance().addIntervalConverter(conv);
            DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
            DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
            MutableInterval test = new MutableInterval(dt1.toString() + '/' + dt2.toString());
            assertEquals(1234L, test.getStartMillis());
            assertEquals(5678L, test.getEndMillis());
        } finally {
            ConverterManager.getInstance().addIntervalConverter(oldConv);
        }
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object6
    public void testConstructor_Object6() throws Throwable {
        IntervalConverter oldConv = ConverterManager.getInstance().getIntervalConverter(new MutableInterval(0L, 0L));
        IntervalConverter conv = new IntervalConverter() {
            public boolean isReadableInterval(Object object, Chronology chrono) {
                return false;
            }
            public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {
                interval.setChronology(chrono);
                interval.setInterval(1234L, 5678L);
            }
            public Class<?> getSupportedType() {
                return ReadableInterval.class;
            }
        };
        try {
            ConverterManager.getInstance().addIntervalConverter(conv);
            Interval base = new Interval(-1000L, 1000L);
            MutableInterval test = new MutableInterval(base);
            assertEquals(1234L, test.getStartMillis());
            assertEquals(5678L, test.getEndMillis());
        } finally {
            ConverterManager.getInstance().addIntervalConverter(oldConv);
        }
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object_Chronology1
    public void testConstructor_Object_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval base = new Interval(dt1, dt2);
        
        MutableInterval test = new MutableInterval(base, BuddhistChronology.getInstance());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(BuddhistChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Constructors::testConstructor_Object_Chronology2
    public void testConstructor_Object_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval base = new Interval(dt1, dt2);
        
        MutableInterval test = new MutableInterval(base, null);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestMutableInterval_Updates::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_long_long1
    public void testSetInterval_long_long1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(TEST_TIME1 - 1, TEST_TIME2 + 1);
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_long_long2
    public void testSetInterval_long_long2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setInterval(TEST_TIME1 - 1, TEST_TIME1 - 2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RI_RI1
    public void testSetInterval_RI_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(new Instant(TEST_TIME1 - 1), new Instant(TEST_TIME2 + 1));
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RI_RI2
    public void testSetInterval_RI_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setInterval(new Instant(TEST_TIME1 - 1), new Instant(TEST_TIME1 - 2));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RI_RI3
    public void testSetInterval_RI_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(null, new Instant(TEST_TIME2 + 1));
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RI_RI4
    public void testSetInterval_RI_RI4() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(new Instant(TEST_TIME1 - 1), null);
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RI_RI5
    public void testSetInterval_RI_RI5() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(null, null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RInterval1
    public void testSetInterval_RInterval1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setInterval(new Interval(TEST_TIME1 - 1, TEST_TIME2 + 1));
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RInterval2
    public void testSetInterval_RInterval2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setInterval(new MockBadInterval());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetInterval_RInterval3
    public void testSetInterval_RInterval3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setInterval(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetStartMillis_long1
    public void testSetStartMillis_long1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setStartMillis(TEST_TIME1 - 1);
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetStartMillis_long2
    public void testSetStartMillis_long2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setStartMillis(TEST_TIME2 + 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetStart_RI1
    public void testSetStart_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setStart(new Instant(TEST_TIME1 - 1));
        assertEquals(TEST_TIME1 - 1, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetStart_RI2
    public void testSetStart_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setStart(new Instant(TEST_TIME2 + 1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetStart_RI3
    public void testSetStart_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setStart(null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetEndMillis_long1
    public void testSetEndMillis_long1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setEndMillis(TEST_TIME2 + 1);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetEndMillis_long2
    public void testSetEndMillis_long2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setEndMillis(TEST_TIME1 - 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetEnd_RI1
    public void testSetEnd_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setEnd(new Instant(TEST_TIME2 + 1));
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME2 + 1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetEnd_RI2
    public void testSetEnd_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setEnd(new Instant(TEST_TIME1 - 1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetEnd_RI3
    public void testSetEnd_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setEnd(null);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationAfterStart_long1
    public void testSetDurationAfterStart_long1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationAfterStart(123L);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1 + 123L, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSeDurationAfterStart_long2
    public void testSeDurationAfterStart_long2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setDurationAfterStart(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationAfterStart_RI1
    public void testSetDurationAfterStart_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationAfterStart(new Duration(123L));
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1 + 123L, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSeDurationAfterStart_RI2
    public void testSeDurationAfterStart_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setDurationAfterStart(new Duration(-1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationAfterStart_RI3
    public void testSetDurationAfterStart_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationAfterStart(null);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationBeforeEnd_long1
    public void testSetDurationBeforeEnd_long1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationBeforeEnd(123L);
        assertEquals(TEST_TIME2 - 123L, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSeDurationBeforeEnd_long2
    public void testSeDurationBeforeEnd_long2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setDurationBeforeEnd(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationBeforeEnd_RI1
    public void testSetDurationBeforeEnd_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationBeforeEnd(new Duration(123L));
        assertEquals(TEST_TIME2 - 123L, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSeDurationBeforeEnd_RI2
    public void testSeDurationBeforeEnd_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setDurationBeforeEnd(new Duration(-1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetDurationBeforeEnd_RI3
    public void testSetDurationBeforeEnd_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setDurationBeforeEnd(null);
        assertEquals(TEST_TIME2, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetPeriodAfterStart_RI1
    public void testSetPeriodAfterStart_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setPeriodAfterStart(new Period(123L));
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1 + 123L, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSePeriodAfterStart_RI2
    public void testSePeriodAfterStart_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setPeriodAfterStart(new Period(-1L));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetPeriodAfterStart_RI3
    public void testSetPeriodAfterStart_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setPeriodAfterStart(null);
        assertEquals(TEST_TIME1, test.getStartMillis());
        assertEquals(TEST_TIME1, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSetPeriodBeforeEnd_RI1
    public void testSetPeriodBeforeEnd_RI1() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setPeriodBeforeEnd(new Period(123L));
        assertEquals(TEST_TIME2 - 123L, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutableInterval_Updates::testSePeriodBeforeEnd_RI2
    public void testSePeriodBeforeEnd_RI2() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        try {
            test.setPeriodBeforeEnd(new Period(-1L));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutableInterval_Updates::testSetPeriodBeforeEnd_RI3
    public void testSetPeriodBeforeEnd_RI3() {
        MutableInterval test = new MutableInterval(TEST_TIME1, TEST_TIME2);
        test.setPeriodBeforeEnd(null);
        assertEquals(TEST_TIME2, test.getStartMillis());
        assertEquals(TEST_TIME2, test.getEndMillis());
    }

// org.joda.time.TestMutablePeriod_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutablePeriod_Basics::testGetPeriodType
    public void testGetPeriodType() {
        MutablePeriod test = new MutablePeriod();
        assertEquals(PeriodType.standard(), test.getPeriodType());
    }

// org.joda.time.TestMutablePeriod_Basics::testGetMethods
    public void testGetMethods() {
        MutablePeriod test = new MutablePeriod();
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        MutablePeriod test1 = new MutablePeriod(123L);
        MutablePeriod test2 = new MutablePeriod(123L);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        MutablePeriod test3 = new MutablePeriod(321L);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockMutablePeriod(123L)));
        assertEquals(false, test1.equals(new Period(123L, PeriodType.dayTime())));
    }

// org.joda.time.TestMutablePeriod_Basics::testSerialization
    public void testSerialization() throws Exception {
        MutablePeriod test = new MutablePeriod(123L);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        MutablePeriod result = (MutablePeriod) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestMutablePeriod_Basics::testToString
    public void testToString() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals("P1Y2M3W4DT5H6M7.008S", test.toString());
        
        test = new MutablePeriod(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("PT0S", test.toString());
        
        test = new MutablePeriod(12345L);
        assertEquals("PT12.345S", test.toString());
    }

// org.joda.time.TestMutablePeriod_Basics::testToPeriod
    public void testToPeriod() {
        MutablePeriod test = new MutablePeriod(123L);
        Period result = test.toPeriod();
        assertEquals(test, result);
    }

// org.joda.time.TestMutablePeriod_Basics::testToMutablePeriod
    public void testToMutablePeriod() {
        MutablePeriod test = new MutablePeriod(123L);
        MutablePeriod result = test.toMutablePeriod();
        assertEquals(test, result);
    }

// org.joda.time.TestMutablePeriod_Basics::testToDurationFrom
    public void testToDurationFrom() {
        MutablePeriod test = new MutablePeriod(123L);
        assertEquals(new Duration(123L), test.toDurationFrom(new Instant(0L)));
    }

// org.joda.time.TestMutablePeriod_Basics::testCopy
    public void testCopy() {
        MutablePeriod test = new MutablePeriod(123L);
        MutablePeriod copy = test.copy();
        assertEquals(test.getPeriodType(), copy.getPeriodType());
        assertEquals(test, copy);
    }

// org.joda.time.TestMutablePeriod_Basics::testClone
    public void testClone() {
        MutablePeriod test = new MutablePeriod(123L);
        MutablePeriod copy = (MutablePeriod) test.clone();
        assertEquals(test.getPeriodType(), copy.getPeriodType());
        assertEquals(test, copy);
    }

// org.joda.time.TestMutablePeriod_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 890), MutablePeriod.parse("P1Y2M3W4DT5H6M7.890S"));
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor1
    public void testConstructor1() throws Throwable {
        MutablePeriod test = new MutablePeriod();
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_PeriodType1
    public void testConstructor_PeriodType1() throws Throwable {
        MutablePeriod test = new MutablePeriod(PeriodType.yearMonthDayTime());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_PeriodType2
    public void testConstructor_PeriodType2() throws Throwable {
        MutablePeriod test = new MutablePeriod((PeriodType) null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        long length =
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long3
    public void testConstructor_long3() throws Throwable {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        MutablePeriod test = new MutablePeriod(length);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType1
    public void testConstructor_long_PeriodType1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, (PeriodType) null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType2
    public void testConstructor_long_PeriodType2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.millis());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType3
    public void testConstructor_long_PeriodType3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.standard());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType4
    public void testConstructor_long_PeriodType4() throws Throwable {
        long length =
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.standard().withMillisRemoved());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_Chronology1
    public void testConstructor_long_Chronology1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, ISOChronology.getInstance());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_Chronology2
    public void testConstructor_long_Chronology2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, ISOChronology.getInstanceUTC());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_Chronology3
    public void testConstructor_long_Chronology3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, (Chronology) null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType_Chronology1
    public void testConstructor_long_PeriodType_Chronology1() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.time().withMillisRemoved(), ISOChronology.getInstance());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType_Chronology2
    public void testConstructor_long_PeriodType_Chronology2() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.standard(), ISOChronology.getInstanceUTC());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType_Chronology3
    public void testConstructor_long_PeriodType_Chronology3() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, PeriodType.standard(), (Chronology) null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_PeriodType_Chronology4
    public void testConstructor_long_PeriodType_Chronology4() throws Throwable {
        long length = 4 * DateTimeConstants.MILLIS_PER_DAY +
                5 * DateTimeConstants.MILLIS_PER_HOUR +
                6 * DateTimeConstants.MILLIS_PER_MINUTE +
                7 * DateTimeConstants.MILLIS_PER_SECOND + 8;
        MutablePeriod test = new MutablePeriod(length, (PeriodType) null, (Chronology) null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_4int1
    public void testConstructor_4int1() throws Throwable {
        MutablePeriod test = new MutablePeriod(5, 6, 7, 8);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_8int1
    public void testConstructor_8int1() throws Throwable {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_8int__PeriodType1
    public void testConstructor_8int__PeriodType1() throws Throwable {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8, null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_8int__PeriodType2
    public void testConstructor_8int__PeriodType2() throws Throwable {
        MutablePeriod test = new MutablePeriod(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.dayTime());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_8int__PeriodType3
    public void testConstructor_8int__PeriodType3() throws Throwable {
        try {
            new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.dayTime());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long1
    public void testConstructor_long_long1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long2
    public void testConstructor_long_long2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_PeriodType1
    public void testConstructor_long_long_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), (PeriodType) null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_PeriodType2
    public void testConstructor_long_long_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), PeriodType.dayTime());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_PeriodType3
    public void testConstructor_long_long_PeriodType3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 6, 9, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), PeriodType.standard().withMillisRemoved());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_Chronology1
    public void testConstructor_long_long_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, CopticChronology.getInstance());
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, CopticChronology.getInstance());
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), CopticChronology.getInstance());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_Chronology2
    public void testConstructor_long_long_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), (Chronology) null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_PeriodType_Chronology1
    public void testConstructor_long_long_PeriodType_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, CopticChronology.getInstance());
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, CopticChronology.getInstance());
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), (PeriodType) null, CopticChronology.getInstance());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_long_long_PeriodType_Chronology2
    public void testConstructor_long_long_PeriodType_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1.getMillis(), dt2.getMillis(), (PeriodType) null, null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI1
    public void testConstructor_RI_RI1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI2
    public void testConstructor_RI_RI2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI3
    public void testConstructor_RI_RI3() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI4
    public void testConstructor_RI_RI4() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        DateTime dt2 = null;  
        MutablePeriod test = new MutablePeriod(dt1, dt2);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI5
    public void testConstructor_RI_RI5() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = null;  
        MutablePeriod test = new MutablePeriod(dt1, dt2);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI_PeriodType1
    public void testConstructor_RI_RI_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2, null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI_PeriodType2
    public void testConstructor_RI_RI_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 7, 10, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2, PeriodType.dayTime());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI_PeriodType3
    public void testConstructor_RI_RI_PeriodType3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2004, 6, 9, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2, PeriodType.standard().withMillisRemoved());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI_PeriodType4
    public void testConstructor_RI_RI_PeriodType4() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = new DateTime(2005, 7, 17, 1, 1, 1, 1);
        MutablePeriod test = new MutablePeriod(dt1, dt2, PeriodType.standard());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RI_PeriodType5
    public void testConstructor_RI_RI_PeriodType5() throws Throwable {
        DateTime dt1 = null;  
        DateTime dt2 = null;  
        MutablePeriod test = new MutablePeriod(dt1, dt2, PeriodType.standard());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RD1
    public void testConstructor_RI_RD1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        MutablePeriod test = new MutablePeriod(dt1, dur);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RD2
    public void testConstructor_RI_RD2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        MutablePeriod test = new MutablePeriod(dt1, dur);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RD_PeriodType1
    public void testConstructor_RI_RD_PeriodType1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Duration dur = new Interval(dt1, dt2).toDuration();
        MutablePeriod test = new MutablePeriod(dt1, dur, PeriodType.yearDayTime());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_RI_RD_PeriodType2
    public void testConstructor_RI_RD_PeriodType2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Duration dur = null;
        MutablePeriod test = new MutablePeriod(dt1, dur, (PeriodType) null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        MutablePeriod test = new MutablePeriod("P1Y2M3D");
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        MutablePeriod test = new MutablePeriod((Object) null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object3
    public void testConstructor_Object3() throws Throwable {
        MutablePeriod test = new MutablePeriod(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()));
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object4
    public void testConstructor_Object4() throws Throwable {
        Period base = new Period(1, 1, 0, 1, 1, 1, 1, 1, PeriodType.standard());
        MutablePeriod test = new MutablePeriod(base);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_PeriodType1
    public void testConstructor_Object_PeriodType1() throws Throwable {
        MutablePeriod test = new MutablePeriod("P1Y2M3D", PeriodType.yearMonthDayTime());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_PeriodType2
    public void testConstructor_Object_PeriodType2() throws Throwable {
        MutablePeriod test = new MutablePeriod((Object) null, PeriodType.yearMonthDayTime());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_PeriodType3
    public void testConstructor_Object_PeriodType3() throws Throwable {
        MutablePeriod test = new MutablePeriod(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()), PeriodType.yearMonthDayTime());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_PeriodType4
    public void testConstructor_Object_PeriodType4() throws Throwable {
        MutablePeriod test = new MutablePeriod(new Period(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.dayTime()), (PeriodType) null);
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_Chronology1
    public void testConstructor_Object_Chronology1() throws Throwable {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        MutablePeriod test = new MutablePeriod(new Duration(length), ISOChronology.getInstance());
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

// org.joda.time.TestMutablePeriod_Constructors::testConstructor_Object_Chronology2
    public void testConstructor_Object_Chronology2() throws Throwable {
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        MutablePeriod test = new MutablePeriod(new Duration(length), ISOChronology.getInstanceUTC());
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(64, test.getWeeks());
        assertEquals(2, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestMutablePeriod_Updates::testClear
    public void testClear() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.clear();
        assertEquals(new MutablePeriod(), test);
        
        test = new MutablePeriod(1, 2, 0, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        test.clear();
        assertEquals(new MutablePeriod(PeriodType.yearMonthDayTime()), test);
    }

// org.joda.time.TestMutablePeriod_Updates::testAddYears
    public void testAddYears() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addYears(10);
        assertEquals(11, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addYears(-10);
        assertEquals(-9, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addYears(0);
        assertEquals(1, test.getYears());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddMonths
    public void testAddMonths() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMonths(10);
        assertEquals(12, test.getMonths());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMonths(-10);
        assertEquals(-8, test.getMonths());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMonths(0);
        assertEquals(2, test.getMonths());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddWeeks
    public void testAddWeeks() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addWeeks(10);
        assertEquals(13, test.getWeeks());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addWeeks(-10);
        assertEquals(-7, test.getWeeks());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addWeeks(0);
        assertEquals(3, test.getWeeks());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddDays
    public void testAddDays() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addDays(10);
        assertEquals(14, test.getDays());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addDays(-10);
        assertEquals(-6, test.getDays());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addDays(0);
        assertEquals(4, test.getDays());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddHours
    public void testAddHours() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addHours(10);
        assertEquals(15, test.getHours());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addHours(-10);
        assertEquals(-5, test.getHours());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addHours(0);
        assertEquals(5, test.getHours());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddMinutes
    public void testAddMinutes() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMinutes(10);
        assertEquals(16, test.getMinutes());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMinutes(-10);
        assertEquals(-4, test.getMinutes());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMinutes(0);
        assertEquals(6, test.getMinutes());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddSeconds
    public void testAddSeconds() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addSeconds(10);
        assertEquals(17, test.getSeconds());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addSeconds(-10);
        assertEquals(-3, test.getSeconds());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addSeconds(0);
        assertEquals(7, test.getSeconds());
    }

// org.joda.time.TestMutablePeriod_Updates::testAddMillis
    public void testAddMillis() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMillis(10);
        assertEquals(18, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMillis(-10);
        assertEquals(-2, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.addMillis(0);
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetYears
    public void testSetYears() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setYears(10);
        assertEquals(10, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setYears(-10);
        assertEquals(-10, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setYears(0);
        assertEquals(0, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setYears(1);
        assertEquals(1, test.getYears());
        
        test = new MutablePeriod(0, 0, 0, 0, 0, 0, 0, 1, PeriodType.millis());
        try {
            test.setYears(1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutablePeriod_Updates::testSetMonths
    public void testSetMonths() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMonths(10);
        assertEquals(10, test.getMonths());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMonths(-10);
        assertEquals(-10, test.getMonths());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMonths(0);
        assertEquals(0, test.getMonths());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMonths(2);
        assertEquals(2, test.getMonths());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetWeeks
    public void testSetWeeks() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setWeeks(10);
        assertEquals(10, test.getWeeks());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setWeeks(-10);
        assertEquals(-10, test.getWeeks());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setWeeks(0);
        assertEquals(0, test.getWeeks());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setWeeks(3);
        assertEquals(3, test.getWeeks());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetDays
    public void testSetDays() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setDays(10);
        assertEquals(10, test.getDays());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setDays(-10);
        assertEquals(-10, test.getDays());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setDays(0);
        assertEquals(0, test.getDays());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setDays(4);
        assertEquals(4, test.getDays());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetHours
    public void testSetHours() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setHours(10);
        assertEquals(10, test.getHours());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setHours(-10);
        assertEquals(-10, test.getHours());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setHours(0);
        assertEquals(0, test.getHours());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setHours(5);
        assertEquals(5, test.getHours());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetMinutes
    public void testSetMinutes() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMinutes(10);
        assertEquals(10, test.getMinutes());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMinutes(-10);
        assertEquals(-10, test.getMinutes());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMinutes(0);
        assertEquals(0, test.getMinutes());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMinutes(6);
        assertEquals(6, test.getMinutes());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetSeconds
    public void testSetSeconds() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setSeconds(10);
        assertEquals(10, test.getSeconds());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setSeconds(-10);
        assertEquals(-10, test.getSeconds());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setSeconds(0);
        assertEquals(0, test.getSeconds());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setSeconds(7);
        assertEquals(7, test.getSeconds());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetMillis
    public void testSetMillis() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMillis(10);
        assertEquals(10, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMillis(-10);
        assertEquals(-10, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMillis(0);
        assertEquals(0, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setMillis(8);
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSet_Field
    public void testSet_Field() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.set(DurationFieldType.years(), 10);
        assertEquals(10, test.getYears());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test.set(null, 10);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_Field
    public void testAdd_Field() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.add(DurationFieldType.years(), 10);
        assertEquals(11, test.getYears());
        
        test = new MutablePeriod(0, 0, 0, 0, 0, 0, 0, 1, PeriodType.millis());
        test.add(DurationFieldType.years(), 0);
        assertEquals(0, test.getYears());
        assertEquals(1, test.getMillis());
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test.add(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            test.add(null, 10);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_8ints1
    public void testSetPeriod_8ints1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod(11, 12, 13, 14, 15, 16, 17, 18);
        assertEquals(11, test.getYears());
        assertEquals(12, test.getMonths());
        assertEquals(13, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_8ints2
    public void testSetPeriod_8ints2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        try {
            test.setPeriod(11, 12, 13, 14, 15, 16, 17, 18);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_8ints3
    public void testSetPeriod_8ints3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        test.setPeriod(0, 0, 0, 0, 0, 0, 0, 18);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_8ints4
    public void testSetPeriod_8ints4() {
        MutablePeriod test = new MutablePeriod(0, 0, 0, 0, 5, 6, 7, 8);
        test.setPeriod(11, 12, 13, 14, 15, 16, 17, 18);
        assertEquals(11, test.getYears());
        assertEquals(12, test.getMonths());
        assertEquals(13, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RP1
    public void testSetPeriod_RP1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod(new MutablePeriod(11, 12, 13, 14, 15, 16, 17, 18));
        assertEquals(11, test.getYears());
        assertEquals(12, test.getMonths());
        assertEquals(13, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RP2
    public void testSetPeriod_RP2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        try {
            test.setPeriod(new MutablePeriod(11, 12, 13, 14, 15, 16, 17, 18));
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RP3
    public void testSetPeriod_RP3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        test.setPeriod(new MutablePeriod(0, 0, 0, 0, 0, 0, 0, 18));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RP4
    public void testSetPeriod_RP4() {
        MutablePeriod test = new MutablePeriod(0, 0, 0, 0, 5, 6, 7, 8);
        test.setPeriod(new MutablePeriod(11, 12, 13, 14, 15, 16, 17, 18));
        assertEquals(11, test.getYears());
        assertEquals(12, test.getMonths());
        assertEquals(13, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RP5
    public void testSetPeriod_RP5() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod((ReadablePeriod) null);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long1
    public void testSetPeriod_long_long1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long2
    public void testSetPeriod_long_long2() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt2.getMillis(), dt1.getMillis());
        assertEquals(-1, test.getYears());
        assertEquals(-1, test.getMonths());
        assertEquals(-1, test.getWeeks());
        assertEquals(-1, test.getDays());
        assertEquals(-1, test.getHours());
        assertEquals(-1, test.getMinutes());
        assertEquals(-1, test.getSeconds());
        assertEquals(-1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long3
    public void testSetPeriod_long_long3() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        test.setPeriod(dt1.getMillis(), dt1.getMillis());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoYears
    public void testSetPeriod_long_long_NoYears() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withYearsRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(0, test.getYears());
        assertEquals(13, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoMonths
    public void testSetPeriod_long_long_NoMonths() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withMonthsRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(5, test.getWeeks());
        assertEquals(3, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoWeeks
    public void testSetPeriod_long_long_NoWeeks() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withWeeksRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(8, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoDays
    public void testSetPeriod_long_long_NoDays() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withDaysRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(25, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoHours
    public void testSetPeriod_long_long_NoHours() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withHoursRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(61, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoMinutes
    public void testSetPeriod_long_long_NoMinutes() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withMinutesRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(61, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoSeconds
    public void testSetPeriod_long_long_NoSeconds() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withSecondsRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(1001, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long_long_NoMillis
    public void testSetPeriod_long_long_NoMillis() {
        MutablePeriod test = new MutablePeriod(PeriodType.standard().withMillisRemoved());
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1.getMillis(), dt2.getMillis());
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RI_RI1
    public void testSetPeriod_RI_RI1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt1, dt2);
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RI_RI2
    public void testSetPeriod_RI_RI2() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(dt2, dt1);
        assertEquals(-1, test.getYears());
        assertEquals(-1, test.getMonths());
        assertEquals(-1, test.getWeeks());
        assertEquals(-1, test.getDays());
        assertEquals(-1, test.getHours());
        assertEquals(-1, test.getMinutes());
        assertEquals(-1, test.getSeconds());
        assertEquals(-1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RI_RI3
    public void testSetPeriod_RI_RI3() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        test.setPeriod(dt1, dt1);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RInterval1
    public void testSetPeriod_RInterval1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DateTime dt1 = new DateTime(2002, 6, 9, 13, 15, 17, 19);
        DateTime dt2 = new DateTime(2003, 7, 17, 14, 16, 18, 20);
        test.setPeriod(new Interval(dt1, dt2));
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(1, test.getWeeks());
        assertEquals(1, test.getDays());
        assertEquals(1, test.getHours());
        assertEquals(1, test.getMinutes());
        assertEquals(1, test.getSeconds());
        assertEquals(1, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RInterval2
    public void testSetPeriod_RInterval2() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod((ReadableInterval) null);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long1
    public void testSetPeriod_long1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod(100L);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_long2
    public void testSetPeriod_long2() {
        MutablePeriod test = new MutablePeriod();
        test.setPeriod(
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L);
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RD1
    public void testSetPeriod_RD1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod(new Duration(100L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RD2
    public void testSetPeriod_RD2() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        long length =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        test.setPeriod(new Duration(length));
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testSetPeriod_RD3
    public void testSetPeriod_RD3() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.setPeriod((ReadableDuration) null);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_8ints1
    public void testAdd_8ints1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_8ints2
    public void testAdd_8ints2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.yearMonthDayTime());
        try {
            test.add(1, 2, 3, 4, 5, 6, 7, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long1
    public void testAdd_long1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(100L);
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(200, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long2
    public void testAdd_long2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.standard());
        long ms =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        test.add(ms);
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long3
    public void testAdd_long3() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.add(2100L);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(9, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long_Chronology1
    public void testAdd_long_Chronology1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(100L, ISOChronology.getInstance());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(200, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long_Chronology2
    public void testAdd_long_Chronology2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.standard());
        long ms =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        test.add(ms, ISOChronology.getInstance());
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((450 * 24) + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_long_Chronology3
    public void testAdd_long_Chronology3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.standard());
        long ms =
            (4L + (3L * 7L) + (2L * 30L) + 365L) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        test.add(ms, ISOChronology.getInstanceUTC());
        
        assertEquals(0, test.getYears());  
        assertEquals(0, test.getMonths());
        assertEquals(64, test.getWeeks());
        assertEquals(2, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RD1
    public void testAdd_RD1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(new Duration(100L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(200, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RD2
    public void testAdd_RD2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.yearMonthDayTime());
        long ms =
            (4L + (3L * 7L)) * DateTimeConstants.MILLIS_PER_DAY +
            5L * DateTimeConstants.MILLIS_PER_HOUR +
            6L * DateTimeConstants.MILLIS_PER_MINUTE +
            7L * DateTimeConstants.MILLIS_PER_SECOND + 8L;
        test.add(new Duration(ms));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals((4 + (3 * 7)) * 24 + 5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RD3
    public void testAdd_RD3() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.add((ReadableDuration) null);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP1
    public void testAdd_RP1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(new Period(100L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(200, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP2
    public void testAdd_RP2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.standard());  
        test.add(new Period(1, 2, 3, 4, 5, 6, 7, 0, PeriodType.standard().withMillisRemoved()));
        
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP3
    public void testAdd_RP3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.standard());
        test.add(new Period(0L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP4
    public void testAdd_RP4() {
        MutablePeriod test = new MutablePeriod(1, 2, 0, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        try {
            test.add(new Period(1, 2, 3, 4, 5, 6, 7, 8));  
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP5
    public void testAdd_RP5() {
        MutablePeriod test = new MutablePeriod(1, 2, 0, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        test.add(new Period(1, 2, 0, 4, 5, 6, 7, 8));  
        assertEquals(2, test.getYears());
        assertEquals(4, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(8, test.getDays());
        assertEquals(10, test.getHours());
        assertEquals(12, test.getMinutes());
        assertEquals(14, test.getSeconds());
        assertEquals(16, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RP6
    public void testAdd_RP6() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.add((ReadablePeriod) null);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RInterval1
    public void testAdd_RInterval1() {
        MutablePeriod test = new MutablePeriod(100L);
        test.add(new Interval(100L, 200L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(200, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RInterval2
    public void testAdd_RInterval2() {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 12, 18, 0, 0, 0, 8);
        MutablePeriod test = new MutablePeriod(100L);  
        test.add(new Interval(dt1, dt2));
        assertEquals(1, test.getYears());  
        assertEquals(6, test.getMonths());  
        assertEquals(1, test.getWeeks());  
        assertEquals(2, test.getDays());  
        assertEquals(0, test.getHours());  
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RInterval3
    public void testAdd_RInterval3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.yearMonthDayTime());
        test.add(new Interval(0L, 0L));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RInterval4
    public void testAdd_RInterval4() {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 17, 0, 0, 0, 8);
        MutablePeriod test = new MutablePeriod(100L, PeriodType.yearMonthDayTime());
        test.add(new Interval(dt1, dt2));
        assertEquals(1, test.getYears());
        assertEquals(1, test.getMonths());
        assertEquals(0, test.getWeeks());  
        assertEquals(8, test.getDays());  
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(108, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testAdd_RInterval5
    public void testAdd_RInterval5() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.add((ReadableInterval) null);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testMergePeriod_RP1
    public void testMergePeriod_RP1() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.mergePeriod(new MutablePeriod(0, 0, 0, 14, 15, 16, 17, 18, PeriodType.dayTime()));
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testMergePeriod_RP2
    public void testMergePeriod_RP2() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        try {
            test.mergePeriod(new MutablePeriod(11, 12, 13, 14, 15, 16, 17, 18));
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(100, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testMergePeriod_RP3
    public void testMergePeriod_RP3() {
        MutablePeriod test = new MutablePeriod(100L, PeriodType.millis());
        test.mergePeriod(new MutablePeriod(0, 0, 0, 0, 0, 0, 0, 18));
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testMergePeriod_RP4
    public void testMergePeriod_RP4() {
        MutablePeriod test = new MutablePeriod(0, 0, 0, 0, 5, 6, 7, 8);
        test.mergePeriod(new MutablePeriod(11, 12, 13, 14, 15, 16, 17, 18));
        assertEquals(11, test.getYears());
        assertEquals(12, test.getMonths());
        assertEquals(13, test.getWeeks());
        assertEquals(14, test.getDays());
        assertEquals(15, test.getHours());
        assertEquals(16, test.getMinutes());
        assertEquals(17, test.getSeconds());
        assertEquals(18, test.getMillis());
    }

// org.joda.time.TestMutablePeriod_Updates::testMergePeriod_RP5
    public void testMergePeriod_RP5() {
        MutablePeriod test = new MutablePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        test.mergePeriod((ReadablePeriod) null);
        assertEquals(1, test.getYears());
        assertEquals(2, test.getMonths());
        assertEquals(3, test.getWeeks());
        assertEquals(4, test.getDays());
        assertEquals(5, test.getHours());
        assertEquals(6, test.getMinutes());
        assertEquals(7, test.getSeconds());
        assertEquals(8, test.getMillis());
    }

// org.joda.time.TestPartial_Basics::testGet
    public void testGet() {
        Partial test = createHourMinPartial();
        assertEquals(10, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, test.get(DateTimeFieldType.minuteOfHour()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.get(DateTimeFieldType.secondOfMinute());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Basics::testSize
    public void testSize() {
        Partial test = createHourMinPartial();
        assertEquals(2, test.size());
    }

// org.joda.time.TestPartial_Basics::testGetFieldType
    public void testGetFieldType() {
        Partial test = createHourMinPartial();
        assertSame(DateTimeFieldType.hourOfDay(), test.getFieldType(0));
        assertSame(DateTimeFieldType.minuteOfHour(), test.getFieldType(1));
        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestPartial_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        Partial test = createHourMinPartial();
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertEquals(2, fields.length);
        assertSame(DateTimeFieldType.hourOfDay(), fields[0]);
        assertSame(DateTimeFieldType.minuteOfHour(), fields[1]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestPartial_Basics::testGetField
    public void testGetField() {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        assertSame(CopticChronology.getInstanceUTC().hourOfDay(), test.getField(0));
        assertSame(CopticChronology.getInstanceUTC().minuteOfHour(), test.getField(1));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(5);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestPartial_Basics::testGetFields
    public void testGetFields() {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertEquals(2, fields.length);
        assertSame(CopticChronology.getInstanceUTC().hourOfDay(), fields[0]);
        assertSame(CopticChronology.getInstanceUTC().minuteOfHour(), fields[1]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestPartial_Basics::testGetValue
    public void testGetValue() {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        assertEquals(10, test.getValue(0));
        assertEquals(20, test.getValue(1));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(2);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestPartial_Basics::testGetValues
    public void testGetValues() {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        int[] values = test.getValues();
        assertEquals(2, values.length);
        assertEquals(10, values[0]);
        assertEquals(20, values[1]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestPartial_Basics::testIsSupported
    public void testIsSupported() {
        Partial test = createHourMinPartial(COPTIC_PARIS);
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(false, test.isSupported(DateTimeFieldType.secondOfMinute()));
        assertEquals(false, test.isSupported(DateTimeFieldType.millisOfSecond()));
        assertEquals(false, test.isSupported(DateTimeFieldType.dayOfMonth()));
    }

// org.joda.time.TestPartial_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        Partial test1 = createHourMinPartial(COPTIC_PARIS);
        Partial test2 = createHourMinPartial(COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        Partial test3 = createHourMinPartial2(COPTIC_PARIS);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
        assertEquals(new TimeOfDay(10, 20, 30, 40), createTODPartial(ISO_UTC));
    }

// org.joda.time.TestPartial_Basics::testCompareTo
    public void testCompareTo() {
        Partial test1 = createHourMinPartial();
        Partial test1a = createHourMinPartial();
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        Partial test2 = createHourMinPartial2(ISO_UTC);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        Partial test3 = createHourMinPartial2(COPTIC_UTC);
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        assertEquals(0, new TimeOfDay(10, 20, 30, 40).compareTo(createTODPartial(ISO_UTC)));
        
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

        try {
            test1.compareTo(new YearMonthDay());
            fail();
        } catch (ClassCastException ex) {}
        try {
            createTODPartial(ISO_UTC).without(DateTimeFieldType.hourOfDay()).compareTo(new YearMonthDay());
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestPartial_Basics::testIsEqual_TOD
    public void testIsEqual_TOD() {
        Partial test1 = createHourMinPartial();
        Partial test1a = createHourMinPartial();
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        Partial test2 = createHourMinPartial2(ISO_UTC);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        Partial test3 = createHourMinPartial2(COPTIC_UTC);
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            createHourMinPartial().isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Basics::testIsBefore_TOD
    public void testIsBefore_TOD() {
        Partial test1 = createHourMinPartial();
        Partial test1a = createHourMinPartial();
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        Partial test2 = createHourMinPartial2(ISO_UTC);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        Partial test3 = createHourMinPartial2(COPTIC_UTC);
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            createHourMinPartial().isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Basics::testIsAfter_TOD
    public void testIsAfter_TOD() {
        Partial test1 = createHourMinPartial();
        Partial test1a = createHourMinPartial();
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        Partial test2 = createHourMinPartial2(ISO_UTC);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        Partial test3 = createHourMinPartial2(COPTIC_UTC);
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            createHourMinPartial().isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestPartial_Basics::testWithChronologyRetainFields_Chrono
    public void testWithChronologyRetainFields_Chrono() {
        Partial base = createHourMinPartial(COPTIC_PARIS);
        Partial test = base.withChronologyRetainFields(BUDDHIST_TOKYO);
        check(base, 10, 20);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 10, 20);
        assertEquals(BUDDHIST_UTC, test.getChronology());
    }

// org.joda.time.TestPartial_Basics::testWithChronologyRetainFields_sameChrono
    public void testWithChronologyRetainFields_sameChrono() {
        Partial base = createHourMinPartial(COPTIC_PARIS);
        Partial test = base.withChronologyRetainFields(COPTIC_TOKYO);
        assertSame(base, test);
    }

// org.joda.time.TestPartial_Basics::testWithChronologyRetainFields_nullChrono
    public void testWithChronologyRetainFields_nullChrono() {
        Partial base = createHourMinPartial(COPTIC_PARIS);
        Partial test = base.withChronologyRetainFields(null);
        check(base, 10, 20);
        assertEquals(COPTIC_UTC, base.getChronology());
        check(test, 10, 20);
        assertEquals(ISO_UTC, test.getChronology());
    }

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
