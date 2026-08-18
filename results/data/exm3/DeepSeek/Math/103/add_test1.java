// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testExtremeLargeDeviations() throws Exception {
    NormalDistribution distribution = (NormalDistribution) getDistribution();
    distribution.setMean(0);
    distribution.setStandardDeviation(1);
    double x = 1000.0;
    double prob = distribution.cumulativeProbability(x);
    assertTrue(prob > 0.999999);
    x = -1000.0;
    prob = distribution.cumulativeProbability(x);
    assertTrue(prob < 0.000001);
    distribution.setMean(5.0);
    distribution.setStandardDeviation(0.1);
    double zThreshold = 6.0 * distribution.getStandardDeviation() * Math.sqrt(2.0);
    x = distribution.getMean() + 10 * zThreshold;
    prob = distribution.cumulativeProbability(x);
    assertEquals(1.0, prob, 0.0);
    x = distribution.getMean() - 10 * zThreshold;
    prob = distribution.cumulativeProbability(x);
    assertEquals(0.0, prob, 0.0);
}
