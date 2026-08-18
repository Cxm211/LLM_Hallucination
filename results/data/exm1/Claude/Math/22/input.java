// buggy code
    public boolean isSupportLowerBoundInclusive() {
        return true;
    }

    public boolean isSupportUpperBoundInclusive() {
        return false;
    }

// relevant test
// org.apache.commons.math3.distribution.FDistributionTest::testCumulativeProbabilityExtremes
    public void testCumulativeProbabilityExtremes() {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.FDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.FDistributionTest::testDfAccessors
    public void testDfAccessors() {
        FDistribution dist = (FDistribution) getDistribution();
        Assert.assertEquals(5d, dist.getNumeratorDegreesOfFreedom(), Double.MIN_VALUE);
        Assert.assertEquals(6d, dist.getDenominatorDegreesOfFreedom(), Double.MIN_VALUE);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new FDistribution(0, 1);
            Assert.fail("Expecting NotStrictlyPositiveException for df = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
        try {
            new FDistribution(1, 0);
            Assert.fail("Expecting NotStrictlyPositiveException for df = 0");
        } catch (NotStrictlyPositiveException ex) {
            
        }
    }

// org.apache.commons.math3.distribution.FDistributionTest::testLargeDegreesOfFreedom
    public void testLargeDegreesOfFreedom() {
        FDistribution fd = new FDistribution(100000, 100000);
        double p = fd.cumulativeProbability(.999);
        double x = fd.inverseCumulativeProbability(p);
        Assert.assertEquals(.999, x, 1.0e-5);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testSmallDegreesOfFreedom
    public void testSmallDegreesOfFreedom() {
        FDistribution fd = new FDistribution(1, 1);
        double p = fd.cumulativeProbability(0.975);
        double x = fd.inverseCumulativeProbability(p);
        Assert.assertEquals(0.975, x, 1.0e-5);

        fd = new FDistribution(1, 2);
        p = fd.cumulativeProbability(0.975);
        x = fd.inverseCumulativeProbability(p);
        Assert.assertEquals(0.975, x, 1.0e-5);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        FDistribution dist;

        dist = new FDistribution(1, 2);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));

        dist = new FDistribution(1, 3);
        Assert.assertEquals(dist.getNumericalMean(), 3d / (3d - 2d), tol);
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));

        dist = new FDistribution(1, 5);
        Assert.assertEquals(dist.getNumericalMean(), 5d / (5d - 2d), tol);
        Assert.assertEquals(dist.getNumericalVariance(), (2d * 5d * 5d * 4d) / 9d, tol);
    }

