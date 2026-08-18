// buggy code
    private BigFraction(final double value, final double epsilon,
                        final int maxDenominator, int maxIterations)
        throws FractionConversionException {
        long overflow = Integer.MAX_VALUE;
        double r0 = value;
        long a0 = (long) FastMath.floor(r0);
        if (a0 > overflow) {
            throw new FractionConversionException(value, a0, 1l);
        }

        // check for (almost) integer arguments, which should not go
        // to iterations.
        if (FastMath.abs(a0 - value) < epsilon) {
            numerator = BigInteger.valueOf(a0);
            denominator = BigInteger.ONE;
            return;
        }

        long p0 = 1;
        long q0 = 0;
        long p1 = a0;
        long q1 = 1;

        long p2 = 0;
        long q2 = 1;

        int n = 0;
        boolean stop = false;
        do {
            ++n;
            final double r1 = 1.0 / (r0 - a0);
            final long a1 = (long) FastMath.floor(r1);
            p2 = (a1 * p1) + p0;
            q2 = (a1 * q1) + q0;
            if ((p2 > overflow) || (q2 > overflow)) {
                // in maxDenominator mode, if the last fraction was very close to the actual value
                // q2 may overflow in the next iteration; in this case return the last one.
                throw new FractionConversionException(value, p2, q2);
            }

            final double convergent = (double) p2 / (double) q2;
            if ((n < maxIterations) &&
                (FastMath.abs(convergent - value) > epsilon) &&
                (q2 < maxDenominator)) {
                p0 = p1;
                p1 = p2;
                q0 = q1;
                q1 = q2;
                a0 = a1;
                r0 = r1;
            } else {
                stop = true;
            }
        } while (!stop);

        if (n >= maxIterations) {
            throw new FractionConversionException(value, maxIterations);
        }

        if (q2 < maxDenominator) {
            numerator   = BigInteger.valueOf(p2);
            denominator = BigInteger.valueOf(q2);
        } else {
            numerator   = BigInteger.valueOf(p1);
            denominator = BigInteger.valueOf(q1);
        }
    }

    private Fraction(double value, double epsilon, int maxDenominator, int maxIterations)
        throws FractionConversionException
    {
        long overflow = Integer.MAX_VALUE;
        double r0 = value;
        long a0 = (long)FastMath.floor(r0);
        if (FastMath.abs(a0) > overflow) {
            throw new FractionConversionException(value, a0, 1l);
        }

        // check for (almost) integer arguments, which should not go to iterations.
        if (FastMath.abs(a0 - value) < epsilon) {
            this.numerator = (int) a0;
            this.denominator = 1;
            return;
        }

        long p0 = 1;
        long q0 = 0;
        long p1 = a0;
        long q1 = 1;

        long p2 = 0;
        long q2 = 1;

        int n = 0;
        boolean stop = false;
        do {
            ++n;
            double r1 = 1.0 / (r0 - a0);
            long a1 = (long)FastMath.floor(r1);
            p2 = (a1 * p1) + p0;
            q2 = (a1 * q1) + q0;

            if ((FastMath.abs(p2) > overflow) || (FastMath.abs(q2) > overflow)) {
                // in maxDenominator mode, if the last fraction was very close to the actual value
                // q2 may overflow in the next iteration; in this case return the last one.
                throw new FractionConversionException(value, p2, q2);
            }

            double convergent = (double)p2 / (double)q2;
            if (n < maxIterations && FastMath.abs(convergent - value) > epsilon && q2 < maxDenominator) {
                p0 = p1;
                p1 = p2;
                q0 = q1;
                q1 = q2;
                a0 = a1;
                r0 = r1;
            } else {
                stop = true;
            }
        } while (!stop);

        if (n >= maxIterations) {
            throw new FractionConversionException(value, maxIterations);
        }

        if (q2 < maxDenominator) {
            this.numerator = (int) p2;
            this.denominator = (int) q2;
        } else {
            this.numerator = (int) p1;
            this.denominator = (int) q1;
        }

    }

