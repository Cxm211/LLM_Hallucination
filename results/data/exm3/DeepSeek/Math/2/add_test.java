// org/apache/commons/math3/distribution/HypergeometricDistributionTest.java
@Test
    public void testGetNumericalMeanOverflow() {
        // Case 1: product > Integer.MAX_VALUE, overflow to negative
        final int N1 = 43130568;
        final int K1 = 42976365;
        final int n1 = 50;
        final HypergeometricDistribution dist1 = new HypergeometricDistribution(N1, K1, n1);
        final double expectedMean1 = ((double) n1 * (double) K1) / (double) N1;
        Assert.assertEquals(expectedMean1, dist1.getNumericalMean(), 1e-10);
        
        // Case 2: product > 2^32, overflow to positive
        final int N2 = 200000;
        final int K2 = 100000;
        final int n2 = 100000;
        final HypergeometricDistribution dist2 = new HypergeometricDistribution(N2, K2, n2);
        final double expectedMean2 = ((double) n2 * (double) K2) / (double) N2;
        Assert.assertEquals(expectedMean2, dist2.getNumericalMean(), 1e-10);
    }
