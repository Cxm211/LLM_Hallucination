// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testLargeStandardDeviation() throws Exception {
    NormalDistribution distribution = (NormalDistribution) getDistribution();
    distribution.setMean(0.0);
    distribution.setStandardDeviation(100.0);
    
    // With large std dev, values closer to mean should be near 0.5
    double prob10 = distribution.cumulativeProbability(10.0);
    assertTrue(prob10 > 0.5);
    assertTrue(prob10 < 0.6);
    
    double probNeg10 = distribution.cumulativeProbability(-10.0);
    assertTrue(probNeg10 < 0.5);
    assertTrue(probNeg10 > 0.4);
}