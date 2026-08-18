// buggy code
    public double cumulativeProbability(double x) throws MathException {
        final double dev = x - mean;
        try {
        return 0.5 * (1.0 + Erf.erf((dev) /
                    (standardDeviation * FastMath.sqrt(2.0))));
        } catch (MaxIterationsExceededException ex) {
            if (x < (mean - 20 * standardDeviation)) { // JDK 1.5 blows at 38
                return 0;
            } else if (x > (mean + 20 * standardDeviation)) {
                return 1;
            } else {
                throw ex;
            }
        }
    }

// relevant test
// org.apache.commons.math.distribution.NormalDistributionTest::testQuantiles
    public void testQuantiles() throws Exception {
        setDensityTestValues(new double[] {0.0385649760808, 0.172836231799, 0.284958771715, 0.172836231799, 0.0385649760808,
                0.00316560600853, 9.55930184035e-05, 1.06194251052e-06});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new NormalDistributionImpl(0, 1));
        setDensityTestValues(new double[] {0.0539909665132, 0.241970724519, 0.398942280401, 0.241970724519, 0.0539909665132,
                0.00443184841194, 0.000133830225765, 1.48671951473e-06});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new NormalDistributionImpl(0, 0.1));
        setDensityTestValues(new double[] {0.539909665132, 2.41970724519, 3.98942280401, 2.41970724519,
                0.539909665132, 0.0443184841194, 0.00133830225765, 1.48671951473e-05});
        verifyQuantiles();
        verifyDensities();
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

// org.apache.commons.math.distribution.NormalDistributionTest::testGetStandardDeviation
    public void testGetStandardDeviation() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        assertEquals(1.4, distribution.getStandardDeviation(), 0);
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testPreconditions
    public void testPreconditions() {
        try {
            new NormalDistributionImpl(1, 0);
            fail("Should have generated NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testDensity
    public void testDensity() {
        double [] x = new double[]{-2, -1, 0, 1, 2};
        
        checkDensity(0, 1, x, new double[]{0.05399096651, 0.24197072452, 0.39894228040, 0.24197072452, 0.05399096651});
        
        checkDensity(1.1, 1, x, new double[]{0.003266819056,0.043983595980,0.217852177033,0.396952547477,0.266085249899});
    }

// org.apache.commons.math.distribution.NormalDistributionTest::testExtremeValues
    public void testExtremeValues() throws Exception {
        NormalDistribution distribution = new NormalDistributionImpl(0, 1);
        for (int i = 0; i < 100; i++) { 
            double lowerTail = distribution.cumulativeProbability(-i);
            double upperTail = distribution.cumulativeProbability(i);
            if (i < 9) { 
                
                
                assertTrue(lowerTail > 0.0d);
                assertTrue(upperTail < 1.0d);
            }
            else { 
                assertTrue(lowerTail < 0.00001);
                assertTrue(upperTail > 0.99999);
            }
        }
        
        assertEquals(distribution.cumulativeProbability(Double.MAX_VALUE), 1, 0);
        assertEquals(distribution.cumulativeProbability(-Double.MAX_VALUE), 0, 0);
        assertEquals(distribution.cumulativeProbability(Double.POSITIVE_INFINITY), 1, 0);
        assertEquals(distribution.cumulativeProbability(Double.NEGATIVE_INFINITY), 0, 0);
        
   }

// org.apache.commons.math.distribution.NormalDistributionTest::testMath280
    public void testMath280() throws MathException {
        NormalDistribution normal = new NormalDistributionImpl(0,1);
        double result = normal.inverseCumulativeProbability(0.9986501019683698);
        assertEquals(3.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.841344746068543);
        assertEquals(1.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.9999683287581673);
        assertEquals(4.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.9772498680518209);
        assertEquals(2.0, result, defaultTolerance);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testNormalApproximateProbability
    public void testNormalApproximateProbability() throws Exception {
        PoissonDistribution dist = new PoissonDistributionImpl(100);
        double result = dist.normalApproximateProbability(110)
                - dist.normalApproximateProbability(89);
        assertEquals(0.706281887248, result, 1E-10);

        dist = new PoissonDistributionImpl(10000);
        result = dist.normalApproximateProbability(10200)
        - dist.normalApproximateProbability(9899);
        assertEquals(0.820070051552, result, 1E-10);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testDegenerateInverseCumulativeProbability
    public void testDegenerateInverseCumulativeProbability() throws Exception {
        PoissonDistribution dist = new PoissonDistributionImpl(DEFAULT_TEST_POISSON_PARAMETER);
        assertEquals(Integer.MAX_VALUE, dist.inverseCumulativeProbability(1.0d));
        assertEquals(-1, dist.inverseCumulativeProbability(0d));
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testMean
    public void testMean() {
        PoissonDistribution dist;
        try {
            dist = new PoissonDistributionImpl(-1);
            fail("negative mean: NotStrictlyPositiveException expected");
        } catch(NotStrictlyPositiveException ex) {
            
        }

        dist = new PoissonDistributionImpl(10.0);
        assertEquals(10.0, dist.getMean(), 0.0);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testLargeMeanCumulativeProbability
    public void testLargeMeanCumulativeProbability() {
        double mean = 1.0;
        while (mean <= 10000000.0) {
            PoissonDistribution dist = new PoissonDistributionImpl(mean);

            double x = mean * 2.0;
            double dx = x / 10.0;
            double p = Double.NaN;
            double sigma = FastMath.sqrt(mean);
            while (x >= 0) {
                try {
                    p = dist.cumulativeProbability(x);
                    assertFalse("NaN cumulative probability returned for mean = " +
                            mean + " x = " + x,Double.isNaN(p));
                    if (x > mean - 2 * sigma) {
                        assertTrue("Zero cum probaility returned for mean = " +
                                mean + " x = " + x, p > 0);
                    }
                } catch (MathException ex) {
                    fail("mean of " + mean + " and x of " + x + " caused " + ex.getMessage());
                }
                x -= dx;
            }

            mean *= 10.0;
        }
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testCumulativeProbabilitySpecial
    public void testCumulativeProbabilitySpecial() throws Exception {
        PoissonDistribution dist;
        dist = new PoissonDistributionImpl(9120);
        checkProbability(dist, 9075);
        checkProbability(dist, 9102);
        dist = new PoissonDistributionImpl(5058);
        checkProbability(dist, 5044);
        dist = new PoissonDistributionImpl(6986);
        checkProbability(dist, 6950);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testLargeMeanInverseCumulativeProbability
    public void testLargeMeanInverseCumulativeProbability() throws Exception {
        double mean = 1.0;
        while (mean <= 100000.0) { 
            PoissonDistribution dist = new PoissonDistributionImpl(mean);
            double p = 0.1;
            double dp = p;
            while (p < .99) {
                double ret = Double.NaN;
                try {
                    ret = dist.inverseCumulativeProbability(p);
                    
                    assertTrue(p >= dist.cumulativeProbability(ret));
                    assertTrue(p < dist.cumulativeProbability(ret + 1));
                } catch (MathException ex) {
                    fail("mean of " + mean + " and p of " + p + " caused " + ex.getMessage());
                }
                p += dp;
            }
            mean *= 10.0;
        }
    }
