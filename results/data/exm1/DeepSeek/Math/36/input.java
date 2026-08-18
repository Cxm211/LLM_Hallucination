// buggy code
    public double doubleValue() {
        double result = numerator.doubleValue() / denominator.doubleValue();
            // Numerator and/or denominator must be out of range:
            // Calculate how far to shift them to put them in range.
        return result;
    }

    public float floatValue() {
        float result = numerator.floatValue() / denominator.floatValue();
            // Numerator and/or denominator must be out of range:
            // Calculate how far to shift them to put them in range.
        return result;
    }

// relevant test
// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstChebyshevPolynomials
    public void testFirstChebyshevPolynomials() {
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(3), "-3 x + 4 x^3");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(2), "-1 + 2 x^2");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(1), "x");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(0), "1");

        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(7), "-7 x + 56 x^3 - 112 x^5 + 64 x^7");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(6), "-1 + 18 x^2 - 48 x^4 + 32 x^6");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(5), "5 x - 20 x^3 + 16 x^5");
        checkPolynomial(PolynomialsUtils.createChebyshevPolynomial(4), "1 - 8 x^2 + 8 x^4");

    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testChebyshevBounds
    public void testChebyshevBounds() {
        for (int k = 0; k < 12; ++k) {
            PolynomialFunction Tk = PolynomialsUtils.createChebyshevPolynomial(k);
            for (double x = -1; x <= 1; x += 0.02) {
                Assert.assertTrue(k + " " + Tk.value(x), FastMath.abs(Tk.value(x)) < (1 + 1e-12));
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

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testChebyshevOrthogonality
    public void testChebyshevOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return 1 / FastMath.sqrt(1 - x * x);
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createChebyshevPolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createChebyshevPolynomial(j);
                checkOrthogonality(pi, pj, weight, -0.9999, 0.9999, 1.5, 0.03);
            }
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstHermitePolynomials
    public void testFirstHermitePolynomials() {
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(3), "-12 x + 8 x^3");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(2), "-2 + 4 x^2");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(1), "2 x");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(0), "1");

        checkPolynomial(PolynomialsUtils.createHermitePolynomial(7), "-1680 x + 3360 x^3 - 1344 x^5 + 128 x^7");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(6), "-120 + 720 x^2 - 480 x^4 + 64 x^6");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(5), "120 x - 160 x^3 + 32 x^5");
        checkPolynomial(PolynomialsUtils.createHermitePolynomial(4), "12 - 48 x^2 + 16 x^4");

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

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testHermiteOrthogonality
    public void testHermiteOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.exp(-x * x);
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createHermitePolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createHermitePolynomial(j);
                checkOrthogonality(pi, pj, weight, -50, 50, 1.5, 1.0e-8);
            }
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstLaguerrePolynomials
    public void testFirstLaguerrePolynomials() {
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(3), 6l, "6 - 18 x + 9 x^2 - x^3");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(2), 2l, "2 - 4 x + x^2");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(1), 1l, "1 - x");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(0), 1l, "1");

        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(7), 5040l,
                "5040 - 35280 x + 52920 x^2 - 29400 x^3"
                + " + 7350 x^4 - 882 x^5 + 49 x^6 - x^7");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(6),  720l,
                "720 - 4320 x + 5400 x^2 - 2400 x^3 + 450 x^4"
                + " - 36 x^5 + x^6");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(5),  120l,
        "120 - 600 x + 600 x^2 - 200 x^3 + 25 x^4 - x^5");
        checkPolynomial(PolynomialsUtils.createLaguerrePolynomial(4),   24l,
        "24 - 96 x + 72 x^2 - 16 x^3 + x^4");

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

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testLaguerreOrthogonality
    public void testLaguerreOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.exp(-x);
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createLaguerrePolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createLaguerrePolynomial(j);
                checkOrthogonality(pi, pj, weight, 0.0, 100.0, 0.99999, 1.0e-13);
            }
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testFirstLegendrePolynomials
    public void testFirstLegendrePolynomials() {
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(3),  2l, "-3 x + 5 x^3");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(2),  2l, "-1 + 3 x^2");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(1),  1l, "x");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(0),  1l, "1");

        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(7), 16l, "-35 x + 315 x^3 - 693 x^5 + 429 x^7");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(6), 16l, "-5 + 105 x^2 - 315 x^4 + 231 x^6");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(5),  8l, "15 x - 70 x^3 + 63 x^5");
        checkPolynomial(PolynomialsUtils.createLegendrePolynomial(4),  8l, "3 - 30 x^2 + 35 x^4");

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

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testLegendreOrthogonality
    public void testLegendreOrthogonality() {
        UnivariateFunction weight = new UnivariateFunction() {
            public double value(double x) {
                return 1;
            }
        };
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction pi = PolynomialsUtils.createLegendrePolynomial(i);
            for (int j = 0; j <= i; ++j) {
                PolynomialFunction pj = PolynomialsUtils.createLegendrePolynomial(j);
                checkOrthogonality(pi, pj, weight, -1, 1, 0.1, 1.0e-13);
            }
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testHighDegreeLegendre
    public void testHighDegreeLegendre() {
        PolynomialsUtils.createLegendrePolynomial(40);
        double[] l40 = PolynomialsUtils.createLegendrePolynomial(40).getCoefficients();
        double denominator = 274877906944d;
        double[] numerators = new double[] {
                          +34461632205d,            -28258538408100d,          +3847870979902950d,        -207785032914759300d,
                  +5929294332103310025d,     -103301483474866556880d,    +1197358103913226000200d,    -9763073770369381232400d,
              +58171647881784229843050d,  -260061484647976556945400d,  +888315281771246239250340d, -2345767627188139419665400d,
            +4819022625419112503443050d, -7710436200670580005508880d, +9566652323054238154983240d, -9104813935044723209570256d,
            +6516550296251767619752905d, -3391858621221953912598660d, +1211378079007840683070950d,  -265365894974690562152100d,
              +26876802183334044115405d
        };
        for (int i = 0; i < l40.length; ++i) {
            if (i % 2 == 0) {
                double ci = numerators[i / 2] / denominator;
                Assert.assertEquals(ci, l40[i], FastMath.abs(ci) * 1e-15);
            } else {
                Assert.assertEquals(0, l40[i], 0);
            }
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testJacobiLegendre
    public void testJacobiLegendre() {
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction legendre = PolynomialsUtils.createLegendrePolynomial(i);
            PolynomialFunction jacobi   = PolynomialsUtils.createJacobiPolynomial(i, 0, 0);
            checkNullPolynomial(legendre.subtract(jacobi));
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testJacobiEvaluationAt1
    public void testJacobiEvaluationAt1() {
        for (int v = 0; v < 10; ++v) {
            for (int w = 0; w < 10; ++w) {
                for (int i = 0; i < 10; ++i) {
                    PolynomialFunction jacobi = PolynomialsUtils.createJacobiPolynomial(i, v, w);
                    double binomial = ArithmeticUtils.binomialCoefficient(v + i, i);
                    Assert.assertTrue(Precision.equals(binomial, jacobi.value(1.0), 1));
                }
            }
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testJacobiOrthogonality
    public void testJacobiOrthogonality() {
        for (int v = 0; v < 5; ++v) {
            for (int w = v; w < 5; ++w) {
                final int vv = v;
                final int ww = w;
                UnivariateFunction weight = new UnivariateFunction() {
                    public double value(double x) {
                        return FastMath.pow(1 - x, vv) * FastMath.pow(1 + x, ww);
                    }
                };
                for (int i = 0; i < 10; ++i) {
                    PolynomialFunction pi = PolynomialsUtils.createJacobiPolynomial(i, v, w);
                    for (int j = 0; j <= i; ++j) {
                        PolynomialFunction pj = PolynomialsUtils.createJacobiPolynomial(j, v, w);
                        checkOrthogonality(pi, pj, weight, -1, 1, 0.1, 1.0e-12);
                    }
                }
            }
        }
    }

// org.apache.commons.math.analysis.polynomials.PolynomialsUtilsTest::testShift
    public void testShift() {
        
        PolynomialFunction f1x = new PolynomialFunction(new double[] { 1, 1, 2 });

        PolynomialFunction f1x1
            = new PolynomialFunction(PolynomialsUtils.shift(f1x.getCoefficients(), 1));
        checkPolynomial(f1x1, "4 + 5 x + 2 x^2");

        PolynomialFunction f1xM1
            = new PolynomialFunction(PolynomialsUtils.shift(f1x.getCoefficients(), -1));
        checkPolynomial(f1xM1, "2 - 3 x + 2 x^2");
        
        PolynomialFunction f1x3
            = new PolynomialFunction(PolynomialsUtils.shift(f1x.getCoefficients(), 3));
        checkPolynomial(f1x3, "22 + 13 x + 2 x^2");

        
        PolynomialFunction f2x = new PolynomialFunction(new double[]{2, 0, 3, 8, 0, 121});

        PolynomialFunction f2x1
            = new PolynomialFunction(PolynomialsUtils.shift(f2x.getCoefficients(), 1));
        checkPolynomial(f2x1, "134 + 635 x + 1237 x^2 + 1218 x^3 + 605 x^4 + 121 x^5");

        PolynomialFunction f2x3
            = new PolynomialFunction(PolynomialsUtils.shift(f2x.getCoefficients(), 3));
        checkPolynomial(f2x3, "29648 + 49239 x + 32745 x^2 + 10898 x^3 + 1815 x^4 + 121 x^5");
    }

// org.apache.commons.math.distribution.KolmogorovSmirnovDistributionTest::testCumulativeDensityFunction
    public void testCumulativeDensityFunction() throws Exception {
        
        KolmogorovSmirnovDistribution dist;
        
        
        
        

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(4.907829957616471622388047046469198862537e-86, dist.cdf(0.005, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(5.151982014280041957199687829849210629618e-06, dist.cdf(0.02, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(0.01291614648162886340443389343590752105229, dist.cdf(0.031111, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(200);
        Assert.assertEquals(0.1067137011362679355208626930107129737735, dist.cdf(0.04, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(1.914734701559404553985102395145063418825e-53, dist.cdf(0.005, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(0.001171328985781981343872182321774744195864, dist.cdf(0.02, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(0.1142955196267499418105728636874118819833, dist.cdf(0.031111, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(341);
        Assert.assertEquals(0.3685529520496805266915885113121476024389, dist.cdf(0.04, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(1.810657144595055888918455512707637574637e-47, dist.cdf(0.005, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(0.003068542559702356568168690742481885536108, dist.cdf(0.02, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(0.1658291700122746237244797384846606291831, dist.cdf(0.031111, false), TOLERANCE);

        
        dist = new KolmogorovSmirnovDistribution(389);
        Assert.assertEquals(0.4513143712128902529379104180407011881471, dist.cdf(0.04, false), TOLERANCE);

    }

// org.apache.commons.math.fraction.BigFractionFieldTest::testZero
    public void testZero() {
        Assert.assertEquals(BigFraction.ZERO, BigFractionField.getInstance().getZero());
    }

// org.apache.commons.math.fraction.BigFractionFieldTest::testOne
    public void testOne() {
        Assert.assertEquals(BigFraction.ONE, BigFractionField.getInstance().getOne());
    }

// org.apache.commons.math.fraction.BigFractionFieldTest::testSerial
    public void testSerial() {
        
        BigFractionField field = BigFractionField.getInstance();
        Assert.assertTrue(field == TestUtils.serializeAndRecover(field));
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testFormat
    public void testFormat() {
        BigFraction c = new BigFraction(1, 2);
        String expected = "1 / 2";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testFormatNegative
    public void testFormatNegative() {
        BigFraction c = new BigFraction(-1, 2);
        String expected = "-1 / 2";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testFormatZero
    public void testFormatZero() {
        BigFraction c = new BigFraction(0, 1);
        String expected = "0 / 1";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testFormatImproper
    public void testFormatImproper() {
        BigFraction c = new BigFraction(5, 3);

        String actual = properFormat.format(c);
        Assert.assertEquals("1 2 / 3", actual);

        actual = improperFormat.format(c);
        Assert.assertEquals("5 / 3", actual);
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testFormatImproperNegative
    public void testFormatImproperNegative() {
        BigFraction c = new BigFraction(-5, 3);

        String actual = properFormat.format(c);
        Assert.assertEquals("-1 2 / 3", actual);

        actual = improperFormat.format(c);
        Assert.assertEquals("-5 / 3", actual);
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testParse
    public void testParse() {
        String source = "1 / 2";

        {
            BigFraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(BigInteger.ONE, c.getNumerator());
            Assert.assertEquals(BigInteger.valueOf(2l), c.getDenominator());

            c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(BigInteger.ONE, c.getNumerator());
            Assert.assertEquals(BigInteger.valueOf(2l), c.getDenominator());
        }
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testParseInteger
    public void testParseInteger() {
        String source = "10";
        {
            BigFraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(BigInteger.TEN, c.getNumerator());
            Assert.assertEquals(BigInteger.ONE, c.getDenominator());
        }
        {
            BigFraction c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(BigInteger.TEN, c.getNumerator());
            Assert.assertEquals(BigInteger.ONE, c.getDenominator());
        }
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testParseInvalid
    public void testParseInvalid() {
        String source = "a";
        String msg = "should not be able to parse '10 / a'.";
        try {
            properFormat.parse(source);
            Assert.fail(msg);
        } catch (MathParseException ex) {
            
        }
        try {
            improperFormat.parse(source);
            Assert.fail(msg);
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testParseInvalidDenominator
    public void testParseInvalidDenominator() {
        String source = "10 / a";
        String msg = "should not be able to parse '10 / a'.";
        try {
            properFormat.parse(source);
            Assert.fail(msg);
        } catch (MathParseException ex) {
            
        }
        try {
            improperFormat.parse(source);
            Assert.fail(msg);
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testParseNegative
    public void testParseNegative() {

        {
            String source = "-1 / 2";
            BigFraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumeratorAsInt());
            Assert.assertEquals(2, c.getDenominatorAsInt());

            c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumeratorAsInt());
            Assert.assertEquals(2, c.getDenominatorAsInt());

            source = "1 / -2";
            c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumeratorAsInt());
            Assert.assertEquals(2, c.getDenominatorAsInt());

            c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumeratorAsInt());
            Assert.assertEquals(2, c.getDenominatorAsInt());
        }
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testParseProper
    public void testParseProper() {
        String source = "1 2 / 3";

        {
            BigFraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(5, c.getNumeratorAsInt());
            Assert.assertEquals(3, c.getDenominatorAsInt());
        }

        try {
            improperFormat.parse(source);
            Assert.fail("invalid improper fraction.");
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testParseProperNegative
    public void testParseProperNegative() {
        String source = "-1 2 / 3";
        {
            BigFraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-5, c.getNumeratorAsInt());
            Assert.assertEquals(3, c.getDenominatorAsInt());
        }

        try {
            improperFormat.parse(source);
            Assert.fail("invalid improper fraction.");
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testParseProperInvalidMinus
    public void testParseProperInvalidMinus() {
        String source = "2 -2 / 3";
        try {
            properFormat.parse(source);
            Assert.fail("invalid minus in improper fraction.");
        } catch (MathParseException ex) {
            
        }
        source = "2 2 / -3";
        try {
            properFormat.parse(source);
            Assert.fail("invalid minus in improper fraction.");
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testParseBig
    public void testParseBig() {
        BigFraction f1 =
            improperFormat.parse("167213075789791382630275400487886041651764456874403" +
                                 " / " +
                                 "53225575123090058458126718248444563466137046489291");
        Assert.assertEquals(FastMath.PI, f1.doubleValue(), 0.0);
        BigFraction f2 =
            properFormat.parse("3 " +
                               "7536350420521207255895245742552351253353317406530" +
                               " / " +
                               "53225575123090058458126718248444563466137046489291");
        Assert.assertEquals(FastMath.PI, f2.doubleValue(), 0.0);
        Assert.assertEquals(f1, f2);
        BigDecimal pi =
            new BigDecimal("3.141592653589793238462643383279502884197169399375105820974944592307816406286208998628034825342117068");
        Assert.assertEquals(pi, f1.bigDecimalValue(99, BigDecimal.ROUND_HALF_EVEN));
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testNumeratorFormat
    public void testNumeratorFormat() {
        NumberFormat old = properFormat.getNumeratorFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        properFormat.setNumeratorFormat(nf);
        Assert.assertEquals(nf, properFormat.getNumeratorFormat());
        properFormat.setNumeratorFormat(old);

        old = improperFormat.getNumeratorFormat();
        nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        improperFormat.setNumeratorFormat(nf);
        Assert.assertEquals(nf, improperFormat.getNumeratorFormat());
        improperFormat.setNumeratorFormat(old);
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testDenominatorFormat
    public void testDenominatorFormat() {
        NumberFormat old = properFormat.getDenominatorFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        properFormat.setDenominatorFormat(nf);
        Assert.assertEquals(nf, properFormat.getDenominatorFormat());
        properFormat.setDenominatorFormat(old);

        old = improperFormat.getDenominatorFormat();
        nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        improperFormat.setDenominatorFormat(nf);
        Assert.assertEquals(nf, improperFormat.getDenominatorFormat());
        improperFormat.setDenominatorFormat(old);
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testWholeFormat
    public void testWholeFormat() {
        ProperBigFractionFormat format = (ProperBigFractionFormat)properFormat;

        NumberFormat old = format.getWholeFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        format.setWholeFormat(nf);
        Assert.assertEquals(nf, format.getWholeFormat());
        format.setWholeFormat(old);
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testLongFormat
    public void testLongFormat() {
        Assert.assertEquals("10 / 1", improperFormat.format(10l));
    }

// org.apache.commons.math.fraction.BigFractionFormatTest::testDoubleFormat
    public void testDoubleFormat() {
        Assert.assertEquals("1 / 16", improperFormat.format(0.0625));
    }

// org.apache.commons.math.fraction.BigFractionTest::testConstructor
    public void testConstructor() throws Exception {
        assertFraction(0, 1, new BigFraction(0, 1));
        assertFraction(0, 1, new BigFraction(0l, 2l));
        assertFraction(0, 1, new BigFraction(0, -1));
        assertFraction(1, 2, new BigFraction(1, 2));
        assertFraction(1, 2, new BigFraction(2, 4));
        assertFraction(-1, 2, new BigFraction(-1, 2));
        assertFraction(-1, 2, new BigFraction(1, -2));
        assertFraction(-1, 2, new BigFraction(-2, 4));
        assertFraction(-1, 2, new BigFraction(2, -4));
        assertFraction(11, 1, new BigFraction(11));
        assertFraction(11, 1, new BigFraction(11l));
        assertFraction(11, 1, new BigFraction(new BigInteger("11")));

        assertFraction(0, 1, new BigFraction(0.00000000000001, 1.0e-5, 100));
        assertFraction(2, 5, new BigFraction(0.40000000000001, 1.0e-5, 100));
        assertFraction(15, 1, new BigFraction(15.0000000000001, 1.0e-5, 100));

        Assert.assertEquals(0.00000000000001, new BigFraction(0.00000000000001).doubleValue(), 0.0);
        Assert.assertEquals(0.40000000000001, new BigFraction(0.40000000000001).doubleValue(), 0.0);
        Assert.assertEquals(15.0000000000001, new BigFraction(15.0000000000001).doubleValue(), 0.0);
        assertFraction(3602879701896487l, 9007199254740992l, new BigFraction(0.40000000000001));
        assertFraction(1055531162664967l, 70368744177664l, new BigFraction(15.0000000000001));
        try {
            new BigFraction(null, BigInteger.ONE);
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException npe) {
            
        }
        try {
            new BigFraction(BigInteger.ONE, null);
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException npe) {
            
        }
        try {
            new BigFraction(BigInteger.ONE, BigInteger.ZERO);
            Assert.fail("Expecting ZeroException");
        } catch (ZeroException npe) {
            
        }
        try {
            new BigFraction(2.0 * Integer.MAX_VALUE, 1.0e-5, 100000);
            Assert.fail("Expecting FractionConversionException");
        } catch (FractionConversionException fce) {
            
        }
    }

// org.apache.commons.math.fraction.BigFractionTest::testGoldenRatio
    public void testGoldenRatio() {
        
        new BigFraction((1 + FastMath.sqrt(5)) / 2, 1.0e-12, 25);
    }

// org.apache.commons.math.fraction.BigFractionTest::testDoubleConstructor
    public void testDoubleConstructor() throws ConvergenceException {
        assertFraction(1, 2, new BigFraction((double) 1 / (double) 2, 1.0e-5, 100));
        assertFraction(1, 3, new BigFraction((double) 1 / (double) 3, 1.0e-5, 100));
        assertFraction(2, 3, new BigFraction((double) 2 / (double) 3, 1.0e-5, 100));
        assertFraction(1, 4, new BigFraction((double) 1 / (double) 4, 1.0e-5, 100));
        assertFraction(3, 4, new BigFraction((double) 3 / (double) 4, 1.0e-5, 100));
        assertFraction(1, 5, new BigFraction((double) 1 / (double) 5, 1.0e-5, 100));
        assertFraction(2, 5, new BigFraction((double) 2 / (double) 5, 1.0e-5, 100));
        assertFraction(3, 5, new BigFraction((double) 3 / (double) 5, 1.0e-5, 100));
        assertFraction(4, 5, new BigFraction((double) 4 / (double) 5, 1.0e-5, 100));
        assertFraction(1, 6, new BigFraction((double) 1 / (double) 6, 1.0e-5, 100));
        assertFraction(5, 6, new BigFraction((double) 5 / (double) 6, 1.0e-5, 100));
        assertFraction(1, 7, new BigFraction((double) 1 / (double) 7, 1.0e-5, 100));
        assertFraction(2, 7, new BigFraction((double) 2 / (double) 7, 1.0e-5, 100));
        assertFraction(3, 7, new BigFraction((double) 3 / (double) 7, 1.0e-5, 100));
        assertFraction(4, 7, new BigFraction((double) 4 / (double) 7, 1.0e-5, 100));
        assertFraction(5, 7, new BigFraction((double) 5 / (double) 7, 1.0e-5, 100));
        assertFraction(6, 7, new BigFraction((double) 6 / (double) 7, 1.0e-5, 100));
        assertFraction(1, 8, new BigFraction((double) 1 / (double) 8, 1.0e-5, 100));
        assertFraction(3, 8, new BigFraction((double) 3 / (double) 8, 1.0e-5, 100));
        assertFraction(5, 8, new BigFraction((double) 5 / (double) 8, 1.0e-5, 100));
        assertFraction(7, 8, new BigFraction((double) 7 / (double) 8, 1.0e-5, 100));
        assertFraction(1, 9, new BigFraction((double) 1 / (double) 9, 1.0e-5, 100));
        assertFraction(2, 9, new BigFraction((double) 2 / (double) 9, 1.0e-5, 100));
        assertFraction(4, 9, new BigFraction((double) 4 / (double) 9, 1.0e-5, 100));
        assertFraction(5, 9, new BigFraction((double) 5 / (double) 9, 1.0e-5, 100));
        assertFraction(7, 9, new BigFraction((double) 7 / (double) 9, 1.0e-5, 100));
        assertFraction(8, 9, new BigFraction((double) 8 / (double) 9, 1.0e-5, 100));
        assertFraction(1, 10, new BigFraction((double) 1 / (double) 10, 1.0e-5, 100));
        assertFraction(3, 10, new BigFraction((double) 3 / (double) 10, 1.0e-5, 100));
        assertFraction(7, 10, new BigFraction((double) 7 / (double) 10, 1.0e-5, 100));
        assertFraction(9, 10, new BigFraction((double) 9 / (double) 10, 1.0e-5, 100));
        assertFraction(1, 11, new BigFraction((double) 1 / (double) 11, 1.0e-5, 100));
        assertFraction(2, 11, new BigFraction((double) 2 / (double) 11, 1.0e-5, 100));
        assertFraction(3, 11, new BigFraction((double) 3 / (double) 11, 1.0e-5, 100));
        assertFraction(4, 11, new BigFraction((double) 4 / (double) 11, 1.0e-5, 100));
        assertFraction(5, 11, new BigFraction((double) 5 / (double) 11, 1.0e-5, 100));
        assertFraction(6, 11, new BigFraction((double) 6 / (double) 11, 1.0e-5, 100));
        assertFraction(7, 11, new BigFraction((double) 7 / (double) 11, 1.0e-5, 100));
        assertFraction(8, 11, new BigFraction((double) 8 / (double) 11, 1.0e-5, 100));
        assertFraction(9, 11, new BigFraction((double) 9 / (double) 11, 1.0e-5, 100));
        assertFraction(10, 11, new BigFraction((double) 10 / (double) 11, 1.0e-5, 100));
    }

// org.apache.commons.math.fraction.BigFractionTest::testDigitLimitConstructor
    public void testDigitLimitConstructor() throws ConvergenceException {
        assertFraction(2, 5, new BigFraction(0.4, 9));
        assertFraction(2, 5, new BigFraction(0.4, 99));
        assertFraction(2, 5, new BigFraction(0.4, 999));

        assertFraction(3, 5, new BigFraction(0.6152, 9));
        assertFraction(8, 13, new BigFraction(0.6152, 99));
        assertFraction(510, 829, new BigFraction(0.6152, 999));
        assertFraction(769, 1250, new BigFraction(0.6152, 9999));
    }

// org.apache.commons.math.fraction.BigFractionTest::testEpsilonLimitConstructor
    public void testEpsilonLimitConstructor() throws ConvergenceException {
        assertFraction(2, 5, new BigFraction(0.4, 1.0e-5, 100));

        assertFraction(3, 5, new BigFraction(0.6152, 0.02, 100));
        assertFraction(8, 13, new BigFraction(0.6152, 1.0e-3, 100));
        assertFraction(251, 408, new BigFraction(0.6152, 1.0e-4, 100));
        assertFraction(251, 408, new BigFraction(0.6152, 1.0e-5, 100));
        assertFraction(510, 829, new BigFraction(0.6152, 1.0e-6, 100));
        assertFraction(769, 1250, new BigFraction(0.6152, 1.0e-7, 100));
    }

// org.apache.commons.math.fraction.BigFractionTest::testCompareTo
    public void testCompareTo() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);
        BigFraction third = new BigFraction(1, 2);

        Assert.assertEquals(0, first.compareTo(first));
        Assert.assertEquals(0, first.compareTo(third));
        Assert.assertEquals(1, first.compareTo(second));
        Assert.assertEquals(-1, second.compareTo(first));

        
        
        
        BigFraction pi1 = new BigFraction(1068966896, 340262731);
        BigFraction pi2 = new BigFraction( 411557987, 131002976);
        Assert.assertEquals(-1, pi1.compareTo(pi2));
        Assert.assertEquals( 1, pi2.compareTo(pi1));
        Assert.assertEquals(0.0, pi1.doubleValue() - pi2.doubleValue(), 1.0e-20);

    }

// org.apache.commons.math.fraction.BigFractionTest::testDoubleValue
    public void testDoubleValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);

        Assert.assertEquals(0.5, first.doubleValue(), 0.0);
        Assert.assertEquals(1.0 / 3.0, second.doubleValue(), 0.0);
    }

// org.apache.commons.math.fraction.BigFractionTest::testDoubleValueForLargeNumeratorAndDenominator
    public void testDoubleValueForLargeNumeratorAndDenominator() {
        final BigInteger pow400 = BigInteger.TEN.pow(400);
        final BigInteger pow401 = BigInteger.TEN.pow(401);
        final BigInteger two = new BigInteger("2");
        final BigFraction large = new BigFraction(pow401.add(BigInteger.ONE),
                                                  pow400.multiply(two));

        Assert.assertEquals(5, large.doubleValue(), 1e-15);
    }

// org.apache.commons.math.fraction.BigFractionTest::testFloatValueForLargeNumeratorAndDenominator
    public void testFloatValueForLargeNumeratorAndDenominator() {
        final BigInteger pow400 = BigInteger.TEN.pow(400);
        final BigInteger pow401 = BigInteger.TEN.pow(401);
        final BigInteger two = new BigInteger("2");
        final BigFraction large = new BigFraction(pow401.add(BigInteger.ONE),
                                                  pow400.multiply(two));

        Assert.assertEquals(5, large.floatValue(), 1e-15);
    }

// org.apache.commons.math.fraction.BigFractionTest::testFloatValue
    public void testFloatValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);

        Assert.assertEquals(0.5f, first.floatValue(), 0.0f);
        Assert.assertEquals((float) (1.0 / 3.0), second.floatValue(), 0.0f);
    }

// org.apache.commons.math.fraction.BigFractionTest::testIntValue
    public void testIntValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(3, 2);

        Assert.assertEquals(0, first.intValue());
        Assert.assertEquals(1, second.intValue());
    }

// org.apache.commons.math.fraction.BigFractionTest::testLongValue
    public void testLongValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(3, 2);

        Assert.assertEquals(0L, first.longValue());
        Assert.assertEquals(1L, second.longValue());
    }

// org.apache.commons.math.fraction.BigFractionTest::testConstructorDouble
    public void testConstructorDouble() {
        assertFraction(1, 2, new BigFraction(0.5));
        assertFraction(6004799503160661l, 18014398509481984l, new BigFraction(1.0 / 3.0));
        assertFraction(6124895493223875l, 36028797018963968l, new BigFraction(17.0 / 100.0));
        assertFraction(1784551352345559l, 562949953421312l, new BigFraction(317.0 / 100.0));
        assertFraction(-1, 2, new BigFraction(-0.5));
        assertFraction(-6004799503160661l, 18014398509481984l, new BigFraction(-1.0 / 3.0));
        assertFraction(-6124895493223875l, 36028797018963968l, new BigFraction(17.0 / -100.0));
        assertFraction(-1784551352345559l, 562949953421312l, new BigFraction(-317.0 / 100.0));
        for (double v : new double[] { Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            try {
                new BigFraction(v);
                Assert.fail("Expecting IllegalArgumentException");
            } catch (IllegalArgumentException iae) {
                
            }
        }
        Assert.assertEquals(1l, new BigFraction(Double.MAX_VALUE).getDenominatorAsLong());
        Assert.assertEquals(1l, new BigFraction(Double.longBitsToDouble(0x0010000000000000L)).getNumeratorAsLong());
        Assert.assertEquals(1l, new BigFraction(Double.MIN_VALUE).getNumeratorAsLong());
    }

// org.apache.commons.math.fraction.BigFractionTest::testAbs
    public void testAbs() {
        BigFraction a = new BigFraction(10, 21);
        BigFraction b = new BigFraction(-10, 21);
        BigFraction c = new BigFraction(10, -21);

        assertFraction(10, 21, a.abs());
        assertFraction(10, 21, b.abs());
        assertFraction(10, 21, c.abs());
    }

// org.apache.commons.math.fraction.BigFractionTest::testReciprocal
    public void testReciprocal() {
        BigFraction f = null;

        f = new BigFraction(50, 75);
        f = f.reciprocal();
        Assert.assertEquals(3, f.getNumeratorAsInt());
        Assert.assertEquals(2, f.getDenominatorAsInt());

        f = new BigFraction(4, 3);
        f = f.reciprocal();
        Assert.assertEquals(3, f.getNumeratorAsInt());
        Assert.assertEquals(4, f.getDenominatorAsInt());

        f = new BigFraction(-15, 47);
        f = f.reciprocal();
        Assert.assertEquals(-47, f.getNumeratorAsInt());
        Assert.assertEquals(15, f.getDenominatorAsInt());

        f = new BigFraction(0, 3);
        try {
            f = f.reciprocal();
            Assert.fail("expecting ZeroException");
        } catch (ZeroException ex) {
        }

        
        f = new BigFraction(Integer.MAX_VALUE, 1);
        f = f.reciprocal();
        Assert.assertEquals(1, f.getNumeratorAsInt());
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
    }

// org.apache.commons.math.fraction.BigFractionTest::testNegate
    public void testNegate() {
        BigFraction f = null;

        f = new BigFraction(50, 75);
        f = f.negate();
        Assert.assertEquals(-2, f.getNumeratorAsInt());
        Assert.assertEquals(3, f.getDenominatorAsInt());

        f = new BigFraction(-50, 75);
        f = f.negate();
        Assert.assertEquals(2, f.getNumeratorAsInt());
        Assert.assertEquals(3, f.getDenominatorAsInt());

        
        f = new BigFraction(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
        f = f.negate();
        Assert.assertEquals(Integer.MIN_VALUE + 2, f.getNumeratorAsInt());
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());

    }

// org.apache.commons.math.fraction.BigFractionTest::testAdd
    public void testAdd() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(1, 1, a.add(a));
        assertFraction(7, 6, a.add(b));
        assertFraction(7, 6, b.add(a));
        assertFraction(4, 3, b.add(b));

        BigFraction f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        BigFraction f2 = BigFraction.ONE;
        BigFraction f = f1.add(f2);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(-1, 13 * 13 * 2 * 2);
        f2 = new BigFraction(-2, 13 * 17 * 2);
        f = f1.add(f2);
        Assert.assertEquals(13 * 13 * 17 * 2 * 2, f.getDenominatorAsInt());
        Assert.assertEquals(-17 - 2 * 13 * 2, f.getNumeratorAsInt());

        try {
            f.add((BigFraction) null);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

        
        
        f1 = new BigFraction(1, 32768 * 3);
        f2 = new BigFraction(1, 59049);
        f = f1.add(f2);
        Assert.assertEquals(52451, f.getNumeratorAsInt());
        Assert.assertEquals(1934917632, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, 3);
        f2 = new BigFraction(1, 3);
        f = f1.add(f2);
        Assert.assertEquals(Integer.MIN_VALUE + 1, f.getNumeratorAsInt());
        Assert.assertEquals(3, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(BigInteger.ONE);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f = f.add(BigInteger.ZERO);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(1);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f = f.add(0);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(1l);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f = f.add(0l);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

    }

// org.apache.commons.math.fraction.BigFractionTest::testDivide
    public void testDivide() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(1, 1, a.divide(a));
        assertFraction(3, 4, a.divide(b));
        assertFraction(4, 3, b.divide(a));
        assertFraction(1, 1, b.divide(b));

        BigFraction f1 = new BigFraction(3, 5);
        BigFraction f2 = BigFraction.ZERO;
        try {
            f1.divide(f2);
            Assert.fail("expecting ArithmeticException");
        } catch (ZeroException ex) {
        }

        f1 = new BigFraction(0, 5);
        f2 = new BigFraction(2, 7);
        BigFraction f = f1.divide(f2);
        Assert.assertSame(BigFraction.ZERO, f);

        f1 = new BigFraction(2, 7);
        f2 = BigFraction.ONE;
        f = f1.divide(f2);
        Assert.assertEquals(2, f.getNumeratorAsInt());
        Assert.assertEquals(7, f.getDenominatorAsInt());

        f1 = new BigFraction(1, Integer.MAX_VALUE);
        f = f1.divide(f1);
        Assert.assertEquals(1, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f2 = new BigFraction(1, Integer.MAX_VALUE);
        f = f1.divide(f2);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        try {
            f.divide((BigFraction) null);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide(BigInteger.valueOf(Integer.MIN_VALUE));
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        Assert.assertEquals(1, f.getNumeratorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide(Integer.MIN_VALUE);
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        Assert.assertEquals(1, f.getNumeratorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide((long) Integer.MIN_VALUE);
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        Assert.assertEquals(1, f.getNumeratorAsInt());

    }

// org.apache.commons.math.fraction.BigFractionTest::testMultiply
    public void testMultiply() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(1, 4, a.multiply(a));
        assertFraction(1, 3, a.multiply(b));
        assertFraction(1, 3, b.multiply(a));
        assertFraction(4, 9, b.multiply(b));

        BigFraction f1 = new BigFraction(Integer.MAX_VALUE, 1);
        BigFraction f2 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        BigFraction f = f1.multiply(f2);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f = f2.multiply(Integer.MAX_VALUE);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f = f2.multiply((long) Integer.MAX_VALUE);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        try {
            f.multiply((BigFraction) null);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

    }

// org.apache.commons.math.fraction.BigFractionTest::testSubtract
    public void testSubtract() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(0, 1, a.subtract(a));
        assertFraction(-1, 6, a.subtract(b));
        assertFraction(1, 6, b.subtract(a));
        assertFraction(0, 1, b.subtract(b));

        BigFraction f = new BigFraction(1, 1);
        try {
            f.subtract((BigFraction) null);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

        
        
        BigFraction f1 = new BigFraction(1, 32768 * 3);
        BigFraction f2 = new BigFraction(1, 59049);
        f = f1.subtract(f2);
        Assert.assertEquals(-13085, f.getNumeratorAsInt());
        Assert.assertEquals(1934917632, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, 3);
        f2 = new BigFraction(1, 3).negate();
        f = f1.subtract(f2);
        Assert.assertEquals(Integer.MIN_VALUE + 1, f.getNumeratorAsInt());
        Assert.assertEquals(3, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE, 1);
        f2 = BigFraction.ONE;
        f = f1.subtract(f2);
        Assert.assertEquals(Integer.MAX_VALUE - 1, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

    }

// org.apache.commons.math.fraction.BigFractionTest::testBigDecimalValue
    public void testBigDecimalValue() {
        Assert.assertEquals(new BigDecimal(0.5), new BigFraction(1, 2).bigDecimalValue());
        Assert.assertEquals(new BigDecimal("0.0003"), new BigFraction(3, 10000).bigDecimalValue());
        Assert.assertEquals(new BigDecimal("0"), new BigFraction(1, 3).bigDecimalValue(BigDecimal.ROUND_DOWN));
        Assert.assertEquals(new BigDecimal("0.333"), new BigFraction(1, 3).bigDecimalValue(3, BigDecimal.ROUND_DOWN));
    }

// org.apache.commons.math.fraction.BigFractionTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BigFraction zero = new BigFraction(0, 1);
        BigFraction nullFraction = null;
        Assert.assertTrue(zero.equals(zero));
        Assert.assertFalse(zero.equals(nullFraction));
        Assert.assertFalse(zero.equals(Double.valueOf(0)));
        BigFraction zero2 = new BigFraction(0, 2);
        Assert.assertTrue(zero.equals(zero2));
        Assert.assertEquals(zero.hashCode(), zero2.hashCode());
        BigFraction one = new BigFraction(1, 1);
        Assert.assertFalse((one.equals(zero) || zero.equals(one)));
        Assert.assertTrue(one.equals(BigFraction.ONE));
    }

// org.apache.commons.math.fraction.BigFractionTest::testGetReducedFraction
    public void testGetReducedFraction() {
        BigFraction threeFourths = new BigFraction(3, 4);
        Assert.assertTrue(threeFourths.equals(BigFraction.getReducedFraction(6, 8)));
        Assert.assertTrue(BigFraction.ZERO.equals(BigFraction.getReducedFraction(0, -1)));
        try {
            BigFraction.getReducedFraction(1, 0);
            Assert.fail("expecting ZeroException");
        } catch (ZeroException ex) {
            
        }
        Assert.assertEquals(BigFraction.getReducedFraction(2, Integer.MIN_VALUE).getNumeratorAsInt(), -1);
        Assert.assertEquals(BigFraction.getReducedFraction(1, -1).getNumeratorAsInt(), -1);
    }

// org.apache.commons.math.fraction.BigFractionTest::testPercentage
    public void testPercentage() {
        Assert.assertEquals(50.0, new BigFraction(1, 2).percentageValue(), 1.0e-15);
    }

// org.apache.commons.math.fraction.BigFractionTest::testPow
    public void testPow() {
        Assert.assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(13));
        Assert.assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(13l));
        Assert.assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(BigInteger.valueOf(13l)));
        Assert.assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(0));
        Assert.assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(0l));
        Assert.assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(BigInteger.valueOf(0l)));
        Assert.assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(-13));
        Assert.assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(-13l));
        Assert.assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(BigInteger.valueOf(-13l)));
    }

// org.apache.commons.math.fraction.BigFractionTest::testMath340
    public void testMath340() {
        BigFraction fractionA = new BigFraction(0.00131);
        BigFraction fractionB = new BigFraction(.37).reciprocal();
        BigFraction errorResult = fractionA.multiply(fractionB);
        BigFraction correctResult = new BigFraction(fractionA.getNumerator().multiply(fractionB.getNumerator()),
                                                    fractionA.getDenominator().multiply(fractionB.getDenominator()));
        Assert.assertEquals(correctResult, errorResult);
    }

// org.apache.commons.math.fraction.BigFractionTest::testSerial
    public void testSerial() throws FractionConversionException {
        BigFraction[] fractions = {
            new BigFraction(3, 4), BigFraction.ONE, BigFraction.ZERO,
            new BigFraction(17), new BigFraction(FastMath.PI, 1000),
            new BigFraction(-5, 2)
        };
        for (BigFraction fraction : fractions) {
            Assert.assertEquals(fraction, TestUtils.serializeAndRecover(fraction));
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRealMatrix
    public void testCreateRealMatrix() {
        Assert.assertEquals(new BlockRealMatrix(testData),
                MatrixUtils.createRealMatrix(testData));
        try {
            MatrixUtils.createRealMatrix(new double[][] {{1}, {1,2}});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(new double[][] {{}, {}});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testcreateFieldMatrix
    public void testcreateFieldMatrix() {
        Assert.assertEquals(new Array2DRowFieldMatrix<Fraction>(asFraction(testData)),
                     MatrixUtils.createFieldMatrix(asFraction(testData)));
        Assert.assertEquals(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), fractionColMatrix),
                     MatrixUtils.createFieldMatrix(fractionColMatrix));
        try {
            MatrixUtils.createFieldMatrix(asFraction(new double[][] {{1}, {1,2}}));  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createFieldMatrix(asFraction(new double[][] {{}, {}}));  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createFieldMatrix((Fraction[][])null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRowRealMatrix
    public void testCreateRowRealMatrix() {
        Assert.assertEquals(MatrixUtils.createRowRealMatrix(row),
                     new BlockRealMatrix(rowMatrix));
        try {
            MatrixUtils.createRowRealMatrix(new double[] {});  
            Assert.fail("Expecting NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            MatrixUtils.createRowRealMatrix(null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRowFieldMatrix
    public void testCreateRowFieldMatrix() {
        Assert.assertEquals(MatrixUtils.createRowFieldMatrix(asFraction(row)),
                     new Array2DRowFieldMatrix<Fraction>(asFraction(rowMatrix)));
        Assert.assertEquals(MatrixUtils.createRowFieldMatrix(fractionRow),
                     new Array2DRowFieldMatrix<Fraction>(fractionRowMatrix));
        try {
            MatrixUtils.createRowFieldMatrix(new Fraction[] {});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRowFieldMatrix((Fraction[]) null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateColumnRealMatrix
    public void testCreateColumnRealMatrix() {
        Assert.assertEquals(MatrixUtils.createColumnRealMatrix(col),
                     new BlockRealMatrix(colMatrix));
        try {
            MatrixUtils.createColumnRealMatrix(new double[] {});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnRealMatrix(null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateColumnFieldMatrix
    public void testCreateColumnFieldMatrix() {
        Assert.assertEquals(MatrixUtils.createColumnFieldMatrix(asFraction(col)),
                     new Array2DRowFieldMatrix<Fraction>(asFraction(colMatrix)));
        Assert.assertEquals(MatrixUtils.createColumnFieldMatrix(fractionCol),
                     new Array2DRowFieldMatrix<Fraction>(fractionColMatrix));

        try {
            MatrixUtils.createColumnFieldMatrix(new Fraction[] {});  
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnFieldMatrix((Fraction[]) null);  
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateIdentityMatrix
    public void testCreateIdentityMatrix() {
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(3));
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(2));
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testcreateFieldIdentityMatrix
    public void testcreateFieldIdentityMatrix() {
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 3));
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 2));
        checkIdentityFieldMatrix(MatrixUtils.createFieldIdentityMatrix(FractionField.getInstance(), 1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testBigFractionConverter
    public void testBigFractionConverter() {
        BigFraction[][] bfData = {
                { new BigFraction(1), new BigFraction(2), new BigFraction(3) },
                { new BigFraction(2), new BigFraction(5), new BigFraction(3) },
                { new BigFraction(1), new BigFraction(0), new BigFraction(8) }
        };
        FieldMatrix<BigFraction> m = new Array2DRowFieldMatrix<BigFraction>(bfData, false);
        RealMatrix converted = MatrixUtils.bigFractionMatrixToRealMatrix(m);
        RealMatrix reference = new Array2DRowRealMatrix(testData, false);
        Assert.assertEquals(0.0, converted.subtract(reference).getNorm(), 0.0);
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testFractionConverter
    public void testFractionConverter() {
        Fraction[][] fData = {
                { new Fraction(1), new Fraction(2), new Fraction(3) },
                { new Fraction(2), new Fraction(5), new Fraction(3) },
                { new Fraction(1), new Fraction(0), new Fraction(8) }
        };
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(fData, false);
        RealMatrix converted = MatrixUtils.fractionMatrixToRealMatrix(m);
        RealMatrix reference = new Array2DRowRealMatrix(testData, false);
        Assert.assertEquals(0.0, converted.subtract(reference).getNorm(), 0.0);
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testSolveLowerTriangularSystem
    public void testSolveLowerTriangularSystem(){
        RealMatrix rm = new Array2DRowRealMatrix(
                new double[][] { {2,0,0,0 }, { 1,1,0,0 }, { 3,3,3,0 }, { 3,3,3,4 } },
                       false);
        RealVector b = new ArrayRealVector(new double[] { 2,3,4,8 }, false);
        MatrixUtils.solveLowerTriangularSystem(rm, b);
        TestUtils.assertEquals( new double[]{1,2,-1.66666666666667, 1.0}  , b.toArray() , 1.0e-12);
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testSolveUpperTriangularSystem
    public void testSolveUpperTriangularSystem(){
        RealMatrix rm = new Array2DRowRealMatrix(
                new double[][] { {1,2,3 }, { 0,1,1 }, { 0,0,2 } },
                       false);
        RealVector b = new ArrayRealVector(new double[] { 8,4,2 }, false);
        MatrixUtils.solveUpperTriangularSystem(rm, b);
        TestUtils.assertEquals( new double[]{-1,3,1}  , b.toArray() , 1.0e-12);
    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::dimensionCheck
    public void dimensionCheck() {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsBashforthIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testMinStep
    public void testMinStep() {

          TestProblem1 pb = new TestProblem1();
          double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
          double maxStep = pb.getFinalTime() - pb.getInitialTime();
          double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
          double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

          FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, minStep, maxStep,
                                                                    vecAbsoluteTolerance,
                                                                    vecRelativeTolerance);
          TestProblemHandler handler = new TestProblemHandler(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(pb,
                          pb.getInitialTime(), pb.getInitialState(),
                          pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance()
        {

        int previousCalls = Integer.MAX_VALUE;
        for (int i = -12; i < -5; ++i) {
            TestProblem1 pb = new TestProblem1();
            double minStep = 0;
            double maxStep = pb.getFinalTime() - pb.getInitialTime();
            double scalAbsoluteTolerance = FastMath.pow(10.0, i);
            double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

            FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb,
                            pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);

            
            
            
            Assert.assertTrue(handler.getMaximalValueError() > (50.0 * scalAbsoluteTolerance));
            Assert.assertTrue(handler.getMaximalValueError() < (300.0 * scalAbsoluteTolerance));
            Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);

            int calls = pb.getCalls();
            Assert.assertEquals(integ.getEvaluations(), calls);
            Assert.assertTrue(calls <= previousCalls);
            previousCalls = calls;

        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations() {

        TestProblem1 pb  = new TestProblem1();
        double range = pb.getFinalTime() - pb.getInitialTime();

        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(2, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.setMaxEvaluations(650);
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::backward
    public void backward() {

        TestProblem5 pb = new TestProblem5();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        Assert.assertTrue(handler.getLastError() < 1.5e-8);
        Assert.assertTrue(handler.getMaximalValueError() < 1.5e-8);
        Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);
        Assert.assertEquals("Adams-Bashforth", integ.getName());
    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::polynomial
    public void polynomial() {
        TestProblem6 pb = new TestProblem6();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        for (int nSteps = 2; nSteps < 8; ++nSteps) {
            AdamsBashforthIntegrator integ =
                new AdamsBashforthIntegrator(nSteps, 1.0e-6 * range, 0.1 * range, 1.0e-5, 1.0e-5);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);
            if (nSteps < 4) {
                Assert.assertTrue(handler.getMaximalValueError() > 1.0e-03);
            } else {
                Assert.assertTrue(handler.getMaximalValueError() < 4.0e-12);
            }
        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::dimensionCheck
    public void dimensionCheck() {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsMoultonIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testMinStep
    public void testMinStep() {

          TestProblem1 pb = new TestProblem1();
          double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
          double maxStep = pb.getFinalTime() - pb.getInitialTime();
          double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
          double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

          FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, minStep, maxStep,
                                                                  vecAbsoluteTolerance,
                                                                  vecRelativeTolerance);
          TestProblemHandler handler = new TestProblemHandler(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(pb,
                          pb.getInitialTime(), pb.getInitialState(),
                          pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance()
        {

        int previousCalls = Integer.MAX_VALUE;
        for (int i = -12; i < -2; ++i) {
            TestProblem1 pb = new TestProblem1();
            double minStep = 0;
            double maxStep = pb.getFinalTime() - pb.getInitialTime();
            double scalAbsoluteTolerance = FastMath.pow(10.0, i);
            double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

            FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, minStep, maxStep,
                                                                    scalAbsoluteTolerance,
                                                                    scalRelativeTolerance);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb,
                            pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);

            
            
            
            Assert.assertTrue(handler.getMaximalValueError() > ( 0.5 * scalAbsoluteTolerance));
            Assert.assertTrue(handler.getMaximalValueError() < (11.0 * scalAbsoluteTolerance));
            Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);

            int calls = pb.getCalls();
            Assert.assertEquals(integ.getEvaluations(), calls);
            Assert.assertTrue(calls <= previousCalls);
            previousCalls = calls;

        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations() {

        TestProblem1 pb  = new TestProblem1();
        double range = pb.getFinalTime() - pb.getInitialTime();

        AdamsMoultonIntegrator integ = new AdamsMoultonIntegrator(2, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.setMaxEvaluations(650);
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::backward
    public void backward() {

        TestProblem5 pb = new TestProblem5();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        Assert.assertTrue(handler.getLastError() < 1.0e-9);
        Assert.assertTrue(handler.getMaximalValueError() < 1.0e-9);
        Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);
        Assert.assertEquals("Adams-Moulton", integ.getName());
    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::polynomial
    public void polynomial() {
        TestProblem6 pb = new TestProblem6();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        for (int nSteps = 2; nSteps < 8; ++nSteps) {
            AdamsMoultonIntegrator integ =
                new AdamsMoultonIntegrator(nSteps, 1.0e-6 * range, 0.1 * range, 1.0e-5, 1.0e-5);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);
            if (nSteps < 4) {
                Assert.assertTrue(handler.getMaximalValueError() > 7.0e-04);
            } else {
                Assert.assertTrue(handler.getMaximalValueError() < 3.0e-13);
            }
        }

    }

// org.apache.commons.math.ode.sampling.NordsieckStepInterpolatorTest::derivativesConsistency
    public void derivativesConsistency() {
        TestProblem3 pb = new TestProblem3();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 5e-9);
    }

// org.apache.commons.math.ode.sampling.NordsieckStepInterpolatorTest::serialization
    public void serialization()
    throws IOException, ClassNotFoundException {

        TestProblem1 pb = new TestProblem1();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.addStepHandler(new ContinuousOutputModel());
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream    oos = new ObjectOutputStream(bos);
        for (StepHandler handler : integ.getStepHandlers()) {
            oos.writeObject(handler);
        }

        Assert.assertTrue(bos.size () >  25500);
        Assert.assertTrue(bos.size () <  26500);

        ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream     ois = new ObjectInputStream(bis);
        ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

        Random random = new Random(347588535632l);
        double maxError = 0.0;
        for (int i = 0; i < 1000; ++i) {
            double r = random.nextDouble();
            double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
            cm.setInterpolatedTime(time);
            double[] interpolatedY = cm.getInterpolatedState ();
            double[] theoreticalY  = pb.computeTheoreticalState(time);
            double dx = interpolatedY[0] - theoreticalY[0];
            double dy = interpolatedY[1] - theoreticalY[1];
            double error = dx * dx + dy * dy;
            if (error > maxError) {
                maxError = error;
            }
        }

        Assert.assertTrue(maxError < 1.0e-6);

    }
