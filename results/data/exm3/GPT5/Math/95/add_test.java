// org/apache/commons/math/distribution/FDistributionTest.java
public void testSmallDegreesOfFreedomDifferentParams() throws Exception {
        org.apache.commons.math.distribution.FDistributionImpl fd =
            new org.apache.commons.math.distribution.FDistributionImpl(5.0, 2.0);
        double x = 0.5;
        double p = fd.cumulativeProbability(x);
        double xBack = fd.inverseCumulativeProbability(p);
        assertEquals(x, xBack, 1.0e-6);
    }