// org/apache/commons/math3/distribution/BinomialDistributionTest.java
@Test
    public void testMath718EdgeCase() {
        // Test with a very large trial count at boundary
        BinomialDistribution dist = new BinomialDistribution(25000000, 0.5);
        int p = dist.inverseCumulativeProbability(0.5);
        Assert.assertEquals(12500000, p);
    }