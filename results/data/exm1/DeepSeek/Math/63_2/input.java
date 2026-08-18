// buggy code
    public static boolean equals(double x, double y) {
        return (Double.isNaN(x) && Double.isNaN(y)) || x == y;
    }

// relevant test
// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        SummaryStatistics u = createSummaryStatistics();
        SummaryStatistics t = null;
        int emptyHash = u.hashCode();
        assertTrue("reflexive", u.equals(u));
        assertFalse("non-null compared to null", u.equals(t));
        assertFalse("wrong type", u.equals(Double.valueOf(0)));
        t = createSummaryStatistics();
        assertTrue("empty instances should be equal", t.equals(u));
        assertTrue("empty instances should be equal", u.equals(t));
        assertEquals("empty hash code", emptyHash, t.hashCode());

        
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        assertFalse("different n's should make instances not equal", t.equals(u));
        assertFalse("different n's should make instances not equal", u.equals(t));
        assertTrue("different n's should make hashcodes different",
                u.hashCode() != t.hashCode());

        
        t.addValue(2d);
        t.addValue(1d);
        t.addValue(3d);
        t.addValue(4d);
        assertTrue("summaries based on same data should be equal", t.equals(u));
        assertTrue("summaries based on same data should be equal", u.equals(t));
        assertEquals("summaries based on same data should have same hashcodes",
                u.hashCode(), t.hashCode());

        
        u.clear();
        t.clear();
        assertTrue("empty instances should be equal", t.equals(u));
        assertTrue("empty instances should be equal", u.equals(t));
        assertEquals("empty hash code", emptyHash, t.hashCode());
        assertEquals("empty hash code", emptyHash, u.hashCode());
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testCopy
    public void testCopy() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        SummaryStatistics v = new SummaryStatistics(u);
        assertEquals(u, v);
        assertEquals(v, u);
        assertTrue(v.geoMean == v.getGeoMeanImpl());
        assertTrue(v.mean == v.getMeanImpl());
        assertTrue(v.min == v.getMinImpl());
        assertTrue(v.max == v.getMaxImpl());
        assertTrue(v.sum == v.getSumImpl());
        assertTrue(v.sumsq == v.getSumsqImpl());
        assertTrue(v.sumLog == v.getSumLogImpl());
        assertTrue(v.variance == v.getVarianceImpl());

        
        u.addValue(7d);
        u.addValue(9d);
        u.addValue(11d);
        u.addValue(23d);
        v.addValue(7d);
        v.addValue(9d);
        v.addValue(11d);
        v.addValue(23d);
        assertEquals(u, v);
        assertEquals(v, u);

        
        u.clear();
        u.setSumImpl(new Sum());
        SummaryStatistics.copy(u,v);
        assertEquals(u.sum, v.sum);
        assertEquals(u.getSumImpl(), v.getSumImpl());

    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.setMeanImpl(new Sum());
        u.setSumLogImpl(new Sum());
        u.addValue(1);
        u.addValue(3);
        assertEquals(4, u.getMean(), 1E-14);
        assertEquals(4, u.getSumOfLogs(), 1E-14);
        assertEquals(FastMath.exp(2), u.getGeometricMean(), 1E-14);
        u.clear();
        u.addValue(1);
        u.addValue(2);
        assertEquals(3, u.getMean(), 1E-14);
        u.clear();
        u.setMeanImpl(new Mean()); 
    }

// org.apache.commons.math.stat.descriptive.SummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() throws Exception {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(1);
        u.addValue(3);
        try {
            u.setMeanImpl(new Sum());
            fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.moment.GeometricMeanTest::testSpecialValues
    public void testSpecialValues() {
        GeometricMean mean = new GeometricMean();
        
        assertTrue(Double.isNaN(mean.getResult()));

        
        mean.increment(1d);
        assertFalse(Double.isNaN(mean.getResult()));

        
        mean.increment(0d);
        assertEquals(0d, mean.getResult(), 0);

        
        mean.increment(Double.POSITIVE_INFINITY);
        assertTrue(Double.isNaN(mean.getResult()));

        
        mean.clear();
        assertTrue(Double.isNaN(mean.getResult()));

        
        mean.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, mean.getResult(), 0);

        
        mean.increment(-2d);
        assertTrue(Double.isNaN(mean.getResult()));
    }

// org.apache.commons.math.stat.descriptive.moment.KurtosisTest::testNaN
    public void testNaN() {
        Kurtosis kurt = new Kurtosis();
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        assertFalse(Double.isNaN(kurt.getResult()));
    }

// org.apache.commons.math.stat.descriptive.moment.MeanTest::testSmallSamples
    public void testSmallSamples() {
        Mean mean = new Mean();
        assertTrue(Double.isNaN(mean.getResult()));
        mean.increment(1d);
        assertEquals(1d, mean.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.MeanTest::testWeightedMean
    public void testWeightedMean() {
        Mean mean = new Mean();
        assertEquals(expectedWeightedValue(), mean.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());
        assertEquals(expectedValue(), mean.evaluate(testArray, identicalWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math.stat.descriptive.moment.SkewnessTest::testNaN
    public void testNaN() {
        Skewness skew = new Skewness();
        assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        assertFalse(Double.isNaN(skew.getResult()));
    }

// org.apache.commons.math.stat.descriptive.moment.StandardDeviationTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.StandardDeviationTest::testPopulation
    public void testPopulation() {
        double[] values = {-1.0d, 3.1d, 4.0d, -2.1d, 22d, 11.7d, 3d, 14d};
        double sigma = populationStandardDeviation(values);
        SecondMoment m = new SecondMoment();
        m.evaluate(values);  
        StandardDeviation s1 = new StandardDeviation();
        s1.setBiasCorrected(false);
        assertEquals(sigma, s1.evaluate(values), 1E-14);
        s1.incrementAll(values);
        assertEquals(sigma, s1.getResult(), 1E-14);
        s1 = new StandardDeviation(false, m);
        assertEquals(sigma, s1.getResult(), 1E-14);
        s1 = new StandardDeviation(false);
        assertEquals(sigma, s1.evaluate(values), 1E-14);
        s1.incrementAll(values);
        assertEquals(sigma, s1.getResult(), 1E-14);
    }

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testPopulation
    public void testPopulation() {
        double[] values = {-1.0d, 3.1d, 4.0d, -2.1d, 22d, 11.7d, 3d, 14d};
        SecondMoment m = new SecondMoment();
        m.evaluate(values);  
        Variance v1 = new Variance();
        v1.setBiasCorrected(false);
        assertEquals(populationVariance(values), v1.evaluate(values), 1E-14);
        v1.incrementAll(values);
        assertEquals(populationVariance(values), v1.getResult(), 1E-14);
        v1 = new Variance(false, m);
        assertEquals(populationVariance(values), v1.getResult(), 1E-14);
        v1 = new Variance(false);
        assertEquals(populationVariance(values), v1.evaluate(values), 1E-14);
        v1.incrementAll(values);
        assertEquals(populationVariance(values), v1.getResult(), 1E-14);
    }

// org.apache.commons.math.stat.descriptive.moment.VarianceTest::testWeightedVariance
    public void testWeightedVariance() {
        Variance variance = new Variance();
        assertEquals(expectedWeightedValue(),
                variance.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());

        
        assertEquals(expectedValue(),
                variance.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());

        
        
        assertEquals(expectedValue(),
                variance.evaluate(testArray, MathUtils.normalizeArray(identicalWeightsArray, testArray.length),
                        0, testArray.length), getTolerance());

    }

// org.apache.commons.math.stat.descriptive.moment.VectorialMeanTest::testMismatch
    public void testMismatch() {
        try {
            new VectorialMean(8).increment(new double[5]);
            fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            assertEquals(5, dme.getDimension1());
            assertEquals(8, dme.getDimension2());
        } catch (Exception e) {
            fail("wrong exception type caught: " + e.getClass().getName());
        }
    }

// org.apache.commons.math.stat.descriptive.moment.VectorialMeanTest::testSimplistic
    public void testSimplistic() throws DimensionMismatchException {
        VectorialMean stat = new VectorialMean(2);
        stat.increment(new double[] {-1.0,  1.0});
        stat.increment(new double[] { 1.0, -1.0});
        double[] mean = stat.getResult();
        assertEquals(0.0, mean[0], 1.0e-12);
        assertEquals(0.0, mean[1], 1.0e-12);
    }

// org.apache.commons.math.stat.descriptive.moment.VectorialMeanTest::testBasicStats
    public void testBasicStats() throws DimensionMismatchException {

        VectorialMean stat = new VectorialMean(points[0].length);
        for (int i = 0; i < points.length; ++i) {
            stat.increment(points[i]);
        }

        assertEquals(points.length, stat.getN());

        double[] mean = stat.getResult();
        double[]   refMean = new double[] { 1.78, 1.62,  3.12};

        for (int i = 0; i < mean.length; ++i) {
            assertEquals(refMean[i], mean[i], 1.0e-12);
        }

    }

// org.apache.commons.math.stat.descriptive.moment.VectorialMeanTest::testSerial
    public void testSerial() throws DimensionMismatchException {
        VectorialMean stat = new VectorialMean(points[0].length);
        for (int i = 0; i < points.length; ++i) {
            stat.increment(points[i]);
        }
        assertEquals(stat, TestUtils.serializeAndRecover(stat));
    }

// org.apache.commons.math.stat.descriptive.rank.MaxTest::testSpecialValues
    public void testSpecialValues() {
        double[] testArray = {0d, Double.NaN, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY};
        Max max = new Max();
        assertTrue(Double.isNaN(max.getResult()));
        max.increment(testArray[0]);
        assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[1]);
        assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[2]);
        assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[3]);
        assertEquals(Double.POSITIVE_INFINITY, max.getResult(), 0);
        assertEquals(Double.POSITIVE_INFINITY, max.evaluate(testArray), 0);
    }

// org.apache.commons.math.stat.descriptive.rank.MaxTest::testNaNs
    public void testNaNs() {
        Max max = new Max();
        double nan = Double.NaN;
        assertEquals(3d, max.evaluate(new double[]{nan, 2d, 3d}), 0);
        assertEquals(3d, max.evaluate(new double[]{1d, nan, 3d}), 0);
        assertEquals(2d, max.evaluate(new double[]{1d, 2d, nan}), 0);
        assertTrue(Double.isNaN(max.evaluate(new double[]{nan, nan, nan})));
    }

// org.apache.commons.math.stat.descriptive.rank.MinTest::testSpecialValues
    public void testSpecialValues() {
        double[] testArray = {0d, Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY};
        Min min = new Min();
        assertTrue(Double.isNaN(min.getResult()));
        min.increment(testArray[0]);
        assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[1]);
        assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[2]);
        assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[3]);
        assertEquals(Double.NEGATIVE_INFINITY, min.getResult(), 0);
        assertEquals(Double.NEGATIVE_INFINITY, min.evaluate(testArray), 0);
    }

