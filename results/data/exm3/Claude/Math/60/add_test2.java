// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testNonStandardDistributionExtremes() throws Exception {
    NormalDistribution distribution = new NormalDistributionImpl(50, 10);
    assertEquals(0.0, distribution.cumulativeProbability(Double.NEGATIVE_INFINITY), 0);
    assertEquals(1.0, distribution.cumulativeProbability(Double.POSITIVE_INFINITY), 0);
    assertTrue(distribution.cumulativeProbability(-1000) < 0.00001);
    assertTrue(distribution.cumulativeProbability(1000) > 0.99999);
}