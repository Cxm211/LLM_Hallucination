// buggy code
    public Complex parse(String source, ParsePosition pos) {
        int initialIndex = pos.getIndex();

        // parse whitespace
        parseAndIgnoreWhitespace(source, pos);

        // parse real
        Number re = parseNumber(source, getRealFormat(), pos);
        if (re == null) {
            // invalid real number
            // set index back to initial, error index should already be set
            // character examined.
            pos.setIndex(initialIndex);
            return null;
        }

        // parse sign
        int startIndex = pos.getIndex();
        char c = parseNextCharacter(source, pos);
        int sign = 0;
        switch (c) {
        case 0 :
            // no sign
            // return real only complex number
            return new Complex(re.doubleValue(), 0.0);
        case '-' :
            sign = -1;
            break;
        case '+' :
            sign = 1;
            break;
        default :
            // invalid sign
            // set index back to initial, error index should be the last
            // character examined.
            pos.setIndex(initialIndex);
            pos.setErrorIndex(startIndex);
            return null;
        }

        // parse whitespace
        parseAndIgnoreWhitespace(source, pos);

        // parse imaginary
        Number im = parseNumber(source, getRealFormat(), pos);
        if (im == null) {
            // invalid imaginary number
            // set index back to initial, error index should already be set
            // character examined.
            pos.setIndex(initialIndex);
            return null;
        }

        // parse imaginary character
        int n = getImaginaryCharacter().length();
        startIndex = pos.getIndex();
        int endIndex = startIndex + n;
        if (
            source.substring(startIndex, endIndex).compareTo(
            getImaginaryCharacter()) != 0) {
            // set index back to initial, error index should be the start index
            // character examined.
            pos.setIndex(initialIndex);
            pos.setErrorIndex(startIndex);
            return null;
        }
        pos.setIndex(endIndex);

        return new Complex(re.doubleValue(), im.doubleValue() * sign);
    }

