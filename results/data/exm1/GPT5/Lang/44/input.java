// buggy code
    public static Number createNumber(String val) throws NumberFormatException {
        if (val == null) {
            return null;
        }
        if (val.length() == 0) {
            throw new NumberFormatException("\"\" is not a valid number.");
        }
        if (val.startsWith("--")) {
            // this is protection for poorness in java.lang.BigDecimal.
            // it accepts this as a legal value, but it does not appear 
            // to be in specification of class. OS X Java parses it to 
            // a wrong value.
            return null;
        }
        if (val.startsWith("0x") || val.startsWith("-0x")) {
            return createInteger(val);
        }   
        char lastChar = val.charAt(val.length() - 1);
        String mant;
        String dec;
        String exp;
        int decPos = val.indexOf('.');
        int expPos = val.indexOf('e') + val.indexOf('E') + 1;

        if (decPos > -1) {

            if (expPos > -1) {
                if (expPos < decPos) {
                    throw new NumberFormatException(val + " is not a valid number.");
                }
                dec = val.substring(decPos + 1, expPos);
            } else {
                dec = val.substring(decPos + 1);
            }
            mant = val.substring(0, decPos);
        } else {
            if (expPos > -1) {
                mant = val.substring(0, expPos);
            } else {
                mant = val;
            }
            dec = null;
        }
        if (!Character.isDigit(lastChar)) {
            if (expPos > -1 && expPos < val.length() - 1) {
                exp = val.substring(expPos + 1, val.length() - 1);
            } else {
                exp = null;
            }
            //Requesting a specific type..
            String numeric = val.substring(0, val.length() - 1);
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
                    throw new NumberFormatException(val + " is not a valid number.");
                case 'f' :
                case 'F' :
                    try {
                        Float f = NumberUtils.createFloat(numeric);
                        if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                            //If it's too big for a float or the float value = 0 and the string
                            //has non-zeros in it, then float does not have the precision we want
                            return f;
                        }

                    } catch (NumberFormatException e) {
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
                        // empty catch
                    }
                    try {
                        return createBigDecimal(numeric);
                    } catch (NumberFormatException e) {
                        // empty catch
                    }
                    //Fall through
                default :
                    throw new NumberFormatException(val + " is not a valid number.");

            }
        } else {
            //User doesn't have a preference on the return type, so let's start
            //small and go from there...
            if (expPos > -1 && expPos < val.length() - 1) {
                exp = val.substring(expPos + 1, val.length());
            } else {
                exp = null;
            }
            if (dec == null && exp == null) {
                //Must be an int,long,bigint
                try {
                    return createInteger(val);
                } catch (NumberFormatException nfe) {
                    // empty catch
                }
                try {
                    return createLong(val);
                } catch (NumberFormatException nfe) {
                    // empty catch
                }
                return createBigInteger(val);

            } else {
                //Must be a float,double,BigDec
                boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
                try {
                    Float f = createFloat(val);
                    if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                        return f;
                    }
                } catch (NumberFormatException nfe) {
                    // empty catch
                }
                try {
                    Double d = createDouble(val);
                    if (!(d.isInfinite() || (d.doubleValue() == 0.0D && !allZeros))) {
                        return d;
                    }
                } catch (NumberFormatException nfe) {
                    // empty catch
                }

                return createBigDecimal(val);

            }

        }
    }

// relevant test
// org.apache.commons.lang.NumberUtilsTest::testStringToIntString
    public void testStringToIntString() {
        assertTrue("stringToInt(String) 1 failed", NumberUtils.stringToInt("12345") == 12345);
        assertTrue("stringToInt(String) 2 failed", NumberUtils.stringToInt("abc") == 0);
    }

// org.apache.commons.lang.NumberUtilsTest::testStringToIntStringI
    public void testStringToIntStringI() {
        assertTrue("stringToInt(String,int) 1 failed", NumberUtils.stringToInt("12345", 5) == 12345);
        assertTrue("stringToInt(String,int) 2 failed", NumberUtils.stringToInt("1234.5", 5) == 5);
    }