// org.apache.commons.math.stat.descriptive.rank.MinTest::testNaNs
    public void testNaNs() {
        Min min = new Min();
        double nan = Double.NaN;
        assertEquals(2d, min.evaluate(new double[]{nan, 2d, 3d}), 0);
        assertEquals(1d, min.evaluate(new double[]{1d, nan, 3d}), 0);
        assertEquals(1d, min.evaluate(new double[]{1d, 2d, nan}), 0);
        assertTrue(Double.isNaN(min.evaluate(new double[]{nan, nan, nan})));
    }

// org.apache.commons.math.stat.descriptive.summary.ProductTest::testSpecialValues
    public void testSpecialValues() {
        Product product = new Product();
        assertTrue(Double.isNaN(product.getResult()));
        product.increment(1);
        assertEquals(1, product.getResult(), 0);
        product.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, product.getResult(), 0);
        product.increment(Double.NEGATIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, product.getResult(), 0);
        product.increment(Double.NaN);
        assertTrue(Double.isNaN(product.getResult()));
        product.increment(1);
        assertTrue(Double.isNaN(product.getResult()));
    }

// org.apache.commons.math.stat.descriptive.summary.ProductTest::testWeightedProduct
    public void testWeightedProduct() {
        Product product = new Product();
        assertEquals(expectedWeightedValue(), product.evaluate(testArray, testWeightsArray, 0, testArray.length),getTolerance());
        assertEquals(expectedValue(), product.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math.stat.descriptive.summary.SumLogTest::testSpecialValues
    public void testSpecialValues() {
        SumOfLogs sum = new SumOfLogs();
        
        assertTrue(Double.isNaN(sum.getResult()));

        
        sum.increment(1d);
        assertFalse(Double.isNaN(sum.getResult()));

        
        sum.increment(0d);
        assertEquals(Double.NEGATIVE_INFINITY, sum.getResult(), 0);

        
        sum.increment(Double.POSITIVE_INFINITY);
        assertTrue(Double.isNaN(sum.getResult()));

        
        sum.clear();
        assertTrue(Double.isNaN(sum.getResult()));

        
        sum.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sum.getResult(), 0);

        
        sum.increment(-2d);
        assertTrue(Double.isNaN(sum.getResult()));
    }

// org.apache.commons.math.stat.descriptive.summary.SumSqTest::testSpecialValues
    public void testSpecialValues() {
        SumOfSquares sumSq = new SumOfSquares();
        assertTrue(Double.isNaN(sumSq.getResult()));
        sumSq.increment(2d);
        assertEquals(4d, sumSq.getResult(), 0);
        sumSq.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sumSq.getResult(), 0);
        sumSq.increment(Double.NEGATIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sumSq.getResult(), 0);
        sumSq.increment(Double.NaN);
        assertTrue(Double.isNaN(sumSq.getResult()));
        sumSq.increment(1);
        assertTrue(Double.isNaN(sumSq.getResult()));
    }

// org.apache.commons.math.stat.descriptive.summary.SumTest::testSpecialValues
    public void testSpecialValues() {
        Sum sum = new Sum();
        assertTrue(Double.isNaN(sum.getResult()));
        sum.increment(1);
        assertEquals(1, sum.getResult(), 0);
        sum.increment(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, sum.getResult(), 0);
        sum.increment(Double.NEGATIVE_INFINITY);
        assertTrue(Double.isNaN(sum.getResult()));
        sum.increment(1);
        assertTrue(Double.isNaN(sum.getResult()));
    }

