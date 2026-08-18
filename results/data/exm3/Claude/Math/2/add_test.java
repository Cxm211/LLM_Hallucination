// org/apache/commons/math3/distribution/HypergeometricDistributionTest.java
@Test
    public void testGetNumericalMeanOverflow() {
        final int N = 100000;
        final int m = 60000;
        final int n = 50000;
        final HypergeometricDistribution dist = new HypergeometricDistribution(N, m, n);
        final double expectedMean = 30000.0;
        final double actualMean = dist.getNumericalMean();
        Assert.assertEquals(expectedMean, actualMean, 0.0001);
    }