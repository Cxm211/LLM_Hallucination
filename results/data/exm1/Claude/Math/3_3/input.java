// buggy code
    public static double linearCombination(final double[] a, final double[] b)
        throws DimensionMismatchException {
        final int len = a.length;
        if (len != b.length) {
            throw new DimensionMismatchException(len, b.length);
        }

            // Revert to scalar multiplication.

        final double[] prodHigh = new double[len];
        double prodLowSum = 0;

        for (int i = 0; i < len; i++) {
            final double ai = a[i];
            final double ca = SPLIT_FACTOR * ai;
            final double aHigh = ca - (ca - ai);
            final double aLow = ai - aHigh;

            final double bi = b[i];
            final double cb = SPLIT_FACTOR * bi;
            final double bHigh = cb - (cb - bi);
            final double bLow = bi - bHigh;
            prodHigh[i] = ai * bi;
            final double prodLow = aLow * bLow - (((prodHigh[i] -
                                                    aHigh * bHigh) -
                                                   aLow * bHigh) -
                                                  aHigh * bLow);
            prodLowSum += prodLow;
        }


        final double prodHighCur = prodHigh[0];
        double prodHighNext = prodHigh[1];
        double sHighPrev = prodHighCur + prodHighNext;
        double sPrime = sHighPrev - prodHighNext;
        double sLowSum = (prodHighNext - (sHighPrev - sPrime)) + (prodHighCur - sPrime);

        final int lenMinusOne = len - 1;
        for (int i = 1; i < lenMinusOne; i++) {
            prodHighNext = prodHigh[i + 1];
            final double sHighCur = sHighPrev + prodHighNext;
            sPrime = sHighCur - prodHighNext;
            sLowSum += (prodHighNext - (sHighCur - sPrime)) + (sHighPrev - sPrime);
            sHighPrev = sHighCur;
        }

        double result = sHighPrev + (prodLowSum + sLowSum);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = 0;
            for (int i = 0; i < len; ++i) {
                result += a[i] * b[i];
            }
        }

        return result;
    }

