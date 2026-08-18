// buggy code
    public int compareTo(Fraction object) {
        double nOd = doubleValue();
        double dOn = object.doubleValue();
        return (nOd < dOn) ? -1 : ((nOd > dOn) ? +1 : 0);
    }

// relevant test
// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstChebyshevPolynomials
    public void testFirstChebyshevPolynomials() {

        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(3), "-3.0 x + 4.0 x^3");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(2), "-1.0 + 2.0 x^2");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(1), "x");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(0), "1.0");

        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(7), "-7.0 x + 56.0 x^3 - 112.0 x^5 + 64.0 x^7");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(6), "-1.0 + 18.0 x^2 - 48.0 x^4 + 32.0 x^6");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(5), "5.0 x - 20.0 x^3 + 16.0 x^5");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(4), "1.0 - 8.0 x^2 + 8.0 x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testChebyshevBounds
    public void testChebyshevBounds() {
        for (int k = 0; k < 12; ++k) {
            PolynomialFunction Tk = PolynomialsUtils.createChebyshevPolynomial(k);
            for (double x = -1.0; x <= 1.0; x += 0.02) {
                assertTrue(k + " " + Tk.value(x), Math.abs(Tk.value(x)) < (1.0 + 1.0e-12));
            }
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testChebyshevDifferentials
    public void testChebyshevDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Tk0 = PolynomialsUtils.createChebyshevPolynomial(k);
            PolynomialFunction Tk1 = Tk0.polynomialDerivative();
            PolynomialFunction Tk2 = Tk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k * k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -1});
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1, 0, -1 });

            PolynomialFunction Tk0g0 = Tk0.multiply(g0);
            PolynomialFunction Tk1g1 = Tk1.multiply(g1);
            PolynomialFunction Tk2g2 = Tk2.multiply(g2);

            checkNullPolynomial(Tk0g0.add(Tk1g1.add(Tk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstHermitePolynomials
    public void testFirstHermitePolynomials() {

        checkPolynomial(PolynomialsUtils.createHermitePolynomial(3), "-12.0 x + 8.0 x^3");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(2), "-2.0 + 4.0 x^2");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(1), "2.0 x");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(0), "1.0");

        checkPolynomial(PolynomialsUtils.createHermitePolynomial(7), "-1680.0 x + 3360.0 x^3 - 1344.0 x^5 + 128.0 x^7");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(6), "-120.0 + 720.0 x^2 - 480.0 x^4 + 64.0 x^6");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(5), "120.0 x - 160.0 x^3 + 32.0 x^5");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(4), "12.0 - 48.0 x^2 + 16.0 x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testHermiteDifferentials
    public void testHermiteDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Hk0 = PolynomialsUtils.createHermitePolynomial(k);
            PolynomialFunction Hk1 = Hk0.polynomialDerivative();
            PolynomialFunction Hk2 = Hk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { 2 * k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -2 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1 });

            PolynomialFunction Hk0g0 = Hk0.multiply(g0);
            PolynomialFunction Hk1g1 = Hk1.multiply(g1);
            PolynomialFunction Hk2g2 = Hk2.multiply(g2);

            checkNullPolynomial(Hk0g0.add(Hk1g1.add(Hk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstLaguerrePolynomials
    public void testFirstLaguerrePolynomials() {

        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(3), 6l, "6.0 - 18.0 x + 9.0 x^2 - x^3");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(2), 2l, "2.0 - 4.0 x + x^2");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(1), 1l, "1.0 - x");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(0), 1l, "1.0");

        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(7), 5040l,
                "5040.0 - 35280.0 x + 52920.0 x^2 - 29400.0 x^3"
                + " + 7350.0 x^4 - 882.0 x^5 + 49.0 x^6 - x^7");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(6),  720l,
                "720.0 - 4320.0 x + 5400.0 x^2 - 2400.0 x^3 + 450.0 x^4"
                + " - 36.0 x^5 + x^6");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(5),  120l,
        "120.0 - 600.0 x + 600.0 x^2 - 200.0 x^3 + 25.0 x^4 - x^5");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(4),   24l,
        "24.0 - 96.0 x + 72.0 x^2 - 16.0 x^3 + x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testLaguerreDifferentials
    public void testLaguerreDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Lk0 = PolynomialsUtils.createLaguerrePolynomial(k);
            PolynomialFunction Lk1 = Lk0.polynomialDerivative();
            PolynomialFunction Lk2 = Lk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 1, -1 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 0, 1 });

            PolynomialFunction Lk0g0 = Lk0.multiply(g0);
            PolynomialFunction Lk1g1 = Lk1.multiply(g1);
            PolynomialFunction Lk2g2 = Lk2.multiply(g2);

            checkNullPolynomial(Lk0g0.add(Lk1g1.add(Lk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstLegendrePolynomials
    public void testFirstLegendrePolynomials() {

        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(3),  2l, "-3.0 x + 5.0 x^3");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(2),  2l, "-1.0 + 3.0 x^2");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(1),  1l, "x");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(0),  1l, "1.0");

        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(7), 16l, "-35.0 x + 315.0 x^3 - 693.0 x^5 + 429.0 x^7");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(6), 16l, "-5.0 + 105.0 x^2 - 315.0 x^4 + 231.0 x^6");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(5),  8l, "15.0 x - 70.0 x^3 + 63.0 x^5");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(4),  8l, "3.0 - 30.0 x^2 + 35.0 x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testLegendreDifferentials
    public void testLegendreDifferentials() {
        for (int k = 0; k < 12; ++k) {

            PolynomialFunction Pk0 = PolynomialsUtils.createLegendrePolynomial(k);
            PolynomialFunction Pk1 = Pk0.polynomialDerivative();
            PolynomialFunction Pk2 = Pk1.polynomialDerivative();

            PolynomialFunction g0 = new PolynomialFunction(new double[] { k * (k + 1) });
            PolynomialFunction g1 = new PolynomialFunction(new double[] { 0, -2 });
            PolynomialFunction g2 = new PolynomialFunction(new double[] { 1, 0, -1 });

            PolynomialFunction Pk0g0 = Pk0.multiply(g0);
            PolynomialFunction Pk1g1 = Pk1.multiply(g1);
            PolynomialFunction Pk2g2 = Pk2.multiply(g2);

            checkNullPolynomial(Pk0g0.add(Pk1g1.add(Pk2g2)));

        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testHighDegreeLegendre
    public void testHighDegreeLegendre() {
        try {
            PolynomialsUtils.createLegendrePolynomial(40);
            fail("an exception should have been thrown");
        } catch (ArithmeticException ae) {
            
        }

    }

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
            properFormat.parse(source);
            fail("invalid minus in improper fraction.");
        } catch (ParseException ex) {
            
        }
        source = "2 2 / -3";
        try {
            properFormat.parse(source);
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

// org.apache.commons.math.fraction.FractionFormatTest::testLongFormat
    public void testLongFormat() {
        assertEquals("10 / 1", improperFormat.format(10l));
    }

// org.apache.commons.math.fraction.FractionFormatTest::testDoubleFormat
    public void testDoubleFormat() {
        assertEquals("355 / 113", improperFormat.format(Math.PI));
    }

// org.apache.commons.math.fraction.FractionTest::testConstructor
    public void testConstructor() {
        assertFraction(0, 1, new Fraction(0, 1));
        assertFraction(0, 1, new Fraction(0, 2));
        assertFraction(0, 1, new Fraction(0, -1));
        assertFraction(1, 2, new Fraction(1, 2));
        assertFraction(1, 2, new Fraction(2, 4));
        assertFraction(-1, 2, new Fraction(-1, 2));
        assertFraction(-1, 2, new Fraction(1, -2));
        assertFraction(-1, 2, new Fraction(-2, 4));
        assertFraction(-1, 2, new Fraction(2, -4));
        
        
        try {
            new Fraction(Integer.MIN_VALUE, -1);
            fail();
        } catch (ArithmeticException ex) {
            
        }
        try {
            new Fraction(1, Integer.MIN_VALUE);
            fail();
        } catch (ArithmeticException ex) {
            
        }
        try {        
            assertFraction(0, 1, new Fraction(0.00000000000001));
            assertFraction(2, 5, new Fraction(0.40000000000001));
            assertFraction(15, 1, new Fraction(15.0000000000001));
            
        } catch (ConvergenceException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.fraction.FractionTest::testGoldenRatio
    public void testGoldenRatio() {
        try {
            
            new Fraction((1 + Math.sqrt(5)) / 2, 1.0e-12, 25);
            fail("an exception should have been thrown");
        } catch (ConvergenceException ce) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.fraction.FractionTest::testDoubleConstructor
    public void testDoubleConstructor() throws ConvergenceException  {
        assertFraction(1, 2, new Fraction((double)1 / (double)2));
        assertFraction(1, 3, new Fraction((double)1 / (double)3));
        assertFraction(2, 3, new Fraction((double)2 / (double)3));
        assertFraction(1, 4, new Fraction((double)1 / (double)4));
        assertFraction(3, 4, new Fraction((double)3 / (double)4));
        assertFraction(1, 5, new Fraction((double)1 / (double)5));
        assertFraction(2, 5, new Fraction((double)2 / (double)5));
        assertFraction(3, 5, new Fraction((double)3 / (double)5));
        assertFraction(4, 5, new Fraction((double)4 / (double)5));
        assertFraction(1, 6, new Fraction((double)1 / (double)6));
        assertFraction(5, 6, new Fraction((double)5 / (double)6));
        assertFraction(1, 7, new Fraction((double)1 / (double)7));
        assertFraction(2, 7, new Fraction((double)2 / (double)7));
        assertFraction(3, 7, new Fraction((double)3 / (double)7));
        assertFraction(4, 7, new Fraction((double)4 / (double)7));
        assertFraction(5, 7, new Fraction((double)5 / (double)7));
        assertFraction(6, 7, new Fraction((double)6 / (double)7));
        assertFraction(1, 8, new Fraction((double)1 / (double)8));
        assertFraction(3, 8, new Fraction((double)3 / (double)8));
        assertFraction(5, 8, new Fraction((double)5 / (double)8));
        assertFraction(7, 8, new Fraction((double)7 / (double)8));
        assertFraction(1, 9, new Fraction((double)1 / (double)9));
        assertFraction(2, 9, new Fraction((double)2 / (double)9));
        assertFraction(4, 9, new Fraction((double)4 / (double)9));
        assertFraction(5, 9, new Fraction((double)5 / (double)9));
        assertFraction(7, 9, new Fraction((double)7 / (double)9));
        assertFraction(8, 9, new Fraction((double)8 / (double)9));
        assertFraction(1, 10, new Fraction((double)1 / (double)10));
        assertFraction(3, 10, new Fraction((double)3 / (double)10));
        assertFraction(7, 10, new Fraction((double)7 / (double)10));
        assertFraction(9, 10, new Fraction((double)9 / (double)10));
        assertFraction(1, 11, new Fraction((double)1 / (double)11));
        assertFraction(2, 11, new Fraction((double)2 / (double)11));
        assertFraction(3, 11, new Fraction((double)3 / (double)11));
        assertFraction(4, 11, new Fraction((double)4 / (double)11));
        assertFraction(5, 11, new Fraction((double)5 / (double)11));
        assertFraction(6, 11, new Fraction((double)6 / (double)11));
        assertFraction(7, 11, new Fraction((double)7 / (double)11));
        assertFraction(8, 11, new Fraction((double)8 / (double)11));
        assertFraction(9, 11, new Fraction((double)9 / (double)11));
        assertFraction(10, 11, new Fraction((double)10 / (double)11));
    }

// org.apache.commons.math.fraction.FractionTest::testDigitLimitConstructor
    public void testDigitLimitConstructor() throws ConvergenceException  {
        assertFraction(2, 5, new Fraction(0.4,   9));
        assertFraction(2, 5, new Fraction(0.4,  99));
        assertFraction(2, 5, new Fraction(0.4, 999));

        assertFraction(3, 5,      new Fraction(0.6152,    9));
        assertFraction(8, 13,     new Fraction(0.6152,   99));
        assertFraction(510, 829,  new Fraction(0.6152,  999));
        assertFraction(769, 1250, new Fraction(0.6152, 9999));
    }

// org.apache.commons.math.fraction.FractionTest::testIntegerOverflow
    public void testIntegerOverflow() {
        checkIntegerOverflow(0.75000000001455192);
        checkIntegerOverflow(1.0e10);
    }

// org.apache.commons.math.fraction.FractionTest::testEpsilonLimitConstructor
    public void testEpsilonLimitConstructor() throws ConvergenceException  {
        assertFraction(2, 5, new Fraction(0.4, 1.0e-5, 100));

        assertFraction(3, 5,      new Fraction(0.6152, 0.02, 100));
        assertFraction(8, 13,     new Fraction(0.6152, 1.0e-3, 100));
        assertFraction(251, 408,  new Fraction(0.6152, 1.0e-4, 100));
        assertFraction(251, 408,  new Fraction(0.6152, 1.0e-5, 100));
        assertFraction(510, 829,  new Fraction(0.6152, 1.0e-6, 100));
        assertFraction(769, 1250, new Fraction(0.6152, 1.0e-7, 100));
    }

// org.apache.commons.math.fraction.FractionTest::testCompareTo
    public void testCompareTo() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);
        Fraction third = new Fraction(1, 2);
        
        assertEquals(0, first.compareTo(first));
        assertEquals(0, first.compareTo(third));
        assertEquals(1, first.compareTo(second));
        assertEquals(-1, second.compareTo(first));

        
        
        
        Fraction pi1 = new Fraction(1068966896, 340262731);
        Fraction pi2 = new Fraction( 411557987, 131002976);
        assertEquals(-1, pi1.compareTo(pi2));
        assertEquals( 1, pi2.compareTo(pi1));
        assertEquals(0.0, pi1.doubleValue() - pi2.doubleValue(), 1.0e-20);
    }

// org.apache.commons.math.fraction.FractionTest::testDoubleValue
    public void testDoubleValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);

        assertEquals(0.5, first.doubleValue(), 0.0);
        assertEquals(1.0 / 3.0, second.doubleValue(), 0.0);
    }

// org.apache.commons.math.fraction.FractionTest::testFloatValue
    public void testFloatValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);

        assertEquals(0.5f, first.floatValue(), 0.0f);
        assertEquals((float)(1.0 / 3.0), second.floatValue(), 0.0f);
    }

