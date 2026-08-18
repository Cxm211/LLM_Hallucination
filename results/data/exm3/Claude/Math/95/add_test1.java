// org/apache/commons/math/distribution/FDistributionTest.java
public void testGetInitialDomainWithDenominatorDFExactlyTwo() throws Exception {
    org.apache.commons.math.distribution.FDistributionImpl fd =
        new org.apache.commons.math.distribution.FDistributionImpl(3.0, 2.0);
    double p = fd.cumulativeProbability(2.5);
    double x = fd.inverseCumulativeProbability(p);
    assertEquals(2.5, x, 1.0e-5);
}