// org/apache/commons/math/distribution/FDistributionTest.java
public void testGetInitialDomainWithLargeDenominatorDF() throws Exception {
    org.apache.commons.math.distribution.FDistributionImpl fd =
        new org.apache.commons.math.distribution.FDistributionImpl(5.0, 10.0);
    double p = fd.cumulativeProbability(1.5);
    double x = fd.inverseCumulativeProbability(p);
    assertEquals(1.5, x, 1.0e-5);
}