// org.apache.commons.math.fraction.FractionTest::testIntValue
    public void testIntValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(3, 2);

        assertEquals(0, first.intValue());
        assertEquals(1, second.intValue());
    }

// org.apache.commons.math.fraction.FractionTest::testLongValue
    public void testLongValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(3, 2);

        assertEquals(0L, first.longValue());
        assertEquals(1L, second.longValue());
    }

// org.apache.commons.math.fraction.FractionTest::testConstructorDouble
    public void testConstructorDouble() {
        try {
            assertFraction(1, 2, new Fraction(0.5));
            assertFraction(1, 3, new Fraction(1.0 / 3.0));
            assertFraction(17, 100, new Fraction(17.0 / 100.0));
            assertFraction(317, 100, new Fraction(317.0 / 100.0));
            assertFraction(-1, 2, new Fraction(-0.5));
            assertFraction(-1, 3, new Fraction(-1.0 / 3.0));
            assertFraction(-17, 100, new Fraction(17.0 / -100.0));
            assertFraction(-317, 100, new Fraction(-317.0 / 100.0));
        } catch (ConvergenceException ex) {
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.fraction.FractionTest::testAbs
    public void testAbs() {
        Fraction a = new Fraction(10, 21);
        Fraction b = new Fraction(-10, 21);
        Fraction c = new Fraction(10, -21);
        
        assertFraction(10, 21, a.abs());
        assertFraction(10, 21, b.abs());
        assertFraction(10, 21, c.abs());
    }

// org.apache.commons.math.fraction.FractionTest::testReciprocal
    public void testReciprocal() {
        Fraction f = null;
        
        f = new Fraction(50, 75);
        f = f.reciprocal();
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());
        
        f = new Fraction(4, 3);
        f = f.reciprocal();
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
        
        f = new Fraction(-15, 47);
        f = f.reciprocal();
        assertEquals(-47, f.getNumerator());
        assertEquals(15, f.getDenominator());
        
        f = new Fraction(0, 3);
        try {
            f = f.reciprocal();
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}

        
        f = new Fraction(Integer.MAX_VALUE, 1);
        f = f.reciprocal();
        assertEquals(1, f.getNumerator());
        assertEquals(Integer.MAX_VALUE, f.getDenominator());
    }

// org.apache.commons.math.fraction.FractionTest::testNegate
    public void testNegate() {
        Fraction f = null;
        
        f = new Fraction(50, 75);
        f = f.negate();
        assertEquals(-2, f.getNumerator());
        assertEquals(3, f.getDenominator());
        
        f = new Fraction(-50, 75);
        f = f.negate();
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());

        
        f = new Fraction(Integer.MAX_VALUE-1, Integer.MAX_VALUE);
        f = f.negate();
        assertEquals(Integer.MIN_VALUE+2, f.getNumerator());
        assertEquals(Integer.MAX_VALUE, f.getDenominator());

        f = new Fraction(Integer.MIN_VALUE, 1);
        try {
            f = f.negate();
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}
    }

