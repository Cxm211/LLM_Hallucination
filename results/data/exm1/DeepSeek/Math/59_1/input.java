// buggy code
    public static float max(final float a, final float b) {
        return (a <= b) ? b : (Float.isNaN(a + b) ? Float.NaN : b);
    }

// relevant test
// org.apache.commons.math.distribution.TDistributionTest::testDfAccessors
    public void testDfAccessors() {
        TDistribution dist = (TDistribution) getDistribution();
        assertEquals(5d, dist.getDegreesOfFreedom(), Double.MIN_VALUE);
    }

// org.apache.commons.math.distribution.TDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new TDistributionImpl(0);
            fail("Expecting NotStrictlyPositiveException for df = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math.distribution.TDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        TDistribution dist;
        
        dist = new TDistributionImpl(1);
        assertTrue(Double.isNaN(dist.getNumericalMean()));
        assertTrue(Double.isNaN(dist.getNumericalVariance()));
        
        dist = new TDistributionImpl(1.5);
        assertEquals(dist.getNumericalMean(), 0, tol);
        assertTrue(Double.isInfinite(dist.getNumericalVariance()));
        
        dist = new TDistributionImpl(5);
        assertEquals(dist.getNumericalMean(), 0, tol);
        assertEquals(dist.getNumericalVariance(), 5d / (5d - 2d), tol);        
    }

// org.apache.commons.math.distribution.WeibullDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0.0, 1.0});
        setInverseCumulativeTestValues(
                new double[] {0.0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.WeibullDistributionTest::testAlpha
    public void testAlpha() {
        WeibullDistribution dist = new WeibullDistributionImpl(1, 2);
        assertEquals(1, dist.getShape(), 0);
        try {
            dist = new WeibullDistributionImpl(0, 2);
            fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math.distribution.WeibullDistributionTest::testBeta
    public void testBeta() {
        WeibullDistribution dist = new WeibullDistributionImpl(1, 2);
        assertEquals(2, dist.getScale(), 0);
        try {
            dist = new WeibullDistributionImpl(1, 0);
            fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math.distribution.WeibullDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        WeibullDistribution dist;
        
        dist = new WeibullDistributionImpl(2.5, 3.5);
        
        assertEquals(dist.getNumericalMean(), 3.5 * FastMath.exp(Gamma.logGamma(1 + (1 / 2.5))), tol);
        assertEquals(dist.getNumericalVariance(), (3.5 * 3.5) * 
                FastMath.exp(Gamma.logGamma(1 + (2 / 2.5))) -
                (dist.getNumericalMean() * dist.getNumericalMean()), tol); 
        
        dist = new WeibullDistributionImpl(10.4, 2.222);
        assertEquals(dist.getNumericalMean(), 2.222 * FastMath.exp(Gamma.logGamma(1 + (1 / 10.4))), tol);
        assertEquals(dist.getNumericalVariance(), (2.222 * 2.222) * 
                FastMath.exp(Gamma.logGamma(1 + (2 / 10.4))) -
                (dist.getNumericalMean() * dist.getNumericalMean()), tol);
    }

// org.apache.commons.math.distribution.WeibullDistributionTest::testSampling
    public void testSampling() {}

// org.apache.commons.math.distribution.ZipfDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new ZipfDistributionImpl(0, 1);
            fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
        try {
            new ZipfDistributionImpl(1, 0);
            fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math.distribution.ZipfDistributionTest::testMomonts
    public void testMomonts() {
        final double tol = 1e-9;
        ZipfDistribution dist;
        
        dist = new ZipfDistributionImpl(2, 0.5);
        assertEquals(dist.getNumericalMean(), FastMath.sqrt(2), tol);
        assertEquals(dist.getNumericalVariance(), 0.24264068711928521, tol); 
    }

// org.apache.commons.math.fraction.BigFractionTest::testConstructor
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

        try {
            assertFraction(0, 1, new BigFraction(0.00000000000001, 1.0e-5, 100));
            assertFraction(2, 5, new BigFraction(0.40000000000001, 1.0e-5, 100));
            assertFraction(15, 1, new BigFraction(15.0000000000001, 1.0e-5, 100));
        } catch (ConvergenceException ex) {
            fail(ex.getMessage());
        }
        assertEquals(0.00000000000001, new BigFraction(0.00000000000001).doubleValue(), 0.0);
        assertEquals(0.40000000000001, new BigFraction(0.40000000000001).doubleValue(), 0.0);
        assertEquals(15.0000000000001, new BigFraction(15.0000000000001).doubleValue(), 0.0);
        assertFraction(3602879701896487l, 9007199254740992l, new BigFraction(0.40000000000001));
        assertFraction(1055531162664967l, 70368744177664l, new BigFraction(15.0000000000001));
        try {
            new BigFraction(null, BigInteger.ONE);
            fail("Expecting NullArgumentException");
        } catch (NullArgumentException npe) {
            
        }
        try {
            new BigFraction(BigInteger.ONE, null);
            fail("Expecting NullArgumentException");
        } catch (NullArgumentException npe) {
            
        }
        try {
            new BigFraction(BigInteger.ONE, BigInteger.ZERO);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException npe) {
            
        }
        try {
            new BigFraction(2.0 * Integer.MAX_VALUE, 1.0e-5, 100000);
            fail("Expecting FractionConversionException");
        } catch (FractionConversionException fce) {
            
        }
    }

// org.apache.commons.math.fraction.BigFractionTest::testGoldenRatio
    public void testGoldenRatio() {
        try {
            
            
            new BigFraction((1 + FastMath.sqrt(5)) / 2, 1.0e-12, 25);
            fail("an exception should have been thrown");
        } catch (ConvergenceException ce) {
            
        }
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

        assertEquals(0, first.compareTo(first));
        assertEquals(0, first.compareTo(third));
        assertEquals(1, first.compareTo(second));
        assertEquals(-1, second.compareTo(first));

        
        
        
        BigFraction pi1 = new BigFraction(1068966896, 340262731);
        BigFraction pi2 = new BigFraction( 411557987, 131002976);
        assertEquals(-1, pi1.compareTo(pi2));
        assertEquals( 1, pi2.compareTo(pi1));
        assertEquals(0.0, pi1.doubleValue() - pi2.doubleValue(), 1.0e-20);

    }

// org.apache.commons.math.fraction.BigFractionTest::testDoubleValue
    public void testDoubleValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);

        assertEquals(0.5, first.doubleValue(), 0.0);
        assertEquals(1.0 / 3.0, second.doubleValue(), 0.0);
    }

// org.apache.commons.math.fraction.BigFractionTest::testFloatValue
    public void testFloatValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);

        assertEquals(0.5f, first.floatValue(), 0.0f);
        assertEquals((float) (1.0 / 3.0), second.floatValue(), 0.0f);
    }

// org.apache.commons.math.fraction.BigFractionTest::testIntValue
    public void testIntValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(3, 2);

        assertEquals(0, first.intValue());
        assertEquals(1, second.intValue());
    }

// org.apache.commons.math.fraction.BigFractionTest::testLongValue
    public void testLongValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(3, 2);

        assertEquals(0L, first.longValue());
        assertEquals(1L, second.longValue());
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
                fail("Expecting IllegalArgumentException");
            } catch (IllegalArgumentException iae) {
                
            }
        }
        assertEquals(1l, new BigFraction(Double.MAX_VALUE).getDenominatorAsLong());
        assertEquals(1l, new BigFraction(Double.longBitsToDouble(0x0010000000000000L)).getNumeratorAsLong());
        assertEquals(1l, new BigFraction(Double.MIN_VALUE).getNumeratorAsLong());
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
        assertEquals(3, f.getNumeratorAsInt());
        assertEquals(2, f.getDenominatorAsInt());

        f = new BigFraction(4, 3);
        f = f.reciprocal();
        assertEquals(3, f.getNumeratorAsInt());
        assertEquals(4, f.getDenominatorAsInt());

        f = new BigFraction(-15, 47);
        f = f.reciprocal();
        assertEquals(-47, f.getNumeratorAsInt());
        assertEquals(15, f.getDenominatorAsInt());

        f = new BigFraction(0, 3);
        try {
            f = f.reciprocal();
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }

        
        f = new BigFraction(Integer.MAX_VALUE, 1);
        f = f.reciprocal();
        assertEquals(1, f.getNumeratorAsInt());
        assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
    }

// org.apache.commons.math.fraction.BigFractionTest::testNegate
    public void testNegate() {
        BigFraction f = null;

        f = new BigFraction(50, 75);
        f = f.negate();
        assertEquals(-2, f.getNumeratorAsInt());
        assertEquals(3, f.getDenominatorAsInt());

        f = new BigFraction(-50, 75);
        f = f.negate();
        assertEquals(2, f.getNumeratorAsInt());
        assertEquals(3, f.getDenominatorAsInt());

        
        f = new BigFraction(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
        f = f.negate();
        assertEquals(Integer.MIN_VALUE + 2, f.getNumeratorAsInt());
        assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());

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
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(-1, 13 * 13 * 2 * 2);
        f2 = new BigFraction(-2, 13 * 17 * 2);
        f = f1.add(f2);
        assertEquals(13 * 13 * 17 * 2 * 2, f.getDenominatorAsInt());
        assertEquals(-17 - 2 * 13 * 2, f.getNumeratorAsInt());

        try {
            f.add((BigFraction) null);
            fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

        
        
        f1 = new BigFraction(1, 32768 * 3);
        f2 = new BigFraction(1, 59049);
        f = f1.add(f2);
        assertEquals(52451, f.getNumeratorAsInt());
        assertEquals(1934917632, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, 3);
        f2 = new BigFraction(1, 3);
        f = f1.add(f2);
        assertEquals(Integer.MIN_VALUE + 1, f.getNumeratorAsInt());
        assertEquals(3, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(BigInteger.ONE);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f = f.add(BigInteger.ZERO);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(1);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f = f.add(0);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(1l);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f = f.add(0l);
        assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

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
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }

        f1 = new BigFraction(0, 5);
        f2 = new BigFraction(2, 7);
        BigFraction f = f1.divide(f2);
        assertSame(BigFraction.ZERO, f);

        f1 = new BigFraction(2, 7);
        f2 = BigFraction.ONE;
        f = f1.divide(f2);
        assertEquals(2, f.getNumeratorAsInt());
        assertEquals(7, f.getDenominatorAsInt());

        f1 = new BigFraction(1, Integer.MAX_VALUE);
        f = f1.divide(f1);
        assertEquals(1, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f2 = new BigFraction(1, Integer.MAX_VALUE);
        f = f1.divide(f2);
        assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        try {
            f.divide((BigFraction) null);
            fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide(BigInteger.valueOf(Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        assertEquals(1, f.getNumeratorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide(Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        assertEquals(1, f.getNumeratorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide((long) Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        assertEquals(1, f.getNumeratorAsInt());

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
        assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f = f2.multiply(Integer.MAX_VALUE);
        assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        f = f2.multiply((long) Integer.MAX_VALUE);
        assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

        try {
            f.multiply((BigFraction) null);
            fail("expecting NullArgumentException");
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
            fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

        
        
        BigFraction f1 = new BigFraction(1, 32768 * 3);
        BigFraction f2 = new BigFraction(1, 59049);
        f = f1.subtract(f2);
        assertEquals(-13085, f.getNumeratorAsInt());
        assertEquals(1934917632, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, 3);
        f2 = new BigFraction(1, 3).negate();
        f = f1.subtract(f2);
        assertEquals(Integer.MIN_VALUE + 1, f.getNumeratorAsInt());
        assertEquals(3, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE, 1);
        f2 = BigFraction.ONE;
        f = f1.subtract(f2);
        assertEquals(Integer.MAX_VALUE - 1, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());

    }

// org.apache.commons.math.fraction.BigFractionTest::testBigDecimalValue
    public void testBigDecimalValue() {
        assertEquals(new BigDecimal(0.5), new BigFraction(1, 2).bigDecimalValue());
        assertEquals(new BigDecimal("0.0003"), new BigFraction(3, 10000).bigDecimalValue());
        assertEquals(new BigDecimal("0"), new BigFraction(1, 3).bigDecimalValue(BigDecimal.ROUND_DOWN));
        assertEquals(new BigDecimal("0.333"), new BigFraction(1, 3).bigDecimalValue(3, BigDecimal.ROUND_DOWN));
    }

// org.apache.commons.math.fraction.BigFractionTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BigFraction zero = new BigFraction(0, 1);
        BigFraction nullFraction = null;
        assertTrue(zero.equals(zero));
        assertFalse(zero.equals(nullFraction));
        assertFalse(zero.equals(Double.valueOf(0)));
        BigFraction zero2 = new BigFraction(0, 2);
        assertTrue(zero.equals(zero2));
        assertEquals(zero.hashCode(), zero2.hashCode());
        BigFraction one = new BigFraction(1, 1);
        assertFalse((one.equals(zero) || zero.equals(one)));
        assertTrue(one.equals(BigFraction.ONE));
    }

// org.apache.commons.math.fraction.BigFractionTest::testGetReducedFraction
    public void testGetReducedFraction() {
        BigFraction threeFourths = new BigFraction(3, 4);
        assertTrue(threeFourths.equals(BigFraction.getReducedFraction(6, 8)));
        assertTrue(BigFraction.ZERO.equals(BigFraction.getReducedFraction(0, -1)));
        try {
            BigFraction.getReducedFraction(1, 0);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        }
        assertEquals(BigFraction.getReducedFraction(2, Integer.MIN_VALUE).getNumeratorAsInt(), -1);
        assertEquals(BigFraction.getReducedFraction(1, -1).getNumeratorAsInt(), -1);
    }

// org.apache.commons.math.fraction.BigFractionTest::testPow
    public void testPow() {
        assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(13));
        assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(13l));
        assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(BigInteger.valueOf(13l)));
        assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(0));
        assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(0l));
        assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(BigInteger.valueOf(0l)));
        assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(-13));
        assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(-13l));
        assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(BigInteger.valueOf(-13l)));
    }

// org.apache.commons.math.fraction.BigFractionTest::testMath340
    public void testMath340() {
        BigFraction fractionA = new BigFraction(0.00131);
        BigFraction fractionB = new BigFraction(.37).reciprocal();
        BigFraction errorResult = fractionA.multiply(fractionB);
        BigFraction correctResult = new BigFraction(fractionA.getNumerator().multiply(fractionB.getNumerator()),
                                                    fractionA.getDenominator().multiply(fractionB.getDenominator()));
        assertEquals(correctResult, errorResult);
    }

// org.apache.commons.math.fraction.BigFractionTest::testSerial
    public void testSerial() throws FractionConversionException {
        BigFraction[] fractions = {
            new BigFraction(3, 4), BigFraction.ONE, BigFraction.ZERO,
            new BigFraction(17), new BigFraction(FastMath.PI, 1000),
            new BigFraction(-5, 2)
        };
        for (BigFraction fraction : fractions) {
            assertEquals(fraction, TestUtils.serializeAndRecover(fraction));
        }
    }

// org.apache.commons.math.fraction.FractionFieldTest::testZero
    public void testZero() {
        assertEquals(Fraction.ZERO, FractionField.getInstance().getZero());
    }

// org.apache.commons.math.fraction.FractionFieldTest::testOne
    public void testOne() {
        assertEquals(Fraction.ONE, FractionField.getInstance().getOne());
    }

