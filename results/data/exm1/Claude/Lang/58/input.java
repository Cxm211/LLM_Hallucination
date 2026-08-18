// buggy code
    public static Number createNumber(String str) throws NumberFormatException {
        if (str == null) {
            return null;
        }
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }  
        if (str.startsWith("--")) {
            // this is protection for poorness in java.lang.BigDecimal.
            // it accepts this as a legal value, but it does not appear 
            // to be in specification of class. OS X Java parses it to 
            // a wrong value.
            return null;
        }
        if (str.startsWith("0x") || str.startsWith("-0x")) {
            return createInteger(str);
        }   
        char lastChar = str.charAt(str.length() - 1);
        String mant;
        String dec;
        String exp;
        int decPos = str.indexOf('.');
        int expPos = str.indexOf('e') + str.indexOf('E') + 1;

        if (decPos > -1) {

            if (expPos > -1) {
                if (expPos < decPos) {
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                dec = str.substring(decPos + 1, expPos);
            } else {
                dec = str.substring(decPos + 1);
            }
            mant = str.substring(0, decPos);
        } else {
            if (expPos > -1) {
                mant = str.substring(0, expPos);
            } else {
                mant = str;
            }
            dec = null;
        }
        if (!Character.isDigit(lastChar)) {
            if (expPos > -1 && expPos < str.length() - 1) {
                exp = str.substring(expPos + 1, str.length() - 1);
            } else {
                exp = null;
            }
            //Requesting a specific type..
            String numeric = str.substring(0, str.length() - 1);
            boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
            switch (lastChar) {
                case 'l' :
                case 'L' :
                    if (dec == null
                        && exp == null
                        && isDigits(numeric.substring(1))
                        && (numeric.charAt(0) == '-' || Character.isDigit(numeric.charAt(0)))) {
                        try {
                            return createLong(numeric);
                        } catch (NumberFormatException nfe) {
                            //Too big for a long
                        }
                        return createBigInteger(numeric);

                    }
                    throw new NumberFormatException(str + " is not a valid number.");
                case 'f' :
                case 'F' :
                    try {
                        Float f = NumberUtils.createFloat(numeric);
                        if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                            //If it's too big for a float or the float value = 0 and the string
                            //has non-zeros in it, then float does not have the precision we want
                            return f;
                        }

                    } catch (NumberFormatException nfe) {
                        // ignore the bad number
                    }
                    //Fall through
                case 'd' :
                case 'D' :
                    try {
                        Double d = NumberUtils.createDouble(numeric);
                        if (!(d.isInfinite() || (d.floatValue() == 0.0D && !allZeros))) {
                            return d;
                        }
                    } catch (NumberFormatException nfe) {
                        // ignore the bad number
                    }
                    try {
                        return createBigDecimal(numeric);
                    } catch (NumberFormatException e) {
                        // ignore the bad number
                    }
                    //Fall through
                default :
                    throw new NumberFormatException(str + " is not a valid number.");

            }
        } else {
            //User doesn't have a preference on the return type, so let's start
            //small and go from there...
            if (expPos > -1 && expPos < str.length() - 1) {
                exp = str.substring(expPos + 1, str.length());
            } else {
                exp = null;
            }
            if (dec == null && exp == null) {
                //Must be an int,long,bigint
                try {
                    return createInteger(str);
                } catch (NumberFormatException nfe) {
                    // ignore the bad number
                }
                try {
                    return createLong(str);
                } catch (NumberFormatException nfe) {
                    // ignore the bad number
                }
                return createBigInteger(str);

            } else {
                //Must be a float,double,BigDec
                boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
                try {
                    Float f = createFloat(str);
                    if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                        return f;
                    }
                } catch (NumberFormatException nfe) {
                    // ignore the bad number
                }
                try {
                    Double d = createDouble(str);
                    if (!(d.isInfinite() || (d.doubleValue() == 0.0D && !allZeros))) {
                        return d;
                    }
                } catch (NumberFormatException nfe) {
                    // ignore the bad number
                }

                return createBigDecimal(str);

            }
        }
    }