// org.apache.commons.math.fraction.FractionTest::testAdd
    public void testAdd() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);
        
        assertFraction(1, 1, a.add(a));
        assertFraction(7, 6, a.add(b));
        assertFraction(7, 6, b.add(a));
        assertFraction(4, 3, b.add(b));
        
        Fraction f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        Fraction f2 = Fraction.ONE;
        Fraction f = f1.add(f2);
        assertEquals(Integer.MAX_VALUE, f.getNumerator());
        assertEquals(1, f.getDenominator());
        
        f1 = new Fraction(-1, 13*13*2*2);
        f2 = new Fraction(-2, 13*17*2);
        f = f1.add(f2);
        assertEquals(13*13*17*2*2, f.getDenominator());
        assertEquals(-17 - 2*13*2, f.getNumerator());
        
        try {
            f.add(null);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}
        
        
        
        f1 = new Fraction(1,32768*3);
        f2 = new Fraction(1,59049);
        f = f1.add(f2);
        assertEquals(52451, f.getNumerator());
        assertEquals(1934917632, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, 3);
        f2 = new Fraction(1,3);
        f = f1.add(f2);
        assertEquals(Integer.MIN_VALUE+1, f.getNumerator());
        assertEquals(3, f.getDenominator());
        
        f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        f2 = Fraction.ONE;
        f = f1.add(f2);
        assertEquals(Integer.MAX_VALUE, f.getNumerator());
        assertEquals(1, f.getDenominator());
        
        try {
            f = f.add(Fraction.ONE); 
            fail("expecting ArithmeticException but got: " + f.toString());
        } catch (ArithmeticException ex) {}
        
        
        f1 = new Fraction(Integer.MIN_VALUE, 5);
        f2 = new Fraction(-1,5);
        try {
            f = f1.add(f2); 
            fail("expecting ArithmeticException but got: " + f.toString());
        } catch (ArithmeticException ex) {}
        
        try {
            f= new Fraction(-Integer.MAX_VALUE, 1);
            f = f.add(f);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}
        
        try {
            f= new Fraction(-Integer.MAX_VALUE, 1);
            f = f.add(f);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}
        
        f1 = new Fraction(3,327680);
        f2 = new Fraction(2,59049);
        try {
            f = f1.add(f2); 
            fail("expecting ArithmeticException but got: " + f.toString());
        } catch (ArithmeticException ex) {}
    }

