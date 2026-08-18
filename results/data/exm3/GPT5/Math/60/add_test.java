// org/apache/commons/math/distribution/NormalDistributionTest.java::testNaNInput
public void testNaNInput() throws Exception {
        NormalDistribution distribution = new NormalDistributionImpl(0, 1);
        double v = distribution.cumulativeProbability(Double.NaN);
        assertTrue(Double.isNaN(v));
    }