// relevant test
// org.apache.commons.lang.BooleanUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new BooleanUtils());
        Constructor[] cons = BooleanUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(BooleanUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(BooleanUtils.class.getModifiers()));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_negate_Boolean
    public void test_negate_Boolean() {
        assertSame(null, BooleanUtils.negate(null));
        assertSame(Boolean.TRUE, BooleanUtils.negate(Boolean.FALSE));
        assertSame(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_isTrue_Boolean
    public void test_isTrue_Boolean() {
        assertEquals(true, BooleanUtils.isTrue(Boolean.TRUE));
        assertEquals(false, BooleanUtils.isTrue(Boolean.FALSE));
        assertEquals(false, BooleanUtils.isTrue((Boolean) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_isFalse_Boolean
    public void test_isFalse_Boolean() {
        assertEquals(false, BooleanUtils.isFalse(Boolean.TRUE));
        assertEquals(true, BooleanUtils.isFalse(Boolean.FALSE));
        assertEquals(false, BooleanUtils.isFalse((Boolean) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_boolean
    public void test_toBooleanObject_boolean() {
        assertSame(Boolean.TRUE, BooleanUtils.toBooleanObject(true));
        assertSame(Boolean.FALSE, BooleanUtils.toBooleanObject(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_Boolean
    public void test_toBoolean_Boolean() {
        assertEquals(true, BooleanUtils.toBoolean(Boolean.TRUE));
        assertEquals(false, BooleanUtils.toBoolean(Boolean.FALSE));
        assertEquals(false, BooleanUtils.toBoolean((Boolean) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanDefaultIfNull_Boolean_boolean
    public void test_toBooleanDefaultIfNull_Boolean_boolean() {
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, true));
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, false));
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull((Boolean) null, true));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull((Boolean) null, false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_int
    public void test_toBoolean_int() {
        assertEquals(true, BooleanUtils.toBoolean(1));
        assertEquals(true, BooleanUtils.toBoolean(-1));
        assertEquals(false, BooleanUtils.toBoolean(0));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_int
    public void test_toBooleanObject_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(-1));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_Integer
    public void test_toBooleanObject_Integer() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(1)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(-1)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(new Integer(0)));
        assertEquals(null, BooleanUtils.toBooleanObject((Integer) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_int_int_int
    public void test_toBoolean_int_int_int() {
        assertEquals(true, BooleanUtils.toBoolean(6, 6, 7));
        assertEquals(false, BooleanUtils.toBoolean(7, 6, 7));
        try {
            BooleanUtils.toBoolean(8, 6, 7);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_Integer_Integer_Integer
    public void test_toBoolean_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);

        assertEquals(true, BooleanUtils.toBoolean((Integer) null, null, seven));
        assertEquals(false, BooleanUtils.toBoolean((Integer) null, six, null));
        try {
            BooleanUtils.toBoolean(null, six, seven);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(true, BooleanUtils.toBoolean(new Integer(6), six, seven));
        assertEquals(false, BooleanUtils.toBoolean(new Integer(7), six, seven));
        try {
            BooleanUtils.toBoolean(new Integer(8), six, seven);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_int_int_int
    public void test_toBooleanObject_int_int_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(6, 6, 7, 8));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(7, 6, 7, 8));
        assertEquals(null, BooleanUtils.toBooleanObject(8, 6, 7, 8));
        try {
            BooleanUtils.toBooleanObject(9, 6, 7, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_Integer_Integer_Integer_Integer
    public void test_toBooleanObject_Integer_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        Integer eight = new Integer(8);

        assertSame(Boolean.TRUE, BooleanUtils.toBooleanObject((Integer) null, null, seven, eight));
        assertSame(Boolean.FALSE, BooleanUtils.toBooleanObject((Integer) null, six, null, eight));
        assertSame(null, BooleanUtils.toBooleanObject((Integer) null, six, seven, null));
        try {
            BooleanUtils.toBooleanObject(null, six, seven, eight);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(6), six, seven, eight));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(new Integer(7), six, seven, eight));
        assertEquals(null, BooleanUtils.toBooleanObject(new Integer(8), six, seven, eight));
        try {
            BooleanUtils.toBooleanObject(new Integer(9), six, seven, eight);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toInteger_boolean
    public void test_toInteger_boolean() {
        assertEquals(1, BooleanUtils.toInteger(true));
        assertEquals(0, BooleanUtils.toInteger(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toIntegerObject_boolean
    public void test_toIntegerObject_boolean() {
        assertEquals(new Integer(1), BooleanUtils.toIntegerObject(true));
        assertEquals(new Integer(0), BooleanUtils.toIntegerObject(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toIntegerObject_Boolean
    public void test_toIntegerObject_Boolean() {
        assertEquals(new Integer(1), BooleanUtils.toIntegerObject(Boolean.TRUE));
        assertEquals(new Integer(0), BooleanUtils.toIntegerObject(Boolean.FALSE));
        assertEquals(null, BooleanUtils.toIntegerObject((Boolean) null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toInteger_boolean_int_int
    public void test_toInteger_boolean_int_int() {
        assertEquals(6, BooleanUtils.toInteger(true, 6, 7));
        assertEquals(7, BooleanUtils.toInteger(false, 6, 7));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toInteger_Boolean_int_int_int
    public void test_toInteger_Boolean_int_int_int() {
        assertEquals(6, BooleanUtils.toInteger(Boolean.TRUE, 6, 7, 8));
        assertEquals(7, BooleanUtils.toInteger(Boolean.FALSE, 6, 7, 8));
        assertEquals(8, BooleanUtils.toInteger(null, 6, 7, 8));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toIntegerObject_boolean_Integer_Integer
    public void test_toIntegerObject_boolean_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        assertEquals(six, BooleanUtils.toIntegerObject(true, six, seven));
        assertEquals(seven, BooleanUtils.toIntegerObject(false, six, seven));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toIntegerObject_Boolean_Integer_Integer_Integer
    public void test_toIntegerObject_Boolean_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        Integer eight = new Integer(8);
        assertEquals(six, BooleanUtils.toIntegerObject(Boolean.TRUE, six, seven, eight));
        assertEquals(seven, BooleanUtils.toIntegerObject(Boolean.FALSE, six, seven, eight));
        assertEquals(eight, BooleanUtils.toIntegerObject((Boolean) null, six, seven, eight));
        assertEquals(null, BooleanUtils.toIntegerObject((Boolean) null, six, seven, null));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_String
    public void test_toBooleanObject_String() {
        assertEquals(null, BooleanUtils.toBooleanObject((String) null));
        assertEquals(null, BooleanUtils.toBooleanObject(""));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("off"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("FALSE"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("NO"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("OFF"));
        assertEquals(null, BooleanUtils.toBooleanObject("oof"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("on"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TRUE"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("ON"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("YES"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TruE"));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBooleanObject_String_String_String_String
    public void test_toBooleanObject_String_String_String_String() {
        assertSame(Boolean.TRUE, BooleanUtils.toBooleanObject((String) null, null, "N", "U"));
        assertSame(Boolean.FALSE, BooleanUtils.toBooleanObject((String) null, "Y", null, "U"));
        assertSame(null, BooleanUtils.toBooleanObject((String) null, "Y", "N", null));
        try {
            BooleanUtils.toBooleanObject((String) null, "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("Y", "Y", "N", "U"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("N", "Y", "N", "U"));
        assertEquals(null, BooleanUtils.toBooleanObject("U", "Y", "N", "U"));
        try {
            BooleanUtils.toBooleanObject(null, "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            BooleanUtils.toBooleanObject("X", "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_String
    public void test_toBoolean_String() {
        assertEquals(false, BooleanUtils.toBoolean((String) null));
        assertEquals(false, BooleanUtils.toBoolean(""));
        assertEquals(false, BooleanUtils.toBoolean("off"));
        assertEquals(false, BooleanUtils.toBoolean("oof"));
        assertEquals(false, BooleanUtils.toBoolean("yep"));
        assertEquals(false, BooleanUtils.toBoolean("trux"));
        assertEquals(false, BooleanUtils.toBoolean("false"));
        assertEquals(false, BooleanUtils.toBoolean("a"));
        assertEquals(true, BooleanUtils.toBoolean("true")); 
        assertEquals(true, BooleanUtils.toBoolean(new StringBuffer("tr").append("ue").toString()));
        assertEquals(true, BooleanUtils.toBoolean("truE"));
        assertEquals(true, BooleanUtils.toBoolean("trUe"));
        assertEquals(true, BooleanUtils.toBoolean("trUE"));
        assertEquals(true, BooleanUtils.toBoolean("tRue"));
        assertEquals(true, BooleanUtils.toBoolean("tRuE"));
        assertEquals(true, BooleanUtils.toBoolean("tRUe"));
        assertEquals(true, BooleanUtils.toBoolean("tRUE"));
        assertEquals(true, BooleanUtils.toBoolean("TRUE"));
        assertEquals(true, BooleanUtils.toBoolean("TRUe"));
        assertEquals(true, BooleanUtils.toBoolean("TRuE"));
        assertEquals(true, BooleanUtils.toBoolean("TRue"));
        assertEquals(true, BooleanUtils.toBoolean("TrUE"));
        assertEquals(true, BooleanUtils.toBoolean("TrUe"));
        assertEquals(true, BooleanUtils.toBoolean("TruE"));
        assertEquals(true, BooleanUtils.toBoolean("True"));
        assertEquals(true, BooleanUtils.toBoolean("on"));
        assertEquals(true, BooleanUtils.toBoolean("oN"));
        assertEquals(true, BooleanUtils.toBoolean("On"));
        assertEquals(true, BooleanUtils.toBoolean("ON"));
        assertEquals(true, BooleanUtils.toBoolean("yes"));
        assertEquals(true, BooleanUtils.toBoolean("yeS"));
        assertEquals(true, BooleanUtils.toBoolean("yEs"));
        assertEquals(true, BooleanUtils.toBoolean("yES"));
        assertEquals(true, BooleanUtils.toBoolean("Yes"));
        assertEquals(true, BooleanUtils.toBoolean("YeS"));
        assertEquals(true, BooleanUtils.toBoolean("YEs"));
        assertEquals(true, BooleanUtils.toBoolean("YES"));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toBoolean_String_String_String
    public void test_toBoolean_String_String_String() {
        assertEquals(true, BooleanUtils.toBoolean((String) null, null, "N"));
        assertEquals(false, BooleanUtils.toBoolean((String) null, "Y", null));
        try {
            BooleanUtils.toBooleanObject((String) null, "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(true, BooleanUtils.toBoolean("Y", "Y", "N"));
        assertEquals(false, BooleanUtils.toBoolean("N", "Y", "N"));
        try {
            BooleanUtils.toBoolean(null, "Y", "N");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            BooleanUtils.toBoolean("X", "Y", "N");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringTrueFalse_Boolean
    public void test_toStringTrueFalse_Boolean() {
        assertEquals(null, BooleanUtils.toStringTrueFalse((Boolean) null));
        assertEquals("true", BooleanUtils.toStringTrueFalse(Boolean.TRUE));
        assertEquals("false", BooleanUtils.toStringTrueFalse(Boolean.FALSE));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringOnOff_Boolean
    public void test_toStringOnOff_Boolean() {
        assertEquals(null, BooleanUtils.toStringOnOff((Boolean) null));
        assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringYesNo_Boolean
    public void test_toStringYesNo_Boolean() {
        assertEquals(null, BooleanUtils.toStringYesNo((Boolean) null));
        assertEquals("yes", BooleanUtils.toStringYesNo(Boolean.TRUE));
        assertEquals("no", BooleanUtils.toStringYesNo(Boolean.FALSE));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toString_Boolean_String_String_String
    public void test_toString_Boolean_String_String_String() {
        assertEquals("U", BooleanUtils.toString((Boolean) null, "Y", "N", "U"));
        assertEquals("Y", BooleanUtils.toString(Boolean.TRUE, "Y", "N", "U"));
        assertEquals("N", BooleanUtils.toString(Boolean.FALSE, "Y", "N", "U"));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringTrueFalse_boolean
    public void test_toStringTrueFalse_boolean() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringOnOff_boolean
    public void test_toStringOnOff_boolean() {
        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toStringYesNo_boolean
    public void test_toStringYesNo_boolean() {
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));
    }

// org.apache.commons.lang.BooleanUtilsTest::test_toString_boolean_String_String_String
    public void test_toString_boolean_String_String_String() {
        assertEquals("Y", BooleanUtils.toString(true, "Y", "N"));
        assertEquals("N", BooleanUtils.toString(false, "Y", "N"));
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_primitive_nullInput
    public void testXor_primitive_nullInput() {
        final boolean[] b = null;
        try {
            BooleanUtils.xor(b);
            fail("Exception was not thrown for null input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_primitive_emptyInput
    public void testXor_primitive_emptyInput() {
        try {
            BooleanUtils.xor(new boolean[] {});
            fail("Exception was not thrown for empty input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_primitive_validInput_2items
    public void testXor_primitive_validInput_2items() {
        assertTrue(
            "True result for (true, true)",
            ! BooleanUtils.xor(new boolean[] { true, true }));

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils.xor(new boolean[] { false, false }));

        assertTrue(
            "False result for (true, false)",
            BooleanUtils.xor(new boolean[] { true, false }));

        assertTrue(
            "False result for (false, true)",
            BooleanUtils.xor(new boolean[] { false, true }));
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_primitive_validInput_3items
    public void testXor_primitive_validInput_3items() {
        assertTrue(
            "False result for (false, false, true)",
            BooleanUtils.xor(new boolean[] { false, false, true }));

        assertTrue(
            "False result for (false, true, false)",
            BooleanUtils.xor(new boolean[] { false, true, false }));

        assertTrue(
            "False result for (true, false, false)",
            BooleanUtils.xor(new boolean[] { true, false, false }));

        assertTrue(
            "True result for (true, true, true)",
            ! BooleanUtils.xor(new boolean[] { true, true, true }));

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils.xor(new boolean[] { false, false, false }));

        assertTrue(
            "True result for (true, true, false)",
            ! BooleanUtils.xor(new boolean[] { true, true, false }));

        assertTrue(
            "True result for (true, false, true)",
            ! BooleanUtils.xor(new boolean[] { true, false, true }));

        assertTrue(
            "False result for (false, true, true)",
            ! BooleanUtils.xor(new boolean[] { false, true, true }));
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_object_nullInput
    public void testXor_object_nullInput() {
        final Boolean[] b = null;
        try {
            BooleanUtils.xor(b);
            fail("Exception was not thrown for null input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_object_emptyInput
    public void testXor_object_emptyInput() {
        try {
            BooleanUtils.xor(new Boolean[] {});
            fail("Exception was not thrown for empty input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_object_nullElementInput
    public void testXor_object_nullElementInput() {
        try {
            BooleanUtils.xor(new Boolean[] {null});
            fail("Exception was not thrown for null element input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_object_validInput_2items
    public void testXor_object_validInput_2items() {
        assertTrue(
            "True result for (true, true)",
            ! BooleanUtils
                .xor(new Boolean[] { Boolean.TRUE, Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils
                .xor(new Boolean[] { Boolean.FALSE, Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "False result for (true, false)",
            BooleanUtils
                .xor(new Boolean[] { Boolean.TRUE, Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "False result for (false, true)",
            BooleanUtils
                .xor(new Boolean[] { Boolean.FALSE, Boolean.TRUE })
                .booleanValue());
    }

// org.apache.commons.lang.BooleanUtilsTest::testXor_object_validInput_3items
    public void testXor_object_validInput_3items() {
        assertTrue(
            "False result for (false, false, true)",
            BooleanUtils
                .xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.FALSE,
                        Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "False result for (false, true, false)",
            BooleanUtils
                .xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.TRUE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "False result for (true, false, false)",
            BooleanUtils
                .xor(
                    new Boolean[] {
                        Boolean.TRUE,
                        Boolean.FALSE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "True result for (true, true, true)",
            ! BooleanUtils
                .xor(new Boolean[] { Boolean.TRUE, Boolean.TRUE, Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.FALSE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "True result for (true, true, false)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.TRUE,
                        Boolean.TRUE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "True result for (true, false, true)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.TRUE,
                        Boolean.FALSE,
                        Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "False result for (false, true, true)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.TRUE,
                        Boolean.TRUE })
                .booleanValue());
                
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testReflectionCompare
    public void testReflectionCompare() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(4);
        assertTrue(CompareToBuilder.reflectionCompare(o1, o1) == 0);
        assertTrue(CompareToBuilder.reflectionCompare(o1, o2) == 0);
        o2.setA(5);
        assertTrue(CompareToBuilder.reflectionCompare(o1, o2) < 0);
        assertTrue(CompareToBuilder.reflectionCompare(o2, o1) > 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testReflectionCompareEx1
    public void testReflectionCompareEx1() {
        TestObject o1 = new TestObject(4);
        try {
            CompareToBuilder.reflectionCompare(o1, null);
        } catch (NullPointerException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testReflectionCompareEx2
    public void testReflectionCompareEx2() {
        TestObject o1 = new TestObject(4);
        Object o2 = new Object();
        try {
            CompareToBuilder.reflectionCompare(o1, o2);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testReflectionHierarchyCompare
    public void testReflectionHierarchyCompare() {
        testReflectionHierarchyCompare(false, null);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testReflectionHierarchyCompareExcludeFields
    public void testReflectionHierarchyCompareExcludeFields() {
        String[] excludeFields = new String[] { "b" };
        testReflectionHierarchyCompare(true, excludeFields);
        
        TestSubObject x;
        TestSubObject y;
        TestSubObject z;
        
        x = new TestSubObject(1, 1);
        y = new TestSubObject(2, 1);
        z = new TestSubObject(3, 1);
        assertXYZCompareOrder(x, y, z, true, excludeFields);

        x = new TestSubObject(1, 3);
        y = new TestSubObject(2, 2);
        z = new TestSubObject(3, 1);
        assertXYZCompareOrder(x, y, z, true, excludeFields);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testReflectionHierarchyCompareTransients
    public void testReflectionHierarchyCompareTransients() {
        testReflectionHierarchyCompare(true, null);

        TestTransientSubObject x;
        TestTransientSubObject y;
        TestTransientSubObject z;

        x = new TestTransientSubObject(1, 1);
        y = new TestTransientSubObject(2, 2);
        z = new TestTransientSubObject(3, 3);
        assertXYZCompareOrder(x, y, z, true, null);
        
        x = new TestTransientSubObject(1, 1);
        y = new TestTransientSubObject(1, 2);
        z = new TestTransientSubObject(1, 3);
        assertXYZCompareOrder(x, y, z, true, null);  
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testReflectionHierarchyCompare
    public void testReflectionHierarchyCompare(boolean testTransients, String[] excludeFields) {
        TestObject to1 = new TestObject(1);
        TestObject to2 = new TestObject(2);
        TestObject to3 = new TestObject(3);
        TestSubObject tso1 = new TestSubObject(1, 1);
        TestSubObject tso2 = new TestSubObject(2, 2);
        TestSubObject tso3 = new TestSubObject(3, 3);
        
        assertReflectionCompareContract(to1, to1, to1, false, excludeFields);
        assertReflectionCompareContract(to1, to2, to3, false, excludeFields);
        assertReflectionCompareContract(tso1, tso1, tso1, false, excludeFields);
        assertReflectionCompareContract(tso1, tso2, tso3, false, excludeFields);
        assertReflectionCompareContract("1", "2", "3", false, excludeFields);
        
        assertTrue(0 != CompareToBuilder.reflectionCompare(tso1, new TestSubObject(1, 0), testTransients));
        assertTrue(0 != CompareToBuilder.reflectionCompare(tso1, new TestSubObject(0, 1), testTransients));

        
        assertXYZCompareOrder(to1, to2, to3, true, null);
        
        assertXYZCompareOrder(tso1, tso2, tso3, true, null);  
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testAppendSuper
    public void testAppendSuper() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(5);
        assertTrue(new CompareToBuilder().appendSuper(0).append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().appendSuper(0).append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().appendSuper(0).append(o2, o1).toComparison() > 0);
        
        assertTrue(new CompareToBuilder().appendSuper(-1).append(o1, o1).toComparison() < 0);
        assertTrue(new CompareToBuilder().appendSuper(-1).append(o1, o2).toComparison() < 0);
        
        assertTrue(new CompareToBuilder().appendSuper(1).append(o1, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().appendSuper(1).append(o1, o2).toComparison() > 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testObject
    public void testObject() {
        TestObject o1 = new TestObject(4);
        TestObject o2 = new TestObject(4);
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() == 0);
        o2.setA(5);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        
        assertTrue(new CompareToBuilder().append(o1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((Object) null, (Object) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, o1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testObjectEx2
    public void testObjectEx2() {
        TestObject o1 = new TestObject(4);
        Object o2 = new Object();
        try {
            new CompareToBuilder().append(o1, o2);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testObjectComparator
    public void testObjectComparator() {
        String o1 = "Fred";
        String o2 = "Fred";
        assertTrue(new CompareToBuilder().append(o1, o1, String.CASE_INSENSITIVE_ORDER).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2, String.CASE_INSENSITIVE_ORDER).toComparison() == 0);
        o2 = "FRED";
        assertTrue(new CompareToBuilder().append(o1, o2, String.CASE_INSENSITIVE_ORDER).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o2, o1, String.CASE_INSENSITIVE_ORDER).toComparison() == 0);
        o2 = "FREDA";
        assertTrue(new CompareToBuilder().append(o1, o2, String.CASE_INSENSITIVE_ORDER).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1, String.CASE_INSENSITIVE_ORDER).toComparison() > 0);
        
        assertTrue(new CompareToBuilder().append(o1, null, String.CASE_INSENSITIVE_ORDER).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((Object) null, (Object) null, String.CASE_INSENSITIVE_ORDER).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, o1, String.CASE_INSENSITIVE_ORDER).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testObjectComparatorNull
    public void testObjectComparatorNull() {
        String o1 = "Fred";
        String o2 = "Fred";
        assertTrue(new CompareToBuilder().append(o1, o1, null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2, null).toComparison() == 0);
        o2 = "Zebra";
        assertTrue(new CompareToBuilder().append(o1, o2, null).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1, null).toComparison() > 0);
        
        assertTrue(new CompareToBuilder().append(o1, null, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((Object) null, (Object) null, null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, o1, null).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testLong
    public void testLong() {
        long o1 = 1L;
        long o2 = 2L;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Long.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Long.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Long.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Long.MIN_VALUE, o1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testInt
    public void testInt() {
        int o1 = 1;
        int o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Integer.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Integer.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Integer.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Integer.MIN_VALUE, o1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testShort
    public void testShort() {
        short o1 = 1;
        short o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Short.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Short.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Short.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Short.MIN_VALUE, o1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testChar
    public void testChar() {
        char o1 = 1;
        char o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Character.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Character.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Character.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Character.MIN_VALUE, o1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testByte
    public void testByte() {
        byte o1 = 1;
        byte o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Byte.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Byte.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Byte.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Byte.MIN_VALUE, o1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testDouble
    public void testDouble() {
        double o1 = 1;
        double o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Double.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Double.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Double.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Double.MIN_VALUE, o1).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Double.NaN, Double.NaN).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(Double.NaN, Double.MAX_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Double.POSITIVE_INFINITY, Double.MAX_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Double.NEGATIVE_INFINITY, Double.MIN_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o1, Double.NaN).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Double.NaN, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(-0.0, 0.0).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(0.0, -0.0).toComparison() > 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testFloat
    public void testFloat() {
        float o1 = 1;
        float o2 = 2;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Float.MAX_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Float.MAX_VALUE, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o1, Float.MIN_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Float.MIN_VALUE, o1).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Float.NaN, Float.NaN).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(Float.NaN, Float.MAX_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Float.POSITIVE_INFINITY, Float.MAX_VALUE).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(Float.NEGATIVE_INFINITY, Float.MIN_VALUE).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(o1, Float.NaN).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(Float.NaN, o1).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(-0.0, 0.0).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(0.0, -0.0).toComparison() > 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testBoolean
    public void testBoolean() {
        boolean o1 = true;
        boolean o2 = false;
        assertTrue(new CompareToBuilder().append(o1, o1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o2, o2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(o1, o2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(o2, o1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testObjectArray
    public void testObjectArray() {
        TestObject[] obj1 = new TestObject[2];
        obj1[0] = new TestObject(4);
        obj1[1] = new TestObject(5);
        TestObject[] obj2 = new TestObject[2];
        obj2[0] = new TestObject(4);
        obj2[1] = new TestObject(5);
        TestObject[] obj3 = new TestObject[3];
        obj3[0] = new TestObject(4);
        obj3[1] = new TestObject(5);
        obj3[2] = new TestObject(6);
        
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);
        
        obj1[1] = new TestObject(7);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((Object[]) null, (Object[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testLongArray
    public void testLongArray() {
        long[] obj1 = new long[2];
        obj1[0] = 5L;
        obj1[1] = 6L;
        long[] obj2 = new long[2];
        obj2[0] = 5L;
        obj2[1] = 6L;
        long[] obj3 = new long[3];
        obj3[0] = 5L;
        obj3[1] = 6L;
        obj3[2] = 7L;
        
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((long[]) null, (long[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testIntArray
    public void testIntArray() {
        int[] obj1 = new int[2];
        obj1[0] = 5;
        obj1[1] = 6;
        int[] obj2 = new int[2];
        obj2[0] = 5;
        obj2[1] = 6;
        int[] obj3 = new int[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((int[]) null, (int[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testShortArray
    public void testShortArray() {
        short[] obj1 = new short[2];
        obj1[0] = 5;
        obj1[1] = 6;
        short[] obj2 = new short[2];
        obj2[0] = 5;
        obj2[1] = 6;
        short[] obj3 = new short[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((short[]) null, (short[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testCharArray
    public void testCharArray() {
        char[] obj1 = new char[2];
        obj1[0] = 5;
        obj1[1] = 6;
        char[] obj2 = new char[2];
        obj2[0] = 5;
        obj2[1] = 6;
        char[] obj3 = new char[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((char[]) null, (char[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testByteArray
    public void testByteArray() {
        byte[] obj1 = new byte[2];
        obj1[0] = 5;
        obj1[1] = 6;
        byte[] obj2 = new byte[2];
        obj2[0] = 5;
        obj2[1] = 6;
        byte[] obj3 = new byte[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((byte[]) null, (byte[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testDoubleArray
    public void testDoubleArray() {
        double[] obj1 = new double[2];
        obj1[0] = 5;
        obj1[1] = 6;
        double[] obj2 = new double[2];
        obj2[0] = 5;
        obj2[1] = 6;
        double[] obj3 = new double[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((double[]) null, (double[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testFloatArray
    public void testFloatArray() {
        float[] obj1 = new float[2];
        obj1[0] = 5;
        obj1[1] = 6;
        float[] obj2 = new float[2];
        obj2[0] = 5;
        obj2[1] = 6;
        float[] obj3 = new float[3];
        obj3[0] = 5;
        obj3[1] = 6;
        obj3[2] = 7;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((float[]) null, (float[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testBooleanArray
    public void testBooleanArray() {
        boolean[] obj1 = new boolean[2];
        obj1[0] = true;
        obj1[1] = false;
        boolean[] obj2 = new boolean[2];
        obj2[0] = true;
        obj2[1] = false;
        boolean[] obj3 = new boolean[3];
        obj3[0] = true;
        obj3[1] = false;
        obj3[2] = true;

        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        obj1[1] = true;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);

        assertTrue(new CompareToBuilder().append(obj1, null).toComparison() > 0);
        assertTrue(new CompareToBuilder().append((boolean[]) null, (boolean[]) null).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(null, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testMultiLongArray
    public void testMultiLongArray() {
        long[][] array1 = new long[2][2];
        long[][] array2 = new long[2][2];
        long[][] array3 = new long[2][3];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
                array3[i][j] = (i + 1) * (j + 1);
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testMultiIntArray
    public void testMultiIntArray() {
        int[][] array1 = new int[2][2];
        int[][] array2 = new int[2][2];
        int[][] array3 = new int[2][3];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
                array3[i][j] = (i + 1) * (j + 1);
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testMultiShortArray
    public void testMultiShortArray() {
        short[][] array1 = new short[2][2];
        short[][] array2 = new short[2][2];
        short[][] array3 = new short[2][3];
        for (short i = 0; i < array1.length; ++i) {
            for (short j = 0; j < array1[0].length; j++) {
                array1[i][j] = (short)((i + 1) * (j + 1));
                array2[i][j] = (short)((i + 1) * (j + 1));
                array3[i][j] = (short)((i + 1) * (j + 1));
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testMultiCharArray
    public void testMultiCharArray() {
        char[][] array1 = new char[2][2];
        char[][] array2 = new char[2][2];
        char[][] array3 = new char[2][3];
        for (short i = 0; i < array1.length; ++i) {
            for (short j = 0; j < array1[0].length; j++) {
                array1[i][j] = (char)((i + 1) * (j + 1));
                array2[i][j] = (char)((i + 1) * (j + 1));
                array3[i][j] = (char)((i + 1) * (j + 1));
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testMultiByteArray
    public void testMultiByteArray() {
        byte[][] array1 = new byte[2][2];
        byte[][] array2 = new byte[2][2];
        byte[][] array3 = new byte[2][3];
        for (byte i = 0; i < array1.length; ++i) {
            for (byte j = 0; j < array1[0].length; j++) {
                array1[i][j] = (byte)((i + 1) * (j + 1));
                array2[i][j] = (byte)((i + 1) * (j + 1));
                array3[i][j] = (byte)((i + 1) * (j + 1));
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 127;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testMultiFloatArray
    public void testMultiFloatArray() {
        float[][] array1 = new float[2][2];
        float[][] array2 = new float[2][2];
        float[][] array3 = new float[2][3];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = ((i + 1) * (j + 1));
                array2[i][j] = ((i + 1) * (j + 1));
                array3[i][j] = ((i + 1) * (j + 1));
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 127;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testMultiDoubleArray
    public void testMultiDoubleArray() {
        double[][] array1 = new double[2][2];
        double[][] array2 = new double[2][2];
        double[][] array3 = new double[2][3];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = ((i + 1) * (j + 1));
                array2[i][j] = ((i + 1) * (j + 1));
                array3[i][j] = ((i + 1) * (j + 1));
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 127;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testMultiBooleanArray
    public void testMultiBooleanArray() {
        boolean[][] array1 = new boolean[2][2];
        boolean[][] array2 = new boolean[2][2];
        boolean[][] array3 = new boolean[2][3];
        for (int i = 0; i < array1.length; ++i) {
            for (int j = 0; j < array1[0].length; j++) {
                array1[i][j] = ((i == 1) ^ (j == 1));
                array2[i][j] = ((i == 1) ^ (j == 1));
                array3[i][j] = ((i == 1) ^ (j == 1));
            }
        }
        array3[1][2] = false;
        array3[1][2] = false;
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = true;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testRaggedArray
    public void testRaggedArray() {
        long array1[][] = new long[2][];
        long array2[][] = new long[2][];
        long array3[][] = new long[3][];
        for (int i = 0; i < array1.length; ++i) {
            array1[i] = new long[2];
            array2[i] = new long[2];
            array3[i] = new long[3];
            for (int j = 0; j < array1[i].length; ++j) {
                array1[i][j] = (i + 1) * (j + 1);
                array2[i][j] = (i + 1) * (j + 1);
                array3[i][j] = (i + 1) * (j + 1);
            }
        }
        array3[1][2] = 100;
        array3[1][2] = 100;
        
        
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        array1[1][1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testMixedArray
    public void testMixedArray() {
        Object array1[] = new Object[2];
        Object array2[] = new Object[2];
        Object array3[] = new Object[2];
        for (int i = 0; i < array1.length; ++i) {
            array1[i] = new long[2];
            array2[i] = new long[2];
            array3[i] = new long[3];
            for (int j = 0; j < 2; ++j) {
                ((long[]) array1[i])[j] = (i + 1) * (j + 1);
                ((long[]) array2[i])[j] = (i + 1) * (j + 1);
                ((long[]) array3[i])[j] = (i + 1) * (j + 1);
            }
        }
        ((long[]) array3[0])[2] = 1;
        ((long[]) array3[1])[2] = 1;
        assertTrue(new CompareToBuilder().append(array1, array1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(array1, array3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(array3, array1).toComparison() > 0);
        ((long[]) array1[1])[1] = 200;
        assertTrue(new CompareToBuilder().append(array1, array2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(array2, array1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testObjectArrayHiddenByObject
    public void testObjectArrayHiddenByObject() {
        TestObject[] array1 = new TestObject[2];
        array1[0] = new TestObject(4);
        array1[1] = new TestObject(5);
        TestObject[] array2 = new TestObject[2];
        array2[0] = new TestObject(4);
        array2[1] = new TestObject(5);
        TestObject[] array3 = new TestObject[3];
        array3[0] = new TestObject(4);
        array3[1] = new TestObject(5);
        array3[2] = new TestObject(6);
        
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = new TestObject(7);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testLongArrayHiddenByObject
    public void testLongArrayHiddenByObject() {
        long[] array1 = new long[2];
        array1[0] = 5L;
        array1[1] = 6L;
        long[] array2 = new long[2];
        array2[0] = 5L;
        array2[1] = 6L;
        long[] array3 = new long[3];
        array3[0] = 5L;
        array3[1] = 6L;
        array3[2] = 7L;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testIntArrayHiddenByObject
    public void testIntArrayHiddenByObject() {
        int[] array1 = new int[2];
        array1[0] = 5;
        array1[1] = 6;
        int[] array2 = new int[2];
        array2[0] = 5;
        array2[1] = 6;
        int[] array3 = new int[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testShortArrayHiddenByObject
    public void testShortArrayHiddenByObject() {
        short[] array1 = new short[2];
        array1[0] = 5;
        array1[1] = 6;
        short[] array2 = new short[2];
        array2[0] = 5;
        array2[1] = 6;
        short[] array3 = new short[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testCharArrayHiddenByObject
    public void testCharArrayHiddenByObject() {
        char[] array1 = new char[2];
        array1[0] = 5;
        array1[1] = 6;
        char[] array2 = new char[2];
        array2[0] = 5;
        array2[1] = 6;
        char[] array3 = new char[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testByteArrayHiddenByObject
    public void testByteArrayHiddenByObject() {
        byte[] array1 = new byte[2];
        array1[0] = 5;
        array1[1] = 6;
        byte[] array2 = new byte[2];
        array2[0] = 5;
        array2[1] = 6;
        byte[] array3 = new byte[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testDoubleArrayHiddenByObject
    public void testDoubleArrayHiddenByObject() {
        double[] array1 = new double[2];
        array1[0] = 5;
        array1[1] = 6;
        double[] array2 = new double[2];
        array2[0] = 5;
        array2[1] = 6;
        double[] array3 = new double[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testFloatArrayHiddenByObject
    public void testFloatArrayHiddenByObject() {
        float[] array1 = new float[2];
        array1[0] = 5;
        array1[1] = 6;
        float[] array2 = new float[2];
        array2[0] = 5;
        array2[1] = 6;
        float[] array3 = new float[3];
        array3[0] = 5;
        array3[1] = 6;
        array3[2] = 7;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = 7;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.builder.CompareToBuilderTest::testBooleanArrayHiddenByObject
    public void testBooleanArrayHiddenByObject() {
        boolean[] array1 = new boolean[2];
        array1[0] = true;
        array1[1] = false;
        boolean[] array2 = new boolean[2];
        array2[0] = true;
        array2[1] = false;
        boolean[] array3 = new boolean[3];
        array3[0] = true;
        array3[1] = false;
        array3[2] = true;
        Object obj1 = array1;
        Object obj2 = array2;
        Object obj3 = array3;
        assertTrue(new CompareToBuilder().append(obj1, obj1).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() == 0);
        assertTrue(new CompareToBuilder().append(obj1, obj3).toComparison() < 0);
        assertTrue(new CompareToBuilder().append(obj3, obj1).toComparison() > 0);

        array1[1] = true;
        assertTrue(new CompareToBuilder().append(obj1, obj2).toComparison() > 0);
        assertTrue(new CompareToBuilder().append(obj2, obj1).toComparison() < 0);
    }

// org.apache.commons.lang.math.DoubleRangeTest::testConstructor1a
    public void testConstructor1a() {
        DoubleRange nr = new DoubleRange(8d);
        assertEquals(double8, nr.getMinimumNumber());
        assertEquals(double8, nr.getMaximumNumber());
        
        try {
            new DoubleRange(Double.NaN);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.DoubleRangeTest::testConstructor1b
    public void testConstructor1b() {
        DoubleRange nr = new DoubleRange(double8);
        assertSame(double8, nr.getMinimumNumber());
        assertSame(double8, nr.getMaximumNumber());
        
        Range r = new DoubleRange(nonComparableNumber);
        
        try {
            new DoubleRange(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DoubleRange(new Double(Double.NaN));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.DoubleRangeTest::testConstructor2a
    public void testConstructor2a() {
        DoubleRange nr = new DoubleRange(8d, 10d);
        assertEquals(double8, nr.getMinimumNumber());
        assertEquals(double10, nr.getMaximumNumber());
        
        nr = new DoubleRange(10d, 8d);
        assertEquals(double8, nr.getMinimumNumber());
        assertEquals(double10, nr.getMaximumNumber());
        
        try {
            new DoubleRange(Double.NaN, 8d);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.DoubleRangeTest::testConstructor2b
    public void testConstructor2b() {
        DoubleRange nr = new DoubleRange(double8, double10);
        assertSame(double8, nr.getMinimumNumber());
        assertSame(double10, nr.getMaximumNumber());
        
        nr = new DoubleRange(double10, double8);
        assertSame(double8, nr.getMinimumNumber());
        assertSame(double10, nr.getMaximumNumber());
        
        nr = new DoubleRange(double8, double10);
        assertSame(double8, nr.getMinimumNumber());
        assertEquals(double10, nr.getMaximumNumber());
        
        
        try {
            new DoubleRange(double8, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DoubleRange(null, double8);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DoubleRange(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        try {
            new DoubleRange(new Double(Double.NaN), double10);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.DoubleRangeTest::testContainsNumber
    public void testContainsNumber() {
        assertEquals(false, tenToTwenty.containsNumber(null));
        assertEquals(true, tenToTwenty.containsNumber(nonComparableNumber));
        
        assertEquals(false, tenToTwenty.containsNumber(five));
        assertEquals(true, tenToTwenty.containsNumber(ten));
        assertEquals(true, tenToTwenty.containsNumber(fifteen));
        assertEquals(true, tenToTwenty.containsNumber(twenty));
        assertEquals(false, tenToTwenty.containsNumber(twentyFive));
        
        assertEquals(false, tenToTwenty.containsNumber(long8));
        assertEquals(true, tenToTwenty.containsNumber(long10));
        assertEquals(true, tenToTwenty.containsNumber(long12));
        assertEquals(true, tenToTwenty.containsNumber(long20));
        assertEquals(false, tenToTwenty.containsNumber(long21));
        
        assertEquals(false, tenToTwenty.containsNumber(double8));
        assertEquals(true, tenToTwenty.containsNumber(double10));
        assertEquals(true, tenToTwenty.containsNumber(double12));
        assertEquals(true, tenToTwenty.containsNumber(double20));
        assertEquals(false, tenToTwenty.containsNumber(double21));
        
        assertEquals(false, tenToTwenty.containsNumber(float8));
        assertEquals(true, tenToTwenty.containsNumber(float10));
        assertEquals(true, tenToTwenty.containsNumber(float12));
        assertEquals(true, tenToTwenty.containsNumber(float20));
        assertEquals(false, tenToTwenty.containsNumber(float21));
    }

// org.apache.commons.lang.math.DoubleRangeTest::testToString
    public void testToString() {
        String str = tenToTwenty.toString();
        assertEquals("Range[10.0,20.0]", str);
        assertSame(str, tenToTwenty.toString());
        assertEquals("Range[-20.0,-10.0]", createRange(new Integer(-20), new Integer(-10)).toString());
    }

// org.apache.commons.lang.math.FloatRangeTest::testConstructor1a
    public void testConstructor1a() {
        FloatRange nr = new FloatRange(8f);
        assertEquals(float8, nr.getMinimumNumber());
        assertEquals(float8, nr.getMaximumNumber());
        
        try {
            new FloatRange(Float.NaN);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.FloatRangeTest::testConstructor1b
    public void testConstructor1b() {
        FloatRange nr = new FloatRange(float8);
        assertSame(float8, nr.getMinimumNumber());
        assertSame(float8, nr.getMaximumNumber());
        
        Range r = new FloatRange(nonComparableNumber);
        
        try {
            new FloatRange(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new FloatRange(new Double(Double.NaN));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.FloatRangeTest::testConstructor2a
    public void testConstructor2a() {
        FloatRange nr = new FloatRange(8f, 10f);
        assertEquals(float8, nr.getMinimumNumber());
        assertEquals(float10, nr.getMaximumNumber());
        
        nr = new FloatRange(10f, 8f);
        assertEquals(float8, nr.getMinimumNumber());
        assertEquals(float10, nr.getMaximumNumber());
        
        try {
            new FloatRange(Float.NaN, 8f);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.FloatRangeTest::testConstructor2b
    public void testConstructor2b() {
        FloatRange nr = new FloatRange(float8, float10);
        assertSame(float8, nr.getMinimumNumber());
        assertSame(float10, nr.getMaximumNumber());
        
        nr = new FloatRange(float10, float8);
        assertSame(float8, nr.getMinimumNumber());
        assertSame(float10, nr.getMaximumNumber());
        
        nr = new FloatRange(float8, float10);
        assertSame(float8, nr.getMinimumNumber());
        assertEquals(float10, nr.getMaximumNumber());
        
        
        try {
            new FloatRange(float8, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new FloatRange(null, float8);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new FloatRange(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        try {
            new FloatRange(new Double(Double.NaN), float10);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.FloatRangeTest::testContainsNumber
    public void testContainsNumber() {
        assertEquals(false, tenToTwenty.containsNumber(null));
        assertEquals(true, tenToTwenty.containsNumber(nonComparableNumber));
        
        assertEquals(false, tenToTwenty.containsNumber(five));
        assertEquals(true, tenToTwenty.containsNumber(ten));
        assertEquals(true, tenToTwenty.containsNumber(fifteen));
        assertEquals(true, tenToTwenty.containsNumber(twenty));
        assertEquals(false, tenToTwenty.containsNumber(twentyFive));
        
        assertEquals(false, tenToTwenty.containsNumber(long8));
        assertEquals(true, tenToTwenty.containsNumber(long10));
        assertEquals(true, tenToTwenty.containsNumber(long12));
        assertEquals(true, tenToTwenty.containsNumber(long20));
        assertEquals(false, tenToTwenty.containsNumber(long21));
        
        assertEquals(false, tenToTwenty.containsNumber(double8));
        assertEquals(true, tenToTwenty.containsNumber(double10));
        assertEquals(true, tenToTwenty.containsNumber(double12));
        assertEquals(true, tenToTwenty.containsNumber(double20));
        assertEquals(false, tenToTwenty.containsNumber(double21));
        
        assertEquals(false, tenToTwenty.containsNumber(float8));
        assertEquals(true, tenToTwenty.containsNumber(float10));
        assertEquals(true, tenToTwenty.containsNumber(float12));
        assertEquals(true, tenToTwenty.containsNumber(float20));
        assertEquals(false, tenToTwenty.containsNumber(float21));
    }

// org.apache.commons.lang.math.FloatRangeTest::testToString
    public void testToString() {
        String str = tenToTwenty.toString();
        assertEquals("Range[10.0,20.0]", str);
        assertSame(str, tenToTwenty.toString());
        assertEquals("Range[-20.0,-10.0]", createRange(new Integer(-20), new Integer(-10)).toString());
    }

// org.apache.commons.lang.math.IntRangeTest::testConstructor1a
    public void testConstructor1a() {
        IntRange nr = new IntRange(5);
        assertEquals(five, nr.getMinimumNumber());
        assertEquals(five, nr.getMaximumNumber());
    }

// org.apache.commons.lang.math.IntRangeTest::testConstructor1b
    public void testConstructor1b() {
        IntRange nr = new IntRange(five);
        assertSame(five, nr.getMinimumNumber());
        assertSame(five, nr.getMaximumNumber());
        
        Range r = new IntRange(nonComparableNumber);
        
        try {
            new IntRange(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.IntRangeTest::testConstructor2a
    public void testConstructor2a() {
        IntRange nr = new IntRange(5, 10);
        assertEquals(five, nr.getMinimumNumber());
        assertEquals(ten, nr.getMaximumNumber());
        
        nr = new IntRange(5, 10);
        assertEquals(five, nr.getMinimumNumber());
        assertEquals(ten, nr.getMaximumNumber());
    }

// org.apache.commons.lang.math.IntRangeTest::testConstructor2b
    public void testConstructor2b() {
        IntRange nr = new IntRange(five, ten);
        assertSame(five, nr.getMinimumNumber());
        assertSame(ten, nr.getMaximumNumber());
        
        nr = new IntRange(ten, five);
        assertSame(five, nr.getMinimumNumber());
        assertSame(ten, nr.getMaximumNumber());
        
        nr = new IntRange(five, long10);
        assertSame(five, nr.getMinimumNumber());
        assertEquals(ten, nr.getMaximumNumber());
        
        
        Long fiveL = new Long(5L);
        Long tenL = new Long(10L);
        nr = new IntRange(fiveL, tenL);
        assertEquals(five, nr.getMinimumNumber());
        assertEquals(ten, nr.getMaximumNumber());
        nr = new IntRange(tenL, fiveL);
        assertEquals(five, nr.getMinimumNumber());
        assertEquals(ten, nr.getMaximumNumber());
        
        
        try {
            new IntRange(five, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new IntRange(null, five);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new IntRange(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.IntRangeTest::testContainsNumber
    public void testContainsNumber() {
        assertEquals(false, tenToTwenty.containsNumber(null));
        assertEquals(true, tenToTwenty.containsNumber(nonComparableNumber));
        
        assertEquals(false, tenToTwenty.containsNumber(five));
        assertEquals(true, tenToTwenty.containsNumber(ten));
        assertEquals(true, tenToTwenty.containsNumber(fifteen));
        assertEquals(true, tenToTwenty.containsNumber(twenty));
        assertEquals(false, tenToTwenty.containsNumber(twentyFive));
        
        assertEquals(false, tenToTwenty.containsNumber(long8));
        assertEquals(true, tenToTwenty.containsNumber(long10));
        assertEquals(true, tenToTwenty.containsNumber(long12));
        assertEquals(true, tenToTwenty.containsNumber(long20));
        assertEquals(false, tenToTwenty.containsNumber(long21));
        
        assertEquals(false, tenToTwenty.containsNumber(double8));
        assertEquals(true, tenToTwenty.containsNumber(double10));
        assertEquals(true, tenToTwenty.containsNumber(double12));
        assertEquals(true, tenToTwenty.containsNumber(double20));
        assertEquals(false, tenToTwenty.containsNumber(double21));
        
        assertEquals(false, tenToTwenty.containsNumber(float8));
        assertEquals(true, tenToTwenty.containsNumber(float10));
        assertEquals(true, tenToTwenty.containsNumber(float12));
        assertEquals(true, tenToTwenty.containsNumber(float20));
        assertEquals(false, tenToTwenty.containsNumber(float21));
    }

// org.apache.commons.lang.math.IntRangeTest::testContainsIntegerBig
    public void testContainsIntegerBig() {
        IntRange big = new IntRange(Integer.MAX_VALUE, Integer.MAX_VALUE- 2);
        assertEquals(true, big.containsInteger(Integer.MAX_VALUE - 1));
        assertEquals(false, big.containsInteger(Integer.MAX_VALUE - 3));
    }

// org.apache.commons.lang.math.LongRangeTest::testConstructor1a
    public void testConstructor1a() {
        LongRange nr = new LongRange(8L);
        assertEquals(long8, nr.getMinimumNumber());
        assertEquals(long8, nr.getMaximumNumber());
    }

// org.apache.commons.lang.math.LongRangeTest::testConstructor1b
    public void testConstructor1b() {
        LongRange nr = new LongRange(long8);
        assertSame(long8, nr.getMinimumNumber());
        assertSame(long8, nr.getMaximumNumber());
        
        Range r = new LongRange(nonComparableNumber);
        
        try {
            new LongRange(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.LongRangeTest::testConstructor2a
    public void testConstructor2a() {
        LongRange nr = new LongRange(8L, 10L);
        assertEquals(long8, nr.getMinimumNumber());
        assertEquals(long10, nr.getMaximumNumber());
        
        nr = new LongRange(10L, 8L);
        assertEquals(long8, nr.getMinimumNumber());
        assertEquals(long10, nr.getMaximumNumber());
    }

// org.apache.commons.lang.math.LongRangeTest::testConstructor2b
    public void testConstructor2b() {
        LongRange nr = new LongRange(long8, long10);
        assertSame(long8, nr.getMinimumNumber());
        assertSame(long10, nr.getMaximumNumber());
        
        nr = new LongRange(long10, long8);
        assertSame(long8, nr.getMinimumNumber());
        assertSame(long10, nr.getMaximumNumber());
        
        nr = new LongRange(long8, long10);
        assertSame(long8, nr.getMinimumNumber());
        assertEquals(long10, nr.getMaximumNumber());
        
        
        try {
            new LongRange(long8, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LongRange(null, long8);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new LongRange(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.math.LongRangeTest::testContainsNumber
    public void testContainsNumber() {
        assertEquals(false, tenToTwenty.containsNumber(null));
        assertEquals(true, tenToTwenty.containsNumber(nonComparableNumber));
        
        assertEquals(false, tenToTwenty.containsNumber(five));
        assertEquals(true, tenToTwenty.containsNumber(ten));
        assertEquals(true, tenToTwenty.containsNumber(fifteen));
        assertEquals(true, tenToTwenty.containsNumber(twenty));
        assertEquals(false, tenToTwenty.containsNumber(twentyFive));
        
        assertEquals(false, tenToTwenty.containsNumber(long8));
        assertEquals(true, tenToTwenty.containsNumber(long10));
        assertEquals(true, tenToTwenty.containsNumber(long12));
        assertEquals(true, tenToTwenty.containsNumber(long20));
        assertEquals(false, tenToTwenty.containsNumber(long21));
        
        assertEquals(false, tenToTwenty.containsNumber(double8));
        assertEquals(true, tenToTwenty.containsNumber(double10));
        assertEquals(true, tenToTwenty.containsNumber(double12));
        assertEquals(true, tenToTwenty.containsNumber(double20));
        assertEquals(false, tenToTwenty.containsNumber(double21));
        
        assertEquals(false, tenToTwenty.containsNumber(float8));
        assertEquals(true, tenToTwenty.containsNumber(float10));
        assertEquals(true, tenToTwenty.containsNumber(float12));
        assertEquals(true, tenToTwenty.containsNumber(float20));
        assertEquals(false, tenToTwenty.containsNumber(float21));
    }

// org.apache.commons.lang.math.LongRangeTest::testContainsLongBig
    public void testContainsLongBig() {
        LongRange big = new LongRange(Long.MAX_VALUE, Long.MAX_VALUE- 2);
        assertEquals(true, big.containsLong(Long.MAX_VALUE - 1));
        assertEquals(false, big.containsLong(Long.MAX_VALUE - 3));
    }

// org.apache.commons.lang.math.NumberRangeTest::testConstructor1
    public void testConstructor1() {
        NumberRange nr = new NumberRange(five);
        assertSame(five, nr.getMinimumNumber());
        assertSame(five, nr.getMaximumNumber());
    }

// org.apache.commons.lang.math.NumberRangeTest::testConstructor1Exceptions
    public void testConstructor1Exceptions() {
        this.checkConstructorException(null);
        this.checkConstructorException(nonComparableNumber);
        this.checkConstructorException(new Float(Float.NaN));
        this.checkConstructorException(new Double(Double.NaN));
    }

// org.apache.commons.lang.math.NumberRangeTest::testConstructor2
    public void testConstructor2() {
        NumberRange nr = new NumberRange(five, ten);
        assertSame(five, nr.getMinimumNumber());
        assertSame(ten, nr.getMaximumNumber());

        nr = new NumberRange(ten, five);
        assertSame(five, nr.getMinimumNumber());
        assertSame(ten, nr.getMaximumNumber());
    }

// org.apache.commons.lang.math.NumberRangeTest::testConstructor2Exceptions
    public void testConstructor2Exceptions() {
        this.checkConstructorException(null, null);

        this.checkConstructorException(new Float(12.2f), new Double(12.2));
        this.checkConstructorException(new Float(Float.NaN), new Double(12.2));
        this.checkConstructorException(new Double(Double.NaN), new Double(12.2));
        this.checkConstructorException(new Double(12.2), new Double(Double.NaN));
        this.checkConstructorException(new Double(Double.NaN), new Double(Double.NaN));
        this.checkConstructorException(null, new Double(12.2));
        this.checkConstructorException(new Double(12.2), null);

        this.checkConstructorException(new Double(12.2f), new Float(12.2));
        this.checkConstructorException(new Double(Double.NaN), new Float(12.2));
        this.checkConstructorException(new Float(Float.NaN), new Float(12.2));
        this.checkConstructorException(new Float(12.2), new Float(Float.NaN));
        this.checkConstructorException(new Float(Float.NaN), new Float(Float.NaN));
        this.checkConstructorException(null, new Float(12.2));
        this.checkConstructorException(new Float(12.2), null);

        this.checkConstructorException(nonComparableNumber, nonComparableNumber);
        this.checkConstructorException(null, nonComparableNumber);
        this.checkConstructorException(nonComparableNumber, null);
        this.checkConstructorException(new Float(12.2), nonComparableNumber);
        this.checkConstructorException(nonComparableNumber, new Float(12.2));
    }

// org.apache.commons.lang.math.NumberRangeTest::testContainsLongBig
    public void testContainsLongBig() {
        
        NumberRange big = new NumberRange(new Long(Long.MAX_VALUE), new Long(Long.MAX_VALUE - 2));
        assertEquals(true, big.containsLong(Long.MAX_VALUE - 1));
        assertEquals(false, big.containsLong(Long.MAX_VALUE - 3));
    }

// org.apache.commons.lang.math.NumberRangeTest::testContainsNumber
    public void testContainsNumber() {
        assertEquals(false, tenToTwenty.containsNumber(null));
        assertEquals(false, tenToTwenty.containsNumber(five));
        assertEquals(true, tenToTwenty.containsNumber(ten));
        assertEquals(true, tenToTwenty.containsNumber(fifteen));
        assertEquals(true, tenToTwenty.containsNumber(twenty));
        assertEquals(false, tenToTwenty.containsNumber(twentyFive));

        try {
            tenToTwenty.containsNumber(long21);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

// org.apache.commons.lang.math.NumberUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new NumberUtils());
        Constructor[] cons = NumberUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(NumberUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(NumberUtils.class.getModifiers()));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testStringToIntString
    public void testStringToIntString() {
        assertTrue("stringToInt(String) 1 failed", NumberUtils.stringToInt("12345") == 12345);
        assertTrue("stringToInt(String) 2 failed", NumberUtils.stringToInt("abc") == 0);
        assertTrue("stringToInt(empty) failed", NumberUtils.stringToInt("") == 0);
        assertTrue("stringToInt(null) failed", NumberUtils.stringToInt(null) == 0);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testToIntString
    public void testToIntString() {
        assertTrue("toInt(String) 1 failed", NumberUtils.toInt("12345") == 12345);
        assertTrue("toInt(String) 2 failed", NumberUtils.toInt("abc") == 0);
        assertTrue("toInt(empty) failed", NumberUtils.toInt("") == 0);
        assertTrue("toInt(null) failed", NumberUtils.toInt(null) == 0);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testStringToIntStringI
    public void testStringToIntStringI() {
        assertTrue("stringToInt(String,int) 1 failed", NumberUtils.stringToInt("12345", 5) == 12345);
        assertTrue("stringToInt(String,int) 2 failed", NumberUtils.stringToInt("1234.5", 5) == 5);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testToIntStringI
    public void testToIntStringI() {
        assertTrue("toInt(String,int) 1 failed", NumberUtils.toInt("12345", 5) == 12345);
        assertTrue("toInt(String,int) 2 failed", NumberUtils.toInt("1234.5", 5) == 5);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testToLongString
    public void testToLongString() {
        assertTrue("toLong(String) 1 failed", NumberUtils.toLong("12345") == 12345l);
        assertTrue("toLong(String) 2 failed", NumberUtils.toLong("abc") == 0l);
        assertTrue("toLong(String) 3 failed", NumberUtils.toLong("1L") == 0l);
        assertTrue("toLong(String) 4 failed", NumberUtils.toLong("1l") == 0l);
        assertTrue("toLong(Long.MAX_VALUE) failed", NumberUtils.toLong(Long.MAX_VALUE+"") == Long.MAX_VALUE);
        assertTrue("toLong(Long.MIN_VALUE) failed", NumberUtils.toLong(Long.MIN_VALUE+"") == Long.MIN_VALUE);
        assertTrue("toLong(empty) failed", NumberUtils.toLong("") == 0l);
        assertTrue("toLong(null) failed", NumberUtils.toLong(null) == 0l);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testToLongStringL
    public void testToLongStringL() {
        assertTrue("toLong(String,long) 1 failed", NumberUtils.toLong("12345", 5l) == 12345l);
        assertTrue("toLong(String,long) 2 failed", NumberUtils.toLong("1234.5", 5l) == 5l);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testToFloatString
    public void testToFloatString() {
        assertTrue("toFloat(String) 1 failed", NumberUtils.toFloat("-1.2345") == -1.2345f);
        assertTrue("toFloat(String) 2 failed", NumberUtils.toFloat("1.2345") == 1.2345f);
        assertTrue("toFloat(String) 3 failed", NumberUtils.toFloat("abc") == 0.0f);
        assertTrue("toFloat(Float.MAX_VALUE) failed", NumberUtils.toFloat(Float.MAX_VALUE+"") ==  Float.MAX_VALUE);
        assertTrue("toFloat(Float.MIN_VALUE) failed", NumberUtils.toFloat(Float.MIN_VALUE+"") == Float.MIN_VALUE);
        assertTrue("toFloat(empty) failed", NumberUtils.toFloat("") == 0.0f);
        assertTrue("toFloat(null) failed", NumberUtils.toFloat(null) == 0.0f);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testToFloatStringF
    public void testToFloatStringF() {
        assertTrue("toFloat(String,int) 1 failed", NumberUtils.toFloat("1.2345", 5.1f) == 1.2345f);
        assertTrue("toFloat(String,int) 2 failed", NumberUtils.toFloat("a", 5.0f) == 5.0f);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testStringToDoubleString
    public void testStringToDoubleString() {
        assertTrue("toDouble(String) 1 failed", NumberUtils.toDouble("-1.2345") == -1.2345d);
        assertTrue("toDouble(String) 2 failed", NumberUtils.toDouble("1.2345") == 1.2345d);
        assertTrue("toDouble(String) 3 failed", NumberUtils.toDouble("abc") == 0.0d);
        assertTrue("toDouble(Double.MAX_VALUE) failed", NumberUtils.toDouble(Double.MAX_VALUE+"") == Double.MAX_VALUE);
        assertTrue("toDouble(Double.MIN_VALUE) failed", NumberUtils.toDouble(Double.MIN_VALUE+"") == Double.MIN_VALUE);
        assertTrue("toDouble(empty) failed", NumberUtils.toDouble("") == 0.0d);
        assertTrue("toDouble(null) failed", NumberUtils.toDouble(null) == 0.0d);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testStringToDoubleStringD
    public void testStringToDoubleStringD() {
        assertTrue("toDouble(String,int) 1 failed", NumberUtils.toDouble("1.2345", 5.1d) == 1.2345d);
        assertTrue("toDouble(String,int) 2 failed", NumberUtils.toDouble("a", 5.0d) == 5.0d);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateNumber
    public void testCreateNumber() {
        
        assertEquals("createNumber(String) 1 failed", new Float("1234.5"), NumberUtils.createNumber("1234.5"));
        assertEquals("createNumber(String) 2 failed", new Integer("12345"), NumberUtils.createNumber("12345"));
        assertEquals("createNumber(String) 3 failed", new Double("1234.5"), NumberUtils.createNumber("1234.5D"));
        assertEquals("createNumber(String) 3 failed", new Double("1234.5"), NumberUtils.createNumber("1234.5d"));
        assertEquals("createNumber(String) 4 failed", new Float("1234.5"), NumberUtils.createNumber("1234.5F"));
        assertEquals("createNumber(String) 4 failed", new Float("1234.5"), NumberUtils.createNumber("1234.5f"));
        assertEquals("createNumber(String) 5 failed", new Long(Integer.MAX_VALUE + 1L), NumberUtils.createNumber(""
            + (Integer.MAX_VALUE + 1L)));
        assertEquals("createNumber(String) 6 failed", new Long(12345), NumberUtils.createNumber("12345L"));
        assertEquals("createNumber(String) 6 failed", new Long(12345), NumberUtils.createNumber("12345l"));
        assertEquals("createNumber(String) 7 failed", new Float("-1234.5"), NumberUtils.createNumber("-1234.5"));
        assertEquals("createNumber(String) 8 failed", new Integer("-12345"), NumberUtils.createNumber("-12345"));
        assertTrue("createNumber(String) 9 failed", 0xFADE == NumberUtils.createNumber("0xFADE").intValue());
        assertTrue("createNumber(String) 10 failed", -0xFADE == NumberUtils.createNumber("-0xFADE").intValue());
        assertEquals("createNumber(String) 11 failed", new Double("1.1E200"), NumberUtils.createNumber("1.1E200"));
        assertEquals("createNumber(String) 12 failed", new Float("1.1E20"), NumberUtils.createNumber("1.1E20"));
        assertEquals("createNumber(String) 13 failed", new Double("-1.1E200"), NumberUtils.createNumber("-1.1E200"));
        assertEquals("createNumber(String) 14 failed", new Double("1.1E-200"), NumberUtils.createNumber("1.1E-200"));
        assertEquals("createNumber(null) failed", null, NumberUtils.createNumber(null));
        assertEquals("createNumber(String) failed", new BigInteger("12345678901234567890"), NumberUtils
                .createNumber("12345678901234567890L"));

        
        if (SystemUtils.isJavaVersionAtLeast(1.3f)) {
            assertEquals("createNumber(String) 15 failed", new BigDecimal("1.1E-700"), NumberUtils
                    .createNumber("1.1E-700F"));
        }
        assertEquals("createNumber(String) 16 failed", new Long("10" + Integer.MAX_VALUE), NumberUtils
                .createNumber("10" + Integer.MAX_VALUE + "L"));
        assertEquals("createNumber(String) 17 failed", new Long("10" + Integer.MAX_VALUE), NumberUtils
                .createNumber("10" + Integer.MAX_VALUE));
        assertEquals("createNumber(String) 18 failed", new BigInteger("10" + Long.MAX_VALUE), NumberUtils
                .createNumber("10" + Long.MAX_VALUE));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateFloat
    public void testCreateFloat() {
        assertEquals("createFloat(String) failed", new Float("1234.5"), NumberUtils.createFloat("1234.5"));
        assertEquals("createFloat(null) failed", null, NumberUtils.createFloat(null));
        this.testCreateFloatFailure("");
        this.testCreateFloatFailure(" ");
        this.testCreateFloatFailure("\b\t\n\f\r");
        
        this.testCreateFloatFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateFloatFailure
    protected void testCreateFloatFailure(String str) {
        try {
            Float value = NumberUtils.createFloat(str);
            fail("createFloat(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateDouble
    public void testCreateDouble() {
        assertEquals("createDouble(String) failed", new Double("1234.5"), NumberUtils.createDouble("1234.5"));
        assertEquals("createDouble(null) failed", null, NumberUtils.createDouble(null));
        this.testCreateDoubleFailure("");
        this.testCreateDoubleFailure(" ");
        this.testCreateDoubleFailure("\b\t\n\f\r");
        
        this.testCreateDoubleFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateDoubleFailure
    protected void testCreateDoubleFailure(String str) {
        try {
            Double value = NumberUtils.createDouble(str);
            fail("createDouble(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateInteger
    public void testCreateInteger() {
        assertEquals("createInteger(String) failed", new Integer("12345"), NumberUtils.createInteger("12345"));
        assertEquals("createInteger(null) failed", null, NumberUtils.createInteger(null));
        this.testCreateIntegerFailure("");
        this.testCreateIntegerFailure(" ");
        this.testCreateIntegerFailure("\b\t\n\f\r");
        
        this.testCreateIntegerFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateIntegerFailure
    protected void testCreateIntegerFailure(String str) {
        try {
            Integer value = NumberUtils.createInteger(str);
            fail("createInteger(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateLong
    public void testCreateLong() {
        assertEquals("createLong(String) failed", new Long("12345"), NumberUtils.createLong("12345"));
        assertEquals("createLong(null) failed", null, NumberUtils.createLong(null));
        this.testCreateLongFailure("");
        this.testCreateLongFailure(" ");
        this.testCreateLongFailure("\b\t\n\f\r");
        
        this.testCreateLongFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateLongFailure
    protected void testCreateLongFailure(String str) {
        try {
            Long value = NumberUtils.createLong(str);
            fail("createLong(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateBigInteger
    public void testCreateBigInteger() {
        assertEquals("createBigInteger(String) failed", new BigInteger("12345"), NumberUtils.createBigInteger("12345"));
        assertEquals("createBigInteger(null) failed", null, NumberUtils.createBigInteger(null));
        this.testCreateBigIntegerFailure("");
        this.testCreateBigIntegerFailure(" ");
        this.testCreateBigIntegerFailure("\b\t\n\f\r");
        
        this.testCreateBigIntegerFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateBigIntegerFailure
    protected void testCreateBigIntegerFailure(String str) {
        try {
            BigInteger value = NumberUtils.createBigInteger(str);
            fail("createBigInteger(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateBigDecimal
    public void testCreateBigDecimal() {
        assertEquals("createBigDecimal(String) failed", new BigDecimal("1234.5"), NumberUtils.createBigDecimal("1234.5"));
        assertEquals("createBigDecimal(null) failed", null, NumberUtils.createBigDecimal(null));
        this.testCreateBigDecimalFailure("");
        this.testCreateBigDecimalFailure(" ");
        this.testCreateBigDecimalFailure("\b\t\n\f\r");
        
        this.testCreateBigDecimalFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCreateBigDecimalFailure
    protected void testCreateBigDecimalFailure(String str) {
        try {
            BigDecimal value = NumberUtils.createBigDecimal(str);
            fail("createBigDecimal(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang.math.NumberUtilsTest::testEqualsByte
    public void testEqualsByte() {
        byte[] array1 = null;
        byte[] array2 = null;
        assertEquals( true, NumberUtils.equals(array1, array2) );
        assertEquals( true, NumberUtils.equals(array2, array1) );

        array1 = new byte[] { 50, 20 }; 
        assertEquals( false, NumberUtils.equals(array1, array2) );
        assertEquals( false, NumberUtils.equals(array2, array1) );

        
        array2 = array1;
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new byte[] { 50, 20 };
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new byte[] { 20, 50 };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new byte[] { 50, 20, 10 };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new byte[] { 50 };
        assertEquals( false, NumberUtils.equals(array1, array2) );
    }

// org.apache.commons.lang.math.NumberUtilsTest::testEqualsShort
    public void testEqualsShort() {
        short[] array1 = null;
        short[] array2 = null;
        assertEquals( true, NumberUtils.equals(array1, array2) );
        assertEquals( true, NumberUtils.equals(array2, array1) );

        array1 = new short[] { 50, 20 }; 
        assertEquals( false, NumberUtils.equals(array1, array2) );
        assertEquals( false, NumberUtils.equals(array2, array1) );

        
        array2 = array1;
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new short[] { 50, 20 };
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new short[] { 20, 50 };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new short[] { 50, 20, 10 };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new short[] { 50 };
        assertEquals( false, NumberUtils.equals(array1, array2) );
    }

// org.apache.commons.lang.math.NumberUtilsTest::testEqualsInt
    public void testEqualsInt() {
        int[] array1 = null;
        int[] array2 = null;
        assertEquals( true, NumberUtils.equals(array1, array2) );
        assertEquals( true, NumberUtils.equals(array2, array1) );

        array1 = new int[] { 50, 20 }; 
        assertEquals( false, NumberUtils.equals(array1, array2) );
        assertEquals( false, NumberUtils.equals(array2, array1) );

        
        array2 = array1;
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new int[] { 50, 20 };
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new int[] { 20, 50 };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new int[] { 50, 20, 10 };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new int[] { 50 };
        assertEquals( false, NumberUtils.equals(array1, array2) );
    }

// org.apache.commons.lang.math.NumberUtilsTest::testEqualsLong
    public void testEqualsLong() {
        long[] array1 = null;
        long[] array2 = null;
        assertEquals( true, NumberUtils.equals(array1, array2) );
        assertEquals( true, NumberUtils.equals(array2, array1) );

        array1 = new long[] { 50L, 20L }; 
        assertEquals( false, NumberUtils.equals(array1, array2) );
        assertEquals( false, NumberUtils.equals(array2, array1) );

        
        array2 = array1;
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new long[] { 50L, 20L };
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new long[] { 20L, 50L };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new long[] { 50L, 20L, 10L };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new long[] { 50L };
        assertEquals( false, NumberUtils.equals(array1, array2) );
    }

// org.apache.commons.lang.math.NumberUtilsTest::testEqualsFloat
    public void testEqualsFloat() {
        float[] array1 = null;
        float[] array2 = null;
        assertEquals( true, NumberUtils.equals(array1, array2) );
        assertEquals( true, NumberUtils.equals(array2, array1) );

        array1 = new float[] { 50.6f, 20.6f }; 
        assertEquals( false, NumberUtils.equals(array1, array2) );
        assertEquals( false, NumberUtils.equals(array2, array1) );

        
        array2 = array1;
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new float[] { 50.6f, 20.6f };
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new float[] { 20.6f, 50.6f };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new float[] { 50.6f, 20.6f, 10.6f };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new float[] { 50.6f };
        assertEquals( false, NumberUtils.equals(array1, array2) );
    }

// org.apache.commons.lang.math.NumberUtilsTest::testEqualsDouble
    public void testEqualsDouble() {
        double[] array1 = null;
        double[] array2 = null;
        assertEquals( true, NumberUtils.equals(array1, array2) );
        assertEquals( true, NumberUtils.equals(array2, array1) );

        array1 = new double[] { 50.6, 20.6 }; 
        assertEquals( false, NumberUtils.equals(array1, array2) );
        assertEquals( false, NumberUtils.equals(array2, array1) );

        
        array2 = array1;
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new double[] { 50.6, 20.6 };
        assertEquals( true, NumberUtils.equals(array1, array2) );

        
        array2 = new double[] { 20.6, 50.6 };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new double[] { 50.6, 20.6, 10.6 };
        assertEquals( false, NumberUtils.equals(array1, array2) );

        
        array2 = new double[] { 50.6 };
        assertEquals( false, NumberUtils.equals(array1, array2) );
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinLong
    public void testMinLong() {
        final long[] l = null;
        try {
            NumberUtils.min(l);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new long[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(long[]) failed for array length 1",
            5,
            NumberUtils.min(new long[] { 5 }));

        assertEquals(
            "min(long[]) failed for array length 2",
            6,
            NumberUtils.min(new long[] { 6, 9 }));

        assertEquals(-10, NumberUtils.min(new long[] { -10, -5, 0, 5, 10 }));
        assertEquals(-10, NumberUtils.min(new long[] { -5, 0, -10, 5, 10 }));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinInt
    public void testMinInt() {
        final int[] i = null;
        try {
            NumberUtils.min(i);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new int[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(int[]) failed for array length 1",
            5,
            NumberUtils.min(new int[] { 5 }));

        assertEquals(
            "min(int[]) failed for array length 2",
            6,
            NumberUtils.min(new int[] { 6, 9 }));

        assertEquals(-10, NumberUtils.min(new int[] { -10, -5, 0, 5, 10 }));
        assertEquals(-10, NumberUtils.min(new int[] { -5, 0, -10, 5, 10 }));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinShort
    public void testMinShort() {
        final short[] s = null;
        try {
            NumberUtils.min(s);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new short[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(short[]) failed for array length 1",
            5,
            NumberUtils.min(new short[] { 5 }));

        assertEquals(
            "min(short[]) failed for array length 2",
            6,
            NumberUtils.min(new short[] { 6, 9 }));

        assertEquals(-10, NumberUtils.min(new short[] { -10, -5, 0, 5, 10 }));
        assertEquals(-10, NumberUtils.min(new short[] { -5, 0, -10, 5, 10 }));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinByte
    public void testMinByte() {
        final byte[] b = null;
        try {
            NumberUtils.min(b);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new byte[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(byte[]) failed for array length 1",
            5,
            NumberUtils.min(new byte[] { 5 }));

        assertEquals(
            "min(byte[]) failed for array length 2",
            6,
            NumberUtils.min(new byte[] { 6, 9 }));

        assertEquals(-10, NumberUtils.min(new byte[] { -10, -5, 0, 5, 10 }));
        assertEquals(-10, NumberUtils.min(new byte[] { -5, 0, -10, 5, 10 }));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinDouble
    public void testMinDouble() {
        final double[] d = null;
        try {
            NumberUtils.min(d);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new double[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(double[]) failed for array length 1",
            5.12,
            NumberUtils.min(new double[] { 5.12 }),
            0);

        assertEquals(
            "min(double[]) failed for array length 2",
            6.23,
            NumberUtils.min(new double[] { 6.23, 9.34 }),
            0);

        assertEquals(
            "min(double[]) failed for array length 5",
            -10.45,
            NumberUtils.min(new double[] { -10.45, -5.56, 0, 5.67, 10.78 }),
            0);
        assertEquals(-10, NumberUtils.min(new double[] { -10, -5, 0, 5, 10 }), 0.0001);
        assertEquals(-10, NumberUtils.min(new double[] { -5, 0, -10, 5, 10 }), 0.0001);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinFloat
    public void testMinFloat() {
        final float[] f = null;
        try {
            NumberUtils.min(f);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.min(new float[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "min(float[]) failed for array length 1",
            5.9f,
            NumberUtils.min(new float[] { 5.9f }),
            0);

        assertEquals(
            "min(float[]) failed for array length 2",
            6.8f,
            NumberUtils.min(new float[] { 6.8f, 9.7f }),
            0);

        assertEquals(
            "min(float[]) failed for array length 5",
            -10.6f,
            NumberUtils.min(new float[] { -10.6f, -5.5f, 0, 5.4f, 10.3f }),
            0);
        assertEquals(-10, NumberUtils.min(new float[] { -10, -5, 0, 5, 10 }), 0.0001f);
        assertEquals(-10, NumberUtils.min(new float[] { -5, 0, -10, 5, 10 }), 0.0001f);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaxLong
    public void testMaxLong() {
        final long[] l = null;
        try {
            NumberUtils.max(l);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new long[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(long[]) failed for array length 1",
            5,
            NumberUtils.max(new long[] { 5 }));

        assertEquals(
            "max(long[]) failed for array length 2",
            9,
            NumberUtils.max(new long[] { 6, 9 }));

        assertEquals(
            "max(long[]) failed for array length 5",
            10,
            NumberUtils.max(new long[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new long[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new long[] { -5, 0, 10, 5, -10 }));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaxInt
    public void testMaxInt() {
        final int[] i = null;
        try {
            NumberUtils.max(i);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new int[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(int[]) failed for array length 1",
            5,
            NumberUtils.max(new int[] { 5 }));

        assertEquals(
            "max(int[]) failed for array length 2",
            9,
            NumberUtils.max(new int[] { 6, 9 }));

        assertEquals(
            "max(int[]) failed for array length 5",
            10,
            NumberUtils.max(new int[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new int[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new int[] { -5, 0, 10, 5, -10 }));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaxShort
    public void testMaxShort() {
        final short[] s = null;
        try {
            NumberUtils.max(s);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new short[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(short[]) failed for array length 1",
            5,
            NumberUtils.max(new short[] { 5 }));

        assertEquals(
            "max(short[]) failed for array length 2",
            9,
            NumberUtils.max(new short[] { 6, 9 }));

        assertEquals(
            "max(short[]) failed for array length 5",
            10,
            NumberUtils.max(new short[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new short[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new short[] { -5, 0, 10, 5, -10 }));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaxByte
    public void testMaxByte() {
        final byte[] b = null;
        try {
            NumberUtils.max(b);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new byte[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(byte[]) failed for array length 1",
            5,
            NumberUtils.max(new byte[] { 5 }));

        assertEquals(
            "max(byte[]) failed for array length 2",
            9,
            NumberUtils.max(new byte[] { 6, 9 }));

        assertEquals(
            "max(byte[]) failed for array length 5",
            10,
            NumberUtils.max(new byte[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new byte[] { -10, -5, 0, 5, 10 }));
        assertEquals(10, NumberUtils.max(new byte[] { -5, 0, 10, 5, -10 }));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaxDouble
    public void testMaxDouble() {
        final double[] d = null;
        try {
            NumberUtils.max(d);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new double[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(double[]) failed for array length 1",
            5.1f,
            NumberUtils.max(new double[] { 5.1f }),
            0);

        assertEquals(
            "max(double[]) failed for array length 2",
            9.2f,
            NumberUtils.max(new double[] { 6.3f, 9.2f }),
            0);

        assertEquals(
            "max(double[]) failed for float length 5",
            10.4f,
            NumberUtils.max(new double[] { -10.5f, -5.6f, 0, 5.7f, 10.4f }),
            0);
        assertEquals(10, NumberUtils.max(new double[] { -10, -5, 0, 5, 10 }), 0.0001);
        assertEquals(10, NumberUtils.max(new double[] { -5, 0, 10, 5, -10 }), 0.0001);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaxFloat
    public void testMaxFloat() {
        final float[] f = null;
        try {
            NumberUtils.max(f);
            fail("No exception was thrown for null input.");
        } catch (IllegalArgumentException ex) {}

        try {
            NumberUtils.max(new float[0]);
            fail("No exception was thrown for empty input.");
        } catch (IllegalArgumentException ex) {}

        assertEquals(
            "max(float[]) failed for array length 1",
            5.1f,
            NumberUtils.max(new float[] { 5.1f }),
            0);

        assertEquals(
            "max(float[]) failed for array length 2",
            9.2f,
            NumberUtils.max(new float[] { 6.3f, 9.2f }),
            0);

        assertEquals(
            "max(float[]) failed for float length 5",
            10.4f,
            NumberUtils.max(new float[] { -10.5f, -5.6f, 0, 5.7f, 10.4f }),
            0);
        assertEquals(10, NumberUtils.max(new float[] { -10, -5, 0, 5, 10 }), 0.0001f);
        assertEquals(10, NumberUtils.max(new float[] { -5, 0, 10, 5, -10 }), 0.0001f);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinimumLong
    public void testMinimumLong() {
        assertEquals("minimum(long,long,long) 1 failed", 12345L, NumberUtils.min(12345L, 12345L + 1L, 12345L + 2L));
        assertEquals("minimum(long,long,long) 2 failed", 12345L, NumberUtils.min(12345L + 1L, 12345L, 12345 + 2L));
        assertEquals("minimum(long,long,long) 3 failed", 12345L, NumberUtils.min(12345L + 1L, 12345L + 2L, 12345L));
        assertEquals("minimum(long,long,long) 4 failed", 12345L, NumberUtils.min(12345L + 1L, 12345L, 12345L));
        assertEquals("minimum(long,long,long) 5 failed", 12345L, NumberUtils.min(12345L, 12345L, 12345L));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinimumInt
    public void testMinimumInt() {
        assertEquals("minimum(int,int,int) 1 failed", 12345, NumberUtils.min(12345, 12345 + 1, 12345 + 2));
        assertEquals("minimum(int,int,int) 2 failed", 12345, NumberUtils.min(12345 + 1, 12345, 12345 + 2));
        assertEquals("minimum(int,int,int) 3 failed", 12345, NumberUtils.min(12345 + 1, 12345 + 2, 12345));
        assertEquals("minimum(int,int,int) 4 failed", 12345, NumberUtils.min(12345 + 1, 12345, 12345));
        assertEquals("minimum(int,int,int) 5 failed", 12345, NumberUtils.min(12345, 12345, 12345));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinimumShort
    public void testMinimumShort() {
        short low = 1234;
        short mid = 1234 + 1;
        short high = 1234 + 2;
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(low, mid, high));
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(mid, low, high));
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(mid, high, low));
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(low, mid, low));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinimumByte
    public void testMinimumByte() {
        byte low = 123;
        byte mid = 123 + 1;
        byte high = 123 + 2;
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(low, mid, high));
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(mid, low, high));
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(mid, high, low));
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(low, mid, low));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinimumDouble
    public void testMinimumDouble() {
        double low = 12.3;
        double mid = 12.3 + 1;
        double high = 12.3 + 2;
        assertEquals(low, NumberUtils.min(low, mid, high), 0.0001);
        assertEquals(low, NumberUtils.min(mid, low, high), 0.0001);
        assertEquals(low, NumberUtils.min(mid, high, low), 0.0001);
        assertEquals(low, NumberUtils.min(low, mid, low), 0.0001);
        assertEquals(mid, NumberUtils.min(high, mid, high), 0.0001);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMinimumFloat
    public void testMinimumFloat() {
        float low = 12.3f;
        float mid = 12.3f + 1;
        float high = 12.3f + 2;
        assertEquals(low, NumberUtils.min(low, mid, high), 0.0001f);
        assertEquals(low, NumberUtils.min(mid, low, high), 0.0001f);
        assertEquals(low, NumberUtils.min(mid, high, low), 0.0001f);
        assertEquals(low, NumberUtils.min(low, mid, low), 0.0001f);
        assertEquals(mid, NumberUtils.min(high, mid, high), 0.0001f);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaximumLong
    public void testMaximumLong() {
        assertEquals("maximum(long,long,long) 1 failed", 12345L, NumberUtils.max(12345L, 12345L - 1L, 12345L - 2L));
        assertEquals("maximum(long,long,long) 2 failed", 12345L, NumberUtils.max(12345L - 1L, 12345L, 12345L - 2L));
        assertEquals("maximum(long,long,long) 3 failed", 12345L, NumberUtils.max(12345L - 1L, 12345L - 2L, 12345L));
        assertEquals("maximum(long,long,long) 4 failed", 12345L, NumberUtils.max(12345L - 1L, 12345L, 12345L));
        assertEquals("maximum(long,long,long) 5 failed", 12345L, NumberUtils.max(12345L, 12345L, 12345L));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaximumInt
    public void testMaximumInt() {
        assertEquals("maximum(int,int,int) 1 failed", 12345, NumberUtils.max(12345, 12345 - 1, 12345 - 2));
        assertEquals("maximum(int,int,int) 2 failed", 12345, NumberUtils.max(12345 - 1, 12345, 12345 - 2));
        assertEquals("maximum(int,int,int) 3 failed", 12345, NumberUtils.max(12345 - 1, 12345 - 2, 12345));
        assertEquals("maximum(int,int,int) 4 failed", 12345, NumberUtils.max(12345 - 1, 12345, 12345));
        assertEquals("maximum(int,int,int) 5 failed", 12345, NumberUtils.max(12345, 12345, 12345));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaximumShort
    public void testMaximumShort() {
        short low = 1234;
        short mid = 1234 + 1;
        short high = 1234 + 2;
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(low, mid, high));
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(mid, low, high));
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(mid, high, low));
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(high, mid, high));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaximumByte
    public void testMaximumByte() {
        byte low = 123;
        byte mid = 123 + 1;
        byte high = 123 + 2;
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(low, mid, high));
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(mid, low, high));
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(mid, high, low));
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(high, mid, high));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaximumDouble
    public void testMaximumDouble() {
        double low = 12.3;
        double mid = 12.3 + 1;
        double high = 12.3 + 2;
        assertEquals(high, NumberUtils.max(low, mid, high), 0.0001);
        assertEquals(high, NumberUtils.max(mid, low, high), 0.0001);
        assertEquals(high, NumberUtils.max(mid, high, low), 0.0001);
        assertEquals(mid, NumberUtils.max(low, mid, low), 0.0001);
        assertEquals(high, NumberUtils.max(high, mid, high), 0.0001);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testMaximumFloat
    public void testMaximumFloat() {
        float low = 12.3f;
        float mid = 12.3f + 1;
        float high = 12.3f + 2;
        assertEquals(high, NumberUtils.max(low, mid, high), 0.0001f);
        assertEquals(high, NumberUtils.max(mid, low, high), 0.0001f);
        assertEquals(high, NumberUtils.max(mid, high, low), 0.0001f);
        assertEquals(mid, NumberUtils.max(low, mid, low), 0.0001f);
        assertEquals(high, NumberUtils.max(high, mid, high), 0.0001f);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCompareDouble
    public void testCompareDouble() {
        assertTrue(NumberUtils.compare(Double.NaN, Double.NaN) == 0);
        assertTrue(NumberUtils.compare(Double.NaN, Double.POSITIVE_INFINITY) == +1);
        assertTrue(NumberUtils.compare(Double.NaN, Double.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(Double.NaN, 1.2d) == +1);
        assertTrue(NumberUtils.compare(Double.NaN, 0.0d) == +1);
        assertTrue(NumberUtils.compare(Double.NaN, -0.0d) == +1);
        assertTrue(NumberUtils.compare(Double.NaN, -1.2d) == +1);
        assertTrue(NumberUtils.compare(Double.NaN, -Double.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(Double.NaN, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(Double.POSITIVE_INFINITY, Double.NaN) == -1);
        assertTrue(NumberUtils.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY) == 0);
        assertTrue(NumberUtils.compare(Double.POSITIVE_INFINITY, Double.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(Double.POSITIVE_INFINITY, 1.2d) == +1);
        assertTrue(NumberUtils.compare(Double.POSITIVE_INFINITY, 0.0d) == +1);
        assertTrue(NumberUtils.compare(Double.POSITIVE_INFINITY, -0.0d) == +1);
        assertTrue(NumberUtils.compare(Double.POSITIVE_INFINITY, -1.2d) == +1);
        assertTrue(NumberUtils.compare(Double.POSITIVE_INFINITY, -Double.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(Double.MAX_VALUE, Double.NaN) == -1);
        assertTrue(NumberUtils.compare(Double.MAX_VALUE, Double.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(Double.MAX_VALUE, Double.MAX_VALUE) == 0);
        assertTrue(NumberUtils.compare(Double.MAX_VALUE, 1.2d) == +1);
        assertTrue(NumberUtils.compare(Double.MAX_VALUE, 0.0d) == +1);
        assertTrue(NumberUtils.compare(Double.MAX_VALUE, -0.0d) == +1);
        assertTrue(NumberUtils.compare(Double.MAX_VALUE, -1.2d) == +1);
        assertTrue(NumberUtils.compare(Double.MAX_VALUE, -Double.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(Double.MAX_VALUE, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(1.2d, Double.NaN) == -1);
        assertTrue(NumberUtils.compare(1.2d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(1.2d, Double.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(1.2d, 1.2d) == 0);
        assertTrue(NumberUtils.compare(1.2d, 0.0d) == +1);
        assertTrue(NumberUtils.compare(1.2d, -0.0d) == +1);
        assertTrue(NumberUtils.compare(1.2d, -1.2d) == +1);
        assertTrue(NumberUtils.compare(1.2d, -Double.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(1.2d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(0.0d, Double.NaN) == -1);
        assertTrue(NumberUtils.compare(0.0d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(0.0d, Double.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(0.0d, 1.2d) == -1);
        assertTrue(NumberUtils.compare(0.0d, 0.0d) == 0);
        assertTrue(NumberUtils.compare(0.0d, -0.0d) == +1);
        assertTrue(NumberUtils.compare(0.0d, -1.2d) == +1);
        assertTrue(NumberUtils.compare(0.0d, -Double.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(0.0d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(-0.0d, Double.NaN) == -1);
        assertTrue(NumberUtils.compare(-0.0d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(-0.0d, Double.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(-0.0d, 1.2d) == -1);
        assertTrue(NumberUtils.compare(-0.0d, 0.0d) == -1);
        assertTrue(NumberUtils.compare(-0.0d, -0.0d) == 0);
        assertTrue(NumberUtils.compare(-0.0d, -1.2d) == +1);
        assertTrue(NumberUtils.compare(-0.0d, -Double.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(-0.0d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(-1.2d, Double.NaN) == -1);
        assertTrue(NumberUtils.compare(-1.2d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(-1.2d, Double.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(-1.2d, 1.2d) == -1);
        assertTrue(NumberUtils.compare(-1.2d, 0.0d) == -1);
        assertTrue(NumberUtils.compare(-1.2d, -0.0d) == -1);
        assertTrue(NumberUtils.compare(-1.2d, -1.2d) == 0);
        assertTrue(NumberUtils.compare(-1.2d, -Double.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(-1.2d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(-Double.MAX_VALUE, Double.NaN) == -1);
        assertTrue(NumberUtils.compare(-Double.MAX_VALUE, Double.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(-Double.MAX_VALUE, Double.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(-Double.MAX_VALUE, 1.2d) == -1);
        assertTrue(NumberUtils.compare(-Double.MAX_VALUE, 0.0d) == -1);
        assertTrue(NumberUtils.compare(-Double.MAX_VALUE, -0.0d) == -1);
        assertTrue(NumberUtils.compare(-Double.MAX_VALUE, -1.2d) == -1);
        assertTrue(NumberUtils.compare(-Double.MAX_VALUE, -Double.MAX_VALUE) == 0);
        assertTrue(NumberUtils.compare(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(Double.NEGATIVE_INFINITY, Double.NaN) == -1);
        assertTrue(NumberUtils.compare(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(Double.NEGATIVE_INFINITY, Double.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(Double.NEGATIVE_INFINITY, 1.2d) == -1);
        assertTrue(NumberUtils.compare(Double.NEGATIVE_INFINITY, 0.0d) == -1);
        assertTrue(NumberUtils.compare(Double.NEGATIVE_INFINITY, -0.0d) == -1);
        assertTrue(NumberUtils.compare(Double.NEGATIVE_INFINITY, -1.2d) == -1);
        assertTrue(NumberUtils.compare(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY) == 0);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testCompareFloat
    public void testCompareFloat() {
        assertTrue(NumberUtils.compare(Float.NaN, Float.NaN) == 0);
        assertTrue(NumberUtils.compare(Float.NaN, Float.POSITIVE_INFINITY) == +1);
        assertTrue(NumberUtils.compare(Float.NaN, Float.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(Float.NaN, 1.2f) == +1);
        assertTrue(NumberUtils.compare(Float.NaN, 0.0f) == +1);
        assertTrue(NumberUtils.compare(Float.NaN, -0.0f) == +1);
        assertTrue(NumberUtils.compare(Float.NaN, -1.2f) == +1);
        assertTrue(NumberUtils.compare(Float.NaN, -Float.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(Float.NaN, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(Float.POSITIVE_INFINITY, Float.NaN) == -1);
        assertTrue(NumberUtils.compare(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY) == 0);
        assertTrue(NumberUtils.compare(Float.POSITIVE_INFINITY, Float.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(Float.POSITIVE_INFINITY, 1.2f) == +1);
        assertTrue(NumberUtils.compare(Float.POSITIVE_INFINITY, 0.0f) == +1);
        assertTrue(NumberUtils.compare(Float.POSITIVE_INFINITY, -0.0f) == +1);
        assertTrue(NumberUtils.compare(Float.POSITIVE_INFINITY, -1.2f) == +1);
        assertTrue(NumberUtils.compare(Float.POSITIVE_INFINITY, -Float.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(Float.MAX_VALUE, Float.NaN) == -1);
        assertTrue(NumberUtils.compare(Float.MAX_VALUE, Float.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(Float.MAX_VALUE, Float.MAX_VALUE) == 0);
        assertTrue(NumberUtils.compare(Float.MAX_VALUE, 1.2f) == +1);
        assertTrue(NumberUtils.compare(Float.MAX_VALUE, 0.0f) == +1);
        assertTrue(NumberUtils.compare(Float.MAX_VALUE, -0.0f) == +1);
        assertTrue(NumberUtils.compare(Float.MAX_VALUE, -1.2f) == +1);
        assertTrue(NumberUtils.compare(Float.MAX_VALUE, -Float.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(Float.MAX_VALUE, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(1.2f, Float.NaN) == -1);
        assertTrue(NumberUtils.compare(1.2f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(1.2f, Float.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(1.2f, 1.2f) == 0);
        assertTrue(NumberUtils.compare(1.2f, 0.0f) == +1);
        assertTrue(NumberUtils.compare(1.2f, -0.0f) == +1);
        assertTrue(NumberUtils.compare(1.2f, -1.2f) == +1);
        assertTrue(NumberUtils.compare(1.2f, -Float.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(1.2f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(0.0f, Float.NaN) == -1);
        assertTrue(NumberUtils.compare(0.0f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(0.0f, Float.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(0.0f, 1.2f) == -1);
        assertTrue(NumberUtils.compare(0.0f, 0.0f) == 0);
        assertTrue(NumberUtils.compare(0.0f, -0.0f) == +1);
        assertTrue(NumberUtils.compare(0.0f, -1.2f) == +1);
        assertTrue(NumberUtils.compare(0.0f, -Float.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(0.0f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(-0.0f, Float.NaN) == -1);
        assertTrue(NumberUtils.compare(-0.0f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(-0.0f, Float.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(-0.0f, 1.2f) == -1);
        assertTrue(NumberUtils.compare(-0.0f, 0.0f) == -1);
        assertTrue(NumberUtils.compare(-0.0f, -0.0f) == 0);
        assertTrue(NumberUtils.compare(-0.0f, -1.2f) == +1);
        assertTrue(NumberUtils.compare(-0.0f, -Float.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(-0.0f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(-1.2f, Float.NaN) == -1);
        assertTrue(NumberUtils.compare(-1.2f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(-1.2f, Float.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(-1.2f, 1.2f) == -1);
        assertTrue(NumberUtils.compare(-1.2f, 0.0f) == -1);
        assertTrue(NumberUtils.compare(-1.2f, -0.0f) == -1);
        assertTrue(NumberUtils.compare(-1.2f, -1.2f) == 0);
        assertTrue(NumberUtils.compare(-1.2f, -Float.MAX_VALUE) == +1);
        assertTrue(NumberUtils.compare(-1.2f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(-Float.MAX_VALUE, Float.NaN) == -1);
        assertTrue(NumberUtils.compare(-Float.MAX_VALUE, Float.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(-Float.MAX_VALUE, Float.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(-Float.MAX_VALUE, 1.2f) == -1);
        assertTrue(NumberUtils.compare(-Float.MAX_VALUE, 0.0f) == -1);
        assertTrue(NumberUtils.compare(-Float.MAX_VALUE, -0.0f) == -1);
        assertTrue(NumberUtils.compare(-Float.MAX_VALUE, -1.2f) == -1);
        assertTrue(NumberUtils.compare(-Float.MAX_VALUE, -Float.MAX_VALUE) == 0);
        assertTrue(NumberUtils.compare(-Float.MAX_VALUE, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(NumberUtils.compare(Float.NEGATIVE_INFINITY, Float.NaN) == -1);
        assertTrue(NumberUtils.compare(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY) == -1);
        assertTrue(NumberUtils.compare(Float.NEGATIVE_INFINITY, Float.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(Float.NEGATIVE_INFINITY, 1.2f) == -1);
        assertTrue(NumberUtils.compare(Float.NEGATIVE_INFINITY, 0.0f) == -1);
        assertTrue(NumberUtils.compare(Float.NEGATIVE_INFINITY, -0.0f) == -1);
        assertTrue(NumberUtils.compare(Float.NEGATIVE_INFINITY, -1.2f) == -1);
        assertTrue(NumberUtils.compare(Float.NEGATIVE_INFINITY, -Float.MAX_VALUE) == -1);
        assertTrue(NumberUtils.compare(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY) == 0);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testIsDigits
    public void testIsDigits() {
        assertEquals("isDigits(null) failed", false, NumberUtils.isDigits(null));
        assertEquals("isDigits('') failed", false, NumberUtils.isDigits(""));
        assertEquals("isDigits(String) failed", true, NumberUtils.isDigits("12345"));
        assertEquals("isDigits(String) neg 1 failed", false, NumberUtils.isDigits("1234.5"));
        assertEquals("isDigits(String) neg 3 failed", false, NumberUtils.isDigits("1ab"));
        assertEquals("isDigits(String) neg 4 failed", false, NumberUtils.isDigits("abc"));
    }

// org.apache.commons.lang.math.NumberUtilsTest::testIsNumber
    public void testIsNumber() {
        String val = "12345";
        assertTrue("isNumber(String) 1 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 1 failed", checkCreateNumber(val));
        val = "1234.5";
        assertTrue("isNumber(String) 2 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 2 failed", checkCreateNumber(val));
        val = ".12345";
        assertTrue("isNumber(String) 3 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 3 failed", checkCreateNumber(val));
        val = "1234E5";
        assertTrue("isNumber(String) 4 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 4 failed", checkCreateNumber(val));
        val = "1234E+5";
        assertTrue("isNumber(String) 5 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 5 failed", checkCreateNumber(val));
        val = "1234E-5";
        assertTrue("isNumber(String) 6 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 6 failed", checkCreateNumber(val));
        val = "123.4E5";
        assertTrue("isNumber(String) 7 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 7 failed", checkCreateNumber(val));
        val = "-1234";
        assertTrue("isNumber(String) 8 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 8 failed", checkCreateNumber(val));
        val = "-1234.5";
        assertTrue("isNumber(String) 9 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 9 failed", checkCreateNumber(val));
        val = "-.12345";
        assertTrue("isNumber(String) 10 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 10 failed", checkCreateNumber(val));
        val = "-1234E5";
        assertTrue("isNumber(String) 11 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 11 failed", checkCreateNumber(val));
        val = "0";
        assertTrue("isNumber(String) 12 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 12 failed", checkCreateNumber(val));
        val = "-0";
        assertTrue("isNumber(String) 13 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 13 failed", checkCreateNumber(val));
        val = "01234";
        assertTrue("isNumber(String) 14 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 14 failed", checkCreateNumber(val));
        val = "-01234";
        assertTrue("isNumber(String) 15 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 15 failed", checkCreateNumber(val));
        val = "0xABC123";
        assertTrue("isNumber(String) 16 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 16 failed", checkCreateNumber(val));
        val = "0x0";
        assertTrue("isNumber(String) 17 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 17 failed", checkCreateNumber(val));
        val = "123.4E21D";
        assertTrue("isNumber(String) 19 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 19 failed", checkCreateNumber(val));
        val = "-221.23F";
        assertTrue("isNumber(String) 20 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 20 failed", checkCreateNumber(val));
        val = "22338L";
        assertTrue("isNumber(String) 21 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 21 failed", checkCreateNumber(val));
        val = null;
        assertTrue("isNumber(String) 1 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 1 Neg failed", !checkCreateNumber(val));
        val = "";
        assertTrue("isNumber(String) 2 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 2 Neg failed", !checkCreateNumber(val));
        val = "--2.3";
        assertTrue("isNumber(String) 3 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 3 Neg failed", !checkCreateNumber(val));
        val = ".12.3";
        assertTrue("isNumber(String) 4 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 4 Neg failed", !checkCreateNumber(val));
        val = "-123E";
        assertTrue("isNumber(String) 5 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 5 Neg failed", !checkCreateNumber(val));
        val = "-123E+-212";
        assertTrue("isNumber(String) 6 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 6 Neg failed", !checkCreateNumber(val));
        val = "-123E2.12";
        assertTrue("isNumber(String) 7 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 7 Neg failed", !checkCreateNumber(val));
        val = "0xGF";
        assertTrue("isNumber(String) 8 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 8 Neg failed", !checkCreateNumber(val));
        val = "0xFAE-1";
        assertTrue("isNumber(String) 9 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 9 Neg failed", !checkCreateNumber(val));
        val = ".";
        assertTrue("isNumber(String) 10 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 10 Neg failed", !checkCreateNumber(val));
        val = "-0ABC123";
        assertTrue("isNumber(String) 11 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 11 Neg failed", !checkCreateNumber(val));
        val = "123.4E-D";
        assertTrue("isNumber(String) 12 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 12 Neg failed", !checkCreateNumber(val));
        val = "123.4ED";
        assertTrue("isNumber(String) 13 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 13 Neg failed", !checkCreateNumber(val));
        val = "1234E5l";
        assertTrue("isNumber(String) 14 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 14 Neg failed", !checkCreateNumber(val));
        val = "11a";
        assertTrue("isNumber(String) 15 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 15 Neg failed", !checkCreateNumber(val)); 
        val = "1a";
        assertTrue("isNumber(String) 16 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 16 Neg failed", !checkCreateNumber(val)); 
        val = "a";
        assertTrue("isNumber(String) 17 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 17 Neg failed", !checkCreateNumber(val)); 
        val = "11g";
        assertTrue("isNumber(String) 18 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 18 Neg failed", !checkCreateNumber(val)); 
        val = "11z";
        assertTrue("isNumber(String) 19 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 19 Neg failed", !checkCreateNumber(val)); 
        val = "11def";
        assertTrue("isNumber(String) 20 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 20 Neg failed", !checkCreateNumber(val)); 
        val = "11d11";
        assertTrue("isNumber(String) 21 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 21 Neg failed", !checkCreateNumber(val)); 
        val = "11 11";
        assertTrue("isNumber(String) 22 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 22 Neg failed", !checkCreateNumber(val));
        val = " 1111";
        assertTrue("isNumber(String) 23 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 23 Neg failed", !checkCreateNumber(val));
        val = "1111 ";
        assertTrue("isNumber(String) 24 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 24 Neg failed", !checkCreateNumber(val));

    }

// org.apache.commons.lang.math.NumberUtilsTest::testConstants
    public void testConstants() {
        assertTrue(NumberUtils.LONG_ZERO instanceof Long);
        assertTrue(NumberUtils.LONG_ONE instanceof Long);
        assertTrue(NumberUtils.LONG_MINUS_ONE instanceof Long);
        assertTrue(NumberUtils.INTEGER_ZERO instanceof Integer);
        assertTrue(NumberUtils.INTEGER_ONE instanceof Integer);
        assertTrue(NumberUtils.INTEGER_MINUS_ONE instanceof Integer);
        assertTrue(NumberUtils.SHORT_ZERO instanceof Short);
        assertTrue(NumberUtils.SHORT_ONE instanceof Short);
        assertTrue(NumberUtils.SHORT_MINUS_ONE instanceof Short);
        assertTrue(NumberUtils.BYTE_ZERO instanceof Byte);
        assertTrue(NumberUtils.BYTE_ONE instanceof Byte);
        assertTrue(NumberUtils.BYTE_MINUS_ONE instanceof Byte);
        assertTrue(NumberUtils.DOUBLE_ZERO instanceof Double);
        assertTrue(NumberUtils.DOUBLE_ONE instanceof Double);
        assertTrue(NumberUtils.DOUBLE_MINUS_ONE instanceof Double);
        assertTrue(NumberUtils.FLOAT_ZERO instanceof Float);
        assertTrue(NumberUtils.FLOAT_ONE instanceof Float);
        assertTrue(NumberUtils.FLOAT_MINUS_ONE instanceof Float);
        
        assertTrue(NumberUtils.LONG_ZERO.longValue() == 0);
        assertTrue(NumberUtils.LONG_ONE.longValue() == 1);
        assertTrue(NumberUtils.LONG_MINUS_ONE.longValue() == -1);
        assertTrue(NumberUtils.INTEGER_ZERO.intValue() == 0);
        assertTrue(NumberUtils.INTEGER_ONE.intValue() == 1);
        assertTrue(NumberUtils.INTEGER_MINUS_ONE.intValue() == -1);
        assertTrue(NumberUtils.SHORT_ZERO.shortValue() == 0);
        assertTrue(NumberUtils.SHORT_ONE.shortValue() == 1);
        assertTrue(NumberUtils.SHORT_MINUS_ONE.shortValue() == -1);
        assertTrue(NumberUtils.BYTE_ZERO.byteValue() == 0);
        assertTrue(NumberUtils.BYTE_ONE.byteValue() == 1);
        assertTrue(NumberUtils.BYTE_MINUS_ONE.byteValue() == -1);
        assertTrue(NumberUtils.DOUBLE_ZERO.doubleValue() == 0.0d);
        assertTrue(NumberUtils.DOUBLE_ONE.doubleValue() == 1.0d);
        assertTrue(NumberUtils.DOUBLE_MINUS_ONE.doubleValue() == -1.0d);
        assertTrue(NumberUtils.FLOAT_ZERO.floatValue() == 0.0f);
        assertTrue(NumberUtils.FLOAT_ONE.floatValue() == 1.0f);
        assertTrue(NumberUtils.FLOAT_MINUS_ONE.floatValue() == -1.0f);
    }

// org.apache.commons.lang.math.NumberUtilsTest::testLang300
    public void testLang300() {
        NumberUtils.createNumber("-1l");
        NumberUtils.createNumber("01l");
        NumberUtils.createNumber("1l");
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testConstructors
    public void testConstructors() {
        assertEquals(0d, new MutableDouble().doubleValue(), 0.0001d);
        
        assertEquals(1d, new MutableDouble(1d).doubleValue(), 0.0001d);
        
        assertEquals(2d, new MutableDouble(new Double(2d)).doubleValue(), 0.0001d);
        assertEquals(3d, new MutableDouble(new MutableDouble(3d)).doubleValue(), 0.0001d);
        try {
            new MutableDouble(null);
            fail();
        } catch (NullPointerException ex) {}
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testGetSet
    public void testGetSet() {
        final MutableDouble mutNum = new MutableDouble(0d);
        assertEquals(0d, new MutableDouble().doubleValue(), 0.0001d);
        assertEquals(new Double(0), new MutableDouble().getValue());
        
        mutNum.setValue(1);
        assertEquals(1d, mutNum.doubleValue(), 0.0001d);
        assertEquals(new Double(1d), mutNum.getValue());
        
        mutNum.setValue(new Double(2d));
        assertEquals(2d, mutNum.doubleValue(), 0.0001d);
        assertEquals(new Double(2d), mutNum.getValue());
        
        mutNum.setValue(new MutableDouble(3d));
        assertEquals(3d, mutNum.doubleValue(), 0.0001d);
        assertEquals(new Double(3d), mutNum.getValue());
        try {
            mutNum.setValue(null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            mutNum.setValue("0");
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testNanInfinite
    public void testNanInfinite() {
        MutableDouble mutNum = new MutableDouble(Double.NaN);
        assertEquals(true, mutNum.isNaN());
        
        mutNum = new MutableDouble(Double.POSITIVE_INFINITY);
        assertEquals(true, mutNum.isInfinite());
        
        mutNum = new MutableDouble(Double.NEGATIVE_INFINITY);
        assertEquals(true, mutNum.isInfinite());
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testEquals
    public void testEquals() {
        final MutableDouble mutNumA = new MutableDouble(0d);
        final MutableDouble mutNumB = new MutableDouble(0d);
        final MutableDouble mutNumC = new MutableDouble(1d);

        assertEquals(true, mutNumA.equals(mutNumA));
        assertEquals(true, mutNumA.equals(mutNumB));
        assertEquals(true, mutNumB.equals(mutNumA));
        assertEquals(true, mutNumB.equals(mutNumB));
        assertEquals(false, mutNumA.equals(mutNumC));
        assertEquals(false, mutNumB.equals(mutNumC));
        assertEquals(true, mutNumC.equals(mutNumC));
        assertEquals(false, mutNumA.equals(null));
        assertEquals(false, mutNumA.equals(new Double(0d)));
        assertEquals(false, mutNumA.equals("0"));
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testHashCode
    public void testHashCode() {
        final MutableDouble mutNumA = new MutableDouble(0d);
        final MutableDouble mutNumB = new MutableDouble(0d);
        final MutableDouble mutNumC = new MutableDouble(1d);

        assertEquals(true, mutNumA.hashCode() == mutNumA.hashCode());
        assertEquals(true, mutNumA.hashCode() == mutNumB.hashCode());
        assertEquals(false, mutNumA.hashCode() == mutNumC.hashCode());
        assertEquals(true, mutNumA.hashCode() == new Double(0d).hashCode());
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testCompareTo
    public void testCompareTo() {
        final MutableDouble mutNum = new MutableDouble(0d);

        assertEquals(0, mutNum.compareTo(new MutableDouble(0d)));
        assertEquals(+1, mutNum.compareTo(new MutableDouble(-1d)));
        assertEquals(-1, mutNum.compareTo(new MutableDouble(1d)));
        try {
            mutNum.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            mutNum.compareTo(new Double(0d));
            fail();
        } catch (ClassCastException ex) {}
        try {
            mutNum.compareTo("0");
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testPrimitiveValues
    public void testPrimitiveValues() {
        MutableDouble mutNum = new MutableDouble(1.7);
        
        assertEquals( 1.7F, mutNum.floatValue(), 0 );
        assertEquals( 1.7, mutNum.doubleValue(), 0 );
        assertEquals( (byte) 1, mutNum.byteValue() );
        assertEquals( (short) 1, mutNum.shortValue() );
        assertEquals( 1, mutNum.intValue() );
        assertEquals( 1L, mutNum.longValue() );
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testToDouble
    public void testToDouble() {
        assertEquals(new Double(0d), new MutableDouble(0d).toDouble());
        assertEquals(new Double(12.3d), new MutableDouble(12.3d).toDouble());
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testIncrement
    public void testIncrement() {
        MutableDouble mutNum = new MutableDouble(1);
        mutNum.increment();
        
        assertEquals(2, mutNum.intValue());
        assertEquals(2L, mutNum.longValue());
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testDecrement
    public void testDecrement() {
        MutableDouble mutNum = new MutableDouble(1);
        mutNum.decrement();
        
        assertEquals(0, mutNum.intValue());
        assertEquals(0L, mutNum.longValue());
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testAddValuePrimitive
    public void testAddValuePrimitive() {
        MutableDouble mutNum = new MutableDouble(1);
        mutNum.add(1.1d);
        
        assertEquals(2.1d, mutNum.doubleValue(), 0.01d);
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testAddValueObject
    public void testAddValueObject() {
        MutableDouble mutNum = new MutableDouble(1);
        mutNum.add(new Double(1.1d));
        
        assertEquals(2.1d, mutNum.doubleValue(), 0.01d);
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testSubtractValuePrimitive
    public void testSubtractValuePrimitive() {
        MutableDouble mutNum = new MutableDouble(1);
        mutNum.subtract(0.9d);
        
        assertEquals(0.1d, mutNum.doubleValue(), 0.01d);
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testSubtractValueObject
    public void testSubtractValueObject() {
        MutableDouble mutNum = new MutableDouble(1);
        mutNum.subtract(new Double(0.9d));
        
        assertEquals(0.1d, mutNum.doubleValue(), 0.01d);
    }

// org.apache.commons.lang.mutable.MutableDoubleTest::testToString
    public void testToString() {
        assertEquals("0.0", new MutableDouble(0d).toString());
        assertEquals("10.0", new MutableDouble(10d).toString());
        assertEquals("-123.0", new MutableDouble(-123d).toString());
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testConstructors
    public void testConstructors() {
        assertEquals(0f, new MutableFloat().floatValue(), 0.0001f);
        
        assertEquals(1f, new MutableFloat(1f).floatValue(), 0.0001f);
        
        assertEquals(2f, new MutableFloat(new Float(2f)).floatValue(), 0.0001f);
        assertEquals(3f, new MutableFloat(new MutableFloat(3f)).floatValue(), 0.0001f);
        try {
            new MutableFloat(null);
            fail();
        } catch (NullPointerException ex) {}
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testGetSet
    public void testGetSet() {
        final MutableFloat mutNum = new MutableFloat(0f);
        assertEquals(0f, new MutableFloat().floatValue(), 0.0001f);
        assertEquals(new Float(0), new MutableFloat().getValue());
        
        mutNum.setValue(1);
        assertEquals(1f, mutNum.floatValue(), 0.0001f);
        assertEquals(new Float(1f), mutNum.getValue());
        
        mutNum.setValue(new Float(2f));
        assertEquals(2f, mutNum.floatValue(), 0.0001f);
        assertEquals(new Float(2f), mutNum.getValue());
        
        mutNum.setValue(new MutableFloat(3f));
        assertEquals(3f, mutNum.floatValue(), 0.0001f);
        assertEquals(new Float(3f), mutNum.getValue());
        try {
            mutNum.setValue(null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            mutNum.setValue("0");
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testNanInfinite
    public void testNanInfinite() {
        MutableFloat mutNum = new MutableFloat(Float.NaN);
        assertEquals(true, mutNum.isNaN());
        
        mutNum = new MutableFloat(Float.POSITIVE_INFINITY);
        assertEquals(true, mutNum.isInfinite());
        
        mutNum = new MutableFloat(Float.NEGATIVE_INFINITY);
        assertEquals(true, mutNum.isInfinite());
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testEquals
    public void testEquals() {
        final MutableFloat mutNumA = new MutableFloat(0f);
        final MutableFloat mutNumB = new MutableFloat(0f);
        final MutableFloat mutNumC = new MutableFloat(1f);

        assertEquals(true, mutNumA.equals(mutNumA));
        assertEquals(true, mutNumA.equals(mutNumB));
        assertEquals(true, mutNumB.equals(mutNumA));
        assertEquals(true, mutNumB.equals(mutNumB));
        assertEquals(false, mutNumA.equals(mutNumC));
        assertEquals(false, mutNumB.equals(mutNumC));
        assertEquals(true, mutNumC.equals(mutNumC));
        assertEquals(false, mutNumA.equals(null));
        assertEquals(false, mutNumA.equals(new Float(0f)));
        assertEquals(false, mutNumA.equals("0"));
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testHashCode
    public void testHashCode() {
        final MutableFloat mutNumA = new MutableFloat(0f);
        final MutableFloat mutNumB = new MutableFloat(0f);
        final MutableFloat mutNumC = new MutableFloat(1f);

        assertEquals(true, mutNumA.hashCode() == mutNumA.hashCode());
        assertEquals(true, mutNumA.hashCode() == mutNumB.hashCode());
        assertEquals(false, mutNumA.hashCode() == mutNumC.hashCode());
        assertEquals(true, mutNumA.hashCode() == new Float(0f).hashCode());
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testCompareTo
    public void testCompareTo() {
        final MutableFloat mutNum = new MutableFloat(0f);

        assertEquals(0, mutNum.compareTo(new MutableFloat(0f)));
        assertEquals(+1, mutNum.compareTo(new MutableFloat(-1f)));
        assertEquals(-1, mutNum.compareTo(new MutableFloat(1f)));
        try {
            mutNum.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            mutNum.compareTo(new Float(0f));
            fail();
        } catch (ClassCastException ex) {}
        try {
            mutNum.compareTo("0");
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testPrimitiveValues
    public void testPrimitiveValues() {
        MutableFloat mutNum = new MutableFloat(1.7F);
        
        assertEquals( 1, mutNum.intValue() );
        assertEquals( 1.7, mutNum.doubleValue(), 0.00001 );
        assertEquals( (byte) 1, mutNum.byteValue() );
        assertEquals( (short) 1, mutNum.shortValue() );
        assertEquals( 1, mutNum.intValue() );
        assertEquals( 1L, mutNum.longValue() );
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testToFloat
    public void testToFloat() {
        assertEquals(new Float(0f), new MutableFloat(0f).toFloat());
        assertEquals(new Float(12.3f), new MutableFloat(12.3f).toFloat());
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testIncrement
    public void testIncrement() {
        MutableFloat mutNum = new MutableFloat(1);
        mutNum.increment();
        
        assertEquals(2, mutNum.intValue());
        assertEquals(2L, mutNum.longValue());
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testDecrement
    public void testDecrement() {
        MutableFloat mutNum = new MutableFloat(1);
        mutNum.decrement();
        
        assertEquals(0, mutNum.intValue());
        assertEquals(0L, mutNum.longValue());
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testAddValuePrimitive
    public void testAddValuePrimitive() {
        MutableFloat mutNum = new MutableFloat(1);
        mutNum.add(1.1f);
        
        assertEquals(2.1f, mutNum.floatValue(), 0.01f);
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testAddValueObject
    public void testAddValueObject() {
        MutableFloat mutNum = new MutableFloat(1);
        mutNum.add(new Float(1.1f));
        
        assertEquals(2.1f, mutNum.floatValue(), 0.01f);
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testSubtractValuePrimitive
    public void testSubtractValuePrimitive() {
        MutableFloat mutNum = new MutableFloat(1);
        mutNum.subtract(0.9f);
        
        assertEquals(0.1f, mutNum.floatValue(), 0.01f);
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testSubtractValueObject
    public void testSubtractValueObject() {
        MutableFloat mutNum = new MutableFloat(1);
        mutNum.subtract(new Float(0.9f));
        
        assertEquals(0.1f, mutNum.floatValue(), 0.01f);
    }

// org.apache.commons.lang.mutable.MutableFloatTest::testToString
    public void testToString() {
        assertEquals("0.0", new MutableFloat(0f).toString());
        assertEquals("10.0", new MutableFloat(10f).toString());
        assertEquals("-123.0", new MutableFloat(-123f).toString());
    }