// org.apache.commons.math3.distribution.FDistributionTest::testMath785
    public void testMath785() {
        

        try {
            double prob = 0.01;
            FDistribution f = new FDistribution(200000, 200000);
            double result = f.inverseCumulativeProbability(prob);
            Assert.assertTrue(result < 1.0);
        } catch (Exception e) {
            Assert.fail("Failing to calculate inverse cumulative probability");
        }
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testGetLowerBound
    public void testGetLowerBound() {
        UniformRealDistribution distribution = makeDistribution();
        Assert.assertEquals(-0.5, distribution.getSupportLowerBound(), 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testGetUpperBound
    public void testGetUpperBound() {
        UniformRealDistribution distribution = makeDistribution();
        Assert.assertEquals(1.25, distribution.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testPreconditions1
    public void testPreconditions1() {
        new UniformRealDistribution(0, 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testPreconditions2
    public void testPreconditions2() {
        new UniformRealDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testMeanVariance
    public void testMeanVariance() {
        UniformRealDistribution dist;

        dist = new UniformRealDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 0.5, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 1/12.0, 0);

        dist = new UniformRealDistribution(-1.5, 0.6);
        Assert.assertEquals(dist.getNumericalMean(), -0.45, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 0.3675, 0);

        dist = new UniformRealDistribution(-0.5, 1.25);
        Assert.assertEquals(dist.getNumericalMean(), 0.375, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 0.2552083333333333, 0);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testFit
    public void testFit() {
        final RealDistribution rng = new UniformRealDistribution(-100, 100);
        rng.reseedRandomGenerator(64925784252L);

        final LevenbergMarquardtOptimizer optim = new LevenbergMarquardtOptimizer();
        final PolynomialFitter fitter = new PolynomialFitter(optim);
        final double[] coeff = { 12.9, -3.4, 2.1 }; 
        final PolynomialFunction f = new PolynomialFunction(coeff);

        
        for (int i = 0; i < 100; i++) {
            final double x = rng.sample();
            fitter.addObservedPoint(x, f.value(x));
        }

        
        final double[] best = fitter.fit(new double[] { -1e-20, 3e15, -5e25 });

        TestUtils.assertEquals("best != coeff", coeff, best, 1e-12);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testNoError
    public void testNoError() {
        Random randomizer = new Random(64925784252l);
        for (int degree = 1; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
            for (int i = 0; i <= degree; ++i) {
                fitter.addObservedPoint(1.0, i, p.value(i));
            }

            final double[] init = new double[degree + 1];
            PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                               (1.0 + FastMath.abs(p.value(x)));
                Assert.assertEquals(0.0, error, 1.0e-6);
            }
        }
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testSmallError
    public void testSmallError() {
        Random randomizer = new Random(53882150042l);
        double maxError = 0;
        for (int degree = 0; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
            for (double x = -1.0; x < 1.0; x += 0.01) {
                fitter.addObservedPoint(1.0, x,
                                        p.value(x) + 0.1 * randomizer.nextGaussian());
            }

            final double[] init = new double[degree + 1];
            PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                              (1.0 + FastMath.abs(p.value(x)));
                maxError = FastMath.max(maxError, error);
                Assert.assertTrue(FastMath.abs(error) < 0.1);
            }
        }
        Assert.assertTrue(maxError > 0.01);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testMath798
    public void testMath798() {
        final double tol = 1e-14;
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol);
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 3;

        final double[] lm = doMath798(new LevenbergMarquardtOptimizer(checker), maxEval, init);
        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);

        for (int i = 0; i <= 1; i++) {
            Assert.assertEquals(lm[i], gn[i], tol);
        }
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testMath798WithToleranceTooLow
    public void testMath798WithToleranceTooLow() {
        final double tol = 1e-100;
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol);
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 10000; 

        final double[] lm = doMath798(new LevenbergMarquardtOptimizer(checker), maxEval, init);
        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);

        for (int i = 0; i <= 1; i++) {
            Assert.assertEquals(lm[i], gn[i], tol);
        }
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testRedundantSolvable
    public void testRedundantSolvable() {
        
        checkUnsolvableProblem(new LevenbergMarquardtOptimizer(), true);
    }

// org.apache.commons.math3.optimization.fitting.PolynomialFitterTest::testRedundantUnsolvable
    public void testRedundantUnsolvable() {
        
        checkUnsolvableProblem(new GaussNewtonOptimizer(true, new SimpleVectorValueChecker(1e-15, 1e-15)), false);
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testNonInvertible
    public void testNonInvertible() {
        
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });

        AbstractLeastSquaresOptimizer optimizer = createOptimizer();
        optimizer.optimize(100, problem, problem.target, new double[] { 1, 1, 1 }, new double[] { 0, 0, 0 });
        Assert.assertTrue(FastMath.sqrt(problem.target.length) * optimizer.getRMS() > 0.6);

        double[][] cov = optimizer.getCovariances(1.5e-14);
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testControlParameters
    public void testControlParameters() {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        checkEstimate(circle, 0.1, 10, 1.0e-14, 1.0e-16, 1.0e-10, false);
        checkEstimate(circle, 0.1, 10, 1.0e-15, 1.0e-17, 1.0e-10, true);
        checkEstimate(circle, 0.1,  5, 1.0e-15, 1.0e-16, 1.0e-10, true);
        circle.addPoint(300, -300);
        checkEstimate(circle, 0.1, 20, 1.0e-18, 1.0e-16, 1.0e-10, true);
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testMath199
    public void testMath199() {
        try {
            QuadraticProblem problem = new QuadraticProblem();
            problem.addPoint (0, -3.182591015485607);
            problem.addPoint (1, -2.5581184967730577);
            problem.addPoint (2, -2.1488478161387325);
            problem.addPoint (3, -1.9122489313410047);
            problem.addPoint (4, 1.7785661310051026);
            LevenbergMarquardtOptimizer optimizer
                = new LevenbergMarquardtOptimizer(100, 1e-10, 1e-10, 1e-10, 0);
            optimizer.optimize(100, problem,
                               new double[] { 0, 0, 0, 0, 0 },
                               new double[] { 0.0, 4.4e-323, 1.0, 4.4e-323, 0.0 },
                               new double[] { 0, 0, 0 });
            Assert.fail("an exception should have been thrown");
        } catch (ConvergenceException ee) {
            
        }
    }

// org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest::testBevington
    public void testBevington() {
        final double[][] dataPoints = {
            
            { 15, 30, 45, 60, 75, 90, 105, 120, 135, 150,
              165, 180, 195, 210, 225, 240, 255, 270, 285, 300,
              315, 330, 345, 360, 375, 390, 405, 420, 435, 450,
              465, 480, 495, 510, 525, 540, 555, 570, 585, 600,
              615, 630, 645, 660, 675, 690, 705, 720, 735, 750,
              765, 780, 795, 810, 825, 840, 855, 870, 885, },
            
            { 775, 479, 380, 302, 185, 157, 137, 119, 110, 89,
              74, 61, 66, 68, 48, 54, 51, 46, 55, 29,
              28, 37, 49, 26, 35, 29, 31, 24, 25, 35,
              24, 30, 26, 28, 21, 18, 20, 27, 17, 17,
              14, 17, 24, 11, 22, 17, 12, 10, 13, 16,
              9, 9, 14, 21, 17, 13, 12, 18, 10, },
        };

        final BevingtonProblem problem = new BevingtonProblem();

        final int len = dataPoints[0].length;
        final double[] weights = new double[len];
        for (int i = 0; i < len; i++) {
            problem.addPoint(dataPoints[0][i],
                             dataPoints[1][i]);

            weights[i] = 1 / dataPoints[1][i];
        }

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();

        final PointVectorValuePair optimum =
            optimizer.optimize(100, problem, dataPoints[1], weights,
                               new double[] { 10, 900, 80, 27, 225 });

        final double chi2 = optimizer.getChiSquare();
        final double[] solution = optimum.getPoint();
        final double[] expectedSolution = { 10.4, 958.3, 131.4, 33.9, 205.0 };

        final double[][] covarMatrix = optimizer.getCovariances();
        final double[][] expectedCovarMatrix = {
            { 3.38, -3.69, 27.98, -2.34, -49.24 },
            { -3.69, 2492.26, 81.89, -69.21, -8.9 },
            { 27.98, 81.89, 468.99, -44.22, -615.44 },
            { -2.34, -69.21, -44.22, 6.39, 53.80 },
            { -49.24, -8.9, -615.44, 53.8, 929.45 }
        };

        final int numParams = expectedSolution.length;

        
        for (int i = 0; i < numParams; i++) {
            final double error = FastMath.sqrt(expectedCovarMatrix[i][i]);
            Assert.assertEquals("Parameter " + i, expectedSolution[i], solution[i], error);
        }

        
        
        for (int i = 0; i < numParams; i++) {
            for (int j = 0; j < numParams; j++) {
                Assert.assertEquals("Covariance matrix [" + i + "][" + j + "]",
                                    expectedCovarMatrix[i][j],
                                    covarMatrix[i][j],
                                    FastMath.abs(0.1 * expectedCovarMatrix[i][j]));
            }
        }
    }

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
        for (Point2D.Double p : factory.generate(numPoints)) {
            circle.addPoint(p.x, p.y);
            
        }

        
        final double[] init = { 90, 659, 115 };

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();
        final PointVectorValuePair optimum = optimizer.optimize(100, circle,
                                                                circle.target(), circle.weight(),
                                                                init);

        final double[] paramFound = optimum.getPoint();

        
        final double[][] covMatrix = optimizer.getCovariances();
        final double[] asymptoticStandardErrorFound = optimizer.guessParametersErrors();
        final double[] sigmaFound = new double[covMatrix.length];
        for (int i = 0; i < covMatrix.length; i++) {
            sigmaFound[i] = FastMath.sqrt(covMatrix[i][i]);

        }

        

        
        Assert.assertEquals(xCenter, paramFound[0], asymptoticStandardErrorFound[0]);
        Assert.assertEquals(yCenter, paramFound[1], asymptoticStandardErrorFound[1]);
        Assert.assertEquals(radius, paramFound[2], asymptoticStandardErrorFound[2]);
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

// org.apache.commons.math3.util.MathUtilsTest::testHash
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
                    Assert.assertEquals(MathUtils.hash(testArray[i]), MathUtils.hash(testArray[j]));
                    Assert.assertEquals(MathUtils.hash(testArray[j]), MathUtils.hash(testArray[i]));
                } else {
                    Assert.assertTrue(MathUtils.hash(testArray[i]) != MathUtils.hash(testArray[j]));
                    Assert.assertTrue(MathUtils.hash(testArray[j]) != MathUtils.hash(testArray[i]));
                }
            }
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testArrayHash
    public void testArrayHash() {
        Assert.assertEquals(0, MathUtils.hash((double[]) null));
        Assert.assertEquals(MathUtils.hash(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }),
                     MathUtils.hash(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }));
        Assert.assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { FastMath.nextAfter(1d, 2d) }));
        Assert.assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { 1d, 1d }));
    }

