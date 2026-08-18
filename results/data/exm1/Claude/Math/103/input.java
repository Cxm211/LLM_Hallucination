// buggy code
    public double cumulativeProbability(double x) throws MathException {
            return 0.5 * (1.0 + Erf.erf((x - mean) /
                    (standardDeviation * Math.sqrt(2.0))));
    }

// relevant test
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

// org.apache.commons.math.distribution.PoissonDistributionTest::testNormalApproximateProbability
    public void testNormalApproximateProbability() throws Exception {
        PoissonDistribution dist = new PoissonDistributionImpl(100);
        double result = dist.normalApproximateProbability(110)
                - dist.normalApproximateProbability(89);
        assertEquals(0.706281887248, result, 1E-10);
        dist.setMean(10000);
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
        PoissonDistribution dist = new PoissonDistributionImpl(DEFAULT_TEST_POISSON_PARAMETER);
        try {
            dist.setMean(-1);
            fail("negative mean.  IllegalArgumentException expected");
        } catch(IllegalArgumentException ex) {
        }
        
        dist.setMean(10.0);
        assertEquals(10.0, dist.getMean(), 0.0);
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testLargeMeanCumulativeProbability
    public void testLargeMeanCumulativeProbability() {
        PoissonDistribution dist = new PoissonDistributionImpl(1.0);
        double mean = 1.0;
        while (mean <= 10000000.0) {
            dist.setMean(mean);
            
            double x = mean * 2.0;
            double dx = x / 10.0;
            while (x >= 0) {
                try {
                    dist.cumulativeProbability(x);
                } catch (MathException ex) {
                    fail("mean of " + mean + " and x of " + x + " caused " + ex.getMessage());
                }
                x -= dx;
            }
            
            mean *= 10.0;
        }
    }

// org.apache.commons.math.distribution.PoissonDistributionTest::testLargeMeanInverseCumulativeProbability
    public void testLargeMeanInverseCumulativeProbability() {
        PoissonDistribution dist = new PoissonDistributionImpl(1.0);
        double mean = 1.0;
        while (mean <= 10000000.0) {
            dist.setMean(mean);
            
            double p = 0.1;
            double dp = p;
            while (p < 1.0) {
                try {
                    dist.inverseCumulativeProbability(p);
                } catch (MathException ex) {
                    fail("mean of " + mean + " and p of " + p + " caused " + ex.getMessage());
                }
                p += dp;
            }
            
            mean *= 10.0;
        }
    }
