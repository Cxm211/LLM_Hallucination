// org/apache/commons/math/distribution/FDistributionTest.java
public void testInverseForHighValue() throws Exception {
        org.apache.commons.math.distribution.FDistributionImpl fd =
            new org.apache.commons.math.distribution.FDistributionImpl(2.0, 5.0);
        double v = 10.0;
        double p = fd.cumulativeProbability(v);
        double x = fd.inverseCumulativeProbability(p);
        assertEquals(v, x, 1.0e-5);
    }