// org.apache.commons.math.fraction.FractionFieldTest::testSerial
    public void testSerial() {
        
        FractionField field = FractionField.getInstance();
        assertTrue(field == TestUtils.serializeAndRecover(field));
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
        assertEquals("355 / 113", improperFormat.format(FastMath.PI));
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
        } catch (MathArithmeticException ex) {
            
        }
        try {
            new Fraction(1, Integer.MIN_VALUE);
            fail();
        } catch (MathArithmeticException ex) {
            
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
            
            new Fraction((1 + FastMath.sqrt(5)) / 2, 1.0e-12, 25);
            fail("an exception should have been thrown");
        } catch (ConvergenceException ce) {
            
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
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        
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
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}
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
        f = f1.add(1);
        assertEquals(Integer.MAX_VALUE, f.getNumerator());
        assertEquals(1, f.getDenominator());

        f1 = new Fraction(-1, 13*13*2*2);
        f2 = new Fraction(-2, 13*17*2);
        f = f1.add(f2);
        assertEquals(13*13*17*2*2, f.getDenominator());
        assertEquals(-17 - 2*13*2, f.getNumerator());

        try {
            f.add(null);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        
        
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
            fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}

        
        f1 = new Fraction(Integer.MIN_VALUE, 5);
        f2 = new Fraction(-1,5);
        try {
            f = f1.add(f2); 
            fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(-Integer.MAX_VALUE, 1);
            f = f.add(f);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(-Integer.MAX_VALUE, 1);
            f = f.add(f);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(3,327680);
        f2 = new Fraction(2,59049);
        try {
            f = f1.add(f2); 
            fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}
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
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

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
            fail("MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        try {
            f1 = new Fraction(1, Integer.MAX_VALUE);
            f = f1.divide(f1.reciprocal());  
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}
        try {
            f1 = new Fraction(1, -Integer.MAX_VALUE);
            f = f1.divide(f1.reciprocal());  
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(6, 35);
        f  = f1.divide(15);
        assertEquals(2, f.getNumerator());
        assertEquals(175, f.getDenominator());

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
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        f1 = new Fraction(6, 35);
        f  = f1.multiply(15);
        assertEquals(18, f.getNumerator());
        assertEquals(7, f.getDenominator());
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
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        
        
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
        f = f1.subtract(1);
        assertEquals(Integer.MAX_VALUE-1, f.getNumerator());
        assertEquals(1, f.getDenominator());

        try {
            f1 = new Fraction(1, Integer.MAX_VALUE);
            f2 = new Fraction(1, Integer.MAX_VALUE - 1);
            f = f1.subtract(f2);
            fail("expecting MathArithmeticException");  
        } catch (MathArithmeticException ex) {}

        
        f1 = new Fraction(Integer.MIN_VALUE, 5);
        f2 = new Fraction(1,5);
        try {
            f = f1.subtract(f2); 
            fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(Integer.MIN_VALUE, 1);
            f = f.subtract(Fraction.ONE);
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(Integer.MAX_VALUE, 1);
            f = f.subtract(Fraction.ONE.negate());
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(3,327680);
        f2 = new Fraction(2,59049);
        try {
            f = f1.subtract(f2); 
            fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}
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
            fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }
        assertEquals(Fraction.getReducedFraction
                (2, Integer.MIN_VALUE).getNumerator(),-1);
        assertEquals(Fraction.getReducedFraction
                (1, -1).getNumerator(), -1);
    }

// org.apache.commons.math.fraction.FractionTest::testToString
    public void testToString() {
        assertEquals("0", new Fraction(0, 3).toString());
        assertEquals("3", new Fraction(6, 2).toString());
        assertEquals("2 / 3", new Fraction(18, 27).toString());
    }

// org.apache.commons.math.fraction.FractionTest::testSerial
    public void testSerial() throws FractionConversionException {
        Fraction[] fractions = {
            new Fraction(3, 4), Fraction.ONE, Fraction.ZERO,
            new Fraction(17), new Fraction(FastMath.PI, 1000),
            new Fraction(-5, 2)
        };
        for (Fraction fraction : fractions) {
            assertEquals(fraction, TestUtils.serializeAndRecover(fraction));
        }
    }

// org.apache.commons.math.genetics.ElitisticListPopulationTest::testNextGeneration
    public void testNextGeneration() {
        ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.203);

        for (int i=0; i<pop.getPopulationLimit(); i++) {
            pop.addChromosome(new DummyChromosome());
        }

        Population nextGeneration = pop.nextGeneration();

        assertEquals(20, nextGeneration.getPopulationSize());
    }

// org.apache.commons.math.genetics.FitnessCachingTest::testFitnessCaching
    public void testFitnessCaching() {
        
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE, 
                new BinaryMutation(),
                MUTATION_RATE, 
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        
        Population initial = randomPopulation();
        
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        
        ga.evolve(initial, stopCond);

        int neededCalls =
            POPULATION_SIZE  +
            (NUM_GENERATIONS - 1)  * (int)(POPULATION_SIZE * (1.0 - ELITISM_RATE)) 
            ;
        assertTrue(fitnessCalls <= neededCalls); 
    }

// org.apache.commons.math.genetics.GeneticAlgorithmTestBinary::test
    public void test() {
        

        
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE, 
                new BinaryMutation(),
                MUTATION_RATE,
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        assertEquals(0, ga.getGenerationsEvolved());

        
        Population initial = randomPopulation();
        
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        
        Chromosome bestInitial = initial.getFittestChromosome();

        
        Population finalPopulation = ga.evolve(initial, stopCond);

        
        Chromosome bestFinal = finalPopulation.getFittestChromosome();

        
        

        assertTrue(bestFinal.compareTo(bestInitial) > 0);
        assertEquals(NUM_GENERATIONS, ga.getGenerationsEvolved());

    }

// org.apache.commons.math.genetics.GeneticAlgorithmTestPermutations::test
    public void test() {
        

        
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE,
                new RandomKeyMutation(),
                MUTATION_RATE,
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        
        Population initial = randomPopulation();
        
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        
        Chromosome bestInitial = initial.getFittestChromosome();

        
        Population finalPopulation = ga.evolve(initial, stopCond);

        
        Chromosome bestFinal = finalPopulation.getFittestChromosome();

        
        

        assertTrue(bestFinal.compareTo(bestInitial) > 0);

        
        
    }

// org.apache.commons.math.geometry.RotationTest::testIdentity
  public void testIdentity() {

    Rotation r = Rotation.IDENTITY;
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

    r = new Rotation(-1, 0, 0, 0, false);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

    r = new Rotation(42, 0, 0, 0, true);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

  }

// org.apache.commons.math.geometry.RotationTest::testAxisAngle
  public void testAxisAngle() {

    Rotation r = new Rotation(new Vector3D(10, 10, 10), 2 * FastMath.PI / 3);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_I);
    double s = 1 / FastMath.sqrt(3);
    checkVector(r.getAxis(), new Vector3D(s, s, s));
    checkAngle(r.getAngle(), 2 * FastMath.PI / 3);

    try {
      new Rotation(new Vector3D(0, 0, 0), 2 * FastMath.PI / 3);
      fail("an exception should have been thrown");
    } catch (ArithmeticException e) {
    }

    r = new Rotation(Vector3D.PLUS_K, 1.5 * FastMath.PI);
    checkVector(r.getAxis(), new Vector3D(0, 0, -1));
    checkAngle(r.getAngle(), 0.5 * FastMath.PI);

    r = new Rotation(Vector3D.PLUS_J, FastMath.PI);
    checkVector(r.getAxis(), Vector3D.PLUS_J);
    checkAngle(r.getAngle(), FastMath.PI);

    checkVector(Rotation.IDENTITY.getAxis(), Vector3D.PLUS_I);

  }

// org.apache.commons.math.geometry.RotationTest::testRevert
  public void testRevert() {
    Rotation r = new Rotation(0.001, 0.36, 0.48, 0.8, true);
    Rotation reverted = r.revert();
    checkRotation(r.applyTo(reverted), 1, 0, 0, 0);
    checkRotation(reverted.applyTo(r), 1, 0, 0, 0);
    assertEquals(r.getAngle(), reverted.getAngle(), 1.0e-12);
    assertEquals(-1, Vector3D.dotProduct(r.getAxis(), reverted.getAxis()), 1.0e-12);
  }

// org.apache.commons.math.geometry.RotationTest::testVectorOnePair
  public void testVectorOnePair() {

    Vector3D u = new Vector3D(3, 2, 1);
    Vector3D v = new Vector3D(-4, 2, 2);
    Rotation r = new Rotation(u, v);
    checkVector(r.applyTo(u.scalarMultiply(v.getNorm())), v.scalarMultiply(u.getNorm()));

    checkAngle(new Rotation(u, u.negate()).getAngle(), FastMath.PI);

    try {
        new Rotation(u, Vector3D.ZERO);
        fail("an exception should have been thrown");
    } catch (IllegalArgumentException e) {
        
    }

  }

// org.apache.commons.math.geometry.RotationTest::testVectorTwoPairs
  public void testVectorTwoPairs() {

    Vector3D u1 = new Vector3D(3, 0, 0);
    Vector3D u2 = new Vector3D(0, 5, 0);
    Vector3D v1 = new Vector3D(0, 0, 2);
    Vector3D v2 = new Vector3D(-2, 0, 2);
    Rotation r = new Rotation(u1, u2, v1, v2);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.MINUS_I);

    r = new Rotation(u1, u2, u1.negate(), u2.negate());
    Vector3D axis = r.getAxis();
    if (Vector3D.dotProduct(axis, Vector3D.PLUS_K) > 0) {
      checkVector(axis, Vector3D.PLUS_K);
    } else {
      checkVector(axis, Vector3D.MINUS_K);
    }
    checkAngle(r.getAngle(), FastMath.PI);

    double sqrt = FastMath.sqrt(2) / 2;
    r = new Rotation(Vector3D.PLUS_I,  Vector3D.PLUS_J,
                     new Vector3D(0.5, 0.5,  sqrt),
                     new Vector3D(0.5, 0.5, -sqrt));
    checkRotation(r, sqrt, 0.5, 0.5, 0);

    r = new Rotation(u1, u2, u1, Vector3D.crossProduct(u1, u2));
    checkRotation(r, sqrt, -sqrt, 0, 0);

    checkRotation(new Rotation(u1, u2, u1, u2), 1, 0, 0, 0);

    try {
        new Rotation(u1, u2, Vector3D.ZERO, v2);
        fail("an exception should have been thrown");
    } catch (IllegalArgumentException e) {
      
    }

  }

// org.apache.commons.math.geometry.RotationTest::testMatrix
  public void testMatrix()
    throws NotARotationMatrixException {

    try {
      new Rotation(new double[][] {
                     { 0.0, 1.0, 0.0 },
                     { 1.0, 0.0, 0.0 }
                   }, 1.0e-7);
      fail("Expecting NotARotationMatrixException");
    } catch (NotARotationMatrixException nrme) {
      
    }

    try {
      new Rotation(new double[][] {
                     {  0.445888,  0.797184, -0.407040 },
                     {  0.821760, -0.184320,  0.539200 },
                     { -0.354816,  0.574912,  0.737280 }
                   }, 1.0e-7);
      fail("Expecting NotARotationMatrixException");
    } catch (NotARotationMatrixException nrme) {
      
    }

    try {
        new Rotation(new double[][] {
                       {  0.4,  0.8, -0.4 },
                       { -0.4,  0.6,  0.7 },
                       {  0.8, -0.2,  0.5 }
                     }, 1.0e-15);
        fail("Expecting NotARotationMatrixException");
      } catch (NotARotationMatrixException nrme) {
        
      }

    checkRotation(new Rotation(new double[][] {
                                 {  0.445888,  0.797184, -0.407040 },
                                 { -0.354816,  0.574912,  0.737280 },
                                 {  0.821760, -0.184320,  0.539200 }
                               }, 1.0e-10),
                  0.8, 0.288, 0.384, 0.36);

    checkRotation(new Rotation(new double[][] {
                                 {  0.539200,  0.737280,  0.407040 },
                                 {  0.184320, -0.574912,  0.797184 },
                                 {  0.821760, -0.354816, -0.445888 }
                              }, 1.0e-10),
                  0.36, 0.8, 0.288, 0.384);

    checkRotation(new Rotation(new double[][] {
                                 { -0.445888,  0.797184, -0.407040 },
                                 {  0.354816,  0.574912,  0.737280 },
                                 {  0.821760,  0.184320, -0.539200 }
                               }, 1.0e-10),
                  0.384, 0.36, 0.8, 0.288);

    checkRotation(new Rotation(new double[][] {
                                 { -0.539200,  0.737280,  0.407040 },
                                 { -0.184320, -0.574912,  0.797184 },
                                 {  0.821760,  0.354816,  0.445888 }
                               }, 1.0e-10),
                  0.288, 0.384, 0.36, 0.8);

    double[][] m1 = { { 0.0, 1.0, 0.0 },
                      { 0.0, 0.0, 1.0 },
                      { 1.0, 0.0, 0.0 } };
    Rotation r = new Rotation(m1, 1.0e-7);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_J);

    double[][] m2 = { { 0.83203, -0.55012, -0.07139 },
                      { 0.48293,  0.78164, -0.39474 },
                      { 0.27296,  0.29396,  0.91602 } };
    r = new Rotation(m2, 1.0e-12);

    double[][] m3 = r.getMatrix();
    double d00 = m2[0][0] - m3[0][0];
    double d01 = m2[0][1] - m3[0][1];
    double d02 = m2[0][2] - m3[0][2];
    double d10 = m2[1][0] - m3[1][0];
    double d11 = m2[1][1] - m3[1][1];
    double d12 = m2[1][2] - m3[1][2];
    double d20 = m2[2][0] - m3[2][0];
    double d21 = m2[2][1] - m3[2][1];
    double d22 = m2[2][2] - m3[2][2];

    assertTrue(FastMath.abs(d00) < 6.0e-6);
    assertTrue(FastMath.abs(d01) < 6.0e-6);
    assertTrue(FastMath.abs(d02) < 6.0e-6);
    assertTrue(FastMath.abs(d10) < 6.0e-6);
    assertTrue(FastMath.abs(d11) < 6.0e-6);
    assertTrue(FastMath.abs(d12) < 6.0e-6);
    assertTrue(FastMath.abs(d20) < 6.0e-6);
    assertTrue(FastMath.abs(d21) < 6.0e-6);
    assertTrue(FastMath.abs(d22) < 6.0e-6);

    assertTrue(FastMath.abs(d00) > 4.0e-7);
    assertTrue(FastMath.abs(d01) > 4.0e-7);
    assertTrue(FastMath.abs(d02) > 4.0e-7);
    assertTrue(FastMath.abs(d10) > 4.0e-7);
    assertTrue(FastMath.abs(d11) > 4.0e-7);
    assertTrue(FastMath.abs(d12) > 4.0e-7);
    assertTrue(FastMath.abs(d20) > 4.0e-7);
    assertTrue(FastMath.abs(d21) > 4.0e-7);
    assertTrue(FastMath.abs(d22) > 4.0e-7);

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        double m3tm3 = m3[i][0] * m3[j][0]
                     + m3[i][1] * m3[j][1]
                     + m3[i][2] * m3[j][2];
        if (i == j) {
          assertTrue(FastMath.abs(m3tm3 - 1.0) < 1.0e-10);
        } else {
          assertTrue(FastMath.abs(m3tm3) < 1.0e-10);
        }
      }
    }

    checkVector(r.applyTo(Vector3D.PLUS_I),
                new Vector3D(m3[0][0], m3[1][0], m3[2][0]));
    checkVector(r.applyTo(Vector3D.PLUS_J),
                new Vector3D(m3[0][1], m3[1][1], m3[2][1]));
    checkVector(r.applyTo(Vector3D.PLUS_K),
                new Vector3D(m3[0][2], m3[1][2], m3[2][2]));

    double[][] m4 = { { 1.0,  0.0,  0.0 },
                      { 0.0, -1.0,  0.0 },
                      { 0.0,  0.0, -1.0 } };
    r = new Rotation(m4, 1.0e-7);
    checkAngle(r.getAngle(), FastMath.PI);

    try {
      double[][] m5 = { { 0.0, 0.0, 1.0 },
                        { 0.0, 1.0, 0.0 },
                        { 1.0, 0.0, 0.0 } };
      r = new Rotation(m5, 1.0e-7);
      fail("got " + r + ", should have caught an exception");
    } catch (NotARotationMatrixException e) {
      
    }

  }

// org.apache.commons.math.geometry.RotationTest::testAngles
  public void testAngles()
    throws CardanEulerSingularityException {

    RotationOrder[] CardanOrders = {
      RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
      RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
    };

    for (int i = 0; i < CardanOrders.length; ++i) {
      for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 0.3) {
        for (double alpha2 = -1.55; alpha2 < 1.55; alpha2 += 0.3) {
          for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 0.3) {
            Rotation r = new Rotation(CardanOrders[i], alpha1, alpha2, alpha3);
            double[] angles = r.getAngles(CardanOrders[i]);
            checkAngle(angles[0], alpha1);
            checkAngle(angles[1], alpha2);
            checkAngle(angles[2], alpha3);
          }
        }
      }
    }

    RotationOrder[] EulerOrders = {
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
          };

    for (int i = 0; i < EulerOrders.length; ++i) {
      for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 0.3) {
        for (double alpha2 = 0.05; alpha2 < 3.1; alpha2 += 0.3) {
          for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 0.3) {
            Rotation r = new Rotation(EulerOrders[i],
                                      alpha1, alpha2, alpha3);
            double[] angles = r.getAngles(EulerOrders[i]);
            checkAngle(angles[0], alpha1);
            checkAngle(angles[1], alpha2);
            checkAngle(angles[2], alpha3);
          }
        }
      }
    }

  }

// org.apache.commons.math.geometry.RotationTest::testSingularities
  public void testSingularities() {

    RotationOrder[] CardanOrders = {
      RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
      RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
    };

    double[] singularCardanAngle = { FastMath.PI / 2, -FastMath.PI / 2 };
    for (int i = 0; i < CardanOrders.length; ++i) {
      for (int j = 0; j < singularCardanAngle.length; ++j) {
        Rotation r = new Rotation(CardanOrders[i], 0.1, singularCardanAngle[j], 0.3);
        try {
          r.getAngles(CardanOrders[i]);
          fail("an exception should have been caught");
        } catch (CardanEulerSingularityException cese) {
          
        }
      }
    }

    RotationOrder[] EulerOrders = {
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
          };

    double[] singularEulerAngle = { 0, FastMath.PI };
    for (int i = 0; i < EulerOrders.length; ++i) {
      for (int j = 0; j < singularEulerAngle.length; ++j) {
        Rotation r = new Rotation(EulerOrders[i], 0.1, singularEulerAngle[j], 0.3);
        try {
          r.getAngles(EulerOrders[i]);
          fail("an exception should have been caught");
        } catch (CardanEulerSingularityException cese) {
          
        }
      }
    }

  }

