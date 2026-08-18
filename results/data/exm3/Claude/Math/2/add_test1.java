// org/apache/commons/math3/distribution/HypergeometricDistributionTest.java
@Test
    public void testGetNumericalMeanLargeParameters() {
        final int N = Integer.MAX_VALUE - 1;
        final int m = Integer.MAX_VALUE / 2;
        final int n = 1000;
        final HypergeometricDistribution dist = new HypergeometricDistribution(N, m, n);
        final double expectedMean = (double) n * m / N;
        final double actualMean = dist.getNumericalMean();
        Assert.assertEquals(expectedMean, actualMean, 0.0001);
    }