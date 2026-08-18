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
                        && (numeric.charAt(0) == '-' && isDigits(numeric.substring(1)) || isDigits(numeric))) {
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
                    //$FALL-THROUGH$
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
                    //$FALL-THROUGH$
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

    public static boolean isNumber(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1) {
            if (chars[start] == '0' && chars[start + 1] == 'x') {
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9')
                        && (chars[i] < 'a' || chars[i] > 'f')
                        && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
              // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent   
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (!allowSigns
                && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                || chars[i] == 'L') {
                // not allowing L with an exponent
                return foundDigit && !hasExp;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

// relevant test
// org.apache.commons.lang3.BooleanUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new BooleanUtils());
        Constructor<?>[] cons = BooleanUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(BooleanUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(BooleanUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_negate_Boolean
    public void test_negate_Boolean() {
        assertSame(null, BooleanUtils.negate(null));
        assertSame(Boolean.TRUE, BooleanUtils.negate(Boolean.FALSE));
        assertSame(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_isTrue_Boolean
    public void test_isTrue_Boolean() {
        assertEquals(true, BooleanUtils.isTrue(Boolean.TRUE));
        assertEquals(false, BooleanUtils.isTrue(Boolean.FALSE));
        assertEquals(false, BooleanUtils.isTrue((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_isNotTrue_Boolean
    public void test_isNotTrue_Boolean() {
        assertEquals(false, BooleanUtils.isNotTrue(Boolean.TRUE));
        assertEquals(true, BooleanUtils.isNotTrue(Boolean.FALSE));
        assertEquals(true, BooleanUtils.isNotTrue((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_isFalse_Boolean
    public void test_isFalse_Boolean() {
        assertEquals(false, BooleanUtils.isFalse(Boolean.TRUE));
        assertEquals(true, BooleanUtils.isFalse(Boolean.FALSE));
        assertEquals(false, BooleanUtils.isFalse((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_isNotFalse_Boolean
    public void test_isNotFalse_Boolean() {
        assertEquals(true, BooleanUtils.isNotFalse(Boolean.TRUE));
        assertEquals(false, BooleanUtils.isNotFalse(Boolean.FALSE));
        assertEquals(true, BooleanUtils.isNotFalse((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_Boolean
    public void test_toBoolean_Boolean() {
        assertEquals(true, BooleanUtils.toBoolean(Boolean.TRUE));
        assertEquals(false, BooleanUtils.toBoolean(Boolean.FALSE));
        assertEquals(false, BooleanUtils.toBoolean((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanDefaultIfNull_Boolean_boolean
    public void test_toBooleanDefaultIfNull_Boolean_boolean() {
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, true));
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, false));
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull((Boolean) null, true));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull((Boolean) null, false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_int
    public void test_toBoolean_int() {
        assertEquals(true, BooleanUtils.toBoolean(1));
        assertEquals(true, BooleanUtils.toBoolean(-1));
        assertEquals(false, BooleanUtils.toBoolean(0));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_int
    public void test_toBooleanObject_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(-1));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_Integer
    public void test_toBooleanObject_Integer() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(1)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(-1)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(new Integer(0)));
        assertEquals(null, BooleanUtils.toBooleanObject((Integer) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_int_int_int
    public void test_toBoolean_int_int_int() {
        assertEquals(true, BooleanUtils.toBoolean(6, 6, 7));
        assertEquals(false, BooleanUtils.toBoolean(7, 6, 7));
        try {
            BooleanUtils.toBoolean(8, 6, 7);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_Integer_Integer_Integer
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

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_int_int_int
    public void test_toBooleanObject_int_int_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(6, 6, 7, 8));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(7, 6, 7, 8));
        assertEquals(null, BooleanUtils.toBooleanObject(8, 6, 7, 8));
        try {
            BooleanUtils.toBooleanObject(9, 6, 7, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_Integer_Integer_Integer_Integer
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

// org.apache.commons.lang3.BooleanUtilsTest::test_toInteger_boolean
    public void test_toInteger_boolean() {
        assertEquals(1, BooleanUtils.toInteger(true));
        assertEquals(0, BooleanUtils.toInteger(false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toIntegerObject_boolean
    public void test_toIntegerObject_boolean() {
        assertEquals(new Integer(1), BooleanUtils.toIntegerObject(true));
        assertEquals(new Integer(0), BooleanUtils.toIntegerObject(false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toIntegerObject_Boolean
    public void test_toIntegerObject_Boolean() {
        assertEquals(new Integer(1), BooleanUtils.toIntegerObject(Boolean.TRUE));
        assertEquals(new Integer(0), BooleanUtils.toIntegerObject(Boolean.FALSE));
        assertEquals(null, BooleanUtils.toIntegerObject((Boolean) null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toInteger_boolean_int_int
    public void test_toInteger_boolean_int_int() {
        assertEquals(6, BooleanUtils.toInteger(true, 6, 7));
        assertEquals(7, BooleanUtils.toInteger(false, 6, 7));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toInteger_Boolean_int_int_int
    public void test_toInteger_Boolean_int_int_int() {
        assertEquals(6, BooleanUtils.toInteger(Boolean.TRUE, 6, 7, 8));
        assertEquals(7, BooleanUtils.toInteger(Boolean.FALSE, 6, 7, 8));
        assertEquals(8, BooleanUtils.toInteger(null, 6, 7, 8));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toIntegerObject_boolean_Integer_Integer
    public void test_toIntegerObject_boolean_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        assertEquals(six, BooleanUtils.toIntegerObject(true, six, seven));
        assertEquals(seven, BooleanUtils.toIntegerObject(false, six, seven));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toIntegerObject_Boolean_Integer_Integer_Integer
    public void test_toIntegerObject_Boolean_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        Integer eight = new Integer(8);
        assertEquals(six, BooleanUtils.toIntegerObject(Boolean.TRUE, six, seven, eight));
        assertEquals(seven, BooleanUtils.toIntegerObject(Boolean.FALSE, six, seven, eight));
        assertEquals(eight, BooleanUtils.toIntegerObject((Boolean) null, six, seven, eight));
        assertEquals(null, BooleanUtils.toIntegerObject((Boolean) null, six, seven, null));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_String
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

// org.apache.commons.lang3.BooleanUtilsTest::test_toBooleanObject_String_String_String_String
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

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_String
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
        assertEquals(false, BooleanUtils.toBoolean("yes?"));
        assertEquals(false, BooleanUtils.toBoolean("tru"));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toBoolean_String_String_String
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

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringTrueFalse_Boolean
    public void test_toStringTrueFalse_Boolean() {
        assertEquals(null, BooleanUtils.toStringTrueFalse((Boolean) null));
        assertEquals("true", BooleanUtils.toStringTrueFalse(Boolean.TRUE));
        assertEquals("false", BooleanUtils.toStringTrueFalse(Boolean.FALSE));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringOnOff_Boolean
    public void test_toStringOnOff_Boolean() {
        assertEquals(null, BooleanUtils.toStringOnOff((Boolean) null));
        assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringYesNo_Boolean
    public void test_toStringYesNo_Boolean() {
        assertEquals(null, BooleanUtils.toStringYesNo((Boolean) null));
        assertEquals("yes", BooleanUtils.toStringYesNo(Boolean.TRUE));
        assertEquals("no", BooleanUtils.toStringYesNo(Boolean.FALSE));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toString_Boolean_String_String_String
    public void test_toString_Boolean_String_String_String() {
        assertEquals("U", BooleanUtils.toString((Boolean) null, "Y", "N", "U"));
        assertEquals("Y", BooleanUtils.toString(Boolean.TRUE, "Y", "N", "U"));
        assertEquals("N", BooleanUtils.toString(Boolean.FALSE, "Y", "N", "U"));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringTrueFalse_boolean
    public void test_toStringTrueFalse_boolean() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringOnOff_boolean
    public void test_toStringOnOff_boolean() {
        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toStringYesNo_boolean
    public void test_toStringYesNo_boolean() {
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));
    }

// org.apache.commons.lang3.BooleanUtilsTest::test_toString_boolean_String_String_String
    public void test_toString_boolean_String_String_String() {
        assertEquals("Y", BooleanUtils.toString(true, "Y", "N"));
        assertEquals("N", BooleanUtils.toString(false, "Y", "N"));
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_primitive_nullInput
    public void testXor_primitive_nullInput() {
        final boolean[] b = null;
        try {
            BooleanUtils.xor(b);
            fail("Exception was not thrown for null input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_primitive_emptyInput
    public void testXor_primitive_emptyInput() {
        try {
            BooleanUtils.xor(new boolean[] {});
            fail("Exception was not thrown for empty input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_primitive_validInput_2items
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

// org.apache.commons.lang3.BooleanUtilsTest::testXor_primitive_validInput_3items
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

// org.apache.commons.lang3.BooleanUtilsTest::testXor_object_nullInput
    public void testXor_object_nullInput() {
        final Boolean[] b = null;
        try {
            BooleanUtils.xor(b);
            fail("Exception was not thrown for null input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_object_emptyInput
    public void testXor_object_emptyInput() {
        try {
            BooleanUtils.xor(new Boolean[] {});
            fail("Exception was not thrown for empty input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_object_nullElementInput
    public void testXor_object_nullElementInput() {
        try {
            BooleanUtils.xor(new Boolean[] {null});
            fail("Exception was not thrown for null element input.");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.BooleanUtilsTest::testXor_object_validInput_2items
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

// org.apache.commons.lang3.BooleanUtilsTest::testXor_object_validInput_3items
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

// org.apache.commons.lang3.math.NumberUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new NumberUtils());
        Constructor<?>[] cons = NumberUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(NumberUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(NumberUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToIntString
    public void testToIntString() {
        assertTrue("toInt(String) 1 failed", NumberUtils.toInt("12345") == 12345);
        assertTrue("toInt(String) 2 failed", NumberUtils.toInt("abc") == 0);
        assertTrue("toInt(empty) failed", NumberUtils.toInt("") == 0);
        assertTrue("toInt(null) failed", NumberUtils.toInt(null) == 0);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToIntStringI
    public void testToIntStringI() {
        assertTrue("toInt(String,int) 1 failed", NumberUtils.toInt("12345", 5) == 12345);
        assertTrue("toInt(String,int) 2 failed", NumberUtils.toInt("1234.5", 5) == 5);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToLongString
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

// org.apache.commons.lang3.math.NumberUtilsTest::testToLongStringL
    public void testToLongStringL() {
        assertTrue("toLong(String,long) 1 failed", NumberUtils.toLong("12345", 5l) == 12345l);
        assertTrue("toLong(String,long) 2 failed", NumberUtils.toLong("1234.5", 5l) == 5l);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToFloatString
    public void testToFloatString() {
        assertTrue("toFloat(String) 1 failed", NumberUtils.toFloat("-1.2345") == -1.2345f);
        assertTrue("toFloat(String) 2 failed", NumberUtils.toFloat("1.2345") == 1.2345f);
        assertTrue("toFloat(String) 3 failed", NumberUtils.toFloat("abc") == 0.0f);
        assertTrue("toFloat(Float.MAX_VALUE) failed", NumberUtils.toFloat(Float.MAX_VALUE+"") ==  Float.MAX_VALUE);
        assertTrue("toFloat(Float.MIN_VALUE) failed", NumberUtils.toFloat(Float.MIN_VALUE+"") == Float.MIN_VALUE);
        assertTrue("toFloat(empty) failed", NumberUtils.toFloat("") == 0.0f);
        assertTrue("toFloat(null) failed", NumberUtils.toFloat(null) == 0.0f);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToFloatStringF
    public void testToFloatStringF() {
        assertTrue("toFloat(String,int) 1 failed", NumberUtils.toFloat("1.2345", 5.1f) == 1.2345f);
        assertTrue("toFloat(String,int) 2 failed", NumberUtils.toFloat("a", 5.0f) == 5.0f);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testStringToDoubleString
    public void testStringToDoubleString() {
        assertTrue("toDouble(String) 1 failed", NumberUtils.toDouble("-1.2345") == -1.2345d);
        assertTrue("toDouble(String) 2 failed", NumberUtils.toDouble("1.2345") == 1.2345d);
        assertTrue("toDouble(String) 3 failed", NumberUtils.toDouble("abc") == 0.0d);
        assertTrue("toDouble(Double.MAX_VALUE) failed", NumberUtils.toDouble(Double.MAX_VALUE+"") == Double.MAX_VALUE);
        assertTrue("toDouble(Double.MIN_VALUE) failed", NumberUtils.toDouble(Double.MIN_VALUE+"") == Double.MIN_VALUE);
        assertTrue("toDouble(empty) failed", NumberUtils.toDouble("") == 0.0d);
        assertTrue("toDouble(null) failed", NumberUtils.toDouble(null) == 0.0d);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testStringToDoubleStringD
    public void testStringToDoubleStringD() {
        assertTrue("toDouble(String,int) 1 failed", NumberUtils.toDouble("1.2345", 5.1d) == 1.2345d);
        assertTrue("toDouble(String,int) 2 failed", NumberUtils.toDouble("a", 5.0d) == 5.0d);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToByteString
    public void testToByteString() {
        assertTrue("toByte(String) 1 failed", NumberUtils.toByte("123") == 123);
        assertTrue("toByte(String) 2 failed", NumberUtils.toByte("abc") == 0);
        assertTrue("toByte(empty) failed", NumberUtils.toByte("") == 0);
        assertTrue("toByte(null) failed", NumberUtils.toByte(null) == 0);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToByteStringI
    public void testToByteStringI() {
        assertTrue("toByte(String,byte) 1 failed", NumberUtils.toByte("123", (byte) 5) == 123);
        assertTrue("toByte(String,byte) 2 failed", NumberUtils.toByte("12.3", (byte) 5) == 5);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToShortString
    public void testToShortString() {
        assertTrue("toShort(String) 1 failed", NumberUtils.toShort("12345") == 12345);
        assertTrue("toShort(String) 2 failed", NumberUtils.toShort("abc") == 0);
        assertTrue("toShort(empty) failed", NumberUtils.toShort("") == 0);
        assertTrue("toShort(null) failed", NumberUtils.toShort(null) == 0);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testToShortStringI
    public void testToShortStringI() {
        assertTrue("toShort(String,short) 1 failed", NumberUtils.toShort("12345", (short) 5) == 12345);
        assertTrue("toShort(String,short) 2 failed", NumberUtils.toShort("1234.5", (short) 5) == 5);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateNumber
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

        
        assertEquals("createNumber(String) LANG-521 failed", new Float("2."), NumberUtils.createNumber("2."));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateFloat
    public void testCreateFloat() {
        assertEquals("createFloat(String) failed", new Float("1234.5"), NumberUtils.createFloat("1234.5"));
        assertEquals("createFloat(null) failed", null, NumberUtils.createFloat(null));
        this.testCreateFloatFailure("");
        this.testCreateFloatFailure(" ");
        this.testCreateFloatFailure("\b\t\n\f\r");
        
        this.testCreateFloatFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateFloatFailure
    protected void testCreateFloatFailure(String str) {
        try {
            Float value = NumberUtils.createFloat(str);
            fail("createFloat(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateDouble
    public void testCreateDouble() {
        assertEquals("createDouble(String) failed", new Double("1234.5"), NumberUtils.createDouble("1234.5"));
        assertEquals("createDouble(null) failed", null, NumberUtils.createDouble(null));
        this.testCreateDoubleFailure("");
        this.testCreateDoubleFailure(" ");
        this.testCreateDoubleFailure("\b\t\n\f\r");
        
        this.testCreateDoubleFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateDoubleFailure
    protected void testCreateDoubleFailure(String str) {
        try {
            Double value = NumberUtils.createDouble(str);
            fail("createDouble(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateInteger
    public void testCreateInteger() {
        assertEquals("createInteger(String) failed", new Integer("12345"), NumberUtils.createInteger("12345"));
        assertEquals("createInteger(null) failed", null, NumberUtils.createInteger(null));
        this.testCreateIntegerFailure("");
        this.testCreateIntegerFailure(" ");
        this.testCreateIntegerFailure("\b\t\n\f\r");
        
        this.testCreateIntegerFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateIntegerFailure
    protected void testCreateIntegerFailure(String str) {
        try {
            Integer value = NumberUtils.createInteger(str);
            fail("createInteger(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateLong
    public void testCreateLong() {
        assertEquals("createLong(String) failed", new Long("12345"), NumberUtils.createLong("12345"));
        assertEquals("createLong(null) failed", null, NumberUtils.createLong(null));
        this.testCreateLongFailure("");
        this.testCreateLongFailure(" ");
        this.testCreateLongFailure("\b\t\n\f\r");
        
        this.testCreateLongFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateLongFailure
    protected void testCreateLongFailure(String str) {
        try {
            Long value = NumberUtils.createLong(str);
            fail("createLong(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateBigInteger
    public void testCreateBigInteger() {
        assertEquals("createBigInteger(String) failed", new BigInteger("12345"), NumberUtils.createBigInteger("12345"));
        assertEquals("createBigInteger(null) failed", null, NumberUtils.createBigInteger(null));
        this.testCreateBigIntegerFailure("");
        this.testCreateBigIntegerFailure(" ");
        this.testCreateBigIntegerFailure("\b\t\n\f\r");
        
        this.testCreateBigIntegerFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateBigIntegerFailure
    protected void testCreateBigIntegerFailure(String str) {
        try {
            BigInteger value = NumberUtils.createBigInteger(str);
            fail("createBigInteger(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateBigDecimal
    public void testCreateBigDecimal() {
        assertEquals("createBigDecimal(String) failed", new BigDecimal("1234.5"), NumberUtils.createBigDecimal("1234.5"));
        assertEquals("createBigDecimal(null) failed", null, NumberUtils.createBigDecimal(null));
        this.testCreateBigDecimalFailure("");
        this.testCreateBigDecimalFailure(" ");
        this.testCreateBigDecimalFailure("\b\t\n\f\r");
        
        this.testCreateBigDecimalFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCreateBigDecimalFailure
    protected void testCreateBigDecimalFailure(String str) {
        try {
            BigDecimal value = NumberUtils.createBigDecimal(str);
            fail("createBigDecimal(blank) failed: " + value);
        } catch (NumberFormatException ex) {
            
        }
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinLong
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMinInt
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMinShort
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMinByte
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMinDouble
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMinFloat
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxLong
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxInt
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxShort
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxByte
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxDouble
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMaxFloat
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumLong
    public void testMinimumLong() {
        assertEquals("minimum(long,long,long) 1 failed", 12345L, NumberUtils.min(12345L, 12345L + 1L, 12345L + 2L));
        assertEquals("minimum(long,long,long) 2 failed", 12345L, NumberUtils.min(12345L + 1L, 12345L, 12345 + 2L));
        assertEquals("minimum(long,long,long) 3 failed", 12345L, NumberUtils.min(12345L + 1L, 12345L + 2L, 12345L));
        assertEquals("minimum(long,long,long) 4 failed", 12345L, NumberUtils.min(12345L + 1L, 12345L, 12345L));
        assertEquals("minimum(long,long,long) 5 failed", 12345L, NumberUtils.min(12345L, 12345L, 12345L));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumInt
    public void testMinimumInt() {
        assertEquals("minimum(int,int,int) 1 failed", 12345, NumberUtils.min(12345, 12345 + 1, 12345 + 2));
        assertEquals("minimum(int,int,int) 2 failed", 12345, NumberUtils.min(12345 + 1, 12345, 12345 + 2));
        assertEquals("minimum(int,int,int) 3 failed", 12345, NumberUtils.min(12345 + 1, 12345 + 2, 12345));
        assertEquals("minimum(int,int,int) 4 failed", 12345, NumberUtils.min(12345 + 1, 12345, 12345));
        assertEquals("minimum(int,int,int) 5 failed", 12345, NumberUtils.min(12345, 12345, 12345));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumShort
    public void testMinimumShort() {
        short low = 1234;
        short mid = 1234 + 1;
        short high = 1234 + 2;
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(low, mid, high));
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(mid, low, high));
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(mid, high, low));
        assertEquals("minimum(short,short,short) 1 failed", low, NumberUtils.min(low, mid, low));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumByte
    public void testMinimumByte() {
        byte low = 123;
        byte mid = 123 + 1;
        byte high = 123 + 2;
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(low, mid, high));
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(mid, low, high));
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(mid, high, low));
        assertEquals("minimum(byte,byte,byte) 1 failed", low, NumberUtils.min(low, mid, low));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumDouble
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMinimumFloat
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumLong
    public void testMaximumLong() {
        assertEquals("maximum(long,long,long) 1 failed", 12345L, NumberUtils.max(12345L, 12345L - 1L, 12345L - 2L));
        assertEquals("maximum(long,long,long) 2 failed", 12345L, NumberUtils.max(12345L - 1L, 12345L, 12345L - 2L));
        assertEquals("maximum(long,long,long) 3 failed", 12345L, NumberUtils.max(12345L - 1L, 12345L - 2L, 12345L));
        assertEquals("maximum(long,long,long) 4 failed", 12345L, NumberUtils.max(12345L - 1L, 12345L, 12345L));
        assertEquals("maximum(long,long,long) 5 failed", 12345L, NumberUtils.max(12345L, 12345L, 12345L));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumInt
    public void testMaximumInt() {
        assertEquals("maximum(int,int,int) 1 failed", 12345, NumberUtils.max(12345, 12345 - 1, 12345 - 2));
        assertEquals("maximum(int,int,int) 2 failed", 12345, NumberUtils.max(12345 - 1, 12345, 12345 - 2));
        assertEquals("maximum(int,int,int) 3 failed", 12345, NumberUtils.max(12345 - 1, 12345 - 2, 12345));
        assertEquals("maximum(int,int,int) 4 failed", 12345, NumberUtils.max(12345 - 1, 12345, 12345));
        assertEquals("maximum(int,int,int) 5 failed", 12345, NumberUtils.max(12345, 12345, 12345));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumShort
    public void testMaximumShort() {
        short low = 1234;
        short mid = 1234 + 1;
        short high = 1234 + 2;
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(low, mid, high));
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(mid, low, high));
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(mid, high, low));
        assertEquals("maximum(short,short,short) 1 failed", high, NumberUtils.max(high, mid, high));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumByte
    public void testMaximumByte() {
        byte low = 123;
        byte mid = 123 + 1;
        byte high = 123 + 2;
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(low, mid, high));
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(mid, low, high));
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(mid, high, low));
        assertEquals("maximum(byte,byte,byte) 1 failed", high, NumberUtils.max(high, mid, high));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumDouble
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

// org.apache.commons.lang3.math.NumberUtilsTest::testMaximumFloat
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

// org.apache.commons.lang3.math.NumberUtilsTest::testCompareDouble
    public void testCompareDouble() {
        assertTrue(Double.compare(Double.NaN, Double.NaN) == 0);
        assertTrue(Double.compare(Double.NaN, Double.POSITIVE_INFINITY) == +1);
        assertTrue(Double.compare(Double.NaN, Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(Double.NaN, 1.2d) == +1);
        assertTrue(Double.compare(Double.NaN, 0.0d) == +1);
        assertTrue(Double.compare(Double.NaN, -0.0d) == +1);
        assertTrue(Double.compare(Double.NaN, -1.2d) == +1);
        assertTrue(Double.compare(Double.NaN, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(Double.NaN, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.NaN) == -1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY) == 0);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, 1.2d) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, 0.0d) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, -0.0d) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, -1.2d) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(Double.MAX_VALUE, Double.NaN) == -1);
        assertTrue(Double.compare(Double.MAX_VALUE, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(Double.MAX_VALUE, Double.MAX_VALUE) == 0);
        assertTrue(Double.compare(Double.MAX_VALUE, 1.2d) == +1);
        assertTrue(Double.compare(Double.MAX_VALUE, 0.0d) == +1);
        assertTrue(Double.compare(Double.MAX_VALUE, -0.0d) == +1);
        assertTrue(Double.compare(Double.MAX_VALUE, -1.2d) == +1);
        assertTrue(Double.compare(Double.MAX_VALUE, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(Double.MAX_VALUE, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(1.2d, Double.NaN) == -1);
        assertTrue(Double.compare(1.2d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(1.2d, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(1.2d, 1.2d) == 0);
        assertTrue(Double.compare(1.2d, 0.0d) == +1);
        assertTrue(Double.compare(1.2d, -0.0d) == +1);
        assertTrue(Double.compare(1.2d, -1.2d) == +1);
        assertTrue(Double.compare(1.2d, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(1.2d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(0.0d, Double.NaN) == -1);
        assertTrue(Double.compare(0.0d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(0.0d, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(0.0d, 1.2d) == -1);
        assertTrue(Double.compare(0.0d, 0.0d) == 0);
        assertTrue(Double.compare(0.0d, -0.0d) == +1);
        assertTrue(Double.compare(0.0d, -1.2d) == +1);
        assertTrue(Double.compare(0.0d, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(0.0d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(-0.0d, Double.NaN) == -1);
        assertTrue(Double.compare(-0.0d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(-0.0d, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(-0.0d, 1.2d) == -1);
        assertTrue(Double.compare(-0.0d, 0.0d) == -1);
        assertTrue(Double.compare(-0.0d, -0.0d) == 0);
        assertTrue(Double.compare(-0.0d, -1.2d) == +1);
        assertTrue(Double.compare(-0.0d, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(-0.0d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(-1.2d, Double.NaN) == -1);
        assertTrue(Double.compare(-1.2d, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(-1.2d, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(-1.2d, 1.2d) == -1);
        assertTrue(Double.compare(-1.2d, 0.0d) == -1);
        assertTrue(Double.compare(-1.2d, -0.0d) == -1);
        assertTrue(Double.compare(-1.2d, -1.2d) == 0);
        assertTrue(Double.compare(-1.2d, -Double.MAX_VALUE) == +1);
        assertTrue(Double.compare(-1.2d, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(-Double.MAX_VALUE, Double.NaN) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, 1.2d) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, 0.0d) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, -0.0d) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, -1.2d) == -1);
        assertTrue(Double.compare(-Double.MAX_VALUE, -Double.MAX_VALUE) == 0);
        assertTrue(Double.compare(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.NaN) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, 1.2d) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, 0.0d) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, -0.0d) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, -1.2d) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE) == -1);
        assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY) == 0);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testCompareFloat
    public void testCompareFloat() {
        assertTrue(Float.compare(Float.NaN, Float.NaN) == 0);
        assertTrue(Float.compare(Float.NaN, Float.POSITIVE_INFINITY) == +1);
        assertTrue(Float.compare(Float.NaN, Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(Float.NaN, 1.2f) == +1);
        assertTrue(Float.compare(Float.NaN, 0.0f) == +1);
        assertTrue(Float.compare(Float.NaN, -0.0f) == +1);
        assertTrue(Float.compare(Float.NaN, -1.2f) == +1);
        assertTrue(Float.compare(Float.NaN, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(Float.NaN, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.NaN) == -1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY) == 0);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, 1.2f) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, 0.0f) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, -0.0f) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, -1.2f) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(Float.MAX_VALUE, Float.NaN) == -1);
        assertTrue(Float.compare(Float.MAX_VALUE, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(Float.MAX_VALUE, Float.MAX_VALUE) == 0);
        assertTrue(Float.compare(Float.MAX_VALUE, 1.2f) == +1);
        assertTrue(Float.compare(Float.MAX_VALUE, 0.0f) == +1);
        assertTrue(Float.compare(Float.MAX_VALUE, -0.0f) == +1);
        assertTrue(Float.compare(Float.MAX_VALUE, -1.2f) == +1);
        assertTrue(Float.compare(Float.MAX_VALUE, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(Float.MAX_VALUE, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(1.2f, Float.NaN) == -1);
        assertTrue(Float.compare(1.2f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(1.2f, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(1.2f, 1.2f) == 0);
        assertTrue(Float.compare(1.2f, 0.0f) == +1);
        assertTrue(Float.compare(1.2f, -0.0f) == +1);
        assertTrue(Float.compare(1.2f, -1.2f) == +1);
        assertTrue(Float.compare(1.2f, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(1.2f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(0.0f, Float.NaN) == -1);
        assertTrue(Float.compare(0.0f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(0.0f, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(0.0f, 1.2f) == -1);
        assertTrue(Float.compare(0.0f, 0.0f) == 0);
        assertTrue(Float.compare(0.0f, -0.0f) == +1);
        assertTrue(Float.compare(0.0f, -1.2f) == +1);
        assertTrue(Float.compare(0.0f, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(0.0f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(-0.0f, Float.NaN) == -1);
        assertTrue(Float.compare(-0.0f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(-0.0f, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(-0.0f, 1.2f) == -1);
        assertTrue(Float.compare(-0.0f, 0.0f) == -1);
        assertTrue(Float.compare(-0.0f, -0.0f) == 0);
        assertTrue(Float.compare(-0.0f, -1.2f) == +1);
        assertTrue(Float.compare(-0.0f, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(-0.0f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(-1.2f, Float.NaN) == -1);
        assertTrue(Float.compare(-1.2f, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(-1.2f, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(-1.2f, 1.2f) == -1);
        assertTrue(Float.compare(-1.2f, 0.0f) == -1);
        assertTrue(Float.compare(-1.2f, -0.0f) == -1);
        assertTrue(Float.compare(-1.2f, -1.2f) == 0);
        assertTrue(Float.compare(-1.2f, -Float.MAX_VALUE) == +1);
        assertTrue(Float.compare(-1.2f, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(-Float.MAX_VALUE, Float.NaN) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, 1.2f) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, 0.0f) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, -0.0f) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, -1.2f) == -1);
        assertTrue(Float.compare(-Float.MAX_VALUE, -Float.MAX_VALUE) == 0);
        assertTrue(Float.compare(-Float.MAX_VALUE, Float.NEGATIVE_INFINITY) == +1);
        
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.NaN) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, 1.2f) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, 0.0f) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, -0.0f) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, -1.2f) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, -Float.MAX_VALUE) == -1);
        assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY) == 0);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testIsDigits
    public void testIsDigits() {
        assertEquals("isDigits(null) failed", false, NumberUtils.isDigits(null));
        assertEquals("isDigits('') failed", false, NumberUtils.isDigits(""));
        assertEquals("isDigits(String) failed", true, NumberUtils.isDigits("12345"));
        assertEquals("isDigits(String) neg 1 failed", false, NumberUtils.isDigits("1234.5"));
        assertEquals("isDigits(String) neg 3 failed", false, NumberUtils.isDigits("1ab"));
        assertEquals("isDigits(String) neg 4 failed", false, NumberUtils.isDigits("abc"));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testIsNumber
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

        
        val = "2.";
        assertTrue("isNumber(String) LANG-521 failed", NumberUtils.isNumber(val));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testConstants
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

// org.apache.commons.lang3.math.NumberUtilsTest::testLang300
    public void testLang300() {
        NumberUtils.createNumber("-1l");
        NumberUtils.createNumber("01l");
        NumberUtils.createNumber("1l");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testLang381
    public void testLang381() {
        assertTrue(Double.isNaN(NumberUtils.min(1.2, 2.5, Double.NaN)));
        assertTrue(Double.isNaN(NumberUtils.max(1.2, 2.5, Double.NaN)));
        assertTrue(Float.isNaN(NumberUtils.min(1.2f, 2.5f, Float.NaN)));
        assertTrue(Float.isNaN(NumberUtils.max(1.2f, 2.5f, Float.NaN)));

        double[] a = new double[] { 1.2, Double.NaN, 3.7, 27.0, 42.0, Double.NaN };
        assertTrue(Double.isNaN(NumberUtils.max(a)));
        assertTrue(Double.isNaN(NumberUtils.min(a)));

        double[] b = new double[] { Double.NaN, 1.2, Double.NaN, 3.7, 27.0, 42.0, Double.NaN };
        assertTrue(Double.isNaN(NumberUtils.max(b)));
        assertTrue(Double.isNaN(NumberUtils.min(b)));

        float[] aF = new float[] { 1.2f, Float.NaN, 3.7f, 27.0f, 42.0f, Float.NaN };
        assertTrue(Float.isNaN(NumberUtils.max(aF)));

        float[] bF = new float[] { Float.NaN, 1.2f, Float.NaN, 3.7f, 27.0f, 42.0f, Float.NaN };
        assertTrue(Float.isNaN(NumberUtils.max(bF)));
    }

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testConstructor
    public void testConstructor() throws Exception {
        assertNotNull(MethodUtils.class.newInstance());
    }

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testInvokeConstructor
    public void testInvokeConstructor() throws Exception {
        assertEquals("()", ConstructorUtils.invokeConstructor(TestBean.class,
                ArrayUtils.EMPTY_CLASS_ARRAY).toString());
        assertEquals("()", ConstructorUtils.invokeConstructor(TestBean.class,
                (Class[]) null).toString());
        assertEquals("(String)", ConstructorUtils.invokeConstructor(
                TestBean.class, "").toString());
        assertEquals("(Object)", ConstructorUtils.invokeConstructor(
                TestBean.class, new Object()).toString());
        assertEquals("(Object)", ConstructorUtils.invokeConstructor(
                TestBean.class, Boolean.TRUE).toString());
        assertEquals("(Integer)", ConstructorUtils.invokeConstructor(
                TestBean.class, NumberUtils.INTEGER_ONE).toString());
        assertEquals("(int)", ConstructorUtils.invokeConstructor(
                TestBean.class, NumberUtils.BYTE_ONE).toString());
        assertEquals("(double)", ConstructorUtils.invokeConstructor(
                TestBean.class, NumberUtils.LONG_ONE).toString());
        assertEquals("(double)", ConstructorUtils.invokeConstructor(
                TestBean.class, NumberUtils.DOUBLE_ONE).toString());
    }

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testInvokeExactConstructor
    public void testInvokeExactConstructor() throws Exception {
        assertEquals("()", ConstructorUtils.invokeExactConstructor(
                TestBean.class, ArrayUtils.EMPTY_CLASS_ARRAY).toString());
        assertEquals("()", ConstructorUtils.invokeExactConstructor(
                TestBean.class, (Class[]) null).toString());
        assertEquals("(String)", ConstructorUtils.invokeExactConstructor(
                TestBean.class, "").toString());
        assertEquals("(Object)", ConstructorUtils.invokeExactConstructor(
                TestBean.class, new Object()).toString());
        assertEquals("(Integer)", ConstructorUtils.invokeExactConstructor(
                TestBean.class, NumberUtils.INTEGER_ONE).toString());
        assertEquals("(double)", ConstructorUtils.invokeExactConstructor(
                TestBean.class, new Object[] { NumberUtils.DOUBLE_ONE },
                new Class[] { Double.TYPE }).toString());

        try {
            ConstructorUtils.invokeExactConstructor(TestBean.class,
                    NumberUtils.BYTE_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            ConstructorUtils.invokeExactConstructor(TestBean.class,
                    NumberUtils.LONG_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            ConstructorUtils.invokeExactConstructor(TestBean.class,
                    Boolean.TRUE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
    }

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testGetAccessibleConstructor
    public void testGetAccessibleConstructor() throws Exception {
        assertNotNull(ConstructorUtils.getAccessibleConstructor(Object.class
                .getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY)));
        assertNull(ConstructorUtils.getAccessibleConstructor(PrivateClass.class
                .getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY)));
    }

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testGetAccessibleConstructorFromDescription
    public void testGetAccessibleConstructorFromDescription() throws Exception {
        assertNotNull(ConstructorUtils.getAccessibleConstructor(Object.class,
                ArrayUtils.EMPTY_CLASS_ARRAY));
        assertNull(ConstructorUtils.getAccessibleConstructor(
                PrivateClass.class, ArrayUtils.EMPTY_CLASS_ARRAY));
    }

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testGetMatchingAccessibleMethod
    public void testGetMatchingAccessibleMethod() throws Exception {
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                ArrayUtils.EMPTY_CLASS_ARRAY, ArrayUtils.EMPTY_CLASS_ARRAY);
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class, null,
                ArrayUtils.EMPTY_CLASS_ARRAY);
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(String.class), singletonArray(String.class));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Object.class), singletonArray(Object.class));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Boolean.class), singletonArray(Object.class));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Byte.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Byte.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Short.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Short.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Character.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Character.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Integer.class), singletonArray(Integer.class));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Integer.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Long.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Long.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Float.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Float.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Double.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Double.TYPE), singletonArray(Double.TYPE));
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testConstructor
    public void testConstructor() throws Exception {
        assertNotNull(MethodUtils.class.newInstance());
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testInvokeMethod
    public void testInvokeMethod() throws Exception {
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                (Class[]) null));
        assertEquals("foo(String)", MethodUtils.invokeMethod(testBean, "foo",
                ""));
        assertEquals("foo(Object)", MethodUtils.invokeMethod(testBean, "foo",
                new Object()));
        assertEquals("foo(Object)", MethodUtils.invokeMethod(testBean, "foo",
                Boolean.TRUE));
        assertEquals("foo(Integer)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.INTEGER_ONE));
        assertEquals("foo(int)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.BYTE_ONE));
        assertEquals("foo(double)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.LONG_ONE));
        assertEquals("foo(double)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.DOUBLE_ONE));
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testInvokeExactMethod
    public void testInvokeExactMethod() throws Exception {
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                (Class[]) null));
        assertEquals("foo(String)", MethodUtils.invokeExactMethod(testBean,
                "foo", ""));
        assertEquals("foo(Object)", MethodUtils.invokeExactMethod(testBean,
                "foo", new Object()));
        assertEquals("foo(Integer)", MethodUtils.invokeExactMethod(testBean,
                "foo", NumberUtils.INTEGER_ONE));
        assertEquals("foo(double)", MethodUtils.invokeExactMethod(testBean,
                "foo", new Object[] { NumberUtils.DOUBLE_ONE },
                new Class[] { Double.TYPE }));

        try {
            MethodUtils
                    .invokeExactMethod(testBean, "foo", NumberUtils.BYTE_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            MethodUtils
                    .invokeExactMethod(testBean, "foo", NumberUtils.LONG_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            MethodUtils.invokeExactMethod(testBean, "foo", Boolean.TRUE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testInvokeStaticMethod
    public void testInvokeStaticMethod() throws Exception {
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", (Class[]) null));
        assertEquals("bar(String)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", ""));
        assertEquals("bar(Object)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", new Object()));
        assertEquals("bar(Object)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", Boolean.TRUE));
        assertEquals("bar(Integer)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", NumberUtils.INTEGER_ONE));
        assertEquals("bar(int)", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", NumberUtils.BYTE_ONE));
        assertEquals("bar(double)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", NumberUtils.LONG_ONE));
        assertEquals("bar(double)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", NumberUtils.DOUBLE_ONE));
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testInvokeExactStaticMethod
    public void testInvokeExactStaticMethod() throws Exception {
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", (Class[]) null));
        assertEquals("bar(String)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", ""));
        assertEquals("bar(Object)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", new Object()));
        assertEquals("bar(Integer)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", NumberUtils.INTEGER_ONE));
        assertEquals("bar(double)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", new Object[] { NumberUtils.DOUBLE_ONE },
                new Class[] { Double.TYPE }));

        try {
            MethodUtils.invokeExactStaticMethod(TestBean.class, "bar",
                    NumberUtils.BYTE_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            MethodUtils.invokeExactStaticMethod(TestBean.class, "bar",
                    NumberUtils.LONG_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            MethodUtils.invokeExactStaticMethod(TestBean.class, "bar",
                    Boolean.TRUE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testGetAccessibleInterfaceMethod
    public void testGetAccessibleInterfaceMethod() throws Exception {

        Class<?>[][] p = { ArrayUtils.EMPTY_CLASS_ARRAY, null };
        for (int i = 0; i < p.length; i++) {
            Method method = TestMutable.class.getMethod("getValue", p[i]);
            Method accessibleMethod = MethodUtils.getAccessibleMethod(method);
            assertNotSame(accessibleMethod, method);
            assertSame(Mutable.class, accessibleMethod.getDeclaringClass());
        }
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testGetAccessibleInterfaceMethodFromDescription
    public void testGetAccessibleInterfaceMethodFromDescription()
            throws Exception {
        Class<?>[][] p = { ArrayUtils.EMPTY_CLASS_ARRAY, null };
        for (int i = 0; i < p.length; i++) {
            Method accessibleMethod = MethodUtils.getAccessibleMethod(
                    TestMutable.class, "getValue", p[i]);
            assertSame(Mutable.class, accessibleMethod.getDeclaringClass());
        }
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testGetAccessiblePublicMethod
    public void testGetAccessiblePublicMethod() throws Exception {
        assertSame(MutableObject.class, MethodUtils.getAccessibleMethod(
                MutableObject.class.getMethod("getValue",
                        ArrayUtils.EMPTY_CLASS_ARRAY)).getDeclaringClass());
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testGetAccessiblePublicMethodFromDescription
    public void testGetAccessiblePublicMethodFromDescription() throws Exception {
        assertSame(MutableObject.class, MethodUtils.getAccessibleMethod(
                MutableObject.class, "getValue", ArrayUtils.EMPTY_CLASS_ARRAY)
                .getDeclaringClass());
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testGetMatchingAccessibleMethod
    public void testGetMatchingAccessibleMethod() throws Exception {
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                ArrayUtils.EMPTY_CLASS_ARRAY, ArrayUtils.EMPTY_CLASS_ARRAY);
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                null, ArrayUtils.EMPTY_CLASS_ARRAY);
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(String.class), singletonArray(String.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Object.class), singletonArray(Object.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Boolean.class), singletonArray(Object.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Byte.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Byte.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Short.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Short.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Character.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Character.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Integer.class), singletonArray(Integer.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Integer.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Long.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Long.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Float.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Float.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Double.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Double.TYPE), singletonArray(Double.TYPE));
    }