// org.apache.commons.math.geometry.RotationTest::testQuaternion
  public void testQuaternion() {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    double n = 23.5;
    Rotation r2 = new Rotation(n * r1.getQ0(), n * r1.getQ1(),
                               n * r1.getQ2(), n * r1.getQ3(),
                               true);
    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyTo(u), r1.applyTo(u));
        }
      }
    }

    r1 = new Rotation( 0.288,  0.384,  0.36,  0.8, false);
    checkRotation(r1, -r1.getQ0(), -r1.getQ1(), -r1.getQ2(), -r1.getQ3());

  }

// org.apache.commons.math.geometry.RotationTest::testCompose
  public void testCompose() {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    Rotation r2 = new Rotation(new Vector3D(-1, 3, 2), 0.3);
    Rotation r3 = r2.applyTo(r1);

    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyTo(r1.applyTo(u)), r3.applyTo(u));
        }
      }
    }

  }

// org.apache.commons.math.geometry.RotationTest::testComposeInverse
  public void testComposeInverse() {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    Rotation r2 = new Rotation(new Vector3D(-1, 3, 2), 0.3);
    Rotation r3 = r2.applyInverseTo(r1);

    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyInverseTo(r1.applyTo(u)), r3.applyTo(u));
        }
      }
    }

  }

// org.apache.commons.math.geometry.RotationTest::testApplyInverseTo
  public void testApplyInverseTo() {

    Rotation r = new Rotation(new Vector3D(2, -3, 5), 1.7);
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(FastMath.cos(lambda) * FastMath.cos(phi),
                                    FastMath.sin(lambda) * FastMath.cos(phi),
                                    FastMath.sin(phi));
          r.applyInverseTo(r.applyTo(u));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

    r = Rotation.IDENTITY;
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(FastMath.cos(lambda) * FastMath.cos(phi),
                                    FastMath.sin(lambda) * FastMath.cos(phi),
                                    FastMath.sin(phi));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

    r = new Rotation(Vector3D.PLUS_K, FastMath.PI);
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(FastMath.cos(lambda) * FastMath.cos(phi),
                                    FastMath.sin(lambda) * FastMath.cos(phi),
                                    FastMath.sin(phi));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

  }

// org.apache.commons.math.geometry.Vector3DTest::testConstructors
    public void testConstructors() {
        double r = FastMath.sqrt(2) /2;
        checkVector(new Vector3D(2, new Vector3D(FastMath.PI / 3, -FastMath.PI / 4)),
                    r, r * FastMath.sqrt(3), -2 * r);
        checkVector(new Vector3D(2, Vector3D.PLUS_I,
                                 -3, Vector3D.MINUS_K),
                    2, 0, 3);
        checkVector(new Vector3D(2, Vector3D.PLUS_I,
                                 5, Vector3D.PLUS_J,
                                 -3, Vector3D.MINUS_K),
                    2, 5, 3);
        checkVector(new Vector3D(2, Vector3D.PLUS_I,
                                 5, Vector3D.PLUS_J,
                                 5, Vector3D.MINUS_J,
                                 -3, Vector3D.MINUS_K),
                    2, 0, 3);
    }

// org.apache.commons.math.geometry.Vector3DTest::testCoordinates
    public void testCoordinates() {
        Vector3D v = new Vector3D(1, 2, 3);
        Assert.assertTrue(FastMath.abs(v.getX() - 1) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(v.getY() - 2) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(v.getZ() - 3) < 1.0e-12);
    }

// org.apache.commons.math.geometry.Vector3DTest::testNorm1
    public void testNorm1() {
        Assert.assertEquals(0.0, Vector3D.ZERO.getNorm1(), 0);
        Assert.assertEquals(6.0, new Vector3D(1, -2, 3).getNorm1(), 0);
    }

// org.apache.commons.math.geometry.Vector3DTest::testNorm
    public void testNorm() {
        Assert.assertEquals(0.0, Vector3D.ZERO.getNorm(), 0);
        Assert.assertEquals(FastMath.sqrt(14), new Vector3D(1, 2, 3).getNorm(), 1.0e-12);
    }

// org.apache.commons.math.geometry.Vector3DTest::testNormInf
    public void testNormInf() {
        Assert.assertEquals(0.0, Vector3D.ZERO.getNormInf(), 0);
        Assert.assertEquals(3.0, new Vector3D(1, -2, 3).getNormInf(), 0);
    }

// org.apache.commons.math.geometry.Vector3DTest::testDistance1
    public void testDistance1() {
        Vector3D v1 = new Vector3D(1, -2, 3);
        Vector3D v2 = new Vector3D(-4, 2, 0);
        Assert.assertEquals(0.0, Vector3D.distance1(Vector3D.MINUS_I, Vector3D.MINUS_I), 0);
        Assert.assertEquals(12.0, Vector3D.distance1(v1, v2), 1.0e-12);
        Assert.assertEquals(v1.subtract(v2).getNorm1(), Vector3D.distance1(v1, v2), 1.0e-12);
    }

// org.apache.commons.math.geometry.Vector3DTest::testDistance
    public void testDistance() {
        Vector3D v1 = new Vector3D(1, -2, 3);
        Vector3D v2 = new Vector3D(-4, 2, 0);
        Assert.assertEquals(0.0, Vector3D.distance(Vector3D.MINUS_I, Vector3D.MINUS_I), 0);
        Assert.assertEquals(FastMath.sqrt(50), Vector3D.distance(v1, v2), 1.0e-12);
        Assert.assertEquals(v1.subtract(v2).getNorm(), Vector3D.distance(v1, v2), 1.0e-12);
    }

// org.apache.commons.math.geometry.Vector3DTest::testDistanceSq
    public void testDistanceSq() {
        Vector3D v1 = new Vector3D(1, -2, 3);
        Vector3D v2 = new Vector3D(-4, 2, 0);
        Assert.assertEquals(0.0, Vector3D.distanceSq(Vector3D.MINUS_I, Vector3D.MINUS_I), 0);
        Assert.assertEquals(50.0, Vector3D.distanceSq(v1, v2), 1.0e-12);
        Assert.assertEquals(Vector3D.distance(v1, v2) * Vector3D.distance(v1, v2),
                            Vector3D.distanceSq(v1, v2), 1.0e-12);
  }

// org.apache.commons.math.geometry.Vector3DTest::testDistanceInf
    public void testDistanceInf() {
        Vector3D v1 = new Vector3D(1, -2, 3);
        Vector3D v2 = new Vector3D(-4, 2, 0);
        Assert.assertEquals(0.0, Vector3D.distanceInf(Vector3D.MINUS_I, Vector3D.MINUS_I), 0);
        Assert.assertEquals(5.0, Vector3D.distanceInf(v1, v2), 1.0e-12);
        Assert.assertEquals(v1.subtract(v2).getNormInf(), Vector3D.distanceInf(v1, v2), 1.0e-12);
    }

// org.apache.commons.math.geometry.Vector3DTest::testSubtract
    public void testSubtract() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(-3, -2, -1);
        v1 = v1.subtract(v2);
        checkVector(v1, 4, 4, 4);

        checkVector(v2.subtract(v1), -7, -6, -5);
        checkVector(v2.subtract(3, v1), -15, -14, -13);
    }

// org.apache.commons.math.geometry.Vector3DTest::testAdd
    public void testAdd() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(-3, -2, -1);
        v1 = v1.add(v2);
        checkVector(v1, -2, 0, 2);

        checkVector(v2.add(v1), -5, -2, 1);
        checkVector(v2.add(3, v1), -9, -2, 5);
    }

// org.apache.commons.math.geometry.Vector3DTest::testScalarProduct
    public void testScalarProduct() {
        Vector3D v = new Vector3D(1, 2, 3);
        v = v.scalarMultiply(3);
        checkVector(v, 3, 6, 9);

        checkVector(v.scalarMultiply(0.5), 1.5, 3, 4.5);
    }

// org.apache.commons.math.geometry.Vector3DTest::testVectorialProducts
    public void testVectorialProducts() {
        Vector3D v1 = new Vector3D(2, 1, -4);
        Vector3D v2 = new Vector3D(3, 1, -1);

        Assert.assertTrue(FastMath.abs(Vector3D.dotProduct(v1, v2) - 11) < 1.0e-12);

        Vector3D v3 = Vector3D.crossProduct(v1, v2);
        checkVector(v3, 3, -10, -1);

        Assert.assertTrue(FastMath.abs(Vector3D.dotProduct(v1, v3)) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(Vector3D.dotProduct(v2, v3)) < 1.0e-12);
    }

// org.apache.commons.math.geometry.Vector3DTest::testAngular
    public void testAngular() {
        Assert.assertEquals(0,           Vector3D.PLUS_I.getAlpha(), 1.0e-10);
        Assert.assertEquals(0,           Vector3D.PLUS_I.getDelta(), 1.0e-10);
        Assert.assertEquals(FastMath.PI / 2, Vector3D.PLUS_J.getAlpha(), 1.0e-10);
        Assert.assertEquals(0,           Vector3D.PLUS_J.getDelta(), 1.0e-10);
        Assert.assertEquals(0,           Vector3D.PLUS_K.getAlpha(), 1.0e-10);
        Assert.assertEquals(FastMath.PI / 2, Vector3D.PLUS_K.getDelta(), 1.0e-10);
      
        Vector3D u = new Vector3D(-1, 1, -1);
        Assert.assertEquals(3 * FastMath.PI /4, u.getAlpha(), 1.0e-10);
        Assert.assertEquals(-1.0 / FastMath.sqrt(3), FastMath.sin(u.getDelta()), 1.0e-10);
    }

// org.apache.commons.math.geometry.Vector3DTest::testAngularSeparation
    public void testAngularSeparation() {
        Vector3D v1 = new Vector3D(2, -1, 4);

        Vector3D  k = v1.normalize();
        Vector3D  i = k.orthogonal();
        Vector3D v2 = k.scalarMultiply(FastMath.cos(1.2)).add(i.scalarMultiply(FastMath.sin(1.2)));

        Assert.assertTrue(FastMath.abs(Vector3D.angle(v1, v2) - 1.2) < 1.0e-12);
  }

// org.apache.commons.math.geometry.Vector3DTest::testNormalize
    public void testNormalize() {
        Assert.assertEquals(1.0, new Vector3D(5, -4, 2).normalize().getNorm(), 1.0e-12);
        try {
            Vector3D.ZERO.normalize();
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException ae) {
            
        }
    }

// org.apache.commons.math.geometry.Vector3DTest::testOrthogonal
    public void testOrthogonal() {
        Vector3D v1 = new Vector3D(0.1, 2.5, 1.3);
        Assert.assertEquals(0.0, Vector3D.dotProduct(v1, v1.orthogonal()), 1.0e-12);
        Vector3D v2 = new Vector3D(2.3, -0.003, 7.6);
        Assert.assertEquals(0.0, Vector3D.dotProduct(v2, v2.orthogonal()), 1.0e-12);
        Vector3D v3 = new Vector3D(-1.7, 1.4, 0.2);
        Assert.assertEquals(0.0, Vector3D.dotProduct(v3, v3.orthogonal()), 1.0e-12);
        try {
            new Vector3D(0, 0, 0).orthogonal();
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException ae) {
            
        }
    }