// relevant test
// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testCircleFitting2
    public void testCircleFitting2() {
        final double xCenter = 123.456;
        final double yCenter = 654.321;
        final double xSigma = 10;
        final double ySigma = 15;
        final double radius = 111.111;
        
        final long seed = 59421061L;
        final RandomCirclePointGenerator factory
            = new RandomCirclePointGenerator(xCenter, yCenter, radius,
                                             xSigma, ySigma,
                                             seed);
        final CircleProblem circle = new CircleProblem(xSigma, ySigma);

        final int numPoints = 10;
        for (Vector2D p : factory.generate(numPoints)) {
            circle.addPoint(p);
            
        }

        
        final double[] init = { 90, 659, 115 };

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();
        final PointVectorValuePair optimum = optimizer.optimize(100, circle,
                                                                circle.target(), circle.weight(),
                                                                init);

        final double[] paramFound = optimum.getPoint();

        
        final double[][] covMatrix = optimizer.computeCovariances(paramFound, 1e-14);
        final double[] asymptoticStandardErrorFound = optimizer.guessParametersErrors();
        final double[] sigmaFound = new double[covMatrix.length];
        for (int i = 0; i < covMatrix.length; i++) {
            sigmaFound[i] = FastMath.sqrt(covMatrix[i][i]);

        }

        

        
        Assert.assertEquals(xCenter, paramFound[0], asymptoticStandardErrorFound[0]);
        Assert.assertEquals(yCenter, paramFound[1], asymptoticStandardErrorFound[1]);
        Assert.assertEquals(radius, paramFound[2], asymptoticStandardErrorFound[2]);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testSinMin
    public void testSinMin() {
        UnivariateFunction f = new Sin();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(3 * Math.PI / 2, optimizer.optimize(200, f, GoalType.MINIMIZE, 4, 5).getPoint(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        Assert.assertEquals(3 * Math.PI / 2, optimizer.optimize(200, f, GoalType.MINIMIZE, 1, 5).getPoint(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 100);
        Assert.assertTrue(optimizer.getEvaluations() >= 15);
        try {
            optimizer.optimize(10, f, GoalType.MINIMIZE, 4, 5);
            Assert.fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException fee) {
            
        }
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testSinMinWithValueChecker
    public void testSinMinWithValueChecker() {
        final UnivariateFunction f = new Sin();
        final ConvergenceChecker<UnivariatePointValuePair> checker = new SimpleUnivariateValueChecker(1e-5, 1e-14);
        
        
        
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        final UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, 4, 5);
        Assert.assertEquals(3 * Math.PI / 2, result.getPoint(), 1e-3);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testBoundaries
    public void testBoundaries() {
        final double lower = -1.0;
        final double upper = +1.0;
        UnivariateFunction f = new UnivariateFunction() {            
            public double value(double x) {
                if (x < lower) {
                    throw new NumberIsTooSmallException(x, lower, true);
                } else if (x > upper) {
                    throw new NumberIsTooLargeException(x, upper, true);
                } else {
                    return x;
                }
            }
        };
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(lower,
                            optimizer.optimize(100, f, GoalType.MINIMIZE, lower, upper).getPoint(),
                            1.0e-8);
        Assert.assertEquals(upper,
                            optimizer.optimize(100, f, GoalType.MAXIMIZE, lower, upper).getPoint(),
                            1.0e-8);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testQuinticMin
    public void testQuinticMin() {
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(-0.27195613, optimizer.optimize(200, f, GoalType.MINIMIZE, -0.3, -0.2).getPoint(), 1.0e-8);
        Assert.assertEquals( 0.82221643, optimizer.optimize(200, f, GoalType.MINIMIZE,  0.3,  0.9).getPoint(), 1.0e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);

        
        Assert.assertEquals(-0.27195613, optimizer.optimize(200, f, GoalType.MINIMIZE, -1.0, 0.2).getPoint(), 1.0e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testQuinticMinStatistics
    public void testQuinticMinStatistics() {
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);

        final DescriptiveStatistics[] stat = new DescriptiveStatistics[2];
        for (int i = 0; i < stat.length; i++) {
            stat[i] = new DescriptiveStatistics();
        }

        final double min = -0.75;
        final double max = 0.25;
        final int nSamples = 200;
        final double delta = (max - min) / nSamples;
        for (int i = 0; i < nSamples; i++) {
            final double start = min + i * delta;
            stat[0].addValue(optimizer.optimize(40, f, GoalType.MINIMIZE, min, max, start).getPoint());
            stat[1].addValue(optimizer.getEvaluations());
        }

        final double meanOptValue = stat[0].getMean();
        final double medianEval = stat[1].getPercentile(50);
        Assert.assertTrue(meanOptValue > -0.2719561281);
        Assert.assertTrue(meanOptValue < -0.2719561280);
        Assert.assertEquals(23, (int) medianEval);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testQuinticMax
    public void testQuinticMax() {
        
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-12, 1e-14);
        Assert.assertEquals(0.27195613, optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.2, 0.3).getPoint(), 1e-8);
        try {
            optimizer.optimize(5, f, GoalType.MAXIMIZE, 0.2, 0.3);
            Assert.fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException miee) {
            
        }
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testMinEndpoints
    public void testMinEndpoints() {
        UnivariateFunction f = new Sin();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-8, 1e-14);

        
        double result = optimizer.optimize(50, f, GoalType.MINIMIZE, 3 * Math.PI / 2, 5).getPoint();
        Assert.assertEquals(3 * Math.PI / 2, result, 1e-6);

        result = optimizer.optimize(50, f, GoalType.MINIMIZE, 4, 3 * Math.PI / 2).getPoint();
        Assert.assertEquals(3 * Math.PI / 2, result, 1e-6);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testMath832
    public void testMath832() {
        final UnivariateFunction f = new UnivariateFunction() {
                public double value(double x) {
                    final double sqrtX = FastMath.sqrt(x);
                    final double a = 1e2 * sqrtX;
                    final double b = 1e6 / x;
                    final double c = 1e4 / sqrtX;

                    return a + b + c;
                }
            };

        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-8);
        final double result = optimizer.optimize(1483,
                                                 f,
                                                 GoalType.MINIMIZE,
                                                 Double.MIN_VALUE,
                                                 Double.MAX_VALUE).getPoint();

        Assert.assertEquals(804.9355825, result, 1e-6);
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testKeepInitIfBest
    public void testKeepInitIfBest() {
        final double minSin = 3 * Math.PI / 2;
        final double offset = 1e-8;
        final double delta = 1e-7;
        final UnivariateFunction f1 = new Sin();
        final UnivariateFunction f2 = new StepFunction(new double[] { minSin, minSin + offset, minSin + 2 * offset},
                                                       new double[] { 0, -1, 0 });
        final UnivariateFunction f = FunctionUtils.add(f1, f2);
        
        
        final double relTol = 1e-8;
        final UnivariateOptimizer optimizer = new BrentOptimizer(relTol, 1e-100);
        final double init = minSin + 1.5 * offset;
        final UnivariatePointValuePair result
            = optimizer.optimize(200, f, GoalType.MINIMIZE,
                                 minSin - 6.789 * delta,
                                 minSin + 9.876 * delta,
                                 init);
        final int numEval = optimizer.getEvaluations();

        final double sol = result.getPoint();
        final double expected = init;

        Assert.assertTrue("Best point not reported", f.value(sol) <= f.value(expected));
    }

// org.apache.commons.math3.optimization.univariate.BrentOptimizerTest::testMath855
    public void testMath855() {
        final double minSin = 3 * Math.PI / 2;
        final double offset = 1e-8;
        final double delta = 1e-7;
        final UnivariateFunction f1 = new Sin();
        final UnivariateFunction f2 = new StepFunction(new double[] { minSin, minSin + offset, minSin + 5 * offset },
                                                       new double[] { 0, -1, 0 });
        final UnivariateFunction f = FunctionUtils.add(f1, f2);
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-8, 1e-100);
        final UnivariatePointValuePair result
            = optimizer.optimize(200, f, GoalType.MINIMIZE,
                                 minSin - 6.789 * delta,
                                 minSin + 9.876 * delta);
        final int numEval = optimizer.getEvaluations();

        final double sol = result.getPoint();
        final double expected = 4.712389027602411;

        
        
        

        Assert.assertTrue("Best point not reported", f.value(sol) <= f.value(expected));
    }

// org.apache.commons.math3.stat.CertifiedDataTest::testSummaryStatistics
    public void testSummaryStatistics() throws Exception {
        SummaryStatistics u = new SummaryStatistics();
        loadStats("data/PiDigits.txt", u);
        Assert.assertEquals("PiDigits: std", std, u.getStandardDeviation(), 1E-13);
        Assert.assertEquals("PiDigits: mean", mean, u.getMean(), 1E-13);

        loadStats("data/Mavro.txt", u);
        Assert.assertEquals("Mavro: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("Mavro: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Michelso.txt", u);
        Assert.assertEquals("Michelso: std", std, u.getStandardDeviation(), 1E-13);
        Assert.assertEquals("Michelso: mean", mean, u.getMean(), 1E-13);

        loadStats("data/NumAcc1.txt", u);
        Assert.assertEquals("NumAcc1: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc1: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc2.txt", u);
        Assert.assertEquals("NumAcc2: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc2: mean", mean, u.getMean(), 1E-14);
    }

// org.apache.commons.math3.stat.CertifiedDataTest::testDescriptiveStatistics
    public void testDescriptiveStatistics() throws Exception {

        DescriptiveStatistics u = new DescriptiveStatistics();

        loadStats("data/PiDigits.txt", u);
        Assert.assertEquals("PiDigits: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("PiDigits: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Mavro.txt", u);
        Assert.assertEquals("Mavro: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("Mavro: mean", mean, u.getMean(), 1E-14);

        loadStats("data/Michelso.txt", u);
        Assert.assertEquals("Michelso: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("Michelso: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc1.txt", u);
        Assert.assertEquals("NumAcc1: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc1: mean", mean, u.getMean(), 1E-14);

        loadStats("data/NumAcc2.txt", u);
        Assert.assertEquals("NumAcc2: std", std, u.getStandardDeviation(), 1E-14);
        Assert.assertEquals("NumAcc2: mean", mean, u.getMean(), 1E-14);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testStats
    public void testStats() {
        double[] values = new double[] { ONE, TWO, TWO, THREE };
        Assert.assertEquals("sum", SUM, StatUtils.sum(values), TOLERANCE);
        Assert.assertEquals("sumsq", SUMSQ, StatUtils.sumSq(values), TOLERANCE);
        Assert.assertEquals("var", VAR, StatUtils.variance(values), TOLERANCE);
        Assert.assertEquals("var with mean", VAR, StatUtils.variance(values, MEAN), TOLERANCE);
        Assert.assertEquals("mean", MEAN, StatUtils.mean(values), TOLERANCE);
        Assert.assertEquals("min", MIN, StatUtils.min(values), TOLERANCE);
        Assert.assertEquals("max", MAX, StatUtils.max(values), TOLERANCE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testN0andN1Conditions
    public void testN0andN1Conditions() {
        double[] values = new double[0];

        Assert.assertTrue(
            "Mean of n = 0 set should be NaN",
            Double.isNaN(StatUtils.mean(values)));
        Assert.assertTrue(
            "Variance of n = 0 set should be NaN",
            Double.isNaN(StatUtils.variance(values)));

        values = new double[] { ONE };

        Assert.assertTrue(
            "Mean of n = 1 set should be value of single item n1",
            StatUtils.mean(values) == ONE);
        Assert.assertTrue(
            "Variance of n = 1 set should be zero",
            StatUtils.variance(values) == 0);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testArrayIndexConditions
    public void testArrayIndexConditions() {
        double[] values = { 1.0, 2.0, 3.0, 4.0 };

        Assert.assertEquals(
            "Sum not expected",
            5.0,
            StatUtils.sum(values, 1, 2),
            Double.MIN_VALUE);
        Assert.assertEquals(
            "Sum not expected",
            3.0,
            StatUtils.sum(values, 0, 2),
            Double.MIN_VALUE);
        Assert.assertEquals(
            "Sum not expected",
            7.0,
            StatUtils.sum(values, 2, 2),
            Double.MIN_VALUE);

        try {
            StatUtils.sum(values, 2, 3);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            
        }

        try {
            StatUtils.sum(values, -1, 2);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            
        }

    }

// org.apache.commons.math3.stat.StatUtilsTest::testSumSq
    public void testSumSq() {
        double[] x = null;

        
        try {
            StatUtils.sumSq(x);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.sumSq(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(0, StatUtils.sumSq(x), TOLERANCE);
        TestUtils.assertEquals(0, StatUtils.sumSq(x, 0, 0), TOLERANCE);

        
        x = new double[] {TWO};
        TestUtils.assertEquals(4, StatUtils.sumSq(x), TOLERANCE);
        TestUtils.assertEquals(4, StatUtils.sumSq(x, 0, 1), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(18, StatUtils.sumSq(x), TOLERANCE);
        TestUtils.assertEquals(8, StatUtils.sumSq(x, 1, 2), TOLERANCE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testProduct
    public void testProduct() {
        double[] x = null;

        
        try {
            StatUtils.product(x);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.product(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(1, StatUtils.product(x), TOLERANCE);
        TestUtils.assertEquals(1, StatUtils.product(x, 0, 0), TOLERANCE);

        
        x = new double[] {TWO};
        TestUtils.assertEquals(TWO, StatUtils.product(x), TOLERANCE);
        TestUtils.assertEquals(TWO, StatUtils.product(x, 0, 1), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(12, StatUtils.product(x), TOLERANCE);
        TestUtils.assertEquals(4, StatUtils.product(x, 1, 2), TOLERANCE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testSumLog
    public void testSumLog() {
        double[] x = null;

        
        try {
            StatUtils.sumLog(x);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.sumLog(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(0, StatUtils.sumLog(x), TOLERANCE);
        TestUtils.assertEquals(0, StatUtils.sumLog(x, 0, 0), TOLERANCE);

        
        x = new double[] {TWO};
        TestUtils.assertEquals(FastMath.log(TWO), StatUtils.sumLog(x), TOLERANCE);
        TestUtils.assertEquals(FastMath.log(TWO), StatUtils.sumLog(x, 0, 1), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(FastMath.log(ONE) + 2.0 * FastMath.log(TWO) + FastMath.log(THREE), StatUtils.sumLog(x), TOLERANCE);
        TestUtils.assertEquals(2.0 * FastMath.log(TWO), StatUtils.sumLog(x, 1, 2), TOLERANCE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testMean
    public void testMean() {
        double[] x = null;

        try {
            StatUtils.mean(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.mean(x, 0, 0), TOLERANCE);

        
        x = new double[] {TWO};
        TestUtils.assertEquals(TWO, StatUtils.mean(x, 0, 1), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(2.5, StatUtils.mean(x, 2, 2), TOLERANCE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testVariance
    public void testVariance() {
        double[] x = null;

        try {
            StatUtils.variance(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.variance(x, 0, 0), TOLERANCE);

        
        x = new double[] {TWO};
        TestUtils.assertEquals(0.0, StatUtils.variance(x, 0, 1), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(0.5, StatUtils.variance(x, 2, 2), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(0.5, StatUtils.variance(x,2.5, 2, 2), TOLERANCE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testPopulationVariance
    public void testPopulationVariance() {
        double[] x = null;

        try {
            StatUtils.variance(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.populationVariance(x, 0, 0), TOLERANCE);

        
        x = new double[] {TWO};
        TestUtils.assertEquals(0.0, StatUtils.populationVariance(x, 0, 1), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(0.25, StatUtils.populationVariance(x, 0, 2), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(0.25, StatUtils.populationVariance(x, 2.5, 2, 2), TOLERANCE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testMax
    public void testMax() {
        double[] x = null;

        try {
            StatUtils.max(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.max(x, 0, 0), TOLERANCE);

        
        x = new double[] {TWO};
        TestUtils.assertEquals(TWO, StatUtils.max(x, 0, 1), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(THREE, StatUtils.max(x, 1, 3), TOLERANCE);

        
        x = new double[] {NAN, TWO, THREE};
        TestUtils.assertEquals(THREE, StatUtils.max(x), TOLERANCE);

        
        x = new double[] {ONE, NAN, THREE};
        TestUtils.assertEquals(THREE, StatUtils.max(x), TOLERANCE);

        
        x = new double[] {ONE, TWO, NAN};
        TestUtils.assertEquals(TWO, StatUtils.max(x), TOLERANCE);

        
        x = new double[] {NAN, NAN, NAN};
        TestUtils.assertEquals(NAN, StatUtils.max(x), TOLERANCE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testMin
    public void testMin() {
        double[] x = null;

        try {
            StatUtils.min(x, 0, 4);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.min(x, 0, 0), TOLERANCE);

        
        x = new double[] {TWO};
        TestUtils.assertEquals(TWO, StatUtils.min(x, 0, 1), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(TWO, StatUtils.min(x, 1, 3), TOLERANCE);

        
        x = new double[] {NAN, TWO, THREE};
        TestUtils.assertEquals(TWO, StatUtils.min(x), TOLERANCE);

        
        x = new double[] {ONE, NAN, THREE};
        TestUtils.assertEquals(ONE, StatUtils.min(x), TOLERANCE);

        
        x = new double[] {ONE, TWO, NAN};
        TestUtils.assertEquals(ONE, StatUtils.min(x), TOLERANCE);

        
        x = new double[] {NAN, NAN, NAN};
        TestUtils.assertEquals(NAN, StatUtils.min(x), TOLERANCE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testPercentile
    public void testPercentile() {
        double[] x = null;

        
        try {
            StatUtils.percentile(x, .25);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            StatUtils.percentile(x, 0, 4, 0.25);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        x = new double[] {};
        TestUtils.assertEquals(Double.NaN, StatUtils.percentile(x, 25), TOLERANCE);
        TestUtils.assertEquals(Double.NaN, StatUtils.percentile(x, 0, 0, 25), TOLERANCE);

        
        x = new double[] {TWO};
        TestUtils.assertEquals(TWO, StatUtils.percentile(x, 25), TOLERANCE);
        TestUtils.assertEquals(TWO, StatUtils.percentile(x, 0, 1, 25), TOLERANCE);

        
        x = new double[] {ONE, TWO, TWO, THREE};
        TestUtils.assertEquals(2.5, StatUtils.percentile(x, 70), TOLERANCE);
        TestUtils.assertEquals(2.5, StatUtils.percentile(x, 1, 3, 62.5), TOLERANCE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testDifferenceStats
    public void testDifferenceStats() {
        double sample1[] = {1d, 2d, 3d, 4d};
        double sample2[] = {1d, 3d, 4d, 2d};
        double diff[] = {0d, -1d, -1d, 2d};
        double small[] = {1d, 4d};
        double meanDifference = StatUtils.meanDifference(sample1, sample2);
        Assert.assertEquals(StatUtils.sumDifference(sample1, sample2), StatUtils.sum(diff), TOLERANCE);
        Assert.assertEquals(meanDifference, StatUtils.mean(diff), TOLERANCE);
        Assert.assertEquals(StatUtils.varianceDifference(sample1, sample2, meanDifference),
                StatUtils.variance(diff), TOLERANCE);
        try {
            StatUtils.meanDifference(sample1, small);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            StatUtils.varianceDifference(sample1, small, meanDifference);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            double[] single = {1.0};
            StatUtils.varianceDifference(single, single, meanDifference);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.StatUtilsTest::testGeometricMean
    public void testGeometricMean() {
        double[] test = null;
        try {
            StatUtils.geometricMean(test);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        test = new double[] {2, 4, 6, 8};
        Assert.assertEquals(FastMath.exp(0.25d * StatUtils.sumLog(test)),
                StatUtils.geometricMean(test), Double.MIN_VALUE);
        Assert.assertEquals(FastMath.exp(0.5 * StatUtils.sumLog(test, 0, 2)),
                StatUtils.geometricMean(test, 0, 2), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.StatUtilsTest::testNormalize1
    public void testNormalize1() {
        double sample[] = { 50, 100 };
        double expectedSample[] = { -25 / Math.sqrt(1250), 25 / Math.sqrt(1250) };
        double[] out = StatUtils.normalize(sample);
        for (int i = 0; i < out.length; i++) {
            Assert.assertTrue(Precision.equals(out[i], expectedSample[i], 1));
        }

    }

// org.apache.commons.math3.stat.StatUtilsTest::testNormalize2
    public void testNormalize2() {
        
        int length = 77;
        double sample[] = new double[length];
        for (int i = 0; i < length; i++) {
            sample[i] = Math.random();
        }
        
        double standardizedSample[] = StatUtils.normalize(sample);

        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        for (int i = 0; i < length; i++) {
            stats.addValue(standardizedSample[i]);
        }
        
        double distance = 1E-10;
        
        Assert.assertEquals(0.0, stats.getMean(), distance);
        Assert.assertEquals(1.0, stats.getStandardDeviation(), distance);

    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testCluster
    public void testCluster() {
        
        final EuclideanDoublePoint[] points = new EuclideanDoublePoint[] {
                new EuclideanDoublePoint(new double[] { 83.08303244924173, 58.83387754182331 }),
                new EuclideanDoublePoint(new double[] { 45.05445510940626, 23.469642649637535 }),
                new EuclideanDoublePoint(new double[] { 14.96417921432294, 69.0264096390456 }),
                new EuclideanDoublePoint(new double[] { 73.53189604333602, 34.896145021310076 }),
                new EuclideanDoublePoint(new double[] { 73.28498173551634, 33.96860806993209 }),
                new EuclideanDoublePoint(new double[] { 73.45828098873608, 33.92584423092194 }),
                new EuclideanDoublePoint(new double[] { 73.9657889183145, 35.73191006924026 }),
                new EuclideanDoublePoint(new double[] { 74.0074097183533, 36.81735596177168 }),
                new EuclideanDoublePoint(new double[] { 73.41247541410848, 34.27314856695011 }),
                new EuclideanDoublePoint(new double[] { 73.9156256353017, 36.83206791547127 }),
                new EuclideanDoublePoint(new double[] { 74.81499205809087, 37.15682749846019 }),
                new EuclideanDoublePoint(new double[] { 74.03144880081527, 37.57399178552441 }),
                new EuclideanDoublePoint(new double[] { 74.51870941207744, 38.674258946906775 }),
                new EuclideanDoublePoint(new double[] { 74.50754595105536, 35.58903978415765 }),
                new EuclideanDoublePoint(new double[] { 74.51322752749547, 36.030572259100154 }),
                new EuclideanDoublePoint(new double[] { 59.27900996617973, 46.41091720294207 }),
                new EuclideanDoublePoint(new double[] { 59.73744793841615, 46.20015558367595 }),
                new EuclideanDoublePoint(new double[] { 58.81134076672606, 45.71150126331486 }),
                new EuclideanDoublePoint(new double[] { 58.52225539437495, 47.416083617601544 }),
                new EuclideanDoublePoint(new double[] { 58.218626647023484, 47.36228902172297 }),
                new EuclideanDoublePoint(new double[] { 60.27139669447206, 46.606106348801404 }),
                new EuclideanDoublePoint(new double[] { 60.894962462363765, 46.976924697402865 }),
                new EuclideanDoublePoint(new double[] { 62.29048673878424, 47.66970563563518 }),
                new EuclideanDoublePoint(new double[] { 61.03857608977705, 46.212924720020965 }),
                new EuclideanDoublePoint(new double[] { 60.16916214139201, 45.18193661351688 }),
                new EuclideanDoublePoint(new double[] { 59.90036905976012, 47.555364347063005 }),
                new EuclideanDoublePoint(new double[] { 62.33003634144552, 47.83941489877179 }),
                new EuclideanDoublePoint(new double[] { 57.86035536718555, 47.31117930193432 }),
                new EuclideanDoublePoint(new double[] { 58.13715479685925, 48.985960494028404 }),
                new EuclideanDoublePoint(new double[] { 56.131923963548616, 46.8508904252667 }),
                new EuclideanDoublePoint(new double[] { 55.976329887053, 47.46384037658572 }),
                new EuclideanDoublePoint(new double[] { 56.23245975235477, 47.940035191131756 }),
                new EuclideanDoublePoint(new double[] { 58.51687048212625, 46.622885352699086 }),
                new EuclideanDoublePoint(new double[] { 57.85411081905477, 45.95394361577928 }),
                new EuclideanDoublePoint(new double[] { 56.445776311447844, 45.162093662656844 }),
                new EuclideanDoublePoint(new double[] { 57.36691949656233, 47.50097194337286 }),
                new EuclideanDoublePoint(new double[] { 58.243626387557015, 46.114052729681134 }),
                new EuclideanDoublePoint(new double[] { 56.27224595635198, 44.799080066150054 }),
                new EuclideanDoublePoint(new double[] { 57.606924816500396, 46.94291057763621 }),
                new EuclideanDoublePoint(new double[] { 30.18714230041951, 13.877149710431695 }),
                new EuclideanDoublePoint(new double[] { 30.449448810657486, 13.490778346545994 }),
                new EuclideanDoublePoint(new double[] { 30.295018390286714, 13.264889000216499 }),
                new EuclideanDoublePoint(new double[] { 30.160201832884923, 11.89278262341395 }),
                new EuclideanDoublePoint(new double[] { 31.341509791789576, 15.282655921997502 }),
                new EuclideanDoublePoint(new double[] { 31.68601630325429, 14.756873246748 }),
                new EuclideanDoublePoint(new double[] { 29.325963742565364, 12.097849250072613 }),
                new EuclideanDoublePoint(new double[] { 29.54820742388256, 13.613295356975868 }),
                new EuclideanDoublePoint(new double[] { 28.79359608888626, 10.36352064087987 }),
                new EuclideanDoublePoint(new double[] { 31.01284597092308, 12.788479208014905 }),
                new EuclideanDoublePoint(new double[] { 27.58509216737002, 11.47570110601373 }),
                new EuclideanDoublePoint(new double[] { 28.593799561727792, 10.780998203903437 }),
                new EuclideanDoublePoint(new double[] { 31.356105766724795, 15.080316198524088 }),
                new EuclideanDoublePoint(new double[] { 31.25948503636755, 13.674329151166603 }),
                new EuclideanDoublePoint(new double[] { 32.31590076372959, 14.95261758659035 }),
                new EuclideanDoublePoint(new double[] { 30.460413702763617, 15.88402809202671 }),
                new EuclideanDoublePoint(new double[] { 32.56178203062154, 14.586076852632686 }),
                new EuclideanDoublePoint(new double[] { 32.76138648530468, 16.239837325178087 }),
                new EuclideanDoublePoint(new double[] { 30.1829453331884, 14.709592407103628 }),
                new EuclideanDoublePoint(new double[] { 29.55088173528202, 15.0651247180067 }),
                new EuclideanDoublePoint(new double[] { 29.004155302187428, 14.089665298582986 }),
                new EuclideanDoublePoint(new double[] { 29.339624439831823, 13.29096065578051 }),
                new EuclideanDoublePoint(new double[] { 30.997460327576846, 14.551914158277214 }),
                new EuclideanDoublePoint(new double[] { 30.66784126125276, 16.269703107886016 })
        };

        final DBSCANClusterer<EuclideanDoublePoint> transformer =
                new DBSCANClusterer<EuclideanDoublePoint>(2.0, 5);
        final List<Cluster<EuclideanDoublePoint>> clusters = transformer.cluster(Arrays.asList(points));

        final List<EuclideanDoublePoint> clusterOne =
                Arrays.asList(points[3], points[4], points[5], points[6], points[7], points[8], points[9], points[10],
                              points[11], points[12], points[13], points[14]);
        final List<EuclideanDoublePoint> clusterTwo =
                Arrays.asList(points[15], points[16], points[17], points[18], points[19], points[20], points[21],
                              points[22], points[23], points[24], points[25], points[26], points[27], points[28],
                              points[29], points[30], points[31], points[32], points[33], points[34], points[35],
                              points[36], points[37], points[38]);
        final List<EuclideanDoublePoint> clusterThree =
                Arrays.asList(points[39], points[40], points[41], points[42], points[43], points[44], points[45],
                              points[46], points[47], points[48], points[49], points[50], points[51], points[52],
                              points[53], points[54], points[55], points[56], points[57], points[58], points[59],
                              points[60], points[61], points[62]);

        boolean cluster1Found = false;
        boolean cluster2Found = false;
        boolean cluster3Found = false;
        Assert.assertEquals(3, clusters.size());
        for (final Cluster<EuclideanDoublePoint> cluster : clusters) {
            if (cluster.getPoints().containsAll(clusterOne)) {
                cluster1Found = true;
            }
            if (cluster.getPoints().containsAll(clusterTwo)) {
                cluster2Found = true;
            }
            if (cluster.getPoints().containsAll(clusterThree)) {
                cluster3Found = true;
            }
        }
        Assert.assertTrue(cluster1Found);
        Assert.assertTrue(cluster2Found);
        Assert.assertTrue(cluster3Found);
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testSingleLink
    public void testSingleLink() {
        final EuclideanIntegerPoint[] points = {
                new EuclideanIntegerPoint(new int[] {10, 10}), 
                new EuclideanIntegerPoint(new int[] {12, 9}),
                new EuclideanIntegerPoint(new int[] {10, 8}),
                new EuclideanIntegerPoint(new int[] {8, 8}),
                new EuclideanIntegerPoint(new int[] {8, 6}),
                new EuclideanIntegerPoint(new int[] {7, 7}),
                new EuclideanIntegerPoint(new int[] {5, 6}),  
                new EuclideanIntegerPoint(new int[] {14, 8}), 
                new EuclideanIntegerPoint(new int[] {7, 15}), 
                new EuclideanIntegerPoint(new int[] {17, 8}), 
                
        };
        
        final DBSCANClusterer<EuclideanIntegerPoint> clusterer = new DBSCANClusterer<EuclideanIntegerPoint>(3, 3);
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(Arrays.asList(points));
        
        Assert.assertEquals(1, clusters.size());
        
        final List<EuclideanIntegerPoint> clusterOne =
                Arrays.asList(points[0], points[1], points[2], points[3], points[4], points[5], points[6], points[7]);
        Assert.assertTrue(clusters.get(0).getPoints().containsAll(clusterOne));
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testGetEps
    public void testGetEps() {
        final DBSCANClusterer<EuclideanDoublePoint> transformer = new DBSCANClusterer<EuclideanDoublePoint>(2.0, 5);
        Assert.assertEquals(2.0, transformer.getEps(), 0.0);
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testGetMinPts
    public void testGetMinPts() {
        final DBSCANClusterer<EuclideanDoublePoint> transformer = new DBSCANClusterer<EuclideanDoublePoint>(2.0, 5);
        Assert.assertEquals(5, transformer.getMinPts());
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testNegativeEps
    public void testNegativeEps() {
        new DBSCANClusterer<EuclideanDoublePoint>(-2.0, 5);
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testNegativeMinPts
    public void testNegativeMinPts() {
        new DBSCANClusterer<EuclideanDoublePoint>(2.0, -5);
    }

// org.apache.commons.math3.stat.clustering.DBSCANClustererTest::testNullDataset
    public void testNullDataset() {
        DBSCANClusterer<EuclideanDoublePoint> clusterer = new DBSCANClusterer<EuclideanDoublePoint>(2.0, 5);
        clusterer.cluster(null);
    }

// org.apache.commons.math3.stat.clustering.EuclideanDoublePointTest::testArrayIsReference
    public void testArrayIsReference() {
        final double[] array = { -3.0, -2.0, -1.0, 0.0, 1.0 };
        Assert.assertArrayEquals(array, new EuclideanDoublePoint(array).getPoint(), 1.0e-15);
    }

// org.apache.commons.math3.stat.clustering.EuclideanDoublePointTest::testDistance
    public void testDistance() {
        final EuclideanDoublePoint e1 = new EuclideanDoublePoint(new double[] { -3.0, -2.0, -1.0, 0.0, 1.0 });
        final EuclideanDoublePoint e2 = new EuclideanDoublePoint(new double[] { 1.0, 0.0, -1.0, 1.0, 1.0 });
        Assert.assertEquals(FastMath.sqrt(21.0), e1.distanceFrom(e2), 1.0e-15);
        Assert.assertEquals(0.0, e1.distanceFrom(e1), 1.0e-15);
        Assert.assertEquals(0.0, e2.distanceFrom(e2), 1.0e-15);
    }

// org.apache.commons.math3.stat.clustering.EuclideanDoublePointTest::testCentroid
    public void testCentroid() {
        final List<EuclideanDoublePoint> list = new ArrayList<EuclideanDoublePoint>();
        list.add(new EuclideanDoublePoint(new double[] { 1.0, 3.0 }));
        list.add(new EuclideanDoublePoint(new double[] { 2.0, 2.0 }));
        list.add(new EuclideanDoublePoint(new double[] { 3.0, 3.0 }));
        list.add(new EuclideanDoublePoint(new double[] { 2.0, 4.0 }));
        final EuclideanDoublePoint c = list.get(0).centroidOf(list);
        Assert.assertEquals(2.0, c.getPoint()[0], 1.0e-15);
        Assert.assertEquals(3.0, c.getPoint()[1], 1.0e-15);
    }

// org.apache.commons.math3.stat.clustering.EuclideanDoublePointTest::testSerial
    public void testSerial() {
        final EuclideanDoublePoint p = new EuclideanDoublePoint(new double[] { -3.0, -2.0, -1.0, 0.0, 1.0 });
        Assert.assertEquals(p, TestUtils.serializeAndRecover(p));
    }

// org.apache.commons.math3.stat.clustering.EuclideanIntegerPointTest::testArrayIsReference
    public void testArrayIsReference() {
        int[] array = { -3, -2, -1, 0, 1 };
        Assert.assertTrue(array == new EuclideanIntegerPoint(array).getPoint());
    }

// org.apache.commons.math3.stat.clustering.EuclideanIntegerPointTest::testDistance
    public void testDistance() {
        EuclideanIntegerPoint e1 = new EuclideanIntegerPoint(new int[] { -3, -2, -1, 0, 1 });
        EuclideanIntegerPoint e2 = new EuclideanIntegerPoint(new int[] {  1,  0, -1, 1, 1 });
        Assert.assertEquals(FastMath.sqrt(21.0), e1.distanceFrom(e2), 1.0e-15);
        Assert.assertEquals(0.0, e1.distanceFrom(e1), 1.0e-15);
        Assert.assertEquals(0.0, e2.distanceFrom(e2), 1.0e-15);
    }

// org.apache.commons.math3.stat.clustering.EuclideanIntegerPointTest::testCentroid
    public void testCentroid() {
        List<EuclideanIntegerPoint> list = new ArrayList<EuclideanIntegerPoint>();
        list.add(new EuclideanIntegerPoint(new int[] {  1,  3 }));
        list.add(new EuclideanIntegerPoint(new int[] {  2,  2 }));
        list.add(new EuclideanIntegerPoint(new int[] {  3,  3 }));
        list.add(new EuclideanIntegerPoint(new int[] {  2,  4 }));
        EuclideanIntegerPoint c = list.get(0).centroidOf(list);
        Assert.assertEquals(2, c.getPoint()[0]);
        Assert.assertEquals(3, c.getPoint()[1]);
    }

// org.apache.commons.math3.stat.clustering.EuclideanIntegerPointTest::testSerial
    public void testSerial() {
        EuclideanIntegerPoint p = new EuclideanIntegerPoint(new int[] { -3, -2, -1, 0, 1 });
        Assert.assertEquals(p, TestUtils.serializeAndRecover(p));
    }

// org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest::dimension2
    public void dimension2() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
            new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l));
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {

                
                new EuclideanIntegerPoint(new int[] { -15,  3 }),
                new EuclideanIntegerPoint(new int[] { -15,  4 }),
                new EuclideanIntegerPoint(new int[] { -15,  5 }),
                new EuclideanIntegerPoint(new int[] { -14,  3 }),
                new EuclideanIntegerPoint(new int[] { -14,  5 }),
                new EuclideanIntegerPoint(new int[] { -13,  3 }),
                new EuclideanIntegerPoint(new int[] { -13,  4 }),
                new EuclideanIntegerPoint(new int[] { -13,  5 }),

                
                new EuclideanIntegerPoint(new int[] { -1,  0 }),
                new EuclideanIntegerPoint(new int[] { -1, -1 }),
                new EuclideanIntegerPoint(new int[] {  0, -1 }),
                new EuclideanIntegerPoint(new int[] {  1, -1 }),
                new EuclideanIntegerPoint(new int[] {  1, -2 }),

                
                new EuclideanIntegerPoint(new int[] { 13,  3 }),
                new EuclideanIntegerPoint(new int[] { 13,  4 }),
                new EuclideanIntegerPoint(new int[] { 14,  4 }),
                new EuclideanIntegerPoint(new int[] { 14,  7 }),
                new EuclideanIntegerPoint(new int[] { 16,  5 }),
                new EuclideanIntegerPoint(new int[] { 16,  6 }),
                new EuclideanIntegerPoint(new int[] { 17,  4 }),
                new EuclideanIntegerPoint(new int[] { 17,  7 })

        };
        List<Cluster<EuclideanIntegerPoint>> clusters =
            transformer.cluster(Arrays.asList(points), 3, 5, 10);

        Assert.assertEquals(3, clusters.size());
        boolean cluster1Found = false;
        boolean cluster2Found = false;
        boolean cluster3Found = false;
        for (Cluster<EuclideanIntegerPoint> cluster : clusters) {
            int[] center = cluster.getCenter().getPoint();
            if (center[0] < 0) {
                cluster1Found = true;
                Assert.assertEquals(8, cluster.getPoints().size());
                Assert.assertEquals(-14, center[0]);
                Assert.assertEquals( 4, center[1]);
            } else if (center[1] < 0) {
                cluster2Found = true;
                Assert.assertEquals(5, cluster.getPoints().size());
                Assert.assertEquals( 0, center[0]);
                Assert.assertEquals(-1, center[1]);
            } else {
                cluster3Found = true;
                Assert.assertEquals(8, cluster.getPoints().size());
                Assert.assertEquals(15, center[0]);
                Assert.assertEquals(5, center[1]);
            }
        }
        Assert.assertTrue(cluster1Found);
        Assert.assertTrue(cluster2Found);
        Assert.assertTrue(cluster3Found);

    }

// org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisDegenerate
    public void testPerformClusterAnalysisDegenerate() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer = new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                new Random(1746432956321l));
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {
                new EuclideanIntegerPoint(new int[] { 1959, 325100 }),
                new EuclideanIntegerPoint(new int[] { 1960, 373200 }), };
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(Arrays.asList(points), 1, 1);
        Assert.assertEquals(1, clusters.size());
        Assert.assertEquals(2, (clusters.get(0).getPoints().size()));
        EuclideanIntegerPoint pt1 = new EuclideanIntegerPoint(new int[] { 1959, 325100 });
        EuclideanIntegerPoint pt2 = new EuclideanIntegerPoint(new int[] { 1960, 373200 });
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt1));
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt2));

    }

// org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest::testCertainSpace
    public void testCertainSpace() {
        KMeansPlusPlusClusterer.EmptyClusterStrategy[] strategies = {
            KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE,
            KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER,
            KMeansPlusPlusClusterer.EmptyClusterStrategy.FARTHEST_POINT
        };
        for (KMeansPlusPlusClusterer.EmptyClusterStrategy strategy : strategies) {
            KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l), strategy);
            int numberOfVariables = 27;
            
            int position1 = 1;
            int position2 = position1 + numberOfVariables;
            int position3 = position2 + numberOfVariables;
            int position4 = position3 + numberOfVariables;
            
            int multiplier = 1000000;

            EuclideanIntegerPoint[] breakingPoints = new EuclideanIntegerPoint[numberOfVariables];
            
            for (int i = 0; i < numberOfVariables; i++) {
                int points[] = { position1, position2, position3, position4 };
                
                for (int j = 0; j < points.length; j++) {
                    points[j] = points[j] * multiplier;
                }
                EuclideanIntegerPoint euclideanIntegerPoint = new EuclideanIntegerPoint(points);
                breakingPoints[i] = euclideanIntegerPoint;
                position1 = position1 + numberOfVariables;
                position2 = position2 + numberOfVariables;
                position3 = position3 + numberOfVariables;
                position4 = position4 + numberOfVariables;
            }

            for (int n = 2; n < 27; ++n) {
                List<Cluster<EuclideanIntegerPoint>> clusters =
                    transformer.cluster(Arrays.asList(breakingPoints), n, 100);
                Assert.assertEquals(n, clusters.size());
                int sum = 0;
                for (Cluster<EuclideanIntegerPoint> cluster : clusters) {
                    sum += cluster.getPoints().size();
                }
                Assert.assertEquals(numberOfVariables, sum);
            }
        }

    }

// org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest::testSmallDistances
    public void testSmallDistances() {
        
        
        int[] repeatedArray = { 0 };
        int[] uniqueArray = { 1 };
        CloseIntegerPoint repeatedPoint =
            new CloseIntegerPoint(new EuclideanIntegerPoint(repeatedArray));
        CloseIntegerPoint uniquePoint =
            new CloseIntegerPoint(new EuclideanIntegerPoint(uniqueArray));

        Collection<CloseIntegerPoint> points = new ArrayList<CloseIntegerPoint>();
        final int NUM_REPEATED_POINTS = 10 * 1000;
        for (int i = 0; i < NUM_REPEATED_POINTS; ++i) {
            points.add(repeatedPoint);
        }
        points.add(uniquePoint);

        
        
        final long RANDOM_SEED = 0;
        final int NUM_CLUSTERS = 2;
        final int NUM_ITERATIONS = 0;
        KMeansPlusPlusClusterer<CloseIntegerPoint> clusterer =
            new KMeansPlusPlusClusterer<CloseIntegerPoint>(new Random(RANDOM_SEED));
        List<Cluster<CloseIntegerPoint>> clusters =
            clusterer.cluster(points, NUM_CLUSTERS, NUM_ITERATIONS);

        
        boolean uniquePointIsCenter = false;
        for (Cluster<CloseIntegerPoint> cluster : clusters) {
            if (cluster.getCenter().equals(uniquePoint)) {
                uniquePointIsCenter = true;
            }
        }
        Assert.assertTrue(uniquePointIsCenter);
    }

// org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisToManyClusters
    public void testPerformClusterAnalysisToManyClusters() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer = 
            new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                    new Random(1746432956321l));
        
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {
            new EuclideanIntegerPoint(new int[] {
                1959, 325100
            }), new EuclideanIntegerPoint(new int[] {
                1960, 373200
            })
        };
        
        transformer.cluster(Arrays.asList(points), 3, 1);

    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testLongly
    public void testLongly() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();
        double[] rData = new double[] {
         12333921.73333333246, 3.679666000000000e+04, 343330206.333333313,
         1649102.666666666744, 1117681.066666666651, 23461965.733333334, 16240.93333333333248,
         36796.66000000000, 1.164576250000000e+02, 1063604.115416667,
         6258.666250000000, 3490.253750000000, 73503.000000000, 50.92333333333334,
         343330206.33333331347, 1.063604115416667e+06, 9879353659.329166412,
         56124369.854166664183, 30880428.345833335072, 685240944.600000024, 470977.90000000002328,
         1649102.66666666674, 6.258666250000000e+03, 56124369.854166664,
         873223.429166666698, -115378.762499999997, 4462741.533333333, 2973.03333333333330,
         1117681.06666666665, 3.490253750000000e+03, 30880428.345833335,
         -115378.762499999997, 484304.095833333326, 1764098.133333333, 1382.43333333333339,
         23461965.73333333433, 7.350300000000000e+04, 685240944.600000024,
         4462741.533333333209, 1764098.133333333302, 48387348.933333330, 32917.40000000000146,
         16240.93333333333, 5.092333333333334e+01, 470977.900000000,
         2973.033333333333, 1382.433333333333, 32917.40000000, 22.66666666666667
        };

        TestUtils.assertEquals("covariance matrix", createRealMatrix(rData, 7, 7), covarianceMatrix, 10E-9);

    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testSwissFertility
    public void testSwissFertility() {
         RealMatrix matrix = createRealMatrix(swissData, 47, 5);
         RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();
         double[] rData = new double[] {
           156.0424976873265, 100.1691489361702, -64.36692876965772, -79.7295097132285, 241.5632030527289,
           100.169148936170251, 515.7994172062905, -124.39283071230344, -139.6574005550416, 379.9043755781684,
           -64.3669287696577, -124.3928307123034, 63.64662349676226, 53.5758556891767, -190.5606105457909,
           -79.7295097132285, -139.6574005550416, 53.57585568917669, 92.4560592044403, -61.6988297872340,
            241.5632030527289, 379.9043755781684, -190.56061054579092, -61.6988297872340, 1739.2945371877890
         };

         TestUtils.assertEquals("covariance matrix", createRealMatrix(rData, 5, 5), covarianceMatrix, 10E-13);
    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        Assert.assertEquals(0d, new Covariance().covariance(noVariance, values, true), Double.MIN_VALUE);
        Assert.assertEquals(0d, new Covariance().covariance(noVariance, noVariance, true), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testOneColumn
    public void testOneColumn() {
        RealMatrix cov = new Covariance(new double[][] {{1}, {2}}, false).getCovarianceMatrix();
        Assert.assertEquals(1, cov.getRowDimension());
        Assert.assertEquals(1, cov.getColumnDimension());
        Assert.assertEquals(0.25, cov.getEntry(0, 0), 1.0e-15);
    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new Covariance().covariance(one, two, false);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            new Covariance(new double[][] {{},{}});
            Assert.fail("Expecting NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.stat.correlation.CovarianceTest::testConsistency
    public void testConsistency() {
        final RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        final RealMatrix covarianceMatrix = new Covariance(matrix).getCovarianceMatrix();

        
        Variance variance = new Variance();
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(variance.evaluate(matrix.getColumn(i)), covarianceMatrix.getEntry(i,i), 10E-14);
        }

        
        Assert.assertEquals(covarianceMatrix.getEntry(2, 3),
                new Covariance().covariance(matrix.getColumn(2), matrix.getColumn(3), true), 10E-14);
        Assert.assertEquals(covarianceMatrix.getEntry(2, 3), covarianceMatrix.getEntry(3, 2), Double.MIN_VALUE);

        
        RealMatrix repeatedColumns = new Array2DRowRealMatrix(47, 3);
        for (int i = 0; i < 3; i++) {
            repeatedColumns.setColumnMatrix(i, matrix.getColumnMatrix(0));
        }
        RealMatrix repeatedCovarianceMatrix = new Covariance(repeatedColumns).getCovarianceMatrix();
        double columnVariance = variance.evaluate(matrix.getColumn(0));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Assert.assertEquals(columnVariance, repeatedCovarianceMatrix.getEntry(i, j), 10E-14);
            }
        }

        
        double[][] data = matrix.getData();
        TestUtils.assertEquals("Covariances",
                covarianceMatrix, new Covariance().computeCovarianceMatrix(data),Double.MIN_VALUE);
        TestUtils.assertEquals("Covariances",
                covarianceMatrix, new Covariance().computeCovarianceMatrix(data, true),Double.MIN_VALUE);

        double[] x = data[0];
        double[] y = data[1];
        Assert.assertEquals(new Covariance().covariance(x, y),
                new Covariance().covariance(x, y, true), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testLongly
    public void testLongly() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
        double[] rData = new double[] {
                1.000000000000000, 0.9708985250610560, 0.9835516111796693, 0.5024980838759942,
                0.4573073999764817, 0.960390571594376, 0.9713294591921188,
                0.970898525061056, 1.0000000000000000, 0.9915891780247822, 0.6206333925590966,
                0.4647441876006747, 0.979163432977498, 0.9911491900672053,
                0.983551611179669, 0.9915891780247822, 1.0000000000000000, 0.6042609398895580,
                0.4464367918926265, 0.991090069458478, 0.9952734837647849,
                0.502498083875994, 0.6206333925590966, 0.6042609398895580, 1.0000000000000000,
                -0.1774206295018783, 0.686551516365312, 0.6682566045621746,
                0.457307399976482, 0.4647441876006747, 0.4464367918926265, -0.1774206295018783,
                1.0000000000000000, 0.364416267189032, 0.4172451498349454,
                0.960390571594376, 0.9791634329774981, 0.9910900694584777, 0.6865515163653120,
                0.3644162671890320, 1.000000000000000, 0.9939528462329257,
                0.971329459192119, 0.9911491900672053, 0.9952734837647849, 0.6682566045621746,
                0.4172451498349454, 0.993952846232926, 1.0000000000000000
        };
        TestUtils.assertEquals("correlation matrix", createRealMatrix(rData, 7, 7), correlationMatrix, 10E-15);

        double[] rPvalues = new double[] {
                4.38904690369668e-10,
                8.36353208910623e-12, 7.8159700933611e-14,
                0.0472894097790304, 0.01030636128354301, 0.01316878049026582,
                0.0749178049642416, 0.06971758330341182, 0.0830166169296545, 0.510948586323452,
                3.693245043123738e-09, 4.327782576751815e-11, 1.167954621905665e-13, 0.00331028281967516, 0.1652293725106684,
                3.95834476307755e-10, 1.114663916723657e-13, 1.332267629550188e-15, 0.00466039138541463, 0.1078477071581498, 7.771561172376096e-15
        };
        RealMatrix rPMatrix = createLowerTriangularRealMatrix(rPvalues, 7);
        fillUpper(rPMatrix, 0d);
        TestUtils.assertEquals("correlation p values", rPMatrix, corrInstance.getCorrelationPValues(), 10E-15);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testSwissFertility
    public void testSwissFertility() {
         RealMatrix matrix = createRealMatrix(swissData, 47, 5);
         PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
         RealMatrix correlationMatrix = corrInstance.getCorrelationMatrix();
         double[] rData = new double[] {
               1.0000000000000000, 0.3530791836199747, -0.6458827064572875, -0.6637888570350691,  0.4636847006517939,
                 0.3530791836199747, 1.0000000000000000,-0.6865422086171366, -0.6395225189483201, 0.4010950530487398,
                -0.6458827064572875, -0.6865422086171366, 1.0000000000000000, 0.6984152962884830, -0.5727418060641666,
                -0.6637888570350691, -0.6395225189483201, 0.6984152962884830, 1.0000000000000000, -0.1538589170909148,
                 0.4636847006517939, 0.4010950530487398, -0.5727418060641666, -0.1538589170909148, 1.0000000000000000
         };
         TestUtils.assertEquals("correlation matrix", createRealMatrix(rData, 5, 5), correlationMatrix, 10E-15);

         double[] rPvalues = new double[] {
                 0.01491720061472623,
                 9.45043734069043e-07, 9.95151527133974e-08,
                 3.658616965962355e-07, 1.304590105694471e-06, 4.811397236181847e-08,
                 0.001028523190118147, 0.005204433539191644, 2.588307925380906e-05, 0.301807756132683
         };
         RealMatrix rPMatrix = createLowerTriangularRealMatrix(rPvalues, 5);
         fillUpper(rPMatrix, 0d);
         TestUtils.assertEquals("correlation p values", rPMatrix, corrInstance.getCorrelationPValues(), 10E-15);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testPValueNearZero
    public void testPValueNearZero() {
        
        int dimension = 120;
        double[][] data = new double[dimension][2];
        for (int i = 0; i < dimension; i++) {
            data[i][0] = i;
            data[i][1] = i + 1/((double)i + 1);
        }
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(data);
        Assert.assertTrue(corrInstance.getCorrelationPValues().getEntry(0, 1) > 0);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testConstant
    public void testConstant() {
        double[] noVariance = new double[] {1, 1, 1, 1};
        double[] values = new double[] {1, 2, 3, 4};
        Assert.assertTrue(Double.isNaN(new PearsonsCorrelation().correlation(noVariance, values)));
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testInsufficientData
    public void testInsufficientData() {
        double[] one = new double[] {1};
        double[] two = new double[] {2};
        try {
            new PearsonsCorrelation().correlation(one, two);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        RealMatrix matrix = new BlockRealMatrix(new double[][] {{0},{1}});
        try {
            new PearsonsCorrelation(matrix);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testStdErrorConsistency
    public void testStdErrorConsistency() {
        TDistribution tDistribution = new TDistribution(45);
        RealMatrix matrix = createRealMatrix(swissData, 47, 5);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        RealMatrix rValues = corrInstance.getCorrelationMatrix();
        RealMatrix pValues = corrInstance.getCorrelationPValues();
        RealMatrix stdErrors = corrInstance.getCorrelationStandardErrors();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                double t = FastMath.abs(rValues.getEntry(i, j)) / stdErrors.getEntry(i, j);
                double p = 2 * (1 - tDistribution.cumulativeProbability(t));
                Assert.assertEquals(p, pValues.getEntry(i, j), 10E-15);
            }
        }
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testCovarianceConsistency
    public void testCovarianceConsistency() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        Covariance covInstance = new Covariance(matrix);
        PearsonsCorrelation corrFromCovInstance = new PearsonsCorrelation(covInstance);
        TestUtils.assertEquals("correlation values", corrInstance.getCorrelationMatrix(),
                corrFromCovInstance.getCorrelationMatrix(), 10E-15);
        TestUtils.assertEquals("p values", corrInstance.getCorrelationPValues(),
                corrFromCovInstance.getCorrelationPValues(), 10E-15);
        TestUtils.assertEquals("standard errors", corrInstance.getCorrelationStandardErrors(),
                corrFromCovInstance.getCorrelationStandardErrors(), 10E-15);

        PearsonsCorrelation corrFromCovInstance2 =
            new PearsonsCorrelation(covInstance.getCovarianceMatrix(), 16);
        TestUtils.assertEquals("correlation values", corrInstance.getCorrelationMatrix(),
                corrFromCovInstance2.getCorrelationMatrix(), 10E-15);
        TestUtils.assertEquals("p values", corrInstance.getCorrelationPValues(),
                corrFromCovInstance2.getCorrelationPValues(), 10E-15);
        TestUtils.assertEquals("standard errors", corrInstance.getCorrelationStandardErrors(),
                corrFromCovInstance2.getCorrelationStandardErrors(), 10E-15);
    }

// org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest::testConsistency
    public void testConsistency() {
        RealMatrix matrix = createRealMatrix(longleyData, 16, 7);
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(matrix);
        double[][] data = matrix.getData();
        double[] x = matrix.getColumn(0);
        double[] y = matrix.getColumn(1);
        Assert.assertEquals(new PearsonsCorrelation().correlation(x, y),
                corrInstance.getCorrelationMatrix().getEntry(0, 1), Double.MIN_VALUE);
        TestUtils.assertEquals("Correlation matrix", corrInstance.getCorrelationMatrix(),
                new PearsonsCorrelation().computeCorrelationMatrix(data), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest::testAggregation
    public void testAggregation() {
        AggregateSummaryStatistics aggregate = new AggregateSummaryStatistics();
        SummaryStatistics setOneStats = aggregate.createContributingStatistics();
        SummaryStatistics setTwoStats = aggregate.createContributingStatistics();

        Assert.assertNotNull("The set one contributing stats are null", setOneStats);
        Assert.assertNotNull("The set two contributing stats are null", setTwoStats);
        Assert.assertNotSame("Contributing stats objects are the same", setOneStats, setTwoStats);

        setOneStats.addValue(2);
        setOneStats.addValue(3);
        setOneStats.addValue(5);
        setOneStats.addValue(7);
        setOneStats.addValue(11);
        Assert.assertEquals("Wrong number of set one values", 5, setOneStats.getN());
        Assert.assertTrue("Wrong sum of set one values", Precision.equals(28.0, setOneStats.getSum(), 1));

        setTwoStats.addValue(2);
        setTwoStats.addValue(4);
        setTwoStats.addValue(8);
        Assert.assertEquals("Wrong number of set two values", 3, setTwoStats.getN());
        Assert.assertTrue("Wrong sum of set two values", Precision.equals(14.0, setTwoStats.getSum(), 1));

        Assert.assertEquals("Wrong number of aggregate values", 8, aggregate.getN());
        Assert.assertTrue("Wrong aggregate sum", Precision.equals(42.0, aggregate.getSum(), 1));
    }

// org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest::testAggregationConsistency
    public void testAggregationConsistency() {

        
        double[] totalSample = generateSample();
        double[][] subSamples = generatePartition(totalSample);
        int nSamples = subSamples.length;

        
        AggregateSummaryStatistics aggregate = new AggregateSummaryStatistics();
        SummaryStatistics totalStats = new SummaryStatistics();

        
        SummaryStatistics componentStats[] = new SummaryStatistics[nSamples];

        for (int i = 0; i < nSamples; i++) {

            
            componentStats[i] = aggregate.createContributingStatistics();

            
            for (int j = 0; j < subSamples[i].length; j++) {
                componentStats[i].addValue(subSamples[i][j]);
            }
        }

        
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        Assert.assertEquals(totalStats.getSummary(), aggregate.getSummary());

    }

// org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest::testAggregate
    public void testAggregate() {

        
        double[] totalSample = generateSample();
        double[][] subSamples = generatePartition(totalSample);
        int nSamples = subSamples.length;

        
        SummaryStatistics totalStats = new SummaryStatistics();
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        SummaryStatistics[] subSampleStats = new SummaryStatistics[nSamples];
        for (int i = 0; i < nSamples; i++) {
            subSampleStats[i] = new SummaryStatistics();
        }
        Collection<SummaryStatistics> aggregate = new ArrayList<SummaryStatistics>();
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < subSamples[i].length; j++) {
                subSampleStats[i].addValue(subSamples[i][j]);
            }
            aggregate.add(subSampleStats[i]);
        }

        
        StatisticalSummary aggregatedStats = AggregateSummaryStatistics.aggregate(aggregate);
        assertEquals(totalStats.getSummary(), aggregatedStats, 10E-12);
    }

// org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest::testAggregateDegenerate
    public void testAggregateDegenerate() {
        double[] totalSample = {1, 2, 3, 4, 5};
        double[][] subSamples = {{1}, {2}, {3}, {4}, {5}};

        
        SummaryStatistics totalStats = new SummaryStatistics();
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        SummaryStatistics[] subSampleStats = new SummaryStatistics[5];
        for (int i = 0; i < 5; i++) {
            subSampleStats[i] = new SummaryStatistics();
        }
        Collection<SummaryStatistics> aggregate = new ArrayList<SummaryStatistics>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < subSamples[i].length; j++) {
                subSampleStats[i].addValue(subSamples[i][j]);
            }
            aggregate.add(subSampleStats[i]);
        }

        
        StatisticalSummaryValues aggregatedStats = AggregateSummaryStatistics.aggregate(aggregate);
        assertEquals(totalStats.getSummary(), aggregatedStats, 10E-12);
    }

// org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest::testAggregateSpecialValues
    public void testAggregateSpecialValues() {
        double[] totalSample = {Double.POSITIVE_INFINITY, 2, 3, Double.NaN, 5};
        double[][] subSamples = {{Double.POSITIVE_INFINITY, 2}, {3}, {Double.NaN}, {5}};

        
        SummaryStatistics totalStats = new SummaryStatistics();
        for (int i = 0; i < totalSample.length; i++) {
            totalStats.addValue(totalSample[i]);
        }

        
        SummaryStatistics[] subSampleStats = new SummaryStatistics[5];
        for (int i = 0; i < 4; i++) {
            subSampleStats[i] = new SummaryStatistics();
        }
        Collection<SummaryStatistics> aggregate = new ArrayList<SummaryStatistics>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < subSamples[i].length; j++) {
                subSampleStats[i].addValue(subSamples[i][j]);
            }
            aggregate.add(subSampleStats[i]);
        }

        
        StatisticalSummaryValues aggregatedStats = AggregateSummaryStatistics.aggregate(aggregate);
        assertEquals(totalStats.getSummary(), aggregatedStats, 10E-12);

    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testSetterInjection
    public void testSetterInjection() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(3);
        Assert.assertEquals(2, stats.getMean(), 1E-10);
        
        stats.setMeanImpl(new deepMean());
        Assert.assertEquals(42, stats.getMean(), 1E-10);
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testCopy
    public void testCopy() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(3);
        DescriptiveStatistics copy = new DescriptiveStatistics(stats);
        Assert.assertEquals(2, copy.getMean(), 1E-10);
        
        stats.setMeanImpl(new deepMean());
        copy = stats.copy();
        Assert.assertEquals(42, copy.getMean(), 1E-10);
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testWindowSize
    public void testWindowSize() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.setWindowSize(300);
        for (int i = 0; i < 100; ++i) {
            stats.addValue(i + 1);
        }
        int refSum = (100 * 101) / 2;
        Assert.assertEquals(refSum / 100.0, stats.getMean(), 1E-10);
        Assert.assertEquals(300, stats.getWindowSize());
        try {
            stats.setWindowSize(-3);
            Assert.fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        }
        Assert.assertEquals(300, stats.getWindowSize());
        stats.setWindowSize(50);
        Assert.assertEquals(50, stats.getWindowSize());
        int refSum2 = refSum - (50 * 51) / 2;
        Assert.assertEquals(refSum2 / 50.0, stats.getMean(), 1E-10);
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testGetValues
    public void testGetValues() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        for (int i = 100; i > 0; --i) {
            stats.addValue(i);
        }
        int refSum = (100 * 101) / 2;
        Assert.assertEquals(refSum / 100.0, stats.getMean(), 1E-10);
        double[] v = stats.getValues();
        for (int i = 0; i < v.length; ++i) {
            Assert.assertEquals(100.0 - i, v[i], 1.0e-10);
        }
        double[] s = stats.getSortedValues();
        for (int i = 0; i < s.length; ++i) {
            Assert.assertEquals(i + 1.0, s[i], 1.0e-10);
        }
        Assert.assertEquals(12.0, stats.getElement(88), 1.0e-10);
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testToString
    public void testToString() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(2);
        stats.addValue(3);
        Locale d = Locale.getDefault();
        Locale.setDefault(Locale.US);
        Assert.assertEquals("DescriptiveStatistics:\n" +
                     "n: 3\n" +
                     "min: 1.0\n" +
                     "max: 3.0\n" +
                     "mean: 2.0\n" +
                     "std dev: 1.0\n" +
                     "median: 2.0\n" +
                     "skewness: 0.0\n" +
                     "kurtosis: NaN\n",  stats.toString());
        Locale.setDefault(d);
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testShuffledStatistics
    public void testShuffledStatistics() {
        
        
        
        DescriptiveStatistics reference = createDescriptiveStatistics();
        DescriptiveStatistics shuffled  = createDescriptiveStatistics();

        UnivariateStatistic tmp = shuffled.getGeometricMeanImpl();
        shuffled.setGeometricMeanImpl(shuffled.getMeanImpl());
        shuffled.setMeanImpl(shuffled.getKurtosisImpl());
        shuffled.setKurtosisImpl(shuffled.getSkewnessImpl());
        shuffled.setSkewnessImpl(shuffled.getVarianceImpl());
        shuffled.setVarianceImpl(shuffled.getMaxImpl());
        shuffled.setMaxImpl(shuffled.getMinImpl());
        shuffled.setMinImpl(shuffled.getSumImpl());
        shuffled.setSumImpl(shuffled.getSumsqImpl());
        shuffled.setSumsqImpl(tmp);

        for (int i = 100; i > 0; --i) {
            reference.addValue(i);
            shuffled.addValue(i);
        }

        Assert.assertEquals(reference.getMean(),          shuffled.getGeometricMean(), 1.0e-10);
        Assert.assertEquals(reference.getKurtosis(),      shuffled.getMean(),          1.0e-10);
        Assert.assertEquals(reference.getSkewness(),      shuffled.getKurtosis(), 1.0e-10);
        Assert.assertEquals(reference.getVariance(),      shuffled.getSkewness(), 1.0e-10);
        Assert.assertEquals(reference.getMax(),           shuffled.getVariance(), 1.0e-10);
        Assert.assertEquals(reference.getMin(),           shuffled.getMax(), 1.0e-10);
        Assert.assertEquals(reference.getSum(),           shuffled.getMin(), 1.0e-10);
        Assert.assertEquals(reference.getSumsq(),         shuffled.getSum(), 1.0e-10);
        Assert.assertEquals(reference.getGeometricMean(), shuffled.getSumsq(), 1.0e-10);

    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testPercentileSetter
    public void testPercentileSetter() {
        DescriptiveStatistics stats = createDescriptiveStatistics();
        stats.addValue(1);
        stats.addValue(2);
        stats.addValue(3);
        Assert.assertEquals(2, stats.getPercentile(50.0), 1E-10);

        
        stats.setPercentileImpl(new goodPercentile());
        Assert.assertEquals(2, stats.getPercentile(50.0), 1E-10);

        
        stats.setPercentileImpl(new subPercentile());
        Assert.assertEquals(10.0, stats.getPercentile(10.0), 1E-10);

        
        try {
            stats.setPercentileImpl(new badPercentile());
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::test20090720
    public void test20090720() {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(100);
        for (int i = 0; i < 161; i++) {
            descriptiveStatistics.addValue(1.2);
        }
        descriptiveStatistics.clear();
        descriptiveStatistics.addValue(1.2);
        Assert.assertEquals(1, descriptiveStatistics.getN());
    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testRemoval
    public void testRemoval() {

        final DescriptiveStatistics dstat = createDescriptiveStatistics();

        checkremoval(dstat, 1, 6.0, 0.0, Double.NaN);
        checkremoval(dstat, 3, 5.0, 3.0, 4.5);
        checkremoval(dstat, 6, 3.5, 2.5, 3.0);
        checkremoval(dstat, 9, 3.5, 2.5, 3.0);
        checkremoval(dstat, DescriptiveStatistics.INFINITE_WINDOW, 3.5, 2.5, 3.0);

    }

// org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest::testSummaryConsistency
    public void testSummaryConsistency() {
        final DescriptiveStatistics dstats = new DescriptiveStatistics();
        final SummaryStatistics sstats = new SummaryStatistics();
        final int windowSize = 5;
        dstats.setWindowSize(windowSize);
        final double tol = 1E-12;
        for (int i = 0; i < 20; i++) {
            dstats.addValue(i);
            sstats.clear();
            double[] values = dstats.getValues();
            for (int j = 0; j < values.length; j++) {
                sstats.addValue(values[j]);
            }
            TestUtils.assertEquals(dstats.getMean(), sstats.getMean(), tol);
            TestUtils.assertEquals(new Mean().evaluate(values), dstats.getMean(), tol);
            TestUtils.assertEquals(dstats.getMax(), sstats.getMax(), tol);
            TestUtils.assertEquals(new Max().evaluate(values), dstats.getMax(), tol);
            TestUtils.assertEquals(dstats.getGeometricMean(), sstats.getGeometricMean(), tol);
            TestUtils.assertEquals(new GeometricMean().evaluate(values), dstats.getGeometricMean(), tol);
            TestUtils.assertEquals(dstats.getMin(), sstats.getMin(), tol);
            TestUtils.assertEquals(new Min().evaluate(values), dstats.getMin(), tol);
            TestUtils.assertEquals(dstats.getStandardDeviation(), sstats.getStandardDeviation(), tol);
            TestUtils.assertEquals(dstats.getVariance(), sstats.getVariance(), tol);
            TestUtils.assertEquals(new Variance().evaluate(values), dstats.getVariance(), tol);
            TestUtils.assertEquals(dstats.getSum(), sstats.getSum(), tol);
            TestUtils.assertEquals(new Sum().evaluate(values), dstats.getSum(), tol);
            TestUtils.assertEquals(dstats.getSumsq(), sstats.getSumsq(), tol);
            TestUtils.assertEquals(new SumOfSquares().evaluate(values), dstats.getSumsq(), tol);
            TestUtils.assertEquals(dstats.getPopulationVariance(), sstats.getPopulationVariance(), tol);
            TestUtils.assertEquals(new Variance(false).evaluate(values), dstats.getPopulationVariance(), tol);
        }
    }

// org.apache.commons.math3.stat.descriptive.ListUnivariateImplTest::testStats
    public void testStats() {
        List<Object> externalList = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl( externalList );

        Assert.assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(two);
        u.addValue(two);
        u.addValue(three);
        Assert.assertEquals("N",n,u.getN(),tolerance);
        Assert.assertEquals("sum",sum,u.getSum(),tolerance);
        Assert.assertEquals("sumsq",sumSq,u.getSumsq(),tolerance);
        Assert.assertEquals("var",var,u.getVariance(),tolerance);
        Assert.assertEquals("std",std,u.getStandardDeviation(),tolerance);
        Assert.assertEquals("mean",mean,u.getMean(),tolerance);
        Assert.assertEquals("min",min,u.getMin(),tolerance);
        Assert.assertEquals("max",max,u.getMax(),tolerance);
        u.clear();
        Assert.assertEquals("total count",0,u.getN(),tolerance);
    }

// org.apache.commons.math3.stat.descriptive.ListUnivariateImplTest::testN0andN1Conditions
    public void testN0andN1Conditions() {
        List<Object> list = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl( list );

        Assert.assertTrue("Mean of n = 0 set should be NaN", Double.isNaN( u.getMean() ) );
        Assert.assertTrue("Standard Deviation of n = 0 set should be NaN", Double.isNaN( u.getStandardDeviation() ) );
        Assert.assertTrue("Variance of n = 0 set should be NaN", Double.isNaN(u.getVariance() ) );

        list.add( Double.valueOf(one));

        Assert.assertTrue( "Mean of n = 1 set should be value of single item n1", u.getMean() == one);
        Assert.assertTrue( "StdDev of n = 1 set should be zero, instead it is: " + u.getStandardDeviation(), u.getStandardDeviation() == 0);
        Assert.assertTrue( "Variance of n = 1 set should be zero", u.getVariance() == 0);
    }

// org.apache.commons.math3.stat.descriptive.ListUnivariateImplTest::testSkewAndKurtosis
    public void testSkewAndKurtosis() {
        DescriptiveStatistics u = new DescriptiveStatistics();

        double[] testArray = { 12.5, 12, 11.8, 14.2, 14.9, 14.5, 21, 8.2, 10.3, 11.3, 14.1,
                                             9.9, 12.2, 12, 12.1, 11, 19.8, 11, 10, 8.8, 9, 12.3 };
        for( int i = 0; i < testArray.length; i++) {
            u.addValue( testArray[i]);
        }

        Assert.assertEquals("mean", 12.40455, u.getMean(), 0.0001);
        Assert.assertEquals("variance", 10.00236, u.getVariance(), 0.0001);
        Assert.assertEquals("skewness", 1.437424, u.getSkewness(), 0.0001);
        Assert.assertEquals("kurtosis", 2.37719, u.getKurtosis(), 0.0001);
    }

// org.apache.commons.math3.stat.descriptive.ListUnivariateImplTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() {
        ListUnivariateImpl u = new ListUnivariateImpl(new ArrayList<Object>());
        u.setWindowSize(10);

        u.addValue( 1.0 );
        u.addValue( 2.0 );
        u.addValue( 3.0 );
        u.addValue( 4.0 );

        Assert.assertEquals( "Geometric mean not expected", 2.213364, u.getGeometricMean(), 0.00001 );

        
        
        for( int i = 0; i < 10; i++ ) {
            u.addValue( i + 2 );
        }
        

        Assert.assertEquals( "Geometric mean not expected", 5.755931, u.getGeometricMean(), 0.00001 );

    }

// org.apache.commons.math3.stat.descriptive.ListUnivariateImplTest::testSerialization
    public void testSerialization() {

        DescriptiveStatistics u = new ListUnivariateImpl();

        Assert.assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(two);

        DescriptiveStatistics u2 = (DescriptiveStatistics)TestUtils.serializeAndRecover(u);

        u2.addValue(two);
        u2.addValue(three);

        Assert.assertEquals("N",n,u2.getN(),tolerance);
        Assert.assertEquals("sum",sum,u2.getSum(),tolerance);
        Assert.assertEquals("sumsq",sumSq,u2.getSumsq(),tolerance);
        Assert.assertEquals("var",var,u2.getVariance(),tolerance);
        Assert.assertEquals("std",std,u2.getStandardDeviation(),tolerance);
        Assert.assertEquals("mean",mean,u2.getMean(),tolerance);
        Assert.assertEquals("min",min,u2.getMin(),tolerance);
        Assert.assertEquals("max",max,u2.getMax(),tolerance);

        u2.clear();
        Assert.assertEquals("total count",0,u2.getN(),tolerance);
    }

// org.apache.commons.math3.stat.descriptive.MixedListUnivariateImplTest::testStats
    public void testStats() {
        List<Object> externalList = new ArrayList<Object>();

        DescriptiveStatistics u = new ListUnivariateImpl(externalList,transformers);

        Assert.assertEquals("total count", 0, u.getN(), tolerance);
        u.addValue(one);
        u.addValue(two);
        u.addValue(two);
        u.addValue(three);
        Assert.assertEquals("N", n, u.getN(), tolerance);
        Assert.assertEquals("sum", sum, u.getSum(), tolerance);
        Assert.assertEquals("sumsq", sumSq, u.getSumsq(), tolerance);
        Assert.assertEquals("var", var, u.getVariance(), tolerance);
        Assert.assertEquals("std", std, u.getStandardDeviation(), tolerance);
        Assert.assertEquals("mean", mean, u.getMean(), tolerance);
        Assert.assertEquals("min", min, u.getMin(), tolerance);
        Assert.assertEquals("max", max, u.getMax(), tolerance);
        u.clear();
        Assert.assertEquals("total count", 0, u.getN(), tolerance);
    }

// org.apache.commons.math3.stat.descriptive.MixedListUnivariateImplTest::testN0andN1Conditions
    public void testN0andN1Conditions() {
        DescriptiveStatistics u = new ListUnivariateImpl(new ArrayList<Object>(),transformers);

        Assert.assertTrue(
            "Mean of n = 0 set should be NaN",
            Double.isNaN(u.getMean()));
        Assert.assertTrue(
            "Standard Deviation of n = 0 set should be NaN",
            Double.isNaN(u.getStandardDeviation()));
        Assert.assertTrue(
            "Variance of n = 0 set should be NaN",
            Double.isNaN(u.getVariance()));

        u.addValue(one);

        Assert.assertTrue(
            "Mean of n = 1 set should be value of single item n1, instead it is " + u.getMean() ,
            u.getMean() == one);

        Assert.assertTrue(
            "StdDev of n = 1 set should be zero, instead it is: "
                + u.getStandardDeviation(),
            u.getStandardDeviation() == 0);
        Assert.assertTrue(
            "Variance of n = 1 set should be zero",
            u.getVariance() == 0);
    }

// org.apache.commons.math3.stat.descriptive.MixedListUnivariateImplTest::testSkewAndKurtosis
    public void testSkewAndKurtosis() {
        ListUnivariateImpl u =
            new ListUnivariateImpl(new ArrayList<Object>(), transformers);

        u.addObject("12.5");
        u.addObject(Integer.valueOf(12));
        u.addObject("11.8");
        u.addObject("14.2");
        u.addObject(new Foo());
        u.addObject("14.5");
        u.addObject(Long.valueOf(21));
        u.addObject("8.2");
        u.addObject("10.3");
        u.addObject("11.3");
        u.addObject(Float.valueOf(14.1f));
        u.addObject("9.9");
        u.addObject("12.2");
        u.addObject(new Bar());
        u.addObject("12.1");
        u.addObject("11");
        u.addObject(Double.valueOf(19.8));
        u.addObject("11");
        u.addObject("10");
        u.addObject("8.8");
        u.addObject("9");
        u.addObject("12.3");

        Assert.assertEquals("mean", 12.40455, u.getMean(), 0.0001);
        Assert.assertEquals("variance", 10.00236, u.getVariance(), 0.0001);
        Assert.assertEquals("skewness", 1.437424, u.getSkewness(), 0.0001);
        Assert.assertEquals("kurtosis", 2.37719, u.getKurtosis(), 0.0001);
    }

// org.apache.commons.math3.stat.descriptive.MixedListUnivariateImplTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() {
        ListUnivariateImpl u = new ListUnivariateImpl(new ArrayList<Object>(),transformers);
        u.setWindowSize(10);

        u.addValue(1.0);
        u.addValue(2.0);
        u.addValue(3.0);
        u.addValue(4.0);

        Assert.assertEquals(
            "Geometric mean not expected",
            2.213364,
            u.getGeometricMean(),
            0.00001);

        
        
        for (int i = 0; i < 10; i++) {
            u.addValue(i + 2);
        }
        
        Assert.assertEquals(
            "Geometric mean not expected",
            5.755931,
            u.getGeometricMean(),
            0.00001);

    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        u.setMeanImpl(new StorelessUnivariateStatistic[] {
                        new sumMean(), new sumMean()
                      });
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        Assert.assertEquals(4, u.getMean()[0], 1E-14);
        Assert.assertEquals(6, u.getMean()[1], 1E-14);
        u.clear();
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        Assert.assertEquals(4, u.getMean()[0], 1E-14);
        Assert.assertEquals(6, u.getMean()[1], 1E-14);
        u.clear();
        u.setMeanImpl(new StorelessUnivariateStatistic[] {
                        new Mean(), new Mean()
                      }); 
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        Assert.assertEquals(2, u.getMean()[0], 1E-14);
        Assert.assertEquals(3, u.getMean()[1], 1E-14);
        Assert.assertEquals(2, u.getDimension());
    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        try {
            u.setMeanImpl(new StorelessUnivariateStatistic[] {
                            new sumMean(), new sumMean()
                          });
            Assert.fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testToString
    public void testToString() {
        MultivariateSummaryStatistics stats = createMultivariateSummaryStatistics(2, true);
        stats.addValue(new double[] {1, 3});
        stats.addValue(new double[] {2, 2});
        stats.addValue(new double[] {3, 1});
        Locale d = Locale.getDefault();
        Locale.setDefault(Locale.US);
        final String suffix = System.getProperty("line.separator");
        Assert.assertEquals("MultivariateSummaryStatistics:" + suffix+
                     "n: 3" +suffix+
                     "min: 1.0, 1.0" +suffix+
                     "max: 3.0, 3.0" +suffix+
                     "mean: 2.0, 2.0" +suffix+
                     "geometric mean: 1.817..., 1.817..." +suffix+
                     "sum of squares: 14.0, 14.0" +suffix+
                     "sum of logarithms: 1.791..., 1.791..." +suffix+
                     "standard deviation: 1.0, 1.0" +suffix+
                     "covariance: Array2DRowRealMatrix{{1.0,-1.0},{-1.0,1.0}}" +suffix,
                     stats.toString().replaceAll("([0-9]+\\.[0-9][0-9][0-9])[0-9]+", "$1..."));
        Locale.setDefault(d);
    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testShuffledStatistics
    public void testShuffledStatistics() {
        
        
        
        MultivariateSummaryStatistics reference = createMultivariateSummaryStatistics(2, true);
        MultivariateSummaryStatistics shuffled  = createMultivariateSummaryStatistics(2, true);

        StorelessUnivariateStatistic[] tmp = shuffled.getGeoMeanImpl();
        shuffled.setGeoMeanImpl(shuffled.getMeanImpl());
        shuffled.setMeanImpl(shuffled.getMaxImpl());
        shuffled.setMaxImpl(shuffled.getMinImpl());
        shuffled.setMinImpl(shuffled.getSumImpl());
        shuffled.setSumImpl(shuffled.getSumsqImpl());
        shuffled.setSumsqImpl(shuffled.getSumLogImpl());
        shuffled.setSumLogImpl(tmp);

        for (int i = 100; i > 0; --i) {
            reference.addValue(new double[] {i, i});
            shuffled.addValue(new double[] {i, i});
        }

        TestUtils.assertEquals(reference.getMean(),          shuffled.getGeometricMean(), 1.0e-10);
        TestUtils.assertEquals(reference.getMax(),           shuffled.getMean(),          1.0e-10);
        TestUtils.assertEquals(reference.getMin(),           shuffled.getMax(),           1.0e-10);
        TestUtils.assertEquals(reference.getSum(),           shuffled.getMin(),           1.0e-10);
        TestUtils.assertEquals(reference.getSumSq(),         shuffled.getSum(),           1.0e-10);
        TestUtils.assertEquals(reference.getSumLog(),        shuffled.getSumSq(),         1.0e-10);
        TestUtils.assertEquals(reference.getGeometricMean(), shuffled.getSumLog(),        1.0e-10);

    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testDimension
    public void testDimension() {
        try {
            createMultivariateSummaryStatistics(2, true).addValue(new double[3]);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException dme) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testStats
    public void testStats() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        Assert.assertEquals(0, u.getN());
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 2, 3 });
        u.addValue(new double[] { 2, 3 });
        u.addValue(new double[] { 3, 4 });
        Assert.assertEquals( 4, u.getN());
        Assert.assertEquals( 8, u.getSum()[0], 1.0e-10);
        Assert.assertEquals(12, u.getSum()[1], 1.0e-10);
        Assert.assertEquals(18, u.getSumSq()[0], 1.0e-10);
        Assert.assertEquals(38, u.getSumSq()[1], 1.0e-10);
        Assert.assertEquals( 1, u.getMin()[0], 1.0e-10);
        Assert.assertEquals( 2, u.getMin()[1], 1.0e-10);
        Assert.assertEquals( 3, u.getMax()[0], 1.0e-10);
        Assert.assertEquals( 4, u.getMax()[1], 1.0e-10);
        Assert.assertEquals(2.4849066497880003102, u.getSumLog()[0], 1.0e-10);
        Assert.assertEquals( 4.276666119016055311, u.getSumLog()[1], 1.0e-10);
        Assert.assertEquals( 1.8612097182041991979, u.getGeometricMean()[0], 1.0e-10);
        Assert.assertEquals( 2.9129506302439405217, u.getGeometricMean()[1], 1.0e-10);
        Assert.assertEquals( 2, u.getMean()[0], 1.0e-10);
        Assert.assertEquals( 3, u.getMean()[1], 1.0e-10);
        Assert.assertEquals(FastMath.sqrt(2.0 / 3.0), u.getStandardDeviation()[0], 1.0e-10);
        Assert.assertEquals(FastMath.sqrt(2.0 / 3.0), u.getStandardDeviation()[1], 1.0e-10);
        Assert.assertEquals(2.0 / 3.0, u.getCovariance().getEntry(0, 0), 1.0e-10);
        Assert.assertEquals(2.0 / 3.0, u.getCovariance().getEntry(0, 1), 1.0e-10);
        Assert.assertEquals(2.0 / 3.0, u.getCovariance().getEntry(1, 0), 1.0e-10);
        Assert.assertEquals(2.0 / 3.0, u.getCovariance().getEntry(1, 1), 1.0e-10);
        u.clear();
        Assert.assertEquals(0, u.getN());
    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testN0andN1Conditions
    public void testN0andN1Conditions() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(1, true);
        Assert.assertTrue(Double.isNaN(u.getMean()[0]));
        Assert.assertTrue(Double.isNaN(u.getStandardDeviation()[0]));

        
        u.addValue(new double[] { 1 });
        Assert.assertEquals(1.0, u.getMean()[0], 1.0e-10);
        Assert.assertEquals(1.0, u.getGeometricMean()[0], 1.0e-10);
        Assert.assertEquals(0.0, u.getStandardDeviation()[0], 1.0e-10);

        
        u.addValue(new double[] { 2 });
        Assert.assertTrue(u.getStandardDeviation()[0] > 0);

    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testNaNContracts
    public void testNaNContracts() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(1, true);
        Assert.assertTrue(Double.isNaN(u.getMean()[0]));
        Assert.assertTrue(Double.isNaN(u.getMin()[0]));
        Assert.assertTrue(Double.isNaN(u.getStandardDeviation()[0]));
        Assert.assertTrue(Double.isNaN(u.getGeometricMean()[0]));

        u.addValue(new double[] { 1.0 });
        Assert.assertFalse(Double.isNaN(u.getMean()[0]));
        Assert.assertFalse(Double.isNaN(u.getMin()[0]));
        Assert.assertFalse(Double.isNaN(u.getStandardDeviation()[0]));
        Assert.assertFalse(Double.isNaN(u.getGeometricMean()[0]));

    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testSerialization
    public void testSerialization() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        
        TestUtils.checkSerializedEquality(u);
        MultivariateSummaryStatistics s = (MultivariateSummaryStatistics) TestUtils.serializeAndRecover(u);
        Assert.assertEquals(u, s);

        
        u.addValue(new double[] { 2d, 1d });
        u.addValue(new double[] { 1d, 1d });
        u.addValue(new double[] { 3d, 1d });
        u.addValue(new double[] { 4d, 1d });
        u.addValue(new double[] { 5d, 1d });

        
        TestUtils.checkSerializedEquality(u);
        s = (MultivariateSummaryStatistics) TestUtils.serializeAndRecover(u);
        Assert.assertEquals(u, s);

    }

// org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        MultivariateSummaryStatistics u = createMultivariateSummaryStatistics(2, true);
        MultivariateSummaryStatistics t = null;
        int emptyHash = u.hashCode();
        Assert.assertTrue(u.equals(u));
        Assert.assertFalse(u.equals(t));
        Assert.assertFalse(u.equals(Double.valueOf(0)));
        t = createMultivariateSummaryStatistics(2, true);
        Assert.assertTrue(t.equals(u));
        Assert.assertTrue(u.equals(t));
        Assert.assertEquals(emptyHash, t.hashCode());

        
        u.addValue(new double[] { 2d, 1d });
        u.addValue(new double[] { 1d, 1d });
        u.addValue(new double[] { 3d, 1d });
        u.addValue(new double[] { 4d, 1d });
        u.addValue(new double[] { 5d, 1d });
        Assert.assertFalse(t.equals(u));
        Assert.assertFalse(u.equals(t));
        Assert.assertTrue(u.hashCode() != t.hashCode());

        
        t.addValue(new double[] { 2d, 1d });
        t.addValue(new double[] { 1d, 1d });
        t.addValue(new double[] { 3d, 1d });
        t.addValue(new double[] { 4d, 1d });
        t.addValue(new double[] { 5d, 1d });
        Assert.assertTrue(t.equals(u));
        Assert.assertTrue(u.equals(t));
        Assert.assertEquals(u.hashCode(), t.hashCode());

        
        u.clear();
        t.clear();
        Assert.assertTrue(t.equals(u));
        Assert.assertTrue(u.equals(t));
        Assert.assertEquals(emptyHash, t.hashCode());
        Assert.assertEquals(emptyHash, u.hashCode());
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testStats
    public void testStats() {
        SummaryStatistics u = createSummaryStatistics();
        Assert.assertEquals("total count",0,u.getN(),tolerance);
        u.addValue(one);
        u.addValue(twoF);
        u.addValue(twoL);
        u.addValue(three);
        Assert.assertEquals("N",n,u.getN(),tolerance);
        Assert.assertEquals("sum",sum,u.getSum(),tolerance);
        Assert.assertEquals("sumsq",sumSq,u.getSumsq(),tolerance);
        Assert.assertEquals("var",var,u.getVariance(),tolerance);
        Assert.assertEquals("population var",popVar,u.getPopulationVariance(),tolerance);
        Assert.assertEquals("std",std,u.getStandardDeviation(),tolerance);
        Assert.assertEquals("mean",mean,u.getMean(),tolerance);
        Assert.assertEquals("min",min,u.getMin(),tolerance);
        Assert.assertEquals("max",max,u.getMax(),tolerance);
        u.clear();
        Assert.assertEquals("total count",0,u.getN(),tolerance);
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testN0andN1Conditions
    public void testN0andN1Conditions() {
        SummaryStatistics u = createSummaryStatistics();
        Assert.assertTrue("Mean of n = 0 set should be NaN",
                Double.isNaN( u.getMean() ) );
        Assert.assertTrue("Standard Deviation of n = 0 set should be NaN",
                Double.isNaN( u.getStandardDeviation() ) );
        Assert.assertTrue("Variance of n = 0 set should be NaN",
                Double.isNaN(u.getVariance() ) );

        
        u.addValue(one);
        Assert.assertTrue("mean should be one (n = 1)",
                u.getMean() == one);
        Assert.assertTrue("geometric should be one (n = 1) instead it is " + u.getGeometricMean(),
                u.getGeometricMean() == one);
        Assert.assertTrue("Std should be zero (n = 1)",
                u.getStandardDeviation() == 0.0);
        Assert.assertTrue("variance should be zero (n = 1)",
                u.getVariance() == 0.0);

        
        u.addValue(twoF);
        Assert.assertTrue("Std should not be zero (n = 2)",
                u.getStandardDeviation() != 0.0);
        Assert.assertTrue("variance should not be zero (n = 2)",
                u.getVariance() != 0.0);

    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testProductAndGeometricMean
    public void testProductAndGeometricMean() {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue( 1.0 );
        u.addValue( 2.0 );
        u.addValue( 3.0 );
        u.addValue( 4.0 );

        Assert.assertEquals( "Geometric mean not expected", 2.213364,
                u.getGeometricMean(), 0.00001 );
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testNaNContracts
    public void testNaNContracts() {
        SummaryStatistics u = createSummaryStatistics();
        Assert.assertTrue("mean not NaN",Double.isNaN(u.getMean()));
        Assert.assertTrue("min not NaN",Double.isNaN(u.getMin()));
        Assert.assertTrue("std dev not NaN",Double.isNaN(u.getStandardDeviation()));
        Assert.assertTrue("var not NaN",Double.isNaN(u.getVariance()));
        Assert.assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        u.addValue(1.0);

        Assert.assertEquals( "mean not expected", 1.0,
                u.getMean(), Double.MIN_VALUE);
        Assert.assertEquals( "variance not expected", 0.0,
                u.getVariance(), Double.MIN_VALUE);
        Assert.assertEquals( "geometric mean not expected", 1.0,
                u.getGeometricMean(), Double.MIN_VALUE);

        u.addValue(-1.0);

        Assert.assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        u.addValue(0.0);

        Assert.assertTrue("geom mean not NaN",Double.isNaN(u.getGeometricMean()));

        
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testGetSummary
    public void testGetSummary() {
        SummaryStatistics u = createSummaryStatistics();
        StatisticalSummary summary = u.getSummary();
        verifySummary(u, summary);
        u.addValue(1d);
        summary = u.getSummary();
        verifySummary(u, summary);
        u.addValue(2d);
        summary = u.getSummary();
        verifySummary(u, summary);
        u.addValue(2d);
        summary = u.getSummary();
        verifySummary(u, summary);
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testSerialization
    public void testSerialization() {
        SummaryStatistics u = createSummaryStatistics();
        
        TestUtils.checkSerializedEquality(u);
        SummaryStatistics s = (SummaryStatistics) TestUtils.serializeAndRecover(u);
        StatisticalSummary summary = s.getSummary();
        verifySummary(u, summary);

        
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        u.addValue(5d);

        
        TestUtils.checkSerializedEquality(u);
        s = (SummaryStatistics) TestUtils.serializeAndRecover(u);
        summary = s.getSummary();
        verifySummary(u, summary);

    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        SummaryStatistics u = createSummaryStatistics();
        SummaryStatistics t = null;
        int emptyHash = u.hashCode();
        Assert.assertTrue("reflexive", u.equals(u));
        Assert.assertFalse("non-null compared to null", u.equals(t));
        Assert.assertFalse("wrong type", u.equals(Double.valueOf(0)));
        t = createSummaryStatistics();
        Assert.assertTrue("empty instances should be equal", t.equals(u));
        Assert.assertTrue("empty instances should be equal", u.equals(t));
        Assert.assertEquals("empty hash code", emptyHash, t.hashCode());

        
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        Assert.assertFalse("different n's should make instances not equal", t.equals(u));
        Assert.assertFalse("different n's should make instances not equal", u.equals(t));
        Assert.assertTrue("different n's should make hashcodes different",
                u.hashCode() != t.hashCode());

        
        t.addValue(2d);
        t.addValue(1d);
        t.addValue(3d);
        t.addValue(4d);
        Assert.assertTrue("summaries based on same data should be equal", t.equals(u));
        Assert.assertTrue("summaries based on same data should be equal", u.equals(t));
        Assert.assertEquals("summaries based on same data should have same hashcodes",
                u.hashCode(), t.hashCode());

        
        u.clear();
        t.clear();
        Assert.assertTrue("empty instances should be equal", t.equals(u));
        Assert.assertTrue("empty instances should be equal", u.equals(t));
        Assert.assertEquals("empty hash code", emptyHash, t.hashCode());
        Assert.assertEquals("empty hash code", emptyHash, u.hashCode());
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testCopy
    public void testCopy() {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(2d);
        u.addValue(1d);
        u.addValue(3d);
        u.addValue(4d);
        SummaryStatistics v = new SummaryStatistics(u);
        Assert.assertEquals(u, v);
        Assert.assertEquals(v, u);

        
        u.addValue(7d);
        u.addValue(9d);
        u.addValue(11d);
        u.addValue(23d);
        v.addValue(7d);
        v.addValue(9d);
        v.addValue(11d);
        v.addValue(23d);
        Assert.assertEquals(u, v);
        Assert.assertEquals(v, u);

        
        u.clear();
        u.setSumImpl(new Sum());
        SummaryStatistics.copy(u,v);
        Assert.assertEquals(u.getSumImpl(), v.getSumImpl());

    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() {
        SummaryStatistics u = createSummaryStatistics();
        u.setMeanImpl(new Sum());
        u.setSumLogImpl(new Sum());
        u.addValue(1);
        u.addValue(3);
        Assert.assertEquals(4, u.getMean(), 1E-14);
        Assert.assertEquals(4, u.getSumOfLogs(), 1E-14);
        Assert.assertEquals(FastMath.exp(2), u.getGeometricMean(), 1E-14);
        u.clear();
        u.addValue(1);
        u.addValue(2);
        Assert.assertEquals(3, u.getMean(), 1E-14);
        u.clear();
        u.setMeanImpl(new Mean()); 
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() {
        SummaryStatistics u = createSummaryStatistics();
        u.addValue(1);
        u.addValue(3);
        try {
            u.setMeanImpl(new Sum());
            Assert.fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testOverrideVarianceWithMathClass
    public void testOverrideVarianceWithMathClass() {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setVarianceImpl(new Variance(false)); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new Variance(false)).evaluate(scores),stats.getVariance(), 0); 
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testOverrideMeanWithMathClass
    public void testOverrideMeanWithMathClass() {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setMeanImpl(new Mean()); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new Mean()).evaluate(scores),stats.getMean(), 0); 
    }

// org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest::testOverrideGeoMeanWithMathClass
    public void testOverrideGeoMeanWithMathClass() {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setGeoMeanImpl(new GeometricMean()); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new GeometricMean()).evaluate(scores),stats.getGeometricMean(), 0); 
    }

// org.apache.commons.math3.stat.descriptive.moment.GeometricMeanTest::testSpecialValues
    public void testSpecialValues() {
        GeometricMean mean = new GeometricMean();
        
        Assert.assertTrue(Double.isNaN(mean.getResult()));

        
        mean.increment(1d);
        Assert.assertFalse(Double.isNaN(mean.getResult()));

        
        mean.increment(0d);
        Assert.assertEquals(0d, mean.getResult(), 0);

        
        mean.increment(Double.POSITIVE_INFINITY);
        Assert.assertTrue(Double.isNaN(mean.getResult()));

        
        mean.clear();
        Assert.assertTrue(Double.isNaN(mean.getResult()));

        
        mean.increment(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, mean.getResult(), 0);

        
        mean.increment(-2d);
        Assert.assertTrue(Double.isNaN(mean.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.moment.InteractionTest::testInteraction
    public void testInteraction() {

        FourthMoment m4 = new FourthMoment();
        Mean m = new Mean(m4);
        Variance v = new Variance(m4);
        Skewness s= new Skewness(m4);
        Kurtosis k = new Kurtosis(m4);

        for (int i = 0; i < testArray.length; i++){
            m4.increment(testArray[i]);
        }

        Assert.assertEquals(mean,m.getResult(),tolerance);
        Assert.assertEquals(var,v.getResult(),tolerance);
        Assert.assertEquals(skew ,s.getResult(),tolerance);
        Assert.assertEquals(kurt,k.getResult(),tolerance);

    }

// org.apache.commons.math3.stat.descriptive.moment.KurtosisTest::testNaN
    public void testNaN() {
        Kurtosis kurt = new Kurtosis();
        Assert.assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        Assert.assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        Assert.assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        Assert.assertTrue(Double.isNaN(kurt.getResult()));
        kurt.increment(1d);
        Assert.assertFalse(Double.isNaN(kurt.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.moment.MeanTest::testSmallSamples
    public void testSmallSamples() {
        Mean mean = new Mean();
        Assert.assertTrue(Double.isNaN(mean.getResult()));
        mean.increment(1d);
        Assert.assertEquals(1d, mean.getResult(), 0);
    }

// org.apache.commons.math3.stat.descriptive.moment.MeanTest::testWeightedMean
    public void testWeightedMean() {
        Mean mean = new Mean();
        Assert.assertEquals(expectedWeightedValue(), mean.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());
        Assert.assertEquals(expectedValue(), mean.evaluate(testArray, identicalWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math3.stat.descriptive.moment.SemiVarianceTest::testInsufficientData
    public void testInsufficientData() {
        double[] nothing = null;
        SemiVariance sv = new SemiVariance();
        try {
            sv.evaluate(nothing);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException iae) {
        }

        try {
            sv.setVarianceDirection(SemiVariance.UPSIDE_VARIANCE);
            sv.evaluate(nothing);
            Assert.fail("null is not a valid data array.");
        } catch (MathIllegalArgumentException iae) {
        }
        nothing = new double[] {};
        Assert.assertTrue(Double.isNaN(sv.evaluate(nothing)));
    }

// org.apache.commons.math3.stat.descriptive.moment.SemiVarianceTest::testSingleDown
    public void testSingleDown() {
        SemiVariance sv = new SemiVariance();
        double[] values = { 50.0d };
        double singletest = sv.evaluate(values);
        Assert.assertEquals(0.0d, singletest, 0);
    }

// org.apache.commons.math3.stat.descriptive.moment.SemiVarianceTest::testSingleUp
    public void testSingleUp() {
        SemiVariance sv = new SemiVariance(SemiVariance.UPSIDE_VARIANCE);
        double[] values = { 50.0d };
        double singletest = sv.evaluate(values);
        Assert.assertEquals(0.0d, singletest, 0);
    }

// org.apache.commons.math3.stat.descriptive.moment.SemiVarianceTest::testSample
    public void testSample() {
        final double[] values = { -2.0d, 2.0d, 4.0d, -2.0d, 22.0d, 11.0d, 3.0d, 14.0d, 5.0d };
        final int length = values.length;
        final double mean = StatUtils.mean(values); 
        final SemiVariance sv = new SemiVariance();  
        final double downsideSemiVariance = sv.evaluate(values); 
        Assert.assertEquals(TestUtils.sumSquareDev(new double[] {-2d, 2d, 4d, -2d, 3d, 5d}, mean) / (length - 1),
                downsideSemiVariance, 1E-14);

        sv.setVarianceDirection(SemiVariance.UPSIDE_VARIANCE);
        final double upsideSemiVariance = sv.evaluate(values);
        Assert.assertEquals(TestUtils.sumSquareDev(new double[] {22d, 11d, 14d}, mean) / (length - 1),
                upsideSemiVariance, 1E-14);

        
        Assert.assertEquals(StatUtils.variance(values), downsideSemiVariance + upsideSemiVariance, 10e-12);
    }

// org.apache.commons.math3.stat.descriptive.moment.SemiVarianceTest::testPopulation
    public void testPopulation() {
        double[] values = { -2.0d, 2.0d, 4.0d, -2.0d, 22.0d, 11.0d, 3.0d, 14.0d, 5.0d };
        SemiVariance sv = new SemiVariance(false);

        double singletest = sv.evaluate(values);
        Assert.assertEquals(19.556d, singletest, 0.01d);

        sv.setVarianceDirection(SemiVariance.UPSIDE_VARIANCE);
        singletest = sv.evaluate(values);
        Assert.assertEquals(36.222d, singletest, 0.01d);
    }

// org.apache.commons.math3.stat.descriptive.moment.SemiVarianceTest::testNonMeanCutoffs
    public void testNonMeanCutoffs() {
        double[] values = { -2.0d, 2.0d, 4.0d, -2.0d, 22.0d, 11.0d, 3.0d, 14.0d, 5.0d };
        SemiVariance sv = new SemiVariance(false); 

        double singletest = sv.evaluate(values, 1.0d, SemiVariance.DOWNSIDE_VARIANCE, false, 0, values.length);
        Assert.assertEquals(TestUtils.sumSquareDev(new double[] { -2d, -2d }, 1.0d) / values.length,
                singletest, 0.01d);

        singletest = sv.evaluate(values, 3.0d, SemiVariance.UPSIDE_VARIANCE, false, 0, values.length);
        Assert.assertEquals(TestUtils.sumSquareDev(new double[] { 4d, 22d, 11d, 14d, 5d }, 3.0d) / values.length, singletest,
                0.01d);
    }

// org.apache.commons.math3.stat.descriptive.moment.SemiVarianceTest::testVarianceDecompMeanCutoff
    public void testVarianceDecompMeanCutoff() {
        double[] values = { -2.0d, 2.0d, 4.0d, -2.0d, 22.0d, 11.0d, 3.0d, 14.0d, 5.0d };
        double variance = StatUtils.variance(values);
        SemiVariance sv = new SemiVariance(true); 
        sv.setVarianceDirection(SemiVariance.DOWNSIDE_VARIANCE);
        final double lower = sv.evaluate(values);
        sv.setVarianceDirection(SemiVariance.UPSIDE_VARIANCE);
        final double upper = sv.evaluate(values);
        Assert.assertEquals(variance, lower + upper, 10e-12);
    }

// org.apache.commons.math3.stat.descriptive.moment.SemiVarianceTest::testVarianceDecompNonMeanCutoff
    public void testVarianceDecompNonMeanCutoff() {
        double[] values = { -2.0d, 2.0d, 4.0d, -2.0d, 22.0d, 11.0d, 3.0d, 14.0d, 5.0d };
        double target = 0;
        double totalSumOfSquares = TestUtils.sumSquareDev(values, target);
        SemiVariance sv = new SemiVariance(true); 
        sv.setVarianceDirection(SemiVariance.DOWNSIDE_VARIANCE);
        double lower = sv.evaluate(values, target);
        sv.setVarianceDirection(SemiVariance.UPSIDE_VARIANCE);
        double upper = sv.evaluate(values, target);
        Assert.assertEquals(totalSumOfSquares / (values.length - 1), lower + upper, 10e-12);
    }

// org.apache.commons.math3.stat.descriptive.moment.SemiVarianceTest::testNoVariance
    public void testNoVariance() {
        final double[] values = {100d, 100d, 100d, 100d};
        SemiVariance sv = new SemiVariance();
        Assert.assertEquals(0, sv.evaluate(values), 10E-12);
        Assert.assertEquals(0, sv.evaluate(values, 100d), 10E-12);
        Assert.assertEquals(0, sv.evaluate(values, 100d, SemiVariance.UPSIDE_VARIANCE, false, 0, values.length), 10E-12);
    }

// org.apache.commons.math3.stat.descriptive.moment.SkewnessTest::testNaN
    public void testNaN() {
        Skewness skew = new Skewness();
        Assert.assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        Assert.assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        Assert.assertTrue(Double.isNaN(skew.getResult()));
        skew.increment(1d);
        Assert.assertFalse(Double.isNaN(skew.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.moment.StandardDeviationTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        Assert.assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        Assert.assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math3.stat.descriptive.moment.StandardDeviationTest::testPopulation
    public void testPopulation() {
        double[] values = {-1.0d, 3.1d, 4.0d, -2.1d, 22d, 11.7d, 3d, 14d};
        double sigma = populationStandardDeviation(values);
        SecondMoment m = new SecondMoment();
        m.evaluate(values);  
        StandardDeviation s1 = new StandardDeviation();
        s1.setBiasCorrected(false);
        Assert.assertEquals(sigma, s1.evaluate(values), 1E-14);
        s1.incrementAll(values);
        Assert.assertEquals(sigma, s1.getResult(), 1E-14);
        s1 = new StandardDeviation(false, m);
        Assert.assertEquals(sigma, s1.getResult(), 1E-14);
        s1 = new StandardDeviation(false);
        Assert.assertEquals(sigma, s1.evaluate(values), 1E-14);
        s1.incrementAll(values);
        Assert.assertEquals(sigma, s1.getResult(), 1E-14);
    }

// org.apache.commons.math3.stat.descriptive.moment.VarianceTest::testNaN
    public void testNaN() {
        StandardDeviation std = new StandardDeviation();
        Assert.assertTrue(Double.isNaN(std.getResult()));
        std.increment(1d);
        Assert.assertEquals(0d, std.getResult(), 0);
    }

// org.apache.commons.math3.stat.descriptive.moment.VarianceTest::testPopulation
    public void testPopulation() {
        double[] values = {-1.0d, 3.1d, 4.0d, -2.1d, 22d, 11.7d, 3d, 14d};
        SecondMoment m = new SecondMoment();
        m.evaluate(values);  
        Variance v1 = new Variance();
        v1.setBiasCorrected(false);
        Assert.assertEquals(populationVariance(values), v1.evaluate(values), 1E-14);
        v1.incrementAll(values);
        Assert.assertEquals(populationVariance(values), v1.getResult(), 1E-14);
        v1 = new Variance(false, m);
        Assert.assertEquals(populationVariance(values), v1.getResult(), 1E-14);
        v1 = new Variance(false);
        Assert.assertEquals(populationVariance(values), v1.evaluate(values), 1E-14);
        v1.incrementAll(values);
        Assert.assertEquals(populationVariance(values), v1.getResult(), 1E-14);
    }

// org.apache.commons.math3.stat.descriptive.moment.VarianceTest::testWeightedVariance
    public void testWeightedVariance() {
        Variance variance = new Variance();
        Assert.assertEquals(expectedWeightedValue(),
                variance.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());

        
        Assert.assertEquals(expectedValue(),
                variance.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());

        
        
        Assert.assertEquals(expectedValue(),
                variance.evaluate(testArray, MathArrays.normalizeArray(identicalWeightsArray, testArray.length),
                        0, testArray.length), getTolerance());

    }

// org.apache.commons.math3.stat.descriptive.moment.VectorialMeanTest::testMismatch
    public void testMismatch() {
        try {
            new VectorialMean(8).increment(new double[5]);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            Assert.assertEquals(5, dme.getArgument());
            Assert.assertEquals(8, dme.getDimension());
        }
    }

// org.apache.commons.math3.stat.descriptive.moment.VectorialMeanTest::testSimplistic
    public void testSimplistic() {
        VectorialMean stat = new VectorialMean(2);
        stat.increment(new double[] {-1.0,  1.0});
        stat.increment(new double[] { 1.0, -1.0});
        double[] mean = stat.getResult();
        Assert.assertEquals(0.0, mean[0], 1.0e-12);
        Assert.assertEquals(0.0, mean[1], 1.0e-12);
    }

// org.apache.commons.math3.stat.descriptive.moment.VectorialMeanTest::testBasicStats
    public void testBasicStats() {

        VectorialMean stat = new VectorialMean(points[0].length);
        for (int i = 0; i < points.length; ++i) {
            stat.increment(points[i]);
        }

        Assert.assertEquals(points.length, stat.getN());

        double[] mean = stat.getResult();
        double[]   refMean = new double[] { 1.78, 1.62,  3.12};

        for (int i = 0; i < mean.length; ++i) {
            Assert.assertEquals(refMean[i], mean[i], 1.0e-12);
        }

    }

// org.apache.commons.math3.stat.descriptive.moment.VectorialMeanTest::testSerial
    public void testSerial() {
        VectorialMean stat = new VectorialMean(points[0].length);
        for (int i = 0; i < points.length; ++i) {
            stat.increment(points[i]);
        }
        Assert.assertEquals(stat, TestUtils.serializeAndRecover(stat));
    }

// org.apache.commons.math3.stat.descriptive.rank.MaxTest::testSpecialValues
    public void testSpecialValues() {
        double[] testArray = {0d, Double.NaN, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY};
        Max max = new Max();
        Assert.assertTrue(Double.isNaN(max.getResult()));
        max.increment(testArray[0]);
        Assert.assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[1]);
        Assert.assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[2]);
        Assert.assertEquals(0d, max.getResult(), 0);
        max.increment(testArray[3]);
        Assert.assertEquals(Double.POSITIVE_INFINITY, max.getResult(), 0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, max.evaluate(testArray), 0);
    }

// org.apache.commons.math3.stat.descriptive.rank.MaxTest::testNaNs
    public void testNaNs() {
        Max max = new Max();
        double nan = Double.NaN;
        Assert.assertEquals(3d, max.evaluate(new double[]{nan, 2d, 3d}), 0);
        Assert.assertEquals(3d, max.evaluate(new double[]{1d, nan, 3d}), 0);
        Assert.assertEquals(2d, max.evaluate(new double[]{1d, 2d, nan}), 0);
        Assert.assertTrue(Double.isNaN(max.evaluate(new double[]{nan, nan, nan})));
    }

// org.apache.commons.math3.stat.descriptive.rank.MinTest::testSpecialValues
    public void testSpecialValues() {
        double[] testArray = {0d, Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY};
        Min min = new Min();
        Assert.assertTrue(Double.isNaN(min.getResult()));
        min.increment(testArray[0]);
        Assert.assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[1]);
        Assert.assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[2]);
        Assert.assertEquals(0d, min.getResult(), 0);
        min.increment(testArray[3]);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, min.getResult(), 0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, min.evaluate(testArray), 0);
    }

// org.apache.commons.math3.stat.descriptive.rank.MinTest::testNaNs
    public void testNaNs() {
        Min min = new Min();
        double nan = Double.NaN;
        Assert.assertEquals(2d, min.evaluate(new double[]{nan, 2d, 3d}), 0);
        Assert.assertEquals(1d, min.evaluate(new double[]{1d, nan, 3d}), 0);
        Assert.assertEquals(1d, min.evaluate(new double[]{1d, 2d, nan}), 0);
        Assert.assertTrue(Double.isNaN(min.evaluate(new double[]{nan, nan, nan})));
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testHighPercentile
    public void testHighPercentile(){
        double[] d = new double[]{1, 2, 3};
        Percentile p = new Percentile(75);
        Assert.assertEquals(3.0, p.evaluate(d), 1.0e-5);
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testLowPercentile
    public void testLowPercentile() {
        double[] d = new double[] {0, 1};
        Percentile p = new Percentile(25);
        Assert.assertEquals(0d, p.evaluate(d), Double.MIN_VALUE);
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testPercentile
    public void testPercentile() {
        double[] d = new double[] {1, 3, 2, 4};
        Percentile p = new Percentile(30);
        Assert.assertEquals(1.5, p.evaluate(d), 1.0e-5);
        p.setQuantile(25);
        Assert.assertEquals(1.25, p.evaluate(d), 1.0e-5);
        p.setQuantile(75);
        Assert.assertEquals(3.75, p.evaluate(d), 1.0e-5);
        p.setQuantile(50);
        Assert.assertEquals(2.5, p.evaluate(d), 1.0e-5);

        
        try {
            p.evaluate(d, 0, d.length, -1.0);
            Assert.fail();
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            p.evaluate(d, 0, d.length, 101.0);
            Assert.fail();
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testNISTExample
    public void testNISTExample() {
        double[] d = new double[] {95.1772, 95.1567, 95.1937, 95.1959,
                95.1442, 95.0610,  95.1591, 95.1195, 95.1772, 95.0925, 95.1990, 95.1682
        };
        Percentile p = new Percentile(90);
        Assert.assertEquals(95.1981, p.evaluate(d), 1.0e-4);
        Assert.assertEquals(95.1990, p.evaluate(d,0,d.length, 100d), 0);
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::test5
    public void test5() {
        Percentile percentile = new Percentile(5);
        Assert.assertEquals(this.percentile5, percentile.evaluate(testArray), getTolerance());
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testNullEmpty
    public void testNullEmpty() {
        Percentile percentile = new Percentile(50);
        double[] nullArray = null;
        double[] emptyArray = new double[] {};
        try {
            percentile.evaluate(nullArray);
            Assert.fail("Expecting MathIllegalArgumentException for null array");
        } catch (MathIllegalArgumentException ex) {
            
        }
        Assert.assertTrue(Double.isNaN(percentile.evaluate(emptyArray)));
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testSingleton
    public void testSingleton() {
        Percentile percentile = new Percentile(50);
        double[] singletonArray = new double[] {1d};
        Assert.assertEquals(1d, percentile.evaluate(singletonArray), 0);
        Assert.assertEquals(1d, percentile.evaluate(singletonArray, 0, 1), 0);
        Assert.assertEquals(1d, percentile.evaluate(singletonArray, 0, 1, 5), 0);
        Assert.assertEquals(1d, percentile.evaluate(singletonArray, 0, 1, 100), 0);
        Assert.assertTrue(Double.isNaN(percentile.evaluate(singletonArray, 0, 0)));
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testSpecialValues
    public void testSpecialValues() {
        Percentile percentile = new Percentile(50);
        double[] specialValues = new double[] {0d, 1d, 2d, 3d, 4d,  Double.NaN};
        Assert.assertEquals(2.5d, percentile.evaluate(specialValues), 0);
        specialValues =  new double[] {Double.NEGATIVE_INFINITY, 1d, 2d, 3d,
                Double.NaN, Double.POSITIVE_INFINITY};
        Assert.assertEquals(2.5d, percentile.evaluate(specialValues), 0);
        specialValues = new double[] {1d, 1d, Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY};
        Assert.assertTrue(Double.isInfinite(percentile.evaluate(specialValues)));
        specialValues = new double[] {1d, 1d, Double.NaN,
                Double.NaN};
        Assert.assertTrue(Double.isNaN(percentile.evaluate(specialValues)));
        specialValues = new double[] {1d, 1d, Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY};
        
        Assert.assertTrue(Double.isNaN(percentile.evaluate(specialValues)));
    }

// org.apache.commons.math3.stat.descriptive.rank.PercentileTest::testSetQuantile
    public void testSetQuantile() {
        Percentile percentile = new Percentile(10);
        percentile.setQuantile(100); 
        Assert.assertEquals(100, percentile.getQuantile(), 0);
        try {
            percentile.setQuantile(0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            new Percentile(0);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.descriptive.summary.ProductTest::testSpecialValues
    public void testSpecialValues() {
        Product product = new Product();
        Assert.assertEquals(1, product.getResult(), 0);
        product.increment(1);
        Assert.assertEquals(1, product.getResult(), 0);
        product.increment(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, product.getResult(), 0);
        product.increment(Double.NEGATIVE_INFINITY);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, product.getResult(), 0);
        product.increment(Double.NaN);
        Assert.assertTrue(Double.isNaN(product.getResult()));
        product.increment(1);
        Assert.assertTrue(Double.isNaN(product.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.summary.ProductTest::testWeightedProduct
    public void testWeightedProduct() {
        Product product = new Product();
        Assert.assertEquals(expectedWeightedValue(), product.evaluate(testArray, testWeightsArray, 0, testArray.length),getTolerance());
        Assert.assertEquals(expectedValue(), product.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math3.stat.descriptive.summary.SumLogTest::testSpecialValues
    public void testSpecialValues() {
        SumOfLogs sum = new SumOfLogs();
        
        Assert.assertEquals(0, sum.getResult(), 0);

        
        sum.increment(1d);
        Assert.assertFalse(Double.isNaN(sum.getResult()));

        
        sum.increment(0d);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, sum.getResult(), 0);

        
        sum.increment(Double.POSITIVE_INFINITY);
        Assert.assertTrue(Double.isNaN(sum.getResult()));

        
        sum.clear();
        Assert.assertEquals(0, sum.getResult(), 0);

        
        sum.increment(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, sum.getResult(), 0);

        
        sum.increment(-2d);
        Assert.assertTrue(Double.isNaN(sum.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.summary.SumSqTest::testSpecialValues
    public void testSpecialValues() {
        SumOfSquares sumSq = new SumOfSquares();
        Assert.assertEquals(0, sumSq.getResult(), 0);
        sumSq.increment(2d);
        Assert.assertEquals(4d, sumSq.getResult(), 0);
        sumSq.increment(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, sumSq.getResult(), 0);
        sumSq.increment(Double.NEGATIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, sumSq.getResult(), 0);
        sumSq.increment(Double.NaN);
        Assert.assertTrue(Double.isNaN(sumSq.getResult()));
        sumSq.increment(1);
        Assert.assertTrue(Double.isNaN(sumSq.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.summary.SumTest::testSpecialValues
    public void testSpecialValues() {
        Sum sum = new Sum();
        Assert.assertEquals(0, sum.getResult(), 0);
        sum.increment(1);
        Assert.assertEquals(1, sum.getResult(), 0);
        sum.increment(Double.POSITIVE_INFINITY);
        Assert.assertEquals(Double.POSITIVE_INFINITY, sum.getResult(), 0);
        sum.increment(Double.NEGATIVE_INFINITY);
        Assert.assertTrue(Double.isNaN(sum.getResult()));
        sum.increment(1);
        Assert.assertTrue(Double.isNaN(sum.getResult()));
    }

// org.apache.commons.math3.stat.descriptive.summary.SumTest::testWeightedSum
    public void testWeightedSum() {
        Sum sum = new Sum();
        Assert.assertEquals(expectedWeightedValue(), sum.evaluate(testArray, testWeightsArray, 0, testArray.length), getTolerance());
        Assert.assertEquals(expectedValue(), sum.evaluate(testArray, unitWeightsArray, 0, testArray.length), getTolerance());
    }

// org.apache.commons.math3.stat.descriptive.summary.SumTest::testWeightedConsistency
    public void testWeightedConsistency() {}

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquare
    public void testChiSquare() {

        
        
        
        

        long[] observed = {10, 9, 11};
        double[] expected = {10, 10, 10};
        Assert.assertEquals("chi-square statistic", 0.2,  testStatistic.chiSquare(expected, observed), 10E-12);
        Assert.assertEquals("chi-square p-value", 0.904837418036, testStatistic.chiSquareTest(expected, observed), 1E-10);

        long[] observed1 = { 500, 623, 72, 70, 31 };
        double[] expected1 = { 485, 541, 82, 61, 37 };
        Assert.assertEquals( "chi-square test statistic", 9.023307936427388, testStatistic.chiSquare(expected1, observed1), 1E-10);
        Assert.assertEquals("chi-square p-value", 0.06051952647453607, testStatistic.chiSquareTest(expected1, observed1), 1E-9);
        Assert.assertTrue("chi-square test reject", testStatistic.chiSquareTest(expected1, observed1, 0.08));
        Assert.assertTrue("chi-square test accept", !testStatistic.chiSquareTest(expected1, observed1, 0.05));

        try {
            testStatistic.chiSquareTest(expected1, observed1, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            testStatistic.chiSquare(tooShortEx, tooShortObs);
            Assert.fail("arguments too short, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            testStatistic.chiSquare(unMatchedEx, unMatchedObs);
            Assert.fail("arrays have different lengths, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        expected[0] = 0;
        try {
            testStatistic.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            
        }

        
        expected[0] = 1;
        observed[0] = -1;
        try {
            testStatistic.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotPositiveException expected");
        } catch (NotPositiveException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareIndependence
    public void testChiSquareIndependence() {

        

        long[][] counts = { {40, 22, 43}, {91, 21, 28}, {60, 10, 22}};
        Assert.assertEquals( "chi-square test statistic", 22.709027688, testStatistic.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.000144751460134, testStatistic.chiSquareTest(counts), 1E-9);
        Assert.assertTrue("chi-square test reject", testStatistic.chiSquareTest(counts, 0.0002));
        Assert.assertTrue("chi-square test accept", !testStatistic.chiSquareTest(counts, 0.0001));

        long[][] counts2 = {{10, 15}, {30, 40}, {60, 90} };
        Assert.assertEquals( "chi-square test statistic", 0.168965517241, testStatistic.chiSquare(counts2), 1E-9);
        Assert.assertEquals("chi-square p-value",0.918987499852, testStatistic.chiSquareTest(counts2), 1E-9);
        Assert.assertTrue("chi-square test accept", !testStatistic.chiSquareTest(counts2, 0.1));

        
        long[][] counts3 = { {40, 22, 43}, {91, 21, 28}, {60, 10}};
        try {
            testStatistic.chiSquare(counts3);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts4 = {{40, 22, 43}};
        try {
            testStatistic.chiSquare(counts4);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            testStatistic.chiSquare(counts5);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            testStatistic.chiSquare(counts6);
            Assert.fail("Expecting NotPositiveException");
        } catch (NotPositiveException ex) {
            
        }

        
        try {
            testStatistic.chiSquareTest(counts, 0);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareLargeTestStatistic
    public void testChiSquareLargeTestStatistic() {
        double[] exp = new double[] {
            3389119.5, 649136.6, 285745.4, 25357364.76, 11291189.78, 543628.0,
            232921.0, 437665.75
        };

        long[] obs = new long[] {
            2372383, 584222, 257170, 17750155, 7903832, 489265, 209628, 393899
        };
        org.apache.commons.math3.stat.inference.ChiSquareTest csti =
            new org.apache.commons.math3.stat.inference.ChiSquareTest();
        double cst = csti.chiSquareTest(exp, obs);
        Assert.assertEquals("chi-square p-value", 0.0, cst, 1E-3);
        Assert.assertEquals( "chi-square test statistic",
                114875.90421929007, testStatistic.chiSquare(exp, obs), 1E-9);
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareZeroCount
    public void testChiSquareZeroCount() {
        
        long[][] counts = { {40, 0, 4}, {91, 1, 2}, {60, 2, 0}};
        Assert.assertEquals( "chi-square test statistic", 9.67444662263,
                testStatistic.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.0462835770603,
                testStatistic.chiSquareTest(counts), 1E-9);
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonEqualCounts
    public void testChiSquareDataSetsComparisonEqualCounts()
        {
        long[] observed1 = {10, 12, 12, 10};
        long[] observed2 = {5, 15, 14, 10};
        Assert.assertEquals("chi-square p value", 0.541096,
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertEquals("chi-square test statistic", 2.153846,
                testStatistic.chiSquareDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertFalse("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.4));
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonUnEqualCounts
    public void testChiSquareDataSetsComparisonUnEqualCounts()
        {
        long[] observed1 = {10, 12, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        Assert.assertEquals("chi-square p value", 0.124115,
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertEquals("chi-square test statistic", 7.232189,
                testStatistic.chiSquareDataSetsComparison(
                observed1, observed2), 1E-6);
        Assert.assertTrue("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.13));
        Assert.assertFalse("chi-square test result",
                testStatistic.chiSquareTestDataSetsComparison(
                observed1, observed2, 0.12));
    }

// org.apache.commons.math3.stat.inference.ChiSquareTestTest::testChiSquareDataSetsComparisonBadCounts
    public void testChiSquareDataSetsComparisonBadCounts()
        {
        long[] observed1 = {10, -1, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed1, observed2);
            Assert.fail("Expecting NotPositiveException - negative count");
        } catch (NotPositiveException ex) {
            
        }
        long[] observed3 = {10, 0, 12, 10, 15};
        long[] observed4 = {15, 0, 10, 15, 5};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed3, observed4);
            Assert.fail("Expecting ZeroException - double 0's");
        } catch (ZeroException ex) {
            
        }
        long[] observed5 = {10, 10, 12, 10, 15};
        long[] observed6 = {0, 0, 0, 0, 0};
        try {
            testStatistic.chiSquareTestDataSetsComparison(
                    observed5, observed6);
            Assert.fail("Expecting ZeroException - vanishing counts");
        } catch (ZeroException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestGoodnesOfFit1
    public void testGTestGoodnesOfFit1() throws Exception {
        final double[] exp = new double[]{
            3d, 1d
        };

        final long[] obs = new long[]{
            423, 133
        };

        Assert.assertEquals("G test statistic",
                0.348721, testStatistic.g(exp, obs), 1E-6);
        final double p_gtgf = testStatistic.gTest(exp, obs);
        Assert.assertEquals("g-Test p-value", 0.55483, p_gtgf, 1E-5);

        Assert.assertFalse(testStatistic.gTest(exp, obs, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestGoodnesOfFit2
    public void testGTestGoodnesOfFit2() throws Exception {
        final double[] exp = new double[]{
            0.54d, 0.40d, 0.05d, 0.01d
        };

        final long[] obs = new long[]{
            70, 79, 3, 4
        };
        Assert.assertEquals("G test statistic",
                13.144799, testStatistic.g(exp, obs), 1E-6);
        final double p_gtgf = testStatistic.gTest(exp, obs);
        Assert.assertEquals("g-Test p-value", 0.004333, p_gtgf, 1E-5);

        Assert.assertTrue(testStatistic.gTest(exp, obs, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestGoodnesOfFit3
    public void testGTestGoodnesOfFit3() throws Exception {
        final double[] exp = new double[]{
            0.167d, 0.483d, 0.350d
        };

        final long[] obs = new long[]{
            14, 21, 25
        };

        Assert.assertEquals("G test statistic",
                4.5554, testStatistic.g(exp, obs), 1E-4);
        
        final double p_gtgf = testStatistic.gTestIntrinsic(exp, obs);
        Assert.assertEquals("g-Test p-value", 0.0328, p_gtgf, 1E-4);

        Assert.assertFalse(testStatistic.gTest(exp, obs, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestIndependance1
    public void testGTestIndependance1() throws Exception {
        final long[] obs1 = new long[]{
            268, 199, 42
        };

        final long[] obs2 = new long[]{
            807, 759, 184
        };

        final double g = testStatistic.gDataSetsComparison(obs1, obs2);

        Assert.assertEquals("G test statistic",
                7.3008170, g, 1E-6);
        final double p_gti = testStatistic.gTestDataSetsComparison(obs1, obs2);

        Assert.assertEquals("g-Test p-value", 0.0259805, p_gti, 1E-6);
        Assert.assertTrue(testStatistic.gTestDataSetsComparison(obs1, obs2, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestIndependance2
    public void testGTestIndependance2() throws Exception {
        final long[] obs1 = new long[]{
            127, 99, 264
        };

        final long[] obs2 = new long[]{
            116, 67, 161
        };

        final double g = testStatistic.gDataSetsComparison(obs1, obs2);

        Assert.assertEquals("G test statistic",
                6.227288, g, 1E-6);
        final double p_gti = testStatistic.gTestDataSetsComparison(obs1, obs2);

        Assert.assertEquals("g-Test p-value", 0.04443, p_gti, 1E-5);
        Assert.assertTrue(testStatistic.gTestDataSetsComparison(obs1, obs2, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestIndependance3
    public void testGTestIndependance3() throws Exception {
        final long[] obs1 = new long[]{
            190, 149
        };

        final long[] obs2 = new long[]{
            42, 49
        };

        final double g = testStatistic.gDataSetsComparison(obs1, obs2);
        Assert.assertEquals("G test statistic",
                2.8187, g, 1E-4);
        final double p_gti = testStatistic.gTestDataSetsComparison(obs1, obs2);
        Assert.assertEquals("g-Test p-value", 0.09317325, p_gti, 1E-6);

        Assert.assertFalse(testStatistic.gTestDataSetsComparison(obs1, obs2, 0.05));
    }

// org.apache.commons.math3.stat.inference.GTestTest::testGTestSetsComparisonBadCounts
    public void testGTestSetsComparisonBadCounts() {
        long[] observed1 = {10, -1, 12, 10, 15};
        long[] observed2 = {15, 10, 10, 15, 5};
        try {
            testStatistic.gTestDataSetsComparison(
                    observed1, observed2);
            Assert.fail("Expecting NotPositiveException - negative count");
        } catch (NotPositiveException ex) {
            
        }
        long[] observed3 = {10, 0, 12, 10, 15};
        long[] observed4 = {15, 0, 10, 15, 5};
        try {
            testStatistic.gTestDataSetsComparison(
                    observed3, observed4);
            Assert.fail("Expecting ZeroException - double 0's");
        } catch (ZeroException ex) {
            
        }
        long[] observed5 = {10, 10, 12, 10, 15};
        long[] observed6 = {0, 0, 0, 0, 0};
        try {
            testStatistic.gTestDataSetsComparison(
                    observed5, observed6);
            Assert.fail("Expecting ZeroException - vanishing counts");
        } catch (ZeroException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.GTestTest::testUnmatchedArrays
    public void testUnmatchedArrays() {
        final long[] observed = { 0, 1, 2, 3 };
        final double[] expected = { 1, 1, 2 };
        final long[] observed2 = {3, 4};
        try {
            testStatistic.gTest(expected, observed);
            Assert.fail("arrays have different lengths, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }
        try {
            testStatistic.gTestDataSetsComparison(observed, observed2);
            Assert.fail("arrays have different lengths, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.GTestTest::testNegativeObservedCounts
    public void testNegativeObservedCounts() {
        final long[] observed = { 0, 1, 2, -3 };
        final double[] expected = { 1, 1, 2, 3};
        final long[] observed2 = {3, 4, 5, 0};
        try {
            testStatistic.gTest(expected, observed);
            Assert.fail("negative observed count, NotPositiveException expected");
        } catch (NotPositiveException ex) {
            
        }
        try {
            testStatistic.gTestDataSetsComparison(observed, observed2);
            Assert.fail("negative observed count, NotPositiveException expected");
        } catch (NotPositiveException ex) {
            
        } 
    }

// org.apache.commons.math3.stat.inference.GTestTest::testZeroExpectedCounts
    public void testZeroExpectedCounts() {
        final long[] observed = { 0, 1, 2, -3 };
        final double[] expected = { 1, 0, 2, 3};
        try {
            testStatistic.gTest(expected, observed);
            Assert.fail("zero expected count, NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.GTestTest::testBadAlpha
    public void testBadAlpha() {
        final long[] observed = { 0, 1, 2, 3 };
        final double[] expected = { 1, 2, 2, 3};
        final long[] observed2 = { 0, 2, 2, 3 };
        try {
            testStatistic.gTest(expected, observed, 0.8);
            Assert.fail("zero expected count, NotStrictlyPositiveException expected");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            testStatistic.gTestDataSetsComparison(observed, observed2, -0.5);
            Assert.fail("zero expected count, NotStrictlyPositiveException expected");
        } catch (OutOfRangeException ex) {
            
        }  
    }

// org.apache.commons.math3.stat.inference.GTestTest::testScaling
    public void testScaling() {
      final long[] observed = {9, 11, 10, 8, 12};
      final double[] expected1 = {10, 10, 10, 10, 10};
      final double[] expected2 = {1000, 1000, 1000, 1000, 1000};
      final double[] expected3 = {1, 1, 1, 1, 1};
      final double tol = 1E-15;
      Assert.assertEquals(
              testStatistic.gTest(expected1, observed),
              testStatistic.gTest(expected2, observed),
              tol);
      Assert.assertEquals(
              testStatistic.gTest(expected1, observed),
              testStatistic.gTest(expected3, observed),
              tol);
    }

// org.apache.commons.math3.stat.inference.GTestTest::testRootLogLikelihood
    public void testRootLogLikelihood() {
        
        Assert.assertTrue(testStatistic.rootLogLikelihoodRatio(904, 21060, 1144, 283012) > 0.0);

        
        Assert.assertTrue(testStatistic.rootLogLikelihoodRatio(36, 21928, 60280, 623876) < 0.0);

        Assert.assertEquals(Math.sqrt(2.772589), testStatistic.rootLogLikelihoodRatio(1, 0, 0, 1), 0.000001);
        Assert.assertEquals(-Math.sqrt(2.772589), testStatistic.rootLogLikelihoodRatio(0, 1, 1, 0), 0.000001);
        Assert.assertEquals(Math.sqrt(27.72589), testStatistic.rootLogLikelihoodRatio(10, 0, 0, 10), 0.00001);

        Assert.assertEquals(Math.sqrt(39.33052), testStatistic.rootLogLikelihoodRatio(5, 1995, 0, 100000), 0.00001);
        Assert.assertEquals(-Math.sqrt(39.33052), testStatistic.rootLogLikelihoodRatio(0, 100000, 5, 1995), 0.00001);

        Assert.assertEquals(Math.sqrt(4730.737), testStatistic.rootLogLikelihoodRatio(1000, 1995, 1000, 100000), 0.001);
        Assert.assertEquals(-Math.sqrt(4730.737), testStatistic.rootLogLikelihoodRatio(1000, 100000, 1000, 1995), 0.001);

        Assert.assertEquals(Math.sqrt(5734.343), testStatistic.rootLogLikelihoodRatio(1000, 1000, 1000, 100000), 0.001);
        Assert.assertEquals(Math.sqrt(5714.932), testStatistic.rootLogLikelihoodRatio(1000, 1000, 1000, 99000), 0.001);
    }

// org.apache.commons.math3.stat.inference.OneWayAnovaTest::testAnovaFValue
    public void testAnovaFValue() {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        Assert.assertEquals("ANOVA F-value",  24.67361709460624,
                 testStatistic.anovaFValue(threeClasses), 1E-12);

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        Assert.assertEquals("ANOVA F-value",  0.0150579150579,
                 testStatistic.anovaFValue(twoClasses), 1E-12);

        List<double[]> emptyContents = new ArrayList<double[]>();
        emptyContents.add(emptyArray);
        emptyContents.add(classC);
        try {
            testStatistic.anovaFValue(emptyContents);
            Assert.fail("empty array for key classX, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        List<double[]> tooFew = new ArrayList<double[]>();
        tooFew.add(classA);
        try {
            testStatistic.anovaFValue(tooFew);
            Assert.fail("less than two classes, MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.OneWayAnovaTest::testAnovaPValue
    public void testAnovaPValue() {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        Assert.assertEquals("ANOVA P-value", 6.959446E-06,
                 testStatistic.anovaPValue(threeClasses), 1E-12);

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        Assert.assertEquals("ANOVA P-value",  0.904212960464,
                 testStatistic.anovaPValue(twoClasses), 1E-12);

    }

// org.apache.commons.math3.stat.inference.OneWayAnovaTest::testAnovaPValueSummaryStatistics
    public void testAnovaPValueSummaryStatistics() {
        
        List<SummaryStatistics> threeClasses = new ArrayList<SummaryStatistics>();
        SummaryStatistics statsA = new SummaryStatistics();
        for (double a : classA) {
            statsA.addValue(a);
        }
        threeClasses.add(statsA);
        SummaryStatistics statsB = new SummaryStatistics();
        for (double b : classB) {
            statsB.addValue(b);
        }
        threeClasses.add(statsB);
        SummaryStatistics statsC = new SummaryStatistics();
        for (double c : classC) {
            statsC.addValue(c);
        }
        threeClasses.add(statsC);

        Assert.assertEquals("ANOVA P-value", 6.959446E-06,
                 testStatistic.anovaPValue(threeClasses, true), 1E-12);

        List<SummaryStatistics> twoClasses = new ArrayList<SummaryStatistics>();
        twoClasses.add(statsA);
        twoClasses.add(statsB);

        Assert.assertEquals("ANOVA P-value",  0.904212960464,
                 testStatistic.anovaPValue(twoClasses, false), 1E-12);

    }

// org.apache.commons.math3.stat.inference.OneWayAnovaTest::testAnovaTest
    public void testAnovaTest() {
        
        List<double[]> threeClasses = new ArrayList<double[]>();
        threeClasses.add(classA);
        threeClasses.add(classB);
        threeClasses.add(classC);

        Assert.assertTrue("ANOVA Test P<0.01", testStatistic.anovaTest(threeClasses, 0.01));

        List<double[]> twoClasses = new ArrayList<double[]>();
        twoClasses.add(classA);
        twoClasses.add(classB);

        Assert.assertFalse("ANOVA Test P>0.01", testStatistic.anovaTest(twoClasses, 0.01));
    }

// org.apache.commons.math3.stat.inference.TTestTest::testOneSampleT
    public void testOneSampleT() {
        double[] observed =
            {93.0, 103.0, 95.0, 101.0, 91.0, 105.0, 96.0, 94.0, 101.0,  88.0, 98.0, 94.0, 101.0, 92.0, 95.0 };
        double mu = 100.0;
        SummaryStatistics sampleStats = null;
        sampleStats = new SummaryStatistics();
        for (int i = 0; i < observed.length; i++) {
            sampleStats.addValue(observed[i]);
        }

        
        Assert.assertEquals("t statistic",  -2.81976445346,
                testStatistic.t(mu, observed), 10E-10);
        Assert.assertEquals("t statistic",  -2.81976445346,
                testStatistic.t(mu, sampleStats), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                testStatistic.tTest(mu, observed), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                testStatistic.tTest(mu, sampleStats), 10E-10);

        try {
            testStatistic.t(mu, (double[]) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, (SummaryStatistics) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            testStatistic.t(mu, emptyObs);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(mu, emptyStats);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(mu, tooShortObs);
            Assert.fail("insufficient data to compute t statistic, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            testStatistic.tTest(mu, tooShortObs);
            Assert.fail("insufficient data to perform t test, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
           
        }

        try {
            testStatistic.t(mu, tooShortStats);
            Assert.fail("insufficient data to compute t statistic, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            testStatistic.tTest(mu, tooShortStats);
            Assert.fail("insufficient data to perform t test, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TTestTest::testOneSampleTTest
    public void testOneSampleTTest() {
        double[] oneSidedP =
            {2d, 0d, 6d, 6d, 3d, 3d, 2d, 3d, -6d, 6d, 6d, 6d, 3d, 0d, 1d, 1d, 0d, 2d, 3d, 3d };
        SummaryStatistics oneSidedPStats = new SummaryStatistics();
        for (int i = 0; i < oneSidedP.length; i++) {
            oneSidedPStats.addValue(oneSidedP[i]);
        }
        
        Assert.assertEquals("one sample t stat", 3.86485535541,
                testStatistic.t(0d, oneSidedP), 10E-10);
        Assert.assertEquals("one sample t stat", 3.86485535541,
                testStatistic.t(0d, oneSidedPStats),1E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                testStatistic.tTest(0d, oneSidedP) / 2d, 10E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                testStatistic.tTest(0d, oneSidedPStats) / 2d, 10E-5);
        Assert.assertTrue("one sample t-test reject", testStatistic.tTest(0d, oneSidedP, 0.01));
        Assert.assertTrue("one sample t-test reject", testStatistic.tTest(0d, oneSidedPStats, 0.01));
        Assert.assertTrue("one sample t-test accept", !testStatistic.tTest(0d, oneSidedP, 0.0001));
        Assert.assertTrue("one sample t-test accept", !testStatistic.tTest(0d, oneSidedPStats, 0.0001));

        try {
            testStatistic.tTest(0d, oneSidedP, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            testStatistic.tTest(0d, oneSidedPStats, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.TTestTest::testTwoSampleTHeterscedastic
    public void testTwoSampleTHeterscedastic() {
        double[] sample1 = { 7d, -4d, 18d, 17d, -3d, -5d, 1d, 10d, 11d, -2d };
        double[] sample2 = { -1d, 12d, -1d, -3d, 3d, -5d, 5d, 2d, -11d, -1d, -3d };
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                testStatistic.t(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                testStatistic.t(sampleStats1, sampleStats2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                testStatistic.tTest(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                testStatistic.tTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                testStatistic.tTest(sample1, sample2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                testStatistic.tTest(sampleStats1, sampleStats2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !testStatistic.tTest(sample1, sample2, 0.1));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !testStatistic.tTest(sampleStats1, sampleStats2, 0.1));

        try {
            testStatistic.tTest(sample1, sample2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            testStatistic.tTest(sampleStats1, sampleStats2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            testStatistic.tTest(sample1, tooShortObs, .01);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.tTest(sampleStats1, tooShortStats, .01);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.tTest(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
           
        }

        try {
            testStatistic.tTest(sampleStats1, tooShortStats);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            testStatistic.t(sampleStats1, tooShortStats);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
           
        }
    }

// org.apache.commons.math3.stat.inference.TTestTest::testTwoSampleTHomoscedastic
    public void testTwoSampleTHomoscedastic() {
        double[] sample1 ={2, 4, 6, 8, 10, 97};
        double[] sample2 = {4, 6, 8, 10, 16};
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample homoscedastic t stat", 0.73096310086,
              testStatistic.homoscedasticT(sample1, sample2), 10E-11);
        Assert.assertEquals("two sample homoscedastic p value", 0.4833963785,
                testStatistic.homoscedasticTTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample homoscedastic t-test reject",
                testStatistic.homoscedasticTTest(sample1, sample2, 0.49));
        Assert.assertTrue("two sample homoscedastic t-test accept",
                !testStatistic.homoscedasticTTest(sample1, sample2, 0.48));
    }

// org.apache.commons.math3.stat.inference.TTestTest::testSmallSamples
    public void testSmallSamples() {
        double[] sample1 = {1d, 3d};
        double[] sample2 = {4d, 5d};

        
        Assert.assertEquals(-2.2360679775, testStatistic.t(sample1, sample2),
                1E-10);
        Assert.assertEquals(0.198727388935, testStatistic.tTest(sample1, sample2),
                1E-10);
    }

// org.apache.commons.math3.stat.inference.TTestTest::testPaired
    public void testPaired() {
        double[] sample1 = {1d, 3d, 5d, 7d};
        double[] sample2 = {0d, 6d, 11d, 2d};
        double[] sample3 = {5d, 7d, 8d, 10d};

        
        Assert.assertEquals(-0.3133, testStatistic.pairedT(sample1, sample2), 1E-4);
        Assert.assertEquals(0.774544295819, testStatistic.pairedTTest(sample1, sample2), 1E-10);
        Assert.assertEquals(0.001208, testStatistic.pairedTTest(sample1, sample3), 1E-6);
        Assert.assertFalse(testStatistic.pairedTTest(sample1, sample3, .001));
        Assert.assertTrue(testStatistic.pairedTTest(sample1, sample3, .002));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquare
    public void testChiSquare() {

        
        
        
        

        long[] observed = {10, 9, 11};
        double[] expected = {10, 10, 10};
        Assert.assertEquals("chi-square statistic", 0.2,  TestUtils.chiSquare(expected, observed), 10E-12);
        Assert.assertEquals("chi-square p-value", 0.904837418036, TestUtils.chiSquareTest(expected, observed), 1E-10);

        long[] observed1 = { 500, 623, 72, 70, 31 };
        double[] expected1 = { 485, 541, 82, 61, 37 };
        Assert.assertEquals( "chi-square test statistic", 9.023307936427388, TestUtils.chiSquare(expected1, observed1), 1E-10);
        Assert.assertEquals("chi-square p-value", 0.06051952647453607, TestUtils.chiSquareTest(expected1, observed1), 1E-9);
        Assert.assertTrue("chi-square test reject", TestUtils.chiSquareTest(expected1, observed1, 0.07));
        Assert.assertTrue("chi-square test accept", !TestUtils.chiSquareTest(expected1, observed1, 0.05));

        try {
            TestUtils.chiSquareTest(expected1, observed1, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        long[] tooShortObs = { 0 };
        double[] tooShortEx = { 1 };
        try {
            TestUtils.chiSquare(tooShortEx, tooShortObs);
            Assert.fail("arguments too short, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[] unMatchedObs = { 0, 1, 2, 3 };
        double[] unMatchedEx = { 1, 1, 2 };
        try {
            TestUtils.chiSquare(unMatchedEx, unMatchedObs);
            Assert.fail("arrays have different lengths, DimensionMismatchException expected");
        } catch (DimensionMismatchException ex) {
            
        }

        
        expected[0] = 0;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            
        }

        
        expected[0] = 1;
        observed[0] = -1;
        try {
            TestUtils.chiSquareTest(expected, observed, .01);
            Assert.fail("bad expected count, NotPositiveException expected");
        } catch (NotPositiveException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquareIndependence
    public void testChiSquareIndependence() {

        

        long[][] counts = { {40, 22, 43}, {91, 21, 28}, {60, 10, 22}};
        Assert.assertEquals( "chi-square test statistic", 22.709027688, TestUtils.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.000144751460134, TestUtils.chiSquareTest(counts), 1E-9);
        Assert.assertTrue("chi-square test reject", TestUtils.chiSquareTest(counts, 0.0002));
        Assert.assertTrue("chi-square test accept", !TestUtils.chiSquareTest(counts, 0.0001));

        long[][] counts2 = {{10, 15}, {30, 40}, {60, 90} };
        Assert.assertEquals( "chi-square test statistic", 0.168965517241, TestUtils.chiSquare(counts2), 1E-9);
        Assert.assertEquals("chi-square p-value",0.918987499852, TestUtils.chiSquareTest(counts2), 1E-9);
        Assert.assertTrue("chi-square test accept", !TestUtils.chiSquareTest(counts2, 0.1));

        
        long[][] counts3 = { {40, 22, 43}, {91, 21, 28}, {60, 10}};
        try {
            TestUtils.chiSquare(counts3);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts4 = {{40, 22, 43}};
        try {
            TestUtils.chiSquare(counts4);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }
        long[][] counts5 = {{40}, {40}, {30}, {10}};
        try {
            TestUtils.chiSquare(counts5);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }

        
        long[][] counts6 = {{10, -2}, {30, 40}, {60, 90} };
        try {
            TestUtils.chiSquare(counts6);
            Assert.fail("Expecting NotPositiveException");
        } catch (NotPositiveException ex) {
            
        }

        
        try {
            TestUtils.chiSquareTest(counts, 0);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquareLargeTestStatistic
    public void testChiSquareLargeTestStatistic() {
        double[] exp = new double[] {
                3389119.5, 649136.6, 285745.4, 25357364.76, 11291189.78, 543628.0,
                232921.0, 437665.75
        };

        long[] obs = new long[] {
                2372383, 584222, 257170, 17750155, 7903832, 489265, 209628, 393899
        };
        org.apache.commons.math3.stat.inference.ChiSquareTest csti =
            new org.apache.commons.math3.stat.inference.ChiSquareTest();
        double cst = csti.chiSquareTest(exp, obs);
        Assert.assertEquals("chi-square p-value", 0.0, cst, 1E-3);
        Assert.assertEquals( "chi-square test statistic",
                114875.90421929007, TestUtils.chiSquare(exp, obs), 1E-9);
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testChiSquareZeroCount
    public void testChiSquareZeroCount() {
        
        long[][] counts = { {40, 0, 4}, {91, 1, 2}, {60, 2, 0}};
        Assert.assertEquals( "chi-square test statistic", 9.67444662263,
                TestUtils.chiSquare(counts), 1E-9);
        Assert.assertEquals("chi-square p-value", 0.0462835770603,
                TestUtils.chiSquareTest(counts), 1E-9);
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testOneSampleT
    public void testOneSampleT() {
        double[] observed =
            {93.0, 103.0, 95.0, 101.0, 91.0, 105.0, 96.0, 94.0, 101.0,  88.0, 98.0, 94.0, 101.0, 92.0, 95.0 };
        double mu = 100.0;
        SummaryStatistics sampleStats = null;
        sampleStats = new SummaryStatistics();
        for (int i = 0; i < observed.length; i++) {
            sampleStats.addValue(observed[i]);
        }

        
        Assert.assertEquals("t statistic",  -2.81976445346,
                TestUtils.t(mu, observed), 10E-10);
        Assert.assertEquals("t statistic",  -2.81976445346,
                TestUtils.t(mu, sampleStats), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                TestUtils.tTest(mu, observed), 10E-10);
        Assert.assertEquals("p value", 0.0136390585873,
                TestUtils.tTest(mu, sampleStats), 10E-10);

        try {
            TestUtils.t(mu, (double[]) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            Assert.fail("arguments too short, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyObs);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(mu, emptyStats);
            Assert.fail("arguments too short, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(mu, tooShortObs);
            Assert.fail("insufficient data to compute t statistic, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }
        try {
            TestUtils.tTest(mu, tooShortObs);
            Assert.fail("insufficient data to perform t test, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(mu, (SummaryStatistics) null);
            Assert.fail("insufficient data to compute t statistic, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
        try {
            TestUtils.tTest(mu, (SummaryStatistics) null);
            Assert.fail("insufficient data to perform t test, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testOneSampleTTest
    public void testOneSampleTTest() {
        double[] oneSidedP =
            {2d, 0d, 6d, 6d, 3d, 3d, 2d, 3d, -6d, 6d, 6d, 6d, 3d, 0d, 1d, 1d, 0d, 2d, 3d, 3d };
        SummaryStatistics oneSidedPStats = new SummaryStatistics();
        for (int i = 0; i < oneSidedP.length; i++) {
            oneSidedPStats.addValue(oneSidedP[i]);
        }
        
        Assert.assertEquals("one sample t stat", 3.86485535541,
                TestUtils.t(0d, oneSidedP), 10E-10);
        Assert.assertEquals("one sample t stat", 3.86485535541,
                TestUtils.t(0d, oneSidedPStats),1E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                TestUtils.tTest(0d, oneSidedP) / 2d, 10E-10);
        Assert.assertEquals("one sample p value", 0.000521637019637,
                TestUtils.tTest(0d, oneSidedPStats) / 2d, 10E-5);
        Assert.assertTrue("one sample t-test reject", TestUtils.tTest(0d, oneSidedP, 0.01));
        Assert.assertTrue("one sample t-test reject", TestUtils.tTest(0d, oneSidedPStats, 0.01));
        Assert.assertTrue("one sample t-test accept", !TestUtils.tTest(0d, oneSidedP, 0.0001));
        Assert.assertTrue("one sample t-test accept", !TestUtils.tTest(0d, oneSidedPStats, 0.0001));

        try {
            TestUtils.tTest(0d, oneSidedP, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            TestUtils.tTest(0d, oneSidedPStats, 95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testTwoSampleTHeterscedastic
    public void testTwoSampleTHeterscedastic() {
        double[] sample1 = { 7d, -4d, 18d, 17d, -3d, -5d, 1d, 10d, 11d, -2d };
        double[] sample2 = { -1d, 12d, -1d, -3d, 3d, -5d, 5d, 2d, -11d, -1d, -3d };
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                TestUtils.t(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic t stat", 1.60371728768,
                TestUtils.t(sampleStats1, sampleStats2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                TestUtils.tTest(sample1, sample2), 1E-10);
        Assert.assertEquals("two sample heteroscedastic p value", 0.128839369622,
                TestUtils.tTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                TestUtils.tTest(sample1, sample2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test reject",
                TestUtils.tTest(sampleStats1, sampleStats2, 0.2));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !TestUtils.tTest(sample1, sample2, 0.1));
        Assert.assertTrue("two sample heteroscedastic t-test accept",
                !TestUtils.tTest(sampleStats1, sampleStats2, 0.1));

        try {
            TestUtils.tTest(sample1, sample2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, sampleStats2, .95);
            Assert.fail("alpha out of range, OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        try {
            TestUtils.tTest(sample1, tooShortObs, .01);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null, .01);
            Assert.fail("insufficient data, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.tTest(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.tTest(sampleStats1, (SummaryStatistics) null);
            Assert.fail("insufficient data, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }

        try {
            TestUtils.t(sample1, tooShortObs);
            Assert.fail("insufficient data, NumberIsTooSmallException expected");
        } catch (NumberIsTooSmallException ex) {
            
        }

        try {
            TestUtils.t(sampleStats1, (SummaryStatistics) null);
            Assert.fail("insufficient data, NullArgumentException expected");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testTwoSampleTHomoscedastic
    public void testTwoSampleTHomoscedastic() {
        double[] sample1 ={2, 4, 6, 8, 10, 97};
        double[] sample2 = {4, 6, 8, 10, 16};
        SummaryStatistics sampleStats1 = new SummaryStatistics();
        for (int i = 0; i < sample1.length; i++) {
            sampleStats1.addValue(sample1[i]);
        }
        SummaryStatistics sampleStats2 = new SummaryStatistics();
        for (int i = 0; i < sample2.length; i++) {
            sampleStats2.addValue(sample2[i]);
        }

        
        Assert.assertEquals("two sample homoscedastic t stat", 0.73096310086,
                TestUtils.homoscedasticT(sample1, sample2), 10E-11);
        Assert.assertEquals("two sample homoscedastic p value", 0.4833963785,
                TestUtils.homoscedasticTTest(sampleStats1, sampleStats2), 1E-10);
        Assert.assertTrue("two sample homoscedastic t-test reject",
                TestUtils.homoscedasticTTest(sample1, sample2, 0.49));
        Assert.assertTrue("two sample homoscedastic t-test accept",
                !TestUtils.homoscedasticTTest(sample1, sample2, 0.48));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testSmallSamples
    public void testSmallSamples() {
        double[] sample1 = {1d, 3d};
        double[] sample2 = {4d, 5d};

        
        Assert.assertEquals(-2.2360679775, TestUtils.t(sample1, sample2),
                1E-10);
        Assert.assertEquals(0.198727388935, TestUtils.tTest(sample1, sample2),
                1E-10);
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testPaired
    public void testPaired() {
        double[] sample1 = {1d, 3d, 5d, 7d};
        double[] sample2 = {0d, 6d, 11d, 2d};
        double[] sample3 = {5d, 7d, 8d, 10d};

        
        Assert.assertEquals(-0.3133, TestUtils.pairedT(sample1, sample2), 1E-4);
        Assert.assertEquals(0.774544295819, TestUtils.pairedTTest(sample1, sample2), 1E-10);
        Assert.assertEquals(0.001208, TestUtils.pairedTTest(sample1, sample3), 1E-6);
        Assert.assertFalse(TestUtils.pairedTTest(sample1, sample3, .001));
        Assert.assertTrue(TestUtils.pairedTTest(sample1, sample3, .002));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testOneWayAnovaUtils
    public void testOneWayAnovaUtils() {
        classes.add(classA);
        classes.add(classB);
        classes.add(classC);
        Assert.assertEquals(oneWayAnova.anovaFValue(classes),
                TestUtils.oneWayAnovaFValue(classes), 10E-12);
        Assert.assertEquals(oneWayAnova.anovaPValue(classes),
                TestUtils.oneWayAnovaPValue(classes), 10E-12);
        Assert.assertEquals(oneWayAnova.anovaTest(classes, 0.01),
                TestUtils.oneWayAnovaTest(classes, 0.01));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testGTestGoodnesOfFit
    public void testGTestGoodnesOfFit() throws Exception {
        double[] exp = new double[]{
            0.54d, 0.40d, 0.05d, 0.01d
        };

        long[] obs = new long[]{
            70, 79, 3, 4
        };
        Assert.assertEquals("G test statistic",
                13.144799, TestUtils.g(exp, obs), 1E-5);
        double p_gtgf = TestUtils.gTest(exp, obs);
        Assert.assertEquals("g-Test p-value", 0.004333, p_gtgf, 1E-5);

        Assert.assertTrue(TestUtils.gTest(exp, obs, 0.05));
}

// org.apache.commons.math3.stat.inference.TestUtilsTest::testGTestIndependance
    public void testGTestIndependance() throws Exception {
        long[] obs1 = new long[]{
            268, 199, 42
        };

        long[] obs2 = new long[]{
            807, 759, 184
        };

        double g = TestUtils.gDataSetsComparison(obs1, obs2);

        Assert.assertEquals("G test statistic",
                7.3008170, g, 1E-4);
        double p_gti = TestUtils.gTestDataSetsComparison(obs1, obs2);

        Assert.assertEquals("g-Test p-value", 0.0259805, p_gti, 1E-4);
        Assert.assertTrue(TestUtils.gTestDataSetsComparison(obs1, obs2, 0.05));
    }

// org.apache.commons.math3.stat.inference.TestUtilsTest::testRootLogLikelihood
    public void testRootLogLikelihood() {
        
        Assert.assertTrue(TestUtils.rootLogLikelihoodRatio(904, 21060, 1144, 283012) > 0.0);

        
        Assert.assertTrue(TestUtils.rootLogLikelihoodRatio(36, 21928, 60280, 623876) < 0.0);

        Assert.assertEquals(Math.sqrt(2.772589), TestUtils.rootLogLikelihoodRatio(1, 0, 0, 1), 0.000001);
        Assert.assertEquals(-Math.sqrt(2.772589), TestUtils.rootLogLikelihoodRatio(0, 1, 1, 0), 0.000001);
        Assert.assertEquals(Math.sqrt(27.72589), TestUtils.rootLogLikelihoodRatio(10, 0, 0, 10), 0.00001);

        Assert.assertEquals(Math.sqrt(39.33052), TestUtils.rootLogLikelihoodRatio(5, 1995, 0, 100000), 0.00001);
        Assert.assertEquals(-Math.sqrt(39.33052), TestUtils.rootLogLikelihoodRatio(0, 100000, 5, 1995), 0.00001);

        Assert.assertEquals(Math.sqrt(4730.737), TestUtils.rootLogLikelihoodRatio(1000, 1995, 1000, 100000), 0.001);
        Assert.assertEquals(-Math.sqrt(4730.737), TestUtils.rootLogLikelihoodRatio(1000, 100000, 1000, 1995), 0.001);

        Assert.assertEquals(Math.sqrt(5734.343), TestUtils.rootLogLikelihoodRatio(1000, 1000, 1000, 100000), 0.001);
        Assert.assertEquals(Math.sqrt(5714.932), TestUtils.rootLogLikelihoodRatio(1000, 1000, 1000, 99000), 0.001);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddXSampleData
    public void cannotAddXSampleData() {
        createRegression().newSampleData(new double[]{}, null, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullYSampleData
    public void cannotAddNullYSampleData() {
        createRegression().newSampleData(null, new double[][]{}, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullCovarianceData
    public void cannotAddNullCovarianceData() {
        createRegression().newSampleData(new double[]{}, new double[][]{}, null);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::notEnoughData
    public void notEnoughData() {
        double[]   reducedY = new double[y.length - 1];
        double[][] reducedX = new double[x.length - 1][];
        double[][] reducedO = new double[omega.length - 1][];
        System.arraycopy(y,     0, reducedY, 0, reducedY.length);
        System.arraycopy(x,     0, reducedX, 0, reducedX.length);
        System.arraycopy(omega, 0, reducedO, 0, reducedO.length);
        createRegression().newSampleData(reducedY, reducedX, reducedO);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataWithSampleSizeMismatch
    public void cannotAddCovarianceDataWithSampleSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[1][];
        omega[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataThatIsNotSquare
    public void cannotAddCovarianceDataThatIsNotSquare() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[3][];
        omega[0] = new double[]{1.0, 0};
        omega[1] = new double[]{0, 1.0};
        omega[2] = new double[]{0, 2.0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testYVariance
    public void testYVariance() {

        

        GLSMultipleLinearRegression model = new GLSMultipleLinearRegression();
        model.newSampleData(y, x, omega);
        TestUtils.assertEquals(model.calculateYVariance(), 3.5, 0);
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testNewSample2
    public void testNewSample2() {
        double[] y = new double[] {1, 2, 3, 4}; 
        double[][] x = new double[][] {
          {19, 22, 33},
          {20, 30, 40},
          {25, 35, 45},
          {27, 37, 47}   
        };
        double[][] covariance = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(2).getData();
        GLSMultipleLinearRegression regression = new GLSMultipleLinearRegression();
        regression.newSampleData(y, x, covariance);
        RealMatrix combinedX = regression.getX().copy();
        RealVector combinedY = regression.getY().copy();
        RealMatrix combinedCovInv = regression.getOmegaInverse();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.getX());
        Assert.assertEquals(combinedY, regression.getY());
        Assert.assertEquals(combinedCovInv, regression.getOmegaInverse());
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testGLSOLSConsistency
    public void testGLSOLSConsistency() {      
        RealMatrix identityCov = MatrixUtils.createRealIdentityMatrix(16);
        GLSMultipleLinearRegression glsModel = new GLSMultipleLinearRegression();
        OLSMultipleLinearRegression olsModel = new OLSMultipleLinearRegression();
        glsModel.newSampleData(longley, 16, 6);
        olsModel.newSampleData(longley, 16, 6);
        glsModel.newCovarianceData(identityCov.getData());
        double[] olsBeta = olsModel.calculateBeta().toArray();
        double[] glsBeta = glsModel.calculateBeta().toArray();
        
        
        for (int i = 0; i < olsBeta.length; i++) {
            TestUtils.assertRelativelyEquals(olsBeta[i], glsBeta[i], 10E-7);
        }
    }

// org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest::testGLSEfficiency
    public void testGLSEfficiency() {
        RandomGenerator rg = new JDKRandomGenerator();
        rg.setSeed(200);  
        
        
        
        final int nObs = 16;
        double[] sigma = new double[nObs];
        for (int i = 0; i < nObs; i++) {
            sigma[i] = 10 * rg.nextDouble();
        }
        
        
        
        final int numSeeds = 1000;
        RealMatrix errorSeeds = MatrixUtils.createRealMatrix(numSeeds, nObs);
        for (int i = 0; i < numSeeds; i++) {
            for (int j = 0; j < nObs; j++) {
                errorSeeds.setEntry(i, j, rg.nextGaussian() * sigma[j]);
            }
        }
        
        
        RealMatrix cov = (new Covariance(errorSeeds)).getCovarianceMatrix();
          
        
        GaussianRandomGenerator rawGenerator = new GaussianRandomGenerator(rg);
        double[] errorMeans = new double[nObs];  
        CorrelatedRandomVectorGenerator gen = new CorrelatedRandomVectorGenerator(errorMeans, cov,
         1.0e-12 * cov.getNorm(), rawGenerator);
        
        
        
        
        
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.newSampleData(longley, nObs, 6);
        final RealVector b = ols.calculateBeta().copy();
        final RealMatrix x = ols.getX().copy();
        
        
        GLSMultipleLinearRegression gls = new GLSMultipleLinearRegression();
        gls.newSampleData(longley, nObs, 6);
        gls.newCovarianceData(cov.getData());
        
        
        DescriptiveStatistics olsBetaStats = new DescriptiveStatistics();
        DescriptiveStatistics glsBetaStats = new DescriptiveStatistics();
        
        
        
        final int nModels = 10000;
        for (int i = 0; i < nModels; i++) {
            
            
            RealVector u = MatrixUtils.createRealVector(gen.nextVector());
            double[] y = u.add(x.operate(b)).toArray();
            
            
            ols.newYSampleData(y);
            RealVector olsBeta = ols.calculateBeta();
            
            
            gls.newYSampleData(y);
            RealVector glsBeta = gls.calculateBeta();
            
            
            double dist = olsBeta.getDistance(b);
            olsBetaStats.addValue(dist * dist);
            dist = glsBeta.getDistance(b);
            glsBetaStats.addValue(dist * dist);
            
        }
        
        
        assert(olsBetaStats.getMean() > 1.5 * glsBetaStats.getMean());
        assert(olsBetaStats.getStandardDeviation() > glsBetaStats.getStandardDeviation());  
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testHasIntercept
    public void testHasIntercept() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(10, false);
        if (instance.hasIntercept()) {
            Assert.fail("Should not have intercept");
        }
        instance = new MillerUpdatingRegression(10, true);
        if (!instance.hasIntercept()) {
            Assert.fail("Should have intercept");
        }
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testAddObsGetNClear
    public void testAddObsGetNClear() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        double[][] xAll = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            xAll[i] = new double[3];
            xAll[i][0] = Math.log(airdata[3][i]);
            xAll[i][1] = Math.log(airdata[4][i]);
            xAll[i][2] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }
        instance.addObservations(xAll, y);
        if (instance.getN() != xAll.length) {
            Assert.fail("Number of observations not correct in bulk addition");
        }
        instance.clear();
        for (int i = 0; i < xAll.length; i++) {
            instance.addObservation(xAll[i], y[i]);
        }
        if (instance.getN() != xAll.length) {
            Assert.fail("Number of observations not correct in drip addition");
        }
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testNegativeTestAddObs
    public void testNegativeTestAddObs() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        try {
            instance.addObservation(new double[]{1.0}, 0.0);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, 0.0);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0}, 0.0);
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException");
        }

        
        instance = new MillerUpdatingRegression(3, false);
        try {
            instance.addObservation(new double[]{1.0}, 0.0);
            Assert.fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, 0.0);
            Assert.fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        }
        try {
            instance.addObservation(new double[]{1.0, 1.0, 1.0}, 0.0);
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException [NOINTERCEPT]");
        }
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testNegativeTestAddMultipleObs
    public void testNegativeTestAddMultipleObs() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        try {
            double[][] tst = {{1.0, 1.0, 1.0}, {1.20, 2.0, 2.1}};
            double[] y = {1.0};
            instance.addObservations(tst, y);

            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException");
        }

        try {
            double[][] tst = {{1.0, 1.0, 1.0}, {1.20, 2.0, 2.1}};
            double[] y = {1.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
            instance.addObservations(tst, y);

            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            Assert.fail("Should throw IllegalArgumentException");
        }
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testRegressAirlineConstantExternal
    public void testRegressAirlineConstantExternal() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        try {
            RegressionResults result = instance.regress();
            Assert.assertNotNull("The test case is a prototype.", result);
            TestUtils.assertEquals(
                    new double[]{9.5169, 0.8827, 0.4540, -1.6275},
                    result.getParameterEstimates(), 1e-4);

            TestUtils.assertEquals(
                    new double[]{.2292445, .0132545, .0203042, .345302},
                    result.getStdErrorOfEstimates(), 1.0e-4);

            TestUtils.assertEquals(0.01552839, result.getMeanSquareError(), 1.0e-8);
        } catch (Exception e) {
            Assert.fail("Should not throw exception but does");
        }
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testRegressAirlineConstantInternal
    public void testRegressAirlineConstantInternal() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[3];
            x[i][0] = Math.log(airdata[3][i]);
            x[i][1] = Math.log(airdata[4][i]);
            x[i][2] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        try {
            RegressionResults result = instance.regress();
            Assert.assertNotNull("The test case is a prototype.", result);
            TestUtils.assertEquals(
                    new double[]{9.5169, 0.8827, 0.4540, -1.6275},
                    result.getParameterEstimates(), 1e-4);

            TestUtils.assertEquals(
                    new double[]{.2292445, .0132545, .0203042, .345302},
                    result.getStdErrorOfEstimates(), 1.0e-4);

            TestUtils.assertEquals(0.9883, result.getRSquared(), 1.0e-4);
            TestUtils.assertEquals(0.01552839, result.getMeanSquareError(), 1.0e-8);
        } catch (Exception e) {
            Assert.fail("Should not throw exception but does");
        }
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testFilippelli
    public void testFilippelli() {
        double[] data = new double[]{
            0.8116, -6.860120914,
            0.9072, -4.324130045,
            0.9052, -4.358625055,
            0.9039, -4.358426747,
            0.8053, -6.955852379,
            0.8377, -6.661145254,
            0.8667, -6.355462942,
            0.8809, -6.118102026,
            0.7975, -7.115148017,
            0.8162, -6.815308569,
            0.8515, -6.519993057,
            0.8766, -6.204119983,
            0.8885, -5.853871964,
            0.8859, -6.109523091,
            0.8959, -5.79832982,
            0.8913, -5.482672118,
            0.8959, -5.171791386,
            0.8971, -4.851705903,
            0.9021, -4.517126416,
            0.909, -4.143573228,
            0.9139, -3.709075441,
            0.9199, -3.499489089,
            0.8692, -6.300769497,
            0.8872, -5.953504836,
            0.89, -5.642065153,
            0.891, -5.031376979,
            0.8977, -4.680685696,
            0.9035, -4.329846955,
            0.9078, -3.928486195,
            0.7675, -8.56735134,
            0.7705, -8.363211311,
            0.7713, -8.107682739,
            0.7736, -7.823908741,
            0.7775, -7.522878745,
            0.7841, -7.218819279,
            0.7971, -6.920818754,
            0.8329, -6.628932138,
            0.8641, -6.323946875,
            0.8804, -5.991399828,
            0.7668, -8.781464495,
            0.7633, -8.663140179,
            0.7678, -8.473531488,
            0.7697, -8.247337057,
            0.77, -7.971428747,
            0.7749, -7.676129393,
            0.7796, -7.352812702,
            0.7897, -7.072065318,
            0.8131, -6.774174009,
            0.8498, -6.478861916,
            0.8741, -6.159517513,
            0.8061, -6.835647144,
            0.846, -6.53165267,
            0.8751, -6.224098421,
            0.8856, -5.910094889,
            0.8919, -5.598599459,
            0.8934, -5.290645224,
            0.894, -4.974284616,
            0.8957, -4.64454848,
            0.9047, -4.290560426,
            0.9129, -3.885055584,
            0.9209, -3.408378962,
            0.9219, -3.13200249,
            0.7739, -8.726767166,
            0.7681, -8.66695597,
            0.7665, -8.511026475,
            0.7703, -8.165388579,
            0.7702, -7.886056648,
            0.7761, -7.588043762,
            0.7809, -7.283412422,
            0.7961, -6.995678626,
            0.8253, -6.691862621,
            0.8602, -6.392544977,
            0.8809, -6.067374056,
            0.8301, -6.684029655,
            0.8664, -6.378719832,
            0.8834, -6.065855188,
            0.8898, -5.752272167,
            0.8964, -5.132414673,
            0.8963, -4.811352704,
            0.9074, -4.098269308,
            0.9119, -3.66174277,
            0.9228, -3.2644011
        };
        MillerUpdatingRegression model = new MillerUpdatingRegression(10, true);
        int off = 0;
        double[] tmp = new double[10];
        int nobs = 82;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];

            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            tmp[5] = tmp[0] * tmp[4];
            tmp[6] = tmp[0] * tmp[5];
            tmp[7] = tmp[0] * tmp[6];
            tmp[8] = tmp[0] * tmp[7];
            tmp[9] = tmp[0] * tmp[8];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    -1467.48961422980,
                    -2772.17959193342,
                    -2316.37108160893,
                    -1127.97394098372,
                    -354.478233703349,
                    -75.1242017393757,
                    -10.8753180355343,
                    -1.06221498588947,
                    -0.670191154593408E-01,
                    -0.246781078275479E-02,
                    -0.402962525080404E-04
                }, 1E-5); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{
                    298.084530995537,
                    559.779865474950,
                    466.477572127796,
                    227.204274477751,
                    71.6478660875927,
                    15.2897178747400,
                    2.23691159816033,
                    0.221624321934227,
                    0.142363763154724E-01,
                    0.535617408889821E-03,
                    0.896632837373868E-05
                }, 1E-5); 

        TestUtils.assertEquals(0.996727416185620, result.getRSquared(), 1.0e-8);
        TestUtils.assertEquals(0.112091743968020E-04, result.getMeanSquareError(), 1.0e-10);
        TestUtils.assertEquals(0.795851382172941E-03, result.getErrorSumSquares(), 1.0e-10);

    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testWampler1
    public void testWampler1() {
        double[] data = new double[]{
            1, 0,
            6, 1,
            63, 2,
            364, 3,
            1365, 4,
            3906, 5,
            9331, 6,
            19608, 7,
            37449, 8,
            66430, 9,
            111111, 10,
            177156, 11,
            271453, 12,
            402234, 13,
            579195, 14,
            813616, 15,
            1118481, 16,
            1508598, 17,
            2000719, 18,
            2613660, 19,
            3368421, 20};

        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 

        TestUtils.assertEquals(1.0, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, result.getMeanSquareError(), 1.0e-7);
        TestUtils.assertEquals(0.00, result.getErrorSumSquares(), 1.0e-6);

        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testWampler2
    public void testWampler2() {
        double[] data = new double[]{
            1.00000, 0,
            1.11111, 1,
            1.24992, 2,
            1.42753, 3,
            1.65984, 4,
            1.96875, 5,
            2.38336, 6,
            2.94117, 7,
            3.68928, 8,
            4.68559, 9,
            6.00000, 10,
            7.71561, 11,
            9.92992, 12,
            12.75603, 13,
            16.32384, 14,
            20.78125, 15,
            26.29536, 16,
            33.05367, 17,
            41.26528, 18,
            51.16209, 19,
            63.00000, 20};

        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0e-1, 1.0e-2,
                    1.0e-3, 1.0e-4,
                    1.0e-5}, 1E-8); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 

        TestUtils.assertEquals(1.0, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, result.getMeanSquareError(), 1.0e-7);
        TestUtils.assertEquals(0.00, result.getErrorSumSquares(), 1.0e-6);
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testWampler3
    public void testWampler3() {
        double[] data = new double[]{
            760, 0,
            -2042, 1,
            2111, 2,
            -1684, 3,
            3888, 4,
            1858, 5,
            11379, 6,
            17560, 7,
            39287, 8,
            64382, 9,
            113159, 10,
            175108, 11,
            273291, 12,
            400186, 13,
            581243, 14,
            811568, 15,
            1121004, 16,
            1506550, 17,
            2002767, 18,
            2611612, 19,
            3369180, 20};
        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8); 
        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{2152.32624678170,
                    2363.55173469681, 779.343524331583,
                    101.475507550350, 5.64566512170752,
                    0.112324854679312}, 1E-8); 

        TestUtils.assertEquals(.999995559025820, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(5570284.53333333, result.getMeanSquareError(), 1.0e-7);
        TestUtils.assertEquals(83554268.0000000, result.getErrorSumSquares(), 1.0e-6);
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testWampler4
    public void testWampler4() {
        double[] data = new double[]{
            75901, 0,
            -204794, 1,
            204863, 2,
            -204436, 3,
            253665, 4,
            -200894, 5,
            214131, 6,
            -185192, 7,
            221249, 8,
            -138370, 9,
            315911, 10,
            -27644, 11,
            455253, 12,
            197434, 13,
            783995, 14,
            608816, 15,
            1370781, 16,
            1303798, 17,
            2205519, 18,
            2408860, 19,
            3444321, 20};
        MillerUpdatingRegression model = new MillerUpdatingRegression(5, true);
        int off = 0;
        double[] tmp = new double[5];
        int nobs = 21;
        for (int i = 0; i < nobs; i++) {
            tmp[0] = data[off + 1];
            tmp[1] = tmp[0] * tmp[0];
            tmp[2] = tmp[0] * tmp[1];
            tmp[3] = tmp[0] * tmp[2];
            tmp[4] = tmp[0] * tmp[3];
            model.addObservation(tmp, data[off]);
            off += 2;
        }
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8); 

        double[] se = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(se,
                new double[]{215232.624678170,
                    236355.173469681, 77934.3524331583,
                    10147.5507550350, 564.566512170752,
                    11.2324854679312}, 1E-8); 

        TestUtils.assertEquals(.957478440825662, result.getRSquared(), 1.0e-10);
        TestUtils.assertEquals(55702845333.3333, result.getMeanSquareError(), 1.0e-4);
        TestUtils.assertEquals(835542680000.000, result.getErrorSumSquares(), 1.0e-3);

        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testLongly
    public void testLongly() {
        
        
        double[] design = new double[]{
            60323, 83.0, 234289, 2356, 1590, 107608, 1947,
            61122, 88.5, 259426, 2325, 1456, 108632, 1948,
            60171, 88.2, 258054, 3682, 1616, 109773, 1949,
            61187, 89.5, 284599, 3351, 1650, 110929, 1950,
            63221, 96.2, 328975, 2099, 3099, 112075, 1951,
            63639, 98.1, 346999, 1932, 3594, 113270, 1952,
            64989, 99.0, 365385, 1870, 3547, 115094, 1953,
            63761, 100.0, 363112, 3578, 3350, 116219, 1954,
            66019, 101.2, 397469, 2904, 3048, 117388, 1955,
            67857, 104.6, 419180, 2822, 2857, 118734, 1956,
            68169, 108.4, 442769, 2936, 2798, 120445, 1957,
            66513, 110.8, 444546, 4681, 2637, 121950, 1958,
            68655, 112.6, 482704, 3813, 2552, 123366, 1959,
            69564, 114.2, 502601, 3931, 2514, 125368, 1960,
            69331, 115.7, 518173, 4806, 2572, 127852, 1961,
            70551, 116.9, 554894, 4007, 2827, 130081, 1962
        };

        final int nobs = 16;
        final int nvars = 6;

        
        MillerUpdatingRegression model = new MillerUpdatingRegression(6, true);
        int off = 0;
        double[] tmp = new double[6];
        for (int i = 0; i < nobs; i++) {
            System.arraycopy(design, off + 1, tmp, 0, nvars);
            model.addObservation(tmp, design[off]);
            off += nvars + 1;
        }

        
        RegressionResults result = model.regress();
        double[] betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{-3482258.63459582, 15.0618722713733,
                    -0.358191792925910E-01, -2.02022980381683,
                    -1.03322686717359, -0.511041056535807E-01,
                    1829.15146461355}, 1E-8); 

        
        double[] errors = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(new double[]{890420.383607373,
                    84.9149257747669,
                    0.334910077722432E-01,
                    0.488399681651699,
                    0.214274163161675,
                    0.226073200069370,
                    455.478499142212}, errors, 1E-6);

        
        TestUtils.assertEquals(0.995479004577296, result.getRSquared(), 1E-12);
        TestUtils.assertEquals(0.992465007628826, result.getAdjustedRSquared(), 1E-12);

        model = new MillerUpdatingRegression(6, false);
        off = 0;
        for (int i = 0; i < nobs; i++) {
            System.arraycopy(design, off + 1, tmp, 0, nvars);
            model.addObservation(tmp, design[off]);
            off += nvars + 1;
        }
        
        result = model.regress();
        betaHat = result.getParameterEstimates();
        TestUtils.assertEquals(betaHat,
                new double[]{-52.99357013868291, 0.07107319907358,
                    -0.42346585566399, -0.57256866841929,
                    -0.41420358884978, 48.41786562001326}, 1E-11);

        
        errors = result.getStdErrorOfEstimates();
        TestUtils.assertEquals(new double[]{129.54486693117232, 0.03016640003786,
                    0.41773654056612, 0.27899087467676, 0.32128496193363,
                    17.68948737819961}, errors, 1E-11);

        TestUtils.assertEquals(0.9999670130706, result.getRSquared(), 1E-12);
        TestUtils.assertEquals(0.999947220913, result.getAdjustedRSquared(), 1E-12);

    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testOneRedundantColumn
    public void testOneRedundantColumn() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        MillerUpdatingRegression instance2 = new MillerUpdatingRegression(5, false);
        double[][] x = new double[airdata[0].length][];
        double[][] x2 = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x2[i] = new double[5];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];

            x2[i][0] = x[i][0];
            x2[i][1] = x[i][1];
            x2[i][2] = x[i][2];
            x2[i][3] = x[i][3];
            x2[i][4] = x[i][3];

            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        RegressionResults result = instance.regress();
        Assert.assertNotNull("Could not estimate initial regression", result);

        instance2.addObservations(x2, y);
        RegressionResults resultRedundant = instance2.regress();
        Assert.assertNotNull("Could not estimate redundant regression", resultRedundant);
        double[] beta = result.getParameterEstimates();
        double[] betar = resultRedundant.getParameterEstimates();
        double[] se = result.getStdErrorOfEstimates();
        double[] ser = resultRedundant.getStdErrorOfEstimates();

        for (int i = 0; i < beta.length; i++) {
            if (Math.abs(beta[i] - betar[i]) > 1.0e-8) {
                Assert.fail("Parameters not correctly estimated");
            }
            if (Math.abs(se[i] - ser[i]) > 1.0e-8) {
                Assert.fail("Standard errors not correctly estimated");
            }
            for (int j = 0; j < i; j++) {
                if (Math.abs(result.getCovarianceOfParameters(i, j)
                        - resultRedundant.getCovarianceOfParameters(i, j)) > 1.0e-8) {
                    Assert.fail("Variance Covariance not correct");
                }
            }
        }

        TestUtils.assertEquals(result.getAdjustedRSquared(), resultRedundant.getAdjustedRSquared(), 1.0e-8);
        TestUtils.assertEquals(result.getErrorSumSquares(), resultRedundant.getErrorSumSquares(), 1.0e-8);
        TestUtils.assertEquals(result.getMeanSquareError(), resultRedundant.getMeanSquareError(), 1.0e-8);
        TestUtils.assertEquals(result.getRSquared(), resultRedundant.getRSquared(), 1.0e-8);
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testThreeRedundantColumn
    public void testThreeRedundantColumn() {

        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        MillerUpdatingRegression instance2 = new MillerUpdatingRegression(7, false);
        double[][] x = new double[airdata[0].length][];
        double[][] x2 = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x2[i] = new double[7];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];

            x2[i][0] = x[i][0];
            x2[i][1] = x[i][0];
            x2[i][2] = x[i][1];
            x2[i][3] = x[i][2];
            x2[i][4] = x[i][1];
            x2[i][5] = x[i][3];
            x2[i][6] = x[i][2];

            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        RegressionResults result = instance.regress();
        Assert.assertNotNull("Could not estimate initial regression", result);

        instance2.addObservations(x2, y);
        RegressionResults resultRedundant = instance2.regress();
        Assert.assertNotNull("Could not estimate redundant regression", resultRedundant);
        double[] beta = result.getParameterEstimates();
        double[] betar = resultRedundant.getParameterEstimates();
        double[] se = result.getStdErrorOfEstimates();
        double[] ser = resultRedundant.getStdErrorOfEstimates();

        if (Math.abs(beta[0] - betar[0]) > 1.0e-8) {
            Assert.fail("Parameters not correct after reorder (0,3)");
        }
        if (Math.abs(beta[1] - betar[2]) > 1.0e-8) {
            Assert.fail("Parameters not correct after reorder (1,2)");
        }
        if (Math.abs(beta[2] - betar[3]) > 1.0e-8) {
            Assert.fail("Parameters not correct after reorder (2,1)");
        }
        if (Math.abs(beta[3] - betar[5]) > 1.0e-8) {
            Assert.fail("Parameters not correct after reorder (3,0)");
        }

        if (Math.abs(se[0] - ser[0]) > 1.0e-8) {
            Assert.fail("Se not correct after reorder (0,3)");
        }
        if (Math.abs(se[1] - ser[2]) > 1.0e-8) {
            Assert.fail("Se not correct after reorder (1,2)");
        }
        if (Math.abs(se[2] - ser[3]) > 1.0e-8) {
            Assert.fail("Se not correct after reorder (2,1)");
        }
        if (Math.abs(se[3] - ser[5]) > 1.0e-8) {
            Assert.fail("Se not correct after reorder (3,0)");
        }

        if (Math.abs(result.getCovarianceOfParameters(0, 0)
                - resultRedundant.getCovarianceOfParameters(0, 0)) > 1.0e-8) {
            Assert.fail("VCV not correct after reorder (0,0)");
        }
        if (Math.abs(result.getCovarianceOfParameters(0, 1)
                - resultRedundant.getCovarianceOfParameters(0, 2)) > 1.0e-8) {
            Assert.fail("VCV not correct after reorder (0,1)<->(0,2)");
        }
        if (Math.abs(result.getCovarianceOfParameters(0, 2)
                - resultRedundant.getCovarianceOfParameters(0, 3)) > 1.0e-8) {
            Assert.fail("VCV not correct after reorder (0,2)<->(0,1)");
        }
        if (Math.abs(result.getCovarianceOfParameters(0, 3)
                - resultRedundant.getCovarianceOfParameters(0, 5)) > 1.0e-8) {
            Assert.fail("VCV not correct after reorder (0,3)<->(0,3)");
        }
        if (Math.abs(result.getCovarianceOfParameters(1, 0)
                - resultRedundant.getCovarianceOfParameters(2, 0)) > 1.0e-8) {
            Assert.fail("VCV not correct after reorder (1,0)<->(2,0)");
        }
        if (Math.abs(result.getCovarianceOfParameters(1, 1)
                - resultRedundant.getCovarianceOfParameters(2, 2)) > 1.0e-8) {
            Assert.fail("VCV not correct  (1,1)<->(2,1)");
        }
        if (Math.abs(result.getCovarianceOfParameters(1, 2)
                - resultRedundant.getCovarianceOfParameters(2, 3)) > 1.0e-8) {
            Assert.fail("VCV not correct  (1,2)<->(2,2)");
        }

        if (Math.abs(result.getCovarianceOfParameters(2, 0)
                - resultRedundant.getCovarianceOfParameters(3, 0)) > 1.0e-8) {
            Assert.fail("VCV not correct  (2,0)<->(1,0)");
        }
        if (Math.abs(result.getCovarianceOfParameters(2, 1)
                - resultRedundant.getCovarianceOfParameters(3, 2)) > 1.0e-8) {
            Assert.fail("VCV not correct  (2,1)<->(1,2)");
        }

        if (Math.abs(result.getCovarianceOfParameters(3, 3)
                - resultRedundant.getCovarianceOfParameters(5, 5)) > 1.0e-8) {
            Assert.fail("VCV not correct  (3,3)<->(3,2)");
        }

        TestUtils.assertEquals(result.getAdjustedRSquared(), resultRedundant.getAdjustedRSquared(), 1.0e-8);
        TestUtils.assertEquals(result.getErrorSumSquares(), resultRedundant.getErrorSumSquares(), 1.0e-8);
        TestUtils.assertEquals(result.getMeanSquareError(), resultRedundant.getMeanSquareError(), 1.0e-8);
        TestUtils.assertEquals(result.getRSquared(), resultRedundant.getRSquared(), 1.0e-8);
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testPCorr
    public void testPCorr() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        double[] cp = new double[10];
        double[] yxcorr = new double[4];
        double[] diag = new double[4];
        double sumysq = 0.0;
        int off = 0;
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
            off = 0;
            for (int j = 0; j < 4; j++) {
                double tmp = x[i][j];
                for (int k = 0; k <= j; k++, off++) {
                    cp[off] += tmp * x[i][k];
                }
                yxcorr[j] += tmp * y[i];
            }
            sumysq += y[i] * y[i];
        }
        PearsonsCorrelation pearson = new PearsonsCorrelation(x);
        RealMatrix corr = pearson.getCorrelationMatrix();
        off = 0;
        for (int i = 0; i < 4; i++, off += (i + 1)) {
            diag[i] = FastMath.sqrt(cp[off]);
        }

        instance.addObservations(x, y);
        double[] pc = instance.getPartialCorrelations(0);
        int idx = 0;
        off = 0;
        int off2 = 6;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < i; j++) {
                if (Math.abs(pc[idx] - cp[off] / (diag[i] * diag[j])) > 1.0e-8) {
                    Assert.fail("Failed cross products... i = " + i + " j = " + j);
                }
                ++idx;
                ++off;
            }
            ++off;
            if (Math.abs(pc[i+off2] - yxcorr[ i] / (FastMath.sqrt(sumysq) * diag[i])) > 1.0e-8) {
                Assert.fail("Assert.failed cross product i = " + i + " y");
            }
        }
        double[] pc2 = instance.getPartialCorrelations(1);

        idx = 0;

        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < i; j++) {
                if (Math.abs(pc2[idx] - corr.getEntry(j, i)) > 1.0e-8) {
                    Assert.fail("Failed cross products... i = " + i + " j = " + j);
                }
                ++idx;
            }
        }
        double[] pc3 = instance.getPartialCorrelations(2);
        if (pc3 == null) {
            Assert.fail("Should not be null");
        }
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testHdiag
    public void testHdiag() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(4, false);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[4];
            x[i][0] = 1.0;
            x[i][1] = Math.log(airdata[3][i]);
            x[i][2] = Math.log(airdata[4][i]);
            x[i][3] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }
        instance.addObservations(x, y);
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.setNoIntercept(true);
        ols.newSampleData(y, x);

        RealMatrix rm = ols.calculateHat();
        for (int i = 0; i < x.length; i++) {
            TestUtils.assertEquals(instance.getDiagonalOfHatMatrix(x[i]), rm.getEntry(i, i), 1.0e-8);
        }
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testHdiagConstant
    public void testHdiagConstant() {
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        double[][] x = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[3];
            x[i][0] = Math.log(airdata[3][i]);
            x[i][1] = Math.log(airdata[4][i]);
            x[i][2] = airdata[5][i];
            y[i] = Math.log(airdata[2][i]);
        }
        instance.addObservations(x, y);
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.setNoIntercept(false);
        ols.newSampleData(y, x);

        RealMatrix rm = ols.calculateHat();
        for (int i = 0; i < x.length; i++) {
            TestUtils.assertEquals(instance.getDiagonalOfHatMatrix(x[i]), rm.getEntry(i, i), 1.0e-8);
        }
        return;
    }

// org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest::testSubsetRegression
    public void testSubsetRegression() {
        
        MillerUpdatingRegression instance = new MillerUpdatingRegression(3, true);
        MillerUpdatingRegression redRegression = new MillerUpdatingRegression(2, true);
        double[][] x = new double[airdata[0].length][];
        double[][] xReduced = new double[airdata[0].length][];
        double[] y = new double[airdata[0].length];
        for (int i = 0; i < airdata[0].length; i++) {
            x[i] = new double[3];
            x[i][0] = Math.log(airdata[3][i]);
            x[i][1] = Math.log(airdata[4][i]);
            x[i][2] = airdata[5][i];
            
            xReduced[i] = new double[2];
            xReduced[i][0] = Math.log(airdata[3][i]);
            xReduced[i][1] = Math.log(airdata[4][i]);
            
            y[i] = Math.log(airdata[2][i]);
        }

        instance.addObservations(x, y);
        redRegression.addObservations(xReduced, y);
        
        RegressionResults resultsInstance = instance.regress( new int[]{0,1,2} );
        RegressionResults resultsReduced = redRegression.regress();
        
        TestUtils.assertEquals(resultsInstance.getParameterEstimates(), resultsReduced.getParameterEstimates(), 1.0e-12);
        TestUtils.assertEquals(resultsInstance.getStdErrorOfEstimates(), resultsReduced.getStdErrorOfEstimates(), 1.0e-12);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testPerfectFit
    public void testPerfectFit() {
        double[] betaHat = regression.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                               new double[]{ 11.0, 1.0 / 2.0, 2.0 / 3.0, 3.0 / 4.0, 4.0 / 5.0, 5.0 / 6.0 },
                               1e-14);
        double[] residuals = regression.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{0d,0d,0d,0d,0d,0d},
                               1e-14);
        RealMatrix errors =
            new Array2DRowRealMatrix(regression.estimateRegressionParametersVariance(), false);
        final double[] s = { 1.0, -1.0 /  2.0, -1.0 /  3.0, -1.0 /  4.0, -1.0 /  5.0, -1.0 /  6.0 };
        RealMatrix referenceVariance = new Array2DRowRealMatrix(s.length, s.length);
        referenceVariance.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value) {
                if (row == 0) {
                    return s[column];
                }
                double x = s[row] * s[column];
                return (row == column) ? 2 * x : x;
            }
        });
       Assert.assertEquals(0.0,
                     errors.subtract(referenceVariance).getNorm(),
                     5.0e-16 * referenceVariance.getNorm());
       Assert.assertEquals(1, ((OLSMultipleLinearRegression) regression).calculateRSquared(), 1E-12);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testLongly
    public void testLongly() {
        
        
        double[] design = new double[] {
            60323,83.0,234289,2356,1590,107608,1947,
            61122,88.5,259426,2325,1456,108632,1948,
            60171,88.2,258054,3682,1616,109773,1949,
            61187,89.5,284599,3351,1650,110929,1950,
            63221,96.2,328975,2099,3099,112075,1951,
            63639,98.1,346999,1932,3594,113270,1952,
            64989,99.0,365385,1870,3547,115094,1953,
            63761,100.0,363112,3578,3350,116219,1954,
            66019,101.2,397469,2904,3048,117388,1955,
            67857,104.6,419180,2822,2857,118734,1956,
            68169,108.4,442769,2936,2798,120445,1957,
            66513,110.8,444546,4681,2637,121950,1958,
            68655,112.6,482704,3813,2552,123366,1959,
            69564,114.2,502601,3931,2514,125368,1960,
            69331,115.7,518173,4806,2572,127852,1961,
            70551,116.9,554894,4007,2827,130081,1962
        };

        final int nobs = 16;
        final int nvars = 6;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
          new double[]{-3482258.63459582, 15.0618722713733,
                -0.358191792925910E-01,-2.02022980381683,
                -1.03322686717359,-0.511041056535807E-01,
                 1829.15146461355}, 2E-8); 

        
        double[] residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                267.340029759711,-94.0139423988359,46.28716775752924,
                -410.114621930906,309.7145907602313,-249.3112153297231,
                -164.0489563956039,-13.18035686637081,14.30477260005235,
                 455.394094551857,-17.26892711483297,-39.0550425226967,
                -155.5499735953195,-85.6713080421283,341.9315139607727,
                -206.7578251937366},
                      1E-8);

        
        double[] errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {890420.383607373,
                       84.9149257747669,
                       0.334910077722432E-01,
                       0.488399681651699,
                       0.214274163161675,
                       0.226073200069370,
                       455.478499142212}, errors, 1E-6);
        
        
        Assert.assertEquals(304.8540735619638, model.estimateRegressionStandardError(), 1E-10);
        
        
        Assert.assertEquals(0.995479004577296, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.992465007628826, model.calculateAdjustedRSquared(), 1E-12);
        
        checkVarianceConsistency(model);
        
        
        model.setNoIntercept(true);
        model.newSampleData(design, nobs, nvars);
        
        
        betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
          new double[]{-52.99357013868291, 0.07107319907358,
                -0.42346585566399,-0.57256866841929,
                -0.41420358884978, 48.41786562001326}, 1E-11); 
        
        
        errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {129.54486693117232, 0.03016640003786,
                0.41773654056612, 0.27899087467676, 0.32128496193363,
                17.68948737819961}, errors, 1E-11);
        
        
        residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                279.90274927293092, -130.32465380836874, 90.73228661967445, -401.31252201634948,
                -440.46768772620027, -543.54512853774793, 201.32111639536299, 215.90889365977932,
                73.09368242049943, 913.21694494481869, 424.82484953610174, -8.56475876776709,
                -361.32974610842876, 27.34560497213464, 151.28955976355002, -492.49937355336846},
                      1E-10);
        
        
        Assert.assertEquals(475.1655079819517, model.estimateRegressionStandardError(), 1E-10);
        
        
        Assert.assertEquals(0.9999670130706, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.999947220913, model.calculateAdjustedRSquared(), 1E-12);
         
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testSwissFertility
    public void testSwissFertility() {
        double[] design = new double[] {
            80.2,17.0,15,12,9.96,
            83.1,45.1,6,9,84.84,
            92.5,39.7,5,5,93.40,
            85.8,36.5,12,7,33.77,
            76.9,43.5,17,15,5.16,
            76.1,35.3,9,7,90.57,
            83.8,70.2,16,7,92.85,
            92.4,67.8,14,8,97.16,
            82.4,53.3,12,7,97.67,
            82.9,45.2,16,13,91.38,
            87.1,64.5,14,6,98.61,
            64.1,62.0,21,12,8.52,
            66.9,67.5,14,7,2.27,
            68.9,60.7,19,12,4.43,
            61.7,69.3,22,5,2.82,
            68.3,72.6,18,2,24.20,
            71.7,34.0,17,8,3.30,
            55.7,19.4,26,28,12.11,
            54.3,15.2,31,20,2.15,
            65.1,73.0,19,9,2.84,
            65.5,59.8,22,10,5.23,
            65.0,55.1,14,3,4.52,
            56.6,50.9,22,12,15.14,
            57.4,54.1,20,6,4.20,
            72.5,71.2,12,1,2.40,
            74.2,58.1,14,8,5.23,
            72.0,63.5,6,3,2.56,
            60.5,60.8,16,10,7.72,
            58.3,26.8,25,19,18.46,
            65.4,49.5,15,8,6.10,
            75.5,85.9,3,2,99.71,
            69.3,84.9,7,6,99.68,
            77.3,89.7,5,2,100.00,
            70.5,78.2,12,6,98.96,
            79.4,64.9,7,3,98.22,
            65.0,75.9,9,9,99.06,
            92.2,84.6,3,3,99.46,
            79.3,63.1,13,13,96.83,
            70.4,38.4,26,12,5.62,
            65.7,7.7,29,11,13.79,
            72.7,16.7,22,13,11.22,
            64.4,17.6,35,32,16.92,
            77.6,37.6,15,7,4.97,
            67.6,18.7,25,7,8.65,
            35.0,1.2,37,53,42.34,
            44.7,46.6,16,29,50.43,
            42.8,27.7,22,29,58.33
        };
        
        final int nobs = 47;
        final int nvars = 4;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{91.05542390271397,
                -0.22064551045715,
                -0.26058239824328,
                -0.96161238456030,
                 0.12441843147162}, 1E-12);

        
        double[] residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                7.1044267859730512,1.6580347433531366,
                4.6944952770029644,8.4548022690166160,13.6547432343186212,
               -9.3586864458500774,7.5822446330520386,15.5568995563859289,
                0.8113090736598980,7.1186762732484308,7.4251378771228724,
                2.6761316873234109,0.8351584810309354,7.1769991119615177,
               -3.8746753206299553,-3.1337779476387251,-0.1412575244091504,
                1.1186809170469780,-6.3588097346816594,3.4039270429434074,
                2.3374058329820175,-7.9272368576900503,-7.8361010968497959,
               -11.2597369269357070,0.9445333697827101,6.6544245101380328,
               -0.9146136301118665,-4.3152449403848570,-4.3536932047009183,
               -3.8907885169304661,-6.3027643926302188,-7.8308982189289091,
               -3.1792280015332750,-6.7167298771158226,-4.8469946718041754,
               -10.6335664353633685,11.1031134362036958,6.0084032641811733,
                5.4326230830188482,-7.2375578629692230,2.1671550814448222,
                15.0147574652763112,4.8625103516321015,-7.1597256413907706,
                -0.4515205619767598,-10.2916870903837587,-15.7812984571900063},
                1E-12);

        
        double[] errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {6.94881329475087,
                0.07360008972340,
                0.27410957467466,
                0.19454551679325,
                0.03726654773803}, errors, 1E-10);
        
        
        Assert.assertEquals(7.73642194433223, model.estimateRegressionStandardError(), 1E-12);
        
        
        Assert.assertEquals(0.649789742860228, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.6164363850373927, model.calculateAdjustedRSquared(), 1E-12);
        
        checkVarianceConsistency(model);
        
        
        model = new OLSMultipleLinearRegression();
        model.setNoIntercept(true);
        model.newSampleData(design, nobs, nvars);

        
        betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{0.52191832900513,
                  2.36588087917963,
                  -0.94770353802795, 
                  0.30851985863609}, 1E-12);

        
        residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                44.138759883538249, 27.720705122356215, 35.873200836126799, 
                34.574619581211977, 26.600168342080213, 15.074636243026923, -12.704904871199814,
                1.497443824078134, 2.691972687079431, 5.582798774291231, -4.422986561283165, 
                -9.198581600334345, 4.481765170730647, 2.273520207553216, -22.649827853221336,
                -17.747900013943308, 20.298314638496436, 6.861405135329779, -8.684712790954924,
                -10.298639278062371, -9.896618896845819, 4.568568616351242, -15.313570491727944,
                -13.762961360873966, 7.156100301980509, 16.722282219843990, 26.716200609071898,
                -1.991466398777079, -2.523342564719335, 9.776486693095093, -5.297535127628603,
                -16.639070567471094, -10.302057295211819, -23.549487860816846, 1.506624392156384,
                -17.939174438345930, 13.105792202765040, -1.943329906928462, -1.516005841666695,
                -0.759066561832886, 20.793137744128977, -2.485236153005426, 27.588238710486976,
                2.658333257106881, -15.998337823623046, -5.550742066720694, -14.219077806826615},
                1E-12);

        
        errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {0.10470063765677, 0.41684100584290,
                0.43370143099691, 0.07694953606522}, errors, 1E-10);
        
        
        Assert.assertEquals(17.24710630547, model.estimateRegressionStandardError(), 1E-10);
        
        
        Assert.assertEquals(0.946350722085, model.calculateRSquared(), 1E-12);
        Assert.assertEquals(0.9413600915813, model.calculateAdjustedRSquared(), 1E-12);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testHat
    public void testHat() {

        
        double[] design = new double[] {
                11.14, .499, 11.1,
                12.74, .558, 8.9,
                13.13, .604, 8.8,
                11.51, .441, 8.9,
                12.38, .550, 8.8,
                12.60, .528, 9.9,
                11.13, .418, 10.7,
                11.7, .480, 10.5,
                11.02, .406, 10.5,
                11.41, .467, 10.7
        };

        int nobs = 10;
        int nvars = 2;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        RealMatrix hat = model.calculateHat();

        
        double[] referenceData = new double[] {
                .418, -.002,  .079, -.274, -.046,  .181,  .128,  .222,  .050,  .242,
                       .242,  .292,  .136,  .243,  .128, -.041,  .033, -.035,  .004,
                              .417, -.019,  .273,  .187, -.126,  .044, -.153,  .004,
                                     .604,  .197, -.038,  .168, -.022,  .275, -.028,
                                            .252,  .111, -.030,  .019, -.010, -.010,
                                                   .148,  .042,  .117,  .012,  .111,
                                                          .262,  .145,  .277,  .174,
                                                                 .154,  .120,  .168,
                                                                        .315,  .148,
                                                                               .187
        };

        
        int k = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = i; j < 10; j++) {
                Assert.assertEquals(referenceData[k], hat.getEntry(i, j), 10e-3);
                Assert.assertEquals(hat.getEntry(i, j), hat.getEntry(j, i), 10e-12);
                k++;
            }
        }

        
        double[] residuals = model.estimateResiduals();
        RealMatrix I = MatrixUtils.createRealIdentityMatrix(10);
        double[] hatResiduals = I.subtract(hat).operate(model.getY()).toArray();
        TestUtils.assertEquals(residuals, hatResiduals, 10e-12);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testYVariance
    public void testYVariance() {

        

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(y, x);
        TestUtils.assertEquals(model.calculateYVariance(), 3.5, 0);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testNewSample2
    public void testNewSample2() {
        double[] y = new double[] {1, 2, 3, 4}; 
        double[][] x = new double[][] {
          {19, 22, 33},
          {20, 30, 40},
          {25, 35, 45},
          {27, 37, 47}   
        };
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);
        RealMatrix combinedX = regression.getX().copy();
        RealVector combinedY = regression.getY().copy();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.getX());
        Assert.assertEquals(combinedY, regression.getY());
        
        
        regression.setNoIntercept(true);
        regression.newSampleData(y, x);
        combinedX = regression.getX().copy();
        combinedY = regression.getY().copy();
        regression.newXSampleData(x);
        regression.newYSampleData(y);
        Assert.assertEquals(combinedX, regression.getX());
        Assert.assertEquals(combinedY, regression.getY());
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testNewSampleDataYNull
    public void testNewSampleDataYNull() {
        createRegression().newSampleData(null, new double[][] {});
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testNewSampleDataXNull
    public void testNewSampleDataXNull() {
        createRegression().newSampleData(new double[] {}, null);
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testWampler1
    public void testWampler1() {
        double[] data = new double[]{
            1, 0,
            6, 1,
            63, 2,
            364, 3,
            1365, 4,
            3906, 5,
            9331, 6,
            19608, 7,
            37449, 8,
            66430, 9,
            111111, 10,
            177156, 11,
            271453, 12,
            402234, 13,
            579195, 14,
            813616, 15,
            1118481, 16,
            1508598, 17,
            2000719, 18,
            2613660, 19,
            3368421, 20};
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();

        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{1.0,
                    1.0, 1.0,
                    1.0, 1.0,
                    1.0}, 1E-8);

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 

        TestUtils.assertEquals(1.0, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, model.estimateErrorVariance(), 1.0e-7);
        TestUtils.assertEquals(0.00, model.calculateResidualSumOfSquares(), 1.0e-6);

        return;
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testWampler2
    public void testWampler2() {
        double[] data = new double[]{
            1.00000, 0,
            1.11111, 1,
            1.24992, 2,
            1.42753, 3,
            1.65984, 4,
            1.96875, 5,
            2.38336, 6,
            2.94117, 7,
            3.68928, 8,
            4.68559, 9,
            6.00000, 10,
            7.71561, 11,
            9.92992, 12,
            12.75603, 13,
            16.32384, 14,
            20.78125, 15,
            26.29536, 16,
            33.05367, 17,
            41.26528, 18,
            51.16209, 19,
            63.00000, 20};
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();

        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    1.0,
                    1.0e-1,
                    1.0e-2,
                    1.0e-3, 1.0e-4,
                    1.0e-5}, 1E-8);

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0}, 1E-8); 
        TestUtils.assertEquals(1.0, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(0, model.estimateErrorVariance(), 1.0e-7);
        TestUtils.assertEquals(0.00, model.calculateResidualSumOfSquares(), 1.0e-6);
        return;
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testWampler3
    public void testWampler3() {
        double[] data = new double[]{
            760, 0,
            -2042, 1,
            2111, 2,
            -1684, 3,
            3888, 4,
            1858, 5,
            11379, 6,
            17560, 7,
            39287, 8,
            64382, 9,
            113159, 10,
            175108, 11,
            273291, 12,
            400186, 13,
            581243, 14,
            811568, 15,
            1121004, 16,
            1506550, 17,
            2002767, 18,
            2611612, 19,
            3369180, 20};

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0}, 1E-8); 

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{2152.32624678170,
                    2363.55173469681, 779.343524331583,
                    101.475507550350, 5.64566512170752,
                    0.112324854679312}, 1E-8); 

        TestUtils.assertEquals(.999995559025820, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(5570284.53333333, model.estimateErrorVariance(), 1.0e-6);
        TestUtils.assertEquals(83554268.0000000, model.calculateResidualSumOfSquares(), 1.0e-5);
        return;
    }

// org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest::testWampler4
    public void testWampler4() {
        double[] data = new double[]{
            75901, 0,
            -204794, 1,
            204863, 2,
            -204436, 3,
            253665, 4,
            -200894, 5,
            214131, 6,
            -185192, 7,
            221249, 8,
            -138370, 9,
            315911, 10,
            -27644, 11,
            455253, 12,
            197434, 13,
            783995, 14,
            608816, 15,
            1370781, 16,
            1303798, 17,
            2205519, 18,
            2408860, 19,
            3444321, 20};

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        final int nvars = 5;
        final int nobs = 21;
        double[] tmp = new double[(nvars + 1) * nobs];
        int off = 0;
        int off2 = 0;
        for (int i = 0; i < nobs; i++) {
            tmp[off2] = data[off];
            tmp[off2 + 1] = data[off + 1];
            tmp[off2 + 2] = tmp[off2 + 1] * tmp[off2 + 1];
            tmp[off2 + 3] = tmp[off2 + 1] * tmp[off2 + 2];
            tmp[off2 + 4] = tmp[off2 + 1] * tmp[off2 + 3];
            tmp[off2 + 5] = tmp[off2 + 1] * tmp[off2 + 4];
            off2 += (nvars + 1);
            off += 2;
        }
        model.newSampleData(tmp, nobs, nvars);
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0}, 1E-6); 

        double[] se = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(se,
                new double[]{215232.624678170,
                    236355.173469681, 77934.3524331583,
                    10147.5507550350, 564.566512170752,
                    11.2324854679312}, 1E-8); 

        TestUtils.assertEquals(.957478440825662, model.calculateRSquared(), 1.0e-10);
        TestUtils.assertEquals(55702845333.3333, model.estimateErrorVariance(), 1.0e-4);
        TestUtils.assertEquals(835542680000.000, model.calculateResidualSumOfSquares(), 1.0e-3);
        return;
    }

// org.apache.commons.math3.transform.FastCosineTransformerTest::testAdHocData
    public void testAdHocData() {
        FastCosineTransformer transformer;
        transformer = new FastCosineTransformer(DctNormalization.STANDARD_DCT_I);
        double result[], tolerance = 1E-12;

        double x[] = {
            0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0
        };
        double y[] =
            {
                172.0, -105.096569476353, 27.3137084989848, -12.9593152353742,
                8.0, -5.78585076868676, 4.68629150101524, -4.15826451958632,
                4.0
            };

        result = transformer.transform(x, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.transform(y, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        TransformUtils.scaleArray(x, FastMath.sqrt(0.5 * (x.length - 1)));

        transformer = new FastCosineTransformer(DctNormalization.ORTHOGONAL_DCT_I);
        result = transformer.transform(y, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.transform(x, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastCosineTransformerTest::testParameters
    public void testParameters()
        throws Exception {
        UnivariateFunction f = new Sin();
        FastCosineTransformer transformer;
        transformer = new FastCosineTransformer(DctNormalization.STANDARD_DCT_I);

        try {
            
            transformer.transform(f, 1, -1, 65, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 1, TransformType.FORWARD);
            Assert
                .fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 64, TransformType.FORWARD);
            Assert
                .fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.transform.FastCosineTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        FastCosineTransformer transformer;
        transformer = new FastCosineTransformer(DctNormalization.STANDARD_DCT_I);
        double min, max, result[], tolerance = 1E-12;
        int N = 9;

        double expected[] =
            {
                0.0, 3.26197262739567, 0.0, -2.17958042710327, 0.0,
                -0.648846697642915, 0.0, -0.433545502649478, 0.0
            };
        min = 0.0;
        max = 2.0 * FastMath.PI * N / (N - 1);
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        for (int i = 0; i < N; i++) {
            Assert.assertEquals(expected[i], result[i], tolerance);
        }

        min = -FastMath.PI;
        max = FastMath.PI * (N + 1) / (N - 1);
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        for (int i = 0; i < N; i++) {
            Assert.assertEquals(-expected[i], result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformComplexSizeNotAPowerOfTwo
    public void testTransformComplexSizeNotAPowerOfTwo() {
        final int n = 127;
        final Complex[] x = createComplexData(n);
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(x, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": MathIllegalArgumentException was expected");
                } catch (MathIllegalArgumentException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformRealSizeNotAPowerOfTwo
    public void testTransformRealSizeNotAPowerOfTwo() {
        final int n = 127;
        final double[] x = createRealData(n);
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(x, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": MathIllegalArgumentException was expected");
                } catch (MathIllegalArgumentException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformFunctionSizeNotAPowerOfTwo
    public void testTransformFunctionSizeNotAPowerOfTwo() {
        final int n = 127;
        final UnivariateFunction f = new Sin();
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(f, 0.0, Math.PI, n, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": MathIllegalArgumentException was expected");
                } catch (MathIllegalArgumentException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformFunctionNotStrictlyPositiveNumberOfSamples
    public void testTransformFunctionNotStrictlyPositiveNumberOfSamples() {
        final int n = -128;
        final UnivariateFunction f = new Sin();
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(f, 0.0, Math.PI, n, type[j]);
                    fft.transform(f, 0.0, Math.PI, n, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": NotStrictlyPositiveException was expected");
                } catch (NotStrictlyPositiveException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformFunctionInvalidBounds
    public void testTransformFunctionInvalidBounds() {
        final int n = 128;
        final UnivariateFunction f = new Sin();
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                final FastFourierTransformer fft;
                fft = new FastFourierTransformer(norm[i]);
                try {
                    fft.transform(f, Math.PI, 0.0, n, type[j]);
                    Assert.fail(norm[i] + ", " + type[j] +
                        ": NumberIsTooLargeException was expected");
                } catch (NumberIsTooLargeException e) {
                    
                }
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testTransformComplex
    public void testTransformComplex() {
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                doTestTransformComplex(2, 1.0E-15, norm[i], type[j]);
                doTestTransformComplex(4, 1.0E-14, norm[i], type[j]);
                doTestTransformComplex(8, 1.0E-14, norm[i], type[j]);
                doTestTransformComplex(16, 1.0E-13, norm[i], type[j]);
                doTestTransformComplex(32, 1.0E-13, norm[i], type[j]);
                doTestTransformComplex(64, 1.0E-12, norm[i], type[j]);
                doTestTransformComplex(128, 1.0E-12, norm[i], type[j]);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testStandardTransformReal
    public void testStandardTransformReal() {
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                doTestTransformReal(2, 1.0E-15, norm[i], type[j]);
                doTestTransformReal(4, 1.0E-14, norm[i], type[j]);
                doTestTransformReal(8, 1.0E-14, norm[i], type[j]);
                doTestTransformReal(16, 1.0E-13, norm[i], type[j]);
                doTestTransformReal(32, 1.0E-13, norm[i], type[j]);
                doTestTransformReal(64, 1.0E-13, norm[i], type[j]);
                doTestTransformReal(128, 1.0E-11, norm[i], type[j]);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testStandardTransformFunction
    public void testStandardTransformFunction() {
        final UnivariateFunction f = new Sinc();
        final double min = -FastMath.PI;
        final double max = FastMath.PI;
        final DftNormalization[] norm;
        norm = DftNormalization.values();
        final TransformType[] type;
        type = TransformType.values();
        for (int i = 0; i < norm.length; i++) {
            for (int j = 0; j < type.length; j++) {
                doTestTransformFunction(f, min, max, 2, 1.0E-15, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 4, 1.0E-14, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 8, 1.0E-14, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 16, 1.0E-13, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 32, 1.0E-13, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 64, 1.0E-12, norm[i], type[j]);
                doTestTransformFunction(f, min, max, 128, 1.0E-11, norm[i], type[j]);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testAdHocData
    public void testAdHocData() {
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex result[]; double tolerance = 1E-12;

        double x[] = {1.3, 2.4, 1.7, 4.1, 2.9, 1.7, 5.1, 2.7};
        Complex y[] = {
            new Complex(21.9, 0.0),
            new Complex(-2.09497474683058, 1.91507575950825),
            new Complex(-2.6, 2.7),
            new Complex(-1.10502525316942, -4.88492424049175),
            new Complex(0.1, 0.0),
            new Complex(-1.10502525316942, 4.88492424049175),
            new Complex(-2.6, -2.7),
            new Complex(-2.09497474683058, -1.91507575950825)};

        result = transformer.transform(x, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i].getReal(), result[i].getReal(), tolerance);
            Assert.assertEquals(y[i].getImaginary(), result[i].getImaginary(), tolerance);
        }

        result = transformer.transform(y, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        double x2[] = {10.4, 21.6, 40.8, 13.6, 23.2, 32.8, 13.6, 19.2};
        TransformUtils.scaleArray(x2, 1.0 / FastMath.sqrt(x2.length));
        Complex y2[] = y;

        transformer = new FastFourierTransformer(DftNormalization.UNITARY);
        result = transformer.transform(y2, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x2[i], result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        result = transformer.transform(x2, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y2[i].getReal(), result[i].getReal(), tolerance);
            Assert.assertEquals(y2[i].getImaginary(), result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex result[]; int N = 1 << 8;
        double min, max, tolerance = 1E-12;

        min = 0.0; max = 2.0 * FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        Assert.assertEquals(0.0, result[1].getReal(), tolerance);
        Assert.assertEquals(-(N >> 1), result[1].getImaginary(), tolerance);
        Assert.assertEquals(0.0, result[N-1].getReal(), tolerance);
        Assert.assertEquals(N >> 1, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.INVERSE);
        Assert.assertEquals(0.0, result[1].getReal(), tolerance);
        Assert.assertEquals(-0.5, result[1].getImaginary(), tolerance);
        Assert.assertEquals(0.0, result[N-1].getReal(), tolerance);
        Assert.assertEquals(0.5, result[N-1].getImaginary(), tolerance);
        for (int i = 0; i < N-1; i += (i == 0 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i].getReal(), tolerance);
            Assert.assertEquals(0.0, result[i].getImaginary(), tolerance);
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::test2DData
    public void test2DData() {
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.STANDARD);

        double tolerance = 1E-12;
        Complex[][] input = new Complex[][] {new Complex[] {new Complex(1, 0),
                                                            new Complex(2, 0)},
                                             new Complex[] {new Complex(3, 1),
                                                            new Complex(4, 2)}};
        Complex[][] goodOutput = new Complex[][] {new Complex[] {new Complex(5,
                1.5), new Complex(-1, -.5)}, new Complex[] {new Complex(-2,
                -1.5), new Complex(0, .5)}};
        for (int i = 0; i < goodOutput.length; i++) {
            TransformUtils.scaleArray(
                goodOutput[i],
                FastMath.sqrt(goodOutput[i].length) *
                    FastMath.sqrt(goodOutput.length));
        }
        Complex[][] output = (Complex[][])transformer.mdfft(input, TransformType.FORWARD);
        Complex[][] output2 = (Complex[][])transformer.mdfft(output, TransformType.INVERSE);

        Assert.assertEquals(input.length, output.length);
        Assert.assertEquals(input.length, output2.length);
        Assert.assertEquals(input[0].length, output[0].length);
        Assert.assertEquals(input[0].length, output2[0].length);
        Assert.assertEquals(input[1].length, output[1].length);
        Assert.assertEquals(input[1].length, output2[1].length);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                Assert.assertEquals(input[i][j].getImaginary(), output2[i][j].getImaginary(),
                             tolerance);
                Assert.assertEquals(input[i][j].getReal(), output2[i][j].getReal(), tolerance);
                Assert.assertEquals(goodOutput[i][j].getImaginary(), output[i][j].getImaginary(),
                             tolerance);
                Assert.assertEquals(goodOutput[i][j].getReal(), output[i][j].getReal(), tolerance);
            }
        }
    }

// org.apache.commons.math3.transform.FastFourierTransformerTest::test2DDataUnitary
    public void test2DDataUnitary() {
        FastFourierTransformer transformer;
        transformer = new FastFourierTransformer(DftNormalization.UNITARY);
        double tolerance = 1E-12;
        Complex[][] input = new Complex[][] {new Complex[] {new Complex(1, 0),
                                                            new Complex(2, 0)},
                                             new Complex[] {new Complex(3, 1),
                                                            new Complex(4, 2)}};
        Complex[][] goodOutput = new Complex[][] {new Complex[] {new Complex(5,
                1.5), new Complex(-1, -.5)}, new Complex[] {new Complex(-2,
                -1.5), new Complex(0, .5)}};
        Complex[][] output = (Complex[][])transformer.mdfft(input, TransformType.FORWARD);
        Complex[][] output2 = (Complex[][])transformer.mdfft(output, TransformType.INVERSE);

        Assert.assertEquals(input.length, output.length);
        Assert.assertEquals(input.length, output2.length);
        Assert.assertEquals(input[0].length, output[0].length);
        Assert.assertEquals(input[0].length, output2[0].length);
        Assert.assertEquals(input[1].length, output[1].length);
        Assert.assertEquals(input[1].length, output2[1].length);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                Assert.assertEquals(input[i][j].getImaginary(), output2[i][j].getImaginary(),
                             tolerance);
                Assert.assertEquals(input[i][j].getReal(), output2[i][j].getReal(), tolerance);
                Assert.assertEquals(goodOutput[i][j].getImaginary(), output[i][j].getImaginary(),
                             tolerance);
                Assert.assertEquals(goodOutput[i][j].getReal(), output[i][j].getReal(), tolerance);
            }
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testTransformRealFirstElementNotZero
    public void testTransformRealFirstElementNotZero() {
        final TransformType[] type = TransformType.values();
        final double[] data = new double[] {
            1.0, 1.0, 1.0, 1.0
        };
        final RealTransformer transformer = createRealTransformer();
        for (int j = 0; j < type.length; j++) {
            try {
                transformer.transform(data, type[j]);
                Assert.fail(type[j].toString());
            } catch (MathIllegalArgumentException e) {
                
            }
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testAdHocData
    public void testAdHocData() {
        FastSineTransformer transformer;
        transformer = new FastSineTransformer(DstNormalization.STANDARD_DST_I);
        double result[], tolerance = 1E-12;

        double x[] = { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 };
        double y[] = { 0.0, 20.1093579685034, -9.65685424949238,
                       5.98642305066196, -4.0, 2.67271455167720,
                      -1.65685424949238, 0.795649469518633 };

        result = transformer.transform(x, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }

        result = transformer.transform(y, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        TransformUtils.scaleArray(x, FastMath.sqrt(x.length / 2.0));
        transformer = new FastSineTransformer(DstNormalization.ORTHOGONAL_DST_I);

        result = transformer.transform(y, TransformType.FORWARD);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(x[i], result[i], tolerance);
        }

        result = transformer.transform(x, TransformType.INVERSE);
        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals(y[i], result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testSinFunction
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        FastSineTransformer transformer;
        transformer = new FastSineTransformer(DstNormalization.STANDARD_DST_I);
        double min, max, result[], tolerance = 1E-12; int N = 1 << 8;

        min = 0.0; max = 2.0 * FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        Assert.assertEquals(N >> 1, result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i], tolerance);
        }

        min = -FastMath.PI; max = FastMath.PI;
        result = transformer.transform(f, min, max, N, TransformType.FORWARD);
        Assert.assertEquals(-(N >> 1), result[2], tolerance);
        for (int i = 0; i < N; i += (i == 1 ? 2 : 1)) {
            Assert.assertEquals(0.0, result[i], tolerance);
        }
    }

// org.apache.commons.math3.transform.FastSineTransformerTest::testParameters
    public void testParameters() throws Exception {
        UnivariateFunction f = new Sin();
        FastSineTransformer transformer;
        transformer = new FastSineTransformer(DstNormalization.STANDARD_DST_I);

        try {
            
            transformer.transform(f, 1, -1, 64, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 0, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            
            transformer.transform(f, -1, 1, 100, TransformType.FORWARD);
            Assert.fail("Expecting IllegalArgumentException - bad samples number");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.util.Decimal64Test::testAdd
    public void testAdd() {
        Decimal64 expected, actual;

        expected = new Decimal64(X + Y);
        actual = PLUS_X.add(PLUS_Y);
        Assert.assertEquals(expected, actual);
        actual = PLUS_Y.add(PLUS_X);
        Assert.assertEquals(expected, actual);

        expected = new Decimal64(X + (-Y));
        actual = PLUS_X.add(MINUS_Y);
        Assert.assertEquals(expected, actual);
        actual = MINUS_Y.add(PLUS_X);
        Assert.assertEquals(expected, actual);

        expected = new Decimal64((-X) + (-Y));
        actual = MINUS_X.add(MINUS_Y);
        Assert.assertEquals(expected, actual);
        actual = MINUS_Y.add(MINUS_X);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.POSITIVE_INFINITY;
        actual = PLUS_X.add(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY.add(PLUS_X);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.add(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY.add(MINUS_X);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY.add(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.NEGATIVE_INFINITY;
        actual = PLUS_X.add(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.add(PLUS_X);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.add(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.add(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.add(MINUS_X);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.NAN;
        actual = Decimal64.POSITIVE_INFINITY.add(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.add(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = PLUS_X.add(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.add(PLUS_X);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.add(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.add(MINUS_X);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY.add(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.add(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.add(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.add(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.add(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.util.Decimal64Test::testSubtract
    public void testSubtract() {
        Decimal64 expected, actual;

        expected = new Decimal64(X - Y);
        actual = PLUS_X.subtract(PLUS_Y);
        Assert.assertEquals(expected, actual);

        expected = new Decimal64(X - (-Y));
        actual = PLUS_X.subtract(MINUS_Y);
        Assert.assertEquals(expected, actual);

        expected = new Decimal64((-X) - Y);
        actual = MINUS_X.subtract(PLUS_Y);
        Assert.assertEquals(expected, actual);

        expected = new Decimal64((-X) - (-Y));
        actual = MINUS_X.subtract(MINUS_Y);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.NEGATIVE_INFINITY;
        actual = PLUS_X.subtract(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.subtract(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY
                .subtract(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.POSITIVE_INFINITY;
        actual = PLUS_X.subtract(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.subtract(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY
                .subtract(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.NAN;
        actual = Decimal64.POSITIVE_INFINITY
                .subtract(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY
                .subtract(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = PLUS_X.subtract(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.subtract(PLUS_X);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.subtract(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.subtract(MINUS_X);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY.subtract(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.subtract(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.subtract(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.subtract(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.subtract(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.util.Decimal64Test::testNegate
    public void testNegate() {
        Decimal64 expected, actual;

        expected = MINUS_X;
        actual = PLUS_X.negate();
        Assert.assertEquals(expected, actual);

        expected = PLUS_X;
        actual = MINUS_X.negate();
        Assert.assertEquals(expected, actual);

        expected = MINUS_ZERO;
        actual = PLUS_ZERO.negate();
        Assert.assertEquals(expected, actual);

        expected = PLUS_ZERO;
        actual = MINUS_ZERO.negate();
        Assert.assertEquals(expected, actual);

        expected = Decimal64.POSITIVE_INFINITY;
        actual = Decimal64.NEGATIVE_INFINITY.negate();
        Assert.assertEquals(expected, actual);

        expected = Decimal64.NEGATIVE_INFINITY;
        actual = Decimal64.POSITIVE_INFINITY.negate();
        Assert.assertEquals(expected, actual);

        expected = Decimal64.NAN;
        actual = Decimal64.NAN.negate();
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.util.Decimal64Test::testMultiply
    public void testMultiply() {
        Decimal64 expected, actual;

        expected = new Decimal64(X * Y);
        actual = PLUS_X.multiply(PLUS_Y);
        Assert.assertEquals(expected, actual);
        actual = PLUS_Y.multiply(PLUS_X);
        Assert.assertEquals(expected, actual);

        expected = new Decimal64(X * (-Y));
        actual = PLUS_X.multiply(MINUS_Y);
        Assert.assertEquals(expected, actual);
        actual = MINUS_Y.multiply(PLUS_X);
        Assert.assertEquals(expected, actual);

        expected = new Decimal64((-X) * (-Y));
        actual = MINUS_X.multiply(MINUS_Y);
        Assert.assertEquals(expected, actual);
        actual = MINUS_Y.multiply(MINUS_X);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.POSITIVE_INFINITY;
        actual = PLUS_X.multiply(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY.multiply(PLUS_X);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.multiply(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.multiply(MINUS_X);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY
                .multiply(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY
                .multiply(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.NEGATIVE_INFINITY;
        actual = PLUS_X.multiply(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.multiply(PLUS_X);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.multiply(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY.multiply(MINUS_X);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY
                .multiply(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY
                .multiply(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.NAN;
        actual = PLUS_X.multiply(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.multiply(PLUS_X);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.multiply(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.multiply(MINUS_X);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY.multiply(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.multiply(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.multiply(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.multiply(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.multiply(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.util.Decimal64Test::testDivide
    public void testDivide() {
        Decimal64 expected, actual;

        expected = new Decimal64(X / Y);
        actual = PLUS_X.divide(PLUS_Y);
        Assert.assertEquals(expected, actual);

        expected = new Decimal64(X / (-Y));
        actual = PLUS_X.divide(MINUS_Y);
        Assert.assertEquals(expected, actual);

        expected = new Decimal64((-X) / Y);
        actual = MINUS_X.divide(PLUS_Y);
        Assert.assertEquals(expected, actual);

        expected = new Decimal64((-X) / (-Y));
        actual = MINUS_X.divide(MINUS_Y);
        Assert.assertEquals(expected, actual);

        expected = PLUS_ZERO;
        actual = PLUS_X.divide(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.divide(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);

        expected = MINUS_ZERO;
        actual = MINUS_X.divide(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = PLUS_X.divide(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.POSITIVE_INFINITY;
        actual = Decimal64.POSITIVE_INFINITY.divide(PLUS_X);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.divide(MINUS_X);
        Assert.assertEquals(expected, actual);
        actual = PLUS_X.divide(PLUS_ZERO);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.divide(MINUS_ZERO);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.NEGATIVE_INFINITY;
        actual = Decimal64.POSITIVE_INFINITY.divide(MINUS_X);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.divide(PLUS_X);
        Assert.assertEquals(expected, actual);
        actual = PLUS_X.divide(MINUS_ZERO);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.divide(PLUS_ZERO);
        Assert.assertEquals(expected, actual);

        expected = Decimal64.NAN;
        actual = Decimal64.POSITIVE_INFINITY
                .divide(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY
                .divide(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY
                .divide(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY
                .divide(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = PLUS_X.divide(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.divide(PLUS_X);
        Assert.assertEquals(expected, actual);
        actual = MINUS_X.divide(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.divide(MINUS_X);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.POSITIVE_INFINITY.divide(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.divide(Decimal64.POSITIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NEGATIVE_INFINITY.divide(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.divide(Decimal64.NEGATIVE_INFINITY);
        Assert.assertEquals(expected, actual);
        actual = Decimal64.NAN.divide(Decimal64.NAN);
        Assert.assertEquals(expected, actual);
        actual = PLUS_ZERO.divide(PLUS_ZERO);
        Assert.assertEquals(expected, actual);
        actual = PLUS_ZERO.divide(MINUS_ZERO);
        Assert.assertEquals(expected, actual);
        actual = MINUS_ZERO.divide(PLUS_ZERO);
        Assert.assertEquals(expected, actual);
        actual = MINUS_ZERO.divide(MINUS_ZERO);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.util.Decimal64Test::testReciprocal
    public void testReciprocal() {
        Decimal64 expected, actual;

        expected = new Decimal64(1.0 / X);
        actual = PLUS_X.reciprocal();
        Assert.assertEquals(expected, actual);

        expected = new Decimal64(1.0 / (-X));
        actual = MINUS_X.reciprocal();
        Assert.assertEquals(expected, actual);

        expected = PLUS_ZERO;
        actual = Decimal64.POSITIVE_INFINITY.reciprocal();
        Assert.assertEquals(expected, actual);

        expected = MINUS_ZERO;
        actual = Decimal64.NEGATIVE_INFINITY.reciprocal();
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.util.Decimal64Test::testIsInfinite
    public void testIsInfinite() {
        Assert.assertFalse(MINUS_X.isInfinite());
        Assert.assertFalse(PLUS_X.isInfinite());
        Assert.assertFalse(MINUS_Y.isInfinite());
        Assert.assertFalse(PLUS_Y.isInfinite());
        Assert.assertFalse(Decimal64.NAN.isInfinite());

        Assert.assertTrue(Decimal64.NEGATIVE_INFINITY.isInfinite());
        Assert.assertTrue(Decimal64.POSITIVE_INFINITY.isInfinite());
    }

// org.apache.commons.math3.util.Decimal64Test::testIsNaN
    public void testIsNaN() {
        Assert.assertFalse(MINUS_X.isNaN());
        Assert.assertFalse(PLUS_X.isNaN());
        Assert.assertFalse(MINUS_Y.isNaN());
        Assert.assertFalse(PLUS_Y.isNaN());
        Assert.assertFalse(Decimal64.NEGATIVE_INFINITY.isNaN());
        Assert.assertFalse(Decimal64.POSITIVE_INFINITY.isNaN());

        Assert.assertTrue(Decimal64.NAN.isNaN());
    }

// org.apache.commons.math3.util.MathArraysTest::testScale
    public void testScale() {
        final double[] test = new double[] { -2.5, -1, 0, 1, 2.5 };
        final double[] correctTest = MathArrays.copyOf(test);
        final double[] correctScaled = new double[]{5.25, 2.1, 0, -2.1, -5.25};
        
        final double[] scaled = MathArrays.scale(-2.1, test);

        
        for (int i = 0; i < test.length; i++) {
            Assert.assertEquals(correctTest[i], test[i], 0);
        }

        
        for (int i = 0; i < scaled.length; i++) {
            Assert.assertEquals(correctScaled[i], scaled[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testScaleInPlace
    public void testScaleInPlace() {
        final double[] test = new double[] { -2.5, -1, 0, 1, 2.5 };
        final double[] correctScaled = new double[]{5.25, 2.1, 0, -2.1, -5.25};
        MathArrays.scaleInPlace(-2.1, test);

        
        for (int i = 0; i < test.length; i++) {
            Assert.assertEquals(correctScaled[i], test[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeAddPrecondition
    public void testEbeAddPrecondition() {
        MathArrays.ebeAdd(new double[3], new double[4]);
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeSubtractPrecondition
    public void testEbeSubtractPrecondition() {
        MathArrays.ebeSubtract(new double[3], new double[4]);
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeMultiplyPrecondition
    public void testEbeMultiplyPrecondition() {
        MathArrays.ebeMultiply(new double[3], new double[4]);
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeDividePrecondition
    public void testEbeDividePrecondition() {
        MathArrays.ebeDivide(new double[3], new double[4]);
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeAdd
    public void testEbeAdd() {
        final double[] a = { 0, 1, 2 };
        final double[] b = { 3, 5, 7 };
        final double[] r = MathArrays.ebeAdd(a, b);

        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i] + b[i], r[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeSubtract
    public void testEbeSubtract() {
        final double[] a = { 0, 1, 2 };
        final double[] b = { 3, 5, 7 };
        final double[] r = MathArrays.ebeSubtract(a, b);

        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i] - b[i], r[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeMultiply
    public void testEbeMultiply() {
        final double[] a = { 0, 1, 2 };
        final double[] b = { 3, 5, 7 };
        final double[] r = MathArrays.ebeMultiply(a, b);

        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i] * b[i], r[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testEbeDivide
    public void testEbeDivide() {
        final double[] a = { 0, 1, 2 };
        final double[] b = { 3, 5, 7 };
        final double[] r = MathArrays.ebeDivide(a, b);

        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i] / b[i], r[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testL1DistanceDouble
    public void testL1DistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        Assert.assertTrue(Precision.equals(7.0, MathArrays.distance1(p1, p2), 1));
    }

// org.apache.commons.math3.util.MathArraysTest::testL1DistanceInt
    public void testL1DistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        Assert.assertEquals(7, MathArrays.distance1(p1, p2));
    }

// org.apache.commons.math3.util.MathArraysTest::testL2DistanceDouble
    public void testL2DistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        Assert.assertTrue(Precision.equals(5.0, MathArrays.distance(p1, p2), 1));
    }

// org.apache.commons.math3.util.MathArraysTest::testL2DistanceInt
    public void testL2DistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        Assert.assertTrue(Precision.equals(5, MathArrays.distance(p1, p2), 1));
    }

// org.apache.commons.math3.util.MathArraysTest::testLInfDistanceDouble
    public void testLInfDistanceDouble() {
        double[] p1 = { 2.5,  0.0 };
        double[] p2 = { -0.5, 4.0 };
        Assert.assertTrue(Precision.equals(4.0, MathArrays.distanceInf(p1, p2), 1));
    }

// org.apache.commons.math3.util.MathArraysTest::testLInfDistanceInt
    public void testLInfDistanceInt() {
        int[] p1 = { 3, 0 };
        int[] p2 = { 0, 4 };
        Assert.assertEquals(4, MathArrays.distanceInf(p1, p2));
    }

// org.apache.commons.math3.util.MathArraysTest::testCheckOrder
    public void testCheckOrder() {
        MathArrays.checkOrder(new double[] {-15, -5.5, -1, 2, 15},
                             MathArrays.OrderDirection.INCREASING, true);
        MathArrays.checkOrder(new double[] {-15, -5.5, -1, 2, 2},
                             MathArrays.OrderDirection.INCREASING, false);
        MathArrays.checkOrder(new double[] {3, -5.5, -11, -27.5},
                             MathArrays.OrderDirection.DECREASING, true);
        MathArrays.checkOrder(new double[] {3, 0, 0, -5.5, -11, -27.5},
                             MathArrays.OrderDirection.DECREASING, false);

        try {
            MathArrays.checkOrder(new double[] {-15, -5.5, -1, -1, 2, 15},
                                 MathArrays.OrderDirection.INCREASING, true);
            Assert.fail("an exception should have been thrown");
        } catch (NonMonotonicSequenceException e) {
            
        }
        try {
            MathArrays.checkOrder(new double[] {-15, -5.5, -1, -2, 2},
                                 MathArrays.OrderDirection.INCREASING, false);
            Assert.fail("an exception should have been thrown");
        } catch (NonMonotonicSequenceException e) {
            
        }
        try {
            MathArrays.checkOrder(new double[] {3, 3, -5.5, -11, -27.5},
                                 MathArrays.OrderDirection.DECREASING, true);
            Assert.fail("an exception should have been thrown");
        } catch (NonMonotonicSequenceException e) {
            
        }
        try {
            MathArrays.checkOrder(new double[] {3, -1, 0, -5.5, -11, -27.5},
                                 MathArrays.OrderDirection.DECREASING, false);
            Assert.fail("an exception should have been thrown");
        } catch (NonMonotonicSequenceException e) {
            
        }
        try {
            MathArrays.checkOrder(new double[] {3, 0, -5.5, -11, -10},
                                 MathArrays.OrderDirection.DECREASING, false);
            Assert.fail("an exception should have been thrown");
        } catch (NonMonotonicSequenceException e) {
            
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testIsMonotonic
    public void testIsMonotonic() {
        Assert.assertFalse(MathArrays.isMonotonic(new double[] { -15, -5.5, -1, -1, 2, 15 },
                                                  MathArrays.OrderDirection.INCREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(new double[] { -15, -5.5, -1, 0, 2, 15 },
                                                 MathArrays.OrderDirection.INCREASING, true));
        Assert.assertFalse(MathArrays.isMonotonic(new double[] { -15, -5.5, -1, -2, 2 },
                                                  MathArrays.OrderDirection.INCREASING, false));
        Assert.assertTrue(MathArrays.isMonotonic(new double[] { -15, -5.5, -1, -1, 2 },
                                                 MathArrays.OrderDirection.INCREASING, false));
        Assert.assertFalse(MathArrays.isMonotonic(new double[] { 3, 3, -5.5, -11, -27.5 },
                                                  MathArrays.OrderDirection.DECREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(new double[] { 3, 2, -5.5, -11, -27.5 },
                                                 MathArrays.OrderDirection.DECREASING, true));
        Assert.assertFalse(MathArrays.isMonotonic(new double[] { 3, -1, 0, -5.5, -11, -27.5 },
                                                  MathArrays.OrderDirection.DECREASING, false));
        Assert.assertTrue(MathArrays.isMonotonic(new double[] { 3, 0, 0, -5.5, -11, -27.5 },
                                                 MathArrays.OrderDirection.DECREASING, false));
    }

// org.apache.commons.math3.util.MathArraysTest::testIsMonotonicComparable
    public void testIsMonotonicComparable() {
        Assert.assertFalse(MathArrays.isMonotonic(new Double[] { new Double(-15),
                                                                 new Double(-5.5),
                                                                 new Double(-1),
                                                                 new Double(-1),
                                                                 new Double(2),
                                                                 new Double(15) },
                MathArrays.OrderDirection.INCREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(new Double[] { new Double(-15),
                                                                new Double(-5.5),
                                                                new Double(-1),
                                                                new Double(0),
                                                                new Double(2),
                                                                new Double(15) },
                MathArrays.OrderDirection.INCREASING, true));
        Assert.assertFalse(MathArrays.isMonotonic(new Double[] { new Double(-15),
                                                                 new Double(-5.5),
                                                                 new Double(-1),
                                                                 new Double(-2),
                                                                 new Double(2) },
                MathArrays.OrderDirection.INCREASING, false));
        Assert.assertTrue(MathArrays.isMonotonic(new Double[] { new Double(-15),
                                                                new Double(-5.5),
                                                                new Double(-1),
                                                                new Double(-1),
                                                                new Double(2) },
                MathArrays.OrderDirection.INCREASING, false));
        Assert.assertFalse(MathArrays.isMonotonic(new Double[] { new Double(3),
                                                                 new Double(3),
                                                                 new Double(-5.5),
                                                                 new Double(-11),
                                                                 new Double(-27.5) },
                MathArrays.OrderDirection.DECREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(new Double[] { new Double(3),
                                                                new Double(2),
                                                                new Double(-5.5),
                                                                new Double(-11),
                                                                new Double(-27.5) },
                MathArrays.OrderDirection.DECREASING, true));
        Assert.assertFalse(MathArrays.isMonotonic(new Double[] { new Double(3),
                                                                 new Double(-1),
                                                                 new Double(0),
                                                                 new Double(-5.5),
                                                                 new Double(-11),
                                                                 new Double(-27.5) },
                MathArrays.OrderDirection.DECREASING, false));
        Assert.assertTrue(MathArrays.isMonotonic(new Double[] { new Double(3),
                                                                new Double(0),
                                                                new Double(0),
                                                                new Double(-5.5),
                                                                new Double(-11),
                                                                new Double(-27.5) },
                MathArrays.OrderDirection.DECREASING, false));
    }

// org.apache.commons.math3.util.MathArraysTest::testCheckRectangular
    public void testCheckRectangular() {
        final long[][] rect = new long[][] {{0, 1}, {2, 3}};
        final long[][] ragged = new long[][] {{0, 1}, {2}};
        final long[][] nullArray = null;
        final long[][] empty = new long[][] {};
        MathArrays.checkRectangular(rect);
        MathArrays.checkRectangular(empty);
        try {
            MathArrays.checkRectangular(ragged);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }
        try {
            MathArrays.checkRectangular(nullArray);
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        } 
    }

// org.apache.commons.math3.util.MathArraysTest::testCheckPositive
    public void testCheckPositive() {
        final double[] positive = new double[] {1, 2, 3};
        final double[] nonNegative = new double[] {0, 1, 2};
        final double[] nullArray = null;
        final double[] empty = new double[] {};
        MathArrays.checkPositive(positive);
        MathArrays.checkPositive(empty);
        try {
            MathArrays.checkPositive(nullArray);
            Assert.fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
        try {
            MathArrays.checkPositive(nonNegative);
            Assert.fail("Expecting NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCheckNonNegative
    public void testCheckNonNegative() {
        final long[] nonNegative = new long[] {0, 1};
        final long[] hasNegative = new long[] {-1};
        final long[] nullArray = null;
        final long[] empty = new long[] {};
        MathArrays.checkNonNegative(nonNegative);
        MathArrays.checkNonNegative(empty);
        try {
            MathArrays.checkNonNegative(nullArray);
            Assert.fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
        try {
            MathArrays.checkNonNegative(hasNegative);
            Assert.fail("Expecting NotPositiveException");
        } catch (NotPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCheckNonNegative2D
    public void testCheckNonNegative2D() {
        final long[][] nonNegative = new long[][] {{0, 1}, {1, 0}};
        final long[][] hasNegative = new long[][] {{-1}, {0}};
        final long[][] nullArray = null;
        final long[][] empty = new long[][] {};
        MathArrays.checkNonNegative(nonNegative);
        MathArrays.checkNonNegative(empty);
        try {
            MathArrays.checkNonNegative(nullArray);
            Assert.fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        }
        try {
            MathArrays.checkNonNegative(hasNegative);
            Assert.fail("Expecting NotPositiveException");
        } catch (NotPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testSortInPlace
    public void testSortInPlace() {
        final double[] x1 = {2,   5,  -3, 1,  4};
        final double[] x2 = {4,  25,   9, 1, 16};
        final double[] x3 = {8, 125, -27, 1, 64};

        MathArrays.sortInPlace(x1, x2, x3);

        Assert.assertEquals(-3,  x1[0], Math.ulp(1d));
        Assert.assertEquals(9,   x2[0], Math.ulp(1d));
        Assert.assertEquals(-27, x3[0], Math.ulp(1d));

        Assert.assertEquals(1, x1[1], Math.ulp(1d));
        Assert.assertEquals(1, x2[1], Math.ulp(1d));
        Assert.assertEquals(1, x3[1], Math.ulp(1d));

        Assert.assertEquals(2, x1[2], Math.ulp(1d));
        Assert.assertEquals(4, x2[2], Math.ulp(1d));
        Assert.assertEquals(8, x3[2], Math.ulp(1d));

        Assert.assertEquals(4,  x1[3], Math.ulp(1d));
        Assert.assertEquals(16, x2[3], Math.ulp(1d));
        Assert.assertEquals(64, x3[3], Math.ulp(1d));

        Assert.assertEquals(5,   x1[4], Math.ulp(1d));
        Assert.assertEquals(25,  x2[4], Math.ulp(1d));
        Assert.assertEquals(125, x3[4], Math.ulp(1d));
    }

// org.apache.commons.math3.util.MathArraysTest::testSortInPlaceDecresasingOrder
    public void testSortInPlaceDecresasingOrder() {
        final double[] x1 = {2,   5,  -3, 1,  4};
        final double[] x2 = {4,  25,   9, 1, 16};
        final double[] x3 = {8, 125, -27, 1, 64};

        MathArrays.sortInPlace(x1,
                               MathArrays.OrderDirection.DECREASING,
                               x2, x3);

        Assert.assertEquals(-3,  x1[4], Math.ulp(1d));
        Assert.assertEquals(9,   x2[4], Math.ulp(1d));
        Assert.assertEquals(-27, x3[4], Math.ulp(1d));

        Assert.assertEquals(1, x1[3], Math.ulp(1d));
        Assert.assertEquals(1, x2[3], Math.ulp(1d));
        Assert.assertEquals(1, x3[3], Math.ulp(1d));

        Assert.assertEquals(2, x1[2], Math.ulp(1d));
        Assert.assertEquals(4, x2[2], Math.ulp(1d));
        Assert.assertEquals(8, x3[2], Math.ulp(1d));

        Assert.assertEquals(4,  x1[1], Math.ulp(1d));
        Assert.assertEquals(16, x2[1], Math.ulp(1d));
        Assert.assertEquals(64, x3[1], Math.ulp(1d));

        Assert.assertEquals(5,   x1[0], Math.ulp(1d));
        Assert.assertEquals(25,  x2[0], Math.ulp(1d));
        Assert.assertEquals(125, x3[0], Math.ulp(1d));
    }

// org.apache.commons.math3.util.MathArraysTest::testSortInPlaceExample
    public void testSortInPlaceExample() {
        final double[] x = {3, 1, 2};
        final double[] y = {1, 2, 3};
        final double[] z = {0, 5, 7};
        MathArrays.sortInPlace(x, y, z);
        final double[] sx = {1, 2, 3};
        final double[] sy = {2, 3, 1};
        final double[] sz = {5, 7, 0};
        Assert.assertTrue(Arrays.equals(sx, x));
        Assert.assertTrue(Arrays.equals(sy, y));
        Assert.assertTrue(Arrays.equals(sz, z));
    }

// org.apache.commons.math3.util.MathArraysTest::testSortInPlaceFailures
    public void testSortInPlaceFailures() {
        final double[] nullArray = null;
        final double[] one = {1};
        final double[] two = {1, 2};
        final double[] onep = {2};
        try {
            MathArrays.sortInPlace(one, two);
            Assert.fail("Expecting DimensionMismatchException");
        } catch (DimensionMismatchException ex) {
            
        }
        try {
            MathArrays.sortInPlace(one, nullArray);
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
        try {
            MathArrays.sortInPlace(one, onep, nullArray);
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfInt
    public void testCopyOfInt() {
        final int[] source = { Integer.MIN_VALUE,
                               -1, 0, 1, 3, 113, 4769,
                               Integer.MAX_VALUE };
        final int[] dest = MathArrays.copyOf(source);

        Assert.assertEquals(dest.length, source.length);
        for (int i = 0; i < source.length; i++) {
            Assert.assertEquals(source[i], dest[i]);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfInt2
    public void testCopyOfInt2() {
        final int[] source = { Integer.MIN_VALUE,
                               -1, 0, 1, 3, 113, 4769,
                               Integer.MAX_VALUE };
        final int offset = 3;
        final int[] dest = MathArrays.copyOf(source, source.length - offset);

        Assert.assertEquals(dest.length, source.length - offset);
        for (int i = 0; i < source.length - offset; i++) {
            Assert.assertEquals(source[i], dest[i]);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfInt3
    public void testCopyOfInt3() {
        final int[] source = { Integer.MIN_VALUE,
                               -1, 0, 1, 3, 113, 4769,
                               Integer.MAX_VALUE };
        final int offset = 3;
        final int[] dest = MathArrays.copyOf(source, source.length + offset);

        Assert.assertEquals(dest.length, source.length + offset);
        for (int i = 0; i < source.length; i++) {
            Assert.assertEquals(source[i], dest[i]);
        }
        for (int i = source.length; i < source.length + offset; i++) {
            Assert.assertEquals(0, dest[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfDouble
    public void testCopyOfDouble() {
        final double[] source = { Double.NEGATIVE_INFINITY,
                                  -Double.MAX_VALUE,
                                  -1, 0,
                                  Double.MIN_VALUE,
                                  Math.ulp(1d),
                                  1, 3, 113, 4769,
                                  Double.MAX_VALUE,
                                  Double.POSITIVE_INFINITY };
        final double[] dest = MathArrays.copyOf(source);

        Assert.assertEquals(dest.length, source.length);
        for (int i = 0; i < source.length; i++) {
            Assert.assertEquals(source[i], dest[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfDouble2
    public void testCopyOfDouble2() {
        final double[] source = { Double.NEGATIVE_INFINITY,
                                  -Double.MAX_VALUE,
                                  -1, 0,
                                  Double.MIN_VALUE,
                                  Math.ulp(1d),
                                  1, 3, 113, 4769,
                                  Double.MAX_VALUE,
                                  Double.POSITIVE_INFINITY };
        final int offset = 3;
        final double[] dest = MathArrays.copyOf(source, source.length - offset);

        Assert.assertEquals(dest.length, source.length - offset);
        for (int i = 0; i < source.length - offset; i++) {
            Assert.assertEquals(source[i], dest[i], 0);
        }
    }

// org.apache.commons.math3.util.MathArraysTest::testCopyOfDouble3
    public void testCopyOfDouble3() {
        final double[] source = { Double.NEGATIVE_INFINITY,
                                  -Double.MAX_VALUE,
                                  -1, 0,
                                  Double.MIN_VALUE,
                                  Math.ulp(1d),
                                  1, 3, 113, 4769,
                                  Double.MAX_VALUE,
                                  Double.POSITIVE_INFINITY };
        final int offset = 3;
        final double[] dest = MathArrays.copyOf(source, source.length + offset);

        Assert.assertEquals(dest.length, source.length + offset);
        for (int i = 0; i < source.length; i++) {
            Assert.assertEquals(source[i], dest[i], 0);
        }
        for (int i = source.length; i < source.length + offset; i++) {
            Assert.assertEquals(0, dest[i], 0);
        }
    }
