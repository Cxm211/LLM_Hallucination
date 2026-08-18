// buggy code
    public Fraction parse(String source, ParsePosition pos) {
        // try to parse improper fraction
        Fraction ret = super.parse(source, pos);
        if (ret != null) {
            return ret;
        }
        
        int initialIndex = pos.getIndex();

        // parse whitespace
        parseAndIgnoreWhitespace(source, pos);

        // parse whole
        Number whole = getWholeFormat().parse(source, pos);
        if (whole == null) {
            // invalid integer number
            // set index back to initial, error index should already be set
            // character examined.
            pos.setIndex(initialIndex);
            return null;
        }

        // parse whitespace
        parseAndIgnoreWhitespace(source, pos);
        
        // parse numerator
        Number num = getNumeratorFormat().parse(source, pos);
        if (num == null) {
            // invalid integer number
            // set index back to initial, error index should already be set
            // character examined.
            pos.setIndex(initialIndex);
            return null;
        }
        
            // minus signs should be leading, invalid expression

        // parse '/'
        int startIndex = pos.getIndex();
        char c = parseNextCharacter(source, pos);
        switch (c) {
        case 0 :
            // no '/'
            // return num as a fraction
            return new Fraction(num.intValue(), 1);
        case '/' :
            // found '/', continue parsing denominator
            break;
        default :
            // invalid '/'
            // set index back to initial, error index should be the last
            // character examined.
            pos.setIndex(initialIndex);
            pos.setErrorIndex(startIndex);
            return null;
        }

        // parse whitespace
        parseAndIgnoreWhitespace(source, pos);

        // parse denominator
        Number den = getDenominatorFormat().parse(source, pos);
        if (den == null) {
            // invalid integer number
            // set index back to initial, error index should already be set
            // character examined.
            pos.setIndex(initialIndex);
            return null;
        }
        
            // minus signs must be leading, invalid

        int w = whole.intValue();
        int n = num.intValue();
        int d = den.intValue();
        return new Fraction(((Math.abs(w) * d) + n) * MathUtils.sign(w), d);
    }