// org.apache.commons.math.geometry.Vector3DTest::testAngle
    public void testAngle() {
        Assert.assertEquals(0.22572612855273393616,
                            Vector3D.angle(new Vector3D(1, 2, 3), new Vector3D(4, 5, 6)),
                            1.0e-12);
        Assert.assertEquals(7.98595620686106654517199e-8,
                            Vector3D.angle(new Vector3D(1, 2, 3), new Vector3D(2, 4, 6.000001)),
                            1.0e-12);
        Assert.assertEquals(3.14159257373023116985197793156,
                            Vector3D.angle(new Vector3D(1, 2, 3), new Vector3D(-2, -4, -6.000001)),
                            1.0e-12);
        try {
            Vector3D.angle(Vector3D.ZERO, Vector3D.PLUS_I);
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException ae) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testDimensions
    public void testDimensions() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Array2DRowRealMatrix m1 = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(m1.getData());
        assertEquals(m2,m1);
        Array2DRowRealMatrix m3 = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m4 = new Array2DRowRealMatrix(m3.getData(), false);
        assertEquals(m4,m3);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testAdd
    public void testAdd() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
                    testDataPlusInv[row][col],sumEntries[row][col],
                        entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testAddFail
    public void testAddFail() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        try {
            m.add(m2);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testNorm
    public void testNorm() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        assertEquals("testData Frobenius norm", FastMath.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        assertEquals("testData2 Frobenius norm", FastMath.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testDataInv);
        TestUtils.assertEquals("m-n = m + -n",m.subtract(m2),
            m2.scalarMultiply(-1d).add(m),entryTolerance);
        try {
            m.subtract(new Array2DRowRealMatrix(testData2));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMultiply
     public void testMultiply() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        Array2DRowRealMatrix identity = new Array2DRowRealMatrix(id);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        TestUtils.assertEquals("inverse multiply",m.multiply(mInv),
            identity,entryTolerance);
        TestUtils.assertEquals("inverse multiply",mInv.multiply(m),
            identity,entryTolerance);
        TestUtils.assertEquals("identity multiply",m.multiply(identity),
            m,entryTolerance);
        TestUtils.assertEquals("identity multiply",identity.multiply(mInv),
            mInv,entryTolerance);
        TestUtils.assertEquals("identity multiply",m2.multiply(identity),
            m2,entryTolerance);
        try {
            m.multiply(new Array2DRowRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMultiply2
    public void testMultiply2() {
       RealMatrix m3 = new Array2DRowRealMatrix(d3);
       RealMatrix m4 = new Array2DRowRealMatrix(d4);
       RealMatrix m5 = new Array2DRowRealMatrix(d5);
       TestUtils.assertEquals("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = new Array2DRowRealMatrix(id);
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new Array2DRowRealMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("scalar add",new Array2DRowRealMatrix(testDataPlus2),
            m.scalarAdd(2d),entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testOperate
    public void testOperate() {
        RealMatrix m = new Array2DRowRealMatrix(id);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(testVector), entryTolerance);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(new ArrayRealVector(testVector)).getData(), entryTolerance);
        m = new Array2DRowRealMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        }, false);
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0], 1.0e-12);
        assertEquals( 7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals("inverse-transpose", mIT, mTI, normTolerance);
        m = new Array2DRowRealMatrix(testData2);
        RealMatrix mt = new Array2DRowRealMatrix(testData2T);
        TestUtils.assertEquals("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("premultiply", m.preMultiply(testVector),
                    preMultTest, normTolerance);
        TestUtils.assertEquals("premultiply", m.preMultiply(new ArrayRealVector(testVector).getData()),
                    preMultTest, normTolerance);
        m = new Array2DRowRealMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new Array2DRowRealMatrix(d3);
        RealMatrix m4 = new Array2DRowRealMatrix(d4);
        RealMatrix m5 = new Array2DRowRealMatrix(d5);
        TestUtils.assertEquals("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        Array2DRowRealMatrix identity = new Array2DRowRealMatrix(id);
        TestUtils.assertEquals("inverse multiply",m.preMultiply(mInv),
                identity,entryTolerance);
        TestUtils.assertEquals("inverse multiply",mInv.preMultiply(m),
                identity,entryTolerance);
        TestUtils.assertEquals("identity multiply",m.preMultiply(identity),
                m,entryTolerance);
        TestUtils.assertEquals("identity multiply",identity.preMultiply(mInv),
                mInv,entryTolerance);
        try {
            m.preMultiply(new Array2DRowRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("get row",m.getRow(0),testDataRow1,entryTolerance);
        TestUtils.assertEquals("get col",m.getColumn(2),testDataCol3,entryTolerance);
        try {
            m.getRow(10);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            fail ("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new Array2DRowRealMatrix(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new Array2DRowRealMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new Array2DRowRealMatrix(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);

    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkGetSubMatrix(m, subRows23Cols00,  2 , 3 , 0, 0, false);
        checkGetSubMatrix(m, subRows00Cols33,  0 , 0 , 3, 3, false);
        checkGetSubMatrix(m, subRows01Cols23,  0 , 1 , 2, 3, false);
        checkGetSubMatrix(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 },    false);
        checkGetSubMatrix(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 },    false);
        checkGetSubMatrix(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 }, false);
        checkGetSubMatrix(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 }, false);
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkGetSubMatrix(m, null,  1, 0, 2, 4, true);
        checkGetSubMatrix(m, null, -1, 1, 2, 2, true);
        checkGetSubMatrix(m, null,  1, 0, 2, 2, true);
        checkGetSubMatrix(m, null,  1, 0, 2, 4, true);
        checkGetSubMatrix(m, null, new int[] {},    new int[] { 0 }, true);
        checkGetSubMatrix(m, null, new int[] { 0 }, new int[] { 4 }, true);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkCopy(m, subRows23Cols00,  2 , 3 , 0, 0, false);
        checkCopy(m, subRows00Cols33,  0 , 0 , 3, 3, false);
        checkCopy(m, subRows01Cols23,  0 , 1 , 2, 3, false);
        checkCopy(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 },    false);
        checkCopy(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 },    false);
        checkCopy(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 }, false);
        checkCopy(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 }, false);
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);

        checkCopy(m, null,  1, 0, 2, 4, true);
        checkCopy(m, null, -1, 1, 2, 2, true);
        checkCopy(m, null,  1, 0, 2, 2, true);
        checkCopy(m, null,  1, 0, 2, 4, true);
        checkCopy(m, null, new int[] {},    new int[] { 0 }, true);
        checkCopy(m, null, new int[] { 0 }, new int[] { 4 }, true);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mRow0 = new Array2DRowRealMatrix(subRow0);
        RealMatrix mRow3 = new Array2DRowRealMatrix(subRow3);
        assertEquals("Row0", mRow0,
                m.getRowMatrix(0));
        assertEquals("Row3", mRow3,
                m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mRow3 = new Array2DRowRealMatrix(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mColumn1 = new Array2DRowRealMatrix(subColumn1);
        RealMatrix mColumn3 = new Array2DRowRealMatrix(subColumn3);
        assertEquals("Column1", mColumn1,
                m.getColumnMatrix(1));
        assertEquals("Column3", mColumn3,
                m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mColumn3 = new Array2DRowRealMatrix(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertEquals("Row0", mRow0, m.getRowVector(0));
        assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnVector(1));
        assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        double[] mColumn1 = columnToArray(subColumn1);
        double[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m1 = (Array2DRowRealMatrix) m.copy();
        Array2DRowRealMatrix mt = (Array2DRowRealMatrix) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new Array2DRowRealMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testToString
    public void testToString() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        assertEquals("Array2DRowRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
        m = new Array2DRowRealMatrix();
        assertEquals("Array2DRowRealMatrix{}",
                m.toString());
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        assertEquals(expected, m);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix();
        try {
            m2.setSubMatrix(testData,0,1);
            fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testWalk
    public void testWalk() throws MathUserException {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new Array2DRowRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSerial
    public void testSerial()  {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testConstructors
    public void testConstructors() {

        ArrayFieldVector<Fraction> v0 = new ArrayFieldVector<Fraction>(FractionField.getInstance());
        assertEquals(0, v0.getDimension());

        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(FractionField.getInstance(), 7);
        assertEquals(7, v1.getDimension());
        assertEquals(new Fraction(0), v1.getEntry(6));

        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(5, new Fraction(123, 100));
        assertEquals(5, v2.getDimension());
        assertEquals(new Fraction(123, 100), v2.getEntry(4));

        ArrayFieldVector<Fraction> v3 = new ArrayFieldVector<Fraction>(vec1);
        assertEquals(3, v3.getDimension());
        assertEquals(new Fraction(2), v3.getEntry(1));

        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(vec4, 3, 2);
        assertEquals(2, v4.getDimension());
        assertEquals(new Fraction(4), v4.getEntry(0));
        try {
            new ArrayFieldVector<Fraction>(vec4, 8, 3);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        FieldVector<Fraction> v5_i = new ArrayFieldVector<Fraction>(dvec1);
        assertEquals(9, v5_i.getDimension());
        assertEquals(new Fraction(9), v5_i.getEntry(8));

        ArrayFieldVector<Fraction> v5 = new ArrayFieldVector<Fraction>(dvec1);
        assertEquals(9, v5.getDimension());
        assertEquals(new Fraction(9), v5.getEntry(8));

        ArrayFieldVector<Fraction> v6 = new ArrayFieldVector<Fraction>(dvec1, 3, 2);
        assertEquals(2, v6.getDimension());
        assertEquals(new Fraction(4), v6.getEntry(0));
        try {
            new ArrayFieldVector<Fraction>(dvec1, 8, 3);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        ArrayFieldVector<Fraction> v7 = new ArrayFieldVector<Fraction>(v1);
        assertEquals(7, v7.getDimension());
        assertEquals(new Fraction(0), v7.getEntry(6));

        FieldVectorTestImpl<Fraction> v7_i = new FieldVectorTestImpl<Fraction>(vec1);

        ArrayFieldVector<Fraction> v7_2 = new ArrayFieldVector<Fraction>(v7_i);
        assertEquals(3, v7_2.getDimension());
        assertEquals(new Fraction(2), v7_2.getEntry(1));

        ArrayFieldVector<Fraction> v8 = new ArrayFieldVector<Fraction>(v1, true);
        assertEquals(7, v8.getDimension());
        assertEquals(new Fraction(0), v8.getEntry(6));
        assertNotSame("testData not same object ", v1.data, v8.data);

        ArrayFieldVector<Fraction> v8_2 = new ArrayFieldVector<Fraction>(v1, false);
        assertEquals(7, v8_2.getDimension());
        assertEquals(new Fraction(0), v8_2.getEntry(6));
        assertEquals(v1.data, v8_2.data);

        ArrayFieldVector<Fraction> v9 = new ArrayFieldVector<Fraction>(v1, v3);
        assertEquals(10, v9.getDimension());
        assertEquals(new Fraction(1), v9.getEntry(7));

    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testDataInOut
    public void testDataInOut() {

        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(vec2);
        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(vec4);
        FieldVectorTestImpl<Fraction> v2_t = new FieldVectorTestImpl<Fraction>(vec2);

        FieldVector<Fraction> v_append_1 = v1.append(v2);
        assertEquals(6, v_append_1.getDimension());
        assertEquals(new Fraction(4), v_append_1.getEntry(3));

        FieldVector<Fraction> v_append_2 = v1.append(new Fraction(2));
        assertEquals(4, v_append_2.getDimension());
        assertEquals(new Fraction(2), v_append_2.getEntry(3));

        FieldVector<Fraction> v_append_3 = v1.append(vec2);
        assertEquals(6, v_append_3.getDimension());
        assertEquals(new Fraction(4), v_append_3.getEntry(3));

        FieldVector<Fraction> v_append_4 = v1.append(v2_t);
        assertEquals(6, v_append_4.getDimension());
        assertEquals(new Fraction(4), v_append_4.getEntry(3));

        FieldVector<Fraction> v_copy = v1.copy();
        assertEquals(3, v_copy.getDimension());
        assertNotSame("testData not same object ", v1.data, v_copy.getData());

        Fraction[] a_frac = v1.toArray();
        assertEquals(3, a_frac.length);
        assertNotSame("testData not same object ", v1.data, a_frac);

        FieldVector<Fraction> vout5 = v4.getSubVector(3, 3);
        assertEquals(3, vout5.getDimension());
        assertEquals(new Fraction(5), vout5.getEntry(1));
        try {
            v4.getSubVector(3, 7);
            fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set1 = (ArrayFieldVector<Fraction>) v1.copy();
        v_set1.setEntry(1, new Fraction(11));
        assertEquals(new Fraction(11), v_set1.getEntry(1));
        try {
            v_set1.setEntry(3, new Fraction(11));
            fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set2 = (ArrayFieldVector<Fraction>) v4.copy();
        v_set2.set(3, v1);
        assertEquals(new Fraction(1), v_set2.getEntry(3));
        assertEquals(new Fraction(7), v_set2.getEntry(6));
        try {
            v_set2.set(7, v1);
            fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set3 = (ArrayFieldVector<Fraction>) v1.copy();
        v_set3.set(new Fraction(13));
        assertEquals(new Fraction(13), v_set3.getEntry(2));

        try {
            v_set3.getEntry(23);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set4 = (ArrayFieldVector<Fraction>) v4.copy();
        v_set4.setSubVector(3, v2_t);
        assertEquals(new Fraction(4), v_set4.getEntry(3));
        assertEquals(new Fraction(7), v_set4.getEntry(6));
        try {
            v_set4.setSubVector(7, v2_t);
            fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> vout10 = (ArrayFieldVector<Fraction>) v1.copy();
        ArrayFieldVector<Fraction> vout10_2 = (ArrayFieldVector<Fraction>) v1.copy();
        assertEquals(vout10, vout10_2);
        vout10_2.setEntry(0, new Fraction(11, 10));
        assertNotSame(vout10, vout10_2);

    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testMapFunctions
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

// org.apache.commons.math.linear.ArrayFieldVectorTest::testBasicFunctions
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
        assertEquals("compare val ",new Fraction(32), dot);

        
        Fraction dot_2 =  v1.dotProduct(v2_t);
        assertEquals("compare val ",new Fraction(32), dot_2);

        FieldMatrix<Fraction> m_outerProduct = v1.outerProduct(v2);
        assertEquals("compare val ",new Fraction(4), m_outerProduct.getEntry(0,0));

        FieldMatrix<Fraction> m_outerProduct_2 = v1.outerProduct(v2_t);
        assertEquals("compare val ",new Fraction(4), m_outerProduct_2.getEntry(0,0));

        ArrayFieldVector<Fraction> v_projection = v1.projection(v2);
        Fraction[] result_projection = {new Fraction(128, 77), new Fraction(160, 77), new Fraction(192, 77)};
        checkArray("compare vect", v_projection.getData(), result_projection);

        FieldVector<Fraction> v_projection_2 = v1.projection(v2_t);
        Fraction[] result_projection_2 = {new Fraction(128, 77), new Fraction(160, 77), new Fraction(192, 77)};
        checkArray("compare vect", v_projection_2.getData(), result_projection_2);

    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testMisc
    public void testMisc() {
        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(vec4);
        FieldVector<Fraction> v4_2 = new ArrayFieldVector<Fraction>(vec4);

        String out1 = v1.toString();
        assertTrue("some output ",  out1.length()!=0);
        
        try {
            v1.checkVectorDimensions(2);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

       try {
            v1.checkVectorDimensions(v4);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            v1.checkVectorDimensions(v4_2);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testSerial
    public void testSerial()  {
        ArrayFieldVector<Fraction> v = new ArrayFieldVector<Fraction>(vec1);
        assertEquals(v,TestUtils.serializeAndRecover(v));
    }

// org.apache.commons.math.linear.ArrayFieldVectorTest::testZeroVectors
    public void testZeroVectors() {

        
        try {
            new ArrayFieldVector<Fraction>(new Fraction[0]);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            new ArrayFieldVector<Fraction>(new Fraction[0], true);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            new ArrayFieldVector<Fraction>(new Fraction[0], false);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        assertEquals(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), new Fraction[0]).getDimension());
        assertEquals(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), new Fraction[0], true).getDimension());
        assertEquals(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), new Fraction[0], false).getDimension());

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testConstructors
    public void testConstructors() {

        ArrayRealVector v0 = new ArrayRealVector();
        Assert.assertEquals("testData len", 0, v0.getDimension());

        ArrayRealVector v1 = new ArrayRealVector(7);
        Assert.assertEquals("testData len", 7, v1.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v1.getEntry(6), 0);

        ArrayRealVector v2 = new ArrayRealVector(5, 1.23);
        Assert.assertEquals("testData len", 5, v2.getDimension());
        Assert.assertEquals("testData is 1.23 ", 1.23, v2.getEntry(4), 0);

        ArrayRealVector v3 = new ArrayRealVector(vec1);
        Assert.assertEquals("testData len", 3, v3.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3.getEntry(1), 0);

        ArrayRealVector v3_bis = new ArrayRealVector(vec1, true);
        Assert.assertEquals("testData len", 3, v3_bis.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3_bis.getEntry(1), 0);
        Assert.assertNotSame(v3_bis.getDataRef(), vec1);
        Assert.assertNotSame(v3_bis.getData(), vec1);

        ArrayRealVector v3_ter = new ArrayRealVector(vec1, false);
        Assert.assertEquals("testData len", 3, v3_ter.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3_ter.getEntry(1), 0);
        Assert.assertSame(v3_ter.getDataRef(), vec1);
        Assert.assertNotSame(v3_ter.getData(), vec1);

        ArrayRealVector v4 = new ArrayRealVector(vec4, 3, 2);
        Assert.assertEquals("testData len", 2, v4.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v4.getEntry(0), 0);
        try {
            new ArrayRealVector(vec4, 8, 3);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        RealVector v5_i = new ArrayRealVector(dvec1);
        Assert.assertEquals("testData len", 9, v5_i.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v5_i.getEntry(8), 0);

        ArrayRealVector v5 = new ArrayRealVector(dvec1);
        Assert.assertEquals("testData len", 9, v5.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v5.getEntry(8), 0);

        ArrayRealVector v6 = new ArrayRealVector(dvec1, 3, 2);
        Assert.assertEquals("testData len", 2, v6.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v6.getEntry(0), 0);
        try {
            new ArrayRealVector(dvec1, 8, 3);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        ArrayRealVector v7 = new ArrayRealVector(v1);
        Assert.assertEquals("testData len", 7, v7.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v7.getEntry(6), 0);

        RealVectorTestImpl v7_i = new RealVectorTestImpl(vec1);

        ArrayRealVector v7_2 = new ArrayRealVector(v7_i);
        Assert.assertEquals("testData len", 3, v7_2.getDimension());
        Assert.assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1), 0);

        ArrayRealVector v8 = new ArrayRealVector(v1, true);
        Assert.assertEquals("testData len", 7, v8.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6), 0);
        Assert.assertNotSame("testData not same object ", v1.data, v8.data);

        ArrayRealVector v8_2 = new ArrayRealVector(v1, false);
        Assert.assertEquals("testData len", 7, v8_2.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v8_2.getEntry(6), 0);
        Assert.assertEquals("testData same object ", v1.data, v8_2.data);

        ArrayRealVector v9 = new ArrayRealVector(v1, v3);
        Assert.assertEquals("testData len", 10, v9.getDimension());
        Assert.assertEquals("testData is 1.0 ", 1.0, v9.getEntry(7), 0);

        ArrayRealVector v10 = new ArrayRealVector(v2, new RealVectorTestImpl(vec3));
        Assert.assertEquals("testData len", 8, v10.getDimension());
        Assert.assertEquals("testData is 1.23 ", 1.23, v10.getEntry(4), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v10.getEntry(5), 0);

        ArrayRealVector v11 = new ArrayRealVector(new RealVectorTestImpl(vec3), v2);
        Assert.assertEquals("testData len", 8, v11.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v11.getEntry(2), 0);
        Assert.assertEquals("testData is 1.23 ", 1.23, v11.getEntry(3), 0);

        ArrayRealVector v12 = new ArrayRealVector(v2, vec3);
        Assert.assertEquals("testData len", 8, v12.getDimension());
        Assert.assertEquals("testData is 1.23 ", 1.23, v12.getEntry(4), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v12.getEntry(5), 0);

        ArrayRealVector v13 = new ArrayRealVector(vec3, v2);
        Assert.assertEquals("testData len", 8, v13.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v13.getEntry(2), 0);
        Assert.assertEquals("testData is 1.23 ", 1.23, v13.getEntry(3), 0);

        ArrayRealVector v14 = new ArrayRealVector(vec3, vec4);
        Assert.assertEquals("testData len", 12, v14.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v14.getEntry(2), 0);
        Assert.assertEquals("testData is 1.0 ", 1.0, v14.getEntry(3), 0);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testDataInOut
    public void testDataInOut() {

        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v2 = new ArrayRealVector(vec2);
        ArrayRealVector v4 = new ArrayRealVector(vec4);
        RealVectorTestImpl v2_t = new RealVectorTestImpl(vec2);

        RealVector v_append_1 = v1.append(v2);
        Assert.assertEquals("testData len", 6, v_append_1.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v_append_1.getEntry(3), 0);

        RealVector v_append_2 = v1.append(2.0);
        Assert.assertEquals("testData len", 4, v_append_2.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v_append_2.getEntry(3), 0);

        RealVector v_append_3 = v1.append(vec2);
        Assert.assertEquals("testData len", 6, v_append_3.getDimension());
        Assert.assertEquals("testData is  ", 4.0, v_append_3.getEntry(3), 0);

        RealVector v_append_4 = v1.append(v2_t);
        Assert.assertEquals("testData len", 6, v_append_4.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v_append_4.getEntry(3), 0);

        RealVector v_append_5 = v1.append((RealVector) v2);
        Assert.assertEquals("testData len", 6, v_append_5.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v_append_5.getEntry(3), 0);

        RealVector v_copy = v1.copy();
        Assert.assertEquals("testData len", 3, v_copy.getDimension());
        Assert.assertNotSame("testData not same object ", v1.data, v_copy.getData());

        double[] a_double = v1.toArray();
        Assert.assertEquals("testData len", 3, a_double.length);
        Assert.assertNotSame("testData not same object ", v1.data, a_double);

        RealVector vout5 = v4.getSubVector(3, 3);
        Assert.assertEquals("testData len", 3, vout5.getDimension());
        Assert.assertEquals("testData is 4.0 ", 5.0, vout5.getEntry(1), 0);
        try {
            v4.getSubVector(3, 7);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayRealVector v_set1 = v1.copy();
        v_set1.setEntry(1, 11.0);
        Assert.assertEquals("testData is 11.0 ", 11.0, v_set1.getEntry(1), 0);
        try {
            v_set1.setEntry(3, 11.0);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayRealVector v_set2 = v4.copy();
        v_set2.set(3, v1);
        Assert.assertEquals("testData is 1.0 ", 1.0, v_set2.getEntry(3), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v_set2.getEntry(6), 0);
        try {
            v_set2.set(7, v1);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayRealVector v_set3 = v1.copy();
        v_set3.set(13.0);
        Assert.assertEquals("testData is 13.0 ", 13.0, v_set3.getEntry(2), 0);

        try {
            v_set3.getEntry(23);
            Assert.fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }

        ArrayRealVector v_set4 = v4.copy();
        v_set4.setSubVector(3, v2_t);
        Assert.assertEquals("testData is 1.0 ", 4.0, v_set4.getEntry(3), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v_set4.getEntry(6), 0);
        try {
            v_set4.setSubVector(7, v2_t);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayRealVector vout10 = v1.copy();
        ArrayRealVector vout10_2 = v1.copy();
        Assert.assertEquals(vout10, vout10_2);
        vout10_2.setEntry(0, 1.1);
        Assert.assertNotSame(vout10, vout10_2);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testMapFunctions
    public void testMapFunctions() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);

        
        RealVector v_mapAdd = v1.mapAdd(2.0d);
        double[] result_mapAdd = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAdd,v_mapAdd.getData(),normTolerance);

        
        RealVector v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(2.0d);
        double[] result_mapAddToSelf = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData(),normTolerance);

        
        RealVector v_mapSubtract = v1.mapSubtract(2.0d);
        double[] result_mapSubtract = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtract,v_mapSubtract.getData(),normTolerance);

        
        RealVector v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(2.0d);
        double[] result_mapSubtractToSelf = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData(),normTolerance);

        
        RealVector v_mapMultiply = v1.mapMultiply(2.0d);
        double[] result_mapMultiply = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiply,v_mapMultiply.getData(),normTolerance);

        
        RealVector v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(2.0d);
        double[] result_mapMultiplyToSelf = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData(),normTolerance);

        
        RealVector v_mapDivide = v1.mapDivide(2.0d);
        double[] result_mapDivide = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivide,v_mapDivide.getData(),normTolerance);

        
        RealVector v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(2.0d);
        double[] result_mapDivideToSelf = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData(),normTolerance);

        
        RealVector v_mapPow = v1.map(new Power(2));
        double[] result_mapPow = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPow,v_mapPow.getData(),normTolerance);

        
        RealVector v_mapPowToSelf = v1.copy();
        v_mapPowToSelf.mapToSelf(new Power(2));
        double[] result_mapPowToSelf = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPowToSelf,v_mapPowToSelf.getData(),normTolerance);

        
        RealVector v_mapExp = v1.map(new Exp());
        double[] result_mapExp = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExp,v_mapExp.getData(),normTolerance);

        
        RealVector v_mapExpToSelf = v1.copy();
        v_mapExpToSelf.mapToSelf(new Exp());
        double[] result_mapExpToSelf = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExpToSelf,v_mapExpToSelf.getData(),normTolerance);

        
        RealVector v_mapExpm1 = v1.map(new Expm1());
        double[] result_mapExpm1 = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1,v_mapExpm1.getData(),normTolerance);

        
        RealVector v_mapExpm1ToSelf = v1.copy();
        v_mapExpm1ToSelf.mapToSelf(new Expm1());
        double[] result_mapExpm1ToSelf = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1ToSelf,v_mapExpm1ToSelf.getData(),normTolerance);

        
        RealVector v_mapLog = v1.map(new Log());
        double[] result_mapLog = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLog,v_mapLog.getData(),normTolerance);

        
        RealVector v_mapLogToSelf = v1.copy();
        v_mapLogToSelf.mapToSelf(new Log());
        double[] result_mapLogToSelf = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLogToSelf,v_mapLogToSelf.getData(),normTolerance);

        
        RealVector v_mapLog10 = v1.map(new Log10());
        double[] result_mapLog10 = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10,v_mapLog10.getData(),normTolerance);

        
        RealVector v_mapLog10ToSelf = v1.copy();
        v_mapLog10ToSelf.mapToSelf(new Log10());
        double[] result_mapLog10ToSelf = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10ToSelf,v_mapLog10ToSelf.getData(),normTolerance);

        
        RealVector v_mapLog1p = v1.map(new Log1p());
        double[] result_mapLog1p = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1p,v_mapLog1p.getData(),normTolerance);

        
        RealVector v_mapLog1pToSelf = v1.copy();
        v_mapLog1pToSelf.mapToSelf(new Log1p());
        double[] result_mapLog1pToSelf = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1pToSelf,v_mapLog1pToSelf.getData(),normTolerance);

        
        RealVector v_mapCosh = v1.map(new Cosh());
        double[] result_mapCosh = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCosh,v_mapCosh.getData(),normTolerance);

        
        RealVector v_mapCoshToSelf = v1.copy();
        v_mapCoshToSelf.mapToSelf(new Cosh());
        double[] result_mapCoshToSelf = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCoshToSelf,v_mapCoshToSelf.getData(),normTolerance);

        
        RealVector v_mapSinh = v1.map(new Sinh());
        double[] result_mapSinh = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinh,v_mapSinh.getData(),normTolerance);

        
        RealVector v_mapSinhToSelf = v1.copy();
        v_mapSinhToSelf.mapToSelf(new Sinh());
        double[] result_mapSinhToSelf = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinhToSelf,v_mapSinhToSelf.getData(),normTolerance);

        
        RealVector v_mapTanh = v1.map(new Tanh());
        double[] result_mapTanh = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanh,v_mapTanh.getData(),normTolerance);

        
        RealVector v_mapTanhToSelf = v1.copy();
        v_mapTanhToSelf.mapToSelf(new Tanh());
        double[] result_mapTanhToSelf = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanhToSelf,v_mapTanhToSelf.getData(),normTolerance);

        
        RealVector v_mapCos = v1.map(new Cos());
        double[] result_mapCos = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCos,v_mapCos.getData(),normTolerance);

        
        RealVector v_mapCosToSelf = v1.copy();
        v_mapCosToSelf.mapToSelf(new Cos());
        double[] result_mapCosToSelf = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCosToSelf,v_mapCosToSelf.getData(),normTolerance);

        
        RealVector v_mapSin = v1.map(new Sin());
        double[] result_mapSin = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSin,v_mapSin.getData(),normTolerance);

        
        RealVector v_mapSinToSelf = v1.copy();
        v_mapSinToSelf.mapToSelf(new Sin());
        double[] result_mapSinToSelf = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSinToSelf,v_mapSinToSelf.getData(),normTolerance);

        
        RealVector v_mapTan = v1.map(new Tan());
        double[] result_mapTan = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTan,v_mapTan.getData(),normTolerance);

        
        RealVector v_mapTanToSelf = v1.copy();
        v_mapTanToSelf.mapToSelf(new Tan());
        double[] result_mapTanToSelf = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTanToSelf,v_mapTanToSelf.getData(),normTolerance);

        double[] vat_a = {0d, 0.5d, 1.0d};
        ArrayRealVector vat = new ArrayRealVector(vat_a);

        
        RealVector v_mapAcos = vat.map(new Acos());
        double[] result_mapAcos = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcos,v_mapAcos.getData(),normTolerance);

        
        RealVector v_mapAcosToSelf = vat.copy();
        v_mapAcosToSelf.mapToSelf(new Acos());
        double[] result_mapAcosToSelf = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcosToSelf,v_mapAcosToSelf.getData(),normTolerance);

        
        RealVector v_mapAsin = vat.map(new Asin());
        double[] result_mapAsin = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsin,v_mapAsin.getData(),normTolerance);

        
        RealVector v_mapAsinToSelf = vat.copy();
        v_mapAsinToSelf.mapToSelf(new Asin());
        double[] result_mapAsinToSelf = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsinToSelf,v_mapAsinToSelf.getData(),normTolerance);

        
        RealVector v_mapAtan = vat.map(new Atan());
        double[] result_mapAtan = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtan,v_mapAtan.getData(),normTolerance);

        
        RealVector v_mapAtanToSelf = vat.copy();
        v_mapAtanToSelf.mapToSelf(new Atan());
        double[] result_mapAtanToSelf = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtanToSelf,v_mapAtanToSelf.getData(),normTolerance);

        
        RealVector v_mapInv = v1.map(new Inverse());
        double[] result_mapInv = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInv,v_mapInv.getData(),normTolerance);

        
        RealVector v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapToSelf(new Inverse());
        double[] result_mapInvToSelf = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData(),normTolerance);

        double[] abs_a = {-1.0d, 0.0d, 1.0d};
        ArrayRealVector abs_v = new ArrayRealVector(abs_a);

        
        RealVector v_mapAbs = abs_v.map(new Abs());
        double[] result_mapAbs = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbs,v_mapAbs.getData(),normTolerance);

        
        RealVector v_mapAbsToSelf = abs_v.copy();
        v_mapAbsToSelf.mapToSelf(new Abs());
        double[] result_mapAbsToSelf = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbsToSelf,v_mapAbsToSelf.getData(),normTolerance);

        
        RealVector v_mapSqrt = v1.map(new Sqrt());
        double[] result_mapSqrt = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrt,v_mapSqrt.getData(),normTolerance);

        
        RealVector v_mapSqrtToSelf = v1.copy();
        v_mapSqrtToSelf.mapToSelf(new Sqrt());
        double[] result_mapSqrtToSelf = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrtToSelf,v_mapSqrtToSelf.getData(),normTolerance);

        double[] cbrt_a = {-2.0d, 0.0d, 2.0d};
        ArrayRealVector cbrt_v = new ArrayRealVector(cbrt_a);

        
        RealVector v_mapCbrt = cbrt_v.map(new Cbrt());
        double[] result_mapCbrt = {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrt,v_mapCbrt.getData(),normTolerance);

        
        RealVector v_mapCbrtToSelf = cbrt_v.copy();
        v_mapCbrtToSelf.mapToSelf(new Cbrt());
        double[] result_mapCbrtToSelf =  {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrtToSelf,v_mapCbrtToSelf.getData(),normTolerance);

        double[] ceil_a = {-1.1d, 0.9d, 1.1d};
        ArrayRealVector ceil_v = new ArrayRealVector(ceil_a);

        
        RealVector v_mapCeil = ceil_v.map(new Ceil());
        double[] result_mapCeil = {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeil,v_mapCeil.getData(),normTolerance);

        
        RealVector v_mapCeilToSelf = ceil_v.copy();
        v_mapCeilToSelf.mapToSelf(new Ceil());
        double[] result_mapCeilToSelf =  {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeilToSelf,v_mapCeilToSelf.getData(),normTolerance);

        
        RealVector v_mapFloor = ceil_v.map(new Floor());
        double[] result_mapFloor = {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloor,v_mapFloor.getData(),normTolerance);

        
        RealVector v_mapFloorToSelf = ceil_v.copy();
        v_mapFloorToSelf.mapToSelf(new Floor());
        double[] result_mapFloorToSelf =  {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloorToSelf,v_mapFloorToSelf.getData(),normTolerance);

        
        RealVector v_mapRint = ceil_v.map(new Rint());
        double[] result_mapRint = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRint,v_mapRint.getData(),normTolerance);

        
        RealVector v_mapRintToSelf = ceil_v.copy();
        v_mapRintToSelf.mapToSelf(new Rint());
        double[] result_mapRintToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRintToSelf,v_mapRintToSelf.getData(),normTolerance);

        
        RealVector v_mapSignum = ceil_v.map(new Signum());
        double[] result_mapSignum = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignum,v_mapSignum.getData(),normTolerance);

        
        RealVector v_mapSignumToSelf = ceil_v.copy();
        v_mapSignumToSelf.mapToSelf(new Signum());
        double[] result_mapSignumToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignumToSelf,v_mapSignumToSelf.getData(),normTolerance);

        
        
        RealVector v_mapUlp = ceil_v.map(new Ulp());
        double[] result_mapUlp = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlp,v_mapUlp.getData(),normTolerance);

        
        RealVector v_mapUlpToSelf = ceil_v.copy();
        v_mapUlpToSelf.mapToSelf(new Ulp());
        double[] result_mapUlpToSelf = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlpToSelf,v_mapUlpToSelf.getData(),normTolerance);
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testBasicFunctions
    public void testBasicFunctions() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v2 = new ArrayRealVector(vec2);
        ArrayRealVector v5 = new ArrayRealVector(vec5);
        ArrayRealVector v_null = new ArrayRealVector(vec_null);

        RealVectorTestImpl v2_t = new RealVectorTestImpl(vec2);

        
        double d_getNorm = v5.getNorm();
        Assert.assertEquals("compare values  ", 8.4261497731763586307, d_getNorm, normTolerance);

        
        double d_getL1Norm = v5.getL1Norm();
        Assert.assertEquals("compare values  ", 17.0, d_getL1Norm, normTolerance);

        
        double d_getLInfNorm = v5.getLInfNorm();
        Assert.assertEquals("compare values  ", 6.0, d_getLInfNorm, normTolerance);

        
        double dist = v1.getDistance(v2);
        Assert.assertEquals("compare values  ",v1.subtract(v2).getNorm(), dist, normTolerance);

        
        double dist_2 = v1.getDistance(v2_t);
        Assert.assertEquals("compare values  ", v1.subtract(v2).getNorm(),dist_2, normTolerance);

        
        double dist_3 = v1.getDistance((RealVector) v2);
        Assert.assertEquals("compare values  ", v1.subtract(v2).getNorm(),dist_3, normTolerance);

        
        double d_getL1Distance = v1. getL1Distance(v2);
        Assert.assertEquals("compare values  ", 9d, d_getL1Distance, normTolerance);

        double d_getL1Distance_2 = v1.getL1Distance(v2_t);
        Assert.assertEquals("compare values  ", 9d, d_getL1Distance_2, normTolerance);

        double d_getL1Distance_3 = v1.getL1Distance((RealVector) v2);
        Assert.assertEquals("compare values  ", 9d, d_getL1Distance_3, normTolerance);

        
        double d_getLInfDistance = v1.getLInfDistance(v2);
        Assert.assertEquals("compare values  ", 3d, d_getLInfDistance, normTolerance);

        double d_getLInfDistance_2 = v1. getLInfDistance(v2_t);
        Assert.assertEquals("compare values  ", 3d, d_getLInfDistance_2, normTolerance);

        double d_getLInfDistance_3 = v1. getLInfDistance((RealVector) v2);
        Assert.assertEquals("compare values  ", 3d, d_getLInfDistance_3, normTolerance);

        
        ArrayRealVector v_add = v1.add(v2);
        double[] result_add = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add.getData(), result_add, normTolerance);

        RealVectorTestImpl vt2 = new RealVectorTestImpl(vec2);
        RealVector v_add_i = v1.add(vt2);
        double[] result_add_i = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add_i.getData(),result_add_i,normTolerance);

        
        ArrayRealVector v_subtract = v1.subtract(v2);
        double[] result_subtract = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract.getData(),result_subtract,normTolerance);

        RealVector v_subtract_i = v1.subtract(vt2);
        double[] result_subtract_i = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract_i.getData(),result_subtract_i,normTolerance);

        
        ArrayRealVector  v_ebeMultiply = v1.ebeMultiply(v2);
        double[] result_ebeMultiply = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply,normTolerance);

        RealVector  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        double[] result_ebeMultiply_2 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2,normTolerance);

        RealVector  v_ebeMultiply_3 = v1.ebeMultiply((RealVector) v2);
        double[] result_ebeMultiply_3 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_3.getData(),result_ebeMultiply_3,normTolerance);

        
        ArrayRealVector  v_ebeDivide = v1.ebeDivide(v2);
        double[] result_ebeDivide = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide.getData(),result_ebeDivide,normTolerance);

        RealVector  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        double[] result_ebeDivide_2 = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2,normTolerance);

        RealVector  v_ebeDivide_3 = v1.ebeDivide((RealVector) v2);
        double[] result_ebeDivide_3 = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide_3.getData(),result_ebeDivide_3,normTolerance);

        
        double dot =  v1.dotProduct(v2);
        Assert.assertEquals("compare val ",32d, dot, normTolerance);

        
        double dot_2 =  v1.dotProduct(v2_t);
        Assert.assertEquals("compare val ",32d, dot_2, normTolerance);

        RealMatrix m_outerProduct = v1.outerProduct(v2);
        Assert.assertEquals("compare val ",4d, m_outerProduct.getEntry(0,0), normTolerance);

        RealMatrix m_outerProduct_2 = v1.outerProduct(v2_t);
        Assert.assertEquals("compare val ",4d, m_outerProduct_2.getEntry(0,0), normTolerance);

        RealMatrix m_outerProduct_3 = v1.outerProduct((RealVector) v2);
        Assert.assertEquals("compare val ",4d, m_outerProduct_3.getEntry(0,0), normTolerance);

        RealVector v_unitVector = v1.unitVector();
        RealVector v_unitVector_2 = v1.mapDivide(v1.getNorm());
        assertClose("compare vect" ,v_unitVector.getData(),v_unitVector_2.getData(),normTolerance);

        try {
            v_null.unitVector();
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }

        ArrayRealVector v_unitize = v1.copy();
        v_unitize.unitize();
        assertClose("compare vect" ,v_unitVector_2.getData(),v_unitize.getData(),normTolerance);
        try {
            v_null.unitize();
            Assert.fail("Expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }

        ArrayRealVector v_projection = v1.projection(v2);
        double[] result_projection = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection.getData(), result_projection, normTolerance);

        RealVector v_projection_2 = v1.projection(v2_t);
        double[] result_projection_2 = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection_2.getData(), result_projection_2, normTolerance);

        RealVector v_projection_3 = v1.projection(v2.getData());
        double[] result_projection_3 = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection_3.getData(), result_projection_3, normTolerance);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testMisc
    public void testMisc() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v4 = new ArrayRealVector(vec4);
        RealVector v4_2 = new ArrayRealVector(vec4);

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

