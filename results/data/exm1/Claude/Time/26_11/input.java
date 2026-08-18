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
// org.joda.time.field.TestPreciseDurationDateTimeField::test_getLeapAmount_long
    public void test_getLeapAmount_long() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(0, field.getLeapAmount(0L));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getLeapDurationField
    public void test_getLeapDurationField() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(null, field.getLeapDurationField());
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getMinimumValue
    public void test_getMinimumValue() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(0, field.getMinimumValue());
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getMinimumValue_long
    public void test_getMinimumValue_long() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(0, field.getMinimumValue(0L));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getMinimumValue_RP
    public void test_getMinimumValue_RP() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(0, field.getMinimumValue(new TimeOfDay()));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getMinimumValue_RP_intarray
    public void test_getMinimumValue_RP_intarray() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(0, field.getMinimumValue(new TimeOfDay(), new int[4]));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getMaximumValue
    public void test_getMaximumValue() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(59, field.getMaximumValue());
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getMaximumValue_long
    public void test_getMaximumValue_long() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(59, field.getMaximumValue(0L));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getMaximumValue_RP
    public void test_getMaximumValue_RP() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(59, field.getMaximumValue(new TimeOfDay()));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getMaximumValue_RP_intarray
    public void test_getMaximumValue_RP_intarray() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(59, field.getMaximumValue(new TimeOfDay(), new int[4]));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getMaximumTextLength_Locale
    public void test_getMaximumTextLength_Locale() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(2, field.getMaximumTextLength(Locale.ENGLISH));

        field = new MockPreciseDurationDateTimeField() {
            public int getMaximumValue() {
                return 5;
            }
        };
        assertEquals(1, field.getMaximumTextLength(Locale.ENGLISH));
        
        field = new MockPreciseDurationDateTimeField() {
            public int getMaximumValue() {
                return 555;
            }
        };
        assertEquals(3, field.getMaximumTextLength(Locale.ENGLISH));
        
        field = new MockPreciseDurationDateTimeField() {
            public int getMaximumValue() {
                return 5555;
            }
        };
        assertEquals(4, field.getMaximumTextLength(Locale.ENGLISH));
        
        field = new MockPreciseDurationDateTimeField() {
            public int getMaximumValue() {
                return -1;
            }
        };
        assertEquals(2, field.getMaximumTextLength(Locale.ENGLISH));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_getMaximumShortTextLength_Locale
    public void test_getMaximumShortTextLength_Locale() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(2, field.getMaximumShortTextLength(Locale.ENGLISH));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_roundFloor_long
    public void test_roundFloor_long() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(-120L, field.roundFloor(-61L));
        assertEquals(-60L, field.roundFloor(-60L));
        assertEquals(-60L, field.roundFloor(-59L));
        assertEquals(-60L, field.roundFloor(-1L));
        assertEquals(0L, field.roundFloor(0L));
        assertEquals(0L, field.roundFloor(1L));
        assertEquals(0L, field.roundFloor(29L));
        assertEquals(0L, field.roundFloor(30L));
        assertEquals(0L, field.roundFloor(31L));
        assertEquals(60L, field.roundFloor(60L));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_roundCeiling_long
    public void test_roundCeiling_long() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(-60L, field.roundCeiling(-61L));
        assertEquals(-60L, field.roundCeiling(-60L));
        assertEquals(0L, field.roundCeiling(-59L));
        assertEquals(0L, field.roundCeiling(-1L));
        assertEquals(0L, field.roundCeiling(0L));
        assertEquals(60L, field.roundCeiling(1L));
        assertEquals(60L, field.roundCeiling(29L));
        assertEquals(60L, field.roundCeiling(30L));
        assertEquals(60L, field.roundCeiling(31L));
        assertEquals(60L, field.roundCeiling(60L));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_roundHalfFloor_long
    public void test_roundHalfFloor_long() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(0L, field.roundHalfFloor(0L));
        assertEquals(0L, field.roundHalfFloor(29L));
        assertEquals(0L, field.roundHalfFloor(30L));
        assertEquals(60L, field.roundHalfFloor(31L));
        assertEquals(60L, field.roundHalfFloor(60L));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_roundHalfCeiling_long
    public void test_roundHalfCeiling_long() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(0L, field.roundHalfCeiling(0L));
        assertEquals(0L, field.roundHalfCeiling(29L));
        assertEquals(60L, field.roundHalfCeiling(30L));
        assertEquals(60L, field.roundHalfCeiling(31L));
        assertEquals(60L, field.roundHalfCeiling(60L));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_roundHalfEven_long
    public void test_roundHalfEven_long() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(0L, field.roundHalfEven(0L));
        assertEquals(0L, field.roundHalfEven(29L));
        assertEquals(0L, field.roundHalfEven(30L));
        assertEquals(60L, field.roundHalfEven(31L));
        assertEquals(60L, field.roundHalfEven(60L));
        assertEquals(60L, field.roundHalfEven(89L));
        assertEquals(120L, field.roundHalfEven(90L));
        assertEquals(120L, field.roundHalfEven(91L));
    }

// org.joda.time.field.TestPreciseDurationDateTimeField::test_remainder_long
    public void test_remainder_long() {
        BaseDateTimeField field = new MockPreciseDurationDateTimeField();
        assertEquals(0L, field.remainder(0L));
        assertEquals(29L, field.remainder(29L));
        assertEquals(30L, field.remainder(30L));
        assertEquals(31L, field.remainder(31L));
        assertEquals(0L, field.remainder(60L));
    }

