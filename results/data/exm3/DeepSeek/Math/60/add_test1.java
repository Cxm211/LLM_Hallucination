// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testCumulativeProbabilityExtremeSmallStandardDeviation() throws Exception {
    double sd = Double.MIN_VALUE;
    NormalDistribution distribution = new NormalDistributionImpl(0.0, sd);
    assertEquals(1.0, distribution.cumulativeProbability(1.0), 0.0);
    assertEquals(0.0, distribution.cumulativeProbability(-1.0), 0.0);
}
