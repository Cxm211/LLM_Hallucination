// buggy code
    public int parseInto(ReadWritableInstant instant, String text, int position) {
        DateTimeParser parser = requireParser();
        if (instant == null) {
            throw new IllegalArgumentException("Instant must not be null");
        }
        
        long instantMillis = instant.getMillis();
        Chronology chrono = instant.getChronology();
        long instantLocal = instantMillis + chrono.getZone().getOffset(instantMillis);
        chrono = selectChronology(chrono);
        
        DateTimeParserBucket bucket = new DateTimeParserBucket(
            instantLocal, chrono, iLocale, iPivotYear, iDefaultYear);
        int newPos = parser.parseInto(bucket, text, position);
        instant.setMillis(bucket.computeMillis(false, text));
        if (iOffsetParsed && bucket.getOffsetInteger() != null) {
            int parsedOffset = bucket.getOffsetInteger();
            DateTimeZone parsedZone = DateTimeZone.forOffsetMillis(parsedOffset);
            chrono = chrono.withZone(parsedZone);
        } else if (bucket.getZone() != null) {
            chrono = chrono.withZone(bucket.getZone());
        }
        instant.setChronology(chrono);
        if (iZone != null) {
            instant.setZone(iZone);
        }
        return newPos;
    }

// relevant test
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