// org.apache.commons.lang.NumberUtilsTest::testCreateNumber
    public void testCreateNumber() {
        
        assertEquals("createNumber(String) 1 failed", new Float("1234.5"), NumberUtils.createNumber("1234.5"));
        assertEquals("createNumber(String) 2 failed", new Integer("12345"), NumberUtils.createNumber("12345"));
        assertEquals("createNumber(String) 3 failed", new Double("1234.5"), NumberUtils.createNumber("1234.5D"));
        assertEquals("createNumber(String) 4 failed", new Float("1234.5"), NumberUtils.createNumber("1234.5F"));
        assertEquals("createNumber(String) 5 failed", new Long(Integer.MAX_VALUE + 1L), NumberUtils.createNumber("" + (Integer.MAX_VALUE + 1L)));
        assertEquals("createNumber(String) 6 failed", new BigInteger(Long.MAX_VALUE + "0"), NumberUtils.createNumber(Long.MAX_VALUE + "0L"));
        assertEquals("createNumber(String) 7 failed", new Long(12345), NumberUtils.createNumber("12345L"));
        assertEquals("createNumber(String) 8 failed", new Float("-1234.5"), NumberUtils.createNumber("-1234.5"));
        assertEquals("createNumber(String) 9 failed", new Integer("-12345"), NumberUtils.createNumber("-12345"));
        assertTrue("createNumber(String) 10 failed", 0xFADE == NumberUtils.createNumber("0xFADE").intValue());
        assertTrue("createNumber(String) 11 failed", -0xFADE == NumberUtils.createNumber("-0xFADE").intValue());
        assertEquals("createNumber(String) 12 failed", new Double("1.1E200"), NumberUtils.createNumber("1.1E200"));
        assertEquals("createNumber(String) 13 failed", new Float("1.1E20"), NumberUtils.createNumber("1.1E20"));
        assertEquals("createNumber(String) 14 failed", new Double("-1.1E200"), NumberUtils.createNumber("-1.1E200"));
        assertEquals("createNumber(String) 15 failed", new Double("1.1E-200"), NumberUtils.createNumber("1.1E-200"));
        assertEquals("createNumber(String) 16 failed", new Double("1.1E-200"), NumberUtils.createNumber("1.1E-200"));

        
        if(SystemUtils.isJavaVersionAtLeast(1.3f)) { 
            assertEquals("createNumber(String) 15 failed", new BigDecimal("1.1E-700"), NumberUtils.createNumber("1.1E-700F"));
        }
        assertEquals(
            "createNumber(String) 16 failed",
            new Long("10" + Integer.MAX_VALUE),
            NumberUtils.createNumber("10" + Integer.MAX_VALUE + "L"));
        assertEquals(
            "createNumber(String) 17 failed",
            new Long("10" + Integer.MAX_VALUE),
            NumberUtils.createNumber("10" + Integer.MAX_VALUE));
        assertEquals(
            "createNumber(String) 18 failed",
            new BigInteger("10" + Long.MAX_VALUE),
            NumberUtils.createNumber("10" + Long.MAX_VALUE));

    }

// org.apache.commons.lang.NumberUtilsTest::testCreateFloat
    public void testCreateFloat() {
        assertEquals("createFloat(String) failed", new Float("1234.5"), NumberUtils.createFloat("1234.5"));
    }

// org.apache.commons.lang.NumberUtilsTest::testCreateDouble
    public void testCreateDouble() {
        assertEquals("createDouble(String) failed", new Double("1234.5"), NumberUtils.createDouble("1234.5"));
    }

// org.apache.commons.lang.NumberUtilsTest::testCreateInteger
    public void testCreateInteger() {
        assertEquals("createInteger(String) failed", new Integer("12345"), NumberUtils.createInteger("12345"));
    }

// org.apache.commons.lang.NumberUtilsTest::testCreateLong
    public void testCreateLong() {
        assertEquals("createInteger(String) failed", new Long("12345"), NumberUtils.createLong("12345"));
    }

// org.apache.commons.lang.NumberUtilsTest::testCreateBigInteger
    public void testCreateBigInteger() {
        assertEquals("createBigInteger(String) failed", new BigInteger("12345"), NumberUtils.createBigInteger("12345"));
    }

// org.apache.commons.lang.NumberUtilsTest::testCreateBigDecimal
    public void testCreateBigDecimal() {
        assertEquals("createBigDecimal(String) failed", new BigDecimal("1234.5"), NumberUtils.createBigDecimal("1234.5"));
    }

// org.apache.commons.lang.NumberUtilsTest::testMinimumLong
    public void testMinimumLong() {
        assertEquals("minimum(long,long,long) 1 failed", 12345L, NumberUtils.minimum(12345L, 12345L + 1L, 12345L + 2L));
        assertEquals("minimum(long,long,long) 2 failed", 12345L, NumberUtils.minimum(12345L + 1L, 12345L, 12345 + 2L));
        assertEquals("minimum(long,long,long) 3 failed", 12345L, NumberUtils.minimum(12345L + 1L, 12345L + 2L, 12345L));
        assertEquals("minimum(long,long,long) 4 failed", 12345L, NumberUtils.minimum(12345L + 1L, 12345L, 12345L));
        assertEquals("minimum(long,long,long) 5 failed", 12345L, NumberUtils.minimum(12345L, 12345L, 12345L));

    }

