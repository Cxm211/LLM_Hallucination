// org/apache/commons/math/distribution/FDistributionTest.java
public void testInverseForLowValue() throws Exception {
        org.apache.commons.math.distribution.FDistributionImpl fd =
            new org.apache.commons.math.distribution.FDistributionImpl(3.0, 4.0);
        double v = 0.1;
        double p = fd.cumulativeProbability(v);
        double x = fd.inverseCumulativeProbability(p);
        assertEquals(v, x, 1.0e-5);
    }