// org.apache.commons.math.stat.descriptive.summary.SumTest::testWeightedSum
    public void testWeightedSum() {
        Sum sum = new Sum();
        assertEquals(expectedWeightedValue(), sum.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());
        assertEquals(expectedValue(), sum.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math.stat.descriptive.summary.SumTest::testWeightedConsistency
    public void testWeightedConsistency() {}

// org.apache.commons.math.util.MathUtilsTest::test0Choose0
    public void test0Choose0() {
        assertEquals(MathUtils.binomialCoefficientDouble(0, 0), 1d, 0);
        assertEquals(MathUtils.binomialCoefficientLog(0, 0), 0d, 0);
        assertEquals(MathUtils.binomialCoefficient(0, 0), 1);
    }

// org.apache.commons.math.util.MathUtilsTest::testAddAndCheck
    public void testAddAndCheck() {
        int big = Integer.MAX_VALUE;
        int bigNeg = Integer.MIN_VALUE;
        assertEquals(big, MathUtils.addAndCheck(big, 0));
        try {
            MathUtils.addAndCheck(big, 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }
        try {
            MathUtils.addAndCheck(bigNeg, -1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testAddAndCheckLong
    public void testAddAndCheckLong() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        assertEquals(max, MathUtils.addAndCheck(max, 0L));
        assertEquals(min, MathUtils.addAndCheck(min, 0L));
        assertEquals(max, MathUtils.addAndCheck(0L, max));
        assertEquals(min, MathUtils.addAndCheck(0L, min));
        assertEquals(1, MathUtils.addAndCheck(-1L, 2L));
        assertEquals(1, MathUtils.addAndCheck(2L, -1L));
        assertEquals(-3, MathUtils.addAndCheck(-2L, -1L));
        assertEquals(min, MathUtils.addAndCheck(min + 1, -1L));
        testAddAndCheckLongFailure(max, 1L);
        testAddAndCheckLongFailure(min, -1L);
        testAddAndCheckLongFailure(1L, max);
        testAddAndCheckLongFailure(-1L, min);
    }

// org.apache.commons.math.util.MathUtilsTest::testBinomialCoefficient
    public void testBinomialCoefficient() {
        long[] bcoef5 = {
            1,
            5,
            10,
            10,
            5,
            1 };
        long[] bcoef6 = {
            1,
            6,
            15,
            20,
            15,
            6,
            1 };
        for (int i = 0; i < 6; i++) {
            assertEquals("5 choose " + i, bcoef5[i], MathUtils.binomialCoefficient(5, i));
        }
        for (int i = 0; i < 7; i++) {
            assertEquals("6 choose " + i, bcoef6[i], MathUtils.binomialCoefficient(6, i));
        }

        for (int n = 1; n < 10; n++) {
            for (int k = 0; k <= n; k++) {
                assertEquals(n + " choose " + k, binomialCoefficient(n, k), MathUtils.binomialCoefficient(n, k));
                assertEquals(n + " choose " + k, binomialCoefficient(n, k), MathUtils.binomialCoefficientDouble(n, k), Double.MIN_VALUE);
                assertEquals(n + " choose " + k, FastMath.log(binomialCoefficient(n, k)), MathUtils.binomialCoefficientLog(n, k), 10E-12);
            }
        }

        int[] n = { 34, 66, 100, 1500, 1500 };
        int[] k = { 17, 33, 10, 1500 - 4, 4 };
        for (int i = 0; i < n.length; i++) {
            long expected = binomialCoefficient(n[i], k[i]);
            assertEquals(n[i] + " choose " + k[i], expected,
                MathUtils.binomialCoefficient(n[i], k[i]));
            assertEquals(n[i] + " choose " + k[i], expected,
                MathUtils.binomialCoefficientDouble(n[i], k[i]), 0.0);
            assertEquals("log(" + n[i] + " choose " + k[i] + ")", FastMath.log(expected),
                MathUtils.binomialCoefficientLog(n[i], k[i]), 0.0);
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testBinomialCoefficientLarge
    public void testBinomialCoefficientLarge() throws Exception {
        
        for (int n = 0; n <= 200; n++) {
            for (int k = 0; k <= n; k++) {
                long ourResult = -1;
                long exactResult = -1;
                boolean shouldThrow = false;
                boolean didThrow = false;
                try {
                    ourResult = MathUtils.binomialCoefficient(n, k);
                } catch (ArithmeticException ex) {
                    didThrow = true;
                }
                try {
                    exactResult = binomialCoefficient(n, k);
                } catch (ArithmeticException ex) {
                    shouldThrow = true;
                }
                assertEquals(n + " choose " + k, exactResult, ourResult);
                assertEquals(n + " choose " + k, shouldThrow, didThrow);
                assertTrue(n + " choose " + k, (n > 66 || !didThrow));

                if (!shouldThrow && exactResult > 1) {
                    assertEquals(n + " choose " + k, 1.,
                        MathUtils.binomialCoefficientDouble(n, k) / exactResult, 1e-10);
                    assertEquals(n + " choose " + k, 1,
                        MathUtils.binomialCoefficientLog(n, k) / FastMath.log(exactResult), 1e-10);
                }
            }
        }

        long ourResult = MathUtils.binomialCoefficient(300, 3);
        long exactResult = binomialCoefficient(300, 3);
        assertEquals(exactResult, ourResult);

        ourResult = MathUtils.binomialCoefficient(700, 697);
        exactResult = binomialCoefficient(700, 697);
        assertEquals(exactResult, ourResult);

        
        try {
            MathUtils.binomialCoefficient(700, 300);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        }

        int n = 10000;
        ourResult = MathUtils.binomialCoefficient(n, 3);
        exactResult = binomialCoefficient(n, 3);
        assertEquals(exactResult, ourResult);
        assertEquals(1, MathUtils.binomialCoefficientDouble(n, 3) / exactResult, 1e-10);
        assertEquals(1, MathUtils.binomialCoefficientLog(n, 3) / FastMath.log(exactResult), 1e-10);

    }

// org.apache.commons.math.util.MathUtilsTest::testBinomialCoefficientFail
    public void testBinomialCoefficientFail() {
        try {
            MathUtils.binomialCoefficient(4, 5);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            MathUtils.binomialCoefficientDouble(4, 5);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            MathUtils.binomialCoefficientLog(4, 5);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            MathUtils.binomialCoefficient(-1, -2);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MathUtils.binomialCoefficientDouble(-1, -2);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MathUtils.binomialCoefficientLog(-1, -2);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            MathUtils.binomialCoefficient(67, 30);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        }
        try {
            MathUtils.binomialCoefficient(67, 34);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        }
        double x = MathUtils.binomialCoefficientDouble(1030, 515);
        assertTrue("expecting infinite binomial coefficient", Double
            .isInfinite(x));
    }

// org.apache.commons.math.util.MathUtilsTest::testCompareTo
    public void testCompareTo() {
      assertEquals(0, MathUtils.compareTo(152.33, 152.32, .011));
      assertTrue(MathUtils.compareTo(152.308, 152.32, .011) < 0);
      assertTrue(MathUtils.compareTo(152.33, 152.318, .011) > 0);
    }

// org.apache.commons.math.util.MathUtilsTest::testCosh
    public void testCosh() {
        double x = 3.0;
        double expected = 10.06766;
        assertEquals(expected, MathUtils.cosh(x), 1.0e-5);
    }

// org.apache.commons.math.util.MathUtilsTest::testCoshNaN
    public void testCoshNaN() {
        assertTrue(Double.isNaN(MathUtils.cosh(Double.NaN)));
    }

// org.apache.commons.math.util.MathUtilsTest::testEqualsIncludingNaN
    public void testEqualsIncludingNaN() {
        double[] testArray = {
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            1d,
            0d };
        for (int i = 0; i < testArray.length; i++) {
            for (int j = 0; j < testArray.length; j++) {
                if (i == j) {
                    assertTrue(MathUtils.equalsIncludingNaN(testArray[i], testArray[j]));
                    assertTrue(MathUtils.equalsIncludingNaN(testArray[j], testArray[i]));
                } else {
                    assertTrue(!MathUtils.equalsIncludingNaN(testArray[i], testArray[j]));
                    assertTrue(!MathUtils.equalsIncludingNaN(testArray[j], testArray[i]));
                }
            }
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testEqualsWithAllowedDelta
    public void testEqualsWithAllowedDelta() {
        assertTrue(MathUtils.equals(153.0000, 153.0000, .0625));
        assertTrue(MathUtils.equals(153.0000, 153.0625, .0625));
        assertTrue(MathUtils.equals(152.9375, 153.0000, .0625));
        assertFalse(MathUtils.equals(Double.NaN, Double.NaN, 1.0));
        assertTrue(MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
        assertTrue(MathUtils.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0));
        assertFalse(MathUtils.equals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
        assertFalse(MathUtils.equals(153.0000, 153.0625, .0624));
        assertFalse(MathUtils.equals(152.9374, 153.0000, .0625));
    }

// org.apache.commons.math.util.MathUtilsTest::testEqualsIncludingNaNWithAllowedDelta
    public void testEqualsIncludingNaNWithAllowedDelta() {
        assertTrue(MathUtils.equalsIncludingNaN(153.0000, 153.0000, .0625));
        assertTrue(MathUtils.equalsIncludingNaN(153.0000, 153.0625, .0625));
        assertTrue(MathUtils.equalsIncludingNaN(152.9375, 153.0000, .0625));
        assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 1.0));
        assertTrue(MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
        assertTrue(MathUtils.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0));
        assertFalse(MathUtils.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0));
        assertFalse(MathUtils.equalsIncludingNaN(153.0000, 153.0625, .0624));
        assertFalse(MathUtils.equalsIncludingNaN(152.9374, 153.0000, .0625));
    }

// org.apache.commons.math.util.MathUtilsTest::testEqualsWithAllowedUlps
    public void testEqualsWithAllowedUlps() {
        assertTrue(MathUtils.equals(0.0, -0.0, 1));

        assertTrue(MathUtils.equals(1.0, 1 + FastMath.ulp(1d), 1));
        assertFalse(MathUtils.equals(1.0, 1 + 2 * FastMath.ulp(1d), 1));

        final double nUp1 = FastMath.nextAfter(1d, Double.POSITIVE_INFINITY);
        final double nnUp1 = FastMath.nextAfter(nUp1, Double.POSITIVE_INFINITY);
        assertTrue(MathUtils.equals(1.0, nUp1, 1));
        assertTrue(MathUtils.equals(nUp1, nnUp1, 1));
        assertFalse(MathUtils.equals(1.0, nnUp1, 1));

        assertTrue(MathUtils.equals(0.0, FastMath.ulp(0d), 1));
        assertTrue(MathUtils.equals(0.0, -FastMath.ulp(0d), 1));

        assertTrue(MathUtils.equals(153.0, 153.0, 1));

        assertTrue(MathUtils.equals(153.0, 153.00000000000003, 1));
        assertFalse(MathUtils.equals(153.0, 153.00000000000006, 1));
        assertTrue(MathUtils.equals(153.0, 152.99999999999997, 1));
        assertFalse(MathUtils.equals(153, 152.99999999999994, 1));

        assertTrue(MathUtils.equals(-128.0, -127.99999999999999, 1));
        assertFalse(MathUtils.equals(-128.0, -127.99999999999997, 1));
        assertTrue(MathUtils.equals(-128.0, -128.00000000000003, 1));
        assertFalse(MathUtils.equals(-128.0, -128.00000000000006, 1));

        assertTrue(MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1));
        assertTrue(MathUtils.equals(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 1));

        assertTrue(MathUtils.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1));
        assertTrue(MathUtils.equals(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY, 1));

        assertFalse(MathUtils.equals(Double.NaN, Double.NaN, 1));

        assertFalse(MathUtils.equals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 100000));
    }

// org.apache.commons.math.util.MathUtilsTest::testEqualsIncludingNaNWithAllowedUlps
    public void testEqualsIncludingNaNWithAllowedUlps() {
        assertTrue(MathUtils.equalsIncludingNaN(0.0, -0.0, 1));

        assertTrue(MathUtils.equalsIncludingNaN(1.0, 1 + FastMath.ulp(1d), 1));
        assertFalse(MathUtils.equalsIncludingNaN(1.0, 1 + 2 * FastMath.ulp(1d), 1));

        final double nUp1 = FastMath.nextAfter(1d, Double.POSITIVE_INFINITY);
        final double nnUp1 = FastMath.nextAfter(nUp1, Double.POSITIVE_INFINITY);
        assertTrue(MathUtils.equalsIncludingNaN(1.0, nUp1, 1));
        assertTrue(MathUtils.equalsIncludingNaN(nUp1, nnUp1, 1));
        assertFalse(MathUtils.equalsIncludingNaN(1.0, nnUp1, 1));

        assertTrue(MathUtils.equalsIncludingNaN(0.0, FastMath.ulp(0d), 1));
        assertTrue(MathUtils.equalsIncludingNaN(0.0, -FastMath.ulp(0d), 1));

        assertTrue(MathUtils.equalsIncludingNaN(153.0, 153.0, 1));

        assertTrue(MathUtils.equalsIncludingNaN(153.0, 153.00000000000003, 1));
        assertFalse(MathUtils.equalsIncludingNaN(153.0, 153.00000000000006, 1));
        assertTrue(MathUtils.equalsIncludingNaN(153.0, 152.99999999999997, 1));
        assertFalse(MathUtils.equalsIncludingNaN(153, 152.99999999999994, 1));

        assertTrue(MathUtils.equalsIncludingNaN(-128.0, -127.99999999999999, 1));
        assertFalse(MathUtils.equalsIncludingNaN(-128.0, -127.99999999999997, 1));
        assertTrue(MathUtils.equalsIncludingNaN(-128.0, -128.00000000000003, 1));
        assertFalse(MathUtils.equalsIncludingNaN(-128.0, -128.00000000000006, 1));

        assertTrue(MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1));
        assertTrue(MathUtils.equalsIncludingNaN(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 1));

        assertTrue(MathUtils.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1));
        assertTrue(MathUtils.equalsIncludingNaN(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY, 1));

        assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 1));

        assertFalse(MathUtils.equalsIncludingNaN(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 100000));
    }

