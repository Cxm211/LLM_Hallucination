// buggy code
    public double solve(double min, double max) throws MaxIterationsExceededException, 
        FunctionEvaluationException {
        
        clearResult();
        verifyInterval(min, max);
        
        double ret = Double.NaN;
        
        double yMin = f.value(min);
        double yMax = f.value(max);
        
        // Verify bracketing
        double sign = yMin * yMax;
        if (sign >= 0) {
            // check if either value is close to a zero
                // neither value is close to zero and min and max do not bracket root.
                throw new IllegalArgumentException
                ("Function values at endpoints do not have different signs." +
                        "  Endpoints: [" + min + "," + max + "]" + 
                        "  Values: [" + yMin + "," + yMax + "]");
        } else {
            // solve using only the first endpoint as initial guess
            ret = solve(min, yMin, max, yMax, min, yMin);
            // either min or max is a root
        }

        return ret;
    }

// relevant test
// org.apache.commons.math.analysis.BrentSolverTest::testSinZero
    public void testSinZero() throws MathException {
        
        
        
        UnivariateRealFunction f = new SinFunction();
        double result;
        UnivariateRealSolver solver = new BrentSolver(f);
        
        result = solver.solve(3, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 5);
        
        result = solver.solve(1, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 6);
        solver = new SecantSolver(f);
        result = solver.solve(3, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 5);
        result = solver.solve(1, 4);
        
        
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 6);
        assertEquals(result, solver.getResult(), 0);
    }

// org.apache.commons.math.analysis.BrentSolverTest::testQuinticZero
    public void testQuinticZero() throws MathException {
        
        
        
        
        
        
        
        UnivariateRealFunction f = new QuinticFunction();
        double result;
        
        UnivariateRealSolver solver = new BrentSolver(f);
        
        
        result = solver.solve(-0.2, 0.2);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        assertTrue(solver.getIterationCount() <= 2);
        
        
        result = solver.solve(-0.1, 0.3);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 6);
        
        result = solver.solve(-0.3, 0.45);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 7);
        
        result = solver.solve(0.3, 0.7);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 7);
        
        result = solver.solve(0.2, 0.6);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 7);
        
        result = solver.solve(0.05, 0.95);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 9);
        
        
        result = solver.solve(0.85, 1.25);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 9);
        
        result = solver.solve(0.8, 1.2);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 9);
        
        result = solver.solve(0.85, 1.75);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 11);
        
        result = solver.solve(0.55, 1.45);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 8);
        
        result = solver.solve(0.85, 5);
        
       
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 13);
        
        solver = new SecantSolver(f);
        result = solver.solve(-0.2, 0.2);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 2);
        result = solver.solve(-0.1, 0.3);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 6);
        result = solver.solve(-0.3, 0.45);
        
        
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 7);
        result = solver.solve(0.3, 0.7);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 8);
        result = solver.solve(0.2, 0.6);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 7);
        result = solver.solve(0.05, 0.95);
        
        
        assertEquals(result, 0.5, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 9);
        result = solver.solve(0.85, 1.25);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 11);
        result = solver.solve(0.8, 1.2);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 9);
        result = solver.solve(0.85, 1.75);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 15);
        
        
        result = solver.solve(0.55, 1.45);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 8);
        result = solver.solve(0.85, 5);
        
        
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        
        assertTrue(solver.getIterationCount() <= 15);
        
        result = UnivariateRealSolverUtils.solve(f, -0.2, 0.2);
        assertEquals(result, 0, solver.getAbsoluteAccuracy());
        result = UnivariateRealSolverUtils.solve(f, -0.1, 0.3);
        assertEquals(result, 0, 1E-8);
        result = UnivariateRealSolverUtils.solve(f, -0.3, 0.45);
        assertEquals(result, 0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.3, 0.7);
        assertEquals(result, 0.5, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.2, 0.6);
        assertEquals(result, 0.5, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.05, 0.95);
        assertEquals(result, 0.5, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.85, 1.25);
        assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.8, 1.2);
        assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.85, 1.75);
        assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.55, 1.45);
        assertEquals(result, 1.0, 1E-6);
        result = UnivariateRealSolverUtils.solve(f, 0.85, 5);
        assertEquals(result, 1.0, 1E-6);
    }

// org.apache.commons.math.analysis.BrentSolverTest::testRootEndpoints
    public void testRootEndpoints() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BrentSolver(f);
        
        
        double result = solver.solve(Math.PI, 4);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());

        result = solver.solve(3, Math.PI);
        assertEquals(result, Math.PI, solver.getAbsoluteAccuracy());
    }

