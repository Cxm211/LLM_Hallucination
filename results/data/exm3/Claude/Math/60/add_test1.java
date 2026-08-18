// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testVeryLargeDeviation() throws Exception {
    NormalDistribution distribution = new NormalDistributionImpl(100, 1);
    assertEquals(0.0, distribution.cumulativeProbability(-100), 0);
    assertEquals(1.0, distribution.cumulativeProbability(300), 0);
}