// org.apache.commons.math.linear.ArrayRealVectorTest::testPredicates
    public void testPredicates() {

        ArrayRealVector v = new ArrayRealVector(new double[] { 0, 1, 2 });

        Assert.assertFalse(v.isNaN());
        v.setEntry(1, Double.NaN);
        Assert.assertTrue(v.isNaN());

        Assert.assertFalse(v.isInfinite());
        v.setEntry(0, Double.POSITIVE_INFINITY);
        Assert.assertFalse(v.isInfinite());
        v.setEntry(1, 1);
        Assert.assertTrue(v.isInfinite());
        v.setEntry(0, 1);
        Assert.assertFalse(v.isInfinite());

        v.setEntry(0, 0);
        Assert.assertEquals(v, new ArrayRealVector(new double[] { 0, 1, 2 }));
        Assert.assertNotSame(v, new ArrayRealVector(new double[] { 0, 1, 2 + FastMath.ulp(2)}));
        Assert.assertNotSame(v, new ArrayRealVector(new double[] { 0, 1, 2, 3 }));

        Assert.assertEquals(new ArrayRealVector(new double[] { Double.NaN, 1, 2 }).hashCode(),
                     new ArrayRealVector(new double[] { 0, Double.NaN, 2 }).hashCode());

        Assert.assertTrue(new ArrayRealVector(new double[] { Double.NaN, 1, 2 }).hashCode() !=
                   new ArrayRealVector(new double[] { 0, 1, 2 }).hashCode());

        Assert.assertTrue(v.equals(v));
        Assert.assertTrue(v.equals(v.copy()));
        Assert.assertFalse(v.equals(null));
        Assert.assertFalse(v.equals(v.getDataRef()));
        Assert.assertFalse(v.equals(v.getSubVector(0, v.getDimension() - 1)));
        Assert.assertTrue(v.equals(v.getSubVector(0, v.getDimension())));

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testSerial
    public void testSerial()  {
        ArrayRealVector v = new ArrayRealVector(new double[] { 0, 1, 2 });
        Assert.assertEquals(v,TestUtils.serializeAndRecover(v));
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testZeroVectors
    public void testZeroVectors() {
        Assert.assertEquals(0, new ArrayRealVector(new double[0]).getDimension());
        Assert.assertEquals(0, new ArrayRealVector(new double[0], true).getDimension());
        Assert.assertEquals(0, new ArrayRealVector(new double[0], false).getDimension());
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testMinMax
    public void testMinMax()  {
        ArrayRealVector v1 = new ArrayRealVector(new double[] { 0, -6, 4, 12, 7 });
        Assert.assertEquals(1,  v1.getMinIndex());
        Assert.assertEquals(-6, v1.getMinValue(), 1.0e-12);
        Assert.assertEquals(3,  v1.getMaxIndex());
        Assert.assertEquals(12, v1.getMaxValue(), 1.0e-12);
        ArrayRealVector v2 = new ArrayRealVector(new double[] { Double.NaN, 3, Double.NaN, -2 });
        Assert.assertEquals(3,  v2.getMinIndex());
        Assert.assertEquals(-2, v2.getMinValue(), 1.0e-12);
        Assert.assertEquals(1,  v2.getMaxIndex());
        Assert.assertEquals(3, v2.getMaxValue(), 1.0e-12);
        ArrayRealVector v3 = new ArrayRealVector(new double[] { Double.NaN, Double.NaN });
        Assert.assertEquals(-1,  v3.getMinIndex());
        Assert.assertTrue(Double.isNaN(v3.getMinValue()));
        Assert.assertEquals(-1,  v3.getMaxIndex());
        Assert.assertTrue(Double.isNaN(v3.getMaxValue()));
        ArrayRealVector v4 = new ArrayRealVector(new double[0]);
        Assert.assertEquals(-1,  v4.getMinIndex());
        Assert.assertTrue(Double.isNaN(v4.getMinValue()));
        Assert.assertEquals(-1,  v4.getMaxIndex());
        Assert.assertTrue(Double.isNaN(v4.getMaxValue()));
    }

// org.apache.commons.math.linear.BiDiagonalTransformerTest::testDimensions
    public void testDimensions() {
        checkdimensions(MatrixUtils.createRealMatrix(testSquare));
        checkdimensions(MatrixUtils.createRealMatrix(testNonSquare));
        checkdimensions(MatrixUtils.createRealMatrix(testNonSquare).transpose());
    }

// org.apache.commons.math.linear.BiDiagonalTransformerTest::testAEqualUSVt
    public void testAEqualUSVt() {
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testSquare));
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testNonSquare));
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testNonSquare).transpose());
    }