// org.apache.commons.math.util.MathUtilsTest::testArrayEquals
    public void testArrayEquals() {
        assertFalse(MathUtils.equals(new double[] { 1d }, null));
        assertFalse(MathUtils.equals(null, new double[] { 1d }));
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));

        assertFalse(MathUtils.equals(new double[] { 1d }, new double[0]));
        assertTrue(MathUtils.equals(new double[] { 1d }, new double[] { 1d }));
        assertTrue(MathUtils.equals(new double[] {
                                      Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }, new double[] {
                                      Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }));
        assertFalse(MathUtils.equals(new double[] { Double.NaN },
                                     new double[] { Double.NaN }));
        assertFalse(MathUtils.equals(new double[] { Double.POSITIVE_INFINITY },
                                     new double[] { Double.NEGATIVE_INFINITY }));
        assertFalse(MathUtils.equals(new double[] { 1d },
                                     new double[] { FastMath.nextAfter(FastMath.nextAfter(1d, 2d), 2d) }));

    }

// org.apache.commons.math.util.MathUtilsTest::testArrayEqualsIncludingNaN
    public void testArrayEqualsIncludingNaN() {
        assertFalse(MathUtils.equalsIncludingNaN(new double[] { 1d }, null));
        assertFalse(MathUtils.equalsIncludingNaN(null, new double[] { 1d }));
        assertTrue(MathUtils.equalsIncludingNaN((double[]) null, (double[]) null));

        assertFalse(MathUtils.equalsIncludingNaN(new double[] { 1d }, new double[0]));
        assertTrue(MathUtils.equalsIncludingNaN(new double[] { 1d }, new double[] { 1d }));
        assertTrue(MathUtils.equalsIncludingNaN(new double[] {
                    Double.NaN, Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY, 1d, 0d
                }, new double[] {
                    Double.NaN, Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY, 1d, 0d
                }));
        assertFalse(MathUtils.equalsIncludingNaN(new double[] { Double.POSITIVE_INFINITY },
                                                 new double[] { Double.NEGATIVE_INFINITY }));
        assertFalse(MathUtils.equalsIncludingNaN(new double[] { 1d },
                                                 new double[] { FastMath.nextAfter(FastMath.nextAfter(1d, 2d), 2d) }));
    }

// org.apache.commons.math.util.MathUtilsTest::testFactorial
    public void testFactorial() {
        for (int i = 1; i < 21; i++) {
            assertEquals(i + "! ", factorial(i), MathUtils.factorial(i));
            assertEquals(i + "! ", factorial(i), MathUtils.factorialDouble(i), Double.MIN_VALUE);
            assertEquals(i + "! ", FastMath.log(factorial(i)), MathUtils.factorialLog(i), 10E-12);
        }

        assertEquals("0", 1, MathUtils.factorial(0));
        assertEquals("0", 1.0d, MathUtils.factorialDouble(0), 1E-14);
        assertEquals("0", 0.0d, MathUtils.factorialLog(0), 1E-14);
    }

// org.apache.commons.math.util.MathUtilsTest::testFactorialFail
    public void testFactorialFail() {
        try {
            MathUtils.factorial(-1);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MathUtils.factorialDouble(-1);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MathUtils.factorialLog(-1);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MathUtils.factorial(21);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        }
        assertTrue("expecting infinite factorial value", Double.isInfinite(MathUtils.factorialDouble(171)));
    }