// org.apache.commons.math3.util.MathUtilsTest::testPermutedArrayHash
    public void testPermutedArrayHash() {
        double[] original = new double[10];
        double[] permuted = new double[10];
        RandomDataImpl random = new RandomDataImpl();

        
        for (int i = 0; i < 10; i++) {
            final RealDistribution u = new UniformRealDistribution(i + 0.5, i + 0.75);
            original[i] = u.sample();
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

        
        Assert.assertFalse(MathUtils.hash(original) == MathUtils.hash(permuted));
    }

// org.apache.commons.math3.util.MathUtilsTest::testIndicatorByte
    public void testIndicatorByte() {
        Assert.assertEquals((byte)1, MathUtils.copySign((byte)1, (byte)2));
        Assert.assertEquals((byte)1, MathUtils.copySign((byte)1, (byte)0));
        Assert.assertEquals((byte)(-1), MathUtils.copySign((byte)1, (byte)(-2)));
    }

// org.apache.commons.math3.util.MathUtilsTest::testIndicatorInt
    public void testIndicatorInt() {
        Assert.assertEquals(1, MathUtils.copySign(1, 2));
        Assert.assertEquals(1, MathUtils.copySign(1, 0));
        Assert.assertEquals((-1), MathUtils.copySign(1, -2));
    }

// org.apache.commons.math3.util.MathUtilsTest::testIndicatorLong
    public void testIndicatorLong() {
        Assert.assertEquals(1L, MathUtils.copySign(1L, 2L));
        Assert.assertEquals(1L, MathUtils.copySign(1L, 0L));
        Assert.assertEquals(-1L, MathUtils.copySign(1L, -2L));
    }

// org.apache.commons.math3.util.MathUtilsTest::testIndicatorShort
    public void testIndicatorShort() {
        Assert.assertEquals((short)1, MathUtils.copySign((short)1, (short)2));
        Assert.assertEquals((short)1, MathUtils.copySign((short)1, (short)0));
        Assert.assertEquals((short)(-1), MathUtils.copySign((short)1, (short)(-2)));
    }

// org.apache.commons.math3.util.MathUtilsTest::testNormalizeAngle
    public void testNormalizeAngle() {
        for (double a = -15.0; a <= 15.0; a += 0.1) {
            for (double b = -15.0; b <= 15.0; b += 0.2) {
                double c = MathUtils.normalizeAngle(a, b);
                Assert.assertTrue((b - FastMath.PI) <= c);
                Assert.assertTrue(c <= (b + FastMath.PI));
                double twoK = FastMath.rint((a - c) / FastMath.PI);
                Assert.assertEquals(c, a - twoK * FastMath.PI, 1.0e-14);
            }
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testReduce
    public void testReduce() {
        final double period = -12.222;
        final double offset = 13;

        final double delta = 1.5;

        double orig = offset + 122456789 * period + delta;
        double expected = delta;
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, period, offset),
                            1e-7);
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, -period, offset),
                            1e-7);

        orig = offset - 123356789 * period - delta;
        expected = Math.abs(period) - delta;
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, period, offset),
                            1e-6);
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, -period, offset),
                            1e-6);

        orig = offset - 123446789 * period + delta;
        expected = delta;
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, period, offset),
                            1e-6);
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, -period, offset),
                            1e-6);

        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig, Double.NaN, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.NaN, period, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig, period, Double.NaN)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig, period,
                Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                period, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig,
                Double.POSITIVE_INFINITY, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                period, Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,  Double.POSITIVE_INFINITY)));
    }