// org.apache.commons.math.linear.BiDiagonalTransformerTest::testUOrthogonal
    public void testUOrthogonal() {
        checkOrthogonal(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare)).getU());
        checkOrthogonal(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testNonSquare)).getU());
        checkOrthogonal(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testNonSquare).transpose()).getU());
    }

// org.apache.commons.math.linear.BiDiagonalTransformerTest::testVOrthogonal
    public void testVOrthogonal() {
        checkOrthogonal(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare)).getV());
        checkOrthogonal(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testNonSquare)).getV());
        checkOrthogonal(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testNonSquare).transpose()).getV());
    }

// org.apache.commons.math.linear.BiDiagonalTransformerTest::testBBiDiagonal
    public void testBBiDiagonal() {
        checkBiDiagonal(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare)).getB());
        checkBiDiagonal(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testNonSquare)).getB());
        checkBiDiagonal(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testNonSquare).transpose()).getB());
    }

// org.apache.commons.math.linear.BiDiagonalTransformerTest::testSingularMatrix
    public void testSingularMatrix() {
       BiDiagonalTransformer transformer =
            new BiDiagonalTransformer(MatrixUtils.createRealMatrix(new double[][] {
                { 1.0, 2.0, 3.0 },
                { 2.0, 3.0, 4.0 },
                { 3.0, 5.0, 7.0 }
            }));
       final double s3  = FastMath.sqrt(3.0);
       final double s14 = FastMath.sqrt(14.0);
       final double s1553 = FastMath.sqrt(1553.0);
       RealMatrix uRef = MatrixUtils.createRealMatrix(new double[][] {
           {  -1.0 / s14,  5.0 / (s3 * s14),  1.0 / s3 },
           {  -2.0 / s14, -4.0 / (s3 * s14),  1.0 / s3 },
           {  -3.0 / s14,  1.0 / (s3 * s14), -1.0 / s3 }
       });
       RealMatrix bRef = MatrixUtils.createRealMatrix(new double[][] {
           { -s14, s1553 / s14,   0.0 },
           {  0.0, -87 * s3 / (s14 * s1553), -s3 * s14 / s1553 },
           {  0.0, 0.0, 0.0 }
       });
       RealMatrix vRef = MatrixUtils.createRealMatrix(new double[][] {
           { 1.0,   0.0,         0.0        },
           { 0.0,  -23 / s1553,  32 / s1553 },
           { 0.0,  -32 / s1553, -23 / s1553 }
       });

       
       RealMatrix u = transformer.getU();
       Assert.assertEquals(0, u.subtract(uRef).getNorm(), 1.0e-14);
       RealMatrix b = transformer.getB();
       Assert.assertEquals(0, b.subtract(bRef).getNorm(), 1.0e-14);
       RealMatrix v = transformer.getV();
       Assert.assertEquals(0, v.subtract(vRef).getNorm(), 1.0e-14);

       
       Assert.assertTrue(u == transformer.getU());
       Assert.assertTrue(b == transformer.getB());
       Assert.assertTrue(v == transformer.getV());

    }

// org.apache.commons.math.linear.BiDiagonalTransformerTest::testMatricesValues
    public void testMatricesValues() {
       BiDiagonalTransformer transformer =
            new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare));
       final double s17 = FastMath.sqrt(17.0);
        RealMatrix uRef = MatrixUtils.createRealMatrix(new double[][] {
                {  -8 / (5 * s17), 19 / (5 * s17) },
                { -19 / (5 * s17), -8 / (5 * s17) }
        });
        RealMatrix bRef = MatrixUtils.createRealMatrix(new double[][] {
                { -3 * s17 / 5, 32 * s17 / 85 },
                {      0.0,     -5 * s17 / 17 }
        });
        RealMatrix vRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1.0,  0.0 },
                { 0.0, -1.0 }
        });

        
        RealMatrix u = transformer.getU();
        Assert.assertEquals(0, u.subtract(uRef).getNorm(), 1.0e-14);
        RealMatrix b = transformer.getB();
        Assert.assertEquals(0, b.subtract(bRef).getNorm(), 1.0e-14);
        RealMatrix v = transformer.getV();
        Assert.assertEquals(0, v.subtract(vRef).getNorm(), 1.0e-14);

        
        Assert.assertTrue(u == transformer.getU());
        Assert.assertTrue(b == transformer.getB());
        Assert.assertTrue(v == transformer.getV());

    }