// org.apache.commons.math.fraction.FractionTest::testDivide
    public void testDivide() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);
        
        assertFraction(1, 1, a.divide(a));
        assertFraction(3, 4, a.divide(b));
        assertFraction(4, 3, b.divide(a));
        assertFraction(1, 1, b.divide(b));
        
        Fraction f1 = new Fraction(3, 5);
        Fraction f2 = Fraction.ZERO;
        try {
            f1.divide(f2);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}
        
        f1 = new Fraction(0, 5);
        f2 = new Fraction(2, 7);
        Fraction f = f1.divide(f2);
        assertSame(Fraction.ZERO, f);
        
        f1 = new Fraction(2, 7);
        f2 = Fraction.ONE;
        f = f1.divide(f2);
        assertEquals(2, f.getNumerator());
        assertEquals(7, f.getDenominator());
        
        f1 = new Fraction(1, Integer.MAX_VALUE);
        f = f1.divide(f1);  
        assertEquals(1, f.getNumerator());
        assertEquals(1, f.getDenominator());
        
        f1 = new Fraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f2 = new Fraction(1, Integer.MAX_VALUE);
        f = f1.divide(f2);
        assertEquals(Integer.MIN_VALUE, f.getNumerator());
        assertEquals(1, f.getDenominator());

        try {
            f.divide(null);
            fail("IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}
        
        try {
            f1 = new Fraction(1, Integer.MAX_VALUE);
            f = f1.divide(f1.reciprocal());  
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}
        try {
            f1 = new Fraction(1, -Integer.MAX_VALUE);
            f = f1.divide(f1.reciprocal());  
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}
    }

// org.apache.commons.math.fraction.FractionTest::testMultiply
    public void testMultiply() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);
        
        assertFraction(1, 4, a.multiply(a));
        assertFraction(1, 3, a.multiply(b));
        assertFraction(1, 3, b.multiply(a));
        assertFraction(4, 9, b.multiply(b));
        
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        Fraction f = f1.multiply(f2);
        assertEquals(Integer.MIN_VALUE, f.getNumerator());
        assertEquals(1, f.getDenominator());

        try {
            f.multiply(null);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.math.fraction.FractionTest::testSubtract
    public void testSubtract() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);
        
        assertFraction(0, 1, a.subtract(a));
        assertFraction(-1, 6, a.subtract(b));
        assertFraction(1, 6, b.subtract(a));
        assertFraction(0, 1, b.subtract(b));
        
        Fraction f = new Fraction(1,1);
        try {
            f.subtract(null);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}
        
        
        
        Fraction f1 = new Fraction(1,32768*3);
        Fraction f2 = new Fraction(1,59049);
        f = f1.subtract(f2);
        assertEquals(-13085, f.getNumerator());
        assertEquals(1934917632, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, 3);
        f2 = new Fraction(1,3).negate();
        f = f1.subtract(f2);
        assertEquals(Integer.MIN_VALUE+1, f.getNumerator());
        assertEquals(3, f.getDenominator());
        
        f1 = new Fraction(Integer.MAX_VALUE, 1);
        f2 = Fraction.ONE;
        f = f1.subtract(f2);
        assertEquals(Integer.MAX_VALUE-1, f.getNumerator());
        assertEquals(1, f.getDenominator());

        try {
            f1 = new Fraction(1, Integer.MAX_VALUE);
            f2 = new Fraction(1, Integer.MAX_VALUE - 1);
            f = f1.subtract(f2);
            fail("expecting ArithmeticException");  
        } catch (ArithmeticException ex) {}
        
        
        f1 = new Fraction(Integer.MIN_VALUE, 5);
        f2 = new Fraction(1,5);
        try {
            f = f1.subtract(f2); 
            fail("expecting ArithmeticException but got: " + f.toString());
        } catch (ArithmeticException ex) {}
        
        try {
            f= new Fraction(Integer.MIN_VALUE, 1);
            f = f.subtract(Fraction.ONE);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}
        
        try {
            f= new Fraction(Integer.MAX_VALUE, 1);
            f = f.subtract(Fraction.ONE.negate());
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}
        
        f1 = new Fraction(3,327680);
        f2 = new Fraction(2,59049);
        try {
            f = f1.subtract(f2); 
            fail("expecting ArithmeticException but got: " + f.toString());
        } catch (ArithmeticException ex) {}
    }

