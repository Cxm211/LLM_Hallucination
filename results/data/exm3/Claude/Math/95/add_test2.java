// org/apache/commons/math/distribution/FDistributionTest.java
public void testGetInitialDomainWithDenominatorDFBetweenOneAndTwo() throws Exception {
    org.apache.commons.math.distribution.FDistributionImpl fd =
        new org.apache.commons.math.distribution.FDistributionImpl(4.0, 1.5);
    double p = fd.cumulativeProbability(1.8);
    double x = fd.inverseCumulativeProbability(p);
    assertEquals(1.8, x, 1.0e-5);
}