// relevant test
// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testZero
    public void testZero() {
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(0), new BigFraction[] { new BigFraction(0) });
        for (int x = -10; x < 10; x++) {
            BigFraction y = interpolator.value(new BigFraction(x))[0];
            Assert.assertEquals(BigFraction.ZERO, y);
            BigFraction[][] derivatives = interpolator.derivatives(new BigFraction(x), 1);
            Assert.assertEquals(BigFraction.ZERO, derivatives[0][0]);
            Assert.assertEquals(BigFraction.ZERO, derivatives[1][0]);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testQuadratic
    public void testQuadratic() {
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(0), new BigFraction[] { new BigFraction(2) });
        interpolator.addSamplePoint(new BigFraction(1), new BigFraction[] { new BigFraction(0) });
        interpolator.addSamplePoint(new BigFraction(2), new BigFraction[] { new BigFraction(0) });
        for (double x = -10; x < 10; x += 1.0) {
            BigFraction y = interpolator.value(new BigFraction(x))[0];
            Assert.assertEquals((x - 1) * (x - 2), y.doubleValue(), 1.0e-15);
            BigFraction[][] derivatives = interpolator.derivatives(new BigFraction(x), 3);
            Assert.assertEquals((x - 1) * (x - 2), derivatives[0][0].doubleValue(), 1.0e-15);
            Assert.assertEquals(2 * x - 3, derivatives[1][0].doubleValue(), 1.0e-15);
            Assert.assertEquals(2, derivatives[2][0].doubleValue(), 1.0e-15);
            Assert.assertEquals(0, derivatives[3][0].doubleValue(), 1.0e-15);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testMixedDerivatives
    public void testMixedDerivatives() {
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(0), new BigFraction[] { new BigFraction(1) }, new BigFraction[] { new BigFraction(2) });
        interpolator.addSamplePoint(new BigFraction(1), new BigFraction[] { new BigFraction(4) });
        interpolator.addSamplePoint(new BigFraction(2), new BigFraction[] { new BigFraction(5) }, new BigFraction[] { new BigFraction(2) });
        BigFraction[][] derivatives = interpolator.derivatives(new BigFraction(0), 5);
        Assert.assertEquals(new BigFraction(  1), derivatives[0][0]);
        Assert.assertEquals(new BigFraction(  2), derivatives[1][0]);
        Assert.assertEquals(new BigFraction(  8), derivatives[2][0]);
        Assert.assertEquals(new BigFraction(-24), derivatives[3][0]);
        Assert.assertEquals(new BigFraction( 24), derivatives[4][0]);
        Assert.assertEquals(new BigFraction(  0), derivatives[5][0]);
        derivatives = interpolator.derivatives(new BigFraction(1), 5);
        Assert.assertEquals(new BigFraction(  4), derivatives[0][0]);
        Assert.assertEquals(new BigFraction(  2), derivatives[1][0]);
        Assert.assertEquals(new BigFraction( -4), derivatives[2][0]);
        Assert.assertEquals(new BigFraction(  0), derivatives[3][0]);
        Assert.assertEquals(new BigFraction( 24), derivatives[4][0]);
        Assert.assertEquals(new BigFraction(  0), derivatives[5][0]);
        derivatives = interpolator.derivatives(new BigFraction(2), 5);
        Assert.assertEquals(new BigFraction(  5), derivatives[0][0]);
        Assert.assertEquals(new BigFraction(  2), derivatives[1][0]);
        Assert.assertEquals(new BigFraction(  8), derivatives[2][0]);
        Assert.assertEquals(new BigFraction( 24), derivatives[3][0]);
        Assert.assertEquals(new BigFraction( 24), derivatives[4][0]);
        Assert.assertEquals(new BigFraction(  0), derivatives[5][0]);
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testRandomPolynomialsValuesOnly
    public void testRandomPolynomialsValuesOnly() {

        Random random = new Random(0x42b1e7dbd361a932l);

        for (int i = 0; i < 100; ++i) {

            int maxDegree = 0;
            PolynomialFunction[] p = new PolynomialFunction[5];
            for (int k = 0; k < p.length; ++k) {
                int degree = random.nextInt(7);
                p[k] = randomPolynomial(degree, random);
                maxDegree = FastMath.max(maxDegree, degree);
            }

            DfpField field = new DfpField(30);
            Dfp step = field.getOne().divide(field.newDfp(10));
            FieldHermiteInterpolator<Dfp> interpolator = new FieldHermiteInterpolator<Dfp>();
            for (int j = 0; j < 1 + maxDegree; ++j) {
                Dfp x = field.newDfp(j).multiply(step);
                Dfp[] values = new Dfp[p.length];
                for (int k = 0; k < p.length; ++k) {
                    values[k] = field.newDfp(p[k].value(x.getReal()));
                }
                interpolator.addSamplePoint(x, values);
            }

            for (int j = 0; j < 20; ++j) {
                Dfp x = field.newDfp(j).multiply(step);
                Dfp[] values = interpolator.value(x);
                Assert.assertEquals(p.length, values.length);
                for (int k = 0; k < p.length; ++k) {
                    Assert.assertEquals(p[k].value(x.getReal()),
                                        values[k].getReal(),
                                        1.0e-8 * FastMath.abs(p[k].value(x.getReal())));
                }
            }

        }

    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testRandomPolynomialsFirstDerivative
    public void testRandomPolynomialsFirstDerivative() {

        Random random = new Random(0x570803c982ca5d3bl);

        for (int i = 0; i < 100; ++i) {

            int maxDegree = 0;
            PolynomialFunction[] p      = new PolynomialFunction[5];
            PolynomialFunction[] pPrime = new PolynomialFunction[5];
            for (int k = 0; k < p.length; ++k) {
                int degree = random.nextInt(7);
                p[k]      = randomPolynomial(degree, random);
                pPrime[k] = p[k].polynomialDerivative();
                maxDegree = FastMath.max(maxDegree, degree);
            }

            DfpField field = new DfpField(30);
            Dfp step = field.getOne().divide(field.newDfp(10));
            FieldHermiteInterpolator<Dfp> interpolator = new FieldHermiteInterpolator<Dfp>();
            for (int j = 0; j < 1 + maxDegree / 2; ++j) {
                Dfp x = field.newDfp(j).multiply(step);
                Dfp[] values      = new Dfp[p.length];
                Dfp[] derivatives = new Dfp[p.length];
                for (int k = 0; k < p.length; ++k) {
                    values[k]      = field.newDfp(p[k].value(x.getReal()));
                    derivatives[k] = field.newDfp(pPrime[k].value(x.getReal()));
                }
                interpolator.addSamplePoint(x, values, derivatives);
            }

            Dfp h = step.divide(field.newDfp(100000));
            for (int j = 0; j < 20; ++j) {
                Dfp x = field.newDfp(j).multiply(step);
                Dfp[] y  = interpolator.value(x);
                Dfp[] yP = interpolator.value(x.add(h));
                Dfp[] yM = interpolator.value(x.subtract(h));
                Assert.assertEquals(p.length, y.length);
                for (int k = 0; k < p.length; ++k) {
                    Assert.assertEquals(p[k].value(x.getReal()),
                                        y[k].getReal(),
                                        1.0e-8 * FastMath.abs(p[k].value(x.getReal())));
                    Assert.assertEquals(pPrime[k].value(x.getReal()),
                                        yP[k].subtract(yM[k]).divide(h.multiply(2)).getReal(),
                                        4.0e-8 * FastMath.abs(p[k].value(x.getReal())));
                }
            }

        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testSine
    public void testSine() {
        DfpField field = new DfpField(30);
        FieldHermiteInterpolator<Dfp> interpolator = new FieldHermiteInterpolator<Dfp>();
        for (Dfp x = field.getZero(); x.getReal() < FastMath.PI; x = x.add(0.5)) {
            interpolator.addSamplePoint(x, new Dfp[] { x.sin() });
        }
        for (Dfp x = field.newDfp(0.1); x.getReal() < 2.9; x = x.add(0.01)) {
            Dfp y = interpolator.value(x)[0];
            Assert.assertEquals( x.sin().getReal(), y.getReal(), 3.5e-5);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testSquareRoot
    public void testSquareRoot() {
        DfpField field = new DfpField(30);
        FieldHermiteInterpolator<Dfp> interpolator = new FieldHermiteInterpolator<Dfp>();
        for (Dfp x = field.getOne(); x.getReal() < 3.6; x = x.add(0.5)) {
            interpolator.addSamplePoint(x, new Dfp[] { x.sqrt() });
        }
        for (Dfp x = field.newDfp(1.1); x.getReal() < 3.5; x = x.add(0.01)) {
            Dfp y = interpolator.value(x)[0];
            Assert.assertEquals(x.sqrt().getReal(), y.getReal(), 1.5e-4);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testWikipedia
    public void testWikipedia() {
        
        
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(-1),
                                    new BigFraction[] { new BigFraction( 2) },
                                    new BigFraction[] { new BigFraction(-8) },
                                    new BigFraction[] { new BigFraction(56) });
        interpolator.addSamplePoint(new BigFraction( 0),
                                    new BigFraction[] { new BigFraction( 1) },
                                    new BigFraction[] { new BigFraction( 0) },
                                    new BigFraction[] { new BigFraction( 0) });
        interpolator.addSamplePoint(new BigFraction( 1),
                                    new BigFraction[] { new BigFraction( 2) },
                                    new BigFraction[] { new BigFraction( 8) },
                                    new BigFraction[] { new BigFraction(56) });
        for (BigFraction x = new BigFraction(-1); x.doubleValue() <= 1.0; x = x.add(new BigFraction(1, 8))) {
            BigFraction y = interpolator.value(x)[0];
            BigFraction x2 = x.multiply(x);
            BigFraction x4 = x2.multiply(x2);
            BigFraction x8 = x4.multiply(x4);
            Assert.assertEquals(x8.add(new BigFraction(1)), y);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testOnePointParabola
    public void testOnePointParabola() {
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(0),
                                    new BigFraction[] { new BigFraction(1) },
                                    new BigFraction[] { new BigFraction(1) },
                                    new BigFraction[] { new BigFraction(2) });
        for (BigFraction x = new BigFraction(-1); x.doubleValue() <= 1.0; x = x.add(new BigFraction(1, 8))) {
            BigFraction y = interpolator.value(x)[0];
            Assert.assertEquals(BigFraction.ONE.add(x.multiply(BigFraction.ONE.add(x))), y);
        }
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testEmptySampleValue
    public void testEmptySampleValue() {
        new FieldHermiteInterpolator<BigFraction>().value(BigFraction.ZERO);
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testEmptySampleDerivative
    public void testEmptySampleDerivative() {
        new FieldHermiteInterpolator<BigFraction>().derivatives(BigFraction.ZERO, 1);
    }

// org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest::testDuplicatedAbscissa
    public void testDuplicatedAbscissa() {
        FieldHermiteInterpolator<BigFraction> interpolator = new FieldHermiteInterpolator<BigFraction>();
        interpolator.addSamplePoint(new BigFraction(1), new BigFraction[] { new BigFraction(0) });
        interpolator.addSamplePoint(new BigFraction(1), new BigFraction[] { new BigFraction(1) });
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstChebyshevPolynomials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testChebyshevBounds
    public void testChebyshevBounds() {
        for (int k = 0; k < 12; ++k) {
            PolynomialFunction Tk = PolynomialsUtils.createChebyshevPolynomial(k);
            for (double x = -1; x <= 1; x += 0.02) {
                Assert.assertTrue(k + " " + Tk.value(x), FastMath.abs(Tk.value(x)) < (1 + 1e-12));
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testChebyshevDifferentials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testChebyshevOrthogonality
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstHermitePolynomials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testHermiteDifferentials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testHermiteOrthogonality
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstLaguerrePolynomials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLaguerreDifferentials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLaguerreOrthogonality
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testFirstLegendrePolynomials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLegendreDifferentials
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testLegendreOrthogonality
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testHighDegreeLegendre
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testJacobiLegendre
    public void testJacobiLegendre() {
        for (int i = 0; i < 10; ++i) {
            PolynomialFunction legendre = PolynomialsUtils.createLegendrePolynomial(i);
            PolynomialFunction jacobi   = PolynomialsUtils.createJacobiPolynomial(i, 0, 0);
            checkNullPolynomial(legendre.subtract(jacobi));
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testJacobiEvaluationAt1
    public void testJacobiEvaluationAt1() {
        for (int v = 0; v < 10; ++v) {
            for (int w = 0; w < 10; ++w) {
                for (int i = 0; i < 10; ++i) {
                    PolynomialFunction jacobi = PolynomialsUtils.createJacobiPolynomial(i, v, w);
                    double binomial = CombinatoricsUtils.binomialCoefficient(v + i, i);
                    Assert.assertTrue(Precision.equals(binomial, jacobi.value(1.0), 1));
                }
            }
        }
    }

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testJacobiOrthogonality
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

// org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest::testShift
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

// org.apache.commons.math3.distribution.KolmogorovSmirnovDistributionTest::testCumulativeDensityFunction
    public void testCumulativeDensityFunction() {
        
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

// org.apache.commons.math3.fraction.BigFractionFieldTest::testZero
    public void testZero() {
        Assert.assertEquals(BigFraction.ZERO, BigFractionField.getInstance().getZero());
    }

// org.apache.commons.math3.fraction.BigFractionFieldTest::testOne
    public void testOne() {
        Assert.assertEquals(BigFraction.ONE, BigFractionField.getInstance().getOne());
    }

// org.apache.commons.math3.fraction.BigFractionFieldTest::testSerial
    public void testSerial() {
        
        BigFractionField field = BigFractionField.getInstance();
        Assert.assertTrue(field == TestUtils.serializeAndRecover(field));
    }

// org.apache.commons.math3.fraction.BigFractionFormatTest::testFormat
    public void testFormat() {
        BigFraction c = new BigFraction(1, 2);
        String expected = "1 / 2";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.fraction.BigFractionFormatTest::testFormatNegative
    public void testFormatNegative() {
        BigFraction c = new BigFraction(-1, 2);
        String expected = "-1 / 2";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.fraction.BigFractionFormatTest::testFormatZero
    public void testFormatZero() {
        BigFraction c = new BigFraction(0, 1);
        String expected = "0 / 1";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.fraction.BigFractionFormatTest::testFormatImproper
    public void testFormatImproper() {
        BigFraction c = new BigFraction(5, 3);

        String actual = properFormat.format(c);
        Assert.assertEquals("1 2 / 3", actual);

        actual = improperFormat.format(c);
        Assert.assertEquals("5 / 3", actual);
    }

// org.apache.commons.math3.fraction.BigFractionFormatTest::testFormatImproperNegative
    public void testFormatImproperNegative() {
        BigFraction c = new BigFraction(-5, 3);

        String actual = properFormat.format(c);
        Assert.assertEquals("-1 2 / 3", actual);

        actual = improperFormat.format(c);
        Assert.assertEquals("-5 / 3", actual);
    }

// org.apache.commons.math3.fraction.BigFractionFormatTest::testParse
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testParseInteger
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testParseInvalid
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testParseInvalidDenominator
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testParseNegative
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testParseProper
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testParseProperNegative
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testParseProperInvalidMinus
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testParseBig
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testNumeratorFormat
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testDenominatorFormat
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

// org.apache.commons.math3.fraction.BigFractionFormatTest::testWholeFormat
    public void testWholeFormat() {
        ProperBigFractionFormat format = (ProperBigFractionFormat)properFormat;

        NumberFormat old = format.getWholeFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        format.setWholeFormat(nf);
        Assert.assertEquals(nf, format.getWholeFormat());
        format.setWholeFormat(old);
    }

// org.apache.commons.math3.fraction.BigFractionFormatTest::testLongFormat
    public void testLongFormat() {
        Assert.assertEquals("10 / 1", improperFormat.format(10l));
    }

// org.apache.commons.math3.fraction.BigFractionFormatTest::testDoubleFormat
    public void testDoubleFormat() {
        Assert.assertEquals("1 / 16", improperFormat.format(0.0625));
    }

// org.apache.commons.math3.fraction.BigFractionTest::testConstructor
    public void testConstructor() {
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

// org.apache.commons.math3.fraction.BigFractionTest::testGoldenRatio
    public void testGoldenRatio() {
        
        new BigFraction((1 + FastMath.sqrt(5)) / 2, 1.0e-12, 25);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testDoubleConstructor
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

// org.apache.commons.math3.fraction.BigFractionTest::testDigitLimitConstructor
    public void testDigitLimitConstructor() throws ConvergenceException {
        assertFraction(2, 5, new BigFraction(0.4, 9));
        assertFraction(2, 5, new BigFraction(0.4, 99));
        assertFraction(2, 5, new BigFraction(0.4, 999));

        assertFraction(3, 5, new BigFraction(0.6152, 9));
        assertFraction(8, 13, new BigFraction(0.6152, 99));
        assertFraction(510, 829, new BigFraction(0.6152, 999));
        assertFraction(769, 1250, new BigFraction(0.6152, 9999));
        
        
        assertFraction(1, 2, new BigFraction(0.5000000001, 10));
    }

// org.apache.commons.math3.fraction.BigFractionTest::testEpsilonLimitConstructor
    public void testEpsilonLimitConstructor() throws ConvergenceException {
        assertFraction(2, 5, new BigFraction(0.4, 1.0e-5, 100));

        assertFraction(3, 5, new BigFraction(0.6152, 0.02, 100));
        assertFraction(8, 13, new BigFraction(0.6152, 1.0e-3, 100));
        assertFraction(251, 408, new BigFraction(0.6152, 1.0e-4, 100));
        assertFraction(251, 408, new BigFraction(0.6152, 1.0e-5, 100));
        assertFraction(510, 829, new BigFraction(0.6152, 1.0e-6, 100));
        assertFraction(769, 1250, new BigFraction(0.6152, 1.0e-7, 100));
    }

// org.apache.commons.math3.fraction.BigFractionTest::testCompareTo
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

// org.apache.commons.math3.fraction.BigFractionTest::testDoubleValue
    public void testDoubleValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);

        Assert.assertEquals(0.5, first.doubleValue(), 0.0);
        Assert.assertEquals(1.0 / 3.0, second.doubleValue(), 0.0);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testDoubleValueForLargeNumeratorAndDenominator
    public void testDoubleValueForLargeNumeratorAndDenominator() {
        final BigInteger pow400 = BigInteger.TEN.pow(400);
        final BigInteger pow401 = BigInteger.TEN.pow(401);
        final BigInteger two = new BigInteger("2");
        final BigFraction large = new BigFraction(pow401.add(BigInteger.ONE),
                                                  pow400.multiply(two));

        Assert.assertEquals(5, large.doubleValue(), 1e-15);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testFloatValueForLargeNumeratorAndDenominator
    public void testFloatValueForLargeNumeratorAndDenominator() {
        final BigInteger pow400 = BigInteger.TEN.pow(400);
        final BigInteger pow401 = BigInteger.TEN.pow(401);
        final BigInteger two = new BigInteger("2");
        final BigFraction large = new BigFraction(pow401.add(BigInteger.ONE),
                                                  pow400.multiply(two));

        Assert.assertEquals(5, large.floatValue(), 1e-15);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testFloatValue
    public void testFloatValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);

        Assert.assertEquals(0.5f, first.floatValue(), 0.0f);
        Assert.assertEquals((float) (1.0 / 3.0), second.floatValue(), 0.0f);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testIntValue
    public void testIntValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(3, 2);

        Assert.assertEquals(0, first.intValue());
        Assert.assertEquals(1, second.intValue());
    }

// org.apache.commons.math3.fraction.BigFractionTest::testLongValue
    public void testLongValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(3, 2);

        Assert.assertEquals(0L, first.longValue());
        Assert.assertEquals(1L, second.longValue());
    }

// org.apache.commons.math3.fraction.BigFractionTest::testConstructorDouble
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

// org.apache.commons.math3.fraction.BigFractionTest::testAbs
    public void testAbs() {
        BigFraction a = new BigFraction(10, 21);
        BigFraction b = new BigFraction(-10, 21);
        BigFraction c = new BigFraction(10, -21);

        assertFraction(10, 21, a.abs());
        assertFraction(10, 21, b.abs());
        assertFraction(10, 21, c.abs());
    }

// org.apache.commons.math3.fraction.BigFractionTest::testReciprocal
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

// org.apache.commons.math3.fraction.BigFractionTest::testNegate
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

// org.apache.commons.math3.fraction.BigFractionTest::testAdd
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

// org.apache.commons.math3.fraction.BigFractionTest::testDivide
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
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
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

// org.apache.commons.math3.fraction.BigFractionTest::testMultiply
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

// org.apache.commons.math3.fraction.BigFractionTest::testSubtract
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

// org.apache.commons.math3.fraction.BigFractionTest::testBigDecimalValue
    public void testBigDecimalValue() {
        Assert.assertEquals(new BigDecimal(0.5), new BigFraction(1, 2).bigDecimalValue());
        Assert.assertEquals(new BigDecimal("0.0003"), new BigFraction(3, 10000).bigDecimalValue());
        Assert.assertEquals(new BigDecimal("0"), new BigFraction(1, 3).bigDecimalValue(BigDecimal.ROUND_DOWN));
        Assert.assertEquals(new BigDecimal("0.333"), new BigFraction(1, 3).bigDecimalValue(3, BigDecimal.ROUND_DOWN));
    }

// org.apache.commons.math3.fraction.BigFractionTest::testEqualsAndHashCode
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

// org.apache.commons.math3.fraction.BigFractionTest::testGetReducedFraction
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

// org.apache.commons.math3.fraction.BigFractionTest::testPercentage
    public void testPercentage() {
        Assert.assertEquals(50.0, new BigFraction(1, 2).percentageValue(), 1.0e-15);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testPow
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

// org.apache.commons.math3.fraction.BigFractionTest::testMath340
    public void testMath340() {
        BigFraction fractionA = new BigFraction(0.00131);
        BigFraction fractionB = new BigFraction(.37).reciprocal();
        BigFraction errorResult = fractionA.multiply(fractionB);
        BigFraction correctResult = new BigFraction(fractionA.getNumerator().multiply(fractionB.getNumerator()),
                                                    fractionA.getDenominator().multiply(fractionB.getDenominator()));
        Assert.assertEquals(correctResult, errorResult);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testSerial
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

// org.apache.commons.math3.fraction.FractionFieldTest::testZero
    public void testZero() {
        Assert.assertEquals(Fraction.ZERO, FractionField.getInstance().getZero());
    }

// org.apache.commons.math3.fraction.FractionFieldTest::testOne
    public void testOne() {
        Assert.assertEquals(Fraction.ONE, FractionField.getInstance().getOne());
    }

// org.apache.commons.math3.fraction.FractionFieldTest::testSerial
    public void testSerial() {
        
        FractionField field = FractionField.getInstance();
        Assert.assertTrue(field == TestUtils.serializeAndRecover(field));
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testFormat
    public void testFormat() {
        Fraction c = new Fraction(1, 2);
        String expected = "1 / 2";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testFormatNegative
    public void testFormatNegative() {
        Fraction c = new Fraction(-1, 2);
        String expected = "-1 / 2";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testFormatZero
    public void testFormatZero() {
        Fraction c = new Fraction(0, 1);
        String expected = "0 / 1";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testFormatImproper
    public void testFormatImproper() {
        Fraction c = new Fraction(5, 3);

        String actual = properFormat.format(c);
        Assert.assertEquals("1 2 / 3", actual);

        actual = improperFormat.format(c);
        Assert.assertEquals("5 / 3", actual);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testFormatImproperNegative
    public void testFormatImproperNegative() {
        Fraction c = new Fraction(-5, 3);

        String actual = properFormat.format(c);
        Assert.assertEquals("-1 2 / 3", actual);

        actual = improperFormat.format(c);
        Assert.assertEquals("-5 / 3", actual);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParse
    public void testParse() {
        String source = "1 / 2";

        try {
            Fraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());

            c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());
        } catch (MathParseException ex) {
            Assert.fail(ex.getMessage());
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseInteger
    public void testParseInteger() {
        String source = "10";
        {
            Fraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(10, c.getNumerator());
            Assert.assertEquals(1, c.getDenominator());
        }
        {
            Fraction c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(10, c.getNumerator());
            Assert.assertEquals(1, c.getDenominator());
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseOne1
    public void testParseOne1() {
        String source = "1 / 1";
        Fraction c = properFormat.parse(source);
        Assert.assertNotNull(c);
        Assert.assertEquals(1, c.getNumerator());
        Assert.assertEquals(1, c.getDenominator());
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseOne2
    public void testParseOne2() {
        String source = "10 / 10";
        Fraction c = properFormat.parse(source);
        Assert.assertNotNull(c);
        Assert.assertEquals(1, c.getNumerator());
        Assert.assertEquals(1, c.getDenominator());
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseZero1
    public void testParseZero1() {
        String source = "0 / 1";
        Fraction c = properFormat.parse(source);
        Assert.assertNotNull(c);
        Assert.assertEquals(0, c.getNumerator());
        Assert.assertEquals(1, c.getDenominator());
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseZero2
    public void testParseZero2() {
        String source = "-0 / 1";
        Fraction c = properFormat.parse(source);
        Assert.assertNotNull(c);
        Assert.assertEquals(0, c.getNumerator());
        Assert.assertEquals(1, c.getDenominator());
        
        Assert.assertEquals(Double.POSITIVE_INFINITY, 1d / c.doubleValue(), 0);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseInvalid
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

// org.apache.commons.math3.fraction.FractionFormatTest::testParseInvalidDenominator
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

// org.apache.commons.math3.fraction.FractionFormatTest::testParseNegative
    public void testParseNegative() {

        {
            String source = "-1 / 2";
            Fraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());

            c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());

            source = "1 / -2";
            c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());

            c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseProper
    public void testParseProper() {
        String source = "1 2 / 3";

        {
            Fraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(5, c.getNumerator());
            Assert.assertEquals(3, c.getDenominator());
        }

        try {
            improperFormat.parse(source);
            Assert.fail("invalid improper fraction.");
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseProperNegative
    public void testParseProperNegative() {
        String source = "-1 2 / 3";
        {
            Fraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-5, c.getNumerator());
            Assert.assertEquals(3, c.getDenominator());
        }

        try {
            improperFormat.parse(source);
            Assert.fail("invalid improper fraction.");
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseProperInvalidMinus
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

// org.apache.commons.math3.fraction.FractionFormatTest::testNumeratorFormat
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

// org.apache.commons.math3.fraction.FractionFormatTest::testDenominatorFormat
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

// org.apache.commons.math3.fraction.FractionFormatTest::testWholeFormat
    public void testWholeFormat() {
        ProperFractionFormat format = (ProperFractionFormat)properFormat;

        NumberFormat old = format.getWholeFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        format.setWholeFormat(nf);
        Assert.assertEquals(nf, format.getWholeFormat());
        format.setWholeFormat(old);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testLongFormat
    public void testLongFormat() {
        Assert.assertEquals("10 / 1", improperFormat.format(10l));
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testDoubleFormat
    public void testDoubleFormat() {
        Assert.assertEquals("355 / 113", improperFormat.format(FastMath.PI));
    }

// org.apache.commons.math3.fraction.FractionTest::testConstructor
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
            Assert.fail();
        } catch (MathArithmeticException ex) {
            
        }
        try {
            new Fraction(1, Integer.MIN_VALUE);
            Assert.fail();
        } catch (MathArithmeticException ex) {
            
        }

        assertFraction(0, 1, new Fraction(0.00000000000001));
        assertFraction(2, 5, new Fraction(0.40000000000001));
        assertFraction(15, 1, new Fraction(15.0000000000001));
    }

// org.apache.commons.math3.fraction.FractionTest::testGoldenRatio
    public void testGoldenRatio() {
        
        new Fraction((1 + FastMath.sqrt(5)) / 2, 1.0e-12, 25);
    }

// org.apache.commons.math3.fraction.FractionTest::testDoubleConstructor
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

// org.apache.commons.math3.fraction.FractionTest::testDigitLimitConstructor
    public void testDigitLimitConstructor() throws ConvergenceException  {
        assertFraction(2, 5, new Fraction(0.4,   9));
        assertFraction(2, 5, new Fraction(0.4,  99));
        assertFraction(2, 5, new Fraction(0.4, 999));

        assertFraction(3, 5,      new Fraction(0.6152,    9));
        assertFraction(8, 13,     new Fraction(0.6152,   99));
        assertFraction(510, 829,  new Fraction(0.6152,  999));
        assertFraction(769, 1250, new Fraction(0.6152, 9999));

        
        assertFraction(1, 2, new Fraction(0.5000000001, 10));
    }

// org.apache.commons.math3.fraction.FractionTest::testIntegerOverflow
    public void testIntegerOverflow() {
        checkIntegerOverflow(0.75000000001455192);
        checkIntegerOverflow(1.0e10);
        checkIntegerOverflow(-1.0e10);
        checkIntegerOverflow(-43979.60679604749);
    }

// org.apache.commons.math3.fraction.FractionTest::testEpsilonLimitConstructor
    public void testEpsilonLimitConstructor() throws ConvergenceException  {
        assertFraction(2, 5, new Fraction(0.4, 1.0e-5, 100));

        assertFraction(3, 5,      new Fraction(0.6152, 0.02, 100));
        assertFraction(8, 13,     new Fraction(0.6152, 1.0e-3, 100));
        assertFraction(251, 408,  new Fraction(0.6152, 1.0e-4, 100));
        assertFraction(251, 408,  new Fraction(0.6152, 1.0e-5, 100));
        assertFraction(510, 829,  new Fraction(0.6152, 1.0e-6, 100));
        assertFraction(769, 1250, new Fraction(0.6152, 1.0e-7, 100));
    }

// org.apache.commons.math3.fraction.FractionTest::testCompareTo
    public void testCompareTo() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);
        Fraction third = new Fraction(1, 2);

        Assert.assertEquals(0, first.compareTo(first));
        Assert.assertEquals(0, first.compareTo(third));
        Assert.assertEquals(1, first.compareTo(second));
        Assert.assertEquals(-1, second.compareTo(first));

        
        
        
        Fraction pi1 = new Fraction(1068966896, 340262731);
        Fraction pi2 = new Fraction( 411557987, 131002976);
        Assert.assertEquals(-1, pi1.compareTo(pi2));
        Assert.assertEquals( 1, pi2.compareTo(pi1));
        Assert.assertEquals(0.0, pi1.doubleValue() - pi2.doubleValue(), 1.0e-20);
    }

// org.apache.commons.math3.fraction.FractionTest::testDoubleValue
    public void testDoubleValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);

        Assert.assertEquals(0.5, first.doubleValue(), 0.0);
        Assert.assertEquals(1.0 / 3.0, second.doubleValue(), 0.0);
    }

// org.apache.commons.math3.fraction.FractionTest::testFloatValue
    public void testFloatValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);

        Assert.assertEquals(0.5f, first.floatValue(), 0.0f);
        Assert.assertEquals((float)(1.0 / 3.0), second.floatValue(), 0.0f);
    }

// org.apache.commons.math3.fraction.FractionTest::testIntValue
    public void testIntValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(3, 2);

        Assert.assertEquals(0, first.intValue());
        Assert.assertEquals(1, second.intValue());
    }

// org.apache.commons.math3.fraction.FractionTest::testLongValue
    public void testLongValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(3, 2);

        Assert.assertEquals(0L, first.longValue());
        Assert.assertEquals(1L, second.longValue());
    }

// org.apache.commons.math3.fraction.FractionTest::testConstructorDouble
    public void testConstructorDouble() {
        assertFraction(1, 2, new Fraction(0.5));
        assertFraction(1, 3, new Fraction(1.0 / 3.0));
        assertFraction(17, 100, new Fraction(17.0 / 100.0));
        assertFraction(317, 100, new Fraction(317.0 / 100.0));
        assertFraction(-1, 2, new Fraction(-0.5));
        assertFraction(-1, 3, new Fraction(-1.0 / 3.0));
        assertFraction(-17, 100, new Fraction(17.0 / -100.0));
        assertFraction(-317, 100, new Fraction(-317.0 / 100.0));
    }

// org.apache.commons.math3.fraction.FractionTest::testAbs
    public void testAbs() {
        Fraction a = new Fraction(10, 21);
        Fraction b = new Fraction(-10, 21);
        Fraction c = new Fraction(10, -21);

        assertFraction(10, 21, a.abs());
        assertFraction(10, 21, b.abs());
        assertFraction(10, 21, c.abs());
    }

// org.apache.commons.math3.fraction.FractionTest::testPercentage
    public void testPercentage() {
        Assert.assertEquals(50.0, new Fraction(1, 2).percentageValue(), 1.0e-15);
    }

// org.apache.commons.math3.fraction.FractionTest::testMath835
    public void testMath835() {
        final int numer = Integer.MAX_VALUE / 99;
        final int denom = 1;
        final double percentage = 100 * ((double) numer) / denom;
        final Fraction frac = new Fraction(numer, denom);
        
        
        Assert.assertEquals(percentage, frac.percentageValue(), Math.ulp(percentage));
    }

// org.apache.commons.math3.fraction.FractionTest::testReciprocal
    public void testReciprocal() {
        Fraction f = null;

        f = new Fraction(50, 75);
        f = f.reciprocal();
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = new Fraction(4, 3);
        f = f.reciprocal();
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = new Fraction(-15, 47);
        f = f.reciprocal();
        Assert.assertEquals(-47, f.getNumerator());
        Assert.assertEquals(15, f.getDenominator());

        f = new Fraction(0, 3);
        try {
            f = f.reciprocal();
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        
        f = new Fraction(Integer.MAX_VALUE, 1);
        f = f.reciprocal();
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominator());
    }

// org.apache.commons.math3.fraction.FractionTest::testNegate
    public void testNegate() {
        Fraction f = null;

        f = new Fraction(50, 75);
        f = f.negate();
        Assert.assertEquals(-2, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f = new Fraction(-50, 75);
        f = f.negate();
        Assert.assertEquals(2, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        
        f = new Fraction(Integer.MAX_VALUE-1, Integer.MAX_VALUE);
        f = f.negate();
        Assert.assertEquals(Integer.MIN_VALUE+2, f.getNumerator());
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominator());

        f = new Fraction(Integer.MIN_VALUE, 1);
        try {
            f = f.negate();
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}
    }

// org.apache.commons.math3.fraction.FractionTest::testAdd
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
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
        f = f1.add(1);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f1 = new Fraction(-1, 13*13*2*2);
        f2 = new Fraction(-2, 13*17*2);
        f = f1.add(f2);
        Assert.assertEquals(13*13*17*2*2, f.getDenominator());
        Assert.assertEquals(-17 - 2*13*2, f.getNumerator());

        try {
            f.add(null);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        
        
        f1 = new Fraction(1,32768*3);
        f2 = new Fraction(1,59049);
        f = f1.add(f2);
        Assert.assertEquals(52451, f.getNumerator());
        Assert.assertEquals(1934917632, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, 3);
        f2 = new Fraction(1,3);
        f = f1.add(f2);
        Assert.assertEquals(Integer.MIN_VALUE+1, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        f2 = Fraction.ONE;
        f = f1.add(f2);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        try {
            f = f.add(Fraction.ONE); 
            Assert.fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}

        
        f1 = new Fraction(Integer.MIN_VALUE, 5);
        f2 = new Fraction(-1,5);
        try {
            f = f1.add(f2); 
            Assert.fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(-Integer.MAX_VALUE, 1);
            f = f.add(f);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(-Integer.MAX_VALUE, 1);
            f = f.add(f);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(3,327680);
        f2 = new Fraction(2,59049);
        try {
            f = f1.add(f2); 
            Assert.fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}
    }

// org.apache.commons.math3.fraction.FractionTest::testDivide
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
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(0, 5);
        f2 = new Fraction(2, 7);
        Fraction f = f1.divide(f2);
        Assert.assertSame(Fraction.ZERO, f);

        f1 = new Fraction(2, 7);
        f2 = Fraction.ONE;
        f = f1.divide(f2);
        Assert.assertEquals(2, f.getNumerator());
        Assert.assertEquals(7, f.getDenominator());

        f1 = new Fraction(1, Integer.MAX_VALUE);
        f = f1.divide(f1);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f2 = new Fraction(1, Integer.MAX_VALUE);
        f = f1.divide(f2);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        try {
            f.divide(null);
            Assert.fail("MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        try {
            f1 = new Fraction(1, Integer.MAX_VALUE);
            f = f1.divide(f1.reciprocal());  
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}
        try {
            f1 = new Fraction(1, -Integer.MAX_VALUE);
            f = f1.divide(f1.reciprocal());  
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(6, 35);
        f  = f1.divide(15);
        Assert.assertEquals(2, f.getNumerator());
        Assert.assertEquals(175, f.getDenominator());

    }

// org.apache.commons.math3.fraction.FractionTest::testMultiply
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
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        try {
            f.multiply(null);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        f1 = new Fraction(6, 35);
        f  = f1.multiply(15);
        Assert.assertEquals(18, f.getNumerator());
        Assert.assertEquals(7, f.getDenominator());
    }

// org.apache.commons.math3.fraction.FractionTest::testSubtract
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
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        
        
        Fraction f1 = new Fraction(1,32768*3);
        Fraction f2 = new Fraction(1,59049);
        f = f1.subtract(f2);
        Assert.assertEquals(-13085, f.getNumerator());
        Assert.assertEquals(1934917632, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, 3);
        f2 = new Fraction(1,3).negate();
        f = f1.subtract(f2);
        Assert.assertEquals(Integer.MIN_VALUE+1, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f1 = new Fraction(Integer.MAX_VALUE, 1);
        f2 = Fraction.ONE;
        f = f1.subtract(f2);
        Assert.assertEquals(Integer.MAX_VALUE-1, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
        f = f1.subtract(1);
        Assert.assertEquals(Integer.MAX_VALUE-1, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        try {
            f1 = new Fraction(1, Integer.MAX_VALUE);
            f2 = new Fraction(1, Integer.MAX_VALUE - 1);
            f = f1.subtract(f2);
            Assert.fail("expecting MathArithmeticException");  
        } catch (MathArithmeticException ex) {}

        
        f1 = new Fraction(Integer.MIN_VALUE, 5);
        f2 = new Fraction(1,5);
        try {
            f = f1.subtract(f2); 
            Assert.fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(Integer.MIN_VALUE, 1);
            f = f.subtract(Fraction.ONE);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(Integer.MAX_VALUE, 1);
            f = f.subtract(Fraction.ONE.negate());
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(3,327680);
        f2 = new Fraction(2,59049);
        try {
            f = f1.subtract(f2); 
            Assert.fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}
    }

// org.apache.commons.math3.fraction.FractionTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Fraction zero  = new Fraction(0,1);
        Fraction nullFraction = null;
        Assert.assertTrue( zero.equals(zero));
        Assert.assertFalse(zero.equals(nullFraction));
        Assert.assertFalse(zero.equals(Double.valueOf(0)));
        Fraction zero2 = new Fraction(0,2);
        Assert.assertTrue(zero.equals(zero2));
        Assert.assertEquals(zero.hashCode(), zero2.hashCode());
        Fraction one = new Fraction(1,1);
        Assert.assertFalse((one.equals(zero) ||zero.equals(one)));
    }

// org.apache.commons.math3.fraction.FractionTest::testGetReducedFraction
    public void testGetReducedFraction() {
        Fraction threeFourths = new Fraction(3, 4);
        Assert.assertTrue(threeFourths.equals(Fraction.getReducedFraction(6, 8)));
        Assert.assertTrue(Fraction.ZERO.equals(Fraction.getReducedFraction(0, -1)));
        try {
            Fraction.getReducedFraction(1, 0);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }
        Assert.assertEquals(Fraction.getReducedFraction
                (2, Integer.MIN_VALUE).getNumerator(),-1);
        Assert.assertEquals(Fraction.getReducedFraction
                (1, -1).getNumerator(), -1);
    }

// org.apache.commons.math3.fraction.FractionTest::testToString
    public void testToString() {
        Assert.assertEquals("0", new Fraction(0, 3).toString());
        Assert.assertEquals("3", new Fraction(6, 2).toString());
        Assert.assertEquals("2 / 3", new Fraction(18, 27).toString());
    }

// org.apache.commons.math3.fraction.FractionTest::testSerial
    public void testSerial() throws FractionConversionException {
        Fraction[] fractions = {
            new Fraction(3, 4), Fraction.ONE, Fraction.ZERO,
            new Fraction(17), new Fraction(FastMath.PI, 1000),
            new Fraction(-5, 2)
        };
        for (Fraction fraction : fractions) {
            Assert.assertEquals(fraction, TestUtils.serializeAndRecover(fraction));
        }
    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testConstructors
    public void testConstructors() {

        ArrayFieldVector<Fraction> v0 = new ArrayFieldVector<Fraction>(FractionField.getInstance());
        Assert.assertEquals(0, v0.getDimension());

        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(FractionField.getInstance(), 7);
        Assert.assertEquals(7, v1.getDimension());
        Assert.assertEquals(new Fraction(0), v1.getEntry(6));

        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(5, new Fraction(123, 100));
        Assert.assertEquals(5, v2.getDimension());
        Assert.assertEquals(new Fraction(123, 100), v2.getEntry(4));

        ArrayFieldVector<Fraction> v3 = new ArrayFieldVector<Fraction>(FractionField.getInstance(), vec1);
        Assert.assertEquals(3, v3.getDimension());
        Assert.assertEquals(new Fraction(2), v3.getEntry(1));

        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(FractionField.getInstance(), vec4, 3, 2);
        Assert.assertEquals(2, v4.getDimension());
        Assert.assertEquals(new Fraction(4), v4.getEntry(0));
        try {
            new ArrayFieldVector<Fraction>(vec4, 8, 3);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        FieldVector<Fraction> v5_i = new ArrayFieldVector<Fraction>(dvec1);
        Assert.assertEquals(9, v5_i.getDimension());
        Assert.assertEquals(new Fraction(9), v5_i.getEntry(8));

        ArrayFieldVector<Fraction> v5 = new ArrayFieldVector<Fraction>(dvec1);
        Assert.assertEquals(9, v5.getDimension());
        Assert.assertEquals(new Fraction(9), v5.getEntry(8));

        ArrayFieldVector<Fraction> v6 = new ArrayFieldVector<Fraction>(dvec1, 3, 2);
        Assert.assertEquals(2, v6.getDimension());
        Assert.assertEquals(new Fraction(4), v6.getEntry(0));
        try {
            new ArrayFieldVector<Fraction>(dvec1, 8, 3);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        ArrayFieldVector<Fraction> v7 = new ArrayFieldVector<Fraction>(v1);
        Assert.assertEquals(7, v7.getDimension());
        Assert.assertEquals(new Fraction(0), v7.getEntry(6));

        FieldVectorTestImpl<Fraction> v7_i = new FieldVectorTestImpl<Fraction>(vec1);

        ArrayFieldVector<Fraction> v7_2 = new ArrayFieldVector<Fraction>(v7_i);
        Assert.assertEquals(3, v7_2.getDimension());
        Assert.assertEquals(new Fraction(2), v7_2.getEntry(1));

        ArrayFieldVector<Fraction> v8 = new ArrayFieldVector<Fraction>(v1, true);
        Assert.assertEquals(7, v8.getDimension());
        Assert.assertEquals(new Fraction(0), v8.getEntry(6));
        Assert.assertNotSame("testData not same object ", v1.getDataRef(), v8.getDataRef());

        ArrayFieldVector<Fraction> v8_2 = new ArrayFieldVector<Fraction>(v1, false);
        Assert.assertEquals(7, v8_2.getDimension());
        Assert.assertEquals(new Fraction(0), v8_2.getEntry(6));
        Assert.assertArrayEquals(v1.getDataRef(), v8_2.getDataRef());

        ArrayFieldVector<Fraction> v9 = new ArrayFieldVector<Fraction>(v1, v3);
        Assert.assertEquals(10, v9.getDimension());
        Assert.assertEquals(new Fraction(1), v9.getEntry(7));

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testDataInOut
    public void testDataInOut() {

        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(vec2);
        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(vec4);
        FieldVectorTestImpl<Fraction> v2_t = new FieldVectorTestImpl<Fraction>(vec2);

        FieldVector<Fraction> v_append_1 = v1.append(v2);
        Assert.assertEquals(6, v_append_1.getDimension());
        Assert.assertEquals(new Fraction(4), v_append_1.getEntry(3));

        FieldVector<Fraction> v_append_2 = v1.append(new Fraction(2));
        Assert.assertEquals(4, v_append_2.getDimension());
        Assert.assertEquals(new Fraction(2), v_append_2.getEntry(3));

        FieldVector<Fraction> v_append_4 = v1.append(v2_t);
        Assert.assertEquals(6, v_append_4.getDimension());
        Assert.assertEquals(new Fraction(4), v_append_4.getEntry(3));

        FieldVector<Fraction> v_copy = v1.copy();
        Assert.assertEquals(3, v_copy.getDimension());
        Assert.assertNotSame("testData not same object ", v1.getDataRef(), v_copy.getData());

        Fraction[] a_frac = v1.toArray();
        Assert.assertEquals(3, a_frac.length);
        Assert.assertNotSame("testData not same object ", v1.getDataRef(), a_frac);

        FieldVector<Fraction> vout5 = v4.getSubVector(3, 3);
        Assert.assertEquals(3, vout5.getDimension());
        Assert.assertEquals(new Fraction(5), vout5.getEntry(1));
        try {
            v4.getSubVector(3, 7);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set1 = (ArrayFieldVector<Fraction>) v1.copy();
        v_set1.setEntry(1, new Fraction(11));
        Assert.assertEquals(new Fraction(11), v_set1.getEntry(1));
        try {
            v_set1.setEntry(3, new Fraction(11));
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set2 = (ArrayFieldVector<Fraction>) v4.copy();
        v_set2.set(3, v1);
        Assert.assertEquals(new Fraction(1), v_set2.getEntry(3));
        Assert.assertEquals(new Fraction(7), v_set2.getEntry(6));
        try {
            v_set2.set(7, v1);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set3 = (ArrayFieldVector<Fraction>) v1.copy();
        v_set3.set(new Fraction(13));
        Assert.assertEquals(new Fraction(13), v_set3.getEntry(2));

        try {
            v_set3.getEntry(23);
            Assert.fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set4 = (ArrayFieldVector<Fraction>) v4.copy();
        v_set4.setSubVector(3, v2_t);
        Assert.assertEquals(new Fraction(4), v_set4.getEntry(3));
        Assert.assertEquals(new Fraction(7), v_set4.getEntry(6));
        try {
            v_set4.setSubVector(7, v2_t);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> vout10 = (ArrayFieldVector<Fraction>) v1.copy();
        ArrayFieldVector<Fraction> vout10_2 = (ArrayFieldVector<Fraction>) v1.copy();
        Assert.assertEquals(vout10, vout10_2);
        vout10_2.setEntry(0, new Fraction(11, 10));
        Assert.assertNotSame(vout10, vout10_2);

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testMapFunctions
    public void testMapFunctions() {
        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);

        
        FieldVector<Fraction> v_mapAdd = v1.mapAdd(new Fraction(2));
        Fraction[] result_mapAdd = {new Fraction(3), new Fraction(4), new Fraction(5)};
        checkArray("compare vectors" ,result_mapAdd,v_mapAdd.getData());

        
        FieldVector<Fraction> v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(new Fraction(2));
        Fraction[] result_mapAddToSelf = {new Fraction(3), new Fraction(4), new Fraction(5)};
        checkArray("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData());

        
        FieldVector<Fraction> v_mapSubtract = v1.mapSubtract(new Fraction(2));
        Fraction[] result_mapSubtract = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        checkArray("compare vectors" ,result_mapSubtract,v_mapSubtract.getData());

        
        FieldVector<Fraction> v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(new Fraction(2));
        Fraction[] result_mapSubtractToSelf = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        checkArray("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData());

        
        FieldVector<Fraction> v_mapMultiply = v1.mapMultiply(new Fraction(2));
        Fraction[] result_mapMultiply = {new Fraction(2), new Fraction(4), new Fraction(6)};
        checkArray("compare vectors" ,result_mapMultiply,v_mapMultiply.getData());

        
        FieldVector<Fraction> v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(new Fraction(2));
        Fraction[] result_mapMultiplyToSelf = {new Fraction(2), new Fraction(4), new Fraction(6)};
        checkArray("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData());

        
        FieldVector<Fraction> v_mapDivide = v1.mapDivide(new Fraction(2));
        Fraction[] result_mapDivide = {new Fraction(1, 2), new Fraction(1), new Fraction(3, 2)};
        checkArray("compare vectors" ,result_mapDivide,v_mapDivide.getData());

        
        FieldVector<Fraction> v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(new Fraction(2));
        Fraction[] result_mapDivideToSelf = {new Fraction(1, 2), new Fraction(1), new Fraction(3, 2)};
        checkArray("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData());

        
        FieldVector<Fraction> v_mapInv = v1.mapInv();
        Fraction[] result_mapInv = {new Fraction(1),new Fraction(1, 2),new Fraction(1, 3)};
        checkArray("compare vectors" ,result_mapInv,v_mapInv.getData());

        
        FieldVector<Fraction> v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapInvToSelf();
        Fraction[] result_mapInvToSelf = {new Fraction(1),new Fraction(1, 2),new Fraction(1, 3)};
        checkArray("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData());

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testBasicFunctions
    public void testBasicFunctions() {
        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(vec2);
        new ArrayFieldVector<Fraction>(vec_null);

        FieldVectorTestImpl<Fraction> v2_t = new FieldVectorTestImpl<Fraction>(vec2);

        
        ArrayFieldVector<Fraction> v_add = v1.add(v2);
        Fraction[] result_add = {new Fraction(5), new Fraction(7), new Fraction(9)};
        checkArray("compare vect" ,v_add.getData(),result_add);

        FieldVectorTestImpl<Fraction> vt2 = new FieldVectorTestImpl<Fraction>(vec2);
        FieldVector<Fraction> v_add_i = v1.add(vt2);
        Fraction[] result_add_i = {new Fraction(5), new Fraction(7), new Fraction(9)};
        checkArray("compare vect" ,v_add_i.getData(),result_add_i);

        
        ArrayFieldVector<Fraction> v_subtract = v1.subtract(v2);
        Fraction[] result_subtract = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        checkArray("compare vect" ,v_subtract.getData(),result_subtract);

        FieldVector<Fraction> v_subtract_i = v1.subtract(vt2);
        Fraction[] result_subtract_i = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        checkArray("compare vect" ,v_subtract_i.getData(),result_subtract_i);

        
        ArrayFieldVector<Fraction>  v_ebeMultiply = v1.ebeMultiply(v2);
        Fraction[] result_ebeMultiply = {new Fraction(4), new Fraction(10), new Fraction(18)};
        checkArray("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply);

        FieldVector<Fraction>  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        Fraction[] result_ebeMultiply_2 = {new Fraction(4), new Fraction(10), new Fraction(18)};
        checkArray("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2);

        
        ArrayFieldVector<Fraction>  v_ebeDivide = v1.ebeDivide(v2);
        Fraction[] result_ebeDivide = {new Fraction(1, 4), new Fraction(2, 5), new Fraction(1, 2)};
        checkArray("compare vect" ,v_ebeDivide.getData(),result_ebeDivide);

        FieldVector<Fraction>  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        Fraction[] result_ebeDivide_2 = {new Fraction(1, 4), new Fraction(2, 5), new Fraction(1, 2)};
        checkArray("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2);

        
        Fraction dot =  v1.dotProduct(v2);
        Assert.assertEquals("compare val ",new Fraction(32), dot);

        
        Fraction dot_2 =  v1.dotProduct(v2_t);
        Assert.assertEquals("compare val ",new Fraction(32), dot_2);

        FieldMatrix<Fraction> m_outerProduct = v1.outerProduct(v2);
        Assert.assertEquals("compare val ",new Fraction(4), m_outerProduct.getEntry(0,0));

        FieldMatrix<Fraction> m_outerProduct_2 = v1.outerProduct(v2_t);
        Assert.assertEquals("compare val ",new Fraction(4), m_outerProduct_2.getEntry(0,0));

        ArrayFieldVector<Fraction> v_projection = v1.projection(v2);
        Fraction[] result_projection = {new Fraction(128, 77), new Fraction(160, 77), new Fraction(192, 77)};
        checkArray("compare vect", v_projection.getData(), result_projection);

        FieldVector<Fraction> v_projection_2 = v1.projection(v2_t);
        Fraction[] result_projection_2 = {new Fraction(128, 77), new Fraction(160, 77), new Fraction(192, 77)};
        checkArray("compare vect", v_projection_2.getData(), result_projection_2);

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testMisc
    public void testMisc() {
        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(vec4);
        FieldVector<Fraction> v4_2 = new ArrayFieldVector<Fraction>(vec4);

        String out1 = v1.toString();
        Assert.assertTrue("some output ",  out1.length()!=0);
        
        try {
            v1.checkVectorDimensions(2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

       try {
            v1.checkVectorDimensions(v4);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            v1.checkVectorDimensions(v4_2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testSerial
    public void testSerial()  {
        ArrayFieldVector<Fraction> v = new ArrayFieldVector<Fraction>(vec1);
        Assert.assertEquals(v,TestUtils.serializeAndRecover(v));
    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testZeroVectors
    public void testZeroVectors() {

        
        try {
            new ArrayFieldVector<Fraction>(new Fraction[0]);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            new ArrayFieldVector<Fraction>(new Fraction[0], true);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            new ArrayFieldVector<Fraction>(new Fraction[0], false);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        Assert.assertEquals(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), new Fraction[0]).getDimension());
        Assert.assertEquals(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), new Fraction[0], true).getDimension());
        Assert.assertEquals(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), new Fraction[0], false).getDimension());

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testOuterProduct
    public void testOuterProduct() {
        final ArrayFieldVector<Fraction> u
            = new ArrayFieldVector<Fraction>(FractionField.getInstance(),
                                             new Fraction[] {new Fraction(1),
                                                             new Fraction(2),
                                                             new Fraction(-3)});
        final ArrayFieldVector<Fraction> v
            = new ArrayFieldVector<Fraction>(FractionField.getInstance(),
                                             new Fraction[] {new Fraction(4),
                                                             new Fraction(-2)});

        final FieldMatrix<Fraction> uv = u.outerProduct(v);

        final double tol = Math.ulp(1d);
        Assert.assertEquals(new Fraction(4).doubleValue(), uv.getEntry(0, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-2).doubleValue(), uv.getEntry(0, 1).doubleValue(), tol);
        Assert.assertEquals(new Fraction(8).doubleValue(), uv.getEntry(1, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-4).doubleValue(), uv.getEntry(1, 1).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-12).doubleValue(), uv.getEntry(2, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(6).doubleValue(), uv.getEntry(2, 1).doubleValue(), tol);
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testDimensions
    public void testDimensions() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        Assert.assertEquals("testData row dimension",3,m.getRowDimension());
        Assert.assertEquals("testData column dimension",3,m.getColumnDimension());
        Assert.assertTrue("testData is square",m.isSquare());
        Assert.assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        Assert.assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        Assert.assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Random r = new Random(66636328996002l);
        BlockFieldMatrix<Fraction> m1 = createRandomMatrix(r, 47, 83);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(m1.getData());
        Assert.assertEquals(m1, m2);
        BlockFieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(m3.getData());
        Assert.assertEquals(m3, m4);
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testAdd
    public void testAdd() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        Fraction[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals(testDataPlusInv[row][col],sumEntries[row][col]);
            }
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testAddFail
    public void testAddFail() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testPlusMinus
    public void testPlusMinus() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testDataInv);
        TestUtils.assertEquals(m.subtract(m2), m2.scalarMultiply(new Fraction(-1)).add(m));
        try {
            m.subtract(new BlockFieldMatrix<Fraction>(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testMultiply
    public void testMultiply() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        BlockFieldMatrix<Fraction> identity = new BlockFieldMatrix<Fraction>(id);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        TestUtils.assertEquals(m.multiply(mInv), identity);
        TestUtils.assertEquals(mInv.multiply(m), identity);
        TestUtils.assertEquals(m.multiply(identity), m);
        TestUtils.assertEquals(identity.multiply(mInv), mInv);
        TestUtils.assertEquals(m2.multiply(identity), m2);
        try {
            m.multiply(new BlockFieldMatrix<Fraction>(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSeveralBlocks
    public void testSeveralBlocks() {
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), 37, 41);
        for (int i = 0; i < m.getRowDimension(); ++i) {
            for (int j = 0; j < m.getColumnDimension(); ++j) {
                m.setEntry(i, j, new Fraction(i * 11 + j, 11));
            }
        }

        FieldMatrix<Fraction> mT = m.transpose();
        Assert.assertEquals(m.getRowDimension(), mT.getColumnDimension());
        Assert.assertEquals(m.getColumnDimension(), mT.getRowDimension());
        for (int i = 0; i < mT.getRowDimension(); ++i) {
            for (int j = 0; j < mT.getColumnDimension(); ++j) {
                Assert.assertEquals(m.getEntry(j, i), mT.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mPm = m.add(m);
        for (int i = 0; i < mPm.getRowDimension(); ++i) {
            for (int j = 0; j < mPm.getColumnDimension(); ++j) {
                Assert.assertEquals(m.getEntry(i, j).multiply(new Fraction(2)), mPm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mPmMm = mPm.subtract(m);
        for (int i = 0; i < mPmMm.getRowDimension(); ++i) {
            for (int j = 0; j < mPmMm.getColumnDimension(); ++j) {
                Assert.assertEquals(m.getEntry(i, j), mPmMm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mTm = mT.multiply(m);
        for (int i = 0; i < mTm.getRowDimension(); ++i) {
            for (int j = 0; j < mTm.getColumnDimension(); ++j) {
                Fraction sum = Fraction.ZERO;
                for (int k = 0; k < mT.getColumnDimension(); ++k) {
                    sum = sum.add(new Fraction(k * 11 + i, 11).multiply(new Fraction(k * 11 + j, 11)));
                }
                Assert.assertEquals(sum, mTm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mmT = m.multiply(mT);
        for (int i = 0; i < mmT.getRowDimension(); ++i) {
            for (int j = 0; j < mmT.getColumnDimension(); ++j) {
                Fraction sum = Fraction.ZERO;
                for (int k = 0; k < m.getColumnDimension(); ++k) {
                    sum = sum.add(new Fraction(i * 11 + k, 11).multiply(new Fraction(j * 11 + k, 11)));
                }
                Assert.assertEquals(sum, mmT.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub1 = m.getSubMatrix(2, 9, 5, 20);
        for (int i = 0; i < sub1.getRowDimension(); ++i) {
            for (int j = 0; j < sub1.getColumnDimension(); ++j) {
                Assert.assertEquals(new Fraction((i + 2) * 11 + (j + 5), 11), sub1.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub2 = m.getSubMatrix(10, 12, 3, 40);
        for (int i = 0; i < sub2.getRowDimension(); ++i) {
            for (int j = 0; j < sub2.getColumnDimension(); ++j) {
                Assert.assertEquals(new Fraction((i + 10) * 11 + (j + 3), 11), sub2.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub3 = m.getSubMatrix(30, 34, 0, 5);
        for (int i = 0; i < sub3.getRowDimension(); ++i) {
            for (int j = 0; j < sub3.getColumnDimension(); ++j) {
                Assert.assertEquals(new Fraction((i + 30) * 11 + (j + 0), 11), sub3.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub4 = m.getSubMatrix(30, 32, 32, 35);
        for (int i = 0; i < sub4.getRowDimension(); ++i) {
            for (int j = 0; j < sub4.getColumnDimension(); ++j) {
                Assert.assertEquals(new Fraction((i + 30) * 11 + (j + 32), 11), sub4.getEntry(i, j));
            }
        }

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testMultiply2
    public void testMultiply2() {
       FieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(d3);
       FieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(d4);
       FieldMatrix<Fraction> m5 = new BlockFieldMatrix<Fraction>(d5);
       TestUtils.assertEquals(m3.multiply(m4), m5);
   }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(id);
        Assert.assertEquals(new Fraction(3),m.getTrace());
        m = new BlockFieldMatrix<Fraction>(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(new BlockFieldMatrix<Fraction>(testDataPlus2),
                               m.scalarAdd(new Fraction(2)));
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(testVector, m.operate(testVector));
        TestUtils.assertEquals(testVector, m.operate(new ArrayFieldVector<Fraction>(testVector)).getData());
        m = new BlockFieldMatrix<Fraction>(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testOperateLarge
    public void testOperateLarge() {
        int p = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int q = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int r =  BlockFieldMatrix.BLOCK_SIZE / 2;
        Random random = new Random(111007463902334l);
        FieldMatrix<Fraction> m1 = createRandomMatrix(random, p, q);
        FieldMatrix<Fraction> m2 = createRandomMatrix(random, q, r);
        FieldMatrix<Fraction> m1m2 = m1.multiply(m2);
        for (int i = 0; i < r; ++i) {
            TestUtils.assertEquals(m1m2.getColumn(i), m1.operate(m2.getColumn(i)));
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testOperatePremultiplyLarge
    public void testOperatePremultiplyLarge() {
        int p = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int q = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int r =  BlockFieldMatrix.BLOCK_SIZE / 2;
        Random random = new Random(111007463902334l);
        FieldMatrix<Fraction> m1 = createRandomMatrix(random, p, q);
        FieldMatrix<Fraction> m2 = createRandomMatrix(random, q, r);
        FieldMatrix<Fraction> m1m2 = m1.multiply(m2);
        for (int i = 0; i < p; ++i) {
            TestUtils.assertEquals(m1m2.getRow(i), m2.preMultiply(m1.getRow(i)));
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = new BlockFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(2) },
                { new Fraction(3), new Fraction(4) },
                { new Fraction(5), new Fraction(6) }
        });
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals( new Fraction(3), b[0]);
        Assert.assertEquals( new Fraction(7), b[1]);
        Assert.assertEquals(new Fraction(11), b[2]);
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecomposition<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecomposition<Fraction>(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals(mIT, mTI);
        m = new BlockFieldMatrix<Fraction>(testData2);
        FieldMatrix<Fraction> mt = new BlockFieldMatrix<Fraction>(testData2T);
        TestUtils.assertEquals(mt, m.transpose());
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.preMultiply(testVector), preMultTest);
        TestUtils.assertEquals(m.preMultiply(new ArrayFieldVector<Fraction>(testVector).getData()),
                               preMultTest);
        m = new BlockFieldMatrix<Fraction>(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testPremultiply
    public void testPremultiply() {
        FieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(d3);
        FieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(d4);
        FieldMatrix<Fraction> m5 = new BlockFieldMatrix<Fraction>(d5);
        TestUtils.assertEquals(m4.preMultiply(m3), m5);

        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        BlockFieldMatrix<Fraction> identity = new BlockFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(m.preMultiply(mInv), identity);
        TestUtils.assertEquals(mInv.preMultiply(m), identity);
        TestUtils.assertEquals(m.preMultiply(identity), m);
        TestUtils.assertEquals(identity.preMultiply(mInv), mInv);
        try {
            m.preMultiply(new BlockFieldMatrix<Fraction>(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.getRow(0), testDataRow1);
        TestUtils.assertEquals(m.getColumn(2), testDataCol3);
        try {
            m.getRow(10);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        Assert.assertEquals(m.getEntry(0,1),new Fraction(2));
        try {
            m.getEntry(10, 4);
            Assert.fail ("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = {
                {new Fraction(1),new Fraction(2),new Fraction(3)},
                {new Fraction(2),new Fraction(5),new Fraction(3)}
        };
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(matrixData);
        
        Fraction[][] matrixData2 = {
                {new Fraction(1),new Fraction(2)},
                {new Fraction(2),new Fraction(5)},
                {new Fraction(1), new Fraction(7)}
        };
        FieldMatrix<Fraction> n = new BlockFieldMatrix<Fraction>(matrixData2);
        
        FieldMatrix<Fraction> p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecomposition<Fraction>(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = {
                {new Fraction(2), new Fraction(3), new Fraction(-2)},
                {new Fraction(-1), new Fraction(7), new Fraction(6)},
                {new Fraction(4), new Fraction(-3), new Fraction(-5)}
        };
        FieldMatrix<Fraction> coefficients = new BlockFieldMatrix<Fraction>(coefficientsData);
        Fraction[] constants = {
            new Fraction(1), new Fraction(-2), new Fraction(1)
        };
        Fraction[] solution;
        solution = new FieldLUDecomposition<Fraction>(coefficients)
            .getSolver()
            .solve(new ArrayFieldVector<Fraction>(constants, false)).toArray();
        Assert.assertEquals(new Fraction(2).multiply(solution[0]).
                     add(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(2).multiply(solution[2])),
                     constants[0]);
        Assert.assertEquals(new Fraction(-1).multiply(solution[0]).
                     add(new Fraction(7).multiply(solution[1])).
                     add(new Fraction(6).multiply(solution[2])),
                     constants[1]);
        Assert.assertEquals(new Fraction(4).multiply(solution[0]).
                     subtract(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(5).multiply(solution[2])),
                     constants[2]);

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        checkGetSubMatrix(m, subRows23Cols00,  2 , 3 , 0, 0);
        checkGetSubMatrix(m, subRows00Cols33,  0 , 0 , 3, 3);
        checkGetSubMatrix(m, subRows01Cols23,  0 , 1 , 2, 3);
        checkGetSubMatrix(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 });
        checkGetSubMatrix(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 });
        checkGetSubMatrix(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 });
        checkGetSubMatrix(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 });
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkGetSubMatrix(m, null,  1, 0, 2, 4);
        checkGetSubMatrix(m, null, -1, 1, 2, 2);
        checkGetSubMatrix(m, null,  1, 0, 2, 2);
        checkGetSubMatrix(m, null,  1, 0, 2, 4);
        checkGetSubMatrix(m, null, new int[] {},    new int[] { 0 });
        checkGetSubMatrix(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetMatrixLarge
    public void testGetSetMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n - 4, n - 4).scalarAdd(new Fraction(1));

        m.setSubMatrix(sub.getData(), 2, 2);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((i < 2) || (i > n - 3) || (j < 2) || (j > n - 3)) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        Assert.assertEquals(sub, m.getSubMatrix(2, n - 3, 2, n - 3));
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        checkCopy(m, subRows23Cols00,  2 , 3 , 0, 0);
        checkCopy(m, subRows00Cols33,  0 , 0 , 3, 3);
        checkCopy(m, subRows01Cols23,  0 , 1 , 2, 3);
        checkCopy(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 });
        checkCopy(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 });
        checkCopy(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 });
        checkCopy(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 });
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });

        checkCopy(m, null,  1, 0, 2, 4);
        checkCopy(m, null, -1, 1, 2, 2);
        checkCopy(m, null,  1, 0, 2, 2);
        checkCopy(m, null,  1, 0, 2, 4);
        checkCopy(m, null, new int[] {}, new int[] { 0 });
        checkCopy(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m     = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow0 = new BlockFieldMatrix<Fraction>(subRow0);
        FieldMatrix<Fraction> mRow3 = new BlockFieldMatrix<Fraction>(subRow3);
        Assert.assertEquals("Row0", mRow0, m.getRowMatrix(0));
        Assert.assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow3 = new BlockFieldMatrix<Fraction>(subRow3);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetRowMatrixLarge
    public void testGetSetRowMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), 1, n).scalarAdd(new Fraction(1));

        m.setRowMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        Assert.assertEquals(sub, m.getRowMatrix(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn1 = new BlockFieldMatrix<Fraction>(subColumn1);
        FieldMatrix<Fraction> mColumn3 = new BlockFieldMatrix<Fraction>(subColumn3);
        Assert.assertEquals(mColumn1, m.getColumnMatrix(1));
        Assert.assertEquals(mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn3 = new BlockFieldMatrix<Fraction>(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetColumnMatrixLarge
    public void testGetSetColumnMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, 1).scalarAdd(new Fraction(1));

        m.setColumnMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        Assert.assertEquals(sub, m.getColumnMatrix(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        Assert.assertEquals(mRow0, m.getRowVector(0));
        Assert.assertEquals(mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetRowVector
    public void testSetRowVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetRowVectorLarge
    public void testGetSetRowVectorLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldVector<Fraction> sub = new ArrayFieldVector<Fraction>(n, new Fraction(1));

        m.setRowVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        Assert.assertEquals(sub, m.getRowVector(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        Assert.assertEquals(mColumn1, m.getColumnVector(1));
        Assert.assertEquals(mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetColumnVectorLarge
    public void testGetSetColumnVectorLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldVector<Fraction> sub = new ArrayFieldVector<Fraction>(n, new Fraction(1));

        m.setColumnVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        Assert.assertEquals(sub, m.getColumnVector(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetRow
    public void testGetRow() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRow(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetRow
    public void testSetRow() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Assert.assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new Fraction[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetRowLarge
    public void testGetSetRowLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        Fraction[] sub = new Fraction[n];
        Arrays.fill(sub, new Fraction(1));

        m.setRow(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        checkArrays(sub, m.getRow(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetColumn
    public void testGetColumn() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn1 = columnToArray(subColumn1);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetColumn
    public void testSetColumn() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        Assert.assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new Fraction[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetColumnLarge
    public void testGetSetColumnLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        Fraction[] sub = new Fraction[n];
        Arrays.fill(sub, new Fraction(1));

        m.setColumn(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        checkArrays(sub, m.getColumn(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m1 = (BlockFieldMatrix<Fraction>) m.copy();
        BlockFieldMatrix<Fraction> mt = (BlockFieldMatrix<Fraction>) m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(new BlockFieldMatrix<Fraction>(bigSingular)));
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testToString
    public void testToString() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        Assert.assertEquals("BlockFieldMatrix{{1,2,3},{2,5,3},{1,0,8}}", m.toString());
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        m.setSubMatrix(detData2,1,1);
        FieldMatrix<Fraction> expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(1),new Fraction(2),new Fraction(3)},{new Fraction(2),new Fraction(1),new Fraction(3)},{new Fraction(1),new Fraction(2),new Fraction(4)}});
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(1),new Fraction(3),new Fraction(3)},{new Fraction(2),new Fraction(4),new Fraction(3)},{new Fraction(1),new Fraction(2),new Fraction(4)}});
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(3),new Fraction(4),new Fraction(5)},{new Fraction(4),new Fraction(7),new Fraction(5)},{new Fraction(3),new Fraction(2),new Fraction(10)}});
        Assert.assertEquals(expected, m);

        
        BlockFieldMatrix<Fraction> matrix =
            new BlockFieldMatrix<Fraction>(new Fraction[][] {
                    {new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4)},
                    {new Fraction(5), new Fraction(6), new Fraction(7), new Fraction(8)},
                    {new Fraction(9), new Fraction(0), new Fraction(1) , new Fraction(2)}
            });
        matrix.setSubMatrix(new Fraction[][] {
                {new Fraction(3), new Fraction(4)},
                {new Fraction(5), new Fraction(6)}
        }, 1, 1);
        expected =
            new BlockFieldMatrix<Fraction>(new Fraction[][] {
                    {new Fraction(1), new Fraction(2), new Fraction(3),new Fraction(4)},
                    {new Fraction(5), new Fraction(3), new Fraction(4), new Fraction(8)},
                    {new Fraction(9), new Fraction(5) ,new Fraction(6), new Fraction(2)}
            });
        Assert.assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData,1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{new Fraction(1)}, {new Fraction(2), new Fraction(3)}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSerial
    public void testSerial()  {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        Assert.assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testDimensions
    public void testDimensions() {
        FieldMatrix<Fraction> matrix =
            new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData);
        FieldLUDecomposition<Fraction> LU = new FieldLUDecomposition<Fraction>(matrix);
        Assert.assertEquals(testData.length, LU.getL().getRowDimension());
        Assert.assertEquals(testData.length, LU.getL().getColumnDimension());
        Assert.assertEquals(testData.length, LU.getU().getRowDimension());
        Assert.assertEquals(testData.length, LU.getU().getColumnDimension());
        Assert.assertEquals(testData.length, LU.getP().getRowDimension());
        Assert.assertEquals(testData.length, LU.getP().getColumnDimension());

    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testNonSquare
    public void testNonSquare() {
        try {
            
            new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                    { Fraction.ZERO, Fraction.ZERO },
                    { Fraction.ZERO, Fraction.ZERO },
                    { Fraction.ZERO, Fraction.ZERO }
            }));
            Assert.fail("Expected NonSquareMatrixException");
        } catch (NonSquareMatrixException ime) {
            
        }
    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testPAEqualLU
    public void testPAEqualLU() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData);
        FieldLUDecomposition<Fraction> lu = new FieldLUDecomposition<Fraction>(matrix);
        FieldMatrix<Fraction> l = lu.getL();
        FieldMatrix<Fraction> u = lu.getU();
        FieldMatrix<Fraction> p = lu.getP();
        TestUtils.assertEquals(p.multiply(matrix), l.multiply(u));

        matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testDataMinus);
        lu = new FieldLUDecomposition<Fraction>(matrix);
        l = lu.getL();
        u = lu.getU();
        p = lu.getP();
        TestUtils.assertEquals(p.multiply(matrix), l.multiply(u));

        matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), 17, 17);
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            matrix.setEntry(i, i, Fraction.ONE);
        }
        lu = new FieldLUDecomposition<Fraction>(matrix);
        l = lu.getL();
        u = lu.getU();
        p = lu.getP();
        TestUtils.assertEquals(p.multiply(matrix), l.multiply(u));

        matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), singular);
        lu = new FieldLUDecomposition<Fraction>(matrix);
        Assert.assertFalse(lu.getSolver().isNonSingular());
        Assert.assertNull(lu.getL());
        Assert.assertNull(lu.getU());
        Assert.assertNull(lu.getP());

        matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), bigSingular);
        lu = new FieldLUDecomposition<Fraction>(matrix);
        Assert.assertFalse(lu.getSolver().isNonSingular());
        Assert.assertNull(lu.getL());
        Assert.assertNull(lu.getU());
        Assert.assertNull(lu.getP());

    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testLLowerTriangular
    public void testLLowerTriangular() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData);
        FieldMatrix<Fraction> l = new FieldLUDecomposition<Fraction>(matrix).getL();
        for (int i = 0; i < l.getRowDimension(); i++) {
            Assert.assertEquals(Fraction.ONE, l.getEntry(i, i));
            for (int j = i + 1; j < l.getColumnDimension(); j++) {
                Assert.assertEquals(Fraction.ZERO, l.getEntry(i, j));
            }
        }
    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testUUpperTriangular
    public void testUUpperTriangular() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData);
        FieldMatrix<Fraction> u = new FieldLUDecomposition<Fraction>(matrix).getU();
        for (int i = 0; i < u.getRowDimension(); i++) {
            for (int j = 0; j < i; j++) {
                Assert.assertEquals(Fraction.ZERO, u.getEntry(i, j));
            }
        }
    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testPPermutation
    public void testPPermutation() {
        FieldMatrix<Fraction> matrix = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData);
        FieldMatrix<Fraction> p   = new FieldLUDecomposition<Fraction>(matrix).getP();

        FieldMatrix<Fraction> ppT = p.multiply(p.transpose());
        FieldMatrix<Fraction> id  =
            new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(),
                                          p.getRowDimension(), p.getRowDimension());
        for (int i = 0; i < id.getRowDimension(); ++i) {
            id.setEntry(i, i, Fraction.ONE);
        }
        TestUtils.assertEquals(id, ppT);

        for (int i = 0; i < p.getRowDimension(); i++) {
            int zeroCount  = 0;
            int oneCount   = 0;
            int otherCount = 0;
            for (int j = 0; j < p.getColumnDimension(); j++) {
                final Fraction e = p.getEntry(i, j);
                if (e.equals(Fraction.ZERO)) {
                    ++zeroCount;
                } else if (e.equals(Fraction.ONE)) {
                    ++oneCount;
                } else {
                    ++otherCount;
                }
            }
            Assert.assertEquals(p.getColumnDimension() - 1, zeroCount);
            Assert.assertEquals(1, oneCount);
            Assert.assertEquals(0, otherCount);
        }

        for (int j = 0; j < p.getColumnDimension(); j++) {
            int zeroCount  = 0;
            int oneCount   = 0;
            int otherCount = 0;
            for (int i = 0; i < p.getRowDimension(); i++) {
                final Fraction e = p.getEntry(i, j);
                if (e.equals(Fraction.ZERO)) {
                    ++zeroCount;
                } else if (e.equals(Fraction.ONE)) {
                    ++oneCount;
                } else {
                    ++otherCount;
                }
            }
            Assert.assertEquals(p.getRowDimension() - 1, zeroCount);
            Assert.assertEquals(1, oneCount);
            Assert.assertEquals(0, otherCount);
        }

    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testSingular
    public void testSingular() {
        FieldLUDecomposition<Fraction> lu =
            new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData));
        Assert.assertTrue(lu.getSolver().isNonSingular());
        lu = new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), singular));
        Assert.assertFalse(lu.getSolver().isNonSingular());
        lu = new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), bigSingular));
        Assert.assertFalse(lu.getSolver().isNonSingular());
    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testMatricesValues1
    public void testMatricesValues1() {
       FieldLUDecomposition<Fraction> lu =
            new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testData));
        FieldMatrix<Fraction> lRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(2), new Fraction(1), new Fraction(0) },
                { new Fraction(1), new Fraction(-2), new Fraction(1) }
        });
        FieldMatrix<Fraction> uRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(1),  new Fraction(2), new Fraction(3) },
                { new Fraction(0), new Fraction(1), new Fraction(-3) },
                { new Fraction(0),  new Fraction(0), new Fraction(-1) }
        });
        FieldMatrix<Fraction> pRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(0), new Fraction(1), new Fraction(0) },
                { new Fraction(0), new Fraction(0), new Fraction(1) }
        });
        int[] pivotRef = { 0, 1, 2 };

        
        FieldMatrix<Fraction> l = lu.getL();
        TestUtils.assertEquals(lRef, l);
        FieldMatrix<Fraction> u = lu.getU();
        TestUtils.assertEquals(uRef, u);
        FieldMatrix<Fraction> p = lu.getP();
        TestUtils.assertEquals(pRef, p);
        int[] pivot = lu.getPivot();
        for (int i = 0; i < pivotRef.length; ++i) {
            Assert.assertEquals(pivotRef[i], pivot[i]);
        }

        
        Assert.assertTrue(l == lu.getL());
        Assert.assertTrue(u == lu.getU());
        Assert.assertTrue(p == lu.getP());

    }

// org.apache.commons.math3.linear.FieldLUDecompositionTest::testMatricesValues2
    public void testMatricesValues2() {
       FieldLUDecomposition<Fraction> lu =
            new FieldLUDecomposition<Fraction>(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), luData));
        FieldMatrix<Fraction> lRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(3), new Fraction(1), new Fraction(0) },
                { new Fraction(1), new Fraction(0), new Fraction(1) }
        });
        FieldMatrix<Fraction> uRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(2), new Fraction(3), new Fraction(3)    },
                { new Fraction(0), new Fraction(-3), new Fraction(-1)  },
                { new Fraction(0), new Fraction(0), new Fraction(4) }
        });
        FieldMatrix<Fraction> pRef = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), new Fraction[][] {
                { new Fraction(1), new Fraction(0), new Fraction(0) },
                { new Fraction(0), new Fraction(0), new Fraction(1) },
                { new Fraction(0), new Fraction(1), new Fraction(0) }
        });
        int[] pivotRef = { 0, 2, 1 };

        
        FieldMatrix<Fraction> l = lu.getL();
        TestUtils.assertEquals(lRef, l);
        FieldMatrix<Fraction> u = lu.getU();
        TestUtils.assertEquals(uRef, u);
        FieldMatrix<Fraction> p = lu.getP();
        TestUtils.assertEquals(pRef, p);
        int[] pivot = lu.getPivot();
        for (int i = 0; i < pivotRef.length; ++i) {
            Assert.assertEquals(pivotRef[i], pivot[i]);
        }

        
        Assert.assertTrue(l == lu.getL());
        Assert.assertTrue(u == lu.getU());
        Assert.assertTrue(p == lu.getP());
    }

// org.apache.commons.math3.linear.FieldLUSolverTest::testSingular
    public void testSingular() {
        FieldDecompositionSolver<Fraction> solver;
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(testData))
            .getSolver();
        Assert.assertTrue(solver.isNonSingular());
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(singular))
            .getSolver();
        Assert.assertFalse(solver.isNonSingular());
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(bigSingular))
            .getSolver();
        Assert.assertFalse(solver.isNonSingular());
    }

// org.apache.commons.math3.linear.FieldLUSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        FieldDecompositionSolver<Fraction> solver;
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(testData))
            .getSolver();
        FieldMatrix<Fraction> b = createFractionMatrix(new int[2][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math3.linear.FieldLUSolverTest::testSolveSingularityErrors
    public void testSolveSingularityErrors() {
        FieldDecompositionSolver<Fraction> solver;
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(singular))
            .getSolver();
        FieldMatrix<Fraction> b = createFractionMatrix(new int[2][2]);
        try {
            solver.solve(b);
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
        try {
            solver.solve(b.getColumnVector(0));
            Assert.fail("an exception should have been thrown");
        } catch (SingularMatrixException ime) {
            
        }
    }

// org.apache.commons.math3.linear.FieldLUSolverTest::testSolve
    public void testSolve() {
        FieldDecompositionSolver<Fraction> solver;
        solver = new FieldLUDecomposition<Fraction>(createFractionMatrix(testData))
            .getSolver();
        FieldMatrix<Fraction> b = createFractionMatrix(new int[][] {
                { 1, 0 }, { 2, -5 }, { 3, 1 }
        });
        FieldMatrix<Fraction> xRef = createFractionMatrix(new int[][] {
                { 19, -71 }, { -6, 22 }, { -2, 9 }
        });

        
        FieldMatrix<Fraction> x = solver.solve(b);
        for (int i = 0; i < x.getRowDimension(); i++){
            for (int j = 0; j < x.getColumnDimension(); j++){
                Assert.assertEquals("(" + i + ", " + j + ")",
                                    xRef.getEntry(i, j), x.getEntry(i, j));
            }
        }

        
        for (int j = 0; j < b.getColumnDimension(); j++) {
            final FieldVector<Fraction> xj = solver.solve(b.getColumnVector(j));
            for (int i = 0; i < xj.getDimension(); i++){
                Assert.assertEquals("(" + i + ", " + j + ")",
                                    xRef.getEntry(i, j), xj.getEntry(i));
            }
        }

        
        for (int j = 0; j < b.getColumnDimension(); j++) {
            final SparseFieldVector<Fraction> bj;
            bj = new SparseFieldVector<Fraction>(FractionField.getInstance(),
                                                 b.getColumn(j));
            final FieldVector<Fraction> xj = solver.solve(bj);
            for (int i = 0; i < xj.getDimension(); i++) {
                Assert.assertEquals("(" + i + ", " + j + ")",
                                    xRef.getEntry(i, j), xj.getEntry(i));
            }
        }
    }

// org.apache.commons.math3.linear.FieldLUSolverTest::testDeterminant
    public void testDeterminant() {
        Assert.assertEquals( -1, getDeterminant(createFractionMatrix(testData)), 1E-15);
        Assert.assertEquals(-10, getDeterminant(createFractionMatrix(luData)), 1E-14);
        Assert.assertEquals(  0, getDeterminant(createFractionMatrix(singular)), 1E-15);
        Assert.assertEquals(  0, getDeterminant(createFractionMatrix(bigSingular)), 1E-15);
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testDimensions
    public void testDimensions() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testData2);
        Assert.assertEquals("testData row dimension",3,m.getRowDimension());
        Assert.assertEquals("testData column dimension",3,m.getColumnDimension());
        Assert.assertTrue("testData is square",m.isSquare());
        Assert.assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        Assert.assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        Assert.assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testCopyFunctions
    public void testCopyFunctions() {
        Array2DRowFieldMatrix<Fraction> m1 = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(m1.getData());
        Assert.assertEquals(m2,m1);
        Array2DRowFieldMatrix<Fraction> m3 = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m4 = new Array2DRowFieldMatrix<Fraction>(m3.getData(), false);
        Assert.assertEquals(m4,m3);
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testAdd
    public void testAdd() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        Fraction[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals(testDataPlusInv[row][col],sumEntries[row][col]);
            }
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testAddFail
    public void testAddFail() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testPlusMinus
    public void testPlusMinus() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        TestUtils.assertEquals(m.subtract(m2),m2.scalarMultiply(new Fraction(-1)).add(m));
        try {
            m.subtract(new Array2DRowFieldMatrix<Fraction>(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testMultiply
     public void testMultiply() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        Array2DRowFieldMatrix<Fraction> identity = new Array2DRowFieldMatrix<Fraction>(id);
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(testData2);
        TestUtils.assertEquals(m.multiply(mInv), identity);
        TestUtils.assertEquals(mInv.multiply(m), identity);
        TestUtils.assertEquals(m.multiply(identity), m);
        TestUtils.assertEquals(identity.multiply(mInv), mInv);
        TestUtils.assertEquals(m2.multiply(identity), m2);
        try {
            m.multiply(new Array2DRowFieldMatrix<Fraction>(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testMultiply2
    public void testMultiply2() {
       FieldMatrix<Fraction> m3 = new Array2DRowFieldMatrix<Fraction>(d3);
       FieldMatrix<Fraction> m4 = new Array2DRowFieldMatrix<Fraction>(d4);
       FieldMatrix<Fraction> m5 = new Array2DRowFieldMatrix<Fraction>(d5);
       TestUtils.assertEquals(m3.multiply(m4), m5);
   }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testPower
    public void testPower() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        FieldMatrix<Fraction> mPlusInv = new Array2DRowFieldMatrix<Fraction>(testDataPlusInv);
        FieldMatrix<Fraction> identity = new Array2DRowFieldMatrix<Fraction>(id);

        TestUtils.assertEquals(m.power(0), identity);
        TestUtils.assertEquals(mInv.power(0), identity);
        TestUtils.assertEquals(mPlusInv.power(0), identity);

        TestUtils.assertEquals(m.power(1), m);
        TestUtils.assertEquals(mInv.power(1), mInv);
        TestUtils.assertEquals(mPlusInv.power(1), mPlusInv);

        FieldMatrix<Fraction> C1 = m.copy();
        FieldMatrix<Fraction> C2 = mInv.copy();
        FieldMatrix<Fraction> C3 = mPlusInv.copy();

        
        for (int i = 2; i <= 5; ++i) {
            C1 = C1.multiply(m);
            C2 = C2.multiply(mInv);
            C3 = C3.multiply(mPlusInv);

            TestUtils.assertEquals(m.power(i), C1);
            TestUtils.assertEquals(mInv.power(i), C2);
            TestUtils.assertEquals(mPlusInv.power(i), C3);
        }

        try {
            FieldMatrix<Fraction> mNotSquare = new Array2DRowFieldMatrix<Fraction>(testData2T);
            mNotSquare.power(2);
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }

        try {
            m.power(-1);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(id);
        Assert.assertEquals("identity trace",new Fraction(3),m.getTrace());
        m = new Array2DRowFieldMatrix<Fraction>(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(new Array2DRowFieldMatrix<Fraction>(testDataPlus2), m.scalarAdd(new Fraction(2)));
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(testVector, m.operate(testVector));
        TestUtils.assertEquals(testVector, m.operate(new ArrayFieldVector<Fraction>(testVector)).getData());
        m = new Array2DRowFieldMatrix<Fraction>(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = new Array2DRowFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(2) }, { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) }
        }, false);
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals( new Fraction(3), b[0]);
        Assert.assertEquals( new Fraction(7), b[1]);
        Assert.assertEquals(new Fraction(11), b[2]);
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecomposition<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecomposition<Fraction>(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals(mIT, mTI);
        m = new Array2DRowFieldMatrix<Fraction>(testData2);
        FieldMatrix<Fraction> mt = new Array2DRowFieldMatrix<Fraction>(testData2T);
        TestUtils.assertEquals(mt, m.transpose());
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.preMultiply(testVector), preMultTest);
        TestUtils.assertEquals(m.preMultiply(new ArrayFieldVector<Fraction>(testVector).getData()),
                               preMultTest);
        m = new Array2DRowFieldMatrix<Fraction>(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testPremultiply
    public void testPremultiply() {
        FieldMatrix<Fraction> m3 = new Array2DRowFieldMatrix<Fraction>(d3);
        FieldMatrix<Fraction> m4 = new Array2DRowFieldMatrix<Fraction>(d4);
        FieldMatrix<Fraction> m5 = new Array2DRowFieldMatrix<Fraction>(d5);
        TestUtils.assertEquals(m4.preMultiply(m3), m5);

        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> mInv = new Array2DRowFieldMatrix<Fraction>(testDataInv);
        Array2DRowFieldMatrix<Fraction> identity = new Array2DRowFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(m.preMultiply(mInv), identity);
        TestUtils.assertEquals(mInv.preMultiply(m), identity);
        TestUtils.assertEquals(m.preMultiply(identity), m);
        TestUtils.assertEquals(identity.preMultiply(mInv), mInv);
        try {
            m.preMultiply(new Array2DRowFieldMatrix<Fraction>(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.getRow(0), testDataRow1);
        TestUtils.assertEquals(m.getColumn(2), testDataCol3);
        try {
            m.getRow(10);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Assert.assertEquals("get entry", m.getEntry(0,1), new Fraction(2));
        try {
            m.getEntry(10, 4);
            Assert.fail ("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = {
                {new Fraction(1),new Fraction(2),new Fraction(3)},
                {new Fraction(2),new Fraction(5),new Fraction(3)}
        };
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(matrixData);
        
        Fraction[][] matrixData2 = {
                {new Fraction(1),new Fraction(2)},
                {new Fraction(2),new Fraction(5)},
                {new Fraction(1), new Fraction(7)}
        };
        FieldMatrix<Fraction> n = new Array2DRowFieldMatrix<Fraction>(matrixData2);
        
        FieldMatrix<Fraction> p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecomposition<Fraction>(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = {
                {new Fraction(2), new Fraction(3), new Fraction(-2)},
                {new Fraction(-1), new Fraction(7), new Fraction(6)},
                {new Fraction(4), new Fraction(-3), new Fraction(-5)}
        };
        FieldMatrix<Fraction> coefficients = new Array2DRowFieldMatrix<Fraction>(coefficientsData);
        Fraction[] constants = {
            new Fraction(1), new Fraction(-2), new Fraction(1)
        };
        Fraction[] solution;
        solution = new FieldLUDecomposition<Fraction>(coefficients)
            .getSolver()
            .solve(new ArrayFieldVector<Fraction>(constants, false)).toArray();
        Assert.assertEquals(new Fraction(2).multiply(solution[0]).
                     add(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(2).multiply(solution[2])), constants[0]);
        Assert.assertEquals(new Fraction(-1).multiply(solution[0]).
                     add(new Fraction(7).multiply(solution[1])).
                     add(new Fraction(6).multiply(solution[2])), constants[1]);
        Assert.assertEquals(new Fraction(4).multiply(solution[0]).
                     subtract(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(5).multiply(solution[2])), constants[2]);

    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetSubMatrix
    public void testGetSubMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        checkGetSubMatrix(m, subRows23Cols00,  2 , 3 , 0, 0);
        checkGetSubMatrix(m, subRows00Cols33,  0 , 0 , 3, 3);
        checkGetSubMatrix(m, subRows01Cols23,  0 , 1 , 2, 3);
        checkGetSubMatrix(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 });
        checkGetSubMatrix(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 });
        checkGetSubMatrix(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 });
        checkGetSubMatrix(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 });
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkGetSubMatrix(m, null,  1, 0, 2, 4);
        checkGetSubMatrix(m, null, -1, 1, 2, 2);
        checkGetSubMatrix(m, null,  1, 0, 2, 2);
        checkGetSubMatrix(m, null,  1, 0, 2, 4);
        checkGetSubMatrix(m, null, new int[] {},    new int[] { 0 });
        checkGetSubMatrix(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testCopySubMatrix
    public void testCopySubMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        checkCopy(m, subRows23Cols00,  2 , 3 , 0, 0);
        checkCopy(m, subRows00Cols33,  0 , 0 , 3, 3);
        checkCopy(m, subRows01Cols23,  0 , 1 , 2, 3);
        checkCopy(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 });
        checkCopy(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 });
        checkCopy(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 });
        checkCopy(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 });
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });

        checkCopy(m, null,  1, 0, 2, 4);
        checkCopy(m, null, -1, 1, 2, 2);
        checkCopy(m, null,  1, 0, 2, 2);
        checkCopy(m, null,  1, 0, 2, 4);
        checkCopy(m, null, new int[] {},    new int[] { 0 });
        checkCopy(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow0 = new Array2DRowFieldMatrix<Fraction>(subRow0);
        FieldMatrix<Fraction> mRow3 = new Array2DRowFieldMatrix<Fraction>(subRow3);
        Assert.assertEquals("Row0", mRow0,
                m.getRowMatrix(0));
        Assert.assertEquals("Row3", mRow3,
                m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetRowMatrix
    public void testSetRowMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow3 = new Array2DRowFieldMatrix<Fraction>(subRow3);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn1 = new Array2DRowFieldMatrix<Fraction>(subColumn1);
        FieldMatrix<Fraction> mColumn3 = new Array2DRowFieldMatrix<Fraction>(subColumn3);
        Assert.assertEquals("Column1", mColumn1,
                m.getColumnMatrix(1));
        Assert.assertEquals("Column3", mColumn3,
                m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn3 = new Array2DRowFieldMatrix<Fraction>(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        Assert.assertEquals("Row0", mRow0, m.getRowVector(0));
        Assert.assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetRowVector
    public void testSetRowVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        Assert.assertEquals("Column1", mColumn1, m.getColumnVector(1));
        Assert.assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetColumnVector
    public void testSetColumnVector() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetRow
    public void testGetRow() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRow(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetRow
    public void testSetRow() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        Assert.assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new Fraction[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testGetColumn
    public void testGetColumn() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn1 = columnToArray(subColumn1);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetColumn
    public void testSetColumn() {
        FieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        Assert.assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new Fraction[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Array2DRowFieldMatrix<Fraction> m1 = (Array2DRowFieldMatrix<Fraction>) m.copy();
        Array2DRowFieldMatrix<Fraction> mt = (Array2DRowFieldMatrix<Fraction>) m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(new Array2DRowFieldMatrix<Fraction>(bigSingular)));
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testToString
    public void testToString() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Assert.assertEquals("Array2DRowFieldMatrix{{1,2,3},{2,5,3},{1,0,8}}", m.toString());
        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance());
        Assert.assertEquals("Array2DRowFieldMatrix{}", m.toString());
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSetSubMatrix
    public void testSetSubMatrix() {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        m.setSubMatrix(detData2,1,1);
        FieldMatrix<Fraction> expected = new Array2DRowFieldMatrix<Fraction>
            (new Fraction[][] {
                    {new Fraction(1),new Fraction(2),new Fraction(3)},
                    {new Fraction(2),new Fraction(1),new Fraction(3)},
                    {new Fraction(1),new Fraction(2),new Fraction(4)}
             });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new Array2DRowFieldMatrix<Fraction>
            (new Fraction[][] {
                    {new Fraction(1),new Fraction(3),new Fraction(3)},
                    {new Fraction(2),new Fraction(4),new Fraction(3)},
                    {new Fraction(1),new Fraction(2),new Fraction(4)}
             });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new Array2DRowFieldMatrix<Fraction>
            (new Fraction[][] {
                    {new Fraction(3),new Fraction(4),new Fraction(5)},
                    {new Fraction(4),new Fraction(7),new Fraction(5)},
                    {new Fraction(3),new Fraction(2),new Fraction(10)}
             });
        Assert.assertEquals(expected, m);

        
        try {
            m.setSubMatrix(testData,1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }
        Array2DRowFieldMatrix<Fraction> m2 = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance());
        try {
            m2.setSubMatrix(testData,0,1);
            Assert.fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            Assert.fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{new Fraction(1)}, {new Fraction(2), new Fraction(3)}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        FieldMatrix<Fraction> m =
            new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }
    }

// org.apache.commons.math3.linear.FieldMatrixImplTest::testSerial
    public void testSerial()  {
        Array2DRowFieldMatrix<Fraction> m = new Array2DRowFieldMatrix<Fraction>(testData);
        Assert.assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateRealMatrix
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

// org.apache.commons.math3.linear.MatrixUtilsTest::testcreateFieldMatrix
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

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateRowRealMatrix
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

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateRowFieldMatrix
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

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateColumnRealMatrix
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

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateColumnFieldMatrix
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

// org.apache.commons.math3.linear.MatrixUtilsTest::testCreateIdentityMatrix
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

// org.apache.commons.math3.linear.MatrixUtilsTest::testcreateFieldIdentityMatrix
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

// org.apache.commons.math3.linear.MatrixUtilsTest::testBigFractionConverter
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

// org.apache.commons.math3.linear.MatrixUtilsTest::testFractionConverter
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

// org.apache.commons.math3.linear.MatrixUtilsTest::testSolveLowerTriangularSystem
    public void testSolveLowerTriangularSystem(){
        RealMatrix rm = new Array2DRowRealMatrix(
                new double[][] { {2,0,0,0 }, { 1,1,0,0 }, { 3,3,3,0 }, { 3,3,3,4 } },
                       false);
        RealVector b = new ArrayRealVector(new double[] { 2,3,4,8 }, false);
        MatrixUtils.solveLowerTriangularSystem(rm, b);
        TestUtils.assertEquals( new double[]{1,2,-1.66666666666667, 1.0}  , b.toArray() , 1.0e-12);
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testSolveUpperTriangularSystem
    public void testSolveUpperTriangularSystem(){
        RealMatrix rm = new Array2DRowRealMatrix(
                new double[][] { {1,2,3 }, { 0,1,1 }, { 0,0,2 } },
                       false);
        RealVector b = new ArrayRealVector(new double[] { 8,4,2 }, false);
        MatrixUtils.solveUpperTriangularSystem(rm, b);
        TestUtils.assertEquals( new double[]{-1,3,1}  , b.toArray() , 1.0e-12);
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testBlockInverse
    public void testBlockInverse() {
        final double[][] data = {
            { -1, 0, 123, 4 },
            { -56, 78.9, -0.1, -23.4 },
            { 5.67, 8, -9, 1011 },
            { 12, 345, -67.8, 9 },
        };

        final RealMatrix m = new Array2DRowRealMatrix(data);
        final int len = data.length;
        final double tol = 1e-14;

        for (int splitIndex = 0; splitIndex < 3; splitIndex++) {
            final RealMatrix mInv = MatrixUtils.blockInverse(m, splitIndex);
            final RealMatrix id = m.multiply(mInv);

            
            for (int i = 0; i < len; i++) {
                for (int j = 0; j < len; j++) {
                    final double entry = id.getEntry(i, j);
                    if (i == j) {
                        Assert.assertEquals("[" + i + "][" + j + "]",
                                            1, entry, tol);
                    } else {
                        Assert.assertEquals("[" + i + "][" + j + "]",
                                            0, entry, tol);
                    }
                }
            }
        }
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testBlockInverseNonInvertible
    public void testBlockInverseNonInvertible() {
        final double[][] data = {
            { -1, 0, 123, 4 },
            { -56, 78.9, -0.1, -23.4 },
            { 5.67, 8, -9, 1011 },
            { 5.67, 8, -9, 1011 },
        };

        MatrixUtils.blockInverse(new Array2DRowRealMatrix(data), 2);
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testIsSymmetric
    public void testIsSymmetric() {
        final double eps = Math.ulp(1d);

        final double[][] dataSym = {
            { 1, 2, 3 },
            { 2, 2, 5 },
            { 3, 5, 6 },
        };
        Assert.assertTrue(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataSym), eps));

        final double[][] dataNonSym = {
            { 1, 2, -3 },
            { 2, 2, 5 },
            { 3, 5, 6 },
        };
        Assert.assertFalse(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataNonSym), eps));
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testIsSymmetricTolerance
    public void testIsSymmetricTolerance() {
        final double eps = 1e-4;

        final double[][] dataSym1 = {
            { 1,   1, 1.00009 },
            { 1,   1, 1       },
            { 1.0, 1, 1       },
        };
        Assert.assertTrue(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataSym1), eps));
        final double[][] dataSym2 = {
            { 1,   1, 0.99990 },
            { 1,   1, 1       },
            { 1.0, 1, 1       },
        };
        Assert.assertTrue(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataSym2), eps));

        final double[][] dataNonSym1 = {
            { 1,   1, 1.00011 },
            { 1,   1, 1       },
            { 1.0, 1, 1       },
        };
        Assert.assertFalse(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataNonSym1), eps));
        final double[][] dataNonSym2 = {
            { 1,   1, 0.99989 },
            { 1,   1, 1       },
            { 1.0, 1, 1       },
        };
        Assert.assertFalse(MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(dataNonSym2), eps));
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCheckSymmetric1
    public void testCheckSymmetric1() {
        final double[][] dataSym = {
            { 1, 2, 3 },
            { 2, 2, 5 },
            { 3, 5, 6 },
        };
        MatrixUtils.checkSymmetric(MatrixUtils.createRealMatrix(dataSym), Math.ulp(1d));
    }

// org.apache.commons.math3.linear.MatrixUtilsTest::testCheckSymmetric2
    public void testCheckSymmetric2() {
        final double[][] dataNonSym = {
            { 1, 2, -3 },
            { 2, 2, 5 },
            { 3, 5, 6 },
        };
        MatrixUtils.checkSymmetric(MatrixUtils.createRealMatrix(dataNonSym), Math.ulp(1d));
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testDimensions
    public void testDimensions() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        Assert.assertEquals("testData row dimension", 3, m.getRowDimension());
        Assert.assertEquals("testData column dimension", 3, m.getColumnDimension());
        Assert.assertTrue("testData is square", m.isSquare());
        Assert.assertEquals("testData2 row dimension", m2.getRowDimension(), 2);
        Assert.assertEquals("testData2 column dimension", m2.getColumnDimension(), 3);
        Assert.assertTrue("testData2 is not square", !m2.isSquare());
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        SparseFieldMatrix<Fraction> m1 = createSparseMatrix(testData);
        FieldMatrix<Fraction> m2 = m1.copy();
        Assert.assertEquals(m1.getClass(), m2.getClass());
        Assert.assertEquals((m2), m1);
        SparseFieldMatrix<Fraction> m3 = createSparseMatrix(testData);
        FieldMatrix<Fraction> m4 = m3.copy();
        Assert.assertEquals(m3.getClass(), m4.getClass());
        Assert.assertEquals((m4), m3);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testAdd
    public void testAdd() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> mDataPlusInv = createSparseMatrix(testDataPlusInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals("sum entry entry",
                    mDataPlusInv.getEntry(row, col).doubleValue(), mPlusMInv.getEntry(row, col).doubleValue(),
                    entryTolerance);
            }
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testAddFail
    public void testAddFail() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testPlusMinus
    public void testPlusMinus() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> n = createSparseMatrix(testDataInv);
        assertClose("m-n = m + -n", m.subtract(n),
            n.scalarMultiply(new Fraction(-1)).add(m), entryTolerance);
        try {
            m.subtract(createSparseMatrix(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testMultiply
    public void testMultiply() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> identity = createSparseMatrix(id);
        SparseFieldMatrix<Fraction> m2 = createSparseMatrix(testData2);
        assertClose("inverse multiply", m.multiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", m.multiply(new Array2DRowFieldMatrix<Fraction>(FractionField.getInstance(), testDataInv)), identity,
                    entryTolerance);
        assertClose("inverse multiply", mInv.multiply(m), identity,
                entryTolerance);
        assertClose("identity multiply", m.multiply(identity), m,
                entryTolerance);
        assertClose("identity multiply", identity.multiply(mInv), mInv,
                entryTolerance);
        assertClose("identity multiply", m2.multiply(identity), m2,
                entryTolerance);
        try {
            m.multiply(createSparseMatrix(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testMultiply2
    public void testMultiply2() {
        FieldMatrix<Fraction> m3 = createSparseMatrix(d3);
        FieldMatrix<Fraction> m4 = createSparseMatrix(d4);
        FieldMatrix<Fraction> m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = createSparseMatrix(id);
        Assert.assertEquals("identity trace", 3d, m.getTrace().doubleValue(), entryTolerance);
        m = createSparseMatrix(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("scalar add", createSparseMatrix(testDataPlus2),
            m.scalarAdd(new Fraction(2)), entryTolerance);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = createSparseMatrix(id);
        assertClose("identity operate", testVector, m.operate(testVector),
                entryTolerance);
        assertClose("identity operate", testVector, m.operate(
                new ArrayFieldVector<Fraction>(testVector)).getData(), entryTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2) }, { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) } });
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals(3.0, b[0].doubleValue(), 1.0e-12);
        Assert.assertEquals(7.0, b[1].doubleValue(), 1.0e-12);
        Assert.assertEquals(11.0, b[2].doubleValue(), 1.0e-12);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecomposition<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecomposition<Fraction>(m.transpose()).getSolver().getInverse();
        assertClose("inverse-transpose", mIT, mTI, normTolerance);
        m = createSparseMatrix(testData2);
        FieldMatrix<Fraction> mt = createSparseMatrix(testData2T);
        assertClose("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("premultiply", m.preMultiply(testVector), preMultTest,
            normTolerance);
        assertClose("premultiply", m.preMultiply(
            new ArrayFieldVector<Fraction>(testVector).getData()), preMultTest, normTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testPremultiply
    public void testPremultiply() {
        FieldMatrix<Fraction> m3 = createSparseMatrix(d3);
        FieldMatrix<Fraction> m4 = createSparseMatrix(d4);
        FieldMatrix<Fraction> m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> mInv = createSparseMatrix(testDataInv);
        SparseFieldMatrix<Fraction> identity = createSparseMatrix(id);
        assertClose("inverse multiply", m.preMultiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", mInv.preMultiply(m), identity,
                entryTolerance);
        assertClose("identity multiply", m.preMultiply(identity), m,
                entryTolerance);
        assertClose("identity multiply", identity.preMultiply(mInv), mInv,
                entryTolerance);
        try {
            m.preMultiply(createSparseMatrix(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        assertClose("get row", m.getRow(0), testDataRow1, entryTolerance);
        assertClose("get col", m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = createSparseMatrix(testData);
        Assert.assertEquals("get entry", m.getEntry(0, 1).doubleValue(), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = { { new Fraction(1), new Fraction(2), new Fraction(3) }, { new Fraction(2), new Fraction(5), new Fraction(3) } };
        FieldMatrix<Fraction> m = createSparseMatrix(matrixData);
        
        Fraction[][] matrixData2 = { { new Fraction(1), new Fraction(2) }, { new Fraction(2), new Fraction(5) }, { new Fraction(1), new Fraction(7) } };
        FieldMatrix<Fraction> n = createSparseMatrix(matrixData2);
        
        FieldMatrix<Fraction> p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecomposition<Fraction>(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = { { new Fraction(2), new Fraction(3), new Fraction(-2) }, { new Fraction(-1), new Fraction(7), new Fraction(6) },
                { new Fraction(4), new Fraction(-3), new Fraction(-5) } };
        FieldMatrix<Fraction> coefficients = createSparseMatrix(coefficientsData);
        Fraction[] constants = { new Fraction(1), new Fraction(-2), new Fraction(1) };
        Fraction[] solution;
        solution = new FieldLUDecomposition<Fraction>(coefficients)
            .getSolver()
            .solve(new ArrayFieldVector<Fraction>(constants, false)).toArray();
        Assert.assertEquals((new Fraction(2).multiply((solution[0])).add(new Fraction(3).multiply(solution[1])).subtract(new Fraction(2).multiply(solution[2]))).doubleValue(),
                constants[0].doubleValue(), 1E-12);
        Assert.assertEquals(((new Fraction(-1).multiply(solution[0])).add(new Fraction(7).multiply(solution[1])).add(new Fraction(6).multiply(solution[2]))).doubleValue(),
                constants[1].doubleValue(), 1E-12);
        Assert.assertEquals(((new Fraction(4).multiply(solution[0])).subtract(new Fraction(3).multiply( solution[1])).subtract(new Fraction(5).multiply(solution[2]))).doubleValue(),
                constants[2].doubleValue(), 1E-12);

    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testSubMatrix
    public void testSubMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mRows23Cols00 = createSparseMatrix(subRows23Cols00);
        FieldMatrix<Fraction> mRows00Cols33 = createSparseMatrix(subRows00Cols33);
        FieldMatrix<Fraction> mRows01Cols23 = createSparseMatrix(subRows01Cols23);
        FieldMatrix<Fraction> mRows02Cols13 = createSparseMatrix(subRows02Cols13);
        FieldMatrix<Fraction> mRows03Cols12 = createSparseMatrix(subRows03Cols12);
        FieldMatrix<Fraction> mRows03Cols123 = createSparseMatrix(subRows03Cols123);
        FieldMatrix<Fraction> mRows20Cols123 = createSparseMatrix(subRows20Cols123);
        FieldMatrix<Fraction> mRows31Cols31 = createSparseMatrix(subRows31Cols31);
        Assert.assertEquals("Rows23Cols00", mRows23Cols00, m.getSubMatrix(2, 3, 0, 0));
        Assert.assertEquals("Rows00Cols33", mRows00Cols33, m.getSubMatrix(0, 0, 3, 3));
        Assert.assertEquals("Rows01Cols23", mRows01Cols23, m.getSubMatrix(0, 1, 2, 3));
        Assert.assertEquals("Rows02Cols13", mRows02Cols13,
            m.getSubMatrix(new int[] { 0, 2 }, new int[] { 1, 3 }));
        Assert.assertEquals("Rows03Cols12", mRows03Cols12,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2 }));
        Assert.assertEquals("Rows03Cols123", mRows03Cols123,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2, 3 }));
        Assert.assertEquals("Rows20Cols123", mRows20Cols123,
            m.getSubMatrix(new int[] { 2, 0 }, new int[] { 1, 2, 3 }));
        Assert.assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));
        Assert.assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));

        try {
            m.getSubMatrix(1, 0, 2, 4);
            Assert.fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(-1, 1, 2, 2);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 2);
            Assert.fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 4);
            Assert.fail("Expecting NumberIsTooSmallException");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] { 0 });
            Assert.fail("Expecting NoDataException");
        } catch (NoDataException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] { 0 }, new int[] { 4 });
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mRow0 = createSparseMatrix(subRow0);
        FieldMatrix<Fraction> mRow3 = createSparseMatrix(subRow3);
        Assert.assertEquals("Row0", mRow0, m.getRowMatrix(0));
        Assert.assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldMatrix<Fraction> mColumn1 = createSparseMatrix(subColumn1);
        FieldMatrix<Fraction> mColumn3 = createSparseMatrix(subColumn3);
        Assert.assertEquals("Column1", mColumn1, m.getColumnMatrix(1));
        Assert.assertEquals("Column3", mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        Assert.assertEquals("Row0", mRow0, m.getRowVector(0));
        Assert.assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = createSparseMatrix(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        Assert.assertEquals("Column1", mColumn1, m.getColumnVector(1));
        Assert.assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        SparseFieldMatrix<Fraction> m1 = (SparseFieldMatrix<Fraction>) m.copy();
        SparseFieldMatrix<Fraction> mt = (SparseFieldMatrix<Fraction>) m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(createSparseMatrix(bigSingular)));
    }

// org.apache.commons.math3.linear.SparseFieldMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() {
        SparseFieldMatrix<Fraction> m = createSparseMatrix(testData);
        m.setSubMatrix(detData2, 1, 1);
        FieldMatrix<Fraction> expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2), new Fraction(3) }, { new Fraction(2), new Fraction(1), new Fraction(3) }, { new Fraction(1), new Fraction(2), new Fraction(4) } });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2, 0, 0);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(3), new Fraction(3) }, { new Fraction(2), new Fraction(4), new Fraction(3) }, { new Fraction(1), new Fraction(2), new Fraction(4) } });
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2, 0, 0);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(3), new Fraction(4), new Fraction(5) }, { new Fraction(4), new Fraction(7), new Fraction(5) }, { new Fraction(3), new Fraction(2), new Fraction(10) } });
        Assert.assertEquals(expected, m);

        
        SparseFieldMatrix<Fraction> matrix =
            createSparseMatrix(new Fraction[][] {
        { new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6), new Fraction(7), new Fraction(8) }, { new Fraction(9), new Fraction(0), new Fraction(1), new Fraction(2) } });
        matrix.setSubMatrix(new Fraction[][] { { new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(6) } }, 1, 1);
        expected = createSparseMatrix(new Fraction[][] {
                { new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4) }, { new Fraction(5), new Fraction(3), new Fraction(4), new Fraction(8) }, { new Fraction(9), new Fraction(5), new Fraction(6), new Fraction(2) } });
        Assert.assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData, 1, 1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData, -1, 1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData, 1, -1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }
        try {
            new SparseFieldMatrix<Fraction>(field, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] { { new Fraction(1) }, { new Fraction(2), new Fraction(3) } }, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] { {} }, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }
    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testMapFunctions
    public void testMapFunctions() throws FractionConversionException {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);

        
        FieldVector<Fraction> v_mapAdd = v1.mapAdd(new Fraction(2));
        Fraction[] result_mapAdd = {new Fraction(3), new Fraction(4), new Fraction(5)};
        Assert.assertArrayEquals("compare vectors" ,result_mapAdd,v_mapAdd.getData());

        
        FieldVector<Fraction> v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(new Fraction(2));
        Fraction[] result_mapAddToSelf = {new Fraction(3), new Fraction(4), new Fraction(5)};
        Assert.assertArrayEquals("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData());

        
        FieldVector<Fraction> v_mapSubtract = v1.mapSubtract(new Fraction(2));
        Fraction[] result_mapSubtract = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        Assert.assertArrayEquals("compare vectors" ,result_mapSubtract,v_mapSubtract.getData());

        
        FieldVector<Fraction> v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(new Fraction(2));
        Fraction[] result_mapSubtractToSelf = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        Assert.assertArrayEquals("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData());

        
        FieldVector<Fraction> v_mapMultiply = v1.mapMultiply(new Fraction(2));
        Fraction[] result_mapMultiply = {new Fraction(2), new Fraction(4), new Fraction(6)};
        Assert.assertArrayEquals("compare vectors" ,result_mapMultiply,v_mapMultiply.getData());

        
        FieldVector<Fraction> v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(new Fraction(2));
        Fraction[] result_mapMultiplyToSelf = {new Fraction(2), new Fraction(4), new Fraction(6)};
        Assert.assertArrayEquals("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData());

        
        FieldVector<Fraction> v_mapDivide = v1.mapDivide(new Fraction(2));
        Fraction[] result_mapDivide = {new Fraction(.5d), new Fraction(1), new Fraction(1.5d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapDivide,v_mapDivide.getData());

        
        FieldVector<Fraction> v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(new Fraction(2));
        Fraction[] result_mapDivideToSelf = {new Fraction(.5d), new Fraction(1), new Fraction(1.5d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData());

        
        FieldVector<Fraction> v_mapInv = v1.mapInv();
        Fraction[] result_mapInv = {new Fraction(1),new Fraction(0.5d),new Fraction(3.333333333333333e-01d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapInv,v_mapInv.getData());

        
        FieldVector<Fraction> v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapInvToSelf();
        Fraction[] result_mapInvToSelf = {new Fraction(1),new Fraction(0.5d),new Fraction(3.333333333333333e-01d)};
        Assert.assertArrayEquals("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData());

    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testBasicFunctions
    public void testBasicFunctions() throws FractionConversionException {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);
        SparseFieldVector<Fraction> v2 = new SparseFieldVector<Fraction>(field,vec2);

        FieldVector<Fraction> v2_t = new ArrayFieldVectorTest.FieldVectorTestImpl<Fraction>(vec2);

        
        FieldVector<Fraction> v_add = v1.add(v2);
        Fraction[] result_add = {new Fraction(5), new Fraction(7), new Fraction(9)};
        Assert.assertArrayEquals("compare vect" ,v_add.getData(),result_add);

        FieldVector<Fraction> vt2 = new ArrayFieldVectorTest.FieldVectorTestImpl<Fraction>(vec2);
        FieldVector<Fraction> v_add_i = v1.add(vt2);
        Fraction[] result_add_i = {new Fraction(5), new Fraction(7), new Fraction(9)};
        Assert.assertArrayEquals("compare vect" ,v_add_i.getData(),result_add_i);

        
        SparseFieldVector<Fraction> v_subtract = v1.subtract(v2);
        Fraction[] result_subtract = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        assertClose("compare vect" ,v_subtract.getData(),result_subtract,normTolerance);

        FieldVector<Fraction> v_subtract_i = v1.subtract(vt2);
        Fraction[] result_subtract_i = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        assertClose("compare vect" ,v_subtract_i.getData(),result_subtract_i,normTolerance);

        
        FieldVector<Fraction>  v_ebeMultiply = v1.ebeMultiply(v2);
        Fraction[] result_ebeMultiply = {new Fraction(4), new Fraction(10), new Fraction(18)};
        assertClose("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply,normTolerance);

        FieldVector<Fraction>  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        Fraction[] result_ebeMultiply_2 = {new Fraction(4), new Fraction(10), new Fraction(18)};
        assertClose("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2,normTolerance);

        
        FieldVector<Fraction>  v_ebeDivide = v1.ebeDivide(v2);
        Fraction[] result_ebeDivide = {new Fraction(0.25d), new Fraction(0.4d), new Fraction(0.5d)};
        assertClose("compare vect" ,v_ebeDivide.getData(),result_ebeDivide,normTolerance);

        FieldVector<Fraction>  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        Fraction[] result_ebeDivide_2 = {new Fraction(0.25d), new Fraction(0.4d), new Fraction(0.5d)};
        assertClose("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2,normTolerance);

        
        Fraction dot =  v1.dotProduct(v2);
        Assert.assertEquals("compare val ",new Fraction(32), dot);

        
        Fraction dot_2 =  v1.dotProduct(v2_t);
        Assert.assertEquals("compare val ",new Fraction(32), dot_2);

        FieldMatrix<Fraction> m_outerProduct = v1.outerProduct(v2);
        Assert.assertEquals("compare val ",new Fraction(4), m_outerProduct.getEntry(0,0));

        FieldMatrix<Fraction> m_outerProduct_2 = v1.outerProduct(v2_t);
        Assert.assertEquals("compare val ",new Fraction(4), m_outerProduct_2.getEntry(0,0));

    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testOuterProduct
    public void testOuterProduct() {
        final SparseFieldVector<Fraction> u
            = new SparseFieldVector<Fraction>(FractionField.getInstance(),
                                              new Fraction[] {new Fraction(1),
                                                              new Fraction(2),
                                                              new Fraction(-3)});
        final SparseFieldVector<Fraction> v
            = new SparseFieldVector<Fraction>(FractionField.getInstance(),
                                              new Fraction[] {new Fraction(4),
                                                              new Fraction(-2)});

        final FieldMatrix<Fraction> uv = u.outerProduct(v);

        final double tol = Math.ulp(1d);
        Assert.assertEquals(new Fraction(4).doubleValue(), uv.getEntry(0, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-2).doubleValue(), uv.getEntry(0, 1).doubleValue(), tol);
        Assert.assertEquals(new Fraction(8).doubleValue(), uv.getEntry(1, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-4).doubleValue(), uv.getEntry(1, 1).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-12).doubleValue(), uv.getEntry(2, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(6).doubleValue(), uv.getEntry(2, 1).doubleValue(), tol);
    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testMisc
    public void testMisc() {
        SparseFieldVector<Fraction> v1 = new SparseFieldVector<Fraction>(field,vec1);

        String out1 = v1.toString();
        Assert.assertTrue("some output ",  out1.length()!=0);
        try {
            v1.checkVectorDimensions(2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math3.linear.SparseFieldVectorTest::testPredicates
    public void testPredicates() {

        SparseFieldVector<Fraction> v = new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2) });

        v.setEntry(0, field.getZero());
        Assert.assertEquals(v, new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2) }));
        Assert.assertNotSame(v, new SparseFieldVector<Fraction>(field, new Fraction[] { new Fraction(0), new Fraction(1), new Fraction(2), new Fraction(3) }));

    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::dimensionCheck
    public void dimensionCheck() throws NumberIsTooSmallException, DimensionMismatchException, MaxCountExceededException, NoBracketingException {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsBashforthIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::testMinStep
    public void testMinStep() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException
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

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::backward
    public void backward() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest::polynomial
    public void polynomial() throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {
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

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::dimensionCheck
    public void dimensionCheck()
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsMoultonIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::testMinStep
    public void testMinStep()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::backward
    public void backward()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest::polynomial
    public void polynomial()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {
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

// org.apache.commons.math3.ode.sampling.NordsieckStepInterpolatorTest::derivativesConsistency
    public void derivativesConsistency()
        throws NumberIsTooSmallException, DimensionMismatchException,
               MaxCountExceededException, NoBracketingException {
        TestProblem3 pb = new TestProblem3();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 5e-9);
    }

// org.apache.commons.math3.ode.sampling.NordsieckStepInterpolatorTest::serialization
    public void serialization()
    throws IOException, ClassNotFoundException,
           NumberIsTooSmallException, DimensionMismatchException,
           MaxCountExceededException, NoBracketingException {

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

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutAndGetWith0ExpectedSize
    public void testPutAndGetWith0ExpectedSize() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field,0);
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutAndGetWithExpectedSize
    public void testPutAndGetWithExpectedSize() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field,500);
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutAndGet
    public void testPutAndGet() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutAbsentOnExisting
    public void testPutAbsentOnExisting() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int size = javaMap.size();
        for (Map.Entry<Integer, Fraction> mapEntry : generateAbsent().entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            Assert.assertEquals(++size, map.size());
            Assert.assertEquals(mapEntry.getValue(), map.get(mapEntry.getKey()));
        }
    }
