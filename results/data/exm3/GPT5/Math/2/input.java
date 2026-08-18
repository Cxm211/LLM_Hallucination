// buggy function
    public double getNumericalMean() {
        return (double) (getSampleSize() * getNumberOfSuccesses()) / (double) getPopulationSize();
    }

// trigger testcase
// org/apache/commons/math3/distribution/HypergeometricDistributionTest.java::testMath1021
@Test
    public void testMath1021() {
        final int N = 43130568;
        final int m = 42976365;
        final int n = 50;
        final HypergeometricDistribution dist = new HypergeometricDistribution(N, m, n);

        for (int i = 0; i < 100; i++) {
            final int sample = dist.sample();
            Assert.assertTrue("sample=" + sample, 0 <= sample);
            Assert.assertTrue("sample=" + sample, sample <= n);
        }
    }