// relevant test
// org.apache.commons.math.fraction.FractionFormatTest::testFormat
    public void testFormat() {
        Fraction c = new Fraction(1, 2);
        String expected = "1 / 2";
        
        String actual = properFormat.format(c); 
        assertEquals(expected, actual);

        actual = improperFormat.format(c);
        assertEquals(expected, actual);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testFormatNegative
    public void testFormatNegative() {
        Fraction c = new Fraction(-1, 2);
        String expected = "-1 / 2";

        String actual = properFormat.format(c); 
        assertEquals(expected, actual);

        actual = improperFormat.format(c); 
        assertEquals(expected, actual);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testFormatZero
    public void testFormatZero() {
        Fraction c = new Fraction(0, 1);
        String expected = "0 / 1";

        String actual = properFormat.format(c); 
        assertEquals(expected, actual);

        actual = improperFormat.format(c); 
        assertEquals(expected, actual);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testFormatImproper
    public void testFormatImproper() {
        Fraction c = new Fraction(5, 3);

        String actual = properFormat.format(c); 
        assertEquals("1 2 / 3", actual);

        actual = improperFormat.format(c); 
        assertEquals("5 / 3", actual);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testFormatImproperNegative
    public void testFormatImproperNegative() {
        Fraction c = new Fraction(-5, 3);

        String actual = properFormat.format(c); 
        assertEquals("-1 2 / 3", actual);

        actual = improperFormat.format(c); 
        assertEquals("-5 / 3", actual);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParse
    public void testParse() {
        String source = "1 / 2";

        try {
            Fraction c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(1, c.getNumerator());
            assertEquals(2, c.getDenominator());
            
            c = improperFormat.parse(source);
            assertNotNull(c);
            assertEquals(1, c.getNumerator());
            assertEquals(2, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseInteger
    public void testParseInteger() {
		String source = "10";
    	try {
            Fraction c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(10, c.getNumerator());
            assertEquals(1, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    	try {
            Fraction c = improperFormat.parse(source);
            assertNotNull(c);
            assertEquals(10, c.getNumerator());
            assertEquals(1, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseInvalid
    public void testParseInvalid() {
		String source = "a";
        String msg = "should not be able to parse '10 / a'.";
    	try {
            properFormat.parse(source);
			fail(msg);
        } catch (ParseException ex) {
        	
        }
    	try {
            improperFormat.parse(source);
			fail(msg);
        } catch (ParseException ex) {
        	
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseInvalidDenominator
    public void testParseInvalidDenominator() {
		String source = "10 / a";
        String msg = "should not be able to parse '10 / a'.";
    	try {
            properFormat.parse(source);
			fail(msg);
        } catch (ParseException ex) {
        	
        }
    	try {
            improperFormat.parse(source);
			fail(msg);
        } catch (ParseException ex) {
        	
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseNegative
    public void testParseNegative() {

        try {
            String source = "-1 / 2";
            Fraction c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(-1, c.getNumerator());
            assertEquals(2, c.getDenominator());
            
            c = improperFormat.parse(source);
            assertNotNull(c);
            assertEquals(-1, c.getNumerator());
            assertEquals(2, c.getDenominator());

            source = "1 / -2";
            c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(-1, c.getNumerator());
            assertEquals(2, c.getDenominator());
            
            c = improperFormat.parse(source);
            assertNotNull(c);
            assertEquals(-1, c.getNumerator());
            assertEquals(2, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseProper
    public void testParseProper() {
        String source = "1 2 / 3";

        try {
            Fraction c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(5, c.getNumerator());
            assertEquals(3, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
        
        try {
            improperFormat.parse(source);
            fail("invalid improper fraction.");
        } catch (ParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseProperNegative
    public void testParseProperNegative() {
        String source = "-1 2 / 3";
        try {
            Fraction c = properFormat.parse(source);
            assertNotNull(c);
            assertEquals(-5, c.getNumerator());
            assertEquals(3, c.getDenominator());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
        
        try {
            improperFormat.parse(source);
            fail("invalid improper fraction.");
        } catch (ParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testParseProperInvalidMinus
    public void testParseProperInvalidMinus() {
        String source = "2 -2 / 3";
        try {
            Fraction c = properFormat.parse(source);
            fail("invalid minus in improper fraction.");
        } catch (ParseException ex) {
            
        }
        source = "2 2 / -3";
        try {
            Fraction c = properFormat.parse(source);
            fail("invalid minus in improper fraction.");
        } catch (ParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.FractionFormatTest::testNumeratorFormat
    public void testNumeratorFormat() {
    	NumberFormat old = properFormat.getNumeratorFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
    	properFormat.setNumeratorFormat(nf);
    	assertEquals(nf, properFormat.getNumeratorFormat());
    	properFormat.setNumeratorFormat(old);

    	old = improperFormat.getNumeratorFormat();
        nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
    	improperFormat.setNumeratorFormat(nf);
    	assertEquals(nf, improperFormat.getNumeratorFormat());
    	improperFormat.setNumeratorFormat(old);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testDenominatorFormat
    public void testDenominatorFormat() {
    	NumberFormat old = properFormat.getDenominatorFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
    	properFormat.setDenominatorFormat(nf);
    	assertEquals(nf, properFormat.getDenominatorFormat());
    	properFormat.setDenominatorFormat(old);

    	old = improperFormat.getDenominatorFormat();
        nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
    	improperFormat.setDenominatorFormat(nf);
    	assertEquals(nf, improperFormat.getDenominatorFormat());
    	improperFormat.setDenominatorFormat(old);
    }

// org.apache.commons.math.fraction.FractionFormatTest::testWholeFormat
    public void testWholeFormat() {
    	ProperFractionFormat format = (ProperFractionFormat)properFormat;
    	
    	NumberFormat old = format.getWholeFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
    	format.setWholeFormat(nf);
    	assertEquals(nf, format.getWholeFormat());
    	format.setWholeFormat(old);
    }