// org.apache.commons.math.linear.BiDiagonalTransformerTest::testUpperOrLower
    public void testUpperOrLower() {
        Assert.assertTrue(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testSquare)).isUpperBiDiagonal());
        Assert.assertTrue(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testNonSquare)).isUpperBiDiagonal());
        Assert.assertFalse(new BiDiagonalTransformer(MatrixUtils.createRealMatrix(testNonSquare).transpose()).isUpperBiDiagonal());
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testDimensions
    public void testDimensions() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Random r = new Random(66636328996002l);
        BlockFieldMatrix<Fraction> m1 = createRandomMatrix(r, 47, 83);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(m1.getData());
        assertEquals(m1, m2);
        BlockFieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(m3.getData());
        assertEquals(m3, m4);
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testAdd
    public void testAdd() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        Fraction[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals(testDataPlusInv[row][col],sumEntries[row][col]);
            }
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testAddFail
    public void testAddFail() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        try {
            m.add(m2);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testPlusMinus
    public void testPlusMinus() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testDataInv);
        TestUtils.assertEquals(m.subtract(m2), m2.scalarMultiply(new Fraction(-1)).add(m));
        try {
            m.subtract(new BlockFieldMatrix<Fraction>(testData2));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testMultiply
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
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSeveralBlocks
    public void testSeveralBlocks() {

        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), 37, 41);
        for (int i = 0; i < m.getRowDimension(); ++i) {
            for (int j = 0; j < m.getColumnDimension(); ++j) {
                m.setEntry(i, j, new Fraction(i * 11 + j, 11));
            }
        }

        FieldMatrix<Fraction> mT = m.transpose();
        assertEquals(m.getRowDimension(), mT.getColumnDimension());
        assertEquals(m.getColumnDimension(), mT.getRowDimension());
        for (int i = 0; i < mT.getRowDimension(); ++i) {
            for (int j = 0; j < mT.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(j, i), mT.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mPm = m.add(m);
        for (int i = 0; i < mPm.getRowDimension(); ++i) {
            for (int j = 0; j < mPm.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(i, j).multiply(new Fraction(2)), mPm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mPmMm = mPm.subtract(m);
        for (int i = 0; i < mPmMm.getRowDimension(); ++i) {
            for (int j = 0; j < mPmMm.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(i, j), mPmMm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mTm = mT.multiply(m);
        for (int i = 0; i < mTm.getRowDimension(); ++i) {
            for (int j = 0; j < mTm.getColumnDimension(); ++j) {
                Fraction sum = Fraction.ZERO;
                for (int k = 0; k < mT.getColumnDimension(); ++k) {
                    sum = sum.add(new Fraction(k * 11 + i, 11).multiply(new Fraction(k * 11 + j, 11)));
                }
                assertEquals(sum, mTm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mmT = m.multiply(mT);
        for (int i = 0; i < mmT.getRowDimension(); ++i) {
            for (int j = 0; j < mmT.getColumnDimension(); ++j) {
                Fraction sum = Fraction.ZERO;
                for (int k = 0; k < m.getColumnDimension(); ++k) {
                    sum = sum.add(new Fraction(i * 11 + k, 11).multiply(new Fraction(j * 11 + k, 11)));
                }
                assertEquals(sum, mmT.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub1 = m.getSubMatrix(2, 9, 5, 20);
        for (int i = 0; i < sub1.getRowDimension(); ++i) {
            for (int j = 0; j < sub1.getColumnDimension(); ++j) {
                assertEquals(new Fraction((i + 2) * 11 + (j + 5), 11), sub1.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub2 = m.getSubMatrix(10, 12, 3, 40);
        for (int i = 0; i < sub2.getRowDimension(); ++i) {
            for (int j = 0; j < sub2.getColumnDimension(); ++j) {
                assertEquals(new Fraction((i + 10) * 11 + (j + 3), 11), sub2.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub3 = m.getSubMatrix(30, 34, 0, 5);
        for (int i = 0; i < sub3.getRowDimension(); ++i) {
            for (int j = 0; j < sub3.getColumnDimension(); ++j) {
                assertEquals(new Fraction((i + 30) * 11 + (j + 0), 11), sub3.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub4 = m.getSubMatrix(30, 32, 32, 35);
        for (int i = 0; i < sub4.getRowDimension(); ++i) {
            for (int j = 0; j < sub4.getColumnDimension(); ++j) {
                assertEquals(new Fraction((i + 30) * 11 + (j + 32), 11), sub4.getEntry(i, j));
            }
        }

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testMultiply2
    public void testMultiply2() {
       FieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(d3);
       FieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(d4);
       FieldMatrix<Fraction> m5 = new BlockFieldMatrix<Fraction>(d5);
       TestUtils.assertEquals(m3.multiply(m4), m5);
   }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(id);
        assertEquals(new Fraction(3),m.getTrace());
        m = new BlockFieldMatrix<Fraction>(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(new BlockFieldMatrix<Fraction>(testDataPlus2),
                               m.scalarAdd(new Fraction(2)));
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(testVector, m.operate(testVector));
        TestUtils.assertEquals(testVector, m.operate(new ArrayFieldVector<Fraction>(testVector)).getData());
        m = new BlockFieldMatrix<Fraction>(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testOperateLarge
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

// org.apache.commons.math.linear.BlockFieldMatrixTest::testOperatePremultiplyLarge
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

// org.apache.commons.math.linear.BlockFieldMatrixTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = new BlockFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(2) },
                { new Fraction(3), new Fraction(4) },
                { new Fraction(5), new Fraction(6) }
        });
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( new Fraction(3), b[0]);
        assertEquals( new Fraction(7), b[1]);
        assertEquals(new Fraction(11), b[2]);
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecompositionImpl<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecompositionImpl<Fraction>(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals(mIT, mTI);
        m = new BlockFieldMatrix<Fraction>(testData2);
        FieldMatrix<Fraction> mt = new BlockFieldMatrix<Fraction>(testData2T);
        TestUtils.assertEquals(mt, m.transpose());
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.preMultiply(testVector), preMultTest);
        TestUtils.assertEquals(m.preMultiply(new ArrayFieldVector<Fraction>(testVector).getData()),
                               preMultTest);
        m = new BlockFieldMatrix<Fraction>(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testPremultiply
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
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.getRow(0), testDataRow1);
        TestUtils.assertEquals(m.getColumn(2), testDataCol3);
        try {
            m.getRow(10);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        assertEquals(m.getEntry(0,1),new Fraction(2));
        try {
            m.getEntry(10, 4);
            fail ("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testExamples
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
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecompositionImpl<Fraction>(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = {
                {new Fraction(2), new Fraction(3), new Fraction(-2)},
                {new Fraction(-1), new Fraction(7), new Fraction(6)},
                {new Fraction(4), new Fraction(-3), new Fraction(-5)}
        };
        FieldMatrix<Fraction> coefficients = new BlockFieldMatrix<Fraction>(coefficientsData);
        Fraction[] constants = {new Fraction(1), new Fraction(-2), new Fraction(1)};
        Fraction[] solution = new FieldLUDecompositionImpl<Fraction>(coefficients).getSolver().solve(constants);
        assertEquals(new Fraction(2).multiply(solution[0]).
                     add(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(2).multiply(solution[2])),
                     constants[0]);
        assertEquals(new Fraction(-1).multiply(solution[0]).
                     add(new Fraction(7).multiply(solution[1])).
                     add(new Fraction(6).multiply(solution[2])),
                     constants[1]);
        assertEquals(new Fraction(4).multiply(solution[0]).
                     subtract(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(5).multiply(solution[2])),
                     constants[2]);

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSubMatrix
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

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetMatrixLarge
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
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getSubMatrix(2, n - 3, 2, n - 3));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testCopySubMatrix
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

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m     = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow0 = new BlockFieldMatrix<Fraction>(subRow0);
        FieldMatrix<Fraction> mRow3 = new BlockFieldMatrix<Fraction>(subRow3);
        assertEquals("Row0", mRow0, m.getRowMatrix(0));
        assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow3 = new BlockFieldMatrix<Fraction>(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetRowMatrixLarge
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
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getRowMatrix(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn1 = new BlockFieldMatrix<Fraction>(subColumn1);
        FieldMatrix<Fraction> mColumn3 = new BlockFieldMatrix<Fraction>(subColumn3);
        assertEquals(mColumn1, m.getColumnMatrix(1));
        assertEquals(mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn3 = new BlockFieldMatrix<Fraction>(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetColumnMatrixLarge
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
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getColumnMatrix(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        assertEquals(mRow0, m.getRowVector(0));
        assertEquals(mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetRowVector
    public void testSetRowVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetRowVectorLarge
    public void testGetSetRowVectorLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldVector<Fraction> sub = new ArrayFieldVector<Fraction>(n, new Fraction(1));

        m.setRowVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getRowVector(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        assertEquals(mColumn1, m.getColumnVector(1));
        assertEquals(mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetColumnVectorLarge
    public void testGetSetColumnVectorLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldVector<Fraction> sub = new ArrayFieldVector<Fraction>(n, new Fraction(1));

        m.setColumnVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        assertEquals(sub, m.getColumnVector(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetRow
    public void testGetRow() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetRow
    public void testSetRow() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new Fraction[5]);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetRowLarge
    public void testGetSetRowLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        Fraction[] sub = new Fraction[n];
        Arrays.fill(sub, new Fraction(1));

        m.setRow(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        checkArrays(sub, m.getRow(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetColumn
    public void testGetColumn() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn1 = columnToArray(subColumn1);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetColumn
    public void testSetColumn() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new Fraction[5]);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testGetSetColumnLarge
    public void testGetSetColumnLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        Fraction[] sub = new Fraction[n];
        Arrays.fill(sub, new Fraction(1));

        m.setColumn(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        checkArrays(sub, m.getColumn(2));

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m1 = (BlockFieldMatrix<Fraction>) m.copy();
        BlockFieldMatrix<Fraction> mt = (BlockFieldMatrix<Fraction>) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new BlockFieldMatrix<Fraction>(bigSingular)));
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testToString
    public void testToString() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        assertEquals("BlockFieldMatrix{{1,2,3},{2,5,3},{1,0,8}}", m.toString());
    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        m.setSubMatrix(detData2,1,1);
        FieldMatrix<Fraction> expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(1),new Fraction(2),new Fraction(3)},{new Fraction(2),new Fraction(1),new Fraction(3)},{new Fraction(1),new Fraction(2),new Fraction(4)}});
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(1),new Fraction(3),new Fraction(3)},{new Fraction(2),new Fraction(4),new Fraction(3)},{new Fraction(1),new Fraction(2),new Fraction(4)}});
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(3),new Fraction(4),new Fraction(5)},{new Fraction(4),new Fraction(7),new Fraction(5)},{new Fraction(3),new Fraction(2),new Fraction(10)}});
        assertEquals(expected, m);

        
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
        assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{new Fraction(1)}, {new Fraction(2), new Fraction(3)}}, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{}}, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testWalk
    public void testWalk() throws MathUserException {
        int rows    = 150;
        int columns = 75;

        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(new Fraction(0), m.getEntry(i, 0));
            assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(new Fraction(0), m.getEntry(0, j));
            assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(new Fraction(0), m.getEntry(i, 0));
            assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(new Fraction(0), m.getEntry(0, j));
            assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(new Fraction(0), m.getEntry(i, 0));
            assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(new Fraction(0), m.getEntry(0, j));
            assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(new Fraction(0), m.getEntry(i, 0));
            assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(new Fraction(0), m.getEntry(0, j));
            assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

    }

// org.apache.commons.math.linear.BlockFieldMatrixTest::testSerial
    public void testSerial()  {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testDimensions
    public void testDimensions() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Random r = new Random(66636328996002l);
        BlockRealMatrix m1 = createRandomMatrix(r, 47, 83);
        BlockRealMatrix m2 = new BlockRealMatrix(m1.getData());
        assertEquals(m1, m2);
        BlockRealMatrix m3 = new BlockRealMatrix(testData);
        BlockRealMatrix m4 = new BlockRealMatrix(m3.getData());
        assertEquals(m3, m4);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testAdd
    public void testAdd() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix mInv = new BlockRealMatrix(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
                    testDataPlusInv[row][col],sumEntries[row][col],
                        entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testAddFail
    public void testAddFail() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        try {
            m.add(m2);
            fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testNorm
    public void testNorm() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertEquals("testData Frobenius norm", FastMath.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        assertEquals("testData2 Frobenius norm", FastMath.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testDataInv);
        assertClose(m.subtract(m2), m2.scalarMultiply(-1d).add(m), entryTolerance);
        try {
            m.subtract(new BlockRealMatrix(testData2));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testMultiply
     public void testMultiply() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix mInv = new BlockRealMatrix(testDataInv);
        BlockRealMatrix identity = new BlockRealMatrix(id);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertClose(m.multiply(mInv), identity, entryTolerance);
        assertClose(mInv.multiply(m), identity, entryTolerance);
        assertClose(m.multiply(identity), m, entryTolerance);
        assertClose(identity.multiply(mInv), mInv, entryTolerance);
        assertClose(m2.multiply(identity), m2, entryTolerance);
        try {
            m.multiply(new BlockRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSeveralBlocks
    public void testSeveralBlocks() {

        RealMatrix m = new BlockRealMatrix(35, 71);
        for (int i = 0; i < m.getRowDimension(); ++i) {
            for (int j = 0; j < m.getColumnDimension(); ++j) {
                m.setEntry(i, j, i + j / 1024.0);
            }
        }

        RealMatrix mT = m.transpose();
        assertEquals(m.getRowDimension(), mT.getColumnDimension());
        assertEquals(m.getColumnDimension(), mT.getRowDimension());
        for (int i = 0; i < mT.getRowDimension(); ++i) {
            for (int j = 0; j < mT.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(j, i), mT.getEntry(i, j), 0);
            }
        }

        RealMatrix mPm = m.add(m);
        for (int i = 0; i < mPm.getRowDimension(); ++i) {
            for (int j = 0; j < mPm.getColumnDimension(); ++j) {
                assertEquals(2 * m.getEntry(i, j), mPm.getEntry(i, j), 0);
            }
        }

        RealMatrix mPmMm = mPm.subtract(m);
        for (int i = 0; i < mPmMm.getRowDimension(); ++i) {
            for (int j = 0; j < mPmMm.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(i, j), mPmMm.getEntry(i, j), 0);
            }
        }

        RealMatrix mTm = mT.multiply(m);
        for (int i = 0; i < mTm.getRowDimension(); ++i) {
            for (int j = 0; j < mTm.getColumnDimension(); ++j) {
                double sum = 0;
                for (int k = 0; k < mT.getColumnDimension(); ++k) {
                    sum += (k + i / 1024.0) * (k + j / 1024.0);
                }
                assertEquals(sum, mTm.getEntry(i, j), 0);
            }
        }

        RealMatrix mmT = m.multiply(mT);
        for (int i = 0; i < mmT.getRowDimension(); ++i) {
            for (int j = 0; j < mmT.getColumnDimension(); ++j) {
                double sum = 0;
                for (int k = 0; k < m.getColumnDimension(); ++k) {
                    sum += (i + k / 1024.0) * (j + k / 1024.0);
                }
                assertEquals(sum, mmT.getEntry(i, j), 0);
            }
        }

        RealMatrix sub1 = m.getSubMatrix(2, 9, 5, 20);
        for (int i = 0; i < sub1.getRowDimension(); ++i) {
            for (int j = 0; j < sub1.getColumnDimension(); ++j) {
                assertEquals((i + 2) + (j + 5) / 1024.0, sub1.getEntry(i, j), 0);
            }
        }

        RealMatrix sub2 = m.getSubMatrix(10, 12, 3, 70);
        for (int i = 0; i < sub2.getRowDimension(); ++i) {
            for (int j = 0; j < sub2.getColumnDimension(); ++j) {
                assertEquals((i + 10) + (j + 3) / 1024.0, sub2.getEntry(i, j), 0);
            }
        }

        RealMatrix sub3 = m.getSubMatrix(30, 34, 0, 5);
        for (int i = 0; i < sub3.getRowDimension(); ++i) {
            for (int j = 0; j < sub3.getColumnDimension(); ++j) {
                assertEquals((i + 30) + (j + 0) / 1024.0, sub3.getEntry(i, j), 0);
            }
        }

        RealMatrix sub4 = m.getSubMatrix(30, 32, 62, 65);
        for (int i = 0; i < sub4.getRowDimension(); ++i) {
            for (int j = 0; j < sub4.getColumnDimension(); ++j) {
                assertEquals((i + 30) + (j + 62) / 1024.0, sub4.getEntry(i, j), 0);
            }
        }

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testMultiply2
    public void testMultiply2() {
       RealMatrix m3 = new BlockRealMatrix(d3);
       RealMatrix m4 = new BlockRealMatrix(d4);
       RealMatrix m5 = new BlockRealMatrix(d5);
       assertClose(m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.BlockRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = new BlockRealMatrix(id);
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new BlockRealMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(new BlockRealMatrix(testDataPlus2), m.scalarAdd(2d), entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testOperate
    public void testOperate() {
        RealMatrix m = new BlockRealMatrix(id);
        assertClose(testVector, m.operate(testVector), entryTolerance);
        assertClose(testVector, m.operate(new ArrayRealVector(testVector)).getData(), entryTolerance);
        m = new BlockRealMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testOperateLarge
    public void testOperateLarge() {
        int p = (7 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int q = (5 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int r =  3 * BlockRealMatrix.BLOCK_SIZE;
        Random random = new Random(111007463902334l);
        RealMatrix m1 = createRandomMatrix(random, p, q);
        RealMatrix m2 = createRandomMatrix(random, q, r);
        RealMatrix m1m2 = m1.multiply(m2);
        for (int i = 0; i < r; ++i) {
            checkArrays(m1m2.getColumn(i), m1.operate(m2.getColumn(i)));
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testOperatePremultiplyLarge
    public void testOperatePremultiplyLarge() {
        int p = (7 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int q = (5 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int r =  3 * BlockRealMatrix.BLOCK_SIZE;
        Random random = new Random(111007463902334l);
        RealMatrix m1 = createRandomMatrix(random, p, q);
        RealMatrix m2 = createRandomMatrix(random, q, r);
        RealMatrix m1m2 = m1.multiply(m2);
        for (int i = 0; i < p; ++i) {
            checkArrays(m1m2.getRow(i), m2.preMultiply(m1.getRow(i)));
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = new BlockRealMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        });
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0], 1.0e-12);
        assertEquals( 7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new BlockRealMatrix(testData);
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        assertClose(mIT, mTI, normTolerance);
        m = new BlockRealMatrix(testData2);
        RealMatrix mt = new BlockRealMatrix(testData2T);
        assertClose(mt, m.transpose(), normTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(m.preMultiply(testVector), preMultTest, normTolerance);
        assertClose(m.preMultiply(new ArrayRealVector(testVector).getData()),
                    preMultTest, normTolerance);
        m = new BlockRealMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new BlockRealMatrix(d3);
        RealMatrix m4 = new BlockRealMatrix(d4);
        RealMatrix m5 = new BlockRealMatrix(d5);
        assertClose(m4.preMultiply(m3), m5, entryTolerance);

        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix mInv = new BlockRealMatrix(testDataInv);
        BlockRealMatrix identity = new BlockRealMatrix(id);
        assertClose(m.preMultiply(mInv), identity, entryTolerance);
        assertClose(mInv.preMultiply(m), identity, entryTolerance);
        assertClose(m.preMultiply(identity), m, entryTolerance);
        assertClose(identity.preMultiply(mInv), mInv, entryTolerance);
        try {
            m.preMultiply(new BlockRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(m.getRow(0), testDataRow1, entryTolerance);
        assertClose(m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            fail ("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new BlockRealMatrix(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new BlockRealMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new BlockRealMatrix(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
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

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetMatrixLarge
    public void testGetSetMatrixLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealMatrix sub = new BlockRealMatrix(n - 4, n - 4).scalarAdd(1);

        m.setSubMatrix(sub.getData(), 2, 2);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((i < 2) || (i > n - 3) || (j < 2) || (j > n - 3)) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getSubMatrix(2, n - 3, 2, n - 3));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
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

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m     = new BlockRealMatrix(subTestData);
        RealMatrix mRow0 = new BlockRealMatrix(subRow0);
        RealMatrix mRow3 = new BlockRealMatrix(subRow3);
        assertEquals("Row0", mRow0, m.getRowMatrix(0));
        assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mRow3 = new BlockRealMatrix(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetRowMatrixLarge
    public void testGetSetRowMatrixLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealMatrix sub = new BlockRealMatrix(1, n).scalarAdd(1);

        m.setRowMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getRowMatrix(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mColumn1 = new BlockRealMatrix(subColumn1);
        RealMatrix mColumn3 = new BlockRealMatrix(subColumn3);
        assertEquals(mColumn1, m.getColumnMatrix(1));
        assertEquals(mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mColumn3 = new BlockRealMatrix(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetColumnMatrixLarge
    public void testGetSetColumnMatrixLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealMatrix sub = new BlockRealMatrix(n, 1).scalarAdd(1);

        m.setColumnMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getColumnMatrix(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertEquals(mRow0, m.getRowVector(0));
        assertEquals(mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetRowVectorLarge
    public void testGetSetRowVectorLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealVector sub = new ArrayRealVector(n, 1.0);

        m.setRowVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getRowVector(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertEquals(mColumn1, m.getColumnVector(1));
        assertEquals(mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetColumnVectorLarge
    public void testGetSetColumnVectorLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealVector sub = new ArrayRealVector(n, 1.0);

        m.setColumnVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getColumnVector(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetRowLarge
    public void testGetSetRowLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        double[] sub = new double[n];
        Arrays.fill(sub, 1.0);

        m.setRow(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        checkArrays(sub, m.getRow(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        double[] mColumn1 = columnToArray(subColumn1);
        double[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetColumnLarge
    public void testGetSetColumnLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        double[] sub = new double[n];
        Arrays.fill(sub, 1.0);

        m.setColumn(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        checkArrays(sub, m.getColumn(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m1 = m.copy();
        BlockRealMatrix mt = m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new BlockRealMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testToString
    public void testToString() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        assertEquals("BlockRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = new BlockRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new BlockRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new BlockRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        assertEquals(expected, m);

        
        BlockRealMatrix matrix = new BlockRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1 , 2}});
        matrix.setSubMatrix(new double[][] {{3, 4}, {5, 6}}, 1, 1);
        expected = new BlockRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 3, 4, 8}, {9, 5 ,6, 2}});
        assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testWalk
    public void testWalk() throws MathUserException {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new BlockRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSerial
    public void testSerial()  {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testDimensions
    public void testDimensions() {
        CholeskyDecomposition llt =
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(testData));
        assertEquals(testData.length, llt.getL().getRowDimension());
        assertEquals(testData.length, llt.getL().getColumnDimension());
        assertEquals(testData.length, llt.getLT().getRowDimension());
        assertEquals(testData.length, llt.getLT().getColumnDimension());
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testNonSquare
    public void testNonSquare() {
        new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(new double[3][2]));
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testNotSymmetricMatrixException
    public void testNotSymmetricMatrixException() {
        double[][] changed = testData.clone();
        changed[0][changed[0].length - 1] += 1.0e-5;
        new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(changed));
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testNotPositiveDefinite
    public void testNotPositiveDefinite() {
        new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(new double[][] {
                { 14, 11, 13, 15, 24 },
                { 11, 34, 13, 8,  25 },
                { 13, 13, 14, 15, 21 },
                { 15, 8,  15, 18, 23 },
                { 24, 25, 21, 23, 45 }
        }));
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testMath274
    public void testMath274() {
        new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(new double[][] {
                { 0.40434286, -0.09376327, 0.30328980, 0.04909388 },
                {-0.09376327,  0.10400408, 0.07137959, 0.04762857 },
                { 0.30328980,  0.07137959, 0.30458776, 0.04882449 },
                { 0.04909388,  0.04762857, 0.04882449, 0.07543265 }

        }));
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testAEqualLLT
    public void testAEqualLLT() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        CholeskyDecomposition llt = new CholeskyDecompositionImpl(matrix);
        RealMatrix l  = llt.getL();
        RealMatrix lt = llt.getLT();
        double norm = l.multiply(lt).subtract(matrix).getNorm();
        assertEquals(0, norm, 1.0e-15);
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testLLowerTriangular
    public void testLLowerTriangular() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        RealMatrix l = new CholeskyDecompositionImpl(matrix).getL();
        for (int i = 0; i < l.getRowDimension(); i++) {
            for (int j = i + 1; j < l.getColumnDimension(); j++) {
                assertEquals(0.0, l.getEntry(i, j), 0.0);
            }
        }
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testLTTransposed
    public void testLTTransposed() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testData);
        CholeskyDecomposition llt = new CholeskyDecompositionImpl(matrix);
        RealMatrix l  = llt.getL();
        RealMatrix lt = llt.getLT();
        double norm = l.subtract(lt.transpose()).getNorm();
        assertEquals(0, norm, 1.0e-15);
    }

// org.apache.commons.math.linear.CholeskyDecompositionImplTest::testMatricesValues
    public void testMatricesValues() {
        RealMatrix lRef = MatrixUtils.createRealMatrix(new double[][] {
                {  1,  0,  0,  0,  0 },
                {  2,  3,  0,  0,  0 },
                {  4,  5,  6,  0,  0 },
                {  7,  8,  9, 10,  0 },
                { 11, 12, 13, 14, 15 }
        });
       CholeskyDecomposition llt =
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(testData));

        
        RealMatrix l = llt.getL();
        assertEquals(0, l.subtract(lRef).getNorm(), 1.0e-13);
        RealMatrix lt = llt.getLT();
        assertEquals(0, lt.subtract(lRef.transpose()).getNorm(), 1.0e-13);

        
        assertTrue(l  == llt.getL());
        assertTrue(lt == llt.getLT());

    }

// org.apache.commons.math.linear.CholeskySolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException iae) {
            
        }
    }

// org.apache.commons.math.linear.CholeskySolverTest::testSolve
    public void testSolve() {
        DecompositionSolver solver =
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                {   78,  -13,    1 },
                {  414,  -62,   -1 },
                { 1312, -202,  -37 },
                { 2989, -542,  145 },
                { 5510, -1465, 201 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1,  0,  1 },
                { 0,  1,  1 },
                { 2,  1, -4 },
                { 2,  2,  2 },
                { 5, -3,  0 }
        });

        
        assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 1.0e-13);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         new ArrayRealVector(solver.solve(b.getColumn(i))).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         solver.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            assertEquals(0,
                         solver.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

    }

// org.apache.commons.math.linear.CholeskySolverTest::testDeterminant
    public void testDeterminant() {
        assertEquals(7290000.0, getDeterminant(MatrixUtils.createRealMatrix(testData)), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension1
    public void testDimension1() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] { { 1.5 } });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(1.5, ed.getRealEigenvalue(0), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension2
    public void testDimension2() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                    { 59.0, 12.0 },
                    { 12.0, 66.0 }
            });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(75.0, ed.getRealEigenvalue(0), 1.0e-15);
        assertEquals(50.0, ed.getRealEigenvalue(1), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension3
    public void testDimension3() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  39632.0, -4824.0, -16560.0 },
                                   {  -4824.0,  8693.0,   7920.0 },
                                   { -16560.0,  7920.0,  17300.0 }
                               });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(50000.0, ed.getRealEigenvalue(0), 3.0e-11);
        assertEquals(12500.0, ed.getRealEigenvalue(1), 3.0e-11);
        assertEquals( 3125.0, ed.getRealEigenvalue(2), 3.0e-11);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension3MultipleRoot
    public void testDimension3MultipleRoot() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                    {  5,   10,   15 },
                    { 10,   20,   30 },
                    { 15,   30,   45 }
            });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(70.0, ed.getRealEigenvalue(0), 3.0e-11);
        assertEquals(0.0,  ed.getRealEigenvalue(1), 3.0e-11);
        assertEquals(0.0,  ed.getRealEigenvalue(2), 3.0e-11);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension4WithSplit
    public void testDimension4WithSplit() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  0.784, -0.288,  0.000,  0.000 },
                                   { -0.288,  0.616,  0.000,  0.000 },
                                   {  0.000,  0.000,  0.164, -0.048 },
                                   {  0.000,  0.000, -0.048,  0.136 }
                               });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(1.0, ed.getRealEigenvalue(0), 1.0e-15);
        assertEquals(0.4, ed.getRealEigenvalue(1), 1.0e-15);
        assertEquals(0.2, ed.getRealEigenvalue(2), 1.0e-15);
        assertEquals(0.1, ed.getRealEigenvalue(3), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension4WithoutSplit
    public void testDimension4WithoutSplit() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  0.5608, -0.2016,  0.1152, -0.2976 },
                                   { -0.2016,  0.4432, -0.2304,  0.1152 },
                                   {  0.1152, -0.2304,  0.3088, -0.1344 },
                                   { -0.2976,  0.1152, -0.1344,  0.3872 }
                               });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(1.0, ed.getRealEigenvalue(0), 1.0e-15);
        assertEquals(0.4, ed.getRealEigenvalue(1), 1.0e-15);
        assertEquals(0.2, ed.getRealEigenvalue(2), 1.0e-15);
        assertEquals(0.1, ed.getRealEigenvalue(3), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testMath308
    public void testMath308() {

        double[] mainTridiagonal = {
            22.330154644539597, 46.65485522478641, 17.393672330044705, 54.46687435351116, 80.17800767709437
        };
        double[] secondaryTridiagonal = {
            13.04450406501361, -5.977590941539671, 2.9040909856707517, 7.1570352792841225
        };

        
        
        double[] refEigenValues = {
            82.044413207204002, 53.456697699894512, 52.536278520113882, 18.847969733754262, 14.138204224043099
        };
        RealVector[] refEigenVectors = {
            new ArrayRealVector(new double[] { -0.000462690386766, -0.002118073109055,  0.011530080757413,  0.252322434584915,  0.967572088232592 }),
            new ArrayRealVector(new double[] {  0.314647769490148,  0.750806415553905, -0.167700312025760, -0.537092972407375,  0.143854968127780 }),
            new ArrayRealVector(new double[] {  0.222368839324646,  0.514921891363332, -0.021377019336614,  0.801196801016305, -0.207446991247740 }),
            new ArrayRealVector(new double[] { -0.713933751051495,  0.190582113553930, -0.671410443368332,  0.056056055955050, -0.006541576993581 }),
            new ArrayRealVector(new double[] { -0.584677060845929,  0.367177264979103,  0.721453187784497, -0.052971054621812,  0.005740715188257 })
        };

        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-5);
            assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 2.0e-7);
        }

    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testMathpbx02
    public void testMathpbx02() {

        double[] mainTridiagonal = {
              7484.860960227216, 18405.28129035345, 13855.225609560746,
             10016.708722343366, 559.8117399576674, 6750.190788301587,
                71.21428769782159
        };
        double[] secondaryTridiagonal = {
             -4175.088570476366,1975.7955858241994,5193.178422374075,
              1995.286659169179,75.34535882933804,-234.0808002076056
        };

        
        
        double[] refEigenValues = {
                20654.744890306974412,16828.208208485466457,
                6893.155912634994820,6757.083016675340332,
                5887.799885688558788,64.309089923240379,
                57.992628792736340
        };
        RealVector[] refEigenVectors = {
                new ArrayRealVector(new double[] {-0.270356342026904, 0.852811091326997, 0.399639490702077, 0.198794657813990, 0.019739323307666, 0.000106983022327, -0.000001216636321}),
                new ArrayRealVector(new double[] {0.179995273578326,-0.402807848153042,0.701870993525734,0.555058211014888,0.068079148898236,0.000509139115227,-0.000007112235617}),
                new ArrayRealVector(new double[] {-0.399582721284727,-0.056629954519333,-0.514406488522827,0.711168164518580,0.225548081276367,0.125943999652923,-0.004321507456014}),
                new ArrayRealVector(new double[] {0.058515721572821,0.010200130057739,0.063516274916536,-0.090696087449378,-0.017148420432597,0.991318870265707,-0.034707338554096}),
                new ArrayRealVector(new double[] {0.855205995537564,0.327134656629775,-0.265382397060548,0.282690729026706,0.105736068025572,-0.009138126622039,0.000367751821196}),
                new ArrayRealVector(new double[] {-0.002913069901144,-0.005177515777101,0.041906334478672,-0.109315918416258,0.436192305456741,0.026307315639535,0.891797507436344}),
                new ArrayRealVector(new double[] {-0.005738311176435,-0.010207611670378,0.082662420517928,-0.215733886094368,0.861606487840411,-0.025478530652759,-0.451080697503958})
        };

        
        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-3);
            if (refEigenVectors[i].dotProduct(decomposition.getEigenvector(i)) < 0) {
                assertEquals(0, refEigenVectors[i].add(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            } else {
                assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            }
        }

    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testMathpbx03
    public void testMathpbx03() {

        double[] mainTridiagonal = {
            1809.0978259647177,3395.4763425956166,1832.1894584712693,3804.364873592377,
            806.0482458637571,2403.656427234185,28.48691431556015
        };
        double[] secondaryTridiagonal = {
            -656.8932064545833,-469.30804108920734,-1021.7714889369421,
            -1152.540497328983,-939.9765163817368,-12.885877015422391
        };

        
        
        double[] refEigenValues = {
            4603.121913685183245,3691.195818048970978,2743.442955402465032,1657.596442107321764,
            1336.797819095331306,30.129865209677519,17.035352085224986
        };

        RealVector[] refEigenVectors = {
            new ArrayRealVector(new double[] {-0.036249830202337,0.154184732411519,-0.346016328392363,0.867540105133093,-0.294483395433451,0.125854235969548,-0.000354507444044}),
            new ArrayRealVector(new double[] {-0.318654191697157,0.912992309960507,-0.129270874079777,-0.184150038178035,0.096521712579439,-0.070468788536461,0.000247918177736}),
            new ArrayRealVector(new double[] {-0.051394668681147,0.073102235876933,0.173502042943743,-0.188311980310942,-0.327158794289386,0.905206581432676,-0.004296342252659}),
            new ArrayRealVector(new double[] {0.838150199198361,0.193305209055716,-0.457341242126146,-0.166933875895419,0.094512811358535,0.119062381338757,-0.000941755685226}),
            new ArrayRealVector(new double[] {0.438071395458547,0.314969169786246,0.768480630802146,0.227919171600705,-0.193317045298647,-0.170305467485594,0.001677380536009}),
            new ArrayRealVector(new double[] {-0.003726503878741,-0.010091946369146,-0.067152015137611,-0.113798146542187,-0.313123000097908,-0.118940107954918,0.932862311396062}),
            new ArrayRealVector(new double[] {0.009373003194332,0.025570377559400,0.170955836081348,0.291954519805750,0.807824267665706,0.320108347088646,0.360202112392266}),
        };

        
        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-4);
            if (refEigenVectors[i].dotProduct(decomposition.getEigenvector(i)) < 0) {
                assertEquals(0, refEigenVectors[i].add(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            } else {
                assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            }
        }

    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testTridiagonal
    public void testTridiagonal() {
        Random r = new Random(4366663527842l);
        double[] ref = new double[30];
        for (int i = 0; i < ref.length; ++i) {
            if (i < 5) {
                ref[i] = 2 * r.nextDouble() - 1;
            } else {
                ref[i] = 0.0001 * r.nextDouble() + 6;
            }
        }
        Arrays.sort(ref);
        TriDiagonalTransformer t =
            new TriDiagonalTransformer(createTestMatrix(r, ref));
        EigenDecomposition ed =
            new EigenDecompositionImpl(t.getMainDiagonalRef(),
                                       t.getSecondaryDiagonalRef(),
                                       MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        assertEquals(ref.length, eigenValues.length);
        for (int i = 0; i < ref.length; ++i) {
            assertEquals(ref[ref.length - i - 1], eigenValues[i], 2.0e-14);
        }

    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimensions
    public void testDimensions() {
        final int m = matrix.getRowDimension();
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(m, ed.getV().getRowDimension());
        assertEquals(m, ed.getV().getColumnDimension());
        assertEquals(m, ed.getD().getColumnDimension());
        assertEquals(m, ed.getD().getColumnDimension());
        assertEquals(m, ed.getVT().getRowDimension());
        assertEquals(m, ed.getVT().getColumnDimension());
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testEigenvalues
    public void testEigenvalues() {
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        assertEquals(refValues.length, eigenValues.length);
        for (int i = 0; i < refValues.length; ++i) {
            assertEquals(refValues[i], eigenValues[i], 3.0e-15);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testBigMatrix
    public void testBigMatrix() {
        Random r = new Random(17748333525117l);
        double[] bigValues = new double[200];
        for (int i = 0; i < bigValues.length; ++i) {
            bigValues[i] = 2 * r.nextDouble() - 1;
        }
        Arrays.sort(bigValues);
        EigenDecomposition ed =
            new EigenDecompositionImpl(createTestMatrix(r, bigValues), MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        assertEquals(bigValues.length, eigenValues.length);
        for (int i = 0; i < bigValues.length; ++i) {
            assertEquals(bigValues[bigValues.length - i - 1], eigenValues[i], 2.0e-14);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testEigenvectors
    public void testEigenvectors() {
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            double lambda = ed.getRealEigenvalue(i);
            RealVector v  = ed.getEigenvector(i);
            RealVector mV = matrix.operate(v);
            assertEquals(0, mV.subtract(v.mapMultiplyToSelf(lambda)).getNorm(), 1.0e-13);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testAEqualVDVt
    public void testAEqualVDVt() {
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealMatrix v  = ed.getV();
        RealMatrix d  = ed.getD();
        RealMatrix vT = ed.getVT();
        double norm = v.multiply(d).multiply(vT).subtract(matrix).getNorm();
        assertEquals(0, norm, 6.0e-13);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testVOrthogonal
    public void testVOrthogonal() {
        RealMatrix v = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN).getV();
        RealMatrix vTv = v.transpose().multiply(v);
        RealMatrix id  = MatrixUtils.createRealIdentityMatrix(vTv.getRowDimension());
        assertEquals(0, vTv.subtract(id).getNorm(), 2.0e-13);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDiagonal
    public void testDiagonal() {
        double[] diagonal = new double[] { -3.0, -2.0, 2.0, 5.0 };
        RealMatrix m = createDiagonalMatrix(diagonal, diagonal.length, diagonal.length);
        EigenDecomposition ed = new EigenDecompositionImpl(m, MathUtils.SAFE_MIN);
        assertEquals(diagonal[0], ed.getRealEigenvalue(3), 2.0e-15);
        assertEquals(diagonal[1], ed.getRealEigenvalue(2), 2.0e-15);
        assertEquals(diagonal[2], ed.getRealEigenvalue(1), 2.0e-15);
        assertEquals(diagonal[3], ed.getRealEigenvalue(0), 2.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testRepeatedEigenvalue
    public void testRepeatedEigenvalue() {
        RealMatrix repeated = MatrixUtils.createRealMatrix(new double[][] {
                {3,  2,  4},
                {2,  0,  2},
                {4,  2,  3}
        });
        EigenDecomposition ed = new EigenDecompositionImpl(repeated, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {8, -1, -1}), ed, 1E-12);
        checkEigenVector((new double[] {2, 1, 2}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDistinctEigenvalues
    public void testDistinctEigenvalues() {
        RealMatrix distinct = MatrixUtils.createRealMatrix(new double[][] {
                {3, 1, -4},
                {1, 3, -4},
                {-4, -4, 8}
        });
        EigenDecomposition ed = new EigenDecompositionImpl(distinct, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {2, 0, 12}), ed, 1E-12);
        checkEigenVector((new double[] {1, -1, 0}), ed, 1E-12);
        checkEigenVector((new double[] {1, 1, 1}), ed, 1E-12);
        checkEigenVector((new double[] {-1, -1, 2}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testZeroDivide
    public void testZeroDivide() {
        RealMatrix indefinite = MatrixUtils.createRealMatrix(new double [][] {
                { 0.0, 1.0, -1.0 },
                { 1.0, 1.0, 0.0 },
                { -1.0,0.0, 1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(indefinite, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {2, 1, -1}), ed, 1E-12);
        double isqrt3 = 1/FastMath.sqrt(3.0);
        checkEigenVector((new double[] {isqrt3,isqrt3,-isqrt3}), ed, 1E-12);
        double isqrt2 = 1/FastMath.sqrt(2.0);
        checkEigenVector((new double[] {0.0,-isqrt2,-isqrt2}), ed, 1E-12);
        double isqrt6 = 1/FastMath.sqrt(6.0);
        checkEigenVector((new double[] {2*isqrt6,-isqrt6,isqrt6}), ed, 1E-12);
    }