// org.joda.time.TestInstant_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestInstant_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        Instant test = new Instant();  
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
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));  
        assertEquals(24, test.get(DateTimeFieldType.clockhourOfDay()));  
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));  
        assertEquals(0, test.get(DateTimeFieldType.hourOfDay()));  
        assertEquals(0, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(0, test.get(DateTimeFieldType.minuteOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(0, test.get(DateTimeFieldType.secondOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.millisOfSecond()));
        assertEquals(0, test.get(DateTimeFieldType.millisOfDay()));
        try {
            test.get((DateTimeFieldType) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInstant_Basics::testGet_DateTimeField
    public void testGet_DateTimeField() {
        Instant test = new Instant();  
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

// org.joda.time.TestInstant_Basics::testGetMethods
    public void testGetMethods() {
        Instant test = new Instant();
        
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(DateTimeZone.UTC, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestInstant_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        Instant test1 = new Instant(TEST_TIME1);
        Instant test2 = new Instant(TEST_TIME1);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        Instant test3 = new Instant(TEST_TIME2);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(false, test1.equals(new DateTime(TEST_TIME1)));
    }

// org.joda.time.TestInstant_Basics::testCompareTo
    public void testCompareTo() {
        Instant test1 = new Instant(TEST_TIME1);
        Instant test1a = new Instant(TEST_TIME1);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        Instant test2 = new Instant(TEST_TIME2);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GregorianChronology.getInstance(PARIS));
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

// org.joda.time.TestInstant_Basics::testIsEqual_long
    public void testIsEqual_long() {
        assertEquals(false, new Instant(TEST_TIME1).isEqual(TEST_TIME2));
        assertEquals(true, new Instant(TEST_TIME1).isEqual(TEST_TIME1));
        assertEquals(false, new Instant(TEST_TIME2).isEqual(TEST_TIME1));
    }

// org.joda.time.TestInstant_Basics::testIsEqualNow
    public void testIsEqualNow() {
        assertEquals(false, new Instant(TEST_TIME_NOW - 1).isEqualNow());
        assertEquals(true, new Instant(TEST_TIME_NOW).isEqualNow());
        assertEquals(false, new Instant(TEST_TIME_NOW + 1).isEqualNow());
    }

// org.joda.time.TestInstant_Basics::testIsEqual_RI
    public void testIsEqual_RI() {
        Instant test1 = new Instant(TEST_TIME1);
        Instant test1a = new Instant(TEST_TIME1);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        Instant test2 = new Instant(TEST_TIME2);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GregorianChronology.getInstance(PARIS));
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        assertEquals(false, test2.isEqual(new MockInstant()));
        assertEquals(true, test1.isEqual(new MockInstant()));
        
        assertEquals(false, new Instant(TEST_TIME_NOW + 1).isEqual(null));
        assertEquals(true, new Instant(TEST_TIME_NOW).isEqual(null));
        assertEquals(false, new Instant(TEST_TIME_NOW - 1).isEqual(null));
    }

// org.joda.time.TestInstant_Basics::testIsBefore_long
    public void testIsBefore_long() {
        assertEquals(true, new Instant(TEST_TIME1).isBefore(TEST_TIME2));
        assertEquals(false, new Instant(TEST_TIME1).isBefore(TEST_TIME1));
        assertEquals(false, new Instant(TEST_TIME2).isBefore(TEST_TIME1));
    }

// org.joda.time.TestInstant_Basics::testIsBeforeNow
    public void testIsBeforeNow() {
        assertEquals(true, new Instant(TEST_TIME_NOW - 1).isBeforeNow());
        assertEquals(false, new Instant(TEST_TIME_NOW).isBeforeNow());
        assertEquals(false, new Instant(TEST_TIME_NOW + 1).isBeforeNow());
    }

// org.joda.time.TestInstant_Basics::testIsBefore_RI
    public void testIsBefore_RI() {
        Instant test1 = new Instant(TEST_TIME1);
        Instant test1a = new Instant(TEST_TIME1);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        Instant test2 = new Instant(TEST_TIME2);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GregorianChronology.getInstance(PARIS));
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        assertEquals(false, test2.isBefore(new MockInstant()));
        assertEquals(false, test1.isBefore(new MockInstant()));
        
        assertEquals(false, new Instant(TEST_TIME_NOW + 1).isBefore(null));
        assertEquals(false, new Instant(TEST_TIME_NOW).isBefore(null));
        assertEquals(true, new Instant(TEST_TIME_NOW - 1).isBefore(null));
    }

// org.joda.time.TestInstant_Basics::testIsAfter_long
    public void testIsAfter_long() {
        assertEquals(false, new Instant(TEST_TIME1).isAfter(TEST_TIME2));
        assertEquals(false, new Instant(TEST_TIME1).isAfter(TEST_TIME1));
        assertEquals(true, new Instant(TEST_TIME2).isAfter(TEST_TIME1));
    }

// org.joda.time.TestInstant_Basics::testIsAfterNow
    public void testIsAfterNow() {
        assertEquals(false, new Instant(TEST_TIME_NOW - 1).isAfterNow());
        assertEquals(false, new Instant(TEST_TIME_NOW).isAfterNow());
        assertEquals(true, new Instant(TEST_TIME_NOW + 1).isAfterNow());
    }

// org.joda.time.TestInstant_Basics::testIsAfter_RI
    public void testIsAfter_RI() {
        Instant test1 = new Instant(TEST_TIME1);
        Instant test1a = new Instant(TEST_TIME1);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        Instant test2 = new Instant(TEST_TIME2);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GregorianChronology.getInstance(PARIS));
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        assertEquals(true, test2.isAfter(new MockInstant()));
        assertEquals(false, test1.isAfter(new MockInstant()));
        
        assertEquals(true, new Instant(TEST_TIME_NOW + 1).isAfter(null));
        assertEquals(false, new Instant(TEST_TIME_NOW).isAfter(null));
        assertEquals(false, new Instant(TEST_TIME_NOW - 1).isAfter(null));
    }

// org.joda.time.TestInstant_Basics::testSerialization
    public void testSerialization() throws Exception {
        Instant test = new Instant(TEST_TIME_NOW);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Instant result = (Instant) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestInstant_Basics::testToString
    public void testToString() {
        Instant test = new Instant(TEST_TIME_NOW);
        assertEquals("2002-06-09T00:00:00.000Z", test.toString());
    }

// org.joda.time.TestInstant_Basics::testToInstant
    public void testToInstant() {
        Instant test = new Instant(TEST_TIME1);
        Instant result = test.toInstant();
        assertSame(test, result);
    }

// org.joda.time.TestInstant_Basics::testToDateTime
    public void testToDateTime() {
        Instant test = new Instant(TEST_TIME1);
        DateTime result = test.toDateTime();
        assertEquals(TEST_TIME1, result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestInstant_Basics::testToDateTimeISO
    public void testToDateTimeISO() {
        Instant test = new Instant(TEST_TIME1);
        DateTime result = test.toDateTimeISO();
        assertSame(DateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestInstant_Basics::testToDateTime_DateTimeZone
    public void testToDateTime_DateTimeZone() {
        Instant test = new Instant(TEST_TIME1);
        DateTime result = test.toDateTime(LONDON);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(LONDON), result.getChronology());

        test = new Instant(TEST_TIME1);
        result = test.toDateTime(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(PARIS), result.getChronology());

        test = new Instant(TEST_TIME1);
        result = test.toDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestInstant_Basics::testToDateTime_Chronology
    public void testToDateTime_Chronology() {
        Instant test = new Instant(TEST_TIME1);
        DateTime result = test.toDateTime(ISOChronology.getInstance());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());

        test = new Instant(TEST_TIME1);
        result = test.toDateTime(GregorianChronology.getInstance(PARIS));
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GregorianChronology.getInstance(PARIS), result.getChronology());

        test = new Instant(TEST_TIME1);
        result = test.toDateTime((Chronology) null);
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestInstant_Basics::testToMutableDateTime
    public void testToMutableDateTime() {
        Instant test = new Instant(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime();
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestInstant_Basics::testToMutableDateTimeISO
    public void testToMutableDateTimeISO() {
        Instant test = new Instant(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTimeISO();
        assertSame(MutableDateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestInstant_Basics::testToMutableDateTime_DateTimeZone
    public void testToMutableDateTime_DateTimeZone() {
        Instant test = new Instant(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime(LONDON);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());

        test = new Instant(TEST_TIME1);
        result = test.toMutableDateTime(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(PARIS), result.getChronology());

        test = new Instant(TEST_TIME1);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestInstant_Basics::testToMutableDateTime_Chronology
    public void testToMutableDateTime_Chronology() {
        Instant test = new Instant(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime(ISOChronology.getInstance());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());

        test = new Instant(TEST_TIME1);
        result = test.toMutableDateTime(GregorianChronology.getInstance(PARIS));
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GregorianChronology.getInstance(PARIS), result.getChronology());

        test = new Instant(TEST_TIME1);
        result = test.toMutableDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISOChronology.getInstance(), result.getChronology());
    }

// org.joda.time.TestInstant_Basics::testToDate
    public void testToDate() {
        Instant test = new Instant(TEST_TIME1);
        Date result = test.toDate();
        assertEquals(test.getMillis(), result.getTime());
    }

// org.joda.time.TestInstant_Basics::testWithMillis_long
    public void testWithMillis_long() {
        Instant test = new Instant(TEST_TIME1);
        Instant result = test.withMillis(TEST_TIME2);
        assertEquals(TEST_TIME2, result.getMillis());
        assertEquals(test.getChronology(), result.getChronology());
        
        test = new Instant(TEST_TIME1);
        result = test.withMillis(TEST_TIME1);
        assertSame(test, result);
    }

// org.joda.time.TestInstant_Basics::testWithDurationAdded_long_int
    public void testWithDurationAdded_long_int() {
        Instant test = new Instant(TEST_TIME1);
        Instant result = test.withDurationAdded(123456789L, 1);
        Instant expected = new Instant(TEST_TIME1 + 123456789L);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(123456789L, 0);
        assertSame(test, result);
        
        result = test.withDurationAdded(123456789L, 2);
        expected = new Instant(TEST_TIME1 + (2L * 123456789L));
        assertEquals(expected, result);
        
        result = test.withDurationAdded(123456789L, -3);
        expected = new Instant(TEST_TIME1 - (3L * 123456789L));
        assertEquals(expected, result);
    }

// org.joda.time.TestInstant_Basics::testWithDurationAdded_RD_int
    public void testWithDurationAdded_RD_int() {
        Instant test = new Instant(TEST_TIME1);
        Instant result = test.withDurationAdded(new Duration(123456789L), 1);
        Instant expected = new Instant(TEST_TIME1 + 123456789L);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(null, 1);
        assertSame(test, result);
        
        result = test.withDurationAdded(new Duration(123456789L), 0);
        assertSame(test, result);
        
        result = test.withDurationAdded(new Duration(123456789L), 2);
        expected = new Instant(TEST_TIME1 + (2L * 123456789L));
        assertEquals(expected, result);
        
        result = test.withDurationAdded(new Duration(123456789L), -3);
        expected = new Instant(TEST_TIME1 - (3L * 123456789L));
        assertEquals(expected, result);
    }

// org.joda.time.TestInstant_Basics::testPlus_long
    public void testPlus_long() {
        Instant test = new Instant(TEST_TIME1);
        Instant result = test.plus(123456789L);
        Instant expected = new Instant(TEST_TIME1 + 123456789L);
        assertEquals(expected, result);
    }

// org.joda.time.TestInstant_Basics::testPlus_RD
    public void testPlus_RD() {
        Instant test = new Instant(TEST_TIME1);
        Instant result = test.plus(new Duration(123456789L));
        Instant expected = new Instant(TEST_TIME1 + 123456789L);
        assertEquals(expected, result);
        
        result = test.plus((ReadableDuration) null);
        assertSame(test, result);
    }

// org.joda.time.TestInstant_Basics::testMinus_long
    public void testMinus_long() {
        Instant test = new Instant(TEST_TIME1);
        Instant result = test.minus(123456789L);
        Instant expected = new Instant(TEST_TIME1 - 123456789L);
        assertEquals(expected, result);
    }

// org.joda.time.TestInstant_Basics::testMinus_RD
    public void testMinus_RD() {
        Instant test = new Instant(TEST_TIME1);
        Instant result = test.minus(new Duration(123456789L));
        Instant expected = new Instant(TEST_TIME1 - 123456789L);
        assertEquals(expected, result);
        
        result = test.minus((ReadableDuration) null);
        assertSame(test, result);
    }

// org.joda.time.TestInstant_Basics::testImmutable
    public void testImmutable() {
        assertTrue(Modifier.isFinal(Instant.class.getModifiers()));
    }

// org.joda.time.TestInstant_Constructors::test_now
    public void test_now() throws Throwable {
        Instant test = Instant.now();
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
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

// org.joda.time.TestInstant_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        Instant test = new Instant((Object) null);
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestInstant_Constructors::testConstructor_badconverterObject
    public void testConstructor_badconverterObject() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            Instant test = new Instant(new Integer(0));
            assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
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

// org.joda.time.TestInterval_Basics::testWithEndMillis_long1
    public void testWithEndMillis_long1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEndMillis(TEST_TIME2 - 1);
        assertEquals(new Interval(TEST_TIME1, TEST_TIME2 - 1, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithEndMillis_long2
    public void testWithEndMillis_long2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        try {
            test.withEndMillis(TEST_TIME1 - 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithEndMillis_long3
    public void testWithEndMillis_long3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEndMillis(TEST_TIME2);
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithEndInstant_RI1
    public void testWithEndInstant_RI1() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEnd(new Instant(TEST_TIME2 - 1));
        assertEquals(new Interval(TEST_TIME1, TEST_TIME2 - 1, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithEndInstant_RI2
    public void testWithEndInstant_RI2() {
        Interval test = new Interval(TEST_TIME1, TEST_TIME2);
        try {
            test.withEnd(new Instant(TEST_TIME1 - 1));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithEndInstant_RI3
    public void testWithEndInstant_RI3() {
        Interval base = new Interval(TEST_TIME1, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withEnd(null);
        assertEquals(new Interval(TEST_TIME1, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart1
    public void testWithDurationAfterStart1() throws Throwable {
        Duration dur = new Duration(TEST_TIME2 - TEST_TIME_NOW);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS);
        Interval test = base.withDurationAfterStart(dur);
        
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart2
    public void testWithDurationAfterStart2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationAfterStart(null);
        
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart3
    public void testWithDurationAfterStart3() throws Throwable {
        Duration dur = new Duration(-1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withDurationAfterStart(dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithDurationAfterStart4
    public void testWithDurationAfterStart4() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationAfterStart(base.toDuration());
        
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd1
    public void testWithDurationBeforeEnd1() throws Throwable {
        Duration dur = new Duration(TEST_TIME_NOW - TEST_TIME1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS);
        Interval test = base.withDurationBeforeEnd(dur);
        
        assertEquals(new Interval(TEST_TIME1, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd2
    public void testWithDurationBeforeEnd2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationBeforeEnd(null);
        
        assertEquals(new Interval(TEST_TIME2, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd3
    public void testWithDurationBeforeEnd3() throws Throwable {
        Duration dur = new Duration(-1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withDurationBeforeEnd(dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithDurationBeforeEnd4
    public void testWithDurationBeforeEnd4() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withDurationBeforeEnd(base.toDuration());
        
        assertSame(base, test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodAfterStart1
    public void testWithPeriodAfterStart1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, COPTIC_PARIS);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        
        Interval base = new Interval(dt, dt);
        Interval test = base.withPeriodAfterStart(dur);
        assertEquals(new Interval(dt, dur), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodAfterStart2
    public void testWithPeriodAfterStart2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withPeriodAfterStart(null);
        
        assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME_NOW, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodAfterStart3
    public void testWithPeriodAfterStart3() throws Throwable {
        Period per = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withPeriodAfterStart(per);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Basics::testWithPeriodBeforeEnd1
    public void testWithPeriodBeforeEnd1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, COPTIC_PARIS);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        
        Interval base = new Interval(dt, dt);
        Interval test = base.withPeriodBeforeEnd(dur);
        assertEquals(new Interval(dur, dt), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodBeforeEnd2
    public void testWithPeriodBeforeEnd2() throws Throwable {
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME2, COPTIC_PARIS);
        Interval test = base.withPeriodBeforeEnd(null);
        
        assertEquals(new Interval(TEST_TIME2, TEST_TIME2, COPTIC_PARIS), test);
    }

// org.joda.time.TestInterval_Basics::testWithPeriodBeforeEnd3
    public void testWithPeriodBeforeEnd3() throws Throwable {
        Period per = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        Interval base = new Interval(TEST_TIME_NOW, TEST_TIME_NOW);
        try {
            base.withPeriodBeforeEnd(per);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        DateTime start = new DateTime(2010, 6, 30, 12, 30, ISOChronology.getInstance(PARIS));
        DateTime end = new DateTime(2010, 7, 1, 14, 30, ISOChronology.getInstance(PARIS));
        assertEquals(new Interval(start, end), Interval.parse("2010-06-30T12:30/2010-07-01T14:30"));
        assertEquals(new Interval(start, end), Interval.parse("2010-06-30T12:30/P1DT2H"));
        assertEquals(new Interval(start, end), Interval.parse("P1DT2H/2010-07-01T14:30"));
    }

// org.joda.time.TestInterval_Constructors::testConstructor_long_long1
    public void testConstructor_long_long1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval test = new Interval(dt1.getMillis(), dt2.getMillis());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_long_long2
    public void testConstructor_long_long2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Interval test = new Interval(dt1.getMillis(), dt1.getMillis());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt1.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_long_long3
    public void testConstructor_long_long3() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        DateTime dt2 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            new Interval(dt1.getMillis(), dt2.getMillis());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Constructors::testConstructor_long_long_Zone
    public void testConstructor_long_long_Zone() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval test = new Interval(dt1.getMillis(), dt2.getMillis(), LONDON);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(LONDON), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_long_long_nullZone
    public void testConstructor_long_long_nullZone() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval test = new Interval(dt1.getMillis(), dt2.getMillis(), (DateTimeZone) null);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_long_long_Chronology
    public void testConstructor_long_long_Chronology() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval test = new Interval(dt1.getMillis(), dt2.getMillis(), GJChronology.getInstance());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(GJChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_long_long_nullChronology
    public void testConstructor_long_long_nullChronology() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval test = new Interval(dt1.getMillis(), dt2.getMillis(), (Chronology) null);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI1
    public void testConstructor_RI_RI1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval test = new Interval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI2
    public void testConstructor_RI_RI2() throws Throwable {
        Instant dt1 = new Instant(new DateTime(2004, 6, 9, 0, 0, 0, 0));
        Instant dt2 = new Instant(new DateTime(2005, 7, 10, 1, 1, 1, 1));
        Interval test = new Interval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI3
    public void testConstructor_RI_RI3() throws Throwable {
        Interval test = new Interval((ReadableInstant) null, (ReadableInstant) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI4
    public void testConstructor_RI_RI4() throws Throwable {
        DateTime dt1 = new DateTime(2000, 6, 9, 0, 0, 0, 0);
        Interval test = new Interval(dt1, (ReadableInstant) null);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI5
    public void testConstructor_RI_RI5() throws Throwable {
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval test = new Interval((ReadableInstant) null, dt2);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI6
    public void testConstructor_RI_RI6() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        Interval test = new Interval(dt1, dt1);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt1.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI7
    public void testConstructor_RI_RI7() throws Throwable {
        DateTime dt1 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        DateTime dt2 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            new Interval(dt1, dt2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI_chronoStart
    public void testConstructor_RI_RI_chronoStart() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, GJChronology.getInstance());
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval test = new Interval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(GJChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI_chronoEnd
    public void testConstructor_RI_RI_chronoEnd() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, GJChronology.getInstance());
        Interval test = new Interval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI_zones
    public void testConstructor_RI_RI_zones() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0, LONDON);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1, PARIS);
        Interval test = new Interval(dt1, dt2);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(LONDON), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RI_instant
    public void testConstructor_RI_RI_instant() throws Throwable {
        Instant dt1 = new Instant(12345678L);
        Instant dt2 = new Instant(22345678L);
        Interval test = new Interval(dt1, dt2);
        assertEquals(12345678L, test.getStartMillis());
        assertEquals(22345678L, test.getEndMillis());
        assertEquals(ISOChronology.getInstanceUTC(), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RP1
    public void testConstructor_RI_RP1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, 6);
        result = ISOChronology.getInstance().hours().add(result, 1);
        
        Interval test = new Interval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RP2
    public void testConstructor_RI_RP2() throws Throwable {
        Instant dt = new Instant(new DateTime(TEST_TIME_NOW));
        Period dur = new Period(0, 6, 0, 3, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstanceUTC().months().add(result, 6);
        result = ISOChronology.getInstanceUTC().days().add(result, 3);
        result = ISOChronology.getInstanceUTC().hours().add(result, 1);
        
        Interval test = new Interval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RP3
    public void testConstructor_RI_RP3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, CopticChronology.getInstanceUTC());
        Period dur = new Period(0, 6, 0, 3, 1, 0, 0, 0, PeriodType.standard());
        long result = TEST_TIME_NOW;
        result = CopticChronology.getInstanceUTC().months().add(result, 6);
        result = CopticChronology.getInstanceUTC().days().add(result, 3);
        result = CopticChronology.getInstanceUTC().hours().add(result, 1);
        
        Interval test = new Interval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RP4
    public void testConstructor_RI_RP4() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(1 * DateTimeConstants.MILLIS_PER_HOUR + 23L);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().hours().add(result, 1);
        result = ISOChronology.getInstance().millis().add(result, 23);
        
        Interval test = new Interval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RP5
    public void testConstructor_RI_RP5() throws Throwable {
        Interval test = new Interval((ReadableInstant) null, (ReadablePeriod) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RP6
    public void testConstructor_RI_RP6() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Interval test = new Interval(dt, (ReadablePeriod) null);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RP7
    public void testConstructor_RI_RP7() throws Throwable {
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, 6);
        result = ISOChronology.getInstance().hourOfDay().add(result, 1);
        
        Interval test = new Interval((ReadableInstant) null, dur);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RP8
    public void testConstructor_RI_RP8() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        try {
            new Interval(dt, dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RP_RI1
    public void testConstructor_RP_RI1() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, -6);
        result = ISOChronology.getInstance().hours().add(result, -1);
        
        Interval test = new Interval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RP_RI2
    public void testConstructor_RP_RI2() throws Throwable {
        Instant dt = new Instant(new DateTime(TEST_TIME_NOW));
        Period dur = new Period(0, 6, 0, 3, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstanceUTC().months().add(result, -6);
        result = ISOChronology.getInstanceUTC().days().add(result, -3);
        result = ISOChronology.getInstanceUTC().hours().add(result, -1);
        
        Interval test = new Interval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RP_RI3
    public void testConstructor_RP_RI3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW, CopticChronology.getInstanceUTC());
        Period dur = new Period(0, 6, 0, 3, 1, 0, 0, 0, PeriodType.standard());
        long result = TEST_TIME_NOW;
        result = CopticChronology.getInstanceUTC().months().add(result, -6);
        result = CopticChronology.getInstanceUTC().days().add(result, -3);
        result = CopticChronology.getInstanceUTC().hours().add(result, -1);
        
        Interval test = new Interval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RP_RI4
    public void testConstructor_RP_RI4() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(1 * DateTimeConstants.MILLIS_PER_HOUR + 23L);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().hours().add(result, -1);
        result = ISOChronology.getInstance().millis().add(result, -23);
        
        Interval test = new Interval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RP_RI5
    public void testConstructor_RP_RI5() throws Throwable {
        Interval test = new Interval((ReadablePeriod) null, (ReadableInstant) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RP_RI6
    public void testConstructor_RP_RI6() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Interval test = new Interval((ReadablePeriod) null, dt);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RP_RI7
    public void testConstructor_RP_RI7() throws Throwable {
        Period dur = new Period(0, 6, 0, 0, 1, 0, 0, 0);
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, -6);
        result = ISOChronology.getInstance().hourOfDay().add(result, -1);
        
        Interval test = new Interval(dur, (ReadableInstant) null);
        assertEquals(result, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RP_RI8
    public void testConstructor_RP_RI8() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Period dur = new Period(0, 0, 0, 0, 0, 0, 0, -1);
        try {
            new Interval(dur, dt);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RD1
    public void testConstructor_RI_RD1() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, 6);
        result = ISOChronology.getInstance().hours().add(result, 1);
        
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(result - TEST_TIME_NOW);
        
        Interval test = new Interval(dt, dur);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RD2
    public void testConstructor_RI_RD2() throws Throwable {
        Interval test = new Interval((ReadableInstant) null, (ReadableDuration) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RD3
    public void testConstructor_RI_RD3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Interval test = new Interval(dt, (ReadableDuration) null);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RD4
    public void testConstructor_RI_RD4() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, 6);
        result = ISOChronology.getInstance().hourOfDay().add(result, 1);
        
        Duration dur = new Duration(result - TEST_TIME_NOW);
        
        Interval test = new Interval((ReadableInstant) null, dur);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(result, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RI_RD5
    public void testConstructor_RI_RD5() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(-1);
        try {
            new Interval(dt, dur);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RD_RI1
    public void testConstructor_RD_RI1() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().months().add(result, -6);
        result = ISOChronology.getInstance().hours().add(result, -1);
        
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(TEST_TIME_NOW - result);
        
        Interval test = new Interval(dur, dt);
        assertEquals(result, test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RD_RI2
    public void testConstructor_RD_RI2() throws Throwable {
        Interval test = new Interval((ReadableDuration) null, (ReadableInstant) null);
        assertEquals(TEST_TIME_NOW, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RD_RI3
    public void testConstructor_RD_RI3() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Interval test = new Interval((ReadableDuration) null, dt);
        assertEquals(dt.getMillis(), test.getStartMillis());
        assertEquals(dt.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RD_RI4
    public void testConstructor_RD_RI4() throws Throwable {
        long result = TEST_TIME_NOW;
        result = ISOChronology.getInstance().monthOfYear().add(result, -6);
        result = ISOChronology.getInstance().hourOfDay().add(result, -1);
        
        Duration dur = new Duration(TEST_TIME_NOW - result);
        
        Interval test = new Interval(dur, (ReadableInstant) null);
        assertEquals(result, test.getStartMillis());
        assertEquals(TEST_TIME_NOW, test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_RD_RI5
    public void testConstructor_RD_RI5() throws Throwable {
        DateTime dt = new DateTime(TEST_TIME_NOW);
        Duration dur = new Duration(-1);
        try {
            new Interval(dur, dt);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestInterval_Constructors::testConstructor_Object1
    public void testConstructor_Object1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval test = new Interval(dt1.toString() + '/' + dt2.toString());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_Object2
    public void testConstructor_Object2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval base = new Interval(dt1, dt2);
        
        Interval test = new Interval(base);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_Object3
    public void testConstructor_Object3() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        MutableInterval base = new MutableInterval(dt1, dt2);
        
        Interval test = new Interval(base);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_Object4
    public void testConstructor_Object4() throws Throwable {
        MockInterval base = new MockInterval();
        Interval test = new Interval(base);
        assertEquals(base.getStartMillis(), test.getStartMillis());
        assertEquals(base.getEndMillis(), test.getEndMillis());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_Object5
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
            Interval test = new Interval(dt1.toString() + '/' + dt2.toString());
            assertEquals(1234L, test.getStartMillis());
            assertEquals(5678L, test.getEndMillis());
        } finally {
            ConverterManager.getInstance().addIntervalConverter(oldConv);
        }
    }

// org.joda.time.TestInterval_Constructors::testConstructor_Object6
    public void testConstructor_Object6() throws Throwable {
        IntervalConverter oldConv = ConverterManager.getInstance().getIntervalConverter(new Interval(0L, 0L));
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
            Interval test = new Interval(base);
            assertEquals(1234L, test.getStartMillis());
            assertEquals(5678L, test.getEndMillis());
        } finally {
            ConverterManager.getInstance().addIntervalConverter(oldConv);
        }
    }

// org.joda.time.TestInterval_Constructors::testConstructor_Object_Chronology1
    public void testConstructor_Object_Chronology1() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval base = new Interval(dt1, dt2);
        
        Interval test = new Interval(base, BuddhistChronology.getInstance());
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(BuddhistChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestInterval_Constructors::testConstructor_Object_Chronology2
    public void testConstructor_Object_Chronology2() throws Throwable {
        DateTime dt1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2005, 7, 10, 1, 1, 1, 1);
        Interval base = new Interval(dt1, dt2);
        
        Interval test = new Interval(base, null);
        assertEquals(dt1.getMillis(), test.getStartMillis());
        assertEquals(dt2.getMillis(), test.getEndMillis());
        assertEquals(ISOChronology.getInstance(), test.getChronology());
    }

// org.joda.time.TestLocalDateTime_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40);
        assertEquals(1970, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(2, test.get(DateTimeFieldType.dayOfWeek()));
        assertEquals(160, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(24, test.get(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(1970, test.get(DateTimeFieldType.weekyear()));
        assertEquals(10, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(30, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(40, test.get(DateTimeFieldType.millisOfSecond()));
        assertEquals(MILLIS_OF_DAY_UTC / 60000 , test.get(DateTimeFieldType.minuteOfDay()));
        assertEquals(MILLIS_OF_DAY_UTC / 1000 , test.get(DateTimeFieldType.secondOfDay()));
        assertEquals(MILLIS_OF_DAY_UTC , test.get(DateTimeFieldType.millisOfDay()));
        assertEquals(10, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(DateTimeConstants.AM, test.get(DateTimeFieldType.halfdayOfDay()));
        
        test = new LocalDateTime(1970, 6, 9, 12, 30);
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.PM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalDateTime(1970, 6, 9, 14, 30);
        assertEquals(2, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(2, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(14, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.PM, test.get(DateTimeFieldType.halfdayOfDay()));
        test = new LocalDateTime(1970, 6, 9, 0, 30);
        assertEquals(0, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(12, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(24, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(DateTimeConstants.AM, test.get(DateTimeFieldType.halfdayOfDay()));
        try {
            test.get(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testSize
    public void testSize() {
        LocalDateTime test = new LocalDateTime();
        assertEquals(4, test.size());
    }

// org.joda.time.TestLocalDateTime_Basics::testGetFieldType_int
    public void testGetFieldType_int() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        assertSame(DateTimeFieldType.year(), test.getFieldType(0));
        assertSame(DateTimeFieldType.monthOfYear(), test.getFieldType(1));
        assertSame(DateTimeFieldType.dayOfMonth(), test.getFieldType(2));
        assertSame(DateTimeFieldType.millisOfDay(), test.getFieldType(3));
        try {
            test.getFieldType(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getFieldType(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGetFieldTypes
    public void testGetFieldTypes() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        DateTimeFieldType[] fields = test.getFieldTypes();
        assertSame(DateTimeFieldType.year(), fields[0]);
        assertSame(DateTimeFieldType.monthOfYear(), fields[1]);
        assertSame(DateTimeFieldType.dayOfMonth(), fields[2]);
        assertSame(DateTimeFieldType.millisOfDay(), fields[3]);
        assertNotSame(test.getFieldTypes(), test.getFieldTypes());
    }

// org.joda.time.TestLocalDateTime_Basics::testGetField_int
    public void testGetField_int() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        assertSame(COPTIC_UTC.year(), test.getField(0));
        assertSame(COPTIC_UTC.monthOfYear(), test.getField(1));
        assertSame(COPTIC_UTC.dayOfMonth(), test.getField(2));
        assertSame(COPTIC_UTC.millisOfDay(), test.getField(3));
        try {
            test.getField(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getField(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGetFields
    public void testGetFields() {
        LocalDateTime test = new LocalDateTime(COPTIC_PARIS);
        DateTimeField[] fields = test.getFields();
        assertSame(COPTIC_UTC.year(), fields[0]);
        assertSame(COPTIC_UTC.monthOfYear(), fields[1]);
        assertSame(COPTIC_UTC.dayOfMonth(), fields[2]);
        assertSame(COPTIC_UTC.millisOfDay(), fields[3]);
        assertNotSame(test.getFields(), test.getFields());
    }

// org.joda.time.TestLocalDateTime_Basics::testGetValue_int
    public void testGetValue_int() {
        LocalDateTime test = new LocalDateTime(ISO_UTC);
        assertEquals(1970, test.getValue(0));
        assertEquals(6, test.getValue(1));
        assertEquals(9, test.getValue(2));
        assertEquals(MILLIS_OF_DAY_UTC, test.getValue(3));
        try {
            test.getValue(-1);
        } catch (IndexOutOfBoundsException ex) {}
        try {
            test.getValue(3);
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testGetValues
    public void testGetValues() {
        LocalDateTime test = new LocalDateTime(ISO_UTC);
        int[] values = test.getValues();
        assertEquals(1970, values[0]);
        assertEquals(6, values[1]);
        assertEquals(9, values[2]);
        assertEquals(MILLIS_OF_DAY_UTC, values[3]);
        assertNotSame(test.getValues(), test.getValues());
    }

// org.joda.time.TestLocalDateTime_Basics::testIsSupported_DateTimeFieldType
    public void testIsSupported_DateTimeFieldType() {
        LocalDateTime test = new LocalDateTime();
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfWeek()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfCentury()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.centuryOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekyearOfCentury()));
        assertEquals(true, test.isSupported(DateTimeFieldType.era()));
        
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
        
        assertEquals(false, test.isSupported((DateTimeFieldType) null));
    }

// org.joda.time.TestLocalDateTime_Basics::testIsSupported_DurationFieldType
    public void testIsSupported_DurationFieldType() {
        LocalDateTime test = new LocalDateTime();
        assertEquals(false, test.isSupported(DurationFieldType.eras()));
        assertEquals(true, test.isSupported(DurationFieldType.centuries()));
        assertEquals(true, test.isSupported(DurationFieldType.years()));
        assertEquals(true, test.isSupported(DurationFieldType.months()));
        assertEquals(true, test.isSupported(DurationFieldType.weekyears()));
        assertEquals(true, test.isSupported(DurationFieldType.weeks()));
        assertEquals(true, test.isSupported(DurationFieldType.days()));
        
        assertEquals(true, test.isSupported(DurationFieldType.hours()));
        assertEquals(true, test.isSupported(DurationFieldType.minutes()));
        assertEquals(true, test.isSupported(DurationFieldType.seconds()));
        assertEquals(true, test.isSupported(DurationFieldType.millis()));
        assertEquals(true, test.isSupported(DurationFieldType.halfdays()));
        
        assertEquals(false, test.isSupported((DurationFieldType) null));
    }

// org.joda.time.TestLocalDateTime_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        LocalDateTime test1 = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        LocalDateTime test2 = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        LocalDateTime test3 = new LocalDateTime(1971, 6, 9, 10, 20, 30, 40);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        Partial partial = new Partial(
                new DateTimeFieldType[] {
                        DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(),
                        DateTimeFieldType.dayOfMonth(), DateTimeFieldType.millisOfDay()},
                new int[] {1970, 6, 9, MILLIS_OF_DAY_UTC}, COPTIC_PARIS);
        assertEquals(true, test1.equals(partial));
        assertEquals(true, test1.hashCode() == partial.hashCode());
        assertEquals(false, test1.equals(MockPartial.EMPTY_INSTANCE));
    }

// org.joda.time.TestLocalDateTime_Basics::testCompareTo
    public void testCompareTo() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.millisOfDay(),
        };
        int[] values = new int[] {2005, 6, 2, MILLIS_OF_DAY_UTC};
        Partial p = new Partial(types, values);
        assertEquals(0, test1.compareTo(p));
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

        try {
            test1.compareTo(new YearMonthDay());
            fail();
        } catch (ClassCastException ex) {}
        try {
            test1.compareTo(new TimeOfDay());
            fail();
        } catch (ClassCastException ex) {}
        Partial partial = new Partial()
            .with(DateTimeFieldType.centuryOfEra(), 1)
            .with(DateTimeFieldType.halfdayOfDay(), 0)
            .with(DateTimeFieldType.dayOfMonth(), 9);
        try {
            new LocalDateTime(1970, 6, 9, 10, 20, 30, 40).compareTo(partial);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testIsEqual_LocalDateTime
    public void testIsEqual_LocalDateTime() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        try {
            new LocalDateTime(2005, 7, 2, 10, 20, 30, 40).isEqual(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testIsBefore_LocalDateTime
    public void testIsBefore_LocalDateTime() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        try {
            new LocalDateTime(2005, 7, 2, 10, 20, 30, 40).isBefore(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testIsAfter_LocalDateTime
    public void testIsAfter_LocalDateTime() {
        LocalDateTime test1 = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        LocalDateTime test1a = new LocalDateTime(2005, 6, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        LocalDateTime test2 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        LocalDateTime test3 = new LocalDateTime(2005, 7, 2, 10, 20, 30, 40, GREGORIAN_UTC);
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        try {
            new LocalDateTime(2005, 7, 2, 10, 20, 30, 40).isAfter(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithDate
    public void testWithDate() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withDate(2006, 2, 1);
        
        check(test, 2004, 6, 9, 10, 20, 30, 40);
        check(result, 2006, 2, 1, 10, 20, 30, 40);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithTime
    public void testWithTime() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withTime(9, 8, 7, 6);
        
        check(test, 2004, 6, 9, 10, 20, 30, 40);
        check(result, 2004, 6, 9, 9, 8, 7, 6);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithField_DateTimeFieldType_int_1
    public void testWithField_DateTimeFieldType_int_1() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 40), test);
        assertEquals(new LocalDateTime(2006, 6, 9, 10, 20, 30, 40), result);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithField_DateTimeFieldType_int_2
    public void testWithField_DateTimeFieldType_int_2() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithField_DateTimeFieldType_int_3
    public void testWithField_DateTimeFieldType_int_3() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withField(DateTimeFieldType.year(), 2004);
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 40), test);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_1
    public void testWithFieldAdded_DurationFieldType_int_1() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 40), test);
        assertEquals(new LocalDateTime(2010, 6, 9, 10, 20, 30, 40), result);
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_2
    public void testWithFieldAdded_DurationFieldType_int_2() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_3
    public void testWithFieldAdded_DurationFieldType_int_3() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testWithFieldAdded_DurationFieldType_int_4
    public void testWithFieldAdded_DurationFieldType_int_4() {
        LocalDateTime test = new LocalDateTime(2004, 6, 9, 10, 20, 30, 40);
        LocalDateTime result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlus_RP
    public void testPlus_RP() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plus(new Period(1, 2, 3, 4, 29, 6, 7, 8));
        LocalDateTime expected = new LocalDateTime(2003, 7, 29, 15, 26, 37, 48, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusYears_int
    public void testPlusYears_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusYears(1);
        LocalDateTime expected = new LocalDateTime(2003, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusMonths(1);
        LocalDateTime expected = new LocalDateTime(2002, 6, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusWeeks_int
    public void testPlusWeeks_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusWeeks(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 10, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusDays_int
    public void testPlusDays_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusDays(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 4, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusHours_int
    public void testPlusHours_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusHours(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 11, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusMinutes_int
    public void testPlusMinutes_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusMinutes(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 21, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusSeconds_int
    public void testPlusSeconds_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusSeconds(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 31, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testPlusMillis_int
    public void testPlusMillis_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.plusMillis(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 30, 41, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinus_RP
    public void testMinus_RP() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        
        LocalDateTime expected = new LocalDateTime(2001, 3, 26, 9, 19, 29, 39, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusYears_int
    public void testMinusYears_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusYears(1);
        LocalDateTime expected = new LocalDateTime(2001, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusMonths(1);
        LocalDateTime expected = new LocalDateTime(2002, 4, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusWeeks_int
    public void testMinusWeeks_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusWeeks(1);
        LocalDateTime expected = new LocalDateTime(2002, 4, 26, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusDays_int
    public void testMinusDays_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusDays(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 2, 10, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusHours_int
    public void testMinusHours_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusHours(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 9, 20, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusMinutes_int
    public void testMinusMinutes_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusMinutes(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 19, 30, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusSeconds_int
    public void testMinusSeconds_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusSeconds(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 29, 40, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testMinusMillis_int
    public void testMinusMillis_int() {
        LocalDateTime test = new LocalDateTime(2002, 5, 3, 10, 20, 30, 40, BUDDHIST_LONDON);
        LocalDateTime result = test.minusMillis(1);
        LocalDateTime expected = new LocalDateTime(2002, 5, 3, 10, 20, 30, 39, BUDDHIST_LONDON);
        assertEquals(expected, result);
        
        result = test.minusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestLocalDateTime_Basics::testGetters
    public void testGetters() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, GJ_UTC);
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(160, test.getDayOfYear());
        assertEquals(2, test.getDayOfWeek());
        assertEquals(24, test.getWeekOfWeekyear());
        assertEquals(1970, test.getWeekyear());
        assertEquals(70, test.getYearOfCentury());
        assertEquals(20, test.getCenturyOfEra());
        assertEquals(1970, test.getYearOfEra());
        assertEquals(DateTimeConstants.AD, test.getEra());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(MILLIS_OF_DAY_UTC, test.getMillisOfDay());
    }

// org.joda.time.TestLocalDateTime_Basics::testWithers
    public void testWithers() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40, GJ_UTC);
        check(test.withYear(2000), 2000, 6, 9, 10, 20, 30, 40);
        check(test.withMonthOfYear(2), 1970, 2, 9, 10, 20, 30, 40);
        check(test.withDayOfMonth(2), 1970, 6, 2, 10, 20, 30, 40);
        check(test.withDayOfYear(6), 1970, 1, 6, 10, 20, 30, 40);
        check(test.withDayOfWeek(6), 1970, 6, 13, 10, 20, 30, 40);
        check(test.withWeekOfWeekyear(6), 1970, 2, 3, 10, 20, 30, 40);
        check(test.withWeekyear(1971), 1971, 6, 15, 10, 20, 30, 40);
        check(test.withYearOfCentury(60), 1960, 6, 9, 10, 20, 30, 40);
        check(test.withCenturyOfEra(21), 2070, 6, 9, 10, 20, 30, 40);
        check(test.withYearOfEra(1066), 1066, 6, 9, 10, 20, 30, 40);
        check(test.withEra(DateTimeConstants.BC), -1970, 6, 9, 10, 20, 30, 40);
        check(test.withHourOfDay(6), 1970, 6, 9, 6, 20, 30, 40);
        check(test.withMinuteOfHour(6), 1970, 6, 9, 10, 6, 30, 40);
        check(test.withSecondOfMinute(6), 1970, 6, 9, 10, 20, 6, 40);
        check(test.withMillisOfSecond(6), 1970, 6, 9, 10, 20, 30, 6);
        check(test.withMillisOfDay(61234), 1970, 6, 9, 0, 1, 1, 234);
        try {
            test.withMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime
    public void testToDateTime() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        
        DateTime test = base.toDateTime();
        check(base, 2005, 6, 9, 6, 7, 8, 9);
        DateTime expected = new DateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_Zone
    public void testToDateTime_Zone() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        
        DateTime test = base.toDateTime(TOKYO);
        check(base, 2005, 6, 9, 6, 7, 8, 9);
        DateTime expected = new DateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_nullZone
    public void testToDateTime_nullZone() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        
        DateTime test = base.toDateTime((DateTimeZone) null);
        check(base, 2005, 6, 9, 6, 7, 8, 9);
        DateTime expected = new DateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToLocalDate
    public void testToLocalDate() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        LocalDate expected = new LocalDate(2005, 6, 9, COPTIC_LONDON);
        assertEquals(expected,base.toLocalDate());
    }

// org.joda.time.TestLocalDateTime_Basics::testToLocalTime
    public void testToLocalTime() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 6, 7, 8, 9, COPTIC_PARIS); 
        LocalTime expected = new LocalTime(6, 7, 8, 9, COPTIC_LONDON);
        assertEquals(expected,base.toLocalTime());
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_RI
    public void testToDateTime_RI() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        DateTime dt = new DateTime(2002, 1, 3, 4, 5, 6, 7, BUDDHIST_TOKYO);
        
        DateTime test = base.toDateTime(dt);
        check(base, 2005, 6, 9, 10, 20, 30, 40);
        DateTime expected = new DateTime(2005, 6, 9, 10, 20, 30, 40, BUDDHIST_TOKYO);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDateTime_nullRI
    public void testToDateTime_nullRI() {
        LocalDateTime base = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        DateTime test = base.toDateTime((ReadableInstant) null);
        check(base, 2005, 6, 9, 10, 20, 30, 40);
        DateTime expected = new DateTime(2005, 6, 9, 10, 20, 30, 40, ISO_LONDON);
        assertEquals(expected, test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_summer
    public void testToDate_summer() {
        LocalDateTime base = new LocalDateTime(2005, 7, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        Date test = base.toDate();
        check(base, 2005, 7, 9, 10, 20, 30, 40);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JULY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        gcal.set(Calendar.HOUR_OF_DAY, 10);
        gcal.set(Calendar.MINUTE, 20);
        gcal.set(Calendar.SECOND, 30);
        gcal.set(Calendar.MILLISECOND, 40);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_winter
    public void testToDate_winter() {
        LocalDateTime base = new LocalDateTime(2005, 1, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        Date test = base.toDate();
        check(base, 2005, 1, 9, 10, 20, 30, 40);
        
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2005);
        gcal.set(Calendar.MONTH, Calendar.JANUARY);
        gcal.set(Calendar.DAY_OF_MONTH, 9);
        gcal.set(Calendar.HOUR_OF_DAY, 10);
        gcal.set(Calendar.MINUTE, 20);
        gcal.set(Calendar.SECOND, 30);
        gcal.set(Calendar.MILLISECOND, 40);
        assertEquals(gcal.getTime(), test);
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_springDST
    public void testToDate_springDST() {
        LocalDateTime base = new LocalDateTime(2007, 4, 2, 0, 20, 0, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 4, 2, 0, 20, 0, 0);
            assertEquals("Mon Apr 02 01:00:00 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_springDST_2Hour40Savings
    public void testToDate_springDST_2Hour40Savings() {
        LocalDateTime base = new LocalDateTime(2007, 4, 2, 0, 20, 0, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000, (3600000 / 6) * 16);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 4, 2, 0, 20, 0, 0);
            assertEquals("Mon Apr 02 02:40:00 GMT+03:40 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testToDate_autumnDST
    public void testToDate_autumnDST() {
        LocalDateTime base = new LocalDateTime(2007, 10, 2, 0, 20, 30, 0);
        
        SimpleTimeZone testZone = new SimpleTimeZone(3600000, "NoMidnight",
                Calendar.APRIL, 2, 0, 0, Calendar.OCTOBER, 2, 0, 3600000);
        TimeZone currentZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(testZone);
            Date test = base.toDate();
            check(base, 2007, 10, 2, 0, 20, 30, 0);
            assertEquals("Tue Oct 02 00:20:30 GMT+02:00 2007", test.toString());
        } finally {
            TimeZone.setDefault(currentZone);
        }
    }

// org.joda.time.TestLocalDateTime_Basics::testProperty
    public void testProperty() {
        LocalDateTime test = new LocalDateTime(2005, 6, 9, 10, 20, 30, 40, GJ_UTC);
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
        assertEquals(test.hourOfDay(), test.property(DateTimeFieldType.hourOfDay()));
        assertEquals(test.minuteOfHour(), test.property(DateTimeFieldType.minuteOfHour()));
        assertEquals(test.secondOfMinute(), test.property(DateTimeFieldType.secondOfMinute()));
        assertEquals(test.millisOfSecond(), test.property(DateTimeFieldType.millisOfSecond()));
        assertEquals(test.millisOfDay(), test.property(DateTimeFieldType.millisOfDay()));
        
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(test, test.property(DateTimeFieldType.minuteOfDay()).getLocalDateTime());
    }

// org.joda.time.TestLocalDateTime_Basics::testSerialization
    public void testSerialization() throws Exception {
        LocalDateTime test = new LocalDateTime(1972, 6, 9, 10, 20, 30, 40, COPTIC_PARIS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDateTime result = (LocalDateTime) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
        assertTrue(Arrays.equals(test.getValues(), result.getValues()));
        assertTrue(Arrays.equals(test.getFields(), result.getFields()));
        assertEquals(test.getChronology(), result.getChronology());
        assertTrue(result.isSupported(DateTimeFieldType.dayOfMonth()));  
    }

// org.joda.time.TestLocalDateTime_Basics::testToString
    public void testToString() {
        LocalDateTime test = new LocalDateTime(2002, 6, 9, 10, 20, 30, 40);
        assertEquals("2002-06-09T10:20:30.040", test.toString());
    }

// org.joda.time.TestLocalDateTime_Basics::testToString_String
    public void testToString_String() {
        LocalDateTime test = new LocalDateTime(2002, 6, 9, 10, 20, 30, 40);
        assertEquals("2002 10", test.toString("yyyy HH"));
        assertEquals("2002-06-09T10:20:30.040", test.toString((String) null));
    }

// org.joda.time.TestLocalDateTime_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        LocalDateTime test = new LocalDateTime(1970, 6, 9, 10, 20, 30, 40);
        assertEquals("Tue 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("mar. 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("1970-06-09T10:20:30.040", test.toString(null, Locale.ENGLISH));
        assertEquals("Tue 9/6", test.toString("EEE d/M", null));
        assertEquals("1970-06-09T10:20:30.040", test.toString(null, null));
    }

// org.joda.time.TestLocalDateTime_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        LocalDateTime test = new LocalDateTime(2002, 6, 9, 10, 20, 30, 40);
        assertEquals("2002 10", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09T10:20:30.040", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestLocalDateTime_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new LocalDateTime(2010, 6, 30, 1, 20), LocalDateTime.parse("2010-06-30T01:20"));
        assertEquals(new LocalDateTime(2010, 1, 2, 14, 50, 30, 432), LocalDateTime.parse("2010-002T14:50:30.432"));
    }

// org.joda.time.TestLocalDateTime_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--dd MM HH").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new LocalDateTime(2010, 6, 30, 13, 0), LocalDateTime.parse("2010--30 06 13", f));
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_FromCalendarFields
    public void testFactory_FromCalendarFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(1970, 2, 3, 4, 5, 6, 7);
        assertEquals(expected, LocalDateTime.fromCalendarFields(cal));
        try {
            LocalDateTime.fromCalendarFields((Calendar) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testFactory_FromDateFields
    public void testFactory_FromDateFields() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(1970, 2, 3, 4, 5 ,6, 7);
        assertEquals(expected, LocalDateTime.fromDateFields(cal.getTime()));
        try {
            LocalDateTime.fromDateFields((Date) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        LocalDateTime test = new LocalDateTime();
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(test, LocalDateTime.now());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime dt = new DateTime(2005, 6, 8, 23, 59, 0, 0, LONDON);
        DateTimeUtils.setCurrentMillisFixed(dt.getMillis());
        
        
        LocalDateTime test = new LocalDateTime(LONDON);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(8, test.getDayOfMonth());
        assertEquals(23, test.getHourOfDay());
        assertEquals(59, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        assertEquals(test, LocalDateTime.now(LONDON));
        
        test = new LocalDateTime(PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(2005, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(59, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
        assertEquals(test, LocalDateTime.now(PARIS));
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        LocalDateTime test = new LocalDateTime((DateTimeZone) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        LocalDateTime test = new LocalDateTime(GREGORIAN_PARIS);
        assertEquals(GREGORIAN_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
        assertEquals(test, LocalDateTime.now(GREGORIAN_PARIS));
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        LocalDateTime test = new LocalDateTime((Chronology) null);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(10 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME1);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME2);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1971, test.getYear());
        assertEquals(5, test.getMonthOfYear());
        assertEquals(7, test.getDayOfMonth());
        assertEquals(14 + OFFSET_MOSCOW, test.getHourOfDay());
        assertEquals(28, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestLocalDateTime_Constructors::testConstructor_long1_DateTimeZone
    public void testConstructor_long1_DateTimeZone() throws Throwable {
        LocalDateTime test = new LocalDateTime(TEST_TIME1, PARIS);
        assertEquals(ISO_UTC, test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(4, test.getMonthOfYear());
        assertEquals(6, test.getDayOfMonth());
        assertEquals(12 + OFFSET_PARIS, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }
