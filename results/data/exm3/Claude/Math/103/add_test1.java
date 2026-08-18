// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testNegativeMeanExtremeValues() throws Exception {
    NormalDistribution distribution = (NormalDistribution) getDistribution();
    distribution.setMean(-50.0);
    distribution.setStandardDeviation(10.0);
    
    // Test far left tail
    double leftTail = distribution.cumulativeProbability(-150.0);
    assertTrue(leftTail > 0.0);
    assertTrue(leftTail < 0.00001);
    
    // Test far right tail
    double rightTail = distribution.cumulativeProbability(50.0);
    assertTrue(rightTail < 1.0);
    assertTrue(rightTail > 0.99999);
}