// org.apache.commons.lang.NumberUtilsTest::testMinimumInt
    public void testMinimumInt() {
        assertEquals("minimum(int,int,int) 1 failed", 12345, NumberUtils.minimum(12345, 12345 + 1, 12345 + 2));
        assertEquals("minimum(int,int,int) 2 failed", 12345, NumberUtils.minimum(12345 + 1, 12345, 12345 + 2));
        assertEquals("minimum(int,int,int) 3 failed", 12345, NumberUtils.minimum(12345 + 1, 12345 + 2, 12345));
        assertEquals("minimum(int,int,int) 4 failed", 12345, NumberUtils.minimum(12345 + 1, 12345, 12345));
        assertEquals("minimum(int,int,int) 5 failed", 12345, NumberUtils.minimum(12345, 12345, 12345));

    }

// org.apache.commons.lang.NumberUtilsTest::testMaximumLong
    public void testMaximumLong() {
        assertEquals("maximum(long,long,long) 1 failed", 12345L, NumberUtils.maximum(12345L, 12345L - 1L, 12345L - 2L));
        assertEquals("maximum(long,long,long) 2 failed", 12345L, NumberUtils.maximum(12345L - 1L, 12345L, 12345L - 2L));
        assertEquals("maximum(long,long,long) 3 failed", 12345L, NumberUtils.maximum(12345L - 1L, 12345L - 2L, 12345L));
        assertEquals("maximum(long,long,long) 4 failed", 12345L, NumberUtils.maximum(12345L - 1L, 12345L, 12345L));
        assertEquals("maximum(long,long,long) 5 failed", 12345L, NumberUtils.maximum(12345L, 12345L, 12345L));

    }

// org.apache.commons.lang.NumberUtilsTest::testMaximumInt
    public void testMaximumInt() {
        assertEquals("maximum(int,int,int) 1 failed", 12345, NumberUtils.maximum(12345, 12345 - 1, 12345 - 2));
        assertEquals("maximum(int,int,int) 2 failed", 12345, NumberUtils.maximum(12345 - 1, 12345, 12345 - 2));
        assertEquals("maximum(int,int,int) 3 failed", 12345, NumberUtils.maximum(12345 - 1, 12345 - 2, 12345));
        assertEquals("maximum(int,int,int) 4 failed", 12345, NumberUtils.maximum(12345 - 1, 12345, 12345));
        assertEquals("maximum(int,int,int) 5 failed", 12345, NumberUtils.maximum(12345, 12345, 12345));

    }

// org.apache.commons.lang.NumberUtilsTest::testCompareDouble
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

// org.apache.commons.lang.NumberUtilsTest::testCompareFloat
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

// org.apache.commons.lang.NumberUtilsTest::testIsDigits
    public void testIsDigits() {
        assertEquals("isDigits(null) failed", false, NumberUtils.isDigits(null));
        assertEquals("isDigits('') failed", false, NumberUtils.isDigits(""));
        assertEquals("isDigits(String) failed", true, NumberUtils.isDigits("12345"));
        assertEquals("isDigits(String) neg 1 failed", false, NumberUtils.isDigits("1234.5"));
        assertEquals("isDigits(String) neg 3 failed", false, NumberUtils.isDigits("1ab"));
        assertEquals("isDigits(String) neg 4 failed", false, NumberUtils.isDigits("abc"));
    }

// org.apache.commons.lang.NumberUtilsTest::testIsNumber
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

    }

// org.apache.commons.lang.NumberUtilsTest::testIsNumberInvalidInput
    public void testIsNumberInvalidInput() {
        String val = "0x";
        assertEquals("isNumber() with 0x wasn't false",  false, NumberUtils.isNumber(val));
        val = "0x3x3";
        assertEquals("isNumber() with 0x3x3 wasn't false",  false, NumberUtils.isNumber(val));
        val = "20EE-3";
        assertEquals("isNumber() with 20EE-3 wasn't false",  false, NumberUtils.isNumber(val));
        val = "2435q";
        assertEquals("isNumber() with 2435q wasn't false",  false, NumberUtils.isNumber(val));
        val = ".";
        assertEquals("isNumber() with . wasn't false",  false, NumberUtils.isNumber(val));

    }

// org.apache.commons.lang.NumberUtilsTest::testPublicNoArgConstructor
    public void testPublicNoArgConstructor() {
        try {
            NumberUtils nu = new NumberUtils();
        } catch( Exception e ) {
            fail( "Error calling public no-arg constructor" );
        }
    }

// org.apache.commons.lang.NumberUtilsTest::testLang457
    public void testLang457() {
        String[] badInputs = new String[] { "l", "L", "f", "F", "junk", "bobL"};
        for(int i=0; i<badInputs.length; i++) {
            try {
                NumberUtils.createNumber(badInputs[i]);
                fail("NumberFormatException was expected for " + badInputs[i]);
            } catch (NumberFormatException e) {
                return; 
            }
        }
    }
