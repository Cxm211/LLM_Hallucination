// buggy function
    public PoissonDistributionImpl(double p, double epsilon, int maxIterations) {
        if (p <= 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_POISSON_MEAN, p);
        }
        mean = p;
        normal = new NormalDistributionImpl(p, FastMath.sqrt(p));
        this.epsilon = epsilon;
        this.maxIterations = maxIterations;
    }

// trigger testcase
// org/apache/commons/math/distribution/PoissonDistributionTest.java::testMean
public void testMean() {
        PoissonDistribution dist;
        try {
            dist = new PoissonDistributionImpl(-1);
            fail("negative mean: NotStrictlyPositiveException expected");
        } catch(NotStrictlyPositiveException ex) {
            // Expected.
        }

        dist = new PoissonDistributionImpl(10.0);
        assertEquals(10.0, dist.getMean(), 0.0);
    }