// org.apache.commons.math.fraction.FractionTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Fraction zero  = new Fraction(0,1);
        Fraction nullFraction = null;
        assertTrue( zero.equals(zero));
        assertFalse(zero.equals(nullFraction));
        assertFalse(zero.equals(Double.valueOf(0)));
        Fraction zero2 = new Fraction(0,2);
        assertTrue(zero.equals(zero2));
        assertEquals(zero.hashCode(), zero2.hashCode());
        Fraction one = new Fraction(1,1);
        assertFalse((one.equals(zero) ||zero.equals(one)));
    }

// org.apache.commons.math.fraction.FractionTest::testGetReducedFraction
    public void testGetReducedFraction() {
        Fraction threeFourths = new Fraction(3, 4);
        assertTrue(threeFourths.equals(Fraction.getReducedFraction(6, 8)));
        assertTrue(Fraction.ZERO.equals(Fraction.getReducedFraction(0, -1)));
        try {
            Fraction.getReducedFraction(1, 0);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        }
        assertEquals(Fraction.getReducedFraction
                (2, Integer.MIN_VALUE).getNumerator(),-1);
        assertEquals(Fraction.getReducedFraction
                (1, -1).getNumerator(), -1);
    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testCoefficients
  public void testCoefficients() {

      double[] coeffs1 = new AdamsBashforthIntegrator(1, 0.01).getCoeffs();
      assertEquals(1, coeffs1.length);
      assertEquals(1.0, coeffs1[0], 1.0e-16);

      double[] coeffs2 = new AdamsBashforthIntegrator(2, 0.01).getCoeffs();
      assertEquals(2, coeffs2.length);
      assertEquals( 3.0 / 2.0, coeffs2[0], 1.0e-16);
      assertEquals(-1.0 / 2.0, coeffs2[1], 1.0e-16);

      double[] coeffs3 = new AdamsBashforthIntegrator(3, 0.01).getCoeffs();
      assertEquals(3, coeffs3.length);
      assertEquals( 23.0 / 12.0, coeffs3[0], 1.0e-16);
      assertEquals(-16.0 / 12.0, coeffs3[1], 1.0e-16);
      assertEquals(  5.0 / 12.0, coeffs3[2], 1.0e-16);

      double[] coeffs4 = new AdamsBashforthIntegrator(4, 0.01).getCoeffs();
      assertEquals(4, coeffs4.length);
      assertEquals( 55.0 / 24.0, coeffs4[0], 1.0e-16);
      assertEquals(-59.0 / 24.0, coeffs4[1], 1.0e-16);
      assertEquals( 37.0 / 24.0, coeffs4[2], 1.0e-16);
      assertEquals( -9.0 / 24.0, coeffs4[3], 1.0e-16);

      double[] coeffs5 = new AdamsBashforthIntegrator(5, 0.01).getCoeffs();
      assertEquals(5, coeffs5.length);
      assertEquals( 1901.0 / 720.0, coeffs5[0], 1.0e-16);
      assertEquals(-2774.0 / 720.0, coeffs5[1], 1.0e-16);
      assertEquals( 2616.0 / 720.0, coeffs5[2], 1.0e-16);
      assertEquals(-1274.0 / 720.0, coeffs5[3], 1.0e-16);
      assertEquals(  251.0 / 720.0, coeffs5[4], 1.0e-16);

      double[] coeffs6 = new AdamsBashforthIntegrator(6, 0.01).getCoeffs();
      assertEquals(6, coeffs6.length);
      assertEquals( 4277.0 / 1440.0, coeffs6[0], 1.0e-16);
      assertEquals(-7923.0 / 1440.0, coeffs6[1], 1.0e-16);
      assertEquals( 9982.0 / 1440.0, coeffs6[2], 1.0e-16);
      assertEquals(-7298.0 / 1440.0, coeffs6[3], 1.0e-16);
      assertEquals( 2877.0 / 1440.0, coeffs6[4], 1.0e-16);
      assertEquals( -475.0 / 1440.0, coeffs6[5], 1.0e-16);

      double[] coeffs7 = new AdamsBashforthIntegrator(7, 0.01).getCoeffs();
      assertEquals(7, coeffs7.length);
      assertEquals( 198721.0 / 60480.0, coeffs7[0], 1.0e-16);
      assertEquals(-447288.0 / 60480.0, coeffs7[1], 1.0e-16);
      assertEquals( 705549.0 / 60480.0, coeffs7[2], 1.0e-16);
      assertEquals(-688256.0 / 60480.0, coeffs7[3], 1.0e-16);
      assertEquals( 407139.0 / 60480.0, coeffs7[4], 1.0e-16);
      assertEquals(-134472.0 / 60480.0, coeffs7[5], 1.0e-16);
      assertEquals(  19087.0 / 60480.0, coeffs7[6], 1.0e-16);

      double[] coeffs8 = new AdamsBashforthIntegrator(8, 0.01).getCoeffs();
      assertEquals(8, coeffs8.length);
      assertEquals(  434241.0 / 120960.0, coeffs8[0], 1.0e-16);
      assertEquals(-1152169.0 / 120960.0, coeffs8[1], 1.0e-16);
      assertEquals( 2183877.0 / 120960.0, coeffs8[2], 1.0e-16);
      assertEquals(-2664477.0 / 120960.0, coeffs8[3], 1.0e-16);
      assertEquals( 2102243.0 / 120960.0, coeffs8[4], 1.0e-16);
      assertEquals(-1041723.0 / 120960.0, coeffs8[5], 1.0e-16);
      assertEquals(  295767.0 / 120960.0, coeffs8[6], 1.0e-16);
      assertEquals(  -36799.0 / 120960.0, coeffs8[7], 1.0e-16);

      double[] coeffs9 = new AdamsBashforthIntegrator(9, 0.01).getCoeffs();
      assertEquals(9, coeffs9.length);
      assertEquals(  14097247.0 / 3628800.0, coeffs9[0], 1.0e-16);
      assertEquals( -43125206.0 / 3628800.0, coeffs9[1], 1.0e-16);
      assertEquals(  95476786.0 / 3628800.0, coeffs9[2], 1.0e-16);
      assertEquals(-139855262.0 / 3628800.0, coeffs9[3], 1.0e-16);
      assertEquals( 137968480.0 / 3628800.0, coeffs9[4], 1.0e-16);
      assertEquals( -91172642.0 / 3628800.0, coeffs9[5], 1.0e-16);
      assertEquals(  38833486.0 / 3628800.0, coeffs9[6], 1.0e-16);
      assertEquals(  -9664106.0 / 3628800.0, coeffs9[7], 1.0e-16);
      assertEquals(   1070017.0 / 3628800.0, coeffs9[8], 1.0e-16);

  }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new AdamsBashforthIntegrator(3, 0.01).integrate(pb,
                                                      0.0, new double[pb.getDimension()+10],
                                                      1.0, new double[pb.getDimension()+10]);
        fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws DerivativeException, IntegratorException {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousError = Double.NaN;
      for (int i = 6; i < 10; ++i) {

        TestProblemAbstract pb  = (TestProblemAbstract) problems[k].clone();
        double step = (pb.getFinalTime() - pb.getInitialTime()) * Math.pow(2.0, -i);

        FirstOrderIntegrator integ = new AdamsBashforthIntegrator(5, step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        double stopTime = integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                                          pb.getFinalTime(), new double[pb.getDimension()]);
        if (functions.length == 0) {
          assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
        }

        double error = handler.getMaximalValueError();
        if (i > 6) {
          assertTrue(error < Math.abs(previousError));
        }
        previousError = error;

      }

    }

  }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testSmallStep
  public void testSmallStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new AdamsBashforthIntegrator(3, step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

   assertTrue(handler.getLastError() < 2.0e-9);
   assertTrue(handler.getMaximalValueError() < 3.0e-8);
   assertEquals(0, handler.getMaximalTimeError(), 1.0e-14);
   assertEquals("Adams-Bashforth", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testBigStep
  public void testBigStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new AdamsBashforthIntegrator(3, step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.05);
    assertTrue(handler.getMaximalValueError() > 0.1);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-14);

  }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testBackward
  public void testBackward()
      throws DerivativeException, IntegratorException {

      TestProblem5 pb = new TestProblem5();
      double step = Math.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

      FirstOrderIntegrator integ = new AdamsBashforthIntegrator(5, step);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      assertTrue(handler.getLastError() < 8.0e-11);
      assertTrue(handler.getMaximalValueError() < 8.0e-11);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      assertEquals("Adams-Bashforth", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testPredictorCoefficients
    public void testPredictorCoefficients() {
        for (int order = 1; order < 10; ++order) {
            double[] moulton = new AdamsMoultonIntegrator(order, 0.01).getPredictorCoeffs();
            double[] bashforth  = new AdamsBashforthIntegrator(order, 0.01).getCoeffs();
            assertEquals(bashforth.length, moulton.length);
            for (int i = 0; i < moulton.length; ++i) {
                assertEquals(bashforth[i], moulton[i], 1.0e-16);
            }
        }
    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testCorrectorCoefficients
    public void testCorrectorCoefficients() {

        double[] coeffs1 = new AdamsMoultonIntegrator(1, 0.01).getCorrectorCoeffs();
        assertEquals(2, coeffs1.length);
        assertEquals(1.0 / 2.0, coeffs1[0], 1.0e-16);
        assertEquals(1.0 / 2.0, coeffs1[1], 1.0e-16);

        double[] coeffs2 = new AdamsMoultonIntegrator(2, 0.01).getCorrectorCoeffs();
        assertEquals(3, coeffs2.length);
        assertEquals( 5.0 / 12.0, coeffs2[0], 1.0e-16);
        assertEquals( 8.0 / 12.0, coeffs2[1], 1.0e-16);
        assertEquals(-1.0 / 12.0, coeffs2[2], 1.0e-16);

        double[] coeffs3 = new AdamsMoultonIntegrator(3, 0.01).getCorrectorCoeffs();
        assertEquals(4, coeffs3.length);
        assertEquals( 9.0 / 24.0, coeffs3[0], 1.0e-16);
        assertEquals(19.0 / 24.0, coeffs3[1], 1.0e-16);
        assertEquals(-5.0 / 24.0, coeffs3[2], 1.0e-16);
        assertEquals( 1.0 / 24.0, coeffs3[3], 1.0e-16);

        double[] coeffs4 = new AdamsMoultonIntegrator(4, 0.01).getCorrectorCoeffs();
        assertEquals(5, coeffs4.length);
        assertEquals( 251.0 / 720.0, coeffs4[0], 1.0e-16);
        assertEquals( 646.0 / 720.0, coeffs4[1], 1.0e-16);
        assertEquals(-264.0 / 720.0, coeffs4[2], 1.0e-16);
        assertEquals( 106.0 / 720.0, coeffs4[3], 1.0e-16);
        assertEquals( -19.0 / 720.0, coeffs4[4], 1.0e-16);

        double[] coeffs5 = new AdamsMoultonIntegrator(5, 0.01).getCorrectorCoeffs();
        assertEquals(6, coeffs5.length);
        assertEquals( 475.0 / 1440.0, coeffs5[0], 1.0e-16);
        assertEquals(1427.0 / 1440.0, coeffs5[1], 1.0e-16);
        assertEquals(-798.0 / 1440.0, coeffs5[2], 1.0e-16);
        assertEquals( 482.0 / 1440.0, coeffs5[3], 1.0e-16);
        assertEquals(-173.0 / 1440.0, coeffs5[4], 1.0e-16);
        assertEquals(  27.0 / 1440.0, coeffs5[5], 1.0e-16);

        double[] coeffs6 = new AdamsMoultonIntegrator(6, 0.01).getCorrectorCoeffs();
        assertEquals(7, coeffs6.length);
        assertEquals( 19087.0 / 60480.0, coeffs6[0], 1.0e-16);
        assertEquals( 65112.0 / 60480.0, coeffs6[1], 1.0e-16);
        assertEquals(-46461.0 / 60480.0, coeffs6[2], 1.0e-16);
        assertEquals( 37504.0 / 60480.0, coeffs6[3], 1.0e-16);
        assertEquals(-20211.0 / 60480.0, coeffs6[4], 1.0e-16);
        assertEquals(  6312.0 / 60480.0, coeffs6[5], 1.0e-16);
        assertEquals(  -863.0 / 60480.0, coeffs6[6], 1.0e-16);

        double[] coeffs7 = new AdamsMoultonIntegrator(7, 0.01).getCorrectorCoeffs();
        assertEquals(8, coeffs7.length);
        assertEquals(  36799.0 / 120960.0, coeffs7[0], 1.0e-16);
        assertEquals( 139849.0 / 120960.0, coeffs7[1], 1.0e-16);
        assertEquals(-121797.0 / 120960.0, coeffs7[2], 1.0e-16);
        assertEquals( 123133.0 / 120960.0, coeffs7[3], 1.0e-16);
        assertEquals( -88547.0 / 120960.0, coeffs7[4], 1.0e-16);
        assertEquals(  41499.0 / 120960.0, coeffs7[5], 1.0e-16);
        assertEquals( -11351.0 / 120960.0, coeffs7[6], 1.0e-16);
        assertEquals(   1375.0 / 120960.0, coeffs7[7], 1.0e-16);

        double[] coeffs8 = new AdamsMoultonIntegrator(8, 0.01).getCorrectorCoeffs();
        assertEquals(9, coeffs8.length);
        assertEquals( 1070017.0 / 3628800.0, coeffs8[0], 1.0e-16);
        assertEquals( 4467094.0 / 3628800.0, coeffs8[1], 1.0e-16);
        assertEquals(-4604594.0 / 3628800.0, coeffs8[2], 1.0e-16);
        assertEquals( 5595358.0 / 3628800.0, coeffs8[3], 1.0e-16);
        assertEquals(-5033120.0 / 3628800.0, coeffs8[4], 1.0e-16);
        assertEquals( 3146338.0 / 3628800.0, coeffs8[5], 1.0e-16);
        assertEquals(-1291214.0 / 3628800.0, coeffs8[6], 1.0e-16);
        assertEquals(  312874.0 / 3628800.0, coeffs8[7], 1.0e-16);
        assertEquals(  -33953.0 / 3628800.0, coeffs8[8], 1.0e-16);

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testDimensionCheck
    public void testDimensionCheck() {
        try  {
            TestProblem1 pb = new TestProblem1();
            new AdamsMoultonIntegrator(3, 0.01).integrate(pb,
                    0.0, new double[pb.getDimension()+10],
                    1.0, new double[pb.getDimension()+10]);
            fail("an exception should have been thrown");
        } catch(DerivativeException de) {
            fail("wrong exception caught");
        } catch(IntegratorException ie) {
        }
    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testDecreasingSteps
    public void testDecreasingSteps()
        throws DerivativeException, IntegratorException {

        TestProblemAbstract[] problems = TestProblemFactory.getProblems();
        for (int k = 0; k < problems.length; ++k) {

            double previousError = Double.NaN;
            for (int i = 6; i < 10; ++i) {

                TestProblemAbstract pb  = (TestProblemAbstract) problems[k].clone();
                double step = (pb.getFinalTime() - pb.getInitialTime()) * Math.pow(2.0, -i);
                if (pb instanceof TestProblem3) {
                    step /= 8;
                }

                FirstOrderIntegrator integ = new AdamsMoultonIntegrator(5, step);
                TestProblemHandler handler = new TestProblemHandler(pb, integ);
                integ.addStepHandler(handler);
                EventHandler[] functions = pb.getEventsHandlers();
                for (int l = 0; l < functions.length; ++l) {
                    integ.addEventHandler(functions[l],
                            Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
                }
                double stopTime = integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
                if (functions.length == 0) {
                    assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
                }

                double error = handler.getMaximalValueError();
                if (i > 6) {
                    assertTrue(error < Math.abs(previousError));
                }
                previousError = error;

            }

        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testSmallStep
    public void testSmallStep()
        throws DerivativeException, IntegratorException {

        TestProblem1 pb  = new TestProblem1();
        double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(3, step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb,
                pb.getInitialTime(), pb.getInitialState(),
                pb.getFinalTime(), new double[pb.getDimension()]);

        assertTrue(handler.getLastError() < 7.0e-12);
        assertTrue(handler.getMaximalValueError() < 4.0e-11);
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-14);
        assertEquals("Adams-Moulton", integ.getName());

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testBigStep
    public void testBigStep()
        throws DerivativeException, IntegratorException {

        TestProblem1 pb  = new TestProblem1();
        double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(3, step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb,
                pb.getInitialTime(), pb.getInitialState(),
                pb.getFinalTime(), new double[pb.getDimension()]);

        assertTrue(handler.getLastError() > 0.01);
        assertTrue(handler.getMaximalValueError() > 0.03);
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-14);

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testBackward
    public void testBackward()
        throws DerivativeException, IntegratorException {

        TestProblem5 pb = new TestProblem5();
        double step = Math.abs(pb.getFinalTime() - pb.getInitialTime()) * 0.001;

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(5, step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        assertTrue(handler.getLastError() < 5.0e-10);
        assertTrue(handler.getMaximalValueError() < 7.0e-10);
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
        assertEquals("Adams-Moulton", integ.getName());
    }
