// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testZeroStandardDeviation() throws Exception {
    NormalDistribution distribution = (NormalDistribution) getDistribution();
    distribution.setMean(5.0);
    distribution.setStandardDeviation(0.0);
    
    // Test value less than mean
    double lowerProb = distribution.cumulativeProbability(3.0);
    assertEquals(0.0, lowerProb, 0.0);
    
    // Test value greater than mean
    double upperProb = distribution.cumulativeProbability(7.0);
    assertEquals(1.0, upperProb, 0.0);
    
    // Test value equal to mean
    double equalProb = distribution.cumulativeProbability(5.0);
    assertEquals(0.5, equalProb, 0.0);
}