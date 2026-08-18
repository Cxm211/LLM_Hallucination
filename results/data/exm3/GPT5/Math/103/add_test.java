// org/apache/commons/math/distribution/NormalDistributionTest.java::testExtremeValues
public void testInvalidStandardDeviation() throws Exception {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        distribution.setMean(0);
        distribution.setStandardDeviation(-1);
        try {
            distribution.cumulativeProbability(0);
            fail("Expected MathException");
        } catch (MathException ex) {
            // expected
        }
    }