// org.apache.commons.math.analysis.BrentSolverTest::testBadEndpoints
    public void testBadEndpoints() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BrentSolver(f);
        try {  
            solver.solve(1, -1);
            fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            
        }
        try {  
            solver.solve(1, 1.5);
            fail("Expecting IllegalArgumentException - non-bracketing");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.BrentSolverTest::testInitialGuess
    public void testInitialGuess() throws MathException {

        MonitoredFunction f = new MonitoredFunction(new QuinticFunction());
        UnivariateRealSolver solver = new BrentSolver(f);
        double result;

        
        result = solver.solve(0.6, 7.0);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        int referenceCallsCount = f.getCallsCount();
        assertTrue(referenceCallsCount >= 13);
 
        
        try {
          result = solver.solve(0.6, 7.0, 0.0);
          fail("an IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught: " + e.getMessage());
        }
 
        
        f.setCallsCount(0);
        result = solver.solve(0.6, 7.0, 0.61);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        assertTrue(f.getCallsCount() > referenceCallsCount);
 
        
        f.setCallsCount(0);
        result = solver.solve(0.6, 7.0, 0.999999);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        assertTrue(f.getCallsCount() < referenceCallsCount);

        
        f.setCallsCount(0);
        result = solver.solve(0.6, 7.0, 1.0);
        assertEquals(result, 1.0, solver.getAbsoluteAccuracy());
        assertEquals(0, solver.getIterationCount());
        assertEquals(1, f.getCallsCount());
 
    }

// org.apache.commons.math.analysis.UnivariateRealSolverFactoryImplTest::testNewBisectionSolverNull
    public void testNewBisectionSolverNull() {
        try {
            factory.newBisectionSolver(null);
            fail();
        } catch(IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.UnivariateRealSolverFactoryImplTest::testNewBisectionSolverValid
    public void testNewBisectionSolverValid() {
        UnivariateRealSolver solver = factory.newBisectionSolver(function);
        assertNotNull(solver);
        assertTrue(solver instanceof BisectionSolver);
    }

// org.apache.commons.math.analysis.UnivariateRealSolverFactoryImplTest::testNewNewtonSolverNull
    public void testNewNewtonSolverNull() {
        try {
            factory.newNewtonSolver(null);
            fail();
        } catch(IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.UnivariateRealSolverFactoryImplTest::testNewNewtonSolverValid
    public void testNewNewtonSolverValid() {
        UnivariateRealSolver solver = factory.newNewtonSolver(function);
        assertNotNull(solver);
        assertTrue(solver instanceof NewtonSolver);
    }

// org.apache.commons.math.analysis.UnivariateRealSolverFactoryImplTest::testNewBrentSolverNull
    public void testNewBrentSolverNull() {
        try {
            factory.newBrentSolver(null);
            fail();
        } catch(IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.UnivariateRealSolverFactoryImplTest::testNewBrentSolverValid
    public void testNewBrentSolverValid() {
        UnivariateRealSolver solver = factory.newBrentSolver(function);
        assertNotNull(solver);
        assertTrue(solver instanceof BrentSolver);
    }

// org.apache.commons.math.analysis.UnivariateRealSolverFactoryImplTest::testNewSecantSolverNull
    public void testNewSecantSolverNull() {
        try {
            factory.newSecantSolver(null);
            fail();
        } catch(IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.UnivariateRealSolverFactoryImplTest::testNewSecantSolverValid
    public void testNewSecantSolverValid() {
        UnivariateRealSolver solver = factory.newSecantSolver(function);
        assertNotNull(solver);
        assertTrue(solver instanceof SecantSolver);
    }

// org.apache.commons.math.analysis.UnivariateRealSolverUtilsTest::testSolveNull
    public void testSolveNull() throws MathException {
        try {
            UnivariateRealSolverUtils.solve(null, 0.0, 4.0);
            fail();
        } catch(IllegalArgumentException ex){
            
        }
    }

// org.apache.commons.math.analysis.UnivariateRealSolverUtilsTest::testSolveBadParameters
    public void testSolveBadParameters() throws MathException {
        try { 
            UnivariateRealSolverUtils.solve(sin,0.0, 4.0, 4.0); 
        } catch (IllegalArgumentException ex) {
            
        }    
        try { 
            UnivariateRealSolverUtils.solve(sin, 0.0, 4.0, 0.0); 
        } catch (IllegalArgumentException ex) {
            
        }        
    }

// org.apache.commons.math.analysis.UnivariateRealSolverUtilsTest::testSolveSin
    public void testSolveSin() throws MathException {     
        double x = UnivariateRealSolverUtils.solve(sin, 1.0, 4.0);
        assertEquals(Math.PI, x, 1.0e-4);
    }

// org.apache.commons.math.analysis.UnivariateRealSolverUtilsTest::testSolveAccuracyNull
    public void testSolveAccuracyNull()  throws MathException {
        try {
            double accuracy = 1.0e-6;
            UnivariateRealSolverUtils.solve(null, 0.0, 4.0, accuracy);
            fail();
        } catch(IllegalArgumentException ex){
            
        }
    }

// org.apache.commons.math.analysis.UnivariateRealSolverUtilsTest::testSolveAccuracySin
    public void testSolveAccuracySin() throws MathException {
        double accuracy = 1.0e-6;
        double x = UnivariateRealSolverUtils.solve(sin, 1.0,
                4.0, accuracy);
        assertEquals(Math.PI, x, accuracy);
    }

// org.apache.commons.math.analysis.UnivariateRealSolverUtilsTest::testSolveNoRoot
    public void testSolveNoRoot() throws MathException {
        try {
            UnivariateRealSolverUtils.solve(sin, 1.0, 1.5);  
            fail("Expecting IllegalArgumentException ");  
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.analysis.UnivariateRealSolverUtilsTest::testBracketSin
    public void testBracketSin() throws MathException {
        double[] result = UnivariateRealSolverUtils.bracket(sin, 
                0.0, -2.0, 2.0);
        assertTrue(sin.value(result[0]) < 0);
        assertTrue(sin.value(result[1]) > 0);
    }

// org.apache.commons.math.analysis.UnivariateRealSolverUtilsTest::testBracketCornerSolution
    public void testBracketCornerSolution() throws MathException {
        try {
            UnivariateRealSolverUtils.bracket(sin, 1.5, 0, 2.0); 
            fail("Expecting ConvergenceException");
        } catch (ConvergenceException ex) {
            
        }
    }

// org.apache.commons.math.analysis.UnivariateRealSolverUtilsTest::testBadParameters
    public void testBadParameters() throws MathException {
        try { 
            UnivariateRealSolverUtils.bracket(null, 1.5, 0, 2.0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try { 
            UnivariateRealSolverUtils.bracket(sin, 2.5, 0, 2.0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try { 
            UnivariateRealSolverUtils.bracket(sin, 1.5, 2.0, 1.0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try { 
            UnivariateRealSolverUtils.bracket(sin, 1.5, 0, 2.0, 0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }        
    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testSmallDf
    public void testSmallDf() throws Exception {
        setDistribution(new ChiSquaredDistributionImpl(0.1d));
        setTolerance(1E-4);
        
        setCumulativeTestPoints(new double[] {1.168926E-60, 1.168926E-40, 1.063132E-32, 
                1.144775E-26, 1.168926E-20, 5.472917, 2.175255, 1.13438, 
                0.5318646, 0.1526342});
        setInverseCumulativeTestValues(getCumulativeTestPoints());
        setInverseCumulativeTestPoints(getCumulativeTestValues());
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testDfAccessors
    public void testDfAccessors() {
        ChiSquaredDistribution distribution = (ChiSquaredDistribution) getDistribution();
        assertEquals(5d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
        distribution.setDegreesOfFreedom(4d);
        assertEquals(4d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
        try {
            distribution.setDegreesOfFreedom(0d);
            fail("Expecting IllegalArgumentException for df = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.distribution.ChiSquareDistributionTest::testSampling
    public void testSampling() {}

// org.apache.commons.math.distribution.FDistributionTest::testCumulativeProbabilityExtremes
    public void testCumulativeProbabilityExtremes() throws Exception {
        setCumulativeTestPoints(new double[] {-2, 0});
        setCumulativeTestValues(new double[] {0, 0});
        verifyCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.FDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.FDistributionTest::testDfAccessors
    public void testDfAccessors() {
        FDistribution distribution = (FDistribution) getDistribution();
        assertEquals(5d, distribution.getNumeratorDegreesOfFreedom(), Double.MIN_VALUE);
        distribution.setNumeratorDegreesOfFreedom(4d);
        assertEquals(4d, distribution.getNumeratorDegreesOfFreedom(), Double.MIN_VALUE);
        assertEquals(6d, distribution.getDenominatorDegreesOfFreedom(), Double.MIN_VALUE);
        distribution.setDenominatorDegreesOfFreedom(4d);
        assertEquals(4d, distribution.getDenominatorDegreesOfFreedom(), Double.MIN_VALUE);
        try {
            distribution.setNumeratorDegreesOfFreedom(0d);
            fail("Expecting IllegalArgumentException for df = 0");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            distribution.setDenominatorDegreesOfFreedom(0d);
            fail("Expecting IllegalArgumentException for df = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.distribution.FDistributionTest::testLargeDegreesOfFreedom
    public void testLargeDegreesOfFreedom() throws Exception {
        org.apache.commons.math.distribution.FDistributionImpl fd =
            new org.apache.commons.math.distribution.FDistributionImpl(
                100000., 100000.);
        double p = fd.cumulativeProbability(.999);
        double x = fd.inverseCumulativeProbability(p);
        assertEquals(.999, x, 1.0e-5);
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testParameterAccessors
    public void testParameterAccessors() {
        GammaDistribution distribution = (GammaDistribution) getDistribution();
        assertEquals(4d, distribution.getAlpha(), 0);
        distribution.setAlpha(3d);
        assertEquals(3d, distribution.getAlpha(), 0);
        assertEquals(2d, distribution.getBeta(), 0);
        distribution.setBeta(4d);
        assertEquals(4d, distribution.getBeta(), 0);
        try {
            distribution.setAlpha(0d);
            fail("Expecting IllegalArgumentException for alpha = 0");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            distribution.setBeta(0d);
            fail("Expecting IllegalArgumentException for beta = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testProbabilities
    public void testProbabilities() throws Exception {
        testProbability(-1.000, 4.0, 2.0, .0000);
        testProbability(15.501, 4.0, 2.0, .9499);
        testProbability(0.504, 4.0, 1.0, .0018);
        testProbability(10.011, 1.0, 2.0, .9933);
        testProbability(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testValues
    public void testValues() throws Exception {
        testValue(15.501, 4.0, 2.0, .9499);
        testValue(0.504, 4.0, 1.0, .0018);
        testValue(10.011, 1.0, 2.0, .9933);
        testValue(5.000, 2.0, 2.0, .7127);
    }

// org.apache.commons.math.distribution.GammaDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testQuantiles
    public void testQuantiles() throws Exception {
        verifyQuantiles();
        setDistribution(new NormalDistributionImpl(0, 1));
        verifyQuantiles();
        setDistribution(new NormalDistributionImpl(0, 0.1));
        verifyQuantiles();
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testGetMean
    public void testGetMean() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        assertEquals(2.1, distribution.getMean(), 0);
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testSetMean
    public void testSetMean() throws Exception {
        double mu = Math.random();
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        distribution.setMean(mu);
        verifyQuantiles();
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testGetStandardDeviation
    public void testGetStandardDeviation() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        assertEquals(1.4, distribution.getStandardDeviation(), 0);  
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testSetStandardDeviation
    public void testSetStandardDeviation() throws Exception {
        double sigma = 0.1d + Math.random();
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        distribution.setStandardDeviation(sigma);
        assertEquals(sigma, distribution.getStandardDeviation(), 0);
        verifyQuantiles();
        try {
            distribution.setStandardDeviation(0);
            fail("Expecting IllegalArgumentException for sd = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testExtremeValues
    public void testExtremeValues() throws Exception {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        distribution.setMean(0);
        distribution.setStandardDeviation(1);
        for (int i = 0; i < 100; i+=5) { 
            double lowerTail = distribution.cumulativeProbability((double)-i);
            double upperTail = distribution.cumulativeProbability((double) i);
            if (i < 10) { 
                assertTrue(lowerTail > 0.0d);
                assertTrue(upperTail < 1.0d);
            }
            else { 
                assertTrue(lowerTail < 0.00001);
                assertTrue(upperTail > 0.99999);
            }
        } 
   }

// org.apache.commons.math.distribution.TDistributionTest::testCumulativeProbabilityAgaintStackOverflow
    public void testCumulativeProbabilityAgaintStackOverflow() throws Exception {
        TDistributionImpl td = new TDistributionImpl(5.);
        td.cumulativeProbability(.1);
        td.cumulativeProbability(.01);
    }

// org.apache.commons.math.distribution.TDistributionTest::testSmallDf
    public void testSmallDf() throws Exception {
        setDistribution(new TDistributionImpl(1d));
        setTolerance(1E-4);
        
        setCumulativeTestPoints(new double[] {-318.3088, -31.82052, -12.70620, -6.313752,
            -3.077684, 0.0, 318.3088, 31.82052, 12.70620,
            6.313752, 3.077684});
        setInverseCumulativeTestValues(getCumulativeTestPoints());
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.TDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() throws Exception {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math.distribution.TDistributionTest::testDfAccessors
    public void testDfAccessors() {
        TDistribution distribution = (TDistribution) getDistribution();
        assertEquals(5d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
        distribution.setDegreesOfFreedom(4d);
        assertEquals(4d, distribution.getDegreesOfFreedom(), Double.MIN_VALUE);
        try {
            distribution.setDegreesOfFreedom(0d);
            fail("Expecting IllegalArgumentException for df = 0");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testSanityChecks
  public void testSanityChecks() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new ClassicalRungeKuttaIntegrator(0.01).integrate(pb,
                                                        0.0, new double[pb.getDimension()+10],
                                                        1.0, new double[pb.getDimension()]);
        fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
    try  {
        TestProblem1 pb = new TestProblem1();
        new ClassicalRungeKuttaIntegrator(0.01).integrate(pb,
                                                          0.0, new double[pb.getDimension()],
                                                          1.0, new double[pb.getDimension()+10]);
          fail("an exception should have been thrown");
      } catch(DerivativeException de) {
        fail("wrong exception caught");
      } catch(IntegratorException ie) {
      }
    try  {
      TestProblem1 pb = new TestProblem1();
      new ClassicalRungeKuttaIntegrator(0.01).integrate(pb,
                                                        0.0, new double[pb.getDimension()],
                                                        0.0, new double[pb.getDimension()]);
        fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws DerivativeException, IntegratorException  {
      
    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousError = Double.NaN;
      for (int i = 4; i < 10; ++i) {

        TestProblemAbstract pb = (TestProblemAbstract) problems[k].clone();
        double step = (pb.getFinalTime() - pb.getInitialTime())
          * Math.pow(2.0, -i);

        FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.setStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                     Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        assertEquals(functions.length, integ.getEventsHandlers().size());
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        double error = handler.getMaximalValueError();
        if (i > 4) {
          assertTrue(error < Math.abs(previousError));
        }
        previousError = error;
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
        integ.clearEventsHandlers();
        assertEquals(0, integ.getEventsHandlers().size());
      }

    }

  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testSmallStep
  public void testSmallStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() < 2.0e-13);
    assertTrue(handler.getMaximalValueError() < 4.0e-12);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals("classical Runge-Kutta", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testBigStep
  public void testBigStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.0004);
    assertTrue(handler.getMaximalValueError() > 0.005);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testKepler
  public void testKepler()
    throws DerivativeException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;

    FirstOrderIntegrator integ = new ClassicalRungeKuttaIntegrator(step);
    integ.setStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      DormandPrince54Integrator integrator = new DormandPrince54Integrator(0.0, 1.0,
                                                                           1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
      fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testMinStep
  public void testMinStep()
    throws DerivativeException, IntegratorException {

    try {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
      double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

      FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                 vecAbsoluteTolerance,
                                                                 vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testSmallLastStep
  public void testSmallLastStep()
    throws DerivativeException, IntegratorException {

    TestProblemAbstract pb = new TestProblem5();
    double minStep = 1.25;
    double maxStep = Math.abs(pb.getFinalTime() - pb.getInitialTime());
    double scalAbsoluteTolerance = 6.0e-4;
    double scalRelativeTolerance = 6.0e-4;

    AdaptiveStepsizeIntegrator integ =
      new DormandPrince54Integrator(minStep, maxStep,
                                    scalAbsoluteTolerance,
                                    scalRelativeTolerance);

    DP54SmallLastHandler handler = new DP54SmallLastHandler(minStep);
    integ.setStepHandler(handler);
    integ.setInitialStepSize(1.7);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    assertTrue(handler.wasLastSeen());
    assertEquals("Dormand-Prince 5(4)", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    throws DerivativeException, IntegratorException {

    int previousCalls = Integer.MAX_VALUE;
    for (int i = -12; i < -2; ++i) {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = Math.pow(10.0, i);
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      EmbeddedRungeKuttaIntegrator integ =
          new DormandPrince54Integrator(minStep, maxStep,
                                        scalAbsoluteTolerance, scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setSafety(0.8);
      integ.setMaxGrowth(5.0);
      integ.setMinReduction(0.3);
      integ.setStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      assertEquals(0.8, integ.getSafety(), 1.0e-12);
      assertEquals(5.0, integ.getMaxGrowth(), 1.0e-12);
      assertEquals(0.3, integ.getMinReduction(), 1.0e-12);

      
      
      
      assertTrue(handler.getMaximalValueError() < (0.7 * scalAbsoluteTolerance));
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testEvents
  public void testEvents()
    throws DerivativeException, IntegratorException {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);
    }
    assertEquals(functions.length, integ.getEventsHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getMaximalValueError() < 5.0e-6);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals(12.0, handler.getLastTime(), 1.0e-8 * maxStep);
    integ.clearEventsHandlers();
    assertEquals(0, integ.getEventsHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testKepler
  public void testKepler()
    throws DerivativeException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    integ.setStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(pb.getCalls() < 2800);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testVariableSteps
  public void testVariableSteps()
    throws DerivativeException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    integ.setStepHandler(new VariableHandler());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0, 1.0,
                                                                             1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
      fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testNullIntervalCheck
  public void testNullIntervalCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0, 1.0,
                                                                             1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()],
                           0.0, new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testMinStep
  public void testMinStep()
    throws DerivativeException, IntegratorException {

    try {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
      double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

      FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                  vecAbsoluteTolerance,
                                                                  vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    throws DerivativeException, IntegratorException {

    int previousCalls = Integer.MAX_VALUE;
    for (int i = -12; i < -2; ++i) {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = Math.pow(10.0, i);
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                  scalAbsoluteTolerance,
                                                                  scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      
      
      
      assertTrue(handler.getMaximalValueError() < (1.3 * scalAbsoluteTolerance));
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testEvents
  public void testEvents()
    throws DerivativeException, IntegratorException {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-9;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                scalAbsoluteTolerance,
                                                                scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);
    }
    assertEquals(functions.length, integ.getEventsHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getMaximalValueError() < 5.0e-8);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals(12.0, handler.getLastTime(), 1.0e-8 * maxStep);
    integ.clearEventsHandlers();
    assertEquals(0, integ.getEventsHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testKepler
  public void testKepler()
    throws DerivativeException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                scalAbsoluteTolerance,
                                                                scalRelativeTolerance);
    integ.setStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(pb.getCalls() < 2900);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testVariableSteps
  public void testVariableSteps()
    throws DerivativeException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    integ.setStepHandler(new VariableHandler());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    assertEquals("Dormand-Prince 8 (5, 3)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testNoDenseOutput
  public void testNoDenseOutput()
    throws DerivativeException, IntegratorException {
    TestProblem1 pb1 = new TestProblem1();
    TestProblem1 pb2 = (TestProblem1) pb1.clone();
    double minStep = 0.1 * (pb1.getFinalTime() - pb1.getInitialTime());
    double maxStep = pb1.getFinalTime() - pb1.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-4;
    double scalRelativeTolerance = 1.0e-4;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                scalAbsoluteTolerance,
                                                                scalRelativeTolerance);
    integ.setStepHandler(DummyStepHandler.getInstance());
    integ.integrate(pb1,
                    pb1.getInitialTime(), pb1.getInitialState(),
                    pb1.getFinalTime(), new double[pb1.getDimension()]);
    int callsWithoutDenseOutput = pb1.getCalls();

    integ.setStepHandler(new InterpolatingStepHandler());
    integ.integrate(pb2,
                    pb2.getInitialTime(), pb2.getInitialState(),
                    pb2.getFinalTime(), new double[pb2.getDimension()]);
    int callsWithDenseOutput = pb2.getCalls();

    assertTrue(callsWithDenseOutput > callsWithoutDenseOutput);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testUnstableDerivative
  public void testUnstableDerivative()
  throws DerivativeException, IntegratorException {
    final StepProblem stepProblem = new StepProblem(0.0, 1.0, 2.0);
    FirstOrderIntegrator integ =
      new DormandPrince853Integrator(0.1, 10, 1.0e-12, 0.0);
    integ.addEventHandler(stepProblem, 1.0, 1.0e-12, 1000);
    double[] y = { Double.NaN };
    integ.integrate(stepProblem, 0.0, new double[] { 0.0 }, 10.0, y);
    assertEquals(8.0, y[0], 1.0e-12);
  }

// org.apache.commons.math.ode.nonstiff.EulerIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new EulerIntegrator(0.01).integrate(pb,
                                          0.0, new double[pb.getDimension()+10],
                                          1.0, new double[pb.getDimension()+10]);
        fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.EulerIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws DerivativeException, IntegratorException {

    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousError = Double.NaN;
      for (int i = 4; i < 10; ++i) {

        TestProblemAbstract pb  = (TestProblemAbstract) problems[k].clone();
        double step = (pb.getFinalTime() - pb.getInitialTime())
          * Math.pow(2.0, -i);

        FirstOrderIntegrator integ = new EulerIntegrator(step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.setStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                     Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        double error = handler.getMaximalValueError();
        if (i > 4) {
          assertTrue(error < Math.abs(previousError));
        }
        previousError = error;
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      }

    }

  }

// org.apache.commons.math.ode.nonstiff.EulerIntegratorTest::testSmallStep
  public void testSmallStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new EulerIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

   assertTrue(handler.getLastError() < 2.0e-4);
   assertTrue(handler.getMaximalValueError() < 1.0e-3);
   assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
   assertEquals("Euler", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.EulerIntegratorTest::testBigStep
  public void testBigStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new EulerIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.01);
    assertTrue(handler.getMaximalValueError() > 0.2);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new GillIntegrator(0.01).integrate(pb,
                                         0.0, new double[pb.getDimension()+10],
                                         1.0, new double[pb.getDimension()+10]);
        fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws DerivativeException, IntegratorException  {
      
    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousError = Double.NaN;
      for (int i = 5; i < 10; ++i) {

        TestProblemAbstract pb = (TestProblemAbstract) problems[k].clone();
        double step = (pb.getFinalTime() - pb.getInitialTime())
          * Math.pow(2.0, -i);

        FirstOrderIntegrator integ = new GillIntegrator(step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.setStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                     Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        double error = handler.getMaximalValueError();
        if (i > 5) {
          assertTrue(error < Math.abs(previousError));
        }
        previousError = error;
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      }

    }

  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testSmallStep
  public void testSmallStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new GillIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() < 2.0e-13);
    assertTrue(handler.getMaximalValueError() < 4.0e-12);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals("Gill", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testBigStep
  public void testBigStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new GillIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.0004);
    assertTrue(handler.getMaximalValueError() > 0.005);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testKepler
  public void testKepler()
    throws DerivativeException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;

    FirstOrderIntegrator integ = new GillIntegrator(step);
    integ.setStepHandler(new KeplerStepHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
  }

// org.apache.commons.math.ode.nonstiff.GillIntegratorTest::testUnstableDerivative
  public void testUnstableDerivative()
  throws DerivativeException, IntegratorException {
    final StepProblem stepProblem = new StepProblem(0.0, 1.0, 2.0);
    FirstOrderIntegrator integ = new GillIntegrator(0.3);
    integ.addEventHandler(stepProblem, 1.0, 1.0e-12, 1000);
    double[] y = { Double.NaN };
    integ.integrate(stepProblem, 0.0, new double[] { 0.0 }, 10.0, y);
    assertEquals(8.0, y[0], 1.0e-12);
  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      AdaptiveStepsizeIntegrator integrator =
        new GraggBulirschStoerIntegrator(0.0, 1.0, 1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
      fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testNullIntervalCheck
  public void testNullIntervalCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      GraggBulirschStoerIntegrator integrator =
        new GraggBulirschStoerIntegrator(0.0, 1.0, 1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()],
                           0.0, new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testMinStep
  public void testMinStep()
    throws DerivativeException, IntegratorException {

    try {
      TestProblem5 pb  = new TestProblem5();
      double minStep   = 0.1 * Math.abs(pb.getFinalTime() - pb.getInitialTime());
      double maxStep   = Math.abs(pb.getFinalTime() - pb.getInitialTime());
      double[] vecAbsoluteTolerance = { 1.0e-20, 1.0e-21 };
      double[] vecRelativeTolerance = { 1.0e-20, 1.0e-21 };

      FirstOrderIntegrator integ =
        new GraggBulirschStoerIntegrator(minStep, maxStep,
                                         vecAbsoluteTolerance, vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }

  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    throws DerivativeException, IntegratorException {

    int previousCalls = Integer.MAX_VALUE;
    for (int i = -12; i < -4; ++i) {
      TestProblem1 pb     = new TestProblem1();
      double minStep      = 0;
      double maxStep      = pb.getFinalTime() - pb.getInitialTime();
      double absTolerance = Math.pow(10.0, i);
      double relTolerance = absTolerance;

      FirstOrderIntegrator integ =
        new GraggBulirschStoerIntegrator(minStep, maxStep,
                                         absTolerance, relTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      
      
      
      double ratio =  handler.getMaximalValueError() / absTolerance;
      assertTrue(ratio < 2.4);
      assertTrue(ratio > 0.02);
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testIntegratorControls
  public void testIntegratorControls() {}

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testEvents
  public void testEvents()
    throws DerivativeException, IntegratorException {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-10;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new GraggBulirschStoerIntegrator(minStep, maxStep,
                                                                  scalAbsoluteTolerance,
                                                                  scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);
    }
    assertEquals(functions.length, integ.getEventsHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getMaximalValueError() < 5.0e-8);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals(12.0, handler.getLastTime(), 1.0e-8 * maxStep);
    integ.clearEventsHandlers();
    assertEquals(0, integ.getEventsHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testKepler
  public void testKepler()
    throws DerivativeException, IntegratorException {

    final TestProblem3 pb = new TestProblem3(0.9);
    double minStep        = 0;
    double maxStep        = pb.getFinalTime() - pb.getInitialTime();
    double absTolerance   = 1.0e-6;
    double relTolerance   = 1.0e-6;

    FirstOrderIntegrator integ =
      new GraggBulirschStoerIntegrator(minStep, maxStep,
                                       absTolerance, relTolerance);
    integ.setStepHandler(new KeplerStepHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(pb.getCalls() < 2150);

  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testVariableSteps
  public void testVariableSteps()
    throws DerivativeException, IntegratorException {

    final TestProblem3 pb = new TestProblem3(0.9);
    double minStep        = 0;
    double maxStep        = pb.getFinalTime() - pb.getInitialTime();
    double absTolerance   = 1.0e-8;
    double relTolerance   = 1.0e-8;
    FirstOrderIntegrator integ =
      new GraggBulirschStoerIntegrator(minStep, maxStep,
                                       absTolerance, relTolerance);
    integ.setStepHandler(new VariableStepHandler());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    assertEquals("Gragg-Bulirsch-Stoer", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegratorTest::testUnstableDerivative
  public void testUnstableDerivative()
    throws DerivativeException, IntegratorException {
    final StepProblem stepProblem = new StepProblem(0.0, 1.0, 2.0);
    FirstOrderIntegrator integ =
      new GraggBulirschStoerIntegrator(0.1, 10, 1.0e-12, 0.0);
    integ.addEventHandler(stepProblem, 1.0, 1.0e-12, 1000);
    double[] y = { Double.NaN };
    integ.integrate(stepProblem, 0.0, new double[] { 0.0 }, 10.0, y);
    assertEquals(8.0, y[0], 1.0e-12);
  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testWrongDerivative
  public void testWrongDerivative() {
    try {
      HighamHall54Integrator integrator =
          new HighamHall54Integrator(0.0, 1.0, 1.0e-10, 1.0e-10);
      FirstOrderDifferentialEquations equations =
          new FirstOrderDifferentialEquations() {
            private static final long serialVersionUID = -1157081786301178032L;
            public void computeDerivatives(double t, double[] y, double[] dot)
            throws DerivativeException {
            if (t < -0.5) {
                throw new DerivativeException("{0}", new String[] { "oops" });
            } else {
                throw new DerivativeException(new RuntimeException("oops"));
           }
          }
          public int getDimension() {
              return 1;
          }
      };

      try  {
        integrator.integrate(equations, -1.0, new double[1], 0.0, new double[1]);
        fail("an exception should have been thrown");
      } catch(DerivativeException de) {
        
      }

      try  {
        integrator.integrate(equations, 0.0, new double[1], 1.0, new double[1]);
        fail("an exception should have been thrown");
      } catch(DerivativeException de) {
        
      }

    } catch (Exception e) {
      fail("wrong exception caught: " + e.getMessage());        
    }
  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testMinStep
  public void testMinStep()
    throws DerivativeException, IntegratorException {

    try {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
      double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

      FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              vecAbsoluteTolerance,
                                                              vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    throws DerivativeException, IntegratorException {

    int previousCalls = Integer.MAX_VALUE;
    for (int i = -12; i < -2; ++i) {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = Math.pow(10.0, i);
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      
      
      
      assertTrue(handler.getMaximalValueError() < (1.3 * scalAbsoluteTolerance));
      assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testEvents
  public void testEvents()
    throws DerivativeException, IntegratorException {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                            scalAbsoluteTolerance,
                                                            scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);
    }
    assertEquals(functions.length, integ.getEventsHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getMaximalValueError() < 1.0e-7);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals(12.0, handler.getLastTime(), 1.0e-8 * maxStep);
    integ.clearEventsHandlers();
    assertEquals(0, integ.getEventsHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testEventsErrors
  public void testEventsErrors()
    throws DerivativeException, IntegratorException {

      final TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ =
          new HighamHall54Integrator(minStep, maxStep,
                                     scalAbsoluteTolerance, scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setStepHandler(handler);

      integ.addEventHandler(new EventHandler() {
        public int eventOccurred(double t, double[] y) {
          return EventHandler.CONTINUE;
        }
        public double g(double t, double[] y) throws EventException {
          double middle = (pb.getInitialTime() + pb.getFinalTime()) / 2;
          double offset = t - middle;
          if (offset > 0) {
            throw new EventException("Evaluation failed for argument = {0}",
                                      new Object[] { Double.valueOf(t) });
          }
          return offset;
        }
        public void resetState(double t, double[] y) {
        }
        private static final long serialVersionUID = 935652725339916361L;
      }, Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);

      try {
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      } catch (Exception e) {
        fail("wrong exception type caught");
      }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testEventsNoConvergence
  public void testEventsNoConvergence()
  throws DerivativeException, IntegratorException {

    final TestProblem1 pb = new TestProblem1();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ =
        new HighamHall54Integrator(minStep, maxStep,
                                   scalAbsoluteTolerance, scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);

    integ.addEventHandler(new EventHandler() {
      public int eventOccurred(double t, double[] y) {
        return EventHandler.CONTINUE;
      }
      public double g(double t, double[] y) {
        double middle = (pb.getInitialTime() + pb.getFinalTime()) / 2;
        double offset = t - middle;
        return (offset > 0) ? (offset + 0.5) : (offset - 0.5);
      }
      public void resetState(double t, double[] y) {
      }
      private static final long serialVersionUID = 935652725339916361L;
    }, Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 3);

    try {
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      fail("an exception should have been thrown");
    } catch (IntegratorException ie) {
       assertTrue(ie.getCause() != null);
       assertTrue(ie.getCause() instanceof ConvergenceException);
    } catch (Exception e) {
      fail("wrong exception type caught");
    }

}

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testSanityChecks
  public void testSanityChecks() {
    try {
      final TestProblem3 pb  = new TestProblem3(0.9);
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), new double[6],
                        pb.getFinalTime(), new double[pb.getDimension()]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[6]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[2], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[2]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getInitialTime(), new double[pb.getDimension()]);
        fail("an exception should have been thrown");
      } catch (IntegratorException ie) {
        
      }

    } catch (Exception e) {
      fail("wrong exception caught: " + e.getMessage());
    }
  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testKepler
  public void testKepler()
    throws DerivativeException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double[] vecAbsoluteTolerance = { 1.0e-8, 1.0e-8, 1.0e-10, 1.0e-10 };
    double[] vecRelativeTolerance = { 1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8 };

    FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                            vecAbsoluteTolerance,
                                                            vecRelativeTolerance);
    integ.setStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    assertEquals("Higham-Hall 5(4)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.MidpointIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new MidpointIntegrator(0.01).integrate(pb,
                                             0.0, new double[pb.getDimension()+10],
                                             1.0, new double[pb.getDimension()+10]);
        fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.MidpointIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws DerivativeException, IntegratorException  {
      
    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {

      double previousError = Double.NaN;
      for (int i = 4; i < 10; ++i) {

        TestProblemAbstract pb = (TestProblemAbstract) problems[k].clone();
        double step = (pb.getFinalTime() - pb.getInitialTime())
          * Math.pow(2.0, -i);
        FirstOrderIntegrator integ = new MidpointIntegrator(step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.setStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                     Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        double error = handler.getMaximalValueError();
        if (i > 4) {
          assertTrue(error < Math.abs(previousError));
        }
        previousError = error;
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      }

    }

  }

// org.apache.commons.math.ode.nonstiff.MidpointIntegratorTest::testSmallStep
  public void testSmallStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new MidpointIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() < 2.0e-7);
    assertTrue(handler.getMaximalValueError() < 1.0e-6);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals("midpoint", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.MidpointIntegratorTest::testBigStep
  public void testBigStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb  = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new MidpointIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.01);
    assertTrue(handler.getMaximalValueError() > 0.05);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
    try  {
      TestProblem1 pb = new TestProblem1();
      new ThreeEighthesIntegrator(0.01).integrate(pb,
                                                  0.0, new double[pb.getDimension()+10],
                                                  1.0, new double[pb.getDimension()+10]);
        fail("an exception should have been thrown");
    } catch(DerivativeException de) {
      fail("wrong exception caught");
    } catch(IntegratorException ie) {
    }
  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testDecreasingSteps
  public void testDecreasingSteps()
    throws DerivativeException, IntegratorException  {
      
    TestProblemAbstract[] problems = TestProblemFactory.getProblems();
    for (int k = 0; k < problems.length; ++k) {
    
      double previousError = Double.NaN;
      for (int i = 4; i < 10; ++i) {

        TestProblemAbstract pb = (TestProblemAbstract) problems[k].clone();
        double step = (pb.getFinalTime() - pb.getInitialTime())
          * Math.pow(2.0, -i);

        FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.setStepHandler(handler);
        EventHandler[] functions = pb.getEventsHandlers();
        for (int l = 0; l < functions.length; ++l) {
          integ.addEventHandler(functions[l],
                                     Double.POSITIVE_INFINITY, 1.0e-6 * step, 1000);
        }
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        double error = handler.getMaximalValueError();
        if (i > 4) {
          assertTrue(error < Math.abs(previousError));
        }
        previousError = error;
        assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      }

    }

  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testSmallStep
 public void testSmallStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;

    FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() < 2.0e-13);
    assertTrue(handler.getMaximalValueError() < 4.0e-12);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
    assertEquals("3/8", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testBigStep
  public void testBigStep()
    throws DerivativeException, IntegratorException {

    TestProblem1 pb = new TestProblem1();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.2;

    FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.setStepHandler(handler);
    integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    assertTrue(handler.getLastError() > 0.0004);
    assertTrue(handler.getMaximalValueError() > 0.005);
    assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

  }

// org.apache.commons.math.ode.nonstiff.ThreeEighthesIntegratorTest::testKepler
  public void testKepler()
    throws DerivativeException, IntegratorException {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.0003;

    FirstOrderIntegrator integ = new ThreeEighthesIntegrator(step);
    integ.setStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
  }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testNorris
    public void testNorris() {
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < data.length; i++) {
            regression.addData(data[i][1], data[i][0]);
        }
        
        
        assertEquals("slope", 1.00211681802045, regression.getSlope(), 10E-12);
        assertEquals("slope std err", 0.429796848199937E-03,
                regression.getSlopeStdErr(),10E-12);
        assertEquals("number of observations", 36, regression.getN());
        assertEquals( "intercept", -0.262323073774029,
            regression.getIntercept(),10E-12);
        assertEquals("std err intercept", 0.232818234301152,
            regression.getInterceptStdErr(),10E-12);
        assertEquals("r-square", 0.999993745883712,
            regression.getRSquare(), 10E-12);
        assertEquals("SSR", 4255954.13232369,
            regression.getRegressionSumSquares(), 10E-9);
        assertEquals("MSE", 0.782864662630069,
            regression.getMeanSquareError(), 10E-10);
        assertEquals("SSE", 26.6173985294224,
            regression.getSumSquaredErrors(),10E-9);
        
          
        assertEquals( "predict(0)",  -0.262323073774029,
            regression.predict(0), 10E-12);
        assertEquals("predict(1)", 1.00211681802045 - 0.262323073774029,
            regression.predict(1), 10E-12);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testCorr
    public void testCorr() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(corrData);
        assertEquals("number of observations", 17, regression.getN());
        assertEquals("r-square", .896123, regression.getRSquare(), 10E-6);
        assertEquals("r", -0.94663767742, regression.getR(), 1E-10);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testNaNs
    public void testNaNs() {
        SimpleRegression regression = new SimpleRegression();
        assertTrue("intercept not NaN", Double.isNaN(regression.getIntercept()));
        assertTrue("slope not NaN", Double.isNaN(regression.getSlope()));
        assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        assertTrue("e not NaN", Double.isNaN(regression.getR()));
        assertTrue("r-square not NaN", Double.isNaN(regression.getRSquare()));
        assertTrue( "RSS not NaN", Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue("SSE not NaN",Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue("SSTO not NaN", Double.isNaN(regression.getTotalSumSquares()));
        assertTrue("predict not NaN", Double.isNaN(regression.predict(0)));

        regression.addData(1, 2);
        regression.addData(1, 3);

        
        assertTrue("intercept not NaN", Double.isNaN(regression.getIntercept()));
        assertTrue("slope not NaN", Double.isNaN(regression.getSlope()));
        assertTrue("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        assertTrue("e not NaN", Double.isNaN(regression.getR()));
        assertTrue("r-square not NaN", Double.isNaN(regression.getRSquare()));
        assertTrue("RSS not NaN", Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue("SSE not NaN", Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue("predict not NaN", Double.isNaN(regression.predict(0)));

        
        assertTrue("SSTO NaN", !Double.isNaN(regression.getTotalSumSquares()));

        regression = new SimpleRegression();

        regression.addData(1, 2);
        regression.addData(3, 3);

        
        assertTrue("interceptNaN", !Double.isNaN(regression.getIntercept()));
        assertTrue("slope NaN", !Double.isNaN(regression.getSlope()));
        assertTrue ("slope std err not NaN", Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err not NaN", Double.isNaN(regression.getInterceptStdErr()));
        assertTrue("MSE not NaN", Double.isNaN(regression.getMeanSquareError()));
        assertTrue("r NaN", !Double.isNaN(regression.getR()));
        assertTrue("r-square NaN", !Double.isNaN(regression.getRSquare()));
        assertTrue("RSS NaN", !Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue("SSE NaN", !Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue("SSTO NaN", !Double.isNaN(regression.getTotalSumSquares()));
        assertTrue("predict NaN", !Double.isNaN(regression.predict(0)));

        regression.addData(1, 4);

        
        assertTrue("MSE NaN", !Double.isNaN(regression.getMeanSquareError()));
        assertTrue("slope std err NaN", !Double.isNaN(regression.getSlopeStdErr()));
        assertTrue("intercept std err NaN", !Double.isNaN(regression.getInterceptStdErr()));
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testClear
    public void testClear() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(corrData);
        assertEquals("number of observations", 17, regression.getN());
        regression.clear();
        assertEquals("number of observations", 0, regression.getN());
        regression.addData(corrData);
        assertEquals("r-square", .896123, regression.getRSquare(), 10E-6);
        regression.addData(data);
        assertEquals("number of observations", 53, regression.getN());
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testInference
    public void testInference() throws Exception {
        
        
        SimpleRegression regression = new SimpleRegression();
        regression.addData(infData);
        assertEquals("slope std err", 0.011448491,
                regression.getSlopeStdErr(), 1E-10);
        assertEquals("std err intercept", 0.286036932,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 4.596e-07,
                regression.getSignificance(),1E-8);    
        assertEquals("slope conf interval half-width", 0.0270713794287, 
                regression.getSlopeConfidenceInterval(),1E-8);
        
        regression = new SimpleRegression();
        regression.addData(infData2);
        assertEquals("slope std err", 1.07260253,
                regression.getSlopeStdErr(), 1E-8);
        assertEquals("std err intercept",4.17718672,
                regression.getInterceptStdErr(),1E-8);
        assertEquals("significance", 0.261829133982,
                regression.getSignificance(),1E-11);    
        assertEquals("slope conf interval half-width", 2.97802204827, 
                regression.getSlopeConfidenceInterval(),1E-8);
        
        
        
        assertTrue("tighter means wider",
                regression.getSlopeConfidenceInterval() < regression.getSlopeConfidenceInterval(0.01));
     
        try {
            regression.getSlopeConfidenceInterval(1);
            fail("expecting IllegalArgumentException for alpha = 1");
        } catch (IllegalArgumentException ex) {
            ;
        }  

    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testPerfect
    public void testPerfect() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(((double) i) / (n - 1), i);
        }
        assertEquals(0.0, regression.getSignificance(), 1.0e-5);
        assertTrue(regression.getSlope() > 0.0);
        assertTrue(regression.getSumSquaredErrors() >= 0.0);
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testPerfectNegative
    public void testPerfectNegative() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(- ((double) i) / (n - 1), i);
        }
   
        assertEquals(0.0, regression.getSignificance(), 1.0e-5);
        assertTrue(regression.getSlope() < 0.0);   
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testRandom
    public void testRandom() throws Exception {
        SimpleRegression regression = new SimpleRegression();
        Random random = new Random(1);
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addData(((double) i) / (n - 1), random.nextDouble());
        }

        assertTrue( 0.0 < regression.getSignificance()
                    && regression.getSignificance() < 1.0);       
    }

// org.apache.commons.math.stat.regression.SimpleRegressionTest::testSSENonNegative
    public void testSSENonNegative() {
        double[] y = { 8915.102, 8919.302, 8923.502 };
        double[] x = { 1.107178495E2, 1.107264895E2, 1.107351295E2 };
        SimpleRegression reg = new SimpleRegression();
        for (int i = 0; i < x.length; i++) {
            reg.addData(x[i], y[i]);
        }
        assertTrue(reg.getSumSquaredErrors() >= 0.0);
    }
