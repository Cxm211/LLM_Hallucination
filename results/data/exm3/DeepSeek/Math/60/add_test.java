// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testCumulativeProbabilityZeroStandardDeviation() throws Exception {
    NormalDistribution distribution = new NormalDistributionImpl(5.0, 0.0);
    assertEquals(0.0, distribution.cumulativeProbability(4.0), 0.0);
    assertEquals(1.0, distribution.cumulativeProbability(5.0), 0.0);
    assertEquals(1.0, distribution.cumulativeProbability(6.0), 0.0);
}