// org.joda.time.field.TestPreciseDurationField::test_constructor
    public void test_constructor() {
        try {
            new PreciseDurationField(null, 10);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestPreciseDurationField::test_getType
    public void test_getType() {
        assertEquals(DurationFieldType.seconds(), iField.getType());
    }

// org.joda.time.field.TestPreciseDurationField::test_getName
    public void test_getName() {
        assertEquals("seconds", iField.getName());
    }

// org.joda.time.field.TestPreciseDurationField::test_isSupported
    public void test_isSupported() {
        assertEquals(true, iField.isSupported());
    }

// org.joda.time.field.TestPreciseDurationField::test_isPrecise
    public void test_isPrecise() {
        assertEquals(true, iField.isPrecise());
    }

// org.joda.time.field.TestPreciseDurationField::test_getUnitMillis
    public void test_getUnitMillis() {
        assertEquals(1000, iField.getUnitMillis());
    }

// org.joda.time.field.TestPreciseDurationField::test_toString
    public void test_toString() {
        assertEquals("DurationField[seconds]", iField.toString());
    }

// org.joda.time.field.TestPreciseDurationField::test_getValue_long
    public void test_getValue_long() {
        assertEquals(0, iField.getValue(0L));
        assertEquals(12345, iField.getValue(12345678L));
        assertEquals(-1, iField.getValue(-1234L));
        assertEquals(INTEGER_MAX, iField.getValue(LONG_INTEGER_MAX * 1000L + 999L));
        try {
            iField.getValue(LONG_INTEGER_MAX * 1000L + 1000L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestPreciseDurationField::test_getValueAsLong_long
    public void test_getValueAsLong_long() {
        assertEquals(0L, iField.getValueAsLong(0L));
        assertEquals(12345L, iField.getValueAsLong(12345678L));
        assertEquals(-1L, iField.getValueAsLong(-1234L));
        assertEquals(LONG_INTEGER_MAX + 1L, iField.getValueAsLong(LONG_INTEGER_MAX * 1000L + 1000L));
    }

// org.joda.time.field.TestPreciseDurationField::test_getValue_long_long
    public void test_getValue_long_long() {
        assertEquals(0, iField.getValue(0L, 567L));
        assertEquals(12345, iField.getValue(12345678L, 567L));
        assertEquals(-1, iField.getValue(-1234L, 567L));
        assertEquals(INTEGER_MAX, iField.getValue(LONG_INTEGER_MAX * 1000L + 999L, 567L));
        try {
            iField.getValue(LONG_INTEGER_MAX * 1000L + 1000L, 567L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestPreciseDurationField::test_getValueAsLong_long_long
    public void test_getValueAsLong_long_long() {
        assertEquals(0L, iField.getValueAsLong(0L, 567L));
        assertEquals(12345L, iField.getValueAsLong(12345678L, 567L));
        assertEquals(-1L, iField.getValueAsLong(-1234L, 567L));
        assertEquals(LONG_INTEGER_MAX + 1L, iField.getValueAsLong(LONG_INTEGER_MAX * 1000L + 1000L, 567L));
    }

// org.joda.time.field.TestPreciseDurationField::test_getMillis_int
    public void test_getMillis_int() {
        assertEquals(0, iField.getMillis(0));
        assertEquals(1234000L, iField.getMillis(1234));
        assertEquals(-1234000L, iField.getMillis(-1234));
        assertEquals(LONG_INTEGER_MAX * 1000L, iField.getMillis(INTEGER_MAX));
    }

// org.joda.time.field.TestPreciseDurationField::test_getMillis_long
    public void test_getMillis_long() {
        assertEquals(0L, iField.getMillis(0L));
        assertEquals(1234000L, iField.getMillis(1234L));
        assertEquals(-1234000L, iField.getMillis(-1234L));
        try {
            iField.getMillis(LONG_MAX);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestPreciseDurationField::test_getMillis_int_long
    public void test_getMillis_int_long() {
        assertEquals(0L, iField.getMillis(0, 567L));
        assertEquals(1234000L, iField.getMillis(1234, 567L));
        assertEquals(-1234000L, iField.getMillis(-1234, 567L));
        assertEquals(LONG_INTEGER_MAX * 1000L, iField.getMillis(INTEGER_MAX, 567L));
    }

// org.joda.time.field.TestPreciseDurationField::test_getMillis_long_long
    public void test_getMillis_long_long() {
        assertEquals(0L, iField.getMillis(0L, 567L));
        assertEquals(1234000L, iField.getMillis(1234L, 567L));
        assertEquals(-1234000L, iField.getMillis(-1234L, 567L));
        try {
            iField.getMillis(LONG_MAX, 567L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestPreciseDurationField::test_add_long_int
    public void test_add_long_int() {
        assertEquals(567L, iField.add(567L, 0));
        assertEquals(567L + 1234000L, iField.add(567L, 1234));
        assertEquals(567L - 1234000L, iField.add(567L, -1234));
        try {
            iField.add(LONG_MAX, 1);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestPreciseDurationField::test_add_long_long
    public void test_add_long_long() {
        assertEquals(567L, iField.add(567L, 0L));
        assertEquals(567L + 1234000L, iField.add(567L, 1234L));
        assertEquals(567L - 1234000L, iField.add(567L, -1234L));
        try {
            iField.add(LONG_MAX, 1L);
            fail();
        } catch (ArithmeticException ex) {}
        try {
            iField.add(1L, LONG_MAX);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestPreciseDurationField::test_getDifference_long_int
    public void test_getDifference_long_int() {
        assertEquals(0, iField.getDifference(1L, 0L));
        assertEquals(567, iField.getDifference(567000L, 0L));
        assertEquals(567 - 1234, iField.getDifference(567000L, 1234000L));
        assertEquals(567 + 1234, iField.getDifference(567000L, -1234000L));
        try {
            iField.getDifference(LONG_MAX, -1L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestPreciseDurationField::test_getDifferenceAsLong_long_long
    public void test_getDifferenceAsLong_long_long() {
        assertEquals(0L, iField.getDifferenceAsLong(1L, 0L));
        assertEquals(567L, iField.getDifferenceAsLong(567000L, 0L));
        assertEquals(567L - 1234L, iField.getDifferenceAsLong(567000L, 1234000L));
        assertEquals(567L + 1234L, iField.getDifferenceAsLong(567000L, -1234000L));
        try {
            iField.getDifferenceAsLong(LONG_MAX, -1L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestPreciseDurationField::test_equals
    public void test_equals() {
        assertEquals(true, iField.equals(iField));
        assertEquals(false, iField.equals(ISOChronology.getInstance().minutes()));
        DurationField dummy = new PreciseDurationField(DurationFieldType.seconds(), 0);
        assertEquals(false, iField.equals(dummy));
        dummy = new PreciseDurationField(DurationFieldType.seconds(), 1000);
        assertEquals(true, iField.equals(dummy));
        dummy = new PreciseDurationField(DurationFieldType.millis(), 1000);
        assertEquals(false, iField.equals(dummy));
        assertEquals(false, iField.equals(""));
        assertEquals(false, iField.equals(null));
    }

// org.joda.time.field.TestPreciseDurationField::test_hashCode
    public void test_hashCode() {
        assertEquals(true, iField.hashCode() == iField.hashCode());
        assertEquals(false, iField.hashCode() == ISOChronology.getInstance().minutes().hashCode());
        DurationField dummy = new PreciseDurationField(DurationFieldType.seconds(), 0);
        assertEquals(false, iField.hashCode() == dummy.hashCode());
        dummy = new PreciseDurationField(DurationFieldType.seconds(), 1000);
        assertEquals(true, iField.hashCode() == dummy.hashCode());
        dummy = new PreciseDurationField(DurationFieldType.millis(), 1000);
        assertEquals(false, iField.hashCode() == dummy.hashCode());
    }

// org.joda.time.field.TestPreciseDurationField::test_compareTo
    public void test_compareTo() {
        assertEquals(0, iField.compareTo(iField));
        assertEquals(-1, iField.compareTo(ISOChronology.getInstance().minutes()));
        DurationField dummy = new PreciseDurationField(DurationFieldType.seconds(), 0);
        assertEquals(1, iField.compareTo(dummy));

        try {
            iField.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}
    }

// org.joda.time.field.TestPreciseDurationField::testSerialization
    public void testSerialization() throws Exception {
        DurationField test = iField;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DurationField result = (DurationField) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.field.TestScaledDurationField::test_constructor
    public void test_constructor() {
        try {
            new ScaledDurationField(null, DurationFieldType.minutes(), 10);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new ScaledDurationField(MillisDurationField.INSTANCE, null, 10);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new ScaledDurationField(MillisDurationField.INSTANCE, DurationFieldType.minutes(), 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new ScaledDurationField(MillisDurationField.INSTANCE, DurationFieldType.minutes(), 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestScaledDurationField::test_getScalar
    public void test_getScalar() {
        assertEquals(90, iField.getScalar());
    }

// org.joda.time.field.TestScaledDurationField::test_getType
    public void test_getType() {
        assertEquals(DurationFieldType.minutes(), iField.getType());
    }

// org.joda.time.field.TestScaledDurationField::test_getName
    public void test_getName() {
        assertEquals("minutes", iField.getName());
    }

// org.joda.time.field.TestScaledDurationField::test_isSupported
    public void test_isSupported() {
        assertEquals(true, iField.isSupported());
    }

// org.joda.time.field.TestScaledDurationField::test_isPrecise
    public void test_isPrecise() {
        assertEquals(true, iField.isPrecise());
    }

// org.joda.time.field.TestScaledDurationField::test_getUnitMillis
    public void test_getUnitMillis() {
        assertEquals(90, iField.getUnitMillis());
    }

// org.joda.time.field.TestScaledDurationField::test_toString
    public void test_toString() {
        assertEquals("DurationField[minutes]", iField.toString());
    }

// org.joda.time.field.TestScaledDurationField::test_getValue_long
    public void test_getValue_long() {
        assertEquals(0, iField.getValue(0L));
        assertEquals(12345678 / 90, iField.getValue(12345678L));
        assertEquals(-1234 / 90, iField.getValue(-1234L));
        assertEquals(INTEGER_MAX / 90, iField.getValue(LONG_INTEGER_MAX));
        try {
            iField.getValue(LONG_INTEGER_MAX + 1L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestScaledDurationField::test_getValueAsLong_long
    public void test_getValueAsLong_long() {
        assertEquals(0L, iField.getValueAsLong(0L));
        assertEquals(12345678L / 90, iField.getValueAsLong(12345678L));
        assertEquals(-1234 / 90L, iField.getValueAsLong(-1234L));
        assertEquals(LONG_INTEGER_MAX + 1L, iField.getValueAsLong(LONG_INTEGER_MAX * 90L + 90L));
    }

// org.joda.time.field.TestScaledDurationField::test_getValue_long_long
    public void test_getValue_long_long() {
        assertEquals(0, iField.getValue(0L, 567L));
        assertEquals(12345678 / 90, iField.getValue(12345678L, 567L));
        assertEquals(-1234 / 90, iField.getValue(-1234L, 567L));
        assertEquals(INTEGER_MAX / 90, iField.getValue(LONG_INTEGER_MAX, 567L));
        try {
            iField.getValue(LONG_INTEGER_MAX + 1L, 567L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestScaledDurationField::test_getValueAsLong_long_long
    public void test_getValueAsLong_long_long() {
        assertEquals(0L, iField.getValueAsLong(0L, 567L));
        assertEquals(12345678 / 90L, iField.getValueAsLong(12345678L, 567L));
        assertEquals(-1234 / 90L, iField.getValueAsLong(-1234L, 567L));
        assertEquals(LONG_INTEGER_MAX + 1L, iField.getValueAsLong(LONG_INTEGER_MAX * 90L + 90L, 567L));
    }

// org.joda.time.field.TestScaledDurationField::test_getMillis_int
    public void test_getMillis_int() {
        assertEquals(0, iField.getMillis(0));
        assertEquals(1234L * 90L, iField.getMillis(1234));
        assertEquals(-1234L * 90L, iField.getMillis(-1234));
        assertEquals(LONG_INTEGER_MAX * 90L, iField.getMillis(INTEGER_MAX));
    }

// org.joda.time.field.TestScaledDurationField::test_getMillis_long
    public void test_getMillis_long() {
        assertEquals(0L, iField.getMillis(0L));
        assertEquals(1234L * 90L, iField.getMillis(1234L));
        assertEquals(-1234L * 90L, iField.getMillis(-1234L));
        try {
            iField.getMillis(LONG_MAX);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestScaledDurationField::test_getMillis_int_long
    public void test_getMillis_int_long() {
        assertEquals(0L, iField.getMillis(0, 567L));
        assertEquals(1234L * 90L, iField.getMillis(1234, 567L));
        assertEquals(-1234L * 90L, iField.getMillis(-1234, 567L));
        assertEquals(LONG_INTEGER_MAX * 90L, iField.getMillis(INTEGER_MAX, 567L));
    }

// org.joda.time.field.TestScaledDurationField::test_getMillis_long_long
    public void test_getMillis_long_long() {
        assertEquals(0L, iField.getMillis(0L, 567L));
        assertEquals(1234L * 90L, iField.getMillis(1234L, 567L));
        assertEquals(-1234L * 90L, iField.getMillis(-1234L, 567L));
        try {
            iField.getMillis(LONG_MAX, 567L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestScaledDurationField::test_add_long_int
    public void test_add_long_int() {
        assertEquals(567L, iField.add(567L, 0));
        assertEquals(567L + 1234L * 90L, iField.add(567L, 1234));
        assertEquals(567L - 1234L * 90L, iField.add(567L, -1234));
        try {
            iField.add(LONG_MAX, 1);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestScaledDurationField::test_add_long_long
    public void test_add_long_long() {
        assertEquals(567L, iField.add(567L, 0L));
        assertEquals(567L + 1234L * 90L, iField.add(567L, 1234L));
        assertEquals(567L - 1234L * 90L, iField.add(567L, -1234L));
        try {
            iField.add(LONG_MAX, 1L);
            fail();
        } catch (ArithmeticException ex) {}
        try {
            iField.add(1L, LONG_MAX);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestScaledDurationField::test_getDifference_long_int
    public void test_getDifference_long_int() {
        assertEquals(0, iField.getDifference(1L, 0L));
        assertEquals(567, iField.getDifference(567L * 90L, 0L));
        assertEquals(567 - 1234, iField.getDifference(567L * 90L, 1234L * 90L));
        assertEquals(567 + 1234, iField.getDifference(567L * 90L, -1234L * 90L));
        try {
            iField.getDifference(LONG_MAX, -1L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestScaledDurationField::test_getDifferenceAsLong_long_long
    public void test_getDifferenceAsLong_long_long() {
        assertEquals(0L, iField.getDifferenceAsLong(1L, 0L));
        assertEquals(567L, iField.getDifferenceAsLong(567L * 90L, 0L));
        assertEquals(567L - 1234L, iField.getDifferenceAsLong(567L * 90L, 1234L * 90L));
        assertEquals(567L + 1234L, iField.getDifferenceAsLong(567L * 90L, -1234L * 90L));
        try {
            iField.getDifferenceAsLong(LONG_MAX, -1L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestScaledDurationField::test_equals
    public void test_equals() {
        assertEquals(true, iField.equals(iField));
        assertEquals(false, iField.equals(ISOChronology.getInstance().minutes()));
        DurationField dummy = new ScaledDurationField(MillisDurationField.INSTANCE, DurationFieldType.minutes(), 2);
        assertEquals(false, iField.equals(dummy));
        dummy = new ScaledDurationField(MillisDurationField.INSTANCE, DurationFieldType.minutes(), 90);
        assertEquals(true, iField.equals(dummy));
        dummy = new ScaledDurationField(MillisDurationField.INSTANCE, DurationFieldType.millis(), 90);
        assertEquals(false, iField.equals(dummy));
        assertEquals(false, iField.equals(""));
        assertEquals(false, iField.equals(null));
    }

// org.joda.time.field.TestScaledDurationField::test_hashCode
    public void test_hashCode() {
        assertEquals(iField.hashCode(), iField.hashCode());
        assertEquals(false, iField.hashCode() == ISOChronology.getInstance().minutes().hashCode());
        DurationField dummy = new ScaledDurationField(MillisDurationField.INSTANCE, DurationFieldType.minutes(), 2);
        assertEquals(false, iField.hashCode() == dummy.hashCode());
        dummy = new ScaledDurationField(MillisDurationField.INSTANCE, DurationFieldType.minutes(), 90);
        assertEquals(true, iField.hashCode() == dummy.hashCode());
        dummy = new ScaledDurationField(MillisDurationField.INSTANCE, DurationFieldType.millis(), 90);
        assertEquals(false, iField.hashCode() == dummy.hashCode());
    }

// org.joda.time.field.TestScaledDurationField::test_compareTo
    public void test_compareTo() {
        assertEquals(0, iField.compareTo(iField));
        assertEquals(-1, iField.compareTo(ISOChronology.getInstance().minutes()));
        DurationField dummy = new PreciseDurationField(DurationFieldType.minutes(), 0);
        assertEquals(1, iField.compareTo(dummy));

        try {
            iField.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}
    }

// org.joda.time.field.TestScaledDurationField::testSerialization
    public void testSerialization() throws Exception {
        DurationField test = iField;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DurationField result = (DurationField) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.field.TestUnsupportedDateTimeField::testNullValuesToGetInstanceThrowsException
    public void testNullValuesToGetInstanceThrowsException() {

        try {
            UnsupportedDateTimeField.getInstance(null, null);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

// org.joda.time.field.TestUnsupportedDateTimeField::testDifferentDurationReturnDifferentObjects
    public void testDifferentDurationReturnDifferentObjects() {

        
        DateTimeField fieldOne = UnsupportedDateTimeField.getInstance(
                dateTimeFieldTypeOne, UnsupportedDurationField
                        .getInstance(weeks));
        DateTimeField fieldTwo = UnsupportedDateTimeField.getInstance(
                dateTimeFieldTypeOne, UnsupportedDurationField
                        .getInstance(weeks));
        assertSame(fieldOne, fieldTwo);

        
        DateTimeField fieldThree = UnsupportedDateTimeField.getInstance(
                dateTimeFieldTypeOne, UnsupportedDurationField
                        .getInstance(months));
        assertNotSame(fieldOne, fieldThree);
    }

// org.joda.time.field.TestUnsupportedDateTimeField::testPublicGetNameMethod
    public void testPublicGetNameMethod() {
        DateTimeField fieldOne = UnsupportedDateTimeField.getInstance(
                dateTimeFieldTypeOne, UnsupportedDurationField
                        .getInstance(weeks));

        assertSame(fieldOne.getName(), dateTimeFieldTypeOne.getName());
    }

// org.joda.time.field.TestUnsupportedDateTimeField::testAlwaysFalseReturnTypes
    public void testAlwaysFalseReturnTypes() {
        DateTimeField fieldOne = UnsupportedDateTimeField.getInstance(
                dateTimeFieldTypeOne, UnsupportedDurationField
                        .getInstance(weeks));
        assertFalse(fieldOne.isLenient());
        assertFalse(fieldOne.isSupported());
    }

// org.joda.time.field.TestUnsupportedDateTimeField::testMethodsThatShouldAlwaysReturnNull
    public void testMethodsThatShouldAlwaysReturnNull() {
        DateTimeField fieldOne = UnsupportedDateTimeField.getInstance(
                dateTimeFieldTypeOne, UnsupportedDurationField
                        .getInstance(weeks));

        assertNull(fieldOne.getLeapDurationField());
        assertNull(fieldOne.getRangeDurationField());
    }

// org.joda.time.field.TestUnsupportedDateTimeField::testUnsupportedMethods
    public void testUnsupportedMethods() {
        DateTimeField fieldOne = UnsupportedDateTimeField.getInstance(
                dateTimeFieldTypeOne, UnsupportedDurationField
                        .getInstance(weeks));

        
        
        try {
            fieldOne.add(localTime, 0, new int[] { 0, 100 }, 100);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        
        try {
            fieldOne.addWrapField(100000L, 250);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        
        
        try {
            fieldOne.addWrapField(localTime, 0, new int[] { 0, 100 }, 100);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        
        
        try {
            fieldOne.addWrapPartial(localTime, 0, new int[] { 0, 100 }, 100);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        
        try {
            fieldOne.get(1000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.getAsShortText(0, Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        try {
            fieldOne.getAsShortText(100000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        try {
            fieldOne.getAsShortText(100000L, Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.getAsShortText(localTime, 0, Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.getAsShortText(localTime, Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.getAsText(0, Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        try {
            fieldOne.getAsText(1000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        try {
            fieldOne.getAsText(1000L, Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.getAsText(localTime, 0, Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.getAsText(localTime, Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.getLeapAmount(System.currentTimeMillis());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.getMaximumShortTextLength(Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.getMaximumTextLength(Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.getMaximumValue();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.getMaximumValue(1000000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.getMaximumValue(localTime);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        
        try {
            fieldOne.getMaximumValue(localTime, new int[] { 0 });
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.getMinimumValue();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.getMinimumValue(10000000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.getMinimumValue(localTime);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.getMinimumValue(localTime, new int[] { 0 });
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.isLeap(System.currentTimeMillis());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.remainder(1000000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.roundCeiling(1000000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        try {
            fieldOne.roundFloor(1000000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.roundHalfCeiling(1000000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.roundHalfEven(1000000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.roundHalfFloor(1000000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.set(1000000L, 1000);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        try {
            fieldOne.set(1000000L, "Unsupported Operation");
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        
        try {
            fieldOne
                    .set(1000000L, "Unsupported Operation", Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        
        
        try {
            fieldOne.set(localTime, 0, new int[] { 0 }, 10000);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        
        
        
        
        
        
        try {
            fieldOne.set(localTime, 0, new int[] { 0 },
                    "Unsupported Operation", Locale.getDefault());
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
    }

// org.joda.time.field.TestUnsupportedDateTimeField::testDelegatedMethods
    public void testDelegatedMethods() {
        DateTimeField fieldOne = UnsupportedDateTimeField.getInstance(
                dateTimeFieldTypeOne, UnsupportedDurationField
                        .getInstance(weeks));
        PreciseDurationField hoursDuration = new PreciseDurationField(
                DurationFieldType.hours(), 10L);
        DateTimeField fieldTwo = UnsupportedDateTimeField.getInstance(
                dateTimeFieldTypeOne, hoursDuration);

        
        
        
        
        
        try {
            fieldOne.add(System.currentTimeMillis(), 100);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        try {
            long currentTime = System.currentTimeMillis();
            long firstComputation = hoursDuration.add(currentTime, 100);
            long secondComputation = fieldTwo.add(currentTime,
                    100);
            assertEquals(firstComputation,secondComputation);
        } catch (UnsupportedOperationException e) {
            assertTrue(false);
        }

        
        
        
        
        
        try {
            fieldOne.add(System.currentTimeMillis(), 1000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        try {
            long currentTime = System.currentTimeMillis();
            long firstComputation = hoursDuration.add(currentTime, 1000L);
            long secondComputation = fieldTwo.add(currentTime,
                    1000L);
            assertTrue(firstComputation == secondComputation);
            assertEquals(firstComputation,secondComputation);
        } catch (UnsupportedOperationException e) {
            assertTrue(false);
        }

        
        
        
        
        
        try {
            fieldOne.getDifference(100000L, 1000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        try {
            int firstDifference = hoursDuration.getDifference(100000L, 1000L);
            int secondDifference = fieldTwo.getDifference(100000L, 1000L);
            assertEquals(firstDifference,secondDifference);
        } catch (UnsupportedOperationException e) {
            assertTrue(false);
        }

        
        
        
        
        
        try {
            fieldOne.getDifferenceAsLong(100000L, 1000L);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        try {
            long firstDifference = hoursDuration.getDifference(100000L, 1000L);
            long secondDifference = fieldTwo.getDifference(100000L, 1000L);
            assertEquals(firstDifference,secondDifference);
        } catch (UnsupportedOperationException e) {
            assertTrue(false);
        }
    }

// org.joda.time.field.TestUnsupportedDateTimeField::testToString
    public void testToString() {
        DateTimeField fieldOne = UnsupportedDateTimeField.getInstance(
                dateTimeFieldTypeOne, UnsupportedDurationField
                        .getInstance(weeks));

        String debugMessage = fieldOne.toString();
        assertNotNull(debugMessage);
        assertTrue(debugMessage.length() > 0);
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

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanLongUpperCase_UK
    public void testFormatParse_textMonthJanLongUpperCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 JANUARY 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShort_France
    public void testFormatParse_textMonthJanShort_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 1, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 janv. 2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanLong_France
    public void testFormatParse_textMonthJanLong_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("23 janvier 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthApr_France
    public void testFormatParse_textMonthApr_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 2, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 f\u00E9vr. 2007", str);  
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 2, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthAtEnd_France
    public void testFormatParse_textMonthAtEnd_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 juin", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2000, 6, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthAtEnd_France_withSpecifiedDefault
    public void testFormatParse_textMonthAtEnd_France_withSpecifiedDefault() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM")
            .withLocale(Locale.FRANCE).withZoneUTC().withDefaultYear(1980);
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 juin", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 1980, 6, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthApr_Korean
    public void testFormatParse_textMonthApr_Korean() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("EEEE, d MMMM yyyy HH:mm")
            .withLocale(Locale.KOREAN).withZoneUTC();
        
        String str = new DateTime(2007, 3, 8, 22, 0, 0, 0, UTC).toString(dateFormatter);
        DateTime date = dateFormatter.parseDateTime(str);
        assertEquals(new DateTime(2007, 3, 8, 22, 0, 0, 0, UTC), date);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textHalfdayAM_UK
    public void testFormatParse_textHalfdayAM_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendClockhourOfHalfday(2)
            .appendLiteral('-')
            .appendHalfdayOfDayText()
            .appendLiteral('-')
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 18, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$06-pm-2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textHalfdayAM_France
    public void testFormatParse_textHalfdayAM_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendClockhourOfHalfday(2)
            .appendLiteral('-')
            .appendHalfdayOfDayText()
            .appendLiteral('-')
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 18, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$06-PM-2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textEraAD_UK
    public void testFormatParse_textEraAD_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendEraText()
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$AD2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textEraAD_France
    public void testFormatParse_textEraAD_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendEraText()
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$ap. J.-C.2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textEraBC_France
    public void testFormatParse_textEraBC_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendEraText()
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(-1, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$av. J.-C.-0001", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, -1, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textYear_UK
    public void testFormatParse_textYear_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendText(DateTimeFieldType.year())
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$2007", str);
        try {
            dateFormatter.parseDateTime(str);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textYear_France
    public void testFormatParse_textYear_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendText(DateTimeFieldType.year())
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$2007", str);
        try {
            dateFormatter.parseDateTime(str);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textAdjoiningHelloWorld_UK
    public void testFormatParse_textAdjoiningHelloWorld_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendDayOfMonth(2)
            .appendMonthOfYearShortText()
            .appendLiteral("HelloWorld")
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$23JunHelloWorld", str);
        dateFormatter.parseDateTime(str);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textAdjoiningMonthDOW_UK
    public void testFormatParse_textAdjoiningMonthDOW_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendDayOfMonth(2)
            .appendMonthOfYearShortText()
            .appendDayOfWeekShortText()
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$23JunSat", str);
        dateFormatter.parseDateTime(str);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_noColon
    public void testFormatParse_zoneId_noColon() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm Z").withZoneUTC();
        String str = new DateTime(2007, 6, 23, 1, 2, 0, 0, UTC).toString(dateFormatter);
        assertEquals("01:02 +0000", str);
        DateTime parsed = dateFormatter.parseDateTime(str);
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_noColon_parseZ
    public void testFormatParse_zoneId_noColon_parseZ() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm Z").withZoneUTC();
        DateTime parsed = dateFormatter.parseDateTime("01:02 Z");
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_colon
    public void testFormatParse_zoneId_colon() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm ZZ").withZoneUTC();
        String str = new DateTime(2007, 6, 23, 1, 2, 0, 0, UTC).toString(dateFormatter);
        assertEquals("01:02 +00:00", str);
        DateTime parsed = dateFormatter.parseDateTime(str);
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_colon_parseZ
    public void testFormatParse_zoneId_colon_parseZ() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm ZZ").withZoneUTC();
        DateTime parsed = dateFormatter.parseDateTime("01:02 Z");
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_stringLengths
    public void testForStyle_stringLengths() {
        try {
            DateTimeFormat.forStyle(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("S");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("SSS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_invalidStrings
    public void testForStyle_invalidStrings() {
        try {
            DateTimeFormat.forStyle("AA");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("--");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("ss");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortDate
    public void testForStyle_shortDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("S-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        DateTime date = new DateTime(
                DateFormat.getDateInstance(DateFormat.SHORT, FRANCE).parse(expect));
        assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortTime
    public void testForStyle_shortTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("-S");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getTimeInstance(DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        if (TimeZone.getDefault() instanceof SimpleTimeZone) {
            
        } else {
            DateTime date = new DateTime(
                DateFormat.getTimeInstance(DateFormat.SHORT, FRANCE).parse(expect));
            assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
        }
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortDateTime
    public void testForStyle_shortDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortDateTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("SS");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        DateTime date = new DateTime(
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, FRANCE).parse(expect));
        assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumDate
    public void testForStyle_mediumDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("M-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumTime
    public void testForStyle_mediumTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("-M");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumDateTime
    public void testForStyle_mediumDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumDateTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("MM");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longDate
    public void testForStyle_longDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.longDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("L-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.LONG, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.LONG, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.LONG, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longTime
    public void testForStyle_longTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longDateTime
    public void testForStyle_longDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullDate
    public void testForStyle_fullDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.fullDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("F-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.FULL, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.FULL, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.FULL, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullTime
    public void testForStyle_fullTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullDateTime
    public void testForStyle_fullDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortMediumDateTime
    public void testForStyle_shortMediumDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.forStyle("SM");
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortLongDateTime
    public void testForStyle_shortLongDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortFullDateTime
    public void testForStyle_shortFullDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumShortDateTime
    public void testForStyle_mediumShortDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.forStyle("MS");
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumLongDateTime
    public void testForStyle_mediumLongDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumFullDateTime
    public void testForStyle_mediumFullDateTime() {}

// org.joda.time.format.TestDateTimeFormatter::testPrint_simple
    public void testPrint_simple() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T10:20:30Z", f.print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_locale
    public void testPrint_locale() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("mer. 2004-06-09T10:20:30Z", f.withLocale(Locale.FRENCH).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withLocale(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_zone
    public void testPrint_zone() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withZone(null).print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withZoneUTC().print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_chrono
    public void testPrint_chrono() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withChronology(ISO_PARIS).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(BUDDHIST_PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withChronology(null).print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withChronology(ISO_PARIS).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(BUDDHIST_PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withChronology(ISO_UTC).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_bufferMethods
    public void testPrint_bufferMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", buf.toString());
        
        buf = new StringBuffer();
        f.printTo(buf, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", buf.toString());
        
        buf = new StringBuffer();
        ISODateTimeFormat.yearMonthDay().printTo(buf, dt.toYearMonthDay());
        assertEquals("2004-06-09", buf.toString());
        
        buf = new StringBuffer();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(buf, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_writerMethods
    public void testPrint_writerMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        CharArrayWriter out = new CharArrayWriter();
        f.printTo(out, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", out.toString());
        
        out = new CharArrayWriter();
        f.printTo(out, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", out.toString());
        
        out = new CharArrayWriter();
        ISODateTimeFormat.yearMonthDay().printTo(out, dt.toYearMonthDay());
        assertEquals("2004-06-09", out.toString());
        
        out = new CharArrayWriter();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(out, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_appendableMethods
    public void testPrint_appendableMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        StringBuilder buf = new StringBuilder();
        f.printTo(buf, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", buf.toString());
        
        buf = new StringBuilder();
        f.printTo(buf, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", buf.toString());
        
        buf = new StringBuilder();
        ISODateTimeFormat.yearMonthDay().printTo(buf, dt.toLocalDate());
        assertEquals("2004-06-09", buf.toString());
        
        buf = new StringBuilder();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(buf, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_chrono_and_zone
    public void testPrint_chrono_and_zone() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T10:20:30Z",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
        
        dt = dt.withChronology(ISO_PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2547-06-09T12:20:30+02:00",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2547-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetLocale
    public void testWithGetLocale() {
        DateTimeFormatter f2 = f.withLocale(Locale.FRENCH);
        assertEquals(Locale.FRENCH, f2.getLocale());
        assertSame(f2, f2.withLocale(Locale.FRENCH));
        
        f2 = f.withLocale(null);
        assertEquals(null, f2.getLocale());
        assertSame(f2, f2.withLocale(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetZone
    public void testWithGetZone() {
        DateTimeFormatter f2 = f.withZone(PARIS);
        assertEquals(PARIS, f2.getZone());
        assertSame(f2, f2.withZone(PARIS));
        
        f2 = f.withZone(null);
        assertEquals(null, f2.getZone());
        assertSame(f2, f2.withZone(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetChronology
    public void testWithGetChronology() {
        DateTimeFormatter f2 = f.withChronology(BUDDHIST_PARIS);
        assertEquals(BUDDHIST_PARIS, f2.getChronology());
        assertSame(f2, f2.withChronology(BUDDHIST_PARIS));
        
        f2 = f.withChronology(null);
        assertEquals(null, f2.getChronology());
        assertSame(f2, f2.withChronology(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetPivotYear
    public void testWithGetPivotYear() {
        DateTimeFormatter f2 = f.withPivotYear(13);
        assertEquals(new Integer(13), f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(13));
        
        f2 = f.withPivotYear(new Integer(14));
        assertEquals(new Integer(14), f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(new Integer(14)));
        
        f2 = f.withPivotYear(null);
        assertEquals(null, f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetOffsetParsedMethods
    public void testWithGetOffsetParsedMethods() {
        DateTimeFormatter f2 = f;
        assertEquals(false, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f.withOffsetParsed();
        assertEquals(true, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f2.withZone(PARIS);
        assertEquals(false, f2.isOffsetParsed());
        assertEquals(PARIS, f2.getZone());
        
        f2 = f2.withOffsetParsed();
        assertEquals(true, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f.withOffsetParsed();
        assertNotSame(f, f2);
        DateTimeFormatter f3 = f2.withOffsetParsed();
        assertSame(f2, f3);
    }

// org.joda.time.format.TestDateTimeFormatter::testPrinterParserMethods
    public void testPrinterParserMethods() {
        DateTimeFormatter f2 = new DateTimeFormatter(f.getPrinter(), f.getParser());
        assertEquals(f.getPrinter(), f2.getPrinter());
        assertEquals(f.getParser(), f2.getParser());
        assertEquals(true, f2.isPrinter());
        assertEquals(true, f2.isParser());
        assertNotNull(f2.print(0L));
        assertNotNull(f2.parseDateTime("Thu 1970-01-01T00:00:00Z"));
        
        f2 = new DateTimeFormatter(f.getPrinter(), null);
        assertEquals(f.getPrinter(), f2.getPrinter());
        assertEquals(null, f2.getParser());
        assertEquals(true, f2.isPrinter());
        assertEquals(false, f2.isParser());
        assertNotNull(f2.print(0L));
        try {
            f2.parseDateTime("Thu 1970-01-01T00:00:00Z");
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        f2 = new DateTimeFormatter(null, f.getParser());
        assertEquals(null, f2.getPrinter());
        assertEquals(f.getParser(), f2.getParser());
        assertEquals(false, f2.isPrinter());
        assertEquals(true, f2.isParser());
        try {
            f2.print(0L);
            fail();
        } catch (UnsupportedOperationException ex) {}
        assertNotNull(f2.parseDateTime("Thu 1970-01-01T00:00:00Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_simple
    public void testParseLocalDate_simple() {
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30Z"));
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalDate(2004, 6, 9, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalDate("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_yearOfEra
    public void testParseLocalDate_yearOfEra() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("YYYY-MM GG")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        LocalDate date = new LocalDate(2005, 10, 1, chrono);
        assertEquals(date, f.parseLocalDate("2005-10 AD"));
        assertEquals(date, f.parseLocalDate("2005-10 CE"));
        
        date = new LocalDate(-2005, 10, 1, chrono);
        assertEquals(date, f.parseLocalDate("2005-10 BC"));
        assertEquals(date, f.parseLocalDate("2005-10 BCE"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_yearOfCentury
    public void testParseLocalDate_yearOfCentury() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("yy M d")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withPivotYear(2050);
        
        LocalDate date = new LocalDate(2050, 8, 4, chrono);
        assertEquals(date, f.parseLocalDate("50 8 4"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_monthDay_feb29
    public void testParseLocalDate_monthDay_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        assertEquals(new LocalDate(2000, 2, 29, chrono), f.parseLocalDate("2 29"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_monthDay_withDefaultYear_feb29
    public void testParseLocalDate_monthDay_withDefaultYear_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withDefaultYear(2012);
        
        assertEquals(new LocalDate(2012, 2, 29, chrono), f.parseLocalDate("2 29"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalTime_simple
    public void testParseLocalTime_simple() {
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30Z"));
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalTime(10, 20, 30, 0, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalTime("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_simple
    public void testParseLocalDateTime_simple() {
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30Z"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 0, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalDateTime("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_monthDay_feb29
    public void testParseLocalDateTime_monthDay_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d H m")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        assertEquals(new LocalDateTime(2000, 2, 29, 13, 40, 0, 0, chrono), f.parseLocalDateTime("2 29 13 40"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_monthDay_withDefaultYear_feb29
    public void testParseLocalDateTime_monthDay_withDefaultYear_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d H m")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withDefaultYear(2012);
        
        assertEquals(new LocalDateTime(2012, 2, 29, 13, 40, 0, 0, chrono), f.parseLocalDateTime("2 29 13 40"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_simple
    public void testParseDateTime_simple() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.parseDateTime("2004-06-09T10:20:30Z"));
        
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone
    public void testParseDateTime_zone() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone2
    public void testParseDateTime_zone2() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseDateTime("2004-06-09T06:20:30-04:00"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone3
    public void testParseDateTime_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(LONDON).parseDateTime("2004-06-09T10:20:30"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(null).parseDateTime("2004-06-09T10:20:30"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        assertEquals(expect, h.withZone(PARIS).parseDateTime("2004-06-09T10:20:30"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_simple_precedence
    public void testParseDateTime_simple_precedence() {
        DateTime expect = null;
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, f.parseDateTime("Wed 2004-06-09T10:20:30Z"));
        
        
        expect = new DateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        
        assertEquals(expect, f.parseDateTime("Mon 2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_offsetParsed
    public void testParseDateTime_offsetParsed() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withOffsetParsed().parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        assertEquals(expect, g.withOffsetParsed().parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withZone(PARIS).withOffsetParsed().parseDateTime("2004-06-09T10:20:30Z"));
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withOffsetParsed().withZone(PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_chrono
    public void testParseDateTime_chrono() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withChronology(ISO_PARIS).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0,LONDON);
        assertEquals(expect, g.withChronology(null).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseDateTime("2547-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS); 
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_simple
    public void testParseMutableDateTime_simple() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        try {
            g.parseMutableDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone
    public void testParseMutableDateTime_zone() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone2
    public void testParseMutableDateTime_zone2() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone3
    public void testParseMutableDateTime_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(LONDON).parseMutableDateTime("2004-06-09T10:20:30"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(null).parseMutableDateTime("2004-06-09T10:20:30"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        assertEquals(expect, h.withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_simple_precedence
    public void testParseMutableDateTime_simple_precedence() {
        MutableDateTime expect = null;
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, f.parseDateTime("Wed 2004-06-09T10:20:30Z"));
        
        
        expect = new MutableDateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        
        assertEquals(expect, f.parseDateTime("Mon 2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_offsetParsed
    public void testParseMutableDateTime_offsetParsed() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withOffsetParsed().parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        assertEquals(expect, g.withOffsetParsed().parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withZone(PARIS).withOffsetParsed().parseMutableDateTime("2004-06-09T10:20:30Z"));
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withOffsetParsed().withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_chrono
    public void testParseMutableDateTime_chrono() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withChronology(ISO_PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0,LONDON);
        assertEquals(expect, g.withChronology(null).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseMutableDateTime("2547-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS); 
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_simple
    public void testParseInto_simple() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        MutableDateTime result = new MutableDateTime(0L);
        assertEquals(20, g.parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        try {
            g.parseInto(null, "2004-06-09T10:20:30Z", 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(~0, g.parseInto(result, "ABC", 0));
        assertEquals(~10, g.parseInto(result, "2004-06-09", 0));
        assertEquals(~13, g.parseInto(result, "XX2004-06-09T", 2));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_zone
    public void testParseInto_zone() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(LONDON).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(null).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_zone2
    public void testParseInto_zone2() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(25, g.withZone(LONDON).parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(25, g.withZone(null).parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(25, g.withZone(PARIS).parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_zone3
    public void testParseInto_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(19, h.withZone(LONDON).parseInto(result, "2004-06-09T10:20:30", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(19, h.withZone(null).parseInto(result, "2004-06-09T10:20:30", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(19, h.withZone(PARIS).parseInto(result, "2004-06-09T10:20:30", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_simple_precedence
    public void testParseInto_simple_precedence() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        
        assertEquals(24, f.parseInto(result, "Mon 2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_offsetParsed
    public void testParseInto_offsetParsed() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withOffsetParsed().parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        result = new MutableDateTime(0L);
        assertEquals(25, g.withOffsetParsed().parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(PARIS).withOffsetParsed().parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withOffsetParsed().withZone(PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_chrono
    public void testParseInto_chrono() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(ISO_PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(null).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(BUDDHIST_PARIS).parseInto(result, "2547-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(BUDDHIST_PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMillis_fractionOfSecondLong
    public void testParseMillis_fractionOfSecondLong() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
            .appendSecondOfDay(2).appendLiteral('.').appendFractionOfSecond(1, 9)
                .toFormatter().withZoneUTC();
        assertEquals(10512, f.parseMillis("10.5123456"));
        assertEquals(10512, f.parseMillis("10.512999"));
    }

// org.joda.time.format.TestDateTimeFormatter::testZoneNameNearTransition
    public void testZoneNameNearTransition() {}

// org.joda.time.format.TestDateTimeFormatter::testZoneShortNameNearTransition
    public void testZoneShortNameNearTransition() {}

// org.joda.time.format.TestDateTimeFormatterBuilder::test_toFormatter
    public void test_toFormatter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        try {
            bld.toFormatter();
            fail();
        } catch (UnsupportedOperationException ex) {}
        bld.appendLiteral('X');
        assertNotNull(bld.toFormatter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_toPrinter
    public void test_toPrinter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        try {
            bld.toPrinter();
            fail();
        } catch (UnsupportedOperationException ex) {}
        bld.appendLiteral('X');
        assertNotNull(bld.toPrinter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_toParser
    public void test_toParser() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        try {
            bld.toParser();
            fail();
        } catch (UnsupportedOperationException ex) {}
        bld.appendLiteral('X');
        assertNotNull(bld.toParser());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_canBuildFormatter
    public void test_canBuildFormatter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        assertEquals(false, bld.canBuildFormatter());
        bld.appendLiteral('X');
        assertEquals(true, bld.canBuildFormatter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_canBuildPrinter
    public void test_canBuildPrinter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        assertEquals(false, bld.canBuildPrinter());
        bld.appendLiteral('X');
        assertEquals(true, bld.canBuildPrinter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_canBuildParser
    public void test_canBuildParser() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        assertEquals(false, bld.canBuildParser());
        bld.appendLiteral('X');
        assertEquals(true, bld.canBuildParser());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_Formatter
    public void test_append_Formatter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral('Y');
        DateTimeFormatter f = bld.toFormatter();
        
        DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
        bld2.appendLiteral('X');
        bld2.append(f);
        bld2.appendLiteral('Z');
        assertEquals("XYZ", bld2.toFormatter().print(0L));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_Printer
    public void test_append_Printer() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral('Y');
        DateTimePrinter p = bld.toPrinter();
        
        DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
        bld2.appendLiteral('X');
        bld2.append(p);
        bld2.appendLiteral('Z');
        assertEquals("XYZ", bld2.toFormatter().print(0L));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_appendFixedDecimal
    public void test_appendFixedDecimal() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendFixedDecimal(DateTimeFieldType.year(), 4);
        DateTimeFormatter f = bld.toFormatter();

        assertEquals("2007", f.print(new DateTime("2007-01-01")));
        assertEquals("0123", f.print(new DateTime("123-01-01")));
        assertEquals("0001", f.print(new DateTime("1-2-3")));
        assertEquals("99999", f.print(new DateTime("99999-2-3")));
        assertEquals("-0099", f.print(new DateTime("-99-2-3")));
        assertEquals("0000", f.print(new DateTime("0-2-3")));

        assertEquals(2001, f.parseDateTime("2001").getYear());
        try {
            f.parseDateTime("-2001");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            f.parseDateTime("200");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            f.parseDateTime("20016");
            fail();
        } catch (IllegalArgumentException e) {
        }

        bld = new DateTimeFormatterBuilder();
        bld.appendFixedDecimal(DateTimeFieldType.hourOfDay(), 2);
        bld.appendLiteral(':');
        bld.appendFixedDecimal(DateTimeFieldType.minuteOfHour(), 2);
        bld.appendLiteral(':');
        bld.appendFixedDecimal(DateTimeFieldType.secondOfMinute(), 2);
        f = bld.toFormatter();

        assertEquals("01:02:34", f.print(new DateTime("T1:2:34")));

        DateTime dt = f.parseDateTime("01:02:34");
        assertEquals(1, dt.getHourOfDay());
        assertEquals(2, dt.getMinuteOfHour());
        assertEquals(34, dt.getSecondOfMinute());

        try {
            f.parseDateTime("0145:02:34");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            f.parseDateTime("01:0:34");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_appendFixedSignedDecimal
    public void test_appendFixedSignedDecimal() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendFixedSignedDecimal(DateTimeFieldType.year(), 4);
        DateTimeFormatter f = bld.toFormatter();

        assertEquals("2007", f.print(new DateTime("2007-01-01")));
        assertEquals("0123", f.print(new DateTime("123-01-01")));
        assertEquals("0001", f.print(new DateTime("1-2-3")));
        assertEquals("99999", f.print(new DateTime("99999-2-3")));
        assertEquals("-0099", f.print(new DateTime("-99-2-3")));
        assertEquals("0000", f.print(new DateTime("0-2-3")));

        assertEquals(2001, f.parseDateTime("2001").getYear());
        assertEquals(-2001, f.parseDateTime("-2001").getYear());
        assertEquals(2001, f.parseDateTime("+2001").getYear());
        try {
            f.parseDateTime("20016");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

// org.joda.time.format.TestISODateTimeFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        ISODateTimeFormat f = new ISODateTimeFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_date
    public void testFormat_date() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09", ISODateTimeFormat.date().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09", ISODateTimeFormat.date().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09", ISODateTimeFormat.date().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_date_partial
    public void testFormat_date_partial() {
        Partial dt = new Partial(
                new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()},
                new int[] {2004, 6, 9});
        assertEquals("2004-06-09", ISODateTimeFormat.date().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_time
    public void testFormat_time() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20:30.040Z", ISODateTimeFormat.time().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20:30.040+01:00", ISODateTimeFormat.time().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20:30.040+02:00", ISODateTimeFormat.time().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_time_partial
    public void testFormat_time_partial() {
        Partial dt = new Partial(
                new DateTimeFieldType[] {DateTimeFieldType.hourOfDay(), DateTimeFieldType.minuteOfHour(),
                        DateTimeFieldType.secondOfMinute(), DateTimeFieldType.millisOfSecond()},
                new int[] {10, 20, 30, 40});
        assertEquals("10:20:30.040", ISODateTimeFormat.time().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_timeNoMillis
    public void testFormat_timeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20:30Z", ISODateTimeFormat.timeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20:30+01:00", ISODateTimeFormat.timeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20:30+02:00", ISODateTimeFormat.timeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_timeNoMillis_partial
    public void testFormat_timeNoMillis_partial() {
        Partial dt = new Partial(
                new DateTimeFieldType[] {DateTimeFieldType.hourOfDay(), DateTimeFieldType.minuteOfHour(),
                        DateTimeFieldType.secondOfMinute(), DateTimeFieldType.millisOfSecond()},
                new int[] {10, 20, 30, 40});
        assertEquals("10:20:30", ISODateTimeFormat.timeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_tTime
    public void testFormat_tTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("T10:20:30.040Z", ISODateTimeFormat.tTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("T11:20:30.040+01:00", ISODateTimeFormat.tTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("T12:20:30.040+02:00", ISODateTimeFormat.tTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_tTimeNoMillis
    public void testFormat_tTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("T10:20:30Z", ISODateTimeFormat.tTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("T11:20:30+01:00", ISODateTimeFormat.tTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("T12:20:30+02:00", ISODateTimeFormat.tTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateTime
    public void testFormat_dateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20:30.040Z", ISODateTimeFormat.dateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20:30.040+01:00", ISODateTimeFormat.dateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20:30.040+02:00", ISODateTimeFormat.dateTime().print(dt));
        

    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateTimeNoMillis
    public void testFormat_dateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20:30Z", ISODateTimeFormat.dateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20:30+01:00", ISODateTimeFormat.dateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20:30+02:00", ISODateTimeFormat.dateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_ordinalDate
    public void testFormat_ordinalDate() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-161", ISODateTimeFormat.ordinalDate().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-161", ISODateTimeFormat.ordinalDate().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-161", ISODateTimeFormat.ordinalDate().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_ordinalDateTime
    public void testFormat_ordinalDateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-161T10:20:30.040Z", ISODateTimeFormat.ordinalDateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-161T11:20:30.040+01:00", ISODateTimeFormat.ordinalDateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-161T12:20:30.040+02:00", ISODateTimeFormat.ordinalDateTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_ordinalDateTimeNoMillis
    public void testFormat_ordinalDateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-161T10:20:30Z", ISODateTimeFormat.ordinalDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-161T11:20:30+01:00", ISODateTimeFormat.ordinalDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-161T12:20:30+02:00", ISODateTimeFormat.ordinalDateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekDate
    public void testFormat_weekDate() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekDate().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekDate().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekDate().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekDateTime
    public void testFormat_weekDateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-W24-3T10:20:30.040Z", ISODateTimeFormat.weekDateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-W24-3T11:20:30.040+01:00", ISODateTimeFormat.weekDateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-W24-3T12:20:30.040+02:00", ISODateTimeFormat.weekDateTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekDateTimeNoMillis
    public void testFormat_weekDateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-W24-3T10:20:30Z", ISODateTimeFormat.weekDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-W24-3T11:20:30+01:00", ISODateTimeFormat.weekDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-W24-3T12:20:30+02:00", ISODateTimeFormat.weekDateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicDate
    public void testFormat_basicDate() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("20040609", ISODateTimeFormat.basicDate().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("20040609", ISODateTimeFormat.basicDate().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("20040609", ISODateTimeFormat.basicDate().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicTime
    public void testFormat_basicTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("102030.040Z", ISODateTimeFormat.basicTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("112030.040+0100", ISODateTimeFormat.basicTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("122030.040+0200", ISODateTimeFormat.basicTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicTimeNoMillis
    public void testFormat_basicTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("102030Z", ISODateTimeFormat.basicTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("112030+0100", ISODateTimeFormat.basicTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("122030+0200", ISODateTimeFormat.basicTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicTTime
    public void testFormat_basicTTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("T102030.040Z", ISODateTimeFormat.basicTTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("T112030.040+0100", ISODateTimeFormat.basicTTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("T122030.040+0200", ISODateTimeFormat.basicTTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicTTimeNoMillis
    public void testFormat_basicTTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("T102030Z", ISODateTimeFormat.basicTTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("T112030+0100", ISODateTimeFormat.basicTTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("T122030+0200", ISODateTimeFormat.basicTTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicDateTime
    public void testFormat_basicDateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("20040609T102030.040Z", ISODateTimeFormat.basicDateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("20040609T112030.040+0100", ISODateTimeFormat.basicDateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("20040609T122030.040+0200", ISODateTimeFormat.basicDateTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicDateTimeNoMillis
    public void testFormat_basicDateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("20040609T102030Z", ISODateTimeFormat.basicDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("20040609T112030+0100", ISODateTimeFormat.basicDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("20040609T122030+0200", ISODateTimeFormat.basicDateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicOrdinalDate
    public void testFormat_basicOrdinalDate() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004161", ISODateTimeFormat.basicOrdinalDate().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004161", ISODateTimeFormat.basicOrdinalDate().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004161", ISODateTimeFormat.basicOrdinalDate().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicOrdinalDateTime
    public void testFormat_basicOrdinalDateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004161T102030.040Z", ISODateTimeFormat.basicOrdinalDateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004161T112030.040+0100", ISODateTimeFormat.basicOrdinalDateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004161T122030.040+0200", ISODateTimeFormat.basicOrdinalDateTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicOrdinalDateTimeNoMillis
    public void testFormat_basicOrdinalDateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004161T102030Z", ISODateTimeFormat.basicOrdinalDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004161T112030+0100", ISODateTimeFormat.basicOrdinalDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004161T122030+0200", ISODateTimeFormat.basicOrdinalDateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicWeekDate
    public void testFormat_basicWeekDate() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004W243", ISODateTimeFormat.basicWeekDate().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004W243", ISODateTimeFormat.basicWeekDate().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004W243", ISODateTimeFormat.basicWeekDate().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicWeekDateTime
    public void testFormat_basicWeekDateTime() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004W243T102030.040Z", ISODateTimeFormat.basicWeekDateTime().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004W243T112030.040+0100", ISODateTimeFormat.basicWeekDateTime().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004W243T122030.040+0200", ISODateTimeFormat.basicWeekDateTime().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_basicWeekDateTimeNoMillis
    public void testFormat_basicWeekDateTimeNoMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004W243T102030Z", ISODateTimeFormat.basicWeekDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004W243T112030+0100", ISODateTimeFormat.basicWeekDateTimeNoMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004W243T122030+0200", ISODateTimeFormat.basicWeekDateTimeNoMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_year
    public void testFormat_year() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004", ISODateTimeFormat.year().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004", ISODateTimeFormat.year().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004", ISODateTimeFormat.year().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_yearMonth
    public void testFormat_yearMonth() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06", ISODateTimeFormat.yearMonth().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06", ISODateTimeFormat.yearMonth().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06", ISODateTimeFormat.yearMonth().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_yearMonthDay
    public void testFormat_yearMonthDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09", ISODateTimeFormat.yearMonthDay().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09", ISODateTimeFormat.yearMonthDay().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09", ISODateTimeFormat.yearMonthDay().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekyear
    public void testFormat_weekyear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004", ISODateTimeFormat.weekyear().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004", ISODateTimeFormat.weekyear().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004", ISODateTimeFormat.weekyear().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekyearWeek
    public void testFormat_weekyearWeek() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-W24", ISODateTimeFormat.weekyearWeek().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-W24", ISODateTimeFormat.weekyearWeek().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-W24", ISODateTimeFormat.weekyearWeek().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_weekyearWeekDay
    public void testFormat_weekyearWeekDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekyearWeekDay().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekyearWeekDay().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-W24-3", ISODateTimeFormat.weekyearWeekDay().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_hour
    public void testFormat_hour() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10", ISODateTimeFormat.hour().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11", ISODateTimeFormat.hour().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12", ISODateTimeFormat.hour().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_hourMinute
    public void testFormat_hourMinute() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20", ISODateTimeFormat.hourMinute().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20", ISODateTimeFormat.hourMinute().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20", ISODateTimeFormat.hourMinute().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_hourMinuteSecond
    public void testFormat_hourMinuteSecond() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20:30", ISODateTimeFormat.hourMinuteSecond().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20:30", ISODateTimeFormat.hourMinuteSecond().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20:30", ISODateTimeFormat.hourMinuteSecond().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_hourMinuteSecondMillis
    public void testFormat_hourMinuteSecondMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20:30.040", ISODateTimeFormat.hourMinuteSecondMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20:30.040", ISODateTimeFormat.hourMinuteSecondMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20:30.040", ISODateTimeFormat.hourMinuteSecondMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_hourMinuteSecondFraction
    public void testFormat_hourMinuteSecondFraction() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("10:20:30.040", ISODateTimeFormat.hourMinuteSecondFraction().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("11:20:30.040", ISODateTimeFormat.hourMinuteSecondFraction().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("12:20:30.040", ISODateTimeFormat.hourMinuteSecondFraction().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateHour
    public void testFormat_dateHour() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10", ISODateTimeFormat.dateHour().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11", ISODateTimeFormat.dateHour().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12", ISODateTimeFormat.dateHour().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateHourMinute
    public void testFormat_dateHourMinute() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20", ISODateTimeFormat.dateHourMinute().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20", ISODateTimeFormat.dateHourMinute().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20", ISODateTimeFormat.dateHourMinute().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateHourMinuteSecond
    public void testFormat_dateHourMinuteSecond() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20:30", ISODateTimeFormat.dateHourMinuteSecond().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20:30", ISODateTimeFormat.dateHourMinuteSecond().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20:30", ISODateTimeFormat.dateHourMinuteSecond().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateHourMinuteSecondMillis
    public void testFormat_dateHourMinuteSecondMillis() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20:30.040", ISODateTimeFormat.dateHourMinuteSecondMillis().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20:30.040", ISODateTimeFormat.dateHourMinuteSecondMillis().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20:30.040", ISODateTimeFormat.dateHourMinuteSecondMillis().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormat::testFormat_dateHourMinuteSecondFraction
    public void testFormat_dateHourMinuteSecondFraction() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("2004-06-09T10:20:30.040", ISODateTimeFormat.dateHourMinuteSecondFraction().print(dt));
        
        dt = dt.withZone(LONDON);
        assertEquals("2004-06-09T11:20:30.040", ISODateTimeFormat.dateHourMinuteSecondFraction().print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("2004-06-09T12:20:30.040", ISODateTimeFormat.dateHourMinuteSecondFraction().print(dt));
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateParser
    public void test_dateParser() {
        DateTimeFormatter parser = ISODateTimeFormat.dateParser();
        assertParse(parser, true, "2006-06-09");
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, true, "2006-06-09T+02:00");
        assertParse(parser, true, "2006-W27-3T+02:00");
        assertParse(parser, true, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_localDateParser
    public void test_localDateParser() {
        DateTimeFormatter parser = ISODateTimeFormat.localDateParser();
        assertEquals(DateTimeZone.UTC, parser.getZone());
        assertParse(parser, true, "2006-06-09");
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateElementParser
    public void test_dateElementParser() {
        DateTimeFormatter parser = ISODateTimeFormat.dateElementParser();
        assertParse(parser, "2006-06-09", new DateTime(2006, 6, 9, 0, 0, 0, 0));
        assertParse(parser, "2006-06-9", new DateTime(2006, 6, 9, 0, 0, 0, 0));
        assertParse(parser, "2006-6-09", new DateTime(2006, 6, 9, 0, 0, 0, 0));
        assertParse(parser, "2006-6-9", new DateTime(2006, 6, 9, 0, 0, 0, 0));
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_timeParser
    public void test_timeParser() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.timeParser();
        assertParse(parser, false, "2006-06-09");
        assertParse(parser, false, "2006-W27-3");
        assertParse(parser, false, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, "T10:20:30.040000000", new DateTime(1970, 1, 1, 10, 20, 30, 40));
        assertParse(parser, "T10:20:30.004", new DateTime(1970, 1, 1, 10, 20, 30, 4));
        assertParse(parser, "T10:20:30.040", new DateTime(1970, 1, 1, 10, 20, 30, 40));
        assertParse(parser, "T10:20:30.400", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T10.5", new DateTime(1970, 1, 1, 10, 30, 0, 0));
        assertParse(parser, "T10:20:30.040+02:00", new DateTime(1970, 1, 1, 8, 20, 30, 40));
        assertParse(parser, "T10.5+02:00", new DateTime(1970, 1, 1, 8, 30, 0, 0));
        
        assertParse(parser, true, "10:20:30.040");
        assertParse(parser, true, "10.5");
        assertParse(parser, true, "10:20:30.040+02:00");
        assertParse(parser, true, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_localTimeParser
    public void test_localTimeParser() {
        DateTimeFormatter parser = ISODateTimeFormat.localTimeParser();
        assertEquals(DateTimeZone.UTC, parser.getZone());
        assertParse(parser, false, "2006-06-09");
        assertParse(parser, false, "2006-W27-3");
        assertParse(parser, false, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, true, "T10:20:30.040");
        assertParse(parser, true, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, true, "10:20:30.040");
        assertParse(parser, true, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
        
        assertParse(parser, true, "00:00:10.512345678");
        assertEquals(10512, parser.parseMillis("00:00:10.512345678"));
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_timeElementParser
    public void test_timeElementParser() {
        DateTimeFormatter parser = ISODateTimeFormat.timeElementParser();
        assertParse(parser, false, "2006-06-09");
        assertParse(parser, false, "2006-W27-3");
        assertParse(parser, false, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, false, "2006-06-09T10:20:30.040");
        assertParse(parser, false, "2006-W27-3T10:20:30.040");
        assertParse(parser, false, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, true, "10:20:30.040");
        assertParse(parser, true, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
        
        assertParse(parser, true, "00:00:10.512345678");
        
        assertEquals(10512, parser.parseMillis("00:00:10.512345678") + DateTimeZone.getDefault().getOffset(0L));
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateTimeParser
    public void test_dateTimeParser() {
        DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
        assertParse(parser, true, "2006-06-09");
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, true, "2006-06-09T+02:00");
        assertParse(parser, true, "2006-W27-3T+02:00");
        assertParse(parser, true, "2006-123T+02:00");
        
        assertParse(parser, true, "2006-06-09T10:20:30.040");
        assertParse(parser, true, "2006-W27-3T10:20:30.040");
        assertParse(parser, true, "2006-123T10:20:30.040");
        assertParse(parser, true, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, true, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, true, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, true, "T10:20:30.040");
        assertParse(parser, true, "T10.5");
        assertParse(parser, true, "T10:20:30.040+02:00");
        assertParse(parser, true, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateOptionalTimeParser
    public void test_dateOptionalTimeParser() {
        DateTimeFormatter parser = ISODateTimeFormat.dateOptionalTimeParser();
        assertParse(parser, true, "2006-06-09");
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, true, "2006-06-09T+02:00");
        assertParse(parser, true, "2006-W27-3T+02:00");
        assertParse(parser, true, "2006-123T+02:00");
        
        assertParse(parser, true, "2006-06-09T10:20:30.040");
        assertParse(parser, true, "2006-W27-3T10:20:30.040");
        assertParse(parser, true, "2006-123T10:20:30.040");
        assertParse(parser, true, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, true, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, true, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_localDateOptionalTimeParser
    public void test_localDateOptionalTimeParser() {
        DateTimeFormatter parser = ISODateTimeFormat.localDateOptionalTimeParser();
        assertEquals(DateTimeZone.UTC, parser.getZone());
        assertParse(parser, true, "2006-06-09");
        assertParse(parser, true, "2006-W27-3");
        assertParse(parser, true, "2006-123");
        assertParse(parser, false, "2006-06-09T+02:00");
        assertParse(parser, false, "2006-W27-3T+02:00");
        assertParse(parser, false, "2006-123T+02:00");
        
        assertParse(parser, true, "2006-06-09T10:20:30.040");
        assertParse(parser, true, "2006-W27-3T10:20:30.040");
        assertParse(parser, true, "2006-123T10:20:30.040");
        assertParse(parser, false, "2006-06-09T10:20:30.040+02:00");
        assertParse(parser, false, "2006-W27-3T10:20:30.040+02:00");
        assertParse(parser, false, "2006-123T10:20:30.040+02:00");
        
        assertParse(parser, false, "T10:20:30.040");
        assertParse(parser, false, "T10.5");
        assertParse(parser, false, "T10:20:30.040+02:00");
        assertParse(parser, false, "T10.5+02:00");
        
        assertParse(parser, false, "10:20:30.040");
        assertParse(parser, false, "10.5");
        assertParse(parser, false, "10:20:30.040+02:00");
        assertParse(parser, false, "10.5+02:00");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_date
    public void test_date() {
        DateTimeFormatter parser = ISODateTimeFormat.date();
        assertParse(parser, "2006-02-04", new DateTime(2006, 2, 4, 0, 0, 0, 0));
        assertParse(parser, "2006-2-04", new DateTime(2006, 2, 4, 0, 0, 0, 0));
        assertParse(parser, "2006-02-4", new DateTime(2006, 2, 4, 0, 0, 0, 0));
        assertParse(parser, "2006-2-4", new DateTime(2006, 2, 4, 0, 0, 0, 0));
        assertParse(parser, false, "2006-02-");
        assertParse(parser, false, "2006-02");
        assertParse(parser, false, "2006--4");
        assertParse(parser, false, "2006-1");
        assertParse(parser, false, "2006");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_time
    public void test_time() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.time();
        assertParse(parser, "10:20:30.400999999Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.400Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.40Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.4Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "5:6:7.8Z", new DateTime(1970, 1, 1, 5, 6, 7, 800));
        assertParse(parser, false, "10:20.400Z");
        assertParse(parser, false, "10:2.400Z");
        assertParse(parser, false, "10.400Z");
        assertParse(parser, false, "1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_timeNoMillis
    public void test_timeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.timeNoMillis();
        assertParse(parser, "10:20:30Z", new DateTime(1970, 1, 1, 10, 20, 30, 0));
        assertParse(parser, "5:6:7Z", new DateTime(1970, 1, 1, 5, 6, 7, 0));
        assertParse(parser, false, "10:20Z");
        assertParse(parser, false, "10:2Z");
        assertParse(parser, false, "10Z");
        assertParse(parser, false, "1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_tTime
    public void test_tTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.tTime();
        assertParse(parser, "T10:20:30.400999999Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T10:20:30.400Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T10:20:30.40Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T10:20:30.4Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T5:6:7.8Z", new DateTime(1970, 1, 1, 5, 6, 7, 800));
        assertParse(parser, false, "T10:20.400Z");
        assertParse(parser, false, "T102.400Z");
        assertParse(parser, false, "T10.400Z");
        assertParse(parser, false, "T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_tTimeNoMillis
    public void test_tTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.tTimeNoMillis();
        assertParse(parser, "T10:20:30Z", new DateTime(1970, 1, 1, 10, 20, 30, 0));
        assertParse(parser, "T5:6:7Z", new DateTime(1970, 1, 1, 5, 6, 7, 0));
        assertParse(parser, false, "T10:20Z");
        assertParse(parser, false, "T10:2Z");
        assertParse(parser, false, "T10Z");
        assertParse(parser, false, "T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateTime
    public void test_dateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.dateTime();
        assertParse(parser, "2006-02-04T10:20:30.400999999Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-02-04T10:20:30.400Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-02-04T10:20:30.40Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-02-04T10:20:30.4Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-02-4T10:20:30.400Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-2-04T10:20:30.400Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-2-4T10:20:30.400Z", new DateTime(2006, 2, 4, 10, 20, 30, 400));
        assertParse(parser, "2006-02-04T5:6:7.800Z", new DateTime(2006, 2, 4, 5, 6, 7, 800));
        assertParse(parser, false, "2006-02-T10:20:30.400Z");
        assertParse(parser, false, "2006-12T10:20:30.400Z");
        assertParse(parser, false, "2006-1T10:20:30.400Z");
        assertParse(parser, false, "2006T10:20:30.400Z");
        assertParse(parser, false, "200T10:20:30.400Z");
        assertParse(parser, false, "20T10:20:30.400Z");
        assertParse(parser, false, "2T10:20:30.400Z");
        assertParse(parser, false, "2006-02-04T10:20.400Z");
        assertParse(parser, false, "2006-02-04T10:2.400Z");
        assertParse(parser, false, "2006-02-04T10.400Z");
        assertParse(parser, false, "2006-02-04T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_dateTimeNoMillis
    public void test_dateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
        assertParse(parser, "2006-02-04T10:20:30Z", new DateTime(2006, 2, 4, 10, 20, 30, 0));
        assertParse(parser, "2006-02-4T10:20:30Z", new DateTime(2006, 2, 4, 10, 20, 30, 0));
        assertParse(parser, "2006-2-04T10:20:30Z", new DateTime(2006, 2, 4, 10, 20, 30, 0));
        assertParse(parser, "2006-2-4T10:20:30Z", new DateTime(2006, 2, 4, 10, 20, 30, 0));
        assertParse(parser, "2006-02-04T5:6:7Z", new DateTime(2006, 2, 4, 5, 6, 7, 0));
        assertParse(parser, false, "2006-02-T10:20:30Z");
        assertParse(parser, false, "2006-12T10:20:30Z");
        assertParse(parser, false, "2006-1T10:20:30Z");
        assertParse(parser, false, "2006T10:20:30Z");
        assertParse(parser, false, "200T10:20:30Z");
        assertParse(parser, false, "20T10:20:30Z");
        assertParse(parser, false, "2T10:20:30Z");
        assertParse(parser, false, "2006-02-04T10:20Z");
        assertParse(parser, false, "2006-02-04T10:2Z");
        assertParse(parser, false, "2006-02-04T10Z");
        assertParse(parser, false, "2006-02-04T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_ordinalDate
    public void test_ordinalDate() {
        DateTimeFormatter parser = ISODateTimeFormat.ordinalDate();
        assertParse(parser, "2006-123", new DateTime(2006, 1, 1, 0, 0, 0, 0).withDayOfYear(123));
        assertParse(parser, "2006-12", new DateTime(2006, 1, 1, 0, 0, 0, 0).withDayOfYear(12));
        assertParse(parser, "2006-1", new DateTime(2006, 1, 1, 0, 0, 0, 0).withDayOfYear(1));
        assertParse(parser, false, "2006-");
        assertParse(parser, false, "2006");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_ordinalDateTime
    public void test_ordinalDateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.ordinalDateTime();
        assertParse(parser, "2006-123T10:20:30.400999999Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006-123T10:20:30.400Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006-123T10:20:30.40Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006-123T10:20:30.4Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006-12T10:20:30.400Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(12));
        assertParse(parser, "2006-1T10:20:30.400Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(1));
        assertParse(parser, "2006-123T5:6:7.800Z", new DateTime(2006, 1, 1, 5, 6, 7, 800).withDayOfYear(123));
        assertParse(parser, false, "2006-T10:20:30.400Z");
        assertParse(parser, false, "2006T10:20:30.400Z");
        assertParse(parser, false, "2006-123T10:20.400Z");
        assertParse(parser, false, "2006-123T10:2.400Z");
        assertParse(parser, false, "2006-123T10.400Z");
        assertParse(parser, false, "2006-123T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_ordinalDateTimeNoMillis
    public void test_ordinalDateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.ordinalDateTimeNoMillis();
        assertParse(parser, "2006-123T10:20:30Z", new DateTime(2006, 1, 1, 10, 20, 30, 0).withDayOfYear(123));
        assertParse(parser, "2006-12T10:20:30Z", new DateTime(2006, 1, 1, 10, 20, 30, 0).withDayOfYear(12));
        assertParse(parser, "2006-1T10:20:30Z", new DateTime(2006, 1, 1, 10, 20, 30, 0).withDayOfYear(1));
        assertParse(parser, "2006-123T5:6:7Z", new DateTime(2006, 1, 1, 5, 6, 7, 0).withDayOfYear(123));
        assertParse(parser, false, "2006-T10:20:30Z");
        assertParse(parser, false, "2006T10:20:30Z");
        assertParse(parser, false, "2006-123T10:20Z");
        assertParse(parser, false, "2006-123T10:2Z");
        assertParse(parser, false, "2006-123T10Z");
        assertParse(parser, false, "2006-123T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_weekDate
    public void test_weekDate() {
        DateTimeFormatter parser = ISODateTimeFormat.weekDate();
        assertParse(parser, "2006-W27-3", new DateTime(2006, 6, 1, 0, 0, 0, 0).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W2-3", new DateTime(2006, 6, 1, 0, 0, 0, 0).withWeekOfWeekyear(2).withDayOfWeek(3));
        assertParse(parser, false, "2006-W-3");
        assertParse(parser, false, "2006-W27-");
        assertParse(parser, false, "2006-W27");
        assertParse(parser, false, "2006-W2");
        assertParse(parser, false, "2006-W");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_weekDateTime
    public void test_weekDateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.weekDateTime();
        assertParse(parser, "2006-W27-3T10:20:30.400999999Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W27-3T10:20:30.400Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W27-3T10:20:30.40Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W27-3T10:20:30.4Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W2-3T10:20:30.400Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(2).withDayOfWeek(3));
        assertParse(parser, "2006-W27-3T5:6:7.800Z", new DateTime(2006, 6, 1, 5, 6, 7, 800).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, false, "2006-W27-T10:20:30.400Z");
        assertParse(parser, false, "2006-W27T10:20:30.400Z");
        assertParse(parser, false, "2006-W2T10:20:30.400Z");
        assertParse(parser, false, "2006-W-3T10:20:30.400Z");
        assertParse(parser, false, "2006-W27-3T10:20.400Z");
        assertParse(parser, false, "2006-W27-3T10:2.400Z");
        assertParse(parser, false, "2006-W27-3T10.400Z");
        assertParse(parser, false, "2006-W27-3T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_weekDateTimeNoMillis
    public void test_weekDateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.weekDateTimeNoMillis();
        assertParse(parser, "2006-W27-3T10:20:30Z", new DateTime(2006, 6, 1, 10, 20, 30, 0).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006-W2-3T10:20:30Z", new DateTime(2006, 6, 1, 10, 20, 30, 0).withWeekOfWeekyear(2).withDayOfWeek(3));
        assertParse(parser, "2006-W27-3T5:6:7Z", new DateTime(2006, 6, 1, 5, 6, 7, 0).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, false, "2006-W27-T10:20:30Z");
        assertParse(parser, false, "2006-W27T10:20:30Z");
        assertParse(parser, false, "2006-W2T10:20:30Z");
        assertParse(parser, false, "2006-W-3T10:20:30Z");
        assertParse(parser, false, "2006-W27-3T10:20Z");
        assertParse(parser, false, "2006-W27-3T10:2Z");
        assertParse(parser, false, "2006-W27-3T10Z");
        assertParse(parser, false, "2006-W27-3T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicDate
    public void test_basicDate() {
        DateTimeFormatter parser = ISODateTimeFormat.basicDate();
        assertParse(parser, "20060204", new DateTime(2006, 2, 4, 0, 0, 0, 0));
        assertParse(parser, false, "2006024");
        assertParse(parser, false, "200602");
        assertParse(parser, false, "20061");
        assertParse(parser, false, "2006");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicTime
    public void test_basicTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicTime();
        assertParse(parser, "102030.400999999Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "102030.400Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "102030.40Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "102030.4Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, false, "10203.400Z");
        assertParse(parser, false, "1020.400Z");
        assertParse(parser, false, "102.400Z");
        assertParse(parser, false, "10.400Z");
        assertParse(parser, false, "1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicTimeNoMillis
    public void test_basicTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicTimeNoMillis();
        assertParse(parser, "102030Z", new DateTime(1970, 1, 1, 10, 20, 30, 0));
        assertParse(parser, false, "10203Z");
        assertParse(parser, false, "1020Z");
        assertParse(parser, false, "102Z");
        assertParse(parser, false, "10Z");
        assertParse(parser, false, "1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicTTime
    public void test_basicTTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicTTime();
        assertParse(parser, "T102030.400999999Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T102030.400Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T102030.40Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "T102030.4Z", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, false, "T10203.400Z");
        assertParse(parser, false, "T1020.400Z");
        assertParse(parser, false, "T102.400Z");
        assertParse(parser, false, "T10.400Z");
        assertParse(parser, false, "T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicTTimeNoMillis
    public void test_basicTTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicTTimeNoMillis();
        assertParse(parser, "T102030Z", new DateTime(1970, 1, 1, 10, 20, 30, 0));
        assertParse(parser, false, "T10203Z");
        assertParse(parser, false, "T1020Z");
        assertParse(parser, false, "T102Z");
        assertParse(parser, false, "T10Z");
        assertParse(parser, false, "T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicDateTime
    public void test_basicDateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicDateTime();
        assertParse(parser, "20061204T102030.400999999Z", new DateTime(2006, 12, 4, 10, 20, 30, 400));
        assertParse(parser, "20061204T102030.400Z", new DateTime(2006, 12, 4, 10, 20, 30, 400));
        assertParse(parser, "20061204T102030.40Z", new DateTime(2006, 12, 4, 10, 20, 30, 400));
        assertParse(parser, "20061204T102030.4Z", new DateTime(2006, 12, 4, 10, 20, 30, 400));
        assertParse(parser, false, "2006120T102030.400Z");
        assertParse(parser, false, "200612T102030.400Z");
        assertParse(parser, false, "20061T102030.400Z");
        assertParse(parser, false, "2006T102030.400Z");
        assertParse(parser, false, "200T102030.400Z");
        assertParse(parser, false, "20T102030.400Z");
        assertParse(parser, false, "2T102030.400Z");
        assertParse(parser, false, "20061204T10203.400Z");
        assertParse(parser, false, "20061204T1020.400Z");
        assertParse(parser, false, "20061204T102.400Z");
        assertParse(parser, false, "20061204T10.400Z");
        assertParse(parser, false, "20061204T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicDateTimeNoMillis
    public void test_basicDateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicDateTimeNoMillis();
        assertParse(parser, "20061204T102030Z", new DateTime(2006, 12, 4, 10, 20, 30, 0));
        assertParse(parser, false, "2006120T102030Z");
        assertParse(parser, false, "200612T102030Z");
        assertParse(parser, false, "20061T102030Z");
        assertParse(parser, false, "2006T102030Z");
        assertParse(parser, false, "200T102030Z");
        assertParse(parser, false, "20T102030Z");
        assertParse(parser, false, "2T102030Z");
        assertParse(parser, false, "20061204T10203Z");
        assertParse(parser, false, "20061204T1020Z");
        assertParse(parser, false, "20061204T102Z");
        assertParse(parser, false, "20061204T10Z");
        assertParse(parser, false, "20061204T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicOrdinalDate
    public void test_basicOrdinalDate() {
        DateTimeFormatter parser = ISODateTimeFormat.basicOrdinalDate();
        assertParse(parser, "2006123", new DateTime(2006, 1, 1, 0, 0, 0, 0).withDayOfYear(123));
        assertParse(parser, false, "200612");
        assertParse(parser, false, "20061");
        assertParse(parser, false, "2006");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicOrdinalDateTime
    public void test_basicOrdinalDateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicOrdinalDateTime();
        assertParse(parser, "2006123T102030.400999999Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006123T102030.400Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006123T102030.40Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, "2006123T102030.4Z", new DateTime(2006, 1, 1, 10, 20, 30, 400).withDayOfYear(123));
        assertParse(parser, false, "200612T102030.400Z");
        assertParse(parser, false, "20061T102030.400Z");
        assertParse(parser, false, "2006T102030.400Z");
        assertParse(parser, false, "200T102030.400Z");
        assertParse(parser, false, "20T102030.400Z");
        assertParse(parser, false, "2T102030.400Z");
        assertParse(parser, false, "2006123T10203.400Z");
        assertParse(parser, false, "2006123T1020.400Z");
        assertParse(parser, false, "2006123T102.400Z");
        assertParse(parser, false, "2006123T10.400Z");
        assertParse(parser, false, "2006123T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicOrdinalDateTimeNoMillis
    public void test_basicOrdinalDateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicOrdinalDateTimeNoMillis();
        assertParse(parser, "2006123T102030Z", new DateTime(2006, 1, 1, 10, 20, 30, 0).withDayOfYear(123));
        assertParse(parser, false, "200612T102030Z");
        assertParse(parser, false, "20061T102030Z");
        assertParse(parser, false, "2006T102030Z");
        assertParse(parser, false, "200T102030Z");
        assertParse(parser, false, "20T102030Z");
        assertParse(parser, false, "2T102030Z");
        assertParse(parser, false, "2006123T10203Z");
        assertParse(parser, false, "2006123T1020Z");
        assertParse(parser, false, "2006123T102Z");
        assertParse(parser, false, "2006123T10Z");
        assertParse(parser, false, "2006123T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicWeekDate
    public void test_basicWeekDate() {
        DateTimeFormatter parser = ISODateTimeFormat.basicWeekDate();
        assertParse(parser, "2006W273", new DateTime(2006, 6, 1, 0, 0, 0, 0).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, false, "2006W27");
        assertParse(parser, false, "2006W2");
        assertParse(parser, false, "2006W");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicWeekDateTime
    public void test_basicWeekDateTime() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicWeekDateTime();
        assertParse(parser, "2006W273T102030.400999999Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006W273T102030.400Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006W273T102030.40Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, "2006W273T102030.4Z", new DateTime(2006, 6, 1, 10, 20, 30, 400).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, false, "2006W27T102030.400Z");
        assertParse(parser, false, "2006W2T102030.400Z");
        assertParse(parser, false, "2006W273T10203.400Z");
        assertParse(parser, false, "2006W273T1020.400Z");
        assertParse(parser, false, "2006W273T102.400Z");
        assertParse(parser, false, "2006W273T10.400Z");
        assertParse(parser, false, "2006W273T1.400Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_basicWeekDateTimeNoMillis
    public void test_basicWeekDateTimeNoMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.basicWeekDateTimeNoMillis();
        assertParse(parser, "2006W273T102030Z", new DateTime(2006, 6, 1, 10, 20, 30, 0).withWeekOfWeekyear(27).withDayOfWeek(3));
        assertParse(parser, false, "2006W27T102030Z");
        assertParse(parser, false, "2006W2T102030Z");
        assertParse(parser, false, "2006W273T10203Z");
        assertParse(parser, false, "2006W273T1020Z");
        assertParse(parser, false, "2006W273T102Z");
        assertParse(parser, false, "2006W273T10Z");
        assertParse(parser, false, "2006W273T1Z");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_hourMinute
    public void test_hourMinute() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.hourMinute();
        assertParse(parser, "10:20", new DateTime(1970, 1, 1, 10, 20, 0, 0));
        assertParse(parser, "5:6", new DateTime(1970, 1, 1, 5, 6, 0, 0));
        assertParse(parser, false, "10:20:30.400999999");
        assertParse(parser, false, "10:20:30.400");
        assertParse(parser, false, "10:20:30");
        assertParse(parser, false, "10:20.400");
        assertParse(parser, false, "10:2.400");
        assertParse(parser, false, "10.400");
        assertParse(parser, false, "1.400");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_hourMinuteSecond
    public void test_hourMinuteSecond() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.hourMinuteSecond();
        assertParse(parser, "10:20:30", new DateTime(1970, 1, 1, 10, 20, 30, 0));
        assertParse(parser, "5:6:7", new DateTime(1970, 1, 1, 5, 6, 7, 0));
        assertParse(parser, false, "10:20:30.400999999");
        assertParse(parser, false, "10:20:30.400");
        assertParse(parser, false, "10:20:30.4");
        assertParse(parser, false, "10:20.400");
        assertParse(parser, false, "10:2.400");
        assertParse(parser, false, "10.400");
        assertParse(parser, false, "1.400");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_hourMinuteSecondMillis
    public void test_hourMinuteSecondMillis() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.hourMinuteSecondMillis();
        assertParse(parser, "10:20:30.400", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.40", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.4", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "5:6:7.8", new DateTime(1970, 1, 1, 5, 6, 7, 800));
        assertParse(parser, false, "10:20:30.400999999");
        assertParse(parser, false, "10:20.400");
        assertParse(parser, false, "10:2.400");
        assertParse(parser, false, "10.400");
        assertParse(parser, false, "1.400");
    }

// org.joda.time.format.TestISODateTimeFormatParsing::test_hourMinuteSecondFraction
    public void test_hourMinuteSecondFraction() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeFormatter parser = ISODateTimeFormat.hourMinuteSecondFraction();
        assertParse(parser, "10:20:30.400999999", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.400", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.40", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "10:20:30.4", new DateTime(1970, 1, 1, 10, 20, 30, 400));
        assertParse(parser, "5:6:7.8", new DateTime(1970, 1, 1, 5, 6, 7, 800));
        assertParse(parser, false, "10:20.400");
        assertParse(parser, false, "10:2.400");
        assertParse(parser, false, "10.400");
        assertParse(parser, false, "1.400");
    }
