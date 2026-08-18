// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testExtremeInfinity() throws Exception {
    NormalDistribution distribution = (NormalDistribution) getDistribution();
    distribution.setMean(0);
    distribution.setStandardDeviation(1);
    assertEquals(0.0, distribution.cumulativeProbability(Double.NEGATIVE_INFINITY), 0.0);
    assertEquals(1.0, distribution.cumulativeProbability(Double.POSITIVE_INFINITY), 0.0);
    distribution.setMean(10.0);
    distribution.setStandardDeviation(2.0);
    assertEquals(0.0, distribution.cumulativeProbability(Double.NEGATIVE_INFINITY), 0.0);
    assertEquals(1.0, distribution.cumulativeProbability(Double.POSITIVE_INFINITY), 0.0);
}