// org.apache.commons.math.util.MathUtilsTest::testGcd
    public void testGcd() {
        int a = 30;
        int b = 50;
        int c = 77;

        assertEquals(0, MathUtils.gcd(0, 0));

        assertEquals(b, MathUtils.gcd(0, b));
        assertEquals(a, MathUtils.gcd(a, 0));
        assertEquals(b, MathUtils.gcd(0, -b));
        assertEquals(a, MathUtils.gcd(-a, 0));

        assertEquals(10, MathUtils.gcd(a, b));
        assertEquals(10, MathUtils.gcd(-a, b));
        assertEquals(10, MathUtils.gcd(a, -b));
        assertEquals(10, MathUtils.gcd(-a, -b));

        assertEquals(1, MathUtils.gcd(a, c));
        assertEquals(1, MathUtils.gcd(-a, c));
        assertEquals(1, MathUtils.gcd(a, -c));
        assertEquals(1, MathUtils.gcd(-a, -c));

        assertEquals(3 * (1<<15), MathUtils.gcd(3 * (1<<20), 9 * (1<<15)));

        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(-Integer.MAX_VALUE, 0));
        assertEquals(1<<30, MathUtils.gcd(1<<30, -Integer.MIN_VALUE));
        try {
            
            MathUtils.gcd(Integer.MIN_VALUE, 0);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }
        try {
            
            MathUtils.gcd(0, Integer.MIN_VALUE);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }
        try {
            
            MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testGcdLong
    public void  testGcdLong(){
        long a = 30;
        long b = 50;
        long c = 77;

        assertEquals(0, MathUtils.gcd(0L, 0));

        assertEquals(b, MathUtils.gcd(0, b));
        assertEquals(a, MathUtils.gcd(a, 0));
        assertEquals(b, MathUtils.gcd(0, -b));
        assertEquals(a, MathUtils.gcd(-a, 0));

        assertEquals(10, MathUtils.gcd(a, b));
        assertEquals(10, MathUtils.gcd(-a, b));
        assertEquals(10, MathUtils.gcd(a, -b));
        assertEquals(10, MathUtils.gcd(-a, -b));

        assertEquals(1, MathUtils.gcd(a, c));
        assertEquals(1, MathUtils.gcd(-a, c));
        assertEquals(1, MathUtils.gcd(a, -c));
        assertEquals(1, MathUtils.gcd(-a, -c));

        assertEquals(3L * (1L<<45), MathUtils.gcd(3L * (1L<<50), 9L * (1L<<45)));

        assertEquals(1L<<45, MathUtils.gcd(1L<<45, Long.MIN_VALUE));

        assertEquals(Long.MAX_VALUE, MathUtils.gcd(Long.MAX_VALUE, 0L));
        assertEquals(Long.MAX_VALUE, MathUtils.gcd(-Long.MAX_VALUE, 0L));
        assertEquals(1, MathUtils.gcd(60247241209L, 153092023L));
        try {
            
            MathUtils.gcd(Long.MIN_VALUE, 0);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }
        try {
            
            MathUtils.gcd(0, Long.MIN_VALUE);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }
        try {
            
            MathUtils.gcd(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testGcdConsistency
    public void testGcdConsistency() {
        int[] primeList = {19, 23, 53, 67, 73, 79, 101, 103, 111, 131};
        ArrayList<Integer> primes = new ArrayList<Integer>();
        for (int i = 0; i < primeList.length; i++) {
            primes.add(Integer.valueOf(primeList[i]));
        }
        RandomDataImpl randomData = new RandomDataImpl();
        for (int i = 0; i < 20; i++) {
            Object[] sample = randomData.nextSample(primes, 4);
            int p1 = ((Integer) sample[0]).intValue();
            int p2 = ((Integer) sample[1]).intValue();
            int p3 = ((Integer) sample[2]).intValue();
            int p4 = ((Integer) sample[3]).intValue();
            int i1 = p1 * p2 * p3;
            int i2 = p1 * p2 * p4;
            int gcd = p1 * p2;
            assertEquals(gcd, MathUtils.gcd(i1, i2));
            long l1 = i1;
            long l2 = i2;
            assertEquals(gcd, MathUtils.gcd(l1, l2));
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testHash
    public void testHash() {
        double[] testArray = {
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            1d,
            0d,
            1E-14,
            (1 + 1E-14),
            Double.MIN_VALUE,
            Double.MAX_VALUE };
        for (int i = 0; i < testArray.length; i++) {
            for (int j = 0; j < testArray.length; j++) {
                if (i == j) {
                    assertEquals(MathUtils.hash(testArray[i]), MathUtils.hash(testArray[j]));
                    assertEquals(MathUtils.hash(testArray[j]), MathUtils.hash(testArray[i]));
                } else {
                    assertTrue(MathUtils.hash(testArray[i]) != MathUtils.hash(testArray[j]));
                    assertTrue(MathUtils.hash(testArray[j]) != MathUtils.hash(testArray[i]));
                }
            }
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testArrayHash
    public void testArrayHash() {
        assertEquals(0, MathUtils.hash((double[]) null));
        assertEquals(MathUtils.hash(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }),
                     MathUtils.hash(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }));
        assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { FastMath.nextAfter(1d, 2d) }));
        assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { 1d, 1d }));
    }

// org.apache.commons.math.util.MathUtilsTest::testPermutedArrayHash
    public void testPermutedArrayHash() {
        double[] original = new double[10];
        double[] permuted = new double[10];
        RandomDataImpl random = new RandomDataImpl();

        
        for (int i = 0; i < 10; i++) {
            original[i] = random.nextUniform(i + 0.5, i + 0.75);
        }

        
        boolean isIdentity = true;
        do {
            int[] permutation = random.nextPermutation(10, 10);
            for (int i = 0; i < 10; i++) {
                if (i != permutation[i]) {
                    isIdentity = false;
                }
                permuted[i] = original[permutation[i]];
            }
        } while (isIdentity);

        
        assertFalse(MathUtils.hash(original) == MathUtils.hash(permuted));
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorByte
    public void testIndicatorByte() {
        assertEquals((byte)1, MathUtils.indicator((byte)2));
        assertEquals((byte)1, MathUtils.indicator((byte)0));
        assertEquals((byte)(-1), MathUtils.indicator((byte)(-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorDouble
    public void testIndicatorDouble() {
        double delta = 0.0;
        assertEquals(1.0, MathUtils.indicator(2.0), delta);
        assertEquals(1.0, MathUtils.indicator(0.0), delta);
        assertEquals(-1.0, MathUtils.indicator(-2.0), delta);
        assertEquals(Double.NaN, MathUtils.indicator(Double.NaN));
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorFloat
    public void testIndicatorFloat() {
        float delta = 0.0F;
        assertEquals(1.0F, MathUtils.indicator(2.0F), delta);
        assertEquals(1.0F, MathUtils.indicator(0.0F), delta);
        assertEquals(-1.0F, MathUtils.indicator(-2.0F), delta);
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorInt
    public void testIndicatorInt() {
        assertEquals(1, MathUtils.indicator((2)));
        assertEquals(1, MathUtils.indicator((0)));
        assertEquals((-1), MathUtils.indicator((-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorLong
    public void testIndicatorLong() {
        assertEquals(1L, MathUtils.indicator(2L));
        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals(-1L, MathUtils.indicator(-2L));
    }

// org.apache.commons.math.util.MathUtilsTest::testIndicatorShort
    public void testIndicatorShort() {
        assertEquals((short)1, MathUtils.indicator((short)2));
        assertEquals((short)1, MathUtils.indicator((short)0));
        assertEquals((short)(-1), MathUtils.indicator((short)(-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testLcm
    public void testLcm() {
        int a = 30;
        int b = 50;
        int c = 77;

        assertEquals(0, MathUtils.lcm(0, b));
        assertEquals(0, MathUtils.lcm(a, 0));
        assertEquals(b, MathUtils.lcm(1, b));
        assertEquals(a, MathUtils.lcm(a, 1));
        assertEquals(150, MathUtils.lcm(a, b));
        assertEquals(150, MathUtils.lcm(-a, b));
        assertEquals(150, MathUtils.lcm(a, -b));
        assertEquals(150, MathUtils.lcm(-a, -b));
        assertEquals(2310, MathUtils.lcm(a, c));

        
        
        assertEquals((1<<20)*15, MathUtils.lcm((1<<20)*3, (1<<20)*5));

        
        assertEquals(0, MathUtils.lcm(0, 0));

        try {
            
            MathUtils.lcm(Integer.MIN_VALUE, 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }

        try {
            
            MathUtils.lcm(Integer.MIN_VALUE, 1<<20);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }

        try {
            MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testLcmLong
    public void testLcmLong() {
        long a = 30;
        long b = 50;
        long c = 77;

        assertEquals(0, MathUtils.lcm(0, b));
        assertEquals(0, MathUtils.lcm(a, 0));
        assertEquals(b, MathUtils.lcm(1, b));
        assertEquals(a, MathUtils.lcm(a, 1));
        assertEquals(150, MathUtils.lcm(a, b));
        assertEquals(150, MathUtils.lcm(-a, b));
        assertEquals(150, MathUtils.lcm(a, -b));
        assertEquals(150, MathUtils.lcm(-a, -b));
        assertEquals(2310, MathUtils.lcm(a, c));

        assertEquals(Long.MAX_VALUE, MathUtils.lcm(60247241209L, 153092023L));

        
        
        assertEquals((1L<<50)*15, MathUtils.lcm((1L<<45)*3, (1L<<50)*5));

        
        assertEquals(0L, MathUtils.lcm(0L, 0L));

        try {
            
            MathUtils.lcm(Long.MIN_VALUE, 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }

        try {
            
            MathUtils.lcm(Long.MIN_VALUE, 1<<20);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }

        assertEquals((long) Integer.MAX_VALUE * (Integer.MAX_VALUE - 1),
            MathUtils.lcm((long)Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
        try {
            MathUtils.lcm(Long.MAX_VALUE, Long.MAX_VALUE - 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testLog
    public void testLog() {
        assertEquals(2.0, MathUtils.log(2, 4), 0);
        assertEquals(3.0, MathUtils.log(2, 8), 0);
        assertTrue(Double.isNaN(MathUtils.log(-1, 1)));
        assertTrue(Double.isNaN(MathUtils.log(1, -1)));
        assertTrue(Double.isNaN(MathUtils.log(0, 0)));
        assertEquals(0, MathUtils.log(0, 10), 0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.log(10, 0), 0);
    }

// org.apache.commons.math.util.MathUtilsTest::testMulAndCheck
    public void testMulAndCheck() {
        int big = Integer.MAX_VALUE;
        int bigNeg = Integer.MIN_VALUE;
        assertEquals(big, MathUtils.mulAndCheck(big, 1));
        try {
            MathUtils.mulAndCheck(big, 2);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }
        try {
            MathUtils.mulAndCheck(bigNeg, 2);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testMulAndCheckLong
    public void testMulAndCheckLong() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        assertEquals(max, MathUtils.mulAndCheck(max, 1L));
        assertEquals(min, MathUtils.mulAndCheck(min, 1L));
        assertEquals(0L, MathUtils.mulAndCheck(max, 0L));
        assertEquals(0L, MathUtils.mulAndCheck(min, 0L));
        assertEquals(max, MathUtils.mulAndCheck(1L, max));
        assertEquals(min, MathUtils.mulAndCheck(1L, min));
        assertEquals(0L, MathUtils.mulAndCheck(0L, max));
        assertEquals(0L, MathUtils.mulAndCheck(0L, min));
        assertEquals(1L, MathUtils.mulAndCheck(-1L, -1L));
        assertEquals(min, MathUtils.mulAndCheck(min / 2, 2));
        testMulAndCheckLongFailure(max, 2L);
        testMulAndCheckLongFailure(2L, max);
        testMulAndCheckLongFailure(min, 2L);
        testMulAndCheckLongFailure(2L, min);
        testMulAndCheckLongFailure(min, -1L);
        testMulAndCheckLongFailure(-1L, min);
    }

// org.apache.commons.math.util.MathUtilsTest::testNextAfter
    public void testNextAfter() {
        
        assertEquals(16.0, FastMath.nextAfter(15.999999999999998, 34.27555555555555), 0.0);

        
        assertEquals(-15.999999999999996, FastMath.nextAfter(-15.999999999999998, 34.27555555555555), 0.0);

        
        assertEquals(15.999999999999996, FastMath.nextAfter(15.999999999999998, 2.142222222222222), 0.0);

        
        assertEquals(-15.999999999999996, FastMath.nextAfter(-15.999999999999998, 2.142222222222222), 0.0);

        
        assertEquals(8.000000000000002, FastMath.nextAfter(8.0, 34.27555555555555), 0.0);

        
        assertEquals(-7.999999999999999, FastMath.nextAfter(-8.0, 34.27555555555555), 0.0);

        
        assertEquals(7.999999999999999, FastMath.nextAfter(8.0, 2.142222222222222), 0.0);

        
        assertEquals(-7.999999999999999, FastMath.nextAfter(-8.0, 2.142222222222222), 0.0);

        
        assertEquals(2.308922399667661E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.308922399667661E-4), 0.0);

        
        assertEquals(2.308922399667661E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.3089223996676606E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, 2.3089223996676603E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.308922399667661E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.3089223996676606E-4), 0.0);

        
        assertEquals(2.3089223996676603E-4, FastMath.nextAfter(2.3089223996676606E-4, -2.3089223996676603E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.308922399667661E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.3089223996676606E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, 2.3089223996676603E-4), 0.0);

        
        assertEquals(-2.308922399667661E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.308922399667661E-4), 0.0);

        
        assertEquals(-2.308922399667661E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.3089223996676606E-4), 0.0);

        
        assertEquals(-2.3089223996676603E-4, FastMath.nextAfter(-2.3089223996676606E-4, -2.3089223996676603E-4), 0.0);

    }

// org.apache.commons.math.util.MathUtilsTest::testNextAfterSpecialCases
    public void testNextAfterSpecialCases() {
        assertTrue(Double.isInfinite(FastMath.nextAfter(Double.NEGATIVE_INFINITY, 0)));
        assertTrue(Double.isInfinite(FastMath.nextAfter(Double.POSITIVE_INFINITY, 0)));
        assertTrue(Double.isNaN(FastMath.nextAfter(Double.NaN, 0)));
        assertTrue(Double.isInfinite(FastMath.nextAfter(Double.MAX_VALUE, Double.POSITIVE_INFINITY)));
        assertTrue(Double.isInfinite(FastMath.nextAfter(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY)));
        assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0, 1), 0);
        assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0, -1), 0);
        assertEquals(0, FastMath.nextAfter(Double.MIN_VALUE, -1), 0);
        assertEquals(0, FastMath.nextAfter(-Double.MIN_VALUE, 1), 0);
    }

// org.apache.commons.math.util.MathUtilsTest::testScalb
    public void testScalb() {
        assertEquals( 0.0, MathUtils.scalb(0.0, 5), 1.0e-15);
        assertEquals(32.0, MathUtils.scalb(1.0, 5), 1.0e-15);
        assertEquals(1.0 / 32.0, MathUtils.scalb(1.0,  -5), 1.0e-15);
        assertEquals(FastMath.PI, MathUtils.scalb(FastMath.PI, 0), 1.0e-15);
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.POSITIVE_INFINITY, 1)));
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.NEGATIVE_INFINITY, 1)));
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 1)));
    }

// org.apache.commons.math.util.MathUtilsTest::testNormalizeAngle
    public void testNormalizeAngle() {
        for (double a = -15.0; a <= 15.0; a += 0.1) {
            for (double b = -15.0; b <= 15.0; b += 0.2) {
                double c = MathUtils.normalizeAngle(a, b);
                assertTrue((b - FastMath.PI) <= c);
                assertTrue(c <= (b + FastMath.PI));
                double twoK = FastMath.rint((a - c) / FastMath.PI);
                assertEquals(c, a - twoK * FastMath.PI, 1.0e-14);
            }
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testNormalizeArray
    public void testNormalizeArray() {
        double[] testValues1 = new double[] {1, 1, 2};
        TestUtils.assertEquals(
                new double[] {.25, .25, .5},
                MathUtils.normalizeArray(testValues1, 1),
                Double.MIN_VALUE);

        double[] testValues2 = new double[] {-1, -1, 1};
        TestUtils.assertEquals(
                new double[] {1, 1, -1},
                MathUtils.normalizeArray(testValues2, 1),
                Double.MIN_VALUE);

        
        double[] testValues3 = new double[] {-1, -1, Double.NaN, 1, Double.NaN};
        TestUtils.assertEquals(
                new double[] {1, 1,Double.NaN, -1, Double.NaN},
                MathUtils.normalizeArray(testValues3, 1),
                Double.MIN_VALUE);

        
        double[] zeroSum = new double[] {-1, 1};
        try {
            MathUtils.normalizeArray(zeroSum, 1);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}

        
        double[] hasInf = new double[] {1, 2, 1, Double.NEGATIVE_INFINITY};
        try {
            MathUtils.normalizeArray(hasInf, 1);
            fail("expecting ArithmeticException");
        } catch (ArithmeticException ex) {}

        
        try {
            MathUtils.normalizeArray(testValues1, Double.POSITIVE_INFINITY);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}

        
        try {
            MathUtils.normalizeArray(testValues1, Double.NaN);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}

    }

// org.apache.commons.math.util.MathUtilsTest::testRoundDouble
    public void testRoundDouble() {
        double x = 1.234567890;
        assertEquals(1.23, MathUtils.round(x, 2), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4), 0.0);

        
        assertEquals(39.25, MathUtils.round(39.245, 2), 0.0);
        assertEquals(39.24, MathUtils.round(39.245, 2, BigDecimal.ROUND_DOWN), 0.0);
        double xx = 39.0;
        xx = xx + 245d / 1000d;
        assertEquals(39.25, MathUtils.round(xx, 2), 0.0);

        
        assertEquals(30.1d, MathUtils.round(30.095d, 2), 0.0d);
        assertEquals(30.1d, MathUtils.round(30.095d, 1), 0.0d);
        assertEquals(33.1d, MathUtils.round(33.095d, 1), 0.0d);
        assertEquals(33.1d, MathUtils.round(33.095d, 2), 0.0d);
        assertEquals(50.09d, MathUtils.round(50.085d, 2), 0.0d);
        assertEquals(50.19d, MathUtils.round(50.185d, 2), 0.0d);
        assertEquals(50.01d, MathUtils.round(50.005d, 2), 0.0d);
        assertEquals(30.01d, MathUtils.round(30.005d, 2), 0.0d);
        assertEquals(30.65d, MathUtils.round(30.645d, 2), 0.0d);

        assertEquals(1.24, MathUtils.round(x, 2, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.23, MathUtils.round(-x, 2, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.234, MathUtils.round(-x, 3, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.2345, MathUtils.round(-x, 4, BigDecimal.ROUND_CEILING), 0.0);

        assertEquals(1.23, MathUtils.round(x, 2, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.234, MathUtils.round(x, 3, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.2345, MathUtils.round(x, 4, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.23, MathUtils.round(-x, 2, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.234, MathUtils.round(-x, 3, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.2345, MathUtils.round(-x, 4, BigDecimal.ROUND_DOWN), 0.0);

        assertEquals(1.23, MathUtils.round(x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(1.234, MathUtils.round(x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(1.2345, MathUtils.round(x, 4, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.24, MathUtils.round(-x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.235, MathUtils.round(-x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.2346, MathUtils.round(-x, 4, BigDecimal.ROUND_FLOOR), 0.0);

        assertEquals(1.23, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.23, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.235, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.2346, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.234, MathUtils.round(1.2345, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.234, MathUtils.round(-1.2345, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);

        assertEquals(1.23, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.23, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.235, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.2346, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.234, MathUtils.round(1.2345, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.234, MathUtils.round(-1.2345, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.236, MathUtils.round(1.2355, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.236, MathUtils.round(-1.2355, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);

        assertEquals(1.23, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.23, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.235, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.2346, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.235, MathUtils.round(1.2345, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.235, MathUtils.round(-1.2345, 3, BigDecimal.ROUND_HALF_UP), 0.0);

        assertEquals(-1.23, MathUtils.round(-1.23, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);
        assertEquals(1.23, MathUtils.round(1.23, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);

        try {
            MathUtils.round(1.234, 2, BigDecimal.ROUND_UNNECESSARY);
            fail();
        } catch (ArithmeticException ex) {
            
        }

        assertEquals(1.24, MathUtils.round(x, 2, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.235, MathUtils.round(x, 3, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.2346, MathUtils.round(x, 4, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.24, MathUtils.round(-x, 2, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.235, MathUtils.round(-x, 3, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.2346, MathUtils.round(-x, 4, BigDecimal.ROUND_UP), 0.0);

        try {
            MathUtils.round(1.234, 2, 1923);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }

        
        assertEquals(39.25, MathUtils.round(39.245, 2, BigDecimal.ROUND_HALF_UP), 0.0);

        
        TestUtils.assertEquals(Double.NaN, MathUtils.round(Double.NaN, 2), 0.0);
        assertEquals(0.0, MathUtils.round(0.0, 2), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 2), 0.0);
    }

// org.apache.commons.math.util.MathUtilsTest::testRoundFloat
    public void testRoundFloat() {
        float x = 1.234567890f;
        assertEquals(1.23f, MathUtils.round(x, 2), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4), 0.0);

        
        assertEquals(30.1f, MathUtils.round(30.095f, 2), 0.0f);
        assertEquals(30.1f, MathUtils.round(30.095f, 1), 0.0f);
        assertEquals(50.09f, MathUtils.round(50.085f, 2), 0.0f);
        assertEquals(50.19f, MathUtils.round(50.185f, 2), 0.0f);
        assertEquals(50.01f, MathUtils.round(50.005f, 2), 0.0f);
        assertEquals(30.01f, MathUtils.round(30.005f, 2), 0.0f);
        assertEquals(30.65f, MathUtils.round(30.645f, 2), 0.0f);

        assertEquals(1.24f, MathUtils.round(x, 2, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.23f, MathUtils.round(-x, 2, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.234f, MathUtils.round(-x, 3, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(-1.2345f, MathUtils.round(-x, 4, BigDecimal.ROUND_CEILING), 0.0);

        assertEquals(1.23f, MathUtils.round(x, 2, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.234f, MathUtils.round(x, 3, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.2345f, MathUtils.round(x, 4, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.23f, MathUtils.round(-x, 2, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.234f, MathUtils.round(-x, 3, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(-1.2345f, MathUtils.round(-x, 4, BigDecimal.ROUND_DOWN), 0.0);

        assertEquals(1.23f, MathUtils.round(x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(1.234f, MathUtils.round(x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(1.2345f, MathUtils.round(x, 4, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.24f, MathUtils.round(-x, 2, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.235f, MathUtils.round(-x, 3, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(-1.2346f, MathUtils.round(-x, 4, BigDecimal.ROUND_FLOOR), 0.0);

        assertEquals(1.23f, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.23f, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.235f, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.2346f, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(1.234f, MathUtils.round(1.2345f, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(-1.234f, MathUtils.round(-1.2345f, 3, BigDecimal.ROUND_HALF_DOWN), 0.0);

        assertEquals(1.23f, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.23f, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.235f, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.2346f, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.234f, MathUtils.round(1.2345f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.234f, MathUtils.round(-1.2345f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.236f, MathUtils.round(1.2355f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(-1.236f, MathUtils.round(-1.2355f, 3, BigDecimal.ROUND_HALF_EVEN), 0.0);

        assertEquals(1.23f, MathUtils.round(x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.23f, MathUtils.round(-x, 2, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.235f, MathUtils.round(-x, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.2346f, MathUtils.round(-x, 4, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.235f, MathUtils.round(1.2345f, 3, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(-1.235f, MathUtils.round(-1.2345f, 3, BigDecimal.ROUND_HALF_UP), 0.0);

        assertEquals(-1.23f, MathUtils.round(-1.23f, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);
        assertEquals(1.23f, MathUtils.round(1.23f, 2, BigDecimal.ROUND_UNNECESSARY), 0.0);

        try {
            MathUtils.round(1.234f, 2, BigDecimal.ROUND_UNNECESSARY);
            fail();
        } catch (ArithmeticException ex) {
            
        }

        assertEquals(1.24f, MathUtils.round(x, 2, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.235f, MathUtils.round(x, 3, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.2346f, MathUtils.round(x, 4, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.24f, MathUtils.round(-x, 2, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.235f, MathUtils.round(-x, 3, BigDecimal.ROUND_UP), 0.0);
        assertEquals(-1.2346f, MathUtils.round(-x, 4, BigDecimal.ROUND_UP), 0.0);

        try {
            MathUtils.round(1.234f, 2, 1923);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }

        
        TestUtils.assertEquals(Float.NaN, MathUtils.round(Float.NaN, 2), 0.0f);
        assertEquals(0.0f, MathUtils.round(0.0f, 2), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.round(Float.POSITIVE_INFINITY, 2), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, MathUtils.round(Float.NEGATIVE_INFINITY, 2), 0.0f);
    }

// org.apache.commons.math.util.MathUtilsTest::testSignByte
    public void testSignByte() {
        assertEquals((byte) 1, MathUtils.sign((byte) 2));
        assertEquals((byte) 0, MathUtils.sign((byte) 0));
        assertEquals((byte) (-1), MathUtils.sign((byte) (-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignDouble
    public void testSignDouble() {
        double delta = 0.0;
        assertEquals(1.0, MathUtils.sign(2.0), delta);
        assertEquals(0.0, MathUtils.sign(0.0), delta);
        assertEquals(-1.0, MathUtils.sign(-2.0), delta);
        TestUtils.assertSame(-0. / 0., MathUtils.sign(Double.NaN));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignFloat
    public void testSignFloat() {
        float delta = 0.0F;
        assertEquals(1.0F, MathUtils.sign(2.0F), delta);
        assertEquals(0.0F, MathUtils.sign(0.0F), delta);
        assertEquals(-1.0F, MathUtils.sign(-2.0F), delta);
        TestUtils.assertSame(Float.NaN, MathUtils.sign(Float.NaN));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignInt
    public void testSignInt() {
        assertEquals(1, MathUtils.sign(2));
        assertEquals(0, MathUtils.sign(0));
        assertEquals((-1), MathUtils.sign((-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignLong
    public void testSignLong() {
        assertEquals(1L, MathUtils.sign(2L));
        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(-1L, MathUtils.sign(-2L));
    }

// org.apache.commons.math.util.MathUtilsTest::testSignShort
    public void testSignShort() {
        assertEquals((short) 1, MathUtils.sign((short) 2));
        assertEquals((short) 0, MathUtils.sign((short) 0));
        assertEquals((short) (-1), MathUtils.sign((short) (-2)));
    }

// org.apache.commons.math.util.MathUtilsTest::testSinh
    public void testSinh() {
        double x = 3.0;
        double expected = 10.01787;
        assertEquals(expected, MathUtils.sinh(x), 1.0e-5);
    }

// org.apache.commons.math.util.MathUtilsTest::testSinhNaN
    public void testSinhNaN() {
        assertTrue(Double.isNaN(MathUtils.sinh(Double.NaN)));
    }

// org.apache.commons.math.util.MathUtilsTest::testSubAndCheck
    public void testSubAndCheck() {
        int big = Integer.MAX_VALUE;
        int bigNeg = Integer.MIN_VALUE;
        assertEquals(big, MathUtils.subAndCheck(big, 0));
        assertEquals(bigNeg + 1, MathUtils.subAndCheck(bigNeg, -1));
        assertEquals(-1, MathUtils.subAndCheck(bigNeg, -big));
        try {
            MathUtils.subAndCheck(big, -1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }
        try {
            MathUtils.subAndCheck(bigNeg, 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testSubAndCheckErrorMessage
    public void testSubAndCheckErrorMessage() {
        int big = Integer.MAX_VALUE;
        try {
            MathUtils.subAndCheck(big, -1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            assertTrue(ex.getMessage().length() > 1);
        }
    }

// org.apache.commons.math.util.MathUtilsTest::testSubAndCheckLong
    public void testSubAndCheckLong() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        assertEquals(max, MathUtils.subAndCheck(max, 0));
        assertEquals(min, MathUtils.subAndCheck(min, 0));
        assertEquals(-max, MathUtils.subAndCheck(0, max));
        assertEquals(min + 1, MathUtils.subAndCheck(min, -1));
        
        assertEquals(-1, MathUtils.subAndCheck(-max - 1, -max));
        assertEquals(max, MathUtils.subAndCheck(-1, -1 - max));
        testSubAndCheckLongFailure(0L, min);
        testSubAndCheckLongFailure(max, -1L);
        testSubAndCheckLongFailure(min, 1L);
    }

// org.apache.commons.math.util.MathUtilsTest::testPow
    public void testPow() {

        assertEquals(1801088541, MathUtils.pow(21, 7));
        assertEquals(1, MathUtils.pow(21, 0));
        try {
            MathUtils.pow(21, -7);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        assertEquals(1801088541, MathUtils.pow(21, 7l));
        assertEquals(1, MathUtils.pow(21, 0l));
        try {
            MathUtils.pow(21, -7l);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        assertEquals(1801088541l, MathUtils.pow(21l, 7));
        assertEquals(1l, MathUtils.pow(21l, 0));
        try {
            MathUtils.pow(21l, -7);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        assertEquals(1801088541l, MathUtils.pow(21l, 7l));
        assertEquals(1l, MathUtils.pow(21l, 0l));
        try {
            MathUtils.pow(21l, -7l);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        BigInteger twentyOne = BigInteger.valueOf(21l);
        assertEquals(BigInteger.valueOf(1801088541l), MathUtils.pow(twentyOne, 7));
        assertEquals(BigInteger.ONE, MathUtils.pow(twentyOne, 0));
        try {
            MathUtils.pow(twentyOne, -7);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        assertEquals(BigInteger.valueOf(1801088541l), MathUtils.pow(twentyOne, 7l));
        assertEquals(BigInteger.ONE, MathUtils.pow(twentyOne, 0l));
        try {
            MathUtils.pow(twentyOne, -7l);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        assertEquals(BigInteger.valueOf(1801088541l), MathUtils.pow(twentyOne, BigInteger.valueOf(7l)));
        assertEquals(BigInteger.ONE, MathUtils.pow(twentyOne, BigInteger.ZERO));
        try {
            MathUtils.pow(twentyOne, BigInteger.valueOf(-7l));
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        BigInteger bigOne =
            new BigInteger("1543786922199448028351389769265814882661837148" +
                           "4763915343722775611762713982220306372888519211" +
                           "560905579993523402015636025177602059044911261");
        assertEquals(bigOne, MathUtils.pow(twentyOne, 103));
        assertEquals(bigOne, MathUtils.pow(twentyOne, 103l));
        assertEquals(bigOne, MathUtils.pow(twentyOne, BigInteger.valueOf(103l)));

    }

// org.apache.commons.math.util.MathUtilsTest::testL1DistanceDouble
    public void testL1DistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        assertEquals(7.0, MathUtils.distance1(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testL1DistanceInt
    public void testL1DistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        assertEquals(7, MathUtils.distance1(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testL2DistanceDouble
    public void testL2DistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        assertEquals(5.0, MathUtils.distance(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testL2DistanceInt
    public void testL2DistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        assertEquals(5.0, MathUtils.distance(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testLInfDistanceDouble
    public void testLInfDistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        assertEquals(4.0, MathUtils.distanceInf(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testLInfDistanceInt
    public void testLInfDistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        assertEquals(4, MathUtils.distanceInf(p1, p2));
    }

// org.apache.commons.math.util.MathUtilsTest::testCheckOrder
    public void testCheckOrder() {
        MathUtils.checkOrder(new double[] {-15, -5.5, -1, 2, 15},
                             MathUtils.OrderDirection.INCREASING, true);
        MathUtils.checkOrder(new double[] {-15, -5.5, -1, 2, 2},
                             MathUtils.OrderDirection.INCREASING, false);
        MathUtils.checkOrder(new double[] {3, -5.5, -11, -27.5},
                             MathUtils.OrderDirection.DECREASING, true);
        MathUtils.checkOrder(new double[] {3, 0, 0, -5.5, -11, -27.5},
                             MathUtils.OrderDirection.DECREASING, false);

        try {
            MathUtils.checkOrder(new double[] {-15, -5.5, -1, -1, 2, 15},
                                 MathUtils.OrderDirection.INCREASING, true);
            fail("an exception should have been thrown");
        } catch (NonMonotonousSequenceException e) {
            
        }
        try {
            MathUtils.checkOrder(new double[] {-15, -5.5, -1, -2, 2},
                                 MathUtils.OrderDirection.INCREASING, false);
            fail("an exception should have been thrown");
        } catch (NonMonotonousSequenceException e) {
            
        }
        try {
            MathUtils.checkOrder(new double[] {3, 3, -5.5, -11, -27.5},
                                 MathUtils.OrderDirection.DECREASING, true);
            fail("an exception should have been thrown");
        } catch (NonMonotonousSequenceException e) {
            
        }
        try {
            MathUtils.checkOrder(new double[] {3, -1, 0, -5.5, -11, -27.5},
                                 MathUtils.OrderDirection.DECREASING, false);
            fail("an exception should have been thrown");
        } catch (NonMonotonousSequenceException e) {
            
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutAndGetWith0ExpectedSize
    public void testPutAndGetWith0ExpectedSize() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field,0);
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutAndGetWithExpectedSize
    public void testPutAndGetWithExpectedSize() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field,500);
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutAndGet
    public void testPutAndGet() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutAbsentOnExisting
    public void testPutAbsentOnExisting() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int size = javaMap.size();
        for (Map.Entry<Integer, Fraction> mapEntry : generateAbsent().entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            assertEquals(++size, map.size());
            assertEquals(mapEntry.getValue(), map.get(mapEntry.getKey()));
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutOnExisting
    public void testPutOnExisting() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            assertEquals(javaMap.size(), map.size());
            assertEquals(mapEntry.getValue(), map.get(mapEntry.getKey()));
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testGetAbsent
    public void testGetAbsent() {
        Map<Integer, Fraction> generated = generateAbsent();
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);

        for (Map.Entry<Integer, Fraction> mapEntry : generated.entrySet())
            assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testGetFromEmpty
    public void testGetFromEmpty() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        assertTrue(field.getZero().equals(map.get(5)));
        assertTrue(field.getZero().equals(map.get(0)));
        assertTrue(field.getZero().equals(map.get(50)));
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testRemove
    public void testRemove() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = javaMap.size();
        assertEquals(mapSize, map.size());
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            map.remove(mapEntry.getKey());
            assertEquals(--mapSize, map.size());
            assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
        }

        
        assertPutAndGet(map);
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testRemove2
    public void testRemove2() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = javaMap.size();
        int count = 0;
        Set<Integer> keysInMap = new HashSet<Integer>(javaMap.keySet());
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            keysInMap.remove(mapEntry.getKey());
            map.remove(mapEntry.getKey());
            assertEquals(--mapSize, map.size());
            assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
            if (count++ > 5)
                break;
        }

        
        assertPutAndGet(map, mapSize, keysInMap);
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testRemoveFromEmpty
    public void testRemoveFromEmpty() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        assertTrue(field.getZero().equals(map.remove(50)));
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testRemoveAbsent
    public void testRemoveAbsent() {
        Map<Integer, Fraction> generated = generateAbsent();

        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = map.size();

        for (Map.Entry<Integer, Fraction> mapEntry : generated.entrySet()) {
            map.remove(mapEntry.getKey());
            assertEquals(mapSize, map.size());
            assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testCopy
    public void testCopy() {
        OpenIntToFieldHashMap<Fraction> copy =
            new OpenIntToFieldHashMap<Fraction>(createFromJavaMap(field));
        assertEquals(javaMap.size(), copy.size());

        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet())
            assertEquals(mapEntry.getValue(), copy.get(mapEntry.getKey()));
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testContainsKey
    public void testContainsKey() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        for (Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            assertTrue(map.containsKey(mapEntry.getKey()));
        }
        for (Map.Entry<Integer, Fraction> mapEntry : generateAbsent().entrySet()) {
            assertFalse(map.containsKey(mapEntry.getKey()));
        }
        for (Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            int key = mapEntry.getKey();
            assertTrue(map.containsKey(key));
            map.remove(key);
            assertFalse(map.containsKey(key));
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testIterator
    public void testIterator() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        OpenIntToFieldHashMap<Fraction>.Iterator iterator = map.iterator();
        for (int i = 0; i < map.size(); ++i) {
            assertTrue(iterator.hasNext());
            iterator.advance();
            int key = iterator.key();
            assertTrue(map.containsKey(key));
            assertEquals(javaMap.get(key), map.get(key));
            assertEquals(javaMap.get(key), iterator.value());
            assertTrue(javaMap.containsKey(key));
        }
        assertFalse(iterator.hasNext());
        try {
            iterator.advance();
            fail("an exception should have been thrown");
        } catch (NoSuchElementException nsee) {
            
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testConcurrentModification
    public void testConcurrentModification() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        OpenIntToFieldHashMap<Fraction>.Iterator iterator = map.iterator();
        map.put(3, new Fraction(3));
        try {
            iterator.advance();
            fail("an exception should have been thrown");
        } catch (ConcurrentModificationException cme) {
            
        }
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutKeysWithCollisions
    public void testPutKeysWithCollisions() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        int key1 = -1996012590;
        Fraction value1 = new Fraction(1);
        map.put(key1, value1);
        int key2 = 835099822;
        map.put(key2, value1);
        int key3 = 1008859686;
        map.put(key3, value1);
        assertEquals(value1, map.get(key3));
        assertEquals(3, map.size());

        map.remove(key2);
        Fraction value2 = new Fraction(2);
        map.put(key3, value2);
        assertEquals(value2, map.get(key3));
        assertEquals(2, map.size());
    }

// org.apache.commons.math.util.OpenIntToFieldTest::testPutKeysWithCollision2
    public void testPutKeysWithCollision2() {
        OpenIntToFieldHashMap<Fraction>map = new OpenIntToFieldHashMap<Fraction>(field);
        int key1 = 837989881;
        Fraction value1 = new Fraction(1);
        map.put(key1, value1);
        int key2 = 476463321;
        map.put(key2, value1);
        assertEquals(2, map.size());
        assertEquals(value1, map.get(key2));

        map.remove(key1);
        Fraction value2 = new Fraction(2);
        map.put(key2, value2);
        assertEquals(1, map.size());
        assertEquals(value2, map.get(key2));
    }