// relevant test
// org.apache.commons.math.complex.ComplexFormatAbstractTest::testSimpleNoDecimals
    public void testSimpleNoDecimals() {
        Complex c = new Complex(1, 1);
        String expected = "1 + 1i";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testSimpleWithDecimals
    public void testSimpleWithDecimals() {
        Complex c = new Complex(1.23, 1.43);
        String expected = "1" + getDecimalCharacter() + "23 + 1" + getDecimalCharacter() + "43i";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testSimpleWithDecimalsTrunc
    public void testSimpleWithDecimalsTrunc() {
        Complex c = new Complex(1.2323, 1.4343);
        String expected = "1" + getDecimalCharacter() + "23 + 1" + getDecimalCharacter() + "43i";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testNegativeReal
    public void testNegativeReal() {
        Complex c = new Complex(-1.2323, 1.4343);
        String expected = "-1" + getDecimalCharacter() + "23 + 1" + getDecimalCharacter() + "43i";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testNegativeImaginary
    public void testNegativeImaginary() {
        Complex c = new Complex(1.2323, -1.4343);
        String expected = "1" + getDecimalCharacter() + "23 - 1" + getDecimalCharacter() + "43i";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testNegativeBoth
    public void testNegativeBoth() {
        Complex c = new Complex(-1.2323, -1.4343);
        String expected = "-1" + getDecimalCharacter() + "23 - 1" + getDecimalCharacter() + "43i";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testZeroReal
    public void testZeroReal() {
        Complex c = new Complex(0.0, -1.4343);
        String expected = "0 - 1" + getDecimalCharacter() + "43i";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testZeroImaginary
    public void testZeroImaginary() {
        Complex c = new Complex(30.233, 0);
        String expected = "30" + getDecimalCharacter() + "23";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testDifferentImaginaryChar
    public void testDifferentImaginaryChar() {
        Complex c = new Complex(1, 1);
        String expected = "1 + 1j";
        String actual = complexFormatJ.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testStaticFormatComplex
    public void testStaticFormatComplex() {
        Locale defaultLocal = Locale.getDefault();
        Locale.setDefault(getLocale());
        Complex c = new Complex(232.222, -342.33);
        String expected = "232" + getDecimalCharacter() + "22 - 342" + getDecimalCharacter() + "33i";
        String actual = ComplexFormat.formatComplex(c);
        assertEquals(expected, actual);
        Locale.setDefault(defaultLocal);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testNan
    public void testNan() {
        Complex c = new Complex(Double.NaN, Double.NaN);
        String expected = "(NaN) + (NaN)i";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testPositiveInfinity
    public void testPositiveInfinity() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        String expected = "(Infinity) + (Infinity)i";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testNegativeInfinity
    public void testNegativeInfinity() {
        Complex c = new Complex(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        String expected = "(-Infinity) - (Infinity)i";
        String actual = complexFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParseSimpleNoDecimals
    public void testParseSimpleNoDecimals() {
        String source = "1 + 1i";
        Complex expected = new Complex(1, 1);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParseSimpleWithDecimals
    public void testParseSimpleWithDecimals() {
        String source = "1" + getDecimalCharacter() + "23 + 1" + getDecimalCharacter() + "43i";
        Complex expected = new Complex(1.23, 1.43);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParseSimpleWithDecimalsTrunc
    public void testParseSimpleWithDecimalsTrunc() {
        String source = "1" + getDecimalCharacter() + "2323 + 1" + getDecimalCharacter() + "4343i";
        Complex expected = new Complex(1.2323, 1.4343);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParseNegativeReal
    public void testParseNegativeReal() {
        String source = "-1" + getDecimalCharacter() + "2323 + 1" + getDecimalCharacter() + "4343i";
        Complex expected = new Complex(-1.2323, 1.4343);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParseNegativeImaginary
    public void testParseNegativeImaginary() {
        String source = "1" + getDecimalCharacter() + "2323 - 1" + getDecimalCharacter() + "4343i";
        Complex expected = new Complex(1.2323, -1.4343);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParseNegativeBoth
    public void testParseNegativeBoth() {
        String source = "-1" + getDecimalCharacter() + "2323 - 1" + getDecimalCharacter() + "4343i";
        Complex expected = new Complex(-1.2323, -1.4343);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParseZeroReal
    public void testParseZeroReal() {
        String source = "0" + getDecimalCharacter() + "0 - 1" + getDecimalCharacter() + "4343i";
        Complex expected = new Complex(0.0, -1.4343);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParseZeroImaginary
    public void testParseZeroImaginary() {
        String source = "-1" + getDecimalCharacter() + "2323";
        Complex expected = new Complex(-1.2323, 0);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParseDifferentImaginaryChar
    public void testParseDifferentImaginaryChar() {
        String source = "-1" + getDecimalCharacter() + "2323 - 1" + getDecimalCharacter() + "4343j";
        Complex expected = new Complex(-1.2323, -1.4343);
        try {
            Complex actual = (Complex)complexFormatJ.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParseNan
    public void testParseNan() {
        String source = "(NaN) + (NaN)i";
        Complex expected = new Complex(Double.NaN, Double.NaN);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testParsePositiveInfinity
    public void testParsePositiveInfinity() {
        String source = "(Infinity) + (Infinity)i";
        Complex expected = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testPaseNegativeInfinity
    public void testPaseNegativeInfinity() {
        String source = "(-Infinity) - (Infinity)i";
        Complex expected = new Complex(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        try {
            Complex actual = (Complex)complexFormat.parseObject(source);
            assertEquals(expected, actual);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testConstructorSingleFormat
    public void testConstructorSingleFormat() {
        NumberFormat nf = NumberFormat.getInstance();
        ComplexFormat cf = new ComplexFormat(nf);
        assertNotNull(cf);
        assertEquals(nf, cf.getRealFormat());
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testGetImaginaryFormat
    public void testGetImaginaryFormat() {
        NumberFormat nf = NumberFormat.getInstance();
        ComplexFormat cf = new ComplexFormat();
        assertNotSame(nf, cf.getImaginaryFormat());
        cf.setImaginaryFormat(nf);
        assertSame(nf, cf.getImaginaryFormat());
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testSetImaginaryFormatNull
    public void testSetImaginaryFormatNull() {
        try {
            ComplexFormat cf = new ComplexFormat();
            cf.setImaginaryFormat(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testSetRealFormatNull
    public void testSetRealFormatNull() {
        try {
            ComplexFormat cf = new ComplexFormat();
            cf.setRealFormat(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testGetRealFormat
    public void testGetRealFormat() {
        NumberFormat nf = NumberFormat.getInstance();
        ComplexFormat cf = new ComplexFormat();
        assertNotSame(nf, cf.getRealFormat());
        cf.setRealFormat(nf);
        assertSame(nf, cf.getRealFormat());
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testSetImaginaryCharacterNull
    public void testSetImaginaryCharacterNull() {
        try {
            ComplexFormat cf = new ComplexFormat();
            cf.setImaginaryCharacter(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testSetImaginaryCharacterEmpty
    public void testSetImaginaryCharacterEmpty() {
        try {
            ComplexFormat cf = new ComplexFormat();
            cf.setImaginaryCharacter("");
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testFormatNumber
    public void testFormatNumber() {
        ComplexFormat cf = ComplexFormat.getInstance(getLocale());
        Double pi = new Double(Math.PI);
        String text = cf.format(pi);
        assertEquals("3" + getDecimalCharacter() + "14", text);
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testFormatObject
    public void testFormatObject() {
        try {
            ComplexFormat cf = new ComplexFormat();
            Object object = new Object();
            cf.format(object);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

// org.apache.commons.math.complex.ComplexFormatAbstractTest::testForgottenImaginaryCharacter
    public void testForgottenImaginaryCharacter() {
        ParsePosition pos = new ParsePosition(0);
        assertNull(new ComplexFormat().parse("1 + 1", pos));
        assertEquals(5, pos.getErrorIndex());
    }