// org.apache.commons.math3.util.MathUtilsTest::testReduceComparedWithNormalizeAngle
    public void testReduceComparedWithNormalizeAngle() {
        final double tol = Math.ulp(1d);
        final double period = 2 * Math.PI;
        for (double a = -15; a <= 15; a += 0.5) {
            for (double center = -15; center <= 15; center += 1) {
                final double nA = MathUtils.normalizeAngle(a, center);
                final double offset = center - Math.PI;
                final double r = MathUtils.reduce(a, period, offset);
                Assert.assertEquals(nA, r + offset, tol);
            }
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testSignByte
    public void testSignByte() {
        final byte one = (byte) 1;
        Assert.assertEquals((byte) 1, MathUtils.copySign(one, (byte) 2));
        Assert.assertEquals((byte) (-1), MathUtils.copySign(one, (byte) (-2)));
    }

// org.apache.commons.math3.util.MathUtilsTest::testSignInt
    public void testSignInt() {
        final int one = 1;
        Assert.assertEquals(1, MathUtils.copySign(one, 2));
        Assert.assertEquals((-1), MathUtils.copySign(one, -2));
    }

// org.apache.commons.math3.util.MathUtilsTest::testSignLong
    public void testSignLong() {
        final long one = 1L;
        Assert.assertEquals(1L, MathUtils.copySign(one, 2L));
        Assert.assertEquals(-1L, MathUtils.copySign(one, -2L));
    }

// org.apache.commons.math3.util.MathUtilsTest::testSignShort
    public void testSignShort() {
        final short one = (short) 1;
        Assert.assertEquals((short) 1, MathUtils.copySign(one, (short) 2));
        Assert.assertEquals((short) (-1), MathUtils.copySign(one, (short) (-2)));
    }

// org.apache.commons.math3.util.MathUtilsTest::testCheckFinite
    public void testCheckFinite() {
        try {
            MathUtils.checkFinite(Double.POSITIVE_INFINITY);
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(Double.NEGATIVE_INFINITY);
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(Double.NaN);
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }

        try {
            MathUtils.checkFinite(new double[] {0, -1, Double.POSITIVE_INFINITY, -2, 3});
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(new double[] {1, Double.NEGATIVE_INFINITY, -2, 3});
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
        try {
            MathUtils.checkFinite(new double[] {4, 3, -1, Double.NaN, -2, 1});
            Assert.fail("an exception should have been thrown");
        } catch (NotFiniteNumberException e) {
            
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testCheckNotNull1
    public void testCheckNotNull1() {
        try {
            Object obj = null;
            MathUtils.checkNotNull(obj);
        } catch (NullArgumentException e) {
            
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testCheckNotNull2
    public void testCheckNotNull2() {
        try {
            double[] array = null;
            MathUtils.checkNotNull(array, LocalizedFormats.INPUT_ARRAY);
        } catch (NullArgumentException e) {
            
        }
    }

// org.apache.commons.math3.util.MathUtilsTest::testCopySignByte
    public void testCopySignByte() {
        byte a = MathUtils.copySign(Byte.MIN_VALUE, (byte) -1);
        Assert.assertEquals(Byte.MIN_VALUE, a);

        final byte minValuePlusOne = Byte.MIN_VALUE + (byte) 1;
        a = MathUtils.copySign(minValuePlusOne, (byte) 1);
        Assert.assertEquals(Byte.MAX_VALUE, a);

        a = MathUtils.copySign(Byte.MAX_VALUE, (byte) -1);
        Assert.assertEquals(minValuePlusOne, a);

        final byte one = 1;
        byte val = -2;
        a = MathUtils.copySign(val, one);
        Assert.assertEquals(-val, a);

        final byte minusOne = -one;
        val = 2;
        a = MathUtils.copySign(val, minusOne);
        Assert.assertEquals(-val, a);

        val = 0;
        a = MathUtils.copySign(val, minusOne);
        Assert.assertEquals(val, a);

        val = 0;
        a = MathUtils.copySign(val, one);
        Assert.assertEquals(val, a);
    }

// org.apache.commons.math3.util.MathUtilsTest::testCopySignByte2
    public void testCopySignByte2() {
        MathUtils.copySign(Byte.MIN_VALUE, (byte) 1);
    }

// org.apache.commons.math3.distribution.RealDistributionAbstractTest::testCumulativeProbabilities
    public void testCumulativeProbabilities() {
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.RealDistributionAbstractTest::testInverseCumulativeProbabilities
    public void testInverseCumulativeProbabilities() {
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.RealDistributionAbstractTest::testDensities
    public void testDensities() {
        verifyDensities();
    }

// org.apache.commons.math3.distribution.RealDistributionAbstractTest::testConsistency
    public void testConsistency() {
        for (int i=1; i < cumulativeTestPoints.length; i++) {

            
            
            TestUtils.assertEquals(0d,
               distribution.cumulativeProbability
                 (cumulativeTestPoints[i], cumulativeTestPoints[i]), tolerance);

            
            double upper = FastMath.max(cumulativeTestPoints[i], cumulativeTestPoints[i -1]);
            double lower = FastMath.min(cumulativeTestPoints[i], cumulativeTestPoints[i -1]);
            double diff = distribution.cumulativeProbability(upper) -
                distribution.cumulativeProbability(lower);
            
            double direct = distribution.cumulativeProbability(lower, upper);
            TestUtils.assertEquals("Inconsistent cumulative probabilities for ("
                    + lower + "," + upper + ")", diff, direct, tolerance);
        }
    }

// org.apache.commons.math3.distribution.RealDistributionAbstractTest::testIllegalArguments
    public void testIllegalArguments() {
        try {
            
            distribution.cumulativeProbability(1, 0);
            Assert.fail("Expecting MathIllegalArgumentException for bad cumulativeProbability interval");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            distribution.inverseCumulativeProbability(-1);
            Assert.fail("Expecting MathIllegalArgumentException for p = -1");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            distribution.inverseCumulativeProbability(2);
            Assert.fail("Expecting MathIllegalArgumentException for p = 2");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.distribution.RealDistributionAbstractTest::testSampling
    public void testSampling() {
        final int sampleSize = 1000;
        distribution.reseedRandomGenerator(1000); 
        double[] sample = distribution.sample(sampleSize);
        double[] quartiles = TestUtils.getDistributionQuartiles(distribution);
        double[] expected = {250, 250, 250, 250};
        long[] counts = new long[4];
        for (int i = 0; i < sampleSize; i++) {
            TestUtils.updateCounts(sample[i], counts, quartiles);
        }
        TestUtils.assertChiSquareAccept(expected, counts, 0.001);
    }

// org.apache.commons.math3.distribution.RealDistributionAbstractTest::testDensityIntegrals
    public void testDensityIntegrals() {
        final double tol = 1.0e-9;
        final BaseAbstractUnivariateIntegrator integrator =
            new IterativeLegendreGaussIntegrator(5, 1.0e-12, 1.0e-10);
        final UnivariateFunction d = new UnivariateFunction() {
            public double value(double x) {
                return distribution.density(x);
            }
        };
        final ArrayList<Double> integrationTestPoints = new ArrayList<Double>();
        for (int i = 0; i < cumulativeTestPoints.length; i++) {
            if (Double.isNaN(cumulativeTestValues[i]) ||
                    cumulativeTestValues[i] < 1.0e-5 ||
                    cumulativeTestValues[i] > 1 - 1.0e-5) {
                continue; 
            }
            integrationTestPoints.add(cumulativeTestPoints[i]);
        }
        Collections.sort(integrationTestPoints);
        for (int i = 1; i < integrationTestPoints.size(); i++) {
            Assert.assertEquals(
                    distribution.cumulativeProbability(  
                            integrationTestPoints.get(0), integrationTestPoints.get(i)),
                            integrator.integrate(
                                    1000000, 
                                    d, integrationTestPoints.get(0),
                                    integrationTestPoints.get(i)), tol);
        }
    }

// org.apache.commons.math3.distribution.RealDistributionAbstractTest::testIsSupportLowerBoundInclusive
    public void testIsSupportLowerBoundInclusive() {
        final double lowerBound = distribution.getSupportLowerBound();
        double result = Double.NaN;
        result = distribution.density(lowerBound);
        Assert.assertEquals(
                !Double.isInfinite(lowerBound) && !Double.isNaN(result) &&
                !Double.isInfinite(result),
                distribution.isSupportLowerBoundInclusive());
         
    }

// org.apache.commons.math3.distribution.RealDistributionAbstractTest::testIsSupportUpperBoundInclusive
    public void testIsSupportUpperBoundInclusive() {
        final double upperBound = distribution.getSupportUpperBound();
        double result = Double.NaN;
        result = distribution.density(upperBound);
        Assert.assertEquals(
                !Double.isInfinite(upperBound) && !Double.isNaN(result) &&
                !Double.isInfinite(result),
                distribution.isSupportUpperBoundInclusive());
         
    }
