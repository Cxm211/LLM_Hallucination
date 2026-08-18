// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testNaNValues() throws Exception {
    NormalDistribution distribution = new NormalDistributionImpl(0, 1);
    assertTrue(Double.isNaN(distribution.cumulativeProbability(Double.NaN)));
}