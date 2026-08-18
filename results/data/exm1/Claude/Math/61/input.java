// buggy code
    public PoissonDistributionImpl(double p, double epsilon, int maxIterations) {
        if (p <= 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_POISSON_MEAN, p);
        }
        mean = p;
        normal = new NormalDistributionImpl(p, FastMath.sqrt(p));
        this.epsilon = epsilon;
        this.maxIterations = maxIterations;
    }

// relevant test
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
